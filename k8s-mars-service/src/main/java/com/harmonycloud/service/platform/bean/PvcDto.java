package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xc
 * @date 2018/7/5 14:08
 */
public class PvcDto implements Serializable {

    private String name;

    private String clusterId;

    private String clusterAliasName;

    private String namespace;

    private String namespaceAliasName;

    //容量
    private String capacity;

    //storageClass name
    private String storageClassName;

    private String storageClassType;

    //状态
    private String status;
    //绑定的服务（多个的话以“，”分割）
    private List<Map<String, Object>> bindingServices;

    private Boolean readOnly;

    private Boolean bindOne;

    private Date createTime;

    private String used;

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

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespaceAliasName() {
        return namespaceAliasName;
    }

    public void setNamespaceAliasName(String namespaceAliasName) {
        this.namespaceAliasName = namespaceAliasName;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getStorageClassName() {
        return storageClassName;
    }

    public void setStorageClassName(String storageClassName) {
        this.storageClassName = storageClassName;
    }

    public String getStorageClassType() {
        return storageClassType;
    }

    public void setStorageClassType(String storageClassType) {
        this.storageClassType = storageClassType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Map<String, Object>> getBindingServices() {
        return bindingServices;
    }

    public void setBindingServices(List<Map<String, Object>> bindingServices) {
        this.bindingServices = bindingServices;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getBindOne() {
        return bindOne;
    }

    public void setBindOne(Boolean bindOne) {
        this.bindOne = bindOne;
    }

    public String getUsed() {
        return used;
    }

    public void setUsed(String used) {
        this.used = used;
    }
}
