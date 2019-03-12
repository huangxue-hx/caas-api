package com.harmonycloud.dao.cluster.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * transfer_cluster_backup
 * @author
 */
public class TransferClusterBackup implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 第几次迁移分区
     */
    private Integer namespaceNum;

    /**
     * 第几次迁移服务
     */
    private Integer deployNum;

    /**
     * 错误原因
     */
    private String errMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 迁移的集群id
     */
    private String transferClusterId;

    /**
     * 是否是断点续传 0:不是 1:是
     */
    private Byte isContinue;

    /**
     * 是否是增量迁移迁移 0:不是 1:是 
     */
    private Byte isDefault;

    /**
     * 迁移集群的百分比
     */
    private String transferClusterPercent;

    /**
     * 迁移到那个项目下 如果是迁移分区则为空
     */
    private String projectId;


    private String errNamespace;

    private String errDeploy;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String gettenantId() {
        return tenantId;
    }

    public void settenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getNamespaceNum() {
        return namespaceNum;
    }

    public void setNamespaceNum(Integer namespaceNum) {
        this.namespaceNum = namespaceNum;
    }

    public Integer getDeployNum() {
        return deployNum;
    }

    public void setDeployNum(Integer deployNum) {
        this.deployNum = deployNum;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
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

    public String getTransferClusterId() {
        return transferClusterId;
    }

    public void setTransferClusterId(String transferClusterId) {
        this.transferClusterId = transferClusterId;
    }

    public Byte getIsContinue() {
        return isContinue;
    }

    public void setIsContinue(Byte isContinue) {
        this.isContinue = isContinue;
    }

    public Byte getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Byte isDefault) {
        this.isDefault = isDefault;
    }

    public String getTransferClusterPercent() {
        return transferClusterPercent;
    }

    public void setTransferClusterPercent(String transferClusterPercent) {
        this.transferClusterPercent = transferClusterPercent;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getErrNamespace() {
        return errNamespace;
    }

    public void setErrNamespace(String errNamespace) {
        this.errNamespace = errNamespace;
    }

    public String getErrDeploy() {
        return errDeploy;
    }

    public void setErrDeploy(String errDeploy) {
        this.errDeploy = errDeploy;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TransferClusterBackup [id=");
        builder.append(id);
        builder.append(", tenantId=");
        builder.append(tenantId);
        builder.append(", namespaceNum=");
        builder.append(namespaceNum);
        builder.append(", deployNum=");
        builder.append(deployNum);
        builder.append(", errMsg=");
        builder.append(errMsg);
        builder.append(", createTime=");
        builder.append(createTime);
        builder.append(", updateTime=");
        builder.append(updateTime);
        builder.append(", transferClusterId=");
        builder.append(transferClusterId);
        builder.append(", isContinue=");
        builder.append(isContinue);
        builder.append(", isDefault=");
        builder.append(isDefault);
        builder.append(", transferClusterPercent=");
        builder.append(transferClusterPercent);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", errNamespace=");
        builder.append(errNamespace);
        builder.append(", errDeploy=");
        builder.append(errDeploy);
        builder.append("]");
        return builder.toString();
    }


}