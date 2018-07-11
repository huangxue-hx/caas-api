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
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.ScService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.system.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author xc
 * @date 2018/6/14 14:36
 */
@Service
public class StorageClassServiceImpl implements StorageClassService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageClassServiceImpl.class);

    private static final String NFS_STORAGE = "NFS";

    private static final String NFS_PROVISIONER = "nfs-provisioner";

    private static final String NFS_PROVISIONER_NAME = "nfs-client-provisioner";

    private static final int NFS_PROVISIONER_USED_TIME = 180;

    private static final String IMAGE_NAME = "imageName";

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

    @Override
    public ActionReturnUtil createStorageClass(StorageClassDto storageClass) throws Exception {
        AssertUtil.notBlank(storageClass.getName(), DictEnum.NAME);
        AssertUtil.notBlank(storageClass.getType(), DictEnum.FLAG_TYPE);
        //获取集群
        Cluster cluster = clusterService.findClusterById(storageClass.getClusterId());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
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
            return storageClassReturn;
        } else {
            LOGGER.error("Storage class使用的存储暂时仅支持NFS");
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

    private Map<String, Object> buildStorageClassMap(StorageClassDto sc, String nfsAddr, String nfsPath) throws Exception {
        Map<String, Object> jsonMap = yamlToMap(NFS_PROVISIONER + "/class.yaml");
        Gson gson = new Gson();
        String jsonString = gson.toJson(jsonMap);
        String storageClassString = jsonString.replace("scName", sc.getName())
                .replace("limitNum", sc.getStorageLimit()).replace("storageType", sc.getType())
                .replace("nfsAddr", nfsAddr).replace("nfsPath", nfsPath);
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
            if (name.equals(PVCAnnotations.get("volume.beta.kubernetes.io/storage-class"))) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.STORAGECLASS_DELETE_ERROR);
            }
        }

        K8SClientResponse response = scService.deleteStorageClassByName(name, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnSuccess();
        } else {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL, DictEnum.STORAGE_CLASS.phrase(), true);
        }
        return ActionReturnUtil.returnSuccessWithData(convertScDto(sc, cluster));
    }

    private StorageClassDto convertScDto(StorageClass sc, Cluster cluster) throws Exception {
        StorageClassDto storageClassDto = new StorageClassDto();
        storageClassDto.setName(sc.getMetadata().getName());
        storageClassDto.setClusterId(cluster.getId());
        Date utcDate = DateUtil.StringToDate(sc.getMetadata().getCreationTimestamp(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        storageClassDto.setCreateTime(utcDate);
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
        return storageClassDto;
    }

    @Override
    public ActionReturnUtil listStorageClass(String clusterId) throws Exception {
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
                StorageClassDto storageClassDto = convertScDto(sc, cluster);
                storageClassDtos.add(storageClassDto);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(storageClassDtos);
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
}