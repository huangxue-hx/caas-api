package com.harmonycloud.service.integration;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.integration.microservice.DeleteAndQueryMsfDto;
import com.harmonycloud.dto.integration.microservice.UpdateMicroServiceDto;
import com.harmonycloud.service.platform.bean.microservice.DeployMsfDto;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * created by jiangmi at 2017-12-4
 */
@Service
public interface MicroServiceService {

    /**
     * 更新微服务实例：副本数、cpu、内存
     * @param updateParams
     * @return ActionReturnUtil
     */
    public ActionReturnUtil updateMicroService(UpdateMicroServiceDto updateParams) throws Exception;


    /**
     * 删除微服务组件实例
     * @param deleteParams
     * @return ActionReturnUtil
     */
    public ActionReturnUtil deleteMicroService(DeleteAndQueryMsfDto deleteParams) throws Exception;

    /**
     * 查询任务执行状态
     * @param taskId
     * @return ActionReturnUtil
     */
    public Map<String, Object> queryTaskStatus(String taskId) throws Exception;

    /**
     * 查询微服务实例状态信息
     * @param params
     * @return ActionReturnUtil
     */
    public Map<String, Object> queryInstanceStatus(DeleteAndQueryMsfDto params) throws Exception;

    /**
     * 根据用户以及角色获取租户
     * @param request
     * @return ActionReturnUtil
     */
    public ActionReturnUtil queryTenantsWithRole(HttpServletRequest request) throws Exception;

    /**
     * 部署微服务组件
     * @param deployParams
     * @return ActionReturnUtil
     */
    public ActionReturnUtil deploySpace(DeployMsfDto deployParams) throws Exception;

    /**
     * 重置空间
     * @param resetParams
     * @return ActionReturnUtil
     */
    public ActionReturnUtil resetSpace(DeployMsfDto resetParams) throws Exception;

    /**
     * 删除空间
     * @param deleteAndQueryMsfDto
     * @return ActionReturnUtil
     */
    public ActionReturnUtil deleteSpace(DeleteAndQueryMsfDto deleteAndQueryMsfDto) throws Exception;

    /**
     * 根据租户获取分区列表
     * @param params
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil getSpaceByTenant(DeleteAndQueryMsfDto params) throws Exception;

}
