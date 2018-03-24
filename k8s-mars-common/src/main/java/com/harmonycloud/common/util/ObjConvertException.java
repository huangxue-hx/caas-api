/*
 * Project: greenline-hrs-std-util
 * 
 * File Created at 2014年12月29日
 * 
 * Copyright 2012 Greenline.com Corporation Limited.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Greenline Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Greenline.com.
 */
package com.harmonycloud.common.util;

/**
 * @Type ObjConverterException
 * @Desc 转换异常类
 * @author zhangkui
 * @Version V1.0
 */
public class ObjConvertException extends RuntimeException {

    public ObjConvertException(String message){
        super(message);
    }

    public ObjConvertException(String message, Throwable cause){
        super(message, cause);
    }

    public ObjConvertException(String errorCode, String message){
        super(errorCode + ":" + message);
    }

    public ObjConvertException(String errorCode, String message, Throwable cause){
        super(errorCode + ":" + message, cause);
    }

    public ObjConvertException(Throwable cause){
        super(cause);
    }

}
