package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeClaimStatus {

	private String phase;
	
	private List<String> accessModes;
	
	private Object capacity;
	
	private List<PersistentVolumeClaimCondition> conditions;

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

	public List<PersistentVolumeClaimCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<PersistentVolumeClaimCondition> conditions) {
		this.conditions = conditions;
	}
	
}
