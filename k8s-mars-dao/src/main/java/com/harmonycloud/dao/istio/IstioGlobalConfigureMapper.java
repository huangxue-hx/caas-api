package com.harmonycloud.dao.istio;

import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;

public interface IstioGlobalConfigureMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(IstioGlobalConfigure record);

    int insertSelective(IstioGlobalConfigure record);

    IstioGlobalConfigure selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(IstioGlobalConfigure record);

    int updateByPrimaryKey(IstioGlobalConfigure record);

    IstioGlobalConfigure getByClusterId(String  clusterId);

    int  updateByClusterId(IstioGlobalConfigure record);
}