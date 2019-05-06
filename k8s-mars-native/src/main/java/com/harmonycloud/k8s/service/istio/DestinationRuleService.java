package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.DestinationRule;
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

@Service
public class DestinationRuleService {

    public K8SClientResponse createDestinationRule(String namespace, DestinationRule destinationRule, Cluster cluster) throws MarsRuntimeException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        K8SURL url = new K8SURL();
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(destinationRule);
        url.setNamespace(namespace).setResource(Resource.DESTINATIONRULES);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse getDestinationRule(String namespace, String destinationRuleName, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        url.setName(destinationRuleName);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        return new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
    }

    //修改服务熔断
    public K8SClientResponse updateDestinationRule(String namespace, String name, DestinationRule destinationRule, Cluster cluster) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.DESTINATIONRULES).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(destinationRule);
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse deleteDestinationRule(String namespace, String name, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.DESTINATIONRULES);
        url.setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        return new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
    }
}
