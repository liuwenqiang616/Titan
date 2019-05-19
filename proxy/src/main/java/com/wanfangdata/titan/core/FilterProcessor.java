package com.wanfangdata.titan.core;

import com.netflix.servo.monitor.DynamicCounter;
import com.wanfangdata.titan.common.ExecutionStatus;
import com.wanfangdata.titan.common.IFilterUsageNotifier;
import com.wanfangdata.titan.common.TitanException;
import com.wanfangdata.titan.common.TitanFilterResult;
import com.wanfangdata.titan.context.RequestContext;
import com.wanfangdata.titan.filters.FilterLoader;
import com.wanfangdata.titan.filters.TitanFilter;
import com.wanfangdata.titan.utils.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class FilterProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterProcessor.class);

    private static FilterProcessor instance = new FilterProcessor();

    private IFilterUsageNotifier usageNotifier;

    protected FilterProcessor() {
        usageNotifier = new BasicFilterUsageNotifier();
    }

    public static FilterProcessor getInstance() {
        return instance;
    }

    /**
     * 在需要重写默认行为的情况下设置单例处理器
     *
     * @param processor
     */
    public static void setProcessor(FilterProcessor processor) {
        instance = processor;
    }

    /**
     * 覆盖默认的过滤器使用通知实现。
     *
     * @param notifier
     */
    public void setFilterUsageNotifier(IFilterUsageNotifier notifier) {
        this.usageNotifier = notifier;
    }


    /**
     * 运行在“post”过滤器之后调用的“post”过滤器。
     *
     * @throws TitanException
     */
    public void postRoute() throws TitanException {
        try {
            runFilters("post");
        } catch (Throwable e) {
            if (e instanceof TitanException) {
                throw (TitanException) e;
            }
            throw new TitanException(e, 500, "UNCAUGHT_EXCEPTION_IN_POST_FILTER_" + e.getClass().getName());
        }

    }

    /**
     * 运行所有“error”过滤器。只有在发生异常时才调用这些。
     *
     * @throws TitanException
     */
    public void error() throws TitanException {
        try {
            runFilters("error");
        } catch (Throwable e) {
            if (e instanceof TitanException) {
                throw (TitanException) e;
            }
            throw new TitanException(e, 500, "UNCAUGHT_EXCEPTION_IN_POST_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * 运行所有“route”过滤器。这些过滤器将调用路由到目标服务。
     *
     * @throws TitanException if an exception occurs.
     */
    public void route() throws TitanException {
        try {
            runFilters("route");
        } catch (Throwable e) {
            if (e instanceof TitanException) {
                throw (TitanException) e;
            }
            throw new TitanException(e, 500, "UNCAUGHT_EXCEPTION_IN_ROUTE_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * 运行所有“pre”过滤器。这些过滤器在路由到之前运行
     * orgin.
     *
     * @throws TitanException
     */
    public void preRoute() throws TitanException {
        try {
            runFilters("pre");
        } catch (Throwable e) {
            if (e instanceof TitanException) {
                throw (TitanException) e;
            }
            throw new TitanException(e, 500, "UNCAUGHT_EXCEPTION_IN_PRE_FILTER_" + e.getClass().getName());
        }
    }

    /**
     * 按类型运行自定义过滤器
     *
     * @param sType
     * @return
     * @throws Throwable throws up an arbitrary exception
     */
    public Object runFilters(String sType) throws Throwable {
        if (RequestContext.getCurrentContext().debugRouting()) {
            Debug.addRoutingDebug("Invoking {" + sType + "} type filters");
        }
        boolean bResult = false;
        List<TitanFilter> list = FilterLoader.getInstance().getFiltersByType(sType);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                TitanFilter titanFilter = list.get(i);
                Object result = processTitanFilter(titanFilter);
                if (result != null && result instanceof Boolean) {
                    bResult |= ((Boolean) result);
                }
            }
        }
        return bResult;
    }

    /**
     * 处理单个TitanFilter。此方法添加调试信息。
     *
     * @param filter
     * @return
     * @throws TitanException
     */
    public Object processTitanFilter(TitanFilter filter) throws TitanException {
        RequestContext ctx = RequestContext.getCurrentContext();
        boolean bDebug = ctx.debugRouting();
        long execTime = 0;
        String filterName = "";

        try {
            long ltime = System.currentTimeMillis();
            filterName = filter.getClass().getSimpleName();

            RequestContext copy = null;
            Object o = null;
            Throwable t = null;

            if (bDebug) {
                Debug.addRoutingDebug("Filter " + filter.filterType() + " " + filter.filterOrder() + " " + filterName);
                copy = ctx.copy();
            }

            TitanFilterResult result = filter.runFilter();
            ExecutionStatus s = result.getStatus();
            execTime = System.currentTimeMillis() - ltime;

            switch (s) {
                case FAILED:
                    t = result.getException();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                    break;
                case SUCCESS:
                    o = result.getResult();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.SUCCESS.name(), execTime);
                    if (bDebug) {
                        Debug.addRoutingDebug("Filter {" + filterName + " TYPE:" + filter.filterType() + " ORDER:" + filter.filterOrder() + "} Execution time = " + execTime + "ms");
                        Debug.compareContextState(filterName, copy);
                    }
                    break;
                default:
                    break;
            }
            if (t != null) throw t;

            usageNotifier.notify(filter, s);

            return o;
        } catch (Throwable e) {
            if (bDebug) {
                Debug.addRoutingDebug("Running Filter failed " + filterName + " type:" + filter.filterType() + " order:" + filter.filterOrder() + " " + e.getMessage());
            }

            usageNotifier.notify(filter, ExecutionStatus.FAILED);
            if (e instanceof TitanException) {
                throw (TitanException) e;
            } else {
                TitanException ex = new TitanException(e, "Filter threw Exception", 500, filter.filterType() + ":" + filterName);
                ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                throw ex;
            }
        }

    }


    /**
     * 每个过滤器在每次调用时 发布一个metric 计数
     */
    public static class BasicFilterUsageNotifier implements IFilterUsageNotifier {
        private static final String METRIC_PREFIX = "titan.filter-";

        @Override
        public void notify(TitanFilter filter, ExecutionStatus status) {
            DynamicCounter.increment(METRIC_PREFIX + filter.getClass().getSimpleName(), "status", status.name(), "filtertype", filter.filterType());
        }
    }

}

