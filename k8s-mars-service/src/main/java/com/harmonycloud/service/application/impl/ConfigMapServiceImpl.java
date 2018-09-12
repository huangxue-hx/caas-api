package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by czm on 2017/6/1.
 */
@Service
public class ConfigMapServiceImpl implements ConfigMapService {

	@Autowired
	NamespaceLocalService namespaceLocalService;
    
    @Override
    public ActionReturnUtil getConfigMapByName(String namespace, String name, String method, Cluster cluster) throws Exception {
		if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		Cluster newCluster= cluster;
    	if (Objects.isNull(newCluster)) {
			newCluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		}
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(name).setResource(Resource.CONFIGMAP);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, newCluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            throw new MarsRuntimeException(response.getBody());
        }
        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
        return ActionReturnUtil.returnSuccessWithData(configMap);
    }

	@Override
	public ActionReturnUtil listConfigMapByName(String namespace, String name) throws Exception {
		if(StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
		if (Objects.isNull(cluster)) {
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
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
		            return ActionReturnUtil.returnErrorWithData(response.getBody());
		        }
		        ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
		        list.add(configMap);
			}
		}
		return ActionReturnUtil.returnSuccessWithData(list);
	}

	@Override
	public void createConfigMap(List<CreateConfigMapDto> configMaps, String namespace, String containerName, String name, Cluster cluster, String type, String username) throws Exception {
		if (configMaps != null && configMaps.size() > 0) {
			K8SURL url1 = new K8SURL();
			url1.setNamespace(namespace).setResource(Resource.CONFIGMAP).setName(name + containerName);
			K8SClientResponse responses = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, null, cluster);
			Map<String, Object> convertJsonToMap = JsonUtil.convertJsonToMap(responses.getBody());
			String metadata = convertJsonToMap.get(CommonConstant.METADATA).toString();
			if (!CommonConstant.EMPTYMETADATA.equals(metadata)) {
				throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_NOT_EXIST);
			}
			K8SURL url = new K8SURL();
			url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
			Map<String, Object> bodys = new HashMap<String, Object>();
			Map<String, Object> meta = new HashMap<String, Object>();
			meta.put("namespace", namespace);
			meta.put("name", name + containerName);
			Map<String, Object> label = new HashMap<String, Object>();
			label.put(type,name);
			meta.put("labels", label);
			bodys.put("metadata", meta);
			Map<String, Object> data = new HashMap<String, Object>();
			for (CreateConfigMapDto configMap : configMaps) {
				if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
					if (StringUtils.isEmpty(configMap.getFile())) {
						data.put("config.json", configMap.getValue());
					} else {
						data.put(configMap.getFile() + "v" + configMap.getTag(), configMap.getValue());
					}
				}
			}
			bodys.put("data", data);
			Map<String, Object> headers = new HashMap<String, Object>();
			headers.put("Content-type", "application/json");
			K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
			if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
				throw new MarsRuntimeException(status.getMessage());
			}
		}
	}

	@Override
	public void updateConfigmap(ConfigMap configMap, Cluster cluster) throws Exception {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("Content-type", "application/json");
		Map<String, Object> bodys = new HashMap<String, Object>();
		bodys = CollectionUtil.transBean2Map(configMap);
		K8SURL url = new K8SURL();
		url.setNamespace(CommonConstant.KUBE_SYSTEM).setResource(Resource.CONFIGMAP).setName(configMap.getMetadata().getName());
		K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, headers, bodys, cluster);
		if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
			throw new MarsRuntimeException(status.getMessage());
		}
	}

    @Override
    public void createConfigMap(String namespace, String configMapName, String serviceName, List<CreateConfigMapDto> configMaps, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", namespace);
        meta.put("name", configMapName);
        Map<String, Object> label = new HashMap<String, Object>();
        label.put("app", serviceName);
        meta.put("labels", label);
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        for (CreateConfigMapDto configMap : configMaps) {
            if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
                if (Objects.isNull(configMap.getValue())){
                    throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_IS_EMPTY);
                }
                if (StringUtils.isEmpty(configMap.getFile())) {
                    data.put("config.json", configMap.getValue().toString());
                } else {
                    data.put(configMap.getFile() + "v" + configMap.getTag(), configMap.getValue().toString());
                }
            }
        }
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }
}
