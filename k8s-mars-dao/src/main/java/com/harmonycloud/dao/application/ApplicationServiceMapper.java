package com.harmonycloud.dao.application;

import java.util.List;

import com.harmonycloud.dao.application.bean.ApplicationService;
import org.apache.ibatis.annotations.Param;

public interface ApplicationServiceMapper {

    int insert(ApplicationService record);

    int selectByIdList(@Param("idList") List<Integer> idList);

    void deleteApplicationService(String name);
    
    List<ApplicationService> listApplicationServiceByAppTemplatesId(@Param("applicationId")int applicationId);
    
    List<ApplicationService> selectAppServiceByAppId(@Param("applicationId")int applicationId, @Param("serviceId")int serviceId);
    
    void deleteByProjectIds(@Param("tenant") String [] tenant);
    
    void deleteApplicationServiceByAppTemplateId(@Param("applicationId")int applicationId);
    
}
