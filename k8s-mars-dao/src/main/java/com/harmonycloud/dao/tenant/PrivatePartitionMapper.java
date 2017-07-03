package com.harmonycloud.dao.tenant;

import java.util.List;

import com.harmonycloud.dao.tenant.bean.PrivatePartition;
import com.harmonycloud.dao.tenant.bean.PrivatePartitionExample;


public interface PrivatePartitionMapper {
    int deleteByExample(PrivatePartitionExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PrivatePartition record);

    int insertSelective(PrivatePartition record);

    List<PrivatePartition> selectByExample(PrivatePartitionExample example);

    PrivatePartition selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PrivatePartition record);

    int updateByPrimaryKey(PrivatePartition record);
}