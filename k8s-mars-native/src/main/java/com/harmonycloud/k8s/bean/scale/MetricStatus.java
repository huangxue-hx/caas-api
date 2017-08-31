package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricStatus {


	private String type;

	private CustomMetricStatus custom;

	private ResourceMetricStatus resource;

	private TimeMetricStatus time;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CustomMetricStatus getCustom() {
		return custom;
	}

	public void setCustom(CustomMetricStatus custom) {
		this.custom = custom;
	}

	public ResourceMetricStatus getResource() {
		return resource;
	}

	public void setResource(ResourceMetricStatus resource) {
		this.resource = resource;
	}

	public TimeMetricStatus getTime() {
		return time;
	}

	public void setTime(TimeMetricStatus time) {
		this.time = time;
	}
}
