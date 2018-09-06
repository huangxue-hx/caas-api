package com.harmonycloud.service.platform.bean;

import com.harmonycloud.dto.application.AffinityDto;
import com.harmonycloud.k8s.bean.HostAlias;

import java.util.List;

public class UpdateDeployment {

	private String name;

	private String namespace;

	private String labels;

	private String instance;

	private String annotation;

	private List<UpdateContainer> containers;

	private String sessionAffinity;

	private String clusterIP;

	private String hostName;

	private String logService;

	private String logPath;

	/**节点亲和*/
	private List<AffinityDto> nodeAffinity;

	/**pod 亲和*/
	private AffinityDto podAffinity;

	/**pod 反亲和*/
	private AffinityDto podAntiAffinity;

	/**pod 是否分散*/
	private AffinityDto podDisperse;

	/**pod 按主机分组调度**/
	private AffinityDto podGroupSchedule;

	private String projectId;

	private List<HostAlias> hostAliases;

	public AffinityDto getPodGroupSchedule() {
		return podGroupSchedule;
	}

	public void setPodGroupSchedule(AffinityDto podGroupSchedule) {
		this.podGroupSchedule = podGroupSchedule;
	}

	public List<HostAlias> getHostAliases() {
		return hostAliases;
	}

	public void setHostAliases(List<HostAlias> hostAliases) {
		this.hostAliases = hostAliases;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
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

	public List<UpdateContainer> getContainers() {
		return containers;
	}

	public void setContainers(List<UpdateContainer> containers) {
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

	public List<AffinityDto> getNodeAffinity() {
		return nodeAffinity;
	}

	public void setNodeAffinity(List<AffinityDto> nodeAffinity) {
		this.nodeAffinity = nodeAffinity;
	}

	public AffinityDto getPodAffinity() {
		return podAffinity;
	}

	public void setPodAffinity(AffinityDto podAffinity) {
		this.podAffinity = podAffinity;
	}

	public AffinityDto getPodAntiAffinity() {
		return podAntiAffinity;
	}

	public void setPodAntiAffinity(AffinityDto podAntiAffinity) {
		this.podAntiAffinity = podAntiAffinity;
	}

	public AffinityDto getPodDisperse() {
		return podDisperse;
	}

	public void setPodDisperse(AffinityDto podDisperse) {
		this.podDisperse = podDisperse;
	}
}
