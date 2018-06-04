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

import java.util.ArrayList;
import java.util.List;

/**
 * @Type ListObjConverter
 * @Desc 对象集合相互转换，对象必须有对称get、set方法，不适用于复杂类型
 * @Version V1.0
 */
public class ListObjConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ListObjConverter.class);

    private ListObjConverter() {

    }

    public static <T> List<T> convert(List<?> source, Class<T> clazz, ObjConverter.ForceMatch... forceMatchs) {
        List<T> target = new ArrayList<T>();
        try {
            if (source == null) {
                LOG.error("对象集合转换异常，来源对象集合为空");
                return target;
            }
            for (Object sourceObj : source) {
                target.add(ObjConverter.convert(sourceObj, clazz, forceMatchs));
            }
        } catch (Exception e) {
            LOG.error("对象集合转换异常:",e);
        }
        return target;

    }
}
