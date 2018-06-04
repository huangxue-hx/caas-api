package com.harmonycloud.api.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.platform.bean.PodDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.NodeLabel;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
@RequestMapping(value = "/clusters")
@Controller
public class NodeController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private PodService podService;
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    HttpSession session;

    @Autowired
    ClusterCacheManager clusterCacheManager;


    /**
     * node 列表
     * 
     * @return
     */
    @RequestMapping(value = "/{clusterId}/nodes")
    @ResponseBody
    public ActionReturnUtil listNode(@PathVariable(value = "clusterId" ) String clusterId) throws Exception {
        return nodeService.listNode(clusterId);

    }

    @RequestMapping(value = "/{clusterId}/nodes/{nodeName:.+}")
    public @ResponseBody ActionReturnUtil getNode(@PathVariable(value = "nodeName") String nodeIp,@PathVariable(value = "clusterId") String clusterId) throws Exception {
        Map node = nodeService.getNode(nodeIp,clusterId);
        return ActionReturnUtil.returnSuccessWithData(node);
    }


    /**
     * pod 列表
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/pod")
    public ActionReturnUtil listPods(@PathVariable(value = "nodeName") String nodeName,Cluster cluster) throws Exception {
        List<PodDto> podList = this.podService.PodList(nodeName, cluster);
        return ActionReturnUtil.returnSuccessWithData(podList);
    }

    /**
     * node事件
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/event")
    public ActionReturnUtil listNodeEvent(@PathVariable(value = "nodeName") String nodeName,Cluster cluster) throws Exception {
        return nodeService.listNodeEvent(nodeName, cluster);
    }
    /**
     * 获取node标签
     * 
     * @param nodeName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/label", method = RequestMethod.GET)
    public ActionReturnUtil getNodeLabels(@PathVariable(value = "nodeName") String nodeName,@PathVariable(value = "clusterId") String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        return nodeService.listNodeLabels(nodeName, cluster, CommonConstant.TRUE);
    }

    /**
     * 修改node标签
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/label", method = RequestMethod.PUT)
    public ActionReturnUtil updateNodeLabels(@PathVariable(value = "nodeName") String nodeName, @ModelAttribute NodeLabel labels,@PathVariable(value = "clusterId") String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        return this.nodeService.updateNodeLabels(nodeName, labels, cluster);
    }

    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/label/available", method = RequestMethod.GET)
    public ActionReturnUtil listNodeAvailablelabels(Cluster cluster) throws Exception {

        return nodeService.listNodeAvailablelabels( cluster);

    }
    /**
     * node 标签列表
     * 
     * @param nodeName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/label/status", method = RequestMethod.GET)
    public ActionReturnUtil listNodeStatusLabels(@PathVariable(value = "nodeName") String nodeName,Cluster cluster) throws Exception {
        Map<String, String> statusLabels = this.nodeService.listNodeStatusLabels(nodeName, cluster);
        return ActionReturnUtil.returnSuccessWithData(statusLabels);
    }
    /**
     * 初始化node状态标签
     * 
     * @param nodeName
     * @param labels
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/label/init", method = RequestMethod.PUT)
    public ActionReturnUtil addNodeLabels(@PathVariable(value = "nodeName") String nodeName, @RequestParam(value = "labels") String labels,@PathVariable(value = "clusterId") String clusterId) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        String[] split = labels.split("=");
        map.put(split[0], split[1]);
        return this.nodeService.addNodeLabels(nodeName, map,clusterId);
    }
    /**
     * 获取为闲置状态的node
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/available", method = RequestMethod.GET)
    public ActionReturnUtil getAvailableFreeNodeList(@PathVariable(value = "clusterId") String clusterId) throws Exception {
        Cluster cluster = clusterCacheManager.getCluster(clusterId);
        if(cluster == null){
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        List<String> nodeList = this.nodeService.getAvailableNodeList(cluster);
        return ActionReturnUtil.returnSuccessWithData(nodeList);
    }
    /**
     * 获取为共享状态的node
     * 
     * @return
     * @throws Exception
     */
