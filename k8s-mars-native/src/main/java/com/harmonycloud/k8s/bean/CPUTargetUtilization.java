package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CPUTargetUtilization {

	private Integer targetPercentage;

	public Integer getTargetPercentage() {
		return targetPercentage;
	}

	public void setTargetPercentage(Integer targetPercentage) {
		this.targetPercentage = targetPercentage;
	}
	
}
