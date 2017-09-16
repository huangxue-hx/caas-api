package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by anson on 17/6/9.
 */
public class StageSonar implements Serializable{
    private Integer id;
    private Integer stageId;
    private Integer qualitygatesId;
    private String projectName;
    private String projectKey;
    private String sonarProperty;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }

    public Integer getQualitygatesId() {
        return qualitygatesId;
    }

    public void setQualitygatesId(Integer qualitygatesId) {
        this.qualitygatesId = qualitygatesId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public String getSonarProperty() {
        return sonarProperty;
    }

    public void setSonarProperty(String sonarProperty) {
        this.sonarProperty = sonarProperty;
    }
}
