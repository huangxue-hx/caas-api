package com.harmonycloud.k8s.service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by czm on 2017/4/19.
 */
@Service
public class ConfigmapService {

    public K8SClientResponse doSepcifyConfigmap(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP);

        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
        return response;
    }

    public K8SClientResponse doSepcifyConfigmap(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

    public K8SClientResponse delete(String namespace, String name, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null, cluster);
        return response;
    }

    public K8SClientResponse updateConfigmap(ConfigMap configMap, Cluster cluster) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(configMap);
        K8SURL url = new K8SURL();
        url.setNamespace(CommonConstant.KUBE_SYSTEM).setResource(Resource.CONFIGMAP).setName(configMap.getMetadata().getName());
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }
}
