package com.wanfangdata.titan.common;

/**
 * Titan 网关访问或添加的HTTP头
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class TitanHeaders {
    public static final String TRANSFER_ENCODING = "transfer-encoding";
    public static final String CHUNKED = "chunked";
    public static final String CONTENT_ENCODING = "content-encoding";
    public static final String CONTENT_LENGTH = "content-length";
    public static final String ACCEPT_ENCODING = "accept-encoding";
    public static final String CONNECTION = "connection";
    public static final String KEEP_ALIVE = "keep-alive";
    public static final String HOST = "host";
    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
}