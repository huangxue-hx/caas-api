package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.dao.dataprivilege.DataPrivilegeGroupMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroup;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupExample;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupMemberService;
import com.harmonycloud.service.dataprivilege.DataPrivilegeGroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by anson on 18/6/20.
 */
@Service
public class DataPrivilegeGroupServiceImpl implements DataPrivilegeGroupService{

    @Autowired
    DataPrivilegeGroupMapper dataPrivilegeGroupMapper;

    @Autowired
    DataPrivilegeGroupMemberService dataPrivilegeGroupMemberService;

    /**
     * 增加组
     * @param type
     * @param tenantId
     * @param projectId
     * @return
     */
    @Override
    public int addGroup(int type, String tenantId, String projectId){
        DataPrivilegeGroup dataPrivilegeGroup = new DataPrivilegeGroup();
        dataPrivilegeGroup.setProjectId(projectId);
        dataPrivilegeGroup.setTenantId(tenantId);
        dataPrivilegeGroup.setType((byte) type);
        dataPrivilegeGroupMapper.insert(dataPrivilegeGroup);
        return dataPrivilegeGroup.getId();
    }

    /**
     * 删除组
     * @param groupId
     */
    @Override
    public void deleteGroup(int groupId) {
        dataPrivilegeGroupMapper.deleteByPrimaryKey(groupId);
    }

    /**
     * 删除组及成员
     * @param groupId
     * @throws Exception
     */
    @Override
    public void deleteGroupWithMember(int groupId) throws Exception {
        this.deleteGroup(groupId);
        dataPrivilegeGroupMemberService.deleteAllMemberFromGroup(groupId);
    }

    /**
     * 查询组
     * @param dataPrivilegeGroup
     * @return
     * @throws Exception
     */
    @Override
    public List<DataPrivilegeGroup> getGroup(DataPrivilegeGroup dataPrivilegeGroup) throws Exception {
        DataPrivilegeGroupExample example = new DataPrivilegeGroupExample();
        DataPrivilegeGroupExample.Criteria criteria = example.createCriteria().andTypeEqualTo(dataPrivilegeGroup.getType());
        if(StringUtils.isNotBlank(dataPrivilegeGroup.getProjectId())){
            criteria.andProjectIdEqualTo(dataPrivilegeGroup.getProjectId());
        }
        if(StringUtils.isNotBlank(dataPrivilegeGroup.getTenantId())){
            criteria.andTenantIdEqualTo(dataPrivilegeGroup.getTenantId());
        }
        List<DataPrivilegeGroup> list = dataPrivilegeGroupMapper.selectByExample(example);
        return list;
    }


}
