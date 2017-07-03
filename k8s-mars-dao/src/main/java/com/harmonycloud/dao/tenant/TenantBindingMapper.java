package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantBindingExample;
import java.util.List;

public interface TenantBindingMapper {
    int deleteByExample(TenantBindingExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TenantBinding record);

    int insertSelective(TenantBinding record);

    List<TenantBinding> selectByExample(TenantBindingExample example);

    TenantBinding selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TenantBinding record);

    int updateByPrimaryKey(TenantBinding record);

    List<String> selectByTenanantName(String tenantName);

    List<TenantBinding> selectTenantsByUserName(String name);

    List<TenantBinding> selectAllTenantNames();
    
    int deleteByTenantid(String tenantid);
    
    int updateBytenantIdSelective(TenantBinding record);

}