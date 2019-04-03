package com.harmonycloud.api.cluster;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.istio.ServiceEntryDto;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.istio.IstioServiceEntryService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(description = "istio serviceentry external")
@RestController
@RequestMapping(value = "/clusters")
public class ClusterIstioController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private IstioCommonService istioCommonService;

    @Autowired
    private IstioServiceEntryService serviceEntryService;

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取全局服务开关状态", response = ActionReturnUtil.class, notes = "globalSwitchStatus值为true，表示全局配置为开启；值为false，全局配置为关闭")
    @ApiImplicitParams({@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "path", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/istiopolicyswitch", method = RequestMethod.GET)
    public ActionReturnUtil getClusterIstioPolicySwitch(@PathVariable("clusterId") String clusterId)
            throws Exception {
        return istioCommonService.getClusterIstioPolicySwitch(clusterId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "集群下开启或关闭Istio服务", response = ActionReturnUtil.class, httpMethod = "POST", notes = "status值为true，开启Istio全局服务；值为false，关闭Istio全局服务")
    @ApiImplicitParams({@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "开启或关闭操作（开启为true，关闭为false）", paramType = "query", dataType = "Boolean")
    })
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/istiopolicyswitch", method = RequestMethod.PUT)
    public ActionReturnUtil updateClusterIstioPolicySwitch(@RequestParam("status") boolean status, @PathVariable("clusterId") String clusterId)
            throws Exception {
        return istioCommonService.updateClusterIstioPolicySwitch(status, clusterId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取开启Istio功能的集群功能", response = ActionReturnUtil.class)
    @ResponseBody
    @RequestMapping(value = "/istiocluster", method = RequestMethod.GET)
    public ActionReturnUtil listIstioCluster() throws Exception {
        logger.info("查询开启Istio功能的集群");
        return istioCommonService.listIstioCluster();
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "创建外部服务入口功能", response = ActionReturnUtil.class)
    @ResponseBody
    @RequestMapping(value="/{clusterId}/externalserviceentries",method = RequestMethod.POST)
    public ActionReturnUtil createExternalServiceEntry(@PathVariable("clusterId") String clusterId,
                                                       @ModelAttribute ServiceEntryDto serviceEntryDto) throws Exception {
        logger.info("创建外部服务入口ServiceEntry,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.createExternalServiceEntry(serviceEntryDto);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "修改外部服务入口功能", response = ActionReturnUtil.class)
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/externalserviceentries", method = RequestMethod.PUT)
    public ActionReturnUtil updateExternalServiceEntry(@ModelAttribute ServiceEntryDto serviceEntryDto)throws Exception {
        logger.info("修改外部服务入口,param:{}", JSONObject.toJSONString(serviceEntryDto));
        return serviceEntryService.updateExternalServiceEntry(serviceEntryDto);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "删除服务入口功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "serviceEntryName", value = "服务入口名称", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/externalserviceentries/{serviceEntryName}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteExternalServiceEntry(@PathVariable("clusterId") String clusterId,
                                                       @PathVariable("serviceEntryName") String serviceEntryName) throws Exception {
        logger.info("删除服务入口,serviceName:{}", serviceEntryName);
        return serviceEntryService.deleteExternalServiceEntry(serviceEntryName, clusterId);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取外部服务入口列表功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "projectId", value = "项目id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/externalserviceentries", method = RequestMethod.GET)
    public ActionReturnUtil listServiceEntry(@RequestParam(value = "clusterId", required =false) String clusterId) throws Exception {
        logger.info("获取外部服务入口列表,clusterId:{}",clusterId);
        return serviceEntryService.listServiceEntry(null, clusterId, CommonConstant.EXTERNAL_SERVICE_ENTRY + "", CommonConstant.ISTIO_NAMESPACE, false);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "获取外部服务入口详细信息功能", response = ActionReturnUtil.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "serviceEntryName", value = "服务入口名称", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区名", paramType = "query", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/externalserviceentries/{serviceEntryName}", method = RequestMethod.GET)
    public ActionReturnUtil getServiceEntry(@PathVariable("clusterId") String clusterId,
                                            @PathVariable("serviceEntryName") String serviceEntryName)throws Exception {
        logger.info("获取外部服务入口详细信息,serviceEntryName:{}",serviceEntryName);
        return serviceEntryService.getServiceEntry(serviceEntryName, CommonConstant.ISTIO_NAMESPACE, clusterId, CommonConstant.EXTERNAL_SERVICE_ENTRY + "");
    }
}
