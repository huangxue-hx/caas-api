package com.harmonycloud.dto.application;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-18
 * @Modified
 */
public class DaemonSetDto {

    private String name;

    private String namespace;

    private String labels;

    private String creator;

    private String createTime;

    private String updateTime;

    private String status;

    private List<String> cpu;

    private List<String> memory;

    private List<String> img;

    private String clusterId;

    private String clusterName;

    private boolean isSystem;

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getCpu() {
        return cpu;
    }

    public void setCpu(List<String> cpu) {
        this.cpu = cpu;
    }

    public List<String> getImg() {
        return img;
    }

    public void setImg(List<String> img) {
        this.img = img;
    }

    public List<String> getMemory() {
        return memory;
    }

    public void setMemory(List<String> memory) {
        this.memory = memory;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }


}

