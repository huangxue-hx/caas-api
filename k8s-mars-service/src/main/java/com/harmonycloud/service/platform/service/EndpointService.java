package com.harmonycloud.service.platform.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class EndpointService {
        public K8SClientResponse doEndpointByNamespace(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method) throws Exception {
                K8SURL url = new K8SURL();
                url.setNamespace(namespace).setResource(Resource.ENDPOINT);
                K8SClientResponse response = new K8SClient().doit(url, method, headers, bodys);
                return response;
        }
}

