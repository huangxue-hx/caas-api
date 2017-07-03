package com.harmonycloud.service.platform.serviceImpl.harbor;

import java.util.*;
import java.util.Map.Entry;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.service.tenant.TenantService;
import net.sf.json.JSONObject;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.service.platform.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import org.springframework.util.CollectionUtils;
import com.harmonycloud.service.platform.bean.HarborManifest;
//import com.harmonycloud.service.platform.bean.HarborManifest;
import com.harmonycloud.service.platform.integrationService.HarborIntegrationService;
import com.harmonycloud.dao.tenant.HarborProjectTenantMapper;
/**
 * Created by zsl on 2017/1/18.
 */
@Service
public class HarborServiceImpl implements HarborService {

    @Autowired
    HarborUtil harborUtil;
    @Autowired
    TenantService tenantService;
    @Autowired
    private RoleBindingService roleBindingService;

    @Autowired
    private HarborIntegrationService harborIntegrationService;

    @Autowired
    private HarborProjectTenantMapper harborProjectTenantMapper;

    private static String SPLIT = "#@#";

    /**
     * harbor 登录接口
     *
     * @param username 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil login(String username, String password) throws Exception {
        if (StringUtils.isEmpty(username)) {
            return ActionReturnUtil.returnErrorWithMsg("username cannot be null");
        }
        if (StringUtils.isEmpty(password)) {
            return ActionReturnUtil.returnErrorWithMsg("password cannot be null");
        }

        String url = HarborClient.getPrefix() + "/login";

        Map<String, Object> params = new HashMap<>();
        params.put("principal", username);
        params.put("password", password);

        String cookie;
        try {
            CloseableHttpResponse response = HttpClientUtil.doPostWithLogin(url, params, null);
            cookie = response.getHeaders("Set-Cookie")[0].getValue().split(";")[0];
            CookieInfo.add("cookie", cookie);
        } catch (Exception e) {
            return ActionReturnUtil.returnErrorWithMsg(e.getMessage());
        }
        return ActionReturnUtil.returnSuccessWithData(cookie);
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
    public ActionReturnUtil projectList(Integer page, Integer pageSize) throws Exception {
        page = (page == null || page < 1) ? 1 : page;
        pageSize = (pageSize == null || pageSize < 1) ? 100 : pageSize;

        String url = HarborClient.getPrefix() + "/api/projects";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("page_size", pageSize);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 根据projectId获取harbor project详情
     *
     * @param projectId id
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getProjectById(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId;

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpGetRequest(url, headers, null);
    }

    /**
     * 根据projectId获取harbor repository列表
     *
     * @param projectId id
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil repoListById(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/repositories";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("project_id", projectId);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 根据repository name获取tags
     *
     * @param repoName repoName
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getTagsByRepoName(String repoName) throws Exception {
        if (StringUtils.isEmpty(repoName)) {
            return ActionReturnUtil.returnErrorWithMsg("repoName cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/repositories/tags";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);

        return HttpClientUtil.httpGetRequest(url, headers, params);
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
    public ActionReturnUtil getManifests(String repoName, String tag) throws Exception {
        if (StringUtils.isEmpty(repoName)) {
            return ActionReturnUtil.returnErrorWithMsg("repoName cannot be null");
        }
        if (StringUtils.isEmpty(tag)) {
            return ActionReturnUtil.returnErrorWithMsg("tag cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/repositories/manifests";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        Map<String, Object> params = new HashMap<>();
        params.put("repo_name", repoName);
        params.put("tag", tag);

        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 创建harbor project
     *
     * @param harborProject bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createProject(HarborProject harborProject) throws Exception {
        if (harborProject == null) {
            return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/projects";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpPostRequestForHarborCreate(url, headers, convertHarborProjectBeanToMap(harborProject));
    }

    /**
     * 删除harbor project
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteProject(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectId;

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpDoDelete(url, null, headers);
    }

    public ActionReturnUtil deleteRepo(String repo, String tag) throws Exception {
        if (StringUtils.isEmpty(repo)) {
            return ActionReturnUtil.returnErrorWithMsg("repo cannot be null");
        }
        String url = HarborClient.getPrefix() + "/api/repositories/?repo_name=" + repo;
        if (!StringUtils.isEmpty(tag)) {
            url = url + "&tag=" + tag;
        }

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpDoDelete(url, null, headers);
    }

    @Override
    public ActionReturnUtil listProvider() throws Exception {
        List<ProviderPlugin> provider = new ArrayList<ProviderPlugin>();
        ProviderPlugin providerPlugin = new ProviderPlugin();
        providerPlugin.setIp(HarborClient.getProvider());
        providerPlugin.setName(Constant.HARBOR);
        providerPlugin.setVersion(harborUtil.getHarborVersion());
        provider.add(providerPlugin);
        return ActionReturnUtil.returnSuccessWithData(provider);
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
            if (harborProject.getProjectId() != null) {
                map.put("project_id", harborProject.getProjectId());
            }
            if (StringUtils.isNotEmpty(harborProject.getProjectName())) {
                map.put("project_name", harborProject.getProjectName());
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
        }

        return map;
    }


    @Override
    public ActionReturnUtil getProjectByUser(String username) throws Exception {
        String label = "nephele_user_" + username + "=" + username;
        List<UserProjectDto> userProjectList = new ArrayList<>();
        K8SClientResponse response = this.roleBindingService.getRolebindingListbyLabelSelector(label);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            RoleBindingList roleBindingList = K8SClient.converToBean(response, RoleBindingList.class);
            List<RoleBinding> items = roleBindingList.getItems();
            for (RoleBinding roleBinding : items) {
                if (roleBinding.getMetadata().getName().contains("harbor")) {
                    Map<String, Object> annotations = roleBinding.getMetadata().getAnnotations();
                    //设置返回值
                    if (annotations != null) {
                        UserProjectDto userProjectDto = new UserProjectDto();
                        Integer project = Integer.valueOf(annotations.get("project").toString());
                        userProjectDto.setProject(project);
                        //根据project-id查询project-name
                        ActionReturnUtil projectDetail = this.getProjectById(project);
                        //返回json有关键字,修改关键字
                        String data = (String) projectDetail.get("data");
                        String result = data.replaceAll("public", "isPublic");
                        String newRes = result.replaceAll("Togglable", "togglable");
                        HarborProjectDetail list = JsonUtil.jsonToPojo(newRes, HarborProjectDetail.class);
                        if (list != null) {
                            userProjectDto.setProjectName(list.getName());
                        }
                        userProjectDto.setRole(roleBinding.getRoleRef().getName());
                        userProjectDto.setRoleBindingName(roleBinding.getMetadata().getName());
                        Map<String, Object> labels = roleBinding.getMetadata().getLabels();
                        Set<Entry<String, Object>> entrySet = labels.entrySet();
                        for (Entry<String, Object> entry : entrySet) {
                            if (entry.getKey().contains("nephele_tenant_")) {
                                userProjectDto.setTenantName(String.valueOf(entry.getValue()));
                            }
                            if (entry.getKey().contains("nephele_tenantid")) {
                                userProjectDto.setTenantid(String.valueOf(entry.getValue()));
                            }
                        }
                        userProjectList.add(userProjectDto);
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(userProjectList);
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
    public ActionReturnUtil getProjectQuota(String projectname) throws Exception {
        if (StringUtils.isEmpty(projectname)) {
            return ActionReturnUtil.returnErrorWithMsg("project name cannot be null");
        }
        //String url = HarborClient.getPrefix() + "/api/projects/quotaList?project_name="+projectname;
        String url = HarborClient.getPrefix() + "/api/projects/quotaList";
        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());
        Map<String, Object> params = new HashMap<>();
        params.put("project_name", projectname);
        return HttpClientUtil.httpGetRequest(url, headers, params);
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
                map.put("quota_size", harborProjectQuota.getQuota_size());
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
    public ActionReturnUtil updateProjectQuota(Integer projectID, HarborProjectQuota harborProjectQuota) throws Exception {

        if (projectID == null || projectID < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectID is invalid");
        }
        if (harborProjectQuota.getQuota_size() == null) {
            return ActionReturnUtil.returnErrorWithMsg("project quota cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/projects/" + projectID + "/quota";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());
        //设置默认值
        harborProjectQuota.setQuota_num(CommonConstant.QUOTA_NUM);
        return HttpClientUtil.httpPostRequestForHarbor(url, headers, convertHarborProjectQuotaToMap(harborProjectQuota));
    }

    /**
     * 根据projectId获取repo详情 repo+tag+domain
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getRepositoryDetailByProjectId(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        ActionReturnUtil repoResponse = repoListById(projectId);
        if ((boolean) repoResponse.get("success") == true) {
            repoResponse.put("data", getHarborRepositoryDetail(repoResponse));
        } else {
            return repoResponse;
        }
        return repoResponse;
    }

    /**
     * 得到harbor repository detail
     *
     * @param repoResponse repo response
     * @return
     * @throws Exception
     */
    private List<HarborRepositoryMessage> getHarborRepositoryDetail(ActionReturnUtil repoResponse) throws Exception {
        if (repoResponse.get("data") != null) {
            //get repository List
            List<String> repoNameList = this.getHarborRepoNameList(repoResponse.get("data").toString());
            List<HarborRepositoryMessage> harborRepositoryList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(repoNameList)) {
                for (String repoName : repoNameList) {
                    if (StringUtils.isNotEmpty(repoName)) {
                        HarborRepositoryMessage harborRepository = getHarborRepositoryDetail(repoName);
                        harborRepositoryList.add(harborRepository);
                    }
                }
            }
            return harborRepositoryList;
        }
        return Collections.emptyList();
    }

    /**
     * 得到harbor repository name list
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<String> getHarborRepoNameList(String dataJson) throws Exception {
        if (StringUtils.isNotEmpty(dataJson)) {
            List<String> repoNameList = JsonUtil.jsonToList(dataJson, String.class);
            if (!CollectionUtils.isEmpty(repoNameList)) {
                return repoNameList;
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
    private HarborRepositoryMessage getHarborRepositoryDetail(String repoName) throws Exception {
        HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
        //get tag list
        List<HarborRepositoryTags> tagLists = new ArrayList<>();
        ActionReturnUtil tagResponse = getTagsByRepoName(repoName);
        List<HarborManifest> repositoryDet = new ArrayList<>();
        if ((boolean) tagResponse.get("success") == true) {
            if (tagResponse.get("data") != null) {
                tagLists = getRepoTagList(tagResponse.get("data").toString());
            }
            if (!CollectionUtils.isEmpty(tagLists)) {
                for (int i = 0; i < tagLists.size(); i++) {
                    String tag = tagLists.get(i).getTag();
                    //get tag detail
                    ActionReturnUtil maniResponse = harborIntegrationService.manifestsOfTag(repoName, tag);
                    if ((boolean) maniResponse.get("success") == true) {
                        // HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
                        if (maniResponse.get("data") != null) {
                            HarborManifest tagDetail = getHarborManifestLite(maniResponse);
                            repositoryDet.add(tagDetail);
                        }
                    }
                }

            }
            String url = HarborClient.getPrefix();
            String[] harborID = url.split("://");
            if (harborID.length ==2 ){
                String[] harborIDPort = harborID[1].split(":");
                if(harborIDPort.length == 2){
                    harborRepository.setFullNameRepo(harborIDPort[0]+"/"+repoName);
                }

            }
            harborRepository.setRepository(repoName);
            harborRepository.setRepositoryDetial(repositoryDet);
        }
        return harborRepository;
    }

    /**
     * 得到image manifest detail lite ,only show vulnerability numbers and some other details;
     */
    private HarborManifest getHarborManifestLite(ActionReturnUtil maniResponse) throws Exception {
        HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
        tagDetail.setVulnerabilityNum(0);
        if (tagDetail.getVulnerabilitySummary().get("vulnerability") != null) {
            Map<String, Object> vulMap = (Map<String, Object>) (tagDetail.getVulnerabilitySummary().get("vulnerability"));
            if (!vulMap.isEmpty()) {
                Map<String, Object> vulMapSec = (Map<String, Object>) (vulMap.get("vulnerability-suminfo"));
                if (!vulMapSec.isEmpty()) {
                    tagDetail.setVulnerabilityNum((Integer) (vulMapSec.get("vulnerability-sum")));
                }
            }
        }
        tagDetail.setVulnerabilitySummary(null);
        tagDetail.setVulnerabilitiesByPackage(null);
        return tagDetail;
    }

    /**
     * 得到project information clair result && quota
     *
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getPolicyDetailList(String projectName) throws Exception {
        HarborProjectInfo harborProjectInfo = new HarborProjectInfo();
        harborProjectInfo.setProject_name(projectName);
        ActionReturnUtil quotaResponse = getProjectQuota(projectName);
        if ((boolean) quotaResponse.get("success") == true) {
            //HarborProjectQuota harborProjectQuota =new HarborProjectQuota();
            if (quotaResponse.get("data") != null) {
                //JSONObject obj = new JSONObject().fromObject(quotaResponse.get("data").toString());
                //harborProjectQuota = (HarborProjectQuota)JSONObject.toBean(obj,HarborProjectQuota.class);
                Map<String, Object> projectQuota = JsonUtil.jsonToMap(quotaResponse.get("data").toString());
                if (projectQuota.get("quota_size") != null) {
                    //float quotaSize = Float.parseFloat(projectQuota.get("quota_size").toString());
                    harborProjectInfo.setQuota_size(Float.parseFloat(projectQuota.get("quota_size").toString()));
                }
                if (projectQuota.get("use_size") != null) {
                    harborProjectInfo.setUse_size(Float.parseFloat(projectQuota.get("use_size").toString()));
                }
                if (projectQuota.get("use_rate") != null) {
                    harborProjectInfo.setUse_rate(Float.parseFloat(projectQuota.get("use_rate").toString()));
                }
            }
        } else {
            return quotaResponse;
        }

        ActionReturnUtil clairResponse = harborIntegrationService.clairStatistcsOfProject(projectName);
        if ((boolean) clairResponse.get("success") == true) {
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
     * 模糊查询镜像repository
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getRepoFuzzySearch(String query, String tenantID) throws Exception {
        // 1.查询租户详情
        TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantID);
        if (null == tenantBinding) {
            return ActionReturnUtil.returnErrorWithData("current tenant does not exist");
        }
        List<HarborProjectTenant> harborProjectTenantList = harborProjectTenantMapper.getByTenantId(tenantID);
        List<String> projectList = new ArrayList<>();
        for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
            projectList.add(harborProjectTenant.getHarborProjectName());
        }
        ActionReturnUtil repoResponse = getFuzzySearch(query);
        // Map<String,List<String>>repoMap= new HashMap<>();
        if ((boolean) repoResponse.get("success") == true) {
            Map<String, List<String>> map = getRepositoryList(repoResponse.get("data").toString());
            Map<String, List<String>> repoListMap = new HashMap<>();
            for (String projectNameID : map.keySet()) {
                String[] nameID = projectNameID.split(SPLIT);
                if (nameID.length ==2) {
                    String projectName = nameID[0];
                    //Integer projectID = Integer.parseInt(nameID[1]);
                    if (projectList.contains(projectName)) {
                        repoListMap.put(projectNameID, map.get(projectNameID));
                    }
                }
            }
            List<HarborProjectInfo> projectRepoList = new ArrayList<>();
            for (String projectNameID : repoListMap.keySet()) {
                String[] nameID = projectNameID.split(SPLIT);
                String projectName;
                Integer projectID;
                if (nameID.length ==2) {
                    projectName = nameID[0];
                    projectID = Integer.parseInt(nameID[1]);
                }else{
                    return ActionReturnUtil.returnErrorWithData("inter error");
                }
                HarborProjectInfo projectInfo = new HarborProjectInfo();
                List<String> repoList = repoListMap.get(projectNameID);
                List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                for (String repositoryName : repoList) {
                    HarborRepositoryMessage repositoryMessage = getHarborRepositoryDetail(repositoryName);
                    repositoryMessagesList.add(repositoryMessage);
                }
                projectInfo.setProject_name(projectName);
                projectInfo.setProject_id(projectID);
                projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                /*
                ActionReturnUtil quotaResponse = getProjectQuota(projectName);
                if ((boolean) quotaResponse.get("success") == true) {
                    if (quotaResponse.get("data") != null) {
                        Map<String,Object> projectQuota =JsonUtil.jsonToMap(quotaResponse.get("data").toString());
                        if (projectQuota.get("quota_size")!=null){
                            projectInfo.setQuota_size(Float.parseFloat(projectQuota.get("quota_size").toString()));
                        }
                        if (projectQuota.get("use_size")!=null){
                            projectInfo.setUse_size(Float.parseFloat(projectQuota.get("use_size").toString()));
                        }
                        if (projectQuota.get("use_rate")!=null){
                            projectInfo.setUse_rate(Float.parseFloat(projectQuota.get("use_rate").toString()));
                        }
                    }
                }
                */
                projectRepoList.add(projectInfo);
            }

            return ActionReturnUtil.returnSuccessWithData(projectRepoList);

        } else {
            return repoResponse;
        }

    }

    /**
     * 查看project配额
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getFuzzySearch(String query) throws Exception {
        if (StringUtils.isEmpty(query)) {
            return ActionReturnUtil.returnErrorWithMsg("query cannot be null");
        }
        //String url = HarborClient.getPrefix() + "/api/projects/quotaList?project_name="+projectname;
        String url = HarborClient.getPrefix() + "/api/search";
        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());
        Map<String, Object> params = new HashMap<>();
        params.put("q", query);
        return HttpClientUtil.httpGetRequest(url, headers, params);
    }

    private Map<String, List<String>> getRepositoryList(String dataJson) {
        if (StringUtils.isNotEmpty(dataJson)) {
            //key值为project的name*id;
            Map<String, List<String>> queryResult = new HashMap<>();

            Map<String, Object> queryMap = JsonUtil.jsonToMap(dataJson);
            if (queryMap.get("repository") != null) {
                //List<Map<String, Object>> mapList = JsonUtil.JsonToMapList((queryMap.get("repository").toString()));
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
     * 查询指定租户的镜像
     *
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getImageByTenantID(String tenantID) throws Exception {
        //获取tenant的projectList
        List<HarborProjectTenant> harborProjectTenantList = harborProjectTenantMapper.getByTenantId(tenantID);
        List<HarborProjectInfo> projectRepoList = new ArrayList<>();
        //获取project的repositoryList
        for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
            ActionReturnUtil repoResponse = repoListById(Integer.parseInt(harborProjectTenant.getHarborProjectId().toString()));
            if ((boolean) repoResponse.get("success") == true) {
                List<String> repoList = JsonUtil.jsonToList(repoResponse.get("data").toString(), String.class);
                HarborProjectInfo projectInfo = new HarborProjectInfo();
                List<HarborRepositoryMessage> repositoryMessagesList = new ArrayList<>();
                //获取repository的tagList
                for (String repositoryName : repoList) {
                    //HarborRepositoryMessage repositoryMessage = getHarborRepositoryDetail(repositoryName);
                    HarborRepositoryMessage harborRepository = new HarborRepositoryMessage();
                    List<HarborRepositoryTags> tagLists = new ArrayList<>();
                    ActionReturnUtil tagResponse = getTagsByRepoName(repositoryName);
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
                        harborRepository.setRepository(repositoryName);
                        harborRepository.setTags(tagList);
                    }else{
                        return tagResponse;
                    }
                    repositoryMessagesList.add(harborRepository);
                }
                projectInfo.setProject_name(harborProjectTenant.getHarborProjectName());
                projectInfo.setHarborRepositoryMessagesList(repositoryMessagesList);
                projectRepoList.add(projectInfo);
            } else {
                return repoResponse;
            }
        }
       return ActionReturnUtil.returnSuccessWithData(projectRepoList);
    }

    /*
    获取租户的所有私有repoList
     */
    public List<String> getRepoListByTenantID(String tenantID) throws Exception{
        //获取tenant的私有仓库projectList
        List<String> privateRepoList =new ArrayList<>();
        HarborProjectTenant projectTenant =new HarborProjectTenant();
        Integer isPublic = 0;
        projectTenant.setIsPublic(isPublic);
        projectTenant.setTenantId(tenantID);
        List<HarborProjectTenant> harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPrivate(projectTenant);
        //获取project的repositoryList
        for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
            ActionReturnUtil repoResponse = repoListById(Integer.parseInt(harborProjectTenant.getHarborProjectId().toString()));
            if ((boolean) repoResponse.get("success") == true) {
                List<String> repoList = JsonUtil.jsonToList(repoResponse.get("data").toString(), String.class);
                for (String repositoryName : repoList) {
                    privateRepoList.add(repositoryName);
                }
            }
        }
        return privateRepoList;
    }
}
