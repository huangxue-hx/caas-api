package com.harmonycloud.common.util;

import java.util.UUID;

/**
 * @Author jiangmi
 * @Description uuid工具类
 * @Date created in 2017-12-19
 * @Modified
 */
public class UUIDUtil {

    /**
     * 生成随机字符串UUID
     * @return
     */
    public static String getUUID(){
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();
        String id = str.replace("-", "");
        return id;
    }

    /**
     * 生成随机字符串UUID
     * @return
     */
    public static String get16UUID(){
        return getUUID().substring(0,16);
    }
}
