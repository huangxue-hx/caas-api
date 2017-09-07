package com.harmonycloud.service.platform.bean;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.ServicePort;

/**
 * 
 * @author jmi
 *
 */
public class AppDetail {
	
	private String clusterIP;
	
	private String serviceAddress;
	
	private List<ServicePort> internalPorts;
	
	private String sessionAffinity;
	
	private String name;
	
	private String namespace;
	
	private String version;
	
	private String createTime;
	
	private String updateTime;
	
	private Integer instance;
	
	private String owner;
	
	private String hostName;
	
	private Map<String, Object> labels;
	
	private String status;
	
	private String annotation;
	
	private List<PodDetail> podList;
	
	private List<EventDetail> events;

	private AutoScaleDto autoScale;
	
	private String restartPolicy;
	
	private List<Event> autoScalingHistory;
	
	private boolean hostIPC;
	
	private boolean hostPID;

	public String getClusterIP() {
		return clusterIP;
	}

	public void setClusterIP(String clusterIP) {
		this.clusterIP = clusterIP;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public List<ServicePort> getInternalPorts() {
		return internalPorts;
	}

	public void setInternalPorts(List<ServicePort> internalPorts) {
		this.internalPorts = internalPorts;
	}

	public String getSessionAffinity() {
		return sessionAffinity;
	}

	public void setSessionAffinity(String sessionAffinity) {
		this.sessionAffinity = sessionAffinity;
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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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

	public Integer getInstance() {
		return instance;
	}

	public void setInstance(Integer instance) {
		this.instance = instance;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public Map<String, Object> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Object> labels) {
		this.labels = labels;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public List<PodDetail> getPodList() {
		return podList;
	}

	public void setPodList(List<PodDetail> podList) {
		this.podList = podList;
	}

	public List<EventDetail> getEvents() {
		return events;
	}

	public void setEvents(List<EventDetail> events) {
		this.events = events;
	}

	public List<Event> getAutoScalingHistory() {
		return autoScalingHistory;
	}

	public void setAutoScalingHistory(List<Event> autoScalingHistory) {
		this.autoScalingHistory = autoScalingHistory;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public AutoScaleDto getAutoScale() {
		return autoScale;
	}

	public void setAutoScale(AutoScaleDto autoScale) {
		this.autoScale = autoScale;
	}
}
