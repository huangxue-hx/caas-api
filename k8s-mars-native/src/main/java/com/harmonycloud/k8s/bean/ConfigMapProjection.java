package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigMapProjection {

	private List<KeyToPath> items;
	
	private String name;
	
	private boolean optional;

	public List<KeyToPath> getItems() {
		return items;
	}

	public void setItems(List<KeyToPath> items) {
		this.items = items;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	
	
}
