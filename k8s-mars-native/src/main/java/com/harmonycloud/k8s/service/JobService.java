package com.harmonycloud.k8s.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.Job;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;

@Service
public class JobService {
	
	/**
	 * 创建Job
	 * @param job 
	 * @param cluster
	 * @return Job
	 */
	public ActionReturnUtil addJob(Job job, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.JOB);
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", job.getMetadata());
		bodys.put("kind", job.getKind());
		bodys.put("spec", job.getSpec());
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.POST,headers,bodys,cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
        }
		return ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * 删除特定Job
	 * @param cluster
	 * @param namespace
	 * @param name
	 * @return
	 */
	public ActionReturnUtil delJobByName(String name, String namespace, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.JOB).setSubpath(name);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE,null,null,cluster);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			return ActionReturnUtil.returnSuccess();
		}
		UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
        return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
	}
	
	/**
	 * 删除符合查询条件的Job
	 * @param cluster
	 * @param namespace
	 * @param queryParams
	 * @return
	 */
	public ActionReturnUtil delJobs(String namespace, Map<String, Object> queryParams, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.JOB);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.DELETE,null,queryParams,cluster);
		if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
			return ActionReturnUtil.returnSuccess();
		}
		UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
        return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
	}
	
	/**
	 * 更新指定的Job
	 * @param cluster
	 * @param namespace
	 * @param queryParams
	 * @return
	 */
	public ActionReturnUtil updateJob(String namespace, String name, Job job, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.JOB).setName(name);
		Map<String, Object> bodys = new HashMap<>();
		bodys.put("metadata", job.getMetadata());
		bodys.put("kind", job.getKind());
		bodys.put("spec", job.getSpec());
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/json");
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.PUT,headers,bodys,cluster);
		if(!HttpStatusUtil.isSuccessStatus(response.getStatus())){
			UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody().toString(),UnversionedStatus.class);
	        return ActionReturnUtil.returnErrorWithMsg(us.getMessage());
		}
		return  ActionReturnUtil.returnSuccess();
	}
	
	/**
	 * read the specified Job
	 * @param cluster
	 * @param namespace
	 * @param name
	 * @param queryParams
	 * @return
	 */
	public K8SClientResponse getJob(String namespace, String name, Map<String, Object> queryParams, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.JOB).setSubpath(name);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET,null,queryParams,cluster);
		return response;
	}
	
	/**
	 * list or watch objects of kind Job
	 * @param cluster
	 * @param namespace
	 * @param queryParams
	 * @return
	 */
	public K8SClientResponse listJob(String namespace, Map<String, Object> queryParams, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.JOB);
		if(StringUtils.isEmpty(namespace)){
			url.setNamespace(namespace);
		}
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET,null,queryParams,cluster);
		return response;
	}
	
	/**
	 * read the specified Job
	 * @param cluster
	 * @param namespace
	 * @param name
	 * @param queryParams
	 * @return
	 */
	public K8SClientResponse watchJob(String namespace, String name, Map<String, Object> queryParams, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setNamespace(namespace).setResource(Resource.JOB).setSubpath(name).setWatch(APIGroup.WATCH);
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET,null,queryParams,cluster);
		return response;
	}
	
	
	/**
	 * watch individual changes to a list of Job
	 * @param cluster
	 * @param namespace
	 * @param name
	 * @param queryParams
	 * @return
	 */
	public K8SClientResponse watchJob(String namespace, Map<String, Object> queryParams, Cluster cluster) throws Exception {
		K8SURL url = new K8SURL();
		url.setResource(Resource.JOB).setWatch(APIGroup.WATCH);
		if(StringUtils.isEmpty(namespace)){
			url.setNamespace(namespace);
		}
		K8SClientResponse response = new K8SClient().doit(url, HTTPMethod.GET,null,queryParams,cluster);
		return response;
	}
}
