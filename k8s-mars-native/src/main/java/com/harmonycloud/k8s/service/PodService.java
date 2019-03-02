package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.exception.MarsRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpK8SClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.PodList;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class PodService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PodService.class);
	private String surfix="/pods";
	
	public K8SClientResponse getSpecifyPod(String namespace,String name, Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
		return response;
	}
	
	public K8SClientResponse getPodByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
		return response;
	}
	
	public K8SClientResponse getPodLogByNamespace(String namespace, String name, String subPath, Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD).setName(name).setSubpath(subPath);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
		return response;
	}
	
	/**
	 * 获取所有Pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 * @see listPods
	 */
	@Deprecated
	public PodList getAllPods(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys){
		try {
			String url=k8sUrl+Constant.POD_VERSION+surfix;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			PodList cList=JsonUtil.jsonToPojo(body.toString(),PodList.class);
			
			return cList;
		} catch (Exception e) {
			LOGGER.warn("获取pod列表失败");
		}
		return null;
	}
	
	/**
	 * 获取所有Pod
	 * @return PodList
	 */
	public PodList listPods(Cluster cluster,String nodeName){
		K8SURL url = new K8SURL();
		url.setResource(Resource.POD);
		Map<String,Object> bodys = new HashMap<>();
		bodys.put("fieldSelector", "spec.nodeName=" + nodeName);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys,cluster);
		if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			PodList podList = K8SClient.converToBean(response, PodList.class);
			return podList;
		}
		return null;
	}

	/**
	 * 获取所有特定Namespace下的所有pods
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public PodList getPodsByNamespace(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace){
		try {
			String url=k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			PodList pList=JsonUtil.jsonToPojo(body.toString(),PodList.class);
			
			return pList;
		} catch (Exception e) {
			LOGGER.warn("获取pod失败，namespace:{}", namespace, e);
		}
		return null;
	}
	
	
	/**
	 * 获取所有特定Namespace下的pod详情
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public Pod getPodByName(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace, String name){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix+"/"+name;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			Pod p = JsonUtil.jsonToPojo(body.toString(),Pod.class);
			
			return p;
		} catch (Exception e) {
			LOGGER.warn("获取pod:{}失败, namespace:{}", name, namespace, e);
		}
		return null;
	}
	
	/**
	 * 删除特定Namespace下的所有pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public UnversionedStatus deletePodByNamespace(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace, String name){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix;
			String body = HttpK8SClientUtil.httpDeleteRequest(url,headers,bodys);
			UnversionedStatus u = JsonUtil.jsonToPojo(body.toString(),UnversionedStatus.class);
			
			return u;
		} catch (Exception e) {
			LOGGER.warn("删除pod:{}失败, namespace:{}", name, namespace, e);
		}
		return null;
	}
	
	/**
	 * 创建pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public Pod createPod(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix;
			String body = HttpK8SClientUtil.httpPostJsonRequest(url,headers,bodys);
			Pod p = JsonUtil.jsonToPojo(body.toString(),Pod.class);
			
			return p;
		} catch (Exception e) {
			LOGGER.warn("创建pod失败, namespace:{}", namespace, e);
		}
		return null;
	}
	
	/**
	 * 修改特定Namespace下的pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public Pod updtaePod(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace, String name){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix+"/"+name;
			String body = HttpK8SClientUtil.httpPutJsonRequest(url,headers,bodys);
			Pod p = JsonUtil.jsonToPojo(body.toString(),Pod.class);
			
			return p;
		} catch (Exception e) {
			LOGGER.warn("更新pod:{}失败, namespace:{}", name, namespace, e);
		}
		return null;
	}
	
	/**
	 * 删除某一特定pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public UnversionedStatus deletePod(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace, String name){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix+"/"+name;
			String body = HttpK8SClientUtil.httpDeleteRequest(url,headers,bodys);
			UnversionedStatus u = JsonUtil.jsonToPojo(body.toString(),UnversionedStatus.class);
			
			return u;
		} catch (Exception e) {
			LOGGER.warn("删除pod:{}失败, namespace:{}", name, namespace, e);
		}
		return null;
	}
	
	/**
	 * 链接某一特定pod
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public String connectPod(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace, String name){
		try {
			String url = k8sUrl+Constant.POD_VERSION+"/namespaces/"+namespace+surfix+"/"+name+"/attach";
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			
			return body;
		} catch (Exception e) {
			LOGGER.warn("链接pod:{}失败, namespace:{}", name, namespace, e);
		}
		return null;
	}
	
	/**
	 * 创建Pod
	 * @param pod 
	 * @param cluster
	 * @return Job
	 */
	public ActionReturnUtil addPod(String namespace, Pod pod, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD);
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", pod.getMetadata());
		bodys.put("kind", pod.getKind());
		bodys.put("spec", pod.getSpec());
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody(),UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(us.getMessage());
        }
		return ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * getPod
	 * @param name 
	 * @param cluster
	 * @return Job
	 */
	public K8SClientResponse getPod(String namespace, String name, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
		return response;
	}
	
	/**
	 * 删除Pod
	 * @param name 
	 * @param cluster
	 * @return Job
	 */
	public K8SClientResponse deletePod(String namespace, String name, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null,cluster);
		return response;
	}
	
	/**
	 * getPod
	 * @param name 
	 * @param cluster
	 * @return Job
	 */
	public K8SClientResponse getPods(String namespace, Map<String,Object> bodys, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,bodys,cluster);
		return response;
	}
	
	/**
	 * 删除Pod
	 * @param name 
	 * @param cluster
	 * @return Job
	 */
	public K8SClientResponse deletePods(String namespace, Map<String,Object> bodys, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.POD);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,bodys,cluster);
		return response;
	}

	public PodList getPodByServiceName(String namespace, String serviceName, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		Map<String, Object> label = new HashMap<>();
		label.put("labelSelector","app=" + serviceName);
		url.setNamespace(namespace).setResource(Resource.POD);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, null, label, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			LOGGER.error("获取pod失败,{}", serviceName);
			return null;
		}
		PodList podList = JsonUtil.jsonToPojo(response.getBody(), PodList.class);
		return podList;
	}
	
}
