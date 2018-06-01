package com.harmonycloud.dto.application;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 应用详情返回结果
 * @Date created in 2018-5-15
 * @Modified
 */
public class ApplicationDetailDto {

    private String clusterId;

    private boolean isMsf;

    private boolean isOperationable;

    private String name;

    private String createTime;

    private String desc;

    private String namespace;

    private String user;

    private String id;

    private String realName;

    private String aliasNamespace;

    private String updateTime;

    private List<ServiceDetailInApplicationDto> serviceList;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public boolean isMsf() {
        return isMsf;
    }

    public void setMsf(boolean msf) {
        isMsf = msf;
    }

    public boolean isOperationable() {
        return isOperationable;
    }

    public void setOperationable(boolean operationable) {
        isOperationable = operationable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAliasNamespace() {
        return aliasNamespace;
    }

    public void setAliasNamespace(String aliasNamespace) {
        this.aliasNamespace = aliasNamespace;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public List<ServiceDetailInApplicationDto> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<ServiceDetailInApplicationDto> serviceList) {
        this.serviceList = serviceList;
    }
}
