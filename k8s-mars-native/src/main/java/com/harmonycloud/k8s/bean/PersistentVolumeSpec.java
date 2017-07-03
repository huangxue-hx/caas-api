package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeSpec {

	private Object capacity;
	
	private NFSVolumeSource nfs;
	
	private List<String> accessModes;
	
	private ObjectReference claimRef;
	
	private String persistentVolumeReclaimPolicy;

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

	public NFSVolumeSource getNfs() {
		return nfs;
	}

	public void setNfs(NFSVolumeSource nfs) {
		this.nfs = nfs;
	}

	public ObjectReference getClaimRef() {
		return claimRef;
	}

	public void setClaimRef(ObjectReference claimRef) {
		this.claimRef = claimRef;
	}

	public String getPersistentVolumeReclaimPolicy() {
		return persistentVolumeReclaimPolicy;
	}

	public void setPersistentVolumeReclaimPolicy(String persistentVolumeReclaimPolicy) {
		this.persistentVolumeReclaimPolicy = persistentVolumeReclaimPolicy;
	}
	
	
}
