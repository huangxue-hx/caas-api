package com.harmonycloud.service.integration.impl;

import com.harmonycloud.dao.microservice.MicroServiceInstanceMapper;
import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;
import com.harmonycloud.service.integration.MicroServiceInstanceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-14
 * @Modified
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class MicroServiceInstanceServiceImpl implements MicroServiceInstanceService{

    @Autowired
    private MicroServiceInstanceMapper msfInstanceMapper;

    @Override
    public MicroServiceInstance findByInstanceId(String instanceId) throws Exception {
        if (StringUtils.isEmpty(instanceId)) {
            return null;
        }
        MicroServiceInstance msfInstance = msfInstanceMapper.findByInstanceId(instanceId);
        return msfInstance;
    }

    @Override
    public boolean updateMicroServiceInstance(MicroServiceInstance microServiceInstance) throws Exception {
        msfInstanceMapper.updateMicroServiceInstance(microServiceInstance);
        return true;
    }

    @Override
    public boolean deleteMicroServiceInstance(String instanceId, String namespaceId) throws Exception{
        msfInstanceMapper.deleteMicroServiceInstance(instanceId, namespaceId);
        return true;
    }

    @Override
    public List<MicroServiceInstance> queryByTaskId(String taskId) throws Exception{
        if (StringUtils.isEmpty(taskId)) {
            return null;
        }
        List<MicroServiceInstance> instances = msfInstanceMapper.queryByTaskId(taskId);
        return instances;
    }

    @Override
    public boolean insertMicroServiceInstance(MicroServiceInstance instance) throws Exception{
        msfInstanceMapper.insertMicroServiceInstance(instance);
        return true;
    }

    public List<MicroServiceInstance> getMsfInstancesByNamespaceId(String namespaceId) throws Exception {
        List<MicroServiceInstance> instances = msfInstanceMapper.getMsfInstancesByNamespaceId(namespaceId);
        return instances;
    }

    @Override
    public int deleteByClusterId(String clusterId){
        return msfInstanceMapper.deleteByClusterId(clusterId);
    }
}
