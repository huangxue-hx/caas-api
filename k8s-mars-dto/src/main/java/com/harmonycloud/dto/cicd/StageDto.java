package com.harmonycloud.dto.cicd;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.ci.bean.Stage;
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
    private String buildEnvironment;
    private List<Map<String, Object>> environmentVariables;
    private boolean useDependency;
    private List<Dependence> dependences;
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
    private List command;
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

    public List getCommand() {
        return command;
    }

    public void setCommand(List command) {
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
        stage.setCommand(JsonUtil.convertToJson(this.command==null?new ArrayList<>():this.command));
        stage.setEnvironmentVariables(JsonUtil.convertToJson(this.environmentVariables == null ? new ArrayList<>() : this.environmentVariables));
        stage.setDependences(JsonUtil.convertToJson(this.dependences == null ? new ArrayList<>() : this.dependences));
        return stage;
    }

    public void convertFromBean(Stage stage){
        BeanUtils.copyProperties(stage, this);
        this.setCommand(JsonUtil.jsonToList(stage.getCommand() == null ? "[]" : stage.getCommand(), String.class));
        this.setEnvironmentVariables(JsonUtil.JsonToMapList(stage.getEnvironmentVariables()));
        this.setDependences(JsonUtil.jsonToList(stage.getDependences() == null ? "[]" : stage.getDependences(), Dependence.class));
    }

    public static class Dependence{
        private String name;
        private String server;
        private String serverPath;
        private String mountPath;


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
    }
}
