package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.application.bean.ConfigFile;

/**
 * Created by gurongyun on 17/03/24.
 */
@Repository
public interface ConfigFileMapper {
    /**
     * get configfile max tags on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @param tenant required
     * @param repoName required
     * @description find configfile
     * @return ConfigFile
     */
    ConfigFile getConfigFile(String name, String tenant, String repoName);

    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param id required
     * @description get configfile by id
     * @return ConfigFile
     */
    ConfigFile getConfigFileById(String id);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param tenant required
     * @param name required
     * @param repoName required
     * @description finds configfiles order by tags desc
     * @return List
     */
    List<ConfigFile> listConfigByName(String tenant, String name, String repoName);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @param tenant required
     * @param repoName required
     * @description delete configfiles
     */
    void deleteConfigFileByName(String name, String tenant, String repoName);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @param tenant required
     * @param repoName required
     * @description finds configfiles order by create_time 
     * @return List
     */
    List<ConfigFile> listConfigByNameLatest(@Param("name")String name, @Param("tenant")String tenant, @Param("repoName")String repoName);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @param tenant required
     * @param repoName required
     * @description finds configfiles order by create_time desc
     * @return List
     */
    List<ConfigFile> listConfigByNameAsc(@Param("name")String name, @Param("tenant")String tenant, @Param("repoName")String repoName);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param tenant required
     * @description finds configfiles 
     * @return List
     */
    List<ConfigFile> listConfigSearch(@Param("tenant")String tenant,@Param("keyword")String keyword);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param tenant required
     * @description find configfiles by tenant,reponame
     * @return list
     */
    List<ConfigFile> listConfigOverview(@Param("tenant")String tenant, @Param("repoName")String repoName);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param id required
     * @description check id by id
     * @return String
     */
    String getId(String id);    

    /**
     * add configfile on 17/03/24.
     * 
     * @author gurongyun
     * @param configFile required
     * @description add configfile
     */
    void saveConfigFile(ConfigFile configFile);

    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param id required
     * @description delete a configfile
     */
    void removeConfigFileById(String id);

    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @description get tenant
     */
    ConfigFile getTenantByName(@Param("name")String name);


    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param name required
     * @param tenant required
     * @param repoName required
     * @description delete configfiles
     */
    void deleteConfigFileByTenant(@Param("tenant")String tenant);



}
