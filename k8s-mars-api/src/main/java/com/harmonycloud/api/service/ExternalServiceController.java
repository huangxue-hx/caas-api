package com.harmonycloud.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.ExternalTypeMapper;
import com.harmonycloud.dto.external.ExternalServiceBean;
import com.harmonycloud.service.platform.service.ExternalService;

/**
 * Created by ly on 2017/3/30.
 * 
 * 
 */
@Controller
@RequestMapping("/externalservice")
public class ExternalServiceController {
        @Autowired
        private ExternalService externalService;
        
        @Autowired
        private ExternalTypeMapper externalTypeMapper;

        private Logger logger = LoggerFactory.getLogger(this.getClass());

        @ResponseBody
        @RequestMapping(value = "/svc", method = RequestMethod.POST)
        public ActionReturnUtil createOutService(@ModelAttribute ExternalServiceBean externalServiceBean)throws Exception {
                try {
                        logger.info("创建svc和endpoint（外部服务）");
                        return externalService.svcCreate(externalServiceBean);
                } catch (Exception e) {
                        logger.error("创建svc和endpoint（外部服务）错误"+",e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }

        @ResponseBody
        @RequestMapping(value = "/service", method = RequestMethod.DELETE)
        public ActionReturnUtil deleteOutService(@RequestParam(value = "name", required = true) String  name )throws Exception {
                try {
                        logger.info("删除外部服务");
                        return externalService.deleteOutService(name);
                } catch (Exception e) {
                        logger.error("删除外部服务错误"+",e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }
        
        @ResponseBody
        @RequestMapping(value = "/servicebytenant", method = RequestMethod.DELETE)
        public ActionReturnUtil deleteOutServicebytenant(@RequestParam(value = "tenant", required = true) String tenantName,String tenantId )throws Exception {
                try {
                        logger.info("删除外部服务");
                        return externalService.deleteOutServicebytenant(tenantName,tenantId);
                } catch (Exception e) {
                        logger.error("删除外部服务错误"+",e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }

        @ResponseBody
        @RequestMapping(value = "/service", method = RequestMethod.PUT)
        public ActionReturnUtil updateOutService(@ModelAttribute ExternalServiceBean externalServiceBean)throws Exception {
                try {
                        logger.info("更新外部服务");
                        return externalService.updateOutService(externalServiceBean);
                } catch (Exception e) {
                        logger.error("更新外部服务错误");
                        e.printStackTrace();
                        throw e;
                }
        }

        @ResponseBody
        @RequestMapping(value = "/services", method = RequestMethod.GET)
        public ActionReturnUtil getListOutServiceBytenant(@RequestParam(value = "tenant", required = true) String tenant,@RequestParam(value = "tenantId", required = true) String tenantId)throws Exception {
                try {
                        logger.info("获取外部服务列表");
                        return externalService.getListOutService(tenant,tenantId);
                } catch (Exception e) {
                        logger.error("获取外部服务列表错误,e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }
        
        @ResponseBody
        @RequestMapping(value = "/getservicetype", method = RequestMethod.GET)
        public ActionReturnUtil getservicetype()throws Exception {
                try {
                        logger.info("获取外部服务类型列表");
                        return ActionReturnUtil.returnSuccessWithData(externalTypeMapper.list());
                } catch (Exception e) {
                        logger.error("获取外部服务类型列表错误,e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }

        @ResponseBody
        @RequestMapping(value = "/getservicebyname", method = RequestMethod.GET)
        public ActionReturnUtil getservicebyname(@RequestParam(value = "name", required = true) String  name )throws Exception {
                try {
                        logger.info("获取外部服务详细信息");
                        return externalService.getservicebyname(name);
                } catch (Exception e) {
                        logger.error("获取外部服务详细信息错误,e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }

        @ResponseBody
        @RequestMapping(value = "/service/labels", method = RequestMethod.GET)
        public ActionReturnUtil getListOutServiceByLabel(@RequestParam(value = "labels", required = true) String labels)throws Exception {   
        	try {
                        logger.info("获取外部服务列表");
                        return externalService.getListOutServiceByLabel(labels);
                } catch (Exception e) {
                        logger.error("获取外部服务列表错误,e="+e.getMessage());
                        e.printStackTrace();
                        throw e;
                }
        }
        
        

}