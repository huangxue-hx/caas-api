package com.harmonycloud.dto.application;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 创建Daemonset的参数bean
 * @Date created in 2017-12-18
 * @Modified
 */
public class DaemonSetDetailDto {

    private String name;

    private String namespace;

    private String labels;

    private String annotation;

    private Integer templateGeneration;

    private String restartPolicy;

    private String status;

    private List<CreateContainerDto> containers;

    private String sessionAffinity;

    private String clusterIP;

    private String nodeSelector;

    private String hostName;

    private String logService;

    private String logPath;

    private String creator;

    private String createTime;

    private String updateTime;

    private boolean hostIPC;

    private boolean hostPID;

    private boolean hostNetwork;

    private Integer runningPods;

    private Integer pods;

    private String clusterId;

    private String clusterName;

    private String aliasName;

    private boolean isSystem;

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
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

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public List<CreateContainerDto> getContainers() {
        return containers;
    }

    public void setContainers(List<CreateContainerDto> containers) {
        this.containers = containers;
    }

    public String getSessionAffinity() {
        return sessionAffinity;
    }

    public void setSessionAffinity(String sessionAffinity) {
        this.sessionAffinity = sessionAffinity;
    }

    public String getClusterIP() {
        return clusterIP;
    }

    public void setClusterIP(String clusterIP) {
        this.clusterIP = clusterIP;
    }

    public String getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(String nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getLogService() {
        return logService;
    }

    public void setLogService(String logService) {
        this.logService = logService;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public boolean isHostIPC() {
        return hostIPC;
    }

    public void setHostIPC(boolean hostIPC) {
        this.hostIPC = hostIPC;
    }

    public boolean isHostPID() {
        return hostPID;
    }

    public void setHostPID(boolean hostPID) {
        this.hostPID = hostPID;
    }

    public boolean isHostNetwork() {
        return hostNetwork;
    }

    public void setHostNetwork(boolean hostNetwork) {
        this.hostNetwork = hostNetwork;
    }

    public Integer getTemplateGeneration() {
        return templateGeneration;
    }

    public void setTemplateGeneration(Integer templateGeneration) {
        this.templateGeneration = templateGeneration;
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

    public Integer getRunningPods() {
        return runningPods;
    }

    public void setRunningPods(Integer runningPods) {
        this.runningPods = runningPods;
    }

    public Integer getPods() {
        return pods;
    }

    public void setPods(Integer pods) {
        this.pods = pods;
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
