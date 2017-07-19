package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.UserGroup;
import com.harmonycloud.dao.user.bean.UserGroupExample;
import java.util.List;

public interface UserGroupMapper {
    int deleteByExample(UserGroupExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserGroup record);

    int insertSelective(UserGroup record);

    List<UserGroup> selectByExample(UserGroupExample example);

    UserGroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserGroup record);

    int updateByPrimaryKey(UserGroup record);
}