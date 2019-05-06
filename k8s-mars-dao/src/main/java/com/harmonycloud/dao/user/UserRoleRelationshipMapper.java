package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.UserRoleRelationship;
import com.harmonycloud.dao.user.bean.UserRoleRelationshipExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRelationshipMapper {
    int deleteByExample(UserRoleRelationshipExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(UserRoleRelationship record);

    int insertSelective(UserRoleRelationship record);

    List<UserRoleRelationship> selectByExample(UserRoleRelationshipExample example);

    List<UserRoleRelationship> selectByUsername(String username);

    UserRoleRelationship selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserRoleRelationship record);

    int updateByPrimaryKey(UserRoleRelationship record);

    void updateLocalRoleFlag(@Param("projectId")String projectId, @Param("usernames")List<String> usernames,
                             @Param("hasLocalRole")boolean hasLocalRole);
}