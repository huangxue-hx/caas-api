package com.harmonycloud.k8s.service;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Update by xc on 2018/8/7.
 */
@Service
public class ServiceAccountService {

    public K8SClientResponse getServiceAccountByName(String namespace, String saName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(namespace);
        url.setResource(Resource.SERVICEACCOUNT);
        url.setName(saName);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
    }

}
