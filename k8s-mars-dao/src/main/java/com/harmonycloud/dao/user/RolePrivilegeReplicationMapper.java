package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.RolePrivilegeReplication;
import com.harmonycloud.dao.user.bean.RolePrivilegeReplicationExample;
import java.util.List;

public interface RolePrivilegeReplicationMapper {
    int deleteByExample(RolePrivilegeReplicationExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RolePrivilegeReplication record);

    int insertSelective(RolePrivilegeReplication record);

    List<RolePrivilegeReplication> selectByExample(RolePrivilegeReplicationExample example);

    RolePrivilegeReplication selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(RolePrivilegeReplication record);

    int updateByPrimaryKey(RolePrivilegeReplication record);
}