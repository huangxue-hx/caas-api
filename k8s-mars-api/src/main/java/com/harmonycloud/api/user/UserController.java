package com.harmonycloud.api.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.dto.user.SummaryUserInfo;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.tenant.UserTenantService;
import com.harmonycloud.service.user.MessageService;
import com.harmonycloud.service.user.ResourceService;
import com.harmonycloud.service.user.RoleService;
import com.harmonycloud.service.user.UserService;


@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService; 
	
	@Autowired
	UserTenantService userTenantService;
	
	@Autowired
    ResourceService resourceService;
	@Autowired
    MessageService messageService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private HttpSession session;
	
	@Value("#{propertiesReader['superadmin.username']}")
	private String superAdmin;
	
	@Autowired
	private TenantService tenantService;

	@Autowired
	ClusterService clusterService;
	/**
	 * 新增用户
	 * @param user
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/adduser", method = RequestMethod.POST)
	public ActionReturnUtil addUser(@RequestParam(value="userName") String userName,@RequestParam(value="Password") String password,
			@RequestParam(value="email") String email,@RequestParam(value="realName") String realName,@RequestParam(value="Comment") String comment) throws Exception {
		User user = new User();
		user.setUsername(userName);
		user.setPassword(password);
		user.setEmail(email);
		user.setRealName(realName);
		if(comment==null ||!"undefined".equals(comment)){
			user.setComment(comment);
		}
		return userService.addUser(user);
	}
	
	/**
	 * 修改用户密码
	 * @param newPassword
	 * @param oldPassword
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/changePwd", method = RequestMethod.PUT)
	public ActionReturnUtil changePwd(@RequestParam(value = "newPassword") final String newPassword,
			@RequestParam(value = "oldPassword") final String oldPassword,@RequestParam(value="userName") final String userName) throws Exception {
		return userService.changePwd(userName, oldPassword, newPassword);
	}
	
	/**
	 * 修改用户realname
	 * @param userName
	 * @param realName
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/changeRealName", method = RequestMethod.PUT)
	public ActionReturnUtil changeRealName(@RequestParam(value = "realName") final String realName,@RequestParam(value="userName") final String userName) throws Exception {
		return userService.changeRealName(userName, realName);
	}
	
	/**
	 * 修改用户email
	 * @param userName
	 * @param email
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/changeEmail", method = RequestMethod.PUT)
	public ActionReturnUtil changeEmail(@RequestParam(value = "email") final String email,@RequestParam(value="userName") final String userName) throws Exception {
		return userService.changeEmail(userName, email);
	}
	
	/**
	 * 重置admin登入密码
	 * @param newPassword
	 * @param oldPassword
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/adminReset", method = RequestMethod.PUT)
	public ActionReturnUtil adminReset(@RequestParam(value = "newPassword") final String newPassword,
			@RequestParam(value = "oldPassword") final String oldPassword,@RequestParam(value="userName") final String userName) throws Exception {
	    Object user = session.getAttribute("username");
        if(user == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
       if(!CommonConstant.ADMIN.equals(user.toString())){
           ActionReturnUtil.returnErrorWithMsg("管理员才能重置密码");
       }
	    return userService.adminReset(userName, oldPassword, newPassword);
	}
	
	/**
	 * 重置用户密码
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value = "/user/userReset", method = RequestMethod.PUT)
	public ActionReturnUtil userReset(@RequestParam(value="userName") final String userName,@RequestParam(value="newPassword") final String newPassword) throws Exception {
	    Object user = session.getAttribute("username");
        if(user == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
       if(!CommonConstant.ADMIN.equals(user.toString())){
           ActionReturnUtil.returnErrorWithMsg("管理员才能重置密码");
       }
	    return userService.userReset(userName,newPassword);
	}
	
	
	/**
	 * 删除用户
	 * @param userName
	 * @return
	 * @throws Exception 
	 */
	@ResponseBody
	@RequestMapping(value="/user/deleteUser",method=RequestMethod.DELETE)
	public ActionReturnUtil deleteUser(@RequestParam(value="userName") final String userName) throws Exception{
		return userService.deleteUser(userName);
	}
	
	/**
	 * 获取用户权限绑定的明细信息
	 * @param username
	 * @return
	 */
	@RequestMapping(value="/rest/rolebinding/user",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil userDetail(@RequestParam(value="user") final String username) throws Exception{
		 List<UserDetailDto> userDetail = userService.userDetail(username);
		 if(userDetail != null){
			 return ActionReturnUtil.returnSuccessWithData(userDetail); 
		 }
		return ActionReturnUtil.returnError();
	}
	
	/**
	 * 获取所有机器账号
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/clusterrolebinding/machineList",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil machineList() throws Exception{
		return userService.listMachineUsers();
	}
	
	/**
	 * 获取所有管理员
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/clusterrolebinding/adminList",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil adminList() throws Exception{
		return userService.listAdmin();
	}
	
	/**
	 * 用户列表
	 * 如果没有用户名则查询所有用户,
	 * 如果有用户名,则查询该用户
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/userlist",method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil userList() throws Exception{
	    return userService.listUsers();  
	}
		
	/**
	 * 获取当前用户
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/currentuser", method=RequestMethod.GET)
	public ActionReturnUtil getCurrentuser() {
		 try {
			logger.info("获取当前用户");
			Object user = session.getAttribute("username");
			if(user == null){
			    throw new K8sAuthException(Constant.HTTP_401);
			}
			String userName = user.toString();
			String userId = session.getAttribute("userId").toString();
			Map<String, Object> res = new HashMap<String, Object>();
			res.put("username", userName);
			res.put("userId", userId);
			if (userName.equals(superAdmin)) {
				ActionReturnUtil tenants = tenantService.tenantList(null,null);
				res.put("tenants", tenants.get("data"));
			}
			
			//判断是否是admin
			String isAdmin = session.getAttribute("isAdmin").toString();
			if ("1".equals(isAdmin)) {
				ActionReturnUtil tenants = tenantService.tenantList(null,null);
				res.put("tenants", tenants.get("data"));
			} else {
			    ActionReturnUtil listTenantsByUserName = tenantService.listTenantsByUserName(userName, false);
//			    ActionReturnUtil roleBindingByUser = roleService.getRoleBindingByUser(userName);
			    if (((List) listTenantsByUserName.get(CommonConstant.DATA)).size()>0) {
			        res.put("tenants",(List) listTenantsByUserName.get(CommonConstant.DATA));
			    }else{
			        res.put("tenants",null);
			    }
			}
			return ActionReturnUtil.returnSuccessWithData(res);
		} catch (Exception e) {
			logger.error("获取当前用户错误， e:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/currentNamespaces", method=RequestMethod.GET)
	public ActionReturnUtil getCurrentNamespace(@RequestParam(value="tenantid") final String tenantId) {
		try {
			logger.info("获取在特定的tenantid下的namespaces");
			Object name = session.getAttribute("username");
            if(name == null){
                throw new K8sAuthException(Constant.HTTP_401);
            }
			String userName = name.toString();
			String isAdmin = session.getAttribute("isAdmin").toString();
			if (userName.equals(superAdmin) || "1".equals(isAdmin)) {
				return tenantService.tenantdetail(tenantId);
			} else {
				return roleService.getRoleBindingWithNamespace(userName, tenantId);
			}
		} catch (Exception e) {
			logger.error("获取在特定的tenantid下的namespaces错误，tenantId="+tenantId+", e:"+e.getMessage());
			return ActionReturnUtil.returnError();
		}
	}

	/**
	 * 在session中设置当前租户的租户和集群信息
	 * @param tenantid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/currentTenant", method = RequestMethod.GET)
	public @ResponseBody ActionReturnUtil currentTenant(@RequestParam(value = "tenantid") String tenantid) throws Exception {
		if(session.getAttribute("username") == null){
		    throw new K8sAuthException(Constant.HTTP_401);
		}

		Cluster cluster = this.tenantService.getClusterByTenantid(tenantid);
		session.setAttribute("currentCluster",cluster);

		return ActionReturnUtil.returnSuccess();

	}
	@RequestMapping(value = "/user/getMenu", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getMenu() throws Exception {
	    Object name = session.getAttribute("username");
        if(name == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        String userName = name.toString();
        User user = userService.getUser(userName);
        List<Map<String, Object>> menu = new ArrayList<>();
        if(user.getIsAdmin()==1){
             menu = resourceService.listAdminMenu();
        }else{
            menu = resourceService.listDevMenu();
        }
        
        return ActionReturnUtil.returnSuccessWithData(menu);

    }
	/**
	 * 更新用户状态为pause
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/user/updateUserStatusPause", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil updateUserStatusPause(@RequestParam(value = "username") String username) throws Exception {
        User user = userService.updateUserStatus(username, CommonConstant.PAUSE);
        if(user==null){
            throw new MarsRuntimeException("更新用户pause状态失败");
        }
        return ActionReturnUtil.returnSuccess();

    }
	/**
     * 更新用户状态为normal
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/user/updateUserStatusNormal", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil updateUserStatusNormal(@RequestParam(value = "username") String username) throws Exception {
        User user = userService.updateUserStatus(username, CommonConstant.NORMAL);
        if(user==null){
            throw new MarsRuntimeException("更新用户normal状态失败");
        }
        return ActionReturnUtil.returnSuccess();

    }
    /**
     * 更改普通用户为admin
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/user/updateUserToAdmin", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil updateUserToAdmin(@RequestParam(value = "username") String username) throws Exception {
        Object user = session.getAttribute("username");
        if(user == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        int isadmin = (Integer)session.getAttribute("isAdmin");
        if(isadmin!=1){
            throw new MarsRuntimeException("admin用户才能操作");
        }
        User user2 = userService.updateUserToAdmin(username,1);
        if(user2==null){
            throw new MarsRuntimeException("更新用户状态失败");
        }
        return ActionReturnUtil.returnSuccess();

    }
    @RequestMapping(value = "/user/updateAdminToNormal", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil updateAdminToNormal(@RequestParam(value = "username") String username) throws Exception {
        Object user = session.getAttribute("username");
        if(user == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        int isadmin = (Integer)session.getAttribute("isAdmin");
        if(isadmin!=1){
            throw new MarsRuntimeException("admin用户才能操作");
        }
       if(username.equals(user.toString())){
           ActionReturnUtil.returnErrorWithMsg("管理员不能操作自己账户");
       }
        User user2 = userService.updateUserToAdmin(username,0);
        if(user2==null){
            throw new MarsRuntimeException("更新用户状态失败");
        }
        return ActionReturnUtil.returnSuccess();

    }
    /**
     * 获取所有pause的用户
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/user/getAllUserPausedList", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAllUserPausedList() throws Exception {
        List<User> list = userService.getAllUserPausedList();
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    /**
     * 获取所有normal的用户
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/user/getAllUserNormalList", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAllUserNormalList() throws Exception {
        List<User> list = userService.getAllUserNormalList();
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    /**
     * 获取30天以内活跃的用户
     * @param username
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/user/getActiveUserList", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getActiveUserList(@RequestParam(value = "domain") Integer domain) throws Exception {
        List<User> list = userService.getActiveUserList(domain);
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    @RequestMapping(value = "/user/getUnauthorizedUserList", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getUnauthorizedUserList() throws Exception {  
        List<User> list = userService.getUnauthorizedUserList();
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    @RequestMapping(value = "/user/getAllSummary", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAllSummary(@RequestParam(value = "domain") Integer domain) throws Exception {
        int isadmin = (Integer)session.getAttribute("isAdmin");
          if(isadmin!=1){
          throw new MarsRuntimeException("admin用户才能查看未授权用户");
      }
        SummaryUserInfo allSummary = userService.getAllSummary(domain);
        return ActionReturnUtil.returnSuccessWithData(allSummary);

    }
    @RequestMapping(value = "/user/getAdminList", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getAdminList() throws Exception {
        int isadmin = (Integer)session.getAttribute("isAdmin");
          if(isadmin!=1){
          throw new MarsRuntimeException("admin用户才能查看admin用户列表");
      }
        List<User> list = userService.getAdminUserList();
        return ActionReturnUtil.returnSuccessWithData(list);

    }
    @RequestMapping(value = "/user/testTime", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil getActiveUserListuu(@RequestParam(value = "domain") Integer domain) throws Exception {
        List<TenantBinding> list = tenantService.testTime(domain);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    
    @RequestMapping(value = "/user/group/create_group", method = RequestMethod.POST)
    public @ResponseBody ActionReturnUtil create_group(@ModelAttribute UserGroup usergroup ) throws Exception {
    	userService.create_group(usergroup);
    	return ActionReturnUtil.returnSuccess();
    }
    
    @RequestMapping(value = "/user/group/delete_group", method = RequestMethod.DELETE)
    public @ResponseBody ActionReturnUtil delete_group(@RequestParam("groupnames[]") List<String> groupnames ) throws Exception {
    	userService.delete_group(groupnames);
    	return ActionReturnUtil.returnSuccess();
    }
    
    @RequestMapping(value = "/user/group/delete_groupbyid", method = RequestMethod.DELETE)
    public @ResponseBody ActionReturnUtil delete_groupbyid(@RequestParam("groupid") int groupid ) throws Exception {
    	userService.delete_groupbyid(groupid);
    	return ActionReturnUtil.returnSuccess();
    }
    
    @RequestMapping(value = "/user/group/search_group", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil get_groups() throws Exception {
    	List<UserGroup> list = userService.get_groups();
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    
    @RequestMapping(value = "/user/group/samegroupname", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil issame(@RequestParam("groupname")String groupname) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(userService.issame(groupname));
    }
    
    @RequestMapping(value = "/user/group/searchuserbygroupid", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil searchuserbygroupid(@RequestParam("groupid")int groupid) throws Exception {
    	List<User> list =  userService.searchuserbygroupid(groupid);
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    
    @RequestMapping(value = "/user/group/search_group_username", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil search_group_username(@RequestParam("username")String username) throws Exception {
    	UserGroup usergroup =  userService.search_group_username(username);
        return ActionReturnUtil.returnSuccessWithData(usergroup);
    }
    
    @RequestMapping(value = "/user/group/search_users_groupname", method = RequestMethod.GET)
    public @ResponseBody ActionReturnUtil search_users_groupname(@RequestParam("groupname")String groupname) throws Exception {
    	List<User> users =  userService.search_users_groupname(groupname);
        return ActionReturnUtil.returnSuccessWithData(users);
    }
    
}