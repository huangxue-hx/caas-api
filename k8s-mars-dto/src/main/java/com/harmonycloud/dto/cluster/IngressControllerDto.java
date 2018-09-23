package com.harmonycloud.dto.cluster;

import java.beans.BeanInfo;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @date 2018/7/31 17:09
 */
public class IngressControllerDto {

    //ingress controller名称
    private String icName;

    private String clusterId;

    private String clusterAliasName;

    private String namespace;

    //租户信息，包含多个租户信息，每个租户一个map，包含：tenantName、tenantId
    private List tenantInfo;

    private int httpPort;

    private String icPort;

    private Date createTime;

    //ingress controller状态
    private String status;

    private Boolean isDefault;

    public String getIcName() {
        return icName;
    }

    public void setIcName(String icName) {
        this.icName = icName;
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
}
