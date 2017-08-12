package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

public class UserTenant  implements Serializable{
    private Integer id;

    private String tenantid;

    private String username;

    private Date createTime;

    private Integer istm;

    private String role;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid == null ? null : tenantid.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getIstm() {
        return istm;
    }

    public void setIstm(Integer istm) {
        this.istm = istm;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role == null ? null : role.trim();
    }
}