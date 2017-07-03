package com.harmonycloud.service.platform.service;
import java.util.Map;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.external.ExternalServiceBean;

/**
 * 外部服务管理
 * 
 * 
 * @author ly
 *
 */
public interface ExternalService {

        //新增外部服务
        public ActionReturnUtil svcCreate(ExternalServiceBean externalServiceBean) throws Exception;

        //删除外部服务
        public ActionReturnUtil deleteOutService(String name) throws Exception;
        
        //根据租户删除外部服务
        public ActionReturnUtil deleteOutServicebytenant(String tenantName,String tenantId) throws Exception;

        //修改外部服务
        public ActionReturnUtil updateOutService(ExternalServiceBean externalServiceBean) throws Exception;

        //查询所有外部服务
        public ActionReturnUtil getListOutService(String tenant,String tenantId) throws Exception;

        //根据label查询外部服务
        public ActionReturnUtil getListOutServiceByLabel(String labels) throws Exception;

         //根据name查询外部服务
        public ActionReturnUtil getservicebyname(String name) throws Exception;

}