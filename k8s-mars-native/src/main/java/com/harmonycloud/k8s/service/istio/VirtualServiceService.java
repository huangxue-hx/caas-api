package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.VirtualService;
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
 * Create by weg on 18-12-3.
 */
@Service
public class VirtualServiceService {

    public K8SClientResponse getVirtualService(String namespace, String virtualServiceName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.VIRTUALSERVICE);
        url.setName(virtualServiceName);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
    }

    //修改服务熔断
    public K8SClientResponse updateVirtualService(String namespace, String name, VirtualService virtualService, Cluster cluster) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.VIRTUALSERVICE);
        url.setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(virtualService);
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse createVirtualService(String namespace, VirtualService virtualService, Cluster cluster) throws MarsRuntimeException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.VIRTUALSERVICE);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(virtualService);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse deleteVirtualService(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.VIRTUALSERVICE);
        url.setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        return new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
    }

}
