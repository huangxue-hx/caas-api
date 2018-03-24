package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.NamespaceList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class NamespaceService {

	/**
	 * 获取所有Namespace
	 * 
	 * @param k8sUrl
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public NamespaceList getAllNamespaces(Cluster cluster) {
		try {
			K8SURL k8SURL = new K8SURL();
			k8SURL.setResource(Resource.NAMESPACE);
			K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null,cluster);
			NamespaceList cList = K8SClient.converToBean(response, NamespaceList.class);
			return cList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 根据label查询namespace
	 * @param lable
	 * @return
	 */
	public NamespaceList getNamespacesListbyLabelSelector(String lable,Cluster cluster) {
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", lable);
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL,  HTTPMethod.GET, null, bodys,cluster);
		NamespaceList namespaceList = K8SClient.converToBean(response, NamespaceList.class);
		return namespaceList;
	}
	public K8SClientResponse create(Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys,cluster);
		return response;
	}

	public K8SClientResponse getNamespace(String name, Map<String, Object> headers, Map<String, Object> bodys,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.NAMESPACE).setNamespace(name);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, headers, bodys,cluster);
		return response;
	}

//	public K8SClientResponse delete(Map<String, Object> headers, Map<String, Object> bodys, String method) {
//		K8SURL k8SURL = new K8SURL();
//		k8SURL.setResource(Resource.NAMESPACE);
//		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys);
//		return response;
//	}

	public K8SClientResponse delete(Map<String, Object> headers, Map<String, Object> bodys, String method,
			String name,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setNamespace(name).setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys,cluster);
		return response;
	}

	public K8SClientResponse list(Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys,cluster);
		return response;
	}

	public K8SClientResponse update(Map<String, Object> headers, Map<String, Object> bodys, String name,Cluster cluster) {
		K8SURL k8SURL = new K8SURL();
		k8SURL.setNamespace(name).setResource(Resource.NAMESPACE);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.PUT, headers, bodys,cluster);
		return response;
	}

	public static void main(String[] args) {
		NamespaceService namespaceService = new NamespaceService();
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", "nephele_tenant=hero");

		K8SClientResponse namespace = namespaceService.getNamespace("zheng-fainall", null, null,null);

		Namespace n = JsonUtil.jsonToPojo(namespace.getBody(), Namespace.class);

		/*
		 * NamespaceList namespaceList =
		 * namespaceService.getAllNamespaces("http://10.10.102.25:8080",null,
		 * bodys);
		 * 
		 * System.out.println(JsonUtil.convertToJson(namespaceList.getItems()));
		 * 
		 * 
		 * K8SClientResponse k8SClientResponse = namespaceService.list(null,
		 * bodys, HTTPMethod.GET);
		 * 
		 * NamespaceList list = JsonUtil.jsonToPojo(k8SClientResponse.getBody(),
		 * NamespaceList.class);
		 */

		K8SClientResponse k8SClientResponse = namespaceService.delete(null, bodys, HTTPMethod.DELETE, "hero-creater",null);


	}
}
