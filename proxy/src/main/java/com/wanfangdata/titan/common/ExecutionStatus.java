package com.wanfangdata.titan.common;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public enum ExecutionStatus {

	SUCCESS(1), SKIPPED(-1), DISABLED(-2), FAILED(-3);

	private int status;

	ExecutionStatus(int status) {
		this.status = status;
	}
}