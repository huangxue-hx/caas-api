package com.harmonycloud.api.integration.microservice;

import com.harmonycloud.common.enumm.MicroServiceCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.integration.microservice.DeleteAndQueryMsfDto;
import com.harmonycloud.dto.integration.microservice.UpdateMicroServiceDto;
import com.harmonycloud.service.integration.MicroServiceService;
import com.harmonycloud.service.platform.bean.microservice.DeployMsfDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author jiangmi
 * @Description 微服务调用接口
 * @Date created in 2017-12-4
 * @Modified
 */
@Controller
@RequestMapping("/msf")
public class MicroServiceController {

    @Autowired
    private MicroServiceService microServiceService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MicroServiceController.class);

    /**
     * 初始化空间（部署微服务组件）
     * @param deployMsfDto 微服务组件信息
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/namespace/deployments", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil deployMicroServices(@RequestBody DeployMsfDto deployMsfDto) {
        LOGGER.info("部署微服务组件");
        try {
            ActionReturnUtil result = microServiceService.deploySpace(deployMsfDto);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, "", null);
        }
    }

    /**
     * 重置空间
     * @param deployMsfDto 重置组件信息
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/namespace/reset", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil resetSpace(@RequestBody DeployMsfDto deployMsfDto) {
        LOGGER.info("重置微服务组件的空间");
        try {
            ActionReturnUtil result = microServiceService.resetSpace(deployMsfDto);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, e.getMessage(), null);
        }
    }

    /**
     * 更新指定的组件
     * @param updateMicroServiceParams 需要更新的组件信息
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/namespace/instances", method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateMicroService(@RequestBody UpdateMicroServiceDto updateMicroServiceParams) {
        LOGGER.info("更新微服务组件实例");
        try {
            ActionReturnUtil result = microServiceService.updateMicroService(updateMicroServiceParams);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.UPDATE_APP_FAILURE, "", null);
        }
    }

    /**
     * 清空空间
     * @param params 空间以及组件实例信息
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/namespace/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteSpace(@RequestBody DeleteAndQueryMsfDto params) {
        LOGGER.info("清空微服务组件的空间");
        try {
            ActionReturnUtil result = microServiceService.deleteSpace(params);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, e.getMessage(), null);
        }
    }

    /**
     * 删除指定的组件实例
     * @param params
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/deleteInstances", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteMicroService(@RequestBody DeleteAndQueryMsfDto params) {
        LOGGER.info("删除微服务组件实例");
        try {
            ActionReturnUtil result = microServiceService.deleteMicroService(params);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.SPACE_TASK_FAILURE, e.getMessage(), null);
        }
    }

    /**
     *  查询各种任务的执行状态
     * @param taskId 任务ID
     * @return Map<String, Object>
     */
    @RequestMapping(value = "/tasks/{task_id}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryTaskStatus(@PathVariable("task_id") String taskId) {
        LOGGER.info("查询微服务的任务状态");
        try {
            Map<String, Object> result = microServiceService.queryTaskStatus(taskId);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", null);
        }
    }

    /**
     * 查询微服务实例的状态
     * @param params 空间和实例信息
     * @return ActionReturnUtil
     */
    @RequestMapping(value = "/queryInstances", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public Map<String, Object> queryMicroServiceInstanceStatus(@RequestBody DeleteAndQueryMsfDto params) {
        LOGGER.info("查询微服务的实例状态");
        try {
            Map<String, Object> result = microServiceService.queryInstanceStatus(params);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", null);
        }
    }

    /**
     * 根据用户获取所属租户以及对应的角色（用户名使用token获取）
     *
     * @return ActionReturnUtil
     * @throws Exception
     */
    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getTenantsWithRole(HttpServletRequest request) {
        LOGGER.info("微服务根据用户查询租户以及对应的角色");
        try{
            ActionReturnUtil result = microServiceService.queryTenantsWithRole(request);
            return result;
        } catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", null);
        }

    }

    /**
     * 根据租户Id查询所有的namespace信息
     *
     * @param params
     * @return ActionReturnUtil
     * @throws Exception
     */
    @RequestMapping(value = "/queryNamespaces", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil queryNamespacesByTenantIds(@RequestBody DeleteAndQueryMsfDto params){
        LOGGER.info("微服务根据租户查询namespace");
        try {
            ActionReturnUtil result = microServiceService.getSpaceByTenant(params);
            return result;
        }catch (Exception e) {
            return ActionReturnUtil.returnCodeAndMsg(MicroServiceCodeMessage.FIND_FAILURE, "", null);
        }
    }
}
