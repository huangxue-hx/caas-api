package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

public class NamespaceLocal implements Serializable {
    private Integer id;

    private String aliasName;

    private String namespaceName;

    private String namespaceId;

    private String tenantId;

    private String clusterId;

    private String clusterAliasName;

    private Date createTime;

    private Date updateTime;

    private Boolean isPrivate;

    private String clusterName;

    private String reserve1;

    private String reserve2;

    private boolean isGpu;

    private static final long serialVersionUID = 1L;

    private Boolean  istioStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName == null ? null : aliasName.trim();
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName == null ? null : namespaceName.trim();
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId == null ? null : namespaceId.trim();
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null ? null : tenantId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName == null ? null : clusterAliasName.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public String getReserve1() {
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1 == null ? null : reserve1.trim();
    }

    public String getReserve2() {
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2 == null ? null : reserve2.trim();
    }

    public Boolean getIstioStatus() {
        return istioStatus;
    }

    public void setIstioStatus(Boolean istioStatus) {
        this.istioStatus = istioStatus;
    }

    public boolean getIsGpu() {
        return isGpu;
    }

    public void setIsGpu(boolean isGpu) {
        this.isGpu = isGpu;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        NamespaceLocal other = (NamespaceLocal) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getAliasName() == null ? other.getAliasName() == null : this.getAliasName().equals(other.getAliasName()))
            && (this.getNamespaceName() == null ? other.getNamespaceName() == null : this.getNamespaceName().equals(other.getNamespaceName()))
            && (this.getNamespaceId() == null ? other.getNamespaceId() == null : this.getNamespaceId().equals(other.getNamespaceId()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getClusterAliasName() == null ? other.getClusterAliasName() == null : this.getClusterAliasName().equals(other.getClusterAliasName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsPrivate() == null ? other.getIsPrivate() == null : this.getIsPrivate().equals(other.getIsPrivate()))
            && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()))
            && (this.getReserve1() == null ? other.getReserve1() == null : this.getReserve1().equals(other.getReserve1()))
            && (this.getReserve2() == null ? other.getReserve2() == null : this.getReserve2().equals(other.getReserve2()))
            && (this.getIstioStatus() == null ? other.getIstioStatus() == null : this.getIstioStatus().equals(other.getIstioStatus()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAliasName() == null) ? 0 : getAliasName().hashCode());
        result = prime * result + ((getNamespaceName() == null) ? 0 : getNamespaceName().hashCode());
        result = prime * result + ((getNamespaceId() == null) ? 0 : getNamespaceId().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getClusterAliasName() == null) ? 0 : getClusterAliasName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsPrivate() == null) ? 0 : getIsPrivate().hashCode());
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
        result = prime * result + ((getReserve2() == null) ? 0 : getReserve2().hashCode());
        result = prime * result + ((getIstioStatus() == null) ? 0 : getIstioStatus().hashCode());
        return result;
    }
}