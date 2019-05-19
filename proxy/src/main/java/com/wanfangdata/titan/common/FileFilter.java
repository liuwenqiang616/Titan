package com.wanfangdata.titan.common;

/**
 * @ClassName FileFilter
 * @Author liuwq
 * @Date 2019/5/19 16:24
 * @Version 1.0
 **/
@FunctionalInterface
public interface FileFilter {
    boolean accept(String name);
}
