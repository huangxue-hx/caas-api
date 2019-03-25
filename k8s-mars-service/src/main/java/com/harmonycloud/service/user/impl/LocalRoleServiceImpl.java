package com.harmonycloud.service.user.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.*;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.UUIDUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.user.LocalRoleMapper;
import com.harmonycloud.dao.user.bean.*;
import com.harmonycloud.dto.user.LocalRoleDto;
import com.harmonycloud.service.common.PrivilegeCustomTypeEnum;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 局部角色Service // created by czl
 * 目前局部角色全局性的（如多表关联的）service存放在这里，因为角色跟各个表都有关系
 */
@Service
public class LocalRoleServiceImpl implements LocalRoleService {
    private static Logger logger = LoggerFactory.getLogger(LocalRoleServiceImpl.class);

    @Autowired
    private LocalRoleMapper localRoleMapper;

    @Autowired
    private LocalUserRoleRelService localUserRoleRelService;

    @Autowired
    private LocalPrivilegeService localRolePrivilegeRuleService;

    @Autowired
    private LocalRolePrivilegeService localRolePrivilegeService;

    @Autowired
    private UserRoleRelationshipService userRoleRelationshipService;

    @Autowired
    private ProjectService projectService;

    private static List allPreConditions;
    /**
     * 创建局部角色
     *
     * @param localRoleDtoIn 必填：ProjectId, RoleName
     */
    @Override
    public void createLocalRole(LocalRoleDto localRoleDtoIn) throws Exception {
        // 检查项目是否存在
        Project project = projectService.getProjectByProjectId(localRoleDtoIn.getProjectId());
        if (null == project){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
//        //角色名重复时报错
//        boolean isRoleExist = isRoleExist(localRoleDtoIn.getProjectId(), localRoleDtoIn.getRoleName());
//        if (isRoleExist){
//            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_EXIST);
//        }
        LocalRole localRole = new LocalRole();
        localRole.setProjectId(localRoleDtoIn.getProjectId());
        localRole.setName(UUIDUtil.get16UUID());
        localRole.setDescription(localRoleDtoIn.getRoleDesc());
        localRole.setNamespaces(localRoleDtoIn.getNamespaces());
        localRole.setCreateTime(DateUtil.getCurrentUtcTime());
        insert(localRole);

    }

    /**
     * 更新局部角色
     *
     * @param localRoleDtoIn
     * @throws Exception
     */
    @Override
    public void updateLocalRole(LocalRoleDto localRoleDtoIn) throws Exception {
        // 检查项目是否存在
        if (!StringUtils.isAnyBlank(localRoleDtoIn.getProjectId())){
            Project project = projectService.getProjectByProjectId(localRoleDtoIn.getProjectId());
            if (null == project){
                throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
            }
        }

//        //角色名重复时报错
//        if (!StringUtils.isAnyBlank(localRoleDtoIn.getRoleName())){
//            boolean isRoleExist = isRoleExist(localRoleDtoIn.getProjectId(), localRoleDtoIn.getRoleName());
//            if (isRoleExist){
//                throw new MarsRuntimeException(ErrorCodeMessage.ROLE_EXIST);
//            }
//        }

        LocalRole localRole = new LocalRole();
        localRole.setId(localRoleDtoIn.getLocalRoleId());
        localRole.setProjectId(localRoleDtoIn.getProjectId());
//        localRole.setName(localRoleDtoIn.getRoleName());
        localRole.setDescription(localRoleDtoIn.getRoleDesc());
        localRole.setNamespaces(localRoleDtoIn.getNamespaces());
        localRole.setUpdateTime(DateUtil.getCurrentUtcTime());
        localRoleMapper.updateByPrimaryKeySelective(localRole);

    }

    /**
     * 根据角色名删除局部角色
     *
     * @param projectId
     * @param roleName
     * @throws Exception
     */
    public void removeRoleByRoleName(String projectId, String roleName) throws Exception {

        Integer localRoleId = getRoleId(projectId, roleName);
        // 判断是否有成员绑定
        checkRoleBinding(localRoleId);
        deleteLocalRoleById(localRoleId);
        // 删除数据权限
        LocalPrivilegeExample ruleCond = new LocalPrivilegeExample();
        ruleCond.createCriteria().andLocalRoleIdEqualTo(localRoleId);
        localRolePrivilegeRuleService.deleteByExample(ruleCond);

        LocalRolePrivilegeExample condition = new LocalRolePrivilegeExample();
        condition.createCriteria().andLocalRoleIdEqualTo(localRoleId);
        localRolePrivilegeService.deleteByExample(condition);

    }

    /**
     * 根据角色名查询局部角色（外部接口）
     *
     * @param projectId
     * @param roleName
     * @return
     * @throws Exception
     */
    @Override
    public List<LocalRole> listLocalRoleByRoleName(String projectId, String roleName) throws Exception {

        return listRoleDetails(projectId, roleName);
    }

    /**
     * 根据用户名查询局部角色
     *
     * @param projectId
     * @param projectId
     * @param userName
     * @return
     * @throws Exception
     */
    @Override
    public List<LocalRole> listRoleByUserName(String projectId, String userName) throws Exception {

        LocalUserRoleRelExample relCondition = new LocalUserRoleRelExample();
        relCondition.createCriteria().andProjectIdEqualTo(projectId).andUserNameEqualTo(userName);
        List<LocalUserRoleRel> localUserRoleRels = localUserRoleRelService.listLocalUserRoleRels(relCondition);
        if(CollectionUtils.isEmpty(localUserRoleRels)){
            return Collections.emptyList();
        }
        List<LocalRole> returnLocalRoles = new ArrayList<>();
        for (LocalUserRoleRel localUserRoleRel:localUserRoleRels) {
            LocalRoleExample condition = new LocalRoleExample();
            condition.createCriteria().andIdEqualTo(localUserRoleRel.getLocalRoleId());
            List<LocalRole> localRoles = listLocalRoles(condition);
            if(CollectionUtils.isEmpty(localRoles)){
                continue;
            }
            returnLocalRoles.addAll(localRoles);
        }
        return returnLocalRoles;
    }

    /**
     * 根据项目、用户查询权限实例
     *
     * @param projectId
     * @param userName
     * @return
     * @throws Exception
     */
    public List<LocalRolePrivilege>  listPrivilegeByProject(String projectId, String userName) throws Exception {
        List<LocalRole> localRoles = listRoleByUserName(projectId, userName);
        if (CollectionUtils.isEmpty(localRoles)){
            return Collections.EMPTY_LIST;
        }
        List<LocalRolePrivilege> privileges = new ArrayList<>();
        for (LocalRole localRole : localRoles) {
            LocalRoleDto privilege = new LocalRoleDto();
            privilege.setLocalRoleId(localRole.getId());
            List<LocalRolePrivilege> curList = listPrivileges(privilege);
            privileges.addAll(curList);
        }
        return privileges;
    }

    /**
     * 根据角色名获取角色id
     *
     * @param projectId
     * @param roleName
     * @return
     * @throws Exception
     */
    private Integer getRoleId(String projectId, String roleName) throws Exception {
        List<LocalRole> roles = listRoleDetails(projectId, roleName);
        // 判断角色是否存在
        if(CollectionUtils.isEmpty(roles)){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        return roles.get(0).getId();
    }

    /**
     * 检查角色绑定
     *
     * @param localRoleId
     * @throws Exception
     */
    private void checkRoleBinding(Integer localRoleId) throws Exception {
        LocalUserRoleRelExample condition = new LocalUserRoleRelExample();
        condition.createCriteria().andLocalRoleIdEqualTo(localRoleId);
        List<LocalUserRoleRel> localUserRoleRels = localUserRoleRelService.listLocalUserRoleRels(condition);
        if (CollectionUtils.isEmpty(localUserRoleRels)){
            return ;
        }
        for (LocalUserRoleRel rel: localUserRoleRels) {
            List userRoleRelationships = userRoleRelationshipService.getRoleByUsername(rel.getUserName());
            if(!CollectionUtils.isEmpty(userRoleRelationships)){
                throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_BINDING_CAN_NOT_DELETE);
            }
        }
    }

    /**
     * 检查为用户分配的角色是否已经存在
     *
     *
     * @return
     */
    private boolean isUserRoleExist(String projectId, String userName, Integer localRoleId){
        LocalUserRoleRelExample condition = new LocalUserRoleRelExample();
        LocalUserRoleRelExample.Criteria criteria = condition.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        criteria.andUserNameEqualTo(userName);
        criteria.andLocalRoleIdEqualTo(localRoleId);
        List<LocalUserRoleRel> localUserRoleRels = localUserRoleRelService.listLocalUserRoleRels(condition);
        return !CollectionUtils.isEmpty(localUserRoleRels);
    }

    /**
     * 根据角色名称查询局部角色
     *
     * @param projectId
     * @param roleName
     * @return
     * @throws Exception
     */
    private List<LocalRole> listRoleDetails(String projectId, String roleName) throws Exception {
        LocalRoleExample condition = new LocalRoleExample();
        LocalRoleExample.Criteria criteria = condition.createCriteria().andProjectIdEqualTo(projectId);
        if(!StringUtils.isAnyBlank(roleName)){
            criteria.andNameEqualTo(roleName);
        }
        return listLocalRoles(condition);

    }

    /**
     * 为局部角色分配用户（成员）
     *
     * @param localRoleDtoIn 必填：ProjectId, UserName, RoleName
     */
    @Transactional
    public void assignRoleToUser(LocalRoleDto localRoleDtoIn) throws Exception {
        AssertUtil.notBlank(localRoleDtoIn.getProjectId(), DictEnum.PROJECT_ID);
        AssertUtil.notNull(localRoleDtoIn.getLocalRoleId(), DictEnum.LOCAL_ROLE_ID);
        List<LocalUserRoleRel> localUserRoleRels = new ArrayList<>();
        //更新之后用户名为空，删除之前授权的用户列表
        if(StringUtils.isNotBlank(localRoleDtoIn.getUserName())){
            String[] userNames = null;
            if (localRoleDtoIn.getUserName().contains(CommonConstant.COMMA)) {
                userNames = localRoleDtoIn.getUserName().split(CommonConstant.COMMA);
            } else {
                userNames = new String[]{localRoleDtoIn.getUserName()};
            }
            for (String userName : userNames) {
                if (StringUtils.isAnyBlank(userName)) {
                    continue;
                }
                // 检查用户是否在该项目下
                List<UserRoleRelationship> userRoleRelationships = userRoleRelationshipService
                        .getUserRoleRelationshipByUsernameAndProjectId(userName, localRoleDtoIn.getProjectId());
                if (CollectionUtils.isEmpty(userRoleRelationships)) {
                    throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_USER_NOT_IN_CURRENT_PROJECT);
                }
                LocalUserRoleRel localUserRoleRel = new LocalUserRoleRel();
                localUserRoleRel.setUserName(userName);
                localUserRoleRel.setProjectId(localRoleDtoIn.getProjectId());
                if (null == localRoleDtoIn.getLocalRoleId()
                        || 0 == localRoleDtoIn.getLocalRoleId()) {
                    List<LocalRole> localRoles = listLocalRoleByRoleName(localRoleDtoIn.getProjectId(), localRoleDtoIn.getRoleName());
                    if (CollectionUtils.isEmpty(localRoles)) {
                        throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
                    } else {
                        localUserRoleRel.setLocalRoleId(localRoles.get(0).getId());
                    }
                } else {
                    localUserRoleRel.setLocalRoleId(localRoleDtoIn.getLocalRoleId());

                }
                localUserRoleRel.setAvailable(Boolean.TRUE);
                localUserRoleRel.setHasLocalRole(true);
                localUserRoleRel.setCreateTime(DateUtil.getCurrentUtcTime());
                localUserRoleRels.add(localUserRoleRel);
            }
        }
        //先删除之前的局部角色授权的用户
        localUserRoleRelService.delete(localRoleDtoIn.getProjectId(),localRoleDtoIn.getLocalRoleId());
        if(!CollectionUtils.isEmpty(localUserRoleRels)) {
            localUserRoleRelService.insert(localUserRoleRels);
        }
        //查询某个项目的已设置局部角色的用户列表, 更新是否有局部角色标识
        LocalRoleDto localRoleQuery = new LocalRoleDto();
        localRoleQuery.setProjectId(localRoleDtoIn.getProjectId());
        List<LocalUserRoleRel>  localUserRoles = this.listUserByRole(localRoleQuery);
        List<String> localUserNames = localUserRoles.stream().map(LocalUserRoleRel::getUserName).collect(Collectors.toList());
        userRoleRelationshipService.setLocalRoleFlag(localRoleDtoIn.getProjectId(),localUserNames);
    }

