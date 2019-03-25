package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * transfer_cluster
 * @author 
 */
public class TransferCluster implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 集群id
     */
    private String clusterId;

    /**
     * 原集群id
     */
    private String oldClusterId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否迁移过 0:未迁移过 1:已迁移过
     */
    private Byte isTransfer;

    /**
     * 是否断点续传过 0:未断点续传过 1:已断点续传过
     */
    private Byte isContinue;

    /**
     * 是否成功 0:正在迁移 -1:失败  1:成功
     */
    private Integer isErr;

    /**
     * 百分比
     */
    private String percent;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getOldClusterId() {
        return oldClusterId;
    }

    public void setOldClusterId(String oldClusterId) {
        this.oldClusterId = oldClusterId;
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

    public Byte getIsTransfer() {
        return isTransfer;
    }

    public void setIsTransfer(Byte isTransfer) {
        this.isTransfer = isTransfer;
    }

    public Byte getIsContinue() {
        return isContinue;
    }

    public void setIsContinue(Byte isContinue) {
        this.isContinue = isContinue;
    }

    public Integer getIsErr() {
        return isErr;
    }

    public void setIsErr(Integer isErr) {
        this.isErr = isErr;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
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
        TransferCluster other = (TransferCluster) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getOldClusterId() == null ? other.getOldClusterId() == null : this.getOldClusterId().equals(other.getOldClusterId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getIsTransfer() == null ? other.getIsTransfer() == null : this.getIsTransfer().equals(other.getIsTransfer()))
            && (this.getIsContinue() == null ? other.getIsContinue() == null : this.getIsContinue().equals(other.getIsContinue()))
            && (this.getIsErr() == null ? other.getIsErr() == null : this.getIsErr().equals(other.getIsErr()))
            && (this.getPercent() == null ? other.getPercent() == null : this.getPercent().equals(other.getPercent()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getOldClusterId() == null) ? 0 : getOldClusterId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsTransfer() == null) ? 0 : getIsTransfer().hashCode());
        result = prime * result + ((getIsContinue() == null) ? 0 : getIsContinue().hashCode());
        result = prime * result + ((getIsErr() == null) ? 0 : getIsErr().hashCode());
        result = prime * result + ((getPercent() == null) ? 0 : getPercent().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", tenantId=").append(tenantId);
        sb.append(", clusterId=").append(clusterId);
        sb.append(", oldClusterId=").append(oldClusterId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isTransfer=").append(isTransfer);
        sb.append(", isContinue=").append(isContinue);
        sb.append(", isErr=").append(isErr);
        sb.append(", percent=").append(percent);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}