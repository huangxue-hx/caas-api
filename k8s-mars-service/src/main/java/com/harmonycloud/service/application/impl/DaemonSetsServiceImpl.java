package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.DaemonSetsService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.SecretService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.PodDetail;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.service.platform.constant.Constant.NODESELECTOR_LABELS_PRE;

/**
 * @Author jiangmi
 * @Description daemonset业务的接口实现
 * @Date created in 2017-12-18
 * @Modified
 */
@Service
public class DaemonSetsServiceImpl implements DaemonSetsService {

    @Autowired
    private ConfigMapService configMapService;

    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    private DaemonSetService dsService;

    @Autowired
    private ConfigmapService configmapService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private PvService pvService;

    @Autowired
    private PodService podService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private EventService eventService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private UserService userService;

    @Autowired
    SecretService secretService;

    @Override
    public ActionReturnUtil createDaemonSet(DaemonSetDetailDto detail, String username) throws Exception {

        //参数检验
        if (Objects.isNull(detail) || StringUtils.isEmpty(detail.getName()) || null == detail.getClusterId()) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        if (CollectionUtils.isEmpty(detail.getContainers())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //查询集群
        Cluster cluster = clusterService.findClusterById(detail.getClusterId());
        //重名校验
        boolean isNameExist = checkDaemonSetName(detail.getName(), Constant.NAMESPACE_SYSTEM, cluster);
        if (isNameExist) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.DAEMONSET_EXIST);
        }
        //判断是否有admin-secret
        ActionReturnUtil actionReturnUtil = this.secretService.doSecret(Constant.NAMESPACE_SYSTEM, cluster.getHarborServer().getHarborAdminAccount(),
                cluster.getHarborServer().getHarborAdminPassword(), cluster);
        if (!actionReturnUtil.isSuccess()) {
            return actionReturnUtil;
        }
        //添加自定义节点标签的前缀
        if(StringUtils.isNotBlank(detail.getNodeSelector())
                && !detail.getNodeSelector().startsWith(NODESELECTOR_LABELS_PRE)){
            detail.setNodeSelector(NODESELECTOR_LABELS_PRE + detail.getNodeSelector());
        }
        for (CreateContainerDto c : detail.getContainers()) {
            if (StringUtils.isEmpty(c.getName())) {
                throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
            }
            if (Objects.nonNull(c.getConfigmap()) && c.getConfigmap().size() > 0) {
                //创建configmap
                configMapService.createConfigMap(c.getConfigmap(), detail.getNamespace(), c.getName(),
                        detail.getName(), cluster, Constant.TYPE_DAEMONSET, username);
            }

            //创建PV
            if (Objects.nonNull(c.getStorage()) && c.getStorage().size() > 0) {
                for (PersistentVolumeDto volume : c.getStorage()) {
                    if (Constant.VOLUME_TYPE_PV.equals(volume.getType())) {
                        if (StringUtils.isEmpty(volume.getPvcName())) {
                            continue;
                        }
                        volume.setNamespace(detail.getNamespace());
                        volume.setServiceType(Constant.TYPE_DAEMONSET);
                        volume.setServiceName(detail.getName());
                        volumeSerivce.createVolume(volume);
                    }
                }
            }
            c.setImg(cluster.getHarborServer().getHarborHost() + "/" + c.getImg());
        }
        //创建组装DaemonSet
        DaemonSet daemonSet = new DaemonSet();
        detail.setNamespace(Constant.NAMESPACE_SYSTEM);
        daemonSet = K8sResultConvert.convertDaemonSetCreate(detail, username);

