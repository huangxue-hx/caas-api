package com.harmonycloud.service.platform.bean;

import java.util.List;

/**
 * 
 * @author jmi
 *
 */
public class HpAutoScaling {
	
	private Integer targetCpu;
	
	private List<Integer> instanceRange;
	
	private Integer cpu;
	
	private String scaleTime;
	
	public HpAutoScaling() {
		
	}
	
	public HpAutoScaling(Integer targetCpu, List<Integer> instanceRange, Integer cpu, String scaleTime) {
		this.cpu = cpu;
		this.targetCpu = targetCpu;
		this.instanceRange = instanceRange;
		this.scaleTime = scaleTime;
	}

	public Integer getTargetCpu() {
		return targetCpu;
	}

	public void setTargetCpu(Integer targetCpu) {
		this.targetCpu = targetCpu;
	}

	public List<Integer> getInstanceRange() {
		return instanceRange;
	}

	public void setInstanceRange(List<Integer> instanceRange) {
		this.instanceRange = instanceRange;
	}

	public Integer getCpu() {
		return cpu;
	}

	public void setCpu(Integer cpu) {
		this.cpu = cpu;
	}

	public String getScaleTime() {
		return scaleTime;
	}

	public void setScaleTime(String scaleTime) {
		this.scaleTime = scaleTime;
	}
	
	

}
