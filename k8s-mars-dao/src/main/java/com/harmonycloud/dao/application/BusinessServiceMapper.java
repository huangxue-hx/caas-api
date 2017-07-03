package com.harmonycloud.dao.application;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.application.bean.BusinessService;

public interface BusinessServiceMapper {

    int insert(BusinessService record);

    int selectByIdList(@Param("idList") List<Integer> idList);

    void deleteBusinessService(String name);
    
    List<BusinessService> listBusinessServiceByBusinessTemplatesId(@Param("businessId")int businessId);
    
    List<BusinessService> selectExternalBusinessServiceByBusinessId(@Param("businessId")int businessId);
    
    void deleteByTenant(@Param("tenant") String [] tenant);
    
    void deleteBusinessServiceByBusinessTemplateId(@Param("businessId")int businessId);
    
}
