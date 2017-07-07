package com.harmonycloud.k8s.service;

import java.util.ArrayList;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class ReplicasetsService {
	
	public K8SClientResponse doRsByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.REPLICASET);
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
		return response;
	}
	public K8SClientResponse doRsByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String name, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.REPLICASET).setName(name);
		K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys, cluster);
		return response;
	}
	private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
		}
		return pairs;
	}
}
