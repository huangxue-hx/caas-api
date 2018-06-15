package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dto.config.ConfigDetailDto;


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
     * @param id
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil getConfigMap(String id) throws Exception;

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
     * @return
     */
    ActionReturnUtil getConfigMapByName(String name, String clusterId, String projectId);
}
