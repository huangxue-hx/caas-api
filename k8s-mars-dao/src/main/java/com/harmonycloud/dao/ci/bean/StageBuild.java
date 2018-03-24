package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by anson on 17/7/17.
 */
public class StageBuild  implements Serializable{
    private Integer id;
    private Integer jobId;
    private Integer stageId;
    private String stageName;
    private Integer stageOrder;
    private Integer stageTypeId;
    private String stageType;
    private Integer stageTemplateTypeId;
    private Integer buildNum;
    private String status;
    private Date startTime;
    private String duration;
    private String log;
    private String image;
    private String testResult;
    private String testUrl;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Integer getStageOrder() {
        return stageOrder;
    }

    public void setStageOrder(Integer stageOrder) {
        this.stageOrder = stageOrder;
    }

    public Integer getStageTypeId() {
        return stageTypeId;
    }

    public void setStageTypeId(Integer stageTypeId) {
        this.stageTypeId = stageTypeId;
    }

    public String getStageType() {
        return stageType;
    }

    public void setStageType(String stageType) {
        this.stageType = stageType;
    }

    public Integer getStageTemplateTypeId() {
        return stageTemplateTypeId;
    }

    public void setStageTemplateTypeId(Integer stageTemplateTypeId) {
        this.stageTemplateTypeId = stageTemplateTypeId;
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
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }
}
