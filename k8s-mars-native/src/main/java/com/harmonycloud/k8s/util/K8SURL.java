package com.harmonycloud.k8s.util;

import java.util.Map;

import com.harmonycloud.k8s.constant.Resource;

/**
 * @author qg
 *
 */
public class K8SURL {

	private  String protocol;

	private  String host;

	private  String port;

	private  String machineToken;
	
	private String watch;

	/**
	 * 资源所属的apigroup（根据资源自动判断）
	 */
	private String apiGroup;
	
	/**
	 * 具体namespace名称
	 */
	private String namespace;
	
	/**
	 * 资源
	 */
	private String resource;
	
	/**
	 * 具体资源名称
	 */
	private String name;
	
	/**
	 * 子操作
	 */
	private String subpath;
	
	private Map<String, Object> queryParams;
	
	public Map<String, Object> getQueryParams() {
		return queryParams;
	}

	public void setQueryParams(Map<String, Object> queryParams) {
		this.queryParams = queryParams;
	}

	public String getApiGroup() {
		return apiGroup;
	}

	public String getNamespace() {
		return namespace;
	}

	public K8SURL setNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public K8SURL setResource(String resource) {
		this.resource = resource;
		this.apiGroup = Resource.getGroupByResource(resource);
		return this;
	}

	public String getName() {
		return name;
	}

	public K8SURL setName(String name) {
		this.name = name;
		return this;
	}

	public String getSubpath() {
		return subpath;
	}

	public K8SURL setSubpath(String subpath) {
		this.subpath = subpath;
		return this;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getMachineToken() {
		return machineToken;
	}

	public void setMachineToken(String machineToken) {
		this.machineToken = machineToken;
	}

	public void setApiGroup(String apiGroup) {
		this.apiGroup = apiGroup;
	}

	public String getWatch() {
		return watch;
	}

	public void setWatch(String watch) {
		this.watch = watch;
	}
}
