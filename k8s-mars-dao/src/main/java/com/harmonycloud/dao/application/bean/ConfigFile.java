package com.harmonycloud.dao.application.bean;

import java.io.Serializable;

/**
 * Created by gurongyun on 17/03/24.
 */
public class ConfigFile implements Serializable {

	private static final long serialVersionUID = 1595317238255724736L;
	private String id; // 编号 唯一标识符
	private String name; // 配置文件名称
	private String description; // 描述
	private String tags; // 版本号
	private String tenantId; // 租户id
	private String projectId; // 项目id
	private String clusterId;
	private String clusterName;
	private String repoName; // 镜像repo
	private String user; // 创建者
	private String createTime; // 创建时间
	private String items; //配置文件内容
	private String path; //容器内部挂载路径

	public ConfigFile() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
}
