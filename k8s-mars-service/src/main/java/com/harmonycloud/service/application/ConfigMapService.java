package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;

/**
 * Created by czm on 2017/6/1.
 */
public interface ConfigMapService {

    public ActionReturnUtil getConfigMapByName(String namespace , String name, String method, Cluster cluster) throws Exception;
    
    public ActionReturnUtil listConfigMapByName(String namespace , String name) throws Exception;

    /**
     * create configmap
     *
     * @param configMaps
     * @param namespace
     * @param containerName
     * @param cluster
     * @param type
     * @param name
     * @param username
     * @return ActionReturnUtil
     * */
    public void createConfigMap(List<CreateConfigMapDto> configMaps, String namespace, String containerName,
                                            String name, Cluster cluster, String type, String username) throws Exception;

    /**
     * 更新configmap
     * @param configMap
     * @param cluster
     * @throws Exception
     */
    public void updateConfigmap(ConfigMap configMap, Cluster cluster) throws Exception;

}
