package com.harmonycloud.dao.ci.bean;

public class DockerFilePage extends DockerFile {

    private String jobNames;

    private String stageNames;

    private String stageIds;

    private String depends;

    public String getJobNames() {
        return jobNames;
    }

    public void setJobNames(String jobNames) {
        this.jobNames = jobNames;
    }

    public String getStageNames() {
        return stageNames;
    }

    public void setStageNames(String stageNames) {
        this.stageNames = stageNames;
    }

    public String getStageIds() {
        return stageIds;
    }

    public void setStageIds(String stageIds) {
        this.stageIds = stageIds;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }
}
