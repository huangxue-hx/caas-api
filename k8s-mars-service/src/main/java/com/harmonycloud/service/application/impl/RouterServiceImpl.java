package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.NodePortClusterUsage;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.ClusterDomain;
import com.harmonycloud.k8s.bean.cluster.ClusterExternal;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.NodeService;
import com.harmonycloud.k8s.service.ServicesService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.RouterService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import com.harmonycloud.service.platform.bean.RouterSvc;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * Created by czm on 2017/1/18. jmi 补充
 */
@Service
public class RouterServiceImpl implements RouterService {

    @Autowired
    private ServicesService sService;

    @Autowired
    HttpSession session;

    @Autowired
    NodeService nodeService;
    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    private ConfigMapService configMapService;

    @Autowired
    private NodePortClusterUsageService portClusterUsageService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private DeploymentService dpService;

    /**
     * 创建router
     *
     * @param parsedIngressList
     * @return
     */
    @Override
    public ActionReturnUtil ingCreate(ParsedIngressListDto parsedIngressList) throws Exception {
        if (parsedIngressList == null || StringUtils.isBlank(parsedIngressList.getNamespace()) || StringUtils.isBlank(parsedIngressList.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String namespace = parsedIngressList.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断集群内是否有相同名称的ingress
        if (checkIngressName(cluster, parsedIngressList.getName())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HTTP_INGRESS_NAME_DUPLICATE);
        }
        Ingress ingress = new Ingress();
        ingress.setMetadata(new ObjectMeta());
        ingress.setSpec(new IngressSpec());

        Map<String, Object> anno = new HashMap<String, Object>();
        if (parsedIngressList.getAnnotaion() != null) {
            anno.put("nephele/annotation", parsedIngressList.getAnnotaion());
            ingress.getMetadata().setAnnotations(anno);
        }
        List<HttpRuleDto> rules = parsedIngressList.getRules();

        Map<String, Object> la = new HashMap<>();
        List<HTTPIngressPath> path = new ArrayList<>();
        String name = "";
        if (rules != null && rules.size() > 0) {
            for (HttpRuleDto rule : rules) {
                name = rule.getService();
                HTTPIngressPath p = new HTTPIngressPath();
                IngressBackend backend = new IngressBackend();
                backend.setServiceName(rule.getService());
                backend.setServicePort(Integer.valueOf(rule.getPort()));
                p.setBackend(backend);
                p.setPath(rule.getPath());
                path.add(p);
            }
        }
        la.put("app", name);
        ingress.getMetadata().setLabels(la);
        ingress.getMetadata().setNamespace(namespace);

        IngressRule ingressRule = new IngressRule();
        ingressRule.setHost(parsedIngressList.getHost());
        HTTPIngressRuleValue http = new HTTPIngressRuleValue();
        http.setPaths(path);
        ingressRule.setHttp(http);
        ingress.getSpec().setRules(new ArrayList<>());
        ingress.getSpec().getRules().add(ingressRule);

        ingress.getMetadata().setName(parsedIngressList.getName());
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
        return ActionReturnUtil.returnSuccessWithData(k.getBody());
    }

    /**
     * 删除HTTP应用网关
     */
    @Override
    public ActionReturnUtil ingDelete(String namespace, String name) throws Exception {
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
    public void deleteRulesByName(String namespace, String name, Cluster cluster) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        AssertUtil.notBlank(name, DictEnum.NAME);
        //获取nginx configmap
        String valuePrefix = namespace + "/" + name;
        deleteNginxConfigMap(valuePrefix, cluster, Constant.PROTOCOL_TCP);
        deleteNginxConfigMap(valuePrefix, cluster, Constant.PROTOCOL_UDP);
    }

    private void deleteNginxConfigMap(String valuePrefix, Cluster cluster, String protocol) throws Exception {
        ConfigMap configMap = getSystemExposeConfigmap(cluster, protocol);
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
    public ConfigMap getSystemExposeConfigmap(Cluster cluster, String protocolType) throws Exception {
        ConfigMap configMap = new ConfigMap();
        ActionReturnUtil result = new ActionReturnUtil();
        if (Constant.PROTOCOL_TCP.equals(protocolType)) {
            result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, Constant.EXPOSE_CONFIGMAP_NAME_TCP, HTTPMethod.GET, cluster);
        }
        if (Constant.PROTOCOL_UDP.equals(protocolType)) {
            result = configMapService.getConfigMapByName(CommonConstant.KUBE_SYSTEM, Constant.EXPOSE_CONFIGMAP_NAME_UDP, HTTPMethod.GET, cluster);
        }
        if (Objects.isNull(result) || !result.isSuccess()) {
            throw new MarsRuntimeException(ErrorCodeMessage.SYSTEM_NGINX_CONFIGMAP_NOT_FIND);
        }
        configMap = (ConfigMap) result.get("data");
        return configMap;
    }

    @Override
    public ActionReturnUtil updateSystemExposeConfigmap(Cluster cluster, String namespace, String service, List<TcpRuleDto> rules, String protocol) throws Exception {
        ConfigMap configMap = getSystemExposeConfigmap(cluster, protocol);
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
    public ActionReturnUtil listExposedRouterWithIngressAndNginx(String namespace, String nameList) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(nameList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //获取cluster
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        //获取F5的IP
        String ip = clusterService.getEntry(namespace);

        String [] names = nameList.split(",");
        List<Map<String, Object>> routerList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            //获取nginx对应的upd和tcp configmap
            ConfigMap configMapTcp = getSystemExposeConfigmap(cluster, Constant.PROTOCOL_TCP);
            ConfigMap configMapUdp = getSystemExposeConfigmap(cluster, Constant.PROTOCOL_UDP);
            String valuePrefix = namespace + "/" + names[i];
            routerList.addAll(listServiceRouter(configMapTcp, valuePrefix, ip, Constant.PROTOCOL_TCP));
            routerList.addAll(listServiceRouter(configMapUdp, valuePrefix, ip, Constant.PROTOCOL_UDP));

            //获取http ingress
            K8SURL url = new K8SURL();
            url.setNamespace(namespace).setResource(Resource.INGRESS);
            Map<String, Object> bodys = new HashMap<String, Object>();
            bodys.put("labelSelector", "app=" + names[i]);
            K8SClientResponse k = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (Constant.HTTP_404 == k.getStatus()) {
                return ActionReturnUtil.returnSuccessWithData(routerList);
            }
            if (!HttpStatusUtil.isSuccessStatus(k.getStatus()) && k.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(k.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            IngressList ingressList = JsonUtil.jsonToPojo(k.getBody(), IngressList.class);
            List<Ingress> list = ingressList.getItems();
            if (CollectionUtils.isNotEmpty(list)) {
                //获取域名
                List<ClusterDomain> domains = clusterService.findDomain(namespace);
                for (Ingress in : list) {
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("name", in.getMetadata().getName());
                    tmp.put("type", "HTTP");
                    List<Map<String, Object>> addressList = new ArrayList<>();
                    List<IngressRule> rules = in.getSpec().getRules();
                    if (CollectionUtils.isNotEmpty(rules)) {
                        IngressRule rule = rules.get(0);
                        HTTPIngressRuleValue http = rule.getHttp();
                        List<HTTPIngressPath> paths = http.getPaths();
                        if (CollectionUtils.isNotEmpty(paths)) {
                            for (HTTPIngressPath path : paths) {
                                Map<String, Object> j = new HashMap<>();
                                if (path.getBackend() != null) {
                                    j.put("port", path.getBackend().getServicePort());
                                }
                                path.setPath("/");
                                if (org.apache.commons.lang.StringUtils.isNotBlank(path.getPath())) {
                                    if (path.getPath().lastIndexOf("/") != path.getPath().length() - 1) {
                                        path.setPath(path.getPath() + "/");
                                    }
                                }
                                //判断域名类型
                                String host = rule.getHost();
                                String[] domainLevels = host.indexOf(CommonConstant.DOT) > -1 ? host.split(CommonConstant.DOT) : null;
                                Integer port = Constant.LIVENESS_PORT;
                                if (domainLevels != null) {
                                    String domainLevelName = (Constant.DOMAIN_LEVEL_FOUR == domainLevels.length) ? Constant.CLUSTER_FOUR_DOMAIN : Constant.CLUSTER_THREE_DOMAIN;
                                    for (ClusterDomain clusterDomain : domains) {
                                        if (domainLevelName.equals(clusterDomain.getDomain())) {
                                            port = clusterDomain.getPort();
                                            break;
                                        }
                                    }
                                }
                                j.put("hostname", rule.getHost() + CommonConstant.COLON + port + path.getPath());
                                addressList.add(j);
                            }
                        }
                    }
                    tmp.put("address", addressList);
                    routerList.add(tmp);
                }
            }
        }

        return ActionReturnUtil.returnSuccessWithData(routerList);
    }

    @Override
    public ActionReturnUtil updateSystemRouteRule(SvcRouterDto svcRouterDto) throws Exception {
        if (Objects.isNull(svcRouterDto) || CollectionUtils.isEmpty(svcRouterDto.getRules())
                || StringUtils.isBlank(svcRouterDto.getName()) || StringUtils.isBlank(svcRouterDto.getNamespace())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(svcRouterDto.getNamespace());
        //获取容器内的端口协议
        K8SClientResponse depRes = dpService.doSpecifyDeployment(svcRouterDto.getNamespace(), svcRouterDto.getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
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
        this.updateSystemExposeConfigmap(cluster, svcRouterDto.getNamespace(), svcRouterDto.getName(), rules, protocol);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteSystemRouteRule(TcpDeleteDto tcpDeleteDto) throws Exception {
        if (Objects.isNull(tcpDeleteDto) || CollectionUtils.isEmpty(tcpDeleteDto.getPorts()) || StringUtils.isBlank(tcpDeleteDto.getNamespace()) || StringUtils.isBlank(tcpDeleteDto.getProtocol())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String namespace = tcpDeleteDto.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        //获取configmap后，删除对应的规则
        ConfigMap configMap = this.getSystemExposeConfigmap(cluster, tcpDeleteDto.getProtocol());
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
    private List<Map<String, Object>> listServiceRouter(ConfigMap configMap, String valuePrefix, String ip, String type) throws Exception {
        List<Map<String, Object>> routerList = new ArrayList<>();
        Map<String, Object> configMapData = (Map<String, Object>) configMap.getData();
        if (Objects.nonNull(configMapData)) {
            for (Map.Entry<String, Object> entry : configMapData.entrySet()) {
                if (entry.getValue().toString().indexOf(valuePrefix) > -1) {
                    String value = entry.getValue().toString();
                    Map<String, Object> tmp = new HashMap<>();
                    tmp.put("type", type);
                    Map<String, Object> address = new HashMap<>();
                    String containerPort = value.substring(value.indexOf(valuePrefix) + valuePrefix.length() + 1);
                    address.put("containerPort", containerPort);
                    address.put("externalPort", entry.getKey());
                    address.put("ip", ip);
                    tmp.put("address", address);
                    routerList.add(tmp);
                }
            }
        }
        return routerList;
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
            this.updateSystemExposeConfigmap(cluster, svcRouterDto.getNamespace(), svcRouterDto.getName(), Arrays.asList(rule), rule.getProtocol());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public List<Map<String, Object>> createExternalRule(ServiceTemplateDto svcTemplate, String namespace) throws Exception {
        List<Map<String, Object>> message = new ArrayList<>();
        for (IngressDto ingress : svcTemplate.getIngress()) {
            if (Constant.PROTOCOL_HTTP.equals(ingress.getType()) && !StringUtils.isEmpty(ingress.getParsedIngressList().getName())) {
                Map<String, Object> labels = new HashMap<String, Object>();
                labels.put("app", svcTemplate.getDeploymentDetail().getName());
                ingress.getParsedIngressList().setLabels(labels);
                ingress.getParsedIngressList().setNamespace(namespace);
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
}
