package com.harmonycloud.service.cache;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.TIME_ZONE_UTC;

/**
 * cluster集群信息redis管理
 */
@Component
public class ImageCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCacheManager.class);
    private static final String REDIS_KEY_REPO = "repository";
    private static final String REDIS_KEY_HARBOR_PROJECT = "harbor_project";
    private static final int IMAGE_LOG_MINUTES_BEFORE = 20;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    HarborService harborService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    ClusterService clusterService;


    public void freshRepository() {
        LOGGER.info("刷新缓存中的镜像信息");
        Long begin = System.currentTimeMillis();
        Set<String> currentKeys = new HashSet<>();
        Set<String> harborProjectKeys = new HashSet<>();
        List<ImageRepository> imageRepositories = new ArrayList<>();
        ImageRepository queryRepository = new ImageRepository();
        queryRepository.setIsNormal(Boolean.TRUE);
        Map<String,Map<String,ImageRepository>> harborProjectMap = new HashMap<>();
        try {
            imageRepositories.addAll(harborProjectService.listRepositories(queryRepository));
            List<HarborOverview> harborOverviews = harborProjectService.getHarborProjectOverview(null, null);
            for(HarborOverview harborOverview : harborOverviews){
                String harborHost = harborOverview.getHarborServer().getHarborHost();
                List<ImageRepository> repos = harborOverview.getRepositories();
                harborProjectMap.put(harborHost,repos.stream().collect(Collectors.toMap(ImageRepository::getHarborProjectName, repo -> repo)));
                for(ImageRepository repo : repos){
                    harborProjectKeys.add(this.getHarborProjectCacheKey(harborHost,repo.getHarborProjectName()));
                    HarborProject harborProject =  harborService.getProjectQuota(harborHost, repo.getHarborProjectName());
                    if(harborProject != null){
                        String harborProjectKey = this.getHarborProjectCacheKey(harborHost, harborProject.getProjectName());
                        this.putHarborProject(harborProjectKey, harborProject);
                    }
                }
            }
        }catch (Exception e){
            LOGGER.error("刷新缓存中的repository失败,读取镜像仓库列表失败", e);
            return;
        }

        for (ImageRepository imageRepository : imageRepositories) {
            try {
                if(harborProjectMap.get(imageRepository.getHarborHost()) != null) {
                    ImageRepository repo = harborProjectMap.get(imageRepository.getHarborHost()).get(imageRepository.getHarborProjectName());
                    if (repo == null || repo.getImageCount() == 0) {
                        continue;
                    }
                }
                ActionReturnUtil result = harborService.repoListById(imageRepository.getHarborHost(),
                        imageRepository.getHarborProjectId());
                if (!result.isSuccess()) {
                    LOGGER.error("刷新缓存中的repository失败,:projectName:{},message:{}",
                            imageRepository.getHarborProjectName(), result.getData());
                    continue;
                }
                List<String> repos = (List<String>)result.getData();
                for(String repoName: repos) {
                    HarborRepositoryMessage repositoryMessage = harborService
                            .getHarborRepositoryDetail(imageRepository.getHarborHost(), repoName);
                    if(repositoryMessage != null){
                        this.putRepoMessage(imageRepository.getHarborHost(), repoName, repositoryMessage);
                    }
                    currentKeys.add(this.getRepoCacheKey(imageRepository.getHarborHost(), repoName));
                }
            } catch (Exception e) {
                LOGGER.error("刷新缓存中的repository失败,imageRepository:{}",
                        JSONObject.toJSONString(imageRepository), e);
            }
        }
        //如果缓存中的key不在目前所有镜像列表中，既镜像已经被删除，则将缓存中的镜像删除
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        Set<String> cacheKeys = repoHashOps.keys();
        for(String key : cacheKeys){
            if(!currentKeys.contains(key)){
                repoHashOps.delete(REDIS_KEY_REPO, key);
            }
        }
        //删除harbor project的已删除的project的缓存
        BoundHashOperations<String, String, String> harborProjectHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_HARBOR_PROJECT);
        Set<String> keys = harborProjectHashOps.keys();
        for(String key : keys){
            if(!harborProjectKeys.contains(key)){
                harborProjectHashOps.delete(REDIS_KEY_HARBOR_PROJECT, key);
            }
        }
        LOGGER.info("刷新缓存中的repository耗时：{}s", (System.currentTimeMillis() - begin)/1000);
    }


    /**
     * 获取单个镜像的详细信息
     * @param harborHost harbor器服务地址
     * @param repoName 镜像名称
     * @return
     */
    public HarborRepositoryMessage getRepoMessage(String harborHost, String repoName){
        AssertUtil.notBlank(harborHost, DictEnum.HARBOR_HOST);
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_NAME);
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        String repoKey = this.getRepoCacheKey(harborHost, repoName);
        String repoJson = repoHashOps.get(repoKey);
        if(StringUtils.isNotBlank(repoJson)){
            HarborRepositoryMessage repositoryMessage = JSONObject.parseObject(repoJson, HarborRepositoryMessage.class);
            return repositoryMessage;
        }
        LOGGER.info("redis缓存未找到repo信息，repoKey：{}",repoKey);
        try {
            HarborRepositoryMessage harborRepository = harborService.getHarborRepositoryDetail(harborHost, repoName);
            if(harborRepository != null){
                putRepoMessage(harborHost, repoName, harborRepository);
                return harborRepository;
            }
        }catch (Exception e){
            LOGGER.error("查询镜像信息失败：harborHost:{}, repoName:{}",new String[]{harborHost, repoName},e);
        }
        return null;
    }

    /**
     * 获取某个harbor的所有镜像名称
     * @return
     */
    public Map<String,Set<String>> getRepoNames(){
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        Set<String> keys = repoHashOps.keys();
        if(CollectionUtils.isEmpty(keys)){
            return Collections.emptyMap();
        }
        Map<String,Set<String>> repoMap = new HashMap<>();
        for(String key : keys){
            String harborHost = this.getHarborHost(key);
            String repoName = this.getRepoName(key);
            if(repoMap.get(harborHost) == null){
                Set<String> repos = new HashSet<>();
                repos.add(repoName);
                repoMap.put(harborHost, repos);
            }else{
                repoMap.get(harborHost).add(repoName);
            }

        }
        return repoMap;
    }


    /**
     * 增加或更新单个镜像详细信息
     * @param repositoryMessage 镜像详细信息
     */
    public void putRepoMessage(String harborHost, String repoName, HarborRepositoryMessage repositoryMessage){
        if(repositoryMessage == null){
            return;
        }
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        String repoJson = JSONObject.toJSONString(repositoryMessage);
        repoHashOps.put(this.getRepoCacheKey(harborHost, repoName), repoJson);
    }

    /**
     * 获取单个镜像的详细信息
     * @param harborHost harbor器服务地址
     * @param harborProjectName harborProject名称
     * @return
     */
    public HarborProject getHarborProject(String harborHost, String harborProjectName){
        AssertUtil.notBlank(harborHost, DictEnum.HARBOR_HOST);
        AssertUtil.notBlank(harborProjectName, DictEnum.REPOSITORY);
        BoundHashOperations<String, String, String> harborProjectHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_HARBOR_PROJECT);
        String key = this.getHarborProjectCacheKey(harborHost, harborProjectName);
        String projectJson = harborProjectHashOps.get(key);
        if(StringUtils.isNotBlank(projectJson)){
            return JSONObject.parseObject(projectJson, HarborProject.class);
        }
        LOGGER.info("redis缓存未找到harborProject信息，harborProjectName：{}",harborProjectName);
        try {
            HarborProject harborProject =  harborService.getProjectQuota(harborHost, harborProjectName);
            if(harborProject != null){
                putHarborProject(harborHost, harborProject);
                return harborProject;
            }
        }catch (Exception e){
            LOGGER.error("查询harbor project信息失败：harborHost:{}, harborProjectName:{}",
                    new String[]{harborHost, harborProjectName},e);
        }
        return null;
    }

    /**
     * 增加或更新单个镜像详细信息
     * @param harborProject harborProject详细信息
     */
    public void putHarborProject(String harborProjectKey, HarborProject harborProject){
        if(harborProject == null){
            return;
        }
        BoundHashOperations<String, String, String> harborProjectHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_HARBOR_PROJECT);
        String harborProjectJson = JSONObject.toJSONString(harborProject);
        harborProjectHashOps.put(harborProjectKey, harborProjectJson);
    }


    /**
     * 根据harbor项目下镜像的操作日志更新操作过的镜像信息
     * @param projectId
     * @throws Exception
     */
    public void freshRepositoryCache(String harborHost,Integer projectId){
        try {
            int end = DateUtil.getTimeInt(new Date());
            //当前时间的前20分钟内的操作日志
            int begin = DateUtil.getTimeInt(DateUtil.addMinute(new Date(), -IMAGE_LOG_MINUTES_BEFORE));
            List<HarborLog> harborLogs = harborService.projectOperationLogs(harborHost, projectId, begin, end, "create/push/delete");
            if (CollectionUtils.isEmpty(harborLogs)) {
                return;
            }
            Set<String> refreshedRepoNames = new HashSet<>();
            for (HarborLog harborLog : harborLogs) {
                Date operateDate = DateUtil.stringToDate(harborLog.getOperationTime(),
                        DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue(), TIME_ZONE_UTC);
                String repoCacheKey = this.getRepoCacheKey(harborHost, harborLog.getRepoName());
                HarborRepositoryMessage harborRepositoryMessage = this.getRepoMessage(harborHost, harborLog.getRepoName());
                //已更新 或 镜像的最后更新时间在操作日志之前，此操作日志已经过时，无需再更新镜像信息
                if (harborRepositoryMessage == null || refreshedRepoNames.contains(repoCacheKey)
                        ||  harborRepositoryMessage.getLastUpdateDate().after(operateDate)) {
                    continue;
                }
                this.putRepoMessage(harborHost, harborLog.getRepoName(), harborService.getHarborRepositoryDetail(harborHost, harborLog.getRepoName()));
                refreshedRepoNames.add(repoCacheKey);
                LOGGER.info("刷新镜像缓存，镜像名称：{}", harborLog.getRepoName());
            }
        }catch (Exception e){
            LOGGER.error("刷新镜像缓存失败，harborHost, projectId:{}", new String[]{harborHost, projectId.toString()}, e);
        }
    }

    /**
     * 镜像缓存Map的key，格式：harborHost/repoName
     * @param harborHost
     * @param repoName
     * @return
     */
    private String getRepoCacheKey(String harborHost, String repoName){
        return harborHost + SLASH + repoName;
    }

    /**
     * harbor project缓存Map的key，格式：harborHost/project
     * @param harborHost
     * @param harborProjectName
     * @return
     */
    public String getHarborProjectCacheKey(String harborHost, String harborProjectName){
        return harborHost + SLASH + harborProjectName;
    }

    private String getHarborHost(String repoKey){
        return repoKey.substring(0, repoKey.indexOf(SLASH));
    }

    private String getRepoName(String repoKey){
        return repoKey.substring(repoKey.indexOf(SLASH) + 1);
    }


}
