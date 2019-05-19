package com.wanfangdata.titan.common;


import com.wanfangdata.titan.filters.TitanFilter;

/**
 * 用于在每次过滤注册回调的接口
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public interface IFilterUsageNotifier {
    public void notify(TitanFilter filter, ExecutionStatus status);
}
