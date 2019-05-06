package com.harmonycloud.api.service;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.service.istio.IstioServiceEntryService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Api(value = "ServiceEntryController", description = "serviceEntry相关接口")
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/serviceentries")
public class ServiceEntryController {

    @Autowired
    private IstioServiceEntryService serviceEntryService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "创建内部服务入口功能", response = ActionReturnUtil.class, httpMethod = "", consumes = "", produces = "", notes = "")
    @ApiImplicitParams({@ApiImplicitParam(name = "tenantId", value = "租户id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目id", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/internalserviceentry", method = RequestMethod.POST)
    public ActionReturnUtil createInternalServiceEntry(@PathVariable("projectId") String projectId,
                                                  @ModelAttribute ServiceEntryDto serviceEntryDto) throws Exception {
        logger.info("创建内部服务入口ServiceEntry,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.createInternalServiceEntry(serviceEntryDto, projectId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "修改内部服务入口功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "tenantId", value = "租户id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目id", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/internalserviceentry",method = RequestMethod.PUT)
    public ActionReturnUtil updateInternalServiceEntry(@PathVariable("projectId") String projectId,
                                             @ModelAttribute ServiceEntryDto serviceEntryDto)throws Exception {
        logger.info("修改内部服务入口,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.updateInternalServiceEntry(serviceEntryDto,projectId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "删除内部服务入口功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "serviceEntryName", value = "服务入口名称", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区名称", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "hosts", value = "域名（外部服务入口可以不传）", paramType = "query", dataType = "String"),
    })
    @ResponseBody
    @RequestMapping(value = "/{serviceEntryName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteInternalServiceEntry(@PathVariable("serviceEntryName") String serviceEntryName,
                                             @RequestParam(value ="namespace", required = false) String namespace,
                                             @RequestParam(value = "hosts", required = false) String hosts,
                                             @RequestParam("clusterId") String clusterId) throws Exception {
        logger.info("删除服务入口,serviceName:{}", serviceEntryName);
        return serviceEntryService.deleteInternalServiceEntry(serviceEntryName, namespace, hosts, clusterId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取内部服务入口列表功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listServiceEntry(@PathVariable("projectId") String projectId,
                                             @RequestParam(value = "clusterId", required =false) String clusterId,
                                             @RequestParam(value ="namespace", required = false) String namespace,
                                             @RequestParam(value = "isTenantScope", required = false) boolean isTenantScope) throws Exception {
        logger.info("获取服务入口列表,projectId:{},clusterId:{}",projectId,clusterId);
        return serviceEntryService.listServiceEntry(projectId, clusterId, CommonConstant.INTERNAL_SERVICE_ENTRY + "", namespace, isTenantScope);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取内部服务入口详细信息功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "serviceEntryName", value = "服务入口名称", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区名", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/{serviceEntryName}", method = RequestMethod.GET)
    public ActionReturnUtil getInternalServiceEntry(@PathVariable("serviceEntryName") String serviceEntryName,
                                             @RequestParam("namespace") String namespace,
                                            @RequestParam("clusterId") String clusterId)throws Exception {
        logger.info("获取服务入口详细信息,serviceEntryName:{}",serviceEntryName);
        return serviceEntryService.getServiceEntry(serviceEntryName, namespace, clusterId, CommonConstant.INTERNAL_SERVICE_ENTRY + "");
    }
}
