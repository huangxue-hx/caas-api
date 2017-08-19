package com.harmonycloud.k8s.service;

import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by czm on 2017/4/18.
 */
@Service
public class ServiceAccountService {
    public K8SClientResponse doSepcifyService(String namespace ,Map<String, Object> qp, String method) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.SERVICEACCOUNT).setQueryParams(qp);
        K8SClientResponse response = new K8sMachineClient().exec(url, method, null,null,null);

        return response;
    }

}
