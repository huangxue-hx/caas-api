package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMappingMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMappingExample;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMappingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupMappingServiceImpl implements DataPrivilegeGroupMappingService {

    @Autowired
    DataPrivilegeGroupMappingMapper dataPrivilegeGroupMappingMapper;

    @Override
    public void initMapping(int roGroupId, int rwGroupId, DataPrivilegeDto dataPrivilegeDto) {
        Integer parentRoMappingId = null;
        Integer parentRwMappingId = null;
        if(StringUtils.isNotBlank(dataPrivilegeDto.getParentData()) && dataPrivilegeDto.getParentDataResourceType() != null){
            DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
            DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria().andDataNameEqualTo(dataPrivilegeDto.getParentData())
                    .andResourceTypeIdEqualTo(dataPrivilegeDto.getParentDataResourceType());
            if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
                criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
                criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
                criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
            }
            List<DataPrivilegeGroupMapping> list = this.getDataPrivilegeGroupMapping(example);
            for(DataPrivilegeGroupMapping dataPrivilegeGroupMapping : list){
                if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                    parentRoMappingId = dataPrivilegeGroupMapping.getId();
                }else if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                    parentRwMappingId = dataPrivilegeGroupMapping.getId();
                }
            }
        }
        DataPrivilegeGroupMapping dataPrivilegeGroupMapping = new DataPrivilegeGroupMapping();
        dataPrivilegeGroupMapping.setDataName(dataPrivilegeDto.getData());
        dataPrivilegeGroupMapping.setResourceTypeId(dataPrivilegeDto.getDataResourceType());
        dataPrivilegeGroupMapping.setProjectId(dataPrivilegeDto.getProjectId());
        dataPrivilegeGroupMapping.setClusterId(dataPrivilegeDto.getClusterId());
        dataPrivilegeGroupMapping.setNamespace(dataPrivilegeDto.getNamespace());
        dataPrivilegeGroupMapping.setGroupId(roGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRoMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READONLY);
        this.addMapping(dataPrivilegeGroupMapping);

        dataPrivilegeGroupMapping.setGroupId(rwGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRwMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READWRITE);
        this.addMapping(dataPrivilegeGroupMapping);
    }

    @Override
    public void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping) {
        dataPrivilegeGroupMappingMapper.insert(dataPrivilegeGroupMapping);
    }

    @Override
    public List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeGroupMappingExample dataPrivilegeGroupMapping) {
        return dataPrivilegeGroupMappingMapper.selectByExample(dataPrivilegeGroupMapping);
    }

    @Override
    public void deleteMappingById(int id) {
        dataPrivilegeGroupMappingMapper.deleteByPrimaryKey(id);
    }
}
