package com.harmonycloud.service.istio;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * create by weg on 18-12-27.
 */
public interface IstioCommonService {

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
     * 获取集群开关信息
     * @param clusterId 集群id
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getClusterIstioPolicySwitch(String clusterId) throws Exception;

    /**
     * 开启/关闭集群开关状态
     * @param status 开关状态
     * @param clusterId 集群id
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateClusterIstioPolicySwitch(boolean status, String clusterId) throws Exception;

    /**
     * 获取分区istio开关信息
     *
     * @param namespace 分区名
     * @param clusterId 集群id
     * @return 返回值
     * @throws MarsRuntimeException 异常信息
     */
    ActionReturnUtil getNamespaceIstioPolicySwitch(String namespace, String clusterId) throws Exception;

    /**
     * 获取分区istio开关信息(容器平台调用)
     * @param namespace 分区名
     * @return 返回值
     * @throws MarsRuntimeException 异常信息
     */
    boolean isIstioEnabled(String namespace) throws Exception;

    /**
     * 开启/关闭istio策略开关
     * @param status 开关状态
     * @param clusterId 集群id
     * @param namespaceName 分区名称
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateNamespaceIstioPolicySwitch(boolean status, String clusterId, String namespaceName) throws Exception;

    /**
     * 获取全局开关状态
     * @param clusterId 集群id
     * @return 返回值
     * @throws Exception 异常信息
     */
    boolean  getIstioGlobalStatus(String clusterId) throws Exception;

    /**
     * @param deployName 服务名
     * @param namespace 分区名
     * @return  返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getDesServiceVersion(String deployName, String namespace) throws Exception;

    /**
     * @param deployName 服务名
     * @param namespace  分区名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil getSourceServiceVersion(String deployName, String namespace, String serviceType) throws Exception;

    /**
     * 获取开启istio的集群
     * @return  返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil listIstioCluster() throws Exception;

    /**
     * 删除策略信息
     * @param namespace 分区名称
     * @param service  服务名
     * @return  返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteIstioPolicy(String namespace, String service, String  clusterId) throws Exception;

    /**
     * 创建destinationRule
     * @param deployName  服务名
     * @param namespace 分区名
     * @param version 版本
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createDestinationRule(String deployName, String namespace, String version) throws Exception;

    /**
     * 修改 destinationRule
     *
     * @param deployName 服务名
     * @param namespace  分区名
     * @param version    版本
     * @param isBlueGreen    是否为蓝绿发布
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateDestinationRule(String deployName, String namespace, String version, boolean isBlueGreen) throws Exception;

    /**
     * 删除destinationRule
     * @param deployName 服务名
     * @param namespace 分区名
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteDestinationRule(String deployName, String namespace) throws Exception;
}
