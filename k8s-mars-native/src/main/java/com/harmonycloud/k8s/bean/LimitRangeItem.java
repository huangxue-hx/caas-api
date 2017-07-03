package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitRangeItem {

	private Object type;
	
	private Object max;
	
	private Object min;
	
//	private Object default;
	
	private Object defaultRequest;
	
	private Object maxLimitRequestRatio;

	public Object getType() {
		return type;
	}

	public void setType(Object type) {
		this.type = type;
	}

	public Object getMax() {
		return max;
	}

	public void setMax(Object max) {
		this.max = max;
	}

	public Object getMin() {
		return min;
	}

	public void setMin(Object min) {
		this.min = min;
	}

	public Object getDefaultRequest() {
		return defaultRequest;
	}

	public void setDefaultRequest(Object defaultRequest) {
		this.defaultRequest = defaultRequest;
	}

	public Object getMaxLimitRequestRatio() {
		return maxLimitRequestRatio;
	}

	public void setMaxLimitRequestRatio(Object maxLimitRequestRatio) {
		this.maxLimitRequestRatio = maxLimitRequestRatio;
	}
	
}
