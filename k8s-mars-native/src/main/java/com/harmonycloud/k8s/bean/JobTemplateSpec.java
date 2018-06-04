package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobTemplateSpec {
	
	private ObjectMeta metadata ;

	private JobSpec spec;
	
	public ObjectMeta getMetadata() {
		return metadata;
	}

	public void setMetadata(ObjectMeta metadata) {
		this.metadata = metadata;
	}

	public JobSpec getSpec() {
		return spec;
	}

	public void setSpec(JobSpec spec) {
		this.spec = spec;
	} 
}
