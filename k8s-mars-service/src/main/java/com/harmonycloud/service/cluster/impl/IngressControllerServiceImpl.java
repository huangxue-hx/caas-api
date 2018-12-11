package com.harmonycloud.service.cluster.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.Constant.IngressControllerConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.cluster.bean.IngressControllerPort;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.service.IcService;
import com.harmonycloud.k8s.service.ServiceAccountService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.IngressControllerService;
import com.harmonycloud.service.platform.bean.NodeDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.TenantClusterQuotaService;
import com.harmonycloud.service.tenant.TenantService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.common.Constant.IngressControllerConstant.*;
import static com.harmonycloud.common.Constant.IngressControllerConstant.LABEL_KEY_APP;
import static com.harmonycloud.service.platform.constant.Constant.LABEL_INGRESS_CLASS;

/**
 * @author xc
 * @date 2018/7/31 19:32
 */
@Service
public class IngressControllerServiceImpl implements IngressControllerService {

    private static Logger LOGGER = LoggerFactory.getLogger(IngressControllerServiceImpl.class);

    @Value("#{propertiesReader['ic.image.tag']}")
    private String icImageTag;

    private static final String IC_PORT_RANGE_CM_NAME = "ingress-controller-port";

    private static final String TCP = "tcp-";

    private static final String UDP = "udp-";

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private IcService icService;

    @Autowired
    private ServiceAccountService serviceAccountService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private TenantClusterQuotaService tenantClusterQuotaService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ConfigmapService configmapService;

