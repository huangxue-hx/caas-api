package com.harmonycloud.k8s.service.istio;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * create by  ljf  18/12/6
 */

@Service
public class ServiceEntryServices {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhiteListsService.class);

    public K8SClientResponse getServiceEntry(String namespace, Map<String, Object> bodys, Cluster cluster,String  name) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICEENTRY).setNamespace(namespace).setName(name);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
    }

    public K8SClientResponse createServiceEntry(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if (StringUtils.isBlank(namespace)) {
            url.setResource(Resource.SERVICEENTRY);
        }else {
            url.setNamespace(namespace).setResource(Resource.SERVICEENTRY);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
        return response;
    }

    public K8SClientResponse getService(String namespace, Map<String, Object> bodys, Cluster cluster,String  name) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(namespace).setName(name);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
    }

    public K8SClientResponse deleteExtService( String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null,cluster);
        return response;
    }

    public K8SClientResponse getServiceEntry(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if (StringUtils.isBlank(namespace)) {
            url.setResource(Resource.SERVICEENTRY);
        }else {
            url.setNamespace(namespace).setResource(Resource.SERVICEENTRY);
        }

        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
        return response;
    }


    public K8SClientResponse deleteServiceEntry( String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICEENTRY).setNamespace(namespace).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, null,cluster);
        return response;
    }
    /**
     * 修改外部服务入口
     */
    public K8SClientResponse updateServiceEntry(String namespace,String name,Map<String ,Object> bodys,Cluster cluster){
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICEENTRY).setNamespace(namespace).setSubpath(name);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, bodys,cluster);
        return response;
    }
    /**
     * 修改服务
     */
    public K8SClientResponse updateService(String namespace,String name,Map<String ,Object> bodys,Cluster cluster){
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, bodys,cluster);
        return response;
    }
    /**
     * 获取 服务入口 详情
     */
    public K8SClientResponse getServiceEntryByName(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String method, String name,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(name);
        url.setNamespace(namespace).setResource(Resource.SERVICEENTRY);
        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys,cluster);
        return response;
    }
}
