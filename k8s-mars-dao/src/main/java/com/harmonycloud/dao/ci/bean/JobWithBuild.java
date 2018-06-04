package com.harmonycloud.dao.ci.bean;


import java.util.Date;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-4-2
 * @Modified
 */
public class JobWithBuild {
    private Integer id;
    private String name;
    private String description;
    private String type;
    private String projectId;
    private String clusterId;
    private Integer buildNum;
    private String status;
    private Date startTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getBuildNum() {
        return buildNum;
    }

    public void setBuildNum(Integer buildNum) {
        this.buildNum = buildNum;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartTime() {
        if(startTime == null){
            return null;
        }
        return (Date)startTime.clone();
    }

    public void setStartTime(Date _startTime) {
        if( _startTime != null) {
            this.startTime = (Date) _startTime.clone();
        }
    }
}
