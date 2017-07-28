package com.harmonycloud.service.platform.serviceImpl.infrastructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.ESFactory;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JSchClient;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.cluster.bean.NodeInstallProgress;
import com.harmonycloud.k8s.bean.ContainerImage;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.bean.Node;
import com.harmonycloud.k8s.bean.NodeAddress;
import com.harmonycloud.k8s.bean.NodeCondition;
import com.harmonycloud.k8s.bean.NodeList;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.NodeInstallProgressService;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.NodeDetailDto;
import com.harmonycloud.service.platform.bean.NodeDetailDto.Status;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.bean.NodeLabel;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.tenant.TenantService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private com.harmonycloud.k8s.service.NodeService nodeService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    TenantService tenantService;
    @Autowired
    InfluxdbService influxdbService;
    @Autowired
    PodService podService;
    @Autowired
    NodeInstallProgressService nodeInstallProgressService;

    @Value("#{propertiesReader['image.domain']}")
    private String harborUrl;
    /**
     * Node列表
     */
    @Override
    public ActionReturnUtil listNode(String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        NodeList nodeList = nodeService.listNode(cluster);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        if (nodeList != null && nodeList.getItems().size() > 0) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                NodeDto nodeDto = new NodeDto();
                nodeDto.setIp(node.getStatus().getAddresses().get(0).getAddress());
                nodeDto.setName(node.getMetadata().getName());
                nodeDto.setTime(node.getMetadata().getCreationTimestamp());
                // if
                // (node.getMetadata().getLabels().get(CommonConstant.CLUSTORROLE)
                // != null) {
                // nodeDto.setType("MasterNode");
                // } else {
                // nodeDto.setType("DataNode");
                // }
                Map<String, Object> labels = node.getMetadata().getLabels();
                if (labels.get(CommonConstant.MASTERNODELABEL) != null) {
                    nodeDto.setType(CommonConstant.MASTERNODE);
                    nodeDto.setNodeShareStatus("主控");
                } else {
                    nodeDto.setType(CommonConstant.DATANODE);
                }
                if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null && labels.get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_B)) {
                    nodeDto.setNodeShareStatus("闲置");
                } else if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null && labels.get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_C)) {
                    nodeDto.setNodeShareStatus("共享");
                } else if (labels.get(CommonConstant.HARMONYCLOUD_STATUS) != null && labels.get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_D)) {
                    nodeDto.setNodeShareStatus("独占");
                } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                        && labels.get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_A)) {
                    nodeDto.setNodeShareStatus("系统");
                }
                List<NodeCondition> conditions = node.getStatus().getConditions();
                for (NodeCondition nodeCondition : conditions) {
                    if (nodeCondition.getType().equals("Ready")) {
                        nodeDto.setStatus(nodeCondition.getStatus());
                        break;
                    }
                }

                NodeDto dto = this.getHostUsege(node, nodeDto, cluster);
                List<Object> list = new ArrayList<Object>();
                // 显示主机标签
                if (labels != null) {
                    Set<Entry<String, Object>> entrySet = labels.entrySet();
                    for (Entry<String, Object> entry : entrySet) {
                        if (entry.getKey().contains("harmonycloud.cn")) {
                            String key = entry.getKey();
                            key = key.replaceAll("harmonycloud.cn/", "");
                            if (list.size() > 0) {
                                list.add("," + key + "=" + entry.getValue());
                            } else {
                                list.add(key + "=" + entry.getValue());
                            }
                        }
                    }
                }
                dto.setCustomLabels(list);
                nodeDtoList.add(dto);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(nodeDtoList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil getNodeDetail(String nodeName, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        // 处理为页面需要的值
        NodeDetailDto nodeDetailDto = new NodeDetailDto();
        if (node != null) {
            // 设置address
            List<NodeAddress> addresses = node.getStatus().getAddresses();
            nodeDetailDto.setAddresses(addresses);
            // 设置images
            List<ContainerImage> images = node.getStatus().getImages();
            nodeDetailDto.setImages(images);
            // 设置status
            List<NodeCondition> conditions = node.getStatus().getConditions();
            for (NodeCondition nodeCondition : conditions) {
                if (nodeCondition.getType().equals("Ready")) {
                    // NodeDetailDto.Status status = nodeDetailDto.new Status();
                    // status.setLastHeartbeatTime(nodeCondition.getLastHeartbeatTime());
                    // status.setLastTransitionTime(nodeCondition.getLastTransitionTime());
                    // status.setMessage(nodeCondition.getMessage());
                    // status.setName(nodeCondition.getType());
                    // status.setReason(nodeCondition.getReason());
                    nodeDetailDto.setStatus(nodeCondition.getStatus());
                    break;
                }
            }
            nodeDetailDto.setArchitecture(node.getStatus().getNodeInfo().getArchitecture());
            nodeDetailDto.setContainerRuntimeVersion(node.getStatus().getNodeInfo().getContainerRuntimeVersion());

            Map<String, Object> capacity = (Map<String, Object>) node.getStatus().getCapacity();
            nodeDetailDto.setCpu(capacity.get("cpu").toString());
            nodeDetailDto.setCreationTime(node.getMetadata().getCreationTimestamp());

            Map<String, Object> allocatable = (Map<String, Object>) node.getStatus().getAllocatable();
            if (allocatable.get("alpha.kubernetes.io/nvidia-gpu") != null) {
                nodeDetailDto.setGpu(allocatable.get("alpha.kubernetes.io/nvidia-gpu").toString());
            }
            if (allocatable.get("gpu") != null) {
                nodeDetailDto.setGpu(allocatable.get("gpu").toString());
            }
            nodeDetailDto.setKernelVersion(node.getStatus().getNodeInfo().getKernelVersion());
            nodeDetailDto.setKubeProxyVersion(node.getStatus().getNodeInfo().getKubeProxyVersion());
            nodeDetailDto.setKubeletVersion(node.getStatus().getNodeInfo().getKubeletVersion());
            nodeDetailDto.setMemory(capacity.get("memory").toString());
            nodeDetailDto.setName(node.getMetadata().getName());
            nodeDetailDto.setOs(node.getStatus().getNodeInfo().getOperatingSystem());
            nodeDetailDto.setPods(capacity.get("pods").toString());
            Map<String, Object> labels = node.getMetadata().getLabels();
            if (labels.get("master") != null && labels.get("master").equals("master")) {
                nodeDetailDto.setType("master");
            } else {
                nodeDetailDto.setType("slave");
            }

        }
        return ActionReturnUtil.returnSuccessWithData(nodeDetailDto);
    }

    /**
     * 获取node事件
     * 
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listNodeEvent(String nodeName, Cluster cluster) throws Exception {
        Map<String, Object> parmas = new HashMap<String, Object>();
        List<Event> events = new ArrayList<Event>();
        if (!StringUtils.isEmpty(nodeName)) {
            Node node = this.nodeService.getNode(nodeName, cluster);

            // 获取事件
            parmas.put("fieldSelector", "involvedObject.uid=" + node.getMetadata().getUid());
            EventList nodeEvent = nodeService.listNodeEvent(parmas);
            events = nodeEvent.getItems();
        } else {
            parmas.put("fieldSelector", "involvedObject.kind=Node");
            EventList nodeEvent = nodeService.listNodeEvent(parmas);
            events = nodeEvent.getItems();
        }
        if (events != null && events.size() > 0) {
            return ActionReturnUtil.returnSuccessWithData(convertRes(events));
        }

        return ActionReturnUtil.returnSuccessWithData(events);
    }

    /**
     * 获取node标签
     */
    @Override
    public ActionReturnUtil listNodeLabels(String nodeName, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        Map<String, Object> labels = node.getMetadata().getLabels();
        List<Object> list = new ArrayList<>();
        if (labels != null) {
            Set<Entry<String, Object>> entrySet = labels.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                if (entry.getKey().contains("harmonycloud.cn")) {
                    String key = entry.getKey();
                    key = key.replaceAll("harmonycloud.cn/", "");
                    list.add(key + "=" + entry.getValue());
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }
    /**
     * 获取node的状态标签
     */
    @Override
    public Map<String, String> listNodeStatusLabels(String nodeName, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node == null) {
            return null;
        }
        Map<String, Object> labels = node.getMetadata().getLabels();
        Map<String, String> returnLabels = new HashMap<>();
        if (labels != null) {
            Set<Entry<String, Object>> entrySet = labels.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                if (!StringUtils.isEmpty(entry.getValue().toString())) {
                    String key = entry.getKey();
                    returnLabels.put(key, entry.getValue().toString());
                }
            }
        }
        return returnLabels;
    }
    private List<EventDetail> convertRes(List<Event> events) throws Exception {
        List<EventDetail> result = new ArrayList<EventDetail>();
        for (Event event : events) {
            EventDetail tmp = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(), event.getLastTimestamp(), event.getCount(), event.getType());
            result.add(tmp);
        }
        return result;
    }

    /**
     * 更新node-labels
     */
    @Override
    public ActionReturnUtil updateNodeLabels(String nodeName, NodeLabel labels, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        Map<String, String> updateLabels = labels.getLabels();
        if (node != null) {
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Iterator<Entry<String, Object>> it = oldLabels.entrySet().iterator();
            List<String> rm = new ArrayList<>();
            if (updateLabels != null && oldLabels != null) {
                // 循环遍历原来标签，如果新标签没有则删除，如果有则更新用户自定义的标签新值
                // 更新labels
                while (it.hasNext()) {
                    Entry<String, Object> itEntry = it.next();
                    String itKey = itEntry.getKey();
                    String[] split = null;
                    if (itKey != null) {
                        split = itKey.split("harmonycloud.cn/");
                    }
                    if (itKey.contains("harmonycloud.cn/") && updateLabels.get(split[1]) == null) {
                        it.remove();
                    } else if (itKey.contains("harmonycloud.cn/") && updateLabels.get(split[1]) != null) {
                        itEntry.setValue(updateLabels.get(split[1]));
                        rm.add(split[1]);
                    }
                }
            } else if (updateLabels == null && oldLabels != null) {
                // 循环遍历原来标签，如果新标签没有值，全部删除用户自定义的标签
                while (it.hasNext()) {
                    Entry<String, Object> itEntry = it.next();
                    String itKey = itEntry.getKey();
                    if (itKey.contains("harmonycloud.cn/")) {
                        it.remove();
                    }
                }
            }
            // 添加新的用户自定义的标签
            if (rm != null && rm.size() > 0) {
                for (String string : rm) {
                    updateLabels.remove(string);
                }
            }
            if (updateLabels != null && !updateLabels.isEmpty()) {
                Iterator<Entry<String, String>> itup = updateLabels.entrySet().iterator();
                while (itup.hasNext()) {
                    Entry<String, String> itEntry = itup.next();
                    String itKey = itEntry.getKey();
                    Object itValue = itEntry.getValue();
                    oldLabels.put("harmonycloud.cn/" + itKey, itValue);
                }
            }
            ObjectMeta metadata = node.getMetadata();
            metadata.setLabels(oldLabels);
            // 更新K8s
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("kind", node.getKind());
            bodys.put("apiVersion", node.getApiVersion());
            bodys.put("spec", node.getSpec());
            bodys.put("status", node.getStatus());
            bodys.put("metadata", metadata);

            K8SClientResponse updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
            if (HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
                return ActionReturnUtil.returnSuccess();
            }
        }
        return ActionReturnUtil.returnError();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil listNodeAvailablelabels(Cluster cluster) throws Exception {
        NodeList nodeList = nodeService.listNode();
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        if (nodeList != null) {
            List<Node> nodes = nodeList.getItems();
            Map<String, Object> tmp = new HashMap<>();
            for (Node node : nodes) {
                Map<String, Object> labels = node.getMetadata().getLabels();
                for (Map.Entry<String, Object> m : labels.entrySet()) {
                    if (m.getKey().indexOf("harmonycloud.cn") > -1) {
                        String key = m.getKey().split("/")[1];
                        Map<String, Object> tMap = new HashMap<>();
                        tMap.put(key, true);
                        tmp.put(m.getValue().toString(), tMap);
                    }
                }
            }
            for (Map.Entry<String, Object> m : tmp.entrySet()) {
                Map<String, Object> labelItem = new HashMap<String, Object>();
                labelItem.put("name", m.getKey());
                List<String> values = new ArrayList<String>();
                for (Map.Entry<String, Object> m2 : ((Map<String, Object>) m.getValue()).entrySet()) {
                    values.add(m2.getKey());
                }
                labelItem.put("values", values);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(res);

    }

    @Override
    public ActionReturnUtil addNodeLabels(String nodeName, Map<String, String> newLabels, String clusterId) throws Exception {
        Cluster cluster = null;
        if (!StringUtils.isEmpty(clusterId)) {
            cluster = this.clusterService.findClusterById(clusterId);
        }
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node != null) {
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Set<Entry<String, String>> entrySet = newLabels.entrySet();
            // 更新labels
            for (Entry<String, String> label : entrySet) {
                // label
                oldLabels.put(label.getKey(), label.getValue());
            }
            ObjectMeta metadata = node.getMetadata();
            metadata.setLabels(oldLabels);
            // 更新K8s
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("kind", node.getKind());
            bodys.put("apiVersion", node.getApiVersion());
            bodys.put("spec", node.getSpec());
            bodys.put("status", node.getStatus());
            bodys.put("metadata", metadata);

            K8SClientResponse updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
            if (HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
                return ActionReturnUtil.returnSuccess();
            }
            return ActionReturnUtil.returnErrorWithMsg("node：" + nodeName + "更新label出错,错误信息：" + updateNode.getBody());
        }
        return ActionReturnUtil.returnErrorWithMsg("node：" + nodeName + "不存在,请检查");
    }

    @Override
    public ActionReturnUtil removeNodeLabels(String nodeName, Map<String, String> labels, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node != null) {
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Set<Entry<String, String>> entrySetRemove = labels.entrySet();
            // 更新labels
            for (Entry<String, String> labelReomve : entrySetRemove) {
                // 删除label
                if (oldLabels.get(labelReomve.getKey()) != null) {
                    oldLabels.remove(labelReomve.getKey());
                }
            }
            ObjectMeta metadata = node.getMetadata();
            metadata.setLabels(oldLabels);
            // 更新K8s
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("kind", node.getKind());
            bodys.put("apiVersion", node.getApiVersion());
            bodys.put("spec", node.getSpec());
            bodys.put("status", node.getStatus());
            bodys.put("metadata", metadata);

            K8SClientResponse updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
            if (HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
                return ActionReturnUtil.returnSuccess();
            }
            return ActionReturnUtil.returnErrorWithMsg("node：" + nodeName + "更新label出错,错误信息：" + updateNode.getBody());
        }
        return ActionReturnUtil.returnErrorWithMsg("node：" + nodeName + "不存在,请检查");
    }

    @Override
    public List<String> getPrivateNamespaceNodeList(String namespace, Cluster cluster) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_D);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys);
        List<String> nodeDtoList = new ArrayList<>();
        if (nodeList != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                Map<String, Object> labels = node.getMetadata().getLabels();
                Set<Entry<String, Object>> entrySet = labels.entrySet();
                for (Entry<String, Object> entry : entrySet) {
                    if (entry.getValue().toString().contains(namespace)) {
                        List<NodeCondition> conditions = node.getStatus().getConditions();
                        if (conditions.size() == 4 && "True".equals(conditions.get(3).getStatus())) {
                            nodeDtoList.add(node.getMetadata().getName());
                        }
                        break;
                    }
                }
            }
        }
        return nodeDtoList;
    }

    @Override
    public List<NodeDto> listAllPrivateNode(Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_D);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        if (nodeList != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                NodeDto nodeDto = new NodeDto();
                nodeDto.setIp(node.getStatus().getAddresses().get(0).getAddress());
                nodeDto.setName(node.getMetadata().getName());
                nodeDto.setTime(node.getMetadata().getCreationTimestamp());
                if (node.getMetadata().getLabels().get("master") == null) {
                    nodeDto.setType("master");
                } else {
                    nodeDto.setType("slave");
                }
                List<NodeCondition> conditions = node.getStatus().getConditions();
                for (NodeCondition nodeCondition : conditions) {
                    if (nodeCondition.getType().equals("Ready")) {
                        nodeDto.setStatus(nodeCondition.getStatus());
                        break;
                    }
                }
                nodeDtoList.add(nodeDto);
            }
        }
        return nodeDtoList;
    }

    @Override
    public List<NodeDto> listPrivateNodeByTenant(String tenantName, Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_D);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        if (nodeList != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                NodeDto nodeDto = new NodeDto();
                nodeDto.setIp(node.getStatus().getAddresses().get(0).getAddress());
                nodeDto.setName(node.getMetadata().getName());
                nodeDto.setTime(node.getMetadata().getCreationTimestamp());
                if (node.getMetadata().getLabels().get("master") == null) {
                    nodeDto.setType("master");
                } else {
                    nodeDto.setType("slave");
                }
                List<NodeCondition> conditions = node.getStatus().getConditions();
                for (NodeCondition nodeCondition : conditions) {
                    if (nodeCondition.getType().equals("Ready")) {
                        nodeDto.setStatus(nodeCondition.getStatus());
                        break;
                    }
                }
                nodeDtoList.add(nodeDto);
            }
        }
        return nodeDtoList;
    }

    @Override
    public List<String> listShareNode(Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_C);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys);
        List<String> nodeDtoList = new ArrayList<>();
        if (nodeList != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                List<NodeCondition> conditions = node.getStatus().getConditions();
                if (conditions.size() == 4 && "True".equals(conditions.get(3).getStatus())) {
                    nodeDtoList.add(node.getMetadata().getName());
                }

            }
        }
        return nodeDtoList;
    }

    @Override
    public List<String> getAvailableNodeList(Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_B);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys);
        List<String> nodeDtoList = new ArrayList<>();
        if (nodeList != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                List<NodeCondition> conditions = node.getStatus().getConditions();
                if (conditions.size() == 4 && "True".equals(conditions.get(3).getStatus())) {
                    nodeDtoList.add(node.getMetadata().getName());
                }

            }
        }
        return nodeDtoList;
    }
    @Override
    public ActionReturnUtil checkNodeStatus(String host, String user, String passwd) throws Exception {
        JSch jsch = JSchClient.createJSch();
        Session session = jsch.getSession(user, host, 22);
        // 如果服务器连接不上，则抛出异常
        if (session == null) {
            // throw new Exception("session is null");
            throw new MarsRuntimeException("主机" + host + "服务器连接异常,请检查服务器是否可用");
        }
        // 设置登陆主机的密码
        session.setPassword(passwd);
        // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        // 设置登陆超时时间
        try {
            session.connect(8000);
        } catch (Exception e) {
            throw new MarsRuntimeException("主机" + host + "连接失败,请检查主机地址,用户名,密码是否输入正确!");
        }
        return ActionReturnUtil.returnSuccess();
    }
    @Override
    public ActionReturnUtil addNode(String host, String user, String passwd, String masterIp, String harborIp, String clusterId) throws Exception {
        // HttpClientUtil.httpPostRequest(url, headers, null);
        NodeInstallProgress nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
        if (nodeInstall == null) {
            nodeInstall = new NodeInstallProgress();
            nodeInstall.setInstallStatus(CommonConstant.BEGIN);
            nodeInstall.setName(host);
            nodeInstall.setProgress(0);
            nodeInstall.setClusterId(Integer.parseInt(clusterId));
            nodeInstallProgressService.addNodeInLineInfo(nodeInstall);
        } else {
            nodeInstall.setProgress(0);
            nodeInstall.setInstallStatus(CommonConstant.BEGIN);
            nodeInstall.setClusterId(Integer.parseInt(clusterId));
            nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("host", host);
        params.put("user", user);
        params.put("passwd", passwd);
        params.put("masterIp", masterIp);
        params.put("harborIp", harborUrl);
        if (StringUtils.isEmpty(host) || StringUtils.isEmpty(user) || StringUtils.isEmpty(passwd) || StringUtils.isEmpty(masterIp) || StringUtils.isEmpty(harborIp)
                || StringUtils.isEmpty(clusterId)) {
            return ActionReturnUtil.returnErrorWithMsg("参数不能为空");
        }
        // ActionReturnUtil httpGetRequest =
        // HttpClientUtil.httpGetRequest("http://10.10.103.132:9999/installnode",
        // null, params);
        // Map<String, String> newLabels = new HashMap<>();
        // if((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) == true){
        // Cluster cluster = null;
        // cluster = this.clusterService.findClusterById(clusterId);
        // Node node = this.nodeService.getNode(host, cluster);
        // while(node==null){
        // Thread.sleep(5000);
        // node = this.nodeService.getNode(host, cluster);
        // }
        // nodeInstall =
        // nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
        // nodeInstall.setInstallStatus(CommonConstant.DONE);
        // nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
        // newLabels.put(CommonConstant.HARMONYCLOUD_STATUS,
        // CommonConstant.LABEL_STATUS_B);
        // ActionReturnUtil addNodeLabels = this.addNodeLabels(host, newLabels,
        // clusterId);
        // if ( (Boolean) addNodeLabels.get(CommonConstant.SUCCESS) == false) {
        // return addNodeLabels;
        // }
        // }
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                ActionReturnUtil flag = new ActionReturnUtil();
                try {
                    Cluster cluster = null;
                    cluster = clusterService.findClusterById(clusterId);
                    ActionReturnUtil httpGetRequest = HttpClientUtil.httpGetRequest("http://" + cluster.getHost() + ":9999/installnode", null, params);
                    if ((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) != CommonConstant.FALSE) {
                        Object object = httpGetRequest.get(CommonConstant.DATA);
                        String ad = object == null ? "" : object.toString();
                        if (!ad.contains("1000")) {
                            NodeInstallProgress nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
                            nodeInstall.setInstallStatus(ad);
                            nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
                        }
                        Map<String, String> newLabels = new HashMap<>();
                        if ((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) == true) {
                            Node node = nodeService.getNode(host, cluster);
                            while (node == null) {
                                Thread.sleep(5000);
                                node = nodeService.getNode(host, cluster);
                            }
                            NodeInstallProgress nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
                            nodeInstall.setInstallStatus(CommonConstant.DONE);
                            nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
                            newLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_B);
                            ActionReturnUtil addNodeLabels = addNodeLabels(host, newLabels, clusterId);
                        }
                    }

                } catch (Exception e) {
                    NodeInstallProgress nodeInstall;
                    try {
                        nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
                        nodeInstall.setErrorMsg(e.getMessage());
                        nodeInstall.setInstallStatus("error");
                        nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        };
        ESFactory.executor.execute(worker);

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil removeNode(String host, String user, String passwd, String clusterId) throws Exception {
        Cluster cluster = this.clusterService.findClusterById(clusterId);
        Map<String, Object> params = new HashMap<>();
        params.put("host", host);
        params.put("user", user);
        params.put("passwd", passwd);
        params.put("masterIp", cluster.getHost());
        params.put("token", cluster.getMachineToken());
        ActionReturnUtil httpGetRequest = HttpClientUtil.httpGetRequest("http://" + cluster.getHost() + ":9999/uninstallnode", null, params);
        if ((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            return httpGetRequest;
        } else {
            Object object = httpGetRequest.get(CommonConstant.DATA);
            String ad = object == null ? "" : object.toString();
            if (ad.contains("1000")) {
                return httpGetRequest;
            } else {
                return ActionReturnUtil.returnErrorWithMsg(ad);
            }
        }
    }

    @Override
    public Map getNode(String nodeIp, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        NodeList nodeList = nodeService.listNode(cluster);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        List<String> otherNodeList = new ArrayList<>();
        NodeDetailDto nodeDetailDto = new NodeDetailDto();
        if (nodeList != null && nodeList.getItems().size() > 0) {
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                if (nodeIp.equals(node.getMetadata().getName())) {
                    // 处理为页面需要的值
                    if (node != null) {
                        if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_B)) {
                            nodeDetailDto.setNodeShareStatus("闲置");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_C)) {
                            nodeDetailDto.setNodeShareStatus("共享");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_D)) {
                            nodeDetailDto.setNodeShareStatus("独占");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_A)) {
                            nodeDetailDto.setNodeShareStatus("系统");
                        }
                        NodeDto nodedto = new NodeDto();
                        NodeDto hostUsege = this.getHostUsege(node, nodedto, cluster);
                        nodeDetailDto.setMemory(hostUsege.getMemory());
                        nodeDetailDto.setCpu(hostUsege.getCpu());
                        nodeDetailDto.setDisk(hostUsege.getDisk());
                        // 设置address
                        List<NodeAddress> addresses = node.getStatus().getAddresses();
                        nodeDetailDto.setAddresses(addresses);
                        // 设置images
                        List<ContainerImage> images = node.getStatus().getImages();
                        nodeDetailDto.setImages(images);
                        // 设置status
                        List<NodeCondition> conditions = node.getStatus().getConditions();
                        for (NodeCondition nodeCondition : conditions) {
                            if (nodeCondition.getType().equals("Ready")) {
                                nodeDetailDto.setStatus(nodeCondition.getStatus());
                                break;
                            }
                        }
                        nodeDetailDto.setArchitecture(node.getStatus().getNodeInfo().getArchitecture());
                        nodeDetailDto.setContainerRuntimeVersion(node.getStatus().getNodeInfo().getContainerRuntimeVersion());
                        Map<String, Object> capacity = (Map<String, Object>) node.getStatus().getCapacity();
                        nodeDetailDto.setCreationTime(node.getMetadata().getCreationTimestamp());

                        Map<String, Object> allocatable = (Map<String, Object>) node.getStatus().getAllocatable();
                        if (allocatable.get("alpha.kubernetes.io/nvidia-gpu") != null) {
                            nodeDetailDto.setGpu(allocatable.get("alpha.kubernetes.io/nvidia-gpu").toString());
                        }
                        if (allocatable.get("gpu") != null) {
                            nodeDetailDto.setGpu(allocatable.get("gpu").toString());
                        }
                        nodeDetailDto.setKernelVersion(node.getStatus().getNodeInfo().getKernelVersion());
                        nodeDetailDto.setKubeProxyVersion(node.getStatus().getNodeInfo().getKubeProxyVersion());
                        nodeDetailDto.setKubeletVersion(node.getStatus().getNodeInfo().getKubeletVersion());
                        nodeDetailDto.setName(node.getMetadata().getName());
                        nodeDetailDto.setOs(node.getStatus().getNodeInfo().getOperatingSystem());
                        nodeDetailDto.setPods(capacity.get("pods").toString());
                        Map<String, Object> labels = node.getMetadata().getLabels();
                        if (labels.get("master") != null && labels.get("master").equals("master")) {
                            nodeDetailDto.setType("master");
                        } else {
                            nodeDetailDto.setType("slave");
                        }

                    }
                } else {
                    otherNodeList.add(node.getMetadata().getName());
                }
            }
        }
        List<Object> podList = podService.PodList(nodeIp, cluster);
        nodeDetailDto.setOtherNodeList(otherNodeList);
        nodeDetailDto.setClusterId(clusterId);
        nodeDetailDto.setPodlist(podList);
        // Node node = nodeService.getNode(nodeIp, cluster);

        Map map = new HashMap<>();
        map.put(CommonConstant.DATA, nodeDetailDto);
        return map;
    }
    @Override
    public Map getNode(String nodeIp, Cluster cluster) throws Exception {
        NodeList nodeList = nodeService.listNode(cluster);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        List<String> otherNodeList = new ArrayList<>();
        NodeDetailDto nodeDetailDto = new NodeDetailDto();
        if (nodeList != null && nodeList.getItems().size() > 0) {
            List<Node> items = nodeList.getItems();
            for (Node node : items) {
                if (nodeIp.equals(node.getMetadata().getName())) {
                    // 处理为页面需要的值
                    if (node != null) {
                        if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_B)) {
                            nodeDetailDto.setNodeShareStatus("闲置");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_C)) {
                            nodeDetailDto.setNodeShareStatus("共享");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_D)) {
                            nodeDetailDto.setNodeShareStatus("独占");
                        } else if (node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS) != null
                                && node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_STATUS).equals(CommonConstant.LABEL_STATUS_A)) {
                            nodeDetailDto.setNodeShareStatus("系统");
                        }
                        NodeDto nodedto = new NodeDto();
                        NodeDto hostUsege = this.getHostUsege(node, nodedto, cluster);
                        nodeDetailDto.setMemory(hostUsege.getMemory());
                        nodeDetailDto.setCpu(hostUsege.getCpu());
                        nodeDetailDto.setDisk(hostUsege.getDisk());
                        // 设置address
                        List<NodeAddress> addresses = node.getStatus().getAddresses();
                        nodeDetailDto.setAddresses(addresses);
                        // 设置images
                        List<ContainerImage> images = node.getStatus().getImages();
                        nodeDetailDto.setImages(images);
                        // 设置status
                        List<NodeCondition> conditions = node.getStatus().getConditions();
                        for (NodeCondition nodeCondition : conditions) {
                            if (nodeCondition.getType().equals("Ready")) {
                                nodeDetailDto.setStatus(nodeCondition.getStatus());
                                break;
                            }
                        }
                        nodeDetailDto.setArchitecture(node.getStatus().getNodeInfo().getArchitecture());
                        nodeDetailDto.setContainerRuntimeVersion(node.getStatus().getNodeInfo().getContainerRuntimeVersion());
                        Map<String, Object> capacity = (Map<String, Object>) node.getStatus().getCapacity();
                        nodeDetailDto.setCreationTime(node.getMetadata().getCreationTimestamp());

                        Map<String, Object> allocatable = (Map<String, Object>) node.getStatus().getAllocatable();
                        if (allocatable.get("alpha.kubernetes.io/nvidia-gpu") != null) {
                            nodeDetailDto.setGpu(allocatable.get("alpha.kubernetes.io/nvidia-gpu").toString());
                        }
                        if (allocatable.get("gpu") != null) {
                            nodeDetailDto.setGpu(allocatable.get("gpu").toString());
                        }
                        nodeDetailDto.setKernelVersion(node.getStatus().getNodeInfo().getKernelVersion());
                        nodeDetailDto.setKubeProxyVersion(node.getStatus().getNodeInfo().getKubeProxyVersion());
                        nodeDetailDto.setKubeletVersion(node.getStatus().getNodeInfo().getKubeletVersion());
                        nodeDetailDto.setName(node.getMetadata().getName());
                        nodeDetailDto.setOs(node.getStatus().getNodeInfo().getOperatingSystem());
                        nodeDetailDto.setPods(capacity.get("pods").toString());
                        Map<String, Object> labels = node.getMetadata().getLabels();
                        if (labels.get("master") != null && labels.get("master").equals("master")) {
                            nodeDetailDto.setType("master");
                        } else {
                            nodeDetailDto.setType("slave");
                        }

                    }
                } else {
                    otherNodeList.add(node.getMetadata().getName());
                }
            }
        }
        List<Object> podList = podService.PodList(nodeIp, cluster);
        nodeDetailDto.setOtherNodeList(otherNodeList);
        nodeDetailDto.setClusterId(cluster.getId().toString());
        nodeDetailDto.setPodlist(podList);
        // Node node = nodeService.getNode(nodeIp, cluster);

        Map map = new HashMap<>();
        map.put(CommonConstant.DATA, nodeDetailDto);
        return map;
    }
    private NodeDto getHostUsege(Node node, NodeDto nodeDto, Cluster cluster) throws Exception {
        double nodeFilesystemCapacity = this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id", cluster, null,
                node.getMetadata().getName());
        Object object = node.getStatus().getAllocatable();
        if (object != null) {
            nodeDto.setCpu(((Map<String, Object>) object).get("cpu").toString());
            String memory = ((Map<String, Object>) object).get("memory").toString();
            memory = memory.substring(0, memory.indexOf("Ki"));
            double memoryDouble = Double.parseDouble(memory);
            nodeDto.setMemory(String.format("%.1f", memoryDouble / 1024 / 1024));
            nodeDto.setDisk(String.format("%.1f", (nodeFilesystemCapacity / 1024 / 1024
                    / 1024)/** 1.024*1.024*1.024 */
            ));
        }
        return nodeDto;
    }

    @Override
    public List<NodeInstallProgress> getOnLineStatusWithClusterId(String clusterId) throws Exception {
        List<NodeInstallProgress> nodeInLineInfoByInstallStatusAndClusterId = this.nodeInstallProgressService.getNodeInLineInfoByInstallStatusAndClusterId(CommonConstant.BEGIN,
                clusterId);

        return nodeInLineInfoByInstallStatusAndClusterId;
    }

    @Override
    public String getOnLineErrorStatus() throws Exception {
        String errorStatus = this.nodeInstallProgressService.getOnLineErrorStatus();

        return errorStatus;
    }
    @Override
    public ActionReturnUtil updateShareToNode(String nodeName, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Node node = this.nodeService.getNode(nodeName, cluster);
        K8SClientResponse updateNode = null;
        if (node != null) {
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Set<Entry<String, Object>> entrySet = oldLabels.entrySet();
            // 更新labels
            for (Entry<String, Object> label : entrySet) {
                // label
                if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null && CommonConstant.LABEL_STATUS_B.equals(oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS))) {
                    oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_C);
                    break;
                } else if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null) {
                    ActionReturnUtil.returnErrorWithMsg("只能修改闲置节点或者无状态节点！");
                }
            }
            if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) == null) {
                oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_C);
            }
            ObjectMeta metadata = node.getMetadata();
            metadata.setLabels(oldLabels);
            // 更新K8s
            Map<String, Object> bodys = new HashMap<>();
            bodys.put(CommonConstant.KIND, node.getKind());
            bodys.put(CommonConstant.APIVERSION, node.getApiVersion());
            bodys.put(CommonConstant.SPEC, node.getSpec());
            bodys.put(CommonConstant.STATUS, node.getStatus());
            bodys.put(CommonConstant.METADATA, metadata);

            updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
            if (HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
                return ActionReturnUtil.returnSuccess();
            }
        }
        return ActionReturnUtil.returnErrorWithMsg(updateNode.getBody());
    }

    @Override
    public ActionReturnUtil cancelAddNode(Integer id) throws Exception {
        this.nodeInstallProgressService.cancelAddNode(id);
        return ActionReturnUtil.returnSuccess();
    }

}
