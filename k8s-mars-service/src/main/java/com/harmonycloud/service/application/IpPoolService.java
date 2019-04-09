package com.harmonycloud.service.application;

import com.harmonycloud.dao.application.bean.ProjectIpPool;
import com.harmonycloud.dto.tenant.ProjectIpPoolDto;

import java.util.List;

/**
 * 按项目分配的ip池管理服务
 */
public interface IpPoolService {


    /**
     * 获取某个项目下的ip池
     *
     * @param projectId
     * @param clusterId 如果为空获取所有集群的ip池，不为空则只查询当前集群下的ip池
     * @return
     */
    List<ProjectIpPoolDto> get(String projectId, String clusterId) throws Exception;

    /**
     * 创建ip池
     */
    void create(ProjectIpPoolDto projectIpPoolDto) throws Exception;

    /**
     * 删除ip池
     */
    void delete(String projectId, String clusterId, String name) throws Exception;

    /**
     * 校验集群
     */
    void checkCluster(String tenantId, String projectId) throws Exception;

    /**
     * 校验集群下某项目是否创建资源池
     */
    boolean checkCluster(String tenantId, String projectId, String clusterId);

    /**
     * 查询ip信息
     */
    ProjectIpPool info(String projectId, String clusterId);

    /**
     * 获取资源池名称（调rest api用）
     */
    String getPoolName(String clusterId, String poolName);
}
