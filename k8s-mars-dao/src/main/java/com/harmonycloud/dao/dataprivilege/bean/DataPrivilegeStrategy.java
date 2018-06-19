package com.harmonycloud.dao.dataprivilege.bean;

import java.io.Serializable;

public class DataPrivilegeStrategy implements Serializable {
    private Integer id;

    private Byte scopeType;

    private String scopeId;

    private String resourceTypeId;

    private Byte strategy;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Byte getScopeType() {
        return scopeType;
    }

    public void setScopeType(Byte scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId == null ? null : scopeId.trim();
    }

    public String getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(String resourceTypeId) {
        this.resourceTypeId = resourceTypeId == null ? null : resourceTypeId.trim();
    }

    public Byte getStrategy() {
        return strategy;
    }

    public void setStrategy(Byte strategy) {
        this.strategy = strategy;
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
        DataPrivilegeStrategy other = (DataPrivilegeStrategy) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getScopeType() == null ? other.getScopeType() == null : this.getScopeType().equals(other.getScopeType()))
            && (this.getScopeId() == null ? other.getScopeId() == null : this.getScopeId().equals(other.getScopeId()))
            && (this.getResourceTypeId() == null ? other.getResourceTypeId() == null : this.getResourceTypeId().equals(other.getResourceTypeId()))
            && (this.getStrategy() == null ? other.getStrategy() == null : this.getStrategy().equals(other.getStrategy()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getScopeType() == null) ? 0 : getScopeType().hashCode());
        result = prime * result + ((getScopeId() == null) ? 0 : getScopeId().hashCode());
        result = prime * result + ((getResourceTypeId() == null) ? 0 : getResourceTypeId().hashCode());
        result = prime * result + ((getStrategy() == null) ? 0 : getStrategy().hashCode());
        return result;
    }
}