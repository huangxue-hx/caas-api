package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.FaultInjectionDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioFaultInjectionService {

    /**
     * 创建故障注入
     * @param deployName  服务名
     * @param faultInjectionDto faultInjectionDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createFaultInjectionPolicy(String deployName, FaultInjectionDto faultInjectionDto) throws Exception;

    /**
     * 修改故障注入
     * @param ruleId 规则id
     * @param faultInjectionDto faultInjectionDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateFaultInjectionPolicy(String ruleId, FaultInjectionDto faultInjectionDto) throws Exception;

    /**
     * 关闭故障注入
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeFaultInjectionPolicy(String namespace, String ruleId, String deployName, String clusterId) throws Exception;

    /**
     * 打开故障注入
     *
     * @param namespace  分区名
     * @param ruleId     规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openFaultInjectionPolicy(String namespace, String ruleId, String deployName, String clusterId, String host) throws Exception;

    /**
     * 删除故障注入
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteFaultInjectionPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;

    /**
     * 获取故障注入
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getFaultInjectionPolicy(String namespace, String ruleId, String deployName, String  clusterId) throws Exception;
}
