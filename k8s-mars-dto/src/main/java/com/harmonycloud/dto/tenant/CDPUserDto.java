package com.harmonycloud.dto.tenant;

import com.harmonycloud.dao.tenant.bean.Project;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zgl on 18-1-16.
 */
public class CDPUserDto implements Serializable {

    private static final long serialVersionUID = -6141661628695983128L;
    //用户Id
    private String userId;
    //ad账号
    private String userAccount;
    //用户名称
    private String userName;
    //电话
    private String tel;
    //邮箱
    private String email;
    //石化盈科的更新用户账号
    private String updateUserAccount;
    //石化盈科的更新用户id
    private String updateUserId;
    //石化盈科的更新用户名
    private String updateUserName;
    //石化盈科的创建用户账号
    private String createUserAccount;
    //石化盈科的创建用户id
    private String createUserId;
    //石化盈科的创建用户名
    private String createUserName;
    //石化盈科的项目备注
    private String remark;
    //石化盈科的系统id
    private String sysId;
    //石化盈科的项目编码
    private String sysCode;
    //石化盈科的项目名称
    private String sysName;
    //石化盈科的项目分类 分类 0：主项目，1：子项目
    private String category;
    //石化盈科的主项目Id 如果是子项目，必填，主项目此字段为空
    private String parentId;
    //角色名称
    private String roleName;
    //角色编码
    private String roleCode;
    private String contact;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
