package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeClaim extends BaseResource{

	private PersistentVolumeClaimSpec spec;
	
	private PersistentVolumeClaimStatus status;

	public PersistentVolumeClaimSpec getSpec() {
		return spec;
	}

	public void setSpec(PersistentVolumeClaimSpec spec) {
		this.spec = spec;
	}

	public PersistentVolumeClaimStatus getStatus() {
		return status;
	}

	public void setStatus(PersistentVolumeClaimStatus status) {
		this.status = status;
	}
	
}
