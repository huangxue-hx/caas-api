package com.harmonycloud.service.platform.serviceImpl.infrastructure;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.NodeDrainProgress;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantPrivateNode;
import com.harmonycloud.dto.cluster.NodeBriefDto;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.NodeDrainProgressService;
import com.harmonycloud.service.tenant.PrivatePartitionService;
import com.harmonycloud.service.tenant.TenantPrivateNodeService;
import org.apache.commons.lang3.StringUtils;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.k8s.bean.cluster.Cluster;
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
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.tenant.TenantService;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.springframework.util.CollectionUtils;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.common.Constant.IngressControllerConstant.LABEL_VALUE_NGINX_CUSTOM;
import static com.harmonycloud.service.platform.constant.Constant.NODESELECTOR_LABELS_PRE;

@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private com.harmonycloud.k8s.service.NodeService nodeService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private InfluxdbService influxdbService;
    @Autowired
    private PodService podService;
    @Autowired
    private NodeInstallProgressService nodeInstallProgressService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private NodeDrainProgressService nodeDrainProgressService;
    @Autowired
    private PrivatePartitionService privatePartitionService;
    @Autowired
    private TenantPrivateNodeService tenantPrivateNodeService;
    @Value("#{propertiesReader['pod.drain.timeout']}")
    private String podDrainTimeout;

    protected static final ExecutorService executor = Executors.newFixedThreadPool(CommonConstant.NUM_FIVE);

    private static final Logger log = LoggerFactory.getLogger(NodeServiceImpl.class);
    private static final String MASTER = "master";
    private static final String SLAVE = "slave";
    private static final String READY = "Ready";
    private static final String LABELSELECTOR = "labelSelector";
    private static final String SECCUSS = "1000";
    private static final int TIMEOUT = 700000;
    private static final String DEFAULT_POD_DRAIN_TIMEOUT = "1800s";
    //共享
    private static final int SHARESTATUS = 1;
    //负债均衡
    private static final int SLBSTATUS = 2;
    //构建节点
    private static final int BUILDSTATUS = 3;
    //系统节点
    private static final int SYSTEMSTATUS = 4;

    /**
     * Node列表
     */
    @Override
    public ActionReturnUtil listNode(String clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        NodeList nodeList = nodeService.listNode(cluster);
        if(nodeList == null || CollectionUtils.isEmpty(nodeList.getItems())){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
        }
        List<NodeDto> nodeDtoList = new ArrayList<>();
        this.dealNodeStatus(nodeList.getItems(), cluster, nodeDtoList);
        nodeDtoList = nodeDtoList.stream().sorted(Comparator.comparing(NodeDto::getSortWeight)).collect(Collectors.toList());
        return ActionReturnUtil.returnSuccessWithData(nodeDtoList);
    }

    public void dealNodeStatus(List<Node> items, Cluster cluster, List<NodeDto> nodeDtoList) {
        for (Node node : items) {
            NodeDto nodeDto = new NodeDto();
            nodeDto.setScheduable(!node.getSpec().isUnschedulable());
            nodeDto.setClusterId(cluster.getId());
            nodeDto.setAliasName(cluster.getAliasName());
            nodeDto.setIp(node.getStatus().getAddresses().get(0).getAddress());
            nodeDto.setName(node.getMetadata().getName());
            nodeDto.setTime(node.getMetadata().getCreationTimestamp());
            nodeDto.setType(CommonConstant.DATANODE);
            Map<String, Object> labels = node.getMetadata().getLabels();
            String nodeSharedStatus = this.getNodeSharedStatus(labels);
            //获取不到节点类型，将节点置为闲置节点
            if(StringUtils.isBlank(nodeSharedStatus)){
                this.addNodeStatus(node.getMetadata().getName(), cluster);
            }
            nodeDto.setNodeShareStatus(nodeSharedStatus);
            nodeDto.setSortWeight(this.getSortWeight(nodeSharedStatus));
            if(DictEnum.NODE_MASTER.phrase().equals(nodeSharedStatus)){
                nodeDto.setType(CommonConstant.MASTERNODE);
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
                    if (entry.getKey().contains(NODESELECTOR_LABELS_PRE)) {
                        String key = entry.getKey();
                        key = key.replaceAll(NODESELECTOR_LABELS_PRE, "");
                        list.add(key + "=" + entry.getValue());
                    }
                }
            }
            dto.setCustomLabels(list);
            nodeDtoList.add(dto);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil getNodeDetail(String nodeName, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        // 处理为页面需要的值
        NodeDetailDto nodeDetailDto = new NodeDetailDto();
        if (node != null) {
            this.dealNode(node,nodeDetailDto,cluster);
        }
        return ActionReturnUtil.returnSuccessWithData(nodeDetailDto);
    }
    private void dealNode(Node node,NodeDetailDto nodeDetailDto,Cluster cluster)throws Exception{
        Map<String, Object> labelsMap = node.getMetadata().getLabels();
        String nodeSharedStatus = this.getNodeSharedStatus(labelsMap);
        nodeDetailDto.setNodeShareStatus(nodeSharedStatus);
        if(DictEnum.NODE_PRIVATE.phrase().equals(nodeSharedStatus)){
            Object o = labelsMap.get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS);
            if (!Objects.isNull(o)){
                String[] labels = o.toString().split(CommonConstant.LINE);
                if (labels.length>0){
                    TenantBinding tenantBinding = this.tenantService.getTenantBytenantName(labels[0]);
                    if (Objects.isNull(tenantBinding)){
                        nodeDetailDto.setTenantAliasName(MessageUtil.getMessage(ErrorCodeMessage.TENANTNOTINTHRCLUSTER));
                    } else {
                        nodeDetailDto.setTenantAliasName(tenantBinding.getAliasName());
                        String tenantId = tenantBinding.getTenantId();
                        String nodeName = node.getMetadata().getName();
                        String clusterId = cluster.getId();
                        TenantPrivateNode tenantPrivateNode = this.tenantPrivateNodeService.getTenantPrivateNode(tenantId, clusterId, nodeName);
                        if (Objects.isNull(tenantPrivateNode)){
                            tenantPrivateNode = new TenantPrivateNode();
                            tenantPrivateNode.setTenantId(tenantId);
                            tenantPrivateNode.setClusterId(clusterId);
                            tenantPrivateNode.setCreateTime(new Date());
                            tenantPrivateNode.setUpdateTime(new Date());
                            tenantPrivateNode.setNodeName(nodeName);
                            this.tenantPrivateNodeService.createTenantPrivateNode(tenantPrivateNode);
                        }
                    }
                }
            }
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
        nodeDetailDto.setScheduable(!node.getSpec().isUnschedulable());
        if (labelsMap.get("master") != null && labelsMap.get("master").equals("master")) {
            nodeDetailDto.setType("master");
        } else {
            nodeDetailDto.setType("slave");
        }
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
        if (!StringUtils.isBlank(nodeName)) {
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
    public ActionReturnUtil listNodeLabels(String nodeName, Cluster cluster, boolean isShowGroup) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        Map<String, Object> labels = node.getMetadata().getLabels();
        List<Object> list = new ArrayList<>();
        if (labels != null) {
            Set<Entry<String, Object>> entrySet = labels.entrySet();
            for (Entry<String, Object> entry : entrySet) {
                if (entry.getKey().contains(NODESELECTOR_LABELS_PRE)) {
                    String key = entry.getKey();
                    key = key.replaceAll(NODESELECTOR_LABELS_PRE, "");
                    if (Constant.NODE_LABEL_GROUP.equals(key) && !isShowGroup) {
                        continue;
                    }
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
                        split = itKey.split(NODESELECTOR_LABELS_PRE);
                    }
                    if (itKey.contains(NODESELECTOR_LABELS_PRE) && updateLabels.get(split[1]) == null) {
                        it.remove();
                    } else if (itKey.contains(NODESELECTOR_LABELS_PRE) && updateLabels.get(split[1]) != null) {
                        itEntry.setValue(updateLabels.get(split[1]));
                        rm.add(split[1]);
                    }
                }
            } else if (updateLabels == null && oldLabels != null) {
                // 循环遍历原来标签，如果新标签没有值，全部删除用户自定义的标签
                while (it.hasNext()) {
                    Entry<String, Object> itEntry = it.next();
                    String itKey = itEntry.getKey();
                    if (itKey.contains(NODESELECTOR_LABELS_PRE)) {
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
                    oldLabels.put(NODESELECTOR_LABELS_PRE + itKey, itValue);
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
                    if (m.getKey().indexOf(NODESELECTOR_LABELS_PRE) > -1) {
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
        return this.addNodeLabels(nodeName, newLabels, clusterService.findClusterById(clusterId));
    }

    public ActionReturnUtil addNodeLabels(String nodeName, Map<String, String> newLabels, Cluster cluster) throws Exception {

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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL, updateNode.getBody(), false);
        }
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_EXIST, DictEnum.NODE.phrase(), true);
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL, updateNode.getBody(), false);
        }
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NOT_EXIST, DictEnum.NODE.phrase(), true);
    }

    @Override
    public List<String> getPrivateNamespaceNodeList(String namespace, Cluster cluster) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_D);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys, cluster);
        List<String> nodeDtoList = new ArrayList<>();
        if (nodeList != null && nodeList.getItems() != null && !nodeList.getItems().isEmpty()) {
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
        String label = CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_D;
        List<NodeDto> nodeDtos = this.listNodeByLabel(label, cluster);
        return nodeDtos;
    }

    /**
     * 获得对应标签的私有节点列表
     *
     * @param label
     * @return
     */
    @Override
    public List<NodeDto> listNodeByLabel(String label, Cluster cluster) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(LABELSELECTOR, label);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys, cluster);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        if (nodeList != null && nodeList.getItems() != null && !nodeList.getItems().isEmpty()) {
            // 处理成为页面需要的值
            List<Node> items = nodeList.getItems();
            this.dealNodeStatus(items, cluster, nodeDtoList);
        }
        return nodeDtoList;
    }

    /**
     * 根据分区获取主机列表
     *
     * @param namespace
     * @return
     * @throws Exception
     */
    @Override
    public List<NodeDto> listNodeByNamespaces(String namespace) throws Exception {
        NamespaceLocal namespaceByName = this.namespaceLocalService.getNamespaceByName(namespace);
        Cluster cluster = this.namespaceLocalService.getClusterByNamespaceName(namespace);
        String tenantId = namespaceByName.getTenantId();
        //查询私有分区独占节点
        String privateLabel = privatePartitionService.getPrivatePartitionLabel(tenantId, namespace);
        List<NodeDto> nodeDtos = this.listNodeByLabel(privateLabel, cluster);
        return nodeDtos;
    }

    @Override
    public List<String> getAvailableNodeList(Cluster cluster) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put(CommonConstant.CONTENT_TYPE, CommonConstant.APPLICATION_JSON);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, CommonConstant.HARMONYCLOUD_STATUS + "=" + CommonConstant.LABEL_STATUS_B);
        NodeList nodeList = nodeService.listNodeByLabel(headers, bodys, cluster);
        List<String> nodeDtoList = new ArrayList<>();
        if (nodeList != null && nodeList.getItems() != null && !nodeList.getItems().isEmpty()) {
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
            throw new MarsRuntimeException(ErrorCodeMessage.CONNECT_FAIL);
        }
        // 设置登陆主机的密码
        session.setPassword(passwd);
        // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
        session.setConfig("StrictHostKeyChecking", "no");
        // 设置登陆超时时间
        try {
            session.connect(8000);
        } catch (Exception e) {
            throw new MarsRuntimeException(ErrorCodeMessage.CONNECT_FAIL);
        }
        session.disconnect();
        return ActionReturnUtil.returnSuccess();
    }

    public ActionReturnUtil getHostName(Session session) throws Exception {

        com.jcraft.jsch.ChannelExec ec = (com.jcraft.jsch.ChannelExec) session.openChannel("exec");

        InputStream inputStream = ec.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        try {
            ec.setCommand("hostname");
            ec.setInputStream(null);
            ec.setErrStream(System.err);
            ec.setOutputStream(System.out);
            ec.connect();
            byte[] tmp = new byte[1024];
            while (true) {
                while (inputStream.available() > 0) {
                    int i = inputStream.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (ec.isClosed()) {
                    if (inputStream.available() > 0)
                        continue;
                    System.out.println("exit-status:1111 ");
                    System.out.println("exit-status: " + ec.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                }
            }
            System.out.printf("hostname：");
        } catch (IOException e) {
            log.warn("getHostName失败", e);
        } catch (JSchException e) {
            log.warn("getHostName失败", e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                log.warn("getHostName失败", e);
            }
            ec.disconnect();
            session.disconnect();
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil addNode(String host, String user, String passwd, String masterIp, String clusterId) throws Exception {
        if (StringUtils.isAnyBlank(host, user, passwd, masterIp, clusterId)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INVALID_PARAMETER);
        }
        ActionReturnUtil actionReturnUtil = this.listNode(clusterId);
        boolean success = actionReturnUtil.isSuccess();
        List<NodeDto> nodeList = (List<NodeDto>) actionReturnUtil.get(CommonConstant.DATA);
        if (!success || CollectionUtils.isEmpty(nodeList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
        }
        List<NodeDto> collect = nodeList.stream().filter(nodeDto -> host.equals(nodeDto.getIp())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(collect)) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_EXIST, host, Boolean.TRUE);
        }
        NodeInstallProgress nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
        if (nodeInstall == null) {
            nodeInstall = new NodeInstallProgress();
            nodeInstall.setInstallStatus(CommonConstant.BEGIN);
            nodeInstall.setName(host);
            nodeInstall.setProgress(0);
            nodeInstall.setClusterId(clusterId);
            nodeInstallProgressService.addNodeInLineInfo(nodeInstall);
        } else {
            nodeInstall.setProgress(0);
            nodeInstall.setInstallStatus(CommonConstant.BEGIN);
            nodeInstall.setClusterId(clusterId);
            nodeInstall.setErrorMsg(null);
            nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
        }
        Cluster cluster = clusterService.findClusterById(clusterId);
        HarborServer harborServer = clusterService.findClusterById(clusterId).getHarborServer();
        Map<String, Object> params = new HashMap<>();
        params.put("host", host);
        params.put("user", user);
        params.put("passwd", passwd);
        params.put("masterIp", cluster.getHost());
        params.put("harborIp", harborServer.getHarborAddress());


        Runnable worker = new Runnable() {
            @Override
            public void run() {
                ActionReturnUtil flag = new ActionReturnUtil();
                try {
                    log.info("开始节点上线");
                    ActionReturnUtil httpGetRequest = HttpClientUtil.httpGetRequest("http://" + cluster.getHost() + ":9999/installnode", null, params, TIMEOUT);
                    if ((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) != CommonConstant.FALSE) {
                        Object object = httpGetRequest.get(CommonConstant.DATA);
                        String ad = object == null ? "" : object.toString();
                        NodeInstallProgress nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
                        if (!ad.contains(SECCUSS)) {
                            nodeInstall.setInstallStatus(ad);
                        }else {
                            nodeInstall.setInstallStatus(CommonConstant.DONE);
                        }
                        //设置进度为0
                        nodeInstall.setProgress(0);
                        nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
                    } else {
                        Object object = httpGetRequest.get(CommonConstant.DATA);
                        throw new Exception(object.toString());
                    }

                } catch (Exception e) {
                    NodeInstallProgress nodeInstall;
                    try {
                        nodeInstall = nodeInstallProgressService.getNodeInLineInfoByNodeIp(host);
                        nodeInstall.setErrorMsg(e.getMessage());
                        nodeInstall.setProgress(0);
                        nodeInstall.setInstallStatus("error");
                        nodeInstallProgressService.updateNodeInLineInfo(nodeInstall);
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        log.warn("节点上线失败", e1);
                    }
                }
            }
        };
        ESFactory.executor.execute(worker);

        return ActionReturnUtil.returnSuccess();
    }

    public void addNodeStatus(String host, Cluster cluster) {
        Map<String, String> newLabels = new HashMap<>();
        newLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_B);
        newLabels.put("alpha.kubernetes.io/fluentd-ds-ready", "true");
        String hostname = null;
        try {
            hostname = gethostname(host, cluster);
            if (!host.equals(hostname)) {
                ActionReturnUtil addNodeLabels = addNodeLabels(hostname, newLabels, cluster);
            } else {
                ActionReturnUtil addNodeLabels = addNodeLabels(host, newLabels, cluster);
            }
        } catch (Exception e) {
            log.warn("添加节点状态失败", e);
        }
    }

    public String gethostname(String host, Cluster cluster) throws Exception {
        NodeList nodeList = nodeService.listNode(cluster);
        List<Node> items = nodeList.getItems();
        for (Node node : items) {
            node.getMetadata();
            List<NodeAddress> addresses = node.getStatus().getAddresses();
            boolean flag = false;
            for (NodeAddress address:addresses) {
                if(address.getType().equals("Hostname")){
                    if(address.getAddress().equals(host)){
                        return address.getAddress();
                    }
                }
                if (address.getType().equals("InternalIP")) {
                    if (address.getAddress().equals(host)) {
                        flag = true;
                        continue;
                    }
                }
                if (flag) {
                    return address.getAddress();
                }
            }
        }
        return null;
    }

    @Override
    public ActionReturnUtil removeNode(String host, String user, String passwd, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Map<String, Object> params = new HashMap<>();
        //判断集群是否存在
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        ActionReturnUtil actionReturnUtil = this.listNode(clusterId);
        boolean success = actionReturnUtil.isSuccess();
        List<NodeDto> nodeList = (List<NodeDto>) actionReturnUtil.get(CommonConstant.DATA);
        if (!success || CollectionUtils.isEmpty(nodeList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
        }
        //检查下线节点是否有效
        boolean removed = Boolean.FALSE;
        for (NodeDto nodeDto : nodeList) {
            switch (nodeDto.getNodeShareStatus()) {
                case CommonConstant.MASTER_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.MASTER_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.SYSTEM_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.SYSTEM_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.BUILD_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.BUILD_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.LBS_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.LBS_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.PRIVATE_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_CANNOT_REMOVED, CommonConstant.PRIVATE_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.SYSTEM_AND_LBS_CN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_CANNOT_REMOVED, CommonConstant.SYSTEM_AND_LBS_CN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.MASTER_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.MASTER_EN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.SYSTEM_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.SYSTEM_EN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.BUILD_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.BUILD_EN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.LBS_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.WORK_NODE_OFFLINE, CommonConstant.LBS_EN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.PRIVATE_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_CANNOT_REMOVED, CommonConstant.PRIVATE_EN, Boolean.TRUE);
                    }
                    break;
                case CommonConstant.SYSTEM_AND_LBS_EN:
                    if (host.equals(nodeDto.getIp())) {
                        throw new MarsRuntimeException(ErrorCodeMessage.NODE_CANNOT_REMOVED, CommonConstant.SYSTEM_AND_LBS_EN, Boolean.TRUE);
                    }
                    break;
                default:
                    if (host.equals(nodeDto.getIp())) {
                        removed = Boolean.TRUE;
                        break;
                    }
            }
            if (removed) {
                //下线节点正常，跳出检查，进行下线
                break;
            }
        }
        if (!removed) {
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_NOT_EXIST, host, Boolean.TRUE);
        }
        String hostName = this.gethostname(host, cluster);
        params.put("host", host);
        params.put("hostName", hostName);
        params.put("user", user);
        params.put("passwd", passwd);

        params.put("token", cluster.getMachineToken());
        params.put("masterIp", cluster.getHost());
        //节点下线
        ActionReturnUtil httpGetRequest = HttpClientUtil.httpGetRequest("http://" + cluster.getHost() + ":9999/uninstallnode", null, params, TIMEOUT);
        if ((Boolean) httpGetRequest.get(CommonConstant.SUCCESS) == CommonConstant.FALSE) {
            return httpGetRequest;
        } else {
            Object object = httpGetRequest.get(CommonConstant.DATA);
            String ad = object == null ? "" : object.toString();
            if (ad.contains(SECCUSS)) {
                return httpGetRequest;
            } else {
                return ActionReturnUtil.returnErrorWithMsg(ad);
            }
        }
    }

    @Override
    public Map getNode(String nodeIp, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        String hostname = this.gethostname(nodeIp, cluster);
        if (StringUtils.isBlank(hostname)) {
            hostname = nodeIp;
        }
        return this.getNode(hostname, cluster);
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
                        this.dealNode(node,nodeDetailDto,cluster);
                    }
                } else {
                    otherNodeList.add(node.getMetadata().getName());
                }
            }
        }
        List<PodDto> podList = podService.PodList(nodeIp, cluster);
        nodeDetailDto.setOtherNodeList(otherNodeList);
        nodeDetailDto.setClusterId(cluster.getId());
        nodeDetailDto.setPodlist(podList);
        // Node node = nodeService.getNode(nodeIp, cluster);

        Map map = new HashMap<>();
        map.put(CommonConstant.DATA, nodeDetailDto);
        return map;
    }

    private NodeDto getHostUsege(Node node, NodeDto nodeDto, Cluster cluster) {
        double nodeFilesystemCapacity = this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id", cluster, null,
                node.getMetadata().getName());
        Object object = node.getStatus().getAllocatable();
        if (object != null) {

            String cpuStr = ((Map<String, Object>) object).get("cpu").toString();
            if(cpuStr.contains("m")) {
                cpuStr = cpuStr.substring(0, cpuStr.indexOf("m"));
                nodeDto.setCpu((Double.valueOf(cpuStr) / 1000) + "");
            } else {
                nodeDto.setCpu(Double.valueOf(cpuStr) + "");
            }
            String memory = ((Map<String, Object>) object).get("memory").toString();
            if(memory.contains("Ki")) {
                memory = memory.substring(0, memory.indexOf("Ki"));
                double memoryDouble = Double.parseDouble(memory);
                nodeDto.setMemory(String.format("%.1f", memoryDouble / 1024 / 1024));
            } else if (memory.contains("Mi")) {
                memory = memory.substring(0, memory.indexOf("Mi"));
                double memoryDouble = Double.parseDouble(memory);
                nodeDto.setMemory(String.format("%.1f", memoryDouble / 1024));
            } else if (memory.contains("Gi")) {
                memory = memory.substring(0, memory.indexOf("Gi"));
                double memoryDouble = Double.parseDouble(memory);
                nodeDto.setMemory(String.format("%.1f", memoryDouble));
            }

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

    /**
     * 更新闲置节点状态
     *
     * @param nodeName
     * @param clusterId
     * @param nodeType  1 共享，2负债均衡，3构建节点，4系统节点
     * @throws Exception
     */
    @Override
    public void updateIdleNodeStatus(String nodeName, String clusterId, Integer nodeType) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node != null) {
            //获取节点标签
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Boolean status= Boolean.FALSE;
            if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null && CommonConstant.LABEL_STATUS_B.equals(oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS))) {
                status = Boolean.TRUE;
            } else if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_STATUS_REQUIRE);
            }
            // 更新labels
            if (status) {
                //A 表示关键组件布置的节点,B 表示闲置状态,C 表示共享状态,D 私有状态，可以供私有分区独占使用,E表示构建节点,F表示负载均衡节点
                switch (nodeType){
                    case SHARESTATUS :
                        oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_C);
                        break;
                    case SLBSTATUS :
                        oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS_LBS, CommonConstant.LABEL_STATUS_F);
                        oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.EMPTYSTRING);
                        break;
                    case BUILDSTATUS :
                        oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_E);
                        break;
                    case SYSTEMSTATUS :
                        oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_A);
                        break;
                    default :
                        throw new MarsRuntimeException(ErrorCodeMessage.UNKNOWN);
                }

            }
            // 更新K8s 标签
            this.updateNode(oldLabels,cluster,node,nodeName);
        }
    }

    @Override
    public void updateWorkNodeToIdleStatus(String nodeName, String clusterId) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node != null) {
            if (!node.getSpec().isUnschedulable()){
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_UNSCHEDULABLE_ONLY);
            }
            node.getSpec().setUnschedulable(!node.getSpec().isUnschedulable());
            //获取该节点的pod列表
            List<PodDto> podList = podService.PodList(nodeName, cluster);
            for (PodDto podDto:podList) {
                //如果该节点还有其他非系统pod则提示不能移除该主机
                if (!(CommonConstant.KUBE_SYSTEM.equals(podDto.getNamespace()) || CommonConstant.DEFAULT.equals(podDto.getNamespace()))){
                    throw new MarsRuntimeException(ErrorCodeMessage.NODE_STATUS_NOT_REMOVE);
                }
            }
            //获取节点标签
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            Boolean status= Boolean.FALSE;
            // 更新labels
            if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null) {
                status = Boolean.TRUE;
            } else if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null) {
                throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_ERROR);
            }
            if (status) {
                oldLabels.put(CommonConstant.HARMONYCLOUD_STATUS, CommonConstant.LABEL_STATUS_B);
            }
            //清除负载均衡标签
            if (oldLabels.get(CommonConstant.HARMONYCLOUD_STATUS_LBS) != null) {
                oldLabels.remove(CommonConstant.HARMONYCLOUD_STATUS_LBS);
            }
            // 更新K8s 标签
            this.updateNode(oldLabels,cluster,node,nodeName);
        }
    }
    private void updateNode(Map<String, Object> oldLabels,Cluster cluster,Node node,String nodeName) throws Exception {
        ObjectMeta metadata = node.getMetadata();
        metadata.setLabels(oldLabels);
        // 更新K8s
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.KIND, node.getKind());
        bodys.put(CommonConstant.APIVERSION, node.getApiVersion());
        bodys.put(CommonConstant.SPEC, node.getSpec());
        bodys.put(CommonConstant.STATUS, node.getStatus());
        bodys.put(CommonConstant.METADATA, metadata);

        K8SClientResponse updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
            log.error(updateNode.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.NODE_LABEL_UPDATE_ERROR,updateNode.getBody(),Boolean.FALSE);
        }
    }
    @Override
    public ActionReturnUtil cancelAddNode(Integer id) throws Exception {
        this.nodeInstallProgressService.cancelAddNode(id);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil listNodeLabels(String namespace, String clusterId) throws Exception {
        if (StringUtils.isEmpty(namespace) && StringUtils.isEmpty(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //获取集群
        Cluster cluster = new Cluster();
        List<NodeDto> nodeList = new ArrayList<>();
        if (StringUtils.isNotBlank(namespace)) {
            cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            //获取node list
            nodeList = this.listNodeByNamespaces(namespace);
        }
        if (StringUtils.isNotBlank(clusterId)) {
            cluster = clusterService.findClusterById(clusterId);
            ActionReturnUtil nodesRes = this.listNode(clusterId);
            if (!nodesRes.isSuccess()) {
                return nodesRes;
            }
            nodeList = (List<NodeDto>) nodesRes.get("data");
        }

        if (nodeList != null && nodeList.size() > 0) {
            List<Object> list = new ArrayList<Object>();
            for (NodeDto no : nodeList) {
                ActionReturnUtil res = listNodeLabels(no.getName(), cluster, CommonConstant.FALSE);
                if (!res.isSuccess()) {
                    return res;
                }
                @SuppressWarnings("unchecked")
                List<Object> ll = (List<Object>) res.get("data");
                list.addAll(ll);
            }
            //去重
            List<Object> newList = list.stream().distinct().collect(Collectors.toList());
            return ActionReturnUtil.returnSuccessWithData(newList);
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NODE_NOT_FIND_IN_CLUSTER);
        }
    }

    @Override
    public ActionReturnUtil switchNodeSchedulable(String nodeName, Boolean schedulable, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Node node = nodeService.getNode(nodeName, cluster);
        K8SClientResponse updateRes = null;
        if (node != null) {
            node.getSpec().setUnschedulable(!schedulable);
            Map<String, Object> bodys = new HashMap<>();
            bodys.put(CommonConstant.KIND, node.getKind());
            bodys.put(CommonConstant.APIVERSION, node.getApiVersion());
            bodys.put(CommonConstant.SPEC, node.getSpec());
            bodys.put(CommonConstant.STATUS, node.getStatus());
            bodys.put(CommonConstant.METADATA, node.getMetadata());
            updateRes = nodeService.updateNode(bodys, nodeName, cluster);
            if (HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                return ActionReturnUtil.returnSuccess();
            }
        }
        if (schedulable) {
            NodeDrainProgress nodeDrainProgress = nodeDrainProgressService.findByNodeName(nodeName, clusterId);
            if (nodeDrainProgress != null && !CommonConstant.CLOSED.equals(nodeDrainProgress.getStatus())) {
                nodeDrainProgress.setStatus(CommonConstant.CLOSED);
                nodeDrainProgress.setUpdateTime(new Date());
                nodeDrainProgressService.updateDrainProgress(nodeDrainProgress);
            }
        }
        return ActionReturnUtil.returnErrorWithMsg(updateRes.getBody());
    }

    @Override
    public ActionReturnUtil drainPod(String nodeName, String clusterId) throws Exception {
        String shellPath = this.getClass().getClassLoader().getResource("shell/nodeDrainPod.sh").getPath();
        if (StringUtils.isBlank(shellPath) || !new File(shellPath).exists()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SCRIPT_NOT_EXIST);
        }
        Cluster cluster = clusterService.findClusterById(clusterId);
        //判断node_drain_progress中有无该node
        NodeDrainProgress existNodeDrainProgress = nodeDrainProgressService.findByNodeName(nodeName, clusterId);
        //如果已经有一个正在进行中的drain过程则返回错误
        if (existNodeDrainProgress != null && existNodeDrainProgress.getStatus().equalsIgnoreCase(CommonConstant.INPROCESS)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DRAIN_IN_PROCESS);
        }
        NodeDrainProgress nodeDrainProgress = new NodeDrainProgress();
        nodeDrainProgress.setNodeName(nodeName);
        nodeDrainProgress.setClusterId(clusterId);
        nodeDrainProgress.setCreateTime(new Date());
        nodeDrainProgress.setStatus(CommonConstant.READY);

        List<PodDto> podList = podService.PodList(nodeName, cluster);
        if (CollectionUtils.isEmpty(podList)) {
            nodeDrainProgress.setPodTotalNum(0);
        }

        nodeDrainProgress.setPodTotalNum(podList.size());
        //没有需要迁移的pod直接返回成功
        if (nodeDrainProgress.getPodTotalNum() == 0) {
            nodeDrainProgress.setStatus(CommonConstant.SUCCESS);
            nodeDrainProgressService.insertDrainProgress(nodeDrainProgress);
            return ActionReturnUtil.returnSuccess();
        } else {
            nodeDrainProgressService.insertDrainProgress(nodeDrainProgress);
        }
        final String node = nodeName;
        final String path = shellPath;

        executor.execute(new Runnable() {

            @Override
            public void run() {
                NodeDrainProgress drainProgress = nodeDrainProgressService.findByNodeName(nodeName, clusterId);
                drainProgress.setStatus(CommonConstant.INPROCESS);
                try {
                    ProcessBuilder proc = new ProcessBuilder("sh", path, node, cluster.getMachineToken(), cluster.getApiServerUrl(),
                            podDrainTimeout == null?DEFAULT_POD_DRAIN_TIMEOUT:podDrainTimeout);
                    Process p = proc.start();
                    String res = null;
                    String errMsg = null;
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    while ((res = stdInput.readLine()) != null) {
                        res = res.trim();
                        log.info(res);
                        //将结果实时存入数据库
                        if (res.toLowerCase().startsWith("pod") && res.toLowerCase().endsWith("evicted")) {
                            drainProgress.setProgress(drainProgress.getProgress() == null ? "" : drainProgress.getProgress() + SEMICOLON + res);
                            nodeDrainProgressService.updateDrainProgress(drainProgress);
                        }
                        if (res.toLowerCase().startsWith("node") && res.toLowerCase().endsWith("drained")) {
                            log.info("应用迁移完成: {}", res);
                        }
                    }
                    while ((res = stdError.readLine()) != null) {
                        errMsg += res;
                        log.error("应用迁移进度错误,{}", res);
                    }
                    int runningStatus = p.waitFor();
                    log.info("应用迁移结果:{}", runningStatus);

                    // 0代表成功
                    if (runningStatus == 0) {
                        log.info("应用迁移成功,nodeName:{}", nodeName);
                        drainProgress.setStatus(CommonConstant.SUCCESS);
                    } else {
                        log.info("应用迁移失败,errorMessage:{}", errMsg);
                        drainProgress.setStatus(CommonConstant.FAIL);
                        drainProgress.setErrorMsg(errMsg);
                    }
                    nodeDrainProgressService.updateDrainProgress(drainProgress);
                } catch (Exception e) {
                    log.error("执行上传文件脚本结果失败:", e);
                    drainProgress.setStatus(CommonConstant.FAIL);
                    drainProgress.setErrorMsg(e.getMessage());
                    nodeDrainProgressService.updateDrainProgress(drainProgress);
                }
            }
        });
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getDrainPodProgress(String nodeName, String clusterId) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        //根据NodeName查询数据库
        NodeDrainProgress nodeDrainProgress = nodeDrainProgressService.findByNodeName(nodeName, clusterId);
        if (nodeDrainProgress == null || nodeDrainProgress.getStatus().equals(CommonConstant.CLOSED)) {
            result.put("inProgress", false);
            return ActionReturnUtil.returnSuccessWithData(result);
        }
        result.put("status", nodeDrainProgress.getStatus());
        result.put("inProgress", true);
        if (nodeDrainProgress.getStatus().equals(CommonConstant.SUCCESS)) {
            result.put("progress", PERCENT_HUNDRED);
            return ActionReturnUtil.returnSuccessWithData(result);
        }
        String progress = nodeDrainProgress.getProgress();
        if (StringUtils.isEmpty(progress)) {
            result.put("progress", 0);
        } else {
            String[] pgs = progress.split(SEMICOLON);
            double percent = new BigDecimal((float) pgs.length / nodeDrainProgress.getPodTotalNum())
                    .setScale(KEEP_DECIMAL_3, BigDecimal.ROUND_HALF_UP).doubleValue();
            result.put("progress", percent * PERCENT_HUNDRED);
        }

        result.put("status", nodeDrainProgress.getStatus());
        result.put("errMsg", nodeDrainProgress.getErrorMsg());
        return ActionReturnUtil.returnSuccessWithData(result);
    }

    /**
     * 移除私有分区主机状态
     *
     * @param nodeName
     * @param updateLabel
     * @param cluster
     * @throws Exception
     */
    @Override
    public void removePrivateNamespaceNodes(String nodeName, Map<String, String> updateLabel, Map<String, String> deleteLabel, Cluster cluster) throws Exception {
        Node node = this.nodeService.getNode(nodeName, cluster);
        if (node != null) {
            Map<String, Object> oldLabels = node.getMetadata().getLabels();
            if (!CollectionUtils.isEmpty(deleteLabel)) {
                deleteLabel.keySet().stream().forEach(label -> {
                    oldLabels.remove(label);
                });
            }
            if (!CollectionUtils.isEmpty(updateLabel)) {
                updateLabel.keySet().stream().forEach(label -> {
                    oldLabels.put(label, updateLabel.get(label));
                });
            }
//            if (!node.getSpec().isUnschedulable()){
//                node.getSpec().setUnschedulable(!node.getSpec().isUnschedulable());
//            }
            ObjectMeta metadata = node.getMetadata();
            metadata.setLabels(oldLabels);
            // 更新K8s
            Map<String, Object> bodys = new HashMap<>();
            bodys.put(CommonConstant.KIND, node.getKind());
            bodys.put(CommonConstant.APIVERSION, node.getApiVersion());
            bodys.put(CommonConstant.SPEC, node.getSpec());
            bodys.put(CommonConstant.STATUS, node.getStatus());
            bodys.put(CommonConstant.METADATA, metadata);

            K8SClientResponse updateNode = this.nodeService.updateNode(bodys, nodeName, cluster);
            if (!HttpStatusUtil.isSuccessStatus(updateNode.getStatus())) {
                throw new MarsRuntimeException(ErrorCodeMessage.UPDATE_FAIL);
            }
        }
    }

    /**
     * 获取不可用主机列表
     */
    @Override
    public List<NodeBriefDto> listUnavailableNodes() throws Exception{
        List<Cluster> clusters = clusterService.listAllCluster(null);
        List<NodeBriefDto> nodeBriefDtoList = new ArrayList<>();
        for (Cluster cluster : clusters){
            NodeList nodeList = nodeService.listNode(cluster);
            for (Node node : nodeList.getItems()){
                NodeBriefDto nodeBriefDto = new NodeBriefDto(cluster.getId(), node.getMetadata().getName());
                for (NodeCondition condition : node.getStatus().getConditions()){
                    if (isNodeConditionNotHealthy(condition)){
                        nodeBriefDto.getConditions().add(condition);
                    }
                }
                if(!CollectionUtils.isEmpty(nodeBriefDto.getConditions())){
                    nodeBriefDtoList.add(nodeBriefDto);
                }
            }
        }
        return nodeBriefDtoList;
    }

    private boolean isNodeConditionNotHealthy(NodeCondition condition){
        boolean isNodeConditionNotHealthy = false;
        switch (condition.getType()){
            case CommonConstant.NODE_CONDITION_OUT_OF_DISK:
            case CommonConstant.NODE_CONDITION_DISK_PRESSURE:
            case CommonConstant.NODE_CONDITION_MEMORY_PRESSURE:
            case CommonConstant.NODE_CONDITION_PID_PRESSURE:
            case CommonConstant.NODE_CONDITION_NETWORK_UNAVAILABLE:
                if (!condition.getStatus().equals(CommonConstant.NODE_CONDITION_STATUS_FALSE)){
                    isNodeConditionNotHealthy = true;
                }
                break;
            case CommonConstant.NODE_CONDITION_READY:
                if (!condition.getStatus().equals(CommonConstant.NODE_CONDITION_STATUS_TRUE)){
                    isNodeConditionNotHealthy = true;
                }
                break;
            default:
        }
        return isNodeConditionNotHealthy;
    }

    /**
     * 获取节点的类型（系统，共享，独占，闲置，负载均衡）
     *
     * @param nodeLabels 节点标签
     * @return
     */
    private String getNodeSharedStatus(Map<String, Object> nodeLabels) {
        if (nodeLabels.get(CommonConstant.MASTERNODELABEL) != null) {
            return DictEnum.NODE_MASTER.phrase();
        }
        String nodeSharedStatus = null;
        boolean lbNode = false;
        if (nodeLabels.get(HARMONYCLOUD_STATUS_LBS) != null
                && (nodeLabels.get(HARMONYCLOUD_STATUS_LBS).equals(CommonConstant.LABEL_STATUS_F)
                || nodeLabels.get(HARMONYCLOUD_STATUS_LBS).equals(LABEL_VALUE_NGINX_CUSTOM))) {
            lbNode = true;
            nodeSharedStatus = DictEnum.NODE_LB.phrase();
        }
        String nodeStatusLabel = null;
        if (nodeLabels.get(CommonConstant.HARMONYCLOUD_STATUS) != null) {
            nodeStatusLabel = nodeLabels.get(CommonConstant.HARMONYCLOUD_STATUS).toString();
        }
        if (nodeStatusLabel == null) {
            return nodeSharedStatus;
        }
        switch (nodeStatusLabel) {
            case LABEL_STATUS_A:
                if (lbNode) {
                    return DictEnum.NODE_SYSTEMANDSLB.phrase();
                } else {
                    return DictEnum.NODE_SYSTEM.phrase();
                }
            case LABEL_STATUS_B:
                return DictEnum.NODE_IDLE.phrase();
            case LABEL_STATUS_C:
                return DictEnum.NODE_PUBLIC.phrase();
            case LABEL_STATUS_D:
                return DictEnum.NODE_PRIVATE.phrase();
            case LABEL_STATUS_E:
                return DictEnum.NODE_BUILD.phrase();
            default:
                return nodeSharedStatus;
        }
    }

    /**
     * 节点列表排序,顺序
     */
    private int getSortWeight(String nodeShareStatus){
        if(DictEnum.NODE_MASTER.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_MASTER.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_ONE;
        }
        if(DictEnum.NODE_SYSTEM.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_SYSTEM.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_TWO;
        }
        if(DictEnum.NODE_SYSTEMANDSLB.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_SYSTEMANDSLB.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_THREE;
        }
        if(DictEnum.NODE_BUILD.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_BUILD.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_FOUR;
        }
        if(DictEnum.NODE_LB.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_LB.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_FIVE;
        }
        if(DictEnum.NODE_PRIVATE.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_PRIVATE.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_SIX;
        }
        if(DictEnum.NODE_PUBLIC.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_PUBLIC.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_SEVEN;
        }
        if(DictEnum.NODE_IDLE.getChPhrase().equals(nodeShareStatus)
                || DictEnum.NODE_IDLE.getEnPhrase().equals(nodeShareStatus)){
            return CommonConstant.NUM_EIGHT;
        }
        return CommonConstant.NUM_NINE;
    }


    /**
     * 获取满足label的node节点
     * @param clusterId
     * @param label
     * @throws MarsRuntimeException
     * @return
     */
    @Override
    public ActionReturnUtil getLabelNodes(String clusterId,String label) throws MarsRuntimeException {
        Cluster cluster = clusterService.findClusterById(clusterId);
        NodeList nodeList = nodeService.listNode(cluster);
        List<NodeDto> nodeDtoList = new ArrayList<>();
        List<Node> nodeData=new ArrayList<>();
        if (nodeList != null && nodeList.getItems().size() > 0) {
            List<Node> nodes = nodeList.getItems();
            for(Node node:nodes){
                Map<String, Object> labels = node.getMetadata().getLabels();
                if (labels != null) {
                    Set<Entry<String, Object>> entrySet = labels.entrySet();
                    for (Entry<String, Object> entry : entrySet) {
                        if (entry.getKey().contains(NODESELECTOR_LABELS_PRE)&&!entry.getKey().contains("group")) {
                            String key = entry.getKey();
                            key = key.replaceAll(NODESELECTOR_LABELS_PRE, "");
                            String labelStr=key + "=" + entry.getValue();
                            if(label.equals(labelStr)){
                                nodeData.add(node);
                            }
                        }
                    }
                }
            }
            dealNodeStatus(nodeData, cluster, nodeDtoList);
        }
        return ActionReturnUtil.returnSuccessWithData(nodeDtoList);
    }
}
