package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.HttpK8SClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.DeleteOptions;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.DeploymentRollback;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

/**
 *
 * @author qg, jmi
 * created 2017-1-11
 */
@Service
public class DeploymentService {

	private String surfix="/deployments";

//	private String url = "http://10.10.102.45:8080"+Constant.APIS_EXTENTIONS_V1BETA1_VERSION+"/namespaces/"+namespace+"/deployments";

	/**
	 * 获取所有Deployment
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public DeploymentList getAllDeployments(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+surfix;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			DeploymentList dList=JsonUtil.jsonToPojo(body.toString(),DeploymentList.class);

			return dList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取某namespace下所有Deployment
	 * @param namespace
	 * @param labels
	 * @param name
	 * @return
	 */
	public K8SClientResponse doDeploymentsByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
		return response;
	}

	/**
	 * 删除某namespace下所有Deployment
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public boolean deleteDeploymentsByNamespace(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+"/namespaces/"+namespace+surfix;
			bodys.put("namespace", namespace);
			String body = HttpK8SClientUtil.httpDeleteRequest(url,headers,bodys);
			UnversionedStatus us = JsonUtil.jsonToPojo(body.toString(),UnversionedStatus.class);

			if(us!=null && "Success".equals(us.getStatus())){
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 创建Deployment
	 * @param deployment
	 * @return
	 */
	public Deployment create(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String namespace,Deployment deployment){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+"/namespaces/"+namespace+surfix;
			bodys.put("body", deployment);
			bodys.put("namespace", namespace);
			String body = HttpK8SClientUtil.httpPostJsonRequest(url,headers,bodys);
			Deployment d=JsonUtil.jsonToPojo(body.toString(),Deployment.class);

			return d;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


	/**
	 * 编辑特定Deployment
	 * @param k8sUrl
	 * @param headers
	 * @param bodys
	 * @param namespace
	 * @param name
	 * @return
	 */
	public K8SClientResponse doSpecifyDeployment(String namespace,String name, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		if(StringUtils.isBlank(name)){
			url.setNamespace(namespace).setResource(Resource.DEPLOYMENT);
		}else {
			url.setNamespace(namespace).setResource(Resource.DEPLOYMENT).setName(name);
		}
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
		return response;
	}
	
	

	/**
	 * 修改特定Deployment
	 * @param namespace
	 * @param name
	 * @param deployment
	 * @return
	 */
	public Deployment replaceSpecifiedDeployment(String k8sUrl, Map<String,Object> bodys,Map<String,Object> headers,String namespace,String name){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+"/namespaces/"+namespace+surfix+"/"+name;
			String body = HttpK8SClientUtil.httpPutJsonRequest(url,headers,bodys);
			Deployment d=JsonUtil.jsonToPojo(body.toString(),Deployment.class);
			return d;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 删除特定Deployment
	 * @param k8sUrl
	 * @param headers
	 * @param bodys
	 * @param namespace
	 * @param name
	 * @return
	 */
	public boolean deleteSpecifiedDeployment(String k8sUrl, Map<String,Object> bodys,Map<String,Object> headers,String namespace,String name,DeleteOptions dp){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+"/namespaces/"+namespace+surfix+"/"+name;
			bodys.put("namespace", namespace);
			bodys.put("name", name);
			bodys.put("body", dp);
			String body = HttpK8SClientUtil.httpDeleteRequest(url,headers,bodys);
			UnversionedStatus us=JsonUtil.jsonToPojo(body.toString(),UnversionedStatus.class);
			
			if(us!=null && "Success".equals(us.getStatus())){
				return true;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 将Deployment撤回到某个版本
	 * @param k8sUrl
	 * @param headers
	 * @param bodys
	 * @param namespace
	 * @param name
	 * @return
	 */
	public DeploymentRollback rollbackSpecifiedDeployment(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys, String namespace,String name, Deployment deployment, DeploymentRollback dr){
		try {
			String url=k8sUrl+Constant.DEPLOYMENT_VERSION+"/namespaces/"+namespace+surfix+"/"+name+"/rollback";
			bodys.put("namespace", namespace);
			bodys.put("name", name);
			bodys.put("body", dr);
			String body = HttpK8SClientUtil.httpPostJsonRequest(url,headers,bodys);
			DeploymentRollback us=JsonUtil.jsonToPojo(body.toString(), DeploymentRollback.class);
			
			return us;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public K8SClientResponse rollbackSpecifiedDeployment(DeploymentRollback deploymentRollback, String namespace,String name, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setName(name).setResource(Resource.DEPLOYMENT).setSubpath("rollback").setApiGroup(APIGroup.APIS_APPS_V1BETA1);
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("name", deploymentRollback.getName());
		bodys.put("rollbackTo", deploymentRollback.getRollbackTo());
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
		return response;
	}
}
