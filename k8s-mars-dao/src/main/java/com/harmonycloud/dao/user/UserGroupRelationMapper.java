package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dao.user.bean.UserGroupRelation;
import com.harmonycloud.dao.user.bean.UserGroupRelationExample;
import java.util.List;

public interface UserGroupRelationMapper {
    int deleteByExample(UserGroupRelationExample example);

    int insert(UserGroupRelation record);

    int insertSelective(UserGroupRelation record);

    List<UserGroupRelation> selectByExample(UserGroupRelationExample example);
    
    void addUserGroupRelation(List<UserGroupRelation> ugrs);

    /**
     * 根据组名获取该组用户列表
     * @param groupname
     * @return
     */
    List<User> selectUserListByGroupName(String groupname);
}