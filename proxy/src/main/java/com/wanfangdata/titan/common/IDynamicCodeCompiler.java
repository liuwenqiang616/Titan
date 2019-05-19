package com.wanfangdata.titan.common;

import java.io.File;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public interface IDynamicCodeCompiler {
    Class compile(String sCode, String sName) throws Exception;

    Class compile(File file) throws Exception;
}