package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroup;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupServiceImpl implements DataPrivilegeGroupService{

    @Autowired
    DataPrivilegeGroupMapper dataPrivilegeGroupMapper;

    @Override
    public int addGroup(int type, String tenantId, String projectId){
        DataPrivilegeGroup dataPrivilegeGroup = new DataPrivilegeGroup();
        dataPrivilegeGroup.setProjectId(projectId);
        dataPrivilegeGroup.setTenantId(tenantId);
        dataPrivilegeGroup.setType((byte) type);
        dataPrivilegeGroupMapper.insert(dataPrivilegeGroup);
        return dataPrivilegeGroup.getId();
    }
}
