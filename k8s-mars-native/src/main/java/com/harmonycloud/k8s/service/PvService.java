package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.CollectionUtil;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.bean.PersistentVolumeClaim;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class PvService {
	/**
	 * PersistentVolume列表
	 * @return PersistentVolumeList
	 */
	public K8SClientResponse listPv(Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUME);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
		return response;
	}
	
	/**
	 * 新增pv,新增成功返回新增pv,新增失败返回null
	 * @param pv
	 * @return
	 */
	public ActionReturnUtil addPv(PersistentVolume pv,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUME);
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", pv.getMetadata());
		bodys.put("kind", pv.getKind());
		bodys.put("spec", pv.getSpec());
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST,headers,bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
			return ActionReturnUtil.returnErrorWithMsg(us.getMessage() );
		}
		return ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * 根据PersistentVolume名称查询PersistentVolume
	 * @param name
	 * @return PersistentVolume
	 */
	public PersistentVolume getPvByName(String name,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUME).setSubpath(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			return K8SClient.converToBean(response, PersistentVolume.class);
		}
		return null;
	}

	/**
	 * 根据PersistentVolume名称删除PersistentVolume
	 * @param name
	 * @return PersistentVolume
	 */
	public ActionReturnUtil delPvByName(String name,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUME).setSubpath(name);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE,null,null,cluster);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			return ActionReturnUtil.returnSuccess();
		}
		return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DELETE_FAIL, response.getBody(), false);
	}
	
	/**
	 * 获取该namespace下该name的pvc
	 * @param name
	 * @param namespace
	 * @return PersistentVolumeClaim
	 */
	public PersistentVolumeClaim getPvc(String name,String namespace,Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUMECLAIM).setSubpath(name).setNamespace(namespace);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
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
	
	/**
	 * pvc 列表
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public K8SClientResponse listPvc(String namespace,Cluster cluster) throws Exception{
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUMECLAIM).setNamespace(namespace);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
		return response;
	}
	
	
	/**
	 * 根据label查询pv列表 
	 * @param label eg nephele_tenantid_c4b6ae6cd95f412c881814666978e3af=c4b6ae6cd95f412c881814666978e3af
	 * @return
	 * @throws Exception
	 */
	public K8SClientResponse listPvBylabel(String label,Cluster cluster) throws Exception{
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("labelSelector", label);
		K8SURL url = new K8SURL();
		url.setResource(Resource.PERSISTENTVOLUME);
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,bodys,cluster);
		return response;
	}
	public K8SClientResponse updatePvByName(PersistentVolume pv,Cluster cluster) throws Exception{
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.METADATA, pv.getMetadata());
        bodys.put(CommonConstant.KIND, pv.getKind());
        bodys.put(CommonConstant.SPEC, pv.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        K8SURL url = new K8SURL();
        url.setResource(Resource.PERSISTENTVOLUME).setName(pv.getMetadata().getName());
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT,headers,bodys,cluster);
        return response;
    }
}
