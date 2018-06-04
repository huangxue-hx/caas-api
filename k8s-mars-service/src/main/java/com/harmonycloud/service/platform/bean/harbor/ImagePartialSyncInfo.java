package com.harmonycloud.service.platform.bean.harbor;

/**
 * 细粒度镜像同步输入参数信息
 */
public class ImagePartialSyncInfo {

    private String srcClusterId;
    private String destClusterId;
    private Integer targetId;
    private String projectId;
    private Integer harborProjectId;
    private String harborProjectName;
    /**
     * 用户自定义项目名称
     */
    private String repositorySuffixName;
    /**
     * 镜像列表，json格式，如{"lib/mysql":["harbor"], "lib/deploy_jobservice":["v1.2","latest"]}
     */
    private String images;

    public String getSrcClusterId() {
        return srcClusterId;
    }

    public void setSrcClusterId(String srcClusterId) {
        this.srcClusterId = srcClusterId;
    }

    public String getDestClusterId() {
        return destClusterId;
    }

    public void setDestClusterId(String destClusterId) {
        this.destClusterId = destClusterId;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getHarborProjectName() {
        return harborProjectName;
    }

    public void setHarborProjectName(String harborProjectName) {
        this.harborProjectName = harborProjectName;
    }

    public String getRepositorySuffixName() {
        return repositorySuffixName;
    }

    public void setRepositorySuffixName(String repositorySuffixName) {
        this.repositorySuffixName = repositorySuffixName;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Integer getHarborProjectId() {
        return harborProjectId;
    }

    public void setHarborProjectId(Integer harborProjectId) {
        this.harborProjectId = harborProjectId;
    }
}
