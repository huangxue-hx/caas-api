package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeClaimStatus {

	private String phase;
	
	private List<String> accessModes;
	
	private Object capacity;

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public List<String> getAccessModes() {
		return accessModes;
	}

	public void setAccessModes(List<String> accessModes) {
		this.accessModes = accessModes;
	}

	public Object getCapacity() {
		return capacity;
	}

	public void setCapacity(Object capacity) {
		this.capacity = capacity;
	}
	
}
