package com.harmonycloud.api.service;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.dto.external.ExternalServiceBean;
import com.harmonycloud.service.platform.service.ServiceEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/serviceentrys")
public class ServiceEntryController {
    @Autowired
    private ServiceEntryService serviceEntryService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil createServiceEntry(@PathVariable("tenantId") String tenantId,
                                               @PathVariable("projectId") String projectId,
                                               @ModelAttribute ServiceEntryDto serviceEntryDto) throws Exception {
        serviceEntryDto.setTenantId(tenantId);
        serviceEntryDto.setProjectId(projectId);
        logger.info("创建外部服务入口ServiceEntry,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.createServiceEntry(serviceEntryDto);
    }

    @ResponseBody
    @RequestMapping(value = "/createInsServiceEntry", method = RequestMethod.POST)
    public ActionReturnUtil createInsServiceEntry(@PathVariable("tenantId") String tenantId,
                                                  @PathVariable("projectId") String projectId,
                                                  @ModelAttribute ServiceEntryDto serviceEntryDto) throws Exception {
        serviceEntryDto.setTenantId(tenantId);
        serviceEntryDto.setProjectId(projectId);
        logger.info("创建内部服务入口ServiceEntry,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.createInsServiceEntry(serviceEntryDto);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.PUT)
    public ActionReturnUtil updateServiceEntry(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("projectId") String projectId,
                                             @ModelAttribute ServiceEntryDto serviceEntryDto)throws Exception {
        serviceEntryDto.setTenantId(tenantId);
        serviceEntryDto.setProjectId(projectId);
        logger.info("修改外部服务入口,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.updateServiceEntry(serviceEntryDto);
    }

    @ResponseBody
    @RequestMapping(value = "/updateInsServiceEntry",method = RequestMethod.PUT)
    public ActionReturnUtil updateInsServiceEntry(@PathVariable("tenantId") String tenantId,
                                             @PathVariable("projectId") String projectId,
                                             @ModelAttribute ServiceEntryDto serviceEntryDto)throws Exception {
        serviceEntryDto.setTenantId(tenantId);
        serviceEntryDto.setProjectId(projectId);
        logger.info("修改内部服务入口,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.updateInsServiceEntry(serviceEntryDto);
    }

    @ResponseBody
    @RequestMapping(value = "/{serviceEntryName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteExtService(@PathVariable("serviceEntryName") String serviceEntryName,
                                             @RequestParam(value = "clusterId", required = true) String clusterId,
                                             @RequestParam(value = "namespace", required = true) String namespace,
                                             @RequestParam(value = "serviceEntryType", required = true) String serviceEntryType) throws Exception {
        logger.info("删除外部服务,clusterId:{},serviceName:{}", clusterId, serviceEntryName);
        return serviceEntryService.deleteExtServiceEntry(clusterId, serviceEntryName, namespace,serviceEntryType);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listExtServiceEntry(@PathVariable("projectId") String projectId,
                                                 @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        return serviceEntryService.listExtServiceEntry(clusterId, projectId);
    }

    @ResponseBody
    @RequestMapping(value = "/getServiceEntry", method = RequestMethod.GET)
    public ActionReturnUtil getExtService(@RequestParam("serviceEntryName") String serviceEntryName,
                                          @RequestParam(value = "clusterId", required = true) String clusterId,
                                          @RequestParam(value = "namespace", required = true) String namespace)throws Exception {
//                logger.info("获取外部服务详细信息,serviceName:{},clusterId:{}",serviceName,clusterId);
        return serviceEntryService.getServiceEntry(clusterId, serviceEntryName,namespace);
    }


    @ResponseBody
    @RequestMapping(value = "/istioOpenCluster", method = RequestMethod.GET)
    public ActionReturnUtil listIstioOpenCluster(@PathVariable("projectId") String projectId) throws Exception {
        return serviceEntryService.listIstioOpenCluster();
    }




}
