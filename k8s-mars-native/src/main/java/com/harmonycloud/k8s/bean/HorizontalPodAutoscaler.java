package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorizontalPodAutoscaler extends BaseResource{

	private HorizontalPodAutoscalerSpec spec;
	
	private HorizontalPodAutoscalerStatus status;

	public HorizontalPodAutoscalerSpec getSpec() {
		return spec;
	}

	public void setSpec(HorizontalPodAutoscalerSpec spec) {
		this.spec = spec;
	}

	public HorizontalPodAutoscalerStatus getStatus() {
		return status;
	}

	public void setStatus(HorizontalPodAutoscalerStatus status) {
		this.status = status;
	}
	
	
	
}
