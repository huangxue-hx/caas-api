package com.harmonycloud.service.platform.integrationServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.HarborSecurityFlagNameEnum;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.tenant.HarborProjectTenantMapper;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.k8s.bean.Subjects;
import com.harmonycloud.k8s.service.RoleBindingService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.platform.bean.HarborManifest;
import com.harmonycloud.service.platform.bean.HarborProject;
import com.harmonycloud.service.platform.bean.HarborRepository;
import com.harmonycloud.service.platform.bean.HarborRepositoryTags;
import com.harmonycloud.service.platform.bean.HarborRole;
import com.harmonycloud.service.platform.bean.HarborSecurityClairStatistcs;
import com.harmonycloud.service.platform.bean.HarborUser;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.dto.HarborUserShowDto;
import com.harmonycloud.service.platform.integrationService.HarborIntegrationService;
import com.harmonycloud.service.platform.service.harbor.HarborMemberService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;
import com.harmonycloud.service.platform.service.harbor.HarborService;



/**
 * Created by zsl on 2017/1/19.
 */
@Service
public class HarborIntegrationServiceImpl implements HarborIntegrationService {

    @Autowired
    private HarborService harborService;

    @Autowired
    private HarborMemberService harborMemberService;

    @Autowired
    private HarborSecurityService harborSecurityService;

    @Autowired
    RoleBindingService roleBindingService;

    @Autowired
    HarborProjectTenantMapper harborProjectTenantMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String HARBOR = "harbor";
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
        return harborService.login(username, password);
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
        return harborService.projectList(page, pageSize);
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

        ActionReturnUtil actionReturnUtil = harborService.getProjectById(projectId);

