package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorizontalPodAutoscalerSpec {

	private CrossVersionObjectReference scaleTargetRef;
	
	private Integer minReplicas;
	
	private Integer maxReplicas;

	private List<MetricSpec> metrics;
	
//	private CPUTargetUtilization cpuUtilization;
//
//	private Integer targetCPUUtilizationPercentage;


	public CrossVersionObjectReference getScaleTargetRef() {
		return scaleTargetRef;
	}

	public void setScaleTargetRef(CrossVersionObjectReference scaleTargetRef) {
		this.scaleTargetRef = scaleTargetRef;
	}

	public Integer getMinReplicas() {
		return minReplicas;
	}

	public void setMinReplicas(Integer minReplicas) {
		this.minReplicas = minReplicas;
	}

	public Integer getMaxReplicas() {
		return maxReplicas;
	}

	public void setMaxReplicas(Integer maxReplicas) {
		this.maxReplicas = maxReplicas;
	}

	public List<MetricSpec> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<MetricSpec> metrics) {
		this.metrics = metrics;
	}
}
