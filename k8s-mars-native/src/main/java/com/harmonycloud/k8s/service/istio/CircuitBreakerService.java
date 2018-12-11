package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.DestinationRule;
import com.harmonycloud.k8s.bean.istio.trafficmanagement.DestinationRuleList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CircuitBreakerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerService.class);

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
    public K8SClientResponse updateCircuitBreakerPolicy(String namespace, String policyName, DestinationRule circuitBreaker, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        url.setName(policyName);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.APIVERSION, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        bodys.put(CommonConstant.KIND, CommonConstant.DESTINATION_RULE);
        bodys.put(CommonConstant.METADATA, circuitBreaker.getMetadata());
        bodys.put(CommonConstant.SPEC, circuitBreaker.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }
}
