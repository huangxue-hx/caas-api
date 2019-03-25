package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.ProjectIpPool;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProjectIpPoolMapper {

    int deleteByPrimaryKey(Integer id);

    int insertSelective(ProjectIpPool record);

    ProjectIpPool selectByPrimaryKey(Integer id);

    // 根据项目id、集群id和名称更新
    int updateByProjectIdAndClusterIdAndName(ProjectIpPool record);

    // 查询列表
    List<ProjectIpPool> selectList(@Param("projectId") String projectId, @Param("clusterId") String clusterId,
                                   @Param("name") String name);

}