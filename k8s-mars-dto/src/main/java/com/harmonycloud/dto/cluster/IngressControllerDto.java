package com.harmonycloud.dto.cluster;

import java.util.Date;
import java.util.List;

/**
 * @author xc
 * @date 2018/7/31 17:09
 */
public class IngressControllerDto {

    //ingress controller名称
    private String icName;

    private String icAliasName;

    private String clusterId;

    private String clusterAliasName;

    private String namespace;

    //租户信息，包含多个租户信息，每个租户一个map，包含：tenantName、tenantId
    private List tenantInfo;

    private int httpPort;

    private int httpsPort;

    private int healthPort;

    private int statusPort;

    private String icPort;

    private Date createTime;

    //ingress controller状态
    private String status;

    private Boolean isDefault;

    private List<String> icNodeNames;

    public String getIcName() {
        return icName;
    }

    public void setIcName(String icName) {
        this.icName = icName;
    }

    public String getIcAliasName() {
        return icAliasName;
    }

    public void setIcAliasName(String icAliasName) {
        this.icAliasName = icAliasName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List getTenantInfo() {
        return tenantInfo;
    }

    public void setTenantInfo(List tenantInfo) {
        this.tenantInfo = tenantInfo;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    public int getHealthPort() {
        return healthPort;
    }

    public void setHealthPort(int healthPort) {
        this.healthPort = healthPort;
    }

    public int getStatusPort() {
        return statusPort;
    }

    public void setStatusPort(int statusPort) {
        this.statusPort = statusPort;
    }

    public String getIcPort() {
        return icPort;
    }

    public void setIcPort(String icPort) {
        this.icPort = icPort;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public List<String> getIcNodeNames() {
        return icNodeNames;
    }

    public void setIcNodeNames(List<String> icNodeNames) {
        this.icNodeNames = icNodeNames;
    }
}
