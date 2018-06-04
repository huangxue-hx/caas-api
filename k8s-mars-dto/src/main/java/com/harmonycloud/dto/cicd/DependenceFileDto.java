package com.harmonycloud.dto.cicd;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2017-12-18
 * @Modified
 */
public class DependenceFileDto {
    private String dependenceName;
    private MultipartFile file;
    private String path;
    private boolean  isDecompressed;
    private String projectId;
    private String clusterId;

    public String getDependenceName() {
        return dependenceName;
    }

    public void setDependenceName(String dependenceName) {
        this.dependenceName = dependenceName;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDecompressed() {
        return isDecompressed;
    }

    public void setDecompressed(boolean decompressed) {
        isDecompressed = decompressed;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
