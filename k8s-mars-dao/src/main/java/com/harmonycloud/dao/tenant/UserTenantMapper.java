package com.harmonycloud.dao.tenant;

import java.util.List;

import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.tenant.bean.UserTenantExample;


public interface UserTenantMapper {
    int deleteByExample(UserTenantExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserTenant record);

    int insertSelective(UserTenant record);

    List<UserTenant> selectByExample(UserTenantExample example);

    UserTenant selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserTenant record);

    int updateByPrimaryKey(UserTenant record);
    
    int deleteByTenantid(String tenantid);
}