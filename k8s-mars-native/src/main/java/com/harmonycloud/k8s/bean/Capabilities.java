package com.harmonycloud.k8s.bean;

import java.util.List;

/**
 * 
 * @author jmi
 *
 */
public class Capabilities {
	
	/*private List<Capability> add;
	
	private List<Capability> drop;

	public List<Capability> getAdd() {
		return add;
	}

	public void setAdd(List<Capability> add) {
		this.add = add;
	}

	public List<Capability> getDrop() {
		return drop;
	}

	public void setDrop(List<Capability> drop) {
		this.drop = drop;
	}*/
	
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
