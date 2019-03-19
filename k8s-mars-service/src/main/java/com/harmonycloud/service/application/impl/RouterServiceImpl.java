package com.harmonycloud.service.application.impl;

import com.fasterxml.classmate.Annotations;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.IngressControllerPort;
import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cluster.ErrDeployDto;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.ClusterDomainPort;
import com.harmonycloud.k8s.bean.cluster.ClusterExternal;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.IcService;
import com.harmonycloud.k8s.service.NodeService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.service.StatefulSetService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.IngressControllerService;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import com.harmonycloud.service.cluster.impl.ClusterServiceImpl;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.NUM_ONE;
import static com.harmonycloud.common.Constant.IngressControllerConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.*;

/**
 * Created by czm on 2017/1/18. jmi 补充
 */
@Service
public class RouterServiceImpl implements RouterService {

    @Autowired
    private ServicesService sService;

    @Autowired
    private HttpSession session;

    @Autowired
    private NodeService nodeService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ConfigMapService configMapService;

    @Autowired
    private NodePortClusterUsageService portClusterUsageService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private StatefulSetService statefulSetService;

    @Autowired
    private StatefulSetsService statefulSetsService;

    @Autowired
    private IcService icService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TenantService tenantService;
    @Autowired
    private IngressControllerService ingressControllerService;
    private static final Logger logger = LoggerFactory.getLogger(RouterServiceImpl.class);
    /**
     * 创建router
     *
     * @param parsedIngressList
     * @return
     */
    @Override
    public ActionReturnUtil ingCreate(ParsedIngressListDto parsedIngressList) throws Exception {
        if (parsedIngressList == null || StringUtils.isBlank(parsedIngressList.getNamespace()) ||
                StringUtils.isBlank(parsedIngressList.getName()) || StringUtils.isBlank(parsedIngressList.getIcName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String icName = parsedIngressList.getIcName();
        String namespace = parsedIngressList.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断集群内是否有相同名称的ingress
        if (checkIngressName(cluster, parsedIngressList.getName())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HTTP_INGRESS_NAME_DUPLICATE);
        }
        //根据icName，检查集群里是否有这个负载均衡器
        IngressControllerDto ingressControllerDto = ingressControllerService.getIngressController(icName, cluster.getId());
        if (ingressControllerDto == null) {
            return ActionReturnUtil.returnErrorWithData(DictEnum.INGRESS_CONTROLLER.phrase(),ErrorCodeMessage.NOT_FOUND);
        }

        ServiceTypeEnum serviceType = null;
        if(StringUtils.isBlank(parsedIngressList.getServiceType())){
            serviceType = ServiceTypeEnum.DEPLOYMENT;
        }else{
            serviceType = ServiceTypeEnum.valueOf(parsedIngressList.getServiceType().toUpperCase());
        }
        //若为有状态服务，且指定了实例名，需要为实例创建单独的service
        String serviceName = null;
        if(serviceType == ServiceTypeEnum.STATEFULSET ){
            if(parsedIngressList.getLabels() != null) {
                parsedIngressList.getLabels().put(Constant.TYPE_STATEFULSET, parsedIngressList.getServiceName());
            }
            if(StringUtils.isNotEmpty(parsedIngressList.getPodName())) {
                serviceName = this.createSvc(parsedIngressList.getPodName(), parsedIngressList.getNamespace(), parsedIngressList.getServiceName(), cluster);
                if (parsedIngressList.getLabels() == null) {
                    parsedIngressList.setLabels(new HashMap<>());
                }
                parsedIngressList.getLabels().put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME, parsedIngressList.getPodName());
                for (HttpRuleDto httpRuleDto : parsedIngressList.getRules()) {
                    httpRuleDto.setService(serviceName);
                }
            }
        }
        Ingress ingress = this.buildIngress(namespace, parsedIngressList, ingressControllerDto);
        Map<String, Object> body = CollectionUtil.transBean2Map(ingress);
        K8SURL url = new K8SURL();
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        url.setNamespace(namespace).setResource(Resource.INGRESS);
        K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.POST, head, body, cluster);
        if (!HttpStatusUtil.isSuccessStatus(k.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(k.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }


        Map<String,Object> labels = new HashMap<String,Object>();
        labels.put(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE,INGRESS_SERVICE_TRUE);
        String name = parsedIngressList.getRules().get(0).getService();
        switch(serviceType){
            case DEPLOYMENT:
                deploymentsService.updateLabels(namespace,name,cluster,labels);
                break;
            case STATEFULSET:
                statefulSetsService.updateLabels(namespace, name, cluster, labels);
                break;
        }
        return ActionReturnUtil.returnSuccessWithData(k.getBody());
    }

    /**
     * 删除HTTP应用网关
     */
    @Override
    public ActionReturnUtil ingDelete(String namespace, String name, String depName, String serviceType) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.INGRESS).setName(name);
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, head, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }

        //当TCP/UDP/HTTP对外服务数量为 0 时，更新对外服务标签为 "ingressFalse"
        updateIngressServiceLabels(namespace, depName, cluster, serviceType);
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }

    @Override
    public ActionReturnUtil svcList(String namespace) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
        List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();
        List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
        if (services != null && !services.isEmpty()) {
            for (int i = 0; i < services.size(); i++) {
                boolean flag = false;
                Map<String, Object> labels = services.get(i).getMetadata().getLabels();
                if (labels != null && !labels.isEmpty()) {
                    for (Map.Entry<String, Object> m : labels.entrySet()) {
                        if (m.getKey().indexOf("nephele") > -1) {
                            flag = true;
                            break;
                        }
                    }
                }

                if (flag) {
                    RouterSvc routerSvc = new RouterSvc();
                    com.harmonycloud.k8s.bean.Service svc = services.get(i);
                    routerSvc.setNamespace(svc.getMetadata().getNamespace());
                    routerSvc.setName(svc.getMetadata().getName());
                    routerSvc.setCreateTime(svc.getMetadata().getCreationTimestamp());
                    Map<String, Object> tMap = new HashMap<String, Object>();
                    for (Map.Entry<String, Object> m : labels.entrySet()) {
                        if (m.getKey().indexOf("nephele") < 0) {
                            tMap.put(m.getKey(), m.getValue());
                        }
                    }
                    routerSvc.setLabels(tMap);
                    routerSvc.setSelector(svc.getSpec().getSelector());
                    routerSvc.setRules(svc.getSpec().getPorts());
                    Map<String, Object> anno = svc.getMetadata().getAnnotations();
                    if (anno != null && !anno.isEmpty()) {
                        if (anno.containsKey("nephele/annotation")
                                && !StringUtils.isEmpty(anno.get("nephele/annotation").toString())) {
                            routerSvc.setAnnotation(anno.get("nephele/annotation").toString());
                        }
                        if (anno.containsKey("nephele/deployment")
                                && !StringUtils.isEmpty(anno.get("nephele/deployment").toString())) {
                            routerSvc.setService(anno.get("nephele/deployment").toString());
                        }
                    }
                    routerSvcs.add(routerSvc);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(routerSvcs);
    }

    @Override
    public void deleteRulesByName(String namespace, String name, List<IngressControllerDto> icList, Cluster cluster) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        AssertUtil.notBlank(name, DictEnum.NAME);
        //获取nginx configmap
        String valuePrefix = namespace + "/" + name;
        for (IngressControllerDto icDto : icList) {
            String icName = icDto.getIcName();
            deleteNginxConfigMap(valuePrefix, icName, cluster, Constant.PROTOCOL_TCP);
            deleteNginxConfigMap(valuePrefix, icName, cluster, Constant.PROTOCOL_UDP);
        }
    }

    private void deleteNginxConfigMap(String valuePrefix, String icName, Cluster cluster, String protocol) throws Exception {
        ConfigMap configMap = getSystemExposeConfigmap(icName, cluster, protocol);
        Map<String, Object> data = (Map<String, Object>) configMap.getData();
        if (null != data) {
            Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
            List<String> externalPorts = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<String, Object> entry = it.next();
                String value = entry.getValue().toString();
                if (value.contains(valuePrefix)) {
                    externalPorts.add(entry.getKey());
                    it.remove();
                }
            }

            //更新configmap
            configMap.setData(data);
            configMapService.updateConfigmap(configMap, cluster);

            //删除数据库端口
            for (String port : externalPorts) {
                portClusterUsageService.deleteNodePortUsage(cluster.getId(), Integer.valueOf(port));
            }
        }
    }

    /**
     * 更新service
     */
    @Override
    public ActionReturnUtil svcUpdate(SvcRouterUpdateDto svcRouterUpdate) throws Exception {
        // 查询
        K8SURL url1 = new K8SURL();
        url1.setNamespace(svcRouterUpdate.getNamespace()).setResource(Resource.SERVICE)
                .setSubpath(svcRouterUpdate.getName());
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        K8SClientResponse serivceResponse = new K8sMachineClient().exec(url1, HTTPMethod.GET, head, null, null);
        com.harmonycloud.k8s.bean.Service service = K8SClient.converToBean(serivceResponse,
                com.harmonycloud.k8s.bean.Service.class);
        String apiVersion = service.getApiVersion();
        String kind = service.getKind();
        ObjectMeta metadata = service.getMetadata();
        // 更新metadata
        Map<String, Object> annotations = metadata.getAnnotations();
        annotations.clear();
        annotations.put("nephele/deployment", svcRouterUpdate.getService());
        Map<String, Object> labels = metadata.getLabels();
        labels.clear();
        List<HttpLabelDto> httpLabels = svcRouterUpdate.getLabels();
        for (HttpLabelDto httpLabel : httpLabels) {
            labels.put(httpLabel.getName(), httpLabel.getValue());
        }
        labels.put("nephele_Type", "HAP");

        ServiceSpec spec = service.getSpec();
        // 更新spec
        ServiceStatus status = service.getStatus();
        // 更新spec.ports
        List<ServicePort> ports = new ArrayList<>();
        List<TcpRuleDto> rules = svcRouterUpdate.getRules();
        for (int i = 0; i < rules.size(); i++) {
            // 判断是否为编辑状态,编辑状态下不更新
            if (rules.get(i).getIsEdit() != null && rules.get(i).getIsEdit() == false) {
                ServicePort port = new ServicePort();
                port.setName(svcRouterUpdate.getName() + "-port" + i);
                port.setPort(Integer.valueOf(rules.get(i).getPort()));
                port.setTargetPort(Integer.valueOf(rules.get(i).getTargetPort()));
                port.setProtocol(rules.get(i).getProtocol());
                ports.add(port);
            }
        }
        // 更新spec.selector
        Map<String, Object> selector = new HashMap<>();
        if (svcRouterUpdate.getSelector() != null && svcRouterUpdate.getSelector().size() > 0) {
            selector.put(svcRouterUpdate.getSelector().get(0).getName(),
                    svcRouterUpdate.getSelector().get(0).getValue());
        }
        spec.setPorts(ports);
        spec.setSelector(selector);
        Map<String, Object> body = new HashMap<>();
        body.put("metadata", metadata);
        body.put("spec", spec);
        body.put("kind", kind);
        body.put("apiVersion", apiVersion);
        body.put("status", status);
        K8SURL url = new K8SURL();
        url.setNamespace(svcRouterUpdate.getNamespace()).setResource(Resource.SERVICE)
                .setSubpath(svcRouterUpdate.getName());
        head.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body, null);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }

    /**
     * 更新HTTP应用网关
     *
     * @param parsedIngressList
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil ingUpdate(ParsedIngressListUpdateDto parsedIngressList) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(parsedIngressList.getNamespace()).setResource(Resource.INGRESS)
                .setName(parsedIngressList.getName());
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        Map<String, Object> body = new HashMap<>();
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        // 处理label
        List<HttpLabelDto> labels = parsedIngressList.getLabels();
        Map<String, Object> label = new HashMap<>();
        Map<String, Object> annotations = new HashMap<>();
        if (labels != null) {
            for (HttpLabelDto httpLabel : labels) {
                label.put(httpLabel.getName(), httpLabel.getValue());
            }
        }
        if (parsedIngressList.getAnnotaion() != null) {
            annotations.put("nephele/annotation", parsedIngressList.getAnnotaion().toString());
            metadata.setAnnotations(annotations);
        }
        metadata.setLabels(label);
        metadata.setName(parsedIngressList.getName());
        metadata.setNamespace(parsedIngressList.getNamespace());
        // 设置spec
        IngressSpec spec = new IngressSpec();
        // 转换为k8s需要的结构
        List<HttpRuleDto> rules = parsedIngressList.getRules();
        List<IngressRule> listRule = new ArrayList<>();
        if (rules != null) {
            for (HttpRuleDto httpRule : rules) {
                // 判断是否为编辑状态,编辑状态下不更新
                if (httpRule.getIsEdit() != null && !httpRule.getIsEdit().equals("true")) {
                    HTTPIngressRuleValue http = new HTTPIngressRuleValue();
                    List<HTTPIngressPath> paths = new ArrayList<>();
                    IngressRule rule = new IngressRule();
                    HTTPIngressPath path = new HTTPIngressPath();
                    IngressBackend backend = new IngressBackend();
                    backend.setServiceName(httpRule.getService());
                    backend.setServicePort(Integer.valueOf(httpRule.getPort()));
                    path.setPath(httpRule.getPath());
                    path.setBackend(backend);
                    paths.add(path);
                    http.setPaths(paths);
                    rule.setHttp(http);
                    rule.setHost(parsedIngressList.getHost());
                    listRule.add(rule);
                }
            }
        }
        spec.setRules(listRule);
        body.put("metadata", metadata);
        body.put("spec", spec);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.PUT, head, body);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());

    }

    @Override
    public ActionReturnUtil getPort(String namespace) throws Exception {
        if (StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Integer port = this.chooseOnePort(cluster);
        NodePortClusterUsage newUsage = new NodePortClusterUsage();
        newUsage.setClusterId(cluster.getId());
        newUsage.setCreateTime(new Date());
        newUsage.setNodeport(port);
        newUsage.setStatus(Constant.EXTERNAL_PORT_STATUS_USED);
        portClusterUsageService.insertNodeportUsage(newUsage);
        return ActionReturnUtil.returnSuccessWithData(port);
    }

    @Override
    public ActionReturnUtil checkPort(String port, String namespace) throws Exception {
        if (StringUtils.isBlank(port) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String clusterId = cluster.getId();
        int externalPort = Integer.valueOf(port);
        NodePortClusterUsage portClusterUsage = portClusterUsageService.selectPortUsageByPort(clusterId, externalPort);
        if (Objects.isNull(portClusterUsage)) {
            return ActionReturnUtil.returnSuccessWithData("false");
        }
        return ActionReturnUtil.returnSuccessWithData("true");
    }

    @Override
    public ActionReturnUtil updatePort(String oldPort, String nowPort, String namespace) throws Exception {
        if (StringUtils.isBlank(oldPort) || StringUtils.isBlank(nowPort) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String clusterId = cluster.getId();
        //根据旧的端口查询出记录
        NodePortClusterUsage oldPortUsage = portClusterUsageService.selectPortUsageByPort(clusterId, Integer.valueOf(oldPort));
        if (Objects.isNull(oldPortUsage)) {
            NodePortClusterUsage newUsage = new NodePortClusterUsage();
            newUsage.setClusterId(clusterId);
            newUsage.setCreateTime(new Date());
            newUsage.setStatus(Constant.EXTERNAL_PORT_STATUS_USED);
            portClusterUsageService.insertNodeportUsage(newUsage);
        } else {
            portClusterUsageService.deleteNodePortUsage(clusterId, Integer.valueOf(oldPort));
            oldPortUsage.setNodeport(Integer.valueOf(nowPort));
            portClusterUsageService.insertNodeportUsage(oldPortUsage);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil delPort(String port, String namespace) throws Exception {
        if (StringUtils.isBlank(port) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String clusterId = cluster.getId();
        portClusterUsageService.deleteNodePortUsage(clusterId, Integer.valueOf(port));
        return ActionReturnUtil.returnSuccess();
    }

    @Deprecated
    @Override
    public List<RouterSvc> listIngressByName(ParsedIngressListDto parsedIngressListDto) throws Exception {
        List<RouterSvc> routerSvcs = new ArrayList<RouterSvc>();
        if (parsedIngressListDto.getNamespace() == null) {
            return routerSvcs;
        }
        if (parsedIngressListDto.getLabels() == null) {
            return routerSvcs;
        }
        String namespace = parsedIngressListDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse response = sService.doServiceByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return routerSvcs;
        }
        ServiceList svList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
        List<com.harmonycloud.k8s.bean.Service> services = svList.getItems();

        if (services != null && !services.isEmpty()) {
            for (int i = 0; i < services.size(); i++) {
                boolean flag = false;
                Map<String, Object> labels = services.get(i).getMetadata().getLabels();
                if (labels != null && !labels.isEmpty()) {
                    for (Map.Entry<String, Object> m : labels.entrySet()) {
                        if (m.getKey().indexOf("nephele") > -1) {
                            flag = true;
                            break;
                        }
                    }
                }

                if (flag) {
                    RouterSvc routerSvc = new RouterSvc();
                    com.harmonycloud.k8s.bean.Service svc = services.get(i);
                    if (svc.getSpec().getSelector().equals(parsedIngressListDto.getLabels())) {
                        routerSvc.setNamespace(svc.getMetadata().getNamespace());
                        routerSvc.setName(svc.getMetadata().getName());
                        routerSvc.setCreateTime(svc.getMetadata().getCreationTimestamp());
                        Map<String, Object> tMap = new HashMap<String, Object>();
                        for (Map.Entry<String, Object> m : labels.entrySet()) {
							/* if (m.getKey().indexOf("nephele") < 0) { */
                            tMap.put(m.getKey(), m.getValue());
							/* } */
                        }
                        routerSvc.setLabels(tMap);
                        routerSvc.setSelector(svc.getSpec().getSelector());
                        routerSvc.setRules(svc.getSpec().getPorts());
                        Map<String, Object> anno = svc.getMetadata().getAnnotations();
                        if (anno != null && !anno.isEmpty()) {
                            if (anno.containsKey("nephele/annotation")
                                    && !StringUtils.isEmpty(anno.get("nephele/annotation").toString())) {
                                routerSvc.setAnnotation(anno.get("nephele/annotation").toString());
                            }
                            if (anno.containsKey("nephele/deployment")
                                    && !StringUtils.isEmpty(anno.get("nephele/deployment").toString())) {
                                routerSvc.setService(anno.get("nephele/deployment").toString());
                            }
                        }
                        routerSvcs.add(routerSvc);
                    }
                }
            }
        }
        return routerSvcs;
    }

    @Override
    public ActionReturnUtil listIngressByName(String namespace, String nameList) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(nameList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String ip = clusterService.getEntry(namespace);
        JSONArray array = new JSONArray();
        List<String> names = new ArrayList<>();
        if (nameList.contains(",")) {
            String[] n = nameList.split(",");
            names = java.util.Arrays.asList(n);
        } else {
            names.add(nameList);
        }
        for (String name : names) {
            // 获取ingress http
            K8SURL url = new K8SURL();
            url.setNamespace(namespace).setResource(Resource.INGRESS);// 资源类型怎么判断
            Map<String, Object> bodys = new HashMap<String, Object>();
            bodys.put("labelSelector", "app=" + name);
            K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (k.getStatus() == Constant.HTTP_404) {
                return ActionReturnUtil.returnSuccess();
            }
            if (!HttpStatusUtil.isSuccessStatus(k.getStatus()) && k.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(k.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            IngressList ingressList = JsonUtil.jsonToPojo(k.getBody(), IngressList.class);
            if (ingressList != null) {
                List<Ingress> list = ingressList.getItems();
                if (list != null && list.size() > 0) {
                    for (Ingress in : list) {
                        JSONObject json = new JSONObject();
                        json.put("name", in.getMetadata().getName());
                        json.put("type", "HTTP");
                        JSONArray ja = new JSONArray();
                        List<IngressRule> rules = in.getSpec().getRules();
                        if (rules != null && rules.size() > 0) {
                            IngressRule rule = rules.get(0);
                            HTTPIngressRuleValue http = rule.getHttp();
                            List<HTTPIngressPath> paths = http.getPaths();
                            if (paths != null && paths.size() > 0) {
                                for (HTTPIngressPath path : paths) {
                                    JSONObject j = new JSONObject();
                                    if (path.getBackend() != null) {
                                        j.put("port", path.getBackend().getServicePort());
                                    }
                                    if (path.getPath().lastIndexOf("/") != path.getPath().length() - 1) {
                                        path.setPath(path.getPath() + "/");
                                    }
                                    j.put("hostname", rule.getHost() + ":30888" + path.getPath());
                                    ja.add(j);
                                }
                            }
                        }
                        json.put("address", ja);
                        array.add(json);
                    }
                }
            }
            // 获取tcp
            ActionReturnUtil tcpRes = svcList(namespace);
            if (!tcpRes.isSuccess()) {
                return tcpRes;
            }
            @SuppressWarnings("unchecked")
            List<RouterSvc> routerSvcs = (List<RouterSvc>) tcpRes.get("data");
            if (routerSvcs != null && routerSvcs.size() > 0) {
                for (RouterSvc routerSvc : routerSvcs) {
                    if (routerSvc.getLabels() != null && routerSvc.getLabels().size() > 0) {
                        Map<String, Object> labels = routerSvc.getLabels();
                        if (labels.get("app") != null && name.equals(labels.get("app").toString())
                                && labels.get("type") != null && "TCP".equals(labels.get("type"))) {
                            JSONObject json = new JSONObject();
                            json.put("name", routerSvc.getName());
                            json.put("type", "TCP");
                            if (routerSvc.getRules() != null) {
                                List<Integer> ports = new ArrayList<Integer>();
                                List<ServicePort> rules = routerSvc.getRules();
                                if (rules.size() > 0) {
                                    JSONArray ja = new JSONArray();
                                    for (ServicePort rule : rules) {
                                        JSONObject j = new JSONObject();
                                        j.put("port", rule.getTargetPort());
                                        j.put("ip", ip + ":" + rule.getPort());
                                        ja.add(j);
                                        ports.add(rule.getPort());
                                    }
                                    json.put("address", ja);
                                    json.put("ports", ports);
                                }
                            }

                            array.add(json);
                        }
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(array);
    }

    @Override
    public List<Ingress> listHttpIngress(String name, String namespace, Cluster cluster) throws Exception {
        List<Ingress> ingresses = new ArrayList<>();
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.INGRESS);
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "app=" + name);
        K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(k.getStatus()) && k.getStatus() != Constant.HTTP_404) {
            throw new Exception("获取ingress失败");
        }
        IngressList ingressList = JsonUtil.jsonToPojo(k.getBody(), IngressList.class);
        if (ingressList != null) {
            return ingressList.getItems();
        }
        return ingresses;
    }

    @Override
    public ConfigMap getSystemExposeConfigmap(String icName, Cluster cluster, String protocolType) throws Exception {
        ConfigMap configMap = new ConfigMap();
        ActionReturnUtil result = new ActionReturnUtil();
        if (StringUtils.isBlank(icName) || icName.equals(IC_DEFAULT_NAME)) {
            if (Constant.PROTOCOL_TCP.equals(protocolType)) {
                result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, EXPOSE_CONFIGMAP_NAME_TCP, HTTPMethod.GET, cluster);
            }
            if (Constant.PROTOCOL_UDP.equals(protocolType)) {
                result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, EXPOSE_CONFIGMAP_NAME_UDP, HTTPMethod.GET, cluster);
            }
        } else {
            if (Constant.PROTOCOL_TCP.equals(protocolType)) {
                result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, "tcp-" + icName, HTTPMethod.GET, cluster);
            }
            if (Constant.PROTOCOL_UDP.equals(protocolType)) {
                result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, "udp-" + icName, HTTPMethod.GET, cluster);
            }
        }
        if (Objects.isNull(result) || !result.isSuccess()) {
            throw new MarsRuntimeException(ErrorCodeMessage.SYSTEM_NGINX_CONFIGMAP_NOT_FIND);
        }
        configMap = (ConfigMap) result.get("data");
        return configMap;
    }

    @Override
    public ActionReturnUtil updateSystemExposeConfigmap(Cluster cluster, String namespace, String service, String icName, List<TcpRuleDto> rules, String protocol) throws Exception {
        ConfigMap configMap = getSystemExposeConfigmap(icName, cluster, protocol);
        Map<String, Object> data = (Map<String, Object>) configMap.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        if (rules != null && rules.size() > 0) {
            for (TcpRuleDto rule : rules) {
                data.put(rule.getPort(), namespace + "/" + service + ":" + rule.getTargetPort());
            }
            //更新configmap
            configMap.setData(data);
            configMapService.updateConfigmap(configMap, cluster);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil listExposedRouterWithIngressAndNginx(String namespace, String nameList, String projectId) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(nameList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //获取cluster
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        //获取F5的IP
        String ip = clusterService.getEntry(namespace);
        String [] names = nameList.split(",");
        List<Map<String, Object>> routerList = new ArrayList<>();
        Project project = projectService.getProjectByProjectId(projectId);
        String tenantId = project.getTenantId();
        Map<String, ConfigMap> icTcpConfigMap = new HashMap<>();
        Map<String, ConfigMap> icUdpConfigMap = new HashMap<>();
        //获取该租户的所有自定义负载均衡器名称
        List<IngressControllerDto> icList = tenantService.getTenantIngressController(tenantId, cluster.getId());
        for (IngressControllerDto ic : icList) {
            //获取负载均衡器的TCP和UDP的端口与服务的映射配置
            String icName = ic.getIcName();
            ConfigMap configMapTcp = getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_TCP);
            ConfigMap configMapUdp = getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_UDP);
            icTcpConfigMap.put(icName, configMapTcp);
            icUdpConfigMap.put(icName, configMapUdp);
        }
        String label = null;
        if (names.length == NUM_ONE) {
            label = "app=" + names[0];
        }
        //查询该分区下的http服务的ingress，如果只查询单个服务ingress，则根据label筛选，如果查询多个ingress，则查询分区下的所有ingress
        List<Ingress> ingresses = icService.listIngress(namespace, label, cluster);
        //将ingress转换成map，key为服务名，值为该服务创建的ingress对象列表
        Map<String,List<Ingress>> ingressMap = new HashMap<>();
        for(Ingress ingress:ingresses){
            Map<String, Object> labels = ingress.getMetadata().getLabels();
            if (labels != null && labels.get("app") != null) {
                String app = labels.get("app").toString();
                if(ingressMap.get(app) == null){
                    List<Ingress> ingressList = new ArrayList<>();
                    ingressList.add(ingress);
                    ingressMap.put(app, ingressList);
                }else {
                    ingressMap.get(app).add(ingress);
                }
            }
        }

        for (int i = 0; i < names.length; i++) {
            Map<String, String> valuePrefixMap = new HashMap<>();
            valuePrefixMap.put(namespace + "/" + names[i], null);
            //statefulSet需要查询单实例的服务
            Map<String, Object> bodys = new HashMap();
            String selectLabel = Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET +CommonConstant.EQUALITY_SIGN + names[i];
            bodys.put(CommonConstant.LABELSELECTOR, selectLabel);
            K8SClientResponse response = sService.doSepcifyService(namespace, null, null, bodys, HTTPMethod.GET, cluster);
            if(HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                ServiceList serviceList = JsonUtil.jsonToPojo(response.getBody(), ServiceList.class);
                if (CollectionUtils.isNotEmpty(serviceList.getItems())) {
                    for (com.harmonycloud.k8s.bean.Service service : serviceList.getItems()) {
                        String valuePrefix = namespace + "/" + service.getMetadata().getName();
                        String podName = null;
                        if (service.getMetadata().getLabels() != null && service.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME) != null) {
                            podName = (String) service.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME);
                        }
                        valuePrefixMap.put(valuePrefix, podName);
                    }
                }
            }
            //获取tcp和udp对外服务，分别对每个负载均衡器检查是否有暴露该服务
            for(String valuePrefix : valuePrefixMap.keySet()) {
                String podName = valuePrefixMap.get(valuePrefix);
                for (IngressControllerDto ic : icList) {
                    String icName = ic.getIcName();
                    routerList.addAll(listServiceRouter(icTcpConfigMap.get(icName), icName, valuePrefix, ip, Constant.PROTOCOL_TCP, podName));
                    routerList.addAll(listServiceRouter(icUdpConfigMap.get(icName), icName, valuePrefix, ip, Constant.PROTOCOL_UDP, podName));
                }
            }
            //获取http对外服务
            List<Ingress> ingressList = ingressMap.get(names[i]);
            if(CollectionUtils.isEmpty(ingressList)){
                continue;
            }
            for(Ingress in : ingressList) {
                routerList.add(this.getIngressRouter(in, icList));
            }
        }
        return ActionReturnUtil.returnSuccessWithData(routerList);
    }

    @Override
    public ActionReturnUtil updateSystemRouteRule(SvcRouterDto svcRouterDto) throws Exception {
        if (Objects.isNull(svcRouterDto) || CollectionUtils.isEmpty(svcRouterDto.getRules()) ||
                StringUtils.isBlank(svcRouterDto.getName()) || StringUtils.isBlank(svcRouterDto.getNamespace()) ||
                StringUtils.isBlank(svcRouterDto.getIcName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        if (!IC_DEFAULT_NAME.equalsIgnoreCase(svcRouterDto.getIcName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.TCP_IC_DEFAULT_ONLY);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(svcRouterDto.getNamespace());
        ServiceTypeEnum serviceType;
        if(StringUtils.isBlank(svcRouterDto.getServiceType())) {
            serviceType = ServiceTypeEnum.DEPLOYMENT;
        }else{
            serviceType = ServiceTypeEnum.valueOf(svcRouterDto.getServiceType().toUpperCase());
        }
        //获取容器内的端口协议
        List<Container> containers = new ArrayList<>();
        switch(serviceType){
            case DEPLOYMENT:
                K8SClientResponse depRes = dpService.doSpecifyDeployment(svcRouterDto.getNamespace(), svcRouterDto.getName(), null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
                    return ActionReturnUtil.returnErrorWithData(depRes.getBody());
                }
                Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
                containers = dep.getSpec().getTemplate().getSpec().getContainers();
                break;
            case STATEFULSET:
                K8SClientResponse stsRes = statefulSetService.doSpecifyStatefulSet(svcRouterDto.getNamespace(), svcRouterDto.getName(), null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(stsRes.getStatus())) {
                    return ActionReturnUtil.returnErrorWithData(stsRes.getBody());
                }
                StatefulSet sts = JsonUtil.jsonToPojo(stsRes.getBody(), StatefulSet.class);
                containers = sts.getSpec().getTemplate().getSpec().getContainers();
                break;
        }


        List<ContainerPort> portAll = new ArrayList<>();
        containers.stream().forEach(c -> { portAll.addAll(c.getPorts()); });
        List<TcpRuleDto> rules = svcRouterDto.getRules();
        String protocol = Constant.PROTOCOL_TCP;
        for (TcpRuleDto rule : rules) {
            //判断端口和协议是否匹配
            boolean isMatch = portAll.stream().anyMatch(port -> String.valueOf(port.getContainerPort()).equals(rule.getTargetPort()) && rule.getProtocol().equals(port.getProtocol()));
            if (!isMatch) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_EXPOSE_NGINX_FAILED);
            }
            //更新数据库
            NodePortClusterUsage usage = new NodePortClusterUsage();
            usage.setNodeport(Integer.valueOf(rule.getPort()));
            usage.setClusterId(cluster.getId());
            usage.setStatus(Constant.EXTERNAL_PORT_STATUS_CONFIRM_USED);
            portClusterUsageService.updateNodePortStatus(usage);
            protocol = rule.getProtocol();
        }
        //若为有状态服务，且指定了实例名，需要为实例创建单独的service
        String serviceName = null;
        if(serviceType == ServiceTypeEnum.STATEFULSET && StringUtils.isNotEmpty(svcRouterDto.getPodName())){
            serviceName = this.createSvc(svcRouterDto.getPodName(), svcRouterDto.getNamespace(), svcRouterDto.getName(), cluster);
        }else{
            serviceName = svcRouterDto.getName();
        }
        this.updateSystemExposeConfigmap(cluster, svcRouterDto.getNamespace(), serviceName, svcRouterDto.getIcName(), rules, protocol);
        Map<String,Object> labels = new HashMap<String,Object>();
        labels.put(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE, INGRESS_SERVICE_TRUE);
        switch(serviceType) {
            case DEPLOYMENT:
                deploymentsService.updateLabels(svcRouterDto.getNamespace(), svcRouterDto.getName(), cluster, labels);
                break;
            case STATEFULSET:
                statefulSetsService.updateLabels(svcRouterDto.getNamespace(), svcRouterDto.getName(), cluster, labels);
                break;
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteSystemRouteRule(TcpDeleteDto tcpDeleteDto, String deployName) throws Exception {
        if (Objects.isNull(tcpDeleteDto) || CollectionUtils.isEmpty(tcpDeleteDto.getPorts()) || StringUtils.isBlank(tcpDeleteDto.getNamespace()) || StringUtils.isBlank(tcpDeleteDto.getProtocol())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String namespace = tcpDeleteDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        //获取configmap后，删除对应的规则
        ConfigMap configMap = this.getSystemExposeConfigmap(tcpDeleteDto.getIcName() ,cluster, tcpDeleteDto.getProtocol());
        List<Integer> externalPorts = tcpDeleteDto.getPorts();
        Map<String, Object> data = (Map<String, Object>) configMap.getData();
        Iterator<Map.Entry<String, Object>> it = data.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            String key = entry.getKey();
            if (externalPorts.contains(Integer.valueOf(key))) {
                it.remove();
            }
        }

        //更新configmap
        configMap.setData(data);
        configMapService.updateConfigmap(configMap, cluster);

        //删除数据库端口
        for (Integer port : externalPorts) {
            portClusterUsageService.deleteNodePortUsage(cluster.getId(), port);
        }
        //当TCP/UDP/HTTP对外服务数量为 0 时，更新对外服务标签为 "ingressFalse"
        updateIngressServiceLabels(namespace, deployName, cluster, tcpDeleteDto.getServiceType());
        List<Map<String, Object>> routerList = new ArrayList<>();
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 获取指定服务下的TCP或UDP的路由
     *
     * @param configMap
     * @param valuePrefix
     * @param ip
     * @return List<Map<String, Object>>
     * @throws Exception
     */
    private List<Map<String, Object>> listServiceRouter(ConfigMap configMap, String icName, String valuePrefix, String ip, String type, String podName) throws Exception {
        List<Map<String, Object>> routerList = new ArrayList<>();
        Map<String, Object> configMapData = (Map<String, Object>) configMap.getData();
        if (Objects.nonNull(configMapData)) {
            for (Map.Entry<String, Object> entry : configMapData.entrySet()) {
                String value = entry.getValue().toString();
                String[] valueArray = value.split(CommonConstant.COLON);
                if (Objects.nonNull(valueArray) && valueArray.length == CommonConstant.NUM_TWO && valuePrefix.equals(valueArray[0])) {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("icName", icName);
                    tmp.put("type", type);
                    Map<String, Object> address = new HashMap<>();
                    String containerPort = valueArray[NUM_ONE];
                    address.put("containerPort", containerPort);
                    address.put("externalPort", entry.getKey());
                    address.put("ip", ip);
                    tmp.put("address", address);
                    if(StringUtils.isNotEmpty(podName)) {
                        tmp.put("podName", podName);
                    }
                    routerList.add(tmp);
                }
            }
        }
        return routerList;
    }

    private void updateIngressServiceLabels(String namespace, String name, Cluster cluster, String serviceType) throws Exception {
        //当TCP/UDP/HTTP对外服务数量为 0 时，更新对外服务标签为 "ingressFalse"
        List<Map<String, Object>> routerList = new ArrayList<>();
        String ip = clusterService.getEntry(namespace);
        String valuePrefix = namespace + "/" + name;
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(namespace);
        //通过tenantId找icName
        List<IngressControllerDto> icList = tenantService.getTenantIngressController(namespaceLocal.getTenantId(), cluster.getId());
        for (IngressControllerDto ic : icList) {
            //获取nginx对应的upd和tcp configmap
            String icName = ic.getIcName();
            ConfigMap configMapTcp = getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_TCP);
            ConfigMap configMapUdp = getSystemExposeConfigmap(icName, cluster, Constant.PROTOCOL_UDP);
            routerList.addAll(listServiceRouter(configMapTcp, icName, valuePrefix, ip, Constant.PROTOCOL_TCP, null));
            routerList.addAll(listServiceRouter(configMapUdp, icName, valuePrefix, ip, Constant.PROTOCOL_UDP, null));
        }

        if(routerList.size() == 0){
            K8SURL url2 = new K8SURL();
            url2.setNamespace(namespace).setResource(Resource.INGRESS);
            Map<String, Object> bodys = new HashMap<String, Object>();
            bodys.put("labelSelector", "app=" + name);
            K8SClientResponse k = new K8sMachineClient().exec(url2, HTTPMethod.GET, null, bodys, cluster);
            IngressList ingressList = JsonUtil.jsonToPojo(k.getBody(), IngressList.class);
            List<Ingress> list = ingressList.getItems();
            if(list.size() == 0){
                Map<String,Object> labels = new HashMap<String,Object>();
                labels.put(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE,INGRESS_SERVICE_FALSE);
                ServiceTypeEnum serviceTypeEnum = null;
                if(StringUtils.isBlank(serviceType)){
                    serviceTypeEnum = ServiceTypeEnum.DEPLOYMENT;
                }else {
                    serviceTypeEnum = ServiceTypeEnum.valueOf(serviceType.toUpperCase());
                }
                switch(serviceTypeEnum){
                    case DEPLOYMENT:
                        deploymentsService.updateLabels(namespace,name,cluster,labels);
                        break;
                    case STATEFULSET:
                        statefulSetsService.updateLabels(namespace, name, cluster, labels);
                        break;
                }
            }
        }
    }

    @Override
    public Integer chooseOnePort(Cluster cluster) throws Exception {
        //从集群内获取端口范围
        Map<String, Integer> range = this.getPortRange(null, cluster);
        Integer minPort = range.get("minPort");
        Integer maxPort = range.get("maxPort");
        Integer resultPort = 0;
        List<NodePortClusterUsage> portClusterUsageList = portClusterUsageService.selectPortUsageByClusterId(cluster.getId());

        //当集群已经有使用过端口，则选择出一个未使用的
        if (CollectionUtils.isNotEmpty(portClusterUsageList)) {
            List<Integer> usagedPortList = new ArrayList<>();
            portClusterUsageList.forEach(port -> {
                usagedPortList.add(port.getNodeport());
            });
            Integer currentPort = minPort;
            while (minPort <= maxPort) {
                if (!usagedPortList.contains(currentPort)) {
                    resultPort = currentPort;
                    break;
                }
                currentPort ++;
            }
            if (currentPort > maxPort) {
                throw new MarsRuntimeException(ErrorCodeMessage.SYSTEM_NO_EXTERNAL_PORT_IN_CLUSTER);
            }
        } else {
            resultPort = minPort;
        }
        return resultPort;
    }

    @Override
    public Map<String, Integer> getPortRange(String namespace, Cluster cluster) throws Exception {
        Cluster newCluster = cluster;
        if (Objects.isNull(cluster) && StringUtils.isNotBlank(namespace)) {
            newCluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            if (Objects.isNull(newCluster)) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
        }
        //从集群内获取端口范围
        List<ClusterExternal> clusterExternals = newCluster.getExternal();
        Integer minPort = 0;
        Integer maxPort = 0;
        for (ClusterExternal external : clusterExternals) {
            minPort = external.getMinPort();
            maxPort = external.getMaxPort();
        }
        if (minPort >= maxPort) {
            throw new MarsRuntimeException(ErrorCodeMessage.SYSTEM_NO_EXTERNAL_PORT_IN_CLUSTER);
        }
        Map<String, Integer> result = new HashMap<>();
        result.put("minPort", minPort);
        result.put("maxPort", maxPort);
        return result;
    }

    @Override
    public ActionReturnUtil createRuleInDeploy(SvcRouterDto svcRouterDto) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(svcRouterDto.getNamespace());
        List<TcpRuleDto> rules = svcRouterDto.getRules();
        for (TcpRuleDto rule : rules) {
            Integer port = StringUtils.isBlank(rule.getPort()) ? this.chooseOnePort(cluster) : Integer.valueOf(rule.getPort());
            NodePortClusterUsage newUsage = new NodePortClusterUsage();
            newUsage.setClusterId(cluster.getId());
            newUsage.setCreateTime(new Date());
            newUsage.setNodeport(port);
            newUsage.setStatus(Constant.EXTERNAL_PORT_STATUS_CONFIRM_USED);
            portClusterUsageService.insertNodeportUsage(newUsage);
            rule.setPort(String.valueOf(port));
            this.updateSystemExposeConfigmap(cluster, svcRouterDto.getNamespace(), svcRouterDto.getName(), svcRouterDto.getIcName(), Arrays.asList(rule), rule.getProtocol());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public List<Map<String, Object>> createExternalRule(ServiceTemplateDto svcTemplate, String namespace, String serviceType) throws Exception {
        List<Map<String, Object>> message = new ArrayList<>();
        for (IngressDto ingress : svcTemplate.getIngress()) {
            if (Constant.PROTOCOL_HTTP.equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
                Map<String, Object> labels = new HashMap<String, Object>();
                if(Constant.STATEFULSET.equalsIgnoreCase(serviceType)){
                    labels.put("app", svcTemplate.getStatefulSetDetail().getName());
                }else {
                    labels.put("app", svcTemplate.getDeploymentDetail().getName());
                }
                labels.put("tenantId", svcTemplate.getTenantId());
                ingress.getParsedIngressList().setLabels(labels);
                ingress.getParsedIngressList().setNamespace(namespace);
                ingress.getParsedIngressList().setServiceType(serviceType);
                ActionReturnUtil httpIngRes = this.ingCreate(ingress.getParsedIngressList());
                if (!httpIngRes.isSuccess()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ingress:" + ingress.getParsedIngressList().getName(), httpIngRes.get("data"));
                    message.add(map);
                }
            }
            if ((ingress.getType().contains(Constant.PROTOCOL_TCP)|| ingress.getType().contains(Constant.PROTOCOL_UDP)) && !StringUtils.isEmpty(ingress.getSvcRouter().getApp())) {
                ingress.getSvcRouter().setNamespace(namespace);
                ingress.getSvcRouter().setName(ingress.getSvcRouter().getApp());
                ingress.getSvcRouter().setServiceType(serviceType);
                ActionReturnUtil tcpSvcRes = this.createRuleInDeploy(ingress.getSvcRouter());
                if (!tcpSvcRes.isSuccess()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("ingress:" + ingress.getParsedIngressList().getName(), tcpSvcRes.get("data"));
                    message.add(map);
                }
            }
        }
        return message;
    }

    /**
     * 检查在一个集群内是否有相同的
     * @param cluster
     * @param name
     * @return
     * @throws Exception
     */
    @Override
    public boolean checkIngressName(Cluster cluster, String name) throws Exception {
        K8SURL url = new K8SURL().setResource(Resource.INGRESS);
        K8SClientResponse checkRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(checkRes.getStatus())) {
            throw new MarsRuntimeException(checkRes.getBody());
        }
        IngressList ingressList = K8SClient.converToBean(checkRes, IngressList.class);
        List<Ingress> ingresses = ingressList.getItems();
        if (Objects.nonNull(ingresses)) {
            boolean isExist = ingresses.stream().anyMatch(ing -> ing.getMetadata().getName().equals(name));
            return isExist;
        }
        return false;
    }

    /**
     * 根据ingress信息以及对应的负载均衡器组装http对外服务的map形式的信息，
     * @param ingress http对外服务ingress
     * @param icList 负载均衡器列表
     * @return
     */
    private Map<String, Object> getIngressRouter(Ingress ingress, List<IngressControllerDto> icList) {
        Map<String, Object> httpSvc = new HashMap<>();
        httpSvc.put("name", ingress.getMetadata().getName());
        String icName = IC_DEFAULT_NAME;
        //获取ingress使用的负载均衡器名称
        if (ingress.getMetadata().getLabels() != null && ingress.getMetadata().getLabels().get(LABEL_INGRESS_CLASS) != null) {
            icName = ingress.getMetadata().getLabels().get(LABEL_INGRESS_CLASS).toString();
        }
        //若暴露单个实例，获取实例名
        if (ingress.getMetadata().getLabels() != null && ingress.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME) != null) {
            httpSvc.put("podName", ingress.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME));
        }
        httpSvc.put("icName", icName);
        httpSvc.put("type", PROTOCOL_HTTP);
        //获取负载均衡器的端口
        String port = IC_DEFAULT_PORT;
        for (IngressControllerDto ic : icList) {
            if (icName.equals(ic.getIcName())) {
                port = String.valueOf(ic.getHttpPort());
                break;
            }
        }
        List<Map<String, Object>> addressList = new ArrayList<>();
        httpSvc.put("address", addressList);
        List<IngressRule> rules = ingress.getSpec().getRules();
        if (CollectionUtils.isEmpty(rules)) {
            return httpSvc;
        }
        Map<String, Object> annotation = ingress.getMetadata().getAnnotations();
        ClusterDomainPort domainPort = Objects.nonNull(annotation) && annotation.containsKey(Constant.INGRESS_MULTIPLE_PORT_ANNOTATION)?
                JsonUtil.jsonToPojo(annotation.get(Constant.INGRESS_MULTIPLE_PORT_ANNOTATION).toString(), ClusterDomainPort.class) : null;
        //一个ingress只有一个规则
        IngressRule rule = rules.get(0);
        HTTPIngressRuleValue http = rule.getHttp();
        List<HTTPIngressPath> paths = http.getPaths();
        //根据ingress的规则组装访问的http url 地址， 包括域名+服务路径和端口
        if (CollectionUtils.isNotEmpty(paths)) {
            for (HTTPIngressPath path : paths) {
                Map<String, Object> j = new HashMap<>();
                if (path.getBackend() != null) {
                    j.put("port", path.getBackend().getServicePort());
                }
                if (StringUtils.isNotBlank(path.getPath())) {
                    if (path.getPath().lastIndexOf(CommonConstant.SLASH) != path.getPath().length() - NUM_ONE) {
                        path.setPath(path.getPath());
                    }
                    if (CommonConstant.SLASH.equals(path.getPath())) {
                        path.setPath("");
                    }
                }
                //设置为外网访问port
                if (Objects.nonNull(domainPort)) {
                    port = String.valueOf(domainPort.getPort());
                    httpSvc.put("type", domainPort.getProtocol());
                }
                //判断域名类型
                String host = rule.getHost();
                String hostName = port.equals(IC_DEFAULT_PORT) ? host : host + CommonConstant.COLON + port;
                hostName = StringUtils.isNotBlank(path.getPath()) ? hostName + path.getPath() : hostName;
                j.put("hostname", hostName);
                addressList.add(j);
            }
        }
        httpSvc.put("address", addressList);
        return httpSvc;
    }

    private Ingress buildIngress(String namespace, ParsedIngressListDto parsedIngressList,
                                 IngressControllerDto ingressControllerDto){
        Ingress ingress = new Ingress();
        ingress.setMetadata(new ObjectMeta());
        ingress.setSpec(new IngressSpec());

        Map<String, Object> annotation = new HashMap<String, Object>();
        if (parsedIngressList.getAnnotation() != null) {
            annotation.put("nephele/annotation", parsedIngressList.getAnnotation());
        }
        String icName = ingressControllerDto.getIcName();
        if (!IC_DEFAULT_NAME.equals(icName)) {
            annotation.put(LABEL_INGRESS_CLASS, icName);
            parsedIngressList.getLabels().put(LABEL_INGRESS_CLASS, icName);
        }
        ClusterDomainPort domainPort = new ClusterDomainPort();
        domainPort.setPort(Integer.valueOf(parsedIngressList.getExposePort()));
        domainPort.setProtocol(parsedIngressList.getProtocol());
        domainPort.setExternal(parsedIngressList.getExternal());
        annotation.put(Constant.INGRESS_MULTIPLE_PORT_ANNOTATION, JsonUtil.convertToJson(domainPort));
        ingress.getMetadata().setAnnotations(annotation);
        List<HttpRuleDto> rules = parsedIngressList.getRules();

        List<HTTPIngressPath> path = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(rules)) {
            for (HttpRuleDto rule : rules) {
                HTTPIngressPath p = new HTTPIngressPath();
                IngressBackend backend = new IngressBackend();
                backend.setServiceName(rule.getService());
                backend.setServicePort(Integer.valueOf(rule.getPort()));
                p.setBackend(backend);
                p.setPath(rule.getPath());
                path.add(p);
            }
        }
        ingress.getMetadata().setLabels(parsedIngressList.getLabels());
        ingress.getMetadata().setNamespace(namespace);

        IngressRule ingressRule = new IngressRule();
        ingressRule.setHost(parsedIngressList.getHost());
        HTTPIngressRuleValue http = new HTTPIngressRuleValue();
        http.setPaths(path);
        ingressRule.setHttp(http);
        ingress.getSpec().setRules(new ArrayList<>());
        ingress.getSpec().getRules().add(ingressRule);

        ingress.getMetadata().setName(parsedIngressList.getName());
        return ingress;
    }

    /**
     * 为单个实例创建服务
     * @param podName
     * @param namespace
     * @param serviceName
     * @param cluster
     * @return
     * @throws Exception
     */
    private String createSvc(String podName, String namespace, String serviceName, Cluster cluster) throws Exception {
        String newServiceName = podName + CommonConstant.LINE + RandomNum.getRandomString(CommonConstant.NUM_EIGHT);

        Map bodys= new HashMap();
        String selectLabel = Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME +CommonConstant.EQUALITY_SIGN + podName;
        bodys.put(CommonConstant.LABELSELECTOR, selectLabel);
        K8SClientResponse getRes = sService.doSepcifyService(namespace, null, null, bodys, HTTPMethod.GET, cluster);
        if(HttpStatusUtil.isSuccessStatus(getRes.getStatus())){
            ServiceList serviceList = JsonUtil.jsonToPojo(getRes.getBody(), ServiceList.class);
            if(CollectionUtils.isNotEmpty(serviceList.getItems())){
                return serviceList.getItems().get(0).getMetadata().getName();
            }
        }
        K8SClientResponse response = sService.doSepcifyService(namespace, serviceName, null, null, HTTPMethod.GET, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())){
            throw new MarsRuntimeException();
        }
        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(response.getBody(), com.harmonycloud.k8s.bean.Service.class);

        com.harmonycloud.k8s.bean.Service createService = new com.harmonycloud.k8s.bean.Service();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(newServiceName);
        objectMeta.setNamespace(namespace);
        Map label = new HashMap();
        label.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME, podName);
        label.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET, serviceName);
        objectMeta.setLabels(label);
        createService.setMetadata(objectMeta);
        ServiceSpec spec = new ServiceSpec();
        Map selector = new HashMap();
        selector.put(CommonConstant.LABEL_STATEFULSET_POD, podName);
        spec.setSelector(selector);
        spec.setPorts(service.getSpec().getPorts());
        createService.setSpec(spec);
        Map<String, Object> createBodys = CollectionUtil.transBean2Map(createService);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse createRes = sService.doSepcifyService(namespace, null, headers, createBodys, HTTPMethod.POST, cluster);
        if(!HttpStatusUtil.isSuccessStatus(createRes.getStatus())){

        }
        return newServiceName;
    }

    @Override
    public ErrDeployDto transferRuleDeploy(SvcRouterDto svcRouterDto,String deployName) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(svcRouterDto.getNamespace());
        List<TcpRuleDto> rules = svcRouterDto.getRules();
        for (TcpRuleDto rule : rules) {
            Integer port = StringUtils.isBlank(rule.getPort()) ? this.chooseOnePort(cluster) : Integer.valueOf(rule.getPort());
            NodePortClusterUsage newUsage = new NodePortClusterUsage();
            newUsage.setClusterId(cluster.getId());
            newUsage.setCreateTime(new Date());
            newUsage.setNodeport(port);
            newUsage.setStatus(Constant.EXTERNAL_PORT_STATUS_CONFIRM_USED);
            portClusterUsageService.insertNodeportUsage(newUsage);
            rule.setPort(String.valueOf(port));
            ErrDeployDto errDeployDto = this.transferSystemExposeConfigmap(cluster, svcRouterDto.getNamespace(), svcRouterDto.getName(), svcRouterDto.getIcName(), Arrays.asList(rule), rule.getProtocol(),deployName);
            if (errDeployDto != null) {
                return errDeployDto;
            }
        }
        return null;
    }

    public ErrDeployDto transferSystemExposeConfigmap(Cluster cluster, String namespace, String service, String icName,
                                                      List<TcpRuleDto> rules, String protocol,String deployName) throws Exception {
        ConfigMap configMap = getSystemExposeConfigmap(icName, cluster, protocol);
        Map<String, Object> data = (Map<String, Object>) configMap.getData();
        if (data == null) {
            data = new HashMap<>();
        }
        if (CollectionUtils.isNotEmpty(rules)) {
            for (TcpRuleDto rule : rules) {
                data.put(rule.getPort(), namespace + "/" + service + ":" + rule.getTargetPort());
            }
            //更新configmap
            configMap.setData(data);
            return configMapService.transferConfigmap(configMap, cluster,deployName);
        }
        return null;
    }

    @Override
    public ErrDeployDto transferIngressCreate(ParsedIngressListDto parsedIngressList, DeploymentTransferDto deploymentTransferDto ,Cluster sourceCluster) throws Exception {
        ErrDeployDto err = new ErrDeployDto();
        if (parsedIngressList == null || StringUtils.isBlank(parsedIngressList.getNamespace()) ||
                StringUtils.isBlank(parsedIngressList.getName()) || StringUtils.isBlank(parsedIngressList.getIcName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String icName = parsedIngressList.getIcName();
        //获取旧的ingress
        List<Ingress> list = this.listHttpIngress(parsedIngressList.getServiceName(), deploymentTransferDto.getCurrentNameSpace(), sourceCluster);
        if (list != null && !list.isEmpty()){
            Map<String, Object> annotations = list.get(0).getMetadata().getAnnotations();
            Map params = JsonUtil.convertJsonToMap(annotations.get(INGRESS_MULTIPLE_PORT_ANNOTATION).toString());
            parsedIngressList.setExposePort(params.get("port").toString());
            parsedIngressList.setProtocol((String) params.get("protocol"));
            parsedIngressList.setExternal((Boolean) params.get("external"));
        }
        String namespace = parsedIngressList.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //判断集群内是否有相同名称的ingress
        if (checkIngressName(cluster, parsedIngressList.getName())) {
            err.setDeployName(deploymentTransferDto.getCurrentDeployName());
            err.setErrMsg(ErrorCodeMessage.HTTP_INGRESS_NAME_DUPLICATE.getReasonChPhrase());
            return err;
        }
        //根据icName，检查集群里是否有这个负载均衡器
        IngressControllerDto ingressControllerDto = ingressControllerService.getIngressController(icName, cluster.getId());
        if (ingressControllerDto == null) {
            err.setDeployName(deploymentTransferDto.getCurrentDeployName());
            err.setErrMsg("Ingress-controller资源不存在！");
            return err;
        }

        ServiceTypeEnum serviceType = null;
        if(StringUtils.isBlank(parsedIngressList.getServiceType())){
            serviceType = ServiceTypeEnum.DEPLOYMENT;
        }else{
            serviceType = ServiceTypeEnum.valueOf(parsedIngressList.getServiceType().toUpperCase());
        }
        //若为有状态服务，且指定了实例名，需要为实例创建单独的service
        String serviceName = null;
        if(serviceType == ServiceTypeEnum.STATEFULSET ){
            if(parsedIngressList.getLabels() != null) {
                parsedIngressList.getLabels().put(Constant.TYPE_STATEFULSET, parsedIngressList.getServiceName());
            }
            if(StringUtils.isNotEmpty(parsedIngressList.getPodName())) {
                serviceName = this.createSvc(parsedIngressList.getPodName(), parsedIngressList.getNamespace(), parsedIngressList.getServiceName(), cluster);
                if (parsedIngressList.getLabels() == null) {
                    parsedIngressList.setLabels(new HashMap<>());
                }
                parsedIngressList.getLabels().put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_PODNAME, parsedIngressList.getPodName());
                for (HttpRuleDto httpRuleDto : parsedIngressList.getRules()) {
                    httpRuleDto.setService(serviceName);
                }
            }
        }
        Ingress ingress = this.buildIngress(namespace, parsedIngressList, ingressControllerDto);
        Map<String, Object> body = CollectionUtil.transBean2Map(ingress);
        K8SURL url = new K8SURL();
        Map<String, Object> head = new HashMap<String, Object>();
        head.put("Content-Type", "application/json");
        url.setNamespace(namespace).setResource(Resource.INGRESS);
        K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.POST, head, body, cluster);
        if (!HttpStatusUtil.isSuccessStatus(k.getStatus())) {
            err.setDeployName(deploymentTransferDto.getCurrentDeployName());
            err.setErrMsg("Ingress创建失败");
            return err;
        }


        Map<String,Object> labels = new HashMap<String,Object>();
        labels.put(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE,INGRESS_SERVICE_TRUE);
        String name = parsedIngressList.getRules().get(0).getService();
        switch(serviceType){
            case DEPLOYMENT:
                deploymentsService.updateLabels(namespace,name,cluster,labels);
                break;
            case STATEFULSET:
                statefulSetsService.updateLabels(namespace, name, cluster, labels);
                break;
        }
        return null;
    }
}
