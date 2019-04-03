package com.harmonycloud.service.migrate.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.application.ServiceTemplatesMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.ci.StageMapper;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.dto.application.CreateContainerDto;
import com.harmonycloud.dto.application.DeploymentDetailDto;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.scale.ComplexPodScale;
import com.harmonycloud.k8s.bean.scale.ComplexPodScaleList;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.PodService;
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
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.ho.yaml.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
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
    @Autowired
    private ServiceTemplatesMapper serviceTemplatesMapper;
    @Autowired
    private StageService stageService;
    @Autowired
    private StageMapper stageMapper;
    @Autowired
    private JobService jobService;
    @Autowired
    private PodService podService;

    /**
     * 版本升级数据迁移
     * @param version
     * @return 错误信息列表
     */
    @Override
    public List<String> migrateData(String version, boolean execute) throws Exception{
        clusters = clusterService.listCluster();
        List<String> messages = new ArrayList<>();
        LOGGER.info("-------------------------开始数据迁移-------------------------");
        messages.addAll(this.migrateConfigMap(execute));
        messages.addAll(this.migrateStorage(execute));
        messages.addAll(this.migrateServiceEntry(execute));
        messages.addAll(this.migrateAutoscale(execute));
        messages.addAll(this.migrateDependence(execute));
        messages.addAll(this.migrateLogCollecConfigForDeployment(execute));
        messages.addAll(this.migrateLogCollecConfigForDaemonset(execute));
        LOGGER.info("-------------------------结束数据迁移-------------------------");
        return messages;
    }

    /**
     * 停止挂载存储的服务
     * @param execute
     * @return
     * @throws Exception
     */
    @Override
    public List<String> stopService(boolean execute)  throws Exception{
        List<Cluster> clusters = clusterService.listCluster();
        List<String> messages = new ArrayList<>();
        StringBuilder sb= new StringBuilder();

        for(Cluster cluster : clusters){
            List<NamespaceLocal> namespaceList = namespaceLocalService.getNamespaceListByClusterId(cluster.getId());
            if(!CollectionUtils.isEmpty(namespaceList)){
                //遍历集群下分区
                for(NamespaceLocal namespaceLocal : namespaceList) {
                    DeploymentList deploymentList = deploymentsService.listDeployments(namespaceLocal.getNamespaceName(), null);
                    if(deploymentList != null && !CollectionUtils.isEmpty(deploymentList.getItems())){
                        //遍历分区下服务
                        for(Deployment deployment : deploymentList.getItems()){
                            Map<String, Object> annotations = deployment.getMetadata().getAnnotations();
                            //判断运行状态
                            if(annotations != null && Constant.STARTING.equals(annotations.get("nephele/status"))){
                                if(deployment.getSpec().getTemplate().getSpec().getVolumes() != null){
                                //遍历服务下存储
                                    for(Volume volume : deployment.getSpec().getTemplate().getSpec().getVolumes()){
                                        //挂载pvc
                                        if(volume.getPersistentVolumeClaim() != null){
                                            LOGGER.info("停止服务, name:{}, namespace:{}", deployment.getMetadata().getName(), deployment.getMetadata().getNamespace());
                                            try {
                                                if(execute) {
                                                    ActionReturnUtil stopRes = deploymentsService.stopDeployments(deployment.getMetadata().getName(), deployment.getMetadata().getNamespace(), null);
                                                    if (!stopRes.isSuccess()) {
                                                        LOGGER.error("服务停止失败, name:{}, namespace:{}, clusterId:{}", deployment.getMetadata().getName(), deployment.getMetadata().getNamespace(), cluster.getId());
                                                        messages.add("服务停止失败, name: +" + deployment.getMetadata().getName() + ", namespace:" + deployment.getMetadata().getNamespace() + ", clusterId:" + cluster.getId());
                                                    } else {
                                                        sb.append(cluster.getId() + "|" + deployment.getMetadata().getNamespace() + "|" + deployment.getMetadata().getName());
                                                        sb.append("\r\n");
                                                    }
                                                }
                                            }catch(Exception e){
                                                LOGGER.error("服务停止失败, name:{}, namespace:{}, clusterId:{}", deployment.getMetadata().getName(), deployment.getMetadata().getNamespace(), cluster.getId());
                                                messages.add("服务停止失败, name: +" + deployment.getMetadata().getName() + ", namespace:" + deployment.getMetadata().getNamespace() + ", clusterId:" + cluster.getId());
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        //服务列表写入文件/tmp/service
        FileOutputStream os = null;
        try {
            File dir = new File("/tmp");
            if(!dir.exists()){
                dir.mkdir();
            }
            File file = new File("/tmp/service");
            if (!file.exists()) {
                file.createNewFile();
            }
            os = new FileOutputStream(file);
            os.write(sb.toString().getBytes());
            os.flush();
        }catch(Exception e){
            LOGGER.error("写服务列表文件失败, {}", e);
            messages.add("写服务列表文件失败");
        }finally {
            if(os != null){
                os.close();
            }
        }
        return messages;
    }

    /**
     * 启动挂载存储的服务
     * @param execute
     * @return
     * @throws Exception
     */
    @Override
    public List<String> startService(boolean execute) throws Exception{
        clusters = clusterService.listCluster();
        List<String> messages = new ArrayList<>();
        File file = new File("/tmp/service");
        if(!file.exists()){
            LOGGER.error("服务列表文件不存在");
            messages.add("服务列表文件不存在");
        }else{
            FileInputStream fileInputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                fileInputStream = new FileInputStream(file);
                inputStreamReader = new InputStreamReader(fileInputStream);
                bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (StringUtils.isNotBlank(line)) {
                        String[] stringArray = line.split("\\|");
                        if (stringArray.length == 3) {
                            LOGGER.info("启动服务, {}", line);
                            if (execute) {
                                try {
                                    ActionReturnUtil startRes = deploymentsService.startDeployments(stringArray[2], stringArray[1], null);
                                    if (!startRes.isSuccess()) {
                                        LOGGER.error("服务启动失败, name:{}, namespace:{}, clusterId:{}", stringArray[2], stringArray[1], stringArray[0]);
                                        messages.add("服务启动失败, name: +" + stringArray[2] + ", namespace:" + stringArray[1] + ", clusterId:" + stringArray[0]);
                                    }
                                } catch (Exception e) {
                                    LOGGER.error("服务启动失败, name:{}, namespace:{}, clusterId:{}", stringArray[2], stringArray[1], stringArray[0]);
                                    messages.add("服务启动失败, name: +" + stringArray[2] + ", namespace:" + stringArray[1] + ", clusterId:" + stringArray[0]);
                                }
                            }
                        }else{
                            LOGGER.error("服务启动失败,{}", line);
                            messages.add("服务启动失败, " + line);
                        }
                    }
                }
            }catch(Exception e){
                LOGGER.info("读文件失败, {}", e);
                messages.add("读文件失败");
            }finally {
                if(fileInputStream != null){
                    fileInputStream.close();
                }
                if(inputStreamReader != null){
                    inputStreamReader.close();
                }
                if(bufferedReader != null){
                    bufferedReader.close();
                }
            }
        }
        return messages;
    }

    private List<String> migrateConfigMap(boolean execute) throws Exception{
        List<String> messages = new ArrayList<>();
        Map<String,List<String>> configGroups = new HashMap<>();
        for(Cluster cluster: clusters){
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
                        } else if(configFile.getConfigFileItemList().size() >1){
                            //创建配置组
                            String configNames = "";
                            for (String key : configFileGroupKey) {
                                configNames += key + " ";
                            }
                            LOGGER.info("创建配置组，配置组名称:{},configNames：{}", configFile.getName(),configNames);
                            if(execute) {
                                ActionReturnUtil res = configCenterService.saveConfig(configFile);
                                if (!res.isSuccess()) {
                                    LOGGER.error("保存配置组失败，configFile：{},res:{}", JSONObject.toJSONString(configFile), JSONObject.toJSONString(res));
                                    messages.add("保存配置组失败,configFileName:" + configFile.getName() + ", message:"+res.getData().toString());
                                }
                            }
                        }
                        this.migrateConfig(configFile, execute, exist, dep, namespaceLocal, container, messages, cluster);
                    }
                }
            }
        }
        //查看服务模板使用的配置文件
        checkTemplateConfigMap();
        migrateCicdConfigMap(configGroups, messages, execute);
        return messages;
    }

    private void checkTemplateConfigMap(){
        List<ServiceTemplates> serviceTemplates = serviceTemplatesMapper.listNameByProjectId(null, null, false, null, null);
        for(ServiceTemplates templates : serviceTemplates){
            try {
                String content = JSONArray.fromObject(templates.getDeploymentContent()).getJSONObject(0).toString().replaceAll(":\"\",", ":" + null + ",").replaceAll(":\"\"", ":" + null + "");
                DeploymentDetailDto deployment = JsonUtil.jsonToPojo(content, DeploymentDetailDto.class);
                List<CreateContainerDto> containerDtos = deployment.getContainers();
                for (CreateContainerDto containerDto : containerDtos) {
                    if (CollectionUtils.isEmpty(containerDto.getConfigmap())) {
                        continue;
                    }
                    List<CreateConfigMapDto> configMapDtos = containerDto.getConfigmap();
                    String configNames = "";
                    for (CreateConfigMapDto configMapDto : configMapDtos) {
                        configNames += configMapDto.getFile() + "(" + configMapDto.getTag() + ") ";
                    }
                    LOGGER.info("服务模板id:{}, 模板名称：{}，租户：{},容器:{}, 使用配置文件:{}",
                            templates.getId(), templates.getName(), templates.getTenant(), containerDto.getName(), configNames);
                }
            } catch (Exception e){
                LOGGER.error("查询模板使用的配置文件失败：serviceTemplateId:{},message:{}", templates.getId(), e.getMessage());
            }
        }
    }

    /**
     * 更新cicd使用的配置文件
     * @param configGroups
     * @param messages
     * @param execute
     */
    private void migrateCicdConfigMap(Map<String,List<String>> configGroups, List<String> messages, boolean execute) {
        Stage stageQuery = new Stage();
        try {
            Map<Integer, com.harmonycloud.dao.ci.bean.Job> jobMap = new HashMap<>();
            List<Stage> stageList = stageService.selectByExample(stageQuery);
            Map<Integer, List<CreateConfigMapDto>> oldStageConfig = new HashMap<>();
            for (Stage stage : stageList) {
                try {
                    com.harmonycloud.dao.ci.bean.Job job = jobMap.get(stage.getJobId());
                    if (job == null) {
                        job = jobService.getJobById(stage.getJobId());
                        jobMap.put(job.getId(), job);
                    }

                    if (StringUtils.isBlank(stage.getConfiguration()) || stage.getConfiguration().length() < "file".length()
                            || stage.getConfiguration().contains("configMapId")){
                        continue;
                    }
                    List<CreateConfigMapDto> configMapDtos = JSONObject.parseArray(stage.getConfiguration(), CreateConfigMapDto.class);
                    oldStageConfig.put(stage.getId(), configMapDtos);
                    String configNames = "";
                    for (CreateConfigMapDto configMapDto : configMapDtos) {
                        configNames += configMapDto.getFile() + "(" + configMapDto.getTag() + ") ";
                    }
                    LOGGER.info("job:{},使用配置文件:{}",
                            "id=" + job.getId() + ",name=" + job.getName() + ",tenant=" + job.getTenantId()
                                    + ",project=" + job.getProjectId()+ ",service=" + stage.getServiceName(), configNames);
                    List<CreateConfigMapDto> updateConfigMap = this.getUpdatedCicdConfigFile(configGroups, configMapDtos,
                            job.getClusterId(), job.getProjectId(), messages, job, stage, execute);
                    if (CollectionUtils.isEmpty(updateConfigMap)){
                        continue;
                    }
                    stage.setConfiguration(JSONObject.toJSONString(updateConfigMap));
                    if (execute) {
                        LOGGER.info("job:{}, 更新配置文件:{}",
                                "id=" + job.getId() + ",name=" + job.getName() + ",tenant=" + job.getTenantId()
                                        + ",project=" + job.getProjectId()+ ",service=" + stage.getServiceName(), stage.getConfiguration());
                        stageMapper.updateStage(stage);
                    }
                } catch (Exception e) {
                    LOGGER.error("更新流水线使用的配置文件失败：stageId:{},message:{}", stage.getId(), e);
                }
            }

            //更新之后检查文件名和路径是否和更新前的一样
            if (execute) {
                List<Stage> newStageList = stageService.selectByExample(stageQuery);
                for (Stage stage : newStageList) {
                    if (StringUtils.isBlank(stage.getConfiguration()) || stage.getConfiguration().length() < "file".length()) {
                        continue;
                    }
                    if (!stage.getConfiguration().contains("configMapId")) {
                        LOGGER.error("stage{}未更新流水线使用的配置文件：", stage.getId());
                    }
                    List<CreateConfigMapDto> configMapDtos = JSONObject.parseArray(stage.getConfiguration(), CreateConfigMapDto.class);
                    List<CreateConfigMapDto> oldConfigs = oldStageConfig.get(stage.getId());
                    if (configMapDtos.size() != oldConfigs.size()) {
                        LOGGER.error("数据迁移之后stageId:{}配置文件数量与更新之前不一致,", stage.getId());
                    }
                    List<String> paths = oldConfigs.stream().map(CreateConfigMapDto::getPath).collect(Collectors.toList());
                    for (CreateConfigMapDto configMapDto : configMapDtos) {
                        if (!paths.contains(configMapDto.getPath() + configMapDto.getFile())) {
                            LOGGER.error("数据迁移之后stageId:{}配置文件路径与更新之前不一致,", stage.getId());
                        }
                    }
                }
            }
        }catch (Exception e){
            LOGGER.error("更新流水线使用的配置文件失败", e);
        }
    }

    private  List<CreateConfigMapDto> getUpdatedCicdConfigFile(Map<String,List<String>> configGroups, List<CreateConfigMapDto> configMapDtos,
                                                               String clusterId, String projectId, List<String> messages, com.harmonycloud.dao.ci.bean.Job job, Stage stage, boolean execute) throws Exception{
        List<CreateConfigMapDto> configs = new ArrayList<>();
        List<String> paths = configMapDtos.stream().map(CreateConfigMapDto::getPath).collect(Collectors.toList());
        if(configMapDtos.size() == 1){
            CreateConfigMapDto config = configMapDtos.get(0);
            ConfigFile configFile = configCenterService.getConfigByNameAndTag(config.getFile(), config.getTag(), projectId, clusterId);
            if (configFile == null || CollectionUtils.isEmpty(configFile.getConfigFileItemList())) {
                LOGGER.error("job{}配置文件未找到,name:{},tag:{},projectId:{},clusterId:{}",
                        job.getId(),config.getFile(),config.getTag(),projectId, clusterId);
                messages.add("job{}配置文件未找到, jobId:{"+job.getId()+"},clusterId:{"+clusterId +"},projectId:{"+projectId+"}"
                        +"name:{"+config.getFile()+"},tag:{"+config.getTag()+"}");
                return Collections.emptyList();
            }
            if (configFile.getConfigFileItemList().size() != 1){
                LOGGER.error("配置文件数量不一致,name:{},tag:{},projectId:{},clusterId:{}",
                        config.getFile(),config.getTag(),projectId, clusterId);
                messages.add("配置文件数量不一致, clusterId:{"+clusterId +"},projectId:{"+projectId+"}"
                        +"name:{"+config.getFile()+"},tag:{"+config.getTag()+"}");
                return Collections.emptyList();
            }
            ConfigFileItem item = configFile.getConfigFileItemList().get(0);
            if (!config.getPath().equals(item.getPath()+item.getFileName())){
                LOGGER.error("job{}配置文件表与流水线的路径不一致,,name:{},tag:{},projectId:{},clusterId:{}",
                        job.getId(), config.getFile(),config.getTag(),projectId, clusterId);
                return Collections.emptyList();
            }
            CreateConfigMapDto configMapDto = new CreateConfigMapDto();
            configMapDto.setAppStore(false);
            configMapDto.setTag(config.getTag());
            configMapDto.setConfigMapId(configFile.getId());
            configMapDto.setFile(configFile.getConfigFileItemList().get(0).getFileName());
            configMapDto.setPath(configFile.getConfigFileItemList().get(0).getPath());
            configMapDto.setName(configFile.getName());
            configs.add(configMapDto);
        } else {
            boolean exit = false;
            for (String key : configGroups.keySet()) {
                List<String> existConfigFileGroupKey = configGroups.get(key);
                if (existConfigFileGroupKey.size() != configMapDtos.size()) {
                    continue;
                }
                boolean contains = true;
                for (CreateConfigMapDto config : configMapDtos) {
                    String configKey = clusterId+"-"+projectId+"-"+config.getFile()+"-"+config.getTag();
                    if (!existConfigFileGroupKey.contains(configKey)) {
                        contains = false;
                        break;
                    }
                }
                //存在相同的配置组
                if (contains) {
                    if (execute) {
                        ConfigDetailDto configDetailDto = configCenterService.getConfigMap(key);
                        if (configDetailDto == null) {
                            LOGGER.error("配置文件未找到,cofnigmapId:{},projectId:{},clusterId:{}",
                                    key, projectId, clusterId);
                            messages.add("配置文件未找到, clusterId:{" + clusterId + "},projectId:{" + projectId + "}"
                                    + "configMapId:{" + key + "}");
                            return Collections.emptyList();
                        }
                        for (ConfigFileItem configFileItem : configDetailDto.getConfigFileItemList()) {
                            if (!paths.contains(configFileItem.getPath() + configFileItem.getFileName())) {
                                LOGGER.error("job{}配置文件表与流水线的路径不一致,,name:{},configMapId:{},projectId:{},clusterId:{}",
                                        job.getId(), key, projectId, clusterId);
                                return Collections.emptyList();
                            }
                            CreateConfigMapDto configMapDto = new CreateConfigMapDto();
                            configMapDto.setAppStore(false);
                            configMapDto.setTag(configDetailDto.getTags());
                            configMapDto.setConfigMapId(configDetailDto.getId());
                            configMapDto.setName(configDetailDto.getName());
                            configMapDto.setFile(configFileItem.getFileName());
                            configMapDto.setPath(configFileItem.getPath());
                            configs.add(configMapDto);
                        }
                    }
                    LOGGER.info("流水线{}对应的配置组已存在，配置id：{}", job.getName(), key);
                    exit = true;
                    break;
                } else {
                    continue;
                }
            }
            if (!exit) {
                ConfigFile configFile = new ConfigFile();
                List<ConfigFileItem> configFileItems = new ArrayList<>();
                for (CreateConfigMapDto configMapDto : configMapDtos) {
                    ConfigFile config = configCenterService
                            .getConfigByNameAndTag(configMapDto.getFile(),configMapDto.getTag(),projectId, clusterId);
                    if (config == null || config.getConfigFileItemList().size() != 1) {
                        LOGGER.error("配置文件未找到或文件数量不匹配,name:{},tag:{},projectId:{},clusterId:{}",
                                configMapDto.getFile(), configMapDto.getTag(), projectId, clusterId);
                        messages.add("配置文件未找到或文件数量不匹配, clusterId:{" + clusterId + "},projectId:{" + projectId + "}"
                                + "name:{" + configMapDto.getFile() + "},tag:{" + configMapDto.getTag() + "}");
                    }
                    ConfigFileItem configFileItem = new ConfigFileItem();
                    configFileItem.setFileName(config.getConfigFileItemList().get(0).getFileName());
                    configFileItem.setPath(config.getConfigFileItemList().get(0).getPath());
                    configFileItem.setContent(config.getConfigFileItemList().get(0).getContent());
                    configFileItems.add(configFileItem);
                }
                String clusterName = clusterId.substring(clusterId.lastIndexOf("-")+1);
                configFile.setConfigFileItemList(configFileItems);
                configFile.setTenantId(job.getTenantId());
                configFile.setProjectId(job.getProjectId());
                configFile.setClusterId(clusterId);
                configFile.setTags(configMapDtos.get(0).getTag());
                configFile.setClusterName(clusterName);
                configFile.setName(stage.getServiceName());
                configFile.setRepoName(stage.getImageName());
                LOGGER.info("流水线创建配置组，name：{},projectId:{}",configFile.getName(), configFile.getProjectId());
                if (execute) {
                    ActionReturnUtil res = configCenterService.saveConfig(configFile);
                    if (!res.isSuccess()) {
                        LOGGER.error("保存配置组失败，configFile：{},res:{}", JSONObject.toJSONString(configFile), JSONObject.toJSONString(res));
                        messages.add("保存配置组失败,configFileName:" + configFile.getName() + ", message:"+res.getData().toString());
                        return configs;
                    }
                    ConfigFile savedConfig = configCenterService.getConfigByNameAndTag(configFile.getName(),
                            configFile.getTags(), configFile.getProjectId(), clusterId);
                    List<ConfigFileItem> items = savedConfig.getConfigFileItemList();
                    for (ConfigFileItem item : items) {
                        CreateConfigMapDto configMapDto = new CreateConfigMapDto();
                        configMapDto.setAppStore(false);
                        configMapDto.setTag(savedConfig.getTags());
                        configMapDto.setConfigMapId(savedConfig.getId());
                        configMapDto.setName(savedConfig.getName());
                        configMapDto.setFile(item.getFileName());
                        configMapDto.setPath(item.getPath());
                        configs.add(configMapDto);
                    }
                }
            }
        }
        return configs;
    }

    private ConfigFile getConfigFile(List<VolumeMountExt> configVolumeMount, Cluster cluster,
                                     NamespaceLocal namespaceLocal,Deployment dep, List<String> messages, List<String> configFileGroupKey){
        String projectId = (String) dep.getMetadata().getLabels().get("harmonycloud.cn/projectId");
        if(configVolumeMount.size()>1){
            LOGGER.info("检测到配置组，clusterId:{},namespace：{},deployment：{}",
                    cluster.getId(),namespaceLocal.getNamespaceName(),dep.getMetadata().getName());
        }
        ConfigFile configFile = null;
        String deployName = dep.getMetadata().getName();
        List<String> clusterNames = clusters.stream().map(Cluster::getName).collect(Collectors.toList());
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
                if (!CollectionUtils.isEmpty(configFile.getConfigFileItemList())) {
                    ConfigFileItem item = configFile.getConfigFileItemList().get(0);
                    String mountFilePath = volumeMountExt.getMountPath()+volumeMountExt.getSubPath();
                    String configFilePath = item.getPath()+item.getFileName();
                    if (!mountFilePath.equals(configFilePath)){
                        LOGGER.error("配置文件数据库与实际发布的服务的路径不一致,name:{},tag:{},projectId:{},clusterId:{}，deploy：{}",
                                name,tag,projectId, cluster.getId(), dep.getMetadata().getName());
                        LOGGER.info("mountPath:{}, config file path:{}",mountFilePath, configFilePath);
                    }
                }
                //如果服务名已经包括集群名称，则配置文件名称与服务名相同，否则为服务名+集群名
                configFile.setName(dep.getMetadata().getName() + "-" + cluster.getName());
                for (String clusterName : clusterNames) {
                    if (deployName.contains(clusterName)) {
                        configFile.setName(deployName);
                        break;
                    }
                }
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
                if (!CollectionUtils.isEmpty(config.getConfigFileItemList())) {
                    ConfigFileItem item = config.getConfigFileItemList().get(0);
                    String mountFilePath = volumeMountExt.getMountPath()+volumeMountExt.getSubPath();
                    String configFilePath = item.getPath()+item.getFileName();
                    if (!mountFilePath.equals(configFilePath)){
                        LOGGER.error("配置文件数据库与实际发布的服务的路径不一致,name:{},tag:{},projectId:{},clusterId:{}，deploy：{}",
                                name,tag,projectId, cluster.getId(), dep.getMetadata().getName());
                        LOGGER.info("mountPath:{}, config file path:{}",mountFilePath, configFilePath);
                    }
                }
                configFile.getConfigFileItemList().addAll(config.getConfigFileItemList());
                configFileGroupKey.add(cluster.getId()+"-"+projectId+"-"+name+"-"+tag);
            }

        }
        if (configFile != null && configFile.getConfigFileItemList().size() > 1) {
            configFile.setId(UUIDUtil.get16UUID());
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
                    existConfigMapId = key;
                    LOGGER.warn("不同服务使用相同的配置，deploy：{},exist:{}, config:{}",
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
                            LOGGER.info("删除重建pvc: {}, namespace: {}", name, namespace);

                            Map<String, Object> pvcLabels = pvc.getMetadata().getLabels();
                            String serviceName = null;
                            if(pvc.getMetadata().getLabels() != null && pvc.getMetadata().getLabels().get(CommonConstant.LABEL_KEY_APP) != null) {
                                serviceName = (String)pvc.getMetadata().getLabels().get(CommonConstant.LABEL_KEY_APP);
                            }
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

                            PersistentVolumeClaim newPvc = new PersistentVolumeClaim();
                            ObjectMeta metadata = new ObjectMeta();
                            metadata.setName(name);
                            metadata.setNamespace(namespace);
                            metadata.setLabels(pvcLabels);
                            newPvc.setMetadata(metadata);
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
                            newPvc.setSpec(spec);
                            String pvcYaml = getPvcYaml(newPvc);


                            if(execute) {
                                //查询pvc绑定的服务，存在pod则停止服务
                                if(StringUtils.isNotBlank(serviceName)){
                                    try {
                                        List<Pod> podList = this.getPodList(serviceName, namespace, cluster);
                                        if (!CollectionUtils.isEmpty(podList)) {
                                            ActionReturnUtil stopRes = deploymentsService.stopDeployments(serviceName, namespace, null);
                                            if (!stopRes.isSuccess()){
                                                LOGGER.error("停止服务失败, deployment: {}, res: {}", serviceName, stopRes.getData());
                                                messages.add("停止服务失败, deployment: " + serviceName);
                                                LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                                messages.add("pvc yaml: \n" + pvcYaml);
                                                continue;
                                            }else{
                                                int retry;
                                                for(retry=0; retry<3;retry++) {
                                                    Thread.sleep(15000);
                                                    podList = this.getPodList(serviceName, namespace, cluster);
                                                    if (CollectionUtils.isEmpty(podList)) {
                                                        break;
                                                    }
                                                }
                                            }
                                            podList = this.getPodList(serviceName, namespace, cluster);
                                            if (!CollectionUtils.isEmpty(podList)) {
                                                throw new Exception();
                                            }
                                        }
                                    }catch(Exception e){
                                        LOGGER.error("停止服务失败, deployment: {}, res: {}", serviceName, e.getMessage());
                                        messages.add("停止服务失败, deployment: " + serviceName);
                                        LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                        messages.add("pvc yaml: \n" + pvcYaml);
                                        continue;
                                    }

                                }

                                try {
                                    K8SClientResponse deleteRes = pvcService.deletePVC(namespace, name, cluster);
                                    if (!HttpStatusUtil.isSuccessStatus(deleteRes.getStatus())) {
                                        LOGGER.error("删除pvc失败, pvc: {}, res: {}", name, deleteRes.getBody());
                                        messages.add("删除pvc失败, pvc: " + name);
                                        LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                        messages.add("pvc yaml: \n" + pvcYaml);
                                        continue;
                                    }else{
                                        Thread.sleep(1000);
                                    }
                                }catch(Exception e){
                                    LOGGER.error("删除pvc失败, pvc: {}, res: {}", name, e.getMessage());
                                    messages.add("删除pvc失败, pvc: " + name);
                                    LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                    messages.add("pvc yaml: \n" + pvcYaml);
                                    continue;
                                }
                                try {
                                    K8SClientResponse createRes = pvcService.createPvc(namespace, newPvc, cluster);
                                    if (!HttpStatusUtil.isSuccessStatus(createRes.getStatus())) {
                                        LOGGER.error("创建pvc失败, pvc: {}, res: {}", name, createRes.getBody());
                                        messages.add("创建pvc失败, pvc: " + name);
                                        LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                        messages.add("pvc yaml: \n" + pvcYaml);
                                    }
                                }catch(Exception e){
                                    LOGGER.error("创建pvc失败, pvc: {}, res: {}", name, e.getMessage());
                                    messages.add("创建pvc失败, pvc: " + name);
                                    LOGGER.error("pvc yaml: \n"+ pvcYaml);
                                    messages.add("pvc yaml: \n" + pvcYaml);
                                }
                            }
                        }
                        LOGGER.info("更新pv: {}", name);
                        if(execute) {
                            PersistentVolume updatePv = pvService.getPvByName(name, cluster);
                            updatePv.getSpec().setClaimRef(null);
                            updatePv.getSpec().setStorageClassName(storageClass.getMetadata().getName());
                            K8SClientResponse updateRes = pvService.updatePvByName(updatePv, cluster);
                            if (!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                                LOGGER.error("更新pv失败, pv: {}, res: {}", name, updateRes.getBody());
                                messages.add("更新pv失败, pv: " + name);
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

    private List<Pod> getPodList(String serviceName, String namespace, Cluster cluster) throws Exception{
        Map<String, Object> bodys = new HashMap<>();
        bodys.put(CommonConstant.LABELSELECTOR, "app=" + serviceName);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            LOGGER.error("获取pod失败, deployment: {}, res: {}", serviceName, podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        if(podList != null){
            return podList.getItems();
        }else{
            return null;
        }
    }

    private String getPvcYaml(PersistentVolumeClaim pvc){
        StringBuilder sb = new StringBuilder();
        sb.append("apiVersion: v1\r\nkind: PersistentVolumeClaim\r\n");
        String pvcContent = Yaml.dump(pvc);
        String yaml= pvcContent.replaceAll("---.+\r\n", "").replaceAll("!.+\r\n", "\r\n");
        sb.append(yaml);
        return sb.toString();
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

    private List<String> migrateLogCollecConfigForDeployment(boolean execute){
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        List<String> messages = new ArrayList<>();

        for (Cluster cluster : clusters){
            K8SURL url = new K8SURL();
            url.setResource(Resource.DEPLOYMENT);
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())){
                LOGGER.error("[clusterId: " + cluster.getId() + "], get deployment list failed");
                messages.add("[clusterId: " + cluster.getId() + "], get deployment list failed");
                continue;
            }
            DeploymentList depList = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
            for (Deployment dep : depList.getItems()){
                boolean flag = false;
                List<String> logpath = new ArrayList<>();
                for (Container container : dep.getSpec().getTemplate().getSpec().getContainers()){
                    flag = dealContainerLog(container, logpath, Constant.DEPLOYMENT, dep.getMetadata().getName());
                }
                if (flag){
                    LOGGER.info("clusterId:{}, namespace:{}, 服务:{}开启日志收集, 日志目录:{}", cluster.getId(), dep.getMetadata().getNamespace(),dep.getMetadata().getName(), logpath.toString());
                }else {
                    continue;
                }
                if (execute){
                    try {
                        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
                        K8SURL k8SURL = new K8SURL();
                        k8SURL.setNamespace(dep.getMetadata().getNamespace()).setResource(Resource.DEPLOYMENT).setName(dep.getMetadata().getName());
                        K8SClientResponse updateRes = new K8sMachineClient().exec(k8SURL, HTTPMethod.PUT, headers, bodys, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                            LOGGER.error("开启服务日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + dep.getMetadata().getNamespace() + ", name: " + dep.getMetadata().getName() + "]");
                            messages.add("开启服务日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + dep.getMetadata().getNamespace() + ", name: " + dep.getMetadata().getName() + "]");
                        }
                    }catch (Exception e){
                        LOGGER.error("开启服务日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + dep.getMetadata().getNamespace() + ", name: " + dep.getMetadata().getName() + "]");
                        messages.add("开启服务日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + dep.getMetadata().getNamespace() + ", name: " + dep.getMetadata().getName() + "]");
                    }
                }
            }
        }
        return messages;
    }

    private List<String> migrateLogCollecConfigForDaemonset(boolean execute){
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        List<String> messages = new ArrayList<>();
        for (Cluster cluster : clusters){
            K8SURL url = new K8SURL();
            url.setResource(Resource.DAEMONTSET).setNamespace("kube-system");
            K8SClientResponse depRes = new K8sMachineClient().exec(url, HTTPMethod.GET, headers, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())){
                LOGGER.error("[clusterId: " + cluster.getId() + "], get daemonset list failed");
                messages.add("[clusterId: " + cluster.getId() + "], get daemonset list failed");
                continue;
            }
            DaemonSetList dsList = JsonUtil.jsonToPojo(depRes.getBody(), DaemonSetList.class);
            for (DaemonSet ds : dsList.getItems()){
                boolean flag = false;
                List<String> logpath = new ArrayList<>();
                for (Container container : ds.getSpec().getTemplate().getSpec().getContainers()){
                    flag = dealContainerLog(container, logpath, Constant.DAEMONSET, ds.getMetadata().getName());
                }
                if (flag){
                    LOGGER.info("clusterId:{}, namespace:{}, 守护进程:{}开启日志收集, 日志目录:{}", cluster.getId(), ds.getMetadata().getNamespace(), ds.getMetadata().getName(), logpath.toString());
                }else {
                    continue;
                }
                if (execute){
                    try {
                        Map<String, Object> bodys = CollectionUtil.transBean2Map(ds);
                        K8SURL k8SURL = new K8SURL();
                        k8SURL.setNamespace(ds.getMetadata().getNamespace()).setResource(Resource.DAEMONTSET).setName(ds.getMetadata().getName());
                        K8SClientResponse updateRes = new K8sMachineClient().exec(k8SURL, HTTPMethod.PUT, headers, bodys, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(updateRes.getStatus())) {
                            LOGGER.error("开启守护进程日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + ds.getMetadata().getNamespace() + ", name: " + ds.getMetadata().getName() + "]");
                            messages.add("开启守护进程日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + ds.getMetadata().getNamespace() + ", name: " + ds.getMetadata().getName() + "]");
                        }
                    }catch (Exception e){
                        LOGGER.error("开启守护进程日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + ds.getMetadata().getNamespace() + ", name: " + ds.getMetadata().getName() + "]");
                        messages.add("开启守护进程日志收集失败, [clusterId: " + cluster.getId() + ", namespace: " + ds.getMetadata().getNamespace() + ", name: " + ds.getMetadata().getName() + "]");
                    }
                }
            }
        }
        return messages;
    }


    private boolean dealContainerLog(Container container, List<String> logpath, String parentResourceType, String parentResourceName){
        EnvVar logEnv = new EnvVar();
        logEnv.setName(Constant.PILOT_LOG_PREFIX);
        EnvVar logEnvTag = new EnvVar();
        logEnvTag.setName(Constant.PILOT_LOG_PREFIX_TAG);
        if (CollectionUtils.isEmpty(container.getVolumeMounts())){
            return false;
        }
        for (VolumeMount volumeMount : container.getVolumeMounts()){
            if (volumeMount.getName().startsWith(Constant.VOLUME_LOGDIR_NAME)){
                logEnv.setValue(volumeMount.getMountPath() + "/*");
                logEnvTag.setValue("k8s_resource_type=" + parentResourceType + ",k8s_resource_name=" + parentResourceName);
                if (!CollectionUtils.isEmpty(container.getEnv())){
                    for (EnvVar env : container.getEnv()){
                        if (env.getName().equals(Constant.PILOT_LOG_PREFIX) || env.getName().equals(Constant.PILOT_LOG_PREFIX_TAG)) return false;
                    }
                    container.getEnv().add(logEnv);
                    container.getEnv().add(logEnvTag);
                    logpath.add(volumeMount.getMountPath());
                    return true;
                }else {
                    List<EnvVar> envList = new ArrayList<>();
                    envList.add(logEnv);
                    envList.add(logEnvTag);
                    container.setEnv(envList);
                    logpath.add(volumeMount.getMountPath());
                    return true;
                }
            }
        }
        return false;
    }
}
