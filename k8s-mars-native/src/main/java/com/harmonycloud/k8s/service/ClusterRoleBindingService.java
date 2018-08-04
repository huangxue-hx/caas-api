package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpK8SClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.ClusterRoleBinding;
import com.harmonycloud.k8s.bean.ClusterRoleBindingList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

@Service
public class ClusterRoleBindingService {

	private String surfix="/clusterrolebindings";
	
	/**
	 * 获取所有ClusterRoleBinding
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public ClusterRoleBindingList getAllClusterRolebindings(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys){
		try {
			String url=k8sUrl+Constant.RBAC_VERSION+surfix;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			ClusterRoleBindingList cList=JsonUtil.jsonToPojo(body.toString(),ClusterRoleBindingList.class);
			
			return cList;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取特定的ClusterRoleBinding
	 * @param url
	 * @param headers
	 * @param bodys
	 * @return
	 */
	public ClusterRoleBinding getSpecifiedClusterRolebindings(String k8sUrl, Map<String,Object> headers, Map<String,Object> bodys,String name){
		try {
			String url=k8sUrl+Constant.RBAC_VERSION+surfix+"/"+name;
			String body = HttpK8SClientUtil.httpGetRequest(url,headers,bodys);
			ClusterRoleBinding c=JsonUtil.jsonToPojo(body.toString(),ClusterRoleBinding.class);
			
			return c;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public K8SClientResponse createClusterRoleBinding(ClusterRoleBinding clusterRoleBinding, Cluster cluster) throws Exception {
		Map<String, Object> bodys = CollectionUtil.transBean2Map(clusterRoleBinding);
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-type", "application/json");

		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.CLUSTERROLEBINDING);

		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.POST, headers, bodys, cluster);
		return response;

	}

	public K8SClientResponse replaceClusterRoleBinding(ClusterRoleBinding clusterRoleBinding, Cluster cluster) throws Exception {
		Map<String, Object> bodys = CollectionUtil.transBean2Map(clusterRoleBinding);
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-type", "application/json");

		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.CLUSTERROLEBINDING).setName(clusterRoleBinding.getMetadata().getName());
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.PUT, headers, bodys, cluster);
		return response;
	}

	public K8SClientResponse deleteClusterRoleBinding(String name, Cluster cluster){
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.CLUSTERROLEBINDING).setName(name);
		return new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, cluster);
	}

	public boolean existClusterRoleBinding(String name, Cluster cluster){
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.CLUSTERROLEBINDING).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
		return HttpStatusUtil.isSuccessStatus(response.getStatus());
	}

	public ClusterRoleBinding getClusterBindingByName(String name, Cluster cluster){
		K8SURL k8SURL = new K8SURL();
		k8SURL.setResource(Resource.CLUSTERROLEBINDING).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			return JsonUtil.jsonToPojo(response.getBody(), ClusterRoleBinding.class);
		}else {
			return null;
		}

	}


	public static void main(String [] args){
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("authorization", "Bearer 330957b867a3462ea457bec41410624b");
	}
}
