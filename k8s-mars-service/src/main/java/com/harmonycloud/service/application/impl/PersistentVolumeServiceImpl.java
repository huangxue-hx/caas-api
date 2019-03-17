package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.dto.cluster.ErrDeployDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.ClusterStorage;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.k8s.constant.Constant.VERSION_V1;
import static com.harmonycloud.service.platform.constant.Constant.LABEL_PROJECT_ID;

@Service
public class PersistentVolumeServiceImpl extends VolumeAbstractService implements PersistentVolumeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistentVolumeServiceImpl.class);
    private static final int SLEEP_TIME_TWO_SECONDS = 2000;
    public static final String PV_STATUS_BOUND = "Bound";
    @Autowired
    private HttpSession session;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private PvService pvService;
    @Autowired
    private PVCService pvcService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private PodService podService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private DeploymentService dpService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private InfluxdbService influxdbService;

    @SuppressWarnings("unchecked")
    @Override
    public List<ClusterStorage> listProvider(String clusterId) throws Exception {
        return clusterService.findClusterById(clusterId).getStorages();
    }

    @Override
    public ClusterStorage getProvider(String clusterId, String type) throws Exception {
        List<ClusterStorage> providers = clusterService.findClusterById(clusterId).getStorages();
        if (CollectionUtils.isEmpty(providers)) {
            return null;
        }
        for (ClusterStorage storage : providers) {
            if (storage.getName().equalsIgnoreCase(type)) {
                return storage;
            }
        }
        return null;
    }

    @Override
    public ClusterStorage getProvider(Cluster cluster, String type) throws Exception {
        List<ClusterStorage> providers = cluster.getStorages();
        for (ClusterStorage storage : providers) {
            if (storage.getName().equalsIgnoreCase(type)) {
                return storage;
            }
        }
        return null;
    }

    /**
     * deployments服务的pv和pvc创建
     *
     * @param volume
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createVolume(PersistentVolumeDto volume) throws Exception {
        if (StringUtils.isBlank(volume.getNamespace()) || StringUtils.isBlank(volume.getPvcName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //获取集群
        Cluster cluster = Constant.NAMESPACE_SYSTEM.equals(volume.getNamespace())?
                clusterService.findClusterById(volume.getClusterId()) : namespaceLocalService.getClusterByNamespaceName(volume.getNamespace());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //判断是否已经创建pv，没有则创建pv
        PersistentVolume pv = pvService.getPvByName(volume.getVolumeName(), cluster);
        ActionReturnUtil pvRes = updatePV(pv, cluster);
        if (!pvRes.isSuccess()) {
            return pvRes;
        }
        if (pv == null) {
            ActionReturnUtil pvResponse = this.createPv(volume, cluster);
            if (!pvResponse.isSuccess()) {
                return pvResponse;
            }
        }
        return this.createPvc(volume, cluster);
    }

    /**
     * 删除pvc,更新pvList
     *
     * @param pvcList
     * @param cluster
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil updatePVList(PersistentVolumeClaimList pvcList, Cluster cluster) throws Exception {
        List<PersistentVolumeClaim> pvcs = pvcList.getItems();
        if (Objects.nonNull(pvcs) && pvcs.size() > 0) {
            for (PersistentVolumeClaim pvc : pvcs) {
                if (Objects.nonNull(pvc) && Objects.nonNull(pvc.getSpec()) && Objects.nonNull(pvc.getSpec().getVolumeName())) {
                    String pvname = pvc.getSpec().getVolumeName();
                    PersistentVolume pv = pvService.getPvByName(pvname, cluster);
                    updatePV(pv, cluster);
                }
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * 删除pvc,更新pv
     *
     * @param pv
     * @param cluster
     * @return ActionReturnUtil
     */
    @Override
    public ActionReturnUtil updatePV(PersistentVolume pv, Cluster cluster) throws Exception {
        if (null != pv) {
            Map<String, Object> bodysPV = new HashMap<String, Object>();
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("name", pv.getMetadata().getName());
            metadata.put("labels", pv.getMetadata().getLabels());
            bodysPV.put("metadata", metadata);
            Map<String, Object> spec = new HashMap<String, Object>();
            spec.put("capacity", pv.getSpec().getCapacity());
            spec.put("nfs", pv.getSpec().getNfs());
            spec.put("accessModes", pv.getSpec().getAccessModes());
            bodysPV.put("spec", spec);
            K8SURL urlPV = new K8SURL();
            urlPV.setResource(Resource.PERSISTENTVOLUME).setSubpath(pv.getMetadata().getName());
            Map<String, Object> headersPV = new HashMap<>();
            headersPV.put("Content-Type", "application/json");
            K8SClientResponse responsePV = new K8sMachineClient().exec(urlPV, HTTPMethod.PUT,
                    headersPV, bodysPV, cluster);
            if (!HttpStatusUtil.isSuccessStatus(responsePV.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(responsePV.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }

        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil createPv(PersistentVolumeDto volume, Cluster cluster) throws Exception {
        PersistentVolume pvByName = this.pvService.getPvByName(volume.getVolumeName(), cluster);
        if (pvByName != null) {
            LOGGER.info("创建pv失败，pv存储券已存在,pvname:{},clusterName:{}", volume.getVolumeName(), cluster.getName());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAME_EXIST, volume.getVolumeName() + " pv", true);
        }
        String projectName = projectService.getProjectByProjectId(volume.getProjectId()).getProjectName();
        if ((projectName.length()+volume.getVolumeName().length()) >= CommonConstant. K8S_NAME_LENGTH_LIMIT){
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NAME_LENGTH_LIMIT, DictEnum.STROAGE.phrase(),true);
        }
        PersistentVolume persistentVolume = new PersistentVolume();
        // 设置metadata
        ObjectMeta metadata = new ObjectMeta();
        metadata.setName(projectName + CommonConstant.DOT + volume.getVolumeName());
        Map<String, Object> labels = new HashMap<>();
        labels.put(LABEL_PROJECT_ID, volume.getProjectId());
        if (StringUtils.isNotBlank(volume.getServiceName())) {
            labels.put(LABEL_KEY_APP, volume.getServiceName());
        }
        metadata.setLabels(labels);
        // 设置spec
        PersistentVolumeSpec spec = new PersistentVolumeSpec();
        Map<String, Object> cap = new HashMap<>();
        String capacity = volume.getCapacity();
        if(capacity.contains(CommonConstant.GI) || capacity.contains(CommonConstant.MI)){
            cap.put(CommonConstant.STORAGE, volume.getCapacity());
        }else {
            cap.put(CommonConstant.STORAGE, volume.getCapacity() + CommonConstant.GI);
        }
        spec.setCapacity(cap);
        ClusterStorage storage = this.getProvider(cluster.getId(), CommonConstant.NFS);
        if (storage == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_PROVIDER_NOT_EXIST, CommonConstant.NFS, true);
        }
        NFSVolumeSource nfs = new NFSVolumeSource();
        // 设置nfs地址
        nfs.setPath(storage.getPath() + "/" + projectName + "/" + volume.getVolumeName());
        nfs.setServer(storage.getIp());
        spec.setNfs(nfs);
        spec.setAccessModes(this.getAccessModes(volume.getReadOnly(), volume.getBindOne()));
        persistentVolume.setMetadata(metadata);
        persistentVolume.setSpec(spec);
        persistentVolume.setApiVersion(VERSION_V1);
        persistentVolume.setKind(CommonConstant.PERSISTENTVOLUME);
        ActionReturnUtil pvCreateRes = pvService.addPv(persistentVolume, cluster);
        if (!pvCreateRes.isSuccess()) {
            return pvCreateRes;
        }
        //新建pod创建nfs内的挂载目录
        String podName = CommonConstant.PV_CREATE_POD_NAME + projectName + CommonConstant.DOT + volume.getVolumeName();
        String command = "mkdir -p /scrub/"+ projectName + "/" + volume.getVolumeName();
        NFSVolumeSource createPvNfs = new NFSVolumeSource();
        createPvNfs.setPath(storage.getPath());
        createPvNfs.setServer(storage.getIp());
        Pod pod = createPod(podName, "pv-dir-create", command, createPvNfs, cluster);
        ActionReturnUtil res = podService.addPod(CommonConstant.DEFAULT_NAMESPACE, pod, cluster);
        startThreadDeletePod(podName, cluster);
        return res;
    }

    @Override
    public List<PvDto> listPv(String projectId, String clusterId, Boolean isBind) throws Exception {
        List<Cluster> clusters = new ArrayList<>();
        K8SURL url = new K8SURL();
        url.setResource(Resource.PERSISTENTVOLUME);
        Map<String, Object> bodys = null;
        if (StringUtils.isNotBlank(projectId)) {
            bodys = new HashMap<>();
            bodys.put("labelSelector", LABEL_PROJECT_ID + "=" + projectId);
        }
        if (StringUtils.isNotBlank(clusterId)) {
            clusters.add(clusterService.findClusterById(clusterId));
        } else {
            clusters = roleLocalService.listCurrentUserRoleCluster();
        }
        List<PvDto> pvDtos = new ArrayList<>();
        for (Cluster cluster : clusters) {
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                PersistentVolumeList persistentVolumeList = K8SClient.converToBean(response, PersistentVolumeList.class);
                List<PersistentVolume> volumes = persistentVolumeList.getItems();
                if (CollectionUtils.isEmpty(volumes)) {
                    continue;
                }
                Map<String, String> pvcAppMap = new HashMap<>();
                K8SClientResponse pvcResponse = pvcService.doSepcifyPVC(null, null, HTTPMethod.GET, cluster);
                if (HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
                    PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcResponse.getBody(), PersistentVolumeClaimList.class);
                    List<PersistentVolumeClaim> pvcs = pvcList.getItems();
                    for (PersistentVolumeClaim pvc : pvcs) {
                        if (pvc.getMetadata().getLabels() == null || (pvc.getMetadata().getLabels().get(LABEL_KEY_APP) == null && pvc.getMetadata().getLabels().get(Constant.TYPE_DAEMONSET) == null)) {
                            continue;
                        }
                        String app = pvc.getMetadata().getLabels().containsKey(LABEL_KEY_APP) ? pvc.getMetadata().getLabels().get(LABEL_KEY_APP).toString() : null;
                        app = pvc.getMetadata().getLabels().containsKey(Constant.TYPE_DAEMONSET) ? pvc.getMetadata().getLabels().get(Constant.TYPE_DAEMONSET).toString() : app;
                        pvcAppMap.put(pvc.getMetadata().getNamespace() + SLASH + pvc.getMetadata().getName(), app);
                    }
                }
                // 处理items返回页面需要的对象
                for (PersistentVolume pv : volumes) {
                    PvDto pvDto = this.convertPvDto(pv, pvcAppMap);
                    //过滤 只返回isBind=true的数据
                    if (isBind != null && isBind && !pvDto.getIsBind()) {
                        continue;
                    }
                    //过滤 只返回isBind=false的数据
                    if (isBind != null && !isBind && pvDto.getIsBind()) {
                        continue;
                    }
                    // 设置读写权限
                    pvDto.setProjectId(projectId);
                    pvDto.setClusterName(cluster.getName());
                    pvDto.setClusterAliasName(cluster.getAliasName());
                    pvDto.setClusterId(cluster.getId());
                    double pvused = this.influxdbService.getPvResourceUsage("volume/usage", cluster, pvDto.getName());
                    pvused = pvused / 1024 / 1024 / 1024;
                    pvDto.setUsed(String.format("%.2f", pvused) + "Gi");
                    pvDtos.add(pvDto);
                }

            }
        }
        return pvDtos;
    }

    @Override
    public ActionReturnUtil deletePv(String name, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        PersistentVolume pvByName = this.pvService.getPvByName(name, cluster);
        if (pvByName == null) {
            LOGGER.info("pv存储券不存在,pvname:{},clusterName:{}", name, cluster.getName());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_QUERY_FAIL);
        }
        if (pvByName.getStatus().getPhase().equals(PV_STATUS_BOUND)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_CAN_NOT_DELETE);
        }
        ActionReturnUtil deletePvRes = pvService.delPvByName(name, cluster);
        if (!deletePvRes.isSuccess()) {
            return deletePvRes;
        }
        //新建pod删除nfs内的挂载目录
        String podName = CommonConstant.PV_DELETE_POD_NAME + name;
        String path = name.replaceFirst("\\.", "/");
        String command = "rm -rf /scrub/"+ path;
        //volumes
        ClusterStorage storage = this.getProvider(cluster.getId(), CommonConstant.NFS);
        if (storage == null) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_PROVIDER_NOT_EXIST, CommonConstant.NFS, true);
        }
        NFSVolumeSource pvNfs = new NFSVolumeSource();
        pvNfs.setPath(storage.getPath());
        pvNfs.setServer(storage.getIp());
        Pod pod = createPod(podName, "pv-dir-delete", command, pvNfs, cluster);
        ActionReturnUtil res = podService.addPod(CommonConstant.DEFAULT_NAMESPACE, pod, cluster);
        startThreadDeletePod(podName, cluster);
        return res;
    }

    @Override
    public ActionReturnUtil getPv(String name, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        PersistentVolume pvByName = this.pvService.getPvByName(name, cluster);
        if (pvByName == null) {
            LOGGER.info("pv存储券不存在,pvname:{},clusterName:{}", name, cluster.getName());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_QUERY_FAIL);
        }
        return ActionReturnUtil.returnSuccessWithData(this.convertPvDto(pvByName, new HashMap<>()));
    }

    public ActionReturnUtil updatePv(PvDto pvDto) throws Exception {
        Assert.hasText(pvDto.getName());
        Assert.hasText(pvDto.getClusterId());
        Assert.hasText(pvDto.getCapacity());
        Cluster cluster = clusterService.findClusterById(pvDto.getClusterId());
        PersistentVolume pvByName = this.pvService.getPvByName(pvDto.getName(), cluster);
        if (pvByName == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_QUERY_FAIL, pvDto.getName(), true);
        }
        Map<String, Object> cap = new HashMap<>();
        cap.put(CommonConstant.STORAGE, pvDto.getCapacity() + CommonConstant.GI);
        pvByName.getSpec().setCapacity(cap);
        pvByName.getSpec().setAccessModes(this.getAccessModes(pvDto.isReadonly(), pvDto.getIsBindOne()));
        K8SClientResponse updatePvByName = pvService.updatePvByName(pvByName, cluster);
        if (!HttpStatusUtil.isSuccessStatus(updatePvByName.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.UPDATE_FAIL, updatePvByName.getBody(), false);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil updatePvByName(PersistentVolumeDto volumeDto) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(volumeDto.getNamespace());
        PvDto pvDto = new PvDto();
        pvDto.setName(volumeDto.getVolumeName());
        pvDto.setIsReadonly(volumeDto.getReadOnly());
        pvDto.setIsBindOne(volumeDto.getBindOne());
        pvDto.setCapacity(volumeDto.getCapacity());
        pvDto.setClusterId(cluster.getId());
        return this.updatePv(pvDto);
    }

    @Override
    public boolean isFsPv(String type) {
        if (type == null) {
            return false;
        }
        if (type.equalsIgnoreCase(Constant.VOLUME_TYPE_LOGDIR)) {
            return false;
        }
        if (type.equalsIgnoreCase(Constant.VOLUME_TYPE_CONFIGMAP)) {
            return false;
        }
        if (type.equalsIgnoreCase(Constant.VOLUME_TYPE_EMPTYDIR)) {
            return false;
        }
        if (type.equalsIgnoreCase(Constant.VOLUME_TYPE_HOSTPASTH)) {
            return false;
        }
        return true;
    }

    @Override
    public ActionReturnUtil recyclePv(String name, String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        PersistentVolume pv = this.pvService.getPvByName(name, cluster);
        if (pv == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PV_QUERY_FAIL, name, true);
        }
        String command = "test -e /scrub && rm -rf /scrub/..?* /scrub/.[!.]* /scrub/*  && test -z \"$(ls-A /scrub)\" || exit 1";
        Pod pod = createPod(CommonConstant.PV_RECYCLE_POD_NAME + name, "pv-recycler", command, pv.getSpec().getNfs(), cluster);
        ActionReturnUtil res = podService.addPod(CommonConstant.DEFAULT_NAMESPACE, pod, cluster);
        startThreadDeletePod(CommonConstant.PV_RECYCLE_POD_NAME + name, cluster);
        return res;

    }

    @Override
    public ActionReturnUtil releasePv(String name, String clusterId, String namespace, String serviceName) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        K8SClientResponse pvcRes = pvcService.getPVC(name, namespace, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        PersistentVolumeClaim persistentVolumeClaim = K8SClient.converToBean(pvcRes, PersistentVolumeClaim.class);
        if (persistentVolumeClaim != null) {
            K8SURL url = new K8SURL();
            url.setName(persistentVolumeClaim.getMetadata().getName()).setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("gracePeriodSeconds", CommonConstant.NUM_ONE);
            K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.DELETE, headers, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除PVC失败,DeploymentName:{}, error:{}", serviceName, response.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.PV_RELEASE_FAIL);
            }
            if (response.getStatus() != Constant.HTTP_404 && persistentVolumeClaim.getSpec() != null && persistentVolumeClaim.getSpec().getVolumeName() != null) {
                String pvName = persistentVolumeClaim.getSpec().getVolumeName();
                PersistentVolume pv = pvService.getPvByName(pvName, null);
                this.updatePV(pv, cluster);
            }
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ErrDeployDto transferPV(PersistentVolume pv, Cluster cluster, String deployName)
            throws MarsRuntimeException {
        ErrDeployDto errDeployDto = new ErrDeployDto();
        if (null != pv) {
            K8SURL urlPV = new K8SURL();
            urlPV.setResource(Resource.PERSISTENTVOLUME).setSubpath(pv.getMetadata().getName());

            PersistentVolumeSpec spec1 = new PersistentVolumeSpec();
            spec1.setNfs(pv.getSpec().getNfs());
            spec1.setAccessModes(pv.getSpec().getAccessModes());
            spec1.setFlexVolume(pv.getSpec().getFlexVolume());
            spec1.setCapacity(pv.getSpec().getCapacity());
            ObjectMeta meta =new ObjectMeta();
            meta.setLabels(pv.getMetadata().getLabels());
            meta.setName(pv.getMetadata().getName());
            meta.setClusterName(pv.getMetadata().getClusterName());
            pv.setMetadata(meta);
            pv.setSpec(spec1);
            try {
                pvService.addPv(pv, cluster);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deletePv(String projectId) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        // 根据lable查询pv
        bodys.put(CommonConstant.LABELSELECTOR, LABEL_PROJECT_ID + "=" + projectId);
        K8SURL url = new K8SURL();
        List<Cluster> clusters = clusterService.listCluster();
        for (Cluster cluster : clusters) {
            url.setResource(com.harmonycloud.k8s.constant.Resource.PERSISTENTVOLUME);
            K8SClientResponse k8SClientResponse = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (HttpStatusUtil.isSuccessStatus(k8SClientResponse.getStatus())) {
                PersistentVolumeList persistentVolumeList = JsonUtil.jsonToPojo(k8SClientResponse.getBody(), PersistentVolumeList.class);
                if (persistentVolumeList == null) {
                    continue;
                }
                List<PersistentVolume> items = persistentVolumeList.getItems();
                if (items.size() > 0) {
                    for (PersistentVolume pv : items) {
                        ActionReturnUtil delPvByName = this.pvService.delPvByName(pv.getMetadata().getName(), cluster);
                        if (!delPvByName.isSuccess()) {
                            LOGGER.error("删除pv失败: response:{}", JSONObject.toJSONString(delPvByName));
                        }
                    }
                }
            } else {
                LOGGER.error("删除pv失败: response:{}", JSONObject.toJSONString(k8SClientResponse));
                throw new MarsRuntimeException(ErrorCodeMessage.PV_DELETE_FAIL);
            }
        }
    }

    private ActionReturnUtil createPvc(PersistentVolumeDto volume, Cluster cluster) throws Exception {
        PersistentVolumeClaim pVolumeClaim = new PersistentVolumeClaim();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(volume.getPvcName());
        PersistentVolumeClaimSpec pvSpec = new PersistentVolumeClaimSpec();
        pvSpec.setAccessModes(this.getAccessModes(volume.getReadOnly(), volume.getBindOne()));
        LabelSelector labelSelector = new LabelSelector();
        Map<String, Object> labels = new HashMap<>();
        labels.put(LABEL_PROJECT_ID, volume.getProjectId());
        if (StringUtils.isBlank(volume.getServiceType())) {
            labels.put(LABEL_KEY_APP, volume.getServiceName());
        } else {
            labels.put(volume.getServiceType(), volume.getServiceName());
        }
        labelSelector.setMatchLabels(labels);
        meta.setLabels(labels);
        pvSpec.setSelector(labelSelector);
        Map<String, Object> limits = new HashMap<>();
        if (volume.getCapacity().contains(CommonConstant.MI) || volume.getCapacity().contains(CommonConstant.GI)) {
            limits.put(CommonConstant.STORAGE, volume.getCapacity());
        } else {
            limits.put(CommonConstant.STORAGE, volume.getCapacity() + CommonConstant.GI);
        }
        String projectName = projectService.getProjectByProjectId(volume.getProjectId()).getProjectName();

        ResourceRequirements resources = new ResourceRequirements();
        resources.setLimits(limits);
        resources.setRequests(limits);
        pvSpec.setResources(resources);
        if(volume.getVolumeName().startsWith(projectName + CommonConstant.DOT)){
            pvSpec.setVolumeName(volume.getVolumeName());
        }else {
            pvSpec.setVolumeName(projectName + CommonConstant.DOT + volume.getVolumeName());
        }
        pVolumeClaim.setMetadata(meta);
        pVolumeClaim.setSpec(pvSpec);
        K8SClientResponse response = pvService.createPvc(volume.getNamespace(), pVolumeClaim, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
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

    private PvDto convertPvDto(PersistentVolume pv, Map<String, String> pvcAppMap) {
        PvDto pvDto = new PvDto();
        // 设置读写权限
        if (pv.getSpec().getAccessModes() != null) {
            if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READONLYMANY)) {
                pvDto.setIsBindOne(false);
                pvDto.setIsReadonly(true);
            }
            if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEMANY)) {
                pvDto.setIsBindOne(false);
                pvDto.setIsReadonly(false);
            }
            if (pv.getSpec().getAccessModes().get(0).equals(CommonConstant.READWRITEONCE)) {
                pvDto.setIsBindOne(true);
                pvDto.setIsReadonly(false);
            }
        }
        // 设置bind
        if (!pv.getStatus().getPhase().equalsIgnoreCase(PV_STATUS_BOUND)) {
            pvDto.setIsBind(false);
        } else {
            pvDto.setIsBind(true);
            // 设置绑定的服务
            Map<String, Object> l = pv.getMetadata().getLabels();
            if (l != null && l.get(LABEL_KEY_APP) != null) {
                pvDto.setServiceName(l.get(LABEL_KEY_APP).toString());
                pvDto.setServiceNamespace(pv.getSpec().getClaimRef().getNamespace());
            } else {
                //兼容 上一版本
                String serviceName = pvcAppMap.get(pv.getSpec().getClaimRef().getNamespace()
                        + SLASH + pv.getSpec().getClaimRef().getName());
                pvDto.setServiceName(serviceName);
                pvDto.setServiceNamespace(pv.getSpec().getClaimRef().getNamespace());
            }
        }
        // 设置容量
        Map<String, String> capacity = (Map<String, String>) pv.getSpec().getCapacity();
        pvDto.setCapacity(capacity.get(CommonConstant.STORAGE));
        // 设置pv名称
        pvDto.setName(pv.getMetadata().getName());
        // 设置time
        pvDto.setCreateTime(DateUtil.utcToGmtDate(pv.getMetadata().getCreationTimestamp()));
        // 设置type
        pvDto.setType(CommonConstant.NFS);
        pvDto.setStatus(pv.getStatus().getPhase());
        return pvDto;
    }

    private Pod createPod(String podName, String containerName, String shCommand, NFSVolumeSource nfsVolumeSource, Cluster cluster) throws Exception {
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
        List<Container> cs = new ArrayList<Container>();
        Container con = new Container();
        con.setName(containerName);
        con.setImage(cluster.getHarborServer().getHarborAddress() + "/k8s-deploy/busybox");
        con.setImagePullPolicy("IfNotPresent");
        List<String> command = new ArrayList<String>();
        command.add("/bin/sh");
        command.add("-c");
        command.add(shCommand);
        con.setCommand(command);
        List<VolumeMount> vms = new ArrayList<VolumeMount>();
        VolumeMount vm = new VolumeMount();
        vm.setMountPath("/scrub");
        vm.setName("vol");
        vms.add(vm);
        con.setVolumeMounts(vms);
        cs.add(con);
        spec.setContainers(cs);
        //volumes
        List<Volume> vs = new ArrayList<>();
        Volume v = new Volume();
        v.setNfs(nfsVolumeSource);
        v.setName("vol");
        vs.add(v);
        spec.setVolumes(vs);
        pod.setSpec(spec);
        return pod;
    }

    private void startThreadDeletePod(String name, Cluster cluster) {
        // 开启线程执行
        new Thread() {
            @Override
            public void run() {
                try {
                    for (; ; ) {
                        //暂停两秒钟后获取pod
                        Thread.sleep(SLEEP_TIME_TWO_SECONDS);
                        K8SClientResponse dp = podService.getPod(CommonConstant.DEFAULT_NAMESPACE, name, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus()) && dp.getStatus() != Constant.HTTP_404) {
                            break;
                        }
                        Pod pod = JsonUtil.jsonToPojo(dp.getBody(), Pod.class);
                        if (pod != null && pod.getMetadata() != null && pod.getMetadata().getName() != null) {
                            if (pod.getStatus() != null && pod.getStatus().getPhase() != null) {
                                if (!POD_STATUS_RUNNING.equals(pod.getStatus().getPhase()) && !POD_STATUS_PENDING.equals(pod.getStatus().getPhase())) {
                                    podService.deletePod(CommonConstant.DEFAULT_NAMESPACE, name, cluster);
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("回收pv失败,name:{},clusterId:{}", new String[]{name, cluster.getId()}, e);
                }
            }
        }.start();
    }
}
