package com.harmonycloud.dao.microservice.bean;

import java.io.Serializable;
import java.util.Date;

public class MicroServiceOperationTask implements Serializable{

    private Integer id;

    private String taskId;

    private Integer status;

    private Integer taskType;

    private String errorMsg;

    private Integer appTemplateId;

    private String namespaceId;

    private Date createTime;

    private Date updateTime;

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public Integer getAppTemplateId() {
        return appTemplateId;
    }

    public void setAppTemplateId(Integer appTemplateId) {
        this.appTemplateId = appTemplateId;
    }
}
