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
    private boolean environmentChange;
    private Integer buildEnvironmentId;
    private String environmentVariables;
    private boolean useDependency;
    private String dependences;
    private Integer dockerfileType;
    private String baseImage;
    private Integer dockerfileId;
    private String dockerfilePath;
    private String imageType;
    private String imageName;
    private String imageTagType;
    private String imageBaseTag;
    private String imageIncreaseTag;
    private String imageTag;
    private String harborProject;
    private String deployType;
    private String namespace;
    private Integer originStageId;
    private String serviceTemplateName;
    private String serviceTemplateTag;
    private String serviceName;
    private String containerName;
    private String configuration;
    private Integer maxSurge;
    private Integer maxUnavailable;
    private Integer instances;
    private String command;
    private String suiteId;
    private String createUser;
    private Date createTime;
    private String updateUser;
    private Date updateTime;
    private String destClusterId;
    private Integer repositoryId;
    private String projectId;
    private String tenantId;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getDestClusterId() {
        return destClusterId;
    }

    public void setDestClusterId(String destClusterId) {
        this.destClusterId = destClusterId;
    }

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

    public boolean isEnvironmentChange() {
        return environmentChange;
    }

    public void setEnvironmentChange(boolean environmentChange) {
        this.environmentChange = environmentChange;
    }

    public Integer getBuildEnvironmentId() {
        return buildEnvironmentId;
    }

    public void setBuildEnvironmentId(Integer buildEnvironmentId) {
        this.buildEnvironmentId = buildEnvironmentId;
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

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
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

    public String getDeployType() {
        return deployType;
    }

    public void setDeployType(String deployType) {
        this.deployType = deployType;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Integer getOriginStageId() {
        return originStageId;
    }

    public void setOriginStageId(Integer originStageId) {
        this.originStageId = originStageId;
    }

    public String getServiceTemplateName() {
        return serviceTemplateName;
    }

    public void setServiceTemplateName(String serviceTemplateName) {
        this.serviceTemplateName = serviceTemplateName;
    }

    public String getServiceTemplateTag() {
        return serviceTemplateTag;
    }

    public void setServiceTemplateTag(String serviceTemplateTag) {
        this.serviceTemplateTag = serviceTemplateTag;
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

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Integer getMaxSurge() {
        return maxSurge;
    }

    public void setMaxSurge(Integer maxSurge) {
        this.maxSurge = maxSurge;
    }

    public Integer getMaxUnavailable() {
        return maxUnavailable;
    }

    public void setMaxUnavailable(Integer maxUnavailable) {
        this.maxUnavailable = maxUnavailable;
    }

    public Integer getInstances() {
        return instances;
    }

    public void setInstances(Integer instances) {
        this.instances = instances;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
