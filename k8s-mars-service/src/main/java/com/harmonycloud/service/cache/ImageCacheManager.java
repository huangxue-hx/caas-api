package com.harmonycloud.service.cache;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;

/**
 * cluster集群信息redis管理
 */
@Component
public class ImageCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCacheManager.class);
    private static final String REDIS_KEY_REPO = "repository";
    private static final String REDIS_KEY_HARBOR_LOG = "harbor_log";


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private HarborService harborService;
    @Autowired
    private HarborProjectService harborProjectService;


    /**
     * 全量刷新镜像信息
     */
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
                currentKeys.addAll(freshRepository(imageRepository.getHarborHost(),imageRepository.getHarborProjectId()));
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
        LOGGER.info("刷新缓存中的repository耗时：{}s", (System.currentTimeMillis() - begin)/1000);
    }

    public Set<String> freshRepository(String harborHost,Integer harborProjectId) throws Exception{
        ActionReturnUtil result = harborService.repoListById(harborHost, harborProjectId);
        if (!result.isSuccess()) {
            LOGGER.error("刷新缓存中的repository失败,:harborHost:{},projectId:{},message:{}",
                    new String[]{harborHost,harborProjectId.toString(), result.getData().toString()});
            return Collections.emptySet();
        }
        List<String> repos = (List<String>)result.getData();
        Set<String> refreshedKeys = new HashSet<>();
        for(String repoName: repos) {
            HarborRepositoryMessage repositoryMessage = harborService
                    .getHarborRepositoryDetail(harborHost, repoName);
            if(repositoryMessage != null){
                this.putRepoMessage(harborHost, repoName, repositoryMessage);
            }
            refreshedKeys.add(this.getRepoCacheKey(harborHost, repoName));
        }
        return refreshedKeys;
    }

    /**
     * 根据版本号更新缓存，查询harbor接口返回的tags列表更新至缓存
     * @param harborHost
     * @param repoName
     * @throws Exception
     */
    public HarborRepositoryMessage freshRepositoryByTags(String harborHost, String repoName) throws Exception{
        boolean hasUpdated = false;
        List<String> tags = harborService.listTag(harborHost, repoName);
        if(CollectionUtils.isEmpty(tags)){
            this.deleteRepoMessage(harborHost, repoName);
            return null;
        }
        HarborRepositoryMessage harborRepositoryMessage = this.getRepoMessage(harborHost, repoName);
        if(harborRepositoryMessage == null || harborRepositoryMessage.getRepositoryDetial() == null){
            HarborRepositoryMessage repositoryMessage = harborService.getHarborRepositoryDetail(harborHost,repoName);
            this.putRepoMessage(harborHost,repoName, repositoryMessage);
            return repositoryMessage;
        }
        List<HarborManifest> manifests = harborRepositoryMessage.getRepositoryDetial();
        List<HarborManifest> newManifests = new ArrayList<>();
        List<String> newTags = new ArrayList<>();
        for(HarborManifest manifest : manifests){
            if(tags.contains(manifest.getTag())){
                newManifests.add(manifest);
                newTags.add(manifest.getTag());
            }
        }
        for(String tag : tags){
            if(!newTags.contains(tag)){
                ActionReturnUtil response = harborService.getManifestsWithVulnerabilitySum(harborHost, repoName, tag);
                if(!response.isSuccess() || response.getData() == null){
                    LOGGER.error("get tag manifest error, harborHost:{}, repoName:{}, tag, res:{}",
                            new String[]{harborHost, repoName, tag, JSONObject.toJSONString(response)});
                    continue;
                }
                newManifests.add((HarborManifest)response.getData());
                hasUpdated = true;
            }
        }
        if(tags.size() == manifests.size() && !hasUpdated){
            LOGGER.info("镜像版本数量相同，且tag名称没有变更，无需更新缓存");
            return harborRepositoryMessage;
        }
        newManifests = this.sort(newManifests);
        harborRepositoryMessage.setRepositoryDetial(newManifests);
        this.putRepoMessage(harborHost,repoName, harborRepositoryMessage);
        return harborRepositoryMessage;
    }

    /**
     * 根据harbor的日志刷新镜像,只有更新过的镜像tag才会刷新
     * @throws Exception
     */
    public void freshRepositoryByLog() throws Exception{
        ImageRepository queryRepository = new ImageRepository();
        queryRepository.setIsNormal(Boolean.TRUE);
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(queryRepository);
        for (ImageRepository imageRepository : imageRepositories) {
            try{
                freshRepositoryCache(imageRepository.getHarborHost(), imageRepository.getHarborProjectId());
            }catch (Exception e){
                LOGGER.info("根据日志刷新镜像缓存信息失败：imageRepository:{}",JSONObject.toJSONString(imageRepository),e);
            }
        }
    }

    /**
     * 根据镜像的创建时间排序，没有时间字段的根据tag名称排序
     * @param harborManifests
     * @return
     */
    public List<HarborManifest> sort(List<HarborManifest> harborManifests){
        if(CollectionUtils.isEmpty(harborManifests)){
            return harborManifests;
        }
        List<HarborManifest> sortedHarborManifests = new ArrayList<>();
        List<HarborManifest>  manifests = harborManifests.stream().filter(harborManifest -> harborManifest.getCreateTime() != null).collect(Collectors.toList());
        List<HarborManifest>  nullCreatedTimeManifests = harborManifests.stream().filter(harborManifest -> harborManifest.getCreateTime() == null).collect(Collectors.toList());
        manifests.sort((manifest1, manifest2) ->
                {
                    int result = manifest2.getCreateTime().compareTo(manifest1.getCreateTime());
                    //时间相同，根据版本号名称排序
                    if(result == 0){
                        return manifest2.getTag().compareTo(manifest1.getTag());
                    }
                    return result;
                });
        nullCreatedTimeManifests.sort((manifest1, manifest2) -> manifest2.getTag().compareTo(manifest1.getTag()));
        sortedHarborManifests.addAll(manifests);
        sortedHarborManifests.addAll(nullCreatedTimeManifests);
        harborManifests = sortedHarborManifests;
        return harborManifests;
    }

    /**
     * 镜像已经被删除，删除缓存中的repo tag详情
     * @param harborHost
     * @param repoName
     */
    public void deleteRepoMessage(String harborHost, String repoName){
        AssertUtil.notBlank(harborHost,DictEnum.HARBOR_HOST);
        AssertUtil.notBlank(repoName,DictEnum.IMAGE_NAME);
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        repoHashOps.delete(REDIS_KEY_REPO, this.getRepoCacheKey(harborHost, repoName));
    }

    /**
     * 增加一个镜像版本
     * @param harborHost
     * @param repoName
     */
    public void addRepoTag(String harborHost, String repoName, HarborManifest harborManifest){
        AssertUtil.notBlank(harborHost,DictEnum.HARBOR_HOST);
        AssertUtil.notBlank(repoName,DictEnum.IMAGE_NAME);
        HarborRepositoryMessage repositoryMessage = this.getRepoMessage(harborHost, repoName);
        if(repositoryMessage == null){
            LOGGER.error("缓存中不存在该镜像信息,harbor:{},repoName:{}",harborHost, repoName);
            return;
        }
        List<HarborManifest> harborManifests = repositoryMessage.getRepositoryDetial();
        boolean tagExists = false;
        for(int i=0;i<harborManifests.size();i++){
            HarborManifest manifest = harborManifests.get(i);
            if(manifest.getTag().equals(harborManifest.getTag())){
                //如果tag已经存在，且digest相同，为同一个镜像，不需要更新
                if(manifest.getDigest().equals(harborManifest.getDigest())){
                    return;
                }else{
                    //如果tag已经存在，digest不同，更新原先的tag信息
                    harborManifests.set(i,harborManifest);
                    tagExists = true;
                    break;
                }
            }
        }
        //tag不存在，为新增tag
        if(!tagExists) {
            repositoryMessage.getRepositoryDetial().add(harborManifest);
        }
        repositoryMessage.setRepositoryDetial(this.sort(repositoryMessage.getRepositoryDetial()));
        this.putRepoMessage(harborHost,repoName,repositoryMessage);
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
    public void putRepoMessage(String harborHost, String repoName, HarborRepositoryMessage repositoryMessage) {
        if (repositoryMessage == null) {
            return;
        }
        //设置tags列表
        repositoryMessage.setTags(repositoryMessage.getRepositoryDetial().stream().map(manifest -> manifest.getTag()).collect(Collectors.toList()));
        BoundHashOperations<String, String, String> repoHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_REPO);
        String repoJson = JSONObject.toJSONString(repositoryMessage);
        repoHashOps.put(this.getRepoCacheKey(harborHost, repoName), repoJson);
    }

    /**
     * 记录某个harbor project的最后push/delete的操作对应的logid以及更新的时间
     */
    private void putHarborLog(String harborHost, Integer projectId, Long harborLogId, int time){
        BoundHashOperations<String, String, String> harborLogHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_HARBOR_LOG);
        harborLogHashOps.put(this.getHarborLogKey(harborHost, projectId), harborLogId + "@" + time);
    }

    private String getHarborLog(String harborHost, Integer projectId){
        BoundHashOperations<String, String, String> harborLogHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_HARBOR_LOG);
        return harborLogHashOps.get(this.getHarborLogKey(harborHost, projectId));
    }


    /**
     * 根据harbor项目下镜像的操作日志更新操作过的镜像信息
     * @param projectId
     * @throws Exception
     */
    public void freshRepositoryCache(String harborHost,Integer projectId){
        try {
            int lastUpdate = 0;
            Long harborLogId = 0L;
            int end = DateUtil.getTimeInt(new Date());
            //该仓库上一次的更新记录
            String lastLog = this.getHarborLog(harborHost, projectId);
            if(lastLog != null){
                lastUpdate = Integer.parseInt(lastLog.substring(lastLog.indexOf("@")+1));
                harborLogId = Long.parseLong(lastLog.substring(0,lastLog.indexOf("@")));
            }
            //该仓库首次刷新或上次刷新距离现在超过1天的，不通过日志刷新,整个镜像版本全量刷新
            if(lastLog == null || end - lastUpdate > ONE_DAY_SECONDS){
                freshRepository(harborHost, projectId);
                this.putHarborLog(harborHost, projectId, harborLogId,end);
                return;
            }
            //查询上次更新距离现在这段之间内这个仓库的镜像更新操作记录，因服务器时间可能存在偏差，查询日志往前多查询5分钟
            int begin = lastUpdate - FIVE_MINUTES_SECONDS;
            List<HarborLog> harborLogs = harborService.projectOperationLogs(harborHost, projectId, begin, end, "push/delete");
            //只需要处理上次最后更新的logId之后的操作记录
            Long lastHarborLogId = harborLogId;
            Map<String, List<HarborLog>> harborLogMap = harborLogs.stream().filter(log ->  log.getLogId() > lastHarborLogId).collect(Collectors.groupingBy(HarborLog::getRepoName));
            if (CollectionUtils.isEmpty(harborLogMap)) {
                //没有需要处理的操作日志，更新最后刷新的时间
                this.putHarborLog(harborHost, projectId, harborLogId, end);
                return;
            }
            //按镜像分组，分别对某个镜像进行刷新
            for (String repoName : harborLogMap.keySet()) {
                try {
                    //判断该镜像是否正在删除中，如果删除中，根据harbor api返回的tag列表更新缓存
                    String key = REDIS_KEY_IMAGE_DELETING + COLON + harborHost + SLASH + repoName;
                    String inDelete = stringRedisTemplate.opsForValue().get(key);
                    if (StringUtils.isNotBlank(inDelete)) {
                        this.freshRepositoryByTags(harborHost, repoName);
                        continue;
                    }
                    List<HarborLog> repoLogs = harborLogMap.get(repoName);
                    //获取这个镜像更新过的所有tag名称列表
                    Set<String> tags = repoLogs.stream().map(HarborLog::getRepoTag).collect(Collectors.toSet());
                    HarborRepositoryMessage harborRepositoryMessage = this.getRepoMessage(harborHost, repoName);
                    //如果缓存中没有，镜像为新建的
                    if (harborRepositoryMessage == null) {
                        this.putRepoMessage(harborHost, repoName, harborService.getHarborRepositoryDetail(harborHost, repoName));
                        continue;
                    }
                    List<HarborManifest> harborManifests = harborRepositoryMessage.getRepositoryDetial();
                    Map<String, HarborManifest> harborManifestMap = harborManifests.stream().collect(Collectors.toMap(HarborManifest::getTag, manifest -> manifest));
                    //每个tag对应的digest值
                    Map<String, String> tagDigest = harborManifests.stream().collect(Collectors.toMap(HarborManifest::getTag, manifest -> manifest.getDigest()));
                    boolean updated = false;
                    for (String tag : tags) {
                        ActionReturnUtil harborManifestRes = harborService.getManifestsWithVulnerabilitySum(harborHost, repoName, tag);
                        if (harborManifestRes.isSuccess()) {
                            HarborManifest manifest = (HarborManifest) harborManifestRes.getData();
                            //缓存中存在的相同digest和tag，不需要更新缓存
                            if (tagDigest.get(tag) != null && tagDigest.get(tag).equals(manifest.getDigest())) {
                                continue;
                            }
                            //更新tag的信息
                            harborManifestMap.put(tag, manifest);
                            updated = true;
                            LOGGER.info("刷新镜像缓存，增加镜像，repoName：{}，tag：{}", repoName, tag);
                        } else if (harborManifestRes.getData() != null
                                && harborManifestRes.getData().toString().indexOf("MANIFEST_UNKNOWN") > 0) {
                            //缓存中tag已经不存在，不需要更新
                            if (tagDigest.get(tag) == null) {
                                continue;
                            }
                            harborManifestMap.remove(tag);
                            updated = true;
                            LOGGER.info("刷新镜像缓存，删除镜像，repoName：{}，tag：{}", repoName, tag);
                        }
                    }
                    if (updated) {
                        if (CollectionUtils.isEmpty(harborManifestMap)) {
                            deleteRepoMessage(harborHost, repoName);
                        } else {
                            List<HarborManifest> updatedHarborManifest = new ArrayList<>(harborManifestMap.values());
                            updatedHarborManifest = this.sort(updatedHarborManifest);
                            harborRepositoryMessage.setRepositoryDetial(updatedHarborManifest);
                            harborRepositoryMessage.setTags(updatedHarborManifest.stream().map(HarborManifest::getTag).collect(Collectors.toList()));
                            this.putRepoMessage(harborHost, repoName, harborRepositoryMessage);
                        }
                    }
                }catch (Exception e){
                    LOGGER.error("刷新镜像缓存失败，harborHost, repo:{}", new String[]{harborHost, repoName}, e);
                }
            }
            //设置最后更新的操作记录
            this.putHarborLog(harborHost, projectId, harborLogs.get(0).getLogId(),end);
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

    private String getHarborLogKey(String harborHost, Integer projectId){
        return "harbor_log:" + harborHost + ":" + projectId ;
    }


}
