package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.LocalUserRoleRel;
import com.harmonycloud.dao.user.bean.LocalUserRoleRelExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocalUserRoleRelMapper {
    int deleteByExample(LocalUserRoleRelExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LocalUserRoleRel record);

    int batchInsert(@Param("records") List<LocalUserRoleRel> records);

    int insertSelective(LocalUserRoleRel record);

    List<LocalUserRoleRel> selectByExample(LocalUserRoleRelExample example);

    LocalUserRoleRel selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LocalUserRoleRel record);

    int updateByPrimaryKey(LocalUserRoleRel record);
}