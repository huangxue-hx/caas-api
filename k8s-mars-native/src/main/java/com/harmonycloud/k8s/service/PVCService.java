package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.PersistentVolumeClaim;
import com.harmonycloud.k8s.bean.StorageClass;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class PVCService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PVCService.class);
	public K8SClientResponse doSepcifyPVC(String namespace , Map<String, Object> query, String method, Cluster cluster) throws Exception {
        return this.doSepcifyPVC(namespace,null,query,method,cluster);
    }
	public K8SClientResponse doSepcifyPVC(String namespace , Map<String, Object> headers, Map<String, Object> bodys, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        if(StringUtils.isBlank(namespace)){
            url.setResource(Resource.PERSISTENTVOLUMECLAIM);
        }else {
            url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        }
        K8SClientResponse response = new K8sMachineClient().exec(url, method,headers, bodys,cluster);
        return response;
    }

    public K8SClientResponse deletePVC(String namespace, String name, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(name).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("gracePeriodSeconds", 1);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        return response;
    }

    public K8SClientResponse getPVC(String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setName(name).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("gracePeriodSeconds", 1);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        return response;
    }

    public PersistentVolumeClaim getPVCByNameAndNamespace(String name, String namespace, Cluster cluster) {
	    K8SURL url = new K8SURL();
	    url.setApiGroup(APIGroup.API_V1_VERSION);
	    url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response, PersistentVolumeClaim.class);
        }
        return null;
    }

    public K8SClientResponse createPvc(String namespace, PersistentVolumeClaim pVolumeClaim, Cluster cluster) throws Exception{
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(pVolumeClaim);
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        return response;
    }

    public PersistentVolumeClaim getPvcByName(String namespace, String pvcName, Cluster cluster) {
	    if(StringUtils.isBlank(pvcName)){
            LOGGER.warn("pvcName为空");
	        return null;
        }
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(namespace);
        url.setResource(Resource.PERSISTENTVOLUMECLAIM);
        url.setSubpath(pvcName);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response, PersistentVolumeClaim.class);
        }
        return null;
    }

    public K8SClientResponse updatePvcByName(PersistentVolumeClaim pvc, Cluster cluster) {
	    K8SURL url = new K8SURL();
	    url.setNamespace(pvc.getMetadata().getNamespace()).setApiGroup(APIGroup.API_V1_VERSION);
        url.setResource(Resource.PERSISTENTVOLUMECLAIM).setName(pvc.getMetadata().getName());
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.METADATA, pvc.getMetadata());
        bodys.put(CommonConstant.KIND, pvc.getKind());
        bodys.put(CommonConstant.SPEC, pvc.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        return new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
    }
}
