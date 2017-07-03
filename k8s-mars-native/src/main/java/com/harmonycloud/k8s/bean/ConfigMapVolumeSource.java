package com.harmonycloud.k8s.bean;

import java.util.List;

/**
 * 
 * @author jmi
 *
 */
public class ConfigMapVolumeSource {
	
	private String name;
	
	private List<KeyToPath> items;
	
	private Integer defaultMode;

	private boolean optional;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KeyToPath> getItems() {
		return items;
	}

	public void setItems(List<KeyToPath> items) {
		this.items = items;
	}

	public Integer getDefaultMode() {
		return defaultMode;
	}

	public void setDefaultMode(Integer defaultMode) {
		this.defaultMode = defaultMode;
	}

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}
}
