package com.harmonycloud.k8s.service.istio;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.istio.policies.Rule;
import com.harmonycloud.k8s.bean.istio.policies.ratelimit.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jmi on 18-9-11.
 */
@Service
public class RateLimitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitService.class);

    public K8SClientResponse createRedisQuota(String namespace, RedisQuota redisQuota, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.REDISQUOTA);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(redisQuota);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse createQuota(String namespace, QuotaInstance quotaInstance, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.QUOTA);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(quotaInstance);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse createQuotaSpec(String namespace, QuotaSpec quotaSpec, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.QUOTASPEC);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(quotaSpec);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse createQuotaSpecBinding(String namespace, QuotaSpecBinding quotaSpecBinding, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.QUOTASPECBINDING);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(quotaSpecBinding);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    public K8SClientResponse createRule(String namespace, Rule rule, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.RULE);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(rule);
        return new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
    }

    /**
     * 按照资源创建顺序来逐条删除
     * @param namespace
     * @param name
     * @param cluster
     * @param num 当前资源创建排列顺序
     * @return
     * @throws MarsRuntimeException
     */
    public Map<String, Object> deleteRateLimitPolicy(String namespace, String name, Cluster cluster, int num) throws MarsRuntimeException {
        Map<String, Object> resMap = new HashMap<>();
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        List<String> resources = Arrays.asList(Resource.RULE, Resource.QUOTASPECBINDING, Resource.QUOTASPEC, Resource.REDISQUOTA, Resource.QUOTA);

        for (int i = 5 - num; i < 5; i++) {
            url.setResource(resources.get(i));
            url.setName(i == 0 ? CommonConstant.RATE_LIMIT_PREFIX + name : name);
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()) {
                LOGGER.error("delete " + resources.get(i) + " error", response.getBody());
                resMap.put("faileNum", 5 - i);
                resMap.put("faileResource", resources.get(i));
                return resMap;
            }
        }
        return resMap;
    }

    public K8SClientResponse updateRedisQuota(String namespace, String name, RedisQuota redisQuota, Cluster cluster) throws MarsRuntimeException, IntrospectionException, InvocationTargetException, IllegalAccessException {
        K8SURL url = new K8SURL();
        url.setResource(Resource.REDISQUOTA).setNamespace(namespace).setName(name);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(redisQuota);
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }

    public K8SClientResponse getRateLimitResource(String namespace, String name, String resourceType, Cluster cluster) throws MarsRuntimeException {
        K8SURL url = new K8SURL();
        if (CommonConstant.RATE_LIMIT_QUOTA.equals(resourceType)) {
            url.setResource(Resource.QUOTA).setNamespace(namespace).setName(name);
        } else if (CommonConstant.RATE_LIMIT_REDIS_QUOTA.equals(resourceType)) {
            url.setResource(Resource.REDISQUOTA).setNamespace(namespace).setName(name);
        } else if (CommonConstant.RATE_LIMIT_QUOTA_SPEC.equals(resourceType)) {
            url.setResource(Resource.QUOTASPEC).setNamespace(namespace).setName(name);
        } else if (CommonConstant.RATE_LIMIT_QUOTA_SPEC_BINDING.equals(resourceType)) {
            url.setResource(Resource.QUOTASPECBINDING).setNamespace(namespace).setName(name);
        } else if (CommonConstant.ISTIO_RULE.equals(resourceType)) {
            url.setResource(Resource.RULE).setNamespace(namespace).setName(CommonConstant.RATE_LIMIT_PREFIX + name);
        } else {
            throw new MarsRuntimeException("不支持RateLimit类型的查询，类型：" + resourceType);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        return response;
    }

}
