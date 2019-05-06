package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.RateLimitDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioRateLimitService {

    /**
     * 创建限流策略
     * @param deployName  服务名
     * @param rateLimitDto rateLimitDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createRateLimitPolicy(String deployName, RateLimitDto rateLimitDto) throws Exception;

    /**
     * 修改限流策略
     * @param ruleId 规则id
     * @param rateLimitDto rateLimitDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateRateLimitPolicy(String ruleId, RateLimitDto rateLimitDto) throws Exception;

    /**
     * 关闭限流策略
     * @param namespace 服务名
     * @param ruleId 规则id
     * @param deployName 服务名称
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 打开限流策略
     * @param namespace 服务名
     * @param ruleId 规则id
     * @param deployName 服务名称
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 删除限流策略
     * @param namespace  服务名
     * @param ruleId 规则id
     * @param deployName 服务名称
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception;
    /**
     * 获取限流策略
     * @param namespace 服务名
     * @param ruleId 规则id
     * @param deployName deployName
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getRateLimitPolicy(String namespace, String ruleId, String deployName) throws Exception;
}
