package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.TimeoutRetryDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioTimeoutRetryService {

    /**
     * 创建超时重试
     * @param deployName  服务名
     * @param timeoutRetryDto timeoutRetryDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createTimeoutRetryPolicy(String deployName, TimeoutRetryDto timeoutRetryDto) throws Exception;

    /**
     * 修改超时重试
     * @param ruleId 规则id
     * @param timeoutRetryDto timeoutRetryDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateTimeoutRetryPolicy(String ruleId, TimeoutRetryDto timeoutRetryDto) throws Exception;

    /**
     * 关闭超时重试
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception;

    /**
     * 打开超时重试
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String  clusterId, String host) throws Exception;

    /**
     * 删除超时重试
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;

    /**
     * 获取超时重试
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getTimeoutRetryPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;
}