    @Override
    public ActionReturnUtil listIngressController(String clusterId) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<IngressControllerDto> ingressControllerDtoList = this.listIngressControllerBrief(clusterId);
        Map<String, List<String>> icAssignedTenantIds = this.getIcAssignedTenantIds(clusterId);
        for (IngressControllerDto ingressControllerDto : ingressControllerDtoList) {
            //设置负载均衡器已分配的租户列表
            List<String> tenantIdList = icAssignedTenantIds.get(ingressControllerDto.getIcName());
            if (CollectionUtils.isNotEmpty(tenantIdList)) {
                ingressControllerDto.setTenantInfo(buildTenantInfoByTenantId(tenantIdList));
            }
            //设置负载均衡器部署的节点列表
            ingressControllerDto.setIcNodeNames(this.getIcNodeNames(ingressControllerDto.getIcName(), cluster));
        }
        return ActionReturnUtil.returnSuccessWithData(ingressControllerDtoList);
    }

    @Override
    public IngressControllerDto getIngressController(String icName, String clusterId) {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SClientResponse response = icService.getIngressController(icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("查询ingresscontroller失败,response:{}", JSONObject.toJSONString(response));
            return null;
        }
        DaemonSet daemonSet = JsonUtil.jsonToPojo(response.getBody(), DaemonSet.class);
        return this.convertIngressController(daemonSet, cluster);
    }

    @Override
    public List<IngressControllerDto> listIngressControllerBrief(String clusterId) {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SClientResponse response = icService.listIngressController(cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("查询ingresscontroller失败,response:{}", JSONObject.toJSONString(response));
            return Collections.emptyList();
        }
        DaemonSetList daemonSetList = JsonUtil.jsonToPojo(response.getBody(), DaemonSetList.class);
        List<IngressControllerDto> ingressControllerDtoList = new ArrayList<>();
        for (DaemonSet daemonSet : daemonSetList.getItems()) {
            ingressControllerDtoList.add(this.convertIngressController(daemonSet, cluster));
        }
        return ingressControllerDtoList;
    }

    private  IngressControllerDto convertIngressController(DaemonSet daemonSet, Cluster cluster){
        if(daemonSet == null || cluster == null){
            return null;
        }
        IngressControllerDto ingressControllerDto = new IngressControllerDto();
        ingressControllerDto.setClusterAliasName(cluster.getAliasName());
        ingressControllerDto.setClusterId(cluster.getId());
        ingressControllerDto.setIcName(daemonSet.getMetadata().getName());
        ingressControllerDto.setNamespace(daemonSet.getMetadata().getNamespace());
        List<ContainerPort> containerPortList = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts();
        ingressControllerDto.setHttpPort(containerPortList.get(0).getContainerPort());
        List<String> icPorts = new ArrayList<>();
        for (ContainerPort containerPort : containerPortList) {
            icPorts.add(containerPort.getProtocol() + ": " + containerPort.getContainerPort());
        }
        ingressControllerDto.setIcPort(StringUtils.join(icPorts.toArray(), ","));
        List<String> containerArgs = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getArgs();
        for(String arg : containerArgs){
            if(arg.contains(CONTAINER_ARGS_HTTP)){
                ingressControllerDto.setHttpPort(Integer.parseInt(arg.split("=")[1]));
            }else if(arg.contains(CONTAINER_ARGS_HTTPS)){
                ingressControllerDto.setHttpsPort(Integer.parseInt(arg.split("=")[1]));
            }else if(arg.contains(CONTAINER_ARGS_HEALTH)){
                ingressControllerDto.setHealthPort(Integer.parseInt(arg.split("=")[1]));
            }else if(arg.contains(CONTAINER_ARGS_STATUS)){
                ingressControllerDto.setStatusPort(Integer.parseInt(arg.split("=")[1]));
            }
        }
        //设置外网http https端口
        Map<String, Object> annotations = daemonSet.getMetadata().getAnnotations();
        if(annotations != null){
            if(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT) != null) {
                int externalHttpPort = Integer.parseInt(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT).toString());
                ingressControllerDto.setExternalHttpPort(externalHttpPort);
            }
            if(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT) != null){
                int externalHttpsPort = Integer.parseInt(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT).toString());
                ingressControllerDto.setExternalHttpsPort(externalHttpsPort);
            }
        }

        if (daemonSet.getStatus() != null && daemonSet.getStatus().getDesiredNumberScheduled() != null && daemonSet.getStatus().getNumberAvailable() != null && daemonSet.getStatus().getDesiredNumberScheduled().equals(daemonSet.getStatus().getNumberAvailable())) {
            ingressControllerDto.setStatus(Constant.SERVICE_START);
        } else {
            ingressControllerDto.setStatus(Constant.SERVICE_STARTING);
        }
        if (IC_DEFAULT_NAME.equals(daemonSet.getMetadata().getName())) {
            ingressControllerDto.setIsDefault(true);
        } else {
            ingressControllerDto.setIsDefault(false);
        }
        ingressControllerDto.setCreateTime(DateUtil.StringToDate(daemonSet.getMetadata().getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
        ingressControllerDto.setIcAliasName(this.getIcAliasName(daemonSet));
        return ingressControllerDto;
    }

    //通过tenantId构造tenantInfo
    private List<Map<String, String>> buildTenantInfoByTenantId(List<String> tenantIdList) throws Exception {
        List<Map<String, String>> tenantInfoList = new ArrayList<>();
        for (String tenantId : tenantIdList) {
            Map<String, String> tenantInfo = new LinkedHashMap<>();
            tenantInfo.put("tenantId", tenantId);
            TenantBinding tenantBinding = tenantService.getTenantByTenantid(tenantId);
            tenantInfo.put("tenantName", tenantBinding.getAliasName());
            tenantInfoList.add(tenantInfo);
        }
        return tenantInfoList;
    }

    @Override
    public ActionReturnUtil getIngressControllerPortRange(String clusterId) throws MarsRuntimeException, IOException {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //获取Ingress-controller-port范围
        Map<String, Integer> portRangeMap = getIngressControllerPortRange(cluster);
        if (portRangeMap == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_PORT_RANGE_NOT_FOUND);
        }
        return ActionReturnUtil.returnSuccessWithData(portRangeMap);
    }

    @Override
    public ActionReturnUtil createIngressController(IngressControllerDto ingressControllerDto)
            throws MarsRuntimeException, IOException {
        String icName = ingressControllerDto.getIcName();
        //获取集群
        Cluster cluster = clusterService.findClusterById(ingressControllerDto.getClusterId());
        List<IngressControllerDto> existIcDtos = this.listIngressControllerBrief(cluster.getId());
        //获取Ingress-controller-port范围
        Map<String, Integer> portRangeMap = getIngressControllerPortRange(cluster);
        this.validateCreate(ingressControllerDto, cluster, existIcDtos, portRangeMap);
        //构建Ingress-controller-port端口
        IngressControllerPort ingressControllerPort = buildIngressControllerPort(ingressControllerDto, portRangeMap, existIcDtos);
        if (ingressControllerPort == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_OTHER_PORT_USED);
        }
        //获取kube-system下serviceAccount default
        K8SClientResponse saResponse = serviceAccountService.getServiceAccountByName(CommonConstant.KUBE_SYSTEM, CommonConstant.DEFAULT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(saResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_SA_NOT_FOUND);
        }
        ServiceAccount serviceAccount = JsonUtil.jsonToPojo(saResponse.getBody(), ServiceAccount.class);
        //拼接Ingress-controller
        DaemonSet daemonSet = buildIngressController(ingressControllerDto, ingressControllerPort, serviceAccount);
        //更新主机节点标签
        this.updateNodeLabel(icName, cluster, Collections.emptyList(), ingressControllerDto.getIcNodeNames());
        //创建tcp、udp配置文件
        ConfigMap tcpConfigMap = buildIcConfigMap(icName, Constant.PROTOCOL_TCP.toLowerCase());
        K8SClientResponse tcpCmResponse = icService.createIcConfigMap(tcpConfigMap, cluster);
        if (!HttpStatusUtil.isSuccessStatus(tcpCmResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(tcpCmResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        ConfigMap udpConfigMap = buildIcConfigMap(icName, Constant.PROTOCOL_UDP.toLowerCase());
        K8SClientResponse udpCmResponse = icService.createIcConfigMap(udpConfigMap, cluster);
        if (!HttpStatusUtil.isSuccessStatus(udpCmResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(udpCmResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //创建Ingress-controller
        K8SClientResponse createResponse= icService.createIngressController(daemonSet, cluster);
        if (!HttpStatusUtil.isSuccessStatus(createResponse.getStatus())) {
            LOGGER.error("创建ingresscontroller失败,response:{}",JSONObject.toJSONString(createResponse));
            //创建ingress controller daemonset失败，回退刚才创建或更新的k8s资源
            this.updateNodeLabel(icName, cluster, ingressControllerDto.getIcNodeNames(), Collections.emptyList());
            configmapService.delete(CommonConstant.KUBE_SYSTEM, tcpConfigMap.getMetadata().getName(), cluster);
            configmapService.delete(CommonConstant.KUBE_SYSTEM, udpConfigMap.getMetadata().getName(), cluster);
            UnversionedStatus status = JsonUtil.jsonToPojo(createResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }

        return ActionReturnUtil.returnSuccess();
    }

    //构建ingress controller对象
    private DaemonSet buildIngressController(IngressControllerDto ingressControllerDto,
                           IngressControllerPort ingressControllerPort, ServiceAccount serviceAccount) {
        DaemonSet daemonSet = new DaemonSet();
        //daemonSet.metadata
        ObjectMeta objectMeta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(LABEL_KEY_NAME, ingressControllerPort.getName());
        labels.put(LABEL_KEY_APP, LABEL_VALUE_APP_NGINX);
        objectMeta.setLabels(labels);
        objectMeta.setNamespace(Constant.NAMESPACE_SYSTEM);
        objectMeta.setName(ingressControllerPort.getName());
        //annotations 设置负载均衡器的别名，外网暴露http和https端口
        Map<String, Object> annotations = new HashMap<>();
        annotations.put(ANNOTATIONS_KEY_ALIAS_NAME, ingressControllerDto.getIcAliasName());
        if (StringUtils.isBlank(ingressControllerDto.getIcAliasName())) {
            annotations.put(ANNOTATIONS_KEY_ALIAS_NAME, ingressControllerDto.getIcName());
        }
        if (ingressControllerDto.getExternalHttpPort() != null && ingressControllerDto.getExternalHttpPort() > 0) {
            annotations.put(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT, String.valueOf(ingressControllerDto.getExternalHttpPort()));
        }
        if (ingressControllerDto.getExternalHttpsPort() != null && ingressControllerDto.getExternalHttpsPort() > 0) {
            annotations.put(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT, String.valueOf(ingressControllerDto.getExternalHttpsPort()));
        }
        objectMeta.setAnnotations(annotations);
        daemonSet.setMetadata(objectMeta);
        //daemonSet.spec
        DaemonSetSpec daemonSetSpec = new DaemonSetSpec();
        //daemonSet.spec.labelSelector
        LabelSelector labelSelector = new LabelSelector();
        Map<String, Object> matchLabels = new HashMap<>(labels);
        labelSelector.setMatchLabels(matchLabels);
        daemonSetSpec.setSelector(labelSelector);
        //daemonSet.spec.updateStrategy
        DaemonSetUpdateStrategy updateStrategy = new DaemonSetUpdateStrategy();
        updateStrategy.setType(DAEMONSET_SPEC_UPDATESTRATEGY_TYPE);
        //daemonSet.spec.template
        PodTemplateSpec templateSpec = new PodTemplateSpec();
        //daemonSet.spec.template.metadata
        objectMeta = new ObjectMeta();
        objectMeta.setLabels(labels);
        templateSpec.setMetadata(objectMeta);
        //daemonSet.spec.template.spec
        PodSpec podSpec = new PodSpec();
        podSpec.setTerminationGracePeriodSeconds(TEMPLATE_SPEC_TERMINATIONGRACEPERIODSECONDS);
        Map<String, Object> nodeSelector = new HashMap<>();
        //如果未指定负载均衡节点，逻辑保持不变,标签为lb=nginx
        //指定负载均衡的节点，标签为lb=nginx-custom，并增加负载均衡名称的标签
        if(CollectionUtils.isEmpty(ingressControllerDto.getIcNodeNames())) {
            nodeSelector.put(TEMPLATE_SPEC_NODESELECTOR_KEY, TEMPLATE_SPEC_NODESELECTOR_VALUE);
        }else{
            nodeSelector.put(TEMPLATE_SPEC_NODESELECTOR_KEY, LABEL_VALUE_NGINX_CUSTOM);
            nodeSelector.put(LABEL_KEY_INGRESS_CONTROLLER_NAME, ingressControllerDto.getIcName());
        }
        podSpec.setNodeSelector(nodeSelector);
        podSpec.setHostNetwork(TEMPLATE_SPEC_HOSTNETWORK);
        //daemonSet.spec.template.spec.containers
        List<Container> containerList = new ArrayList<>();
        Container container = new Container();
        container.setImage(CONTAINER_IMAGE + icImageTag);
        container.setName(ingressControllerPort.getName());
        //readinessProbe
        Probe probe = new Probe();
        HTTPGetAction httpGetAction = new HTTPGetAction();
        httpGetAction.setPath(CONTAINER_READINESSPROBE_PATH);
        httpGetAction.setPort(ingressControllerPort.getHealthPort());
        httpGetAction.setScheme(CONTAINER_READINESSPROBE_SCHEME);
        probe.setHttpGet(httpGetAction);
        container.setReadinessProbe(probe);
        //livenessProbe
        probe = new Probe();
        httpGetAction = new HTTPGetAction();
        httpGetAction.setPath(CONTAINER_LIVENESSPROBE_PATH);
        httpGetAction.setPort(ingressControllerPort.getHealthPort());
        httpGetAction.setScheme(CONTAINER_LIVENESSPROBE_SCHEME);
        probe.setHttpGet(httpGetAction);
        probe.setInitialDelaySeconds(CONTAINER_LIVENESSPROBE_INITIALDELAYSECONDS);
        probe.setTimeoutSeconds(CONTAINER_LIVENESSPROBE_TIMEOUTSECONDS);
        container.setLivenessProbe(probe);
        //env
        List<EnvVar> envVarList = new ArrayList<>();
        EnvVar envVar = new EnvVar();
        envVar.setName(CONTAINER_ENV_ONE_NAME);
        EnvVarSource envVarSource = new EnvVarSource();
        ObjectFieldSelector objectFieldSelector = new ObjectFieldSelector();
        objectFieldSelector.setFieldPath(CONTAINER_ENV_ONE_FieldPath);
        envVarSource.setFieldRef(objectFieldSelector);
        envVar.setValueFrom(envVarSource);
        envVarList.add(envVar);
        envVar = new EnvVar();
        envVar.setName(CONTAINER_ENV_TWO_NAME);
        envVarSource = new EnvVarSource();
        objectFieldSelector = new ObjectFieldSelector();
        objectFieldSelector.setFieldPath(CONTAINER_ENV_TWO_FieldPath);
        envVarSource.setFieldRef(objectFieldSelector);
        envVar.setValueFrom(envVarSource);
        envVarList.add(envVar);
        container.setEnv(envVarList);
        //ports
        List<ContainerPort> containerPortList = new ArrayList<>();
        ContainerPort containerPort = new ContainerPort();
        containerPort.setContainerPort(ingressControllerPort.getHttpPort());
        containerPort.setHostPort(ingressControllerPort.getHttpPort());
        containerPortList.add(containerPort);
        containerPort = new ContainerPort();
        containerPort.setContainerPort(ingressControllerPort.getHttpsPort());
        containerPort.setHostPort(ingressControllerPort.getHttpsPort());
        containerPortList.add(containerPort);
        container.setPorts(containerPortList);
        //args
        List<String> args = new LinkedList<>();
        args.add(CONTAINER_ARGS_NAME);
        args.add(CONTAINER_ARGS_HTTP + ingressControllerPort.getHttpPort());
        args.add(CONTAINER_ARGS_HTTPS + ingressControllerPort.getHttpsPort());
        args.add(CONTAINER_ARGS_HEALTH + ingressControllerPort.getHealthPort());
        args.add(CONTAINER_ARGS_STATUS + ingressControllerPort.getStatusPort());
        args.add(CONTAINER_ARGS_IC_CM);
        args.add(CONTAINER_ARGS_DEFAULT_BACKEND);
        args.add(CONTAINER_ARGS_UDP_CM + ingressControllerPort.getName());
        args.add(CONTAINER_ARGS_TCP_CM + ingressControllerPort.getName());
        args.add(CONTAINER_ARGS_INGRESS_CLASS + ingressControllerPort.getName());
        container.setArgs(args);
        //volumeMounts
        List<VolumeMount> volumeMountList = new ArrayList<>();
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath(CONTAINER_ARGS_VOLUMEMOUNT_PATH);
        volumeMount.setName(serviceAccount.getSecrets().get(0).getName());
        volumeMount.setReadOnly(CONTAINER_ARGS_VOLUMEMOUNT_READONLY);
        volumeMountList.add(volumeMount);
        container.setVolumeMounts(volumeMountList);
        containerList.add(container);
        podSpec.setContainers(containerList);
        //daemonSet.spec.template.spec.volumes
        List<Volume> volumeList = new ArrayList<>();
        Volume volume = new Volume();
        volume.setName(serviceAccount.getSecrets().get(0).getName());
        SecretVolumeSource secretVolumeSource = new SecretVolumeSource();
        secretVolumeSource.setDefaultMode(TEMPLATE_SPEC_VOLUMES_DEFAULTMODE);
        secretVolumeSource.setSecretName(serviceAccount.getSecrets().get(0).getName());
        volume.setSecret(secretVolumeSource);
        volumeList.add(volume);
        podSpec.setVolumes(volumeList);
        templateSpec.setSpec(podSpec);
        daemonSetSpec.setTemplate(templateSpec);
        daemonSetSpec.setUpdateStrategy(updateStrategy);
        daemonSet.setSpec(daemonSetSpec);
        return daemonSet;
    }

    //检查icPort是否在指定范围
    private Boolean checkIcHttpPort(int icPort, Map<String, Integer> portRangeMap) {
        return icPort >= portRangeMap.get(HTTP_MIN_PORT) && icPort <= portRangeMap.get(HTTP_MAX_PORT);
    }

    //构建ingress controller使用的端口
    private IngressControllerPort buildIngressControllerPort(IngressControllerDto ingressControllerDto,
                                                             Map<String, Integer> portRangeMap,
                                                             List<IngressControllerDto> icPortList) {
        IngressControllerPort ingressControllerPort = new IngressControllerPort();
        ingressControllerPort.setName(ingressControllerDto.getIcName());
        ingressControllerPort.setClusterId(ingressControllerDto.getClusterId());
        ingressControllerPort.setHttpPort(ingressControllerDto.getHttpPort());
        //httpsPort
        int httpsPort = getUnusedPort(portRangeMap.get(HTTPS_MIN_PORT),
                portRangeMap.get(HTTPS_MAX_PORT),
                icPortList, HTTPS_PORT);
        if (httpsPort == 0) {
            return null;
        }
        ingressControllerPort.setHttpsPort(httpsPort);
        //healthPort
        int healthPort = getUnusedPort(portRangeMap.get(HEALTH_MIN_PORT), portRangeMap.get(HEALTH_MAX_PORT), icPortList, HEALTH_PORT);
        if (healthPort == 0) {
            return null;
        }
        ingressControllerPort.setHealthPort(healthPort);
        //statusPort
        int statusPort = getUnusedPort(portRangeMap.get(STATUS_MIN_PORT), portRangeMap.get(STATUS_MAX_PORT), icPortList, STATUS_PORT);
        if (statusPort == 0) {
            return null;
        }
        ingressControllerPort.setStatusPort(statusPort);

        return ingressControllerPort;
    }

    //获取未被使用端口，若未找到可用的端口返回0值
    private int getUnusedPort(int minPort, int maxPort, List<IngressControllerDto> icPortList, String usedType) {
        int unUsedPort = 0;
        for (int i = minPort; i <= maxPort; i++) {
            boolean flag = true;
            for (IngressControllerDto ingressControllerPort : icPortList) {
                if (usedType.equals(HTTPS_PORT)) {
                    if (i == ingressControllerPort.getHttpsPort()) {
                        flag = false;
                    }
                }
                if (usedType.equals(HEALTH_PORT)) {
                    if (i == ingressControllerPort.getHealthPort()) {
                        flag = false;
                    }
                }
                if (usedType.equals(STATUS_PORT)) {
                    if (i == ingressControllerPort.getStatusPort()) {
                        flag = false;
                    }
                }
            }
            if (flag) {
                unUsedPort = i;
                break;
            }
        }
        return unUsedPort;
    }

    //获取ingressController端口范围
    @SuppressWarnings("unchecked")
    private Map<String, Integer> getIngressControllerPortRange(Cluster cluster) throws IOException {
        K8SClientResponse cmResponse = icService.getIcConfigMapByName(IC_PORT_RANGE_CM_NAME, cluster);
        if (!HttpStatusUtil.isSuccessStatus(cmResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(cmResponse.getBody(), UnversionedStatus.class);
            LOGGER.error(status.getMessage() + "," + status.getReason());
            return null;
        }
        ConfigMap portConfigMap = JsonUtil.jsonToPojo(cmResponse.getBody(), ConfigMap.class);
        Map<String, String> portData = (Map<String, String>) portConfigMap.getData();
        String portJson = portData.get(IC_PORT_RANGE_CM_NAME);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(portJson, Map.class);
    }

    //构建ingress controller 所需的 tcp、udp configMap
    private ConfigMap buildIcConfigMap(String icName, String type) {
        ConfigMap configMap = new ConfigMap();
        ObjectMeta cmMeta = new ObjectMeta();
        cmMeta.setName(type + "-" + icName);
        cmMeta.setNamespace(CommonConstant.KUBE_SYSTEM);
        configMap.setMetadata(cmMeta);
        return configMap;
    }

    @Override
    public ActionReturnUtil deleteIngressController(String icName, String clusterId) throws Exception {
        //系统默认负载均衡器，不允许删除
        if (IngressControllerConstant.IC_DEFAULT_NAME.equals(icName)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_DEFAULT_NOT_DELETE);
        }
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        //查看是否被占用
        if(checkIcUsedStatus(icName, cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HAD_USED);
        }
        //删除tenant_cluster_quota表绑定
        List<TenantClusterQuota> tenants = tenantClusterQuotaService.listClusterQuotaLikeIcName(icName,clusterId);
        for (TenantClusterQuota tenantClusterQuota:tenants) {
            removeTenants(tenantClusterQuota,icName);
        }
        //删除ingressController
        K8SClientResponse response = icService.deleteIngressController(icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("删除ingresscontroller失败,response:{}",JSONObject.toJSONString(response));
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        List<String> oldIcNodeNames = this.getIcNodeNames(icName, cluster);
        this.updateNodeLabel(icName, cluster, oldIcNodeNames, Collections.emptyList());
        //删除ingressController相关的configMap
        K8SClientResponse response_tcp = icService.deleteConfigMap(TCP + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_tcp.getStatus())) {
            LOGGER.error("删除ingresscontroller tcp配置失败,response:{}",JSONObject.toJSONString(response_tcp));
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        K8SClientResponse response_udp = icService.deleteConfigMap(UDP + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_udp.getStatus())) {
            LOGGER.error("删除ingresscontroller udp配置失败,response:{}",JSONObject.toJSONString(response_udp));
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        K8SClientResponse response_leader = icService.deleteConfigMap("ingress-controller-leader-" + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_leader.getStatus())) {
            LOGGER.error("删除ingresscontroller leader配置失败,response:{}",JSONObject.toJSONString(response_leader));
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateIngressController(IngressControllerDto ingressControllerDto) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(ingressControllerDto.getClusterId());
        String icName = ingressControllerDto.getIcName();
        int httpPort = ingressControllerDto.getHttpPort();
        List<IngressControllerDto> existIcDtos = this.listIngressControllerBrief(cluster.getId());
        //获取Ingress-controller-port范围
        Map<String, Integer> portRangeMap = getIngressControllerPortRange(cluster);
        //检查名称，端口，主机类型
        this.validateUpdate(ingressControllerDto, cluster,  existIcDtos, portRangeMap);
        //查询已经创建的ingress controller
        K8SClientResponse response = icService.getIngressController(icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        DaemonSet daemonSet = JsonUtil.jsonToPojo(response.getBody(), DaemonSet.class);
        int existPort = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();
        //查看ingress controller是否已经在使用（已使用该负载均衡器创建对外服务）,已经在使用的负载均衡器不能修改端口
        if(existPort != httpPort && checkIcUsedStatus(icName, cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HAD_USED);
        }
        //判断端口和别名有没有修改，如果修改了需要更新daemonset
        if(this.isDaemonsetChanged(daemonSet, ingressControllerDto)){
            //更新annotations（别名和外网http，https端口）
            this.updateAnnotations(ingressControllerDto, daemonSet);
            //修改hostPort
            daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).setHostPort(httpPort);
            //修改containerPort
            daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).setContainerPort(httpPort);
            //修改http_port
            daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getArgs().set(1, CONTAINER_ARGS_HTTP + httpPort);
            DaemonSetUpdateStrategy dsUpdateStrategy = new DaemonSetUpdateStrategy();
            dsUpdateStrategy.setType("RollingUpdate");
            daemonSet.getSpec().setUpdateStrategy(dsUpdateStrategy);
            K8SClientResponse icResponse = icService.updateIngressController(icName, daemonSet, cluster);
            if(!HttpStatusUtil.isSuccessStatus(icResponse.getStatus())) {
                LOGGER.error("更新ingresscontroller失败,response:{}",JSONObject.toJSONString(icResponse));
                UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
        }
        //修改了负载均衡的节点，更新节点标签
        List<String> oldIcNodeNames = this.getIcNodeNames(icName, cluster);
        this.updateNodeLabel(icName, cluster, oldIcNodeNames, ingressControllerDto.getIcNodeNames());
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public Boolean checkIcUsedStatus(String icName,  Cluster cluster) throws MarsRuntimeException{

        //查看是否被TCP使用
        K8SClientResponse tcpCmResponse = icService.getIcConfigMapByName(TCP + icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(tcpCmResponse.getStatus())) {
            LOGGER.error("获取tcpConfigMap失败！稍后重试操作！");
            throw new MarsRuntimeException(TCP+DictEnum.CONFIG_MAP.phrase(), ErrorCodeMessage.QUERY_FAIL);
        }
        ConfigMap tcpConfigMap = JsonUtil.jsonToPojo(tcpCmResponse.getBody(), ConfigMap.class);
        if(!Objects.isNull(tcpConfigMap.getData())){
            return true;
        }
        //查看是否被UDP使用
        K8SClientResponse udpCmResponse = icService.getIcConfigMapByName(UDP + icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(udpCmResponse.getStatus())) {
            LOGGER.error("获取udpConfigMap失败！稍后重试操作！");
            throw new MarsRuntimeException(UDP+DictEnum.CONFIG_MAP.phrase(), ErrorCodeMessage.QUERY_FAIL);
        }
        ConfigMap udpConfigMap = JsonUtil.jsonToPojo(udpCmResponse.getBody(), ConfigMap.class);
        if(!Objects.isNull(udpConfigMap.getData())){
            return true;
        }
        //查看是否被HTTP使用
        String label = LABEL_INGRESS_CLASS + "=" + icName;
        K8SClientResponse ingressResponse = icService.getIngressByLabel(label, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ingressResponse.getStatus())) {
            LOGGER.error("获取Ingress失败！稍后重试操作！, res:{}", JSONObject.toJSONString(ingressResponse));
            throw new MarsRuntimeException("ingress", ErrorCodeMessage.QUERY_FAIL);
        }
        IngressList ingressList = JsonUtil.jsonToPojo(ingressResponse.getBody(), IngressList.class);
        if(!CollectionUtils.isEmpty(ingressList.getItems())){
            return true;
        }
        return false;
    }

    /**
     * 检查租户是否已经在使用该负载均衡器
     */
    private Boolean checkIcUsedStatus(String icName, String tenantId, Cluster cluster) throws Exception{
        List<String> clusterIds = new ArrayList<>();
        clusterIds.add(cluster.getId());
        List<NamespaceLocal> namespaces = namespaceLocalService.getNamespaceListByTenantIdAndClusterId(tenantId,clusterIds);
        if(CollectionUtils.isEmpty(namespaces)){
            return false;
        }
        List<String> namespaceNames = namespaces.stream().map(NamespaceLocal::getNamespaceName).collect(Collectors.toList());
        //查看是否被TCP使用
        K8SClientResponse tcpCmResponse = icService.getIcConfigMapByName(TCP + icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(tcpCmResponse.getStatus())) {
            LOGGER.error("获取tcpConfigMap失败！稍后重试操作！");
            return true;
        }
        ConfigMap tcpConfigMap = JsonUtil.jsonToPojo(tcpCmResponse.getBody(), ConfigMap.class);
        boolean inUsed = checkIcUsed(namespaceNames, tcpConfigMap);
        if(inUsed){
            return true;
        }
        //查看是否被UDP使用
        K8SClientResponse udpCmResponse = icService.getIcConfigMapByName(UDP + icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(udpCmResponse.getStatus())) {
            LOGGER.error("获取udpConfigMap失败！稍后重试操作！");
            return true;
        }
        ConfigMap udpConfigMap = JsonUtil.jsonToPojo(udpCmResponse.getBody(), ConfigMap.class);
        inUsed = checkIcUsed(namespaceNames, udpConfigMap);
        if(inUsed){
            return true;
        }
        //查看是否被HTTP使用
        String label = "tenantId=" + tenantId + "," + LABEL_INGRESS_CLASS + "=" + icName;
        K8SClientResponse ingressResponse = icService.getIngressByLabel(label, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ingressResponse.getStatus())) {
            LOGGER.error("获取Ingress失败！稍后重试操作！");
            return true;
        }
        IngressList ingressList = JsonUtil.jsonToPojo(ingressResponse.getBody(), IngressList.class);
        if(!CollectionUtils.isEmpty(ingressList.getItems())){
            return true;
        }
        return false;
    }

    private boolean checkIcUsed(List<String> namespaceNames, ConfigMap configMap){
        if(Objects.isNull(configMap.getData())){
            return false;
        }
        LinkedHashMap<String, String> linkedHashMap = (LinkedHashMap)configMap.getData();
        Collection<String> values = linkedHashMap.values();
        Set<String> namespaceSet = new HashSet<>();
        values.stream().forEach( value -> namespaceSet.add(value.substring(0, value.indexOf("/"))));
        for(String namespace : namespaceSet){
            if(namespaceNames.contains(namespace)){
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionReturnUtil assignIngressController(String icName, String tenantId, String clusterId) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断该负载均衡器是否存在
        K8SClientResponse response = icService.getIngressController(icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_NOT_FOUND);
        }
        //获取所有绑定
        List<TenantClusterQuota> tenantClusterQuotas =  tenantClusterQuotaService.listClusterQuotaLikeIcName(icName, clusterId);

        List<String> newTenantIds = new ArrayList<>();
        if(tenantId != null){
            newTenantIds.addAll(Arrays.asList(tenantId.split(",")));
        }
        //如果后台查到的租户为空，即该负载均衡器还未分配过，为首次给租户分配，则只需要将负载均衡名称加到将要分配的租户的配额表
        if(CollectionUtils.isEmpty(tenantClusterQuotas)){
           for(String s : newTenantIds){
               //添加租户
               addTenants(s, clusterId, icName);
           }
           return ActionReturnUtil.returnSuccess();
        }
        List<String> oldTenantIds = tenantClusterQuotas.stream().map(TenantClusterQuota::getTenantId).collect(Collectors.toList());
        List<String> removedTenantIds = oldTenantIds.stream().filter(id -> !newTenantIds.contains(id)).collect(Collectors.toList());
        //检查要移除的租户是否已经使用该负载均衡器
        //Ingress Controller分配给租户后，被使用；这些租户的集合
        List<String> usedTenantList = new ArrayList<>();
        for (String removedTenantId : removedTenantIds) {
            if (checkIcUsedStatus(icName, removedTenantId, cluster)) {
                TenantBinding tenantBinding = tenantService.getTenantByTenantid(removedTenantId);
                usedTenantList.add(tenantBinding.getAliasName());
            }
        }
        //如果要移除的租户已经在使用该负载均衡器，则返回错误提示
        if (CollectionUtils.isNotEmpty(usedTenantList)) {
            String tenantNames = StringUtils.join(usedTenantList.toArray(), ",");
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HAS_USED_BY_TENANTS, tenantNames);
        }
        //移除负载均衡器分配的租户
        for (String removedTenantId : removedTenantIds) {
            for (TenantClusterQuota t : tenantClusterQuotas) {
                if (t.getTenantId().equals(removedTenantId)) {
                    removeTenants(t, icName);
                }
            }
        }
        //添加负载均衡器分配的租户
        List<String> addedTenantIds = newTenantIds.stream().filter(id -> !oldTenantIds.contains(id)).collect(Collectors.toList());
        for (String s : addedTenantIds) {
            addTenants(s, clusterId, icName);
        }
        return ActionReturnUtil.returnSuccess();
    }

    //向租户配额表中，添加租户被分配的负载均衡器名称
    private void addTenants(String tenantId, String clusterId, String icName) throws Exception {
        TenantClusterQuota tenant = tenantClusterQuotaService.getClusterQuotaByTenantIdAndClusterId(tenantId, clusterId);
        if (StringUtils.isNotBlank(tenant.getIcNames()) ) {
            tenant.setIcNames(tenant.getIcNames() + "," + icName);
        } else {
            tenant.setIcNames(icName);
        }
        tenantClusterQuotaService.updateClusterQuota(tenant);
    }

    //移除租户配额表中，未被分配的负载均衡器名称
    private void removeTenants(TenantClusterQuota tenantClusterQuota, String icName) throws Exception {
        String[] icNs = tenantClusterQuota.getIcNames().split(",");
        StringBuilder sb = new StringBuilder();
        for (String icN : icNs) {
            if (StringUtils.isNotBlank(icN) && !icN.equals(icName)) {
                sb.append(icN);
                sb.append(",");
            }
        }
        //刪除最后一个逗号
        if(sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        tenantClusterQuota.setIcNames(sb.toString());
        tenantClusterQuotaService.updateClusterQuota(tenantClusterQuota);
    }


    private void validateCreate(IngressControllerDto ingressControllerDto, Cluster cluster,
                                List<IngressControllerDto> existIcDtos, Map<String, Integer> portRangeMap) {
        AssertUtil.notBlank(ingressControllerDto.getIcName(),DictEnum.NAME);
        AssertUtil.greaterZero(ingressControllerDto.getHttpPort(), DictEnum.PORT);
        if (CollectionUtils.isEmpty(existIcDtos)) {
            return;
        }
        for (IngressControllerDto existIcDto : existIcDtos) {
            //名称已经存在
            if (existIcDto.getIcName().equalsIgnoreCase(ingressControllerDto.getIcName())
                    || existIcDto.getIcAliasName().equals(ingressControllerDto.getIcAliasName())) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST);
            }
            //端口已经被使用
            if (existIcDto.getHttpPort() == ingressControllerDto.getHttpPort()) {
                throw new MarsRuntimeException(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_USED);
            }
        }
        this.validate(ingressControllerDto, cluster, portRangeMap);

    }

    private void validateUpdate(IngressControllerDto ingressControllerDto, Cluster cluster,
                                List<IngressControllerDto> existIcDtos, Map<String, Integer> portRangeMap) {
        AssertUtil.notBlank(ingressControllerDto.getIcName(),DictEnum.NAME);
        AssertUtil.greaterZero(ingressControllerDto.getHttpPort(), DictEnum.PORT);
        for (IngressControllerDto existIcDto : existIcDtos) {
            //自身不需要校验，已存在的和需要修改名称相同
            if (existIcDto.getIcName().equalsIgnoreCase(ingressControllerDto.getIcName())) {
                continue;
            }
            //修改的别名已经存在
            if (existIcDto.getIcAliasName().equals(ingressControllerDto.getIcAliasName())) {
                throw new MarsRuntimeException(ErrorCodeMessage.NAME_EXIST);
            }
            //修改的端口已经被使用
            if (existIcDto.getHttpPort() == ingressControllerDto.getHttpPort()) {
                throw new MarsRuntimeException(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_USED);
            }
        }
        this.validate(ingressControllerDto, cluster, portRangeMap);
    }

    private void validate(IngressControllerDto ingressControllerDto, Cluster cluster,
                          Map<String, Integer> portRangeMap) {
        //检查icPort（http）端口是否在指定范围内
        if (portRangeMap == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.INGRESS_CONTROLLER_PORT_RANGE_NOT_FOUND);
        }
        if (!checkIcHttpPort(ingressControllerDto.getHttpPort(), portRangeMap)) {
            throw new MarsRuntimeException(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_ERROR);
        }
        //检查节点是否存在以及节点类型是否闲置节点
        if (CollectionUtils.isEmpty(ingressControllerDto.getIcNodeNames())) {
            return;
        }
        List<String> oldIcNodeNames = this.getIcNodeNames(ingressControllerDto.getIcName(), cluster);
        ActionReturnUtil nodeListResponse = nodeService.listNode(cluster.getId());
        if (!nodeListResponse.isSuccess() || nodeListResponse.getData() == null) {
            throw new MarsRuntimeException(DictEnum.NODE.phrase(), ErrorCodeMessage.QUERY_FAIL);
        }
        List<NodeDto> nodeDtoList = (List) nodeListResponse.getData();
        Map<String, NodeDto> nodeDtoMap = nodeDtoList.stream().collect(Collectors.toMap(NodeDto::getName, node -> node));
        for (String nodeName : ingressControllerDto.getIcNodeNames()) {
            NodeDto node = nodeDtoMap.get(nodeName);
            //节点不存在
            if (node == null) {
                throw new MarsRuntimeException(DictEnum.NODE.phrase() + nodeName, ErrorCodeMessage.NOT_EXIST);
            }
            //节点类型非闲置节点
            if (!oldIcNodeNames.contains(nodeName) && !node.getNodeShareStatus().equals(DictEnum.NODE_IDLE.phrase())) {
                throw new MarsRuntimeException(DictEnum.NODE.phrase() + nodeName, ErrorCodeMessage.NOT_EXIST);
            }
        }
    }


    /**
     * 更新节点标签
     * 闲置节点转为负载均衡节点：增加lb,ic-name标签，删除闲置节点的标签
     * 负载均衡节点转为闲置节点：删除lb,ic-name标签，增加闲置节点的标签
     */
    private void updateNodeLabel(String icName, Cluster cluster, List<String> oldIcNodeNames,
                                 List<String> newIcNodeNames) throws MarsRuntimeException {
        Map<String, String> icLabels = new HashMap<>();
        icLabels.put(HARMONYCLOUD_STATUS_LBS, LABEL_VALUE_NGINX_CUSTOM);
        icLabels.put(LABEL_KEY_INGRESS_CONTROLLER_NAME, icName);
        Map<String, String> nodeIdleLabels = new HashMap<>();
        nodeIdleLabels.put(HARMONYCLOUD_STATUS, LABEL_STATUS_B);
        try {
            for (String nodeName : newIcNodeNames) {
                //新增节点,增加lb,ic-name标签，删除闲置节点的标签
                if (!oldIcNodeNames.contains(nodeName)) {
                    nodeService.addNodeLabels(nodeName, icLabels, cluster.getId());
                    nodeService.removeNodeLabels(nodeName, nodeIdleLabels, cluster);
                }
            }
            for (String nodeName : oldIcNodeNames) {
                //删除节点，删除lb,ic-name标签，增加闲置节点的标签
                if (!newIcNodeNames.contains(nodeName)) {
                    nodeService.addNodeLabels(nodeName, nodeIdleLabels, cluster.getId());
                    nodeService.removeNodeLabels(nodeName, icLabels, cluster);
                }
            }
        } catch (Exception e) {
            LOGGER.error("创建负载均衡失败，更新节点标签异常，icName:{}", icName, e);
            throw new MarsRuntimeException(DictEnum.NODE.phrase() + DictEnum.LABEL.phrase(),
                    ErrorCodeMessage.UPDATE_FAIL);
        }

    }

    /**
     * 获取负载均衡器部署的节点类表
     *
     * @param icName
     * @param cluster
     * @return
     */
    private List<String> getIcNodeNames(String icName, Cluster cluster) {
        String label = LABEL_KEY_INGRESS_CONTROLLER_NAME + Constant.EQUAL + icName;
        List<NodeDto> nodes = nodeService.listNodeByLabel(label, cluster);
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        return nodes.stream().map(NodeDto::getName).collect(Collectors.toList());
    }

    /**
     * 获取负载均衡器分配的租户列表
     */
    private Map<String, List<String>> getIcAssignedTenantIds(String clusterId) throws Exception {
        //List<TenantClusterQuota> tenantClusterQuotaList = tenantClusterQuotaService.getClusterQuotaByClusterId(clusterId, false);
        List<TenantClusterQuota> tenantClusterQuotaList = tenantClusterQuotaService.listClusterQuotaICs(clusterId);
        if (CollectionUtils.isEmpty(tenantClusterQuotaList)) {
            return Collections.emptyMap();
        }
        //key为负载均衡器名称，value为负载均衡器分配的租户id列表
        Map<String, List<String>> quotaMap = new HashMap<>();
        for (TenantClusterQuota tenantClusterQuota : tenantClusterQuotaList) {
            if (StringUtils.isBlank(tenantClusterQuota.getIcNames())) {
                continue;
            }
            String[] icNames = tenantClusterQuota.getIcNames().split(COMMA);
            for (String icName : icNames) {
                if (quotaMap.get(icName) == null) {
                    List<String> tenantIds = new ArrayList<>();
                    tenantIds.add(tenantClusterQuota.getTenantId());
                    quotaMap.put(icName, tenantIds);
                } else {
                    quotaMap.get(icName).add(tenantClusterQuota.getTenantId());
                }
            }
        }
        return quotaMap;
    }

    private String getIcAliasName(DaemonSet daemonSet) {
        if (daemonSet.getMetadata().getAnnotations() != null
                && daemonSet.getMetadata().getAnnotations().get(ANNOTATIONS_KEY_ALIAS_NAME) != null) {
            return daemonSet.getMetadata().getAnnotations().get(ANNOTATIONS_KEY_ALIAS_NAME).toString();
        }
        if (IC_DEFAULT_NAME.equals(daemonSet.getMetadata().getName())) {
            return IC_DEFAULT_ALIAS_NAME;
        }
        return daemonSet.getMetadata().getName();
    }

    /**
     * 是否修修改了daemonset
     */
    private boolean isDaemonsetChanged(DaemonSet daemonSet, IngressControllerDto ingressControllerDto){
        //是否修改了别名
        String icAliasName = this.getIcAliasName(daemonSet);
        if (!icAliasName.equals(ingressControllerDto.getIcAliasName())) {
            return true;
        }
        //是否修改了http端口
        int existPort = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();
        if (existPort != ingressControllerDto.getHttpPort()) {
            return true;
        }
        Map<String, Object> annotations = daemonSet.getMetadata().getAnnotations();
        if (annotations == null) {
            return false;
        }
        //是否修改了外网http端口
        Integer externalHttpPort = null;
        if (annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT) != null) {
            externalHttpPort = Integer.parseInt(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT).toString());
        }
        Integer newExternalHttpPort = ingressControllerDto.getExternalHttpPort();
        if (externalHttpPort == null && newExternalHttpPort != null) {
            return true;
        }
        if (externalHttpPort != null && newExternalHttpPort == null) {
            return true;
        }
        if (externalHttpPort != newExternalHttpPort) {
            return true;
        }
        //是否修改了外网http端口
        Integer externalHttpsPort = null;
        if (annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT) != null) {
            externalHttpsPort = Integer.parseInt(annotations.get(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT).toString());
        }
        Integer newExternalHttpsPort = ingressControllerDto.getExternalHttpsPort();
        if (externalHttpsPort == null && newExternalHttpsPort != null) {
            return true;
        }
        if (externalHttpsPort != null && newExternalHttpsPort == null) {
            return true;
        }
        if (externalHttpsPort != newExternalHttpsPort) {
            return true;
        }
        return false;
    }

    /**
     * 修改负载均衡器，更新别名和外网http，https端口annotations
     */
    private void updateAnnotations(IngressControllerDto ingressControllerDto, DaemonSet daemonSet){
        Map<String,Object> annotations = daemonSet.getMetadata().getAnnotations();
        if(annotations == null){
            annotations = new HashMap<>();
        }
        annotations.put(ANNOTATIONS_KEY_ALIAS_NAME, ingressControllerDto.getIcAliasName());
        if(ingressControllerDto.getExternalHttpPort() == null){
            annotations.remove(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT);
        }else{
            annotations.put(ANNOTATIONS_KEY_EXTERNAL_HTTP_PORT, String.valueOf(ingressControllerDto.getExternalHttpPort()));
        }
        if(ingressControllerDto.getExternalHttpsPort() == null){
            annotations.remove(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT);
        }else{
            annotations.put(ANNOTATIONS_KEY_EXTERNAL_HTTPS_PORT, String.valueOf(ingressControllerDto.getExternalHttpsPort()));
        }
        daemonSet.getMetadata().setAnnotations(annotations);
    }
    
}
