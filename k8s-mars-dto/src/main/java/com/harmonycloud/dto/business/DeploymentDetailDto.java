package com.harmonycloud.dto.business;

import java.util.List;

public class DeploymentDetailDto {

	private String name;

	private String namespace;

	private String labels;

	private String instance;

	private String annotation;
	
    private String restartPolicy;

	private List<CreateContainerDto> containers;

	private String sessionAffinity;

	private String clusterIP;

	private String nodeSelector;
	
	private String hostName;

	private String logService;

	private String logPath;
	
	private boolean hostIPC;
	
	private boolean hostPID;
	
	private List<NodeAffinityDto> nodeAffinity;
	
	private List<PodAffinityDto> podAffinity;
	
	private List<PodAffinityDto> podAntiAffinity ;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CreateContainerDto> getContainers() {
		return containers;
	}

	public void setContainers(List<CreateContainerDto> containers) {
		this.containers = containers;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
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

    public String getNodeSelector() {
        return nodeSelector;
    }

    public void setNodeSelector(String nodeSelector) {
        this.nodeSelector = nodeSelector;
    }

	public String getRestartPolicy() {
		return restartPolicy;
	}

	public void setRestartPolicy(String restartPolicy) {
		this.restartPolicy = restartPolicy;
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

	public List<NodeAffinityDto> getNodeAffinity() {
		return nodeAffinity;
	}

	public void setNodeAffinity(List<NodeAffinityDto> nodeAffinity) {
		this.nodeAffinity = nodeAffinity;
	}

	public List<PodAffinityDto> getPodAffinity() {
		return podAffinity;
	}

	public void setPodAffinity(List<PodAffinityDto> podAffinity) {
		this.podAffinity = podAffinity;
	}

	public List<PodAffinityDto> getPodAntiAffinity() {
		return podAntiAffinity;
	}

	public void setPodAntiAffinity(List<PodAffinityDto> podAntiAffinity) {
		this.podAntiAffinity = podAntiAffinity;
	}

}
