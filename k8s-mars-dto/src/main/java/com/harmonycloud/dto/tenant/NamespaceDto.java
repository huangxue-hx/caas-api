package com.harmonycloud.dto.tenant;

import java.util.List;

/**
 * Created by andy on 17-1-20.
 */
public class NamespaceDto {
    //分区简称
    private String name;
    //分区别名
    private String aliasName;
    //租户id
    private String tenantId;
    //备注
    private String annotation;
    //节点名称
    private String nodeName;
    //上次内存
    private String lastMemory;
    //上次cpu
    private String lastCpu;
    //是否为私有
    private Boolean Private = false;
    //是否更新
    private Boolean update = false;

    private NetworkDto network;
    //分区配额
    private QuotaDto quota;
    //集群id
    private String clusterId;
    //主机名列表
    private List<String> nodeList;

    public Boolean getUpdate() {
        return update;
    }

    public void setUpdate(Boolean update) {
        this.update = update;
    }

    public List<String> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<String> nodeList) {
        this.nodeList = nodeList;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public Boolean getPrivate() {
        return Private;
    }

    public void setPrivate(Boolean aPrivate) {
        Private = aPrivate;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getLastMemory() {
        return lastMemory;
    }

    public void setLastMemory(String lastMemory) {
        this.lastMemory = lastMemory;
    }

    public String getLastCpu() {
        return lastCpu;
    }

    public void setLastCpu(String lastCpu) {
        this.lastCpu = lastCpu;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public NetworkDto getNetwork() {
        return network;
    }

    public void setNetwork(NetworkDto network) {
        this.network = network;
    }

    public QuotaDto getQuota() {
        return quota;
    }

    public void setQuota(QuotaDto quota) {
        this.quota = quota;
    }
    public boolean isPrivate() {
        return Private;
    }

    public void setPrivate(boolean private1) {
        Private = private1;
    }

}
