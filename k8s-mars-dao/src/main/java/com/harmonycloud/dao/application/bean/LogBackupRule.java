package com.harmonycloud.dao.application.bean;

import java.io.Serializable;
import java.util.Date;

public class LogBackupRule implements Serializable {
    private Integer id;

    private String clusterId;

    private String backupDir;

    private Integer daysBefore;

    private Integer daysDuration;

    private String maxSnapshotSpeed;

    private String maxRestoreSpeed;

    private Date lastBackupTime;

    private Date createTime;

    private Date updateTime;

    private Boolean available;

    private String clusterAliasName;

    private String clusterName;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir == null ? null : backupDir.trim();
    }

    public Integer getDaysBefore() {
        return daysBefore;
    }

    public void setDaysBefore(Integer daysBefore) {
        this.daysBefore = daysBefore;
    }

    public Integer getDaysDuration() {
        return daysDuration;
    }

    public void setDaysDuration(Integer daysDuration) {
        this.daysDuration = daysDuration;
    }

    public String getMaxSnapshotSpeed() {
        return maxSnapshotSpeed;
    }

    public void setMaxSnapshotSpeed(String maxSnapshotSpeed) {
        this.maxSnapshotSpeed = maxSnapshotSpeed == null ? null : maxSnapshotSpeed.trim();
    }

    public String getMaxRestoreSpeed() {
        return maxRestoreSpeed;
    }

    public void setMaxRestoreSpeed(String maxRestoreSpeed) {
        this.maxRestoreSpeed = maxRestoreSpeed == null ? null : maxRestoreSpeed.trim();
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
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
        LogBackupRule other = (LogBackupRule) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getBackupDir() == null ? other.getBackupDir() == null : this.getBackupDir().equals(other.getBackupDir()))
            && (this.getDaysBefore() == null ? other.getDaysBefore() == null : this.getDaysBefore().equals(other.getDaysBefore()))
            && (this.getDaysDuration() == null ? other.getDaysDuration() == null : this.getDaysDuration().equals(other.getDaysDuration()))
            && (this.getMaxSnapshotSpeed() == null ? other.getMaxSnapshotSpeed() == null : this.getMaxSnapshotSpeed().equals(other.getMaxSnapshotSpeed()))
            && (this.getMaxRestoreSpeed() == null ? other.getMaxRestoreSpeed() == null : this.getMaxRestoreSpeed().equals(other.getMaxRestoreSpeed()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getAvailable() == null ? other.getAvailable() == null : this.getAvailable().equals(other.getAvailable()))
              && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getBackupDir() == null) ? 0 : getBackupDir().hashCode());
        result = prime * result + ((getDaysBefore() == null) ? 0 : getDaysBefore().hashCode());
        result = prime * result + ((getDaysDuration() == null) ? 0 : getDaysDuration().hashCode());
        result = prime * result + ((getMaxSnapshotSpeed() == null) ? 0 : getMaxSnapshotSpeed().hashCode());
        result = prime * result + ((getMaxRestoreSpeed() == null) ? 0 : getMaxRestoreSpeed().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getAvailable() == null) ? 0 : getAvailable().hashCode());
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        return result;
    }

    public Date getLastBackupTime() {
        return lastBackupTime;
    }

    public void setLastBackupTime(Date lastBackupTime) {
        this.lastBackupTime = lastBackupTime;
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }
}