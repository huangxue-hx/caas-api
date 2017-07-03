package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.bean.ClusterRole;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class ClusterRoleService {
	
	/**
	 * 获取所有CluterRole
	 * @return ClusterRoleList
	 */
	public K8SClientResponse listClusterRoles(){
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null);
		return response;
	}
	
	/**
	 * 获取特定的CluterRole
	 * @param name eg:dev
	 * @return ClusterRole
	 */
	public K8SClientResponse getSpecifiedClusterRoles(String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE).setSubpath(name);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET,null,null,null);
		return response;
	}
	
	
	/**
	 * 创建ClusterRole
	 * @param clusterRole
	 * @return
	 */
	public K8SClientResponse createClusterRole(ClusterRole clusterRole) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE);
		Map<String, Object> bodys = new HashMap<>();
		Map<String, Object> header = new HashMap<>();
		header.put("Content-Type", "application/json");
		bodys.put("metadata", clusterRole.getMetadata());
		bodys.put("rules", clusterRole.getRules());
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST, header, bodys,null);
		return response;
	}
	
	/**
	 * 删除clusterRoleName
	 * @param clusterRoleName eg.pm
	 * @return
	 */
	public K8SClientResponse deleteClusterRole(String clusterRoleName) throws Exception{
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE).setSubpath(clusterRoleName);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE, null, null,null);
		return response;
	}
	
	/**
	 * 根据标签筛选ClusterRoleList
	 * @param lable
	 * @return ClusterRoleList
	 */
	public K8SClientResponse getClusterRoleListbyLabelSelector(String lable) throws Exception {
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", lable);
		K8SURL url = new K8SURL();
		url.setResource(Resource.CLUSTERROLE);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET, null, bodys,null);
		return response;
	}
	

}
