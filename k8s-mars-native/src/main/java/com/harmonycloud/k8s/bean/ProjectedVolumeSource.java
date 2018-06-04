package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectedVolumeSource {
	
	private Integer defaultMode;
	
	private List<VolumeProjection> sources;
}
