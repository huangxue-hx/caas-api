package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.Map;

/**
 * created at 2017/1/20
 * @author jmi
 *
 */
public interface DashboardService {
	
	/**
	 * 获取pod 信息
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPodInfo(Cluster cluster ) throws Exception;
	
	/**
	 * 获取机器信息
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getInfraInfo(Cluster cluster) throws Exception;

	/**
	 * 获取node信息
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getNodeInfo(Cluster cluster, String nodename) throws Exception;
	/**
	 * 获取警告信息
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getWarningInfo(Cluster cluster ,String namespace) throws Exception;
	
	/**
	 * 获取事件信息
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getEventInfo(Cluster cluster, String namespace) throws Exception;

	/**
	 * 读取node的license
	 * @param
	 * @return
	 * @throws Exception
	 */

	public ActionReturnUtil getNodeLicense() throws Exception;

	public Map<String, Object> getInfraInfoWorkNode(Cluster cluster) throws Exception;

	/**
	 * 获取组件pod列表
	 * @param cluster
	 * @param podName
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil listK8sComponentPod(Cluster cluster, String podName, String namespace) throws Exception;

	/**
	 * 获取组件pod详情
	 * @param cluster
	 * @param name
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	ActionReturnUtil getComponentPodDetail(Cluster cluster, String name, String namespace) throws Exception;

}
