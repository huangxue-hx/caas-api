package com.harmonycloud.api.application;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.*;
import com.harmonycloud.service.istio.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * Update by weg on 18-11-21.
 */
@Api(value = "IstioController", description = "微服务治理接口")
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys/{deployName}/istiopolicies")
public class IstioController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IstioCommonService istioCommonService;

    @Autowired
    private IstioCircuitBreakerService circuitBreakerService;

    @Autowired
    private IstioRateLimitService rateLimitService;

    @Autowired
    private IstioWhiteListsService whiteListsService;

    @Autowired
    private IstioTrafficShiftingService trafficShiftingService;

    @Autowired
    private IstioFaultInjectionService faultInjectionService;

    @Autowired
    private IstioTimeoutRetryService timeoutRetryService;

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "创建服务策略", response = ActionReturnUtil.class, httpMethod = "POST", notes = "根据传入对象中ruleType判断创建策略类型")
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createIstioPolicy(@PathVariable("deployName") String deployName,
                                              @ModelAttribute CircuitBreakDto circuitBreakDto,
                                              @ModelAttribute RateLimitDto rateLimitDto,
                                              @ModelAttribute WhiteListsDto whiteListsDto,
                                              @ModelAttribute TrafficShiftingDto trafficShiftingDto,
                                              @ModelAttribute FaultInjectionDto faultInjectionDto,
                                              @ModelAttribute TimeoutRetryDto timeoutRetryDto) throws Exception {
        if (Objects.nonNull(circuitBreakDto) && CommonConstant.CIRCUIT_BREAKER.equals(circuitBreakDto.getRuleType())) {
            logger.info("创建熔断策略,param:{}", JSONObject.toJSONString(circuitBreakDto));
            return circuitBreakerService.createCircuitBreakerPolicy(deployName, circuitBreakDto);
        } else if (Objects.nonNull(rateLimitDto) && CommonConstant.RATE_LIMIT.equals(rateLimitDto.getRuleType())) {
            logger.info("创建限流策略,param:{}", JSONObject.toJSONString(rateLimitDto));
            return rateLimitService.createRateLimitPolicy(deployName, rateLimitDto);
        } else if (whiteListsDto != null &&  CommonConstant.WHITE_LISTS.equals(whiteListsDto.getRuleType())){
            logger.info("创建白名单策略,param:{}", JSONObject.toJSONString(whiteListsDto));
            return whiteListsService.createWhiteListsPolicy(whiteListsDto);
        } else if (trafficShiftingDto != null &&  CommonConstant.TRAFFIC_SHIFTING.equals(trafficShiftingDto.getRuleType())) {
            logger.info("创建智能路由策略,param:{}", JSONObject.toJSONString(trafficShiftingDto));
            return trafficShiftingService.createTrafficShiftingPolicy(deployName, trafficShiftingDto);
        } else if (faultInjectionDto != null &&  CommonConstant.FAULT_INJECTION.equals(faultInjectionDto.getRuleType())) {
            logger.info("创建故障注入策略,param:{}", JSONObject.toJSONString(faultInjectionDto));
            return faultInjectionService.createFaultInjectionPolicy(deployName, faultInjectionDto);
        } else if (timeoutRetryDto != null &&  CommonConstant.TIMEOUT_RETRY.equals(timeoutRetryDto.getRuleType())) {
            logger.info("创建超时重试策略,param:{}", JSONObject.toJSONString(timeoutRetryDto));
            return timeoutRetryService.createTimeoutRetryPolicy(deployName, timeoutRetryDto);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_POLICY_DATA_IS_EMPTY);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "修改服务策略", response = ActionReturnUtil.class, httpMethod = "PUT", notes = "根据传入对象中ruleType判断更新策略类型")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.PUT)
    public ActionReturnUtil updateIstioPolicy(@PathVariable("policyId") String policyId,
                                              @ModelAttribute CircuitBreakDto circuitBreakDto,
                                              @ModelAttribute RateLimitDto rateLimitDto,
                                              @ModelAttribute WhiteListsDto whiteListsDto,
                                              @ModelAttribute TrafficShiftingDto trafficShiftingDto,
                                              @ModelAttribute FaultInjectionDto faultInjectionDto,
                                              @ModelAttribute TimeoutRetryDto timeoutRetryDto) throws Exception {
        if (Objects.nonNull(circuitBreakDto) && CommonConstant.CIRCUIT_BREAKER.equals(circuitBreakDto.getRuleType())) {
            logger.info("修改熔断策略,param:{}", JSONObject.toJSONString(circuitBreakDto));
            return circuitBreakerService.updateCircuitBreakerPolicy(policyId, circuitBreakDto);
        } else if (Objects.nonNull(rateLimitDto) && CommonConstant.RATE_LIMIT.equals(rateLimitDto.getRuleType())) {
            logger.info("修改限流策略,param:{}", JSONObject.toJSONString(rateLimitDto));
            return rateLimitService.updateRateLimitPolicy(policyId, rateLimitDto);
        } else if (Objects.nonNull(whiteListsDto) && CommonConstant.WHITE_LISTS.equals(whiteListsDto.getRuleType())){
            logger.info("修改白名单策略,param:{}", JSONObject.toJSONString(whiteListsDto));
            return whiteListsService.updateWhiteListsPolicy(policyId, whiteListsDto);
        } else if (Objects.nonNull(trafficShiftingDto) && CommonConstant.TRAFFIC_SHIFTING.equals(trafficShiftingDto.getRuleType())){
            logger.info("修改智能路由策略,param:{}", JSONObject.toJSONString(trafficShiftingDto));
            return trafficShiftingService.updateTrafficShiftingPolicy(policyId, trafficShiftingDto);
        } else if (Objects.nonNull(faultInjectionDto) && CommonConstant.FAULT_INJECTION.equals(faultInjectionDto.getRuleType())){
            logger.info("修改故障注入策略,param:{}", JSONObject.toJSONString(faultInjectionDto));
            return faultInjectionService.updateFaultInjectionPolicy(policyId, faultInjectionDto);
        } else if (Objects.nonNull(timeoutRetryDto) && CommonConstant.TIMEOUT_RETRY.equals(timeoutRetryDto.getRuleType())){
            logger.info("修改超时重试策略,param:{}", JSONObject.toJSONString(timeoutRetryDto));
            return timeoutRetryService.updateTimeoutRetryPolicy(policyId, timeoutRetryDto);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UPDATE_POLICY_DATA_IS_EMPTY);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "关闭策略", response = ActionReturnUtil.class, httpMethod = "PUT", notes = "根据类型关闭不同的策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}/close", method = RequestMethod.PUT)
    public ActionReturnUtil closeIstioPolicy(@PathVariable("deployName") String deployName,
                                             @PathVariable("policyId") String policyId,
                                             @RequestParam("namespace") String namespace,
                                             @RequestParam("ruleType") String ruleType,
                                             @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            logger.info("关闭熔断策略,policyId:{},deployName:{}", policyId, deployName);
            return circuitBreakerService.closeCircuitBreakerPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            logger.info("关闭限流策略,policyId:{},deployName:{}", policyId, deployName);
            return rateLimitService.closeRateLimitPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.WHITE_LISTS.equals(ruleType)) {
            logger.info("关闭白名单策略,policyId:{},deployName:{}", policyId, deployName);
            return whiteListsService.closeWhiteListsPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.TRAFFIC_SHIFTING.equals(ruleType)) {
            logger.info("关闭智能路由策略,policyId:{},deployName:{}", policyId, deployName);
            return trafficShiftingService.closeTrafficShiftingPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.FAULT_INJECTION.equals(ruleType)) {
            logger.info("关闭故障注入策略,policyId:{},deployName:{}", policyId, deployName);
            return faultInjectionService.closeFaultInjectionPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.TIMEOUT_RETRY.equals(ruleType)) {
            logger.info("关闭超时重试策略,policyId:{},deployName:{}", policyId, deployName);
            return timeoutRetryService.closeTimeoutRetryPolicy(namespace, policyId, deployName, clusterId);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLOSE_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "开启策略", response = ActionReturnUtil.class, httpMethod = "PUT", notes = "根据类型开启不同的策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}/open", method = RequestMethod.PUT)
    public ActionReturnUtil openIstioPolicy(@PathVariable("deployName") String deployName,
                                            @PathVariable("policyId") String policyId,
                                            @RequestParam("namespace") String namespace,
                                            @RequestParam("ruleType") String ruleType,
                                            @RequestParam(value = "clusterId", required = false) String clusterId,
                                            @RequestParam(value = "host", required = false) String host) throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            logger.info("开启熔断策略,policyId:{},deployName:{}", policyId, deployName);
            return circuitBreakerService.openCircuitBreakerPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            logger.info("开启限流策略,policyId:{},deployName:{}", policyId, deployName);
            return rateLimitService.openRateLimitPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.WHITE_LISTS.equals(ruleType)) {
            logger.info("开启白名单策略,policyId:{},deployName:{}", policyId, deployName);
            return whiteListsService.openWhiteListsPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.TRAFFIC_SHIFTING.equals(ruleType)) {
            logger.info("开启智能路由策略,policyId:{},deployName:{}", policyId, deployName);
            return trafficShiftingService.openTrafficShiftingPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.FAULT_INJECTION.equals(ruleType)) {
            logger.info("开启故障注入策略,policyId:{},deployName:{}", policyId, deployName);
            return faultInjectionService.openFaultInjectionPolicy(namespace, policyId, deployName, clusterId, host);
        } else if (CommonConstant.TIMEOUT_RETRY.equals(ruleType)) {
            logger.info("开启超时重试策略,policyId:{},deployName:{}", policyId, deployName);
            return timeoutRetryService.openTimeoutRetryPolicy(namespace, policyId, deployName, clusterId, host);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.OPEN_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "删除策略", response = ActionReturnUtil.class, httpMethod = "DELETE", notes = "删除策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.DELETE)
    public ActionReturnUtil deletePolicy(@PathVariable("deployName") String deployName,
                                         @PathVariable("policyId") String policyId,
                                         @RequestParam("namespace") String namespace,
                                         @RequestParam("ruleType") String ruleType,
                                         @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            logger.info("删除熔断策略,policyId:{},deployName:{}", policyId, deployName);
            return circuitBreakerService.deleteCircuitBreakerPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            logger.info("删除限流策略,policyId:{},deployName:{}", policyId, deployName);
            return rateLimitService.deleteRateLimitPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.WHITE_LISTS.equals(ruleType)) {
            logger.info("删除白名单策略,policyId:{},deployName:{}", policyId, deployName);
            return whiteListsService.deleteWhiteListsPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.TRAFFIC_SHIFTING.equals(ruleType)) {
            logger.info("删除智能路由策略,policyId:{},deployName:{}", policyId, deployName);
            return trafficShiftingService.deleteTrafficShiftingPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.FAULT_INJECTION.equals(ruleType)) {
            logger.info("删除故障注入策略,policyId:{},deployName:{}", policyId, deployName);
            return faultInjectionService.deleteFaultInjectionPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.TIMEOUT_RETRY.equals(ruleType)) {
            logger.info("删除超时重试策略,policyId:{},deployName:{}", policyId, deployName);
            return timeoutRetryService.deleteTimeoutRetryPolicy(namespace, policyId, deployName, clusterId);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DELETE_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取服务下的所有策略", response = ActionReturnUtil.class, httpMethod = "GET", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listIstioPolicies(@PathVariable("deployName") String deployName,
                                              @RequestParam(value = "namespace") String namespace,
                                              @RequestParam(value = "ruleType",required = false) String ruleType,
                                              @RequestParam("clusterId") String clusterId) throws Exception {
        logger.info("获取服务下的所有策略,deployName:{},namespace:{}", deployName, namespace);
        return istioCommonService.listIstioPolicies(deployName, namespace, ruleType, clusterId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取策略详情", response = ActionReturnUtil.class, httpMethod = "GET", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.GET)
    public ActionReturnUtil getIstioPolicy(@PathVariable("deployName") String deployName,
                                           @PathVariable("policyId") String policyId,
                                           @RequestParam("namespace") String namespace,
                                           @RequestParam("ruleType") String ruleType,
                                           @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            logger.info("获取熔断策略详情,policyId:{},deployName:{}", policyId, deployName);
            return circuitBreakerService.getCircuitBreakerPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            logger.info("获取限流策略详情,policyId:{},deployName:{}", policyId, deployName);
            return rateLimitService.getRateLimitPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.WHITE_LISTS.equals(ruleType)) {
            logger.info("获取白名单策略详情,policyId:{},deployName:{}", policyId, deployName);
            return whiteListsService.getWhiteListsPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.TRAFFIC_SHIFTING.equals(ruleType)) {
            logger.info("获取智能路由策略详情,policyId:{},deployName:{}", policyId, deployName);
            return trafficShiftingService.getTrafficShiftingPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.FAULT_INJECTION.equals(ruleType)) {
            logger.info("获取故障注入策略详情,policyId:{},deployName:{}", policyId, deployName);
            return faultInjectionService.getFaultInjectionPolicy(namespace, policyId, deployName, clusterId);
        } else if (CommonConstant.TIMEOUT_RETRY.equals(ruleType)) {
            logger.info("获取超时重试策略详情,policyId:{},deployName:{}", policyId, deployName);
            return timeoutRetryService.getTimeoutRetryPolicy(namespace, policyId, deployName, clusterId);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.GET_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取目标服务版本", response = ActionReturnUtil.class, httpMethod = "GET", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(value = "/desServiceVersions", method = RequestMethod.GET)
    public ActionReturnUtil getDesServiceVersion(@PathVariable("deployName") String deployName,
                                           @RequestParam("namespace") String namespace) throws Exception {
        logger.info("获取目标服务版本,deployName:{},namespace:{}", deployName, namespace);
        return istioCommonService.getDesServiceVersion(deployName, namespace);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取源服务版本", response = ActionReturnUtil.class, httpMethod = "GET", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(value = "/sourceServiceVersions", method = RequestMethod.GET)
    public ActionReturnUtil getSourceServiceVersion(@PathVariable("deployName") String deployName,
                                                    @RequestParam("namespace") String namespace,
                                                    @RequestParam("serviceType") String serviceType) throws Exception {
        logger.info("获取源服务版本,deployName:{},namespace:{}", deployName, namespace);
        return istioCommonService.getSourceServiceVersion(deployName, namespace, serviceType);
    }
}
