package com.harmonycloud.dto.cluster;

import com.harmonycloud.dao.cluster.bean.TransferBindDeploy;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by dengyl on 2019-03-17
 */
public class ClusterTransferBackupDto implements Serializable {

    private static final long serialVersionUID = 3117238362408828566L;

    private Integer id;

    private String tenantId;

    private String tenantName;

    private String tenantAliasName;

    private String errMsg;

    private Date createTime;

    private Date updateTime;

    private String oldClusterId;

    private String transferClusterPercent;

    private String transferClusterId;

    private String transferClusterName;

    private String transferClusterAliasName;

    private List<TransferBindDeploy> transferBindDeploys;


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

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
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

    public String getOldClusterId() {
        return oldClusterId;
    }

    public void setOldClusterId(String oldClusterId) {
        this.oldClusterId = oldClusterId;
    }

    public String getTransferClusterPercent() {
        return transferClusterPercent;
    }

    public void setTransferClusterPercent(String transferClusterPercent) {
        this.transferClusterPercent = transferClusterPercent;
    }

    public String getTransferClusterId() {
        return transferClusterId;
    }

    public void setTransferClusterId(String transferClusterId) {
        this.transferClusterId = transferClusterId;
    }

    public String getTransferClusterName() {
        return transferClusterName;
    }

    public void setTransferClusterName(String transferClusterName) {
        this.transferClusterName = transferClusterName;
    }

    public String getTenantAliasName() {
        return tenantAliasName;
    }

    public void setTenantAliasName(String tenantAliasName) {
        this.tenantAliasName = tenantAliasName;
    }

    public List<TransferBindDeploy> getTransferBindDeploys() {
        return transferBindDeploys;
    }

    public void setTransferBindDeploys(List<TransferBindDeploy> transferBindDeploys) {
        this.transferBindDeploys = transferBindDeploys;
    }

    public String getTransferClusterAliasName() {
        return transferClusterAliasName;
    }

    public void setTransferClusterAliasName(String transferClusterAliasName) {
        this.transferClusterAliasName = transferClusterAliasName;
    }
}
