package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.Privilege;
import com.harmonycloud.dao.user.bean.PrivilegeExample;
import java.util.List;

public interface PrivilegeMapper {
    int deleteByExample(PrivilegeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Privilege record);

    int insertSelective(Privilege record);

    List<Privilege> selectByExample(PrivilegeExample example);

    Privilege selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Privilege record);

    int updateByPrimaryKey(Privilege record);
}