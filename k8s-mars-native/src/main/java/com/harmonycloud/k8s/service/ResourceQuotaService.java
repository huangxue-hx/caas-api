package com.harmonycloud.k8s.service;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.ResourceQuota;
import com.harmonycloud.k8s.bean.ResourceQuotaList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Created by andy on 17-1-20.
 */
@Service
public class ResourceQuotaService {

    public K8SClientResponse create(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.RESOURCEQUOTA);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys,cluster);
        return response;
    }

    public K8SClientResponse update(String namespace, String name, Map<String, Object> headers, Map<String, Object> bodys, String method) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.RESOURCEQUOTA).setName(name);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, method, headers, bodys);
        return response;
    }

    public K8SClientResponse getByNamespace(String namespace, Map<String, Object> headers, Map<String, Object> bodys,Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setNamespace(namespace).setResource(Resource.RESOURCEQUOTA);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, headers, bodys,cluster);
        return response;
    }

    public static void main(String[] args) {

        ResourceQuotaService resourceQuotaService = new ResourceQuotaService();
        K8SClientResponse k8SClientResponse = resourceQuotaService.getByNamespace("zheng-fainall", null, null,null);

        ResourceQuotaList resourceQuota = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), ResourceQuotaList.class);

        System.out.println(k8SClientResponse.getBody());
    }
}
