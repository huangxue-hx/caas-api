package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.dto.external.ExternalServiceBean;

public interface ServiceEntryService {

    //新增外部服务入口
    public ActionReturnUtil createServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;

    //新增内部服务入口
    public ActionReturnUtil createInsServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;

    //修改外部服务入口
    public ActionReturnUtil updateServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;

    //修改内部服务入口
    public ActionReturnUtil updateInsServiceEntry(ServiceEntryDto serviceEntryDto) throws Exception;


    //删除外部服务
    public ActionReturnUtil deleteExtServiceEntry(String clusterId, String serviceEntryName,String namespace,String serviceEntryType) throws Exception;

    //查询服务入口列表
    public ActionReturnUtil listExtServiceEntry(String  clusterId,String projectId) throws  Exception;

    //查询服务入口详情
    public ActionReturnUtil getServiceEntry(String  clusterId,String serviceEntryName,String namespace) throws Exception;

    //查询istio开关被开启的集群
    public ActionReturnUtil listIstioOpenCluster() throws Exception;

}