package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.RolePrivilegeCustom;
import com.harmonycloud.dao.tenant.bean.RolePrivilegeCustomExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePrivilegeCustomMapper {
    int deleteByExample(RolePrivilegeCustomExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RolePrivilegeCustom record);

    int insertSelective(RolePrivilegeCustom record);

    List<RolePrivilegeCustom> selectByExample(RolePrivilegeCustomExample example);

    RolePrivilegeCustom selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RolePrivilegeCustom record, @Param("example") RolePrivilegeCustomExample example);

    int updateByExample(@Param("record") RolePrivilegeCustom record, @Param("example") RolePrivilegeCustomExample example);

    int updateByPrimaryKeySelective(RolePrivilegeCustom record);

    int updateByPrimaryKey(RolePrivilegeCustom record);
}