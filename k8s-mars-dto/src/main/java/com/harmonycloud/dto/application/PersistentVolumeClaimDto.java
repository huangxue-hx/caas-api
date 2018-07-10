package com.harmonycloud.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author xc
 * @date 2018/7/4 19:20
 */
@ApiModel(value = "PersistentVolumeClaim信息")
public class PersistentVolumeClaimDto {
    @ApiModelProperty(value = "PVC名称", name = "name", example = "test-pvc", required = true)
    private String name;
    @ApiModelProperty(value = "PVC所属集群ID", name = "clusterId", example = "cluster-top--dev", required = true)
    private String clusterId;
    @ApiModelProperty(value = "PVC所属分区", name = "namespace", example = "kube-system", required = true)
    private String namespace;
    @ApiModelProperty(value = "PVC所用存储类名称", name = "storageName", example = "storageClass-test", required = true)
    private String storageName;
    @ApiModelProperty(value = "PVC容量值", name = "capacity", example = "2", required = true)
    private String capacity;
    @ApiModelProperty(value = "PVC是否只读", name = "readOnly", example = "true/false")
    private Boolean readOnly;
    @ApiModelProperty(value = "PVC是否只供一个服务使用", name = "bindOne", example = "true/false")
    private Boolean bindOne;
    @ApiModelProperty(value = "PVC所属租户ID", name = "tenantId", example = "admin")
    private String tenantId;
    @ApiModelProperty(value = "PVC所属项目ID", name = "projectId", example = "java")
    private String projectId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Boolean getBindOne() {
        return bindOne;
    }

    public void setBindOne(Boolean bindOne) {
        this.bindOne = bindOne;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
