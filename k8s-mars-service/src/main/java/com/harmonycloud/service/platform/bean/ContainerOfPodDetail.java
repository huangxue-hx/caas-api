package com.harmonycloud.service.platform.bean;

import java.util.List;
import java.util.Map;

import com.harmonycloud.dto.business.SecurityContextDto;
import com.harmonycloud.k8s.bean.ContainerPort;
import com.harmonycloud.k8s.bean.EnvVar;
import com.harmonycloud.k8s.bean.Probe;
import com.harmonycloud.k8s.bean.SecurityContext;

public class ContainerOfPodDetail {
	
	private String name;

	private String deploymentName;
	
	private String img;
	
	private Map<String, Object> resource;
	
	private Probe livenessProbe;
	
	private Probe readinessProbe;
	
	private List<ContainerPort> ports;
	
	private List<String> args;
	
	private List<EnvVar> env;
	
	private List<String> command;
	
	private List<VolumeMountExt> storage;
	
	private Map<String, Object> configmap;
	
	private String restartCount;
	
	private SecurityContextDto securityContext;

	public ContainerOfPodDetail() {
		
	}
	
	public ContainerOfPodDetail(String name, String img, Probe livenessProbe, Probe readinessProbe,
			List<ContainerPort> ports, List<String> args, List<EnvVar> env, List<String> command) {
		this.name = name;
		this.img = img;
		this.livenessProbe = livenessProbe;
		this.readinessProbe = readinessProbe;
		this.ports = ports;
		this.args = args;
		this.env = env;
		this.command = command;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDeploymentName() {
		return deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public Map<String, Object> getResource() {
		return resource;
	}

	public void setResource(Map<String, Object> resource) {
		this.resource = resource;
	}

	public Probe getLivenessProbe() {
		return livenessProbe;
	}

	public void setLivenessProbe(Probe livenessProbe) {
		this.livenessProbe = livenessProbe;
	}

	public Probe getReadinessProbe() {
		return readinessProbe;
	}

	public void setReadinessProbe(Probe readinessProbe) {
		this.readinessProbe = readinessProbe;
	}

	public List<ContainerPort> getPorts() {
		return ports;
	}

	public void setPorts(List<ContainerPort> ports) {
		this.ports = ports;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
	}

	public List<EnvVar> getEnv() {
		return env;
	}

	public void setEnv(List<EnvVar> env) {
		this.env = env;
	}

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

	public List<VolumeMountExt> getStorage() {
		return storage;
	}

	public void setStorage(List<VolumeMountExt> storage) {
		this.storage = storage;
	}

	public Map<String, Object> getConfigmap() {
		return configmap;
	}

	public void setConfigmap(Map<String, Object> configmap) {
		this.configmap = configmap;
	}

	public String getRestartCount() {
		return restartCount;
	}

	public void setRestartCount(String restartCount) {
		this.restartCount = restartCount;
	}

	public SecurityContextDto getSecurityContext() {
		return securityContext;
	}

	public void setSecurityContext(SecurityContextDto securityContext) {
		this.securityContext = securityContext;
	}

}
