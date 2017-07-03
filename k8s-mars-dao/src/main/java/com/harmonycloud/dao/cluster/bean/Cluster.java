package com.harmonycloud.dao.cluster.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhangsl on 16/11/4.
 */
public class Cluster implements Serializable {
    /**
	 *
	 */
	private static final long serialVersionUID = -1886649980404325029L;
	/**
	 *
	 */
	private Long id;
    private String name;
    private String host;
    private String protocol;
    private String authType;
    private String username;
    private String password;
    private String machineToken;
    private String port;
    private String entryPoint;
    private String haproxyVersion;
    private String influxdbUrl;
	private String influxdbDb;
	private String influxdbVersion;
    private String esHost;
	private Integer esPort;
	private String esClusterName;
	private String esVersion;
	private Date createTime;
	private Date updateTime;


	public Cluster() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMachineToken() {
		return machineToken;
	}

	public void setMachineToken(String machineToken) {
		this.machineToken = machineToken;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	public String getHaproxyVersion() {
		return haproxyVersion;
	}

	public void setHaproxyVersion(String haproxyVersion) {
		this.haproxyVersion = haproxyVersion;
	}

	public String getInfluxdbUrl() {
		return influxdbUrl;
	}

	public void setInfluxdbUrl(String influxdbUrl) {
		this.influxdbUrl = influxdbUrl;
	}

	public String getInfluxdbDb() {
		return influxdbDb;
	}

	public void setInfluxdbDb(String influxdbDb) {
		this.influxdbDb = influxdbDb;
	}

	public String getInfluxdbVersion() {
		return influxdbVersion;
	}

	public void setInfluxdbVersion(String influxdbVersion) {
		this.influxdbVersion = influxdbVersion;
	}

	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public Integer getEsPort() {
		return esPort;
	}

	public void setEsPort(Integer esPort) {
		this.esPort = esPort;
	}

	public String getEsClusterName() {
		return esClusterName;
	}

	public void setEsClusterName(String esClusterName) {
		this.esClusterName = esClusterName;
	}

	public String getEsVersion() {
		return esVersion;
	}

	public void setEsVersion(String esVersion) {
		this.esVersion = esVersion;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
}
