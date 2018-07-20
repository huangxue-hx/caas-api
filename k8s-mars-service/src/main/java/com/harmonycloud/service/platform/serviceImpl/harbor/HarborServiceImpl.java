package com.harmonycloud.service.platform.serviceImpl.harbor;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.service.harbor.*;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.harmonycloud.service.platform.client.HarborClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.DEFAULT_PAGE_SIZE;
import static com.harmonycloud.service.platform.constant.Constant.DEFAULT_PAGE_SIZE_1000;
import static com.harmonycloud.service.platform.constant.Constant.TIME_ZONE_UTC;

/**
 * Created by zsl on 2017/1/18.
 */
@Service
public class HarborServiceImpl implements HarborService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborServiceImpl.class);
    private static ExecutorService executorService = Executors.newFixedThreadPool(5);
    //镜像版本数量超过50个的时候走异步删除
    private static final int IMAGE_DELETE_ASYNCHRONOUS_COUNT = 50;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private HarborSecurityService harborSecurityService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private HarborUserService harborUserService;
    @Autowired
    private HarborReplicationService harborReplicationService;
    @Autowired
    private HarborImageCleanService harborImageCleanService;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageCacheManager imageCacheManager;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    private static String SPLIT = "#@#";

    @Override
    public HarborOverview getHarborOverview(String harborHost, String userName) throws Exception {
        List<HarborOverview> harborOverviews = harborProjectService.getHarborProjectOverview(harborHost, userName);
        if(CollectionUtils.isEmpty(harborOverviews)){
            return null;
        }
        HarborOverview harborOverview = harborOverviews.get(0);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        harborServer.setNormal(HarborClient.checkHarborStatus(harborServer));
        ImageRepository imageRepository = new ImageRepository();
        imageRepository.setIsNormal(Boolean.TRUE);
        imageRepository.setHarborHost(harborHost);
        List<ImageRepository> imageRepositories = harborOverview.getRepositories();
        if(!CollectionUtils.isEmpty(imageRepositories)) {
            List<HarborPolicyDetail> replications = harborReplicationService.listPolicies(harborHost);
            List<Integer> harborProjectIds = imageRepositories.stream().map(ImageRepository::getHarborProjectId).collect(Collectors.toList());
            replications = replications.stream().filter(policy -> harborProjectIds.contains(policy.getProject_id())).collect(Collectors.toList());
            List<Integer> repositoryIds = imageRepositories.stream().map(ImageRepository::getId).collect(Collectors.toList());
            List<ImageCleanRule> imageCleanRules = harborImageCleanService.listByIds(repositoryIds);
            harborOverview.setPolicies(replications);
            harborOverview.setCleanRules(imageCleanRules);
        }
        harborOverview.setRepositories(imageRepositories);
        harborOverview.setHarborServer(harborServer);
        return harborOverview;
    }

    /**
     * 分页获取harbor project list
     *
     * @param page     页号
     * @param pageSize 页码
     * @return
     * @throws Exception
     */
    @Override
    public List<HarborProject> listProject(String harborHost, String harborProjectName, Integer page, Integer pageSize) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        List<HarborProject> harborProjects = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        boolean isEnd = false;
        int pageNo = NUM_ONE;
        int onePageSize = (pageSize == null?DEFAULT_PAGE_SIZE_1000:pageSize);
        while(!isEnd) {
            //如果参数传了分页页码，则只需要查询这一页
            if(page != null){
                pageNo = page;
                isEnd = true;
            }
            params.put("page", pageNo++);
            params.put("page_size", onePageSize);
            if (StringUtils.isNotBlank(harborProjectName)) {
                params.put("project_name", harborProjectName);
            }
            ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
            if (response.isSuccess() && response.getData() != null) {
                List<HarborProject> projects = getHarborProjectList(response.getData().toString());
                if(projects.size() < DEFAULT_PAGE_SIZE_1000){
                    isEnd = true;
                }
                harborProjects.addAll(projects);
            }

        }
        return harborProjects;
    }

    /**
     * 根据仓库id获取harbor project详情
     *
     * @param harborProjectId id
     * @return
     * @throws Exception
     */
    @Override
    public HarborProject getHarborProjectById(String harborHost, Integer harborProjectId) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, null);
        if (response.isSuccess() && response.get("data") != null) {
            return getHarborProjectResp(response.get("data").toString());
        }
        return null;
    }

    /**
     * 根据projectId获取harbor repository列表,项目下的所有repo记录
     *
     * @param harborProjectId id
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil repoListById(String harborHost, Integer harborProjectId) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        List<String> repos = new ArrayList<>();
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();

        params.put("project_id", harborProjectId);
        params.put("page_size", DEFAULT_PAGE_SIZE);
        boolean isEnd = false;
        int pageNo = 1;
        while(!isEnd) {
            params.put("page", pageNo++);
            ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
            if (response.isSuccess() && response.get("data") != null) {
                List<String> repoList = JsonUtil.jsonToList(response.get("data").toString(), String.class);
                if(CollectionUtils.isEmpty(repoList)){
                    return ActionReturnUtil.returnSuccessWithData(repos);
                }
                if(repoList.size() < DEFAULT_PAGE_SIZE){
                    isEnd = true;
                }
                repos.addAll(repoList);
            }else {
                LOGGER.error("查询镜像repo list失败,harborHost:{},response:{}",harborHost, JSONObject.toJSONString(response));
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(repos);
    }

    /**
     * 根据projectId获取harbor repository列表,分页查询
     *
     * @param harborProjectId id
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil repoListById(String harborHost, Integer harborProjectId, Integer pageSize, Integer pageNo,
                                         String repoName) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        List<String> repos = new ArrayList<>();
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("project_id", harborProjectId);
        params.put("page_size", pageSize == null ? DEFAULT_PAGE_SIZE_20 : pageSize);
        params.put("page", pageNo == null ? NUM_ONE : pageNo);
        if(StringUtils.isNotBlank(repoName)){
            params.put("q", repoName);
        }
        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        if (response.isSuccess() && response.get("data") != null) {
            List<String> repoList = JsonUtil.jsonToList(response.get("data").toString(), String.class);
            if(CollectionUtils.isEmpty(repoList)){
                return ActionReturnUtil.returnSuccessWithData(repos);
            }
            repos.addAll(repoList);
        }else {
            LOGGER.error("查询镜像repo list失败,harborHost:{},response:{}",harborHost, JSONObject.toJSONString(response));
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
        }
        return ActionReturnUtil.returnSuccessWithData(repos);
    }

    /**
     * 根据repository name获取tags
     *
     * @param repoName repoName
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getTagsByRepoName(String harborHost,String repoName) throws Exception {
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_NAME);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/tags";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 获取manifests
     *
     * @param repoName repoName
     * @param tag      tag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getManifests(String harborHost, String repoName, String tag) throws Exception {
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_NAME);
        AssertUtil.notBlank(repoName, DictEnum.IMAGE_TAG);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/manifests";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);
        params.put("tag", tag);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
    }


    /**
     * 创建harbor project
     *
     * @param harborProject bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createProject(String harborHost, HarborProject harborProject) throws Exception {
        AssertUtil.notNull(harborProject);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpPostRequestForHarborCreate(url, headers, convertHarborProjectBeanToMap(harborProject));
    }

    /**
     * 删除harbor project
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteProject(String harborHost, Integer projectId) throws Exception {
        LOGGER.info("删除project，harborHost：{}，harborProjectId：{}",harborHost, projectId);
        //先删除项目下的同步规则，如果规则已经启动，先停止再删除
        List<HarborPolicyDetail> policies = harborReplicationService.listProjectPolicies(harborHost,projectId);
        if(!CollectionUtils.isEmpty(policies)){
            for(HarborPolicyDetail policy : policies){
                if(policy.getEnabled() == FLAG_TRUE){
                    ActionReturnUtil updateRes = harborReplicationService.updatePolicyEnable(harborHost,
                            policy.getPolicy_id(), FLAG_FALSE);
                    if(!updateRes.isSuccess()){
                        throw new MarsRuntimeException(ErrorCodeMessage.REPLICATION_DELETE_FAIL);
                    }
                }
                ActionReturnUtil deleteRes =harborReplicationService.deletePolicy(harborHost, policy.getPolicy_id());
                if(!deleteRes.isSuccess()){
                    throw new MarsRuntimeException(ErrorCodeMessage.REPLICATION_DELETE_FAIL);
                }
            }
        }
        //先删除项目下的镜像
        ActionReturnUtil repoResponse = this.repoListById(harborHost, projectId);
        if(repoResponse.isSuccess() && repoResponse.getData() != null) {
            List<String> repos = (List<String>) repoResponse.getData();
            if(!CollectionUtils.isEmpty(repos)) {
                for (String repo : repos) {
                    this.deleteRepo(harborHost, repo, null);
                }
            }
        }

        AssertUtil.notNull(projectId, DictEnum.REPOSITORY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + projectId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpDoDelete(url, null, headers);
    }

    public ActionReturnUtil deleteRepo(String harborHost, String repo, String tag) throws Exception {
        AssertUtil.notBlank(repo, DictEnum.IMAGE_NAME);
        if(StringUtils.isBlank(tag)){
            return this.deleteRepo(harborHost,repo);
        }
        HarborRepositoryMessage repository = imageCacheManager.getRepoMessage(harborHost, repo);
        if(repository == null || CollectionUtils.isEmpty(repository.getRepositoryDetial())){
            return ActionReturnUtil.returnErrorWithData(DictEnum.IMAGE.phrase(), ErrorCodeMessage.NOT_EXIST);
        }
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/?repo_name=" + repo + "&tag=" + tag;
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        ActionReturnUtil response = HarborHttpsClientUtil.httpDoDelete(url, null, headers);
        //同一digest的镜像的不同tag版本会一并被删除，需要根据最新tag列表更新缓存
        imageCacheManager.freshRepositoryByTags(harborHost,repo);
        if (!response.isSuccess()) {
            return response;
        }
        return ActionReturnUtil.returnSuccess();

    }

    public ActionReturnUtil deleteRepo(String harborHost, String repo) throws Exception {
        AssertUtil.notBlank(repo, DictEnum.IMAGE_NAME);
        //检查镜像是否正在删除中
        String key = REDIS_KEY_IMAGE_DELETING + COLON + harborHost + SLASH + repo;
        boolean setFlag = stringRedisTemplate.opsForValue().setIfAbsent(key,repo);
        LOGGER.info("检查镜像是否正在被删除中，key：{}，in delete:{}",key,!setFlag);
        if(!setFlag){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.IMAGE_IN_DELETING);
        }
        stringRedisTemplate.expire(key, NUM_ONE, TimeUnit.HOURS);

        try {
            HarborServer harborServer = clusterService.findHarborByHost(harborHost);
            Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
            HarborRepositoryMessage repository = imageCacheManager.getRepoMessage(harborHost, repo);
            //超过50个版本数量的镜像通过异步方式删除
            if (repository!= null && repository.getRepositoryDetial() != null
                    && repository.getRepositoryDetial().size() > IMAGE_DELETE_ASYNCHRONOUS_COUNT) {
                if(!isRepoExist(harborHost,repo)){
                    return ActionReturnUtil.returnErrorWithData(DictEnum.IMAGE.phrase(),ErrorCodeMessage.NOT_EXIST);
                }
                executorService.submit(new LargeImageDeleteTask(harborServer, headers, stringRedisTemplate, imageCacheManager, repo));
                return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.LARGE_IMAGE_DELETE.phrase());
            }
            String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/?repo_name=" + repo;
            ActionReturnUtil response = HarborHttpsClientUtil.httpDoDelete(url, null, headers);
            LOGGER.info("删除镜像结束,删除redis key：{}",key);
            stringRedisTemplate.delete(key);
            if (!response.isSuccess()) {
                if(response.getData() != null && response.getData().toString().contains("Not Found")){
                    imageCacheManager.deleteRepoMessage(harborHost, repo);
                    return ActionReturnUtil.returnErrorWithData(DictEnum.IMAGE.phrase(), ErrorCodeMessage.NOT_EXIST);
                }
                LOGGER.error("删除镜像失败，repo：{},response:{} ",repo, JSONObject.toJSONString(response));
                return response;
            }
            imageCacheManager.deleteRepoMessage(harborHost, repo);
            return ActionReturnUtil.returnSuccess();
        }catch (Exception e){
            LOGGER.error("删除镜像失败，repo：{},删除redis key",repo,e);
            stringRedisTemplate.delete(key);
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DELETE_FAIL);
        }

    }

    /**
     * 检查镜像是否存在
     * @param harborHost
     * @param repo
     * @return
     * @throws Exception
     */
    private boolean isRepoExist(String harborHost, String repo) throws Exception{
        ImageRepository imageRepository = new ImageRepository();
        imageRepository.setHarborProjectName(repo.substring(0,repo.indexOf("/")));
        imageRepository.setHarborHost(harborHost);
        List<ImageRepository> repositories = harborProjectService.listRepositories(imageRepository);
        if(CollectionUtils.isEmpty(repositories)){
            return false;
        }
        ActionReturnUtil response = repoListById(harborHost,repositories.get(0).getHarborProjectId(),null,null,repo);
        if(!response.isSuccess()){
            throw new MarsRuntimeException(DictEnum.IMAGE.phrase(),ErrorCodeMessage.QUERY_FAIL);
        }
        if(response.getData() == null || CollectionUtils.isEmpty((List)response.getData())){
            imageCacheManager.deleteRepoMessage(harborHost, repo);
            return false;
        }
        return true;
    }

    /**
     * harborProject bean 转换为 map
     *
     * @param harborProject bean
     * @return map
     */
    private Map<String, Object> convertHarborProjectBeanToMap(HarborProject harborProject) {
        Map<String, Object> map = new HashMap<>();
        if (harborProject != null) {
            map.put("project_name", harborProject.getProjectName());
            map.put("quota_num", CommonConstant.QUOTA_NUM);
            if(harborProject.getQuotaSize() != null){
                map.put("quota_size", harborProject.getQuotaSize());
            }else{
                map.put("quota_size", CommonConstant.QUOTA_SIZE);
            }
            if (harborProject.getProjectId() != null) {
                map.put("project_id", harborProject.getProjectId());
            }
            if (harborProject.getUserId() != null) {
                map.put("user_id", harborProject.getUserId());
            }
            if (StringUtils.isNotEmpty(harborProject.getOwnerName())) {
                map.put("owner_name", harborProject.getOwnerName());
            }
            if (harborProject.getIsPublic() != null) {
                map.put("public", harborProject.getIsPublic());
            }
            if (harborProject.getDeleted() != null) {
                map.put("deleted", harborProject.getDeleted());
            }
            if (harborProject.getDeleted() != null) {
                map.put("deleted", harborProject.getDeleted());
            }
        }

        return map;
    }

	/*
	 * @lili
	 */

    /**
     * 查看project配额
     *
     * @return
     * @throws Exception
     */
    @Override
    public HarborProject getProjectQuota(String harborHost, String harborProjectName) throws Exception {
        AssertUtil.notBlank(harborProjectName, DictEnum.REPOSITORY);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/quotaList";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("project_name", harborProjectName);
        ActionReturnUtil quotaResponse = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        HarborProject  harborProject = null;
        if (!quotaResponse.isSuccess()) {
            LOGGER.error("查询镜像仓库配额失败，harborHost:{}, harborProjectName:{},reponse:{}",
                    new String[]{harborHost,harborProjectName},JSONObject.toJSONString(quotaResponse));
            return null;
        }
        if (quotaResponse.get("data") != null) {
            Map<String, Object> projectQuota = JsonUtil.jsonToMap(quotaResponse.get("data").toString());
            harborProject = this.convertProjectQuota(projectQuota);
            harborProject.setProjectName(harborProjectName);
        }
        return harborProject;
    }

    /**
     * 生成project配额map
     *
     * @return
     **/
    private Map<String, Object> convertHarborProjectQuotaToMap(HarborProjectQuota harborProjectQuota) {
        Map<String, Object> map = new HashMap<>();
        if (harborProjectQuota != null) {
            if (harborProjectQuota.getQuota_size() != null) {
                map.put("quota_size", harborProjectQuota.getQuota_size() * NUM_SIZE_MEMORY);
            }
            if (harborProjectQuota.getQuota_num() != null) {
                map.put("quota_num", harborProjectQuota.getQuota_num());
            }
        }

        return map;
    }

    /**
     * 更新project配额  HarborProjectQuota
     *
     * @return
     * @throws Exception
     **/
    @Override
    public ActionReturnUtil updateProjectQuota(String harborHost, HarborProjectQuota harborProjectQuota) throws Exception {
        AssertUtil.notNull(harborProjectQuota.getProject_id(), DictEnum.REPOSITORY_ID);
        AssertUtil.notNull(harborProjectQuota.getProject_name(), DictEnum.REPOSITORY);
        AssertUtil.notNull(harborProjectQuota.getQuota_size(), DictEnum.REPOSITORY_QUOTA);
        HarborProject harborProject = this.getProjectQuota(harborHost, harborProjectQuota.getProject_name());
        if(harborProject!= null && harborProject.getUseSize() > harborProjectQuota.getQuota_size()){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HARBOR_QUOTA_UPDATE_EXCEED);
        }
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectQuota.getProject_id() + "/quota";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        //设置默认值
        harborProjectQuota.setQuota_num(CommonConstant.QUOTA_NUM);
        return HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, convertHarborProjectQuotaToMap(harborProjectQuota));
    }

    /**
     * 根据projectId获取repo详情 repo+tag+domain
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getRepositoryDetailByProjectId(String harborHost, Integer projectId, Integer pageSize, Integer pageNo) throws Exception {
        AssertUtil.notNull(projectId, DictEnum.REPOSITORY_ID);
        ActionReturnUtil repoResponse = null;
        if(pageSize == null || pageNo == null) {
            repoResponse = repoListById(harborHost, projectId);
        }else{
            repoResponse = repoListById(harborHost, projectId, pageSize, pageNo,null);
        }
        if (repoResponse.isSuccess() && repoResponse.getData() != null) {
            //查询某个harbor项目下的镜像之前先根据操作日志更新缓存中的镜像
            imageCacheManager.freshRepositoryCache(harborHost, projectId);
            List<HarborRepositoryMessage> harborRepositoryList = new ArrayList<>();
            //get repository List
            List<String> repoNameList = (List<String>)repoResponse.get("data");
            if (!CollectionUtils.isEmpty(repoNameList)) {
                for (String repoName : repoNameList) {
                    if (StringUtils.isNotEmpty(repoName)) {
                        HarborRepositoryMessage harborRepository = imageCacheManager.getRepoMessage(harborHost,repoName);
                        if(harborRepository == null){
                           LOGGER.error("镜像没有获取到版本信息,harborHost:{},repoName:{}",harborHost, repoName);
                           continue;
                        }
                        harborRepositoryList.add(harborRepository);
                    }
                }
            }
            repoResponse.put("data", harborRepositoryList);
        }
        return repoResponse;
    }

    /**
     * 得到project information clair result && quota
     *
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getRepositorySummary(String harborHost, String harborProjectName) throws Exception {
        HarborProjectInfo harborProjectInfo = new HarborProjectInfo();
        harborProjectInfo.setProject_name(harborProjectName);
        HarborProject harborProject = getProjectQuota(harborHost, harborProjectName);
        if (harborProject != null) {
            harborProjectInfo.setQuota_size(harborProject.getQuotaSize());
            harborProjectInfo.setUse_rate(harborProject.getUseRate());
            harborProjectInfo.setUse_size(harborProject.getUseSize());
        }
        ActionReturnUtil clairResponse = harborSecurityService.clairStatistcsOfProject(harborHost,harborProjectName);
        if (clairResponse.isSuccess()) {
            HarborSecurityClairStatistcs harborSecurityClairStatistcs;
            if (clairResponse.get("data") != null) {
                harborSecurityClairStatistcs = (HarborSecurityClairStatistcs) clairResponse.get("data");
                harborProjectInfo.setHarborSecurityClairStatistcs(harborSecurityClairStatistcs);
                harborProjectInfo.setHarborSecurityClairStatistcs(harborSecurityClairStatistcs);
            }
        } else {
            return clairResponse;
        }
        return ActionReturnUtil.returnSuccessWithData(harborProjectInfo);
    }

    /**
     * 根据projectId获取harbor repository列表
     *
     * @param projectId id
     * @return
     * @throws Exception
     */
    public List<HarborLog> projectOperationLogs(String harborHost, Integer projectId, Integer begin, Integer end, String keywords) throws Exception {
        Assert.notNull(projectId);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/"+projectId+"/logs/filter";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("page_size", DEFAULT_PAGE_SIZE_1000);
        params.put("begin_timestamp", begin);
        params.put("end_timestamp", end);
        params.put("keywords", keywords);
        params.put("project_id", projectId);
        params.put("username", "");
        List<HarborLog> harborLogs = new ArrayList<>();
        try{
            //每次查询1000条，最多查询10次，即最多查询10000条操作日志
            for(int i=1; i<= 10; i++) {
                params.put("page", i);
                ActionReturnUtil result = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, params);
                if ((boolean) result.get("success") == true) {
                    List<HarborLog> logs = this.parseOperationLogs(result.get("data").toString());
                    harborLogs.addAll(logs);
                    //如果一页小于1000条，说明是最后一页，结束查询
                    if(logs.size() < DEFAULT_PAGE_SIZE_1000){
                        break;
                    }
                    //第10页查询也有100条
                    if(i == 10 && logs.size() == DEFAULT_PAGE_SIZE_1000){
                        LOGGER.warn("查询操作日志量太多，只返回1000条，projectId:{}",projectId);
                    }
                } else {
                    LOGGER.error("查询harbor项目的操作日志失败, result:{}", JSONObject.toJSONString(result));
                }
            }
        }catch (Exception e){
            LOGGER.error("查询harbor项目的操作日志失败, projectId:{}", projectId, e);
            return null;
        }
        return harborLogs;
    }

    /**
     * 得到harbor repository tag list
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<HarborLog> parseOperationLogs(String dataJson) throws Exception {
        if (StringUtils.isNotEmpty(dataJson)) {
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<HarborLog> harborLogs = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    HarborLog harborLog = new HarborLog();
                    harborLog.setLogId(Long.parseLong(map.get("log_id").toString()));
                    harborLog.setProjectId(Integer.parseInt(map.get("project_id").toString()));
                    harborLog.setRepoName(map.get("repo_name").toString());
                    harborLog.setRepoTag(map.get("repo_tag").toString());
                    harborLog.setOperation(map.get("operation").toString());
                    harborLog.setOperationTime(DateUtil.stringToDate(map.get("op_time").toString(),
                            DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue(),TIME_ZONE_UTC));
                    harborLog.setUserName(map.get("username").toString());
                    harborLogs.add(harborLog);
                }
                return harborLogs;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 得到harbor repository tag list
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<String> getRepoTagList(String dataJson) {
        if (StringUtils.isNotEmpty(dataJson)) {
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<String> harborRepositoryTagsList = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    if (map.get("tag") != null) {
                        harborRepositoryTagsList.add(map.get("tag").toString());
                    }

                }
                return harborRepositoryTagsList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 得到repository details;
     */
    public HarborRepositoryMessage getHarborRepositoryDetail(String harborHost, String repoName) throws Exception {
        Assert.hasText(repoName);
        HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
        List<HarborManifest> repositoryDet = new ArrayList<>();
        Date lastUpdateDate = null;
        List<String> tagLists = this.listTag(harborHost, repoName);
        if (!CollectionUtils.isEmpty(tagLists)) {
            for (String tag : tagLists) {
                ActionReturnUtil response = this.getManifestsWithVulnerabilitySum(harborHost, repoName, tag);
                if(!response.isSuccess() || response.getData() == null){
                    LOGGER.error("get tag manifest error, harborHost:{}, repoName:{}, tag, res:{}",
                            new String[]{harborHost, repoName, tag, JSONObject.toJSONString(response)});
                    continue;
                }
                HarborManifest tagDetail = (HarborManifest)response.getData();
                repositoryDet.add(tagDetail);
                //记录镜像的最后更新时间
                Date operateDate = DateUtil.stringToDate(tagDetail.getCreateTime(),
                        DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), TIME_ZONE_UTC);
                if(lastUpdateDate == null || (operateDate != null && operateDate.after(lastUpdateDate))){
                    lastUpdateDate = operateDate;
                }
            }
            repositoryDet = imageCacheManager.sort(repositoryDet);
        }
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        harborRepository.setFullNameRepo(harborHost + COLON + harborServer.getHarborPort() + "/"+repoName);
        harborRepository.setRepository(repoName);
        harborRepository.setRepositoryDetial(repositoryDet);
        harborRepository.setTags(repositoryDet.stream().map(HarborManifest::getTag).collect(Collectors.toList()));
        if(lastUpdateDate == null){
            harborRepository.setLastUpdateDate(new Date());
        }else{
            harborRepository.setLastUpdateDate(lastUpdateDate);
        }

        return harborRepository;
    }

    @Override
    public ActionReturnUtil getManifestsWithVulnerabilitySum(String harborHost, String repoName, String tag) throws Exception{
        ActionReturnUtil response = harborSecurityService.manifestsOfTag(harborHost, repoName, tag);
        if (!response.isSuccess() || response.getData() == null) {
            return response;
        }
        HarborManifest tagDetail = getHarborManifestLite(response);
        return ActionReturnUtil.returnSuccessWithData(tagDetail);

    }


    /**
     * 得到image manifest detail lite ,only show vulnerability numbers and some other details;
     */
    private HarborManifest getHarborManifestLite(ActionReturnUtil maniResponse) throws Exception {
        HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
        tagDetail.setVulnerabilityNum(-1);
        if(tagDetail.getVulnerabilitySummary() == null){
            tagDetail.setAbnormal(true);
            return tagDetail;
        }
        if(tagDetail.getVulnerabilitySummary().get("success") != null){
            tagDetail.setVulnerabilityNum(0);
        }else if(tagDetail.getVulnerabilitySummary().get("abnormal") != null){
            tagDetail.setAbnormal(true);
        }else if(tagDetail.getVulnerabilitySummary().get("notsupport") != null){
            tagDetail.setNotSupported(true);
        }else if (tagDetail.getVulnerabilitySummary().get("vulnerability") != null) {
            Map<String, Object> vulMap = (Map<String, Object>) (tagDetail.getVulnerabilitySummary().get("vulnerability"));
            if (vulMap != null && !vulMap.isEmpty()) {
                Map<String, Object> vulMapSec = (Map<String, Object>) (vulMap.get("vulnerability-suminfo"));
                if (vulMapSec != null && !vulMapSec.isEmpty()) {
                    Integer vulnerabilitySum = (Integer) (vulMapSec.get("vulnerability-sum"));
                    tagDetail.setVulnerabilityNum(vulnerabilitySum);
                }
            }
        }
        tagDetail.setVulnerabilitySummary(null);
        tagDetail.setVulnerabilitiesByPackage(null);
        return tagDetail;
    }

    /**
     * 模糊查询镜像repository
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getRepoFuzzySearch(String query, String projectId, Boolean isPublic) throws Exception {

        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId,null,isPublic, Boolean.TRUE);
        if(CollectionUtils.isEmpty(imageRepositories)){
            return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
        }
        List<String> projectList = imageRepositories.stream().map(ImageRepository::getHarborProjectName).collect(Collectors.toList());
        Set<HarborServer> harborServers = harborUserService.getUserAvailableHarbor(userService.getCurrentUsername());
        List<HarborProjectInfo> projectRepoList = new ArrayList<>();
        Long begin = System.currentTimeMillis();
        for(HarborServer harborServer : harborServers) {
            ActionReturnUtil repoResponse = getFuzzySearch(harborServer.getHarborHost(), query);
            LOGGER.info("search harbor cost:" + (System.currentTimeMillis() - begin));
            // Map<String,List<String>>repoMap= new HashMap<>();
            if (repoResponse.isSuccess()) {
                Map<String, List<String>> map = getRepositoryList(repoResponse.get("data").toString());
                Map<String, List<String>> repoListMap = new HashMap<>();
                //过滤未记录在云平台数据库中的仓库镜像
                for (String projectNameID : map.keySet()) {
                    String[] nameID = projectNameID.split(SPLIT);
                    if (nameID.length == 2) {
                        String projectName = nameID[0];
                        //Integer projectID = Integer.parseInt(nameID[1]);
                        if (projectList.contains(projectName)) {
                            repoListMap.put(projectNameID, map.get(projectNameID));
                        }
                    }
                }
                for (String projectNameID : repoListMap.keySet()) {
                    String[] nameID = projectNameID.split(SPLIT);
                    String projectName;
                    Integer projectID;
                    if (nameID.length == 2) {
                        projectName = nameID[0];
                        projectID = Integer.parseInt(nameID[1]);
                    } else {
                        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.FORMAT_ERROR,
                                DictEnum.PROJECT.phrase() + DictEnum.NAME.phrase(), true);
                    }
                    imageCacheManager.freshRepositoryCache(harborServer.getHarborHost(), projectID);
                    HarborProjectInfo projectInfo = new HarborProjectInfo();
                    List<String> repoList = repoListMap.get(projectNameID);
                    List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                    for (String repositoryName : repoList) {
                        HarborRepositoryMessage repositoryMessage = imageCacheManager.getRepoMessage(harborServer.getHarborHost(), repositoryName);
                        repositoryMessagesList.add(repositoryMessage);
                    }
                    harborProjectService.setImagePullStatus(repositoryMessagesList);
                    //查询harbor project对应数据库的镜像仓库id和对应的集群，根据harborhost和projectname只能对应一条镜像仓库记录
                    ImageRepository queryRepository = new ImageRepository();
                    queryRepository.setHarborProjectName(projectName);
                    queryRepository.setHarborHost(harborServer.getHarborHost());
                    List<ImageRepository> repositories = harborProjectService.listRepositories(queryRepository);
                    if (CollectionUtils.isEmpty(repositories)) {
                        LOGGER.error("harbor仓库名称不存在数据库中，需检查数据,harborProjectName:{}",projectName);
                        continue;
                    }
                    //如果不是admin，过滤微服务的镜像，只有admin才能看到微服务的镜像
                    if(repositories.get(0).getHarborProjectName().equalsIgnoreCase(HARBOR_PROJECT_NAME_MSF)
                            && !userService.checkCurrentUserIsAdmin()){
                        continue;
                    }
                    ImageRepository repository = repositories.get(0);
                    projectInfo.setRepositoryId(repository.getId());
                    projectInfo.setProject_name(projectName);
                    projectInfo.setProject_id(projectID);
                    projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                    projectInfo.setHarborHost(harborServer.getHarborHost());
                    if(isPublic) {
                        projectInfo.setReferredClusterNames(harborServer.getReferredClusterNames());
                    }else{
                        projectInfo.setReferredClusterNames(repository.getClusterName());
                    }
                    projectRepoList.add(projectInfo);
                }
                LOGGER.info("search image total cost:" + (System.currentTimeMillis() - begin));

            }
        }
        return ActionReturnUtil.returnSuccessWithData(projectRepoList);
    }

    /**
     * 搜索镜像
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getFuzzySearch(String harborHost, String query) throws Exception {
        AssertUtil.notBlank(query, DictEnum.QUERY);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/search";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
    }

    private Map<String, List<String>> getRepositoryList(String dataJson) {
        if (StringUtils.isNotEmpty(dataJson)) {
            //key值为project的name*id;
            Map<String, List<String>> queryResult = new HashMap<>();
            Map<String, Object> queryMap = JsonUtil.jsonToMap(dataJson);
            if (queryMap.get("repository") != null) {
                //List<Map<String, Object>> mapList = JsonUtil.JsonToMapList((queryMap.get("repository").toString()));
                @SuppressWarnings("unchecked")
				List<Map<String, Object>> mapList = (List<Map<String, Object>>) queryMap.get("repository");
                if (!CollectionUtils.isEmpty(mapList)) {
                    for (Map<String, Object> map : mapList) {
                        if (map.get("project_name") != null) {
                            String projectNameId =map.get("project_name").toString()+SPLIT+map.get("project_id").toString();
                            if (queryResult.containsKey(projectNameId)){
                                queryResult.get(projectNameId).add(map.get("repository_name").toString());
                            } else {
                                List<String> repoList = new ArrayList<>();
                                repoList.add(map.get("repository_name").toString());
                                queryResult.put(projectNameId, repoList);
                            }
                        }
                    }
                }
            }

            return queryResult;
        }
        return null;
    }


    /**
     * 查询指定项目的镜像详细信息
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listImageDetail(String projectId) throws Exception {
        //获取项目的projectList
        List<ImageRepository> imageRepositories= harborProjectService.listRepositories(projectId,null,null,Boolean.TRUE);
        List<HarborProjectInfo> projectRepoList = new ArrayList<>();
        //获取project的repositoryList
        for (ImageRepository imageRepository : imageRepositories) {
            ActionReturnUtil repoResponse = repoListById(imageRepository.getHarborHost(), imageRepository.getHarborProjectId());
            if (repoResponse.isSuccess()) {
                List<String> repoList = (List<String>)repoResponse.get("data");
                HarborProjectInfo projectInfo = new HarborProjectInfo();
                List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                //获取repository的tagList
                for (String repoName : repoList) {
                    HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
                    harborRepository.setRepository(repoName);
                    harborRepository.setTags(this.listTag(imageRepository.getHarborHost(), repoName));
                    repositoryMessagesList.add(harborRepository);
                }
                projectInfo.setProject_name(imageRepository.getHarborProjectName());
                projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                projectRepoList.add(projectInfo);
            } else {
                return repoResponse;
            }
        }
       return ActionReturnUtil.returnSuccessWithData(projectRepoList);
    }

    public List<String> listTag(String harborHost, String repoName) throws Exception{
        ActionReturnUtil tagResponse = getTagsByRepoName(harborHost, repoName);
        if ( tagResponse.isSuccess()) {
            if(tagResponse.getData() != null) {
                return getRepoTagList(tagResponse.get("data").toString());
            }else{
                return Collections.emptyList();
            }
        }else{
            LOGGER.error("listTag错误,harborHost:{},repoName:{}，response:{}",new String[]{harborHost, repoName, JSONObject.toJSONString(tagResponse)});
            throw new MarsRuntimeException(DictEnum.IMAGE_TAG.phrase(),ErrorCodeMessage.QUERY_FAIL);
        }
    }

    /**
     * 查询指定项目下的默认第一个镜像
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getFirstImage(String projectId, String clusterId, String harborProjectName, String repoName) throws Exception {
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId, clusterId, null,Boolean.TRUE);
        if(CollectionUtils.isEmpty(imageRepositories)){
            return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
        }
        List<HarborProjectInfo> projectRepoList = new ArrayList<>();
        List<HarborProjectInfo> projectNonRepoList = new ArrayList<>();
        boolean hasSetRepo = false;
        for(int i=0; i<imageRepositories.size(); i++) {
            ImageRepository repository = imageRepositories.get(i);
            HarborProjectInfo projectInfo = new HarborProjectInfo();
            projectInfo.setProject_name(repository.getHarborProjectName());
            //只查询具体某一个project的镜像信息，非该project的不查询repo信息
            if (StringUtils.isNotBlank(harborProjectName) &&
                    !harborProjectName.equalsIgnoreCase(repository.getHarborProjectName())) {
                projectRepoList.add(projectInfo);
                continue;
            }
            //已经查到有repo的project， 其他project不再继续查询project下面的repo
            if(hasSetRepo){
                projectRepoList.add(projectInfo);
                continue;
            }
            imageCacheManager.freshRepositoryCache(repository.getHarborHost(),
                    Integer.parseInt(repository.getHarborProjectId().toString()));
            ActionReturnUtil repoResponse = repoListById(repository.getHarborHost(),
                    Integer.parseInt(repository.getHarborProjectId().toString()));
            if (repoResponse.isSuccess()) {
                List<String> repoList = (List<String>)repoResponse.get("data");
                if (CollectionUtils.isEmpty(repoList)) {
                    projectNonRepoList.add(projectInfo);
                    continue;
                }
                projectRepoList.add(projectInfo);
                List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                //如果查询参数没有指定某个具体的repo镜像名，则查询第一个repo的tag信息，其他repo的不查询tag信息
                if(StringUtils.isBlank(repoName)){
                    repoName = repoList.get(0);
                }
                for(int j=0; j<repoList.size(); j++) {
                    String repositoryName = repoList.get(j);
                    HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
                    harborRepository.setRepository(repositoryName);
                    if(repositoryName.equals(repoName)) {
                        harborRepository = imageCacheManager.getRepoMessage(repository.getHarborHost(),repoName);
                        if(CollectionUtils.isEmpty(harborRepository.getTags()) && !CollectionUtils.isEmpty(harborRepository.getRepositoryDetial())){
                            harborRepository.setTags(harborRepository.getRepositoryDetial().stream().map(manifest -> manifest.getTag()).collect(Collectors.toList()));
                        }
                        if(harborRepository == null){
                            LOGGER.error("镜像没有获取到版本信息,harborHost:{},repoName:{}",repository.getHarborHost(), repoName);
                            continue;
                        }
                        hasSetRepo = true;
                    }
                    repositoryMessagesList.add(harborRepository);
                }
                projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
            } else {
                LOGGER.error("repoListById error. projectId:{},message:{}",
                        repository.getHarborProjectId(), JSONObject.toJSONString(repoResponse));
                return repoResponse;
            }
        }
        //将没有repo的project放在project列表的下面
        projectRepoList.addAll(projectNonRepoList);
        return ActionReturnUtil.returnSuccessWithData(projectRepoList);
    }
    
    /**
     * 查询指定项目的镜像
     *
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getImagesByProjectId(String projectId, String clusterId) throws Exception {
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId,clusterId,null,Boolean.TRUE);
        List<HarborProjectInfo> projectRepoList = new ArrayList<>();
        //获取project的repositoryList
        for (ImageRepository repository : imageRepositories) {
            ActionReturnUtil repoResponse = repoListById(repository.getHarborHost(),
                    Integer.parseInt(repository.getHarborProjectId().toString()));
            if (repoResponse.isSuccess()) {
                List<String> repoList = (List<String>)repoResponse.get("data");
                HarborProjectInfo projectInfo = new HarborProjectInfo();
                List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                //获取repository的tagList
                for (String repositoryName : repoList) {
                    HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
                    harborRepository.setRepository(repositoryName);
                    repositoryMessagesList.add(harborRepository);
                }
                projectInfo.setProject_name(repository.getHarborProjectName());
                projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                projectRepoList.add(projectInfo);
            } 
        }
       return ActionReturnUtil.returnSuccessWithData(projectRepoList);
    }

    @Override
    public boolean syncRegistry(String harborHost) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/internal/syncregistry";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, params);
        if(!response.isSuccess()){
            LOGGER.error("sync registry error. harborHost:{},res:{}", harborHost, JSONObject.toJSONString(response));
            return false;
        }
        return true;
    }


    /**
     * 得到harbor project response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborProject getHarborProjectResp(String dataJson) throws Exception{
        HarborProject harborProject = new HarborProject();
        if (StringUtils.isNotEmpty(dataJson)) {
            Map<String, Object> map = JsonUtil.jsonToMap(dataJson);
            if (map != null) {
                if (map.get("name") != null) {
                    harborProject.setProjectName(map.get("name").toString());
                }
                if (map.get("project_id") != null) {
                    harborProject.setProjectId(Integer.parseInt(map.get("project_id").toString()));
                }
                if (map.get("creation_time") != null) {
                    harborProject.setCreateTime(map.get("creation_time").toString());
                }
            }
        }
        return harborProject;
    }

    /**
     * 得到harbor project response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<HarborProject> getHarborProjectList(String dataJson) throws Exception{
        List<HarborProject> harborProjects = new ArrayList<>();
        if (StringUtils.isNotBlank(dataJson)) {
            List<Map<String, Object>> list = JsonUtil.JsonToMapList(dataJson);
            if(!CollectionUtils.isEmpty(list)) {
                for(Map<String, Object> map : list) {
                    if (map != null) {
                        Integer deleted = Integer.parseInt(map.get("deleted").toString());
                        if(deleted == FLAG_TRUE){
                            continue;
                        }
                        HarborProject harborProject = this.convertProjectQuota(map);
                        harborProject.setProjectName(map.get("name").toString());
                        harborProject.setProjectId(Integer.parseInt(map.get("project_id").toString()));
                        harborProject.setIsPublic(Integer.parseInt(map.get("public").toString()));
                        harborProject.setRepoCount(Integer.parseInt(map.get("repo_count").toString()));
                        harborProject.setCreateTime(map.get("creation_time").toString());
                        harborProjects.add(harborProject);
                    }

                }
            }
        }
        return harborProjects;
    }

    private HarborProject convertProjectQuota(Map<String, Object> projectQuota){
        HarborProject harborProject = new HarborProject();
        if (projectQuota.get("quota_size") != null) {
            Float quotaSize = Float.parseFloat(projectQuota.get("quota_size").toString())/NUM_SIZE_MEMORY;
            harborProject.setQuotaSize(new BigDecimal(quotaSize).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue());
        }
        if (projectQuota.get("use_size") != null) {
            Float useSize = Float.parseFloat(projectQuota.get("use_size").toString())/NUM_SIZE_MEMORY;
            //最后一个镜像上传之前不能控制容量，需要上传之后才能计算，如果最后一个镜像上传之后大于quota_size,则useSize显示quotaSize
            useSize = new BigDecimal(useSize).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue();
            if(useSize > harborProject.getQuotaSize()) {
                harborProject.setUseSize(harborProject.getQuotaSize());
            }else{
                harborProject.setUseSize(useSize);
            }
        }
        if(harborProject.getQuotaSize() > 0){
            Float usageRate = harborProject.getUseSize() / harborProject.getQuotaSize() * PERCENT_HUNDRED;
            harborProject.setUseRate(new BigDecimal(usageRate).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue());
        }
        return harborProject;
    }

}
