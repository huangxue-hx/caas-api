package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * transfer_bind_deploy
 * @author 
 */
public class TransferBindDeploy implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 分区名称
     */
    private String namespace;

    /**
     * 目标集群Id
     */
    private String clusterId;

    /**
     * 服务名称
     */
    private String deployName;

    /**
     * 步骤id
     */
    private Integer stepId;

    /**
     * 租户id
     */
    private String tanantId;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 服务迁移状态 0:为迁移 1:已迁移
     */
    private Integer status;

    /**
     * 错误原因
     */
    private String errMsg;

    /**
     * 是否删除 0:未删除 1:已删除
     */
    private Byte isDelete;

    /**
     * 第几次迁移分区
     */
    private Integer deployNum;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public Integer getStepId() {
        return stepId;
    }

    public void setStepId(Integer stepId) {
        this.stepId = stepId;
    }

    public String getTanantId() {
        return tanantId;
    }

    public void setTanantId(String tanantId) {
        this.tanantId = tanantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
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

    public Byte getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Byte isDelete) {
        this.isDelete = isDelete;
    }

    public Integer getDeployNum() {
        return deployNum;
    }

    public void setDeployNum(Integer deployNum) {
        this.deployNum = deployNum;
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
        TransferBindDeploy other = (TransferBindDeploy) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getNamespace() == null ? other.getNamespace() == null : this.getNamespace().equals(other.getNamespace()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getDeployName() == null ? other.getDeployName() == null : this.getDeployName().equals(other.getDeployName()))
            && (this.getStepId() == null ? other.getStepId() == null : this.getStepId().equals(other.getStepId()))
            && (this.getTanantId() == null ? other.getTanantId() == null : this.getTanantId().equals(other.getTanantId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrMsg() == null ? other.getErrMsg() == null : this.getErrMsg().equals(other.getErrMsg()))
            && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()))
            && (this.getDeployNum() == null ? other.getDeployNum() == null : this.getDeployNum().equals(other.getDeployNum()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getDeployName() == null) ? 0 : getDeployName().hashCode());
        result = prime * result + ((getStepId() == null) ? 0 : getStepId().hashCode());
        result = prime * result + ((getTanantId() == null) ? 0 : getTanantId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrMsg() == null) ? 0 : getErrMsg().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        result = prime * result + ((getDeployNum() == null) ? 0 : getDeployNum().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", namespace=").append(namespace);
        sb.append(", clusterId=").append(clusterId);
        sb.append(", deployName=").append(deployName);
        sb.append(", stepId=").append(stepId);
        sb.append(", tanantId=").append(tanantId);
        sb.append(", projectId=").append(projectId);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", status=").append(status);
        sb.append(", errMsg=").append(errMsg);
        sb.append(", isDelete=").append(isDelete);
        sb.append(", deployNum=").append(deployNum);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}