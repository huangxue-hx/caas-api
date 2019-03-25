package com.harmonycloud.service.integration.convert;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.microservice.bean.MicroServiceInstance;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.microservice.MicroServiceInstanceStatusDto;
import com.harmonycloud.service.platform.bean.microservice.MsfDeployment;
import com.harmonycloud.service.platform.bean.microservice.MsfDeploymentPort;
import com.harmonycloud.service.platform.bean.microservice.MsfDeploymentVolume;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.KubeAffinityConvert;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author jiangmi
 * @Description 封装微服务数据结构类
 * @Date created in 2017-12-12
 * @Modified
 */
public class MsfDataConvert {

    /**
     * 组装kubernetes Deployment 数据结构
     *
     * @param deployment
     * @param userName
     * @return
     * @throws Exception
     */
    public static Deployment formatAppDeploymentData(MsfDeployment deployment, String userName, String appName, String namespace, Cluster cluster, String nodeLabel) throws Exception {
        Deployment dep = new Deployment();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(deployment.getMetadata().getDeployment_name());

        //设置Deployment和pod Label
        Map<String, Object> lmMap = new HashMap<String, Object>();
        if (userName != null) {
            lmMap.put("nephele/user", userName);
        }
        lmMap.put(Constant.NODESELECTOR_LABELS_PRE + "springcloud", "true");
        //放入与应用的关系label
        lmMap.put(Constant.TOPO_LABEL_KEY + CommonConstant.LINE + appName, namespace);
        meta.setLabels(lmMap);
        Map<String, Object> labels = new HashMap<>();
        labels.put(Constant.TYPE_DEPLOYMENT, deployment.getMetadata().getDeployment_name());
        if (!StringUtils.isEmpty(deployment.getTemplate().getLabels())) {
            String[] ls = deployment.getTemplate().getLabels().split(",");
            for (String label : ls) {
                String[] tmp = label.split("=");
                labels.put(tmp[0], tmp[1]);
                lmMap.put(tmp[0], tmp[1]);
                meta.setLabels(lmMap);
            }
        }

        //设置Deployment annotation
        Map<String, Object> anno = new HashMap<String, Object>();
        anno.put("nephele/status", Constant.STARTING);
        anno.put("nephele/replicas", deployment.getSpec().getReplicas());
        anno.put("nephele/labels", deployment.getTemplate().getLabels());
        anno.put("springcloud.params/volumes", CollectionUtils.isEmpty(deployment.getVolumes()) ? null : JsonUtil.convertToJson(deployment.getVolumes()));
        anno.put("springcloud.params/ports", CollectionUtils.isEmpty(deployment.getPorts()) ? null : JsonUtil.convertToJson(deployment.getPorts()));
        meta.setAnnotations(anno);
        dep.setMetadata(meta);

        DeploymentSpec depSpec = new DeploymentSpec();
        depSpec.setReplicas(Integer.valueOf(deployment.getSpec().getReplicas()));
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("Recreate");
        depSpec.setStrategy(strategy);
        LabelSelector labelSelector = new LabelSelector();
        Map<String, Object> matchLabel = new HashMap<String, Object>();
        matchLabel.put(Constant.TYPE_DEPLOYMENT, deployment.getMetadata().getDeployment_name());
        labelSelector.setMatchLabels(matchLabel);
        depSpec.setSelector(labelSelector);
        dep.setSpec(depSpec);

        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        PodSpec podSpec = new PodSpec();
        List<Container> cs = new ArrayList<Container>();
        List<MsfDeploymentVolume> volumeList = deployment.getVolumes();
        List<VolumeMount> volumeMounts = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(volumeList)) {
            Map<String, Object> volumeRes = convertContainerVolume(volumeList, deployment.getMetadata().getDeployment_name());
            List<Volume> volumes = (List<Volume>) volumeRes.get("volume");
            volumeMounts = (List<VolumeMount>) volumeRes.get("volumeMount");
            podSpec.setVolumes(volumes);
        }
        Container container = formatContainer(deployment, cluster);
        if (CollectionUtils.isNotEmpty(volumeMounts)) {
            container.setVolumeMounts(volumeMounts);
        }
        cs.add(container);
        podSpec.setContainers(cs);
        podSpec.setRestartPolicy("Always");
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(CommonConstant.ADMIN + "-secret");
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);

        //设置分区对应的独占或共享标签
        String labelList = StringUtils.isNotBlank(nodeLabel) ? nodeLabel : null;
        if (Objects.nonNull(deployment.getAffinity()) && StringUtils.isNotEmpty(deployment.getAffinity().get("node_affinity"))) {
            labelList = StringUtils.isBlank(labelList) ? deployment.getAffinity().get("node_affinity") : CommonConstant.COMMA + deployment.getAffinity().get("node_affinity");
        }
        podSpec.setAffinity(convertAffinity(labelList));
        ObjectMeta metadata = new ObjectMeta();
        metadata.setLabels(labels);
        podTemplateSpec.setMetadata(metadata);
        podTemplateSpec.setSpec(podSpec);
        depSpec.setTemplate(podTemplateSpec);
        dep.setSpec(depSpec);
        return dep;
    }

    /**
     * 封装亲和度
     *
     * @param nodeLabel
     * @return
     * @throws Exception
     */
    public static Affinity convertAffinity(String nodeLabel) throws Exception {
        Affinity affinity = new Affinity();
        NodeAffinity na = new NodeAffinity();

        //按照逗号分隔
        String[] nodeLabels = nodeLabel.split(CommonConstant.COMMA);
        List<NodeSelectorRequirement> nodeSelectorTerms = new ArrayList<>();
        for (String nl : nodeLabels) {
            nodeSelectorTerms.add(KubeAffinityConvert.convertNodeSelectorTerm(nl));
        }
        //强制执行
        NodeSelector ns = new NodeSelector();
        List<NodeSelectorTerm> nstList = new ArrayList<>();
        NodeSelectorTerm nodeSelectors = new NodeSelectorTerm();
        nodeSelectors.setMatchExpressions(nodeSelectorTerms);
        nstList.add(nodeSelectors);
        ns.setNodeSelectorTerms(nstList);
        na.setRequiredDuringSchedulingIgnoredDuringExecution(ns);
        affinity.setNodeAffinity(na);
        return affinity;
    }

    /**
     * 组装kubernetes Container内Volume相关数据
     *
     * @param msfVolumes
     * @param name
     * @return
     * @throws Exception
     */
    public static Map<String, Object> convertContainerVolume(List<MsfDeploymentVolume> msfVolumes, String name) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Volume> volumes = new ArrayList<Volume>();
        List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
        for (int i = 0; i < msfVolumes.size(); i++) {
            MsfDeploymentVolume v = msfVolumes.get(i);
            String fileUrl = v.getFile_url();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Volume cMap = new Volume();
            cMap.setName((fileName + "v1.0").replace(".", "-"));
            ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
            coMap.setName(name + i);
            List<KeyToPath> items = new LinkedList<KeyToPath>();
            KeyToPath key = new KeyToPath();
            key.setKey(fileName + "v1.0");
            key.setPath(fileName);
            items.add(key);
            coMap.setItems(items);
            cMap.setConfigMap(coMap);
            volumes.add(cMap);
            VolumeMount volm = new VolumeMount();
            volm.setName((fileName + "v1.0").replace(".", "-"));
            volm.setMountPath(v.getMount_path());
            volm.setSubPath(fileName);
            volumeMounts.add(volm);
        }
        result.put("volume", volumes);
        result.put("volumeMount", volumeMounts);
        return result;
    }

    /**
     * 组装除Volume外的Container数据
     *
     * @param deployment
     * @return
     * @throws Exception
     */
    public static Container formatContainer(MsfDeployment deployment, Cluster cluster) throws Exception {
        Container container = new Container();
        container.setName(deployment.getMetadata().getDeployment_name());
        container.setImage(cluster.getHarborServer().getHarborAddress() + "/" + deployment.getTemplate().getRepo() + "/" + deployment.getTemplate().getImage());
        //设置容器端口
        List<ContainerPort> ps = new ArrayList<ContainerPort>();
        for (MsfDeploymentPort p : deployment.getPorts()) {
            ContainerPort port = new ContainerPort();
            port.setContainerPort(Integer.valueOf(p.getContainer_port()));
            port.setProtocol(Constant.PROTOCOL_TCP);
            if (Constant.EXTERNAL_PROTOCOL_UDP.equals(p.getExternal_type())) {
                port.setProtocol(Constant.PROTOCOL_UDP);
            }
            ps.add(port);
        }
        container.setPorts(ps);

        //设置容器环境变量
        List<EnvVar> envVars = new ArrayList<EnvVar>();
        Map<String, String> env = deployment.getEnvironment();
        if (env != null) {
            for (Map.Entry<String, String> tmp : env.entrySet()) {
                EnvVar eVar = new EnvVar();
                eVar.setName(tmp.getKey());
                eVar.setValue(tmp.getValue());
                envVars.add(eVar);
            }
        }
        container.setEnv(envVars);

        //设置资源
        ResourceRequirements limit = new ResourceRequirements();
        Map<String, String> res = new HashMap<String, String>();
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Double cpu = Double.valueOf(deployment.getSpec().getCpu()) * Constant.CONTAINER_RESOURCE_CPU_TIMES;
        res.put("cpu", cpu.intValue() + "m");
        Matcher mm = p.matcher(deployment.getSpec().getMemory());
        String resultm = mm.replaceAll("").trim();
        res.put("memory", resultm + "Mi");
        limit.setLimits(res);
        limit.setRequests(res);
        container.setResources(limit);

        container.setImagePullPolicy("Always");
        return container;
    }

    /**
     * 将请求参数组装成kubernetes Service对象
     *
     * @param dep
     * @return
     * @throws Exception
     */
    public static com.harmonycloud.k8s.bean.Service formatAppServiceData(MsfDeployment dep) throws Exception {
        com.harmonycloud.k8s.bean.Service service = new com.harmonycloud.k8s.bean.Service();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(dep.getSpec().getService_name());
        Map<String, Object> labels = new HashMap<String, Object>();
        labels.put("app", dep.getMetadata().getDeployment_name());
        meta.setLabels(labels);
        ServiceSpec ss = new ServiceSpec();
        Map<String, Object> selector = new HashMap<String, Object>();
        selector.put("app", dep.getMetadata().getDeployment_name());
        ss.setSelector(selector);
        List<ServicePort> spList = new ArrayList<ServicePort>();
        for (int i = 0; i < dep.getPorts().size(); i++) {
            MsfDeploymentPort p = dep.getPorts().get(i);
            Integer type = Integer.valueOf(p.getExternal_type());
            ServicePort sPort = new ServicePort();
            sPort.setProtocol(Constant.PROTOCOL_TCP);
            if (type == Constant.SPRINGCLOUD_PROTOCOL_UDP) {
                sPort.setProtocol(Constant.PROTOCOL_UDP);
            }
            sPort.setPort(Integer.valueOf(p.getService_port()));
            sPort.setName(p.getName());
            spList.add(sPort);
        }
        ss.setPorts(spList);
        service.setSpec(ss);
        service.setMetadata(meta);
        return service;
    }

    /**
     * 根据kubernetes Deployment查询微服务组件实例状态
     *
     * @param dep
     * @return
     * @throws Exception
     */
    public static String getAppStatus(Deployment dep) throws Exception {
        String status = null;
        ObjectMeta meta = dep.getMetadata();
        if (meta.getAnnotations() != null && meta.getAnnotations().containsKey("nephele/status")) {
            String state = meta.getAnnotations().get("nephele/status").toString();
            switch (state) {
                case Constant.STARTING:
                    if (dep.getSpec().getReplicas() != null && dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getReplicas() != null && dep.getStatus().getReplicas() == dep.getStatus().getAvailableReplicas()) {
                        status = Constant.START;
                    } else {
                        status = Constant.STARTING;
                    }
                    break;
                case Constant.STOP:
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                        status = Constant.STOPPING;
                    } else {
                        status = Constant.STOP;
                    }
                    break;
                default:
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                        status = Constant.START;
                    } else {
                        status = Constant.STOP;
                    }
                    break;
            }
        } else {
            if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                status = Constant.START;
            } else {
                status = Constant.STOP;
            }
        }
        return status;
    }

    /**
     * 获取微服务consul组件的UDP对外端口
     *
     * @param deployment
     * @return
     * @throws Exception
     */
    public static String getConsulPort(MsfDeployment deployment) throws Exception {
        String consulPort = null;
        List<MsfDeploymentPort> ports = deployment.getPorts();
        for (MsfDeploymentPort port : ports) {
            if (Constant.EXTERNAL_PROTOCOL_UDP.equals(port.getExternal_type())) {
                consulPort = port.getExpose_port();
            }
        }
        return consulPort;
    }

    /**
     * 微服务实例详情参数转化
     *
     * @param dep
     * @param podList
     * @param svc
     * @param msfIns
     * @return
     * @throws Exception
     */
    public static MicroServiceInstanceStatusDto convertMsfInstanceStatusDetail(Deployment dep, PodList podList, Service svc, MicroServiceInstance msfIns, List<Map<String, Object>> externalInfo) throws Exception {
        MicroServiceInstanceStatusDto msfInstanceDetail = new MicroServiceInstanceStatusDto();
        List<Container> containerList = dep.getSpec().getTemplate().getSpec().getContainers();

        msfInstanceDetail.setName(dep.getMetadata().getName());
        msfInstanceDetail.setStatus(getAppStatus(dep));
        msfInstanceDetail.setService_name(msfIns.getServiceName());
        msfInstanceDetail.setNamespace(dep.getMetadata().getNamespace());
        msfInstanceDetail.setSpace_id(msfIns.getNamespaceId());
        msfInstanceDetail.setCreate_time(dep.getMetadata().getCreationTimestamp());
        msfInstanceDetail.setUpdate_time(DateUtil.DateToString(msfIns.getUpdateTime(), DateStyle.YYYY_MM_DD_HH_MM_SS));
        msfInstanceDetail.setReplicas(msfIns.getReplicas().toString());

        //获取label信息
        String labels = "";
        for (Map.Entry<String, Object> m : dep.getMetadata().getLabels().entrySet()) {
            if (m.getKey().indexOf("nephele/") > 0) {
                labels += m.getKey() + "=" + m.getValue() + ',';
            }
        }
        if (StringUtils.isNotEmpty(labels)) {
            labels = labels.substring(0, labels.length() - 1);
        }
        msfInstanceDetail.setLabels(labels);
        Map<String, Object> annotation = new HashMap<String, Object>();
        annotation = dep.getMetadata().getAnnotations();
        List<MsfDeploymentVolume> volumeParams = StringUtils.isBlank(annotation.get("springcloud.params/volumes").toString()) ? null : JsonUtil.jsonToList(annotation.get("springcloud.params/volumes").toString(), MsfDeploymentVolume.class);
        List<MsfDeploymentPort> portParams = StringUtils.isBlank(annotation.get("springcloud.params/ports").toString()) ? null : JsonUtil.jsonToList(annotation.get("springcloud.params/ports").toString(), MsfDeploymentPort.class);
        msfInstanceDetail.setVolumes(volumeParams);
        msfInstanceDetail.setPorts(portParams);

        //亲和度
        Map<String, String> affinity = new HashMap<String, String>();
        String nodeAff = "";
        if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity())) {
            if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity())) {
                if (CollectionUtils.isNotEmpty(dep.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity().getPreferredDuringSchedulingIgnoredDuringExecution())) {
                    List<PreferredSchedulingTerm> psts = dep.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity().getPreferredDuringSchedulingIgnoredDuringExecution();
                    for (PreferredSchedulingTerm pst : psts) {
                        List<NodeSelectorRequirement> nsqs = pst.getPreference().getMatchExpressions();
                        if (CollectionUtils.isNotEmpty(nsqs)) {
                            for (NodeSelectorRequirement nsq : nsqs) {
                                if (Objects.nonNull(nsq)) {
                                    nodeAff += nsq.getKey() + "=" + nsq.getValues() + ',';
                                    if (StringUtils.isNotEmpty(nodeAff)) {
                                        nodeAff = nodeAff.substring(0, nodeAff.length() - 1);
                                    }
                                }
                            }
                        }
                    }
                }
                affinity.put("node_affinity", nodeAff);
            }
        }
        msfInstanceDetail.setAffinity(affinity);
        List<Map<String, Object>> containers = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(containerList)) {
            for (Container container : containerList) {
                Map<String, Object> resContainer = new HashMap<>();
                resContainer.put("name", container.getName());
                resContainer.put("image", container.getImage());
                resContainer.put("cpu", msfIns.getCpu());
                resContainer.put("memory", msfIns.getMemory());
                List<Map<String, String>> environments = new ArrayList<>();
                List<EnvVar> envs = container.getEnv();
                if (CollectionUtils.isNotEmpty(envs)) {
                    for (EnvVar env : envs) {
                        Map<String, String> msfEnv = new HashMap<String, String>();
                        msfEnv.put("key", env.getName());
                        msfEnv.put("value", env.getValue());
                        environments.add(msfEnv);
                    }
                }
                resContainer.put("environments", environments);
                containers.add(resContainer);
            }
        }
        //获取pod信息
        List<Map<String, Object>> pods = new ArrayList<>();
        for (int i = 0; i < podList.getItems().size(); i++) {
            Pod pod = podList.getItems().get(i);
            Map<String, Object> podMap = new HashMap<>();
            podMap.put("name", pod.getMetadata().getName());
            podMap.put("pod_ip", pod.getStatus().getPodIP());
            podMap.put("node_ip", pod.getStatus().getHostIP());
            podMap.put("start_time", pod.getStatus().getStartTime());
            podMap.put("status", pod.getStatus().getPhase());
            pods.add(podMap);
        }
        msfInstanceDetail.setPods(pods);
        msfInstanceDetail.setContainers(containers);
        msfInstanceDetail.setExternal_services(externalInfo);
        msfInstanceDetail.setInstance_id(msfIns.getInstanceId());
        return msfInstanceDetail;
    }

    /**
     * 将微服务平台传入的参数转成容器云平台的服务模板内的deployment对象
     *
     * @param depName
     * @param namespace
     * @param msfDep
     * @return DeploymentDetailDto
     * @throws Exception
     */
    public static DeploymentDetailDto convertMsfToServiceTemaplate(String depName, String namespace, MsfDeployment msfDep) throws Exception {
        DeploymentDetailDto depDetail = new DeploymentDetailDto();
        depDetail.setName(depName);
        depDetail.setNamespace(namespace);
        depDetail.setInstance(msfDep.getSpec().getReplicas());
        depDetail.setRestartPolicy("Always");
        List<CreateContainerDto> createContainerList = new ArrayList<>();
        CreateContainerDto createContainer = new CreateContainerDto();
        createContainer.setName(depName);
        createContainer.setImg(msfDep.getTemplate().getRepo() + "/" + msfDep.getTemplate().getImage());
        if (msfDep.getTemplate().getImage().indexOf(":") > -1) {
            String image = msfDep.getTemplate().getImage().substring(0, msfDep.getTemplate().getImage().indexOf(":"));
            createContainer.setImg(msfDep.getTemplate().getRepo() + "/" + image);
            createContainer.setTag(msfDep.getTemplate().getImage().substring(msfDep.getTemplate().getImage().indexOf(":") + 1));
        }
        CreateResourceDto resourceDto = new CreateResourceDto();
        resourceDto.setMemory(StringUtils.isBlank(msfDep.getSpec().getMemory()) ? Constant.SPRINGCLOUD_INSTANCE_MEMORY : msfDep.getSpec().getMemory());
        resourceDto.setCpu(StringUtils.isBlank(msfDep.getSpec().getCpu()) ? Constant.SPRINGCLOUD_INSTANCE_CPU : msfDep.getSpec().getCpu());
        createContainer.setResource(resourceDto);
        List<CreatePortDto> portList = new ArrayList<>();
        List<MsfDeploymentPort> msfPortList = msfDep.getPorts();
        for (MsfDeploymentPort p : msfPortList) {
            CreatePortDto port = new CreatePortDto();
            port.setPort(p.getContainer_port());
            port.setProtocol(Constant.PROTOCOL_TCP);
            if (Constant.EXTERNAL_PROTOCOL_UDP.equals(p.getExternal_type())) {
                port.setProtocol(Constant.PROTOCOL_UDP);
            }
            portList.add(port);
        }
        createContainer.setPorts(portList);
        createContainer.setImagePullPolicy("Always");
        SecurityContextDto securityContext = new SecurityContextDto();
        securityContext.setSecurity(false);
        securityContext.setPrivileged(false);
        createContainer.setSecurityContext(securityContext);
        createContainerList.add(createContainer);
        depDetail.setContainers(createContainerList);
        return depDetail;
    }
}
