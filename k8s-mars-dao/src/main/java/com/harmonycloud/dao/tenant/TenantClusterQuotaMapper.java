package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuotaExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TenantClusterQuotaMapper {
    int deleteByExample(TenantClusterQuotaExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TenantClusterQuota record);

    int insertSelective(TenantClusterQuota record);

    List<TenantClusterQuota> selectByExample(TenantClusterQuotaExample example);

    TenantClusterQuota selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(TenantClusterQuota record);

    int updateByPrimaryKey(TenantClusterQuota record);

    int deleteByClusterId(@Param("clusterId")String clusterId);
}