        if (isSuccessRequest(actionReturnUtil)) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborProjectResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
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
        return harborService.createProject(harborProject);
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
        return harborService.deleteProject(projectId);
    }

    /**
     * 根据projectId获取repo详情 repo+tag+domain
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getRepoDomainDetailByProjectId(Integer projectId) throws Exception {
        if (projectId == null || projectId < 0) {
            return ActionReturnUtil.returnErrorWithMsg("projectId is invalid");
        }

        ActionReturnUtil repoResponse = harborService.repoListById(projectId);

        if (isSuccessRequest(repoResponse)) {
            repoResponse.put("data", getHarborRepositoryResp(repoResponse));
        }

        return repoResponse;
    }

    /**
     * 根据projectId获取project下的成员列表
     *
     * @param projectId projectId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil usersOfProject(Integer projectId) throws Exception {

        ActionReturnUtil actionReturnUtil = harborMemberService.usersOfProject(projectId);

        if (isSuccessRequest(actionReturnUtil)) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborUserResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
    }

    /**
     * 创建project下的role
     *
     * @param projectId  projectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createRole(Integer projectId, HarborRole harborRole) throws Exception {
        return harborMemberService.createRole(projectId, harborRole);
    }

    /**
     * 更新project下的role
     *
     * @param projectId  projectId
     * @param userId     userId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updateRole(Integer projectId, Integer userId, HarborRole harborRole) throws Exception {
        return harborMemberService.updateRole(projectId, userId, harborRole);
    }

    /**
     * 删除project下的role
     *
     * @param projectId projectId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteRole(Integer projectId, Integer userId) throws Exception {
        return harborMemberService.deleteRole(projectId, userId);
    }

    /**
     * 在project纬度展示Clair对repo的扫描结果
     *
     * @param projectName project name
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil clairStatistcsOfProject(String projectName) throws Exception {

        ActionReturnUtil actionReturnUtil = harborSecurityService.clairStatistcs(HarborSecurityFlagNameEnum.PROJECT.getFlagName(), projectName);

        if (isSuccessRequest(actionReturnUtil)) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborSecurityClairStatistcsResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
    }

    /**
     * 扫描总结
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil vulnerabilitySummary(String repoName, String tag) throws Exception {
        return harborSecurityService.vulnerabilitySummary(repoName, tag);
    }

    /**
     * 扫描总结 -package纬度
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil vulnerabilitiesByPackage(String repoName, String tag) throws Exception {
        return harborSecurityService.vulnerabilitiesByPackage(repoName, tag);
    }

    /**
     * tag详情
     *
     * @param repoName repo name
     * @param tag      tag
     * @return
     */
    @Override
    public ActionReturnUtil manifestsOfTag(String repoName, String tag) throws Exception {
        ActionReturnUtil manifestsResponse = harborService.getManifests(repoName, tag);

        if (isSuccessRequest(manifestsResponse)) {
            if (manifestsResponse.get("data") != null) {
                return ActionReturnUtil.returnSuccessWithData(getHarborManifestResp(manifestsResponse.get("data").toString(), repoName, tag));
            }
        }

        return ActionReturnUtil.returnError();
    }

    /**
     * 在user纬度展示Clair对repo的扫描结果
     *
     * @param username username
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil clairStatistcsOfUser(String username) throws Exception {

        ActionReturnUtil actionReturnUtil = harborSecurityService.clairStatistcs(HarborSecurityFlagNameEnum.USER.getFlagName(), username);

        if (isSuccessRequest(actionReturnUtil)) {
            if (actionReturnUtil.get("data") != null) {
                actionReturnUtil.put("data", getHarborSecurityClairStatistcsResp(actionReturnUtil.get("data").toString()));
            }
        }

        return actionReturnUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionReturnUtil getUserList(String tenantname, String namespace) throws Exception {

        // 根据namespace查询rolebinding列表
        String lable = new StringBuffer().append(CommonConstant.NEPHELE_TENANT).append(tenantname)
                .append(CommonConstant.EQUALITY_SIGN).append(tenantname).toString();
        K8SClientResponse rolebindingResponse = roleBindingService.getRolebindingInNamespacebyLabelSelector(namespace,
                lable);
        if(!HttpStatusUtil.isSuccessStatus(rolebindingResponse.getStatus())) {
            logger.error("调用k8s接口查询namespace下rolebinding列表失败", rolebindingResponse.getBody());
            return ActionReturnUtil.returnErrorWithMsg(rolebindingResponse.getBody());
        }

        RoleBindingList roleBindingList = JsonUtil.jsonToPojo(rolebindingResponse.getBody(), RoleBindingList.class);
        if(roleBindingList.getItems().size() > 0){
            List<HarborUserShowDto> userList = new ArrayList<>();
            for (RoleBinding roleBinding : roleBindingList.getItems()) {
                // 过滤harbor
                if(roleBinding.getRoleRef() != null &&
                        !org.springframework.util.StringUtils.isEmpty(roleBinding.getRoleRef().getName())){
                    String roleName = roleBinding.getRoleRef().getName();

                    if(roleName.startsWith(HARBOR)){
                        HarborUserShowDto harborUserShowDto = new HarborUserShowDto();
                        harborUserShowDto.setRole(roleName);
                        harborUserShowDto.setNamespace(roleBinding.getMetadata().getNamespace());
                        harborUserShowDto.setTime(roleBinding.getMetadata().getCreationTimestamp());
                        if(roleBinding.getMetadata().getAnnotations() != null){
                            Map<String, Object> annotations = roleBinding.getMetadata().getAnnotations();
                            List<String> userId = new LinkedList<>();
                            userId.add((String) annotations.get("userId"));
                            harborUserShowDto.setUserId(userId);
                            String projectId = (String) annotations.get("project");
                            if(StringUtils.isNotEmpty(projectId)){
                                harborUserShowDto.setHarborid(projectId);
                                generaterHarborProjectName(projectId, harborUserShowDto);
                            }

                        }
                        if(null != roleBinding.getSubjects()){
                            List<String> userNames = new LinkedList<>();
                            harborUserShowDto.setUser(userNames);
                            for (Subjects subjects : roleBinding.getSubjects()) {
                                String userName = subjects.getName();
                                if (StringUtils.isNotEmpty(userName)) {
                                    userNames.add(userName);
                                }
                            }
                        }
                        userList.add(harborUserShowDto);
                    }
                }
            }
            return ActionReturnUtil.returnSuccessWithData(userList);
        }
        return ActionReturnUtil.returnSuccessWithData(null);
    }

    private void generaterHarborProjectName(String projectId, HarborUserShowDto harborUserShowDto) throws Exception{

        // 根据projectId查询harbor名称
        HarborProjectTenant harbor = harborProjectTenantMapper.getByHarborProjectId(Long.valueOf(projectId));
        if(harbor != null){
            String projcetName = harbor.getHarborProjectName();
            if(StringUtils.isNotEmpty(projcetName)){
                harborUserShowDto.setHarborProjectName(projcetName);
            }
        }
    }

    /**
     * 得到harbor repository response
     *
     * @param repoResponse repo response
     * @return
     * @throws Exception
     */
    private List<HarborRepository> getHarborRepositoryResp(ActionReturnUtil repoResponse) throws Exception {
        if (repoResponse.get("data") != null) {
            List<String> repoNameList = this.getHarborRepoNameList(repoResponse.get("data").toString());
            if (!CollectionUtils.isEmpty(repoNameList)) {
                List<HarborRepository> harborRepositoryList = new ArrayList<>();
                for (String repoName : repoNameList) {
                    if (StringUtils.isNotEmpty(repoName)) {
                        HarborRepository harborRepository = new HarborRepository();
                        ActionReturnUtil tagResponse = harborService.getTagsByRepoName(repoName);
                        if (isSuccessRequest(tagResponse)) {
                            if (tagResponse.get("data") != null) {
                                harborRepository.setTags(getRepoTagList(tagResponse.get("data").toString()));
                            }
                        }
                        harborRepository.setName(repoName);
                        harborRepository.setSource(getSource(repoName));
                        harborRepositoryList.add(harborRepository);
                    }
                }
                return harborRepositoryList;
            }
        }
        return Collections.emptyList();
    }


    /**
     * 得到harbor repository name list
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<String> getHarborRepoNameList(String dataJson) throws Exception{
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
    private List<HarborRepositoryTags> getRepoTagList(String dataJson) throws Exception{
        if (StringUtils.isNotEmpty(dataJson)) {
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<HarborRepositoryTags> harborRepositoryTagsList = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    HarborRepositoryTags harborRepositoryTags = new HarborRepositoryTags();
                    if (map.get("tag") != null) {
                        harborRepositoryTags.setTag(map.get("tag").toString());
                    }
                    if (map.get("high_num") != null) {
                        harborRepositoryTags.setHigh_num(Integer.parseInt(map.get("high_num").toString()));
                    }
                    if (map.get("other_num") != null) {
                        harborRepositoryTags.setOther_num(Integer.parseInt(map.get("other_num").toString()));
                    }
                    harborRepositoryTagsList.add(harborRepositoryTags);
                }
                return harborRepositoryTagsList;
            }
        }
        return Collections.emptyList();
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
     * 得到harbor user response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<HarborUser> getHarborUserResp(String dataJson) throws Exception{
        if (StringUtils.isNotEmpty(dataJson)) {
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<HarborUser> harborUserList = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    HarborUser harborUser = new HarborUser();
                    if (map.get("username") != null) {
                        harborUser.setUsername(map.get("username").toString());
                    }
                    if (map.get("role_name") != null) {
                        harborUser.setRoleName(map.get("role_name").toString());
                    }
                    if (map.get("creation_time") != null) {
                        harborUser.setCreationTime(map.get("creation_time").toString());
                    }
                    harborUserList.add(harborUser);
                }
                return harborUserList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 得到harbor security clair statistcs response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborSecurityClairStatistcs getHarborSecurityClairStatistcsResp(String dataJson) throws Exception{
        HarborSecurityClairStatistcs harborSecurityClairStatistcs = new HarborSecurityClairStatistcs();
        if (StringUtils.isNotEmpty(dataJson)) {
            Map<String, Object> map = JsonUtil.jsonToMap(dataJson);
            if (map != null) {
                if (map.get("image_num") != null) {
                    harborSecurityClairStatistcs.setImage_num(Integer.parseInt(map.get("image_num").toString()));
                }
                if (map.get("unsecurity_image_num") != null) {
                    harborSecurityClairStatistcs.setUnsecurity_image_num(Integer.parseInt(map.get("unsecurity_image_num").toString()));
                }
                if (map.get("clair_not_Support") != null) {
                    harborSecurityClairStatistcs.setClair_not_Support(Integer.parseInt(map.get("clair_not_Support").toString()));
                }
                if (map.get("clair_success") != null) {
                    harborSecurityClairStatistcs.setClair_success(Integer.parseInt(map.get("clair_success").toString()));
                }
                if (map.get("abnormal") != null) {
                    harborSecurityClairStatistcs.setAbnormal(Integer.parseInt(map.get("abnormal").toString()));
                }
                if(map.get("mild")!=null){
                    harborSecurityClairStatistcs.setMild(Integer.parseInt(map.get("mild").toString()));
                }
            }
        }
        return harborSecurityClairStatistcs;
    }

    /**
     * 得到harbor manifest response
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborManifest getHarborManifestResp(String dataJson, String repoName, String tag) throws Exception {
        HarborManifest harborManifest = null;
        if (StringUtils.isNotEmpty(dataJson)) {
            ActionReturnUtil vulnerabilitySummaryResponse = harborSecurityService.vulnerabilitySummary(repoName, tag);
            ActionReturnUtil vulnerabilitiesByPackageResponse = harborSecurityService.vulnerabilitiesByPackage(repoName, tag);
            harborManifest = new HarborManifest();
            harborManifest.setTag(tag);
            Map<String, Object> manifestMap = JsonUtil.convertJsonToMap(dataJson);
            if (manifestMap.get("Author") != null) {
                harborManifest.setAuthor(manifestMap.get("Author").toString());
            }
            /*
            if (manifestMap.get("Created") != null) {
                harborManifest.setCreateTime(manifestMap.get("Created").toString());
            }
            */
            if (manifestMap.get("config") != null) {
                String manifestTime = manifestMap.get("config").toString();
                Integer start =manifestTime.indexOf("created");
                harborManifest.setCreateTime(manifestTime.substring(start+10,start+29));
                //harborManifest.setCreateTime(manifestMap.get("config").toString());
            }
            if (isSuccessRequest(vulnerabilitySummaryResponse)) {
                if (vulnerabilitySummaryResponse.get("data") != null) {
                    harborManifest.setVulnerabilitySummary(JsonUtil.convertJsonToMap(vulnerabilitySummaryResponse.get("data").toString()));
                }
            }
            if (isSuccessRequest(vulnerabilitiesByPackageResponse)) {
                if (vulnerabilitySummaryResponse.get("data") != null) {
                    harborManifest.setVulnerabilitiesByPackage(JsonUtil.convertJsonToMap(vulnerabilitiesByPackageResponse.get("data").toString()));
                }
            }
        }
        return harborManifest;
    }

    /**
     * 得到repo source
     *
     * @param repoName repo name
     * @return
     */
    private String getSource(String repoName) {
        return new HarborClient().getDomain() + "/" + repoName;
//        return "10.10.101.52" + "/" + repoName;
    }

    /**
     * 判断是否是正确请求
     *
     * @param actionReturnUtil response
     * @return
     */
    private Boolean isSuccessRequest(ActionReturnUtil actionReturnUtil) {
        return Boolean.TRUE.equals(actionReturnUtil.get("success"));
    }

    public static void main(String[] args) {

        try {
            HarborIntegrationServiceImpl harborIntegrationService = new HarborIntegrationServiceImpl();
            ActionReturnUtil actionReturnUtil = harborIntegrationService.usersOfProject(77);

            System.out.print(actionReturnUtil.get("data").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
