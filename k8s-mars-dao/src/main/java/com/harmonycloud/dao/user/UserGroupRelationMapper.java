package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.UserGroupRelation;
import com.harmonycloud.dao.user.bean.UserGroupRelationExample;
import java.util.List;

public interface UserGroupRelationMapper {
    int deleteByExample(UserGroupRelationExample example);

    int insert(UserGroupRelation record);

    int insertSelective(UserGroupRelation record);

    List<UserGroupRelation> selectByExample(UserGroupRelationExample example);
    
    void addUserGroupRelation(List<UserGroupRelation> ugrs);
}