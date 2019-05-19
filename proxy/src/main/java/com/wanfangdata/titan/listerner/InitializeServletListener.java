package com.wanfangdata.titan.listerner;

import com.netflix.config.ConfigurationManager;
import com.wanfangdata.titan.common.Constants;
import com.wanfangdata.titan.core.FilterFileManager;
import com.wanfangdata.titan.filters.FilterLoader;
import com.wanfangdata.titan.groovy.GroovyCompiler;
import com.wanfangdata.titan.groovy.GroovyFileFilter;
import org.apache.commons.configuration.AbstractConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
@WebListener
public class InitializeServletListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializeServletListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            initTitan();
        } catch (Exception e) {
            LOGGER.error("初始化titan网关出错", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private void initTitan() throws Exception {
        LOGGER.info("开启 Groovy Filter 文件管理");
        final AbstractConfiguration config = ConfigurationManager.getConfigInstance();
        final String preFiltersPath = config.getString(Constants.TITAN_FILTER_PRE_PATH);
        final String postFiltersPath = config.getString(Constants.TITAN_FILTER_POST_PATH);
        final String routeFiltersPath = config.getString(Constants.TITAN_FILTER_ROUTE_PATH);
        final String errorFiltersPath = config.getString(Constants.TITAN_FILTER_ERROR_PATH);
        final String customPath = config.getString(Constants.TITAN_FILTER_CUSTOM_PATH);
        final String titanNodeName = config.getString(Constants.TITAN_FILTERS_CONFIG_NAME);

        FilterLoader.getInstance().setCompiler(new GroovyCompiler());
        FilterFileManager.setFilenameFilter(new GroovyFileFilter());

        FilterFileManager.init(titanNodeName, preFiltersPath, postFiltersPath, routeFiltersPath, errorFiltersPath, customPath);
        LOGGER.info("Groovy Filter 文件管理启动完毕");
    }
}
