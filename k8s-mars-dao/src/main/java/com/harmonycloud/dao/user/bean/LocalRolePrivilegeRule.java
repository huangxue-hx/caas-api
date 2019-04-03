package com.harmonycloud.dao.user.bean;

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;

public class LocalRolePrivilegeRule implements Serializable {
    private Integer id;

    private Integer localRoleId;

    private String description;

    private String resourceType;

    private String tables;

    private Date createTime;

    private Boolean available;

    private String reserve1;

    private String reserve2;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLocalRoleId() {
        return localRoleId;
    }

    public void setLocalRoleId(Integer localRoleId) {
        this.localRoleId = localRoleId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType == null ? null : resourceType.trim();
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables == null ? null : tables.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getReserve1() {
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1 == null ? null : reserve1.trim();
    }

    public String getReserve2() {
        return reserve2;
    }

    public void setReserve2(String reserve2) {
        this.reserve2 = reserve2 == null ? null : reserve2.trim();
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
        LocalRolePrivilegeRule other = (LocalRolePrivilegeRule) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLocalRoleId() == null ? other.getLocalRoleId() == null : this.getLocalRoleId().equals(other.getLocalRoleId()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getResourceType() == null ? other.getResourceType() == null : this.getResourceType().equals(other.getResourceType()))
            && (this.getTables() == null ? other.getTables() == null : this.getTables().equals(other.getTables()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getAvailable() == null ? other.getAvailable() == null : this.getAvailable().equals(other.getAvailable()))
            && (this.getReserve1() == null ? other.getReserve1() == null : this.getReserve1().equals(other.getReserve1()))
            && (this.getReserve2() == null ? other.getReserve2() == null : this.getReserve2().equals(other.getReserve2()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLocalRoleId() == null) ? 0 : getLocalRoleId().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getResourceType() == null) ? 0 : getResourceType().hashCode());
        result = prime * result + ((getTables() == null) ? 0 : getTables().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getAvailable() == null) ? 0 : getAvailable().hashCode());
        result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
        result = prime * result + ((getReserve2() == null) ? 0 : getReserve2().hashCode());
        return result;
    }
}