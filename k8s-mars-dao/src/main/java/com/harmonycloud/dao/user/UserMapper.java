package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserExample;
import java.util.List;

public interface UserMapper {
    int deleteByExample(UserExample example);

    int deleteByPrimaryKey(Long uuid);

    int insert(User record);

    int insertSelective(User record);

    List<User> selectByExample(UserExample example);

    User selectByPrimaryKey(Long uuid);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> selectLikeUsername(String username);
}