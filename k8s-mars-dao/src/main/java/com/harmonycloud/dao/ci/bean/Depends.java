package com.harmonycloud.dao.ci.bean;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;

import java.io.Serializable;
@DataPrivilegeType(type = DataResourceTypeEnum.PIPELINE)
public class Depends  implements Serializable{
    @DataPrivilegeField(type = CommonConstant.DATA_FIELD)
    private Integer jobId;

    private Integer stageId;

    private String jobName;

    private String stageName;
    @DataPrivilegeField(type = CommonConstant.CLUSTERID_FIELD)
    private String clusterId;
    @DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
    private String projectId;

    private String dataPrivilege;

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

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDataPrivilege() {
        return dataPrivilege;
    }

    public void setDataPrivilege(String dataPrivilege) {
        this.dataPrivilege = dataPrivilege;
    }
}
