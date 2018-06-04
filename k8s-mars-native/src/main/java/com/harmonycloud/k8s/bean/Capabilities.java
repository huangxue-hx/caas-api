package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author jmi
 *
 */
@JsonInclude(value=JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Capabilities {
	
	private List<String> add;
	
	private List<String> drop;

	public List<String> getAdd() {
		return add;
	}

	public void setAdd(List<String> add) {
		this.add = add;
	}

	public List<String> getDrop() {
		return drop;
	}

	public void setDrop(List<String> drop) {
		this.drop = drop;
	}
	
}
