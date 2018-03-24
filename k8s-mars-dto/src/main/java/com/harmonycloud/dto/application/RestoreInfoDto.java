package com.harmonycloud.dto.application;

import org.elasticsearch.snapshots.RestoreInfo;

import java.util.List;

public class RestoreInfoDto {

    private String name;
    private List<String> indices;
    private int totalShards;
    private int successfulShards;

    public RestoreInfoDto convertFromESBean(RestoreInfo restoreInfo){
        if (null == restoreInfo){
            return null;
        }
        this.name = restoreInfo.name();
        this.indices = restoreInfo.indices();
        this.totalShards = restoreInfo.totalShards();
        this.successfulShards = restoreInfo.successfulShards();
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
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
}
