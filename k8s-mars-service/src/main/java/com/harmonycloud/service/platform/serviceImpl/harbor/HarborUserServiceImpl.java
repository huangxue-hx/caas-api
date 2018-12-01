package com.harmonycloud.service.platform.serviceImpl.harbor;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.ProjectUserBinding;
import com.harmonycloud.service.platform.bean.UserProjectBiding;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by zhangkui on 2017/12/18.
 * harbor用户管理
 */
@Service
public class HarborUserServiceImpl implements HarborUserService {

    private Logger LOGGER = LoggerFactory.getLogger(HarborUserServiceImpl.class);

    private static final String HARBOR_API_USERS = "/api/users";
    private static final String HARBOR_LOGIN = "/login";

	@Autowired
    private SystemConfigService systemConfigService;
	@Autowired
    private ClusterService clusterService;
	@Autowired
    private UserService userService;
	@Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Override
    public Integer createUser(HarborServer harborServer, User user) throws Exception {
        if(harborServer == null || user == null){
            LOGGER.warn("createUser parameter error,cluster:{},user:{}",harborServer,user);
            return null;
        }
        LOGGER.info("创建Harbor信息，harborServer:{},user:{}", JSONObject.toJSONString(harborServer), user.toString());
        String createUserApiUrl = HarborClient.getHarborUrl(harborServer)+ HARBOR_API_USERS;
        //ldap验证方式获取不到用户的密码，使用系统生成的密码
        String password = user.getPassword();
        if(StringUtils.isBlank(password)){
            password = userService.generatePassWord();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("username", user.getUsername());
        params.put("password", password);
        params.put("realname", user.getRealName());
        params.put("comment", user.getComment());
        params.put("email", user.getEmail());
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("Cookie", HarborClient.checkHarborAdminCookie(harborServer));
        header.put("Content-type", "application/json");

        CloseableHttpResponse response = HarborHttpsClientUtil.doBodyPost(createUserApiUrl, params, header);
        if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
            Header[] headers = response.getHeaders("Location");
            if (headers.length > 0) {
                Header location = headers[0];
                String harborUserId = location.getValue().substring(location.getValue().lastIndexOf("/") + 1);
                if(StringUtils.isBlank(harborUserId)){
                    LOGGER.error("创建Harbor用户失败，response location header:{}", JSONObject.toJSONString(location));
                    return null;
                }
                return Integer.valueOf(harborUserId);
            }
            LOGGER.error("创建Harbor用户失败，response location headers:{}", JSONObject.toJSONString(headers));
            return null;
        }else{
            LOGGER.error("创建Harbor用户失败，response:{}", JSONObject.toJSONString(response));
            return null;
        }

    }

    @Override
    public void harborUserLogin(HarborServer harborServer, User user) throws Exception {
        if(harborServer == null || user == null){
            LOGGER.warn("User parameter error,cluster:{},user:{}",harborServer,user);
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        LOGGER.info("登录Harbor信息，harborServer:{},user:{}", JSONObject.toJSONString(harborServer), user.toString());
        String createUserApiUrl = HarborClient.getHarborUrl(harborServer) + HARBOR_LOGIN;

        String password = user.getPassword();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("principal", user.getUsername());
        params.put("password", password);

        Map<String, Object> header = new HashMap<String, Object>();
//        header.put("Cookie", HarborClient.checkHarborAdminCookie(harborServer));
        header.put("Content-type", "x-www-form-urlencoded");

        CloseableHttpResponse response = HarborHttpsClientUtil.doPostWithLogin(createUserApiUrl, params, null);
        if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
            LOGGER.info("登录Harbor用户成功，response:{}", JSONObject.toJSONString(response));
        }else{
            LOGGER.error("登录Harbor用户失败，response:{}", JSONObject.toJSONString(response));
        }
    }

