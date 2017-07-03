package com.harmonycloud.k8s.service;

import com.harmonycloud.k8s.client.K8SClient;
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
        K8SClientResponse response = new K8SClient().doit(url, method,null, null,null);
        return response;
    }
}
