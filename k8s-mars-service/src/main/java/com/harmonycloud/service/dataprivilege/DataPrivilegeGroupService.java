package com.harmonycloud.service.dataprivilege;

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
}
