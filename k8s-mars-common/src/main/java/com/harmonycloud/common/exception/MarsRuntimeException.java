package com.harmonycloud.common.exception;

import com.harmonycloud.common.enumm.ErrorCodeMessage;

public class MarsRuntimeException extends RuntimeException {

    private Integer errorCode;
    private String errorName;
    private String errorMessage;

    private static final long serialVersionUID = 1L;

    public MarsRuntimeException() {
    }

    public MarsRuntimeException(String message) {
        super(message);
    }

    public MarsRuntimeException(Integer errorCode) {
        this.errorCode = errorCode;
    }

    public MarsRuntimeException(Throwable cause) {
        super(cause);
    }

    public MarsRuntimeException(ErrorCodeMessage error){
         this(error,"",false);
    }

    public MarsRuntimeException(String extendMessage, ErrorCodeMessage error) {
        this.errorCode = error.value();
        this.errorName = error.name();
        this.errorMessage = ErrorCodeMessage.getMessageWithLanguage(error,extendMessage,true);
    }

    public MarsRuntimeException(ErrorCodeMessage error, String extendMessage) {
        this.errorCode = error.value();
        this.errorName = error.name();
        this.errorMessage = ErrorCodeMessage.getMessageWithLanguage(error,extendMessage,false);
    }

    public MarsRuntimeException(ErrorCodeMessage error, String extendMessage, boolean prefix) {
        this.errorCode = error.value();
        this.errorName = error.name();
        this.errorMessage = ErrorCodeMessage.getMessageWithLanguage(error,extendMessage,prefix);
    }

    public MarsRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
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
