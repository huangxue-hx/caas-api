package com.harmonycloud.dao.tenant.bean;

import java.io.Serializable;
import java.util.Date;

public class TenantClusterQuota implements Serializable {
    private Integer id;

    private String tenantId;

    private String clusterId;

    private Double cpuQuota;

    private Double memoryQuota;

    private Double pvQuota;

    private Date updateTime;

    private Date createTime;

    private String clusterName;

    private String reserve1;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null ? null : tenantId.trim();
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Double getCpuQuota() {
        return cpuQuota;
    }

    public void setCpuQuota(Double cpuQuota) {
        this.cpuQuota = cpuQuota;
    }

    public Double getMemoryQuota() {
        return memoryQuota;
    }

    public void setMemoryQuota(Double memoryQuota) {
        this.memoryQuota = memoryQuota;
    }

    public Double getPvQuota() {
        return pvQuota;
    }

    public void setPvQuota(Double pvQuota) {
        this.pvQuota = pvQuota;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public String getReserve1() {
        return reserve1;
    }

    public void setReserve1(String reserve1) {
        this.reserve1 = reserve1 == null ? null : reserve1.trim();
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
        TenantClusterQuota other = (TenantClusterQuota) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTenantId() == null ? other.getTenantId() == null : this.getTenantId().equals(other.getTenantId()))
            && (this.getClusterId() == null ? other.getClusterId() == null : this.getClusterId().equals(other.getClusterId()))
            && (this.getCpuQuota() == null ? other.getCpuQuota() == null : this.getCpuQuota().equals(other.getCpuQuota()))
            && (this.getMemoryQuota() == null ? other.getMemoryQuota() == null : this.getMemoryQuota().equals(other.getMemoryQuota()))
            && (this.getPvQuota() == null ? other.getPvQuota() == null : this.getPvQuota().equals(other.getPvQuota()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getClusterName() == null ? other.getClusterName() == null : this.getClusterName().equals(other.getClusterName()))
            && (this.getReserve1() == null ? other.getReserve1() == null : this.getReserve1().equals(other.getReserve1()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTenantId() == null) ? 0 : getTenantId().hashCode());
        result = prime * result + ((getClusterId() == null) ? 0 : getClusterId().hashCode());
        result = prime * result + ((getCpuQuota() == null) ? 0 : getCpuQuota().hashCode());
        result = prime * result + ((getMemoryQuota() == null) ? 0 : getMemoryQuota().hashCode());
        result = prime * result + ((getPvQuota() == null) ? 0 : getPvQuota().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getClusterName() == null) ? 0 : getClusterName().hashCode());
        result = prime * result + ((getReserve1() == null) ? 0 : getReserve1().hashCode());
        return result;
    }
}