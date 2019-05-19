package com.wanfangdata.titan.common;

/**
 * Titan中处理的所有异常都是titan exceptions
 */
public class TitanException extends Exception {
    public int nStatusCode;
    public String errorCause;

    public TitanException(Throwable throwable, String sMessage, int nStatusCode, String errorCause) {
        super(sMessage, throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }

    public TitanException(String sMessage, int nStatusCode, String errorCause) {
        super(sMessage);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }

    public TitanException(Throwable throwable, int nStatusCode, String errorCause) {
        super(throwable.getMessage(), throwable);
        this.nStatusCode = nStatusCode;
        this.errorCause = errorCause;
    }
}
