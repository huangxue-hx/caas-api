package com.harmonycloud.service.tenant;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.k8s.bean.ResourceQuotaList;


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
     * 查询namespace列表
     * @param tenantid
     * @param tenantname
     * @return
     */
    ActionReturnUtil getNamespaceList(String tenantid, String tenantname) throws Exception;

    /**
     * 查询namespace详情
     * @param tenantid
     * @param name
     * @return
     */
    ActionReturnUtil getNamespaceDetail(String name,String tenantid) throws Exception;
    /**
     * 根据租户id称查询租户下namespace简单列表
     * @param tenantname
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
//<<<<<<< HEAD
    public ResourceQuotaList getResouceQuota(String namespace,Cluster cluster)throws Exception;
    /**
     * 获取分区的专属标签
     * @param tenantid
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getPrivatePartitionLabel(String tenantid,String namespace) throws Exception;
    /**
     * 根据tenantid查询namespace列表详情
     * @param tenantid
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getNamespaceListByTenantid(String tenantid)throws Exception;
}
