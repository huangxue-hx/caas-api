package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMappingExample;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;

import java.util.List;

/**
 * Created by anson on 18/6/20.
 */
public interface DataPrivilegeGroupMappingService {

    /**
     * 初始化权限组与数据的关联
     * @param roGroupId
     * @param rwGroupId
     * @param dataPrivilegeDto
     */
    void initMapping(int roGroupId, int rwGroupId, DataPrivilegeDto dataPrivilegeDto);

    /**
     * 新建数据与权限组的关联
     * @param dataPrivilegeGroupMapping
     */
    void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping);

    /**
     * 查询数据权限关联
     * @param dataPrivilegeGroupMapping
     * @return
     */
    List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeGroupMappingExample dataPrivilegeGroupMapping);

    /**
     * 根据主键id删除关联
     * @param id
     */
    void deleteMappingById(int id);
}
