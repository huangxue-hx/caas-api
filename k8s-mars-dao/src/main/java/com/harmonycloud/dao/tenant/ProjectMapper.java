package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.ProjectExample;

import java.util.List;

public interface ProjectMapper {
    int deleteByExample(ProjectExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Project record);

    int insertSelective(Project record);

    List<Project> selectByExample(ProjectExample example);

    Project selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Project record);

    int updateByPrimaryKey(Project record);
}