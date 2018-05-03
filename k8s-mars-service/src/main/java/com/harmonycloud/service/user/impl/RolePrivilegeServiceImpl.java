package com.harmonycloud.service.user.impl;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.SsoClient;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.RolePrivilegeMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dao.user.ResourceMapper;
import com.harmonycloud.dto.user.MenuDto;
import com.harmonycloud.dto.user.PrivilegeDto;
import com.harmonycloud.dto.user.RoleDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.serviceImpl.infrastructure.NodeServiceImpl;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.*;
import com.harmonycloud.service.tenant.RoleService;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpSession;

/**
 * Created by zgl on 2017/8/10.
 */
@Service("rolePrivilegeService")
@Transactional(rollbackFor = Exception.class)
public class RolePrivilegeServiceImpl implements RolePrivilegeService {

    @Autowired
    private RolePrivilegeMapper rolePrivilegeMapper;
    @Autowired
    RoleLocalService roleLocalService;
    @Autowired
    ResourceMenuRoleService resourceMenuRoleService;
    @Autowired
    private PrivilegeService privilegeService;
    @Autowired
    private RolePrivilegeReplicationService rolePrivilegeReplicationService;
    @Autowired
    private HttpSession session;
    @Autowired
    ProjectService projectService;
    @Autowired
    UserService userService;
    @Autowired
    ResourceMenuService resourceMenuService;

