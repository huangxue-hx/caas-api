package com.harmonycloud.api.cluster;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.impl.ClusterTemplateServiceImpl;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserService;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.harmonycloud.common.Constant.CommonConstant.COUNT;

/**
 * dashboard
 *
 * @author jmi
 */
@Api(description = "查询集群相关信息，以及集群缓存更新")
@RestController
@RequestMapping(value = "/clusters")
public class ClusterController {

    @Autowired
    ClusterService clusterService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    @Autowired
    TenantService tenantService;
    @Autowired
    UserService userService;

    @Autowired
    private com.harmonycloud.k8s.service.NodeService nodeService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ApiOperation(value = "获取集群列表", notes = "根据条件过滤查询集群列表")
    @ApiImplicitParams ({
            @ApiImplicitParam(name = "dataCenter", value = "数据中心", paramType = "query",dataType = "String"),
            @ApiImplicitParam(name = "includePlatformCluster", value = "是否包括上层集群", paramType = "query", dataType = "Boolean") })
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil listCluster(@RequestParam(value = "dataCenter", required = false) String dataCenter,
                                        @RequestParam(value = "includePlatformCluster", required = false) Boolean includePlatformCluster,
                                        @RequestParam(value = "includeDisable", required = false) Boolean includeDisable ,
                                        @RequestParam(value = "template", required = false) String template) throws Exception {
        //默认查询enable的集群，如果传了includeDisable=true则包含disable的集群
        Boolean isEnable = true;
        if(includeDisable != null && includeDisable){
            isEnable = null;
        }
        List<Cluster> clusters = clusterService.listCluster(dataCenter, isEnable, template);
        if(includePlatformCluster != null && includePlatformCluster){
            if(StringUtils.isBlank(dataCenter) || ClusterTemplateServiceImpl.DEFAULT_NAMESAPCE.equals(dataCenter)) {
                clusters.add(clusterService.getPlatformCluster());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(clusters);
    }

    @ApiOperation(value = "查询某个集群详情", notes = "查询某个集群的信息信息")
    @RequestMapping(value = "/{clusterId}", method = RequestMethod.GET)
    @ResponseBody
    public ActionReturnUtil getCluster(@PathVariable("clusterId") String clusterId) throws Exception {
        return ActionReturnUtil.returnSuccessWithData(clusterService.findClusterById(clusterId));
    }

    @ApiOperation(value = "获取集群数量", notes = "获取状态为开启的业务集群数量，不包括上层平台集群")
    @ResponseBody
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public ActionReturnUtil clusterCounter() throws Exception {
        return ActionReturnUtil.returnSuccessWithMap(COUNT, String.valueOf(clusterService.listCluster().size()));
    }

    /**
     * 当cluster信息更新时，由cluster go controller触发刷新cluster缓存信息
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/cache", method = RequestMethod.PUT)
    public ActionReturnUtil refreshClusterCache() throws Exception {
        Map<String, Cluster> clusters = clusterCacheManager.initClusterCache();
        if (CollectionUtils.isEmpty(clusters)) {
            logger.warn("刷新cluster缓存，cluster信息为空");
            return ActionReturnUtil.returnError();
        } else {
            logger.info("刷新cluster缓存成功，cluster size:{}", clusters.size());
            return ActionReturnUtil.returnSuccess();
        }
    }

    /**
     * cluster node 列表
     *
     * @return
     */
    @RequestMapping(value = "/clusterNodeSize")
    @ResponseBody
    public ActionReturnUtil clusterNodeSize() throws Exception {

        try {
            List<Cluster> listCluster = this.clusterService.listCluster();
            int nodeSize = 0;

            if (null != listCluster && listCluster.size() > 0) {
                for (Cluster cluster : listCluster) {
                    NodeList nodeList = nodeService.listNode(cluster);
                    if (null != nodeList && null != nodeList.getItems()) {
                        nodeSize += nodeList.getItems().size();
                    }
                }
            }
            return ActionReturnUtil.returnSuccessWithMap("clusterNodeSize", nodeSize + "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Failed to get cluster node size." + e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL);
        }
    }

    /**
     * cluster domain
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/domain", method = RequestMethod.GET)
    public ActionReturnUtil getClusterDomain(@RequestParam(value = "namespace") String namespace) throws Exception {
        logger.info("获取集群domain");
        return ActionReturnUtil.returnSuccessWithData(clusterService.findDomain(namespace));
    }

    /**
     * 获取F5 IP
     *
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/entry", method = RequestMethod.GET)
    public ActionReturnUtil getEntry(@RequestParam(value = "namespace") String namespace) throws Exception {
        logger.info("获取入口IP");
        return ActionReturnUtil.returnSuccessWithData(clusterService.getEntry(namespace));
    }

}
