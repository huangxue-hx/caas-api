package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;

/**
 * Created by anson on 18/6/20.
 */
public interface DataPrivilegeGroupMappingService {

    /**
     * 新建数据与权限组的关联
     * @param dataPrivilegeGroupMapping
     */
    void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping);
}
