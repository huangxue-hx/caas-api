package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;

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
	public ActionReturnUtil getPodInfo() throws Exception;
	
	/**
	 * 获取机器信息
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getInfraInfo() throws Exception;

	/**
	 * 获取机器信息
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getInfraInfo(Cluster cluster) throws Exception;

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
	public ActionReturnUtil getWarningInfo(String namespace) throws Exception;
	
	/**
	 * 获取事件信息
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getEventInfo(String namespace) throws Exception;

	/**
	 * 读取node的license
	 * @param
	 * @return
	 * @throws Exception
	 */

	public ActionReturnUtil getＮodeLicense() throws Exception;

	public Map<String, Object> getInfraInfoWorkNode(Cluster cluster) throws Exception;

}
