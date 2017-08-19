package com.harmonycloud.k8s.service;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by czm on 2017/4/19.
 */
@Service
public class ConfigmapService {

    public K8SClientResponse doSepcifyConfigmap(String namespace , Map<String, Object> query, String method) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP).setQueryParams(query);
        K8SClientResponse response = new K8sMachineClient().exec(url, method,null, null,null);
        return response;
    }
    public K8SClientResponse doSepcifyConfigmap(String namespace , Map<String, Object> query, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, method,null, query,cluster);
        return response;
    }
    public K8SClientResponse doSepcifyConfigmap(String namespace , Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
        return response;
    }
}
