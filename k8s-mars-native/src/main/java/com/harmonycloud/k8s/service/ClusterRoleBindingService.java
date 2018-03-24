package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.util.HttpK8SClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.ClusterRoleBinding;
import com.harmonycloud.k8s.bean.ClusterRoleBindingList;
import com.harmonycloud.k8s.constant.Constant;

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
	
	
	public static void main(String [] args){
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("authorization", "Bearer 330957b867a3462ea457bec41410624b");
	}
}
