package com.harmonycloud.service.application.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.ScService;
import com.harmonycloud.k8s.service.SecretService;
import com.harmonycloud.k8s.service.StatefulSetService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DaemonSetsService;
import com.harmonycloud.service.application.PersistentVolumeClaimService;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xc
 * @date 2018/6/14 14:36
 */
@Service
public class StorageClassServiceImpl implements StorageClassService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageClassServiceImpl.class);

    private static final String NFS_STORAGE = "NFS";

    private static final String CEPH_RBD_STORAGE = "CEPH-RBD";

    private static final String NFS_PROVISIONER = "nfs-provisioner";

    private static final String CEPH_RBD_PROVISIONER = "ceph-rbd-provisioner";

    private static final String NFS_PROVISIONER_NAME = "nfs-client-provisioner";

    private static final String CEPH_RBD_PROVISIONER_DEPLOYMENTNAME = "ceph-rbd-provisioner";

    private static final int NFS_PROVISIONER_USED_TIME = 180;

    private static final String IMAGE_NAME = "provisionerImageName";

    private static final String CEPH_RBD_IMAGE_NAME = "cephRBDImageName";

    private static final int CREATE_FAIL_NUM = -1;

    private static final int CREATE_ING_NUM = 0;

    private static final int CREATE_SUCCESS_NUM = 1;

    @Autowired
    ClusterService clusterService;

    @Autowired
    ScService scService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    PersistentVolumeClaimService persistentVolumeClaimService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    DaemonSetsService daemonSetsService;

    @Autowired
    SecretService secretService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    StatefulSetService statefulSetService;

    @Override
    public ActionReturnUtil createStorageClass(StorageClassDto storageClass) throws Exception {
        AssertUtil.notBlank(storageClass.getName(), DictEnum.NAME);
        AssertUtil.notBlank(storageClass.getType(), DictEnum.FLAG_TYPE);
        //获取集群
        Cluster cluster = clusterService.findClusterById(storageClass.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Cluster topCluster = clusterService.getPlatformCluster();
        //判断Storage class是否已经存在
        StorageClass sc = scService.getScByName(storageClass.getName(), cluster);
        if (sc != null) {
            LOGGER.error("创建Storage class失败，Storage class存储类名称已存在, scName:{}, clusterName:{}", storageClass.getName(), storageClass.getClusterId());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAME_EXIST, storageClass.getName(), true);
        }
        Map<String, String> configMap = storageClass.getConfigMap();
        //目前云平台暂支持NFS存储
        if (NFS_STORAGE.equals(storageClass.getType())){
            String nfsAddr = configMap.get("NFS_SERVER");
            String nfsPath = configMap.get("NFS_PATH");
            if (StringUtils.isBlank(nfsAddr) || StringUtils.isBlank(nfsPath)){
                throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
            }
            List<SystemConfig> systemConfigList = systemConfigService.findByConfigType(NFS_PROVISIONER);
            String provisionerImage = "";
            for (SystemConfig systemConfig : systemConfigList) {
                if (IMAGE_NAME.equals(systemConfig.getConfigName())) {
                    provisionerImage = cluster.getHarborServer().getHarborHost() + ":" +
                            cluster.getHarborServer().getHarborPort() + systemConfig.getConfigValue();
                }
            }
            if (StringUtils.isBlank(provisionerImage)) {
                throw new MarsRuntimeException(ErrorCodeMessage.NFS_PROVISIONER_CONFIG_ERROR);
            }
            //K8S Storage Class不支持NFS存储，这里需创建支持NFS的插件
            Map<String, Object> nfsProvisionerMap = buildNfsProvisionerMap(storageClass.getName(), nfsAddr, nfsPath, provisionerImage);
            ((Map<String, Object>)nfsProvisionerMap.get("spec")).put("replicas", 1);
            String createTime = Long.toString((new Date()).getTime());
            ((Map<String, Object>)((Map<String, Object>)nfsProvisionerMap.get("metadata")).get("annotations")).put("createTime", createTime);
            K8SURL k8SURL = new K8SURL();
            k8SURL.setNamespace(CommonConstant.KUBE_SYSTEM);
            ActionReturnUtil nfsProvisionerReturn = createK8sResource(k8SURL, nfsProvisionerMap, APIGroup.APIS_EXTENSIONS_V1BETA1_VERSION, Resource.DEPLOYMENT, cluster);
            if (!nfsProvisionerReturn.isSuccess()) {
                LOGGER.error("创建NfsProvisioner失败，data:{}", nfsProvisionerReturn.getData());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NFS_PROVISIONER_CREATE_FAIL);
            }
            //创建StorageClass
            Map<String, Object> storageClassMap = buildStorageClassMap(storageClass, nfsAddr, nfsPath);
            ActionReturnUtil storageClassReturn = createK8sResource(new K8SURL(), storageClassMap, APIGroup.APIS_STORAGECLASS_VERSION, Resource.STORAGECLASS, cluster);
            if (!storageClassReturn.isSuccess()) {
                LOGGER.error("创建NfsProvisioner ClusterRole失败，data:{}", storageClassReturn.getData());
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL, DictEnum.STORAGE_CLASS.phrase(), true);
            }
            if(storageClass.getClusterId().equalsIgnoreCase(topCluster.getId())) {
                setupUploadPod(storageClass);
            }

            return storageClassReturn;
        }    else if (CEPH_RBD_STORAGE.equals(storageClass.getType())) {
        String monitors = configMap.get("monitors");
        String pool = configMap.get("pool");
        String adminId = configMap.get("adminId");
        String userId = configMap.get("userId");
        String cephAdminSecret = configMap.get("cephAdminSecret");
        String cephUserSecret = configMap.get("cephUserSecret");
        if (StringUtils.isBlank(monitors) || StringUtils.isBlank(cephAdminSecret) || StringUtils.isBlank(cephUserSecret)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        // 检查ceph rbd 检查是否存在，不存在则需创建
        if(getDeployment(cluster, CEPH_RBD_PROVISIONER_DEPLOYMENTNAME) == null) {
            List<SystemConfig> systemConfigList = systemConfigService.findByConfigType(CEPH_RBD_PROVISIONER);
            String provisionerImage = "";
            for (SystemConfig systemConfig : systemConfigList) {
                if (CEPH_RBD_IMAGE_NAME.equals(systemConfig.getConfigName())) {
                    provisionerImage = cluster.getHarborServer().getHarborHost() + ":" +
                            cluster.getHarborServer().getHarborPort() + systemConfig.getConfigValue();
                }
            }
            if (StringUtils.isBlank(provisionerImage)) {
                throw new MarsRuntimeException(ErrorCodeMessage.CEPH_RBD_PROVISIONER_CONFIG_ERROR);
            }
            Map<String, Object> cephRBDProvisionerMap = buildCephRBDProvisionerMap(provisionerImage);
            ((Map<String, Object>)cephRBDProvisionerMap.get("spec")).put("replicas", 1);
//                String createTime = Long.toString((new Date()).getTime());
//                ((Map<String, Object>)((Map<String, Object>)cephRBDProvisionerMap.get("metadata")).get("annotations")).put("createTime", createTime);
            K8SURL k8SURL = new K8SURL();
            k8SURL.setNamespace(CommonConstant.KUBE_SYSTEM);
            ActionReturnUtil cephRBDProvisionerReturn = createK8sResource(k8SURL, cephRBDProvisionerMap, APIGroup.APIS_EXTENSIONS_V1BETA1_VERSION, Resource.DEPLOYMENT, cluster);
            if (!cephRBDProvisionerReturn.isSuccess()) {
                LOGGER.error("创建CephRBDProvisioner失败，data:{}", cephRBDProvisionerReturn.getData());
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CEPH_RBD_PROVISIONER_CREATE_FAIL);
            }
        }

        // 创建StrorageCLass 和secret
        String uuid = StringUtil.getId();
        String cephAdminSecretName = "ceph-admin-secret-" + uuid;
        String cephUserSecretName = "ceph-user-secret"  + uuid;
        ActionReturnUtil result = createCephRBDStorageClass(cephAdminSecretName, cephAdminSecret, cephUserSecretName, cephUserSecret, storageClass, monitors, pool, adminId, userId, cluster);
        if (!result.isSuccess()) {
            rollBackCephRBDStorageClass(cephAdminSecretName, cephUserSecretName, storageClass.getName(), cluster);
            throw new MarsRuntimeException(result.getData().toString());
        }
        return result;


    }else {
        LOGGER.error("Storage class使用的存储暂不支持");
        return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.STORAGECLASS_TYPE_ERROR, DictEnum.STORAGE_CLASS.phrase(), true);
    }


}

    private Map<String, Object> buildNfsProvisionerMap(String scName, String nfsAddr, String nfsPath, String provisionerImage) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(NFS_PROVISIONER + "/deployment.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String nfsProvisionerString = jsonString.replaceAll("nfsAddr", nfsAddr).replaceAll("nfsPath", nfsPath)
                .replaceAll("scName", scName).replace("imageName", provisionerImage);
        return gson.fromJson(nfsProvisionerString, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private Map<String, Object> buildCephRBDProvisionerMap(String provisionerImage) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(CEPH_RBD_PROVISIONER + "/deployment.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String nfsProvisionerString = jsonString.replace("imageName", provisionerImage);
        return gson.fromJson(nfsProvisionerString, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private Map<String, Object> buildCephRBDAdminSecretMap(String cephAdminSecretName, String cephAdminSecretKey) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(CEPH_RBD_PROVISIONER + "/admin-secret.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String provisionerString = jsonString.replace("cephAdminSecretName", cephAdminSecretName)
                .replace("cephAdminSecretKey", cephAdminSecretKey);
        return gson.fromJson(provisionerString, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private Map<String, Object> buildCephRBDUserSecretMap(String cephUserSecretName, String cephUserSecretKey) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(CEPH_RBD_PROVISIONER + "/user-secret.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String provisionerString = jsonString.replace("cephUserSecretName", cephUserSecretName)
                .replace("cephUserSecretKey", cephUserSecretKey);
        return gson.fromJson(provisionerString, new TypeToken<Map<String, Object>>(){}.getType());
    }


    private Map<String, Object> buildStorageClassMap(StorageClassDto sc, String nfsAddr, String nfsPath) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(NFS_PROVISIONER + "/class.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String storageClassString = jsonString.replace("scName", sc.getName())
                .replace("limitNum", sc.getStorageLimit()).replace("storageType", sc.getType())
                .replace("nfsAddr", nfsAddr).replace("nfsPath", nfsPath);
        return gson.fromJson(storageClassString, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private Map<String, Object> buildCephRBDStorageClassMap(StorageClassDto sc, String monitors, String pool, String adminId, String adminSecretName, String userId, String userSecretName) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(CEPH_RBD_PROVISIONER + "/class.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String storageClassString = jsonString.replace("scName", sc.getName())
                .replace("limitNum", sc.getStorageLimit()).replace("storageType", sc.getType())
                .replace("monitorsAddr", monitors).replace("poolName", pool)
                .replace("adminIdValue", adminId).replace("adminSecretNameValue", adminSecretName)
                .replace("userIdValue", userId).replace("userSecretNameValue", userSecretName);

        return gson.fromJson(storageClassString, new TypeToken<Map<String, Object>>(){}.getType());
    }

    private ActionReturnUtil createK8sResource(K8SURL k8SURL, Map<String, Object> resourceMap, String resourceApiGroup,
                                               String resourceType, Cluster cluster) {
        k8SURL.setResource(resourceType).setApiGroup(resourceApiGroup);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.POST, headers, resourceMap, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(us.getMessage());
        }
        return ActionReturnUtil.returnSuccess();
    }

    private ActionReturnUtil createCephRBDStorageClass(String cephAdminSecretName, String cephAdminSecret, String cephUserSecretName, String cephUserSecret, StorageClassDto storageClass,
                                                       String monitors, String pool, String adminId, String userId, Cluster cluster) throws Exception {
        // 创建admin secret
        Map<String, Object> adminSecretMap = buildCephRBDAdminSecretMap(cephAdminSecretName, cephAdminSecret);
        K8SURL adminK8SURL = new K8SURL();
        adminK8SURL.setNamespace(CommonConstant.KUBE_SYSTEM);
        ActionReturnUtil adminSecretReturn = createK8sResource(adminK8SURL, adminSecretMap, APIGroup.API_V1_VERSION, Resource.SECRET, cluster);
        if (!adminSecretReturn.isSuccess()) {
            LOGGER.error("创建ceph rbd admin secret失败，data:{}", adminSecretReturn.getData());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CEPH_RBD_SECRET_CREATE_FAIL);
        }

        // 创建user secret
        Map<String, Object> userSecretMap = buildCephRBDUserSecretMap(cephUserSecretName, cephUserSecret);
        K8SURL userK8SURL = new K8SURL();
        userK8SURL.setNamespace(CommonConstant.KUBE_SYSTEM);
        ActionReturnUtil userSecretReturn = createK8sResource(userK8SURL, userSecretMap, APIGroup.API_V1_VERSION, Resource.SECRET, cluster);
        if (!userSecretReturn.isSuccess()) {
            LOGGER.error("创建ceph rbd user secret失败，data:{}", userSecretReturn.getData());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CEPH_RBD_SECRET_CREATE_FAIL);
        }
        //创建StorageClass
        Map<String, Object> storageClassMap = buildCephRBDStorageClassMap(storageClass, monitors, pool, adminId, cephAdminSecretName, userId, cephUserSecretName);

        ActionReturnUtil storageClassReturn = createK8sResource(new K8SURL(), storageClassMap, APIGroup.APIS_STORAGECLASS_VERSION, Resource.STORAGECLASS, cluster);
        if (!storageClassReturn.isSuccess()) {
            LOGGER.error("创建StorageClass失败，data:{}", storageClassReturn.getData());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.DELETE_FAIL, DictEnum.STORAGE_CLASS.phrase(), true);
        }
        return ActionReturnUtil.returnSuccess();
    }


    @Override
    public ActionReturnUtil deleteStorageClass(String name, String clusterId) throws Exception {
        AssertUtil.notBlank(name, DictEnum.NAME);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        //查询要被删除的StorageClass是否存在
        ActionReturnUtil scReturn = getStorageClass(name, clusterId);
        if (!scReturn.isSuccess()) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.STORAGE_CLASS.phrase(), true);
        }
        ActionReturnUtil PVCReturn = getStorageClassPVC(name, cluster);
        Gson gson = new Gson();
        PersistentVolumeClaimList persistentVolumeClaimList = gson.fromJson((String) PVCReturn.getData(), PersistentVolumeClaimList.class);
        List<PersistentVolumeClaim> persistentVolumeClaims = persistentVolumeClaimList.getItems();
        for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaims) {
            Map<String,Object> PVCAnnotations = persistentVolumeClaim.getMetadata().getAnnotations();
            if (PVCAnnotations != null && name.equals(PVCAnnotations.get("volume.beta.kubernetes.io/storage-class"))) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.STORAGECLASS_DELETE_ERROR);
            }
        }
        if(clusterId.equalsIgnoreCase(clusterService.getPlatformCluster().getId())) {
            removeUploadPod(name);
        }
        K8SClientResponse response = scService.deleteStorageClassByName(name, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            // 删除secret
            StorageClass storageClass = K8SClient.converToBean(response, StorageClass.class);
            if(storageClass != null && storageClass.getParameters() != null) {
                String adminSecretName = storageClass.getParameters().getAdminSecretName();
                String userSecretName = storageClass.getParameters().getUserSecretName();
                K8SClientResponse adminSecretResponse = secretService.getSpecifiedSecret(adminSecretName, CommonConstant.KUBE_SYSTEM, cluster);
                if (HttpStatusUtil.isSuccessStatus(adminSecretResponse.getStatus())) {
                    Secret secret = K8SClient.converToBean(adminSecretResponse, Secret.class);
                    if (secret != null) {
                        secretService.deleteSecret(adminSecretName, CommonConstant.KUBE_SYSTEM, cluster);
                    }
                }

                K8SClientResponse userSecretRes = secretService.getSpecifiedSecret(userSecretName, CommonConstant.KUBE_SYSTEM, cluster);
                if (HttpStatusUtil.isSuccessStatus(userSecretRes.getStatus())) {
                    Secret userSecret = K8SClient.converToBean(userSecretRes, Secret.class);
                    if(userSecret != null) {
                        secretService.deleteSecret(userSecretName, CommonConstant.KUBE_SYSTEM, cluster);
                    }
                }
            }
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }

    }

    private void rollBackCephRBDStorageClass(String cephAdminrSecretName, String cephUserSecretName, String storageClassName, Cluster cluster) throws Exception {

        StorageClass sc = scService.getScByName(storageClassName, cluster);
        if(sc != null) {
            scService.deleteStorageClassByName(storageClassName, cluster);
        }

        K8SClientResponse adminSecretResponse = secretService.getSpecifiedSecret(cephAdminrSecretName, CommonConstant.KUBE_SYSTEM, cluster);
        if (HttpStatusUtil.isSuccessStatus(adminSecretResponse.getStatus())) {
            Secret secret = K8SClient.converToBean(adminSecretResponse, Secret.class);
            if(secret != null) {
                secretService.deleteSecret(cephAdminrSecretName, CommonConstant.KUBE_SYSTEM, cluster);
            }
        }

        K8SClientResponse userSecretRes = secretService.getSpecifiedSecret(cephUserSecretName, CommonConstant.KUBE_SYSTEM, cluster);
        if (HttpStatusUtil.isSuccessStatus(userSecretRes.getStatus())) {
            Secret userSecret = K8SClient.converToBean(userSecretRes, Secret.class);
            if(userSecret != null) {
                secretService.deleteSecret(cephUserSecretName, CommonConstant.KUBE_SYSTEM, cluster);
            }
        }



    }

    private ActionReturnUtil getStorageClassPVC(String scName, Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM).setApiGroup(APIGroup.API_V1_VERSION);
        Map<String, Object> queryParams = new HashMap<>();
        Map<String, String> labels = new HashMap<>();
        labels.put("storage-class", scName);
        queryParams.put("labelSelector", labels);
        k8SURL.setQueryParams(queryParams);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus us = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(us.getMessage());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }


    @Override
    public ActionReturnUtil getStorageClass(String name, String clusterId) throws Exception {
        AssertUtil.notBlank(name, DictEnum.NAME);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        StorageClass sc = scService.getScByName(name, cluster);
        if (sc == null) {
            LOGGER.info("StorageClass不存在,storageClassName:{},clusterName:{}", name, cluster.getName());
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL, DictEnum.STORAGE_CLASS.phrase(), true);
        }
        //storageClass关联的PVC
        List<PersistentVolumeClaim> pvcList = new ArrayList<>();
        List<PersistentVolumeClaim> persistentVolumeClaimList = listPvc(cluster).getItems();
        for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaimList) {
            if(persistentVolumeClaim.getMetadata().getAnnotations() != null) {
                String storageClassName = (String) persistentVolumeClaim.getMetadata().getAnnotations().get("volume.beta.kubernetes.io/storage-class");
                if (name.equals(storageClassName)) {
                    pvcList.add(persistentVolumeClaim);
                }
            }
        }

        //PVC关联的服务,DaemonSet
        List serviceList = new ArrayList<>();
        List<Deployment> deploymentList = listDeployment(cluster).getItems();
        List<DaemonSet> daemonSetList = daemonSetsService.listDaemonSets(cluster);
        List<StatefulSet> statefulSetList = statefulSetService.listStatefulSets(null, null, null, cluster).getItems();
        for (PersistentVolumeClaim pvc : pvcList) {
            for (Deployment deployment : deploymentList) {
                if (pvc.getMetadata().getNamespace().equalsIgnoreCase(deployment.getMetadata().getNamespace()) && deployment.getSpec().getTemplate().getSpec().getVolumes() != null && deployment.getSpec().getTemplate().getSpec().getVolumes().size() > 0) {
                    List<Volume> volumeList = deployment.getSpec().getTemplate().getSpec().getVolumes();
                    for (Volume volume : volumeList) {
                        if (volume.getPersistentVolumeClaim() != null && pvc.getMetadata().getName().equals(volume.getPersistentVolumeClaim().getClaimName())) {
                            Map<String, Object> serviceItem = convertServiceItem(deployment.getMetadata(), deployment.getSpec().getTemplate());
                            serviceItem.put("type", CommonConstant.LABEL_KEY_APP);
                            serviceItem.put("status", K8sResultConvert.getDeploymentStatus(deployment));
                            serviceItem.put("instance", deployment.getSpec().getReplicas());
                            serviceList.add(serviceItem);
                        }
                    }
                }
            }
            for (StatefulSet statefulset : statefulSetList){
                if (pvc.getMetadata().getNamespace().equalsIgnoreCase(statefulset.getMetadata().getNamespace()) && CollectionUtils.isNotEmpty(statefulset.getSpec().getTemplate().getSpec().getVolumes())) {
                    List<Volume> volumeList = statefulset.getSpec().getTemplate().getSpec().getVolumes();
                    for (Volume volume : volumeList) {
                        if (volume.getPersistentVolumeClaim() != null && pvc.getMetadata().getName().equals(volume.getPersistentVolumeClaim().getClaimName())) {
                            Map<String, Object> serviceItem = convertServiceItem(statefulset.getMetadata(), statefulset.getSpec().getTemplate());
                            serviceItem.put("type", CommonConstant.LABEL_KEY_STATEFULSET);
                            serviceItem.put("status", K8sResultConvert.getStatefulSetStatus(statefulset));
                            serviceItem.put("instance", statefulset.getSpec().getReplicas());
                            serviceList.add(serviceItem);
                        }
                    }
                }
            }
            if(CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(pvc.getMetadata().getNamespace())){
                for(DaemonSet daemonSet : daemonSetList){
                    List<Volume> volumeList = daemonSet.getSpec().getTemplate().getSpec().getVolumes();
                    if(CollectionUtils.isEmpty(volumeList)){
                        continue;
                    }
                    for (Volume volume : volumeList) {
                        if (volume.getPersistentVolumeClaim() != null && pvc.getMetadata().getName().equals(volume.getPersistentVolumeClaim().getClaimName())) {
                            Map<String, Object> serviceItem = convertServiceItem(daemonSet.getMetadata(), daemonSet.getSpec().getTemplate());
                            serviceItem.put("type", CommonConstant.LABEL_KEY_DAEMONSET);
                            serviceItem.put("status", daemonSetsService.convertDaemonStatus(daemonSet));
                            serviceList.add(serviceItem);
                        }
                    }
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(convertScDto(sc, serviceList, cluster));
    }

    private Map<String, Object> convertServiceItem(ObjectMeta objectMeta, PodTemplateSpec podTemplateSpec) throws Exception {
        Map<String, Object> serviceItem = new HashMap<>();
        serviceItem.put("name", objectMeta.getName());
        serviceItem.put("namespace", objectMeta.getNamespace());
        NamespaceLocal namespace = namespaceLocalService.getNamespaceByName(objectMeta.getNamespace());
        if(namespace != null) {
            serviceItem.put("aliasNamespace", namespace.getAliasName());
        }else{
            serviceItem.put("aliasNamespace", objectMeta.getNamespace());
        }
        List<String> img = new ArrayList<String>();
        List<String> cpu = new ArrayList<String>();
        List<String> memory = new ArrayList<String>();
        List<Container> containers = podTemplateSpec.getSpec().getContainers();
        for (Container container : containers) {
            img.add(container.getImage());
            if (container.getResources() != null && container.getResources().getRequests() != null) {
                Map<String, String> res1 = (Map<String, String>) container.getResources().getRequests();
                cpu.add(res1.get("cpu"));
                memory.add(res1.get("memory"));
            } else if (container.getResources() != null && container.getResources().getLimits() != null) {
                Map<String, String> res1 = (Map<String, String>) container.getResources().getLimits();
                cpu.add(res1.get("cpu"));
                memory.add(res1.get("memory"));
            }
        }
        serviceItem.put("img", img);
        serviceItem.put("cpu", cpu);
        serviceItem.put("memory", memory);

        Map<String, Object> labelsMap = new HashMap<>();
        if (objectMeta.getAnnotations() != null && objectMeta.getAnnotations().containsKey("nephele/labels")) {
            String labels = objectMeta.getAnnotations().get("nephele/labels").toString();
            if (!StringUtils.isEmpty(labels)) {
                String[] arrLabel = labels.split(",");
                for (String l : arrLabel) {
                    String[] tmp = l.split("=");
                    labelsMap.put(tmp[0], tmp[1]);
                }
            }
        }

        serviceItem.put("labels", labelsMap);
        Date utcDate = DateUtil.StringToDate(objectMeta.getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        serviceItem.put("createTime", utcDate);
        return serviceItem;
    }

    private PersistentVolumeClaimList listPvc(Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
        k8SURL.setResource(Resource.PERSISTENTVOLUMECLAIM);
        K8SClientResponse pvcResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcResponse.getStatus())) {
            throw new MarsRuntimeException("查询PVC失败！");
        }
        return JsonUtil.jsonToPojo(pvcResponse.getBody(), PersistentVolumeClaimList.class);
    }

    private DeploymentList listDeployment(Cluster cluster) {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
        k8SURL.setResource(Resource.DEPLOYMENT);
        K8SClientResponse depResponse = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depResponse.getStatus())) {
            throw new MarsRuntimeException("查询Deployment失败！");
        }
        return JsonUtil.jsonToPojo(depResponse.getBody(), DeploymentList.class);
    }

    private StorageClassDto convertScDto(StorageClass sc, List serviceList , Cluster cluster) throws Exception {
        StorageClassDto storageClassDto = new StorageClassDto();
        storageClassDto.setName(sc.getMetadata().getName());
        storageClassDto.setClusterId(cluster.getId());
        Date utcDate = DateUtil.StringToDate(sc.getMetadata().getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        storageClassDto.setCreateTime(utcDate);
        if (sc.getMetadata().getAnnotations() != null) {
            if (sc.getMetadata().getAnnotations().get("type") != null) {
                if (sc.getMetadata().getAnnotations().get("type").equals(NFS_STORAGE)) {
                    ActionReturnUtil nfsProvisioner = getNfsProvisionerStatus(sc.getMetadata().getName(), cluster);
                    storageClassDto.setStatus((int)(nfsProvisioner.get("count")));
                    if (sc.getMetadata().getAnnotations() != null) {
                        if (sc.getMetadata().getAnnotations().get("type") != null) {
                            storageClassDto.setType((String)(sc.getMetadata().getAnnotations().get("type")));
                        }
                        if (sc.getMetadata().getAnnotations().get("storageLimit") != null) {
                            storageClassDto.setStorageLimit((String)(sc.getMetadata().getAnnotations().get("storageLimit")));
                        }
                        Map<String, String> configMap = new HashMap<>();
                        if (sc.getMetadata().getAnnotations().get("NFSADDR") != null) {
                            configMap.put("NFS_SERVER", (String )(sc.getMetadata().getAnnotations().get("NFSADDR")));
                        }
                        if (sc.getMetadata().getAnnotations().get("NFSPATH") != null) {
                            configMap.put("NFS_PATH", (String )(sc.getMetadata().getAnnotations().get("NFSPATH")));
                        }
                        storageClassDto.setConfigMap(configMap);
                    }
                } else if (sc.getMetadata().getAnnotations().get("type").equals(CEPH_RBD_STORAGE)) {
                    storageClassDto.setStatus(CREATE_SUCCESS_NUM);
                    if (sc.getMetadata().getAnnotations() != null) {
                        if (sc.getMetadata().getAnnotations().get("type") != null) {
                            storageClassDto.setType((String)(sc.getMetadata().getAnnotations().get("type")));
                        }
                        if (sc.getMetadata().getAnnotations().get("storageLimit") != null) {
                            storageClassDto.setStorageLimit((String)(sc.getMetadata().getAnnotations().get("storageLimit")));
                        }
                        Map<String, String> configMap = new HashMap<>();
                        if (sc.getParameters().getMonitors() != null) {
                            configMap.put("monitors", (String )(sc.getParameters().getMonitors()));
                        }
                        if (sc.getParameters().getPool() != null) {
                            configMap.put("pool", (String )(sc.getParameters().getPool()));
                        }
                        if (sc.getParameters().getAdminId() != null) {
                            configMap.put("adminId", (String )(sc.getParameters().getAdminId()));
                        }
                        if (sc.getParameters().getUserId() != null) {
                            configMap.put("userId", (String )(sc.getParameters().getUserId()));
                        }

                        if (sc.getParameters().getAdminSecretName() != null) {
                            K8SClientResponse adminSecretResponse = secretService.getSpecifiedSecret(sc.getParameters().getAdminSecretName(), CommonConstant.KUBE_SYSTEM, cluster);
                            if (HttpStatusUtil.isSuccessStatus(adminSecretResponse.getStatus())) {
                                Secret secret = K8SClient.converToBean(adminSecretResponse, Secret.class);
                                if(secret != null) {
                                    Map<String, Object> data = (Map<String, Object>) secret.getData();
                                    if(data != null) {
                                        configMap.put("cephAdminSecret", data.get("key").toString());
                                    }
                                }
                            }
                        }

                        if (sc.getParameters().getUserSecretName() != null) {
                            K8SClientResponse userSecretResponse = secretService.getSpecifiedSecret(sc.getParameters().getUserSecretName(), CommonConstant.KUBE_SYSTEM, cluster);
                            if (HttpStatusUtil.isSuccessStatus(userSecretResponse.getStatus())) {
                                Secret secret = K8SClient.converToBean(userSecretResponse, Secret.class);
                                if(secret != null) {
                                    Map<String, Object> data = (Map<String, Object>) secret.getData();
                                    if(data != null) {
                                        configMap.put("cephUserSecret", data.get("key").toString());
                                    }
                                }
                            }
                        }

                        storageClassDto.setConfigMap(configMap);
                    }
                }
                if (sc.getMetadata().getAnnotations().get("remain") != null) {
                    storageClassDto.setNamespaceRemainLimit((Integer)sc.getMetadata().getAnnotations().get("remain"));
                }
            }
        }
        if (serviceList != null && serviceList.size() > 0) {
            storageClassDto.setServiceList(serviceList);
        }
        return storageClassDto;
    }


    private Deployment getDeployment(Cluster cluster, String name) throws Exception {
        K8SURL k8SURL = new K8SURL();
        k8SURL.setApiGroup(APIGroup.API_V1_VERSION);
        k8SURL.setNamespace(CommonConstant.KUBE_SYSTEM);
        k8SURL.setResource(Resource.DEPLOYMENT).setSubpath(name);
        K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, null, cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
        }
        return null;
    }

    @Override
    public List<StorageClassDto> listStorageClass(String clusterId) throws Exception {
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        List<StorageClassDto> storageClassDtos = new ArrayList<>();

        List<StorageClass> storageClasses = scService.litStorageClassByClusterId(cluster);
        if (!Objects.isNull(storageClasses)) {
            for (StorageClass sc  : storageClasses) {
                StorageClassDto storageClassDto = convertScDto(sc, null, cluster);
                storageClassDtos.add(storageClassDto);
            }
        }
        return storageClassDtos;
    }

    @Override
    public List<StorageClassDto> listStorageClass(String clusterId, String namespace) throws Exception {
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        //获取集群
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        List<StorageClassDto> storageClassDtos = new ArrayList<>();

        List<StorageClass> storageClasses = scService.litStorageClassByClusterId(cluster);

        //根据分区过滤storageClass
        if(StringUtils.isNotBlank(namespace) && !CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(namespace)){
            ResourceQuotaList resourceQuotaList = namespaceService.getResouceQuota(namespace, cluster);
            ResourceQuota resourceQuota = resourceQuotaList.getItems().get(0);
            LinkedHashMap<String,Object> hards = (LinkedHashMap<String, Object>) resourceQuota.getSpec().getHard();
            Map<String, Object> used = (Map<String, Object>) resourceQuota.getStatus().getUsed();
            storageClasses = storageClasses.stream().filter(storageClass -> {
                String name = storageClass.getMetadata().getName();
                if (hards.containsKey(name + ".storageclass.storage.k8s.io/requests.storage")) {
                    String usedStorage = (String) used.get(name + ".storageclass.storage.k8s.io/requests.storage");
                    String hardStorage = (String) hards.get(name + ".storageclass.storage.k8s.io/requests.storage");
                    if (StringUtils.isNotBlank(usedStorage) && StringUtils.isNotBlank(hardStorage)) {
                        Integer usedAmount = Integer.valueOf(usedStorage.replace(CommonConstant.GI, StringUtils.EMPTY));
                        Integer hardAmount = Integer.valueOf(hardStorage.replace(CommonConstant.GI, StringUtils.EMPTY));
                        storageClass.getMetadata().getAnnotations().put("remain", hardAmount - usedAmount);
                    }
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }

        if (!Objects.isNull(storageClasses)) {
            for (StorageClass sc  : storageClasses) {
                StorageClassDto storageClassDto = convertScDto(sc, null, cluster);
                storageClassDtos.add(storageClassDto);
            }
        }

        return storageClassDtos;
    }

    private ActionReturnUtil getNfsProvisionerStatus(String name, Cluster cluster) throws Exception {
        String provisionerName = NFS_PROVISIONER_NAME + "-" + name;
        Deployment nfsProvisioner = scService.getNfsProvisionerByName(provisionerName, cluster);
        if (nfsProvisioner == null) {
            return ActionReturnUtil.returnSuccessWithDataAndCount("", CREATE_FAIL_NUM);
        }
        if (nfsProvisioner.getStatus().getAvailableReplicas() == null) {
            long createTime = Long.parseLong((String) (nfsProvisioner.getMetadata().getAnnotations().get("createTime")));
            long queryTime = (new Date()).getTime();
            int usedTime = DateUtil.getIntervalSeconds(queryTime, createTime);
            if(usedTime < NFS_PROVISIONER_USED_TIME) {
                return ActionReturnUtil.returnSuccessWithDataAndCount(ErrorCodeMessage.NFS_PROVISIONER_CREATE_FAIL, CREATE_ING_NUM);
            } else {
                return ActionReturnUtil.returnSuccessWithDataAndCount(ErrorCodeMessage.NFS_PROVISIONER_CREATE_FAIL, CREATE_FAIL_NUM);
            }
        }
        return ActionReturnUtil.returnSuccessWithDataAndCount("", CREATE_SUCCESS_NUM);
    }

    /**
     * 读取yaml生成Map对象并返回
     *
     * @param yamlPath yaml文件路径
     * @return Map<String, Object>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> yamlToMap(String yamlPath) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(yamlPath);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = (Map<String, Object>) yaml.load(is);
        is.close();
        return yamlMap;
    }

    /**
     * 创建file-upload pod
     * @param storageClass
     * @throws Exception
     */
    private void setupUploadPod(StorageClassDto storageClass) throws Exception{
        Cluster topCluster = clusterService.getPlatformCluster();
        Gson gson = new Gson();
        String deployString = "";
        //NFS存储
        if (NFS_STORAGE.equals(storageClass.getType())) {
            String nfsAddr = storageClass.getConfigMap().get(CommonConstant.NFS_SERVER);
            String nfsPath = storageClass.getConfigMap().get(CommonConstant.NFS_PATH);
            if (StringUtils.isBlank(nfsAddr) || StringUtils.isBlank(nfsPath)) {
                throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
            }
            List<SystemConfig> systemConfigList = systemConfigService.findByConfigType("dependence");
            String dependenceImage = "";
            String dependenceImageCmd = "";
            for (SystemConfig systemConfig : systemConfigList) {
                if ("dependenceUploadImageName".equals(systemConfig.getConfigName())) {
                    dependenceImage = topCluster.getHarborServer().getHarborHost() + ":" +
                            topCluster.getHarborServer().getHarborPort() + systemConfig.getConfigValue();
                } else if ("dependenceUploadImageCmd".equals(systemConfig.getConfigName())) {
                    dependenceImageCmd = systemConfig.getConfigValue();
                }
            }


            Map<String, Object> jsonMap = yamlToMap("dependence/deployment-nfs.yaml");

            String jsonString = gson.toJson(jsonMap);
            deployString = jsonString.replaceAll("nfsAddr", nfsAddr).replaceAll("nfsPath", nfsPath)
                    .replaceAll("scName", storageClass.getName()).replace("imageName", dependenceImage).replace("\"imageCmd\"", dependenceImageCmd);
        }
        Map<String, Object> deployMap = gson.fromJson(deployString, new TypeToken<Map<String, Object>>(){}.getType());
        ((Map<String, Object>)deployMap.get("spec")).put("replicas", 1);

        K8SURL k8surl = new K8SURL();
        k8surl.setNamespace(CommonConstant.KUBE_SYSTEM).setResource(Resource.DEPLOYMENT).setApiGroup(APIGroup.APIS_EXTENSIONS_V1BETA1_VERSION);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();
        K8SClientResponse response = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, deployMap, topCluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            LOGGER.error("create upload pod failed: {}", status.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.FILE_UPLOAD_POD_CREATE_ERROR);
        }
    }

    /**
     * 删除file-upload pod
     * @param name
     * @throws Exception
     */
    private void removeUploadPod(String name) throws Exception{
        Cluster topCluster = clusterService.getPlatformCluster();
        String deployName = CommonConstant.FILE_UPLOAD_POD_NAME_PREFIX + "-" + name;
        K8SClientResponse delRes = deploymentService.doSpecifyDeployment(CommonConstant.KUBE_SYSTEM, deployName, null, null, HTTPMethod.DELETE, topCluster);
        if(!HttpStatusUtil.isSuccessStatus(delRes.getStatus())){
            UnversionedStatus status = JsonUtil.jsonToPojo(delRes.getBody(), UnversionedStatus.class);
            LOGGER.error("delete upload pod failed: {}", status.getMessage());
            throw new MarsRuntimeException(ErrorCodeMessage.FILE_UPLOAD_POD_REMOVE_ERROR);
        }
    }


}