    @Override
    public HarborUser getUserByName(HarborServer harborServer, String userName) throws Exception {
        if(harborServer == null || StringUtils.isBlank(userName)){
            LOGGER.warn("createUser parameter error,harborServer:{},userName:{}",harborServer,userName);
            return null;
        }
        String getUserApi = HarborClient.getHarborUrl(harborServer) + HARBOR_API_USERS + "?username=" + userName;
        HttpClientResponse httpClientResponse = HarborHttpsClientUtil.doGet(getUserApi, null, HarborClient.getAdminCookieHeader(harborServer));
        List<Map<String, Object>> result = JsonUtil.JsonToMapList(httpClientResponse.getBody());
        if (!CollectionUtils.isEmpty(result)) {
            Map<String, Object> userMap = result.get(0);
            HarborUser harborUser = new HarborUser();
            harborUser.setUserId(Integer.valueOf(userMap.get("user_id").toString()));
            harborUser.setUsername(userMap.get("username").toString());
            harborUser.setRealName(userMap.get("realname").toString());
            harborUser.setEmail(userMap.get("email").toString());
            harborUser.setComment(userMap.get("comment").toString());
            return harborUser;
        }
        return null;
    }

    @Override
    public boolean updateUserByName(HarborUser harborUser) throws Exception {
        LOGGER.info("更新harbor用户,harborUser:{}", JSONObject.toJSONString(harborUser));
        if(StringUtils.isBlank(harborUser.getUsername())){
            return false;
        }
        List<String> updateFailedHarborServer = new ArrayList<>();
        Set<HarborServer> harborServers = this.getUserAvailableHarbor(harborUser.getUsername());
        for(HarborServer harborServer : harborServers) {
            try {
                HarborUser existUser = this.getUserByName(harborServer, harborUser.getUsername());
                if (existUser == null) {
                    LOGGER.warn("更新harbor用户失败,用户不存在,  harborServer:{}", JSONObject.toJSONString(harborServer));
                    continue;
                }
                harborUser.setUserId(existUser.getUserId());
                this.updateUserById(harborServer, harborUser);
            }catch(Exception e){
                LOGGER.error("更新harbor用户失败, harborServer:{}", JSONObject.toJSONString(harborServer),e);
                updateFailedHarborServer.add(harborServer.getHarborHost());
            }
        }
        if(!CollectionUtils.isEmpty(updateFailedHarborServer)){
            LOGGER.error("更新harbor用户失败, 失败的harborServer:{}", CollectionUtil.listToString(updateFailedHarborServer));
            if(updateFailedHarborServer.size() == harborServers.size()){
                throw new MarsRuntimeException(ErrorCodeMessage.USER_HARBOR_UPDATE_FAIL);
            }else{
                throw new MarsRuntimeException(ErrorCodeMessage.USER_HARBOR_UPDATE_PART_FAIL,
                        CollectionUtil.listToString(updateFailedHarborServer), false);
            }
        }
        return true;
    }

    @Override
    public boolean updateUserById(HarborServer harborServer, HarborUser harborUser) throws Exception {
        LOGGER.info("更新harbor用户,harborServer:{},harborUser:{}",JSONObject.toJSONString(harborServer), JSONObject.toJSONString(harborUser));
        if(harborServer == null || harborUser == null || harborUser.getUserId() == null){
            return false;
        }
        Map<String, Object> params = this.convertToMapParams(harborUser);
        if(CollectionUtils.isEmpty(params)){
            LOGGER.error("用户信息没有需要更新的字段,harborServer:{},harborUser:{}",JSONObject.toJSONString(harborServer), JSONObject.toJSONString(harborUser));
            throw new MarsRuntimeException(ErrorCodeMessage.USER_UPDATE_INFO_ERROR);
        }
        String updateUrl = HarborClient.getHarborUrl(harborServer) + HARBOR_API_USERS + "/" + harborUser.getUserId();
        HttpClientResponse putRes = HarborHttpsClientUtil.doPut(updateUrl, params, HarborClient.getAdminCookieHeader(harborServer));
        // 根据返回code判断状态
        if (HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            return true;
        }
        LOGGER.error("更新harbor用户信息失败,harborUser:{},response:{}", JSONObject.toJSONString(harborUser),JSONObject.toJSONString(putRes));
        return false;
    }

