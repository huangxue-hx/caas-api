package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMemberExample;
import org.apache.ibatis.annotations.Param;

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

    void insertList(List<DataPrivilegeGroupMember> list);

    void deleteList(List<DataPrivilegeGroupMember> list);

    void insertUserList(@Param("groupId")int groupId, @Param("memberType")int type, @Param("userList")List<String> userList);

    void deleteUserInProject(@Param("projectId")String projectId, @Param("username")String username);

    List<DataPrivilegeGroupMember> selectGroupMemberWithRealName(Integer groupId);

    void copyGroupMember(@Param("srcGroupId")int srcGroupId, @Param("destGroupId")int destGroupId);

    void deleteUserInGroupList(@Param("username")String username, @Param("list")List<Integer> list);

    List<String> selectParentDataGroupUser(int groupId);
}