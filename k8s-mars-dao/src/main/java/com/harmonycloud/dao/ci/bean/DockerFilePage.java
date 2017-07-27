package com.harmonycloud.dao.ci.bean;

import java.util.List;

public class DockerFilePage extends DockerFile {

    private String jobIds;

    private String jobNames;

    private String stageIds;

    private String stageNames;

    private List<Depends> depends;

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

    public List<Depends> getDepends() {
        return depends;
    }

    public void setDepends(List<Depends> depends) {
        this.depends = depends;
    }

    public String getJobIds() {
        return jobIds;
    }

    public void setJobIds(String jobIds) {
        this.jobIds = jobIds;
    }
}
