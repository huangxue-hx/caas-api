package com.harmonycloud.dao.application;

import java.util.List;

import com.harmonycloud.dao.application.bean.ApplicationService;
import org.apache.ibatis.annotations.Param;

public interface ApplicationServiceMapper {

    int insert(ApplicationService record);

    int selectByIdList(@Param("idList") List<Integer> idList);

    void deleteApplicationService(@Param("name")String name, @Param("projectId")String projectId, @Param("clusterId")String clusterId);
    
    List<ApplicationService> listApplicationServiceByAppTemplatesId(@Param("applicationId")int applicationId);
    
    List<ApplicationService> selectAppServiceByAppId(@Param("applicationId")int applicationId, @Param("serviceId")int serviceId);
    
    void deleteByProjectIds(@Param("tenant") String [] tenant);
    
    void deleteApplicationServiceByAppTemplateId(@Param("applicationId")int applicationId);
    
}
