package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.BuildEnvironment;
import com.harmonycloud.dao.ci.bean.BuildEnvironmentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BuildEnvironmentMapper {
    long countByExample(BuildEnvironmentExample example);

    int deleteByExample(BuildEnvironmentExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(BuildEnvironment record);

    int insertSelective(BuildEnvironment record);

    List<BuildEnvironment> selectByExample(BuildEnvironmentExample example);

    BuildEnvironment selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") BuildEnvironment record, @Param("example") BuildEnvironmentExample example);

    int updateByExample(@Param("record") BuildEnvironment record, @Param("example") BuildEnvironmentExample example);

    int updateByPrimaryKeySelective(BuildEnvironment record);

    int updateByPrimaryKey(BuildEnvironment record);

    List<BuildEnvironment> queryAll();

    int deleteByClusterId(@Param("clusterId")String clusterId);

}