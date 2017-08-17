package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

public class RolePrivilege implements Serializable{
    private Integer id;

    private String role;

    private String privilege;

    private Date updateTime;

    private String firstModule;

    private String secondModule;

    private String thirdModule;

    private Boolean status;

    private String mark;

    private Integer parentid;

    private Boolean isparent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role == null ? null : role.trim();
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege == null ? null : privilege.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getFirstModule() {
        return firstModule;
    }

    public void setFirstModule(String firstModule) {
        this.firstModule = firstModule == null ? null : firstModule.trim();
    }

    public String getSecondModule() {
        return secondModule;
    }

    public void setSecondModule(String secondModule) {
        this.secondModule = secondModule == null ? null : secondModule.trim();
    }

    public String getThirdModule() {
        return thirdModule;
    }

    public void setThirdModule(String thirdModule) {
        this.thirdModule = thirdModule == null ? null : thirdModule.trim();
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark == null ? null : mark.trim();
    }

    public Integer getParentid() {
        return parentid;
    }

    public void setParentid(Integer parentid) {
        this.parentid = parentid;
    }

    public Boolean getIsparent() {
        return isparent;
    }

    public void setIsparent(Boolean isparent) {
        this.isparent = isparent;
    }
}