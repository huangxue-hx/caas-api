package com.harmonycloud.api.application;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DeployedServiceNamesDto;
import com.harmonycloud.dto.application.ServiceDeployDto;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.platform.constant.Constant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by anson on 18/8/7.
 */
@Api(description = "有状态服务增删改查启停等操作")
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/statefulsets")
@Controller
public class StatefulSetController {
    @Autowired
    HttpSession session;

    @Autowired
    StatefulSetsService statefulSetsService;

    @Autowired
    ServiceService serviceService;

    /**
     * 获取有状态服务列表
     * @param tenantId
     * @param name
     * @param namespace
     * @param labels
     * @param projectId
     * @param clusterId
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "查询有状态服务列表", notes = "根据条件过滤查询有状态服务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tenantId", value = "租户id", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "name", value = "服务名", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "labels", value = "标签", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "projectId", value = "项目id", paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public ActionReturnUtil listDeployments(@PathVariable(value = "tenantId") String tenantId,
                                            @RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "namespace", required = false) String namespace,
                                            @RequestParam(value = "labels", required = false) String labels,
                                            @PathVariable(value = "projectId") String projectId,
                                            @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(statefulSetsService.listStatefulSets(tenantId, name, namespace, labels, projectId, clusterId));

    }



    /**
     * 获取有状态服务详情
     *
     * @param name
     * @param namespace
     * @return
     */
    @ApiOperation(value = "获取有状态服务详情", notes = "根据分区、名称查询有状态服务详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}", method = RequestMethod.GET)
    public ActionReturnUtil statefulSetDetail(@PathVariable(value = "statefulSetName") String name,
                                             @RequestParam(value = "namespace", required = true) String namespace) throws Exception {

        return ActionReturnUtil.returnSuccessWithData(statefulSetsService.getStatefulSetDetail(namespace, name));

    }

    /**
     * 发布有状态服务
     * @param serviceDeploy
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "发布有状态服务", notes = "K8S对应分区中创建statefulSet及相关资源")
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil deployService(@ModelAttribute ServiceDeployDto serviceDeploy) throws Exception {
        String userName = (String) session.getAttribute(CommonConstant.USERNAME);
        serviceDeploy.getServiceTemplate().setServiceType(Constant.STATEFULSET);
        return serviceService.deployService(serviceDeploy, userName);
    }

    /**
     * 删除有状态服务
     * @return
     * @throws Exception
     */
    @ApiOperation(value = "删除有状态服务", notes = "K8S集群中删除statefulSet及相关资源")
    @ResponseBody
    @RequestMapping(method = RequestMethod.DELETE)
    public ActionReturnUtil deleteStatefulSet(@ModelAttribute DeployedServiceNamesDto deployedServiceNamesDto) throws Exception {
        String userName = (String) session.getAttribute(CommonConstant.USERNAME);
        if(CollectionUtils.isNotEmpty(deployedServiceNamesDto.getServiceList())){
            deployedServiceNamesDto.getServiceList().stream().forEach(serviceNameNamespace->serviceNameNamespace.setServiceType(Constant.STATEFULSET));
        }
        return serviceService.deleteDeployedService(deployedServiceNamesDto, userName);
    }

    @ApiOperation(value = "启动有状态服务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/start", method = RequestMethod.POST)
    public ActionReturnUtil startStatefulSet(@PathVariable(value = "statefulSetName") String name,
                                            @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        String userName = (String) session.getAttribute("username");
        statefulSetsService.startStatefulSet(name, namespace, userName);
        return ActionReturnUtil.returnSuccess();

    }

    @ApiOperation(value = "停止有状态服务", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/stop", method = RequestMethod.POST)
    public ActionReturnUtil stopStatefulSet(@PathVariable(value = "statefulSetName") String name,
                                           @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        String userName = (String) session.getAttribute("username");
        statefulSetsService.stopStatefulSet(name, namespace, userName);
        return ActionReturnUtil.returnSuccess();
    }

    @ApiOperation(value = "修改有状态服务实例数", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "scale", value = "实例数", paramType = "query", dataType = "Integer")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/scale", method = RequestMethod.POST)
    public ActionReturnUtil scaleStatefulSet(@PathVariable(value = "statefulSetName") String name,
                                            @RequestParam(value = "namespace", required = true) String namespace,
                                            @RequestParam(value = "scale") Integer scale) throws Exception {
        String userName = (String) session.getAttribute("username");
        statefulSetsService.scaleStatefulSet(namespace, name, scale, userName);
        return ActionReturnUtil.returnSuccess();
    }

    @ApiOperation(value = "获取有状态服务容器列表", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/containers", method = RequestMethod.GET)
    public ActionReturnUtil getStatefulSetContainer(@PathVariable(value = "statefulSetName") String name,
                                                   @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(statefulSetsService.statefulSetContainer(namespace, name));
    }

    @ApiOperation(value = "获取有状态服务事件", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/events", method = RequestMethod.GET)
    public ActionReturnUtil getAppEvents(@PathVariable(value = "statefulSetName") String name,
                                         @RequestParam(value = "namespace", required = true) String namespace) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(statefulSetsService.getStatefulSetEvents(namespace, name));
    }

    @ApiOperation(value = "获取有状态服务pod", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "statefulSetName", value = "服务名", paramType = "path",dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区", paramType = "query", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{statefulSetName}/pods", method = RequestMethod.GET)
    public ActionReturnUtil podList(@PathVariable(value = "statefulSetName") String name,
                                    @RequestParam(value = "namespace") String namespace) throws Exception {

        return ActionReturnUtil.returnSuccessWithData(statefulSetsService.podList(name, namespace));

    }


}
