package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ConfigMapService;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Created by czm on 2017/6/1.
 */
@Service
public class ConfigMapServiceImpl implements ConfigMapService {
    
    @Override
    public ActionReturnUtil getConfigMapByName(String namespace, String name, String method, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
        return ActionReturnUtil.returnSuccessWithData(configMap);
    }

	@Override
	public ActionReturnUtil listConfigMapByName(String namespace, String name, Cluster cluster) throws Exception {
		if(StringUtils.isEmpty(namespace)){
			return ActionReturnUtil.returnErrorWithMsg("namespace为空");
		}
		if(StringUtils.isEmpty(name)){
			return ActionReturnUtil.returnErrorWithMsg("congfigmap名称为空");
		}
		List<String> names = new ArrayList<String>();
		if(name.contains(",")){
			String [] n = name.split(",");
			names = java.util.Arrays.asList(n);
		}else{
			names.add(name);
		}
		ArrayList<ConfigMap> list = new ArrayList<ConfigMap>();
		if(names != null && names.size() > 0){
			for(String n : names){
				K8SURL url = new K8SURL();
		        url.setNamespace(namespace).setName(n).setResource(Resource.CONFIGMAP);
		        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
		        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
		            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
		        }
		        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
		        list.add(configMap);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(list);
	}
}
