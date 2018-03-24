package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.k8s.bean.scale.MetricStatus;

import java.util.List;

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

	private List<HorizontalPodAutoscalerCondition> conditions;
	
	private List<HorizontalPodAutoscalerMetricStatus> currentMetrics;

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

	public List<HorizontalPodAutoscalerCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<HorizontalPodAutoscalerCondition> conditions) {
		this.conditions = conditions;
	}

	public List<HorizontalPodAutoscalerMetricStatus> getCurrentMetrics() {
		return currentMetrics;
	}

	public void setCurrentMetrics(List<HorizontalPodAutoscalerMetricStatus> currentMetrics) {
		this.currentMetrics = currentMetrics;
	}
}
