package com.harmonycloud.dto.cluster;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Created by hongjie
 */
public class ClusterDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	@NotBlank(message = "cluster name can not be null.")
	private String name;
	@NotBlank(message = "cluster master host can not be null.")
	private String host;
	@NotNull(message = "cluster level can not be null.")
	private Integer level;
	@NotBlank(message = "master server username can not be null.")
	private String username;
	@NotBlank(message = "master server password can not be null.")
	private String password;

	private String harborServer;
	private String harborAdminPassword;
	private String influxdbHost;
	private String esHost;
	private List<ClusterLoadbalanceDto> loadbalances;

	public String getHarborServer() {
		return harborServer;
	}

	public void setHarborServer(String harborServer) {
		this.harborServer = harborServer;
	}

	public String getEsHost() {
		return esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
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

	public String getInfluxdbHost() {
		return influxdbHost;
	}

	public void setInfluxdbHost(String influxdbHost) {
		this.influxdbHost = influxdbHost;
	}

	public List<ClusterLoadbalanceDto> getLoadbalances() {
		return loadbalances;
	}

	public void setLoadbalances(List<ClusterLoadbalanceDto> loadbalances) {
		this.loadbalances = loadbalances;
	}

	public String getHarborAdminPassword() {
		return harborAdminPassword;
	}

	public void setHarborAdminPassword(String harborAdminPassword) {
		this.harborAdminPassword = harborAdminPassword;
	}
}
