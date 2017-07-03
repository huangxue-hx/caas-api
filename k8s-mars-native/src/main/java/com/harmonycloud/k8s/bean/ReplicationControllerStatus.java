package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 * rc
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicationControllerStatus{

	private Integer replicas;
	
	private Integer fullyLabeledReplicas;
	
	private Integer readyReplicas;
	
	private Integer availableReplicas;
	
	private Integer observedGeneration;
	
	private List<ReplicationControllerCondition> conditions;

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public Integer getFullyLabeledReplicas() {
		return fullyLabeledReplicas;
	}

	public void setFullyLabeledReplicas(Integer fullyLabeledReplicas) {
		this.fullyLabeledReplicas = fullyLabeledReplicas;
	}

	public Integer getReadyReplicas() {
		return readyReplicas;
	}

	public void setReadyReplicas(Integer readyReplicas) {
		this.readyReplicas = readyReplicas;
	}

	public Integer getAvailableReplicas() {
		return availableReplicas;
	}

	public void setAvailableReplicas(Integer availableReplicas) {
		this.availableReplicas = availableReplicas;
	}

	public Integer getObservedGeneration() {
		return observedGeneration;
	}

	public void setObservedGeneration(Integer observedGeneration) {
		this.observedGeneration = observedGeneration;
	}

	public List<ReplicationControllerCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<ReplicationControllerCondition> conditions) {
		this.conditions = conditions;
	}
	
	
}
