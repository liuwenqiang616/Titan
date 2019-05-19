package com.wanfangdata.titan.groovy;

import com.wanfangdata.titan.common.IDynamicCodeCompiler;
import groovy.lang.GroovyClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Groovy 代码编译器
 *
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class GroovyCompiler implements IDynamicCodeCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyCompiler.class);

    /**
     * 编译Groovy代码并返回编译代码的类。
     *
     * @param sCode
     * @param sName
     * @return
     */
    public Class compile(String sCode, String sName) {
        GroovyClassLoader loader = getGroovyClassLoader();
        LOGGER.warn("Compiling filter: " + sName);
        Class groovyClass = loader.parseClass(sCode, sName);
        return groovyClass;
    }

    GroovyClassLoader getGroovyClassLoader() {
        return new GroovyClassLoader();
    }

    /**
     * 从文件中编译groovy类
     *
     * @param file
     * @return
     * @throws IOException
     */
    public Class compile(File file) throws IOException {
        GroovyClassLoader loader = getGroovyClassLoader();
        Class groovyClass = loader.parseClass(file);
        return groovyClass;
    }

}