    /**
     * 查询角色下的用户成员
     *
     * @param localRoleDtoIn
     */
    public List<LocalUserRoleRel>  listUserByRole(LocalRoleDto localRoleDtoIn){
        LocalUserRoleRelExample condition = new LocalUserRoleRelExample();
        LocalUserRoleRelExample.Criteria criteria = condition.createCriteria();
        if (!StringUtils.isAnyBlank(localRoleDtoIn.getProjectId())){
            criteria.andProjectIdEqualTo(localRoleDtoIn.getProjectId());
        }
        if (null != localRoleDtoIn.getLocalRoleId()){
            criteria.andLocalRoleIdEqualTo(localRoleDtoIn.getLocalRoleId());
        }
        return localUserRoleRelService.listLocalUserRoleRels(condition);
    }

    /**
     * 为角色分配数据权限
     *
     * @param localRoleDtoIn
     */
    public void assignPrivilege(LocalRoleDto localRoleDtoIn){
        //避免重复
        List<LocalRolePrivilege> localRolePrivileges = listPrivileges(localRoleDtoIn);
        Integer privilegeId = null;
        if (!CollectionUtils.isEmpty(localRolePrivileges)){
            privilegeId = localRolePrivileges.get(0).getId();
        }
        if (null == privilegeId || 0 == privilegeId){
            LocalRolePrivilege localRolePrivilege = new LocalRolePrivilege();
            localRolePrivilege.setLocalRoleId(localRoleDtoIn.getLocalRoleId());
            localRolePrivilege.setResourceType(localRoleDtoIn.getResourceType());
            localRolePrivilege.setResourceId(localRoleDtoIn.getResourceId());
            localRolePrivilege.setConditionType(localRoleDtoIn.getConditionType());
            localRolePrivilege.setConditionValue(localRoleDtoIn.getCondition());
            localRolePrivilege.setCreateTime(DateUtil.getCurrentUtcTime());
            localRolePrivilegeService.insert(localRolePrivilege);
        } else {
            LocalRolePrivilege localRolePrivilege = new LocalRolePrivilege();
            localRolePrivilege.setId(privilegeId);
            localRolePrivilege.setLocalRoleId(localRoleDtoIn.getLocalRoleId());
            localRolePrivilege.setResourceType(localRoleDtoIn.getResourceType());
            localRolePrivilege.setResourceId(localRoleDtoIn.getResourceId());
            localRolePrivilege.setConditionType(localRoleDtoIn.getConditionType());
            localRolePrivilege.setConditionValue(localRoleDtoIn.getCondition());
            localRolePrivilegeService.update(localRolePrivilege);
        }

    }

