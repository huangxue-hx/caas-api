package com.harmonycloud.service.platform.bean.harbor;

public class HarborReplicationTarget {
	private Integer id;
	private String sourceHarborHost;
	private String name;              
    private String endpoint;                 
    private String username;                     
    private String password;
    private String targetHarborHost;
	private Boolean insecure;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
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


	public String getSourceHarborHost() {
		return sourceHarborHost;
	}

	public void setSourceHarborHost(String sourceHarborHost) {
		this.sourceHarborHost = sourceHarborHost;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTargetHarborHost() {
		return targetHarborHost;
	}

	public void setTargetHarborHost(String targetHarborHost) {
		this.targetHarborHost = targetHarborHost;
	}

	public Boolean getInsecure() {
		return insecure;
	}

	public void setInsecure(Boolean insecure) {
		this.insecure = insecure;
	}
}
