package com.harmonycloud.dto.application;

import java.util.List;

/**
 * create by miaokun
 * 配置文件升级数据体
 * created in 2018-8-24
 */
public class ConfigServiceUpdateDto {
    List<String> serviceNameList;
    String edition;

    public List<String> getServiceNameList() {
        return serviceNameList;
    }

    public void setServiceNameList(List<String> serviceNameList) {
        this.serviceNameList = serviceNameList;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }


}
