package com.wanfangdata.titan.common;

/**
 * @ClassName InitializeServletListener
 * @Author liuwq
 * @Date 2019/5/19 15:17
 * @Version 1.0
 **/
public final class TitanFilterResult {

    private Object result;
    private Throwable exception;
    private ExecutionStatus status;

    public TitanFilterResult(Object result, ExecutionStatus status) {
        this.result = result;
        this.status = status;
    }

    public TitanFilterResult(ExecutionStatus status) {
        this.status = status;
    }

    public TitanFilterResult() {
        this.status = ExecutionStatus.DISABLED;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

}
