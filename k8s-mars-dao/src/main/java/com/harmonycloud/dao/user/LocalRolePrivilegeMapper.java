package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.LocalRolePrivilege;
import com.harmonycloud.dao.user.bean.LocalRolePrivilegeExample;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalRolePrivilegeMapper {
    int deleteByExample(LocalRolePrivilegeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LocalRolePrivilege record);

    int insertSelective(LocalRolePrivilege record);

    List<LocalRolePrivilege> selectByExample(LocalRolePrivilegeExample example);

    LocalRolePrivilege selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LocalRolePrivilege record);

    int updateByPrimaryKey(LocalRolePrivilege record);
}