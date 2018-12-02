package com.harmonycloud.common.enumm;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by anson on 18/6/15.
 */
public enum DataResourceTypeEnum {
    APPLICATION(1),
    SERVICE(2),
    CONFIGFILE(3),
    EXTERNALSERVICE(4),
    STORAGE(5),
    PIPELINE(6);


    private final int code;

    private static final Map<Integer, DataResourceTypeEnum> DATA_RESOURCE_TYPE_MAP = new ConcurrentHashMap<>(
            DataResourceTypeEnum.values().length);


    static {
        for (DataResourceTypeEnum type : EnumSet.allOf(DataResourceTypeEnum.class)) {
            DATA_RESOURCE_TYPE_MAP.put(type.getCode(), type);
        }
    }

    DataResourceTypeEnum(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataResourceTypeEnum valueOf(Integer code){
        return DATA_RESOURCE_TYPE_MAP.get(code);
    }
}