//    @ResponseBody
//    @RequestMapping(value = "/node/availablelist/share", method = RequestMethod.GET)
//    public ActionReturnUtil getAvailableShareNodeList(Cluster cluster) throws Exception {
//        List<String> nodeList = this.nodeService.listShareNode(cluster);
//        return ActionReturnUtil.returnSuccessWithData(nodeList);
//    }
//    @ResponseBody
//    @RequestMapping(value = "/nodes/{nodeName}/label", method = RequestMethod.DELETE)
//    public ActionReturnUtil removenodelabel(@PathVariable(value = "nodeName") String nodeName, @RequestParam(value = "labels") String labels,Cluster cluster) throws Exception {
//        Map<String, String> labelsmap = new HashMap<String, String>();
//        labelsmap.put(labels, "true");
//        ActionReturnUtil removeNodeLabels = this.nodeService.removeNodeLabels(nodeName, labelsmap,cluster);
//        return removeNodeLabels;
//    }
//    @ResponseBody
//    @RequestMapping(value = "/node/privatenamespacelist", method = RequestMethod.GET)
//    public ActionReturnUtil getPrivateNamespaceNodeList(@RequestParam(value = "namespace") String namespace,Cluster cluster) throws Exception {
//        List<String> privateNamespaceNodeList = this.nodeService.getPrivateNamespaceNodeList(namespace, cluster);
//        return ActionReturnUtil.returnSuccessWithData(privateNamespaceNodeList);
//    }
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/addNode", method = RequestMethod.POST)
    public ActionReturnUtil addNode(@PathVariable(value = "nodeName") String host,
                                                                    @RequestParam(value = "user", required = true)String user,
                                                                    @RequestParam(value = "passwd", required = true)String passwd,
                                                                    @RequestParam(value = "masterIp", required = true)String masterIp,
                                                                    @RequestParam(value = "harborIp", required = false)String harborIp,
                                                                    @PathVariable(value = "clusterId") String clusterId) throws Exception {
        return this.nodeService.addNode(host,user,passwd,masterIp,clusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/status", method = RequestMethod.GET)
    public ActionReturnUtil checkNodeStatus(@PathVariable(value = "nodeName") String host,
                                                                    @RequestParam(value = "user", required = true)String user,
                                                                    @RequestParam(value = "passwd", required = true)String passwd) throws Exception {
        return this.nodeService.checkNodeStatus(host,user,passwd);
    }
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/removeNode", method = RequestMethod.DELETE)
    public ActionReturnUtil deleteNode(@PathVariable(value = "nodeName") String host,String user,String passwd,@PathVariable(value = "clusterId") String clusterId) throws Exception {
        return  this.nodeService.removeNode(host,user,passwd,clusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/online", method = RequestMethod.GET)
    public ActionReturnUtil getOnLineStatusWithClusterId(@PathVariable(value = "clusterId")String clusterId) throws Exception {
        List<NodeInstallProgress> onLineStatusWithClusterId = this.nodeService.getOnLineStatusWithClusterId(clusterId);
        return  ActionReturnUtil.returnSuccessWithData(onLineStatusWithClusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/errorstatus", method = RequestMethod.GET)
    public ActionReturnUtil getOnLineErrorStatus() throws Exception {
        String onLineErrorStatus = this.nodeService.getOnLineErrorStatus();
        return  ActionReturnUtil.returnSuccessWithData(onLineErrorStatus);
    }
    /**
     * 更新闲置节点状态
     * @param nodeName
     * @param clusterId
     * @param nodeType 1 共享，2负债均衡，3构建节点，4系统节点
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName:.+}", method = RequestMethod.PUT)
    public ActionReturnUtil updateIdleNodeStatus(@PathVariable(value = "nodeName") String nodeName,
                                                 @PathVariable(value = "clusterId") String clusterId,
                                                 Integer nodeType,
                                                 Boolean idleStatus) throws Exception {
        if (idleStatus){
            this.nodeService.updateIdleNodeStatus(nodeName,clusterId,nodeType);
        } else {
            this.nodeService.updateWorkNodeToIdleStatus(nodeName,clusterId);
        }
        return  ActionReturnUtil.returnSuccess();
    }

    /**
     * 取消节点上线程序
     * @param nodeName 节点名称
     * @param processId 节点上线任务id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/status", method = RequestMethod.PUT)
    public ActionReturnUtil cancelAddNode(@PathVariable(value = "nodeName") String nodeName,
                                           @RequestParam(value = "processId") Integer processId) throws Exception {
        ActionReturnUtil updateShareToNode = this.nodeService.cancelAddNode(processId);
        return  ActionReturnUtil.returnSuccessWithData(updateShareToNode);
    }
    
    /**
     * 集群内所有节点的label
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(value = "/nodes/labels")
    public ActionReturnUtil listNodeLabels(@RequestParam(value = "namespace", required = false) String namespace,
                                            @RequestParam(value = "clusterId", required = false) String clusterId) throws Exception {
        return nodeService.listNodeLabels(namespace, clusterId);
    }

    /**
     * 切换主机可维护状态
     * @param nodeName
     * @param schedulable
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/schedule", method = RequestMethod.PUT)
    public ActionReturnUtil setNodeSchedulable(@PathVariable(value = "clusterId") String clusterId,
                                                 @PathVariable(value = "nodeName") String nodeName,
                                                 @RequestParam(value = "schedulable") Boolean schedulable) throws Exception {
        return nodeService.switchNodeSchedulable(nodeName, schedulable, clusterId);
    }

    /**
     * 应用迁移（驱赶pod）
     * @param nodeName
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/drainPod", method = RequestMethod.PUT)
    public ActionReturnUtil drainPod(@PathVariable(value = "clusterId") String clusterId,
                                      @PathVariable(value = "nodeName") String nodeName) throws Exception {
        return nodeService.drainPod(nodeName, clusterId);
    }

    /**
     * 获取应用迁移进度
     * @param nodeName
     * @param clusterId
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/{clusterId}/nodes/{nodeName}/drainProgress", method = RequestMethod.GET)
    public ActionReturnUtil getDrainPodProgress(@PathVariable(value = "nodeName") String nodeName,
                                                @PathVariable(value = "clusterId") String clusterId) throws Exception {
        return nodeService.getDrainPodProgress(nodeName, clusterId);
    }
}
