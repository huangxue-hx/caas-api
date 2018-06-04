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
public class DeploymentStatus {

	private Integer observedGeneration;
	
	private Integer replicas;
	
	private Integer updatedReplicas	;
	
	private Integer availableReplicas;
	
	private Integer unavailableReplicas;
	
	private List<DeploymentCondition> conditions;
	
    private Integer collisionCount;
	
	private Integer readyReplicas;

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

	public Integer getCollisionCount() {
		return collisionCount;
	}

	public void setCollisionCount(Integer collisionCount) {
		this.collisionCount = collisionCount;
	}

	public Integer getReadyReplicas() {
		return readyReplicas;
	}

	public void setReadyReplicas(Integer readyReplicas) {
		this.readyReplicas = readyReplicas;
	}
	
	
}
