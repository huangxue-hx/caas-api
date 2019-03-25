package com.harmonycloud.service.platform.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@ApiModel(value="存储信息")
public class PvDto implements Serializable{

    private static final long serialVersionUID = 5202810321695795037L;
    /**
     * 是否已绑定服务
     */
    private Boolean isBind;
    /**
     * 该pv资源所在的集群名称
     */
    @ApiModelProperty(value="集群名",name="clusterName",example="开发集群")
    private String clusterName;
    private String clusterAliasName;
    @NotBlank
    @ApiModelProperty(value="集群id",name="clusterId",example="cluster-top--dev",required = true)
    private String clusterId;
    @NotBlank
    private String capacity;
    @NotNull
    private Boolean isBindOne;
    @NotBlank
    private String name;
    @NotNull
    private Boolean isReadonly;
    private String tenantId;
    @NotBlank
    private String projectId;
    private Date createTime;
    @NotBlank
    private String type;
    /**
     * pv将被绑定的服务名
     */
    private String serviceName;

    private String serviceNamespace;
    private String status;

    private String used;

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isReadonly() {
        return isReadonly;
    }

    public Boolean getIsReadonly() {
        return isReadonly;
    }

    public void setIsReadonly(Boolean readOnly) {
        this.isReadonly = readOnly;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Boolean getIsBind() {
        return isBind;
    }

    public void setIsBind(Boolean bind) {
        isBind = bind;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean isBindOne() {
        return isBindOne;
    }

    public Boolean getIsBindOne() {
        return isBindOne;
    }

    public void setIsBindOne(Boolean bindOne) {
        isBindOne = bindOne;
    }

    public String getServiceNamespace() {
        return serviceNamespace;
    }

    public void setServiceNamespace(String serviceNamespace) {
        this.serviceNamespace = serviceNamespace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }
}
