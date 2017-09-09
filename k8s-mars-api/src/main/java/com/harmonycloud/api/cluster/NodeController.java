package com.harmonycloud.api.cluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.NodeLabel;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;

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


    /**
     * node 列表
     * 
     * @return
     */
    @RequestMapping(value = "/infrastructure/nodelist")
    @ResponseBody
    public ActionReturnUtil listNode(String clusterId) throws Exception {
        return nodeService.listNode(clusterId);
    }
    @RequestMapping(value = "/infrastructure/getNode")
    public @ResponseBody ActionReturnUtil getNode(String nodeIp,String clusterId) throws Exception {
        Map node = nodeService.getNode(nodeIp,clusterId);
        return ActionReturnUtil.returnSuccessWithData(node);
    }
    /**
     * node 明细
     * 
     * @param nodeName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/infrastructure/nodedetail")
    public ActionReturnUtil getNoodeDetail(String nodeName,Cluster cluster) throws Exception {
        return this.nodeService.getNodeDetail(nodeName,cluster);
    }

    /**
     * pod 列表
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/infrastructure/podList")
    public ActionReturnUtil listPods(@RequestParam(value = "nodeName") String nodeName,Cluster cluster) throws Exception {
        List<Object> podList = this.podService.PodList(nodeName, cluster);
        return ActionReturnUtil.returnSuccessWithData(podList);
    }

    /**
     * node事件
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/infrastructure/nodeevent")
    public ActionReturnUtil listNodeEvent(@RequestParam(value = "nodeName", required = false) String nodeName,Cluster cluster) throws Exception {
        return nodeService.listNodeEvent(nodeName, cluster);
    }
    /**
     * 获取node标签
     * 
     * @param nodeName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/node/labels", method = RequestMethod.GET)
    public ActionReturnUtil nodeLabels(@RequestParam(value = "nodeName") String nodeName,String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        return nodeService.listNodeLabels(nodeName, cluster);
    }

    /**
     * 修改node标签
     * 
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/node/labels", method = RequestMethod.PUT)
    public ActionReturnUtil updateNodeLabels(@RequestParam(value = "nodeName") String nodeName, @ModelAttribute NodeLabel labels,String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        return this.nodeService.updateNodeLabels(nodeName, labels, cluster);
    }

    @ResponseBody
    @RequestMapping(value = "/node/availablelabels", method = RequestMethod.GET)
    public ActionReturnUtil nodeAvailablelabels(Cluster cluster) throws Exception {

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
    @RequestMapping(value = "/node/liststatuslabels", method = RequestMethod.GET)
    public ActionReturnUtil listNodeStatusLabels(@RequestParam(value = "nodeName") String nodeName,Cluster cluster) throws Exception {
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
    @RequestMapping(value = "/node/initlabels", method = RequestMethod.PUT)
    public ActionReturnUtil addNodeLabels(@RequestParam(value = "nodeName") String nodeName, @RequestParam(value = "labels") String labels,String clusterId) throws Exception {
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
    @RequestMapping(value = "/node/availablelist", method = RequestMethod.GET)
    public ActionReturnUtil getAvailableFreeNodeList(Cluster cluster) throws Exception {
        List<String> nodeList = this.nodeService.getAvailableNodeList(cluster);
        return ActionReturnUtil.returnSuccessWithData(nodeList);
    }
    /**
     * 获取为共享状态的node
     * 
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/node/availablelist/share", method = RequestMethod.GET)
    public ActionReturnUtil getAvailableShareNodeList(Cluster cluster) throws Exception {
        List<String> nodeList = this.nodeService.listShareNode(cluster);
        return ActionReturnUtil.returnSuccessWithData(nodeList);
    }
    @ResponseBody
    @RequestMapping(value = "/node/removelabel", method = RequestMethod.PUT)
    public ActionReturnUtil removenodelabel(@RequestParam(value = "nodeName") String nodeName, @RequestParam(value = "labels") String labels,Cluster cluster) throws Exception {
        Map<String, String> labelsmap = new HashMap<String, String>();
        labelsmap.put(labels, "true");
        ActionReturnUtil removeNodeLabels = this.nodeService.removeNodeLabels(nodeName, labelsmap,cluster);
        return removeNodeLabels;
    }
    @ResponseBody
    @RequestMapping(value = "/node/privatenamespacelist", method = RequestMethod.GET)
    public ActionReturnUtil getPrivateNamespaceNodeList(@RequestParam(value = "namespace") String namespace,Cluster cluster) throws Exception {
        List<String> privateNamespaceNodeList = this.nodeService.getPrivateNamespaceNodeList(namespace, cluster);
        return ActionReturnUtil.returnSuccessWithData(privateNamespaceNodeList);
    }
    @ResponseBody
    @RequestMapping(value = "/node/addNode", method = RequestMethod.POST)
    public ActionReturnUtil addNode(@RequestParam(value = "host", required = true) String host,
                                                                    @RequestParam(value = "user", required = true)String user,
                                                                    @RequestParam(value = "passwd", required = true)String passwd,
                                                                    @RequestParam(value = "masterIp", required = true)String masterIp,
                                                                    @RequestParam(value = "harborIp", required = true)String harborIp,
                                                                    @RequestParam(value = "clusterId", required = true) String clusterId) throws Exception {
        
        return this.nodeService.addNode(host,user,passwd,masterIp,harborIp,clusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/node/checkNodeStatus", method = RequestMethod.GET)
    public ActionReturnUtil checkNodeStatus(@RequestParam(value = "host", required = true) String host,
                                                                    @RequestParam(value = "user", required = true)String user,
                                                                    @RequestParam(value = "passwd", required = true)String passwd) throws Exception {
        return this.nodeService.checkNodeStatus(host,user,passwd);
    }
    @ResponseBody
    @RequestMapping(value = "/node/removeNode", method = RequestMethod.DELETE)
    public ActionReturnUtil removeNode(@RequestParam(value = "host") String host,String user,String passwd,String clusterId) throws Exception {
        return  this.nodeService.removeNode(host,user,passwd,clusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/node/getOnLineStatus", method = RequestMethod.GET)
    public ActionReturnUtil getOnLineStatusWithClusterId(@RequestParam(value = "clusterId")String clusterId) throws Exception {
        List<NodeInstallProgress> onLineStatusWithClusterId = this.nodeService.getOnLineStatusWithClusterId(clusterId);
        return  ActionReturnUtil.returnSuccessWithData(onLineStatusWithClusterId);
    }
    @ResponseBody
    @RequestMapping(value = "/node/getOnLineErrorStatus", method = RequestMethod.GET)
    public ActionReturnUtil getOnLineErrorStatus() throws Exception {
        String onLineErrorStatus = this.nodeService.getOnLineErrorStatus();
        return  ActionReturnUtil.returnSuccessWithData(onLineErrorStatus);
    }
    @ResponseBody
    @RequestMapping(value = "/node/updateShareToNode", method = RequestMethod.PUT)
    public ActionReturnUtil updateShareToNode(String nodeName,String clusterId) throws Exception {
        ActionReturnUtil updateShareToNode = this.nodeService.updateShareToNode(nodeName,clusterId);
        return  ActionReturnUtil.returnSuccessWithData(updateShareToNode);
    }
    @ResponseBody
    @RequestMapping(value = "/node/cancelAddNode", method = RequestMethod.PUT)
    public ActionReturnUtil cancelAddNode(Integer id) throws Exception {
        ActionReturnUtil updateShareToNode = this.nodeService.cancelAddNode(id);
        return  ActionReturnUtil.returnSuccessWithData(updateShareToNode);
    }
    
    /**
     * node 所有的label
     * 
     * @return
     */
    @RequestMapping(value = "/infrastructure/nodelist/labels")
    @ResponseBody
    public ActionReturnUtil listNodeLabels() throws Exception {
        Cluster cluster = (Cluster) session.getAttribute("currentCluster");
        if(cluster == null){
            throw new K8sAuthException(Constant.HTTP_401);
        }
        return nodeService.listNodeLabels(cluster);
    }
}
