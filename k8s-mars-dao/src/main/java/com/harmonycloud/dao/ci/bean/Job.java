package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by anson on 17/6/9.
 */
public class Job  implements Serializable{
    private Integer id;
    private String uuid;
    private String name;
    private String description;
    private String type;
    private String tenant;
    private String tenantId;
    private String projectId;
    private String clusterId;
    private boolean notification;
    private String mail;
    private boolean failNotification;
    private boolean successNotification;
//    private boolean trigger;
//    private boolean pollScm;
//    private boolean pollScmCustomize;
//    private String cronExpForPollScm;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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

//    public boolean isTrigger() {
//        return trigger;
//    }
//
//    public void setTrigger(boolean trigger) {
//        this.trigger = trigger;
//    }
//
//    public boolean isPollScm() {
//        return pollScm;
//    }
//
//    public void setPollScm(boolean pollScm) {
//        this.pollScm = pollScm;
//    }
//
//    public boolean isPollScmCustomize() {
//        return pollScmCustomize;
//    }
//
//    public void setPollScmCustomize(boolean pollScmCustomize) {
//        this.pollScmCustomize = pollScmCustomize;
//    }
//
//    public String getCronExpForPollScm() {
//        return cronExpForPollScm;
//    }
//
//    public void setCronExpForPollScm(String cronExpForPollScm) {
//        this.cronExpForPollScm = cronExpForPollScm;
//    }

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
