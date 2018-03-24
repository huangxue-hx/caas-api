package com.harmonycloud.service.platform.serviceImpl.harbor;

import java.math.BigDecimal;
import java.util.*;
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
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.service.harbor.*;
import com.harmonycloud.service.tenant.TenantService;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.harmonycloud.service.platform.client.HarborClient;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.DEFAULT_PAGE_SIZE;
import static com.harmonycloud.service.platform.constant.Constant.TIME_ZONE_UTC;

/**
 * Created by zsl on 2017/1/18.
 */
@Service
public class HarborServiceImpl implements HarborService {
    private static final Logger LOGGER = LoggerFactory.getLogger(HarborServiceImpl.class);
    @Autowired
    TenantService tenantService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private HarborSecurityService harborSecurityService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    HarborUserService harborUserService;
    @Autowired
    HarborReplicationService harborReplicationService;
    @Autowired
    HarborImageCleanService harborImageCleanService;
    @Autowired
    UserService userService;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    ImageCacheManager imageCacheManager;
    //harbor仓库名称与数据库镜像仓库id的对应关系
    private static Map<String, Integer> harborProjectMap = new HashMap<>();

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
        for(ImageRepository repository : imageRepositories){
            HarborProject harborProject = imageCacheManager.getHarborProject(harborHost,repository.getHarborProjectName());
            if(harborProject != null){
                repository.setQuotaSize(harborProject.getQuotaSize());
                repository.setUsageRate(harborProject.getUseRate());
                repository.setUsageSize(harborProject.getUseSize());
            }
        }
        if(!CollectionUtils.isEmpty(imageRepositories)) {
            Map<Integer, String> projectNameMap = imageRepositories.stream()
                    .collect(Collectors.toMap(ImageRepository::getId, repo -> repo.getHarborProjectName()));
            List<HarborPolicyDetail> replications = harborReplicationService.listPolicies(harborHost);
            List<Integer> harborProjectIds = imageRepositories.stream().map(ImageRepository::getHarborProjectId).collect(Collectors.toList());
            replications = replications.stream().filter(policy -> harborProjectIds.contains(policy.getProject_id())).collect(Collectors.toList());
            List<Integer> repositoryIds = imageRepositories.stream().map(ImageRepository::getId).collect(Collectors.toList());
            List<ImageCleanRule> imageCleanRules = harborImageCleanService.listByIds(repositoryIds);
            for(ImageCleanRule imageCleanRule : imageCleanRules){
                imageCleanRule.setHarborProjectName(projectNameMap.get(imageCleanRule.getRepositoryId()));
            }
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
    public List<HarborProject> listProject(String harborHost, Integer page, Integer pageSize) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? DEFAULT_PAGE_SIZE : pageSize;
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("page_size", pageSize);
        ActionReturnUtil response = HttpsClientUtil.httpGetRequest(url, headers, params);
        if(response.isSuccess() && response.getData() != null){
            return getHarborProjectList(response.getData().toString());
        }
        return Collections.emptyList();
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

        ActionReturnUtil response = HttpsClientUtil.httpGetRequest(url, headers, null);
        if (response.isSuccess() && response.get("data") != null) {
            return getHarborProjectResp(response.get("data").toString());
        }
        return null;
    }

