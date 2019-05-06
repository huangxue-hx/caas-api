package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.AppStore;

public interface AppStoreMapper {
	
    int insert(AppStore record);

    AppStore selectByNameAndTag(@Param("name") String name, @Param("tag") String tag);

    void delete(Integer id);
    
    List<AppStore> listByName(@Param("name") String name);
    
    List<AppStore> listApps(@Param("name") String name);
    
    List<AppStore> list();

    AppStore selectById(@Param("id") Integer id);
    
    void update(AppStore record);

}
