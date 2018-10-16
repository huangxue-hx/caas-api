package com.harmonycloud.service.cluster.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
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
import com.harmonycloud.k8s.service.IcService;
import com.harmonycloud.k8s.service.ServiceAccountService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.IngressControllerPortService;
import com.harmonycloud.service.cluster.IngressControllerService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.TenantClusterQuotaService;
import com.harmonycloud.service.tenant.TenantService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.IngressControllerConstant.*;
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
    ClusterService clusterService;

    @Autowired
    IcService icService;

    @Autowired
    IngressControllerPortService ingressControllerPortService;

    @Autowired
    ServiceAccountService serviceAccountService;

    @Autowired
    TenantService tenantService;

    @Autowired
    TenantClusterQuotaService tenantClusterQuotaService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Override
    public ActionReturnUtil listIngressController(String clusterId) throws Exception {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SClientResponse response = icService.listIngressController(cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        DaemonSetList daemonSetList = JsonUtil.jsonToPojo(response.getBody(), DaemonSetList.class);

        List<IngressControllerDto> ingressControllerDtoList = new ArrayList<>();
        for (DaemonSet daemonSet : daemonSetList.getItems()) {
            IngressControllerDto ingressControllerDto = new IngressControllerDto();
            ingressControllerDto.setClusterAliasName(cluster.getAliasName());
            ingressControllerDto.setClusterId(clusterId);
            ingressControllerDto.setIcName(daemonSet.getMetadata().getName());
            ingressControllerDto.setNamespace(daemonSet.getMetadata().getNamespace());
            //设定tenant租户名称
            List<String> tenantIdList = new ArrayList<>();
            List<TenantClusterQuota> tenantClusterQuotaList = tenantClusterQuotaService.getClusterQuotaByClusterId(clusterId, false);
            if (CollectionUtils.isNotEmpty(tenantClusterQuotaList)) {
                for (TenantClusterQuota tenantClusterQuota : tenantClusterQuotaList) {
                    if(StringUtils.isNotEmpty(tenantClusterQuota.getIcNames())) {
                        String[] icNameArray = tenantClusterQuota.getIcNames().split(",");
                        if (ArrayUtils.isNotEmpty(icNameArray)) {
                            for (String icNameExample : icNameArray) {
                                if (ingressControllerDto.getIcName().equals(icNameExample)) {
                                    tenantIdList.add(tenantClusterQuota.getTenantId());
                                }
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(tenantIdList)) {
                ingressControllerDto.setTenantInfo(buildTenantInfoByTenantId(tenantIdList));
            }
            List<ContainerPort> containerPortList = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts();
            ingressControllerDto.setHttpPort(containerPortList.get(0).getContainerPort());
            List<String> icPorts = new ArrayList<>();
            for (ContainerPort containerPort : containerPortList) {
                icPorts.add(containerPort.getProtocol() + ": " + containerPort.getContainerPort());
            }
            ingressControllerDto.setIcPort(StringUtils.join(icPorts.toArray(), ","));
            if (daemonSet.getStatus() != null && daemonSet.getStatus().getDesiredNumberScheduled() != null && daemonSet.getStatus().getNumberAvailable() != null && daemonSet.getStatus().getDesiredNumberScheduled().equals(daemonSet.getStatus().getNumberAvailable())) {
                ingressControllerDto.setStatus(Constant.SERVICE_START);
            } else {
                ingressControllerDto.setStatus(Constant.SERVICE_STARTING);
            }
            if (Constant.INGRESS_CONTROLLER_DEFAULT_NAME.equals(daemonSet.getMetadata().getName())) {
                ingressControllerDto.setIsDefault(true);
            } else {
                ingressControllerDto.setIsDefault(false);
            }
            ingressControllerDto.setCreateTime(DateUtil.StringToDate(daemonSet.getMetadata().getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
            ingressControllerDtoList.add(ingressControllerDto);
        }

        return ActionReturnUtil.returnSuccessWithData(ingressControllerDtoList);
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
    public ActionReturnUtil createIngressController(String clusterId, String icName, int icPort) throws MarsRuntimeException, IOException {
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断Ingress-controller是否存在
        K8SClientResponse response = icService.getIngressController(icName, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_EXIST);
        }
        //获取Ingress-controller-port范围
        Map<String, Integer> portRangeMap = getIngressControllerPortRange(cluster);
        if (portRangeMap == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_PORT_RANGE_NOT_FOUND);
        }
        //检查icPort（http）端口是否在指定范围内
        if (!checkIcHttpPort(icPort, portRangeMap)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_ERROR);
        }
        //获取Ingress-controller-port已使用的
        List<IngressControllerPort> icPortList = checkIcHttpPortAndReturnIcUsedPort(icPort, clusterId);
        if (icPortList == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_USED);
        }
        //构建Ingress-controller-port端口
        IngressControllerPort ingressControllerPort = buildIngressControllerPort(icName, icPort, portRangeMap, icPortList, clusterId);
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
        DaemonSet daemonSet = buildIngressController(ingressControllerPort, serviceAccount);
        //创建tcp、udp配置文件
        ConfigMap tcpConfigMap = buildIcConfigMap(icName, "tcp");
        K8SClientResponse tcpCmResponse = icService.createIcConfigMap(tcpConfigMap, cluster);
        if (!HttpStatusUtil.isSuccessStatus(tcpCmResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(tcpCmResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        ConfigMap udpConfigMap = buildIcConfigMap(icName, "udp");
        K8SClientResponse udpCmResponse = icService.createIcConfigMap(udpConfigMap, cluster);
        if (!HttpStatusUtil.isSuccessStatus(udpCmResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(udpCmResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //创建Ingress-controller
        K8SClientResponse createResponse= icService.createIngressController(daemonSet, cluster);
        if (!HttpStatusUtil.isSuccessStatus(createResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //创建完成后，更新Ingress-controller-port表
        ingressControllerPortService.createIngressControllerPort(ingressControllerPort);

        return ActionReturnUtil.returnSuccess();
    }

    //构建ingress controller对象
    private DaemonSet buildIngressController(IngressControllerPort ingressControllerPort, ServiceAccount serviceAccount) {
        DaemonSet daemonSet = new DaemonSet();
        //daemonSet.metadata
        ObjectMeta objectMeta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(LABEL_KEY_NAME, ingressControllerPort.getName());
        labels.put(LABEL_MARK_KEY, LABEL_MARK_VALUE);
        objectMeta.setLabels(labels);
        objectMeta.setNamespace(Constant.NAMESPACE_SYSTEM);
        objectMeta.setName(ingressControllerPort.getName());
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
        nodeSelector.put(TEMPLATE_SPEC_NODESELECTOR_KEY, TEMPLATE_SPEC_NODESELECTOR_VALUE);
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
    private IngressControllerPort buildIngressControllerPort(String icName,
                                                             int icPort, Map<String, Integer> portRangeMap,
                                                             List<IngressControllerPort> icPortList,
                                                             String clusterId) {
        IngressControllerPort ingressControllerPort = new IngressControllerPort();
        ingressControllerPort.setName(icName);
        ingressControllerPort.setClusterId(clusterId);
        ingressControllerPort.setHttpPort(icPort);
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
    private int getUnusedPort(int minPort, int maxPort, List<IngressControllerPort> icPortList, String usedType) {
        int unUsedPort = 0;
        for (int i = minPort; i <= maxPort; i++) {
            boolean flag = true;
            for (IngressControllerPort ingressControllerPort : icPortList) {
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

    //获取ingressController已使用端口
    private List<IngressControllerPort> checkIcHttpPortAndReturnIcUsedPort(int icPort, String clusterId) {
        List<IngressControllerPort> icPortList = new ArrayList<>();
        icPortList = ingressControllerPortService.listIngressControllerPortByClusterId(clusterId);
        if (CollectionUtils.isNotEmpty(icPortList)) {
            for (IngressControllerPort icPortExample : icPortList) {
                if (icPort == icPortExample.getHttpPort()) {
                    return null;
                }
            }
        }
        return icPortList;
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
        if (Constant.INGRESS_CONTROLLER_DEFAULT_NAME.equals(icName)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_DEFAULT_NOT_DELETE);
        }
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        //查看是否被占用
        if(checkIcUsedStatus(icName, cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HAD_USED);
        }
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //删除ingressController
        K8SClientResponse response = icService.deleteIngressController(icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //删除ingressController相关的configMap
        K8SClientResponse response_tcp = icService.deleteConfigMap("tcp-" + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_tcp.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        K8SClientResponse response_udp = icService.deleteConfigMap("udp-" + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_udp.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        K8SClientResponse response_leader = icService.deleteConfigMap("ingress-controller-leader-" + icName, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response_leader.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //删除表记录
        ingressControllerPortService.deleteIngressControllerPort(icName, clusterId);
        //删除tenant_cluster_quota表绑定
        List<TenantClusterQuota> tenants = tenantClusterQuotaService.listClusterQuotaLikeIcName(icName,clusterId);
        for (TenantClusterQuota tenantClusterQuota:tenants) {
            removeTenants(tenantClusterQuota,icName);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateIngressController(String icName, int icPort, String clusterId) throws Exception {
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
        //获取Ingress-controller-port已使用的
        List<IngressControllerPort> icPortList = checkIcHttpPortAndReturnIcUsedPort(icPort, clusterId);
        if (icPortList == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_USED);
        }
        //检查icPort（http）端口是否在指定范围内
        if (!checkIcHttpPort(icPort, portRangeMap)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HTTP_PORT_ERROR);
        }
        //查看当前controller是否被占用
        if(checkIcUsedStatus(icName, cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_HAD_USED);
        }
        //获取原配置文件
        K8SClientResponse response = icService.getIngressController(icName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        DaemonSet daemonSet = JsonUtil.jsonToPojo(response.getBody(), DaemonSet.class);
        //修改hostPort
        daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).setHostPort(icPort);
        //修改containerPort
        daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getPorts().get(0).setContainerPort(icPort);
        //修改http_port
        daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getArgs().set(1, CONTAINER_ARGS_HTTP + icPort);

        K8SClientResponse icResponse = icService.updateIngressController(icName, daemonSet, cluster);
        if(!HttpStatusUtil.isSuccessStatus(icResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        //修改端口
        List<IngressControllerPort> ingressControllerPortList = ingressControllerPortService.listIngressControllerPortByName(icName);
        for (IngressControllerPort ingressControllerPort : ingressControllerPortList) {
            if (clusterId.equals(ingressControllerPort.getClusterId())) {
                ingressControllerPort.setHttpPort(icPort);
                ingressControllerPortService.updateIngressControllerPort(ingressControllerPort);
                return ActionReturnUtil.returnSuccess();
            }
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.INGRESS_CONTROLLER_PORT_UPDATE_FAIL);
    }

    //查看ingress-controller是否被分配
    private Boolean checkIcAssignStatus(String icName, String clusterId) throws Exception {
        List<TenantClusterQuota> tenantClusterQuotaList = tenantClusterQuotaService.getClusterQuotaByClusterId(clusterId, false);
        if (CollectionUtils.isNotEmpty(tenantClusterQuotaList)) {
            for (TenantClusterQuota tenantClusterQuota : tenantClusterQuotaList) {
                if (StringUtils.isNotEmpty(tenantClusterQuota.getIcNames())) {
                    String[] icNameArray = tenantClusterQuota.getIcNames().split(",");
                    if (ArrayUtils.isNotEmpty(icNameArray)) {
                        for (String icNameExample : icNameArray) {
                            if (icName.equals(icNameExample)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
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

    //查看ingress-controller是否被使用
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
        //如果后台查到的租户为空，则对该负载均衡添加租户组tenants
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
}
