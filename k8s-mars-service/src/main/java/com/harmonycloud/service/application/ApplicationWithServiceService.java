package com.harmonycloud.service.application;

import com.harmonycloud.dao.application.bean.ApplicationService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 操作数据库——应用模板与服务模板的对应关系（application_service）表
 * @Date created in 2017-12-25
 * @Modified
 */
public interface ApplicationWithServiceService {

    /**
     * 添加应用模板与服务模板的关系
     * @param record
     * @throws Exception
     */
    void insert(ApplicationService record) throws Exception;

    /**
     * 根据Id查看绑定情况
     * @param idList
     * @return
     * @throws Exception
     */
    int selectByIdList(List<Integer> idList) throws Exception;

    /**
     * 根据应用模板名称删除关系
     * @param name
     * @throws Exception
     */
    void deleteApplicationService(String name) throws Exception;

    /**
     * 根据应用模板Id获取应用与服务的对应关系
     * @param applicationId
     * @return List<ApplicationService>
     * @throws Exception
     */
    List<ApplicationService> listApplicationServiceByAppTemplatesId(int applicationId) throws Exception;

    /**
     * 根据应用模板Id和服务模板Id查询关系
     * @param applicationId
     * @param serviceId
     * @return List<ApplicationService>
     * @throws Exception
     */
    List<ApplicationService> selectAppServiceByAppId(int applicationId, int serviceId) throws Exception;

    /**
     * 根据项目Id删除对应的关系
     * @param projectIds
     * @throws Exception
     */
    void deleteByProjectIds(String [] projectIds) throws Exception;

    /**
     * 根据应用模板Id删除对应的关系
     * @param applicationId
     * @throws Exception
     */
    void deleteApplicationServiceByAppTemplateId(int applicationId) throws Exception;
}
