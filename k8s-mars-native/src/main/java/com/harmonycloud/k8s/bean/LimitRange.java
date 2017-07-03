package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LimitRange extends BaseResource{
	
	private List<LimitRangeSpec> spec;

	public List<LimitRangeSpec> getSpec() {
		return spec;
	}

	public void setSpec(List<LimitRangeSpec> spec) {
		this.spec = spec;
	}

}
