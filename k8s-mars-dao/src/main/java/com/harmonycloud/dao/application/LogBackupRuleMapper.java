package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.LogBackupRule;
import com.harmonycloud.dao.application.bean.LogBackupRuleExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogBackupRuleMapper {
    int deleteByExample(LogBackupRuleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LogBackupRule record);

    int insertSelective(LogBackupRule record);

    List<LogBackupRule> selectByExample(LogBackupRuleExample example);

    LogBackupRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LogBackupRule record);

    int updateByPrimaryKey(LogBackupRule record);

    int deleteByClusterId(@Param("clusterId")String clusterId);
}