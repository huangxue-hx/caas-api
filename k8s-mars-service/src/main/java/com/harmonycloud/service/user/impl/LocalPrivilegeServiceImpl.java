package com.harmonycloud.service.user.impl;

import com.harmonycloud.dao.user.LocalPrivilegeMapper;
import com.harmonycloud.dao.user.bean.LocalPrivilege;
import com.harmonycloud.dao.user.bean.LocalPrivilegeExample;
import com.harmonycloud.service.user.LocalPrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 局部角色权限规则Service impl // created by czl
 */
@Service
public class LocalPrivilegeServiceImpl implements LocalPrivilegeService {

    @Autowired
    LocalPrivilegeMapper LocalPrivilegeMapper;

    /**
     * 根据角色id查询局部角色的权限规则
     *
     * @param id
     * @return
     */
    public List<LocalPrivilege> listPrivilegeRuleByRoleId(Integer id){
        LocalPrivilegeExample condition = new LocalPrivilegeExample();
        condition.createCriteria().andLocalRoleIdEqualTo(id);
        return listLocalPrivileges(condition);
    }

    /**
     * 根据角色id，resourceType查询局部角色的权限规则
     *
     * @param roleId
     * @param resourceType
     *
     * @return
     */
    public List<LocalPrivilege> listRuleByResourceType(Integer roleId, String resourceType){
        LocalPrivilegeExample condition = new LocalPrivilegeExample();
        condition.createCriteria()
                .andLocalRoleIdEqualTo(roleId)
                .andResourceTypeEqualTo(resourceType);
        return listLocalPrivileges(condition);
    }

    /**
     * 创建局部角色权限规则
     *
     * @param LocalPrivilege
     * @return
     */
    public int insert(LocalPrivilege LocalPrivilege){
        LocalPrivilege.setAvailable(true);
        return LocalPrivilegeMapper.insertSelective(LocalPrivilege);
    }

    /**
     * 带条件查询局部角色权限规则
     *
     * @param condition
     * @return
     */
    public List<LocalPrivilege> listLocalPrivileges(LocalPrivilegeExample condition){
        condition.getOredCriteria().get(0).andAvailableEqualTo(true);
        return LocalPrivilegeMapper.selectByExample(condition);
    }

    /**
     * 更新局部角色权限规则
     *
     * @param LocalPrivilege
     * @return
     */
    public int update(LocalPrivilege LocalPrivilege){
        return LocalPrivilegeMapper.updateByPrimaryKeySelective(LocalPrivilege);
    }

    /**
     * 删除局部角色权限规则
     *
     * @param id
     * @return
     */
    public int delete(Integer id){
        return LocalPrivilegeMapper.deleteByPrimaryKey(id);
    }

    /**
     * 根据条件删除规则
     *
     * @param condition
     * @return
     */
    public int deleteByExample(LocalPrivilegeExample condition){
        return LocalPrivilegeMapper.deleteByExample(condition);
    }
}
