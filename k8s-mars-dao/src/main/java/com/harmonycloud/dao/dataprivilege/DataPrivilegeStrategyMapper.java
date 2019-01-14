package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategy;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeStrategyExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataPrivilegeStrategyMapper {
    int deleteByExample(DataPrivilegeStrategyExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataPrivilegeStrategy record);

    int insertSelective(DataPrivilegeStrategy record);

    List<DataPrivilegeStrategy> selectByExample(DataPrivilegeStrategyExample example);

    DataPrivilegeStrategy selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DataPrivilegeStrategy record);

    int updateByPrimaryKey(DataPrivilegeStrategy record);

    int deleteByScopeId(@Param("scopeId")String scopeId, @Param("scopeType")Byte scopeType);
}