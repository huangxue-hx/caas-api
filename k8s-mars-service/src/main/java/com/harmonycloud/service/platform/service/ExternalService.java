package com.harmonycloud.service.platform.service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.bean.ExternalTypeBean;
import com.harmonycloud.dto.external.ExternalServiceBean;

import java.util.List;

/**
 * 外部服务管理
 * 
 * 
 * @author ly
 *
 */
public interface ExternalService {

        //新增外部服务
        public ActionReturnUtil createExtService(ExternalServiceBean externalServiceBean) throws Exception;

        //删除外部服务
        public ActionReturnUtil deleteExtService(String clusterId, String name, String namespace) throws Exception;
        
        //根据项目删除外部服务
        public ActionReturnUtil deleteExtServiceByProject(String projectId) throws Exception;

        //修改外部服务
        public ActionReturnUtil updateExtService(ExternalServiceBean externalServiceBean) throws Exception;

        //查询外部服务
        public ActionReturnUtil listExtService(String clusterId, String projectId,String serviceType) throws Exception;

         //根据name查询外部服务
        public ActionReturnUtil getExtService(String clusterId, String name,String namespace) throws Exception;

        List<ExternalTypeBean> listExtServiceType() throws Exception;

}