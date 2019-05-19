package com.wanfangdata.titan.core;

import com.wanfangdata.titan.common.TitanException;
import com.wanfangdata.titan.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * 此类将servlet请求和响应初始化为RequestContext
 * 并将filterProcessor调用包装到preroute（）、route（）、postRoute（）和 error（）方法
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class TitanRunner {

    public TitanRunner() {
    }

    /**
     * 设置httpservlet请求和httpresponse
     *
     * @param servletRequest
     * @param servletResponse
     */
    public void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        RequestContext.getCurrentContext().setRequest(new HttpServletRequestWrapper(servletRequest));
        RequestContext.getCurrentContext().setResponse(new HttpServletResponseWrapper(servletResponse));
    }

    /**
     * 执行过滤器类型为 "pre"  TitanFilters
     *
     * @throws TitanException
     */
    public void preRoute() throws TitanException {
        FilterProcessor.getInstance().preRoute();
    }

    /**
     * 执行过滤器类型为 "route"  TitanFilters
     *
     * @throws TitanException
     */
    public void route() throws TitanException {
        FilterProcessor.getInstance().route();
    }

    /**
     * 执行过滤器类型为 "post"  TitanFilters
     *
     * @throws TitanException
     */
    public void postRoute() throws TitanException {
        FilterProcessor.getInstance().postRoute();
    }

    /**
     * 执行过滤器类型为 "error"  TitanFilters
     *
     * @throws TitanException
     */
    public void error() throws TitanException {
        FilterProcessor.getInstance().error();
    }

}
