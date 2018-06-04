package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.UserProject;
import com.harmonycloud.dao.user.bean.UserProjectExample;
import java.util.List;

public interface UserProjectMapper {
    int deleteByExample(UserProjectExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserProject record);

    int insertSelective(UserProject record);

    List<UserProject> selectByExample(UserProjectExample example);

    UserProject selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserProject record);

    int updateByPrimaryKey(UserProject record);
}