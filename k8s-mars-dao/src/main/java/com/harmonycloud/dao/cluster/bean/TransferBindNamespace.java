package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * transfer_bind_namespace
 * @author 
 */
public class TransferBindNamespace implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 原分区
     */
    private String currentNamespace;

    /**
     * 新建的分区
     */
    private String createNamespace;

    /**
     * 目标集群Id
     */
    private String clusterId;

    /**
     * 租户Id
     */
    private String tenantId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0:未删除 1:已删除
     */
    private Byte isDelete;

    /**
     * 是否是默认名称分区 0:不是 1:是
     */
    private Byte isDefault;

    /**
     * 状态 0:未迁移 1:已迁移
     */
    private Integer status;

    /**
     * 失败原因
     */
    private String errMsg;

    /**
     * 第几次迁移应用
     */
    private Integer namespaceNum;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCurrentNamespace() {
        return currentNamespace;
    }

    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }

    public String getCreateNamespace() {
        return createNamespace;
    }

    public void setCreateNamespace(String createNamespace) {
        this.createNamespace = createNamespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Byte getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Byte isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Integer getNamespaceNum() {
        return namespaceNum;
    }

    public void setNamespaceNum(Integer namespaceNum) {
        this.namespaceNum = namespaceNum;
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
        TransferBindNamespace other = (TransferBindNamespace) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getCurrentNamespace() == null ? other.getCurrentNamespace() == null : this.getCurrentNamespace().equals(other.getCurrentNamespace()))
            && (this.getCreateNamespace() == null ? other.getCreateNamespace() == null : this.getCreateNamespace().equals(other.getCreateNamespace()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getIsDefault() == null ? other.getIsDefault() == null : this.getIsDefault().equals(other.getIsDefault()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrMsg() == null ? other.getErrMsg() == null : this.getErrMsg().equals(other.getErrMsg()))
            && (this.getNamespaceNum() == null ? other.getNamespaceNum() == null : this.getNamespaceNum().equals(other.getNamespaceNum()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getCurrentNamespace() == null) ? 0 : getCurrentNamespace().hashCode());
        result = prime * result + ((getCreateNamespace() == null) ? 0 : getCreateNamespace().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getIsDefault() == null) ? 0 : getIsDefault().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrMsg() == null) ? 0 : getErrMsg().hashCode());
        result = prime * result + ((getNamespaceNum() == null) ? 0 : getNamespaceNum().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", currentNamespace=").append(currentNamespace);
        sb.append(", createNamespace=").append(createNamespace);
        sb.append(", clusterId=").append(clusterId);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", isDefault=").append(isDefault);
        sb.append(", status=").append(status);
        sb.append(", errMsg=").append(errMsg);
        sb.append(", namespaceNum=").append(namespaceNum);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}