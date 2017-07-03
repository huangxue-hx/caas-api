package com.harmonycloud.common.exception;

public class K8sAuthException extends Exception {
	
	private static final long serialVersionUID = 1L; 
	
	public K8sAuthException() {  
    }  
  
    public K8sAuthException(String message) {  
        super(message);  
    }  
  
    public K8sAuthException(Throwable cause) {  
        super(cause);  
    }  
  
    public K8sAuthException(String message, Throwable cause) {  
        super(message, cause);  
    }  

}
