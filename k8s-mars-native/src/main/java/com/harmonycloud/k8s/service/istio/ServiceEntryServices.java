package com.harmonycloud.k8s.service.istio;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.ServiceEntry;
import com.harmonycloud.k8s.client.K8SClient;
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

    public K8SClientResponse getServiceEntry(String namespace, Map<String, Object> bodys, Cluster cluster, String name) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICEENTRY).setNamespace(namespace).setName(name);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
    }

    public K8SClientResponse createServiceEntry(String namespace, ServiceEntry serviceEntry, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if (StringUtils.isNotBlank(namespace)) {
            url.setNamespace(namespace);
        }
        url.setResource(Resource.SERVICEENTRY);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> body = CollectionUtil.transBean2Map(serviceEntry);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, body, cluster);
        return response;
    }

    public K8SClientResponse getService(String namespace, Map<String, Object> bodys, Cluster cluster, String name) throws MarsRuntimeException {
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

    public K8SClientResponse getServiceEntry(String namespace, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if (StringUtils.isNotBlank(namespace)) {
            url.setNamespace(namespace);
        }
        url.setResource(Resource.SERVICEENTRY);
        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
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
    public K8SClientResponse updateServiceEntry(String namespace,String name,ServiceEntry serviceEntry,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICEENTRY).setNamespace(namespace).setSubpath(name);
        Map<String, Object> head = new HashMap<>();
        head.put("Content-Type", "application/json");
        Map<String, Object> body = CollectionUtil.transBean2Map(serviceEntry);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body,cluster);
        return response;
    }

    /**
     * 修改服务
     */
    public K8SClientResponse updateService(String namespace, String name, com.harmonycloud.k8s.bean.Service service, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.SERVICE).setNamespace(namespace).setSubpath(name);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        Map<String, Object> body = CollectionUtil.transBean2Map(service);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body, cluster);
        return response;
    }

    /**
     * 获取 服务入口 详情
     */
    public K8SClientResponse getServiceEntryByName(String namespace ,Map<String, Object> headers, Map<String, Object> bodys, String name,Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(name);
        url.setNamespace(namespace).setResource(Resource.SERVICEENTRY);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys,cluster);
        return response;
    }

    /**
     * 删除serviceEntry 创建的destinationRule
     */
    public K8SClientResponse deleteDestinationRule(String namespace, String serviceEntryName, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setName(serviceEntryName);
        url.setNamespace(namespace).setResource(Resource.DESTINATIONRULES);
        Map<String, Object> head = new HashMap<>();
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, head, null, cluster);
        return response;
    }
}
