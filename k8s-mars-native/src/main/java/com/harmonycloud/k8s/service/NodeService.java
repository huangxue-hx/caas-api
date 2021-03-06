package com.harmonycloud.k8s.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.enumm.NodeTypeEnum;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class NodeService {

	private static Logger LOG = LoggerFactory.getLogger(NodeService.class);

	public K8SClientResponse getSpecifyNode(String name, Map<String, Object> headers,
			Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
		return response;
	}

	public K8SClientResponse getNodeByNamespace(String namespace, Map<String, Object> headers,
			Map<String, Object> bodys, String method) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.NODE);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,null);
		return response;
	}

	/**
	 * node 列表
	 * 
	 * @return NodeList
	 */
	public NodeList listNode() {
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			NodeList nodeList = K8SClient.converToBean(response, NodeList.class);
			return nodeList;
		}
		return null;
	}
	public NodeList listNodeByLabel(Map<String, Object> headers,Map<String, Object> bodys, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setResource(Resource.NODE);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            NodeList nodeList = K8SClient.converToBean(response, NodeList.class);
            return nodeList;
        }
        return null;
    }
	/**
	 * 根据nodename获取node
	 * 
	 * @param nodeName
	 * @return Node
	 */
	public Node getNode(String nodeName,Cluster cluster) {
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE).setSubpath(nodeName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			Node node = K8SClient.converToBean(response, Node.class);
			return node;
		}
		return null;
	}

	
	/**
	 * 获取node事件列表
	 * @return
	 */
	public EventList listNodeEvent(Map<String, Object> queryParm){
		K8SURL url = new K8SURL();
		url.setResource(Resource.EVENT);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, queryParm);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			EventList eventList = K8SClient.converToBean(response, EventList.class);
			return eventList;
		}
		return null;
	}
	
	/**
	 * 更新node
	 * @param bodys 更新后的参数
	 * @param nodeName node名称
	 * @return
	 */
	public K8SClientResponse updateNode(Map<String, Object> bodys,String nodeName,Cluster cluster){
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE).setSubpath(nodeName);
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys,cluster);
		return response;
	}

	/**
	 * node 列表
	 *
	 * @return NodeList
	 */
	public NodeList listNode(Cluster cluster) {
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			NodeList nodeList = K8SClient.converToBean(response, NodeList.class);
			return nodeList;
		}
		LOG.error("list node error:{}, clusterId:{}", response.getBody(), cluster.getId());
		return null;
	}

	/**
	 * 获取除共享节点外的节点名称列表
	 *
	 * @return NodeList
	 */
	public List<String> listNotPublicNode(Cluster cluster) {
		K8SURL url = new K8SURL();
		url.setResource(Resource.NODE);
		List<String> list = new ArrayList<>();
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			NodeList nodeList = K8SClient.converToBean(response, NodeList.class);
			List<Node> nodeItems = null;
			if(nodeList != null && nodeList.getItems() != null) {
				nodeItems = nodeList.getItems();
				for(Node node : nodeItems) {
					Map<String, Object> labels = node.getMetadata().getLabels();
					//是否共享节点，共享节点排除
					if (labels.get(NodeTypeEnum.PUBLIC.getLabelKey()) != null
							&& labels.get(NodeTypeEnum.PUBLIC.getLabelKey()).equals(NodeTypeEnum.PUBLIC.getLabelValue())) {
						continue;
					}
					list.add(node.getMetadata().getName());
				}
			}

			return list;
		}
		return null;
	}
}
