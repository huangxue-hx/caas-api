package com.harmonycloud.dao.network;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.harmonycloud.dao.network.bean.NamespceBindSubnet;
import com.harmonycloud.dao.network.bean.NamespceBindSubnetExample;

@Repository
public interface NamespceBindSubnetMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(NamespceBindSubnet record);

    int insertSelective(NamespceBindSubnet record);

    List<NamespceBindSubnet> selectByExample(NamespceBindSubnetExample example);

    NamespceBindSubnet selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NamespceBindSubnet record);

    int updateByPrimaryKey(NamespceBindSubnet record);
    
    int deleteBySubnetId(String subnetid);
}