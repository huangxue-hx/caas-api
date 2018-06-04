package com.harmonycloud.dto.log;

public class AppLogDto {

    private Integer ruleId;

    private String clusterIds;
    private Integer dateBefore;
    private Integer dateDuration;
    private String maxSnapshotSpeed;
    private String maxRestoreSpeed;
    private String backupDir;
    private Boolean available;

    public Integer getRuleId() {
        return ruleId;
    }

    public void setRuleId(Integer ruleId) {
        this.ruleId = ruleId;
    }

    public String getClusterIds() {
        return clusterIds;
    }

    public void setClusterIds(String clusterIds) {
        this.clusterIds = clusterIds;
    }

    public Integer getDateBefore() {
        return dateBefore;
    }

    public void setDateBefore(Integer dateBefore) {
        this.dateBefore = dateBefore;
    }

    public Integer getDateDuration() {
        return dateDuration;
    }

    public void setDateDuration(Integer dateDuration) {
        this.dateDuration = dateDuration;
    }

    public String getMaxSnapshotSpeed() {
        return maxSnapshotSpeed;
    }

    public void setMaxSnapshotSpeed(String maxSnapshotSpeed) {
        this.maxSnapshotSpeed = maxSnapshotSpeed;
    }

    public String getMaxRestoreSpeed() {
        return maxRestoreSpeed;
    }

    public void setMaxRestoreSpeed(String maxRestoreSpeed) {
        this.maxRestoreSpeed = maxRestoreSpeed;
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

}
