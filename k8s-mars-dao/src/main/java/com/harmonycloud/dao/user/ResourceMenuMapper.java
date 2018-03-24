package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.ResourceMenu;
import com.harmonycloud.dao.user.bean.ResourceMenuExample;
import java.util.List;

public interface ResourceMenuMapper {
    int deleteByExample(ResourceMenuExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ResourceMenu record);

    int insertSelective(ResourceMenu record);

    List<ResourceMenu> selectByExample(ResourceMenuExample example);

    ResourceMenu selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ResourceMenu record);

    int updateByPrimaryKey(ResourceMenu record);
}