package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.NodeTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.PersistentVolumeClaimDto;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.service.ScService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.PersistentVolumeClaimService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.platform.bean.PvcDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;

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
    private ClusterService clusterService;

    @Autowired
    private PodService podService;

    @Autowired
    private PvService pvService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private com.harmonycloud.k8s.service.NamespaceService namespaceService;

    @Autowired
    private NamespaceService nsService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private StorageClassService storageClassService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private HttpSession session;

    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;
    @Autowired
    private InfluxdbService influxdbService;

    @Autowired
    private PersistentVolumeService persistentVolumeService;

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
        if(!CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(persistentVolumeClaim.getNamespace())) {
            //判断分区下存储配额是否为0
            String name = persistentVolumeClaim.getNamespace();
            // 1.查询namespace
            String storageClassName = persistentVolumeClaim.getStorageClassName();
            ResourceQuotaList resouceQuotaList = nsService.getResouceQuota(name, cluster);
            List<ResourceQuota> items = resouceQuotaList.getItems();
            ResourceQuota resourceQuota = items.get(0);
            LinkedHashMap<String, Object> hards = (LinkedHashMap<String, Object>) resourceQuota.getSpec().getHard();
            if (hards.get(storageClassName + ".storageclass.storage.k8s.io/requests.storage") == null) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.STORAGE_QUOTA_OVER_FLOOR);
            }
        }
        //构造PersistentVolumeClaim对象
        PersistentVolumeClaim pvClaim = new PersistentVolumeClaim();
        ObjectMeta metaObject = new ObjectMeta();
        metaObject.setName(persistentVolumeClaim.getName());
        metaObject.setNamespace(persistentVolumeClaim.getNamespace());
        Map<String, Object> labels = new HashMap<>();
        if(!CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(persistentVolumeClaim.getNamespace())) {
            labels.put(Constant.NODESELECTOR_LABELS_PRE + LABEL_PROJECT_ID, persistentVolumeClaim.getProjectId());
        }
        labels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.TYPE, CommonConstant.STORAGE);
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
    public ActionReturnUtil listPersistentVolumeClaim(String projectId, String tenantId, String clusterId, String namespace) throws Exception {
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
        List<NamespaceLocal> namespaceLocalList = new ArrayList<>();
        if(StringUtils.isNotBlank(namespace)){
            namespaceLocalList.add(namespaceLocalService.getNamespaceByName(namespace));
            if(CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(namespace)){
                NamespaceLocal namespaceLocal = new NamespaceLocal();
                namespaceLocal.setNamespaceName(CommonConstant.KUBE_SYSTEM);
                namespaceLocal.setAliasName(CommonConstant.KUBE_SYSTEM);
                namespaceLocalList.add(namespaceLocal);
            }
        }else{
            namespaceLocalList.addAll(namespaceLocalService.getNamespaceListByTenantId(tenantId));
            NamespaceLocal namespaceLocal = namespaceLocalService.getKubeSystemNamespace();
            if(namespaceLocal != null) {
                namespaceLocalList.add(namespaceLocal);
            }
        }

        for (Cluster cluster : clusters) {
            if (namespaceLocalList.size() > 0) {
                List<StorageClassDto> storageClassDtoList = storageClassService.listStorageClass(cluster.getId());
                Map<String, StorageClassDto> storageClassDtoMap = storageClassDtoList.stream().collect(Collectors.toMap(StorageClassDto::getName, storageClassDto->storageClassDto));
                for (NamespaceLocal namespaceLocal : namespaceLocalList) {
                    if(namespaceLocal == null || (!KUBE_SYSTEM.equalsIgnoreCase(namespaceLocal.getNamespaceName()) && !namespaceLocal.getClusterId().equalsIgnoreCase(cluster.getId()))){
                        continue;
                    }
                    K8SURL k8SURL = new K8SURL();
                    k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
                    k8SURL.setNamespace(namespaceLocal.getNamespaceName());
                    k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM);
                    bodys = new HashMap<>();
                    String labelSelector = "";
                    if(!KUBE_SYSTEM.equalsIgnoreCase(namespaceLocal.getNamespaceName())){
                        labelSelector += Constant.NODESELECTOR_LABELS_PRE + LABEL_PROJECT_ID + "=" + projectId;
                    }else{
                        labelSelector += Constant.NODESELECTOR_LABELS_PRE + CommonConstant.TYPE + "=" + CommonConstant.STORAGE;
                    }
                    bodys.put("labelSelector", labelSelector);
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
                            double pvused = 0.0;
                            try {
                                pvused = influxdbService.getPvResourceUsage("volume/usage", cluster, pvcDto.getName());
                            }catch (Exception e){
                                LOGGER.error("查询influxdb pv使用量失败，clusterId:{}",cluster.getId(),e);
                            }
                            pvused = pvused / 1024 / 1024 / 1024;
                            pvcDto.setUsed(String.format("%.2f", pvused) + "Gi");
                            pvcDto.setStorageClassName((String) (persistentVolumeClaim.getMetadata().getAnnotations().get(STORAGE_ANNOTATION)));
                            if(StringUtils.isBlank(pvcDto.getStorageClassName())){
                                pvcDto.setStorageClassName(persistentVolumeClaim.getSpec().getStorageClassName());
                            }
                            if(StringUtils.isBlank(pvcDto.getStorageClassName())){
                                pvcDto.setStorageClassName((String)persistentVolumeClaim.getMetadata().getAnnotations().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS));
                            }
                            if (!StringUtils.isBlank(pvcDto.getStorageClassName())) {
                                StorageClassDto storageClassDto = storageClassDtoMap.get(pvcDto.getStorageClassName());
                                if (storageClassDto != null) {
                                    pvcDto.setStorageClassType(storageClassDto.getType());
                                }
                            } else {
                                pvcDto.setStorageClassType("");
                            }
                            pvcDto.setStatus(persistentVolumeClaim.getStatus().getPhase());
                            List<Map<String, Object>> serviceNameList = new ArrayList<>();
                            Map<String, Object> labelMap = persistentVolumeClaim.getMetadata().getLabels();
                            if(labelMap != null) {
                                for (String key : labelMap.keySet()) {
                                    Map<String, Object> map = new HashMap<>();
                                    if (key.contains(LABEL_KEY_APP + CommonConstant.SLASH)) {
                                        map.put(CommonConstant.TYPE, CommonConstant.LABEL_KEY_APP);
                                        map.put(CommonConstant.NAME, labelMap.get(key).toString());
                                        map = dataPrivilegeHelper.filterServiceName(map, labelMap.get(key).toString(), namespaceLocal.getNamespaceName());
                                        serviceNameList.add(map);
                                    } else if (key.contains(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_DAEMONSET)) {
                                        map.put(CommonConstant.TYPE, CommonConstant.LABEL_KEY_DAEMONSET);
                                        map.put(CommonConstant.NAME, labelMap.get(key).toString());
                                        serviceNameList.add(map);
                                    } else if (key.contains(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET)){
                                        map.put(CommonConstant.TYPE, CommonConstant.LABEL_KEY_STATEFULSET);
                                        map.put(CommonConstant.NAME, labelMap.get(key).toString());
                                        map = dataPrivilegeHelper.filterServiceName(map, labelMap.get(key).toString(), namespaceLocal.getNamespaceName());
                                        serviceNameList.add(map);
                                    }
                                }
                            }
                            pvcDto.setBindingServices(serviceNameList);

                            pvcDto.setCreateTime(DateUtil.utcToGmtDate(persistentVolumeClaim.getMetadata().getCreationTimestamp()));
                            //ReadWriteOne,ReadWriteMany, split("ReadWrite")  > 1
                            if (persistentVolumeClaim.getSpec().getAccessModes().get(0).equalsIgnoreCase(CommonConstant.READONLYMANY)) {
                                pvcDto.setReadOnly(true);
                                pvcDto.setBindOne(false);
                            } else if (persistentVolumeClaim.getSpec().getAccessModes().get(0).equalsIgnoreCase(CommonConstant.READWRITEONCE)){
                                pvcDto.setReadOnly(false);
                                pvcDto.setBindOne(true);
                            } else if (persistentVolumeClaim.getSpec().getAccessModes().get(0).equalsIgnoreCase(CommonConstant.READWRITEMANY)) {
                                pvcDto.setReadOnly(false);
                                pvcDto.setBindOne(false);
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
    public List<PvcDto> listPersistentVolumeClaim(String clusterId, String namespace) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SClientResponse response = pvcService.doSepcifyPVC(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("查询pvc失败，cluster：{}，namespace：{}", cluster.getId(), namespace);
            throw new MarsRuntimeException(response.getBody());
        }
        PersistentVolumeClaimList persistentVolumeClaimList = JsonUtil.jsonToPojo(response.getBody(),
                PersistentVolumeClaimList.class);
        if (persistentVolumeClaimList == null || CollectionUtils.isEmpty(persistentVolumeClaimList.getItems())) {
            return Collections.emptyList();
        }
        List<PvcDto> pvcDtos = new ArrayList<>();
        for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaimList.getItems()) {
            PvcDto pvcDto = new PvcDto();
            pvcDto.setName(persistentVolumeClaim.getMetadata().getName());
            pvcDto.setClusterId(cluster.getId());
            pvcDto.setClusterAliasName(cluster.getAliasName());
            pvcDto.setNamespace(namespace);
            pvcDto.setCapacity(((Map<String, String>) (persistentVolumeClaim.getSpec().getResources().getRequests())).get(STORAGE_CAPACITY));
            pvcDtos.add(pvcDto);
        }
        return pvcDtos;
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
        //判断pvc是否在使用
        PersistentVolumeClaim pvcByName = pvcService.getPvcByName(namespace, pvcName, cluster);
        Map<String, Object> labels = pvcByName.getMetadata().getLabels();
        for (String s : labels.keySet()) {
            if(s.contains(CommonConstant.LABEL_KEY_APP)
                    ||s.contains(CommonConstant.LABEL_KEY_DAEMONSET)
                    ||s.contains(CommonConstant.LABEL_KEY_STATEFULSET)){
                throw new MarsRuntimeException(ErrorCodeMessage.PVC_CAN_NOT_DELETE);
            }
        }
        K8SClientResponse pvcResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, cluster);
        if (HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
            if(pvcByName.getMetadata().getAnnotations() != null && pvcByName.getMetadata().getAnnotations().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS) != null){
                String storageClass = (String)pvcByName.getMetadata().getAnnotations().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS);
                return  persistentVolumeService.deletePv(pvcName, storageClass, cluster);
            }
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
        String pvName = pvc.getSpec().getVolumeName();
        PersistentVolume pv = pvService.getPvByName(pvName, cluster);
        if (pvName == null || pv == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_QUERY_FAIL, DictEnum.PV.phrase(), true);
        }
        String podName = RECYCLE_POD_NAME + pvcName;
        List<SystemConfig> systemConfigList = systemConfigService.findByConfigType(RECYCLE_POD_NAME);
        String recycleImage = "";
        for (SystemConfig systemConfig : systemConfigList) {
            if (IMAGE_NAME.equals(systemConfig.getConfigName())) {
                recycleImage = cluster.getHarborServer().getHarborAddress() + systemConfig.getConfigValue();
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
        metadata.setNamespace(CommonConstant.KUBE_SYSTEM);
        pod.setMetadata(metadata);
        //spec
        PodSpec spec = new PodSpec();
        Map<String, Object> nodeSelector = new HashMap<>();
        nodeSelector.put(NodeTypeEnum.SYSTEM.getLabelKey(), NodeTypeEnum.SYSTEM.getLabelValue());
        spec.setNodeSelector(nodeSelector);
        spec.setRestartPolicy(CommonConstant.RESTARTPOLICY_NEVER);
        //container
        List<Container> containerList = new ArrayList<>();
        Container container = new Container();
        container.setName("recycle");
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
            if(StringUtils.isNotEmpty(us.getMessage()) && us.getMessage().contains("already exists")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_RECYCLE);
            }else {
                return ActionReturnUtil.returnErrorWithData(us.getMessage());
            }
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
                    K8SClientResponse dp = podService.getPod(CommonConstant.KUBE_SYSTEM, podName, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(dp.getStatus()) && dp.getStatus() != Constant.HTTP_404) {
                        break;
                    }
                    Pod pod = JsonUtil.jsonToPojo(dp.getBody(), Pod.class);
                    if (pod != null && pod.getMetadata() != null && pod.getMetadata().getName() != null) {
                        if (pod.getStatus() != null && pod.getStatus().getPhase() != null) {
                            if (!POD_STATUS_RUNNING.equals(pod.getStatus().getPhase()) && !POD_STATUS_PENDING.equals(pod.getStatus().getPhase())) {
                                podService.deletePod(CommonConstant.KUBE_SYSTEM, podName, cluster);
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

    public ActionReturnUtil updateLabel(String name, String namespace, Cluster cluster, Map<String, Object> label){
        PersistentVolumeClaim pvc = pvcService.getPvcByName(namespace, name, cluster);
        if (pvc != null) {
            Map<String, Object> labels = pvc.getMetadata().getLabels();
            labels.putAll(label);
            K8SClientResponse pvcResponse = pvcService.updatePvcByName(pvc, cluster);
            if (!HttpStatusUtil.isSuccessStatus((pvcResponse.getStatus()))) {
                UnversionedStatus status = JsonUtil.jsonToPojo(pvcResponse.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 根据当前服务绑定的存储，去除未绑定该服务的pvc中的服务标签
     * @param dep
     * @param cluster
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updatePvcByDeployment(Deployment dep, Cluster cluster) throws Exception {
        List<Volume> volumeList = dep.getSpec().getTemplate().getSpec().getVolumes();
        List<String> currentBoundPvcList = new ArrayList<>();
        //获取服务已绑定的pvc列表
        if(CollectionUtils.isNotEmpty(volumeList)){
            for(Volume volume : volumeList){
                if(volume.getPersistentVolumeClaim() != null){
                    currentBoundPvcList.add(volume.getPersistentVolumeClaim().getClaimName());
                    LOGGER.info("deploy {} have pvc {}",dep.getMetadata().getName(), volume.getPersistentVolumeClaim().getClaimName());
                }
            }
        }
        //查询所有带有该服务标签的pvc，删除其中未被服务绑定的pvc的标签
        Map<String, Object> bodys = new HashMap<>();
        String key = CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + dep.getMetadata().getName();
        String label = key + CommonConstant.EQUALITY_SIGN + dep.getMetadata().getName();
        bodys.put(CommonConstant.LABELSELECTOR, label);
        K8SClientResponse response = pvcService.doSepcifyPVC(dep.getMetadata().getNamespace() , bodys, HTTPMethod.GET, cluster);
        if(!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("查询pvc 列表失败, label:{},res:{}", label, JSONObject.toJSONString(response));
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }else{
            PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(response.getBody(), PersistentVolumeClaimList.class);
            if(CollectionUtils.isNotEmpty(pvcList.getItems())){
                for(PersistentVolumeClaim pvc : pvcList.getItems()){
                    LOGGER.info("app {} have pvc {}",dep.getMetadata().getName(), pvc.getMetadata().getName());
                    if(!currentBoundPvcList.contains(pvc.getMetadata().getName())){
                        LOGGER.info("pvc {} remove key {}",pvc.getMetadata().getName(), key);
                        pvc.getMetadata().getLabels().remove(key);
                        K8SClientResponse res = pvcService.updatePvcByName(pvc, cluster);
                        if(!HttpStatusUtil.isSuccessStatus(res.getStatus())) {
                            LOGGER.error("pvc{}被使用服务标签更新失败,res:{}",
                                    pvc.getMetadata().getName(), JSONObject.toJSONString(res));
                        }
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }


}
