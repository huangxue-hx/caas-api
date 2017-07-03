package com.harmonycloud.k8s.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerStatus {

	private String name;
	
	private ContainerState state;
	
	private ContainerState lastState;
	
	private Boolean ready;
	
	private Integer restartCount;
	
	private String image;
	
	private String imageID;
	
	private String containerID;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ContainerState getState() {
		return state;
	}

	public void setState(ContainerState state) {
		this.state = state;
	}

	public ContainerState getLastState() {
		return lastState;
	}

	public void setLastState(ContainerState lastState) {
		this.lastState = lastState;
	}

	public Boolean getReady() {
		return ready;
	}

	public void setReady(Boolean ready) {
		this.ready = ready;
	}

	public Integer getRestartCount() {
		return restartCount;
	}

	public void setRestartCount(Integer restartCount) {
		this.restartCount = restartCount;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getImageID() {
		return imageID;
	}

	public void setImageID(String imageID) {
		this.imageID = imageID;
	}

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}
}
