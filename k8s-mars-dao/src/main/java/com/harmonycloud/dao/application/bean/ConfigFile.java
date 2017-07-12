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
	private String tenant; // 租户
	private String repoName; // 镜像repo
	private String user; // 创建者
	private String createTime; // 创建时间
	private String item; //配置文件内容
	private String path; //容器内部挂载路径
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		ConfigFile o = (ConfigFile)obj;
		return this.name.equals(o.getName())&&this.repoName.equals(o.getRepoName())&&this.tenant.equals(o.getTenant())&&this.description.equals(o.getDescription())&&this.item.equals(o.getItem())&&this.path.equals(o.getPath());
	}

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

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
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

}
