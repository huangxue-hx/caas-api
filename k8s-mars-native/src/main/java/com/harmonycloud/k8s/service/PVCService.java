package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.constant.HTTPMethod;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class PVCService {
	
	public K8SClientResponse doSepcifyPVC(String namespace , Map<String, Object> query, String method, Cluster cluster) throws Exception {
        return this.doSepcifyPVC(namespace,null,query,method,cluster);
    }
	public K8SClientResponse doSepcifyPVC(String namespace , Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if(StringUtils.isBlank(namespace)){
            url.setResource(Resource.PERSISTENTVOLUMECLAIM);
        }else {
            url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, method,headers, bodys,cluster);
        return response;
    }

    public K8SClientResponse deletePVC(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(name).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("gracePeriodSeconds", 1);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        return response;
    }
}
