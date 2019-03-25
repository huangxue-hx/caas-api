package com.harmonycloud.dto.application;

import java.util.List;

/**
 * create by miaokun
 * 配置文件升级数据体
 * created in 2018-8-24
 */
public class ConfigServiceUpdateDto {
    private List<String> serviceNameList;

    private String tag;

    private String clusterId;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getServiceNameList() {
        return serviceNameList;
    }

    public void setServiceNameList(List<String> serviceNameList) {
        this.serviceNameList = serviceNameList;
    }


}