    @Override
    public boolean updatePassword(String userName, String oldPassword, String newPassword) throws Exception {
        if(StringUtils.isBlank(userName) || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)){
            return false;
        }
        Set<HarborServer> harborServers = this.getUserAvailableHarbor(userName);
        for(HarborServer harborServer : harborServers) {
            try {
                HarborUser existUser = this.getUserByName(harborServer, userName);
                if (existUser == null) {
                    LOGGER.warn("更新harbor用户密码失败,用户不存在, username：{}， harborServer:{}",
                            userName, JSONObject.toJSONString(harborServer));
                    continue;
                }
                String updatePasswordApiUrl = HarborClient.getHarborUrl(harborServer)+ HARBOR_API_USERS + "/" + existUser.getUserId() + "/password";
                Map<String, Object> params = new HashMap<>();
                params.put("old_password", oldPassword);
                params.put("new_password", newPassword);
                HttpClientResponse putRes = HarborHttpsClientUtil.doPut(updatePasswordApiUrl, params, HarborClient.getAdminCookieHeader(harborServer));
                if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
                    LOGGER.error("更新harbor用户密码失败, userName:{}, harborServer:{}", userName, JSONObject.toJSONString(harborServer));
                }
            }catch(Exception e){
                LOGGER.error("更新harbor用户密码失败, harborServer:{}", JSONObject.toJSONString(harborServer),e);
            }
        }
        return true;
    }

    @Override
    public boolean deleteUserByName(String harborUserName) throws Exception {
        LOGGER.info("删除harbor用户,harborUserName:{}", harborUserName);
        if(StringUtils.isBlank(harborUserName)){
            return false;
        }
        Set<HarborServer> harborServers = this.getUserAvailableHarbor(harborUserName);
        for(HarborServer harborServer : harborServers) {
            HarborUser existUser = this.getUserByName(harborServer, harborUserName);
            if (existUser == null) {
                LOGGER.warn("删除harbor用户失败,用户不存在, harborUserName:{}", harborUserName);
                throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
            }
            return this.deleteUserById(harborServer, existUser.getUserId());
        }
        return true;
    }

    @Override
    public boolean deleteUserByName(HarborServer harborServer, String harborUserName) throws Exception {
        LOGGER.info("删除harbor用户,harborServer:{},harborUserName:{}",JSONObject.toJSONString(harborServer), harborUserName);
        if(harborServer == null || StringUtils.isBlank(harborUserName)){
            return false;
        }
        HarborUser existUser = this.getUserByName(harborServer, harborUserName);
        if(existUser == null){
            LOGGER.warn("删除harbor用户失败,用户不存在, harborUserName:{}", harborUserName);
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_EXIST);
        }
        return this.deleteUserById(harborServer, existUser.getUserId());
    }

    @Override
    public boolean deleteUserById(HarborServer harborServer, Integer harborUserId) throws Exception {
        if(harborServer == null || harborUserId == null){
            LOGGER.warn("createUser parameter error,harborServer:{},harborUserId:{}",harborServer,harborUserId);
            return false;
        }
        String deleteUserApiUrl = HarborClient.getHarborUrl(harborServer)+ HARBOR_API_USERS + "/" + harborUserId;
        HttpClientResponse deleteRes = HarborHttpsClientUtil.doDelete(deleteUserApiUrl, null, HarborClient.getAdminCookieHeader(harborServer));
        if (HttpStatusUtil.isSuccessStatus(deleteRes.getStatus())) {
            return true;
        }
        LOGGER.error("删除Harbor用户失败，response:{}", JSONObject.toJSONString(deleteRes));
        return false;
    }

    /**
     * 根据harborProjectId获取project下的成员列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<HarborUser> usersOfProject(String harborHost, Integer harborProjectId) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectId + "/members/";
        Map<String, Object>  headers = HarborClient.getAdminCookieHeader(harborServer);
        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, null);
        if (response.isSuccess() && response.get("data") != null) {
            return getHarborUserResp(response.get("data").toString());
        }

        return Collections.emptyList();

    }

    /**
     * 根据username 查询出在该project的user 权限详情
     * @param harborHost
     * @param harborProjectId
     * @param username
     * @return
     * @throws Exception
     */
    @Override
    public List<HarborUser> usersOfProjectByUsername(String harborHost, Integer harborProjectId, String username) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectId + "/members";
        Map<String, Object> params = new HashMap<>();
        params.put("username",username);
        Map<String, Object>  headers = HarborClient.getAdminCookieHeader(harborServer);
        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        if (response.isSuccess() && response.get("data") != null) {
            return getHarborUserResp(response.get("data").toString());
        }

        return Collections.emptyList();

    }

    /**
     * 创建project下的role
     *
     * @param harborProjectId  harborProjectId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createRole(String harborHost,Integer harborProjectId, HarborRole harborRole) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        AssertUtil.notNull(harborRole);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectId + "/members/";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, convertHarborRoleBeanToMap(harborRole));
        if("user is ready in project".equalsIgnoreCase((String)response.get("data"))){
            return ActionReturnUtil.returnSuccess();
        }
        return response;
    }

    /**
     * 更新project下的role
     *
     * @param harborProjectId  harborProjectId
     * @param userId     userId
     * @param harborRole role bean
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updateRole(String harborHost, Integer harborProjectId, Integer userId, HarborRole harborRole) throws Exception {
        AssertUtil.notNull(harborProjectId, DictEnum.REPOSITORY_ID);
        AssertUtil.notNull(userId, DictEnum.USER_ID);
        AssertUtil.notNull(harborRole, DictEnum.ROLE);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + harborProjectId + "/members/" + userId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, convertHarborRoleBeanToMap(harborRole));

    }

    /**
     * 删除project下的role
     *
     * @param repositoryId repositoryId
     * @param userId    userId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteRole(String harborHost, Integer repositoryId, Integer userId) throws Exception {
        AssertUtil.notNull(repositoryId, DictEnum.REPOSITORY_ID);
        AssertUtil.notNull(userId, DictEnum.USER_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/projects/" + repositoryId + "/members/" + userId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpDoDelete(url, null, headers);

    }

    @Override
    public ActionReturnUtil authUserHarborAccess(String username, String projectId, Boolean isAuthorize, Boolean isPm) throws Exception{
        AssertUtil.notBlank(username, DictEnum.USERNAME);
        AssertUtil.notBlank(projectId, DictEnum.PROJECT_ID);
        List<ImageRepository> imageRepositories = harborProjectService
                .listRepositories(projectId, null, Boolean.FALSE,Boolean.TRUE);
        Map<String, List<ImageRepository>> repositoryMap = imageRepositories.stream()
                .collect(Collectors.groupingBy(ImageRepository::getHarborHost));
        String failedHarbor = "";
        User user = userService.getUser(username);
        Integer harborRole = Constant.HARBOR_ROLE_DEVELOPER;
        if(isPm != null && isPm){
            harborRole = Constant.HARBOR_ROLE_PROJECT_ADMIN;
        }
        for(Map.Entry<String, List<ImageRepository>> entry : repositoryMap.entrySet()){
            List<Integer> harborProjectIds = entry.getValue().stream().map(ImageRepository::getHarborProjectId)
                    .collect(Collectors.toList());
            ProjectUserBinding projectUserBinding = new ProjectUserBinding();
            projectUserBinding.setUserId(user.getId());
            projectUserBinding.setHarborHost(entry.getKey());
            projectUserBinding.setUserName(username);
            projectUserBinding.setHarborRoleType(harborRole);
            projectUserBinding.setProjects(harborProjectIds);
            ActionReturnUtil response = null;
            if(isAuthorize) {
                response = this.bindingUserProjects(projectUserBinding);
            }else{
                response = this.unBindingUserProjects(projectUserBinding);
            }
            if(!response.isSuccess()){
                LOGGER.error("harbor用户授权镜像仓库失败,harborHost:{},isAuthorize{},response:{}",
                        new String[]{entry.getKey(),isAuthorize.toString() ,JSONObject.toJSONString(response)});
                failedHarbor += entry.getKey() + CommonConstant.COMMA;
            }
        }
        if(StringUtils.isNotBlank(failedHarbor)){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HARBOR_AUTH_ACCESS_FAIL,
                    failedHarbor.substring(0, failedHarbor.length()-1),false);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil bindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception {
        List<Integer> projects = projectUserBinding.getProjects();
        ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
        // 获取对象
        for (int i = 0; i < projects.size(); i++) {
            String username = projectUserBinding.getUserName();
            returnUil = bindingProjectUser(projectUserBinding.getHarborHost(), username, projects.get(i),
                    projectUserBinding.getHarborRoleType());
        }
        return returnUil;
    }
    @Override
    public ActionReturnUtil unBindingUserProjects(ProjectUserBinding projectUserBinding)throws Exception {
        List<Integer> projects = projectUserBinding.getProjects();
        ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
        // 获取对象
        for (int i = 0; i < projects.size(); i++) {
            Integer userId = Integer.valueOf(projectUserBinding.getUserId().toString());
            returnUil = unBindingProjectUser(projectUserBinding.getHarborHost(), userId, projects.get(i));
        }
        return returnUil;
    }

    @Override
    public ActionReturnUtil bindingProjectUsers(UserProjectBiding userProjectBinding)throws Exception {
        List<String> users = userProjectBinding.getUserNames();
        ActionReturnUtil returnUil = ActionReturnUtil.returnSuccess();
        // 获取对象
        for (int i = 0; i < users.size(); i++) {
            String username = users.get(i);
            returnUil = bindingProjectUser(userProjectBinding.getHarborHost(),username,
                    userProjectBinding.getHarborProjectId(), userProjectBinding.getHarborRoleType());
        }
        return returnUil;
    }

    //绑定用户到harbor的仓库project
    public ActionReturnUtil bindingProjectUser(String harborHost, String username,Integer harborProjectID, Integer harborRoleType) throws Exception{
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        HarborUser harborUser = this.getUserByName(harborServer,username);
        if(harborUser == null){
            Integer harborUserId = this.createUser(harborServer, userService.getUser(username));
            if(harborUserId == null){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_FAIL,DictEnum.HARBOR_USER.phrase(),true);
            }
        }
        Map<String, Object> bodys = new HashMap<>();
        Map<String, Object> header = new HashMap<>();
        header.put("Content-type", "application/json");
        List<Integer> roleType = new ArrayList<Integer>();
        roleType.add(harborRoleType == null? Constant.HARBOR_ROLE_DEVELOPER : harborRoleType);
        HarborRole harborRole = new HarborRole();
        harborRole.setUsername(username);
        harborRole.setRoleList(roleType);
        return this.createRole(harborHost, harborProjectID, harborRole);

    }
    //解除用户绑定到harbor的仓库project
    public ActionReturnUtil unBindingProjectUser(String harborHost, Integer userId,Integer projectID) throws Exception{
        return this.deleteRole(harborHost, projectID, userId);
    }


    @Override
    public Set<HarborServer> getUserAvailableHarbor(String username) throws Exception{
        Set<HarborServer> harborServers = new HashSet<>();

        List<Cluster> clusters = roleLocalService.getClusterListByUsername(username);
        if(CollectionUtils.isEmpty(clusters)){
            return harborServers;
        }
        for(Cluster cluster : clusters){
            harborServers.add(cluster.getHarborServer());
        }
        return harborServers;
    }

    @Override
    public Set<HarborServer> getCurrentUserAvailableHarbor() throws Exception{
        return this.getUserAvailableHarbor(userService.getCurrentUsername());
    }

    /**
     * harborRole bean 转换为 map
     *
     * @param harborRole harborRole bean
     * @return map
     */
    private Map<String, Object> convertHarborRoleBeanToMap(HarborRole harborRole) throws Exception{
        Map<String, Object> map = new HashMap<>();
        if (harborRole != null) {
            if (!CollectionUtils.isEmpty(harborRole.getRoleList())) {
                Object[] roleArr = harborRole.getRoleList().toArray();
                map.put("roles", roleArr);
            }
            if (StringUtils.isNotEmpty(harborRole.getUsername())) {
                map.put("username", harborRole.getUsername());
            }
        }
        return map;
    }

    private Map<String, Object> convertToMapParams(HarborUser harborUser){
        Map<String, Object> params = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(harborUser.getRealName())) {
            params.put("realname", harborUser.getRealName());
        }
        if(StringUtils.isNotBlank(harborUser.getEmail())) {
            params.put("email", harborUser.getEmail());
        }
        return params;
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
                    if (map.get("user_id") != null) {
                        harborUser.setUserId(Integer.valueOf(map.get("user_id").toString()));
                    }
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

}
