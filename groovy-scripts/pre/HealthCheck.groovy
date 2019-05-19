package scripts.pre

import com.wanfangdata.titan.context.RequestContext
import com.wanfangdata.titan.filters.TitanFilter

import javax.servlet.http.HttpServletResponse

public class HealthCheck extends TitanFilter{
	@Override
	public String filterType() {
		return "pre";
	}
	
	public Object uri() {
		return "/healthcheck";
	}
	
	@Override
	boolean shouldFilter() {
		String path = RequestContext.currentContext.getRequest().getRequestURI()
		return path.equalsIgnoreCase(uri())||path.toLowerCase().endsWith(uri());
	}
	
	public int filterOrder(){
		return 0;
	}
	
	public String responseBody() {
		return "<health>ok</health>";
	}
	
	@Override
	Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		// Set the default response code for static filters to be 200
		ctx.getResponse().setStatus(HttpServletResponse.SC_OK);
		ctx.getResponse().setContentType('application/xml')
		// first StaticResponseFilter instance to match wins, others do not set body and/or status
		if (ctx.getResponseBody() == null) {
			ctx.setResponseBody(responseBody())
			ctx.sendTitanResponse = false;
		}
	}
}
