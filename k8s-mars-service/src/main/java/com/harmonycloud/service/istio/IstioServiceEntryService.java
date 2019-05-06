package com.harmonycloud.service.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;

/**
 * create by weg on 18-12-27.
 */
public interface IstioServiceEntryService {

    /**
     * 创建外部服务入口
     * @param serviceEntryDto serviceEntryDto类
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createExternalServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;

    /**
     * 创建内部服务入口
     * @param serviceEntryDto serviceEntryDto类
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil createInternalServiceEntry(ServiceEntryDto serviceEntryDto, String  projectId) throws Exception;

    /**
     * 修改外部服务入口
     * @param serviceEntryDto serviceEntryDto类
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateExternalServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;

    /**
     * 修改内部服务入口
     * @param serviceEntryDto serviceEntryDto类
     * @param projectId 项目id
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil updateInternalServiceEntry(ServiceEntryDto serviceEntryDto, String projectId) throws Exception;

    /**
     * 删除服务入口
     * @param serviceEntryName 外部服务名
     * @param namespace 分区名
     * @param hosts 域名地址
     * @param clusterId 集群id
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil deleteInternalServiceEntry(String serviceEntryName, String namespace, String hosts, String clusterId) throws Exception;

    /**
     * @param serviceEntryName 外部服务名
     * @param namespace 分区名
     * @param hosts
     * @param clusterId
     * @return
     * @throws Exception
     */
    ActionReturnUtil deleteExternalServiceEntry(String serviceEntryName, String clusterId) throws Exception;

    /**
     *获取服务入口列表
     * @param projectId  项目id
     * @param clusterId  集群id
     * @return 返回值
     * @throws Exception 异常信息
     */
    ActionReturnUtil listServiceEntry(String projectId, String clusterId, String  serviceEntryType,String namespace, boolean isTenantScope) throws Exception;

    /**
     * 获取serviceentry详情信息
     * @param serviceEntryName 外部服务名
     * @param namespace 分区名
     * @param clusterId 集群id
     * @param serviceEntryType  serviceEntry类型
     * @return 返回值
     * @throws Exception  异常信息
     */
    ActionReturnUtil getServiceEntry(String serviceEntryName, String namespace, String clusterId, String serviceEntryType) throws Exception;
}
