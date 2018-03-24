package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PodStatus {
	
	private String phase;
	
	private List<PodCondition> conditions;
	
	private String message;
	
	private String reason;
	
	private String hostIP;
	
	private String podIP;
	
	private String startTime;
	
	private List<ContainerStatus> containerStatuses;
	
	private List<ContainerStatus> initContainerStatuses;
	
	private String qosClass;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public List<PodCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<PodCondition> conditions) {
		this.conditions = conditions;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public String getPodIP() {
		return podIP;
	}

	public void setPodIP(String podIP) {
		this.podIP = podIP;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public List<ContainerStatus> getContainerStatuses() {
		return containerStatuses;
	}

	public void setContainerStatuses(List<ContainerStatus> containerStatuses) {
		this.containerStatuses = containerStatuses;
	}

	public List<ContainerStatus> getInitContainerStatuses() {
		return initContainerStatuses;
	}

	public void setInitContainerStatuses(List<ContainerStatus> initContainerStatuses) {
		this.initContainerStatuses = initContainerStatuses;
	}

	public String getQosClass() {
		return qosClass;
	}

	public void setQosClass(String qosClass) {
		this.qosClass = qosClass;
	}
	
	
}
