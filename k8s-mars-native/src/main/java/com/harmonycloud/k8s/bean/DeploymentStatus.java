package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeploymentStatus {

	private Integer observedGeneration;
	
	private Integer replicas;
	
	private Integer updatedReplicas	;
	
	private Integer availableReplicas;
	
	private Integer unavailableReplicas;
	
	private List<DeploymentCondition> conditions;

	public Integer getObservedGeneration() {
		return observedGeneration;
	}

	public void setObservedGeneration(Integer observedGeneration) {
		this.observedGeneration = observedGeneration;
	}

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public Integer getUpdatedReplicas() {
		return updatedReplicas;
	}

	public void setUpdatedReplicas(Integer updatedReplicas) {
		this.updatedReplicas = updatedReplicas;
	}

	public Integer getAvailableReplicas() {
		return availableReplicas;
	}

	public void setAvailableReplicas(Integer availableReplicas) {
		this.availableReplicas = availableReplicas;
	}

	public Integer getUnavailableReplicas() {
		return unavailableReplicas;
	}

	public void setUnavailableReplicas(Integer unavailableReplicas) {
		this.unavailableReplicas = unavailableReplicas;
	}

	public List<DeploymentCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<DeploymentCondition> conditions) {
		this.conditions = conditions;
	}
	
	
}
