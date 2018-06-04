package com.harmonycloud.dto.external;

import java.io.Serializable;

public class ExternalSvc implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String clusterName;

	private String clusterAliasName;

	private String clusterId;
	
	private String serviceName;

    private String name;

    private String namespace;

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getName() {
    	return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private String ip;
	
	private Integer port;
	
	private String time;
	
	private String type;
	
	private String describe;


	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterAliasName() {
		return clusterAliasName;
	}

	public void setClusterAliasName(String clusterAliasName) {
		this.clusterAliasName = clusterAliasName;
	}
}