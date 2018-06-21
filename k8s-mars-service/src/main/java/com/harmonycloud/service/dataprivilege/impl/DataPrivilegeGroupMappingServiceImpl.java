package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMappingMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupMappingServiceImpl implements DataPrivilegeGroupMappingService {

    @Autowired
    DataPrivilegeGroupMappingMapper dataPrivilegeGroupMappingMapper;

    @Override
    public void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping) {
        dataPrivilegeGroupMappingMapper.insert(dataPrivilegeGroupMapping);
    }
}
