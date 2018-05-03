package com.harmonycloud.service.integration;

import com.harmonycloud.dao.microservice.bean.MicroServiceOperationTask;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */
public interface MicroServiceOperationTaskService {

    /**
     * 插入任务
     * @param task
     */
    boolean insertTask(MicroServiceOperationTask task) throws Exception;

    /**
     * 根据任务Id查询任务
     * @param taskId
     * @return
     */
    MicroServiceOperationTask findByTaskId(String taskId) throws Exception;

    /**
     * 更新任务
     * @param task
     */
    boolean updateTask(MicroServiceOperationTask task) throws Exception;

    /**
     * 根据模板Id和任务类型查找任务
     * @param namespaceId
     * @param taskType
     * @return
     */
    List<MicroServiceOperationTask> findTaskByType(String namespaceId, Integer taskType) throws Exception;

    /**
     * 根据空间Id查询任务
     * @param namespaceId
     * @return
     */
    List<MicroServiceOperationTask> findTaskByNamespace(String namespaceId) throws Exception;

    /**
     * 根据任务状态和namespaceId查询任务
     * @param namespaceId
     * @param status
     * @return
     * @throws Exception
     */
    List<MicroServiceOperationTask> getTaskByNamespaceAndStatus(String namespaceId, String status) throws Exception;

    /**
     * 删除分区下的所有任务
     * @param namespaceId
     * @throws Exception
     */
    void deleteTask(String namespaceId) throws Exception;
}
