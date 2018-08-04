package com.harmonycloud.dto.application;


import com.harmonycloud.k8s.bean.Lifecycle;
import com.harmonycloud.k8s.bean.Probe;

import java.io.Serializable;
import java.util.List;

public class CreateContainerDto implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String name;

	private String img;

	private String tag;

	private CreateResourceDto resource;

	private CreateResourceDto limit;

	private String log;

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

	private Lifecycle lifecycle;

	/**
	 * 同步主机时区
	 */
	private boolean syncTimeZone;

	public Lifecycle getLifecycle() {
		return lifecycle;
	}

	public void setLifecycle(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	public boolean isSyncTimeZone() {
		return syncTimeZone;
	}

	public void setSyncTimeZone(boolean syncTimeZone) {
		this.syncTimeZone = syncTimeZone;
	}

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

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public List<CreatePortDto> getPorts() {
		return ports;
	}

	public void setPorts(List<CreatePortDto> ports) {
		this.ports = ports;
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

	public List<CreateConfigMapDto> getConfigmap() {
		return configmap;
	}

	public void setConfigmap(List<CreateConfigMapDto> configmap) {
		this.configmap = configmap;
	}

	public SecurityContextDto getSecurityContext() {
		return securityContext;
	}

	public void setSecurityContext(SecurityContextDto securityContext) {
		this.securityContext = securityContext;
	}

	public String getImagePullPolicy() {
		return imagePullPolicy;
	}

	public void setImagePullPolicy(String imagePullPolicy) {
		this.imagePullPolicy = imagePullPolicy;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public CreateResourceDto getLimit() {
		return limit;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public void setLimit(CreateResourceDto limit) {
		this.limit = limit;
	}


}


