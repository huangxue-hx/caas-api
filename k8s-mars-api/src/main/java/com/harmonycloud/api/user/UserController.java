package com.harmonycloud.api.user;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dto.user.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.user.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.service.tenant.TenantService;

@Controller
@Api(description = "用户相关操作")
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ResourceService resourceService;
    @Autowired
    private MessageService messageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HttpSession session;

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private RolePrivilegeService rolePrivilegeService;


    /**
     * 是否为系统管理员
     *
     * @param userName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{userName}", method = RequestMethod.GET)
    public ActionReturnUtil isSystemAdmin(@PathVariable(value = "userName") final String userName) throws Exception{
        return userService.isSystemAdmin(userName);
    }

    /**
     * 新增用户
     * 
     * @param user
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addUser(@ModelAttribute User user) throws Exception{
        return userService.addUser(user);
    }

    /**
     * 修改用户密码
     * 
     * @param newPassword
     * @param oldPassword
     * @param userName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}/password", method = RequestMethod.PUT)
    public ActionReturnUtil changePwd(@RequestParam(value = "newPassword") final String newPassword, @RequestParam(value = "oldPassword") final String oldPassword,
            @PathVariable(value = "username") final String userName) throws Exception{
        Object user = session.getAttribute("username");
        if (!userName.equals(user.toString())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.USER_PASSWORD_CHANGE_SELF);
        }
        return userService.changePwd(userName, oldPassword, newPassword);
    }



    /**
     * 修改用phone
     *
     * @param userName
     * @param phone
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}/phone", method = RequestMethod.PUT)
    public ActionReturnUtil changePhone(@RequestParam(value = "phone") final String phone, @PathVariable(value = "username") final String userName) throws Exception{
        return userService.changePhone(userName, phone);
    }

    /**
     * 修改用户realname
     * 
     * @param userName
     * @param realName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}/realname", method = RequestMethod.PUT)
    public ActionReturnUtil changeRealName(@RequestParam(value = "realName") final String realName, @PathVariable(value = "username") final String userName) throws Exception{
        return userService.changeRealName(userName, realName);
    }

    /**
     * 修改用户email
     * 
     * @param userName
     * @param email
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}/email", method = RequestMethod.PUT)
    public ActionReturnUtil changeEmail(@RequestParam(value = "email") final String email, @PathVariable(value = "username") final String userName) throws Exception{
        return userService.changeEmail(userName, email);
    }

    /**
     * 重置用户密码
     *
     * @param userName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}/password/reset", method = RequestMethod.PUT)
    public ActionReturnUtil resetPassword(@PathVariable(value = "username") final String userName) throws Exception {
        Object user = session.getAttribute("username");
        if (!userService.isAdmin(user.toString())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.ONLY_FOR_MANAGER);
        }
        return userService.resetUserPwd(userName);
    }


    /**
     * 删除用户
     * 
     * @param userName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{username}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteUser(@PathVariable(value = "username") final String userName) throws Exception{
        return userService.deleteUser(userName);
    }

    /**
     * 获取用户权限绑定的明细信息
     * 
     * @param username
     * @return
     */
    @RequestMapping(value = "/{username}/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil detailUser(@PathVariable(value = "username") final String username) throws Exception{

        List<UserDetailDto> userDetail = userService.userDetail(username);
        if (userDetail != null) {
            return ActionReturnUtil.returnSuccessWithData(userDetail);
        }
        return ActionReturnUtil.returnError();

    }


    @ApiOperation(value = "查询用户列表", notes = "根据条件筛选用户列表")
    @RequestMapping( method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listUser(@ModelAttribute UserQueryDto userQueryDto) throws Exception{
        return userService.listUsers(userQueryDto);
    }

    /**
     * 获取当前用户
     * 1、从单点服务器同步用户信息至容器云平台数据库user
     * 2、用户信息写入session
     * 3、获取租户列表tenants
     * 4、返回用户信息userId、username、isAdmin、tenants
     * @return
     */
    @SuppressWarnings("unchecked")
    @ResponseBody

    @RequestMapping(value = "/current", method = RequestMethod.GET)
    public ActionReturnUtil getCurrentuser(HttpServletRequest request, HttpServletResponse response,
                                           @RequestParam(value = "isLogin", required = false) boolean isLogin) throws Exception {
        Map res = userService.getcurrentUser(request, response);
        return ActionReturnUtil.returnSuccessWithData(res);
    }

    /**
     * 获取资源菜单
     * 1、从session获取用户名
     * 2、当角色未就位时，返回空资源，等待前端下次调用
     * 3、如果是系统管理员，返回所有有效菜单
     * 4、如果是其他角色，返回其可见菜单
     * @return
     */
    @RequestMapping(value = "/menu", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getMenu(String roleName) throws Exception {
        Object name = session.getAttribute("username");
        if (name == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USER_NOT_LOGIN);
        }
        Boolean getMenu =(Boolean) session.getAttribute("getMenu");
        if(getMenu!=null  &&  !getMenu) {
                return ActionReturnUtil.returnSuccess();
        }
        List<Map<String, Object>> menu = new ArrayList<>();

        if (StringUtils.isEmpty(roleName)){
            String userName = name.toString();
            User user = userService.getUser(userName);
            if (user.getIsAdmin() == 1) {
                menu = resourceService.listMenuByRole("admin");
            } else {
                Object Id = session.getAttribute("tenantId");
                if(Id != null) {
                    String tenantId = Id.toString();
//                    String role = userTenantService.findRoleByName(userName,tenantId);
                    String role = "admin";//TODO 后续角色部分做
                    menu = resourceService.listMenuByRole(role);
                }
            }
        }else {
            menu = resourceService.listAllMenuByRole(roleName);
        }

        long endTime=System.currentTimeMillis(); //获取结束时间
        return ActionReturnUtil.returnSuccessWithData(menu);

    }
    /**
     * 更新用户状态为pause
     * 
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{username}/status", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateUserStatus(@PathVariable(value = "username") String username, @RequestParam(value = "status") String status ) throws Exception{
        if (!(CommonConstant.PAUSE.equals(status) || CommonConstant.NORMAL.equals(status))) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_FAIL);
        }
        User user = userService.updateUserStatus(username, status);
        if (user == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_FAIL);
        }
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 更新用户类型
     *
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{username}/type", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateUserType(@PathVariable(value = "username") String username, @RequestParam(value = "type") String type) throws Exception{
        String currentUsername = this.userService.getCurrentUsername();
        if (StringUtils.isBlank(currentUsername)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.USER_NOT_LOGIN);
        }
        User user = this.userService.getUser(currentUsername);
        if (user.getIsAdmin() != 1) {
            throw new MarsRuntimeException(ErrorCodeMessage.ONLY_FOR_MANAGER);
        }
        User user2 = null;
        if ("0".equals(type) ) {
            if (username.equals(user.getUsername())) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CANNOT_OPERATE_YOURSELF);
            }
            user2  = userService.updateUserToAdmin(username, 0);
        } else if ("1".equals(type)){
            user2 = userService.updateUserToAdmin(username, 1);
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_USER_TYPE);
        }

        if (user2 == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.OPERATION_FAIL);
        }
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 获取所有pause的用户
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/status/pause", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getAllUserPausedList() throws Exception{
        List<User> list = userService.getAllUserPausedList();
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 获取部门pause的用户
     * 
     * @param department
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/departments/{department}/status/pause", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getUserPausedListByDepartmnet(@PathVariable(value = "department") String department) throws Exception{
        List<User> list = userService.getUserPausedListByDepartmnet(department);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 获取所有normal的用户
     * 
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/status/normal", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNormalUsers() throws Exception{
        List<User> list = userService.getAllUserNormalList();
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 获取部门normal的用户
     * 
     * @param department
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/departments/{department}/status/normal", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNormalUsersByDepartmnet(@PathVariable(value = "department") String department) throws Exception{
        List<User> list = userService.getUserNormalListByDepartmnet(department);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 获取domain天以内活跃的用户
     * 
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/status/active", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listActiveUsers(@RequestParam(value = "domain") Integer domain) throws Exception {
        List<User> list = userService.getActiveUserList(domain);
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    /**
     * 根据部门获取domain天以内活跃的用户
     * 
     * @param domain
     * @param department
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/departments/{department}/status/active", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listActiveUsersByDepartmnet(@RequestParam(value = "domain") Integer domain, @PathVariable(value = "department") String department)
            throws Exception {
        List<User> list = userService.getActiveUserListByDepartmnet(domain, department);
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    /**
     * 获取所有部门未授权的用户
     * 
     * @param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/status/unauthorized", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listUnauthorizedUsers() throws Exception {
        List<User> list = userService.getUnauthorizedUserList();
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 根据部门获取未授权的用户
     * 
     * @param department
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/departments/{department}/status/unauthorized", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listUnauthorizedUsersByDepartmnet(@PathVariable(value = "department") String department) throws Exception {
        List<User> list = userService.getUnauthorizedUserListByDepartmnet(department);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    @RequestMapping(value = "/status/summary", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listSummaryUsers(@RequestParam(value = "domain") Integer domain) throws Exception {
        if (!userService.checkCurrentUserIsAdmin()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.ONLY_FOR_MANAGER);
        }
        SummaryUserInfo allSummary = userService.getAllSummary(domain);
        return ActionReturnUtil.returnSuccessWithData(allSummary);

    }
    @RequestMapping(value = "/departments/{department}/status/summary", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listSummaryUsersByDepartmnet(@RequestParam(value = "domain") Integer domain, @PathVariable(value = "department") String department)
            throws Exception {
        if (!userService.checkCurrentUserIsAdmin()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.ONLY_FOR_MANAGER);
        }
        SummaryUserInfo allSummary = userService.getSummaryByDepartmnet(domain, department);
        return ActionReturnUtil.returnSuccessWithData(allSummary);

    }
    @RequestMapping(value = "/status/admin", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listAdminUsers() throws Exception {
        if (!userService.checkCurrentUserIsAdmin()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.ONLY_FOR_MANAGER);
        }
        List<User> list = userService.getAdminUserList();
        return ActionReturnUtil.returnSuccessWithData(list);

    }


    @RequestMapping(value = "/groups", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addGroups(@ModelAttribute UserGroup usergroup) throws Exception {
    	return  userService.createGroup(usergroup);
    }

//    @RequestMapping(value = "/{username}/groups", method = RequestMethod.DELETE)
//    public @ResponseBody ActionReturnUtil delete_group(@RequestParam("groupnames[]") List<String> groupnames) throws Exception {
//    	return userService.delete_group(groupnames);
//    }

    @RequestMapping(value = "/groups/{groupid}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteGroups(@PathVariable("groupid") int groupid) throws Exception {
    	return  userService.deleteGroupbyId(groupid);
    }
    
    @RequestMapping(value = "/groups/{groupid}", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateGroup(@ModelAttribute UserGroupDto usergroupdto) throws Exception {
    	return userService.updateGroup(usergroupdto);
    }

    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listGroups() throws Exception {
        List<UserGroup> list = userService.get_groups();
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    @RequestMapping(value = "/groups/same", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil checkGroupIssame(@RequestParam("groupname") String groupname) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(userService.issame(groupname));
    }

//    @RequestMapping(value = "/{username}/groups/{groupid}/users", method = RequestMethod.GET)
//    public @ResponseBody ActionReturnUtil searchuserbygroupid(@PathVariable("groupid") int groupid) throws Exception {
//        List<User> list = userService.searchuserbygroupid(groupid);
//        return ActionReturnUtil.returnSuccessWithData(list);
//    }

//    @RequestMapping(value = "/{username}/groups/{groupid}/", method = RequestMethod.GET)
//    public @ResponseBody ActionReturnUtil search_group_username(@PathVariable("username") String username) throws Exception {
//        UserGroup usergroup = userService.search_group_username(username);
//        return ActionReturnUtil.returnSuccessWithData(usergroup);
//    }

    @RequestMapping(value = "/{username}/group/searchgroup", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getGroupbyUsername(@PathVariable("username") String username) throws Exception {
        return userService.searchGroupByUsername(username);
    }

    @RequestMapping(value = "/groups/searchuser", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listUsersByGroup(@RequestParam("groupname") String groupname) throws Exception {
        List<User> users = userService.searchUsersGroupname(groupname);
        return ActionReturnUtil.returnSuccessWithData(users);
    }
    
	@RequestMapping(value="/withoutgroup",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listUsersByGroup() throws Exception{
	    return userService.listUserswithoutgroup();  
	}
	
	@RequestMapping(value="/usersfile/export",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil exportFile(HttpServletRequest req,HttpServletResponse resp) throws Exception{
		return  userService.fileexport(req,resp);  
	}
    
	@RequestMapping(value="/usersfile/import",method=RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil impoerFile(@RequestParam(value="file") MultipartFile file) throws Exception{
        /*//获取上传的文件
        MultipartHttpServletRequest multipart = (MultipartHttpServletRequest) request;
        //获得文件
        MultipartFile file = multipart.getFile("file");*/
        //获得数据流
        InputStream in = file.getInputStream();
        //数据导入
        userService.userBulkUpload(in,file);
        in.close();
        return ActionReturnUtil.returnSuccess();   
	}

    @RequestMapping(value="/switchLanguage",method=RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil switchLanguage(@RequestParam(value="language") String language) throws Exception{
        session.setAttribute("language", language);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 用户信息数据同步，from crowd to mars
     *
     * @return
     */
    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil syncUser(@RequestBody List<UserSyncDto> userSyncDtoList) {
        if (!CollectionUtils.isEmpty(userSyncDtoList)) {
            Map<Integer, List<UserSyncDto>> operateType2User = userSyncDtoList.stream().collect(Collectors.groupingBy(UserSyncDto::getOperateType));
            List<UserSyncDto> userSyncDtoInsert = operateType2User.get(1);
            if (!CollectionUtils.isEmpty(userSyncDtoInsert)) {
                try {
                    userService.batchInsert(userSyncDtoInsert);
                } catch (Exception e1) {
                    for (UserSyncDto userSyncDto : userSyncDtoInsert) {
                        try {
                            userService.insertUser(userSyncDto);
                        } catch (Exception e2) {
                            User user = new User();
                            user.setUsername(userSyncDto.getUsername());
                            user.setRealName(userSyncDto.getRealName());
                            user.setEmail(userSyncDto.getEmail());
                            user.setPhone(userSyncDto.getPhone());
                            user.setCrowdUserId(userSyncDto.getCrowdUserId());
                            userService.updateByUserName(user);
                        }
                    }
                }
            }
            userService.updateByCrowdUserId(operateType2User.get(2));
            if (operateType2User.get(3) != null && operateType2User.get(3).size() > 0) {
                userService.batchDeleteByCrowdUserId(operateType2User.get(3).stream().map(UserSyncDto::getCrowdUserId).collect(Collectors.toList()));
            }
        }
        return ActionReturnUtil.returnSuccess();
    }
}
