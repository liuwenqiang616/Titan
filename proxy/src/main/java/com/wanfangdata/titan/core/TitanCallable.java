package com.wanfangdata.titan.core;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultMessageProducer;
import com.wanfangdata.titan.common.TitanException;
import com.wanfangdata.titan.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public class TitanCallable implements Callable {

	private static Logger LOGGER = LoggerFactory.getLogger(TitanCallable.class);

	private AsyncContext ctx;
	private TitanRunner titanRunner;
	private Context catCtx;
	private HttpServletRequest request;

	public TitanCallable(Context catContext, AsyncContext asyncContext, TitanRunner titanRunner,
						 HttpServletRequest request) {
		this.ctx = asyncContext;
		this.titanRunner = titanRunner;
		this.catCtx = catContext;
		this.request = request;
	}

	@Override
	public Object call() throws Exception {
		Cat.logRemoteCallServer(catCtx);
		RequestContext.getCurrentContext().unset();
		Transaction tran = Cat.getProducer().newTransaction("TitanCallable",
				request.getRequestURL().toString());
		RequestContext titanContext = RequestContext.getCurrentContext();
		long start = System.currentTimeMillis();
		try {
			service(ctx.getRequest(), ctx.getResponse());
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable t) {
			LOGGER.error("TitanCallable execute error.", t);
			Cat.logError(t);
			tran.setStatus(t);
		} finally {
			try {
				reportStat(titanContext, start);
			} catch (Throwable t) {
				Cat.logError("TitanCallable collect stats error.", t);
			}
			try {
				ctx.complete();
			} catch (Throwable t) {
				Cat.logError("AsyncContext complete error.", t);
			}
			titanContext.unset();

			tran.complete();
		}
		return null;
	}

	private void service(ServletRequest req, ServletResponse res) {
		try {

			init((HttpServletRequest) req, (HttpServletResponse) res);

			// 标记请求处理引擎为titan
			RequestContext.getCurrentContext().setTitanEngineRan();

			try {
				preRoute();
			} catch (TitanException e) {
				error(e);
				postRoute();
				return;
			}
			try {
				route();
			} catch (TitanException e) {
				error(e);
				postRoute();
				return;
			}
			try {
				postRoute();
			} catch (TitanException e) {
				error(e);
				return;
			}

		} catch (Throwable e) {
			error(new TitanException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()));
		}
	}

	/**
	 * 运行在“post”过滤器之后调用的“post”过滤器。
	 *
	 * @throws TitanException
	 */
	private void postRoute() throws TitanException {
		Transaction tran = Cat.getProducer().newTransaction("TitanCallable", "postRoute");
		try {
			titanRunner.postRoute();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * 运行所有“route”过滤器。这些过滤器将调用路由到目标服务。
	 *
	 * @throws TitanException
	 */
	private void route() throws TitanException {
		Transaction tran = Cat.getProducer().newTransaction("TitanCallable", "route");
		try {
			titanRunner.route();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * 运行所有“pre”过滤器。这些过滤器在路由到之前运行
	 *
	 * @throws TitanException
	 */
	private void preRoute() throws TitanException {
		Transaction tran = Cat.getProducer().newTransaction("TitanCallable", "preRoute");
		try {
			titanRunner.preRoute();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			tran.setStatus(e);
			throw e;
		} finally {
			tran.complete();
		}
	}

	/**
	 * 初始化request请求
	 *
	 * @param servletRequest
	 * @param servletResponse
	 */
	private void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
		titanRunner.init(servletRequest, servletResponse);
	}

	/**
	 * 运行所有“error”过滤器。只有在发生异常时才调用这些。
	 *
	 * @param e
	 */
	private void error(TitanException e) {
		Transaction tran = Cat.getProducer().newTransaction("TitanCallable", "errorRoute");
		try {
			RequestContext.getCurrentContext().setThrowable(e);
			titanRunner.error();
			tran.setStatus(Transaction.SUCCESS);
		} catch (Throwable t) {
			Cat.logError(t);
		} finally {
			tran.complete();
			Cat.logError(e);
		}
	}

	private void reportStat(RequestContext titanContext, long start) {

		long remoteServiceCost = 0l;
		Object remoteCallCost = titanContext.get("remoteCallCost");
		if (remoteCallCost != null) {
			try {
				remoteServiceCost = Long.parseLong(remoteCallCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientCost = 0l;
		Object sendResponseCost = titanContext.get("sendResponseCost");
		if (sendResponseCost != null) {
			try {
				replyClientCost = Long.parseLong(sendResponseCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientReadCost = 0L;
		Object sendResponseReadCost = titanContext.get("sendResponseCost:read");
		if (sendResponseReadCost != null) {
			try {
				replyClientReadCost = Long.parseLong(sendResponseReadCost.toString());
			} catch (Exception ignore) {
			}
		}

		long replyClientWriteCost = 0L;
		Object sendResponseWriteCost = titanContext.get("sendResponseCost:write");
		if (sendResponseWriteCost != null) {
			try {
				replyClientWriteCost = Long.parseLong(sendResponseWriteCost.toString());
			} catch (Exception ignore) {
			}
		}

			if (titanContext.sendTitanResponse()) {
			URL routeUrl = titanContext.getRouteUrl();
			if (routeUrl == null) {
				LOGGER.warn("Unknown Route: [ {" + titanContext
						.getRequest().getRequestURL() + "} ]");
			}
		}

		// TODO report metrics
	}
}
