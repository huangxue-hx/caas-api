package com.harmonycloud.common.util;

import com.harmonycloud.common.enumm.CtsCodeMessage;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;


/**
 * 请求返回util
 * @author yj
 * @date 2017年1月6日
 */
@SuppressWarnings("serial")
public class ActionReturnUtil extends HashMap<String, Object>{

    public static ActionReturnUtil returnSuccess(){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("success",true);
        return returnMap;
    }
    
    public static ActionReturnUtil returnSuccessWithData(Object data){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("data", data);
        returnMap.put("success",true);
        return returnMap;
    }

    public static ActionReturnUtil returnSuccessWithDataAndCount(Object data,int Count){
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("data", data);
        returnMap.put("count",Count);
        returnMap.put("success",true);
        return returnMap;
    }

    public static ActionReturnUtil returnSuccessWithMap(String key,String value){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put(key, value);
        returnMap.put("success",true);
        return returnMap;
    }
    
    public static ActionReturnUtil returnError(){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("success",false);
        return returnMap;
    }

    public static ActionReturnUtil returnErrorWithCodeAndMsg(Object data, Integer errorCode){
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("data", data);
        returnMap.put("errorCode",errorCode);
        returnMap.put("success",false);
        return returnMap;
    }
    
    public static ActionReturnUtil returnErrorWithData(Object data){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("data", data);
        returnMap.put("success",false);
        return returnMap;
    }


    public static ActionReturnUtil returnErrorWithData(Integer errorCode){
        ErrorCodeMessage error = ErrorCodeMessage.valueOf(errorCode);
        return returnErrorWithData(error, "", false);
    }

    public static ActionReturnUtil returnErrorWithData(ErrorCodeMessage error){
        return returnErrorWithData(error, "", false);
    }

    public static ActionReturnUtil returnErrorWithData(ErrorCodeMessage error, String extendMessage){
        return returnErrorWithData(error,extendMessage,false);
    }

    public static ActionReturnUtil returnErrorWithData(String extendMessage, ErrorCodeMessage error){
        return returnErrorWithData(error,extendMessage,true);
    }

    public static ActionReturnUtil returnErrorWithData(ErrorCodeMessage error, String extendMessage, boolean prefix){
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("success",false);
        returnMap.put("errorCode", error.value());
        returnMap.put("data", ErrorCodeMessage.getMessageWithLanguage(error, extendMessage, prefix));
        return returnMap;
    }

    public static ActionReturnUtil returnErrorWithMap(String key,String value){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put(key, value);
        returnMap.put("success",false);
        return returnMap;
    }

    public static ActionReturnUtil returnErrorWithMsg(ErrorCodeMessage error) throws Exception{
        return returnErrorWithMsg(error, "", false);
    }

    public static ActionReturnUtil returnErrorWithMsg(ErrorCodeMessage error, String extendMessage, boolean prefix) throws Exception{
        throw new MarsRuntimeException(ErrorCodeMessage.getMessageWithLanguage(error, extendMessage, prefix));
    }

    public static ActionReturnUtil returnErrorWithMsg(String errMsg) throws Exception{
        throw new MarsRuntimeException(errMsg);
    }
    
    public static ActionReturnUtil returnSuccessWithMsg(String msg){
    	ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("msg", msg);
        returnMap.put("success",true);
        return returnMap;
    }

    public static  ActionReturnUtil returnCodeAndMsg(MicroServiceCodeMessage codeMessage, String extra, String taskId) {
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("code",codeMessage.value());
        returnMap.put("msg", extra + codeMessage.getMessage());
        if (StringUtils.isNotEmpty(taskId)) {
            returnMap.put("task_id", taskId);
        }
        return returnMap;
    }

    public static  ActionReturnUtil returnCodeAndMsg(CtsCodeMessage codeMessage, String extra) {
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("code",codeMessage.value());
        returnMap.put("msg", extra + codeMessage.getMessage());
        return returnMap;
    }

    public static  ActionReturnUtil returnCodeAndMsgWithSuccess(MicroServiceCodeMessage codeMessage, Object data) {
        ActionReturnUtil returnMap = new ActionReturnUtil();
        returnMap.put("code",codeMessage.value());
        returnMap.put("msg", codeMessage.getMessage());
        returnMap.put("data", data);
        return returnMap;
    }

    public boolean isSuccess() {
        return (Boolean) get("success");
    }

    public Object getData() {
        return get("data");
    }
    
}
