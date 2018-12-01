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

    //创建服务熔断
    public K8SClientResponse createCircuitBreakerPolicy(String namespace, DestinationRule circuitBreaker, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.APIVERSION, CommonConstant.NETWORKING_ISTIO_V1ALPHA3);
        bodys.put(CommonConstant.KIND, CommonConstant.DESTINATION_RULE);
        bodys.put(CommonConstant.METADATA, circuitBreaker.getMetadata());
        bodys.put(CommonConstant.SPEC, circuitBreaker.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    //获取服务熔断列表
    public List<DestinationRule> listCircuitBreakerPolicy(String namespace, Map<String, Object> bodys, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("get circuit breaker policy error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_LIST_FAILED);
        }
        DestinationRuleList destinationRuleList = JsonUtil.jsonToPojo(response.getBody(), DestinationRuleList.class);
        return destinationRuleList.getItems();
    }

    //获取服务熔断策略
    public K8SClientResponse getCircuitBreakerPolicy(String namespace, String destinationRuleName, Cluster cluster) throws MarsRuntimeException {
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

    //删除服务熔断
    public void deleteCircuitBreakerPolicy(String namespace, String policyName, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        url.setName(policyName);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("delete circuit breaker policy error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }

    //通过label删除服务熔断策略
    public void deleteCircuitBreakerPolicyByLabel(String namespace, Map<String, Object> bodys, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace);
        url.setResource(Resource.DESTINATIONRULES);
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
            LOGGER.error("delete circuit breaker policy error", response.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.POLICY_DELETE_FAILED);
        }
    }

}
