package com.wanfangdata.titan.servlet;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.Transaction;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicLongProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.wanfangdata.titan.common.CatContext;
import com.wanfangdata.titan.common.Constants;
import com.wanfangdata.titan.core.TitanCallable;
import com.wanfangdata.titan.core.TitanRunner;
import com.wanfangdata.titan.listerner.AsyncTitanListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
@WebServlet(urlPatterns = "/*",
        asyncSupported = true,
        initParams = {
                @WebInitParam(name = "resetEnable", value = "true")
        })
public class AsyncTitanServlet extends HttpServlet {
    private static final long serialVersionUID = 2723461074152665339L;

    private static Logger LOGGER = LoggerFactory.getLogger(AsyncTitanServlet.class);

    private DynamicIntProperty asyncTimeout = DynamicPropertyFactory.getInstance().getIntProperty(Constants.TITAN_SERVLET_ASYNC_TIMEOUT, 20000);
    private DynamicIntProperty coreSize = DynamicPropertyFactory.getInstance().getIntProperty(Constants.TITAN_THREADPOOL_CODE_SIZE, 200);
    private DynamicIntProperty maximumSize = DynamicPropertyFactory.getInstance().getIntProperty(Constants.TITAN_THREADPOOL_MAX_SIZE, 2000);
    private DynamicLongProperty aliveTime = DynamicPropertyFactory.getInstance().getLongProperty(Constants.TITAN_THREADPOOL_ALIVE_TIME, 1000 * 60 * 5);

    private TitanRunner titanRunner = new TitanRunner();
    private AtomicReference<ThreadPoolExecutor> poolExecutorRef = new AtomicReference<ThreadPoolExecutor>();
    private AtomicLong rejectedRequests = new AtomicLong(0);

    @Override
    public void init() throws ServletException {
        reNewThreadPool();
        Runnable c = new Runnable() {
            @Override
            public void run() {
                ThreadPoolExecutor p = poolExecutorRef.get();
                p.setCorePoolSize(coreSize.get());
                p.setMaximumPoolSize(maximumSize.get());
                p.setKeepAliveTime(aliveTime.get(), TimeUnit.MILLISECONDS);
            }
        };

        coreSize.addCallback(c);
        maximumSize.addCallback(c);
        aliveTime.addCallback(c);
    }

    private void reNewThreadPool() {
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(coreSize.get(), maximumSize.get(), aliveTime.get(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
        ThreadPoolExecutor old = poolExecutorRef.getAndSet(poolExecutor);
        if (old != null) {
            shutdownPoolExecutor(old);
        }
    }

    private void shutdownPoolExecutor(ThreadPoolExecutor old) {
        try {
            old.awaitTermination(5, TimeUnit.MINUTES);
            old.shutdown();
        } catch (InterruptedException e) {
            old.shutdownNow();
            LOGGER.error("Shutdown TITAN Thread Pool:", e);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Transaction tran = Cat.getProducer().newTransaction("AsyncTitanServlet", req.getRequestURL().toString());
        req.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(asyncTimeout.get());
        asyncContext.addListener(new AsyncTitanListener());
        try {
            Context ctx = new CatContext();
            Cat.logRemoteCallClient(ctx);
            poolExecutorRef.get().submit(new TitanCallable(ctx, asyncContext, titanRunner, req));
            tran.setStatus(Transaction.SUCCESS);
        } catch (RuntimeException e) {
            Cat.logError(e);
            tran.setStatus(e);
            rejectedRequests.incrementAndGet();
            throw e;
        } finally {
            tran.complete();
        }
    }


    @Override
    public void destroy() {
        shutdownPoolExecutor(poolExecutorRef.get());
    }
}