    /**
     * 查询权限实例列表
     *
     * @param localRoleDtoIn
     * @return
     */
    public List<LocalRolePrivilege> listPrivileges(LocalRoleDto localRoleDtoIn){
        LocalRolePrivilegeExample condition = new LocalRolePrivilegeExample();
        LocalRolePrivilegeExample.Criteria criteria = condition.createCriteria();
        if (null != localRoleDtoIn.getLocalRoleId()){
            criteria.andLocalRoleIdEqualTo(localRoleDtoIn.getLocalRoleId());
        }
        if (!StringUtils.isAnyBlank(localRoleDtoIn.getResourceType())){
            criteria.andResourceTypeEqualTo(localRoleDtoIn.getResourceType());
        }
        if (!StringUtils.isAnyBlank(localRoleDtoIn.getResourceId())){
            criteria.andResourceIdEqualTo(localRoleDtoIn.getResourceId());
        }

        return localRolePrivilegeService.listLocalRolePrivileges(condition);
    }

    /**
     * 为角色增加某种资源类型（如应用）的规则
     *
     * @param localRoleDtoIn
     */
    public void addResourceRule(LocalRoleDto localRoleDtoIn){

        LocalPrivilege localRolePrivilegeRule = new LocalPrivilege();
        localRolePrivilegeRule.setLocalRoleId(localRoleDtoIn.getLocalRoleId());
        localRolePrivilegeRule.setResourceType(localRoleDtoIn.getResourceType());
        localRolePrivilegeRule.setDescription(localRoleDtoIn.getPrivilegeRuleDesc());

        localRolePrivilegeRule.setAvailable(true);
        localRolePrivilegeRule.setCreateTime(DateUtil.getCurrentUtcTime());
        localRolePrivilegeRuleService.insert(localRolePrivilegeRule);

    }

