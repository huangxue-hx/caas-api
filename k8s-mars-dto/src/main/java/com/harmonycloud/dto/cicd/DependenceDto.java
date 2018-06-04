package com.harmonycloud.dto.cicd;

/**
 * Created by anson on 17/8/1.
 */
public class DependenceDto {
    private String name;
    private String projectId;
    private String clusterId;
    private String path;
    private String nfsServer;
    private boolean common;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNfsServer() {
        return nfsServer;
    }

    public void setNfsServer(String nfsServer) {
        this.nfsServer = nfsServer;
    }

    public boolean isCommon() {
        return common;
    }

    public void setCommon(boolean common) {
        this.common = common;
    }
}
