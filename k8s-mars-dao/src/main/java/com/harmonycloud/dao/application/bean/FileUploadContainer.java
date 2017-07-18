package com.harmonycloud.dao.application.bean;

import java.io.Serializable;
import java.util.Date;

public class FileUploadContainer implements Serializable {
    private Integer id;

    private String containerFilePath;

    private String fileName;

    private Long userId;

    private String namespace;

    private String deployment;

    private String pod;

    private String container;

    private Integer phase;

    private String status;

    private String errMsg;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContainerFilePath() {
        return containerFilePath;
    }

    public void setContainerFilePath(String containerFilePath) {
        this.containerFilePath = containerFilePath == null ? null : containerFilePath.trim();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName == null ? null : fileName.trim();
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace == null ? null : namespace.trim();
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment == null ? null : deployment.trim();
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod == null ? null : pod.trim();
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container == null ? null : container.trim();
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(Integer phase) {
        this.phase = phase;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg == null ? null : errMsg.trim();
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
        FileUploadContainer other = (FileUploadContainer) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getContainerFilePath() == null ? other.getContainerFilePath() == null : this.getContainerFilePath().equals(other.getContainerFilePath()))
            && (this.getFileName() == null ? other.getFileName() == null : this.getFileName().equals(other.getFileName()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getNamespace() == null ? other.getNamespace() == null : this.getNamespace().equals(other.getNamespace()))
            && (this.getDeployment() == null ? other.getDeployment() == null : this.getDeployment().equals(other.getDeployment()))
            && (this.getPod() == null ? other.getPod() == null : this.getPod().equals(other.getPod()))
            && (this.getContainer() == null ? other.getContainer() == null : this.getContainer().equals(other.getContainer()))
            && (this.getPhase() == null ? other.getPhase() == null : this.getPhase().equals(other.getPhase()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getErrMsg() == null ? other.getErrMsg() == null : this.getErrMsg().equals(other.getErrMsg()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getContainerFilePath() == null) ? 0 : getContainerFilePath().hashCode());
        result = prime * result + ((getFileName() == null) ? 0 : getFileName().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        result = prime * result + ((getDeployment() == null) ? 0 : getDeployment().hashCode());
        result = prime * result + ((getPod() == null) ? 0 : getPod().hashCode());
        result = prime * result + ((getContainer() == null) ? 0 : getContainer().hashCode());
        result = prime * result + ((getPhase() == null) ? 0 : getPhase().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getErrMsg() == null) ? 0 : getErrMsg().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}