package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
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
     * @param tenant
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil removeConfig(String id, String tenant) throws Exception;

    /**
     * delete configs service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param tenant
     *            required
     * @param repoName
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteConfigs(String name, String tenant, String repoName) throws Exception;

    /**
     * find config lists for center service on 17/03/24.
     * 
     * @author gurongyun
     * @param tenant
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil listConfigSearch(String tenant, String keyword) throws Exception;

    /**
     * find config overview lists service on 17/03/24.
     * 
     * @author gurongyun
     * @param tenant
     *            required
     * @param repoName
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil listConfigOverview(String tenant, String repoName) throws Exception;

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
     * find a config by id service on 17/03/24.
     * 
     * @author gurongyun
     * @param id
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil getById(String id) throws Exception;

    /**
     * find a lastest config service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param tenant
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil getConfigByName(String name, String tenant, String repoName) throws Exception;
    
    /**
     * check service on 17/03/24.
     * 
     * @author gurongyun
     * @param name
     *            required
     * @param tenant
     *            required
     * @return ActionReturnUtil
     */
    ActionReturnUtil checkName(String name,String tenant) throws Exception;
    
    ActionReturnUtil deleteConfigsByTenant(String tenant) throws Exception;
}
