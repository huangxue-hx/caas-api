package com.harmonycloud.dto.cicd;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.ci.bean.Job;
import org.springframework.beans.BeanUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by anson on 17/7/19.
 */
public class JobDto {
    private Integer id;
    private String name;
    private String tenant;
    private boolean notification;
    private List mail;
    private boolean failNotification;
    private boolean successNotification;
    private boolean trigger;
    private boolean pollScm;
    private String cronExpForPollScm;
    private String createUser;
    private String updateUser;
    private Date createTime;
    private Date updateTime;

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

    public boolean isNotification() {
        return notification;
    }

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public List getMail() {
        return mail;
    }

    public void setMail(List mail) {
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

    public Job convertToBean(){
        Job job = new Job();
        BeanUtils.copyProperties(this, job);
        job.setMail(JsonUtil.convertToJson(this.mail));
        return job;
    }

    public void convertFromBean(Job job){
        BeanUtils.copyProperties(job, this);
        this.setMail(JsonUtil.jsonToList(job.getMail(), String.class));
    }
}
