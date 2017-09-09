package com.harmonycloud.dto.business;

import java.util.List;

public class NodeAffinityDto {
	
	private int weight;
	
	private String type;
	
	private List<NodeSelectorTermDto> nst;
	
	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<NodeSelectorTermDto> getNst() {
		return nst;
	}

	public void setNst(List<NodeSelectorTermDto> nst) {
		this.nst = nst;
	}
}
