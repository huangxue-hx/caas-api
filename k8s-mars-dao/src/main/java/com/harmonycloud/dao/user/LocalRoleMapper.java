package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.LocalRole;
import com.harmonycloud.dao.user.bean.LocalRoleExample;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalRoleMapper {
    int deleteByExample(LocalRoleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LocalRole record);

    int insertSelective(LocalRole record);

    List<LocalRole> selectByExample(LocalRoleExample example);

    LocalRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LocalRole record);

    int updateByPrimaryKey(LocalRole record);
}