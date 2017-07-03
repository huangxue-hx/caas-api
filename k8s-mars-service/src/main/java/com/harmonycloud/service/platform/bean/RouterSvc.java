package com.harmonycloud.service.platform.bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.harmonycloud.k8s.bean.ServicePort;

public class RouterSvc implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String namespace;
	
	private String name;
	
	private String createTime;
	
	private Map<String, Object> labels;
	
	private Object selector;
	
	private List<ServicePort> rules;
	
	private String annotation;
	
	private String service;

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public Object getSelector() {
		return selector;
	}

	public void setSelector(Object selector) {
		this.selector = selector;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Map<String, Object> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, Object> labels) {
		this.labels = labels;
	}

	public List<ServicePort> getRules() {
		return rules;
	}

	public void setRules(List<ServicePort> rules) {
		this.rules = rules;
	}

}
