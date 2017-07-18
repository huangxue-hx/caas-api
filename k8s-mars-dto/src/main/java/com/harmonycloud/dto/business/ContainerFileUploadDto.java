package com.harmonycloud.dto.business;

import java.io.Serializable;
import java.util.List;

public class ContainerFileUploadDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String namespace;
	
	private String deployment;
	
	private String containerFilePath;
	
	private List<PodContainerDto> pods;
	
	private boolean continueLast;
	
	private List<Integer> uploadIdList;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getDeployment() {
		return deployment;
	}

	public void setDeployment(String deployment) {
		this.deployment = deployment;
	}

	public String getContainerFilePath() {
		return containerFilePath;
	}

	public void setContainerFilePath(String containerFilePath) {
		this.containerFilePath = containerFilePath;
	}

	public List<PodContainerDto> getPods() {
		return pods;
	}

	public void setPods(List<PodContainerDto> pods) {
		this.pods = pods;
	}

	public boolean isContinueLast() {
		return continueLast;
	}

	public void setContinueLast(boolean continueLast) {
		this.continueLast = continueLast;
	}

	public List<Integer> getUploadIdList() {
		return uploadIdList;
	}

	public void setUploadIdList(List<Integer> uploadIdList) {
		this.uploadIdList = uploadIdList;
	}

}
