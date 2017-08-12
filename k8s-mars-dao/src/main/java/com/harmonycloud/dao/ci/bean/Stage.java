package com.harmonycloud.dao.ci.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by anson on 17/7/11.
 */
public class Stage  implements Serializable{
    private Integer id;
    private String tenant;
    private String jobName;
    private Integer jobId;
    private Integer stageOrder;
    private Integer stageTypeId;
    private Integer stageTemplateType;
    private String stageName;
    private String repositoryType;
    private String repositoryUrl;
    private String repositoryBranch;
    private String credentialsUsername;
    private String credentialsPassword;
    private String buildEnvironment;
    private String environmentVariables;
    private boolean useDependency;
    private String dependences;
    private Integer dockerfileType;
    private String baseImage;
    private Integer dockerfileId;
    private String dockerfilePath;
    private String imageName;
    private String imageTagType;
    private String imageBaseTag;
    private String imageIncreaseTag;
    private String imageTag;
    private String harborProject;
    private String namespace;
    private String serviceName;
    private String containerName;
    private String command;
    private Date createTime;
    private Date updateTime;

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

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
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

    public Integer getStageTemplateType() {
        return stageTemplateType;
    }

    public void setStageTemplateType(Integer stageTemplateType) {
        this.stageTemplateType = stageTemplateType;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(String repositoryType) {
        this.repositoryType = repositoryType;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getRepositoryBranch() {
        return repositoryBranch;
    }

    public void setRepositoryBranch(String repositoryBranch) {
        this.repositoryBranch = repositoryBranch;
    }

    public String getCredentialsUsername() {
        return credentialsUsername;
    }

    public void setCredentialsUsername(String credentialsUsername) {
        this.credentialsUsername = credentialsUsername;
    }

    public String getCredentialsPassword() {
        return credentialsPassword;
    }

    public void setCredentialsPassword(String credentialsPassword) {
        this.credentialsPassword = credentialsPassword;
    }

    public String getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(String buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    public String getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(String environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isUseDependency() {
        return useDependency;
    }

    public void setUseDependency(boolean useDependency) {
        this.useDependency = useDependency;
    }

    public String getDependences() {
        return dependences;
    }

    public void setDependences(String dependences) {
        this.dependences = dependences;
    }

    public Integer getDockerfileType() {
        return dockerfileType;
    }

    public void setDockerfileType(Integer dockerfileType) {
        this.dockerfileType = dockerfileType;
    }

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public Integer getDockerfileId() {
        return dockerfileId;
    }

    public void setDockerfileId(Integer dockerfileId) {
        this.dockerfileId = dockerfileId;
    }

    public String getDockerfilePath() {
        return dockerfilePath;
    }

    public void setDockerfilePath(String dockerfilePath) {
        this.dockerfilePath = dockerfilePath;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTagType() {
        return imageTagType;
    }

    public void setImageTagType(String imageTagType) {
        this.imageTagType = imageTagType;
    }

    public String getImageBaseTag() {
        return imageBaseTag;
    }

    public void setImageBaseTag(String imageBaseTag) {
        this.imageBaseTag = imageBaseTag;
    }

    public String getImageIncreaseTag() {
        return imageIncreaseTag;
    }

    public void setImageIncreaseTag(String imageIncreaseTag) {
        this.imageIncreaseTag = imageIncreaseTag;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getHarborProject() {
        return harborProject;
    }

    public void setHarborProject(String harborProject) {
        this.harborProject = harborProject;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
