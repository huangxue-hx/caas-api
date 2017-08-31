package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetricSpec {


	private String type;

	private CustomMetricSource custom;

	private ResourceMetricSource resource;

	private TimeMetricSource time;

	public MetricSpec() {
	}

	public MetricSpec(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public CustomMetricSource getCustom() {
		return custom;
	}

	public void setCustom(CustomMetricSource custom) {
		this.custom = custom;
	}

	public ResourceMetricSource getResource() {
		return resource;
	}

	public void setResource(ResourceMetricSource resource) {
		this.resource = resource;
	}

	public TimeMetricSource getTime() {
		return time;
	}

	public void setTime(TimeMetricSource time) {
		this.time = time;
	}
}
