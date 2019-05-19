package com.wanfangdata.titan.common;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public interface ITitanFilter {

    /**
     * 此方法的“true”返回意味着应该调用run（）方法
     */
    boolean shouldFilter();

    /**
     * 如果shouldFilter（）为true，则将调用此方法。这种方法是Titanfilter的核心方法
     */
    Object run() throws TitanException;

}