package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerImage {

	private List<String> names;
	
	private Integer sizeBytes;

	public List<String> getNames() {
		return names;
	}

	public void setNames(List<String> names) {
		this.names = names;
	}

	public Integer getSizeBytes() {
		return sizeBytes;
	}

	public void setSizeBytes(Integer sizeBytes) {
		this.sizeBytes = sizeBytes;
	}
	
	
}
