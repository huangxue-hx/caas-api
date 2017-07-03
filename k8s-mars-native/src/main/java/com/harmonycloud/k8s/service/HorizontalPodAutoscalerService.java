package com.harmonycloud.k8s.service;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HorizontalPodAutoscalerService {
	
	public K8SClientResponse doSpecifyHpautoscaler(String namespace,String name, Map<String, Object> headers, Map<String, Object> bodys, String method , Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.HORIZONTALPODAUTOSCALER).setName(name + "-hpa");
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
		return response;
	}
	
	
	public K8SClientResponse doHpautoscalerByNamespace(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.HORIZONTALPODAUTOSCALER);
		if (bodys != null && ((ObjectMeta) bodys.get("metadata")) != null){
			url.setName(((ObjectMeta) bodys.get("metadata")).getName());
		}
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
		return response;
	}

	public K8SClientResponse postHpautoscalerByNamespace(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.HORIZONTALPODAUTOSCALER);
//		if (bodys != null && ((ObejectMeta) bodys.get("metadata")) != null){
//			url.setName(((ObejectMeta) bodys.get("metadata")).getName());
//		}
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys,cluster);
		return response;
	}

}
