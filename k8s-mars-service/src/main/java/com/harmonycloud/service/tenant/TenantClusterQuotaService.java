package com.harmonycloud.service.tenant;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.tenant.ClusterQuotaDto;
import jnr.ffi.annotations.In;

import java.util.List;

/**
 * Created by zgl on 17-12-10.
 */


/**
 * 项目业务接口
 */
public interface TenantClusterQuotaService {

    /**
     * 根据租户id查询集群配额列表 clusterId 为空查询该租户下的所有集群配额
     * @return
     * @throws Exception
     */
    public List<ClusterQuotaDto> listClusterQuotaByTenantid(String tenantId,String clusterId) throws Exception;

    /**
     * 获取集群配额可用值与总配额
     * @param tenantId
     * @param clusterId
     * @param clusterQuotaDto
     * @throws Exception
     */
    public void getClusterUsage(String tenantId,String clusterId,ClusterQuotaDto clusterQuotaDto) throws Exception;
    /**
     * 根据id查询租户某个集群下的配额
     * @param
     * @return
     */
    public TenantClusterQuota getClusterQuotaById(int id) throws Exception;

    /**
     * 创建租户下集群配额
     * @param tenantClusterQuota
     * @throws Exception
     */
    public void createClusterQuota(TenantClusterQuota tenantClusterQuota) throws Exception;
    /**
     * 修改租户下集群配额
     * @param tenantClusterQuota
     * @throws Exception
     */
    public void updateClusterQuota(TenantClusterQuota tenantClusterQuota) throws Exception;

    /**
     * 根据id删除租户下集群配额
     * @param id
     * @throws Exception
     */
    public void deleteClusterQuotaByid(int id) throws Exception;

    /**
     * 根据租户id删除租户下集群配额
     * @param tenantId
     * @throws Exception
     */
    public void deleteClusterQuotaByTenantId(String tenantId) throws Exception;

    /**
     * 根据租户id与集群id获取集群配额
     * @param tenantId
     * @param clusterId
     * @throws Exception
     */
    public TenantClusterQuota getClusterQuotaByTenantIdAndClusterId(String tenantId,String clusterId) throws Exception;

    /**
     * 根据集群id获取集群配额列表
     * @param clusterId
     * @return
     * @throws Exception
     */
    public List<TenantClusterQuota> getClusterQuotaByClusterId(String clusterId) throws Exception;

    /**
     * 根据集群id删除集群配额列表
     * @param clusterId
     * @throws Exception
     */
    public void deleteClusterQuotaByClusterId(String clusterId) throws Exception;

    int deleteByClusterId(String clusterId);
}
