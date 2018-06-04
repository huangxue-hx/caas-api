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
public class PolicyRule {

	private List<String> verbs;
	
	private Object attributeRestrictions;
	
	private List<String> apiGroups;
	
	private List<String> resources;
	
	private List<String> nonResourceURLs;

	public List<String> getVerbs() {
		return verbs;
	}

	public void setVerbs(List<String> verbs) {
		this.verbs = verbs;
	}

	public Object getAttributeRestrictions() {
		return attributeRestrictions;
	}

	public void setAttributeRestrictions(Object attributeRestrictions) {
		this.attributeRestrictions = attributeRestrictions;
	}

	public List<String> getApiGroups() {
		return apiGroups;
	}

	public void setApiGroups(List<String> apiGroups) {
		this.apiGroups = apiGroups;
	}

	public List<String> getResources() {
		return resources;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
	}

	public List<String> getNonResourceURLs() {
		return nonResourceURLs;
	}

	public void setNonResourceURLs(List<String> nonResourceURLs) {
		this.nonResourceURLs = nonResourceURLs;
	}
	
	
}
