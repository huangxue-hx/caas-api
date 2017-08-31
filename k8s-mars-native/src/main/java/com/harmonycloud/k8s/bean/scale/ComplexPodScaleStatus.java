package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexPodScaleStatus {
	
	private String lastScaleTime;
	
	private Integer currentReplicas;
	
	private Integer desiredReplicas;
	
	private List<MetricStatus> currentMetrics;

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

	public List<MetricStatus> getCurrentMetrics() {
		return currentMetrics;
	}

	public void setCurrentMetrics(List<MetricStatus> currentMetrics) {
		this.currentMetrics = currentMetrics;
	}
}
