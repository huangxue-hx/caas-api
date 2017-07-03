package com.harmonycloud.k8s.bean;

/**
 * 
 * @author jmi
 *
 */
public class Capability {
	
	private String name;
	
	private boolean namespaced;
	
	private String kind;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNamespaced() {
		return namespaced;
	}

	public void setNamespaced(boolean namespaced) {
		this.namespaced = namespaced;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

}
