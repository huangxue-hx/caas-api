package com.harmonycloud.service.platform.service;

import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.bean.NodeLabel;

public interface NodeService {
	
	public ActionReturnUtil listNode(String clusterId) throws Exception;

	public ActionReturnUtil getNodeDetail(String nodeName,Cluster cluster) throws Exception;

	public ActionReturnUtil listNodeEvent(String nodeName,Cluster cluster) throws Exception;

	public ActionReturnUtil listNodeLabels(String nodeName,Cluster cluster) throws Exception;
	
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
	 * 获得tenant的私有节点列表
	 * @param tenantName
	 * @return
	 */
	public List<NodeDto> listPrivateNodeByTenant(String tenantName,Cluster cluster) throws Exception;
	/**
	 * 获得所有的共享节点列表
	 * @return
	 */
	public List<String> listShareNode(Cluster cluster) throws Exception;
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
	 * @param harborIp 集群harborip
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil addNode(String host,String user,String passwd,String masterIp,String harborIp,String clusterId) throws Exception;
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
     * @param masterIp 集群masterip
	 * @param token admin的token
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil removeNode( String host,String user,String passwd,String clusterId) throws Exception;
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
     * 根据clusterid获取节点上线状态
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
     * 更新闲置节点或者无状态节点为共享节点
     * @param nodeName
     * @param clusterId
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updateShareToNode(String nodeName,String clusterId) throws Exception;
    /**
     * 取消上线错误状态
     * @param id
     * @return
     * @throws Exception
     */
    public ActionReturnUtil cancelAddNode(Integer id) throws Exception;
    
    /**
     * 获取集群的所有node的label
     * @param id
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listNodeLabels(Cluster cluster) throws Exception;
}
