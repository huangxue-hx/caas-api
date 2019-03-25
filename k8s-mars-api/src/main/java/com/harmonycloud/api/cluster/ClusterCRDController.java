package com.harmonycloud.api.cluster;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.AddClusterDto;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.dto.cluster.ClusterStatusDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.ClusterCRDService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/clusters")
@Controller
public class ClusterCRDController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ClusterCRDService clusterCRDService;
    @Autowired
    private ClusterService clusterService;

    /**
     * 获取 clusterTPR 列表
     * @param dataCenter  clusterTPR 的 namespace
     * @return ActionReturnUtil
     * @throws Exception failed to list
     */
//    @ResponseBody
//    @RequestMapping(method = RequestMethod.GET)
//    public ActionReturnUtil listClusters(@RequestParam(value = "dataCenter", required = false) String dataCenter, @RequestParam(value = "template", required = false) String template) throws Exception {
//        logger.info("list clusters, dataCenter name:{}",dataCenter);
//        return clusterTprService.listClusters(template,dataCenter);
//    }

    /**
     * 根据name 获取clusterTPR对象
     * @param dataCenter namespace
     * @param clusterName  代表 clusterTPR
     * @return ActionReturnUtil
     * @throws Exception failed to get
     */
//    @ResponseBody
//    @RequestMapping(value = "/{clusterName}", method = RequestMethod.GET)
//    public ActionReturnUtil getCluster(@RequestParam(value = "dataCenter") String dataCenter,@PathVariable String clusterName) throws Exception {
//        logger.info("get  clusters, dataCenter name:{}, cluster name: {}",dataCenter, clusterName);
//        return clusterTprService.getCluster(dataCenter,clusterName);
//    }

    /**
     * 根据 clusterTPR name，删除该对象
     * @param dataCenter namespace
     * @param clusterId clusterTPR Name
     * @return ActionReturnUtil
     * @throws Exception failed to delete
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteCluster(@RequestParam(value = "dataCenter") String dataCenter,
                                          @PathVariable String clusterId,
                                          @RequestParam(value = "deleteData", required = false) Boolean deleteData) throws Exception {
        logger.info("delete  clusters, dataCenter name:{}, cluster id: {}",dataCenter, clusterId);
        return clusterCRDService.deleteCluster(dataCenter,clusterId,deleteData);
    }

    /**
     * 创建clusterTPR对象
     * @param clusterCRDDto  关于clusterTPR 数据
     * @return ActionReturnUtil
     * @throws Exception failed to  add
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public ActionReturnUtil addCluster(@RequestBody AddClusterDto clusterCRDDto) throws Exception {
        logger.info("add  clusters");
        return clusterCRDService.addCluster(clusterCRDDto);
    }

    /**
     * 更新clusterTPR 对象
     * @param clusterId 要修改的clusterTPR对象之前的name（若clusterTPR）
     * @param clusterCRDDto 修改后的clusterTPR数据 （若clusterTPR的那么未修改，则与clusterName值一致）
     * @return ActionReturnUtil
     * @throws Exception failed to update
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}", method = RequestMethod.PUT)
    public ActionReturnUtil updateCluster(@PathVariable String clusterId, @RequestBody ClusterCRDDto clusterCRDDto) throws Exception {
        logger.info("update  clusters");
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (null == cluster) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return clusterCRDService.updateCluster(cluster, clusterCRDDto);
    }

    @ResponseBody
    @RequestMapping(value = "/{clusterId}/status", method = RequestMethod.PUT)
    public ActionReturnUtil updateClusterStatus(@PathVariable String clusterId, @RequestBody ClusterStatusDto clusterStatusDto) throws Exception {
        logger.info("update cluster status");
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (null == cluster) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        if (cluster.getIsEnable() == clusterStatusDto.getStatus()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_STATUS_NOT_NEED_UPDATE);
        }
        return clusterCRDService.updateClusterStatus(cluster,clusterStatusDto.getStatus(),clusterStatusDto.getType());
    }
}
