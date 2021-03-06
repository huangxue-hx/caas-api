package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public interface NodeInstallProgressService {

	/**
	 * 新增节点上线信息
	 * 
	 * @param cluster
	 * @return
	 */
	public ActionReturnUtil addNodeInLineInfo(NodeInstallProgress nodeInstallProgress) throws Exception ;
	/**
	 * 更新节点上线信息
	 * @param nodeInstallProgress
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil updateNodeInLineInfo(NodeInstallProgress nodeInstallProgress) throws Exception ;
	/**
	 * 节点上线信息删除节点上线信息
	 * @param nodeInstallProgress
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deleteNodeInLineInfo(String nodeIp) throws Exception ;
	/**
	 * 根据nodeip查询信息
	 * @param String nodeIp
	 * @return
	 * @throws Exception
	 */
	public NodeInstallProgress getNodeInLineInfoByNodeIp(String nodeIp) throws Exception ;
	/**
	 * 根据安装状态查询信息
	 * @param installStatus
	 * @return
	 * @throws Exception
	 */
	public List<NodeInstallProgress> getNodeInLineInfoByInstallStatusAndClusterId(String installStatus,String clusterId) throws Exception ;
	/**
	 * 获取错误信息
	 * @return
	 * @throws Exception
	 */
	public String getOnLineErrorStatus() throws Exception;
	/**
	 * 取消错误的上线状态
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public void cancelAddNode(Integer id) throws Exception;

}
