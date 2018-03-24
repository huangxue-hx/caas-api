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
    //项目id
	private Integer projectId;
    //镜像仓库名称
    private String repoName;
    //镜像标签
    private String repoTag;
    //操作类型， push， pull， create， delete etc.
    private String operation;
    //操作时间
    private String operationTime;
    //操作用户名
    private String userName;

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

    public String getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(String operationTime) {
        this.operationTime = operationTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
