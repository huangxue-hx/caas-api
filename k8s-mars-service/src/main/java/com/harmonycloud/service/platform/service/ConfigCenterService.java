package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.Deployment;

import java.util.List;


/**
 * Created by gurongyun on 17/03/24. configcenter service
 */
public interface ConfigCenterService {

    /**
     * add config service on 17/03/24.
     * 
     * @author gurongyun
     * @param configDetail
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil saveConfig(ConfigDetailDto configDetail, String userName) throws Exception;

    ActionReturnUtil saveConfig(ConfigFile configFile) throws Exception;
    
    /**
     * add config service on 17/03/24.
     * 
     * @author gurongyun
     * @param configDetail
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil updateConfig(ConfigDetailDto configDetail, String userName) throws Exception;

    /**
     * delete config service on 17/03/24.
     * 
     * @author gurongyun
     * @param id
     *            required
     * @param projectId
     *            required
     * @return ActionReturnUtil
     */
    void deleteConfig(String id, String projectId) throws Exception;

    /**
     * delete configs service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param projectId
     *            required
     * @param clusterId
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteConfigMap(String name, String projectId, String clusterId) throws Exception;

    /**
     * find config lists for center service on 17/03/24.
     * 
     * @author gurongyun
     * @param projectId
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil searchConfig(String projectId, String clusterId, String repoName, String keyword) throws Exception;

    /**
     * find config overview lists service on 17/03/24.
     * 
     * @author gurongyun
     * @param projectId
     *            required
     * @param repoName
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil listConfig(String projectId, String repoName) throws Exception;

    /**
     * find configMap on 17/03/24.
     * 
     * @author gurongyun
     * @param configMapId required
     * @return ActionReturnUtil
     */
    ConfigDetailDto getConfigMap(String configMapId) throws Exception;

    /**
     * find a lastest config service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param projectId
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil getLatestConfigMap(String name, String projectId, String repoName,String clusterId,String tags) throws Exception;
    
    /**
     * check service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param projectId
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil checkDuplicateName(String name,String projectId) throws Exception;
    
    ActionReturnUtil deleteConfigByProject(String projectId) throws Exception;

    int deleteByClusterId(String clusterId);

    /**
     * 根据名称获取configmap
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    ActionReturnUtil getConfigMapByName(String namespace, String name) throws Exception;

    /**
     *
     * @param name
     * @param tag
     * @param projectId
     * @param clusterId
     * @return
     */
    ConfigFile getConfigByNameAndTag(String name, String tag, String projectId, String clusterId);

    /**
     * 根据配置组名字,集群id,项目id返回对象
     * @param name
     * @param clusterId
     * @param projectId
     * @param isFilter
     * @return
     * @throws Exception
     */
    ActionReturnUtil getConfigMapByName(String name, String clusterId, String projectId, boolean isFilter) throws Exception;

    /**
     * 返回当前配置组的服务列表
     * @param projectId
     * @param tenantId
     * @param configMapId
     * @return
     */
    List<Deployment> getServiceList(String projectId, String tenantId, String configMapId) throws Exception;

    /**
     * 更新服务的配置组版本
     * @param serviceNameList
     * @return
     */
    ActionReturnUtil updateConfigTag(List<String> serviceNameList, String edition, String configName, String projectId, String tenantId,String clusterId) throws Exception;

    /**
     * 返回所有版本号
     * @param configName
     * @return
     */
    ActionReturnUtil getTagsByConfigName(String configName,String clusterId,String projectId);

    /**
     * 根据配置文件名获取服务列表
     * @return
     */
    ActionReturnUtil getAllServiceByConfigName(String configName,String clusterId,String projectId,String tenantId) throws Exception;

    /**
     * 根据租户集群删除配置文件
     * @param clusterId
     * @param tenantId
     * @throws Exception
     */
    void deleteConfigMap(String clusterId, String tenantId) throws Exception;

    ActionReturnUtil getConfigMapWithService(String configMapId) throws Exception;
}
