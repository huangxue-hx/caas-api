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

    private Integer transferBackupId;

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
    private String tenantId;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 应用名称
     */
    private String appName;
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
    private int isDelete;

    /**
     * 第几次迁移分区
     */
    private Integer deployNum;

    /**
     * 旧的集群id
     */
    private String sourceClusterId;

    /**
     * 旧的分区
     */
    private String sourceNamespace;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
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

    public String gettenantId() {
        return tenantId;
    }

    public void settenantId(String tenantId) {
        this.tenantId = tenantId;
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

    public int getIsDelete() {
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TransferBindDeploy [id=");
        builder.append(id);
        builder.append(", namespace=");
        builder.append(namespace);
        builder.append(", clusterId=");
        builder.append(clusterId);
        builder.append(", deployName=");
        builder.append(deployName);
        builder.append(", stepId=");
        builder.append(stepId);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", createTime=");
        builder.append(createTime);
        builder.append(", updateTime=");
        builder.append(updateTime);
        builder.append(", status=");
        builder.append(status);
        builder.append(", errMsg=");
        builder.append(errMsg);
        builder.append(", isDelete=");
        builder.append(isDelete);
        builder.append(", deployNum=");
        builder.append(deployNum);
        builder.append(", oldClusterId=");
        builder.append(sourceClusterId);
        builder.append(", appName=");
        builder.append(appName);
        builder.append("]");
        return builder.toString();
    }

    public Integer getTransferBackupId() {
        return transferBackupId;
    }

    public void setTransferBackupId(Integer transferBackupId) {
        this.transferBackupId = transferBackupId;
    }

    public String getSourceClusterId() {
        return sourceClusterId;
    }

    public void setSourceClusterId(String sourceClusterId) {
        this.sourceClusterId = sourceClusterId;
    }

    public String getSourceNamespace() {
        return sourceNamespace;
    }

    public void setSourceNamespace(String sourceNamespace) {
        this.sourceNamespace = sourceNamespace;
    }
}