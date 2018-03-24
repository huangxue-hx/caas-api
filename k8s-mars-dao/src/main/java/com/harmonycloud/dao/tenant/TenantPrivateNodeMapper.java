package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.TenantPrivateNode;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNodeExample;
import java.util.List;

public interface TenantPrivateNodeMapper {
    int deleteByExample(TenantPrivateNodeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TenantPrivateNode record);

    int insertSelective(TenantPrivateNode record);

    List<TenantPrivateNode> selectByExample(TenantPrivateNodeExample example);

    TenantPrivateNode selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TenantPrivateNode record);

    int updateByPrimaryKey(TenantPrivateNode record);
}