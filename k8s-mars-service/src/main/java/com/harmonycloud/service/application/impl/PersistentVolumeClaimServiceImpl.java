package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.PersistentVolumeClaimDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.PersistentVolumeClaimService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.PvcDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.POD_STATUS_PENDING;
import static com.harmonycloud.common.Constant.CommonConstant.POD_STATUS_RUNNING;

/**
 * @author xc
 * @date 2018/7/4 18:43
 */
@Service
public class PersistentVolumeClaimServiceImpl implements PersistentVolumeClaimService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentVolumeClaimServiceImpl.class);

    private final static String LABEL_PROJECT_ID = "projectId";

    private final static String STORAGE_ANNOTATION = "volume.beta.kubernetes.io/storage-class";

    private final static String STORAGE_CAPACITY = "storage";

    private final static String READWRITE = "ReadWrite";

    private final static String RECYCLE_POD_NAME = "recycle-pod";

    private static final String IMAGE_NAME = "recycleImageName";

    private static final int SLEEP_TIME_TWO_SECONDS = 2000;

    @Autowired
    ClusterService clusterService;

    @Autowired
    PodService podService;

    @Autowired
    PvService pvService;

    @Autowired
    PVCService pvcService;

    @Autowired
    TenantService tenantService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    com.harmonycloud.k8s.service.NamespaceService namespaceService;

    @Autowired
    RoleLocalService roleLocalService;

    @Autowired
    SystemConfigService systemConfigService;

    @Override
    public ActionReturnUtil createPersistentVolumeClaim(PersistentVolumeClaimDto persistentVolumeClaim) throws Exception {
        //参数判空
        AssertUtil.notBlank(persistentVolumeClaim.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notBlank(persistentVolumeClaim.getName(), DictEnum.NAME);
        AssertUtil.notBlank(persistentVolumeClaim.getCapacity(), DictEnum.PARAM);
        AssertUtil.notBlank(persistentVolumeClaim.getNamespace(), DictEnum.NAMESPACE);
        //获取集群
        Cluster cluster = clusterService.findClusterById(persistentVolumeClaim.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断PVC是否已经存在，通过名称区分
        PersistentVolumeClaim pvc = pvcService.getPVCByNameAndNamespace(persistentVolumeClaim.getName(),
                persistentVolumeClaim.getNamespace(), cluster);
        if (pvc != null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAME_EXIST, persistentVolumeClaim.getName(), true);
        }
        //构造PersistentVolumeClaim对象
        PersistentVolumeClaim pvClaim = new PersistentVolumeClaim();
        ObjectMeta metaObject = new ObjectMeta();
        metaObject.setName(persistentVolumeClaim.getName());
        metaObject.setNamespace(persistentVolumeClaim.getNamespace());
        Map<String, Object> labels = new HashMap<>();
        labels.put(LABEL_PROJECT_ID, persistentVolumeClaim.getProjectId());
        metaObject.setLabels(labels);
        Map<String, Object> annotations = new HashMap<>();
        annotations.put(STORAGE_ANNOTATION, persistentVolumeClaim.getStorageClassName());
        metaObject.setAnnotations(annotations);
        pvClaim.setMetadata(metaObject);
        PersistentVolumeClaimSpec persistentVolumeClaimSpec = new PersistentVolumeClaimSpec();
        persistentVolumeClaimSpec.setAccessModes(this.getAccessModes(persistentVolumeClaim.getReadOnly(), persistentVolumeClaim.getBindOne()));
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, String> requests = new HashMap<>();
        requests.put(STORAGE_CAPACITY, persistentVolumeClaim.getCapacity());
        resourceRequirements.setRequests(requests);
        persistentVolumeClaimSpec.setResources(resourceRequirements);
        pvClaim.setSpec(persistentVolumeClaimSpec);
        //创建PersistentVolumeClaim
        K8SClientResponse response = pvcService.createPvc(persistentVolumeClaim.getNamespace(), pvClaim, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil listPersistentVolumeClaim(String projectId, String tenantId, String clusterId) throws Exception {
        AssertUtil.notBlank(projectId, DictEnum.PROJECT_ID);
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        Map<String, Object> bodys;
        //若传入clusterId，则查到一个相应集群；不传，查出所有集群
        List<Cluster> clusters = new ArrayList<>();
        if (StringUtils.isNotBlank(clusterId)) {
            clusters.add(clusterService.findClusterById(clusterId));
        } else {
            clusters = roleLocalService.listCurrentUserRoleCluster();
        }
        List<PvcDto> pvcDtoList = new ArrayList<>();
        //遍历每个集群，查找每个集群下 租户对应的分区下的 所有PVC
        for (Cluster cluster : clusters) {
            List<NamespaceLocal> namespaceLocalList = namespaceLocalService.getNamespaceListByTenantId(tenantId);
            if (namespaceLocalList.size() > 0) {
                for (NamespaceLocal namespaceLocal : namespaceLocalList) {
                    K8SURL k8SURL = new K8SURL();
                    k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
                    k8SURL.setNamespace(namespaceLocal.getNamespaceName());
                    k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM);
                    bodys = new HashMap<>();
                    bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId);
                    K8SClientResponse pvcResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, bodys, cluster);
                    if (HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
                        PersistentVolumeClaimList persistentVolumeClaimList = JsonUtil.jsonToPojo(pvcResponse.getBody(), PersistentVolumeClaimList.class);
                        //遍历每个PVC，将PVC对象构建成列表所需要的对象
                        for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaimList.getItems()) {
                            PvcDto pvcDto = new PvcDto();
                            pvcDto.setName(persistentVolumeClaim.getMetadata().getName());
                            pvcDto.setClusterId(cluster.getId());
                            pvcDto.setClusterAliasName(cluster.getAliasName());
                            pvcDto.setNamespace(namespaceLocal.getNamespaceName());
                            pvcDto.setNamespaceAliasName(namespaceLocal.getAliasName());
                            pvcDto.setCapacity(((Map<String, String>)(persistentVolumeClaim.getSpec().getResources().getRequests())).get(STORAGE_CAPACITY));
                            pvcDto.setStorageClassName((String) (persistentVolumeClaim.getMetadata().getAnnotations().get(STORAGE_ANNOTATION)));
                            pvcDto.setStatus(persistentVolumeClaim.getStatus().getPhase());
                            //TODO 使用该存储的服务
                            pvcDto.setBindingServices("");
                            pvcDto.setCreateTime(DateUtil.StringToDate(persistentVolumeClaim.getMetadata().getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
                            //ReadWriteOne,ReadWriteMany, split("ReadWrite")  > 1
                            if (persistentVolumeClaim.getSpec().getAccessModes().get(0).split(READWRITE).length > 1) {
                                pvcDto.setReadOnly(false);
                            } else {
                                pvcDto.setReadOnly(true);
                            }
                            pvcDtoList.add(pvcDto);
                        }
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(pvcDtoList);
    }

    @Override
    public ActionReturnUtil deletePersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        AssertUtil.notBlank(pvcName, DictEnum.NAME);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SURL k8SURL = new K8SURL();
        k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
        k8SURL.setNamespace(namespace);
        k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM);
        k8SURL.setSubpath(pvcName);
        K8SClientResponse pvcResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, cluster);
        if (HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PVC_CAN_NOT_DELETE);
        }
    }

    @Override
    public ActionReturnUtil recyclePersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        AssertUtil.notBlank(pvcName, DictEnum.NAME);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(namespace, pvcName, cluster);
        if (pvc == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.PVC.phrase(), true);
        }
        String pvName = "pvc-" + pvc.getMetadata().getUid();
        PersistentVolume pv = pvService.getPvByName(pvName, cluster);
        if (pv == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_QUERY_FAIL, DictEnum.PV.phrase(), true);
        }
        String podName = RECYCLE_POD_NAME;
        List<SystemConfig> systemConfigList = systemConfigService.findByConfigType(RECYCLE_POD_NAME);
        String recycleImage = "";
        for (SystemConfig systemConfig : systemConfigList) {
            if (IMAGE_NAME.equals(systemConfig.getConfigName())) {
                recycleImage = cluster.getHarborServer().getHarborHost() + ":" +
                        cluster.getHarborServer().getHarborPort() + systemConfig.getConfigValue();
            }
        }
        if (StringUtils.isBlank(recycleImage)) {
            throw new MarsRuntimeException(ErrorCodeMessage.RECYCLE_POD_CONFIG_ERROR);
        }
        Pod pod = buildRecyclePod(podName, recycleImage, pv.getSpec().getNfs());
        //创建回收空间的Pod
        ActionReturnUtil recyclePodReturn = createRecyclePod(pod, cluster);
        //循环监听Pod创建状态，完成后回收Pod
        startThreadDeletePod(podName, cluster);
        return recyclePodReturn;
    }

    @Override
    public ActionReturnUtil getPersistentVolumeClaim(String namespace, String pvcName, String clusterId) throws Exception {
        AssertUtil.notBlank(namespace, DictEnum.NAMESPACE);
        AssertUtil.notBlank(pvcName, DictEnum.NAME);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(namespace, pvcName, cluster);
        if (pvc == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.PVC.phrase(), true);
        }
        return ActionReturnUtil.returnSuccessWithData(pvc);
    }

    @Override
    public ActionReturnUtil updatePersistentVolumeClaim(PersistentVolumeClaimDto persistentVolumeClaim) throws Exception {
        AssertUtil.notBlank(persistentVolumeClaim.getClusterId(), DictEnum.CLUSTER_ID);
        AssertUtil.notBlank(persistentVolumeClaim.getName(), DictEnum.NAME);
        AssertUtil.notBlank(persistentVolumeClaim.getCapacity(), DictEnum.PARAM);
        AssertUtil.notBlank(persistentVolumeClaim.getNamespace(), DictEnum.NAMESPACE);
        //获取集群
        Cluster cluster = clusterService.findClusterById(persistentVolumeClaim.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        PersistentVolumeClaim pvc = pvcService.getPvcByName(persistentVolumeClaim.getNamespace(), persistentVolumeClaim.getName(), cluster);
        if (pvc == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.PVC.phrase(), true);
        }
        ResourceRequirements resourceRequirements = new ResourceRequirements();
        Map<String, String> requests = new HashMap<>();
        requests.put(STORAGE_CAPACITY, persistentVolumeClaim.getCapacity());
        resourceRequirements.setRequests(requests);
        pvc.getSpec().setResources(resourceRequirements);
        pvc.getSpec().setAccessModes(this.getAccessModes(persistentVolumeClaim.getReadOnly(), persistentVolumeClaim.getBindOne()));
        K8SClientResponse updateResponse = pvcService.updatePvcByName(pvc, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updateResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(updateResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private Pod buildRecyclePod(String podName, String imageName,NFSVolumeSource nfsVolumeSource) {
        Pod pod = new Pod();
        //metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(podName);
        metadata.setNamespace(CommonConstant.DEFAULT_NAMESPACE);
        pod.setMetadata(metadata);
        //spec
        PodSpec spec = new PodSpec();
        spec.setRestartPolicy(CommonConstant.RESTARTPOLICY_NEVER);
        //container
        List<Container> containerList = new ArrayList<>();
        Container container = new Container();
        container.setName(podName);
        container.setImage(imageName);
        container.setImagePullPolicy("IfNotPresent");
        List<String> command = new ArrayList<>();
        command.add("/bin/sh");
        command.add("-c");
        command.add("test -e /scrub && rm -rf /scrub/..?* /scrub/.[!.]* /scrub/*  && test -z \"$(ls -A /scrub)\" || exit 1");
        container.setCommand(command);
        List<VolumeMount> volumeMountList = new ArrayList<>();
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath("/scrub");
        volumeMount.setName("nfs-recycler");
        volumeMountList.add(volumeMount);
        container.setVolumeMounts(volumeMountList);
        containerList.add(container);
        spec.setContainers(containerList);
        //volumes
        List<Volume> volumeList = new ArrayList<>();
        Volume volume = new Volume();
        volume.setNfs(nfsVolumeSource);
        volume.setName("nfs-recycler");
        volumeList.add(volume);
        spec.setVolumes(volumeList);
        pod.setSpec(spec);
        return pod;
    }

    private ActionReturnUtil createRecyclePod(Pod pod, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(pod.getMetadata().getNamespace());
        url.setResource(Resource.POD);
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("metadata", pod.getMetadata());
        bodys.put("kind", pod.getKind());
        bodys.put("spec", pod.getSpec());
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(us.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private void startThreadDeletePod(String podName, Cluster cluster) {
        // 开启线程执行
        new Thread(() -> {
            try {
                for (; ; ) {
                    //暂停两秒钟后获取pod
                    Thread.sleep(SLEEP_TIME_TWO_SECONDS);
                    K8SClientResponse dp = podService.getPod(CommonConstant.DEFAULT_NAMESPACE, podName, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(dp.getStatus()) && dp.getStatus() != Constant.HTTP_404) {
                        break;
                    }
                    Pod pod = JsonUtil.jsonToPojo(dp.getBody(), Pod.class);
                    if (pod != null && pod.getMetadata() != null && pod.getMetadata().getName() != null) {
                        if (pod.getStatus() != null && pod.getStatus().getPhase() != null) {
                            if (!POD_STATUS_RUNNING.equals(pod.getStatus().getPhase()) && !POD_STATUS_PENDING.equals(pod.getStatus().getPhase())) {
                                podService.deletePod(CommonConstant.DEFAULT_NAMESPACE, podName, cluster);
                                break;
                            }
                        }
                    } else {
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("回收pv失败,name:{},clusterId:{}", new String[]{podName, cluster.getId()}, e);
            }
        }).start();
    }

    private List<String> getAccessModes(Boolean isReadonly, Boolean isBindOne) throws MarsRuntimeException {
        if (isReadonly == null || isBindOne == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<String> accessModes = new ArrayList<>();
        if (isReadonly) {
            accessModes.add(CommonConstant.READONLYMANY);
        } else if (isBindOne) {
            accessModes.add(CommonConstant.READWRITEONCE);
        } else {
            accessModes.add(CommonConstant.READWRITEMANY);

        }
        return accessModes;
    }

}
