package com.harmonycloud.service.tenant;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import java.util.List;

/**
 * 创建本地namespace存储在数据库
 */
public interface NamespaceLocalService {


    /**
     * 创建namespace
     * @param namespaceLocal
     * @return
     */
    public void createNamespace(NamespaceLocal namespaceLocal) throws Exception;


    /**
     * 删除namespace
     * @param namespaceLocal
     * @return
     */
    public void deleteNamespace(NamespaceLocal namespaceLocal) throws Exception;

    /**
     * 根据id删除分区
     * @param id
     * @throws Exception
     */
    public void deleteNamespaceById(Integer id) throws Exception;

    /**
     * 根据tenantId查询namespace列表(查询当前角色的作用域)
     * @param tenantId
     * @return
     */
    public List<NamespaceLocal> getNamespaceListByTenantId(String tenantId) throws Exception;
    /**
     * 根据tenantId查询namespace列表(查询所有集群的分区)
     * @param tenantId
     * @return
     */
    public List<NamespaceLocal> getAllNamespaceListByTenantId(String tenantId) throws Exception;
    /**
     * 根据tenantId查询namespace列表(查询所有集群的分区)微服务专用
     * @param tenantId
     * @return
     */
    public List<NamespaceLocal> getAllNamespaceListByTenantIdWithMSF(String tenantId) throws Exception;

    /**
     * 获取该租户下所有共享的分区
     * @param tenantId
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> getAllPublicNamespaceListByTenantId(String tenantId) throws Exception;
    /**
     * 根据tenantId,clusterId查询namespace列表
     * @param tenantId
     * @param clusterIds
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> getNamespaceListByTenantIdAndClusterId(String tenantId, List<String> clusterIds) throws Exception;

    /**
     * 根据镜像可以部署的分区列表
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> getNamespaceListByRepositoryId(String tenantId, Integer repositoryId) throws Exception;

    /**
     * 根据namespace id查询namespace
     * @param namespaceId
     * @return
     */
    public NamespaceLocal getNamespaceByNamespaceId(String namespaceId) throws Exception;
    /**
     * 根据namespace name查询namespace
     * @param namespaceName
     * @return
     */
    public NamespaceLocal getNamespaceByName(String namespaceName) throws Exception;

    /**
     * 根据namespace aliasName查询namespace
     * @param aliasName
     * @return
     * @throws Exception
     */
    public NamespaceLocal getNamespaceByAliasName(String aliasName) throws Exception;

    /**
     * 根据namespace name 获取集群
     * @param namespaceName
     * @return
     * @throws Exception
     */
    public Cluster getClusterByNamespaceName(String namespaceName) throws Exception;

    /**
     * 根据clusterId查询namespace列表
     * @param clusterId
     * @return
     */
    public List<NamespaceLocal> getNamespaceListByClusterId(String clusterId) throws Exception;

    /**
     * 根据clusterId查询共享namespace列表
     * @param clusterId
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> getPublicNamespaceListByClusterId(String clusterId) throws Exception;
    int deleteByClusterId(String clusterId);

    NamespaceLocal getNamespaceByTenantIdAndName(String tenantId, String namespace) throws Exception;

}
