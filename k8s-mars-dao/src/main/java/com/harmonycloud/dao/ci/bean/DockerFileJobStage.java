package com.harmonycloud.dao.ci.bean;

public class DockerFileJobStage {

    private Integer id;

    private Integer dockerFileId;

    private Integer jobId;

    private Integer stageId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDockerFileId() {
        return dockerFileId;
    }

    public void setDockerFileId(Integer dockerFileId) {
        this.dockerFileId = dockerFileId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }
}
