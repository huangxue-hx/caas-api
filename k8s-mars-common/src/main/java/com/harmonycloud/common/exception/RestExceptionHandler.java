package com.harmonycloud.common.exception;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.harmonycloud.common.util.ActionReturnUtil;


@ControllerAdvice
public class RestExceptionHandler{
	@Autowired
	private HttpSession session;
	private  Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final int CODE_LENGTH = 6;
	
    //运行时异常
    @ExceptionHandler(K8sAuthException.class)
    @ResponseBody
    @ResponseStatus(value=HttpStatus.UNAUTHORIZED)
    public void authExceptionHandler(Exception e) {
        logger.error(e.getMessage(),e);
    	 session.invalidate();
    }  
    //平台运行时非严重异常
    @ExceptionHandler(MarsRuntimeException.class)  
    @ResponseBody  
    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    public ActionReturnUtil nullPointerExceptionHandler(Exception e) {
        MarsRuntimeException exception =(MarsRuntimeException)e;
        String errorMessage = exception.getErrorMessage() == null?(e.getMessage()):exception.getErrorMessage();
        logger.warn(errorMessage,e);
        return ActionReturnUtil.returnErrorWithData(errorMessage);
    }


    
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    public ActionReturnUtil argumentExceptionHandler(Exception e) {
        logger.warn("invalid param",e);
        String errorMsg = e.getMessage();
        if(errorMsg != null && errorMsg.startsWith("[")){
            String code = errorMsg.substring(1,errorMsg.indexOf("]"));
            if(code!= null && code.length() == CODE_LENGTH){
                errorMsg = errorMsg.substring(errorMsg.indexOf("]")+1);
                return ActionReturnUtil.returnErrorWithCodeAndMsg(errorMsg.trim(),Integer.parseInt(code));
            }
        }
        return ActionReturnUtil.returnErrorWithData(e.getMessage());
    }

    @ExceptionHandler(MsfException.class)
    @ResponseBody
    public ActionReturnUtil msfExceptionHandler(Exception e) throws Exception{
        logger.error(e.getMessage(),e);
        MsfException msfException = (MsfException) e;
        return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.valueOf(msfException.getErrorName()), "", null);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    public ActionReturnUtil exceptionHandler(Exception e) {
        logger.error(e.getMessage(),e);
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UNKNOWN);
    }
    
   /* //空指针异常
    @ExceptionHandler(NullPointerException.class)  
    @ResponseBody  
    public String nullPointerExceptionHandler(NullPointerException ex) {  
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(1001, null);
    }   
    //类型转换异常
    @ExceptionHandler(ClassCastException.class)  
    @ResponseBody  
    public String classCastExceptionHandler(ClassCastException ex) {  
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(1002, null);
    }  

    //IO异常
    @ExceptionHandler(IOException.class)  
    @ResponseBody  
    public String iOExceptionHandler(IOException ex) {  
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(1003, null); 
    }  
    //未知方法异常
    @ExceptionHandler(NoSuchMethodException.class)  
    @ResponseBody  
    public String noSuchMethodExceptionHandler(NoSuchMethodException ex) {  
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(1004, null);
    }  

    //数组越界异常
    @ExceptionHandler(IndexOutOfBoundsException.class)  
    @ResponseBody  
    public String indexOutOfBoundsExceptionHandler(IndexOutOfBoundsException ex) {  
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(1005, null);
    }
    //400错误
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String requestNotReadable(HttpMessageNotReadableException ex){
        System.out.println("400..requestNotReadable");
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(400, null);
    }
    //400错误
    @ExceptionHandler({TypeMismatchException.class})
    @ResponseBody
    public String requestTypeMismatch(TypeMismatchException ex){
        System.out.println("400..TypeMismatchException");
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(400, null);
    }
    //400错误
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public String requestMissingServletRequest(MissingServletRequestParameterException ex){
        System.out.println("400..MissingServletRequest");
        ex.printStackTrace();   //==sonar leak==
        return ReturnFormat.retParam(400, null);
    }
    //405错误
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public String request405(){
        System.out.println("405...");
        return ReturnFormat.retParam(405, null);
    }
    //406错误
    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    @ResponseBody
    public String request406(){
        System.out.println("404...");
        return ReturnFormat.retParam(406, null);
    }
    */
    //500错误
    /*@ExceptionHandler({ConversionNotSupportedException.class,HttpMessageNotWritableException.class})
    @ResponseBody
    public String server500(RuntimeException runtimeException){
        System.out.println("500...");
        return "bbbbbb";
       // return ReturnFormat.retParam(406, null);
    }
    
    @ExceptionHandler(java.lang.IllegalArgumentException.class)
    @ResponseBody
    public String server500(IllegalArgumentException illegalArgumentException){
        System.out.println("500...");
        return "cccc";
       // return ReturnFormat.retParam(406, null);
    }*/
}
