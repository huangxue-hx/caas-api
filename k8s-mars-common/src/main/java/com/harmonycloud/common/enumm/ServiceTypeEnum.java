package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * deployment or statefulSet
 */
public enum ServiceTypeEnum {
    DEPLOYMENT(0),STATEFULSET(1);

    private int code;

    private static final Map<Integer, ServiceTypeEnum> SERVICE_TYPE_MAP = new ConcurrentHashMap<>(
            ServiceTypeEnum.values().length);

    static {
        for (ServiceTypeEnum type : EnumSet.allOf(ServiceTypeEnum.class)) {
            SERVICE_TYPE_MAP.put(type.getCode(), type);
        }
    }

    ServiceTypeEnum(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ServiceTypeEnum valueOf(Integer code){
        return SERVICE_TYPE_MAP.get(code);
    }
}