    /**
     * 为角色修改某种资源类型（如应用）的规则
     *
     * @param localRoleDtoIn
     */
    public void updateResourceRule(LocalRoleDto localRoleDtoIn){

        LocalPrivilege localRolePrivilegeRule = new LocalPrivilege();
        localRolePrivilegeRule.setId(localRoleDtoIn.getResourceRuleId());
        localRolePrivilegeRule.setLocalRoleId(localRoleDtoIn.getLocalRoleId());
        localRolePrivilegeRule.setResourceType(localRoleDtoIn.getResourceType());
        localRolePrivilegeRule.setDescription(localRoleDtoIn.getPrivilegeRuleDesc());

        localRolePrivilegeRuleService.update(localRolePrivilegeRule);

    }

    /**
     * 根据roleId查询资源类型规则列表
     *
     * @param localRoleId
     * @return
     */
    public List<LocalPrivilege> listResourceRuleByRoleId(Integer localRoleId){
        return localRolePrivilegeRuleService.listPrivilegeRuleByRoleId(localRoleId);
    }

    /**
     * 根据id删除资源类型规则
     *
     * @param id
     */
    public void deleteResourceRuleById(Integer id){
        LocalPrivilegeExample condition = new LocalPrivilegeExample();
        condition.createCriteria().andIdEqualTo(id);
        List<LocalPrivilege> rules = localRolePrivilegeRuleService.listLocalPrivileges(condition);
        if (CollectionUtils.isEmpty(rules)){
            throw new MarsRuntimeException(ErrorCodeMessage.LOCAL_ROLE_RESOURCE_RULE_NOT_EXIST);
        }
        localRolePrivilegeRuleService.delete(id);
    }

