package com.harmonycloud.dto.business;

import java.util.List;

public class PodAffinityDto {
	
	private  List<NodeSelectorTermDto> labelSelector ;
	
	private String topologyKey;
	
	private List<String> namespaces ;
	
	private int weight;
	
	private String type;

	public String getTopologyKey() {
		return topologyKey;
	}

	public void setTopologyKey(String topologyKey) {
		this.topologyKey = topologyKey;
	}

	public List<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(List<String> namespaces) {
		this.namespaces = namespaces;
	}

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

	public List<NodeSelectorTermDto> getLabelSelector() {
		return labelSelector;
	}

	public void setLabelSelector(List<NodeSelectorTermDto> labelSelector) {
		this.labelSelector = labelSelector;
	}
}
