package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.RolePrivilege;
import com.harmonycloud.dao.user.bean.RolePrivilegeExample;
import java.util.List;

public interface RolePrivilegeMapper {
    int deleteByExample(RolePrivilegeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RolePrivilege record);

    int insertSelective(RolePrivilege record);

    List<RolePrivilege> selectByExample(RolePrivilegeExample example);

    RolePrivilege selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RolePrivilege record);

    int updateByPrimaryKey(RolePrivilege record);
}