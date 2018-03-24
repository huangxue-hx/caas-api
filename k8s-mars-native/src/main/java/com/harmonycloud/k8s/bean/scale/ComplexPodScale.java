package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.bean.BaseResource;

/**
 * @author qg
 *
 */
@JsonInclude(value= JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexPodScale extends BaseResource {

	private ComplexPodScaleSpec spec;
	
	private ComplexPodScaleStatus status;

	public ComplexPodScaleSpec getSpec() {
		return spec;
	}

	public void setSpec(ComplexPodScaleSpec spec) {
		this.spec = spec;
	}

	public ComplexPodScaleStatus getStatus() {
		return status;
	}

	public void setStatus(ComplexPodScaleStatus status) {
		this.status = status;
	}
}
