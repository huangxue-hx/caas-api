package com.harmonycloud.dao.application.bean;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataPrivilegeField;
import com.harmonycloud.common.enumm.DataPrivilegeType;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;

import java.io.Serializable;
import java.util.List;

/**
 * Created by gurongyun on 17/03/24.
 */
@DataPrivilegeType(type = DataResourceTypeEnum.CONFIGFILE)
public class ConfigFile implements Serializable {

	private static final long serialVersionUID = 1595317238255724736L;
	private String id; // 编号 唯一标识符
	@DataPrivilegeField(type = CommonConstant.DATA_FIELD)
	private String name; // 配置文件名称
	private String description; // 描述
	private String tags; // 版本号
	private String tenantId; // 租户id
	@DataPrivilegeField(type = CommonConstant.PROJECTID_FIELD)
	private String projectId; // 项目id
	@DataPrivilegeField(type = CommonConstant.CLUSTERID_FIELD)
	private String clusterId;
	private String clusterName;
	private String repoName; // 镜像repo
	private String user; // 创建者
	private String userRealName;
	private String createTime; // 创建时间
	private String updateTime;	//更新时间
	private List<ConfigFileItem> configFileItemList;//配置文件的明細列表
	private String clusterAliasName;//集群的别名
	private String dataPrivilege;//权限属性
	private boolean appStore;

	public String getClusterAliasName() {
		return clusterAliasName;
	}

	public void setClusterAliasName(String clusterAliasName) {
		this.clusterAliasName = clusterAliasName;
	}

	public List<ConfigFileItem> getConfigFileItemList() {
		return configFileItemList;
	}

	public void setConfigFileItemList(List<ConfigFileItem> configFileItemList) {
		this.configFileItemList = configFileItemList;
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

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
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

	public String getUpdateTime() { return updateTime; }

	public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

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

	public String getDataPrivilege() {
		return dataPrivilege;
	}

	public void setDataPrivilege(String dataPrivilege) {
		this.dataPrivilege = dataPrivilege;
	}

	public boolean getAppStore() {
		return appStore;
	}

	public void setAppStore(boolean appStore) {
		this.appStore = appStore;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ConfigFile that = (ConfigFile) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) return false;
		return clusterId != null ? clusterId.equals(that.clusterId) : that.clusterId == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
		result = 31 * result + (clusterId != null ? clusterId.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		String result = "ConfigFile{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", tags='" + tags + '\'' +
				", tenantId='" + tenantId + '\'' +
				", projectId='" + projectId + '\'' +
				", clusterId='" + clusterId + '\'' +
				", clusterName='" + clusterName + '\'' +
				", repoName='" + repoName + '\'' +
				", user='" + user + '\'' +
				", createTime='" + createTime + '\'' +
				", updateTime='" + updateTime + '\'' +
				", clusterAliasName='" + clusterAliasName + '\'' +
				"},configFileItem=[";
		for(int i=0;i<configFileItemList.size();i++){
			result += configFileItemList.get(0).toString()+",";
		}
		if(result.endsWith(",")){
			result = result.substring(0, result.length()-1)+"]";
		}
		return result;
	}

	public String getUserRealName() {
		return userRealName;
	}

	public void setUserRealName(String userRealName) {
		this.userRealName = userRealName;
	}
}
