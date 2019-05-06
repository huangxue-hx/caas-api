package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.CircuitBreakDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioCircuitBreakerService {

    /**
     * 创建熔断策略
     * @param deployName 服务名
     * @param circuitBreakDto circuitBreakDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createCircuitBreakerPolicy(String deployName, CircuitBreakDto circuitBreakDto) throws Exception;

    /**
     * 修改熔断策略
     * @param ruleId 规则id
     * @param circuitBreakDto circuitBreakDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateCircuitBreakerPolicy(String ruleId, CircuitBreakDto circuitBreakDto) throws Exception;

    /**
     * 关闭熔断策略
     * @param namespace 分区名
     * @param ruleId  规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception;

    /**
     * 开启熔断策略开关
     * @param namespace 分区名
     * @param policyName 策略名称
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openCircuitBreakerPolicy(String namespace, String policyName, String deployName, String clusterId) throws Exception;

    /**
     * 删除熔断策略
     * @param namespace 分区名
     * @param ruleId  规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;

    /**
     * 获取策略列表
     * @param deployName 服务名
     * @param namespace 分区名
     * @param ruleType 规则类型
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil listIstioPolicies(String deployName, String namespace, String ruleType, String  clusterId) throws Exception;

    /**
     * 获取熔断策略
     * @param namespace 分区名
     * @param ruleId  规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getCircuitBreakerPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;
}
