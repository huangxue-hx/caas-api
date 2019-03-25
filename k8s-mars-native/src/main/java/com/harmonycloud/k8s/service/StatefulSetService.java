package com.harmonycloud.k8s.service;


import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.StatefulSet;
import com.harmonycloud.k8s.bean.StatefulSetList;
import com.harmonycloud.k8s.bean.UnversionedStatus;
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
 *
 * @author yekan
 */
@Service
public class StatefulSetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatefulSetService.class);
    /**
     * 创建StatefulSet
     * @param namespace
     * @param statefulSet
     * @param cluster
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createStatefulSet(String namespace, StatefulSet statefulSet, Cluster cluster) throws Exception{
        K8SURL url = new K8SURL();
        url.setResource(Resource.STATEFULSET).setNamespace(namespace);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
        url.setNamespace(namespace).setResource(Resource.STATEFULSET);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        StatefulSet resStatefulSet = JsonUtil.jsonToPojo(response.getBody(), StatefulSet.class);
        return ActionReturnUtil.returnSuccessWithData(resStatefulSet);
    }

    /**
     * 获取所有statefulset
     * @param namespace
     * @param headers
     * @param bodys
     * @param cluster
     * @return
     * @throws Exception
     */
    public StatefulSetList listStatefulSets(String namespace, Map<String, Object> headers, Map<String, Object> bodys, Cluster cluster) throws Exception{
        K8SURL url = new K8SURL();
        if (StringUtils.isNotBlank(namespace)){
            url.setNamespace(namespace).setResource(Resource.STATEFULSET);
        }else {
            url.setResource(Resource.STATEFULSET);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        StatefulSetList resStatefulSetList = JsonUtil.jsonToPojo(response.getBody(), StatefulSetList.class);
        return resStatefulSetList;
    }

    /**
     * 操作一个statefulset
     * @param namespace
     * @param name
     * @param headers
     * @param bodys
     * @param method
     * @param cluster
     * @return
     * @throws Exception
     */
    public K8SClientResponse doSpecifyStatefulSet(String namespace,String name, Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception{
        K8SURL url = new K8SURL();
        if(StringUtils.isBlank(name)){
            url.setNamespace(namespace).setResource(Resource.STATEFULSET);
        }else {
            url.setNamespace(namespace).setResource(Resource.STATEFULSET).setName(name);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, method, headers, bodys, cluster);
        return response;
    }

    /**
     * 获取特定的有状态资源
     * @param namespace
     * @param name
     * @param cluster
     * @return
     * @throws Exception
     */
    public StatefulSet getStatefulSet(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.STATEFULSET).setNamespace(namespace).setName(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            LOGGER.debug("statefulSet获取失败：{}", status.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        StatefulSet resStatefulSet = JsonUtil.jsonToPojo(response.getBody(), StatefulSet.class);
        return resStatefulSet;
    }

    public ActionReturnUtil updateStatefulSet(String namespace, String name, StatefulSet statefulSet, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.STATEFULSET).setNamespace(namespace).setName(name);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            LOGGER.debug("statefulSet更新失败：{}", status.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_UPDATE_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();
    }

}
