package com.harmonycloud.service.migrate.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.UUIDUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.scale.ComplexPodScale;
import com.harmonycloud.k8s.bean.scale.ComplexPodScaleList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.service.PVCService;
import com.harmonycloud.k8s.service.PvService;
import com.harmonycloud.k8s.service.ScService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.migrate.DataMigrateService;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.VolumeMountExt;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.service.platform.convert.K8sResultConvert.TAG_LENGTH;
import static com.harmonycloud.service.platform.convert.K8sResultConvert.TAG_PATTERN;


/**
 * 版本升级数据迁移
 */
@Service
public class DataMigrateServiceImpl implements DataMigrateService{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataMigrateServiceImpl.class);
    private List<Cluster> clusters;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private ConfigCenterService configCenterService;
    @Autowired
    private DeploymentsService deploymentsService;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private PvService pvService;
    @Autowired
    private PVCService pvcService;
    @Autowired
    private ScService scService;

    /**
     * 版本升级数据迁移
     * @param version
     * @return 错误信息列表
     */
    @Override
    public List<String> migrateData(String version, boolean execute) throws Exception{
        clusters = clusterService.listCluster();
        List<String> messages = new ArrayList<>();
        messages.addAll(this.migrateConfigMap(execute));
        messages.addAll(this.migrateStorage(execute));
        messages.addAll(this.migrateServiceEntry(execute));
        messages.addAll(this.migrateAutoscale(execute));
        messages.addAll(this.migrateDependence(execute));
        return messages;
    }

    private List<String> migrateConfigMap(boolean execute) throws Exception{
        List<String> messages = new ArrayList<>();
        for(Cluster cluster: clusters){
            Map<String,List<String>> configGroups = new HashMap<>();
            List<NamespaceLocal> namespaceLocals = namespaceLocalService.getNamespaceListByClusterId(cluster.getId());
            for(NamespaceLocal namespaceLocal : namespaceLocals) {
                DeploymentList deploymentList = deploymentsService.listDeployments(namespaceLocal.getNamespaceName(), null);
                if(deploymentList == null){
                    LOGGER.warn("查询服务列表返回null，clusterId：{}，namespace：{}", cluster.getId(), namespaceLocal.getNamespaceName());
                    continue;
                }
                List<Deployment> deployments = deploymentList.getItems();
                if(CollectionUtils.isEmpty(deployments)){
                    continue;
                }
                for(Deployment dep : deployments){
                    List<ContainerOfPodDetail> containerOfPodDetails = K8sResultConvert.convertDeploymentContainer(dep,
                            dep.getSpec().getTemplate().getSpec().getContainers(), cluster);

                    for(ContainerOfPodDetail container : containerOfPodDetails){
                        List<VolumeMountExt> volumeMountExts= container.getStorage();
                        if(CollectionUtils.isEmpty(volumeMountExts)){
                            continue;
                        }
                        //过滤，是否有配置文件挂载
                        List<VolumeMountExt> configVolumeMount = volumeMountExts.stream()
                                .filter(volme -> "configMap".equals(volme.getType())).collect(Collectors.toList());
                        if(CollectionUtils.isEmpty(configVolumeMount)){
                            continue;
                        }
                        List<String> configFileGroupKey = new ArrayList<>();
                        ConfigFile configFile = this.getConfigFile(configVolumeMount, cluster,namespaceLocal,dep,messages, configFileGroupKey);
                        if(configFile == null){
                            continue;
                        }
                        //配置组是否已经存在，如果已经存在，获取存在的配置组的id
                        String existConfigMapId = this.exist(configFile, configGroups, configFileGroupKey, dep, cluster);
                        boolean exist = StringUtils.isNotBlank(existConfigMapId);
                        if(exist){
                            configFile.setId(existConfigMapId);
                        }
                        this.migrateConfig(configFile, execute, exist, dep, namespaceLocal, container, messages, cluster);
                    }
                }
            }
        }
        return messages;
    }

    private ConfigFile getConfigFile(List<VolumeMountExt> configVolumeMount, Cluster cluster,
                                     NamespaceLocal namespaceLocal,Deployment dep, List<String> messages, List<String> configFileGroupKey){
        String projectId = (String) dep.getMetadata().getLabels().get("harmonycloud.cn/projectId");
        if(configVolumeMount.size()>1){
            LOGGER.info("检测到配置组，clusterId:{},namespace：{},deployment：{}",
                    cluster.getId(),namespaceLocal.getNamespaceName(),dep.getMetadata().getName());
        }
        ConfigFile configFile = null;
        for(VolumeMountExt volumeMountExt : configVolumeMount){
            String tag = "";
            String name = "";
            String volumeMountOldName = volumeMountExt.getOldName();
            if(volumeMountOldName.length()>TAG_LENGTH) {
                tag = volumeMountOldName.substring(volumeMountOldName.length()-TAG_LENGTH);
                if(tag.matches(TAG_PATTERN)){
                    name = volumeMountOldName.substring(0,volumeMountOldName.length()-TAG_LENGTH);
                    tag = tag.replace("v","").replace("-",".");
                }else{
                    LOGGER.error("配置文件名称格式错误,name:{}", volumeMountOldName);
                    messages.add("配置文件名称格式不匹配,或已经更新为新版容器平台匹配的名称。name：" + volumeMountOldName);
                    continue;
                }
            }else{
                LOGGER.error("配置文件名称格式错误,name:{}", volumeMountOldName);
                messages.add("配置文件名称格式错误,name：" + volumeMountOldName);
                continue;
            }

            LOGGER.info("配置文件，clusterId:{},namespace：{},deployment：{},name:{},tag:{},mountPath：{}",
                    cluster.getId(),namespaceLocal.getNamespaceName(),dep.getMetadata().getName(),
                    name,tag,volumeMountExt.getMountPath());
            if(configFile == null){
                configFile = configCenterService
                        .getConfigByNameAndTag(name,tag,projectId, cluster.getId());
                if(configFile == null){
                    LOGGER.error("配置文件未找到,name:{},tag:{},projectId:{},clusterId:{}",
                            name,tag,projectId, cluster.getId());
                    messages.add("配置文件未找到, clusterId:{"+cluster.getId() +"},projectId:{"+projectId+"}"
                            +"name:{"+name+"},tag:{"+tag+"}");
                    continue;
                }
                String configFileName = dep.getMetadata().getName() + "-" + cluster.getName();
                configFile.setName(configFileName);
                configFileGroupKey.add(cluster.getId()+"-"+projectId+"-"+name+"-"+tag);
            }else{
                ConfigFile config = configCenterService
                        .getConfigByNameAndTag(name,tag,projectId, cluster.getId());
                if(config == null){
                    LOGGER.error("配置文件未找到,name:{},tag:{},projectId:{},clusterId:{}",
                            name,tag,projectId, cluster.getId());
                    messages.add("配置文件未找到, clusterId:{"+cluster.getId() +"},projectId:{"+projectId+"}"
                            +"name:{"+name+"},tag:{"+tag+"}");
                    continue;
                }
                configFile.getConfigFileItemList().addAll(config.getConfigFileItemList());
                configFileGroupKey.add(cluster.getId()+"-"+projectId+"-"+name+"-"+tag);
            }

        }
        return configFile;
    }

    private String exist(ConfigFile configFile, Map<String,List<String>> configGroups,
                            List<String> configFileGroupKey, Deployment dep, Cluster cluster){
        String existConfigMapId = "";
        if(configFile != null && configFile.getConfigFileItemList().size() >1) {
            //判断是否有不同的服务使用相同的配置文件组，如果相同只需要保存一组配置文件
            for(String key:configGroups.keySet()){
                List<String> existConfigFileGroupKey = configGroups.get(key);
                if(existConfigFileGroupKey.size() != configFileGroupKey.size()){
                    continue;
                }
                boolean contains = true;
                for(String configKey : configFileGroupKey){
                    if(!existConfigFileGroupKey.contains(configKey)){
                        contains = false;
                        break;
                    }
                }
                //存在相同的配置组
                if(contains){
                    existConfigMapId = configFile.getId();
                    LOGGER.info("不同服务使用相同的配置，deploy：{},exist:{}, config:{}",
                            dep.getMetadata().getName(),key,JSONObject.toJSONString(configFileGroupKey));
                    break;
                }else{
                    continue;
                }
            }
            configGroups.put(configFile.getId(),configFileGroupKey);
        }
        return existConfigMapId;
    }

    private void migrateConfig(ConfigFile configFile, boolean execute,boolean exist,Deployment dep,
                               NamespaceLocal namespaceLocal,ContainerOfPodDetail container, List<String> messages, Cluster cluster )throws Exception{
        //创建配置组
        if(configFile.getConfigFileItemList().size() >1 && !exist){
            LOGGER.info("保存配置组到数据库，configFile：{}", JSONObject.toJSONString(configFile));
            if(execute) {
                configFile.setId(UUIDUtil.get16UUID());
                ActionReturnUtil res = configCenterService.saveConfig(configFile);
                if (!res.isSuccess()) {
                    LOGGER.error("保存配置组失败，configFile：{},res:{}", JSONObject.toJSONString(configFile), JSONObject.toJSONString(res));
                    messages.add("保存配置组失败,configFileName:" + configFile.getName() + ", message:"+res.getData().toString());
                }
            }
        }
        Map<String, Object> addAnnotations = new HashMap<>();
        addAnnotations.put("configmapid-" + container.getName(), configFile.getId());
        LOGGER.info("更新服务注解，deploy：{},namespace:{},annotations:{}，", dep.getMetadata().getName(),namespaceLocal.getNamespaceName(),
                "configmapid-" + container.getName()+"="+ configFile.getId());
        if(execute) {
            ActionReturnUtil res = deploymentsService.updateAnnotations(namespaceLocal.getNamespaceName(),
                    dep.getMetadata().getName(), cluster, addAnnotations);
            if (!res.isSuccess()) {
                LOGGER.error("更新服务注解失败，deployment：{},res:{}", dep.getMetadata().getName(), JSONObject.toJSONString(res));
                messages.add("更新服务注解失败,deployment:" + dep.getMetadata().getName()
                        +", annotations:"+"configmapid-" + container.getName()+"="+ configFile.getId());
            }
        }
    }

    private List<String> migrateStorage(boolean execute) throws Exception {
        List<String> messages = new ArrayList<>();
        for(Cluster cluster: clusters){
            List<StorageClass> storageClassList = scService.litStorageClassByClusterId(cluster);
            if(CollectionUtils.isEmpty(storageClassList)){
                LOGGER.error("storageClass不存在, clusterId: {}", cluster.getId());
                messages.add("storageClass不存在, clusterId: " + cluster.getId());
                continue;
            }
            StorageClass storageClass = storageClassList.get(0);
            K8SClientResponse pvRes = pvService.listPv(cluster);
            if(!HttpStatusUtil.isSuccessStatus(pvRes.getStatus())){
                LOGGER.error("获取pv失败, clusterId: {}", cluster.getId());
                messages.add("获取pv失败, clusterId: " + cluster.getId());
                continue;
            }
            PersistentVolumeList pvList = JsonUtil.jsonToPojo(pvRes.getBody(), PersistentVolumeList.class);
            if(!CollectionUtils.isEmpty(pvList.getItems())){
                for(PersistentVolume pv : pvList.getItems()){
                    String name = pv.getMetadata().getName();
                    LOGGER.info("存储,pv: {}, status: {}, clusterId: {}", name, pv.getStatus().getPhase(), cluster.getId());
                    Map<String, Object> labels = pv.getMetadata().getLabels();
                    if(labels == null || !labels.containsKey(CommonConstant.PROJECTID)){
                        continue;
                    }
                    String projectId = (String)labels.get(CommonConstant.PROJECTID);

                    if(pv.getSpec().getClaimRef() != null){
                        //pv正绑定或曾绑定pvc
                        String namespace = pv.getSpec().getClaimRef().getNamespace();
                        PersistentVolumeClaim pvc  = pvcService.getPVCByNameAndNamespace(name, namespace, cluster);
                        if(pvc == null) {
                            //pvc不存在，pv released
                            LOGGER.info("更新pv: {}", name);
                            if(execute) {
                                pv.getSpec().setClaimRef(null);
                                pv.getSpec().setStorageClassName(storageClass.getMetadata().getName());
                                K8SClientResponse updateRes = pvService.updatePvByName(pv, cluster);
                                if (!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                                    LOGGER.error("更新pv失败, pv: {}, res: {}", name, updateRes.getBody());
                                    messages.add("更新pv失败, pv: " + name);
                                }
                            }
                            LOGGER.info("创建pvc: {}, namespace: {}", name, namespace);
                            if(execute) {
                                pvc = new PersistentVolumeClaim();
                                ObjectMeta metadata = new ObjectMeta();
                                metadata.setName(name);
                                metadata.setNamespace(namespace);
                                Map<String, Object> pvcLabels = new HashMap<>();
                                if (!CommonConstant.KUBE_SYSTEM.equals(namespace)) {
                                    pvcLabels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.PROJECTID, projectId);
                                }
                                pvcLabels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.TYPE, CommonConstant.STORAGE);
                                metadata.setLabels(pvcLabels);
                                pvc.setMetadata(metadata);
                                PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
                                spec.setVolumeName(name);
                                spec.setStorageClassName(storageClass.getMetadata().getName());
                                ResourceRequirements resourceRequirements = new ResourceRequirements();
                                Map<String, String> requests = new HashMap<>();
                                Map<String, Object> capacity = (Map<String, Object>) pv.getSpec().getCapacity();
                                requests.put(CommonConstant.STORAGE, (String) capacity.get(CommonConstant.STORAGE));
                                resourceRequirements.setRequests(requests);
                                spec.setResources(resourceRequirements);
                                spec.setAccessModes(pv.getSpec().getAccessModes());
                                pvc.setSpec(spec);
                                try {
                                    K8SClientResponse createRes = pvcService.createPvc(namespace, pvc, cluster);
                                    if (!HttpStatusUtil.isSuccessStatus(createRes.getStatus())) {
                                        throw new Exception(createRes.getBody());
                                    }
                                }catch(Exception e){
                                    LOGGER.error("创建pvc失败, pvc: {}, res: {}", name, e.getMessage());
                                    messages.add("创建pvc失败, pvc: " + name);
                                }

                            }
                        }else{
                            //pvc存在，pv bound
                            LOGGER.info("更新pvc: {}, namespace: {}", name, namespace);
                            if(execute) {
                                Map<String, Object> pvcLabels = pvc.getMetadata().getLabels();
                                //更新服务和守护进程标签
                                if (pvcLabels.containsKey(CommonConstant.LABEL_KEY_APP)) {
                                    String app = (String) pvcLabels.get(CommonConstant.LABEL_KEY_APP);
                                    pvcLabels.remove(CommonConstant.LABEL_KEY_APP);
                                    pvcLabels.put(CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + app, app);
                                }
                                if(pvcLabels.containsKey(Constant.TYPE_DAEMONSET)){
                                    String daemonset = (String) pvcLabels.get(Constant.TYPE_DAEMONSET);
                                    pvcLabels.remove(Constant.TYPE_DAEMONSET);
                                    pvcLabels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_DAEMONSET  + CommonConstant.LINE + daemonset, daemonset);
                                }
                                //更新项目id标签
                                if (!CommonConstant.KUBE_SYSTEM.equals(namespace)) {
                                    pvcLabels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.PROJECTID, projectId);
                                }
                                //增加存储标签
                                pvcLabels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.TYPE, CommonConstant.STORAGE);
                                //已存在的pvc加上storageclass的注释
                                Map<String, Object> annotations = pvc.getMetadata().getAnnotations();
                                if(annotations == null){
                                    annotations = new HashMap<>();
                                }
                                annotations.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS, storageClass.getMetadata().getName());
                                K8SClientResponse updateRes = pvcService.updatePvcByName(pvc, cluster);
                                if(!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())){
                                    LOGGER.error("更新pvc失败, pvc: {}, res: {}", name, updateRes.getBody());
                                    messages.add("更新pvc失败, pvc: " + name);
                                }
                            }
                        }
                    }else{
                        //pv未绑定过pvc
                        LOGGER.info("删除pv: {}", name);
                        if(execute) {
                            ActionReturnUtil result = pvService.delPvByName(name, cluster);
                            if(!result.isSuccess()){
                                LOGGER.error("删除pv失败, pv: {}, res: {}", name, JSONObject.toJSONString(result));
                                messages.add("删除pv失败, pv: " + name);
                            }
                        }
                    }
                }
            }
        }
        return messages;
    }

    private List<String> migrateServiceEntry(boolean execute){
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        Map<String, Object> labels = new HashMap<>();
        labels.put("harmonycloud.cn/ingress", "true");
        List<String> messages = new ArrayList<>();

        //tcp
        for (Cluster cluster: clusters){
            K8SURL tcpUrl = new K8SURL();
            tcpUrl.setResource(Resource.CONFIGMAP).setNamespace("kube-system");
            K8SClientResponse tcpRes = new K8sMachineClient().exec(tcpUrl, HTTPMethod.GET, headers, null, cluster);
            if(!HttpStatusUtil.isSuccessStatus(tcpRes.getStatus())){
                LOGGER.warn("[clusterId: " +cluster.getId() + "]get configmapList failed");
                messages.add("[clusterId: " +cluster.getId() + "]get configmapList failed");
                continue;
            }
            ConfigMapList tcpCmList = JsonUtil.jsonToPojo(tcpRes.getBody(), ConfigMapList.class);
            LinkedHashMap<String, String > tcpMap = new LinkedHashMap<>();
            for (ConfigMap cm : tcpCmList.getItems()){
                if((cm.getMetadata().getName().startsWith("tcp-ingress-controller-") || cm.getMetadata().getName().equals("system-expose-nginx-config-tcp"))
                        && Objects.nonNull(cm.getData())){
                    tcpMap.putAll((LinkedHashMap)cm.getData());
                }
            }
            if(CollectionUtils.isEmpty(tcpMap)){
                LOGGER.info("[clusterId: " +cluster.getId() + "]该集群无tcp类型对外服务");
                continue;
            }else {
                for (Map.Entry entry : tcpMap.entrySet()){
                    String tmp = (String)entry.getValue();
                    String namespace = null;
                    String deployName = null;
                    try {
                        namespace = tmp.substring(0, tmp.lastIndexOf("/"));
                        deployName = tmp.substring(tmp.lastIndexOf("/")+1, tmp.lastIndexOf(":"));
                        if(execute){
                            deploymentsService.updateLabels(namespace, deployName, cluster, labels);
                        }
                        LOGGER.info("update label > [clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> true (tcp)");
                    }catch (Exception e){
                        LOGGER.info("[clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (tcp)");
                        messages.add("[clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (tcp)");
                    }
                }
            }
        }

        //udp
        for (Cluster cluster: clusters){
            K8SURL udpUrl = new K8SURL();
            udpUrl.setResource(Resource.CONFIGMAP).setNamespace("kube-system");
            K8SClientResponse udpRes = new K8sMachineClient().exec(udpUrl, HTTPMethod.GET, headers, null, cluster);
            if(!HttpStatusUtil.isSuccessStatus(udpRes.getStatus())){
                LOGGER.warn("[clusterId: " +cluster.getId() + "]get configmapList failed");
                messages.add("[clusterId: " +cluster.getId() + "]get configmapList failed");
                continue;
            }
            ConfigMapList udpCmList = JsonUtil.jsonToPojo(udpRes.getBody(), ConfigMapList.class);
            LinkedHashMap<String, String > udpMap = new LinkedHashMap<>();
            for (ConfigMap cm : udpCmList.getItems()){
                if((cm.getMetadata().getName().startsWith("udp-ingress-controller-") || cm.getMetadata().getName().equals("system-expose-nginx-config-udp"))
                        && Objects.nonNull(cm.getData())){
                    udpMap.putAll((LinkedHashMap)cm.getData());
                }
            }
            if(CollectionUtils.isEmpty(udpMap)){
                LOGGER.info("[clusterId: " +cluster.getId() + "]该集群无udp类型对外服务");
                continue;
            }else {
                for (Map.Entry entry : udpMap.entrySet()){
                    String tmp = (String)entry.getValue();
                    String namespace = null;
                    String deployName = null;
                    try {
                        namespace = tmp.substring(0, tmp.lastIndexOf("/"));
                        deployName = tmp.substring(tmp.lastIndexOf("/")+1, tmp.lastIndexOf(":"));
                        if(execute){
                            deploymentsService.updateLabels(namespace, deployName, cluster, labels);
                        }
                        LOGGER.info("update label > [clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> true (udp)");
                    }catch (Exception e){
                        LOGGER.info("[clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (udp)");
                        messages.add("[clusterId: " +cluster.getId() + "]" + namespace + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (udp)");
                    }
                }
            }

        }

        //http
        for (Cluster cluster: clusters){
            K8SURL ingUrl = new K8SURL();
            ingUrl.setResource(Resource.INGRESS);
            K8SClientResponse ingRes = new K8sMachineClient().exec(ingUrl, HTTPMethod.GET, headers, null, cluster);
            if(!HttpStatusUtil.isSuccessStatus(ingRes.getStatus())){
                LOGGER.warn("[clusterId: " +cluster.getId() + "]get ingress list failed");
                messages.add("[clusterId: " +cluster.getId() + "]get ingress list failed");
                continue;
            }
            IngressList ingList = JsonUtil.jsonToPojo(ingRes.getBody(), IngressList.class);
            if(CollectionUtils.isEmpty(ingList.getItems())){
                LOGGER.info("[clusterId: " +cluster.getId() + "]该集群无http类型对外服务");
                continue;
            }else{
                for (Ingress ing : ingList.getItems()){
                    if(ing.getMetadata().getNamespace().equals("kube-system")
                            || StringUtils.isEmpty(ing.getMetadata().getNamespace())
                            || CollectionUtils.isEmpty(ing.getMetadata().getLabels())){
                        continue;
                    }
                    if(ing.getMetadata().getLabels().containsKey("app")){
                        String deployName = null;
                        try{
                            deployName = (String )ing.getMetadata().getLabels().get("app");
                            if(execute){
                                deploymentsService.updateLabels(ing.getMetadata().getNamespace(), deployName, cluster, labels);
                            }
                            LOGGER.info("update label > [clusterId: " +cluster.getId() + "]" + ing.getMetadata().getNamespace() + "/" + deployName + ": harmonycloud.cn/ingress ==> true (http)" );
                        }catch (Exception e){
                            LOGGER.info("[clusterId: " +cluster.getId() + "]" + ing.getMetadata().getNamespace() + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (http)" );
                            messages.add("[clusterId: " +cluster.getId() + "]" + ing.getMetadata().getNamespace() + "/" + deployName + ": harmonycloud.cn/ingress ==> label update failed (http)" );
                        }
                    }
                }
            }
        }
        return messages;
    }


    private List<String> migrateAutoscale(boolean execute) throws Exception{
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> labels = new HashMap<>();
        labels.put("harmonycloud.cn/autoscale", "true");
        List<String> messages = new ArrayList<>();

        //cpa
        for (Cluster cluster: clusters){

            K8SURL url = new K8SURL();
            url.setResource(Resource.COMPLEXPODSCALER);
            K8SClientResponse cpaRes = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
            if(!HttpStatusUtil.isSuccessStatus(cpaRes.getStatus())){
                LOGGER.warn("[clusterId: " +cluster.getId() + "]get cpa list failed");
                messages.add("[clusterId: " +cluster.getId() + "]get cpa list failed");
                continue;
            }
            ComplexPodScaleList complexPodScaleList = JsonUtil.jsonToPojo(cpaRes.getBody(), ComplexPodScaleList.class);
            if(CollectionUtils.isEmpty(complexPodScaleList.getItems())){
                LOGGER.info("[clusterId: " +cluster.getId() + "]该集群无cpa类型自动伸缩");
                continue;
            }else {
                for (ComplexPodScale cps : complexPodScaleList.getItems()){
                    try {
                        if(execute){
                            deploymentsService.updateLabels(cps.getMetadata().getNamespace(), cps.getSpec().getScaleTargetRef().getName(), cluster, labels);
                        }
                        LOGGER.info("update label > [clusterId: " +cluster.getId() + "]" + cps.getMetadata().getNamespace() + "/" + cps.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> true (cpa)" );
                    }catch(Exception e){
                        LOGGER.info("[clusterId: " +cluster.getId() + "]" + cps.getMetadata().getNamespace() + "/" + cps.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> label update failed (cpa)" );
                        messages.add("[clusterId: " +cluster.getId() + "]" + cps.getMetadata().getNamespace() + "/" + cps.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> label update failed (cpa)" );
                    }
                }
            }
        }

        //hpa
        for (Cluster cluster: clusters){

            K8SURL url = new K8SURL();
            url.setResource(Resource.HORIZONTALPODAUTOSCALER);
            K8SClientResponse hpaRes = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
            if(!HttpStatusUtil.isSuccessStatus(hpaRes.getStatus())){
                LOGGER.warn("[clusterId: " +cluster.getId() + "]get hpa list failed");
                messages.add("[clusterId: " +cluster.getId() + "]get hpa list failed");
                continue;
            }
            HorizontalPodAutoscalerList horizontalPodAutoscalerList = JsonUtil.jsonToPojo(hpaRes.getBody(), HorizontalPodAutoscalerList.class);
            if(CollectionUtils.isEmpty(horizontalPodAutoscalerList.getItems())){
                LOGGER.info("[clusterId: " +cluster.getId() + "]该集群无hpa类型自动伸缩");
                continue;
            }else {
                for (HorizontalPodAutoscaler hpa : horizontalPodAutoscalerList.getItems()){
                    try {
                        if(execute){
                            deploymentsService.updateLabels(hpa.getMetadata().getNamespace(), hpa.getSpec().getScaleTargetRef().getName(), cluster, labels);
                        }
                        LOGGER.info("update label > [clusterId: " +cluster.getId() + "]" + hpa.getMetadata().getNamespace() + "/" + hpa.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> true (hpa)" );
                    }catch (Exception e){
                        LOGGER.info("[clusterId: " +cluster.getId() + "]" + hpa.getMetadata().getNamespace() + "/" + hpa.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> label update failed (hpa)" );
                        messages.add("[clusterId: " +cluster.getId() + "]" + hpa.getMetadata().getNamespace() + "/" + hpa.getSpec().getScaleTargetRef().getName() + ": harmonycloud.cn/autoscale ==> label update failed (hpa)" );
                    }
                }
            }
        }
        return messages;
    }

    private List<String> migrateDependence(boolean execute) throws Exception {
        List<String> messages = new ArrayList<>();
        Cluster topCluster = clusterService.getPlatformCluster();
        List<StorageClass> storageClassList = scService.litStorageClassByClusterId(topCluster);
        if(CollectionUtils.isEmpty(storageClassList)){
            LOGGER.error("storageClass不存在, clusterId: {}", topCluster.getId());
            messages.add("storageClass不存在, clusterId: " + topCluster.getId());
        }else{
            StorageClass storageClass = storageClassList.get(0);
            K8SClientResponse pvRes = pvService.listPv(topCluster);
            if(!HttpStatusUtil.isSuccessStatus(pvRes.getStatus())){
                LOGGER.error("获取pv失败, clusterId: {}", topCluster.getId());
                messages.add("获取pv失败, clusterId: " + topCluster.getId());
            }else{
                PersistentVolumeList pvList = JsonUtil.jsonToPojo(pvRes.getBody(), PersistentVolumeList.class);
                for(PersistentVolume pv : pvList.getItems()){
                    Map<String, Object> labels = pv.getMetadata().getLabels();
                    if(labels != null && labels.containsKey("common")){
                        String name = pv.getMetadata().getName();
                        LOGGER.info("依赖: {}, status: {}", name, pv.getStatus().getPhase());
                        PersistentVolumeClaim pvc = pvcService.getPVCByNameAndNamespace(name, CommonConstant.CICD_NAMESPACE, topCluster);
                        if(pvc == null){
                            LOGGER.info("更新pv: {}", name);
                            if(execute) {
                                pv.getSpec().setClaimRef(null);
                                K8SClientResponse updateRes = pvService.updatePvByName(pv, topCluster);
                                if (!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                                    LOGGER.error("更新pv失败, pv: {}, res: {}", name, updateRes.getBody());
                                    messages.add("更新pv失败, pv: " + name);
                                }
                            }
                            LOGGER.info("创建pvc: {}", name);
                            if(execute) {
                                pvc = new PersistentVolumeClaim();
                                ObjectMeta metadata = new ObjectMeta();
                                metadata.setName(name);
                                metadata.setNamespace(CommonConstant.CICD);
                                metadata.setLabels(labels);
                                Map<String, Object> annotations = new HashMap<>();
                                annotations.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS, storageClass.getMetadata().getName());
                                metadata.setAnnotations(annotations);
                                pvc.setMetadata(metadata);
                                PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
                                spec.setVolumeName(name);
                                ResourceRequirements resourceRequirements = new ResourceRequirements();
                                Map<String, String> requests = new HashMap<>();
                                Map<String, Object> capacity = (Map<String, Object>) pv.getSpec().getCapacity();
                                requests.put(CommonConstant.STORAGE, (String) capacity.get(CommonConstant.STORAGE));
                                resourceRequirements.setRequests(requests);
                                spec.setResources(resourceRequirements);
                                spec.setAccessModes(pv.getSpec().getAccessModes());
                                pvc.setSpec(spec);
                                try {
                                    K8SClientResponse createRes = pvcService.createPvc(CommonConstant.CICD_NAMESPACE, pvc, topCluster);
                                    if (!HttpStatusUtil.isSuccessStatus(createRes.getStatus())) {
                                        throw new Exception(createRes.getBody());
                                    }
                                }catch(Exception e){
                                    LOGGER.error("创建pvc失败, pvc: {}, res: {}", name, e.getMessage());
                                    messages.add("创建pvc失败, pvc: " + name);
                                }
                            }
                        }else{
                            LOGGER.info("更新pvc: {}", name);
                            if(execute) {
                                pvc.getMetadata().setLabels(labels);
                                //已存在的pvc加上storageclass的注释
                                Map<String, Object> annotations = pvc.getMetadata().getAnnotations();
                                if (annotations == null) {
                                    annotations = new HashMap<>();
                                }
                                annotations.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS, storageClass.getMetadata().getName());
                                K8SClientResponse updateRes = pvcService.updatePvcByName(pvc, topCluster);
                                if(!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                                    LOGGER.error("更新pvc失败, pvc: {}, res: {}", name, updateRes.getBody());
                                    messages.add("更新pvc失败, pvc: " + name);
                                }
                            }
                        }
                    }
                }
            }
        }
        return messages;
    }
}
