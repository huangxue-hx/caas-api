package com.harmonycloud.service.tenant;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.dto.tenant.NamespaceStorageDto;
import com.harmonycloud.k8s.bean.ResourceQuotaList;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;
import java.util.Map;


/**
 * Created by andy on 17-1-20.
 */
public interface NamespaceService {


    /**
     * 创建namespace
     * @param namespaceDto
     * @return
     */
    ActionReturnUtil createNamespace(NamespaceDto namespaceDto) throws Exception;

    void updateShareNode(NamespaceDto namespaceDto) throws Exception;

    /**
     * 编辑Resource　quato
     * @param namespaceDto
     * @return
     */
    ActionReturnUtil updateNamespace(NamespaceDto namespaceDto) throws Exception;

    /**
     * 删除namespace
     * @param tenantid
     * @param name
     * @return
     */
    ActionReturnUtil deleteNamespace(String tenantid, String name) throws Exception;

    /**
     * 查询namespace列表 带namespace配额
     * @param tenantid
     * @return
     */
    ActionReturnUtil getNamespaceList(String tenantid) throws Exception;

    /**
     * 查询namespace详情
     * @param name 分区名
     * @return
     */
    ActionReturnUtil getNamespaceDetail(String name) throws Exception;
    /**
     * 根据租户id称查询租户下namespace简单列表
     * @param tenantid
     * @return
     */
    public ActionReturnUtil getSimpleNamespaceListByTenant(String tenantid) throws Exception;
    /**
     * 配置信任白名单信息到namespace
     * @param networkNames
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updateNamespaceForTopology(String[] networkNames, String name,Cluster cluster) throws Exception;
    /**
     * 移除配置信任白名单信息到namespace
     * @param networkNames
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil removeNamespaceForTopology(String[] networkNames, String name,Cluster cluster) throws Exception;
    /**
     * 创建信任白名单的网络规则
     * @param namespace
     * @param networkname
     * @param type
     * @param networknamefrom
     * @param networknameto
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createNetworkPolicy(String namespace,String networkname, Integer type, String networknamefrom, String networknameto,Cluster cluster) throws Exception;
    /**
     * 删除信任白名单的网络规则
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil removeNetworkPolicy(String namespace, String networknamefrom, String networknameto,Cluster cluster) throws Exception;
    /**
     * 获取namespace下的资源配额
     * @param namespace
     * @return
     */

    public ResourceQuotaList getResouceQuota(String namespace,Cluster cluster)throws Exception;
    /**
     * 获取分区的专属标签
     * @param tenantid
     * @param namespace
     * @return
     * @throws Exception
     */
    public String getPrivatePartitionLabel(String tenantid,String namespace) throws Exception;
    /**
     * 根据tenantid查询namespace列表详情
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getNamespaceListByTenantid(String tenantid)throws Exception;
    /**
     * 根据tenantid查询集群资源使用列表
     * @param tenantid
     * @param clusterId  如果为null查询所有集群的资源使用情况
     * @return
     * @throws Exception
     */
    public Map<String,List> getClusterQuotaListByTenantid(String tenantid,String clusterId)throws Exception;
    /**
     * 根据clusterId查询namespace配额使用量详情列表
     * @param clusterId
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getNamespaceListByClusterId(String clusterId)throws Exception;

    /**
     * 获取分区的配额
     * @param namespaceList
     * @return
     * @throws Exception
     */
    List<Map<String, Object>>  getNameSpaceQuota(List<NamespaceLocal> namespaceList) throws Exception;

    List<NamespaceStorageDto> listNamespaceStorage(String tenantId, String clusterId) throws Exception;

    /**
     * 根据查询namespace列表详情
     * @param
     * @return
     * @throws Exception
     */
    public Map<String, Object> getNamespaceQuota(String namespace)throws Exception;
    
    /**
     * 根据tenantid查询namespace名称列表（不包含分区配额信息）
     * @param tenantid
     * @return
     * @throws Exception
     */
    public List<NamespaceLocal> listNamespaceNameByTenantid(String tenantid)throws Exception;

    /**
     * 添加私有分区主机状态
     * @param namespaceDto
     * @throws Exception
     */
    public void addPrivateNamespaceNodes(NamespaceDto namespaceDto)throws Exception;
    /**
     * 移除私有分区主机状态
     * @param namespaceDto
     * @throws Exception
     */
    public void removePrivateNamespaceNodes(NamespaceDto namespaceDto)throws Exception;

    /**
     * 添加租户分区独占主机
     * @param namespaceDto
     * @throws Exception
     */
    public void addPrivilegeNamespaceNodes(NamespaceDto namespaceDto)throws Exception;
    /**
     * 获取分区剩下的cpu和内存
     * @param namespace
     * @return Map<String, String>
     * @throws Exception
     */
    Map<String, String> getNamespaceResourceRemainQuota(String namespace) throws Exception;

    ActionReturnUtil checkResourceInTemplateDeploy(Map<String, Long> requireResource, Map<String, String> remainResource) throws Exception;

    void checkStorageResource(Map<String, Long> serviceRequireRes, Map<String, String> remainResource);

    boolean checkTransferResource(List<NamespaceDto> namespaceDtos) throws Exception;

    ActionReturnUtil createQuota(NamespaceDto namespaceDto, Cluster cluster) throws Exception;

}
