package com.harmonycloud.dao.ci.bean;

/**
 * Created by anson on 17/6/9.
 */
public class Job {
    private Integer id;
    private String jobName;
    private String tenant;
    private String projectType;
    private String buildType;
    private String repositoryType;
    private String repositoryUrl;
    private String repositoryBranch;
    private String credentialsUsername;
    private String credentialsPassword;
    private String baseImage;
    private String imageName;
    private String imageTag;
    private String harborProject;
    private String createUser;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
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

    public String getBaseImage() {
        return baseImage;
    }

    public void setBaseImage(String baseImage) {
        this.baseImage = baseImage;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }
}
