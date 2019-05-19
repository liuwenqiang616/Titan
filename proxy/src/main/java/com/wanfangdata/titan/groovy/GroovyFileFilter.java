package com.wanfangdata.titan.groovy;

import com.wanfangdata.titan.common.FileFilter;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class GroovyFileFilter implements FileFilter{
    public boolean accept(String name) {
        return name.endsWith(".groovy");
    }
}