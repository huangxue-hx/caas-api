package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 * rc中Spec对象
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplicationControllerSpec {

	private int replicas;
	
	private Object selector;
	
	private PodTemplateSpec template;

	public int getReplicas() {
		return replicas;
	}

	public void setReplicas(int replicas) {
		this.replicas = replicas;
	}

	public Object getSelector() {
		return selector;
	}

	public void setSelector(Object selector) {
		this.selector = selector;
	}

	public PodTemplateSpec getTemplate() {
		return template;
	}

	public void setTemplate(PodTemplateSpec template) {
		this.template = template;
	}
	
}
