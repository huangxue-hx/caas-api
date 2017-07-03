package com.harmonycloud.k8s.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class EventService {
	
	public K8SClientResponse doEventByNamespace(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.EVENT);
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
		return response;
	}
	

}