    /**
     * 根据projectId获取harbor repository列表
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
            ActionReturnUtil response = HttpsClientUtil.httpGetRequest(url, headers, params);
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
                //如果是未授权，可能是高可用harbor一台已经挂了，切换到另一台需要再重新登录
                if(response.getData() != null && response.getData().toString().contains("Unauthorized")){
                    LOGGER.warn("cookie 已经失效:harborServer{}", harborHost);
                    HarborClient.clearCookie(harborHost);
                }
                LOGGER.error("查询镜像repo list失败,harborHost:{},response:{}",harborHost, JSONObject.toJSONString(response));
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
            }
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

        return HttpsClientUtil.httpGetRequest(url, headers, params);
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

        return HttpsClientUtil.httpGetRequest(url, headers, params);
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

        return HttpsClientUtil.httpPostRequestForHarborCreate(url, headers, convertHarborProjectBeanToMap(harborProject));
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

        return HttpsClientUtil.httpDoDelete(url, null, headers);
    }

    public ActionReturnUtil deleteRepo(String harborHost, String repo, String tag) throws Exception {
        AssertUtil.notBlank(repo, DictEnum.IMAGE_NAME);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/?repo_name=" + repo;
        if (StringUtils.isNotBlank(tag)) {
            url = url + "&tag=" + tag;
        }

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HttpsClientUtil.httpDoDelete(url, null, headers);
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
        ActionReturnUtil quotaResponse = HttpsClientUtil.httpGetRequest(url, headers, params);
        HarborProject  harborProject = new HarborProject();
        harborProject.setProjectName(harborProjectName);
        if (!quotaResponse.isSuccess()) {
            LOGGER.error("查询镜像仓库配额失败，harborHost:{}, harborProjectName:{},reponse:{}",
                    new String[]{harborHost,harborProjectName},JSONObject.toJSONString(quotaResponse));
            return null;
        }
        if (quotaResponse.get("data") != null) {
            Map<String, Object> projectQuota = JsonUtil.jsonToMap(quotaResponse.get("data").toString());
            if (projectQuota.get("quota_size") != null) {
                Float quotaSize = Float.parseFloat(projectQuota.get("quota_size").toString())/NUM_SIZE_MEMORY;
                harborProject.setQuotaSize(new BigDecimal(quotaSize).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue());
            }
            if (projectQuota.get("use_size") != null) {
                Float useSize = Float.parseFloat(projectQuota.get("use_size").toString())/NUM_SIZE_MEMORY;
                harborProject.setUseSize(new BigDecimal(useSize).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue());
            }
            if (projectQuota.get("use_rate") != null) {
                Float useRate = Float.parseFloat(projectQuota.get("use_rate").toString()) * PERCENT_HUNDRED;
                harborProject.setUseRate(new BigDecimal(useRate).setScale(ROUND_SCALE_2, BigDecimal.ROUND_HALF_UP).floatValue());
            }
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
        return HttpsClientUtil.httpPostRequestForHarbor(url, headers, convertHarborProjectQuotaToMap(harborProjectQuota));
    }

    /**
     * 根据projectId获取repo详情 repo+tag+domain
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getRepositoryDetailByProjectId(String harborHost, Integer projectId) throws Exception {
        AssertUtil.notNull(projectId, DictEnum.REPOSITORY_ID);
        ActionReturnUtil repoResponse = repoListById(harborHost, projectId);
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
        params.put("page_size", DEFAULT_PAGE_SIZE);
        params.put("begin_timestamp", begin);
        params.put("end_timestamp", end);
        params.put("keywords", keywords);
        params.put("project_id", projectId);
        params.put("username", "");
        List<HarborLog> harborLogs = new ArrayList<>();
        try{
            //每次查询100条，最多查询10次，即最多查询1000条操作日志
            for(int i=1; i<= 10; i++) {
                params.put("page", i);
                ActionReturnUtil result = HttpsClientUtil.httpPostRequestForHarbor(url, headers, params);
                if ((boolean) result.get("success") == true) {
                    List<HarborLog> logs = this.parseOperationLogs(result.get("data").toString());
                    harborLogs.addAll(logs);
                    //如果一页小于100条，说明是最后一页，结束查询
                    if(logs.size() < DEFAULT_PAGE_SIZE){
                        break;
                    }
                    //第10页查询也有100条
                    if(i == 10 && logs.size() == DEFAULT_PAGE_SIZE){
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
                    harborLog.setProjectId(Integer.parseInt(map.get("project_id").toString()));
                    harborLog.setRepoName(map.get("repo_name").toString());
                    harborLog.setRepoTag(map.get("repo_tag").toString());
                    harborLog.setOperation(map.get("operation").toString());
                    harborLog.setOperationTime(map.get("op_time").toString());
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
    private List<HarborRepositoryTags> getRepoTagList(String dataJson) throws Exception {
        if (StringUtils.isNotEmpty(dataJson)) {
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<HarborRepositoryTags> harborRepositoryTagsList = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    HarborRepositoryTags harborRepositoryTags = new HarborRepositoryTags();
                    if (map.get("tag") != null) {
                        harborRepositoryTags.setTag(map.get("tag").toString());
                    }
                    harborRepositoryTagsList.add(harborRepositoryTags);
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
        //get tag list
        List<HarborRepositoryTags> tagLists = new ArrayList<>();
        ActionReturnUtil tagResponse = getTagsByRepoName(harborHost, repoName);
        List<HarborManifest> repositoryDet = new ArrayList<>();
        Date lastUpdateDate = null;
        if (!tagResponse.isSuccess() || tagResponse.getData() == null) {
            LOGGER.error("get tags error, harborHost:{}, repoName:{}, res:{}",
                    new String[]{harborHost, repoName,JSONObject.toJSONString(tagResponse)});
            //如果是未授权，可能是高可用harbor一台已经挂了，切换到另一台需要再重新登录
            if(tagResponse.getData() != null && tagResponse.getData().toString().contains("Unauthorized")){
                LOGGER.warn("cookie 已经失效:harborServer{}", harborHost);
                HarborClient.clearCookie(harborHost);
            }
            return null;
        }
        tagLists = getRepoTagList(tagResponse.get("data").toString());
        if (!CollectionUtils.isEmpty(tagLists)) {
            for (int i = 0; i < tagLists.size(); i++) {
                String tag = tagLists.get(i).getTag();
                //get tag detail
                ActionReturnUtil maniResponse = harborSecurityService.manifestsOfTag(harborHost, repoName, tag);
                if ((boolean) maniResponse.get("success") == true) {
                    // HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
                    if (maniResponse.get("data") != null) {
                        HarborManifest tagDetail = getHarborManifestLite(maniResponse);
                        repositoryDet.add(tagDetail);
                        //记录镜像的最后更新时间
                        Date operateDate = DateUtil.stringToDate(tagDetail.getCreateTime(),
                                DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), TIME_ZONE_UTC);
                        if(lastUpdateDate == null || (operateDate != null && operateDate.after(lastUpdateDate))){
                            lastUpdateDate = operateDate;
                        }
                    }
                }
            }
            repositoryDet.sort((manifest1, manifest2) -> manifest2.getCreateTime().compareTo(manifest1.getCreateTime()));

        }
        harborRepository.setFullNameRepo(harborHost+"/"+repoName);
        harborRepository.setRepository(repoName);
        harborRepository.setRepositoryDetial(repositoryDet);
        if(lastUpdateDate == null){
            harborRepository.setLastUpdateDate(new Date());
        }else{
            harborRepository.setLastUpdateDate(lastUpdateDate);
        }

        return harborRepository;
    }


    /**
     * 得到image manifest detail lite ,only show vulnerability numbers and some other details;
     */
    private HarborManifest getHarborManifestLite(ActionReturnUtil maniResponse) throws Exception {
        HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
        tagDetail.setVulnerabilityNum(-1);
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
                    if(vulnerabilitySum == 0 && (Integer) (vulMapSec.get("vulnerability-patches-sum")) > 0){
                        tagDetail.setAbnormal(true);
                    }else{
                        tagDetail.setVulnerabilityNum(vulnerabilitySum);
                    }
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

        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId,null,isPublic);
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
                    if(harborProjectMap.get(harborServer.getHarborHost() + SLASH + projectName) == null) {
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
                        projectInfo.setReferredClusterNames(repositories.get(0).getClusterName());
                        harborProjectMap.put(harborServer.getHarborHost() + SLASH + projectName, repositories.get(0).getId());
                    }
                    projectInfo.setRepositoryId(harborProjectMap.get(harborServer.getHarborHost() + SLASH + projectName));
                    projectInfo.setProject_name(projectName);
                    projectInfo.setProject_id(projectID);
                    projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                    projectInfo.setHarborHost(harborServer.getHarborHost());
                    if(isPublic) {
                        projectInfo.setReferredClusterNames(harborServer.getReferredClusterNames());
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
        return HttpsClientUtil.httpGetRequest(url, headers, params);
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
        List<ImageRepository> imageRepositories= harborProjectService.listRepositories(projectId,null,null);
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
                    //HarborRepositoryMessage repositoryMessage = getHarborRepositoryDetail(repositoryName);
                    HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
                    List<HarborRepositoryTags> tagLists = new ArrayList<>();
                    ActionReturnUtil tagResponse = getTagsByRepoName(imageRepository.getHarborHost(), repoName);
                    List<String> tagList = new ArrayList<>();
                    if ((boolean) tagResponse.get("success") == true) {
                        if (tagResponse.get("data") != null) {
                            tagLists = getRepoTagList(tagResponse.get("data").toString());
                        }
                        if (!CollectionUtils.isEmpty(tagLists)) {
                            for (int i = 0; i < tagLists.size(); i++) {
                                String tag = tagLists.get(i).getTag();
                                tagList.add(tag);
                            }
                        }
                        harborRepository.setRepository(repoName);
                        harborRepository.setTags(tagList);
                    }else{
                        return tagResponse;
                    }
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

    /**
     * 查询指定项目下的默认第一个镜像
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getFirstImage(String projectId, String clusterId, String harborProjectName, String repoName) throws Exception {
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId, clusterId, null);
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
                        ActionReturnUtil tagResponse = getTagsByRepoName(repository.getHarborHost(), repositoryName);
                        if ((boolean) tagResponse.get("success") == true) {
                            if (tagResponse.get("data") != null) {
                                List<HarborRepositoryTags> tags = getRepoTagList(tagResponse.get("data").toString());
                                List<String> tagNames = tags.stream()
                                        .map(HarborRepositoryTags::getTag).collect(Collectors.toList());
                                harborRepository.setTags(tagNames);
                            }
                        } else {
                            LOGGER.error("getTagsByRepoName error. repositoryName:{}, message:{}",
                                    repositoryName, JSONObject.toJSONString(tagResponse));
                            return tagResponse;
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
        List<ImageRepository> imageRepositories = harborProjectService.listRepositories(projectId,clusterId,null);
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
                        HarborProject harborProject = new HarborProject();
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

}
