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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @Type ObjConverter
 * @Desc 用于PO,BO,DTO,VO等之间进行转换， 两个对象之间必须有对称的get和set方法，不适用于复杂类型
 * @Version V1.0
 */
public class ObjConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ObjConverter.class);
    private ObjConverter() {

    }
    public static <T> T convert(Object source, Class<T> clazz, ForceMatch... fMatchs) {
        T target = null;
        try {
            if (source == null) {
                return target;
            }
            target = clazz.newInstance();
            convert(source, target, fMatchs);
        } catch (Exception e) {
            LOG.error("对象转换异常", e);
            throw new ObjConvertException(e);
        }
        return target;
    }

    public static <T> void convert(Object source, T target, ForceMatch... fMatchs) {
        try {
            if (source == null || target == null) {
                return;
            }
            Method[] methods = target.getClass().getMethods();
            for (Method tarMethod : methods) {
                String tarMethodName = tarMethod.getName();
                Method souMethod = getSourceMethod(tarMethodName, source.getClass(), fMatchs);
                if (souMethod != null) {
                    try {
                        tarMethod.invoke(target, souMethod.invoke(source));
                    } catch (Exception e) {
                        LOG.error("对象属性copy异常souMethod:"+souMethod+",tarMethodName:"+tarMethodName, e);
                        throw new ObjConvertException(e);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("对象转换异常", e);
            throw new ObjConvertException(e);
        }
    }

    private static Method getSourceMethod(String tarMethodName, Class<?> souClazz, ForceMatch... fMatchs)
            throws ObjConvertException {
        Method souMethod = null;
        if (tarMethodName.startsWith("set")) {
            try {
                souMethod = souClazz.getMethod(tarMethodName.replaceFirst("s", "g"));
            } catch (NoSuchMethodException e) {
                // 强制匹配
                String tarFieldName = getFieldName(tarMethodName);
                for (ForceMatch fMatch : fMatchs) {
                    String souMethodName = null;
                    if (tarFieldName.equals(fMatch.getFieldx())) {
                        souMethodName = getGetMethodName(fMatch.getFieldy());
                    } else if (tarFieldName.equals(fMatch.getFieldy())) {
                        souMethodName = getGetMethodName(fMatch.getFieldx());
                    }
                    if (souMethodName != null) {
                        try {
                            souMethod = souClazz.getMethod(souMethodName);
                        } catch (NoSuchMethodException e1) {
                           LOG.error("对象转换失败，强制匹配参数异常,tarMethodName:{},souClazz:{}", tarMethodName,souClazz);
                            throw new ObjConvertException(e1);
                        }
                        break;
                    }
                }
            }
        }
        return souMethod;
    }

    /**
     * 根据字段名字获取get方法名
     * 
     * @param fieldName
     * @return
     */
    private static String getGetMethodName(String fieldName) {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * 根据方法名返回字段名
     * 
     * @param methodName
     * @return
     */
    private static String getFieldName(String methodName) {
        String fieldName = methodName.substring(3, methodName.length());
        return Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static class ForceMatch {

        private String fieldx;
        private String fieldy;

        public ForceMatch(String fieldx, String fieldy) {
            this.fieldx = fieldx;
            this.fieldy = fieldy;
        }

        public String getFieldx() {
            return fieldx;
        }

        public void setFieldx(String fieldx) {
            this.fieldx = fieldx;
        }

        public String getFieldy() {
            return fieldy;
        }

        public void setFieldy(String fieldy) {
            this.fieldy = fieldy;
        }

    }
}
