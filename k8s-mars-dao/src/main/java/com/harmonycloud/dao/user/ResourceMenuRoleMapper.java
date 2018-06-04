package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.ResourceMenuRole;
import com.harmonycloud.dao.user.bean.ResourceMenuRoleExample;
import java.util.List;

public interface ResourceMenuRoleMapper {
    int deleteByExample(ResourceMenuRoleExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ResourceMenuRole record);

    int insertSelective(ResourceMenuRole record);

    List<ResourceMenuRole> selectByExample(ResourceMenuRoleExample example);

    ResourceMenuRole selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ResourceMenuRole record);

    int updateByPrimaryKey(ResourceMenuRole record);
}