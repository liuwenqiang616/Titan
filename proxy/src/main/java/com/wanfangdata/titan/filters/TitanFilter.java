package com.wanfangdata.titan.filters;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.wanfangdata.titan.common.ExecutionStatus;
import com.wanfangdata.titan.common.ITitanFilter;
import com.wanfangdata.titan.common.TitanFilterResult;

public abstract class TitanFilter implements ITitanFilter, Comparable<TitanFilter> {

	private final DynamicBooleanProperty filterDisabled = DynamicPropertyFactory.getInstance()
			.getBooleanProperty(disablePropertyName(), false);

	abstract public String filterType();

	abstract public int filterOrder();

	public boolean isStaticFilter() {
		return true;
	}

	public String disablePropertyName() {
		return "titan." + this.getClass().getSimpleName() + "." + filterType() + ".disable";
	}

	public boolean isFilterDisabled() {
		return filterDisabled.get();
	}

	public TitanFilterResult runFilter() {
		TitanFilterResult tr = new TitanFilterResult();

		if (!filterDisabled.get()) {		    
			if (shouldFilter()) {
				try {
					Object res = run();
					tr.setStatus(ExecutionStatus.SUCCESS);
					tr.setResult(res);
				} catch (Throwable t) {
					tr.setException(t);
					tr.setStatus(ExecutionStatus.FAILED);
				}
			} else {
				tr.setStatus(ExecutionStatus.SKIPPED);
			}
		}

		return tr;
	}

	public int compareTo(TitanFilter filter) {
		return this.filterOrder() - filter.filterOrder();
	}
}
