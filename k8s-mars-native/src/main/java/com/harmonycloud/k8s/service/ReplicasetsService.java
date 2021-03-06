package com.harmonycloud.k8s.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class ReplicasetsService {

	public K8SClientResponse doRsByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.REPLICASET);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
		return response;
	}
	public K8SClientResponse doRsByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String name, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.REPLICASET).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
		return response;
	}
}
