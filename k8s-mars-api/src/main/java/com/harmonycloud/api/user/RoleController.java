package com.harmonycloud.api.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.user.UserDetailDto;
import com.harmonycloud.service.user.ResourceService;
import com.harmonycloud.service.user.RoleService;
import com.harmonycloud.service.user.impl.ResourceServiceimpl;


@Controller
public class RoleController {
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private ResourceService resourceService;
	
	/**
	 * 获取所有clusterRole列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/clusterroleList",method=RequestMethod.GET)
	public ActionReturnUtil clusterRoleList() throws Exception{
		ActionReturnUtil response = roleService.listClusterRoles();
		return response;
	}
	
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
	@ResponseBody
	@RequestMapping(value="/clusterroleDetail",method=RequestMethod.GET)
	public ActionReturnUtil clusterRoleDetail(@RequestParam(value="name") final String roleName) throws Exception{
		ActionReturnUtil response = roleService.getClusterRoleDetail(roleName);
		return response;
	}
	
	/**
	 * 绑定用户和角色
	 * @param tenantname
	 * @param tenantid
	 * @param namespace
	 * @param role
	 * @param username
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/rolebinding/user",method=RequestMethod.POST)
	public ActionReturnUtil rolebind(@RequestParam(value="tenantname") final String tenantname,@RequestParam(value="tenantid") final String tenantid,
			@RequestParam(value="namespace") final String namespace,@RequestParam(value="role") final String role,@RequestParam(value="user") final String username) throws Exception{
		ActionReturnUtil response = roleService.rolebinding(tenantname,tenantid,namespace,role,username);
		return response;
	}
	
	
	/**
	 * 解绑用户和角色
	 * @param tenantname
	 * @param tenantid
	 * @param namespace
	 * @param role
	 * @param username
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/rolebinding/user",method=RequestMethod.DELETE)
	public ActionReturnUtil roleUnbind(@RequestParam(value="tenantname") final String tenantname,@RequestParam(value="tenantid") final String tenantid,
			@RequestParam(value="namespace") final String namespace,@RequestParam(value="role") final String role,@RequestParam(value="user") final String username) throws Exception{
		return roleService.roleUnbind(tenantname,tenantid,namespace,role,username);
	}
	
	/**
	 * 获取rolebing列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/rolebinding/user",method=RequestMethod.GET)
	public ActionReturnUtil listRolebing(@RequestParam(value="user") final String username) throws Exception{
		List<UserDetailDto> roleBindingList = roleService.userDetail(username);
		//String data = JsonUtil.objectToJson(roleBindingList);
		return ActionReturnUtil.returnSuccessWithData(roleBindingList);
	}
	
	/**
	 * 绑定租户管理员
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/rolebinding/tm",method=RequestMethod.POST)
	public ActionReturnUtil rolebindingTm(@RequestParam(value="tenantid") final String tenantid,@RequestParam(value="user") final String username) throws Exception{
		try {
			roleService.rolebindingTM(tenantid,username);
			return ActionReturnUtil.returnSuccess();
		} catch (Exception e) {
			e.printStackTrace();
			return ActionReturnUtil.returnError();
		}
	}
	
	/**
	 * 解绑租户管理员
	 * @param tenantid
	 * @param username
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/rolebinding/tm",method=RequestMethod.DELETE)
	public ActionReturnUtil roleUnbingdingTm(@RequestParam(value="tenantid") final String tenantid,@RequestParam(value="user") final String username) throws Exception{
		roleService.roleUnbindingTM(tenantid,username);
		return ActionReturnUtil.returnSuccess();
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
	 * 删除clusterrole
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/clusterroles",method=RequestMethod.DELETE)
	public ActionReturnUtil deleteClusterrole(@RequestParam(value="name") String name) throws Exception{
		return this.roleService.deleteClusterrole(name);
	}
	
	
}
