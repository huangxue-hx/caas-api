package com.harmonycloud.dto.cicd;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.dto.cicd.sonar.ConditionDto;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 17/7/12.
 */
public class StageDto {
    private Integer id;
    private String tenant;
    private String jobName;
    private Integer jobId;
    private Integer stageOrder;
    private Integer stageTypeId;
    private String stageTypeName;
    private Integer stageTemplateType;
    private String stageName;
    private String repositoryType;
    private String repositoryUrl;
    private String repositoryBranch;
    private String credentialsUsername;
    private String credentialsPassword;
    private boolean environmentChange;
    private Integer buildEnvironmentId;
    private String buildEnvironment;
    private List<Map<String, Object>> environmentVariables;
    private boolean useDependency;
    private List<Dependence> dependences;
    private Integer dockerfileType;
    private String baseImage;
    private Integer dockerfileId;
    private String dockerfilePath;
    private Integer imageType;
    private String imageName;
    private String imageTagType;
    private String imageBaseTag;
    private String imageIncreaseTag;
    private String imageTag;
    private String harborProject;
    private String deployType;
    private String namespace;
    private Integer originJobId;
    private Integer originStageId;
    private String serviceTemplateName;
    private String serviceTemplateTag;
    private String serviceName;
    private String containerName;
    private List<CreateConfigMapDto> configMaps;
    private Integer maxSurge;
    private Integer maxUnavailable;
    private Integer instances;
    private List command;
    private String suiteId;
    private Date createTime;
    private Date updateTime;

    private List<ConditionDto> conditionDtos;
    private String sonarProperty;

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

    public String getStageTypeName() {
        return stageTypeName;
    }

    public void setStageTypeName(String stageTypeName) {
        this.stageTypeName = stageTypeName;
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

    public String getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(String buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
    }

    public List<Map<String, Object>> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<Map<String, Object>> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public boolean isUseDependency() {
        return useDependency;
    }

    public void setUseDependency(boolean useDependency) {
        this.useDependency = useDependency;
    }

    public List<Dependence> getDependences() {
        return dependences;
    }

    public void setDependences(List<Dependence> dependences) {
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

    public Integer getImageType() {
        return imageType;
    }

    public void setImageType(Integer imageType) {
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

    public Integer getOriginJobId() {
        return originJobId;
    }

    public void setOriginJobId(Integer originJobId) {
        this.originJobId = originJobId;
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

    public List<CreateConfigMapDto> getConfigMaps() {
        return configMaps;
    }

    public void setConfigMaps(List<CreateConfigMapDto> configMaps) {
        this.configMaps = configMaps;
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

    public List getCommand() {
        return command;
    }

    public void setCommand(List command) {
        this.command = command;
    }

    public String getSuiteId() {
        return suiteId;
    }

    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
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

    public List<ConditionDto> getConditionDtos() {
        return conditionDtos;
    }

    public void setConditionDtos(List<ConditionDto> conditionDtos) {
        this.conditionDtos = conditionDtos;
    }

    public String getSonarProperty() {
        return sonarProperty;
    }

    public void setSonarProperty(String sonarProperty) {
        this.sonarProperty = sonarProperty;
    }

    public Stage convertToBean(){
        Stage stage = new Stage();
        BeanUtils.copyProperties(this, stage);
        stage.setCommand(this.command==null ? null : JsonUtil.convertToJson(this.command));
        stage.setEnvironmentVariables(this.environmentVariables == null ? null : JsonUtil.convertToJson(this.environmentVariables));
        stage.setDependences(this.dependences == null ? null : JsonUtil.convertToJson(this.dependences));
        stage.setConfiguration(this.configMaps == null ? null : JSONObject.toJSONString(this.configMaps));
        return stage;
    }

    public void convertFromBean(Stage stage){
        BeanUtils.copyProperties(stage, this);
        this.setCommand(stage.getCommand() == null ? null : JsonUtil.jsonToList(stage.getCommand(), String.class));
        this.setEnvironmentVariables(stage.getEnvironmentVariables() == null ? null : JsonUtil.JsonToMapList(stage.getEnvironmentVariables()));
        this.setDependences(stage.getDependences() == null ? null : JsonUtil.jsonToList(stage.getDependences(), Dependence.class));
        this.setConfigMaps(stage.getConfiguration() == null ? null : JSONObject.parseArray(stage.getConfiguration(), CreateConfigMapDto.class));
    }

    public static class Dependence{
        private String name;
        private String server;
        private String serverPath;
        private String mountPath;
        private String pvName;
        private boolean common;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getServerPath() {
            return serverPath;
        }

        public void setServerPath(String serverPath) {
            this.serverPath = serverPath;
        }

        public String getMountPath() {
            return mountPath;
        }

        public void setMountPath(String mountPath) {
            this.mountPath = mountPath;
        }

        public String getPvName() {
            return pvName;
        }

        public void setPvName(String pvName) {
            this.pvName = pvName;
        }

        public boolean isCommon() {
            return common;
        }

        public void setCommon(boolean common) {
            this.common = common;
        }
    }
}
