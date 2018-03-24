package com.harmonycloud.k8s.service;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServicesService {
	public K8SClientResponse doServiceByNamespace(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if (StringUtils.isBlank(namespace)) {
        	url.setResource(Resource.SERVICE);
		}else {
			url.setNamespace(namespace).setResource(Resource.SERVICE);
		}

        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
        return response;
    }
	public K8SClientResponse doSepcifyService(String namespace ,String name, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.SERVICE).setName(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
		return response;
	}
	public K8SClientResponse doSepcifyServiceByNamespace(String namespace , Map<String, Object> headers, Map<String, Object> bodys,Map<String, Object> query, String method) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.SERVICE).setQueryParams(query);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,null);
		return response;
	}

	public K8SClientResponse doServiceByName(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method, String name) throws Exception {
		K8SURL url = new K8SURL();
		url.setName(name);
		url.setNamespace(namespace).setResource(Resource.SERVICE);
		K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,null);
		return response;
	}
}
