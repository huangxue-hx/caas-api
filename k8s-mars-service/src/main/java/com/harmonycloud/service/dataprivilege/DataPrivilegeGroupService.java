package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroup;

import java.util.List;

/**
 * Created by anson on 18/6/20.
 */
public interface DataPrivilegeGroupService {
    /**
     * 新建组
     * @param type
     * @param tenantId
     * @param projectId
     * @return
     */
    int addGroup(int type, String tenantId, String projectId);

    /**
     * 删除组
     * @param groupId
     * @return
     */
    void deleteGroup(int groupId);

    /**
     * 删除组与成员
     * @param groupId
     */
    void deleteGroupWithMember(int groupId) throws Exception;

    /**
     * 查询权限组
     * @param dataPrivilegeGroup
     * @return
     * @throws Exception
     */
    List<DataPrivilegeGroup> getGroup(DataPrivilegeGroup dataPrivilegeGroup) throws Exception;


}
