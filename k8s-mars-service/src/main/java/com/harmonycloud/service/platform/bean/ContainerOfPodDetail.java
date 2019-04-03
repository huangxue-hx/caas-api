package com.harmonycloud.service.platform.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dto.application.CreateEnvDto;
import com.harmonycloud.dto.application.SecurityContextDto;
import com.harmonycloud.k8s.bean.ContainerPort;
import com.harmonycloud.k8s.bean.EnvVar;
import com.harmonycloud.k8s.bean.Probe;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class ContainerOfPodDetail {
	
	private String name;

	private String deploymentName;
	
	private String img;
	
	private Map<String, Object> resource;
	
	private Map<String, Object> limit;
	
	private Probe livenessProbe;
	
	private Probe readinessProbe;
	
	private List<ContainerPort> ports;
	
	private List<String> args;

	private List<CreateEnvDto> env;
	
	private List<String> command;
	
	private List<VolumeMountExt> storage;
	
	private Map<String, Object> configmap;
	
	private String restartCount;
	
	private SecurityContextDto securityContext;
	
	private String imagePullPolicy;

//	private String  timeZone;
//
//	public String getTimeZone() {
//		return timeZone;
//	}
//
//	public void setTimeZone(String syncTimeZone) {
//		this.timeZone = syncTimeZone;
//	}

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
		this.command = command;
		if(CollectionUtils.isNotEmpty(env)){
			this.env = new ArrayList<>();
			for(EnvVar envVar : env){
				if (envVar.getName().equals(Constant.PILOT_LOG_PREFIX) || envVar.getName().equals(Constant.PILOT_LOG_PREFIX_TAG)){
					continue; //过滤log_pilot收集日志设置的环境变量
				}
				CreateEnvDto envDto = new CreateEnvDto();
				envDto.setName(envVar.getName());
				envDto.setKey(envVar.getName());
				if(StringUtils.isNotEmpty(envVar.getValue())){
					envDto.setType(CommonConstant.ENV_TYPE_EQUAL);
					envDto.setValue(envVar.getValue());
				}else if(envVar.getValueFrom() != null && envVar.getValueFrom().getFieldRef() != null){
					envDto.setType(CommonConstant.ENV_TYPE_FROM);
					envDto.setValue(envVar.getValueFrom().getFieldRef().getFieldPath());
				}
				this.env.add(envDto);
			}
		}
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

	public List<CreateEnvDto> getEnv() {
		return env;
	}

	public void setEnv(List<CreateEnvDto> env) {
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

	public String getImagePullPolicy() {
		return imagePullPolicy;
	}

	public void setImagePullPolicy(String imagePullPolicy) {
		this.imagePullPolicy = imagePullPolicy;
	}

	public Map<String, Object> getLimit() {
		return limit;
	}

	public void setLimit(Map<String, Object> limit) {
		this.limit = limit;
	}

}
