package com.harmonycloud.service.tenant;

import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dto.tenant.CreateNetwork;


/**
 * 2017/2/14
 * 
 * @author zgl
 *
 */
public interface NetworkService {
	/**
	 * 根据tenantid查询网络列表
	 * 
	 * @param tenantid
	 * @return
	 */
	List<Map<String, Object>> networkList(String tenantid) throws Exception;

	/**
	 * 根据tenantName，networkName获取网络信息
	 * @param networkName
	 * @param tenantName
	 * @return
	 */
	NetworkCalico getnetworkbyname(String networkName, String tenantName) throws Exception;

	/**
	 * 根据networkid获取网络信息
	 * 
	 * @param networkid
	 * @return
	 */
	NetworkCalico getnetworkbyNetworkid(String networkid) throws Exception;

	/**
	 * 根据tenantid获取网络信息
	 * 
	 * @param tenantid
	 * @return
	 */
	List<NetworkCalico> getnetworkbyTenantid(String tenantid) throws Exception;

	/**
	 *创建网络
	 * @param createNetwork
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil networkCreate(CreateNetwork createNetwork) throws Exception;

	/**
	 * 根据networkid删除网络
	 * 
	 * @param networkid
	 */
	ActionReturnUtil networkDelete(String networkid) throws Exception;
	/**
	 * 根据tenantid删除网络
	 * @param networkid
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil networkDeleteByTenantid(String networkid) throws Exception;

	/**
	 *创建子网络
	 * @param networkid
	 * @param subnetname
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil subnetworkCreate(String networkid, String subnetname) throws Exception;

	/**
	 * 根据subnetid删除子网
	 * 
	 * @param subnetid
	 */
	void subnetworkDelete(String subnetid) throws Exception;

	/**
	 * 根据subnetid ，namespace 更新子网绑定信息
	 * 
	 * @param subnetid
	 * @param namespace
	 * @return
	 */
	ActionReturnUtil subnetworkupdatebinding(String subnetid, String namespace) throws Exception;

	/**
	 * 查看网络子网
	 * 
	 * @param networkid
	 * @return
	 */
	List<Map<String, Object>> subnetworklistbynetworkid(String networkid) throws Exception;

	/**
	 * 根据networkid和 subnetname获取子网信息
	 * 
	 * @param networkid
	 * @param subnetname
	 * @return
	 */
	NamespceBindSubnet getsubnetbySubnetname(String networkid, String subnetname) throws Exception;

	/**
	 * 根据subnetid和 subnetname获取子网信息
	 * 
	 * @param subnetid
	 * @param subnetname
	 * @return
	 */
	NamespceBindSubnet getsubnetbySubnetnameAndSubnetid(String subnetid, String subnetname) throws Exception;

	/**
	 * 解除绑定namespace
	 * 
	 * @param namespace
	 * @return
	 */
	ActionReturnUtil subnetRemoveBing(String namespace) throws Exception;

	/**
	 * 根据subnetid获取子网
	 * 
	 * @param subnetid
	 * @return
	 */
	NamespceBindSubnet getsubnetbySubnetid(String subnetid) throws Exception;

	/**
	 * 根据networkid查询子网列表
	 * 
	 * @param subnetid
	 * @return
	 */
	List<NamespceBindSubnet> getsubnetbynetworkid(String networkid) throws Exception;

	/**
	 * 获取网络详情
	 * 
	 * @param networkid
	 * @param tenantid
	 * @param bind
	 * @return
	 */
	ActionReturnUtil calicoNetworkdetail(String networkid, String tenantid, String bind) throws Exception;


	/**
	 * 根据networkidfrom，networkidto查询NetworkTopology
	 * 
	 * @param networkidfrom
	 * @param networkidto
	 * @param networknamefrom
	 * @param networknameto
	 * @return
	 */
	NetworkTopology getTopologybyNetworkidfromAndNetworkidto(String networkidfrom, String networkidto,
                                                                    String networknamefrom, String networknameto) throws Exception;

	/**
	 * 根据起点networkid查询NetworkTopology list
	 * 
	 * @param networkid
	 * @return
	 */
	List<NetworkTopology> getTopologybyNetworkid(String networkid) throws Exception;

	/**
	 * 根据目的地networkid查询NetworkTopology list
	 * 
	 * @param networkid
	 * @return
	 */
	List<NetworkTopology> getTopologybydestination(String networkid) throws Exception;

	/**
	 * 根据id删除NetworkTopology
	 * 
	 * @param id
	 * @return
	 */
	NetworkTopology deletetopologybyId(Integer id) throws Exception;

	/**
	 * 创建networkTopology
	 * 
	 * @param networkTopology
	 */
	void createNetworkTopology(NetworkTopology networkTopology) throws Exception;

	ActionReturnUtil subnetChecked(String subnetid, String subnetname) throws Exception;

	/**
	 * 根据networkid获取网络拓扑关系
	 * 
	 * @param networkid
	 * @return
	 */
	List<NetworkTopology> getNetworkTopologyList(String networkid) throws Exception;
	/**
	 * 网络初始化
	 * 
	 * @return
	 */
	ActionReturnUtil netwrokInit(Cluster cluster) throws Exception;

	/**
     * 根据networkid获取目的地网络拓扑关系
     * 
     * @param networkid
     * @return
     */
	List<NetworkTopology> getTrustNetworkTopologyList(String networkid) throws Exception;
    
	public ActionReturnUtil listProvider() throws Exception;

}
