package com.harmonycloud.k8s.service;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.DaemonSet;
import com.harmonycloud.k8s.bean.DaemonSetList;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description Daemonset与k8s交互
  created in 2017-12-18
 * @Modified
 */
@Service
public class DaemonSetService {

    /**
     * 创建DaemonSet
     * @param daemonSet required
     * @param cluster required
     * @param namespace required
     * @return ActionReturnUtil
     */
    public void addDaemonSet(String namespace, DaemonSet daemonSet, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.DAEMONTSET).setNamespace(namespace);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", daemonSet.getMetadata());
        bodys.put("kind", daemonSet.getKind());
        bodys.put("spec", daemonSet.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            throw new MarsRuntimeException(us.getMessage());
        }
    }

    /**
     * 根据名称删除DaemonSet
     * @param cluster required
     * @param namespace required
     * @param name required
     * @return ActionReturnUtil
     */
    public void delDaemonSetByName(String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DAEMONTSET).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()){
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody(),UnversionedStatus.class);
            throw new MarsRuntimeException(us.getMessage());
        }
    }

    /**
     * 删除符合查询条件的DaemonSet
     * @param cluster required
     * @param namespace required
     * @param queryParams required
     * @return ActionReturnUtil
     */
    public void delDaemonSet(String namespace, Map<String, Object> queryParams, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DAEMONTSET);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,queryParams,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus()) && Constant.HTTP_404 != response.getStatus()){
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            throw new MarsRuntimeException(us.getMessage());
        }
    }

    /**
     * read the specified DaemonSet
     * @param cluster required
     * @param namespace required
     * @param name required
     * @return K8SClientResponse
     */
    public DaemonSet getDaemonSet(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DAEMONTSET).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
        if(Constant.HTTP_404 == response.getStatus()) {
            throw new MarsRuntimeException(ErrorCodeMessage.DAEMONSET_NOT_EXIST);
        }
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            throw new MarsRuntimeException(us.getMessage());
        }
        DaemonSet ds = JsonUtil.jsonToPojo(response.getBody(),DaemonSet.class);
        return ds;
    }

    /**
     * list or watch objects of kind DaemonSet
     * @param cluster required
     * @param namespace required
     * @param queryParams required
     * @return K8SClientResponse
     */
    public DaemonSetList listDaemonSet(String namespace, Map<String, Object> queryParams, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.DAEMONTSET).setNamespace(namespace);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,queryParams,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404){
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(sta.getMessage());
        }
        DaemonSetList list = JsonUtil.jsonToPojo(response.getBody(),DaemonSetList.class);
        return list;
    }
    public DaemonSetList listDaemonSet( Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setResource(Resource.DAEMONTSET);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404){
            UnversionedStatus sta = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(sta.getMessage());
        }
        DaemonSetList list = JsonUtil.jsonToPojo(response.getBody(),DaemonSetList.class);
        return list;
    }
    /**
     * 更新指定的DaemonSet
     * @param cluster required
     * @param namespace required
     * @param name required
     * @param daemonSet required
     * @return ActionReturnUtil
     */
    public void updateDaemonSet(String namespace, String name, DaemonSet daemonSet, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.DAEMONTSET).setName(name);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", daemonSet.getMetadata());
        bodys.put("kind", daemonSet.getKind());
        bodys.put("spec", daemonSet.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT,headers,bodys,cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())){
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            throw new MarsRuntimeException(us.getMessage());
        }
    }
}
