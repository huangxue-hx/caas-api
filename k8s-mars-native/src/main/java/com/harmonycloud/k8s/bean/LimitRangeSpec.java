package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitRangeSpec {

	private List<LimitRangeItem> limits;

	public List<LimitRangeItem> getLimits() {
		return limits;
	}

	public void setLimits(List<LimitRangeItem> limits) {
		this.limits = limits;
	}
	
}
