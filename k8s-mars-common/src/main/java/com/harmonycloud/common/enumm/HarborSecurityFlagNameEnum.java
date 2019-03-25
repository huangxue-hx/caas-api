package com.harmonycloud.common.enumm;

/**
 * Created by zsl on 2017/1/22.
 * 镜像安全扫描纬度
 */
public enum HarborSecurityFlagNameEnum {

    PROJECT("project"),USER("user");

    private String flagName;

    HarborSecurityFlagNameEnum(String flagName){
        this.flagName = flagName;
    }

    public String getFlagName() {
        return flagName;
    }

    private void setFlagName(String flagName) {
        this.flagName = flagName;
    }
}
