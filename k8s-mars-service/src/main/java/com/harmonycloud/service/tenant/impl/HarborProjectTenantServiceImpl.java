package com.harmonycloud.service.tenant.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.platform.bean.ProjectUserBinding;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.UserService;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.HarborProjectTenantMapper;
import com.harmonycloud.dao.tenant.TenantBindingMapper;
import com.harmonycloud.dao.tenant.bean.HarborProjectTenant;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import com.harmonycloud.dto.tenant.HarborProjectDto;
import com.harmonycloud.dto.tenant.TenantDto;
import com.harmonycloud.k8s.bean.RoleBinding;
import com.harmonycloud.k8s.bean.RoleBindingList;
import com.harmonycloud.service.platform.bean.UserProjectBiding;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.tenant.HarborProjectTenantService;
import com.harmonycloud.service.tenant.TenantBindingService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.serviceImpl.harbor.HarborServiceImpl;

/**
 * Created by zhangsl on 16/11/13.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class HarborProjectTenantServiceImpl implements HarborProjectTenantService {

    @Autowired
    HarborProjectTenantMapper harborProjectTenantMapper;
    @Autowired
    TenantBindingMapper tenantBindingMapper;
    @Autowired
    TenantService tenantService;
    @Autowired
    TenantBindingService tenantBindingService;
    @Autowired
    HarborService harborService;
    @Autowired
    UserTenantService userTenantService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    HarborServiceImpl harborServiceImpl;
    @Autowired
    UserService userService;

    @Value("#{propertiesReader['image.url']}")
    private String harborUrl;
    private static final int STATUSCODE400 = 400;
    private static final int STATUSCODE401 = 401;
    private static final int STATUSCODE409 = 409;
    private static final int STATUSCODE500 = 500;
    private static int ISPUBLIC =0;
    @Autowired
    HarborUtil harborUtil;

    private static Logger logger = LoggerFactory.getLogger(HarborProjectTenantServiceImpl.class);
    @Override
    public HarborProjectTenant getByHarborProjectId(Long harborProjectTenantId) throws Exception {
        HarborProjectTenant byHarborProjectId = harborProjectTenantMapper.getByHarborProjectId(harborProjectTenantId);
        return byHarborProjectId;
    }
    @Override
    public List<HarborProjectTenant> harborProjectList(){
        return harborProjectTenantMapper.list();
    }
    @Override
    public int create(String harborProjectId, String tenantId, String name,Integer isPublic) {
        if (harborProjectTenantMapper.getByHarborProjectId(Long.parseLong(harborProjectId)) != null) {
            return 0;
        }
        HarborProjectTenant harborProjectTenant = new HarborProjectTenant();
        harborProjectTenant.setHarborProjectId(Long.parseLong(harborProjectId));
        harborProjectTenant.setTenantId(tenantId);
        harborProjectTenant.setTenantName(getTenantName(tenantId));
        harborProjectTenant.setCreateTime(new Date());
        harborProjectTenant.setHarborProjectName(name);
        harborProjectTenant.setIsPublic(isPublic);
        return harborProjectTenantMapper.insert(harborProjectTenant);
    }
    @Override
    public int delete(String harborProjectTenantId) {
        return harborProjectTenantMapper.delete(Long.parseLong(harborProjectTenantId));
    }

    private String getTenantName(String tenantId) {
        TenantBindingExample example = new TenantBindingExample();
        example.createCriteria().andTenantIdEqualTo(tenantId);
        List<TenantBinding> tenantBindings = tenantBindingMapper.selectByExample(example);
        if (tenantBindings == null || tenantBindings.isEmpty()) {
            return "";
        }
        return tenantBindings.get(0).getTenantName();
    }

    //为租户创建镜像project
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ActionReturnUtil createHarborProject(String name, String tenantid,Float quotaSize) throws Exception {

        try {
            // 1.获取租户详情
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantid);
            if (null == tenantBinding) {
                //return ActionReturnUtil.returnError();
                return ActionReturnUtil.returnErrorWithMsg("current tenant is legal");
            }

            List<String> harborProjectList = tenantBinding.getHarborProjectList();

            // 2.1 调用harbor接口创建harbor project
            String harborSearchUrl = harborUrl + CommonConstant.HARBOR_PROJECT;

            String cookie = harborUtil.checkCookieTimeout();
            Map<String, Object> header = new HashMap<>();
            Map<String, Object> body = new HashMap<>();
            header.put("Cookie", cookie);
            header.put("Content-type", "text/plain;charset=UTF-8");
            body.put("project_name", name);
            body.put("user_id", "0");
            body.put("owner_name", "admin");
            body.put("public", 0);
            //body.put("public", publicPro);
            body.put("deleted", 0);
            body.put("quota_num", CommonConstant.QUOTA_NUM);
            //body.put("quota_size", CommonConstant.QUOTA_SIZE);
            body.put("quota_size",quotaSize);

            CloseableHttpResponse response = HttpK8SClientUtil.httpPostJsonRequestForHarbor(harborSearchUrl, header, body);

            Integer statusCode = response.getStatusLine().getStatusCode();

            if (!HttpStatusUtil.isSuccessStatus(statusCode)) {
                logger.error("调用harbor接口创建harbor project失败, statusCode=" + statusCode + ", " + getErrMsg(statusCode));
                return getErrMsg(statusCode);
            }
            String location = response.getHeaders("Location")[0].getValue();

            String newHarborProjectId = location.substring(location.lastIndexOf(CommonConstant.SLASH) + 1, location.length());
            // 2.2 将harbor数据插入数据库
            if (this.create(newHarborProjectId, tenantid, name,ISPUBLIC) < 0) {
                logger.error("将harbor数据插入数据库失败, newHarborProjectId=" + newHarborProjectId + ", tenantid=" + tenantid);
                // return ActionReturnUtil.returnErrorWithMsg("create failed");
                //回滚harbor
                ActionReturnUtil deleteResult = harborService.deleteProject(Integer.valueOf(newHarborProjectId));
                if ((Boolean) deleteResult.get("success") == CommonConstant.FALSE) {
                    logger.error("调用harbor接口删除harbor project失败, projectid=" + newHarborProjectId, deleteResult.get("data"));
                   // return deleteResult;
                }
                return ActionReturnUtil.returnErrorWithMsg("create failed");

            }

            // 2.3 创建镜像数配额
            /*
            String harborQuotaUrl = harborUrl + CommonConstant.HARBOR_PROJECT + "/" + newHarborProjectId + "/quota";
            body.clear();
            body.put("quota_num", CommonConstant.QUOTA_NUM);
            //body.put("quota_size", CommonConstant.QUOTA_SIZE);
            body.put("quota_size",quotaSize);
            CloseableHttpResponse quotaResponse = HttpK8SClientUtil.httpPostJsonRequestForHarbor(harborQuotaUrl, header, body);
            if (!HttpStatusUtil.isSuccessStatus(quotaResponse.getStatusLine().getStatusCode())) {
                logger.error("调用harbor接口创建harbor project失败, statusCode=" + statusCode + ", " + getErrMsg(statusCode));
                return getErrMsg(statusCode);
            }
            */
            // TODO 可以将配额信息存储到数据库中

            // 3.更新租户信息
            harborProjectList.add(newHarborProjectId);

            if (tenantBindingService.updateHarborProjectsByTenantId(tenantid, harborProjectList) < 0) {
                return ActionReturnUtil.returnErrorWithMsg("create failed");
            }

            //4.add users of current tenant to the project
            List<UserTenant> userList = userTenantService.getUserByTenantid(tenantid);
            List<String>userNameList =new ArrayList<String>();
            for(int i = 0; i < userList.size(); i++)
            {
                userNameList.add(userList.get(i).getUsername());
                //System.out.println(list.get(i));
            }
            UserProjectBiding userProject =new UserProjectBiding();
            userProject.setProject(newHarborProjectId);
            userProject.setUserNames(userNameList);
            ActionReturnUtil role1=harborProjectService.bindingProjectUsers(userProject);
            if((boolean)role1.get("success")==false){
                return role1;
            }

        } catch (Exception e) {
            logger.error("创建harbor project失败", e.getMessage());
            throw e;
        }

        return ActionReturnUtil.returnSuccess();
    }

    //删除镜像project
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public ActionReturnUtil deleteHarborProject(String tenantname, String tenantid, String projectid) throws Exception {

        try {
            // 0.公共镜像仓库禁止删除
            HarborProjectTenant harbor = harborProjectTenantMapper.getByHarborProjectId(Long.valueOf(projectid));
            if(harbor != null){
                Integer isPublic = harbor.getIsPublic();
                if (isPublic == 1){
                    return ActionReturnUtil.returnErrorWithData("Public project can not be deleted");
                }

            }
            // 1.查询租户详情
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantid);
            if (null == tenantBinding) {
                //return ActionReturnUtil.returnError();
                return ActionReturnUtil.returnErrorWithData("Current tenant is not existed");
            }

            // 2.判断租户下是否有用户存在，有用户时提示先删除用户再删除harbor project
            /*
            ActionReturnUtil roleBindingResult = this.isExistedImageByTenant(tenantname, projectid);
            if ((Boolean) roleBindingResult.get("success") == CommonConstant.TRUE) {
                if ((Boolean) roleBindingResult.get("data") == CommonConstant.TRUE) {
                    return ActionReturnUtil.returnErrorWithMsg("exist binding user!");
                }
            } else {
                logger.error("查询租户下的rolebinding失败,　tenantname=" + tenantname + ", projectid=" + projectid);
                return ActionReturnUtil.returnError();
            }
            */
            // 3.调用harbor接口删除harbor project
            ActionReturnUtil deleteResult = harborService.deleteProject(Integer.valueOf(projectid));
            if ((Boolean) deleteResult.get("success") == CommonConstant.FALSE) {
                logger.error("调用harbor接口删除harbor project失败, projectid=" + projectid, deleteResult.get("data"));
                return deleteResult;
            }

            // 4. 更新租户绑定信息
            List<String> harborProjectIdList = tenantBinding.getHarborProjectList();
            if (harborProjectIdList.contains(projectid)) {
                harborProjectIdList.remove(projectid);
            }
            if (tenantBindingService.updateHarborProjectsByTenantId(tenantid, harborProjectIdList) < 0) {
                return ActionReturnUtil.returnErrorWithData("delete harbor projecct failed");
            }
            // 5. 删除数据库harbor数据
            if (this.delete(projectid) < 0) {
                return ActionReturnUtil.returnErrorWithData("delete harbor projecct failed");
            }
            return ActionReturnUtil.returnSuccess();
        } catch (Exception e) {
            logger.error("删除harbor project 失败, tenantname=" + tenantname + ",tenantid=" + tenantid + ",projectid=" + projectid);
            // return ActionReturnUtil.returnErrorWithMsg("delete harbor
            // projecct failed");
            throw e;
        }
    }

    @Override
    public List<HarborProjectTenant> getSimplProjectList(String tenantid) throws Exception {
        List<HarborProjectTenant> harborProjectTenantList =new ArrayList<>();
        harborProjectTenantList =harborProjectTenantMapper.getByTenantId(tenantid);
        return harborProjectTenantList;
    }
    //查看租户的镜像project列表
    @Override
    public ActionReturnUtil getProjectList(String tenantid,Integer isPublic,boolean quota) throws Exception {

        try {
            logger.info("查询租户下harbor project列表,tenantid=" + tenantid);
            // 1.查询租户详情
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantid);
            if (null == tenantBinding) {
                //return ActionReturnUtil.returnError();
                return ActionReturnUtil.returnErrorWithData("current tenant does not exist");
            }
            // 2.根据租户id查询harbor project列表
            HarborProjectTenant projectTenant =new HarborProjectTenant();
            projectTenant.setIsPublic(isPublic);
            projectTenant.setTenantId(tenantid);
            //判断请求的是私有还是共有镜像
            List<HarborProjectTenant> harborProjectTenantList =new ArrayList<>();
            if (isPublic == 0) {
                harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPrivate(projectTenant);
            }else if(isPublic ==1 ){
                harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPublic(isPublic);
            }else if(isPublic == 2 ){
                harborProjectTenantList =harborProjectTenantMapper.getByTenantId(projectTenant.getTenantId());
            }

            List<HarborProjectDto> harborProjectDtoList = new ArrayList<>();
            for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
                HarborProjectDto harborProjectDto = new HarborProjectDto();
                harborProjectDto.setName(harborProjectTenant.getHarborProjectName());
                harborProjectDto.setHarborid(harborProjectTenant.getHarborProjectId());
                harborProjectDto.setIsPublic(harborProjectTenant.getIsPublic());
                String date = DateUtil.DateToString(harborProjectTenant.getCreateTime(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
                harborProjectDto.setTime(date);
                //读取租户信息
                TenantDto tenantDto = new TenantDto();
                tenantDto.setName(tenantBinding.getTenantName());
                tenantDto.setTenantId(tenantid);
                //获取project配额信息
                if(quota == true) {
                    ActionReturnUtil quotaResponse = harborService.getProjectQuota(harborProjectTenant.getHarborProjectName());
                    if ((boolean) quotaResponse.get("success") == true) {
                        if (quotaResponse.get("data") != null) {
                            Map<String,Object> projectQuota =JsonUtil.jsonToMap(quotaResponse.get("data").toString());
                            if (projectQuota.get("quota_size")!=null){
                                harborProjectDto.setQuota_size(Float.parseFloat(projectQuota.get("quota_size").toString()));
                            }
                            if (projectQuota.get("use_size")!=null){
                                harborProjectDto.setUse_size(Float.parseFloat(projectQuota.get("use_size").toString()));
                            }
                            if (projectQuota.get("use_rate")!=null){
                                harborProjectDto.setUse_rate(Float.parseFloat(projectQuota.get("use_rate").toString()));
                            }
                        }
                    }
                }else{
                    //获取project的repository数目
                    int repoNum= getRepoNum(harborProjectDto.getHarborid());
                    harborProjectDto.setRepository_num(repoNum);
                }
                harborProjectDto.setTenant(tenantDto);
                harborProjectDtoList.add(harborProjectDto);
            }

            return ActionReturnUtil.returnSuccessWithData(harborProjectDtoList);

        } catch (Exception e) {
            logger.error("查询租户下harbor project列表失败,tenantid=" + tenantid, e.getMessage());
            throw e;
        }
    }

    @Override
    public ActionReturnUtil getProjectDetail(String tenantid, Integer projectid) throws Exception {

        try {
            // 1.查询租户详情
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantid);
            if (null == tenantBinding) {
                return ActionReturnUtil.returnError();
            }
            // 2.调用harbor接口获取详情
            ActionReturnUtil actionReturnUtil = harborService.getProjectById(projectid);
            if ((Boolean) actionReturnUtil.get("success") == false) {
                return actionReturnUtil;
            }
            if (!StringUtils.isEmpty(actionReturnUtil.get("data"))) {
                Map<String, Object> detail = JsonUtil.jsonToMap((String) actionReturnUtil.get("data"));
                HarborProjectDto harborProjectDto = new HarborProjectDto();
                harborProjectDto.setName((String) detail.get("name"));
                harborProjectDto.setTime((String) detail.get("creation_time"));
                harborProjectDto.setHarborid(Long.valueOf(projectid));
                TenantDto tenantDto = new TenantDto();
                tenantDto.setName(tenantBinding.getTenantName());
                tenantDto.setTenantId(tenantid);
                harborProjectDto.setTenant(tenantDto);
                return ActionReturnUtil.returnSuccessWithData(harborProjectDto);
            }

        } catch (Exception e) {
            logger.error("获取harbor project详情失败", e.getMessage());
            throw e;
        }
        return ActionReturnUtil.returnError();
    }

    public static void main(String[] args) {
        List<String> ids = new ArrayList<>();

        ids.add("1");
        ids.add("2");
        ids.add("12");
        if (ids.contains("2")) {
            ids.remove("2");
        }
        System.out.println(JsonUtil.convertToJson(ids));
    }

    private static ActionReturnUtil getErrMsg(Integer statusCode) throws Exception {
        switch (statusCode) {
            case STATUSCODE400 :
                return ActionReturnUtil.returnErrorWithMsg("约束不正常");
            case STATUSCODE401 :
                return ActionReturnUtil.returnErrorWithMsg("未认证");
            case STATUSCODE409 :
                return ActionReturnUtil.returnErrorWithMsg("创建的仓库已经存在,请从新输入!");
            case STATUSCODE500 :
                return ActionReturnUtil.returnErrorWithMsg("意外的内部错误");
            default :
                return ActionReturnUtil.returnErrorWithMsg("镜像仓库未知异常");
        }
    }

    private ActionReturnUtil isExistedImageByTenant(String tenantname, String projectid) throws Exception {

        // 查询租户下面rolebinding
        Map<String, Object> bodys = new HashMap<>();
        K8SURL k8SURL = new K8SURL();
        k8SURL.setResource(Resource.ROLEBINDING);
        bodys.put("labelSelector", "nephele_tenant_" + tenantname + "=" + tenantname);

        K8SClientResponse k8SClientResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, bodys);

        if (HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
            if (StringUtils.isEmpty(k8SClientResponse.getBody())) {
                return ActionReturnUtil.returnSuccessWithData(false);
            }
            RoleBindingList roleBindingList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), RoleBindingList.class);
            if (roleBindingList == null) {
                return ActionReturnUtil.returnSuccessWithData(false);
            }
            for (RoleBinding roleBinding : roleBindingList.getItems()) {
                // 判断harbor下是否有用户存在
                if (roleBinding.getMetadata().getAnnotations() != null && !StringUtils.isEmpty(roleBinding.getMetadata().getAnnotations().get("verbs"))
                        && !StringUtils.isEmpty(roleBinding.getMetadata().getAnnotations().get("project"))) {
                    if (roleBinding.getMetadata().getAnnotations().get("project").equals(projectid)) {
                        return ActionReturnUtil.returnSuccessWithData(true);
                    }

                }
            }
            return ActionReturnUtil.returnSuccessWithData(false);
        } else {
            logger.error("查询租户下面rolebinding失败");
            return ActionReturnUtil.returnError();
        }
    }

    private int getRepoNum(Long projectid)throws Exception{

        ActionReturnUtil response= harborService.repoListById((Integer.parseInt(projectid.toString())));
        if((boolean)response.get("success") == true){
            List<String> list =JsonUtil.jsonToList(response.get("data").toString(),String.class);
            int repoNum = list.size();
            return repoNum;
        }else{
            return 0;
        }

    }
    public ActionReturnUtil clearTenantProject(String tenantid)throws  Exception{
        // 1.查询租户详情
        /*
        TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantid);
        if (null == tenantBinding) {
            return ActionReturnUtil.returnError();
        }
        */
        // 2.获取租户的所有的私有repository
        List<String>repoList = harborServiceImpl.getRepoListByTenantID(tenantid);
        // 3.删除租户的私有repository
        //ActionReturnUtil actionReturn = ActionReturnUtil.returnSuccess();
        if (repoList.size()!=0){
            for(String repoName:repoList){
                ActionReturnUtil reponse = harborService.deleteRepo(repoName,null);
                if ((boolean) reponse.get("success") != true) {
                    logger.error("删除租户的私有镜像repository失败：repoName="+repoName);
                }
            }
        }
        //4.获取用户的私有project列表
        HarborProjectTenant projectTenant =new HarborProjectTenant();
        Integer isPublic = 0;
        projectTenant.setIsPublic(isPublic);
        projectTenant.setTenantId(tenantid);
        List<HarborProjectTenant> harborProjectTenantList = new ArrayList<>();
        harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPrivate(projectTenant);
        //5.删除租户的project
        ActionReturnUtil returnRes = ActionReturnUtil.returnSuccess();
        for(HarborProjectTenant project:harborProjectTenantList){
            Integer projectId = Integer.parseInt(project.getHarborProjectId().toString());
            String projectName = project.getHarborProjectName();
            //harbor删除数据
            ActionReturnUtil reponse = harborService.deleteProject(projectId);
            if ((boolean) reponse.get("success") != true) {
                logger.error("删除租户的私有镜像project出错：projectName="+projectName);
                returnRes=ActionReturnUtil.returnError();
            }
            //数据库删除
            if (this.delete(projectId.toString()) < 0) {
                logger.error("删除租户的私有镜像project数据库出错：projectName="+projectName);
                returnRes=ActionReturnUtil.returnError();
            }
        }
        return returnRes;
    }
    @Override
    public ActionReturnUtil addProjectsToUser(String username,String tenantid) throws Exception{
        List<HarborProjectTenant> harborProjectTenantList =new ArrayList<>();
        ProjectUserBinding projectUserBinding = new ProjectUserBinding();
        HarborProjectTenant projectTenant =new HarborProjectTenant();
        projectTenant.setIsPublic(0);
        projectTenant.setTenantId(tenantid);
        harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPrivate(projectTenant);
        List<String>projectList = new ArrayList<>();
        for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
            String project = harborProjectTenant.getHarborProjectId().toString();
            projectList.add(project);
        }
        projectUserBinding.setUserName(username);
        projectUserBinding.setProjects(projectList);
        ActionReturnUtil returnUtil =harborProjectService.bindingUserProjects(projectUserBinding);
        return returnUtil;
    }
    @Override
    public ActionReturnUtil deleteUserFromProjects(String username,String tenantid) throws Exception{
        List<HarborProjectTenant> harborProjectTenantList =new ArrayList<>();
        ProjectUserBinding projectUserBinding = new ProjectUserBinding();
        HarborProjectTenant projectTenant =new HarborProjectTenant();
        projectTenant.setIsPublic(0);
        projectTenant.setTenantId(tenantid);
        harborProjectTenantList = harborProjectTenantMapper.getByTenantIdPrivate(projectTenant);
        List<String>projectList = new ArrayList<>();
        for (HarborProjectTenant harborProjectTenant : harborProjectTenantList) {
            String project = harborProjectTenant.getHarborProjectId().toString();
            projectList.add(project);
        }
        User user = userService.getUser(username);
        projectUserBinding.setUserId(Integer.valueOf(user.getId().toString()));
        projectUserBinding.setUserName(username);
        projectUserBinding.setProjects(projectList);
        ActionReturnUtil returnUtil =harborProjectService.unBindingUserProjects(projectUserBinding);
        return returnUtil;
    }
}
