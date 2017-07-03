package com.harmonycloud.common.exception;

public class MarsRuntimeException extends Exception {
	
	private static final long serialVersionUID = 1L; 
	
	public MarsRuntimeException() {  
    }  
  
    public MarsRuntimeException(String message) {  
        super(message);  
    }  
  
    public MarsRuntimeException(Throwable cause) {  
        super(cause);  
    }  
  
    public MarsRuntimeException(String message, Throwable cause) {  
        super(message, cause);  
    }  

}
