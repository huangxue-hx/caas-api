package com.harmonycloud.k8s.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeStatus {

	private Object capacity;
	
	private Object allocatable;
	
	private String phase;
	
	private List<NodeCondition> conditions;
	
	private List<NodeAddress> addresses;
	
	private NodeDaemonEndpoints daemonEndpoints;
	
	private NodeSystemInfo nodeInfo;
	
	private List<ContainerImage>images;
	
	private List<String> volumesInUse;
	
	private List<AttachedVolume> volumesAttached;

	public List<AttachedVolume> getVolumesAttached() {
		return volumesAttached;
	}

	public void setVolumesAttached(List<AttachedVolume> volumesAttached) {
		this.volumesAttached = volumesAttached;
	}

	public Object getCapacity() {
		return capacity;
	}

	public void setCapacity(Object capacity) {
		this.capacity = capacity;
	}

	public Object getAllocatable() {
		return allocatable;
	}

	public void setAllocatable(Object allocatable) {
		this.allocatable = allocatable;
	}

	public String getPhase() {
		return phase;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}

	public List<NodeCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<NodeCondition> conditions) {
		this.conditions = conditions;
	}

	public List<NodeAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(List<NodeAddress> addresses) {
		this.addresses = addresses;
	}

	public NodeDaemonEndpoints getDaemonEndpoints() {
		return daemonEndpoints;
	}

	public void setDaemonEndpoints(NodeDaemonEndpoints daemonEndpoints) {
		this.daemonEndpoints = daemonEndpoints;
	}

	public NodeSystemInfo getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(NodeSystemInfo nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	public List<ContainerImage> getImages() {
		return images;
	}

	public void setImages(List<ContainerImage> images) {
		this.images = images;
	}

	public List<String> getVolumesInUse() {
		return volumesInUse;
	}

	public void setVolumesInUse(List<String> volumesInUse) {
		this.volumesInUse = volumesInUse;
	}
}
