package com.harmonycloud.service.integration;

import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 操作msf_instance数据库
 * @Date created in 2017-12-14
 * @Modified
 */
public interface MicroServiceInstanceService {

    /**
     * 根据实例Id查找实例
     * @param instanceId
     * @return
     * @throws Exception
     */
    MicroServiceInstance findByInstanceId(String instanceId) throws Exception;

    /**
     * 更新实例信息
     * @param microServiceInstance
     * @return
     * @throws Exception
     */
    boolean updateMicroServiceInstance(MicroServiceInstance microServiceInstance) throws Exception;

    /**
     * 根据实例Id和namespaceId删除实例信息
     * @param instanceId
     * @param namespaceId
     * @return
     */
    boolean deleteMicroServiceInstance(String instanceId, String namespaceId) throws Exception;

    /**
     * 根据任务Id查询实例
     * @param taskId
     * @return
     */
    List<MicroServiceInstance> queryByTaskId(String taskId) throws Exception;

    /**
     * 插入实例信息
     * @param instance
     * @return
     */
    boolean insertMicroServiceInstance(MicroServiceInstance instance) throws Exception;

    /**
     * 根据namespaceId查询实例
     * @param namespaceId
     * @return
     */
    List<MicroServiceInstance> getMsfInstancesByNamespaceId(String namespaceId) throws Exception;

    int deleteByClusterId(String clusterId);

}
