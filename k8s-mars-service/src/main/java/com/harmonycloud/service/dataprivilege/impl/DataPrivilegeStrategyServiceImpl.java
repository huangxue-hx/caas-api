package com.harmonycloud.service.dataprivilege.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.dataprivilege.DataPrivilegeStrategyMapper;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategyExample;
import com.harmonycloud.service.dataprivilege.DataPrivilegeStrategyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by anson on 18/6/25.
 */
@Service
public class DataPrivilegeStrategyServiceImpl implements DataPrivilegeStrategyService{

    @Autowired
    DataPrivilegeStrategyMapper dataPrivilegeStrategyMapper;

    @Override
    public List<DataPrivilegeStrategy> selectStrategy(String tenantId, String projectId, Integer resourceType) {
        DataPrivilegeStrategyExample example = new DataPrivilegeStrategyExample();
        DataPrivilegeStrategyExample.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(tenantId)){
            criteria.andScopeIdEqualTo(tenantId).andScopeTypeEqualTo(CommonConstant.SCOPE_TENANT);
        }else if(StringUtils.isNotBlank(projectId)){
            criteria.andScopeIdEqualTo(projectId).andScopeTypeEqualTo(CommonConstant.SCOPE_PROJECT);
        }
        return dataPrivilegeStrategyMapper.selectByExample(example);
    }
}
