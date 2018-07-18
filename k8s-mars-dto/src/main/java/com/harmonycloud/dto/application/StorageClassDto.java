package com.harmonycloud.dto.application;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @date 2018/6/14 14:46
 */
@ApiModel(value = "StorageClass信息")
public class StorageClassDto {
    @ApiModelProperty(value = "StorageClass名称", name = "name", example = "test-storage-class", required = true)
    private String name;
    @ApiModelProperty(value = "StorageClass使用的存储类型", name = "type", example = "NFS", required = true)
    private String type;
    @ApiModelProperty(value = "StorageClass所属的集群ID", name = "clusterId", example = "cluster-top--dev", required = true)
    private String clusterId;
    @ApiModelProperty(value = "StorageClass最大存储配额", name = "storageLimit", example = "10", required = true)
    private String storageLimit;
    @ApiModelProperty(value = "StorageClass创建时间", name = "createTime")
    private Date createTime;
    @ApiModelProperty(value = "StorageClass的状态", name = "status", example = "-1/0/1, 创建失败/创建中/创建完成")
    private int status;
    @ApiModelProperty(value = "StorageClass相关配置", name = "configMap", example = "{'NFS_SERVER': '10.10.101.91', 'NFS_PATH' : '/nfs/top'}")
    private Map<String, String> configMap;
    @ApiModelProperty(value = "StorageClass相关服务", name = "serviceList")
    private List serviceList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getStorageLimit() {
        return storageLimit;
    }

    public void setStorageLimit(String storageLimit) {
        this.storageLimit = storageLimit;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, String> configMap) {
        this.configMap = configMap;
    }

    public List getServiceList() {
        return serviceList;
    }

    public void setServiceList(List serviceList) {
        this.serviceList = serviceList;
    }
}