    private static final Logger log = LoggerFactory.getLogger(RolePrivilegeServiceImpl.class);
    //租户管理员角色id
    private static final Integer TM_ROLEID = 2;
    //租户
    private static final String TENANT = "tenant";
    //交付中心
    private static final String DELIVERY = "delivery";
    //应用中心
    private static final String APPCENTER = "appcenter";
    //持续集成交付
    private static final String CICD = "cicd";
    //日志中心
    private static final String LOG = "log";
    //告警中心
    private static final String ALARM = "alarm";
    //租户管理
    private static final String TENANTMGR = "tenantmgr";
    private static final String BASIC = "basic";
    //项目管理
    private static final String PROJECTMGR = "projectmgr";
    //应用服务
    private static final String APP = "app";
    //守护进程服务
    private static final String DAEMONSET = "daemonset";
    //配置文件
    private static final String CONFIGMAP = "configmap";
    //存储
    private static final String VOLUME = "volume";
    //外部服务
    private static final String EXTERNALSERVICE = "externalservice";
    //CICD流水线
    private static final String CICDMGR = "cicdmgr";
    //流水线配置管理
    private static final String ENV = "env";
    //应用日志
    private static final String APPLOG = "applog";
    //审计日志
    private static final String AUDITLOG = "auditlog";
    //日志备份
    private static final String SNAPSHOTRULE = "snapshotrule";
    //日志备份
    private static final String SYSTEMLOG = "systemlog";
    //告警规则
    private static final String ALARMRULE = "alarmrule";
    //告警处理
    private static final String ALARMHANDLE = "alarmhandle";
    //镜像仓库
    private static final String REPOSITORY = "repository";
    //模板
    private static final String TEMPLATE = "template";
    //应用商店
    private static final String ONLINESHOP = "onlineshop";
    //租户管理菜单
    private static final int TENANTMGRMENU = 3;
    //我的租户菜单
    private static final int MYTENANTMENU = 4;
    //我的项目菜单
    private static final int MYPROJECTMENU = 5;
    /**
     * 切换角色
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> switchRole(Integer roleId) throws Exception {
        //获取当前租户id，项目id以及用户名
        String currentTenantId = this.userService.getCurrentTenantId();
        String username = this.userService.getCurrentUsername();
        String currentProjectId = this.userService.getCurrentProjectId();
        Role role = this.roleLocalService.getRoleById(roleId);
        if ( org.apache.commons.lang3.StringUtils.isBlank(username)){
            SsoClient.dealHeader(session);
            throw new MarsRuntimeException(ErrorCodeMessage.USER_NOT_AUTH_OR_TIMEOUT);
        }
        List<Role> availableRoleList = this.roleLocalService.getRoleListByUsernameAndTenantIdAndProjectId(username, currentTenantId, currentProjectId);
        //检查切换的角色是否在用户能切换的角色范围之内
        boolean contains = availableRoleList.contains(role);
        if (!contains){
            SsoClient.dealHeader(session);
            throw new MarsRuntimeException(ErrorCodeMessage.SWITCH_ROLE_INCORRECT);
//            throw new MarsRuntimeException(ErrorCodeMessage.SWITCH_ROLE_INCORRECT,role.getNickName(),Boolean.TRUE);
        }
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> availablePrivilege = this.getAvailablePrivilegeByRoleId(roleId);
        for (Map.Entry<String,Object> entry : availablePrivilege.entrySet()) {
            Map<String,List<Privilege>> mapModule = (Map<String,List<Privilege>>)entry.getValue();
            Map<String, Object> resource = new HashMap<>();
            //获取当前模块的所有的资源
            if (!CollectionUtils.isEmpty(mapModule)){
                for (Map.Entry<String, List<Privilege>> entryResource : mapModule.entrySet()) {
                    List<Privilege> privilegeList = (List<Privilege>)entryResource.getValue();
                    //添加模块下资源权限信息
                    Map<String, Object> privilegeMap = new HashMap<>();
                    //权限初始为false
                    privilegeMap.put(CommonConstant.CREATE,Boolean.FALSE);
                    privilegeMap.put(CommonConstant.UPDATE,Boolean.FALSE);
                    privilegeMap.put(CommonConstant.GET,Boolean.FALSE);
                    privilegeMap.put(CommonConstant.DELETE,Boolean.FALSE);
                    privilegeMap.put(CommonConstant.EXECUTE,Boolean.FALSE);
                    for (Privilege privilege:privilegeList) {
                        String privilegeStr = privilege.getPrivilege();
                        switch (privilegeStr){
                            case CommonConstant.CREATE:
                                privilegeMap.put(CommonConstant.CREATE,Boolean.TRUE);
                                break;
                            case CommonConstant.UPDATE:
                                privilegeMap.put(CommonConstant.UPDATE,Boolean.TRUE);
                                break;
                            case CommonConstant.GET:
                                privilegeMap.put(CommonConstant.GET,Boolean.TRUE);
                                break;
                            case CommonConstant.DELETE:
                                privilegeMap.put(CommonConstant.DELETE,Boolean.TRUE);
                                break;
                            case CommonConstant.EXECUTE:
                                privilegeMap.put(CommonConstant.EXECUTE,Boolean.TRUE);
                                break;
                        }
                    }
//                    privilegeList.stream().forEach(privilege ->{privilegeMap.put(CommonConstant.PRIVILEGE,privilege.getPrivilege());});
                    resource.put(entryResource.getKey(),privilegeMap);
                }
            }
            if (!Objects.isNull(resource)){
                //添加模块信息
                result.put(entry.getKey(),resource);
            }
        }
        session.setAttribute(CommonConstant.PRIVILEGE,result);
        session.setAttribute(CommonConstant.ROLEID,roleId);
        if (roleId == TM_ROLEID){
            //如果为租户管理员，设置项目信息到session中
            List<Project> projects = this.projectService.listTenantProjectByUsername(currentTenantId, username);
            Map<String, Project> projectMap = projects.stream().collect(Collectors.toMap(Project::getProjectId, project -> project));
//            projects.stream().map(Project::getProjectId).collect(Collectors.toMap(Project::getProjectId,))
            session.setAttribute(CommonConstant.PROJECT,projectMap);
        }
        return result;
    }

    @Override
    public void updateRolePrivilege(Integer roleId, List<PrivilegeDto> rolePrivilegeList) throws Exception {
        for (PrivilegeDto rolePrivilege : rolePrivilegeList) {
            Integer id = rolePrivilege.getId();
            Boolean status = rolePrivilege.getStatus();
            RolePrivilege rolePrivilegeById = this.getRolePrivilegeById(id);
            if (Objects.isNull(rolePrivilegeById) || !roleId.equals(rolePrivilegeById.getRoleId())){
                throw new MarsRuntimeException(ErrorCodeMessage.PRIVILEGE_NOT_EXIST);
            }
            if(status^rolePrivilegeById.getStatus()){
                rolePrivilegeById.setStatus(status);
                this.updateRolePrivilege(rolePrivilegeById);
            }
            //权限同步对应角色菜单
            Integer pid = rolePrivilegeById.getPid();
            this.syncRoleMenu(roleId,pid,status);
        }
        this.updateRoleMenu(roleId);
    }
    public void syncRoleMenu(Integer roleId,Integer privilegeId,Boolean status) throws Exception{
        //获取权限
        Privilege privilege = this.privilegeService.getPrivilegeById(privilegeId);
        String module = privilege.getModule();
        String secondResource = privilege.getResource();
        Map<String, Object> availablePrivilege = this.getAvailablePrivilegeByRoleId(roleId);
        Map<String, Object> moduleMap = (Map<String, Object>)availablePrivilege.get(module);
        List<ResourceMenu> resourceMenuList = this.resourceMenuService.getResourceMenuListByModule(module);
        if (CollectionUtils.isEmpty(moduleMap)){
            //模块下所有菜单更新状态
            for (ResourceMenu menu:resourceMenuList) {
                this.updateRoleMenu(roleId,menu.getId(),status,Boolean.TRUE,null);
            }
            return;
        }
        String resource = null;
        ResourceMenuRole resourceMenuRole = null;
        ResourceMenu resourceMenu = null;
        Integer rmid = null;
        Map<String,List> resourceMap = new HashMap<>();
        List privilegeList = null;
        switch (module){
            case TENANT:
                //如果为租户管理权限
                List basic = (List) moduleMap.get(BASIC);
                resourceMap.put(BASIC,basic);
                List tenantmgr = (List) moduleMap.get(TENANTMGR);
                resourceMap.put(TENANTMGR,tenantmgr);
                List projectmgr = (List) moduleMap.get(PROJECTMGR);
                resourceMap.put(PROJECTMGR,projectmgr);
                if (BASIC.equals(secondResource)){
                    //租户管理
                    resourceMenu = resourceMenuList.get(0);
                }else if (TENANTMGR.equals(secondResource)){
                    //我的租户
                    resourceMenu = resourceMenuList.get(CommonConstant.NUM_ONE);
                }else if (PROJECTMGR.equals(secondResource)){
                    //我的项目
                    resourceMenu = resourceMenuList.get(CommonConstant.NUM_TWO);
                }else {
                    return;
                }
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    resource = secondResource;
//                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                    this.updateRoleMenu(roleId,resourceMenu.getId(),status,Boolean.FALSE,resourceMap);
                }
                break;
            case APPCENTER:
                resourceMenu = resourceMenuList.get(0);
                //如果为应用中心权限
                List app = (List) moduleMap.get(APP);
                resourceMap.put(APP,app);
                List daemonSet = (List) moduleMap.get(DAEMONSET);
                resourceMap.put(DAEMONSET,daemonSet);
                List volume = (List) moduleMap.get(VOLUME);
                resourceMap.put(VOLUME,volume);
                List configMap = (List) moduleMap.get(CONFIGMAP);
                resourceMap.put(CONFIGMAP,configMap);
                List externalService = (List) moduleMap.get(EXTERNALSERVICE);
                resourceMap.put(EXTERNALSERVICE,externalService);
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //应用
                    resource = secondResource;
                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                }
                break;
            case CICD:
                //如果为CICD权限
                resourceMenu = resourceMenuList.get(0);
                List cicdmgr = (List) moduleMap.get(CICDMGR);
                resourceMap.put(CICDMGR,cicdmgr);
                List env = (List) moduleMap.get(ENV);
                resourceMap.put(ENV,env);
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //CICD
                    resource = secondResource;
                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                }
                break;
            case DELIVERY:
                //如果为交付中心权限
                resourceMenu = resourceMenuList.get(0);
                List onlineshop = (List) moduleMap.get(ONLINESHOP);
                resourceMap.put(ONLINESHOP,onlineshop);
                List template = (List) moduleMap.get(TEMPLATE);
                resourceMap.put(TEMPLATE,template);
                List repository = (List) moduleMap.get(REPOSITORY);
                resourceMap.put(REPOSITORY,repository);
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //交付中心
                    resource = secondResource;
                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                }
                break;
            case LOG:
                //如果为日志中心权限
                resourceMenu = resourceMenuList.get(0);
                List applog = (List) moduleMap.get(APPLOG);
                resourceMap.put(APPLOG,applog);
                List auditlog = (List) moduleMap.get(AUDITLOG);
                resourceMap.put(AUDITLOG,auditlog);
                List snapshotrule = (List) moduleMap.get(SNAPSHOTRULE);
                resourceMap.put(SNAPSHOTRULE,snapshotrule);
                List systemlog = (List) moduleMap.get(SYSTEMLOG);
                if (!CollectionUtils.isEmpty(systemlog)){
                    resourceMap.put(SYSTEMLOG,systemlog);
                }
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //日志中心
                    resource = secondResource;
                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                }
                break;
            case ALARM:
                //如果为告警中心权限
                resourceMenu = resourceMenuList.get(0);
                List alarmrule = (List) moduleMap.get(ALARMRULE);
                List alarmhandle = (List) moduleMap.get(ALARMHANDLE);
                resourceMap.put(ALARMRULE,alarmrule);
                resourceMap.put(ALARMHANDLE,alarmhandle);
                privilegeList = resourceMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //告警中心
                    resource = secondResource;
                    this.updateSubRoleMenu(roleId,resourceMenu.getId(),status,resource);
                }
                break;
            default:
                resourceMenu = resourceMenuList.get(0);
                privilegeList = (List) moduleMap.get(secondResource);
                if ((!status && CollectionUtils.isEmpty(privilegeList) || status && !CollectionUtils.isEmpty(privilegeList))){
                    //其他模块
                    rmid = resourceMenu.getId();
                    resourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, rmid);
                    if (resourceMenuRole.getAvailable()^status){
                        this.resourceMenuRoleService.updateResourceMenuRole(roleId,rmid,status);
                    }
                }
                break;
        }
    }
    private void updateRoleMenu(Integer roleId,Integer rmid,Boolean status,Boolean isUpdateSubMenu,Map<String,List> resourceMap)throws Exception{
        Map<Integer, ResourceMenuRole> tenantMenuRole = null;
        if (rmid >= TENANTMGRMENU && rmid <= MYPROJECTMENU){
            //为租户模块处理我的租户，租户管理，我的项目(我的租户，租户管理，我的项目同时只能出现一个)
            tenantMenuRole = this.resourceMenuRoleService.getResourceTenantMenuRole(roleId);
            ResourceMenuRole resourceMenuRole = null;
            switch (rmid){
                case TENANTMGRMENU :
                    if (status){
                        //租户管理 模块，把我的租户，我的项目设置为false
                        this.updateResourceMenuRole(tenantMenuRole,MYTENANTMENU,Boolean.FALSE);
                        this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.FALSE);
                    } else {
                        //租户管理 模块，如果我的租户为空，我的项目设置为true
                        if (Objects.isNull(resourceMap)){
                            this.updateResourceMenuRole(tenantMenuRole,TENANTMGRMENU,Boolean.FALSE);
                            this.updateResourceMenuRole(tenantMenuRole,MYTENANTMENU,Boolean.FALSE);
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.TRUE);
                            return;
                        }
                        List tenantMgrList = resourceMap.get(TENANTMGR);
                        if (!tenantMenuRole.get(MYTENANTMENU).getAvailable() && CollectionUtils.isEmpty(tenantMgrList)){
                            //没有租户管理与我的租户菜单，设置我的项目菜单
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.TRUE);
                        }else if (!tenantMenuRole.get(MYTENANTMENU).getAvailable() && !CollectionUtils.isEmpty(tenantMgrList)){
                            //有我的租户权限
                            this.updateResourceMenuRole(tenantMenuRole,MYTENANTMENU,Boolean.TRUE);
                        }
                    }
                    break;
                case MYTENANTMENU :
                    if (status){
                        List tenantBasicList = resourceMap.get(BASIC);
                        if (CollectionUtils.isEmpty(tenantBasicList)){
                            //我的租户 模块，把租户管理，我的项目设置为false
                            this.updateResourceMenuRole(tenantMenuRole,TENANTMGRMENU,Boolean.FALSE);
                            //更新我的项目
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.FALSE);
                        } else {
                            return;
                        }
                    } else {
                        //租户管理 模块，如果我的租户为空，我的项目设置为true
                        if (Objects.isNull(resourceMap)){
                            this.updateResourceMenuRole(tenantMenuRole,TENANTMGRMENU,Boolean.FALSE);
                            this.updateResourceMenuRole(tenantMenuRole,MYTENANTMENU,Boolean.FALSE);
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.TRUE);
                            return;
                        }
                        List tenantBasicList = resourceMap.get(BASIC);
                        if (!tenantMenuRole.get(TENANTMGRMENU).getAvailable() && CollectionUtils.isEmpty(tenantBasicList)){
                            //没有租户管理菜单，设置我的项目菜单
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.TRUE);
                        }else if (!tenantMenuRole.get(TENANTMGRMENU).getAvailable() && !CollectionUtils.isEmpty(tenantBasicList)){
                            //有租户管理权限
                            this.updateResourceMenuRole(tenantMenuRole,TENANTMGRMENU,Boolean.TRUE);
                        }
                    }
                    break;
                case MYPROJECTMENU :
                    if (!status){
                        List tenantBasicList = null;
                        List tenantMgrList = null;
                        if (!Objects.isNull(resourceMap)){
                            tenantBasicList = resourceMap.get(BASIC);
                            tenantMgrList = resourceMap.get(TENANTMGR);
                        }
                        if (!tenantMenuRole.get(MYPROJECTMENU).getAvailable() &&
                                CollectionUtils.isEmpty(tenantBasicList) &&
                                CollectionUtils.isEmpty(tenantMgrList)){
                            //没有租户管理,我的租户菜单，设置我的项目菜单
                            this.updateResourceMenuRole(tenantMenuRole,MYPROJECTMENU,Boolean.TRUE);
                        }
                    }
                    return;
                default:
                    throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
            }
            resourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, rmid);
            if (resourceMenuRole.getAvailable()^status){
                resourceMenuRole.setAvailable(status);
                resourceMenuRoleService.updateResourceMenuRole(resourceMenuRole);
            }
        }else {
            //其他模块
            ResourceMenuRole resourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, rmid);
            if (resourceMenuRole.getAvailable()^status){
                resourceMenuRole.setAvailable(status);
                resourceMenuRoleService.updateResourceMenuRole(resourceMenuRole);
            }
        }

        if (isUpdateSubMenu){
            //如果需要更新子菜单，当子菜单存在的时候更新子菜单
           this.updateSubRoleMenu(roleId,rmid,status,null);
        }

    }
    private void updateResourceMenuRole(Map<Integer, ResourceMenuRole> tenantMenuRole,Integer type,Boolean status) throws Exception{
        ResourceMenuRole resourceMenuRole = tenantMenuRole.get(type);
        if (resourceMenuRole.getAvailable()^status){
            resourceMenuRole.setAvailable(status);
            resourceMenuRoleService.updateResourceMenuRole(resourceMenuRole);
        }
    }
    private void updateSubRoleMenu(Integer roleId,Integer rmid,Boolean status,String module)throws Exception{
        List<ResourceMenu> subResourceMenuList = this.resourceMenuService.getResourceMenuListByParentId(rmid);
        if (CollectionUtils.isEmpty(subResourceMenuList)){
            ResourceMenu menuById = this.resourceMenuService.getResourceMenuById(rmid);
            if (!Objects.isNull(menuById)){
                subResourceMenuList.add(menuById);
            }
        }
        if (!CollectionUtils.isEmpty(subResourceMenuList)){
            for (ResourceMenu resourceMenu:subResourceMenuList) {
                if (!Objects.isNull(module) && module.equals(resourceMenu.getModule())){
                    ResourceMenuRole subResourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, resourceMenu.getId());
                    if (subResourceMenuRole.getAvailable()^status){
                        subResourceMenuRole.setAvailable(status);
                        resourceMenuRoleService.updateResourceMenuRole(subResourceMenuRole);
                        //如果子菜单为true,父级菜单如果为false则需要同步更新父级菜单
                        if (status){
                            ResourceMenuRole parentResourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, rmid);
                            if (!parentResourceMenuRole.getAvailable()){
                                parentResourceMenuRole.setAvailable(status);
                                resourceMenuRoleService.updateResourceMenuRole(parentResourceMenuRole);
                            }
                        }
                    }
                }else if (Objects.isNull(module)){
                    //更新菜单下所有的菜单
                    ResourceMenuRole subResourceMenuRole = this.resourceMenuRoleService.getResourceMenuRole(roleId, resourceMenu.getId());
                    if (subResourceMenuRole.getAvailable()^status){
                        subResourceMenuRole.setAvailable(status);
                        resourceMenuRoleService.updateResourceMenuRole(subResourceMenuRole);
                    }
                }
            }
        }

    }
    /**
     * 更新角色菜单(仅供后台数据库同步使用)
     * @param roleId
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Override
    public void updateRoleMenu(Integer roleId) throws Exception {
//        List<RolePrivilege> moduleByParentId = this.getAllStatusModuleByParentId(0, roleName);
//        for (RolePrivilege map : moduleByParentId) {
//            Resource resource = this.getRolePrivateByMark(map.getMark().equals("租户管理")?"我的租户":map.getMark(),roleName);
//            if(resource == null){
//                throw new MarsRuntimeException("系统异常,权限数据格式错误,请联系管理员");
//            }
//            Boolean status = map.getStatus();
//            if(status ^ resource.getAvailable()){
//                this.resourceService.updateRoleMenuResource(resource.getId(), status);
//            }
//            List<RolePrivilege> moduleByParentIdSecond = this.getAllStatusModuleByParentId(map.getRpid(), roleName);
//            for (RolePrivilege rolePrivilegeSecond : moduleByParentIdSecond) {
//                Resource resourceSecond = this.getRolePrivateByMark(rolePrivilegeSecond.getMark().equals("租户管理")?"我的租户":rolePrivilegeSecond.getMark(),roleName);
//                if(resourceSecond != null){
//                    Boolean statusSecond = rolePrivilegeSecond.getStatus();
//                    if(statusSecond ^ resourceSecond.getAvailable()){
//                        this.resourceService.updateRoleMenuResource(resourceSecond.getId(), statusSecond);
//                    }
//                }
//            }
//        }
    }

    /**
     * 根据roleId获取权限
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public Map<String, Object> getAvailablePrivilegeByRoleId(Integer roleId) throws Exception {
        return this.getPrivilegeByRoleId(roleId,Boolean.TRUE);
    }

    /**
     * 根据roleId获取权限
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public Map<String, Object> getAllPrivilegeByRoleId(Integer roleId) throws Exception {
        return this.getPrivilegeByRoleId(roleId,Boolean.FALSE);
    }

    /**
     * 根据角色id获取权限数据
     * @param roleId
     * @param available 是否有效，当为false的时候查询所有，修改权限时候加载角色所有权限树用到
     * @return
     * @throws Exception
     */
    private Map<String, Object> getPrivilegeByRoleId(Integer roleId,Boolean available) throws Exception {
        Map<String, Object> result = new HashMap<>();
        //空值判断
        if(Objects.isNull(roleId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //有效值判断
        Role role = roleLocalService.getRoleById(roleId);
        if(Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        //获取角色下rolePrivilege列表
        List<RolePrivilege> list = this.getRolePrivilegeByRoleId(roleId,available);
        List<Privilege> privileges = new ArrayList<>();
        if (!CollectionUtils.isEmpty(list)){
            for (RolePrivilege rolePrivilege:list) {
                //获取权限
                Privilege privilegeById = this.privilegeService.getPrivilegeById(rolePrivilege.getPid());
                //基本权限可用才添加
                if (privilegeById.getStatus() && checkSystemPrivilege(roleId,privilegeById.getModule())){
                    //设置角色权限状态
                    privilegeById.setStatus(rolePrivilege.getStatus());
                    privilegeById.setCreateTime(rolePrivilege.getCreateTime());
                    privilegeById.setId(rolePrivilege.getId());
                    privileges.add(privilegeById);
                }
            }
        }
        if (!CollectionUtils.isEmpty(privileges)){
            //根据权限列表组装权限树
            result = this.dealPrivilegeTree(privileges);
        }
        return result;
    }
    private Boolean checkSystemPrivilege(Integer roleId,String module) throws Exception{
        if (roleId > CommonConstant.ADMIN_ROLEID && (module.equals("dashboard") || module.equals("infrastructure")|| module.equals("system"))){
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }
    /**
     * 获取基本权限操作
     *
     * @param
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getAllSystemPrivilege() throws Exception {
        Map<String, Object> result = null;
        //获取所有基础权限
        List<Privilege> privileges = privilegeService.listAllPrivilege();
        List<Privilege> collect = privileges.stream().filter(privilege -> !"infrastructure".equals(privilege.getModule())
                && !"dashboard".equals(privilege.getModule())
                && !"system".equals(privilege.getModule())).collect(Collectors.toList());
        collect.stream().forEach(privilege -> privilege.setStatus(Boolean.FALSE));
        //根据权限列表组装权限树
        result = this.dealPrivilegeTree(collect);
        return result;
    }
    //根据权限列表组装权限树
    private Map<String, Object> dealPrivilegeTree(List<Privilege> privileges){
        Map<String, Object> result = new HashMap<>();
        //获取所有的模块
        if (CollectionUtils.isEmpty(privileges)){
            return result;
        }
        Map<String, List<Privilege>> modules = privileges.stream().collect(Collectors.groupingBy(Privilege::getModule));
        if (!CollectionUtils.isEmpty(modules)){
            for (Map.Entry<String, List<Privilege>> entry : modules.entrySet()) {
                Map<String, Object> resource = new HashMap<>();
                //获取当前模块的所有的资源
                if (!CollectionUtils.isEmpty(entry.getValue())){
                    Map<String, List<Privilege>> resources = entry.getValue().
                            stream().collect(Collectors.groupingBy(Privilege::getResource));
                    if (!CollectionUtils.isEmpty(resources)){
                        for (Map.Entry<String, List<Privilege>> entryResource : resources.entrySet()) {
                            //添加模块下资源权限信息
                            resource.put(entryResource.getKey(),entryResource.getValue());
//                            resource.put(CommonConstant.STATUS,!CollectionUtils.isEmpty(entryResource.getValue()));
                        }
                    }
                }
                if (!Objects.isNull(resource)){
                    //添加模块信息
                    result.put(entry.getKey(),resource);
//                    result.put(CommonConstant.STATUS,!CollectionUtils.isEmpty(resource));//后续调试
                }
            }
        }
        return result;
    }
    private RolePrivilege getRolePrivilegeById(Integer id){
        return this.rolePrivilegeMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据角色id与权限id获取RolePrivilege
     * @param roleId
     * @param pId
     * @return
     */
    private RolePrivilege getRolePrivilegeByRoleIdAndPid(Integer roleId,Integer pId){
        RolePrivilegeExample example = this.getExample();
        example.createCriteria().andPidEqualTo(pId).andRoleIdEqualTo(roleId);
        List<RolePrivilege> rolePrivileges = this.rolePrivilegeMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(rolePrivileges)){
            return null;
        }
        return rolePrivileges.get(0);
    }

    /**
     * 添加rolePrivilege
     * @param rolePrivilege
     */
    public void createRolePrivilege(RolePrivilege rolePrivilege) throws Exception{
        RolePrivilege rolePrivilege1 = this.getRolePrivilegeByRoleIdAndPid(rolePrivilege.getRoleId(),
                rolePrivilege.getPid());
        //判断是否存在
        if (!Objects.isNull(rolePrivilege1)){
            throw new MarsRuntimeException(ErrorCodeMessage.PRIVILEGE_EXIST);
        }
        this.rolePrivilegeMapper.insertSelective(rolePrivilege);
    }

    /**
     * 根据roleId删除rolePrivilege
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public void deleteRolePrivilegeByRoleId(Integer roleId) throws Exception {
        RolePrivilegeExample example = this.getExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        this.rolePrivilegeMapper.deleteByExample(example);
    }

    /**
     * 更新角色权限
     *
     * @param rolePrivilege
     * @throws Exception
     */
    @Override
    public void updateRolePrivilege(RolePrivilege rolePrivilege) throws Exception {
        RolePrivilege rolePrivilegeById = this.getRolePrivilegeById(rolePrivilege.getId());
        if (Objects.isNull(rolePrivilegeById)){
            throw new MarsRuntimeException(ErrorCodeMessage.PRIVILEGE_NOT_EXIST);
        }
        rolePrivilegeById.setStatus(rolePrivilege.getStatus());
        rolePrivilegeById.setUpdateTime(DateUtil.getCurrentUtcTime());
        this.rolePrivilegeMapper.updateByPrimaryKeySelective(rolePrivilegeById);

    }

    /**
     * 根据id删除rolePrivilege
     * @param id
     * @throws Exception
     */
    public void deleteRolePrivilegeById(Integer id) throws Exception{
        RolePrivilege rolePrivilegeById = this.getRolePrivilegeById(id);
        //判断是否存在
        if (!Objects.isNull(rolePrivilegeById)){
            this.rolePrivilegeMapper.deleteByPrimaryKey(id);
        }
    }
    /**
     * 根据角色id获取RolePrivilege列表
     * @param roleId
     * @param available 是否有效，当为false的时候查询所有，修改权限时候加载角色所有权限树用到
     * @return
     */
    public List<RolePrivilege> getRolePrivilegeByRoleId(Integer roleId,Boolean available)throws Exception{
        RolePrivilegeExample example = this.getExample();
        if (available){
            example.createCriteria().andRoleIdEqualTo(roleId).andStatusEqualTo(Boolean.TRUE);
        } else {
            example.createCriteria().andRoleIdEqualTo(roleId);
        }
        List<RolePrivilege> rolePrivileges = this.rolePrivilegeMapper.selectByExample(example);
        return rolePrivileges;
    }

    /**
     * 根据角色id重置角色权限（只针对默认初始角色）
     *
     * @param roleId
     * @throws Exception
     */
    @Override
    public void resetRolePrivilegeByRoleId(Integer roleId) throws Exception {
        Role role = this.roleLocalService.getRoleById(roleId);
        //有效值判断
        if (Objects.isNull(role)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        if (roleId > CommonConstant.UAT_ROLEID){
            throw new MarsRuntimeException(ErrorCodeMessage.RESET_INIT_ROLE_NOLY);
        }
        //如果为初始默认未启动角色（dev,test,ops,uat等），重置为初始状态（初始权限，作用域为空，未启用状态）
        if (roleId > CommonConstant.PM_ROLEID){
            role.setClusterIds(null);
            role.setAvailable(Boolean.FALSE);
            this.roleLocalService.updateRole(role);
        }

        //查询当前角色的备份副本
        List<RolePrivilegeReplication> replication =
                this.rolePrivilegeReplicationService.getRolePrivilegeReplicationByRoleId(roleId);
        if (CollectionUtils.isEmpty(replication)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_PRIVILEGE_REPLICATION_NOT_BLANK);
        }
        //如果状态不同则恢复初始状态
        for (RolePrivilegeReplication rolePrivilegeReplication : replication) {
            RolePrivilege rolePrivilege = this.getRolePrivilegeByRoleIdAndPid(roleId,rolePrivilegeReplication.getPid());
            Boolean status = rolePrivilegeReplication.getStatus();
            if (!Objects.isNull(rolePrivilege) && (rolePrivilege.getStatus() ^ status)){
                rolePrivilege.setStatus(status);
                this.updateRolePrivilege(rolePrivilege);
            }
            //权限同步对应角色菜单
            Integer pid = rolePrivilege.getPid();
            this.syncRoleMenu(roleId,pid, status);
        }
    }

    private RolePrivilegeExample getExample(){
        return new RolePrivilegeExample();
    }
}
