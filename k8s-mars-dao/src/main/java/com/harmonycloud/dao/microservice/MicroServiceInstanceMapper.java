package com.harmonycloud.dao.microservice;

import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MicroServiceInstanceMapper {

    MicroServiceInstance findByInstanceId(String instanceId);

    void updateMicroServiceInstance(MicroServiceInstance microServiceInstance);

    void deleteMicroServiceInstance(@Param("instanceId")String instanceId, @Param("namespaceId")String namespaceId);

    List<MicroServiceInstance> queryByTaskId(String taskId);

    void insertMicroServiceInstance(MicroServiceInstance instance);

    List<MicroServiceInstance> getMsfInstancesByNamespaceId(String namespaceId);

    int deleteByClusterId(@Param("clusterId")String clusterId);

    void deleteByNamespaceId(@Param("namespaceId")String namespaceId);
}
