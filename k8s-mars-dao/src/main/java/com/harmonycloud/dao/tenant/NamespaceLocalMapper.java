package com.harmonycloud.dao.tenant;

import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.NamespaceLocalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NamespaceLocalMapper {
    int deleteByExample(NamespaceLocalExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(NamespaceLocal record);

    int insertSelective(NamespaceLocal record);

    List<NamespaceLocal> selectByExample(NamespaceLocalExample example);

    NamespaceLocal selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(NamespaceLocal record);

    int updateByPrimaryKey(NamespaceLocal record);

    String selectAliasNameByName(@Param("nameSpaceName")String nameSpaceName);
    String selectNameByalias_name(@Param("aliasName")String aliasName);
    int deleteByClusterId(@Param("clusterId")String clusterId);
}