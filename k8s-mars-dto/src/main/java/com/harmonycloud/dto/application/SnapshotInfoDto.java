package com.harmonycloud.dto.application;

import org.elasticsearch.Version;
import org.elasticsearch.snapshots.SnapshotState;

import java.util.*;

public class SnapshotInfoDto {
    private String name;
    private SnapshotState state;
    private String reason;
    private List<String> indices;
    private long startTime;
    private long endTime;
    private int totalShards;
    private int successfulShards;
    private String logStartDate;
    private String logEndDate;
    private Version version;
    private boolean inRestore;
    private List<LogIndexDate> restoredDate;
    private String clusterAliasName;
    private String clusterId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SnapshotState getState() {
        return state;
    }

    public void setState(SnapshotState state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getTotalShards() {
        return totalShards;
    }

    public void setTotalShards(int totalShards) {
        this.totalShards = totalShards;
    }

    public int getSuccessfulShards() {
        return successfulShards;
    }

    public void setSuccessfulShards(int successfulShards) {
        this.successfulShards = successfulShards;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getLogStartDate() {
        return logStartDate;
    }

    public void setLogStartDate(String logStartDate) {
        this.logStartDate = logStartDate;
    }

    public String getLogEndDate() {
        return logEndDate;
    }

    public void setLogEndDate(String logEndDate) {
        this.logEndDate = logEndDate;
    }

    public boolean getInRestore() {
        return inRestore;
    }

    public void setInRestore(boolean inRestore) {
        this.inRestore = inRestore;
    }


    public List<LogIndexDate> getRestoredDate() {
        return restoredDate;
    }

    public void setRestoredDate(List<LogIndexDate> restoredDate) {
        this.restoredDate = restoredDate;
    }

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
