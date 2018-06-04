package com.harmonycloud.dao.harbor.bean;

import java.util.Date;

public class HarborRepositoryProject {

    private Integer id;
    private String repositoryName;
    private Integer harborProjectId;
    private String harborProjectName;
    private Date createdTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public Integer getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Integer harborProjectId) {
        this.harborProjectId = harborProjectId;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
