package com.harmonycloud.dao.network;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.network.bean.NetworkCalico;
import com.harmonycloud.dao.network.bean.NetworkCalicoExample;


@Repository
public interface NetworkCalicoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(NetworkCalico record);

    int insertSelective(NetworkCalico record);

    List<NetworkCalico> selectByExample(NetworkCalicoExample example);

    NetworkCalico selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NetworkCalico record);

    int updateByPrimaryKey(NetworkCalico record);
    
    int deleteByNetworkId(String networkid);
}