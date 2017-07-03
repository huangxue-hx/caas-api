package com.harmonycloud.dao.network;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.network.bean.NetworkTopology;
import com.harmonycloud.dao.network.bean.NetworkTopologyExample;


@Repository
public interface NetworkTopologyMapper {
    int deleteByExample(NetworkTopologyExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(NetworkTopology record);

    int insertSelective(NetworkTopology record);

    List<NetworkTopology> selectByExample(NetworkTopologyExample example);

    NetworkTopology selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NetworkTopology record);

    int updateByPrimaryKey(NetworkTopology record);
}