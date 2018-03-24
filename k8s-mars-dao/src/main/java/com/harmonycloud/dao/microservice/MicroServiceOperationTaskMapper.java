package com.harmonycloud.dao.microservice;

import com.harmonycloud.dao.microservice.bean.MicroServiceOperationTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-4
 * @Modified
 */
public interface MicroServiceOperationTaskMapper {

    void insertTask(MicroServiceOperationTask task);

    MicroServiceOperationTask findByTaskId(String taskId);

    void updateTask(MicroServiceOperationTask task);

    List<MicroServiceOperationTask> findTaskByType(@Param("namespaceId")String namespaceId, @Param("taskType")Integer taskType);

    List<MicroServiceOperationTask> findTaskByNamespace(String namespaceId);

    List<MicroServiceOperationTask> findTasksByStatus(@Param("namespaceId")String namespaceId, @Param("status") Integer status);
}
