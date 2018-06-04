package com.harmonycloud.dao.user.bean;

import java.io.Serializable;
import java.util.Date;

public class Role implements Serializable {
    private Integer id;

    private String name;

    private String nickName;

    private Date createTime;

    private Date updateTime;

    private Boolean available;

    private String clusterIds;

    private String namespaceNames;

    private String reserve1;

    private String reserve2;

    private static final long serialVersionUID = 1L;

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
        this.name = name == null ? null : name.trim();
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName == null ? null : nickName.trim();
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

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getClusterIds() {
        return clusterIds;
    }

    public void setClusterIds(String clusterIds) {
        this.clusterIds = clusterIds == null ? null : clusterIds.trim();
    }

    public String getNamespaceNames() {
        return namespaceNames;
    }

    public void setNamespaceNames(String namespaceNames) {
        this.namespaceNames = namespaceNames == null ? null : namespaceNames.trim();
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
        Role other = (Role) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getNickName() == null ? other.getNickName() == null :
                this.getNickName().equals(other.getNickName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null :
                this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null :
                this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getAvailable() == null ? other.getAvailable() == null :
                this.getAvailable().equals(other.getAvailable()))
            && (this.getClusterIds() == null ? other.getClusterIds() == null :
                this.getClusterIds().equals(other.getClusterIds()))
            && (this.getNamespaceNames() == null ? other.getNamespaceNames() == null :
                this.getNamespaceNames().equals(other.getNamespaceNames()))
            && (this.getReserve1() == null ? other.getReserve1() == null :
                this.getReserve1().equals(other.getReserve1()))
            && (this.getReserve2() == null ? other.getReserve2() == null :
                this.getReserve2().equals(other.getReserve2()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getNickName() == null) ? 0 : getNickName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getAvailable() == null) ? 0 : getAvailable().hashCode());
        result = prime * result + ((getClusterIds() == null) ? 0 : getClusterIds().hashCode());
        result = prime * result + ((getNamespaceNames() == null) ? 0 : getNamespaceNames().hashCode());
        result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
        result = prime * result + ((getReserve2() == null) ? 0 : getReserve2().hashCode());
        return result;
    }
}