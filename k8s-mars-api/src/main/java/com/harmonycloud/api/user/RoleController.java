package com.harmonycloud.api.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.LocalRolePreConditionDto;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.user.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.common.PrivilegeHelper;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.harmonycloud.common.util.ActionReturnUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 角色权限控制器
 */
@Controller
public class RoleController {

	@Autowired
	private RoleLocalService roleLocalService;

	@Autowired
	private ResourceService resourceService;
	@Autowired
	private ResourceMenuRoleService resourceMenuRoleService;

	@Autowired
	private RolePrivilegeService rolePrivilegeService;
    @Autowired
	private LocalRoleService localRoleService;
	@Autowired
	ClusterCacheManager clusterCacheManager;

    @Autowired
	PrivilegeHelper privilegeHelper;

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 切换角色
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/switchRole", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil switchRole(@PathVariable("roleId") Integer roleId) throws Exception {
		Map<String, Object> availablePrivilege = this.rolePrivilegeService.switchRole(roleId);
		return ActionReturnUtil.returnSuccessWithData(availablePrivilege);
	}
	/**
	 * 分配角色权限
	 *  使用场景：
	 *  1、首次创建角色后勾选资源
	 *  2、再次更改菜单
	 *  问题：
	 *  当某菜单被其中项目的pm分配过数据权限后，发生2时，会导致数据权限不可用。
	 *  后台处理策略：
	 *  删除该全局角色权限菜单，不删除数据权限
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateRole(@PathVariable(value = "roleId") Integer roleId,
												@ModelAttribute RoleDto roleDto) throws Exception {
//		logger.info("分配角色权限");
		//空值判断
		if (StringUtils.isBlank(roleDto.getNickName()) || (roleId > CommonConstant.PM_ROLEID && StringUtils.isBlank(roleDto.getClusterIds()))){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}

		roleDto.setId(roleId);
		roleLocalService.updateRole(roleDto);
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 根据角色id获取所有可用权限菜单列表
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/menu", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRolePrivilegeMenu(@PathVariable(value = "roleId") Integer roleId) throws Exception {
//		long startTime=System.currentTimeMillis();   //获取开始时间
		List<MenuDto> menuList = resourceMenuRoleService.getResourceMenuByRoleId(roleId);
//		long endTime=System.currentTimeMillis(); //获取结束时间
		return ActionReturnUtil.returnSuccessWithData(menuList);
	}

	/**
	 * 根据角色名获取有效权限
	 * @param roleId
	 * @param available true表示获取有效权限，false表示获取所有权限
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/privilege", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRolePrivilege(@PathVariable(value = "roleId") Integer roleId,
											 Boolean available) throws Exception {
		//空值判断
		if (Objects.isNull(roleId) || Objects.isNull(available)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		Map<String, Object> privilegeByRole = null;
		if (available){
			//获取有效权限
			privilegeByRole = rolePrivilegeService.getAvailablePrivilegeByRoleId(roleId);
		}else {
			//表示获取所有权限
			privilegeByRole = rolePrivilegeService.getAllPrivilegeByRoleId(roleId);
		}
		return ActionReturnUtil.returnSuccessWithData(privilegeByRole);
	}

	/**
	 * 获取基本可见权限操作列表
	 * 组装成无勾选状态的权限树
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/privilege", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getInitialPrivilege() throws Exception {
//		logger.info("获取系统基本权限操作列表");
		Map<String, Object> privilegeByRole = rolePrivilegeService.getAllSystemPrivilege();
		return ActionReturnUtil.returnSuccessWithData(privilegeByRole);
	}
	/**
	 * 更新菜单权重
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/menu", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateRoleMenuPrivilegeWight(@PathVariable(value = "roleId") Integer roleId,
														 @RequestBody Map<String, Object> map) throws Exception {
		List<Map<Integer,Integer>> weightList = (List<Map<Integer,Integer>>)map.get("weightList");
		if (Objects.isNull(roleId) || CollectionUtils.isEmpty(weightList)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		for (Map<Integer,Integer> weight : weightList) {
			resourceService.updateRoleMenuPrivilegeWight(roleId,weight.get("id"),weight.get("weight"));
		}
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据角色id获取集群列表
	 * 从redis缓存中获取
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/clusters", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getClusterListByRoleId(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		if (Objects.isNull(roleId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<Cluster> clusterList = roleLocalService.getClusterListByRoleId(roleId);
		return ActionReturnUtil.returnSuccessWithData(clusterList);
	}

	/**
	 * 根据角色id删除角色
	 * @param roleId
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteRole(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		if (Objects.isNull(roleId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		roleLocalService.deleteRoleById(roleId);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 添加角色
	 * @param roleDto
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil addRole(@ModelAttribute RoleDto roleDto) throws Exception {
		String nickName = roleDto.getNickName();
		String clusterIds = roleDto.getClusterIds();
		//空值判断
		if (StringUtils.isBlank(nickName)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		if (!Objects.isNull(roleDto.getAvailable())
				&& roleDto.getAvailable()
				&& (StringUtils.isBlank(clusterIds)
				|| StringUtils.isBlank(clusterIds.replaceAll(CommonConstant.COMMA,CommonConstant.EMPTYSTRING)))){
			throw new MarsRuntimeException(ErrorCodeMessage.ROLE_SCOPE_NOT_BLANK);
		}
		if (CollectionUtils.isEmpty(roleDto.getRolePrivilegeList())){
			throw new MarsRuntimeException(ErrorCodeMessage.ROLE_PRIVILEGE_NOT_BLANK);
		}
		roleLocalService.createRole(roleDto);
		return ActionReturnUtil.returnSuccess();
	}
	/**

	/**
	 * 禁用角色
	 * @param roleId
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/disable", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil disableRoleByRoleId(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		//空值判断
		if (Objects.isNull(roleId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		roleLocalService.disableRoleByRoleId(roleId);
		return ActionReturnUtil.returnSuccess();
	}
	@RequestMapping(value = "/roles/{roleId}/copy", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil copyRoleByRoleId(@PathVariable(value = "roleId") Integer roleId,String newNickName) throws Exception {
		//空值判断
		if (Objects.isNull(newNickName)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		roleLocalService.copyRoleByRoleId(roleId,newNickName);
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 启用角色
	 * @param roleId
	 * @param clusterIds 某一集群ID，必填
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/enable", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil enableRoleByRoleId(@PathVariable(value = "roleId") Integer roleId,
											   @RequestParam(value = "clusterIds") String clusterIds) throws Exception {
		//空值判断
		if (StringUtils.isBlank(clusterIds)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		roleLocalService.enableRoleByRoleId(roleId,clusterIds);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据角色id重置角色权限（只针对默认初始角色admin tm pm dev qas ops）
	 * 通过默认角色副本数据库重置role_privilege_new_replication
	 *
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles/{roleId}/privilege/reset", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil resetRolePrivilegeByRoleId(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		//空值判断
		if (Objects.isNull(roleId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		this.rolePrivilegeService.resetRolePrivilegeByRoleId(roleId);
		return ActionReturnUtil.returnSuccess();
	}
	/**
	 * 根据用户名,租户id和项目id获取角色 或者 根据status获取用户角色列表
	 * status true获取用户所有角色列表，false获取用户有效角色列表
	 * @param username
	 * @param projectId
	 * @param status true获取用户所有角色列表，false获取用户有效角色列表
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/roles", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getRoleListByUsernameAndTenantIdAndProjectId(String username,String tenantId,String projectId,Boolean status) throws Exception {
		if (Objects.isNull(status) && StringUtils.isAnyBlank(username,projectId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<Role> list = null;
		if (StringUtils.isNotBlank(username)){
			list = roleLocalService.getRoleListByUsernameAndTenantIdAndProjectId(username,tenantId, projectId);
		} else if (status){
			list = roleLocalService.getAllRoleList();
		}else {
			list = roleLocalService.getAvailableRoleList();
		}

		return ActionReturnUtil.returnSuccessWithData(list);
	}

	/**
	 * 创建局部角色
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createLocalRole(@ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if(StringUtils.isAnyBlank(localRoleDtoIn.getProjectId(), localRoleDtoIn.getRoleDesc())){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleService.createLocalRole(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 更新局部角色
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateLocalRole(@PathVariable(value = "roleId") Integer roleId,
											@ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if(Objects.isNull(roleId)){
			throw new MarsRuntimeException(ErrorCodeMessage.ROLE_ID_BLANK);
		}
		localRoleDtoIn.setLocalRoleId(roleId);
		localRoleService.updateLocalRole(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据角色名删除局部角色
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/projects/{projectId}/users/{roleName}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil removeRoleByRoleName(@PathVariable(value = "roleName") String roleName,
												 @PathVariable(value = "projectId") String projectId) throws Exception {
		if(StringUtils.isAnyBlank(projectId, roleName)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleService.removeRoleByRoleName(projectId, roleName);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据角色名查询局部角色
	 * @param projectId
	 * @param roleName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleName}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listLocalRoleByRoleName(@PathVariable(value = "roleName") String roleName,
													@RequestParam(value = "projectId") String projectId) throws Exception {
		if(StringUtils.isAnyBlank(projectId, roleName)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<LocalRole> localRoles = localRoleService.listLocalRoleByRoleName(projectId, roleName);
		return ActionReturnUtil.returnSuccessWithData(localRoles);
	}

	/**
	 * 查询局部角色
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/projects/{projectId}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listLocalRole(@PathVariable(value = "projectId") String projectId) throws Exception {
		if(StringUtils.isAnyBlank(projectId)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<LocalRole> localRoles = localRoleService.listLocalRoleByRoleName(projectId, null);
		return ActionReturnUtil.returnSuccessWithData(localRoles);
	}

	/**
	 * 根据用户名查询局部角色
	 *
	 * @param projectId
	 * @param username
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/projects/{projectId}/users/{username}", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listRoleByUserName(@PathVariable(value = "projectId") String projectId,
											   @PathVariable(value = "username")  String username) throws Exception {
		if(StringUtils.isAnyBlank(projectId, username)){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<LocalRole> localRoles = localRoleService.listRoleByUserName(projectId, username);
		return ActionReturnUtil.returnSuccessWithData(localRoles);
	}

	/**
	 * 根据id删除局部角色
	 *
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteLocalRoleById(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		if (Objects.isNull(roleId) || 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleService.deleteLocalRoleById(roleId);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 为角色增加某种资源类型（如应用）的规则
	 *
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/rules", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil addResourceRule(@PathVariable(value = "roleId") Integer roleId,
											@ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if (Objects.isNull(roleId) || 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleDtoIn.setLocalRoleId(roleId);
		localRoleService.addResourceRule(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 修改某种资源类型（如应用）的规则
	 *
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/rules", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateResourceRule(@PathVariable(value = "roleId") Integer roleId,
											   @ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if (Objects.isNull(roleId) || 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleDtoIn.setLocalRoleId(roleId);
		localRoleService.updateResourceRule(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据roleId查询资源类型规则列表
	 *
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/rules", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listResourceRuleByRoleId(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		if (Objects.isNull(roleId) || 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		List<LocalPrivilege> localPrivileges = localRoleService.listResourceRuleByRoleId(roleId);
		return ActionReturnUtil.returnSuccessWithData(localPrivileges);
	}

	/**
	 * 根据id删除资源类型规则
	 *
	 * @param roleId
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/rules", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deleteResourceRuleById(@PathVariable(value = "roleId") Integer roleId) throws Exception {
		if (Objects.isNull(roleId) || 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleService.deleteResourceRuleById(roleId);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 更新局部角色的用户（成员）
	 *
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/users", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil assignRoleForUser(@PathVariable(value = "roleId") Integer roleId,
											  @ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		localRoleDtoIn.setLocalRoleId(roleId);
		localRoleService.assignRoleToUser(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 为角色分配数据权限
	 *
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/privilege", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil assignPrivilege(@PathVariable(value = "roleId") Integer roleId,
											@ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if (Objects.isNull(roleId)
				|| 0 == roleId
				|| StringUtils.isAnyBlank(localRoleDtoIn.getResourceType())){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleDtoIn.setLocalRoleId(roleId);
		localRoleService.assignPrivilege(localRoleDtoIn);
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 查询资源权限实例列表
	 *
	 * @param localRoleDtoIn
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/{roleId}/privilege", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listPrivileges(@PathVariable(value = "roleId") Integer roleId,
										   @ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if (Objects.isNull(roleId)
				|| 0 == roleId){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleDtoIn.setLocalRoleId(roleId);
		List<LocalRolePrivilege> localRolePrivileges = localRoleService.listPrivileges(localRoleDtoIn);
		return ActionReturnUtil.returnSuccessWithData(localRolePrivileges);
	}

	/**
	 * 根据resourceType查询资源规则
	 *
	 * @param localRoleDtoIn 必填：ProjectId, UserName, ResourceType
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/resourcetypes/{resourceType}/rules", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listResurceRuleByType(@PathVariable(value = "resourceType") String resourceType,
												  @ModelAttribute LocalRoleDto localRoleDtoIn) throws Exception {
		if (StringUtils.isAnyBlank(localRoleDtoIn.getProjectId(),localRoleDtoIn.getUserName(),localRoleDtoIn.getResourceType())){
			throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
		}
		localRoleDtoIn.setResourceType(resourceType);
		List<LocalPrivilege> localPrivileges = localRoleService.listResurceRuleByType(localRoleDtoIn);
		return ActionReturnUtil.returnSuccessWithData(localPrivileges);
	}

	/**
	 * 根据项目、用户查询权限实例
	 *
	 * @param projectId
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/projects/{projectId}/users/{userName}/privileges", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil  listPrivilegeByProject(@PathVariable(value = "projectId") String projectId,
													@PathVariable(value = "userName") String userName) throws Exception {
		List<LocalRolePrivilege> localRolePrivileges = localRoleService.listPrivilegeByProject(projectId, userName);
		return ActionReturnUtil.returnSuccessWithData(localRolePrivileges);
	}

	/**
	 * 查询角色下的用户成员
	 *
	 */
	@RequestMapping(value = "/localroles/userNames", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil  listUserByRole(@RequestParam(value = "projectId", required = false) String projectId,
													@RequestParam(value = "roleId", required = false) Integer roleId) throws Exception {
		LocalRoleDto localRoleDtoIn = new LocalRoleDto();
		localRoleDtoIn.setProjectId(projectId);
		localRoleDtoIn.setLocalRoleId(roleId);
		List<LocalUserRoleRel> localRolePrivileges = localRoleService.listUserByRole(localRoleDtoIn);
		return ActionReturnUtil.returnSuccessWithData(localRolePrivileges);
	}



	/**
	 * 查询数据权限支持的所有条件，供管理员选择使用
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/localroles/conditions", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listAllPreConditions() throws Exception {
		List<LocalRolePreConditionDto> localRolePreConditionDtos = localRoleService.listAllPreConditions();
		return ActionReturnUtil.returnSuccessWithData(localRolePreConditionDtos);
	}

	@RequestMapping(value = "/localroles/test", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil test() throws Exception {
		PrivilegeApplicationFieldDto privilegeApplicationFieldDto = new PrivilegeApplicationFieldDto();
		privilegeApplicationFieldDto.setNameInSelectApp("app_test");
		List<PrivilegeApplicationFieldDto> privilegeApplicationFieldDtos = new ArrayList<>();
		privilegeApplicationFieldDtos.add(privilegeApplicationFieldDto);
		PrivilegeApplicationFieldDto privilegeApplicationFieldDto1 = new PrivilegeApplicationFieldDto();
		privilegeApplicationFieldDto1.setNameInSelectApp("a_test");
		privilegeApplicationFieldDtos.add(privilegeApplicationFieldDto1);
		return ActionReturnUtil.returnSuccessWithData(privilegeHelper.matchAny(privilegeApplicationFieldDtos));
	}
	@RequestMapping(value = "/roles/initHarborRole", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil initHarborRole() throws Exception {
		this.roleLocalService.initHarborRole();
		return ActionReturnUtil.returnSuccess();
	}
}
