package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMapping;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupMappingExample;
import java.util.List;

public interface DataPrivilegeGroupMappingMapper {
    int deleteByExample(DataPrivilegeGroupMappingExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataPrivilegeGroupMapping record);

    int insertSelective(DataPrivilegeGroupMapping record);

    List<DataPrivilegeGroupMapping> selectByExample(DataPrivilegeGroupMappingExample example);

    DataPrivilegeGroupMapping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DataPrivilegeGroupMapping record);

    int updateByPrimaryKey(DataPrivilegeGroupMapping record);
}