        //创建DaemonSet
        dsService.addDaemonSet(detail.getNamespace(), daemonSet, cluster);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updateDaemonSet(DaemonSetDetailDto detail, String username) throws Exception {
        if (Objects.isNull(detail) || StringUtils.isBlank(detail.getName()) || StringUtils.isBlank(detail.getNamespace()) || StringUtils.isBlank(detail.getClusterId())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //查询集群
        Cluster cluster = clusterService.findClusterById(detail.getClusterId());

        //获取DaemonSet
        DaemonSet ds = dsService.getDaemonSet(detail.getNamespace(), detail.getName(), cluster);
        if (ds != null) {
            //添加updateTime
            Map<String, Object> anno = ds.getMetadata().getAnnotations();
            Date updateTime = DateUtil.getCurrentUtcTime();
            String updateTimestamp = DateUtil.UTC_FORMAT.format(updateTime);
            anno.put("updateTimestamp", updateTimestamp);
            ds.getMetadata().setAnnotations(anno);
            //更新容器
            Map<String, Object> map = K8sResultConvert.convertContainer(detail.getContainers(), detail.getLogService(), detail.getLogPath(), detail.getName());
            List<Container> containers = (List<Container>) map.get("container");
            ds.getSpec().getTemplate().getSpec().setContainers(containers);

            //更新nodeselector
            if(StringUtils.isNotBlank(detail.getNodeSelector())
                    && !detail.getNodeSelector().startsWith(NODESELECTOR_LABELS_PRE)){
                detail.setNodeSelector(NODESELECTOR_LABELS_PRE + detail.getNodeSelector());
            }
            Map<String, Object> nodeSelector = K8sResultConvert.convertNodeSelector(detail.getNodeSelector());
            ds.getSpec().getTemplate().getSpec().setNodeSelector(nodeSelector);

            //更新策略
            DaemonSetUpdateStrategy updateStrategy = new DaemonSetUpdateStrategy();
            updateStrategy.setType("RollingUpdate");
            RollingUpdateDaemonSet rollingUpdateDaemonSet = new RollingUpdateDaemonSet();
            rollingUpdateDaemonSet.setMaxUnavailable(Constant.ROLLINGUPDATE_MAX_UNAVAILABLE);
            updateStrategy.setRollingUpdate(rollingUpdateDaemonSet);
            ds.getSpec().setUpdateStrategy(updateStrategy);

            //更新volume
            List<Volume> volumes = (List<Volume>) map.get("volume");
            ds.getSpec().getTemplate().getSpec().setVolumes(volumes);

            //update DaemonSet
            dsService.updateDaemonSet(detail.getNamespace(), detail.getName(), ds, cluster);
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DAEMONSET_NOT_EXIST);
        }
    }

