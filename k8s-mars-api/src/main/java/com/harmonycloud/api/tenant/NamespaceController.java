package com.harmonycloud.api.tenant;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.tenant.NamespaceDto;
import com.harmonycloud.service.tenant.NamespaceService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/tenants/{tenantId}/namespaces")
public class NamespaceController {

    @Autowired
    NamespaceService namespaceService;
    @Autowired
    NamespaceLocalService namespaceLocalService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    HttpSession session;

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
        logger.info("创建namespace:{}", JSONObject.toJSONString(namespaceDto));
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
        logger.info("修改namespace:{}", JSONObject.toJSONString(namespaceDto));
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
        logger.info("删除namespace:{}",namespaceName);
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
    public ActionReturnUtil listNamespace(@PathVariable("tenantId") String tenantId, @RequestParam(value="clusterId", required = false)String clusterId) throws Exception {
        List<NamespaceLocal> namespaceList = new ArrayList<>();
        if(StringUtils.isBlank(clusterId)) {
            namespaceList = namespaceLocalService.getNamespaceListByTenantId(tenantId);
        }else{
            if(clusterId.contains(CommonConstant.COMMA)){
                String[] clusterIds = clusterId.split(CommonConstant.COMMA);
                for(String cluster : clusterIds) {
                    namespaceList.addAll(namespaceLocalService.getNamespaceListByTenantIdAndClusterId(tenantId, cluster));
                }
            }else {
                namespaceList = namespaceLocalService.getNamespaceListByTenantIdAndClusterId(tenantId, clusterId);
            }
        }
        //添加结果集群返回值
        if (!CollectionUtils.isEmpty(namespaceList)){
            for (NamespaceLocal namespaceLocal:namespaceList) {
                String currentClusterId = namespaceLocal.getClusterId();
                Cluster cluster = clusterService.findClusterById(currentClusterId);
                namespaceLocal.setClusterAliasName(cluster.getAliasName());
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
}
