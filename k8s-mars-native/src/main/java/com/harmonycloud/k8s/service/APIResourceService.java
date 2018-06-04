package com.harmonycloud.k8s.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.APIResourceList;
import com.harmonycloud.k8s.bean.Resource;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class APIResourceService {
	/**
	 * 获取所有api资源列表
	 * @return APIResourceList
	 */
	public List<Resource> listAPIResource() throws Exception {
		K8SURL url = new K8SURL();
		url.setName("apis/extensions/v1beta1");
		//APIResourceList
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,null);
		APIResourceList apiResourceList = K8SClient.converToBean(response, APIResourceList.class);
		List<Resource> resources = apiResourceList.getResources();
		//APIResourceList
		K8SURL url2 = new K8SURL();
		url2.setName("api/v1");
		K8SClientResponse response2 = new K8sMachineClient().exec(url2, HTTPMethod.GET,null,null,null);
		APIResourceList apiResourceList2 = K8SClient.converToBean(response2, APIResourceList.class);
		List<Resource> resources2 = apiResourceList2.getResources();
		//获取两个APIResourceList并集
		resources.addAll(resources2);
		//去重
		//List<Resource> newList = new ArrayList<>(new HashSet<>(resources2));
		List<Resource> newList = new ArrayList<>(resources2);
		for (Resource resource : resources) {
			if(newList.contains(resource) == false){
				newList.add(resource);
			}
		}
		return newList;
	}
	
	/**
	 * 获取所有api资源列表名称
	 * @return
	 */
	public List<String> listAPIResourceNames() throws Exception {
		List<String> aPIResourceNames = new ArrayList<>();
		K8SURL url = new K8SURL();
		url.setName("apis/extensions/v1beta1");
		List<Resource> resources = new ArrayList<>();
		List<Resource> resources2 = new ArrayList<>();
		//APIResourceList
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,null);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			APIResourceList apiResourceList = K8SClient.converToBean(response, APIResourceList.class);
			resources = apiResourceList.getResources();
		}
		//APIResourceList
		K8SURL url2 = new K8SURL();
		url2.setName("api/v1");
		K8SClientResponse response2 = new K8sMachineClient().exec(url2, HTTPMethod.GET,null,null,null);
		if(HttpStatusUtil.isSuccessStatus(response2.getStatus())){
			APIResourceList apiResourceList2 = K8SClient.converToBean(response2, APIResourceList.class);
			resources2 = apiResourceList2.getResources();
		}
		//获取两个APIResourceList并集
		resources.addAll(resources2);
		//去重
		//List<Resource> newList = new ArrayList<>(new HashSet<>(resources2));
		List<Resource> newList = new ArrayList<>(resources2);
		for (Resource resource : resources) {
			if(newList.contains(resource) == false){
				newList.add(resource);
			}
		}
		for (Resource resource : newList) {
			aPIResourceNames.add(resource.getName());
		}
		return aPIResourceNames;
	}
	
}
