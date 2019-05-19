package com.wanfangdata.titan.common;


import com.wanfangdata.titan.filters.TitanFilter;

/**
 * 提供来自给定类的TitanFilter实例的接口
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public interface IFilterFactory {

    public TitanFilter newInstance(Class clazz) throws Exception;
}