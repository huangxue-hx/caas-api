package com.harmonycloud.dao.ci.bean;

import java.util.Date;

/**
 * Created by anson on 17/6/9.
 */
public class Job {
    private Integer id;
    private String name;
    private String tenant;
    private String tenantId;
    private boolean notification;
    private String mail;
    private boolean failNotification;
    private boolean successNotification;
    private boolean trigger;
    private boolean pollScm;
    private String cronExpForPollScm;
    private String createUser;
    private String updateUser;
    private Date createTime;
    private Date updateTime;
    private Integer lastBuildNum;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public boolean isFailNotification() {
        return failNotification;
    }

    public void setFailNotification(boolean failNotification) {
        this.failNotification = failNotification;
    }

    public boolean isSuccessNotification() {
        return successNotification;
    }

    public void setSuccessNotification(boolean successNotification) {
        this.successNotification = successNotification;
    }

    public String getCreateUser() {
        return createUser;
    }

    public boolean isTrigger() {
        return trigger;
    }

    public void setTrigger(boolean trigger) {
        this.trigger = trigger;
    }

    public boolean isPollScm() {
        return pollScm;
    }

    public void setPollScm(boolean pollScm) {
        this.pollScm = pollScm;
    }

    public String getCronExpForPollScm() {
        return cronExpForPollScm;
    }

    public void setCronExpForPollScm(String cronExpForPollScm) {
        this.cronExpForPollScm = cronExpForPollScm;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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

    public Integer getLastBuildNum() {
        return lastBuildNum;
    }

    public void setLastBuildNum(Integer lastBuildNum) {
        this.lastBuildNum = lastBuildNum;
    }
}
