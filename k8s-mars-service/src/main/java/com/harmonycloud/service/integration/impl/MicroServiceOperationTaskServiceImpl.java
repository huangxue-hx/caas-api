package com.harmonycloud.service.integration.impl;

import com.harmonycloud.dao.microservice.MicroServiceOperationTaskMapper;
import com.harmonycloud.dao.microservice.bean.MicroServiceOperationTask;
import com.harmonycloud.service.integration.MicroServiceOperationTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 对微服务操作任务表的数据库操作
 * @Date created in 2017-12-14
 * @Modified
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class MicroServiceOperationTaskServiceImpl implements MicroServiceOperationTaskService{

    @Autowired
    private MicroServiceOperationTaskMapper operationTaskMapper;
    @Override
    public boolean insertTask(MicroServiceOperationTask task) throws Exception{
        operationTaskMapper.insertTask(task);
        return true;
    }

    @Override
    public MicroServiceOperationTask findByTaskId(String taskId) throws Exception{
        MicroServiceOperationTask microServiceOperationTask = operationTaskMapper.findByTaskId(taskId);
        return microServiceOperationTask;
    }

    @Override
    public boolean updateTask(MicroServiceOperationTask task) throws Exception{
        operationTaskMapper.updateTask(task);
        return true;
    }

    @Override
    public List<MicroServiceOperationTask> findTaskByType(String namespaceId, Integer taskType) throws Exception{
        List<MicroServiceOperationTask> task = operationTaskMapper.findTaskByType(namespaceId,taskType);
        return task;
    }

    @Override
    public List<MicroServiceOperationTask> findTaskByNamespace(String namespaceId) throws Exception{
        if (StringUtils.isEmpty(namespaceId)) {
            return null;
        }
        List<MicroServiceOperationTask> operationTasks = operationTaskMapper.findTaskByNamespace(namespaceId);
        return operationTasks;
    }

    @Override
    public List<MicroServiceOperationTask> getTaskByNamespaceAndStatus(String namespaceId, String status) throws Exception {
        List<MicroServiceOperationTask> operationTasks = operationTaskMapper.findTasksByStatus(namespaceId, Integer.valueOf(status));
        return operationTasks;
    }

    @Override
    public void deleteTask(String namespaceId) throws Exception {
        operationTaskMapper.deleteTask(namespaceId);
    }
}
