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
public class PersistentVolume extends BaseResource{
	
	private PersistentVolumeSpec spec;
	
	private PersistentVolumeStatus status;
	
	public PersistentVolumeStatus getStatus() {
		return status;
	}

	public void setStatus(PersistentVolumeStatus status) {
		this.status = status;
	}

	public PersistentVolumeSpec getSpec() {
		return spec;
	}

	public void setSpec(PersistentVolumeSpec spec) {
		this.spec = spec;
	}

	public PersistentVolume(){
		this.setKind("PersistentVolume");
		this.setApiVersion(Constant.PV_API_VERSION);
	}

}
