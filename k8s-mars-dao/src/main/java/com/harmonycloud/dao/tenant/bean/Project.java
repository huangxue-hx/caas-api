package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

public class Project implements Serializable {
    private Integer id;

    private String tenantId;

    private String projectId;

    private String projectSystemCode;

    private String projectName;

    private Date createTime;

    private Date updateTime;

    private String annotation;

    private String pmUsernames;

    private String updateUserAccount;

    private String updateUserId;

    private String updateUserName;

    private String createUserAccount;

    private String createUserId;

    private String createUserName;

    private String aliasName;

    private String reserve1;

    private String reserve2;

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
        this.tenantId = tenantId == null ? null : tenantId.trim();
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }

    public String getProjectSystemCode() {
        return projectSystemCode;
    }

    public void setProjectSystemCode(String projectSystemCode) {
        this.projectSystemCode = projectSystemCode == null ? null : projectSystemCode.trim();
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
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

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation == null ? null : annotation.trim();
    }

    public String getPmUsernames() {
        return pmUsernames;
    }

    public void setPmUsernames(String pmUsernames) {
        this.pmUsernames = pmUsernames == null ? null : pmUsernames.trim();
    }

    public String getUpdateUserAccount() {
        return updateUserAccount;
    }

    public void setUpdateUserAccount(String updateUserAccount) {
        this.updateUserAccount = updateUserAccount == null ? null : updateUserAccount.trim();
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId == null ? null : updateUserId.trim();
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName == null ? null : updateUserName.trim();
    }

    public String getCreateUserAccount() {
        return createUserAccount;
    }

    public void setCreateUserAccount(String createUserAccount) {
        this.createUserAccount = createUserAccount == null ? null : createUserAccount.trim();
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId == null ? null : createUserId.trim();
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName == null ? null : createUserName.trim();
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName == null ? null : aliasName.trim();
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
        Project other = (Project) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getProjectSystemCode() == null ? other.getProjectSystemCode() == null : this.getProjectSystemCode().equals(other.getProjectSystemCode()))
            && (this.getProjectName() == null ? other.getProjectName() == null : this.getProjectName().equals(other.getProjectName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getAnnotation() == null ? other.getAnnotation() == null : this.getAnnotation().equals(other.getAnnotation()))
            && (this.getPmUsernames() == null ? other.getPmUsernames() == null : this.getPmUsernames().equals(other.getPmUsernames()))
            && (this.getUpdateUserAccount() == null ? other.getUpdateUserAccount() == null : this.getUpdateUserAccount().equals(other.getUpdateUserAccount()))
            && (this.getUpdateUserId() == null ? other.getUpdateUserId() == null : this.getUpdateUserId().equals(other.getUpdateUserId()))
            && (this.getUpdateUserName() == null ? other.getUpdateUserName() == null : this.getUpdateUserName().equals(other.getUpdateUserName()))
            && (this.getCreateUserAccount() == null ? other.getCreateUserAccount() == null : this.getCreateUserAccount().equals(other.getCreateUserAccount()))
            && (this.getCreateUserId() == null ? other.getCreateUserId() == null : this.getCreateUserId().equals(other.getCreateUserId()))
            && (this.getCreateUserName() == null ? other.getCreateUserName() == null : this.getCreateUserName().equals(other.getCreateUserName()))
            && (this.getAliasName() == null ? other.getAliasName() == null : this.getAliasName().equals(other.getAliasName()))
            && (this.getReserve1() == null ? other.getReserve1() == null : this.getReserve1().equals(other.getReserve1()))
            && (this.getReserve2() == null ? other.getReserve2() == null : this.getReserve2().equals(other.getReserve2()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getProjectSystemCode() == null) ? 0 : getProjectSystemCode().hashCode());
        result = prime * result + ((getProjectName() == null) ? 0 : getProjectName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getAnnotation() == null) ? 0 : getAnnotation().hashCode());
        result = prime * result + ((getPmUsernames() == null) ? 0 : getPmUsernames().hashCode());
        result = prime * result + ((getUpdateUserAccount() == null) ? 0 : getUpdateUserAccount().hashCode());
        result = prime * result + ((getUpdateUserId() == null) ? 0 : getUpdateUserId().hashCode());
        result = prime * result + ((getUpdateUserName() == null) ? 0 : getUpdateUserName().hashCode());
        result = prime * result + ((getCreateUserAccount() == null) ? 0 : getCreateUserAccount().hashCode());
        result = prime * result + ((getCreateUserId() == null) ? 0 : getCreateUserId().hashCode());
        result = prime * result + ((getCreateUserName() == null) ? 0 : getCreateUserName().hashCode());
        result = prime * result + ((getAliasName() == null) ? 0 : getAliasName().hashCode());
        result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
        result = prime * result + ((getReserve2() == null) ? 0 : getReserve2().hashCode());
        return result;
    }
}