package com.harmonycloud.service.user;

import com.harmonycloud.common.enumm.LocalRolePreConditionDto;
import com.harmonycloud.dao.user.bean.LocalPrivilege;
import com.harmonycloud.dao.user.bean.LocalRole;
import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.LocalUserRoleRel;
import com.harmonycloud.dto.user.LocalRoleDto;

import java.util.List;

/**
 * 局部角色Service // created by czl
 */
public interface LocalRoleService {

    /**
     * 创建局部角色
     *
     * @param localRoleDtoIn 必填：projectId, RoleName
     */
    public void createLocalRole(LocalRoleDto localRoleDtoIn) throws Exception;

    /**
     * 更新局部角色
     *
     * @param localRoleDtoIn
     * @throws Exception
     */
    public void updateLocalRole(LocalRoleDto localRoleDtoIn) throws Exception;
    /**
     * 根据角色名查询局部角色
     *
     * @param projectId
     * @param roleName
     * @return
     * @throws Exception
     */
    public List<LocalRole> listLocalRoleByRoleName(String projectId, String roleName) throws Exception;

    /**
     * 根据用户名查询局部角色
     *
     * @param projectId
     * @param userName
     * @return
     * @throws Exception
     */
    public List<LocalRole> listRoleByUserName(String projectId, String userName) throws Exception;

    /**
     * 根据项目、用户查询权限实例
     *
     * @param projectId
     * @param userName
     * @return
     * @throws Exception
     */
    public List<LocalRolePrivilege>  listPrivilegeByProject(String projectId, String userName) throws Exception;

    /**
     * 根据角色名删除局部角色
     *
     * @param projectId
     * @param roleName
     * @throws Exception
     */
    public void removeRoleByRoleName(String projectId, String roleName) throws Exception;

    /**
     * 查询角色下的用户成员
     *
     * @param localRoleDtoIn
     */
    List<LocalUserRoleRel>  listUserByRole(LocalRoleDto localRoleDtoIn);

    /**
     * 为局部角色分配用户（成员）
     *
     * @param localRoleDtoIn 必填：ProjectId, UserName, RoleName
     */
    public void assignRoleToUser(LocalRoleDto localRoleDtoIn) throws Exception;

    /**
     * 为角色分配数据权限
     *
     * @param localRoleDtoIn
     */
    public void assignPrivilege(LocalRoleDto localRoleDtoIn);

    /**
     * 查询权限实例列表
     *
     * @param localRoleDtoIn
     * @return
     */
    public List<LocalRolePrivilege> listPrivileges(LocalRoleDto localRoleDtoIn);

    /**
     * 为角色增加某种资源类型（如应用）的规则
     *
     * @param localRoleDtoIn
     */
    public void addResourceRule(LocalRoleDto localRoleDtoIn);

    /**
     * 为角色修改某种资源类型（如应用）的规则
     *
     * @param localRoleDtoIn
     */
    public void updateResourceRule(LocalRoleDto localRoleDtoIn);

    /**
     * 查询数据权限支持的所有条件
     *
     * @return
     */
    public List<LocalRolePreConditionDto> listAllPreConditions();

    /**
     * 根据roleId查询资源类型规则列表
     *
     * @param localRoleId
     * @return
     */
    public List<LocalPrivilege> listResourceRuleByRoleId(Integer localRoleId);

    /**
     * 根据id删除资源类型规则
     *
     * @param id
     */
    public void deleteResourceRuleById(Integer id);

    /**
     * 根据resourceType查询资源规则
     *
     * @param localRoleDtoIn
     * @return
     * @throws Exception
     */
    public List<LocalPrivilege> listResurceRuleByType(LocalRoleDto localRoleDtoIn) throws Exception;

    /**
     * 删除局部角色
     *
     * @param id
     * @return
     */
    int deleteLocalRoleById(Integer id);

    /**
     * 获取单个局部角色
     * @param id
     * @return
     */
    LocalRole getLocalRoleById(Integer id);
}
