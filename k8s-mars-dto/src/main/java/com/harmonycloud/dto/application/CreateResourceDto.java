package com.harmonycloud.dto.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateResourceDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String cpu;
	
	private String memory;

	private String gpu;
	
	private int currentRate;

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getGpu() {
		return gpu;
	}

	public void setGpu(String gpu) {
		this.gpu = gpu;
	}

	public int getCurrentRate() {
		return currentRate;
	}

	public void setCurrentRate(int currentRate) {
		this.currentRate = currentRate;
	}

}
