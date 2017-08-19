package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by andy on 17-2-16
 */
@Service
public class NetworkPolicyService {

    public K8SClientResponse create(Map<String, Object> headers, Map<String, Object> bodys, String namespace,Cluster cluster) throws Exception {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setResource(Resource.NETWORKPOLICY).setNamespace(namespace);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.POST, headers, bodys,cluster);
        return response;
    }
    public K8SClientResponse delete(Map<String, Object> headers, Map<String, Object> bodys, String namespace,String name,Cluster cluster) throws Exception {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setResource(Resource.NETWORKPOLICY).setNamespace(namespace).setName(name);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, headers, null,cluster);
        return response;
    }
}
