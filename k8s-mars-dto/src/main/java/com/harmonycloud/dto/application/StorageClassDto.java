package com.harmonycloud.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

/**
 * @author xc
 * @date 2018/6/14 14:46
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageClassDto {

    private String name;

    private String type;

    private String clusterId;

    private String createTime;

    private String quotaLimit;

    public String getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(String quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    private Map<String, String> configMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }
}