    /**
     * 根据resourceType查询资源规则
     *
     * @param localRoleDtoIn 必填：ProjectId, UserName, ResourceType
     * @return
     * @throws Exception
     */
    public List<LocalPrivilege> listResurceRuleByType(LocalRoleDto localRoleDtoIn) throws Exception {
        List<LocalPrivilege> rules = new ArrayList<>();
        List<LocalRole> localRoles =
                listRoleByUserName(localRoleDtoIn.getProjectId(), localRoleDtoIn.getUserName());
        if (CollectionUtils.isEmpty(localRoles)){
            return Collections.emptyList();
        }
        for (LocalRole localRole: localRoles) {
            List<LocalPrivilege> localRolePrivilegeRules
                    = localRolePrivilegeRuleService.listRuleByResourceType(localRole.getId(), localRoleDtoIn.getResourceType());
            rules.addAll(localRolePrivilegeRules);
        }
        return rules;
    }

    /**
     * 查询数据权限支持的所有条件
     *
     * @return
     */
//    @PostConstruct
    public List<LocalRolePreConditionDto> listAllPreConditions(){
        if (!CollectionUtils.isEmpty(allPreConditions)){
            return allPreConditions;
        }
        List<LocalRolePreConditionDto> localRolePreConditionDtos = new ArrayList<>();
        LocalRolePreConditionDto customConditionDto = new LocalRolePreConditionDto();
        Map<String, Set<LocalRolePreFieldDto>> customFields = PrivilegeCustomTypeEnum.listCustomTypes();
        customConditionDto.setConditionFields(customFields);
        List<LocalRolePreFieldDto> comparors = LocalRoleCondComparorEnum.getComparorByType(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM);
        customConditionDto.setOps(comparors);
        List<LocalRolePreFieldDto> resourceTypes = PrivilegeCustomTypeEnum.listResourceTypes();
        customConditionDto.setResourceTypes(resourceTypes);
        customConditionDto.setConditionType(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM);
        localRolePreConditionDtos.add(customConditionDto);
//        LocalRolePreConditionDto sqlConditionDto = new LocalRolePreConditionDto();
//        // TODO 查询可用所有表字段
//        Map<String, List<LocalRolePreFieldDto>> sqlFields = PrivilegeCustomTypeEnum.listCustomTypes();
//        sqlConditionDto.setConditionFields(sqlFields);
//        List<LocalRolePreFieldDto> sqlComparors = LocalRoleCondComparorEnum.getComparorByType(CommonConstant.PRIVILEGE_CONDITION_TYPE_CUSTOM);
//        sqlConditionDto.setOps(sqlComparors);
//        sqlConditionDto.setConditionType(CommonConstant.PRIVILEGE_CONDITION_TYPE_SQL);
//        localRolePreConditionDtos.add(sqlConditionDto);
        allPreConditions = localRolePreConditionDtos;
        return localRolePreConditionDtos;
    }

