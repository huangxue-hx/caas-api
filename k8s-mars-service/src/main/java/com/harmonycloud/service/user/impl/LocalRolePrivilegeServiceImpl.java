package com.harmonycloud.service.user.impl;

import com.harmonycloud.dao.user.LocalRolePrivilegeMapper;
import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.LocalRolePrivilegeExample;
import com.harmonycloud.service.common.PrivilegeHelper;
import com.harmonycloud.service.user.LocalRolePrivilegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 局部角色权限实例Service impl类 // created by czl
 */
@Service
public class LocalRolePrivilegeServiceImpl implements LocalRolePrivilegeService {

    @Autowired
    private LocalRolePrivilegeMapper localRolePrivilegeMapper;

    @Autowired
    private PrivilegeHelper privilegeHelper;

    /**
     * 创建局部角色权限实例
     *
     * @param localRolePrivilege
     * @return
     */
    public int insert(LocalRolePrivilege localRolePrivilege){
        localRolePrivilege.setAvailable(true);
        int privilegeId = localRolePrivilegeMapper.insertSelective(localRolePrivilege);
        privilegeHelper.refreshSqlCondWithCheck(privilegeId);
        return privilegeId;
    }

    /**
     * 带条件查询局部角色权限实例
     *
     * @param condition
     * @return
     */
    public List<LocalRolePrivilege> listLocalRolePrivileges(LocalRolePrivilegeExample condition){
        condition.getOredCriteria().get(0).andAvailableEqualTo(true);
        return localRolePrivilegeMapper.selectByExample(condition);
    }

    /**
     * 带条件查询局部角色权限实例
     *
     * @return
     */
    public List<LocalRolePrivilege> listAllLocalRolePrivileges(){
        LocalRolePrivilegeExample condition = new LocalRolePrivilegeExample();
        condition.getOredCriteria().get(0).andAvailableEqualTo(true);
        return localRolePrivilegeMapper.selectByExample(condition);
    }

    /**
     * 更新局部权限角色实例
     *
     * @param localRolePrivilege
     * @return
     */
    public int update(LocalRolePrivilege localRolePrivilege){
        int result = localRolePrivilegeMapper.updateByPrimaryKeySelective(localRolePrivilege);
        privilegeHelper.refreshSqlCondWithCheck(localRolePrivilege.getId());
        return result;
    }

    /**
     * 删除局部权限角色实例
     *
     * @param id
     * @return
     */
    public int delete(Integer id){
        int result = localRolePrivilegeMapper.deleteByPrimaryKey(id);
        privilegeHelper.refreshSqlCondWithCheck(id);
        return result;
    }

    /**
     * 根据条件删除权限实例
     *
     * @param condition
     * @return
     */
    public int deleteByExample(LocalRolePrivilegeExample condition){
        int result = localRolePrivilegeMapper.deleteByExample(condition);
        List<LocalRolePrivilege> localRolePrivileges = localRolePrivilegeMapper.selectByExample(condition);
        for (LocalRolePrivilege localRolePrivilege : localRolePrivileges) {
            privilegeHelper.refreshSqlCondWithCheck(localRolePrivilege.getId());
        }
        return result;
    }

    /**
     * 根据id查询权限实例
     *
     * @param id
     * @return
     */
    public LocalRolePrivilege get(Integer id){
        return localRolePrivilegeMapper.selectByPrimaryKey(id);
    }
}
