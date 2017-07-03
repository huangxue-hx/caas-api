package com.harmonycloud.common.util;


/**
 * 
 * http状态码判断
 * 
 * @author jmi
 *
 */
public class HttpStatusUtil {
	
	public static boolean isSuccessStatus(int statusCode){
        return String.valueOf(statusCode).startsWith("2");
    }

}
