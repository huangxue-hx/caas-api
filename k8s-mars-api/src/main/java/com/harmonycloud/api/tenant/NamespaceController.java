package com.harmonycloud.api.tenant;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Controller
@RequestMapping("/tenants/{tenantId}/namespaces")
public class NamespaceController {

    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private HttpSession session;
    @Autowired
    private IstioCommonService istioCommonService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 创建namespace
     * 
     * @param namespaceDto
     * @return
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil createNamespace(@PathVariable("tenantId") String tenantId,
                                            @ModelAttribute NamespaceDto namespaceDto) throws Exception {
        namespaceDto.setTenantId(tenantId);
//        logger.info("创建namespace:{}", JSONObject.toJSONString(namespaceDto));
        return namespaceService.createNamespace(namespaceDto);
    }

    /**
     * 编辑namespace下的resource quato
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public ActionReturnUtil updateNamespace(@PathVariable("tenantId") String tenantId,
                                            @ModelAttribute NamespaceDto namespaceDto) throws Exception {

        namespaceDto.setTenantId(tenantId);
//        logger.info("修改namespace:{}", JSONObject.toJSONString(namespaceDto));
        return namespaceService.updateNamespace(namespaceDto);
    }

    /**
     * 删除namespace
     *
     * @return
     */
    @RequestMapping(value = "/{namespaceName}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil deleteNamespace(@PathVariable("tenantId") String tenantId, @PathVariable("namespaceName") String namespaceName)
            throws Exception {
//        logger.info("删除namespace:{}",namespaceName);
        return namespaceService.deleteNamespace(tenantId, namespaceName);

    }

    /**
     * 查询namespace列表(包含namespace上部署的服务信息)
     *
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNamespaceWithDetail(@PathVariable("tenantId") String tenantId) throws Exception {
        return namespaceService.getNamespaceList(tenantId);
    }
    /**
     * 查询namespace基本信息列表
     *
     * @param
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listNamespace(@PathVariable("tenantId") String tenantId,
                                          @RequestParam(value="clusterId", required = false)String clusterId,
                                          @RequestParam(value="repositoryId", required = false) Integer repositoryId,
                                          @RequestParam(value="storage", required = false) Boolean isStorage) throws Exception {
        List<NamespaceLocal> namespaceList = new ArrayList<>();
        //查询镜像可以部署的分区列表
        if(repositoryId != null){
            return ActionReturnUtil.returnSuccessWithData(namespaceLocalService.getNamespaceListByRepositoryId(tenantId,repositoryId));
        }
        if(StringUtils.isBlank(clusterId)) {
            namespaceList = namespaceLocalService.getNamespaceListByTenantId(tenantId);
        }else{
            List<String> clusterIds = new ArrayList<>();
            if(clusterId.contains(CommonConstant.COMMA)){
                String[] clusterIdArr = clusterId.split(CommonConstant.COMMA);
                clusterIds.addAll(Arrays.asList(clusterIdArr));
            }else {
                clusterIds.add(clusterId);
            }
            namespaceList.addAll(namespaceLocalService.getNamespaceListByTenantIdAndClusterId(tenantId, clusterIds));
        }
        //存储页面的分区根据权限增加kube-system
        if(isStorage != null && isStorage){
            NamespaceLocal namespaceLocal = namespaceLocalService.getKubeSystemNamespace();
            if(namespaceLocal != null) {
                namespaceList.add(namespaceLocal);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(namespaceList);
    }

    /**
     * 查询namespace详情
     *
     * @return
     */
    @RequestMapping(value = "/{namespaceName}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespace(@PathVariable("namespaceName") String namespaceName)
            throws Exception {
        return namespaceService.getNamespaceDetail(namespaceName);
    }

    /**
     * 查询namespace配额
     *
     * @return
     */
    @RequestMapping(value = "/{namespaceName}/quota", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getNamespaceQuota(@PathVariable("namespaceName") String namespaceName)
            throws Exception {
        return ActionReturnUtil.returnSuccessWithData(namespaceService.getNamespaceQuota(namespaceName));

    }
    @RequestMapping(value = "/{namespaceName}/removeNodes/{nodeName:.+}", method = RequestMethod.DELETE)
    @ResponseBody
    public ActionReturnUtil removePrivateNamespaceNodes(@PathVariable("namespaceName") String namespaceName,
                                                        @PathVariable("tenantId") String tenantId,
                                                        @PathVariable("nodeName") String nodeName)
            throws Exception {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setTenantId(tenantId);
        namespaceDto.setName(namespaceName);
        namespaceDto.setNodeName(nodeName);
        namespaceService.removePrivateNamespaceNodes(namespaceDto);
        return ActionReturnUtil.returnSuccess();
    }
    @RequestMapping(value = "/{namespaceName}/addNodes/{nodeName:.+}", method = RequestMethod.POST)
    @ResponseBody
    public ActionReturnUtil addPrivateNamespaceNodes(@PathVariable("namespaceName") String namespaceName,
                                                        @PathVariable("tenantId") String tenantId,
                                                        @PathVariable("nodeName") String nodeName) throws Exception {
        NamespaceDto namespaceDto = new NamespaceDto();
        namespaceDto.setTenantId(tenantId);
        namespaceDto.setName(namespaceName);
        namespaceDto.setNodeName(nodeName);
        namespaceService.addPrivilegeNamespaceNodes(namespaceDto);
        return ActionReturnUtil.returnSuccess();
    }


    /**
     * 查询分区是否开启istio注入
     *
     * @param clusterId
     * @return
     * @throws Exception
     */
    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "查询分区istio注入开关状态", response = ActionReturnUtil.class, httpMethod = "", consumes = "", produces = "", notes = "返回值为true，分区开启Istio；值为，关闭分区Istio")
    @ApiImplicitParams({@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"), @ApiImplicitParam(name = "namespace", value = "分区名称", paramType = "path", dataType = "String")})
    @ResponseBody
    @RequestMapping(value = "/{namespace}/istiopolicyswitch", method = RequestMethod.GET)
    public ActionReturnUtil getNamespaceIstioPolicySwitch(@RequestParam(value = "clusterId", required = false) String clusterId, @PathVariable("namespace") String namespace)
            throws Exception {
        return istioCommonService.getNamespaceIstioPolicySwitch(namespace, clusterId);
    }

    /**
     * 开启或关闭分区istio自动注入
     * @param clusterId
     * @param status
     * @param namespaceName
     * @return
     * @throws Exception
     */
    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "现有分区istio服务开启关闭功能", response = ActionReturnUtil.class, httpMethod = "", consumes = "", produces = "", notes = "status值为true，分区开启Istio；值为false，关闭分区Istio")
    @ApiImplicitParams({@ApiImplicitParam(name = "clusterId", value = "集群id", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "开启或关闭操作（开启为true，关闭为false）", paramType = "query", dataType = "Boolean"),
            @ApiImplicitParam(name = "namespaceName", value = "分区名称", paramType = "path", dataType = "String")
    })
    @ResponseBody
    @RequestMapping(value = "/{namespaceName}/istiopolicyswitch", method = RequestMethod.PUT)
    public ActionReturnUtil updateNamespaceIstioPolicySwitch(@RequestParam("clusterId") String clusterId, @RequestParam("status") boolean status, @PathVariable("namespaceName") String namespaceName)
            throws Exception {
        return istioCommonService.updateNamespaceIstioPolicySwitch(status, clusterId, namespaceName);
    }

    @ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
    @ApiOperation(value = "Istio分区总览", response = ActionReturnUtil.class, httpMethod = "", consumes = "", produces = "", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ruleType", value = "策略类型", paramType = "query", dataType = "String"),
            @ApiImplicitParam(name = "namespace", value = "分区名称", paramType = "path", dataType = "String", required = true)
    })
    @ResponseBody
    @RequestMapping(value = "/{namespace}/istiopolicies", method = RequestMethod.GET)
    public ActionReturnUtil listIstioPolicies(@PathVariable("namespace") String namespace,
                                              @RequestParam(value = "ruleType", required = false) String ruleType,
                                              @RequestParam("clusterId") String clusterId)
            throws Exception {
        return istioCommonService.listIstioPolicies(null, namespace, ruleType, clusterId);
    }
}
