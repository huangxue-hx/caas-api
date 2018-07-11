package com.harmonycloud.common.enumm;

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

    DataResourceTypeEnum(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
