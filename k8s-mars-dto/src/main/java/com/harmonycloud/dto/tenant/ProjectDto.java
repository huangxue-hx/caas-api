package com.harmonycloud.dto.tenant;

import java.util.Date;
import java.util.List;

/**
 * Created by zgl on 17-12-18.
 */
public class ProjectDto {
    //id
    private Integer id;
    //项目
    private String aliasName;
    //租户id
    private String tenantId;
    //项目id
    private String projectId;
    //harbor仓库数量
    private Integer harborRepositoryNum;
    //harbor仓库列表
    private List harborRepositoryList;
    //项目编码,对应CDP项目编码（projectCode字段， 基于CDP项目简称字段，36进制生成）
    private String projectSystemCode;
    //项目名称,对应CDP项目名称（projectName字段）
    private String projectName;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //备注
    private String annotation;
    //项目管理员
    private String pmUsernames;
    //项目管理员列表
    private List pmList;
    //项目成员列表
    private List userDataList;
    //项目成员数量
    private Integer userNum;
    //项目应用数量
    private Integer appNum;
    //项目角色列表
    private List roleList;
    //项目局部角色列表
    private List localRoleList;
    //修改项目的用户账号,对应CDP项目修改人（reviseUserName字段）
    private String updateUserAccount;
    //修改项目的用户Id,对应CDP项目修改人（reviseUserId字段）
    private String updateUserId;
    //修改项目的用户名称,对应CDP项目修改人（reviseUserName字段）
    private String updateUserName;
    //创建项目的用户账号,对应CDP项目修改人（reviseUserAccount字段）
    private String createUserAccount;
    //创建项目的用户Id,对应CDP项目创建人ID
    private String createUserId;
    //创建项目的用户名称,对应CDP项目创建人名称（createUserName字段）
    private String createUserName;

    public List getLocalRoleList() {
        return localRoleList;
    }

    public void setLocalRoleList(List localRoleList) {
        this.localRoleList = localRoleList;
    }

    public Integer getUserNum() {
        return userNum;
    }

    public void setUserNum(Integer userNum) {
        this.userNum = userNum;
    }

    public Integer getAppNum() {
        return appNum;
    }

    public void setAppNum(Integer appNum) {
        this.appNum = appNum;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public Integer getHarborRepositoryNum() {
        return harborRepositoryNum;
    }

    public void setHarborRepositoryNum(Integer harborRepositoryNum) {
        this.harborRepositoryNum = harborRepositoryNum;
    }

    public List getHarborRepositoryList() {
        return harborRepositoryList;
    }

    public void setHarborRepositoryList(List harborRepositoryList) {
        this.harborRepositoryList = harborRepositoryList;
    }

    public String getProjectSystemCode() {
        return projectSystemCode;
    }

    public void setProjectSystemCode(String projectSystemCode) {
        this.projectSystemCode = projectSystemCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
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
        this.annotation = annotation;
    }

    public String getPmUsernames() {
        return pmUsernames;
    }

    public void setPmUsernames(String pmUsernames) {
        this.pmUsernames = pmUsernames;
    }

    public List getPmList() {
        return pmList;
    }

    public void setPmList(List pmList) {
        this.pmList = pmList;
    }

    public List getUserDataList() {
        return userDataList;
    }

    public void setUserDataList(List userDataList) {
        this.userDataList = userDataList;
    }

    public List getRoleList() {
        return roleList;
    }

    public void setRoleList(List roleList) {
        this.roleList = roleList;
    }

    public String getUpdateUserAccount() {
        return updateUserAccount;
    }

    public void setUpdateUserAccount(String updateUserAccount) {
        this.updateUserAccount = updateUserAccount;
    }

    public String getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(String updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUserName) {
        this.updateUserName = updateUserName;
    }

    public String getCreateUserAccount() {
        return createUserAccount;
    }

    public void setCreateUserAccount(String createUserAccount) {
        this.createUserAccount = createUserAccount;
    }

    public String getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }
}
