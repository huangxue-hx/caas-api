package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorizontalPodAutoscalerStatus {

	private Integer observedGeneration;
	
	private String lastScaleTime;
	
	private Integer currentReplicas;
	
	private Integer desiredReplicas;
	
	private Integer currentCPUUtilizationPercentage;

	public Integer getObservedGeneration() {
		return observedGeneration;
	}

	public void setObservedGeneration(Integer observedGeneration) {
		this.observedGeneration = observedGeneration;
	}

	public String getLastScaleTime() {
		return lastScaleTime;
	}

	public void setLastScaleTime(String lastScaleTime) {
		this.lastScaleTime = lastScaleTime;
	}

	public Integer getCurrentReplicas() {
		return currentReplicas;
	}

	public void setCurrentReplicas(Integer currentReplicas) {
		this.currentReplicas = currentReplicas;
	}

	public Integer getDesiredReplicas() {
		return desiredReplicas;
	}

	public void setDesiredReplicas(Integer desiredReplicas) {
		this.desiredReplicas = desiredReplicas;
	}

	public Integer getCurrentCPUUtilizationPercentage() {
		return currentCPUUtilizationPercentage;
	}

	public void setCurrentCPUUtilizationPercentage(Integer currentCPUUtilizationPercentage) {
		this.currentCPUUtilizationPercentage = currentCPUUtilizationPercentage;
	}
	
	
}