    // 以下是角色定义表的基本操作
    /**
     * 插入局部角色表
     *
     * @param localRole
     * @return
     */
    private int insert(LocalRole localRole){
        localRole.setAvailable(true);
        return localRoleMapper.insertSelective(localRole);
    }

    /**
     * 查询局部角色所有数据
     *
     * @return
     * @throws Exception
     */
    public List<LocalRole> listAllLocalRoles() throws Exception{
        return localRoleMapper.selectByExample(new LocalRoleExample());
    }

    /**
     *  带条件的查询局部角色表
     *
     * @param localRole
     * @return
     * @throws Exception
     */
    public List<LocalRole> listLocalRoles(LocalRoleExample localRole) throws Exception{
        localRole.getOredCriteria().get(0).andAvailableEqualTo(true);
        return localRoleMapper.selectByExample(localRole);
    }

    /**
     * 根据id查询局部角色表
     *
     * @param id
     * @return
     */
    @Override
    public LocalRole getLocalRoleById(Integer id){
        return localRoleMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据id删除某个局部角色
     *
     * @param id
     * @return
     */
    @Override
    public int deleteLocalRoleById(Integer id){
        LocalRole localRole = this.getLocalRoleById(id);
        if(null == localRole){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_NOT_EXIST);
        }
        return localRoleMapper.deleteByPrimaryKey(id);
    }

}
