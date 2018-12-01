package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.AppStoreService;

public interface AppStoreServiceMapper {

    int insert(AppStoreService record);

    void delete(@Param("appId") int appId);
    
    List<AppStoreService> listByAppId(@Param("appId") int appId);
    

}
