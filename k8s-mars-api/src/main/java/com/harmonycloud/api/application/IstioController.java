package com.harmonycloud.api.application;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.CircuitBreakDto;
import com.harmonycloud.dto.application.istio.RateLimitDto;
import com.harmonycloud.dto.application.istio.WhiteListsDto;
import com.harmonycloud.service.application.IstioService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
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

    @Autowired
    private IstioService istioService;

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "创建服务策略", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "根据传入对象中ruleType判断创建策略类型")
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createIstioPolicy(@PathVariable("deployName") String deployName,
                                              @ModelAttribute CircuitBreakDto circuitBreakDto,
                                              @ModelAttribute RateLimitDto rateLimitDto,
                                              @ModelAttribute WhiteListsDto whiteListsDto ) throws Exception {
        if (Objects.nonNull(circuitBreakDto) && CommonConstant.CIRCUIT_BREAKER.equals(circuitBreakDto.getRuleType())) {
            return istioService.createCircuitBreakerPolicy(deployName, circuitBreakDto);
        } else if (Objects.nonNull(rateLimitDto) && CommonConstant.RATE_LIMIT.equals(rateLimitDto.getRuleType())) {
            return istioService.createRateLimitPolicy(deployName, rateLimitDto);
        } else if (whiteListsDto != null &&  CommonConstant.WHITE_LISTS.equals(whiteListsDto.getRuleType())){
            return istioService.createWhiteListsPolicy(whiteListsDto);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_POLICY_DATA_IS_EMPTY);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "修改服务策略", response = ActionReturnUtil.class, httpMethod = "PUT", consumes = "", produces = "", notes = "根据传入对象中ruleType判断更新策略类型")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.PUT)
    public ActionReturnUtil updateIstioPolicy(@PathVariable("policyId") String policyId,
                                              @ModelAttribute CircuitBreakDto circuitBreakDto,
                                              @ModelAttribute RateLimitDto rateLimitDto,
                                              @ModelAttribute WhiteListsDto whiteListsDto ) throws Exception {
        if (Objects.nonNull(circuitBreakDto) && CommonConstant.CIRCUIT_BREAKER.equals(circuitBreakDto.getRuleType())) {
            return istioService.updateCircuitBreakerPolicy(policyId, circuitBreakDto);
        } else if (Objects.nonNull(rateLimitDto) && CommonConstant.RATE_LIMIT.equals(rateLimitDto.getRuleType())) {
            return istioService.updateRateLimitPolicy(policyId, rateLimitDto);
        }else if(Objects.nonNull(whiteListsDto) && CommonConstant.WHITE_LISTS.equals(whiteListsDto.getRuleType())){
            return istioService.updateWhiteListsPolicy(policyId, whiteListsDto);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.UPDATE_POLICY_DATA_IS_EMPTY);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "关闭策略", response = ActionReturnUtil.class, httpMethod = "PUT", consumes = "", produces = "", notes = "根据类型关闭不同的策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}/close", method = RequestMethod.PUT)
    public ActionReturnUtil closeIstioPolicy(@PathVariable("deployName") String deployName,
                                             @PathVariable("policyId") String policyId,
                                             @RequestParam("namespace") String namespace,
                                             @RequestParam("ruleName") String ruleName,
                                             @RequestParam("ruleType") String ruleType)
            throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            return istioService.closeCircuitBreakerPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            return istioService.closeRateLimitPolicy(namespace, policyId, ruleName);
        }else if(CommonConstant.WHITE_LISTS.equals(ruleType)){
            return istioService.closeWhiteListsPolicy(namespace, policyId, ruleName);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLOSE_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "开启策略", response = ActionReturnUtil.class, httpMethod = "PUT", consumes = "", produces = "", notes = "根据类型开启不同的策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}/open", method = RequestMethod.PUT)
    public ActionReturnUtil openIstioPolicy(@PathVariable("deployName") String deployName,
                                            @PathVariable("policyId") String policyId,
                                            @RequestParam("namespace") String namespace,
                                            @RequestParam("ruleName") String ruleName,
                                            @RequestParam("ruleType") String ruleType)
            throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            return istioService.openCircuitBreakerPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            return istioService.openRateLimitPolicy(namespace, policyId, ruleName);
        }else if(CommonConstant.WHITE_LISTS.equals(ruleType)){
            return  istioService.openWhiteListsPolicy(namespace,policyId,ruleName);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.OPEN_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "删除策略", response = ActionReturnUtil.class, httpMethod = "DELETE", consumes = "", produces = "", notes = "删除策略")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.DELETE)
    public ActionReturnUtil deletePolicy(@PathVariable("deployName") String deployName,
                                         @PathVariable("policyId") String policyId,
                                         @RequestParam("namespace") String namespace,
                                         @RequestParam("ruleName") String ruleName,
                                         @RequestParam("ruleType") String ruleType)
            throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            return istioService.deleteCircuitBreakerPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            return istioService.deleteRateLimitPolicy(namespace, policyId, ruleName);
        }else  if(CommonConstant.WHITE_LISTS.equals(ruleType)){
            return istioService.deleteWhiteListPolicy(namespace, policyId ,ruleName);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DELETE_POLICY_RULETYPE_IS_WRONG);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取服务下的所有策略", response = ActionReturnUtil.class, httpMethod = "GET", consumes = "", produces = "", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listIstioPolicies(@PathVariable("deployName") String deployName,
                                              @RequestParam(value = "namespace") String namespace,
                                              @RequestParam(value = "ruleType",required = false) String ruleType) throws Exception {
        return istioService.listIstioPolicies(deployName, namespace, ruleType);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取策略详情", response = ActionReturnUtil.class, httpMethod = "GET", consumes = "", produces = "", notes = "当前namespace")
    @ResponseBody
    @RequestMapping(value = "/{policyId}", method = RequestMethod.GET)
    public ActionReturnUtil getIstioPolicy(@PathVariable("deployName") String deployName,
                                           @PathVariable("policyId") String policyId,
                                           @RequestParam("namespace") String namespace,
                                           @RequestParam("ruleName") String ruleName,
                                           @RequestParam("ruleType") String ruleType) throws Exception {
        if (CommonConstant.CIRCUIT_BREAKER.equals(ruleType)) {
            return istioService.getCircuitBreakerPolicy(namespace, policyId, deployName);
        } else if (CommonConstant.RATE_LIMIT.equals(ruleType)) {
            return istioService.getRateLimitPolicy(namespace, policyId, ruleName);
        }else if(CommonConstant.WHITE_LISTS.equals(ruleType)){
            return istioService.getWhiteListPolicy(namespace, policyId , ruleName);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.GET_POLICY_RULETYPE_IS_WRONG);
    }
}
