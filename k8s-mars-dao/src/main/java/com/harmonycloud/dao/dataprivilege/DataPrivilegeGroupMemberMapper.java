package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMemberExample;
import java.util.List;

public interface DataPrivilegeGroupMemberMapper {
    int deleteByExample(DataPrivilegeGroupMemberExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataPrivilegeGroupMember record);

    int insertSelective(DataPrivilegeGroupMember record);

    List<DataPrivilegeGroupMember> selectByExample(DataPrivilegeGroupMemberExample example);

    DataPrivilegeGroupMember selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DataPrivilegeGroupMember record);

    int updateByPrimaryKey(DataPrivilegeGroupMember record);
}