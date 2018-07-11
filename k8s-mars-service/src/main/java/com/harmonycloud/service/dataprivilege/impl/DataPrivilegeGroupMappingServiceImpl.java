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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupMappingServiceImpl implements DataPrivilegeGroupMappingService {

    @Autowired
    DataPrivilegeGroupMappingMapper dataPrivilegeGroupMappingMapper;

    /**
     * 初始化数据与权限组的关联
     * @param roGroupId
     * @param rwGroupId
     * @param dataPrivilegeDto
     */
    @Override
    public Map<Integer, Object> initMapping(int roGroupId, int rwGroupId, DataPrivilegeDto dataPrivilegeDto) {
        //获取父资源的关联id
        Integer parentRoMappingId = null;
        Integer parentRwMappingId = null;
        Map<Integer, Object> map = new HashMap<>();
        if(StringUtils.isNotBlank(dataPrivilegeDto.getParentData()) && dataPrivilegeDto.getParentDataResourceType() != null){
            List<DataPrivilegeGroupMapping> list = this.getDataPrivilegeGroupMapping(dataPrivilegeDto);
            for(DataPrivilegeGroupMapping dataPrivilegeGroupMapping : list){
                if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READONLY){
                    parentRoMappingId = dataPrivilegeGroupMapping.getId();
                    map.put(CommonConstant.DATA_READONLY, dataPrivilegeGroupMapping.getGroupId());
                }else if(dataPrivilegeGroupMapping.getPrivilegeType() == CommonConstant.DATA_READWRITE){
                    parentRwMappingId = dataPrivilegeGroupMapping.getId();
                    map.put(CommonConstant.DATA_READWRITE, dataPrivilegeGroupMapping.getGroupId());
                }
            }
        }


        //创建资源与组关联
        DataPrivilegeGroupMapping dataPrivilegeGroupMapping = new DataPrivilegeGroupMapping();
        dataPrivilegeGroupMapping.setDataName(dataPrivilegeDto.getData());
        dataPrivilegeGroupMapping.setResourceTypeId(dataPrivilegeDto.getDataResourceType());
        dataPrivilegeGroupMapping.setProjectId(dataPrivilegeDto.getProjectId());
        dataPrivilegeGroupMapping.setClusterId(dataPrivilegeDto.getClusterId());
        dataPrivilegeGroupMapping.setNamespace(dataPrivilegeDto.getNamespace());
        dataPrivilegeGroupMapping.setCreatorId(dataPrivilegeDto.getCreatorId());

        //只读组
        dataPrivilegeGroupMapping.setGroupId(roGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRoMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READONLY);
        this.addMapping(dataPrivilegeGroupMapping);

        //读写组
        dataPrivilegeGroupMapping.setGroupId(rwGroupId);
        dataPrivilegeGroupMapping.setParentId(parentRwMappingId);
        dataPrivilegeGroupMapping.setPrivilegeType(CommonConstant.DATA_READWRITE);
        this.addMapping(dataPrivilegeGroupMapping);

        return map;
    }


    /**
     * 获取数据与权限组关联
     * @param dataPrivilegeDto
     * @return
     */
    @Override
    public List<DataPrivilegeGroupMapping> listDataPrivilegeGroupMapping(DataPrivilegeDto dataPrivilegeDto) {
        if(!StringUtils.isAnyBlank(dataPrivilegeDto.getData(),dataPrivilegeDto.getNamespace())){
            DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
            DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria().andDataNameEqualTo(dataPrivilegeDto.getData())
                    .andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
            if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
                criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
                criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
            }
            if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
                criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
            }
            return this.getDataPrivilegeGroupMapping(example);
        }

         return null;
    }


    /**
     * 新增关联
     * @param dataPrivilegeGroupMapping
     */
    @Override
    public void addMapping(DataPrivilegeGroupMapping dataPrivilegeGroupMapping) {
        dataPrivilegeGroupMappingMapper.insert(dataPrivilegeGroupMapping);
    }

    /**
     * 获取数据与权限组关联
     * @param example
     * @return
     */
    @Override
    public List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeGroupMappingExample example) {
        return dataPrivilegeGroupMappingMapper.selectByExample(example);
    }


    @Override
    public List<DataPrivilegeGroupMapping> getDataPrivilegeGroupMapping(DataPrivilegeDto dataPrivilegeDto){
        DataPrivilegeGroupMappingExample example = new DataPrivilegeGroupMappingExample();
        DataPrivilegeGroupMappingExample.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(dataPrivilegeDto.getParentData())){
            criteria.andDataNameEqualTo(dataPrivilegeDto.getParentData());
        }else if(StringUtils.isNotBlank(dataPrivilegeDto.getData())){
            criteria.andDataNameEqualTo(dataPrivilegeDto.getData());
        }
        if(dataPrivilegeDto.getParentDataResourceType() != null){
            criteria.andResourceTypeIdEqualTo(dataPrivilegeDto.getParentDataResourceType());
        }else if(dataPrivilegeDto.getDataResourceType() != null){
            criteria.andResourceTypeIdEqualTo(dataPrivilegeDto.getDataResourceType());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getProjectId())) {
            criteria.andProjectIdEqualTo(dataPrivilegeDto.getProjectId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getClusterId())) {
            criteria.andClusterIdEqualTo(dataPrivilegeDto.getClusterId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeDto.getNamespace())) {
            criteria.andNamespaceEqualTo(dataPrivilegeDto.getNamespace());
        }
        return dataPrivilegeGroupMappingMapper.selectByExample(example);
    }

    @Override
    public void deleteMappingById(int id) {
        dataPrivilegeGroupMappingMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<Integer> getChildDataMappingGroupWithoutUser(int groupId, String username) {
        return dataPrivilegeGroupMappingMapper.getChildDataMappingGroupWithoutUser(groupId, username);
    }
}
