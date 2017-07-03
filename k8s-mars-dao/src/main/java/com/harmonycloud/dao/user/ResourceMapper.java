package com.harmonycloud.dao.user;

import com.harmonycloud.dao.user.bean.Resource;
import com.harmonycloud.dao.user.bean.ResourceExample;
import java.util.List;

public interface ResourceMapper {
    int deleteByExample(ResourceExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Resource record);

    int insertSelective(Resource record);

    List<Resource> selectByExample(ResourceExample example);

    Resource selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Resource record);

    int updateByPrimaryKey(Resource record);

    List<Resource> findAll();
}