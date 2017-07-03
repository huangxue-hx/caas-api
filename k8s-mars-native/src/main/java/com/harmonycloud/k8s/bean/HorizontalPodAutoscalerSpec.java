package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorizontalPodAutoscalerSpec {

	private CrossVersionObjectReference scaleTargetRef;
	
	private Integer minReplicas;
	
	private Integer maxReplicas;
	
	private CPUTargetUtilization cpuUtilization;
	
	private Integer targetCPUUtilizationPercentage;

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

	public CPUTargetUtilization getCpuUtilization() {
		return cpuUtilization;
	}

	public void setCpuUtilization(CPUTargetUtilization cpuUtilization) {
		this.cpuUtilization = cpuUtilization;
	}

	public Integer getTargetCPUUtilizationPercentage() {
		return targetCPUUtilizationPercentage;
	}

	public void setTargetCPUUtilizationPercentage(Integer targetCPUUtilizationPercentage) {
		this.targetCPUUtilizationPercentage = targetCPUUtilizationPercentage;
	}

	public CrossVersionObjectReference getScaleTargetRef() {
		return scaleTargetRef;
	}

	public void setScaleTargetRef(CrossVersionObjectReference scaleTargetRef) {
		this.scaleTargetRef = scaleTargetRef;
	}
	
	
}
