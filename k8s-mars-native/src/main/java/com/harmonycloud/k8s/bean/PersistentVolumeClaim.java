package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.harmonycloud.k8s.constant.Constant;

/**
 * @author qg
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentVolumeClaim extends BaseResource{

	private PersistentVolumeClaimSpec spec;
	
	private PersistentVolumeClaimStatus status;
	
	public PersistentVolumeClaim() {
		this.setKind("PersistentVolumeClaim");
		this.setApiVersion(Constant.PVC_VERSION);
	}

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
