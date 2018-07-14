package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMember;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;

import java.util.List;
import java.util.Map;

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
     * @param projectId
     * @param parentGroupId
     * @param privilegeType
     * @throws Exception
     */
    List<String> initGroupMember(int groupId, String projectId, Integer parentGroupId, int privilegeType, List<String> rwUserList) throws Exception;

    /**
     * 删除权限组中所有成员
     * @param groupId
     * @throws Exception
     */
    void deleteAllMemberFromGroup(int groupId) throws Exception;


    /**
     *
     * @param project
     * @param userList
     * @throws Exception
     */
    void addNewProjectMemberToGroup(Project project, List<String> userList) throws Exception;

    /**
     * 删除项目成员时删除数据权限组中成员
     * @param projectId
     * @param username
     * @throws Exception
     */
    void deleteProjectMemberFromGroup(String projectId, String username) throws Exception;

    /**
     * 删除所有权限组中的某成员
     * @param dataPrivilegeGroupMember
     * @throws Exception
     */
    void deleteMemberInAllGroup(DataPrivilegeGroupMember dataPrivilegeGroupMember) throws Exception;

    /**
     * 数据权限组中新增成员
     * @param groupId
     * @param userId
     * @param username
     * @throws Exception
     */
    void addMemberToPrivilegeGroup(Integer groupId, int userId, String username) throws Exception;

    /**
     * 数据权限组中删除成员
     * @param groupId
     * @param username
     * @throws Exception
     */
    void delMemberFromPrivilegeGroup(int groupId, String username) throws Exception;

    /**
     * 查询资源的数据权限成员列表
     * @param dataPrivilegeDto
     * @return
     * @throws Exception
     */
    Map<String,Object> listGroupMemberForData(DataPrivilegeDto dataPrivilegeDto) throws Exception;

    /**
     * 校验增删数据权限成员
     * @param groupId
     * @param username
     * @param groupType
     */
    void verifyMember(Integer groupId, Integer otherGroupId, String username, boolean isAdd, Integer groupType) throws Exception;
}
