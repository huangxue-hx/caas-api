package com.harmonycloud.dto.log;

public class EsSnapshotDto {

    private String clusterId;
    private String snapshotName;
    private String[] indexNames;
    private String[] dates;
    private String logDateStart;
    private String logDateEnd;
    private String maxSnapshotSpeed;
    private String maxRestoreSpeed;
    private String backupDir;
    private String renamePrefix;
    private String renameSuffix;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public String[] getIndexNames() {
        return indexNames;
    }

    public void setIndexNames(String[] indexNames) {
        this.indexNames = indexNames;
    }

    public String[] getDates() {
        return dates;
    }

    public void setDates(String[] dates) {
        this.dates = dates;
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

    public String getRenamePrefix() {
        return renamePrefix;
    }

    public void setRenamePrefix(String renamePrefix) {
        this.renamePrefix = renamePrefix;
    }

    public String getRenameSuffix() {
        return renameSuffix;
    }

    public void setRenameSuffix(String renameSuffix) {
        this.renameSuffix = renameSuffix;
    }

    public String getLogDateStart() {
        return logDateStart;
    }

    public void setLogDateStart(String logDateStart) {
        this.logDateStart = logDateStart;
    }

    public String getLogDateEnd() {
        return logDateEnd;
    }

    public void setLogDateEnd(String logDateEnd) {
        this.logDateEnd = logDateEnd;
    }
}
