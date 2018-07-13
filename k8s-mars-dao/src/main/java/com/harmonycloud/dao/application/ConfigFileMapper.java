package com.harmonycloud.dao.application;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.application.bean.ConfigFile;

/**
 * Created by gurongyun on 17/03/24.
 */
@Repository
public interface ConfigFileMapper {

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
     * @description get configfile by id
     * @return ConfigFile
     */
    ConfigFile getConfig(String id);

    /**
     * on 17/03/24.
     *
     * @author gurongyun
     * @param name required
     * @param projectId required
     * @param repoName required
     * @description finds configfiles order by create_time
     * @return List
     */
    ConfigFile getLatestConfig(@Param("name") String name, @Param("projectId") String projectId, @Param("repoName") String repoName,@Param("clusterId")String clusterId,@Param("tags")String tags);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param projectId required
     * @param name required
     * @param repoName required
     * @description finds configfiles order by tags desc
     * @return List
     */
    List<ConfigFile> listConfigByName(@Param("name") String name, @Param("projectId") String projectId, @Param("clusterId") String clusterId, @Param("repoName") String repoName);

    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param projectId required
     * @description finds configfiles 
     * @return List
     */
    List<ConfigFile> listConfigSearch(@Param("projectId") String projectId,
                                      @Param("clusterIds") Set<String> clusterIds,
                                      @Param("repoName") String repoName,
                                      @Param("keyword") String keyword);
    
    /**
     * on 17/03/24.
     * 
     * @author gurongyun
     * @param projectId required
     * @description find configfiles by projectId,reponame
     * @return list
     */
    List<ConfigFile> listConfigOverview(@Param("projectId") String projectId, @Param("repoName") String repoName, @Param("clusterIds") Set<String> clusterIds);

    /**
     * on 17/03/24.
     *
     * @author gurongyun
     * @param name required
     * @param projectId required
     * @param repoName required
     * @description delete configfiles
     */
    int deleteConfigByName(@Param("name") String name, @Param("projectId") String projectId,
                           @Param("clusterId") String clusterId);

    /**
     * on 17/03/24.
     *
     * @author gurongyun
     * @param projectId required
     * @description delete configfiles
     */
    void deleteConfigByProject(@Param("projectId") String projectId);

    /**
     * on 17/03/24.
     *
     * @author gurongyun
     * @param id required
     * @description delete a configfile
     */
    int deleteConfig(@Param("id") String id, @Param("projectId") String projectId);

    void updateConfig(ConfigFile configFile);

    int deleteByClusterId(@Param("clusterId") String clusterId);

    ConfigFile getConfigByNameAndTag(@Param("name") String name, @Param("tag") String tag, @Param("projectId") String projectId, @Param("clusterId") String clusterId);

    List<ConfigFile> getConfigMapByName(@Param("name") String name, @Param("clusterId") String clusterId, @Param("projectId") String projectId);
}
