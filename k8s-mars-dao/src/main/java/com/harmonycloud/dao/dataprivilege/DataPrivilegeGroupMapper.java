package com.harmonycloud.dao.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroup;
import com.harmonycloud.dao.dataprivilege.bean.DataPrivilegeGroupExample;
import java.util.List;

public interface DataPrivilegeGroupMapper {
    int deleteByExample(DataPrivilegeGroupExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataPrivilegeGroup record);

    int insertSelective(DataPrivilegeGroup record);

    List<DataPrivilegeGroup> selectByExample(DataPrivilegeGroupExample example);

    DataPrivilegeGroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DataPrivilegeGroup record);

    int updateByPrimaryKey(DataPrivilegeGroup record);
}