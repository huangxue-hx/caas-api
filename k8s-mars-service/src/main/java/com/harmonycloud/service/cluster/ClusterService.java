package com.harmonycloud.service.cluster;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.ClusterDomain;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public interface ClusterService {

	/**
	 * 获取容器云平台部署的集群
	 * @return
	 * @throws Exception
	 */
	Cluster getPlatformCluster() throws Exception;

	/**
	 * 根据id 查找cluster
	 * @return
	 */
	Cluster findClusterById(String clusterId);

	String getHarborHost(String clusterId);

	/**
	 * 获取集群资源使用量列表
	 * @return
	 * @throws Exception
	 */
	public List<Map> ListClusterQuota() throws Exception;

	/**
	 * 根据集群id获取集群资源使用量
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	public Map getClusterQuotaByClusterId(String clusterId) throws Exception;

	/**
	 * 获取所有状态正常的业务集群列表，不包含容器云平台部署的集群
	 *
	 * @return
	 */
	List<Cluster> listCluster() throws Exception;

	/**
	 * 获取所有集群，包括上层集群
	 *
	 * @return
	 */
	List<Cluster> listAllCluster(Boolean isEnable);

	/**
	 * 获取某个数据中心下的集群列表,为空则获取所有的业务集群
	 *
	 * @return
	 */
	List<Cluster> listCluster(String dataCenter, Boolean isEnable, String template) throws Exception;

	/**
	 * 查询以数据中心名称分组的集群列表
	 * @param isEnable
	 * @return
	 * @throws Exception
	 */
	Map<String, List<Cluster>> groupCluster(Boolean isEnable) throws Exception;

    List<String> listDisableClusterIds() throws Exception;

	Map<String, Map<String, Object>> getClusterAllocatedResources(String clusterId) throws Exception;

	ActionReturnUtil getClusterResourceUsage(String clusterId, String nodeName) throws Exception;

	/**
	 * 获取集群domain
	 *
	 * @return
	 */
	ClusterDomain findDomain(String namespace) throws Exception;

	/**
	 * 根据harborHost查询harbor具体信息
	 * @param host harbor的host
	 * @return
	 */
	 HarborServer findHarborByHost(String host) throws MarsRuntimeException;

	/**
	 * 查询所有harbor服务
	 * @return
	 */
	 Set<HarborServer> listAllHarbors() throws Exception;

	/**
	 * 获取入口
	 * @param namespace
	 * @return List<String>
	 * @throws Exception
	 */
	String getEntry(String namespace) throws Exception;

	String getClusterNameByClusterId(String clusterId) throws Exception;

	Map<String, Object> getClusterComponentStatus(String clusterId) throws Exception;

	void deleteClusterData(String clusterId) throws Exception;

	Map<String, String> getClustersStorageCapacity () throws Exception;
}
