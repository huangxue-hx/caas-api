package com.harmonycloud.service.platform.bean.harbor;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zsl on 2017/1/19.
 * harbor log bean
 */
public class HarborLog implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long logId;
	private String harborHost;
    //项目id
	private Integer projectId;
    //镜像仓库名称
    private String repoName;
    //镜像标签
    private String repoTag;
    //操作类型， push， pull， create， delete etc.
    private String operation;
    //操作时间
    private Date operationTime;
    //操作用户名
    private String userName;

    public HarborLog() {
    }

    public HarborLog(String harborHost, String repoName, String repoTag) {
        this.harborHost = harborHost;
        this.repoName = repoName;
        this.repoTag = repoTag;
        this.operationTime = new Date();
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoTag() {
        return repoTag;
    }

    public void setRepoTag(String repoTag) {
        this.repoTag = repoTag;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Date getOperationTime() {
        if(operationTime == null){
            return null;
        }
        return (Date)operationTime.clone();
    }

    public void setOperationTime(Date operationTime) {
        if (operationTime != null) {
            this.operationTime = (Date) operationTime.clone();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getHarborHost() {
        return harborHost;
    }

    public void setHarborHost(String harborHost) {
        this.harborHost = harborHost;
    }

}
