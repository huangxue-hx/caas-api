package com.harmonycloud.api.service;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.external.ExternalServiceBean;
import com.harmonycloud.service.platform.service.ExternalService;

/**
 * Created by ly on 2017/3/30.
 * 
 * 
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/extservices")
public class ExternalServiceController {
        @Autowired
        private ExternalService externalService;

        private Logger logger = LoggerFactory.getLogger(this.getClass());

        @ResponseBody
        @RequestMapping(method = RequestMethod.POST)
        public ActionReturnUtil createExtService(@PathVariable("tenantId") String tenantId,
                                                 @PathVariable("projectId") String projectId,
                                                 @ModelAttribute ExternalServiceBean externalServiceBean)throws Exception {
                externalServiceBean.setTenantId(tenantId);
                externalServiceBean.setProjectId(projectId);
                logger.info("创建svc和endpoint（外部服务）,param:{}", JSONObject.toJSONString(externalServiceBean));
                return externalService.createExtService(externalServiceBean);

        }

        @ResponseBody
        @RequestMapping(value = "/{serviceName}", method = RequestMethod.DELETE)
        public ActionReturnUtil deleteExtService(@PathVariable("serviceName") String serviceName,
                                                 @RequestParam(value = "clusterId",required = true) String clusterId,
                                                 @RequestParam(value = "namespace",required = true) String namespace)throws Exception {
                logger.info("删除外部服务,clusterId:{},serviceName:{}",clusterId, serviceName);
                return externalService.deleteExtService(clusterId, serviceName, namespace);

        }
        
        @ResponseBody
        @RequestMapping(method = RequestMethod.DELETE)
        public ActionReturnUtil deleteExtServiceByProject(@PathVariable("projectId") String projectId)throws Exception {
                logger.info("删除项目下的外部服务,projectId:{}",projectId);
                return externalService.deleteExtServiceByProject(projectId);

        }

        @ResponseBody
        @RequestMapping(method = RequestMethod.PUT)
        public ActionReturnUtil updateExtService(@PathVariable("tenantId") String tenantId,
                                                 @PathVariable("projectId") String projectId,
                                                 @ModelAttribute ExternalServiceBean externalServiceBean)throws Exception {
                externalServiceBean.setTenantId(tenantId);
                externalServiceBean.setProjectId(projectId);
                logger.info("修改外部服务,param:{}", JSONObject.toJSONString(externalServiceBean));
                return externalService.updateExtService(externalServiceBean);
        }

        @ResponseBody
        @RequestMapping(method = RequestMethod.GET)
        public ActionReturnUtil listExtService(@PathVariable("projectId") String projectId,
                                               @RequestParam(value = "serviceType", required = false) String serviceType,
                                               @RequestParam(value = "clusterId", required = false) String clusterId)throws Exception {
                logger.info("获取外部服务列表,projectId：{}，serviceType：{}，clusterId：{}", new String[]{projectId, serviceType, clusterId});
                return externalService.listExtService(clusterId,projectId,serviceType);

        }
        
        @ResponseBody
        @RequestMapping(value = "/extsvctypes", method = RequestMethod.GET)
        public ActionReturnUtil getExtServiceType()throws Exception {
                return ActionReturnUtil.returnSuccessWithData(externalService.listExtServiceType());
        }

        @ResponseBody
        @RequestMapping(value = "/{serviceName}", method = RequestMethod.GET)
        public ActionReturnUtil getExtService(@PathVariable("serviceName") String serviceName,
                                              @RequestParam(value = "clusterId", required = true) String clusterId,
                                              @RequestParam(value = "namespace", required = true) String namespace)throws Exception {
                logger.info("获取外部服务详细信息,serviceName:{},clusterId:{}",serviceName,clusterId);
                return externalService.getExtService(clusterId, serviceName,namespace);

        }

}