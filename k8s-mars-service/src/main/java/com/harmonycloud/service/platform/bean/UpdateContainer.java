package com.harmonycloud.service.platform.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.Probe;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateContainer{

	private String name;

	private String img;

	private String tag;

	private CreateResourceDto resource;
	
	private CreateResourceDto limit;

//	private String log;

	private LogVolume log;

	private List<CreatePortDto> ports;

	private List<CreateConfigMapDto> configmap;

	private List<String> command;

	private List<String> args;

	private Probe livenessProbe;

	private Probe readinessProbe;

	private List<CreateEnvDto> env;

	private List<PersistentVolumeDto> storage;
	
	private SecurityContextDto securityContext;
	
	private String imagePullPolicy;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public CreateResourceDto getResource() {
		return resource;
	}

	public void setResource(CreateResourceDto resource) {
		this.resource = resource;
	}

//	public String getLog() {
//		return log;
//	}
//
//	public void setLog(String log) {
//		this.log = log;
//	}

	public List<CreatePortDto> getPorts() {
		return ports;
	}

	public void setPorts(List<CreatePortDto> ports) {
		this.ports = ports;
	}

	public LogVolume getLog() {
		return log;
	}

	public void setLog(LogVolume log) {
		this.log = log;
	}

	//	public CreateConfigMap getConfigmap() {
//		return configmap;
//	}
//
//	public void setConfigmap(CreateConfigMap configmap) {
//		this.configmap = configmap;
//	}


	public List<CreateConfigMapDto> getConfigmap() {
		return configmap;
	}

	public void setConfigmap(List<CreateConfigMapDto> configmap) {
		this.configmap = configmap;
	}

	public List<String> getCommand() {
		return command;
	}

	public void setCommand(List<String> command) {
		this.command = command;
	}

	public List<String> getArgs() {
		return args;
	}

	public void setArgs(List<String> args) {
		this.args = args;
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

	public List<CreateEnvDto> getEnv() {
		return env;
	}

	public void setEnv(List<CreateEnvDto> env) {
		this.env = env;
	}

	public List<PersistentVolumeDto> getStorage() {
		return storage;
	}

	public void setStorage(List<PersistentVolumeDto> storage) {
		this.storage = storage;
	}

	public SecurityContextDto getSecurityContext() {
		return securityContext;
	}

	public String getImagePullPolicy() {
		return imagePullPolicy;
	}

	public void setImagePullPolicy(String imagePullPolicy) {
		this.imagePullPolicy = imagePullPolicy;
	}

	public void setSecurityContext(SecurityContextDto securityContext) {
		this.securityContext = securityContext;
	}

	public CreateResourceDto getLimit() {
		return limit;
	}

	public void setLimit(CreateResourceDto limit) {
		this.limit = limit;
	}



}
