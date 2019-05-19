package com.wanfangdata.titan.filters;

import com.wanfangdata.titan.common.IDynamicCodeCompiler;
import com.wanfangdata.titan.common.IFilterFactory;
import com.wanfangdata.titan.groovy.GroovyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类是titan的核心类之一。它编译、从文件加载并检查源代码是否更改。
 * 它还按过滤器类型保存titanfilters。
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class FilterLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterLoader.class);

    private final static FilterLoader instance = new FilterLoader();

    private static IDynamicCodeCompiler COMPILER;
    private static IFilterFactory FILTER_FACTORY = new DefaultFilterFactory();

    private FilterRegistry filterRegistry = FilterRegistry.instance();
    private final ConcurrentHashMap<String, String> filterClassCode = new ConcurrentHashMap<String, String>();
    private final ConcurrentHashMap<String, String> filterCheck = new ConcurrentHashMap<String, String>();
    private final ConcurrentHashMap<String, List<TitanFilter>> hashFiltersByType = new ConcurrentHashMap<String, List<TitanFilter>>();

    public FilterLoader() {
//    	filterRegistry.put("filter3----", new InternalExecuteRoute());
    }

    /**
     * 设置动态代码编译器
     *
     * @param compiler
     */
    public void setCompiler(IDynamicCodeCompiler compiler) {
        COMPILER = compiler;
    }

    // overidden by tests
    public void setFilterRegistry(FilterRegistry r) {
        this.filterRegistry = r;
    }

    /**
     * 设置filter工厂
     *
     * @param factory
     */
    public void setFilterFactory(IFilterFactory factory) {
        FILTER_FACTORY = factory;
    }

    /**
     * @return 单例的filter加载器
     */
    public static FilterLoader getInstance() {
        return instance;
    }

    /**
     * 如果给定的源和名称检测到过滤器代码已更改或过滤器不存在。否则，它将返回请求的TitanFilter的实例
     *
     * @param sCode source code
     * @param sName name of the filter
     * @return the TitanFilter
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public TitanFilter getFilter(String sCode, String sName) throws Exception {

        if (filterCheck.get(sName) == null) {
            filterCheck.putIfAbsent(sName, sName);
            if (!sCode.equals(filterClassCode.get(sName))) {
                LOGGER.info("reloading code " + sName);
                filterRegistry.remove(sName);
            }
        }
        TitanFilter filter = filterRegistry.get(sName);
        if (filter == null) {
            Class clazz = COMPILER.compile(sCode, sName);
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = (TitanFilter) FILTER_FACTORY.newInstance(clazz);
            }
        }
        return filter;

    }

    /**
     * @return titan过滤器总数
     */
    public int filterInstanceMapSize() {
        return filterRegistry.size();
    }

    /**
     * 从一个文件中，它将读取TitanFilter源代码，编译它，并将其添加到当前筛选器列表中。
     *
     * @param file
     * @return 如果文件中的筛选器成功读取、编译、验证并添加到titan，则为true。
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     */
    public boolean putFilter(GroovyFile file) throws Exception {
        String root = file.getRoot();
        root = root.endsWith("/") ? root.substring(0, root.length() - 1) : root;
        String sName = root + "/" + file.getName();
        TitanFilter filter = filterRegistry.get(sName);
        if (filter == null) {
            Class clazz = COMPILER.compile(file.getContent(),file.getName());
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = (TitanFilter) FILTER_FACTORY.newInstance(clazz);
                filterRegistry.put(sName, filter);
                List<TitanFilter> list = hashFiltersByType.get(filter.filterType());
                if (list != null) {
                    hashFiltersByType.remove(filter.filterType()); //rebuild this list
                }
                return true;
            }
        }

        return false;
    }


    /**
     * 按指定的筛选器类型返回筛选器列表
     *
     * @param filterType
     * @return a List<TitanFilter>
     */
    public List<TitanFilter> getFiltersByType(String filterType) {

        List<TitanFilter> list = hashFiltersByType.get(filterType);
        if (list != null) return list;

        list = new ArrayList<TitanFilter>();

        Collection<TitanFilter> filters = filterRegistry.getAllFilters();
        for (Iterator<TitanFilter> iterator = filters.iterator(); iterator.hasNext(); ) {
            TitanFilter filter = iterator.next();
            if (filter.filterType().equals(filterType)) {
                list.add(filter);
            }
        }
        Collections.sort(list); // sort by priority

        hashFiltersByType.putIfAbsent(filterType, list);
        return list;
    }
}
