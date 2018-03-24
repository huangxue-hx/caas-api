package com.harmonycloud.service.platform.bean;

import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;
import java.util.Map;

public class NodeDto {
    private String ip;
    private String name;
    private String status;
    private String time;
    private String type;
    private String nodeShareStatus;
    private String cpu;
    private String memory;
    private String disk;
    private Boolean scheduable;
    private List<Object> customLabels;
    private String clusterId;
    /**
     * 集群别名
     */
    private String aliasName;

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public List<Object> getCustomLabels() {
        return customLabels;
    }

    public void setCustomLabels(List<Object> customLabels) {
        this.customLabels = customLabels;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public String getNodeShareStatus() {
        return nodeShareStatus;
    }

    public void setNodeShareStatus(String nodeShareStatus) {
        this.nodeShareStatus = nodeShareStatus;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getScheduable() {
        return scheduable;
    }

    public void setScheduable(Boolean scheduable) {
        this.scheduable = scheduable;
    }
}
