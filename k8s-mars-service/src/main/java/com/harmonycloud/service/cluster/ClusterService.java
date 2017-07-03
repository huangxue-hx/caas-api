package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.ClusterDomain;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Service
public interface ClusterService {

	/**
	 * 新增集群
	 * 
	 * @param cluster
	 * @return
	 */
	public ActionReturnUtil addCluster(Cluster cluster) throws Exception ;

	/**
	 * 获取所有集群
	 * 
	 * @return
	 */
	public ActionReturnUtil listClusters() throws Exception ;
	/**
     * 查询集群列表下租户的概览
     * 
     * @return
     */
    public ActionReturnUtil clusterListWithTenantOverView() throws Exception ;

	/**
	 * 根据id 查找cluster
	 * @param id
	 * @return
	 */
	Cluster findClusterById(String id);

	/**
	 * 根据tenantId 返回cluster信息
	 * @param tenantId
	 * @return
	 * @throws Exception
     */
	Cluster findClusterByTenantId(String tenantId) throws Exception;

	/**
	 * 获取所有集群
	 *
	 * @return
	 */
	public List<Cluster> listCluster() throws Exception;
	/**
	 * 根据clusterid查询租户的配额
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	public List<Map>  getTenantQuotaByClusterId(String clusterId) throws Exception;

	/**
	 * 获取集群domain
	 *
	 * @return
	 */
	ClusterDomain find() throws Exception;
	
	/**
	 * 修改集群domain
	 *
	 * @return
	 */
	 public ActionReturnUtil updateDomain(String domain) throws Exception;
}
