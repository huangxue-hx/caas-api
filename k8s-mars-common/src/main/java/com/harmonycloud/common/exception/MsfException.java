package com.harmonycloud.common.exception;

import com.harmonycloud.common.enumm.MicroServiceCodeMessage;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2018-3-2
 * @Modified
 */
public class MsfException extends RuntimeException {

    private String errorCode;
    private String errorName;
    private String errorMessage;

    private static final long serialVersionUID = 1L;

    public MsfException() {
    }

    public MsfException(String errorMessage) {
        super(errorMessage);
    }

    public MsfException(Throwable cause) {
        super(cause);
    }

    public MsfException(MicroServiceCodeMessage error) {
        this.errorCode = error.value();
        this.errorName = error.name();
        this.errorMessage = error.getMessage();
    }

    public MsfException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorName() {
        return errorName;
    }

    public void setErrorName(String errorName) {
        this.errorName = errorName;
    }

}
