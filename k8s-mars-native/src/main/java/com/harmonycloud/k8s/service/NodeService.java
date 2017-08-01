package com.harmonycloud.k8s.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.cluster.bean.Cluster;
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

	public K8SClientResponse getSpecifyNode(String namespace, String name, Map<String, Object> headers,
			Map<String, Object> bodys, String method) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.NODE).setName(name);
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys,null);
		return response;
	}

	public K8SClientResponse getNodeByNamespace(String namespace, Map<String, Object> headers,
			Map<String, Object> bodys, String method) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.NODE);
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys,null);
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
	public NodeList listNodeByLabel(Map<String, Object> headers,Map<String, Object> bodys) {
        K8SURL url = new K8SURL();
        url.setResource(Resource.NODE);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys);
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
		return null;
	}

	/**
	 * node 列表
	 *
	 * @return NodeList
	 */
	public List<String> listNotWorkNode(Cluster cluster) {
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
					if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null
							&& node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_A)) {
						list.add(node.getMetadata().getName());
					}

					if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null
							&& node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_B)) {
						list.add(node.getMetadata().getName());
					}

					if (labels.get(CommonConstant.MASTERNODELABEL) != null) {
						list.add(node.getMetadata().getName());
					}

				}
			}

			return list;
		}
		return null;
	}
}
