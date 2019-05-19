package com.wanfangdata.titan.context;

import java.io.InputStream;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wanfangdata.titan.common.TitanHeaders;
import com.wanfangdata.titan.utils.DeepCopy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.util.Pair;


/**
 * 请求上下文 保存请求、响应、状态信息和数据，以便titanfilters访问和共享
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class RequestContext extends ConcurrentHashMap<String, Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContext.class);

    protected static Class<? extends RequestContext> contextClass = RequestContext.class;

    private static RequestContext testContext = null;

    protected static final ThreadLocal<? extends RequestContext> threadLocal = new ThreadLocal<RequestContext>() {
        @Override
        protected RequestContext initialValue() {
            try {
                return contextClass.newInstance();
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    };

    public RequestContext() {
        super();
    }

    public static void setContextClass(Class<? extends RequestContext> clazz) {
        contextClass = clazz;
    }

    public static void testSetCurrentContext(RequestContext context) {
        testContext = context;
    }

    public static RequestContext getCurrentContext() {
        if (testContext != null)
            return testContext;

        RequestContext context = threadLocal.get();
        return context;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultResponse) {
        Boolean b = (Boolean) get(key);
        if (b != null) {
            return b.booleanValue();
        }
        return defaultResponse;
    }

    public void set(String key) {
        put(key, Boolean.TRUE);
    }

    public void set(String key, Object value) {
        if (value != null) {
            put(key, value);
        } else {
            remove(key);
        }
    }

    public boolean getTitanEngineRan() {
        return getBoolean("titanEngineRan");
    }

    public void setTitanEngineRan() {
        put("TitanEngineRan", true);
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) get("request");
    }

    public void setRequest(HttpServletRequest request) {
        put("request", request);
    }

    public HttpServletResponse getResponse() {
        return (HttpServletResponse) get("response");
    }

    public void setResponse(HttpServletResponse response) {
        set("response", response);
    }

    public Throwable getThrowable() {
        return (Throwable) get("throwable");

    }

    public void setThrowable(Throwable th) {
        put("throwable", th);

    }

    /**
     * 设置 debugRouting
     *
     * @param bDebug
     */
    public void setDebugRouting(boolean bDebug) {
        set("debugRouting", bDebug);
    }

    public boolean debugRouting() {
        return getBoolean("debugRouting");
    }

    /**
     * 设置 "debugRequestHeadersOnly" 到 bHeadersOnly
     *
     * @param bHeadersOnly
     */
    public void setDebugRequestHeadersOnly(boolean bHeadersOnly) {
        set("debugRequestHeadersOnly", bHeadersOnly);

    }

    public boolean debugRequestHeadersOnly() {
        return getBoolean("debugRequestHeadersOnly");
    }

    /**
     * 设置 "debugRequest"
     *
     * @param bDebug
     */
    public void setDebugRequest(boolean bDebug) {
        set("debugRequest", bDebug);
    }

    public boolean debugRequest() {
        return getBoolean("debugRequest");
    }

    /**
     * 剔除 "routeUrl" key
     */
    public void removeRouteUrl() {
        remove("routeUrl");
    }

    /**
     * 设置请求将发送到的RouteURL
     *
     * @param routeUrl a URL
     */
    public void setRouteUrl(URL routeUrl) {
        set("routeUrl", routeUrl);
    }

    public URL getRouteUrl() {
        return (URL) get("routeUrl");
    }

    /**
     * 将此路由命名为一个名称。可能用于速率限制或统计。
     *
     * @param name
     */
    public void setRouteName(String name) {
        set("routeName", name);
    }

    public String getRouteName() {
        return (String) get("routeName");
    }

    /**
     * 将此路由命名为一个名称。可能用于速率限制或统计。
     *
     * @param name
     */
    public void setServiceName(String name) {
        set("serviceName", name);
    }

    public String getServiceName() {
        return (String) get("serviceName");
    }

    /**
     * 返回“路由”。这是一个Titan定义的用于收集请求度量的bucket。默认情况下，路由是URI的第一段例如/get/my/stufacture:route是“get”
     *
     * @return
     */
    public String getRoute() {
        return (String) get("route");
    }

    public void setRoute(String r) {
        set("route", r);
    }

    /**
     * 设置此路由所属的组名。可能用于速率限制或统计数据
     *
     * @param name
     */
    public void setRouteGroup(String name) {
        set("routeGroup", name);
    }

    /**
     * 如果以前设置过，则返回此路由的组名。
     *
     * @return
     */
    public String getRouteGroup() {
        return (String) get("routeGroup");
    }

    /**
     * 将过滤器名称和状态附加到当前请求
     *
     * @param name
     * @param status
     * @param time
     */
    public void addFilterExecutionSummary(String name, String status, long time) {
        StringBuilder sb = getFilterExecutionSummary();
        if (sb.length() > 0)
            sb.append(", ");
        sb.append(name).append('[').append(status).append(']').append('[').append(time).append("ms]");
    }

    /**
     * 当前请求
     */
    public StringBuilder getFilterExecutionSummary() {
        if (get("executedFilters") == null) {
            putIfAbsent("executedFilters", new StringBuilder());
        }
        return (StringBuilder) get("executedFilters");
    }

    /**
     * 设置responseBody
     *
     * @param body
     */
    public void setResponseBody(String body) {
        set("responseBody", body);
    }

    public String getResponseBody() {
        return (String) get("responseBody");
    }

    /**
     * 设置响应数据
     *
     * @param responseDataStream
     */
    public void setResponseDataStream(InputStream responseDataStream) {
        set("responseDataStream", responseDataStream);
    }

    /**
     * 如果响应是gzip，则设置标志response gzipped
     *
     * @param gzipped
     */
    public void setResponseGZipped(boolean gzipped) {
        put("responseGZipped", gzipped);
    }

    public boolean getResponseGZipped() {
        return getBoolean("responseGZipped", true);
    }

    /**
     * 如果出现异常，设置错误处理标志。
     *
     * @param handled
     */
    public void setErrorHandled(boolean handled) {
        put("errorHandled", handled);
    }

    public boolean errorHandled() {
        return getBoolean("errorHandled", false);
    }

    public InputStream getResponseDataStream() {
        return (InputStream) get("responseDataStream");
    }

    /**
     * 如果此值为true则响应会发送到客户端
     *
     * @return
     */
    public boolean sendTitanResponse() {
        return getBoolean("sendTitanResponse", true);
    }

    /**
     * 设置是否发送响应
     *
     * @param bSend
     */
    public void setSendTitanResponse(boolean bSend) {
        set("sendTitanResponse", Boolean.valueOf(bSend));
    }

    /**
     * 返回响应状态码 默认 200
     *
     * @return
     */
    public int getResponseStatusCode() {
        return get("responseStatusCode") != null ? (Integer) get("responseStatusCode") : 500;
    }

    public void setResponseStatusCode(int nStatusCode) {
        getResponse().setStatus(nStatusCode);
        set("responseStatusCode", nStatusCode);
    }

    /**
     * 为目标增加一个http header
     *
     * @param name
     * @param value
     */
    public void addTitanRequestHeader(String name, String value) {
        getTitanRequestHeaders().put(name.toLowerCase(), value);
    }

    /**
     * 返回要发送到目标的请求头列表
     */
    public Map<String, String> getTitanRequestHeaders() {
        if (get("TitanRequestHeaders") == null) {
            HashMap<String, String> TitanRequestHeaders = new HashMap<String, String>();
            putIfAbsent("TitanRequestHeaders", TitanRequestHeaders);
        }
        return (Map<String, String>) get("TitanRequestHeaders");
    }

    /**
     * 添加一个响应头
     *
     * @param name
     * @param value
     */
    public void addTitanResponseHeader(String name, String value) {
        getTitanResponseHeaders().add(new Pair<String, String>(name, value));
    }

    /**
     * 返回当前响应头列表
     *
     * @return a List<Pair<String, String>>
     */
    public List<Pair<String, String>> getTitanResponseHeaders() {
        if (get("TitanResponseHeaders") == null) {
            List<Pair<String, String>> TitanRequestHeaders = new ArrayList<Pair<String, String>>();
            putIfAbsent("TitanResponseHeaders", TitanRequestHeaders);
        }
        return (List<Pair<String, String>>) get("TitanResponseHeaders");
    }

    /**
     * 原始响应头
     *
     * @return the List<Pair<String, String>>
     */
    public List<Pair<String, String>> getOriginResponseHeaders() {
        if (get("originResponseHeaders") == null) {
            List<Pair<String, String>> originResponseHeaders = new ArrayList<Pair<String, String>>();
            putIfAbsent("originResponseHeaders", originResponseHeaders);
        }
        return (List<Pair<String, String>>) get("originResponseHeaders");
    }

    /**
     * 向原始响应头添加头
     *
     * @param name
     * @param value
     */
    public void addOriginResponseHeader(String name, String value) {
        getOriginResponseHeaders().add(new Pair<String, String>(name, value));
    }

    public void addOriginResponseHeader(List<Pair<String, String>> headers) {
        getOriginResponseHeaders().addAll(headers);
    }

    public Integer getOriginContentLength() {
        return (Integer) get("originContentLength");
    }

    /**
     * 从原始响应设置内容长度
     *
     * @param v
     */
    public void setOriginContentLength(Integer v) {
        set("originContentLength", v);
    }

    public void setOriginContentLength(String v) {
        try {
            final Integer i = Integer.valueOf(v);
            set("originContentLength", i);
        } catch (NumberFormatException e) {
            LOGGER.warn("error parsing origin content length", e);
        }
    }

    /**
     * @return 如果请求主体被Chunked，则为true
     */
    public boolean isChunkedRequestBody() {
        final Object v = get("chunkedRequestBody");
        return (v != null) ? (Boolean) v : false;
    }

    public void setChunkedRequestBody() {
        this.set("chunkedRequestBody", Boolean.TRUE);
    }

    /**
     * @return true是客户端请求可以接受gzip编码, 检查 "accept-encoding" 请求头
     */
    public boolean isGzipRequested() {
        final String requestEncoding = this.getRequest().getHeader(TitanHeaders.ACCEPT_ENCODING);
        return requestEncoding != null && requestEncoding.toLowerCase().contains("gzip");
    }

    /**
     * 取消设置线程本地上下文。在请求结束时完成。
     */
    public void unset() {
        threadLocal.remove();
    }

    /**
     * 复制请求上下文。这是用来排错的
     *
     * @return
     */
    public RequestContext copy() {
        RequestContext copy = new RequestContext();
        Iterator<String> it = this.keySet().iterator();
        String key = it.next();
        while (key != null) {
            Object orig = get(key);
            try {
                Object copyValue = DeepCopy.copy(orig);
                if (copyValue != null) {
                    copy.set(key, copyValue);
                } else {
                    copy.set(key, orig);
                }
            } catch (NotSerializableException e) {
                copy.set(key, orig);
            }
            if (it.hasNext()) {
                key = it.next();
            } else {
                key = null;
            }
        }
        return copy;
    }


    public Map<String, List<String>> getRequestQueryParams() {
        return (Map<String, List<String>>) get("requestQueryParams");
    }

    /**
     * 设置请求查询参数列表
     *
     * @param qp Map<String, List<String>> qp
     */
    public void setRequestQueryParams(Map<String, List<String>> qp) {
        put("requestQueryParams", qp);
    }
}