    @Override
    public ActionReturnUtil getDaemonSetDetail(String name, String namespace, String username, String clusterId) throws Exception {

        //参数校验
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //查询集群
        Cluster cluster = clusterService.findClusterById(clusterId);

        //获取DaemonSet
        DaemonSet ds = dsService.getDaemonSet(namespace, name, cluster);
        if (Objects.nonNull(ds) && Objects.nonNull(ds.getMetadata()) && !StringUtils.isEmpty(ds.getMetadata().getName())) {
            DaemonSetDetailDto detail = K8sResultConvert.convertDaemonSetDetail(ds);
            detail.setAliasName(StringUtils.isNotBlank(detail.getCreator())? userService.getUser(detail.getCreator()).getRealName() : null);
            PodSpec podSpec = ds.getSpec().getTemplate().getSpec();
            //容器
            List<Container> containers = podSpec.getContainers();
            List<CreateContainerDto> cs = new ArrayList<>();
            detail.setContainers(cs);
            for (Container container : containers) {
                CreateContainerDto c = new CreateContainerDto();
                //名称
                c.setName(container.getName());
                //镜像
                c.setImg(container.getImage());
                //镜像拉取策略
                c.setImagePullPolicy(container.getImagePullPolicy());
                //端口
                List<ContainerPort> ports = container.getPorts();
                List<CreatePortDto> pos = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(ports)) {
                    for (ContainerPort port : ports) {
                        CreatePortDto p = new CreatePortDto();
                        p.setPort(port.getContainerPort() + "");
                        p.setProtocol(p.getProtocol());
                        pos.add(p);
                    }
                }
                c.setPorts(pos);
                //args
                if (CollectionUtils.isNotEmpty(container.getArgs())) {
                    c.setArgs(container.getArgs());
                }
                //command
                if (CollectionUtils.isNotEmpty(container.getCommand())) {
                    c.setCommand(container.getCommand());
                }
                //env
                if (CollectionUtils.isNotEmpty(container.getEnv())) {
                    List<CreateEnvDto> envs = new ArrayList<>();
                    for (EnvVar env : container.getEnv()) {
                        CreateEnvDto e = new CreateEnvDto();
                        e.setKey(env.getName());
                        e.setValue(env.getValue());
                        envs.add(e);
                    }
                    c.setEnv(envs);
                }
                //livenessProbe
                if (Objects.nonNull(container.getLivenessProbe())) {
                    Probe livenessProbe = new Probe();
                    livenessProbe = K8sResultConvert.convertProdeDto(container.getLivenessProbe());
                    c.setLivenessProbe(livenessProbe);
                }
                //readinessProbe
                if (Objects.nonNull(container.getReadinessProbe())) {
                    Probe readinessProbe = new Probe();
                    readinessProbe = K8sResultConvert.convertProdeDto(container.getReadinessProbe());
                    c.setReadinessProbe(readinessProbe);
                }
                //resources
                if (Objects.nonNull(container.getResources())) {
                    CreateResourceDto rs = new CreateResourceDto();
                    Map<String, Object> map = (Map<String, Object>) container.getResources().getLimits();
                    if (map != null) {
                        rs.setCpu(map.get("cpu").toString());
                        rs.setMemory(map.get("memory").toString());
                        c.setResource(rs);
                    }
                }
                //securityContext
                if (Objects.nonNull(container.getSecurityContext())) {
                    SecurityContextDto sc = new SecurityContextDto();
                    sc.setSecurity(true);
                    sc.setPrivileged(container.getSecurityContext().isPrivileged());
                    if (container.getSecurityContext().getCapabilities() != null) {
                        if (container.getSecurityContext().getCapabilities().getAdd() != null && container.getSecurityContext().getCapabilities().getAdd().size() > 0) {
                            sc.setAdd(container.getSecurityContext().getCapabilities().getAdd());
                        }
                        if (container.getSecurityContext().getCapabilities().getDrop() != null && container.getSecurityContext().getCapabilities().getDrop().size() > 0) {
                            sc.setDrop(container.getSecurityContext().getCapabilities().getDrop());
                        }
                    }

                }

                //storage/configmap/log
                List<CreateConfigMapDto> configmap = new ArrayList<>();
                String log = new String();
                if (Objects.nonNull(container.getVolumeMounts())) {
                    List<PersistentVolumeDto> storage = new ArrayList<>();
                    for (VolumeMount vm : container.getVolumeMounts()) {
                        PersistentVolumeDto cv = new PersistentVolumeDto();
                        for (Volume volume : podSpec.getVolumes()) {
                            if (volume.getConfigMap() != null) {
                                CreateConfigMapDto con = new CreateConfigMapDto();
                                con.setPath(vm.getMountPath());
                                String str = volume.getConfigMap().getName();
                                if (str.lastIndexOf("v") > 0) {
                                    con.setTag(str.substring(str.lastIndexOf("v") + 1, str.length()).replace("-", "."));
                                    con.setFile(str.substring(0, str.lastIndexOf("v")));
                                }
                                K8SURL url = new K8SURL();
                                url.setNamespace(namespace).setName(volume.getConfigMap().getName()).setResource(Resource.CONFIGMAP);
                                K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, null, cluster);
                                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                                    logger.error("DaemonSet详情获取configmap失败:{}", response.getBody());
                                    throw new MarsRuntimeException(ErrorCodeMessage.DAEMONSET_DETAIL_GET_FAILURE);
                                }
                                ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
                                Map<String, Object> datas = (Map<String, Object>) configMap.getData();
                                if (CollectionUtils.isNotEmpty(volume.getConfigMap().getItems())) {
                                    con.setValue(datas.get(volume.getConfigMap().getItems().get(0).getKey()));
                                    configmap.add(con);
                                }
                            } else {
                                cv.setPath(vm.getMountPath());
                                cv.setReadOnly(vm.isReadOnly());
                                if (Objects.isNull(vm.isReadOnly())) {
                                    cv.setReadOnly(Boolean.FALSE);
                                }
                                if (Objects.nonNull(volume.getNfs())) {
                                    cv.setType(Constant.VOLUME_TYPE_PV);
                                    cv.setPvcName(volume.getPersistentVolumeClaim().getClaimName());
                                    cv.setVolumeName(volume.getPersistentVolumeClaim().getClaimName());
                                    PersistentVolume pvByName = this.pvService.getPvByName(name, null);
                                    if (pvByName != null) {
                                        Map<String, Object> map = new HashMap<>();
                                        map = (Map<String, Object>) pvByName.getSpec().getCapacity();
                                        cv.setCapacity(map.get("storage").toString());
                                    }
                                    cv.setBindOne(Boolean.TRUE);
                                    Map<String, Object> labels = new HashMap<>();
                                    labels = pvByName.getMetadata().getLabels();
                                    cv.setProjectId(labels.get(CommonConstant.PROJECT_ID).toString());
                                }
                                if (Objects.nonNull(volume.getEmptyDir())) {
                                    if (volume.getName().contains(Constant.VOLUME_TYPE_LOGDIR)) {
                                        log = vm.getMountPath();
                                        c.setLog(log);
                                    } else {
                                        cv.setType(Constant.VOLUME_TYPE_EMPTYDIR);
                                        if (Objects.nonNull(volume.getEmptyDir())) {
                                            cv.setEmptyDir(volume.getEmptyDir().getMedium());
                                        } else {
                                            cv.setEmptyDir("Disk");
                                        }
                                    }
                                }
                                if (Objects.nonNull(volume.getHostPath())) {
                                    cv.setType(Constant.VOLUME_TYPE_HOSTPASTH);
                                    cv.setHostPath(volume.getHostPath().getPath());

                                }
                                if (Objects.nonNull(volume.getGitRepo())) {
                                    cv.setType(Constant.VOLUME_TYPE_GITREPO);
                                    cv.setGitUrl(volume.getGitRepo().getRepository());
                                    cv.setRevision(volume.getGitRepo().getRevision());
                                }
                                storage.add(cv);
                            }
                        }
                    }
                }
                cs.add(c);
            }
            detail.setClusterId(cluster.getId());
            detail.setClusterName(cluster.getName());
            return ActionReturnUtil.returnSuccessWithData(detail);
        }
        throw new MarsRuntimeException(ErrorCodeMessage.DAEMONSET_NOT_EXIST);
    }

    @Override
    public ActionReturnUtil deleteDaemonSet(String name, String namespace, String username, String clusterId) throws Exception {
        //参数校验
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //查询集群
        Cluster cluster = clusterService.findClusterById(clusterId);

        //删除DaemonSet
        dsService.delDaemonSetByName(name, namespace, cluster);

        //delete configmap
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", Constant.TYPE_DAEMONSET + "=" + name);
        K8SClientResponse conRes = configmapService.doSepcifyConfigmap(namespace, null, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }

        //delete pod
        K8SClientResponse podRes = podService.deletePods(namespace, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus()) && podRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(podRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }

        //get pvc
        K8SClientResponse pvcsRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcsRes.getStatus()) && pvcsRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcsRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        //delete pvc
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        //update pv
        PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcsRes.getBody(), PersistentVolumeClaimList.class);
        //调用更新PV接口
        if (Objects.nonNull(pvcList)) {
            volumeSerivce.updatePVList(pvcList, cluster);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil listDaemonSets(String labels) throws Exception {
        //查询用户可操作的集群
        List<Cluster> clusters = roleLocalService.listCurrentUserRoleCluster();

        if (CollectionUtils.isEmpty(clusters)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        //labels
        Map<String, Object> bodys = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(labels)) {
            bodys.put("labelSelector", labels);
        }
        List<Map<String, Object>> daemonSets = new ArrayList<Map<String, Object>>();
        for (Cluster cluster : clusters) {
            DaemonSetList list = dsService.listDaemonSet(Constant.NAMESPACE_SYSTEM, bodys, cluster);
            if (Objects.nonNull(list) && CollectionUtils.isNotEmpty(list.getItems())) {
                Map<String, Object> dsMap = new HashMap<>();
                dsMap.put("cluster", cluster);
                dsMap.put("daemonSet", list.getItems());
                daemonSets.add(dsMap);
            }
        }

        if (CollectionUtils.isNotEmpty(daemonSets)) {
            return convertDaemonSetList(daemonSets);
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据集群获取daemonset
     *
     * @param cluster
     * @return
     * @throws Exception
     */
    @Override
    public List<DaemonSet> listDaemonSets(Cluster cluster) throws Exception {
        List<Map<String, Object>> daemonSets = new ArrayList<Map<String, Object>>();
        DaemonSetList list = dsService.listDaemonSet( cluster);
        List<DaemonSet> items = list.getItems();
        return list.getItems();
    }

    @Override
    public ActionReturnUtil listPods(String name, String namespace, String clusterId) throws Exception {
        //参数校验
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        //查询集群
        Cluster cluster = clusterService.findClusterById(clusterId);

        //获取DaemonSet
        DaemonSet ds = dsService.getDaemonSet(namespace, name, cluster);
        Map<String, Object> dsLabel = ds.getSpec().getTemplate().getMetadata().getLabels();
        int tag = 1;
        if (Objects.nonNull(ds) && Objects.nonNull(ds.getMetadata()) && StringUtils.isEmpty(ds.getMetadata().getName())) {
            tag = ds.getMetadata().getGeneration();
        }
        List<PodDetail> podDetailList = new ArrayList<>();
        String labelSelector = "";
        for (Map.Entry<String, Object> map : dsLabel.entrySet()) {
            labelSelector = String.join(CommonConstant.COMMA, map.getKey() + CommonConstant.EQUALITY_SIGN + map.getValue().toString());
        }
        //获取DaemonSet的pod列表
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", labelSelector);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            throw new MarsRuntimeException(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        if (Objects.nonNull(podList) && CollectionUtils.isNotEmpty(podList.getItems())) {
            podDetailList.addAll(K8sResultConvert.podListConvert(podList, "v" + tag));
            return ActionReturnUtil.returnSuccessWithData(podDetailList);
        }
        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.POD_NOT_EXIST);
    }

    @Override
    public ActionReturnUtil listEvents(String name, String namespace, String clusterId) throws Exception {

        //参数校验
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(namespace) || StringUtils.isEmpty(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        List<EventDetail> allEvents = new ArrayList<EventDetail>();

        //查询集群
        Cluster cluster = clusterService.findClusterById(clusterId);

        //获取DaemonSet
        DaemonSet ds = dsService.getDaemonSet(namespace, name, cluster);
        Map<String, Object> dsLabel = ds.getSpec().getTemplate().getMetadata().getLabels();
        String labelSelector = "";
        for (Map.Entry<String, Object> map : dsLabel.entrySet()) {
            labelSelector = String.join(CommonConstant.COMMA, map.getKey() + CommonConstant.EQUALITY_SIGN + map.getValue().toString());
        }
        //获取DaemonSet的pod列表
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", labelSelector);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (com.harmonycloud.k8s.constant.Constant.HTTP_404 == podRes.getStatus()) {
            throw new MarsRuntimeException(ErrorCodeMessage.POD_NOT_EXIST);
        }
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            throw new MarsRuntimeException(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        // 循环podlist获取每个pod的事件
        if (Objects.nonNull(podList) && CollectionUtils.isNotEmpty(podList.getItems())) {
            bodys.clear();
            for (Pod pod : podList.getItems()) {
                bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
                K8SClientResponse podevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(podevRes.getStatus())) {
                    return ActionReturnUtil.returnErrorWithMsg(podevRes.getBody());
                }
                EventList podeventList = JsonUtil.jsonToPojo(podevRes.getBody(), EventList.class);
                if (podeventList.getItems() != null && podeventList.getItems().size() > 0) {
                    allEvents.addAll(K8sResultConvert.convertPodEvent(podeventList.getItems()));
                }
            }
        }

        // 获取ds事件
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.uid=" + ds.getMetadata().getUid());
        K8SClientResponse evRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(evRes.getBody());
        }
        EventList dsEventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
        if (Objects.nonNull(dsEventList) && CollectionUtils.isNotEmpty(dsEventList.getItems())) {
            allEvents.addAll(K8sResultConvert.convertPodEvent(dsEventList.getItems()));
        }

        // 对event进行倒序排列
        return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.sortByDesc(allEvents));
    }

    /**
     * validate daemonsetName
     *
     * @param cluster
     * @param namespace
     * @param name
     * @return ActionReturnUtil
     */
    private boolean checkDaemonSetName(String name, String namespace, Cluster cluster) throws Exception {
        K8SURL url1 = new K8SURL();
        url1.setNamespace(namespace).setResource(Resource.DAEMONTSET).setName(name);
        K8SClientResponse responses = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, null, cluster);
        Map<String, Object> convertJsonToMap = JsonUtil.convertJsonToMap(responses.getBody());
        String metadata = convertJsonToMap.get(CommonConstant.METADATA).toString();
        if (!CommonConstant.EMPTYMETADATA.equals(metadata)) {
            return true;
        }
        return false;
    }

    /**
     * 组装DaemonSet
     *
     * @param daemonSets
     * @return ActionReturnUtil
     */
    private ActionReturnUtil convertDaemonSetList(List<Map<String, Object>> daemonSets) throws Exception {
        List<DaemonSetDto> list = new ArrayList<>();
        for (Map<String, Object> dsMap : daemonSets) {
            List<DaemonSet> daemonSetList = (List<DaemonSet>) dsMap.get("daemonSet");
            Cluster cluster = (Cluster) dsMap.get("cluster");
            for (DaemonSet daemonSet : daemonSetList) {
                if (daemonSet != null && daemonSet.getMetadata() != null && daemonSet.getMetadata().getName() != null) {
                    DaemonSetDto daemonSetDto = new DaemonSetDto();
                    daemonSetDto.setName(daemonSet.getMetadata().getName());
                    Map<String, Object> annotation = daemonSet.getMetadata().getAnnotations();
                    if (annotation != null && !annotation.isEmpty()) {
                        //labels
                        if (annotation.containsKey("nephele/labels")) {
                            daemonSetDto.setLabels(annotation.get("nephele/labels").toString());
                        }

                        //updatetime
                        if (annotation.containsKey("updateTimestamp")) {
                            daemonSetDto.setUpdateTime(annotation.get("updateTimestamp").toString());
                        }
                    }
                    //状态
                    if (daemonSet.getStatus() != null && daemonSet.getStatus().getDesiredNumberScheduled() != null && daemonSet.getStatus().getNumberAvailable() != null && daemonSet.getStatus().getDesiredNumberScheduled().equals(daemonSet.getStatus().getNumberAvailable())) {
                        daemonSetDto.setStatus(Constant.SERVICE_START);
                    } else {
                        daemonSetDto.setStatus(Constant.SERVICE_STARTING);
                    }
                    Map<String, Object> l = daemonSet.getMetadata().getLabels();
                    boolean isSystem = true;
                    if (l != null && !l.isEmpty()) {
                        //创建者
                        if (l.containsKey("nephele/user")) {
                            daemonSetDto.setCreator(l.get("nephele/user").toString());
                        }
                        //判断是否是系统的daemonset
                        if (l.containsKey(Constant.TYPE_DAEMONSET)) {
                            isSystem = false;
                        }
                    }
                    //创建时间
                    daemonSetDto.setCreateTime(daemonSet.getMetadata().getCreationTimestamp());
                    //分区
                    daemonSetDto.setNamespace(daemonSet.getMetadata().getNamespace());

                    daemonSetDto.setSystem(isSystem);

                    //镜像
                    List<String> img = new ArrayList<>();
                    //配额
                    List<String> cpu = new ArrayList<>();
                    List<String> memory = new ArrayList<>();
                    for (Container c : daemonSet.getSpec().getTemplate().getSpec().getContainers()) {
                        img.add(c.getImage());
                        if (c.getResources() != null) {
                            Map<String, Object> map = (Map<String, Object>) c.getResources().getLimits();
                            if (Objects.nonNull(map)) {
                                cpu.add(map.get("cpu").toString());
                                memory.add(map.get("memory").toString());
                            }
                        }
                    }
                    daemonSetDto.setImg(img);
                    daemonSetDto.setCpu(cpu);
                    daemonSetDto.setMemory(memory);
                    daemonSetDto.setClusterId(cluster.getId().toString());
                    daemonSetDto.setClusterName(cluster.getAliasName());
                    list.add(daemonSetDto);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }
}
