package com.harmonycloud.dto.tenant;

import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by zgl on 17-12-12.
 */
public class ClusterQuotaDto implements Serializable {

    private static final long serialVersionUID = -6541661628695983123L;
    //主键id
    private Integer id;
    //租户id
    private String tenantId;
    //集群id
    private String clusterId;
    //集群name
    private String clusterName;
    //集群aliasName
    private String clusterAliasName;
    private String dataCenter;

    private String dataCenterName;
    //cpu配额
    private Double cpuQuota;
    //cpu配额类型
    private String cpuQuotaType;
    //集群总cpu配额
    private Double totalCpu = 0D;
    //集群总cpu配额类型
    private String totalCpuType;
    //memory配额
    private Double memoryQuota;
    //memory配额类型
    private String memoryQuotaType;
    //集群总memory配额
    private Double totalMomry = 0D;
    //集群总memory配额类型
    private String totalMemoryType;
    //使用的cpu
    private Double usedCpu = 0D;
    //使用的cpu类型
    private String usedCpuType;
    //未使用的cpu
    private Double unUsedCpu = 0D;
    //未使用的cpu类型
    private String unUsedCpuType;
    //使用的memory
    private Double usedMemory = 0D;
    //使用的memory类型
    private String usedMemoryType;
    //未使用的memory
    private Double unUsedMemory = 0D;
    //未使用的memory类型
    private String unUsedMemoryType;
    //集群使用的cpu
    private Double clusterUsedCpu = 0D;
    //集群使用的cpu类型
    private String clusterUsedCpuType;
    //集群使用的memory
    private Double clusterUsedMemory = 0D;
    //集群使用的memory类型
    private String clusterUsedMemoryType;
    //集群的存储配额
    private List<StorageDto> storageQuota;

    public String getClusterAliasName() {
        return clusterAliasName;
    }

    public void setClusterAliasName(String clusterAliasName) {
        this.clusterAliasName = clusterAliasName;
    }

    public String getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(String dataCenter) {
        this.dataCenter = dataCenter;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public Double getUnUsedCpu() {
        return unUsedCpu;
    }

    public void setUnUsedCpu(Double unUsedCpu) {
        this.unUsedCpu = unUsedCpu;
    }

    public String getUnUsedCpuType() {
        return unUsedCpuType;
    }

    public void setUnUsedCpuType(String unUsedCpuType) {
        this.unUsedCpuType = unUsedCpuType;
    }

    public Double getUnUsedMemory() {
        return unUsedMemory;
    }

    public void setUnUsedMemory(Double unUsedMemory) {
        this.unUsedMemory = unUsedMemory;
    }

    public String getUnUsedMemoryType() {
        return unUsedMemoryType;
    }

    public void setUnUsedMemoryType(String unUsedMemoryType) {
        this.unUsedMemoryType = unUsedMemoryType;
    }

    public Double getClusterUsedCpu() {
        return clusterUsedCpu;
    }

    public void setClusterUsedCpu(Double clusterUsedCpu) {
        this.clusterUsedCpu = clusterUsedCpu;
    }

    public String getClusterUsedCpuType() {
        return clusterUsedCpuType;
    }

    public void setClusterUsedCpuType(String clusterUsedCpuType) {
        this.clusterUsedCpuType = clusterUsedCpuType;
    }

    public Double getClusterUsedMemory() {
        return clusterUsedMemory;
    }

    public void setClusterUsedMemory(Double clusterUsedMemory) {
        this.clusterUsedMemory = clusterUsedMemory;
    }

    public String getClusterUsedMemoryType() {
        return clusterUsedMemoryType;
    }

    public void setClusterUsedMemoryType(String clusterUsedMemoryType) {
        this.clusterUsedMemoryType = clusterUsedMemoryType;
    }

    public Double getTotalCpu() {
        return totalCpu;
    }

    public void setTotalCpu(Double totalCpu) {
        this.totalCpu = totalCpu;
    }

    public String getTotalCpuType() {
        return totalCpuType;
    }

    public void setTotalCpuType(String totalCpuType) {
        this.totalCpuType = totalCpuType;
    }

    public Double getTotalMomry() {
        return totalMomry;
    }

    public void setTotalMomry(Double totalMomry) {
        this.totalMomry = totalMomry;
    }

    public String getTotalMemoryType() {
        return totalMemoryType;
    }

    public void setTotalMemoryType(String totalMemoryType) {
        this.totalMemoryType = totalMemoryType;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

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
        this.tenantId = tenantId;
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

    public String getCpuQuotaType() {
        return cpuQuotaType;
    }

    public void setCpuQuotaType(String cpuQuotaType) {
        this.cpuQuotaType = cpuQuotaType;
    }

    public Double getMemoryQuota() {
        return memoryQuota;
    }

    public void setMemoryQuota(Double memoryQuota) {
        this.memoryQuota = memoryQuota;
    }

    public String getMemoryQuotaType() {
        return memoryQuotaType;
    }

    public void setMemoryQuotaType(String memoryQuotaType) {
        this.memoryQuotaType = memoryQuotaType;
    }


    public String getUsedCpuType() {
        return usedCpuType;
    }

    public void setUsedCpuType(String usedCpuType) {
        this.usedCpuType = usedCpuType;
    }

    public Double getUsedCpu() {
        return usedCpu;
    }

    public void setUsedCpu(Double usedCpu) {
        this.usedCpu = usedCpu;
    }

    public Double getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(Double usedMemory) {
        this.usedMemory = usedMemory;
    }

    public String getUsedMemoryType() {
        return usedMemoryType;
    }

    public void setUsedMemoryType(String usedMemoryType) {
        this.usedMemoryType = usedMemoryType;
    }

    public List<StorageDto> getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(List<StorageDto> storageQuota) {
        this.storageQuota = storageQuota;
    }
}
