package com.harmonycloud.dto.cicd;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2017-12-18
 * @Modified
 */
@ApiModel(value = "依赖文件信息")
public class DependenceFileDto {
    @ApiModelProperty(value = "依赖名", name = "name")
    private String dependenceName;
    @ApiModelProperty(value = "依赖文件", name = "file")
    private MultipartFile file;
    @ApiModelProperty(value = "路径", name = "path")
    private String path;
    @ApiModelProperty(value = "是否解压", name = "isDecompressed")
    private boolean  isDecompressed;
    @ApiModelProperty(value = "项目id", name = "projectId")
    private String projectId;
    @ApiModelProperty(value = "集群id", name = "clusterId")
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
