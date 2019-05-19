package com.wanfangdata.titan.listerner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class AsyncTitanListener implements AsyncListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTitanListener.class);

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
        LOGGER.error("Access {} timeout in AsyncServlet.", ((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        LOGGER.error("Error while access {} in AsyncServlet.", ((HttpServletRequest)event.getAsyncContext().getRequest()).getRequestURL());
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
    }
}