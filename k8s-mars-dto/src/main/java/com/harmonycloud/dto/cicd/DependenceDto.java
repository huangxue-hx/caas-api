package com.harmonycloud.dto.cicd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Created by anson on 17/8/1.
 */
@ApiModel(value = "依赖信息")
public class DependenceDto {
    @ApiModelProperty(value = "依赖名", name = "name")
    private String name;
    @ApiModelProperty(value = "项目id", name = "projectId")
    private String projectId;
    @ApiModelProperty(value = "集群id", name = "clusterId")
    private String clusterId;
    @ApiModelProperty(value = "路径", name = "path")
    private String path;
    @ApiModelProperty(value = "nfsServer", name = "nfsServer")
    private String nfsServer;
    @ApiModelProperty(value = "公有依赖/私有依赖", name = "common")
    private boolean common;

    private String storageClassName;


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

    public String getStorageClassName() {
        return storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }
}
