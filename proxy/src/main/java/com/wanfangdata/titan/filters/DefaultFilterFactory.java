package com.wanfangdata.titan.filters;


import com.wanfangdata.titan.common.IFilterFactory;

/**
 * 用于创建TitanFilter实例的默认工厂。
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class DefaultFilterFactory implements IFilterFactory {

    @Override
    public TitanFilter newInstance(Class clazz) throws InstantiationException, IllegalAccessException {
        return (TitanFilter) clazz.newInstance();
    }

}