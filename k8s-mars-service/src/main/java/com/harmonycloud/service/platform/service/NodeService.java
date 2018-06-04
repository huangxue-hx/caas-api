package com.harmonycloud.service.platform.service;

import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.bean.NodeLabel;

public interface NodeService {
	
	public ActionReturnUtil listNode(String clusterId) throws Exception;

	/**
	 * 处理节点资源信息
	 * @param items
	 * @param cluster
	 * @param nodeDtoList
	 * @throws Exception
	 */
	public void dealNodeStatus(List<Node> items, Cluster cluster, List<NodeDto> nodeDtoList) throws Exception;

	public ActionReturnUtil getNodeDetail(String nodeName,Cluster cluster) throws Exception;

	public ActionReturnUtil listNodeEvent(String nodeName,Cluster cluster) throws Exception;

	public ActionReturnUtil listNodeLabels(String nodeName,Cluster cluster, boolean isShowGroup) throws Exception;
	
	public ActionReturnUtil updateNodeLabels(String nodeName,NodeLabel labels,Cluster cluster) throws Exception;
	public ActionReturnUtil listNodeAvailablelabels(Cluster cluster) throws Exception;
	/**
	 * 给node添加label
	 * @param nodeName
	 * @param labels
	 * @return
	 */
	public ActionReturnUtil addNodeLabels(String nodeName,Map<String, String> labels,String clusterId) throws Exception;
	/**
	 * 给node删除label
	 * @param nodeName
	 * @param labels
	 * @return
	 */
	public ActionReturnUtil removeNodeLabels(String nodeName,Map<String, String> labels,Cluster cluster) throws Exception;
	/**
	 * 获取node的状态标签
	 * @param nodeName
	 * @return
	 */
	public Map<String, String> listNodeStatusLabels(String nodeName,Cluster cluster) throws Exception;
	/**
	 * 获得所有的私有节点列表
	 * 
	 * @return
	 */
	public List<NodeDto> listAllPrivateNode(Cluster cluster) throws Exception;
	/**
	 * 获得对应标签的私有节点列表
	 * @param label
	 * @param cluster
	 * @return
	 */
	public List<NodeDto> listPrivateNodeByLabel(String label,Cluster cluster) throws Exception;

	/**
	 * 根据分区获取主机列表
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public List<NodeDto> listNodeByNamespaces(String namespace) throws Exception;
//	/**
//	 * 获得所有的共享节点列表
//	 * @return
//	 */
//	public List<String> listShareNode(String  clusterId) throws Exception;
	/**
	 * 获得所有的闲置节点列表
	 * @return
	 */
	public List<String> getAvailableNodeList(Cluster cluster) throws Exception;
	/**
	 * 根据namespace获得该namespace独占的私有节点列表
	 * @return
	 */
	public List<String> getPrivateNamespaceNodeList(String namespace,Cluster cluster) throws Exception;
	/**
	 * 节点上线
	 * @param host 上线机器的ip
	 * @param user 上线机器的用户名
	 * @param passwd 上线机器的密码
	 * @param masterIp 集群masterip
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil addNode(String host,String user,String passwd,String masterIp,String clusterId) throws Exception;
	/**
	 * 检测主机状态
	 * @param host
	 * @param user
	 * @param passwd
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil checkNodeStatus(String host,String user,String passwd) throws Exception;
	/**
	 * 节点下线
     * @param host 上线机器的ip
     * @param user 上线机器的用户名
     * @param passwd 上线机器的密码
	 * @return
	 * @throws Exception
	 */

	public ActionReturnUtil removeNode( String host,String user,String passwd,String clusterId) throws Exception;

	/**
	 * get hostname
	 * @param host
	 * @param cluster
	 * @return
	 * @throws Exception
	 */
	public String gethostname( String host,Cluster cluster) throws Exception;
    /**
     * 根据nodeIp,clusterId获取node
     * @param nodeIp,clusterId
     * @return
     * @throws Exception
     */
    public Map  getNode(String nodeIp,String clusterId) throws Exception;
    /**
     * 根据nodeIp,cluster获取node
     * @param nodeIp
     * @param cluster
     * @return
     * @throws Exception
     */
    public Map  getNode(String nodeIp,Cluster cluster) throws Exception;
    /**
     * 根据clusterId获取节点上线状态
     * @param clusterId
     * @return
     * @throws Exception
     */
    public List<NodeInstallProgress>  getOnLineStatusWithClusterId(String clusterId) throws Exception;
    /**
     * 获取错误状态
     * @return
     * @throws Exception
     */
    public String  getOnLineErrorStatus() throws Exception;

	/**
	 * 更新闲置节点状态
	 * @param nodeName
	 * @param clusterId
	 * @param nodeType 1 共享，2负债均衡，3构建节点，4系统节点
	 * @throws Exception
	 */
	public void updateIdleNodeStatus(String nodeName,String clusterId,Integer nodeType) throws Exception;

	/**
	 * 更新工作节点为闲置节点状态
	 * @param nodeName
	 * @param clusterId
	 * @throws Exception
	 */
	public void updateWorkNodeToIdleStatus(String nodeName,String clusterId) throws Exception;
    /**
     * 取消上线错误状态
     * @param id
     * @return
     * @throws Exception
     */
    public ActionReturnUtil cancelAddNode(Integer id) throws Exception;
    
    /**
     * 获取集群的所有node的label
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listNodeLabels(String namespace, String clusterId) throws Exception;

	/**
	 * 切换节点调度状态
	 * @param nodeName
	 * @param schedulable
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil switchNodeSchedulable(String nodeName, Boolean schedulable, String clusterId) throws Exception;

	public ActionReturnUtil drainPod(String nodeName,String clusterId) throws Exception;

	public ActionReturnUtil getDrainPodProgress(String nodeName, String clusterId) throws Exception;

	/**
	 * 移除私有分区主机状态
	 * @param nodeName
	 * @param updateLabel
	 * @param cluster
	 * @throws Exception
	 */
	public void removePrivateNamespaceNodes(String nodeName, Map<String, String> updateLabel,Map<String, String> deleteLabel, Cluster cluster) throws Exception;
}
