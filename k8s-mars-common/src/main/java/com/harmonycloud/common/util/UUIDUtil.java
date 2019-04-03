package com.harmonycloud.common.util;

import java.util.UUID;

/**
 * @Author jiangmi
 * @Description uuid工具类
 * @Date created in 2017-12-19
 * @Modified
 */
public class UUIDUtil {

    public static final int UUID_LENGTH_16 = 16;
    public static final int UUID_LENGTH_36 = 36;

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
        return getUUID().substring(0,UUID_LENGTH_16);
    }
}
