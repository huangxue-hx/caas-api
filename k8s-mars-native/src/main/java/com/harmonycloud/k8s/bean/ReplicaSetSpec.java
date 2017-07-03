package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicaSetSpec {

	private Integer replicas;
	
	private Integer minReadySeconds;
	
	private LabelSelector selector;
	
	private PodTemplateSpec template;

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public Integer getMinReadySeconds() {
		return minReadySeconds;
	}

	public void setMinReadySeconds(Integer minReadySeconds) {
		this.minReadySeconds = minReadySeconds;
	}

	public LabelSelector getSelector() {
		return selector;
	}

	public void setSelector(LabelSelector selector) {
		this.selector = selector;
	}

	public PodTemplateSpec getTemplate() {
		return template;
	}

	public void setTemplate(PodTemplateSpec template) {
		this.template = template;
	}
	
	
}
