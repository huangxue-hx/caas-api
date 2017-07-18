package com.harmonycloud.dto.business;

import java.io.Serializable;
import java.util.List;


public class PodContainerDto implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1727526929211808355L;

	private String name;
	
	private List<String> container;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getContainer() {
		return container;
	}

	public void setContainer(List<String> container) {
		this.container = container;
	}

}
