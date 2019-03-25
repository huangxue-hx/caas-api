package com.harmonycloud.dto.tenant;


public class ProjectIpPoolDto {

	private Integer id;

	private String name;

	private String tenantId;

	private String projectId;

	private String clusterId;

	private String clusterName;

	private String cidr;

	private String subnet;

	private String gateway;

	private Integer ipUsedCount;

	private Integer ipTotal;

	private Double ipUsedRate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getCidr() {
		return cidr;
	}

	public void setCidr(String cidr) {
		this.cidr = cidr;
	}

	public String getSubnet() {
		return subnet;
	}

	public void setSubnet(String subnet) {
		this.subnet = subnet;
	}

	public String getGateway() {
		return gateway;
	}

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public Integer getIpUsedCount() {
		return ipUsedCount;
	}

	public void setIpUsedCount(Integer ipUsedCount) {
		this.ipUsedCount = ipUsedCount;
	}

	public Integer getIpTotal() {
		return ipTotal;
	}

	public void setIpTotal(Integer ipTotal) {
		this.ipTotal = ipTotal;
	}

	public Double getIpUsedRate() {
		return ipUsedRate;
	}

	public void setIpUsedRate(Double ipUsedRate) {
		this.ipUsedRate = ipUsedRate;
	}
}
