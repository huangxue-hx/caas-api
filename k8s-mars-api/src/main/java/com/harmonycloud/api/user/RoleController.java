package com.harmonycloud.api.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.harmonycloud.dao.tenant.bean.RolePrivilege;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dao.user.bean.RoleExample;
import com.harmonycloud.service.tenant.RolePrivilegeService;
import com.harmonycloud.service.tenant.impl.RoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.service.user.ResourceService;
import com.harmonycloud.service.tenant.RoleService;
//import com.harmonycloud.service.user.RoleService;
import com.harmonycloud.service.user.impl.ResourceServiceimpl;

import javax.xml.bind.MarshalException;


@Controller
public class RoleController {
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private ResourceService resourceService;

	@Autowired
	private RolePrivilegeService rolePrivilegeService;


	/**
	 * 更新角色权限
	 * @param roleName
	 * @param rolePrivilegeList
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/updateRolePrivilege", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateRolePrivilege(@RequestBody Map<String, Object> map) throws Exception {
		List<Map<String, Object>> list = (List<Map<String, Object>>)map.get("rolePrivilegeList");
		String roleName = (String)map.get("roleName");
		Role role = roleService.getRoleByRoleName(roleName);
		String description = (String)map.get("description");
		if(description!=null&&!description.isEmpty()&&!description.equals(role.getDescription())){
			role.setUpdateTime(new Date());
			role.setDescription(description);
			roleService.updateRole(role);
		}
//	        rolePrivilegeList
		if(list==null||list.size()==0){
			return ActionReturnUtil.returnSuccess();
		}else {
			rolePrivilegeService.updateRolePrivilege(roleName, list);
			return ActionReturnUtil.returnSuccess();
		}
	}
	/**
	 * 根据角色名获取所有可用权限菜单列表
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/getRolePrivilegeMenu", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRolePrivilegeMenu(String roleName) throws Exception {
		long startTime=System.currentTimeMillis();   //获取开始时间
		Map<String, Object> privilegeByRole = rolePrivilegeService.getAllStatusPrivilegeMenuByRoleName(roleName);
		long endTime=System.currentTimeMillis(); //获取结束时间
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms");
		return ActionReturnUtil.returnSuccessWithData(privilegeByRole);
	}

	/**
	 * 根据角色名获取权限
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/getRolePrivilege", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRolePrivilege(String roleName) throws Exception {
		Map<String, Object> privilegeByRole = rolePrivilegeService.getPrivilegeByRole(roleName);
		return ActionReturnUtil.returnSuccessWithData(privilegeByRole);
	}

	/**
	 * 重置所有角色及权限
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/resetRole", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil resetRole() throws Exception {
		roleService.resetRole();
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 重置对应角色权限
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/resetPrivilegeByRoleName", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil resetPrivilegeByRoleName(String roleName) throws Exception {
		//TODO
		rolePrivilegeService.resetRolePrivilegeByRoleName(roleName);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 更新菜单权重
	 * @param id
	 * @param weight
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/updateRoleMenuPrivilegeWight", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateRoleMenuPrivilegeWight(@RequestBody Map<String, Object> map) throws Exception {
		List<Map<Integer,Integer>> weightList = (List<Map<Integer,Integer>>)map.get("weightList");
		for (Map<Integer,Integer> weight : weightList) {
			resourceService.updateRoleMenuPrivilegeWight(weight.get("id"),weight.get("weight"));
		}
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 根据用户名和tenantid获取角色
	 * @param userName
	 * @param tenantid
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/getRoleByUserNameAndTenantId", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil getRoleByUserNameAndTenant(String userName,String tenantid) throws Exception {
		roleService.getRoleByUserNameAndTenant(userName,tenantid);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据角色名删除角色
	 * @param roleName
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/deleteRole", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteRole(@RequestParam(value = "roleName") String roleName) throws Exception {
		roleService.deleteRole(roleName);
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 添加角色
	 * @param role
	 * @throws Exception
	 */
	/**
	 * 添加角色
	 * @param role
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/addRole", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil addRole(@RequestBody Map<String, Object> map) throws Exception {
		List<Map<String, Object>> NewRolePrivilegeList = (List<Map<String, Object>>)map.get("rolePrivilegeList");
		String roleName = (String)map.get("name");
		String description = (String)map.get("description");
		if(description==null||description.isEmpty()){
			description=roleName;
		}
		Role roleByRoleName = roleService.getRoleByRoleName(roleName);
		if(roleByRoleName != null  && roleByRoleName.getAvailable() == Boolean.TRUE){
			throw new MarshalException("创建的角色："+ roleName + "已经存在,请重新输入!");
		}
		Role role = new Role();
		role.setName(roleName);
		role.setDescription(description);
		role.setAvailable(Boolean.TRUE);
		role.setCreateTime(new Date());
		role.setUpdateTime(new Date());
		roleService.addRole(role,NewRolePrivilegeList);
		return ActionReturnUtil.returnSuccess();
	}
	/**

	/**
	 * 禁用角色
	 * @param roleName
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/disableRoleByRoleName", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil disableRoleByRoleName(String roleName) throws Exception {
		roleService.DisableRoleByRoleName(roleName);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 启用角色
	 * @param roleName
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/enableRoleByRoleName", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil enableRoleByRoleName(String roleName) throws Exception {
		roleService.EnableRoleByRoleName(roleName);
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 根据角色名获取Role
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/role/getRoleByRoleName", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRoleByRoleName(String roleName) throws Exception {
		Role role= roleService.getRoleByRoleName(roleName);
		return ActionReturnUtil.returnSuccessWithData(role);
	}

	/**
	 * 获取所有用户角色列表
	 * @return
	 */
	@RequestMapping(value = "/role/list", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRoleList() throws Exception {
		List<Role> roleList = roleService.getRoleList();
		return ActionReturnUtil.returnSuccessWithData(roleList);
	}


	/**
	 * 获取所有资源操作列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/resources",method=RequestMethod.GET)
	public ActionReturnUtil listResources() throws Exception{
		return resourceService.listAPIResource();
	}

	/**
	 * 获取非资源性api列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/noneResources",method=RequestMethod.GET)
	public ActionReturnUtil listNoneResources() throws Exception {
		return resourceService.listNoneResource();
	}



	/**
	 * 获取所有clusterRole列表
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/clusterroleList",method=RequestMethod.GET)
//	public ActionReturnUtil clusterRoleList() throws Exception{
//		ActionReturnUtil response = roleService.listClusterRoles();
//		return response;
//	}
	
	/**
	 * 根据namespace和role名称返回role明细信息
	 * @param namespace
	 * @param name
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/roleDetail",method=RequestMethod.GET)
	public ActionReturnUtil roleDetail(@RequestParam(value="namespace") final String namespace,@RequestParam(value="name") final String roleName) throws Exception{
		return null;
	}
	
	/**
	 * 根据clusterRole name获取角色权限明细
	 * @param roleName
	 * @return 
	 * @throws Exception
	 */
//	@ResponseBody
//	@RequestMapping(value="/clusterroleDetail",method=RequestMethod.GET)
//	public ActionReturnUtil clusterRoleDetail(@RequestParam(value="name") final String roleName) throws Exception{
//		ActionReturnUtil response = roleService.getClusterRoleDetail(roleName);
//		return response;
//	}
	
	/**
	 * 绑定用户和角色
	 * @param tenantname
	 * @param tenantid
	 * @param namespace
	 * @param role
	 * @param username
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/rolebinding/user",method=RequestMethod.POST)
//	public ActionReturnUtil rolebind(@RequestParam(value="tenantname") final String tenantname,@RequestParam(value="tenantid") final String tenantid,
//			@RequestParam(value="namespace") final String namespace,@RequestParam(value="role") final String role,@RequestParam(value="user") final String username) throws Exception{
//		ActionReturnUtil response = roleService.rolebinding(tenantname,tenantid,namespace,role,username);
//		return response;
//	}
	
	
	/**
	 * 解绑用户和角色
	 * @param tenantname
	 * @param tenantid
	 * @param namespace
	 * @param role
	 * @param username
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/rolebinding/user",method=RequestMethod.DELETE)
//	public ActionReturnUtil roleUnbind(@RequestParam(value="tenantname") final String tenantname,@RequestParam(value="tenantid") final String tenantid,
//			@RequestParam(value="namespace") final String namespace,@RequestParam(value="role") final String role,@RequestParam(value="user") final String username) throws Exception{
//		return roleService.roleUnbind(tenantname,tenantid,namespace,role,username);
//	}
	
	/**
	 * 获取rolebing列表
	 * @return
	 */
//	@ResponseBody
//	@RequestMapping(value="/rolebinding/user",method=RequestMethod.GET)
//	public ActionReturnUtil listRolebing(@RequestParam(value="user") final String username) throws Exception{
//		List<UserDetailDto> roleBindingList = roleService.userDetail(username);
//		//String data = JsonUtil.objectToJson(roleBindingList);
//		return ActionReturnUtil.returnSuccessWithData(roleBindingList);
//	}
	
//	/**
//	 * 绑定租户管理员
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value="/rolebinding/tm",method=RequestMethod.POST)
//	public ActionReturnUtil rolebindingTm(@RequestParam(value="tenantid") final String tenantid,@RequestParam(value="user") final String username) throws Exception{
//		try {
//			roleService.rolebindingTM(tenantid,username);
//			return ActionReturnUtil.returnSuccess();
//		} catch (Exception e) {
//			e.printStackTrace();
//			return ActionReturnUtil.returnError();
//		}
//	}
//
//	/**
//	 * 解绑租户管理员
//	 * @param tenantid
//	 * @param username
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value="/rolebinding/tm",method=RequestMethod.DELETE)
//	public ActionReturnUtil roleUnbingdingTm(@RequestParam(value="tenantid") final String tenantid,@RequestParam(value="user") final String username) throws Exception{
//		roleService.roleUnbindingTM(tenantid,username);
//		return ActionReturnUtil.returnSuccess();
//	}
//	/**
//	 * 删除clusterrole
//	 * @return
//	 */
//	@ResponseBody
//	@RequestMapping(value="/clusterroles",method=RequestMethod.DELETE)
//	public ActionReturnUtil deleteClusterrole(@RequestParam(value="name") String name) throws Exception{
//		return this.roleService.deleteClusterrole(name);
//	}
//
	
}
