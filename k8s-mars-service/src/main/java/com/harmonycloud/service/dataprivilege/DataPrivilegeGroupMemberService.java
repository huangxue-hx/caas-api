package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;

import java.util.List;

/**
 * 权限组与成员接口
 * Created by chencheng on 18-6-20
 */
public interface DataPrivilegeGroupMemberService {


    /**
     * 向组中添加成员
     * @param dataPrivilegeGroupMember
     */
    void addMemberToGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception;

    /**
     * 删除组中的成员
     * @param dataPrivilegeGroupMember
     */
    void delMemberFromGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception;

    /**
     * 返回组中的所有成员
     * @param groupId
     * @return
     */
    List<DataPrivilegeGroupMember> listMemberInGroup(Integer groupId) throws Exception;

    /**
     * 初始化权限组成员
     * @param groupId
     * @param userId
     * @param projectId
     * @throws Exception
     */
    void initGroupMember(int groupId, Long userId, String projectId) throws Exception;

}
