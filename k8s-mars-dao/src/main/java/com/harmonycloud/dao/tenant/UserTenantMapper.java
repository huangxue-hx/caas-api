package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.UserTenant;
import com.harmonycloud.dao.tenant.bean.UserTenantExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UserTenantMapper {
    int deleteByExample(UserTenantExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserTenant record);

    int insertSelective(UserTenant record);

    List<UserTenant> selectByExample(UserTenantExample example);

    UserTenant selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") UserTenant record, @Param("example") UserTenantExample example);

    int updateByExample(@Param("record") UserTenant record, @Param("example") UserTenantExample example);

    int updateByPrimaryKeySelective(UserTenant record);

    int updateByPrimaryKey(UserTenant record);
}