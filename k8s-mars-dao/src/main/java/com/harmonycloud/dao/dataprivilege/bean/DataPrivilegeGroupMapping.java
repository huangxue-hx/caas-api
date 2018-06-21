package com.harmonycloud.dao.dataprivilege.bean;

import java.io.Serializable;

public class DataPrivilegeGroupMapping implements Serializable {
    private Integer id;

    private String dataName;

    private Integer resourceTypeId;

    private String projectId;

    private String clusterId;

    private String namespace;

    private Integer privilegeType;

    private Integer parentId;

    private Integer groupId;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName == null ? null : dataName.trim();
    }

    public Integer getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(Integer resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId == null ? null : projectId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId == null ? null : clusterId.trim();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace == null ? null : namespace.trim();
    }

    public Integer getPrivilegeType() {
        return privilegeType;
    }

    public void setPrivilegeType(Integer privilegeType) {
        this.privilegeType = privilegeType;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        DataPrivilegeGroupMapping other = (DataPrivilegeGroupMapping) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getDataName() == null ? other.getDataName() == null : this.getDataName().equals(other.getDataName()))
            && (this.getResourceTypeId() == null ? other.getResourceTypeId() == null : this.getResourceTypeId().equals(other.getResourceTypeId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getNamespace() == null ? other.getNamespace() == null : this.getNamespace().equals(other.getNamespace()))
            && (this.getPrivilegeType() == null ? other.getPrivilegeType() == null : this.getPrivilegeType().equals(other.getPrivilegeType()))
            && (this.getParentId() == null ? other.getParentId() == null : this.getParentId().equals(other.getParentId()))
            && (this.getGroupId() == null ? other.getGroupId() == null : this.getGroupId().equals(other.getGroupId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getDataName() == null) ? 0 : getDataName().hashCode());
        result = prime * result + ((getResourceTypeId() == null) ? 0 : getResourceTypeId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        result = prime * result + ((getPrivilegeType() == null) ? 0 : getPrivilegeType().hashCode());
        result = prime * result + ((getParentId() == null) ? 0 : getParentId().hashCode());
        result = prime * result + ((getGroupId() == null) ? 0 : getGroupId().hashCode());
        return result;
    }
}