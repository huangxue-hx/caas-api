package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.TrafficShiftingDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioTrafficShiftingService {

    /**
     * 创建智能路由
     * @param deployName  服务名
     * @param trafficShiftingDto trafficShiftingDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createTrafficShiftingPolicy(String deployName, TrafficShiftingDto trafficShiftingDto) throws Exception;

    /**
     * 修改智能路由
     * @param ruleId 规则id
     * @param trafficShiftingDto trafficShiftingDto
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateTrafficShiftingPolicy(String ruleId, TrafficShiftingDto trafficShiftingDto) throws Exception;

    /**
     * 关闭智能路由
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回
     * @throws Exception 异常信息
     */
    ActionReturnUtil closeTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 打开智能路由
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil openTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 删除智能路由
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    /**
     * 获取智能路由
     * @param namespace 分区名
     * @param ruleId 规则id
     * @param deployName 服务名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;
}
