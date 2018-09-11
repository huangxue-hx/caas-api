package com.harmonycloud.service.platform.serviceipml.configcenter;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ConfigFileItemMapper;
import com.harmonycloud.dao.application.ConfigFileMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.application.bean.ConfigService;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by gurongyun on 17/03/24. configcenter serviceImpl
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConfigCenterServiceImpl implements ConfigCenterService {

    DecimalFormat decimalFormat = new DecimalFormat("######0.0");

    @Autowired
    private ConfigFileMapper configFileMapper;
    @Autowired
    private ConfigFileItemMapper configFileItemMapper;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private UserService userService;

    @Autowired
    private ConfigmapService configmapService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private HttpSession session;



    /**
     * add or update config serviceImpl on 17/03/24.
     *
     * @param configDetail required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil saveConfig(ConfigDetailDto configDetail, String userName) throws Exception {
        Assert.notNull(configDetail);
        double tags = Constant.TEMPLATE_TAG;
        // 检查数据库有没有存在
        List<ConfigFile> list = configFileMapper.listConfigByName(configDetail.getName(), configDetail.getProjectId(), configDetail.getClusterId(), null);
        if (Objects.nonNull(configDetail.getIsCreate()) && Boolean.valueOf(configDetail.getIsCreate())) {
            if (!CollectionUtils.isEmpty(list)) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONFIGMAP_NAME_DUPLICATE);
            }
        }
        ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
        // 随机生成64位字符串
        configFile.setId(UUIDUtil.getUUID());
        configFile.setUser(userName);
        if (!CollectionUtils.isEmpty(list)) {
            // 存在版本号+0.1
            tags = Double.valueOf(list.get(0).getTags()) + Constant.TEMPLATE_TAG_INCREMENT;
        }
        configFile.setTags(decimalFormat.format(tags) + "");
        configFile.setClusterId(configFile.getClusterId());
        //查询数据库中是否有该配置组（若没有，则为创建配置;若有，则为保存修改的配置）
        if(list.isEmpty()){
            configFile.setCreateTime(DateUtil.timeFormat.format(new Date()));//创建配置时，添加创建时间字段
        }
        String updateTime = DateUtil.timeFormat.format(new Date());
        configFile.setUpdateTime(updateTime);
        //设置同配置组updateTime为同一值
        configFileMapper.updateUpdateTime(updateTime,configDetail.getName());

        if (!"".equals(configFile.getClusterId()) && configFile.getClusterId().length() > 0) {
            configFile.setClusterName(clusterService.findClusterById(configFile.getClusterId()).getName());
        }
        // 入库
        configFileMapper.saveConfigFile(configFile);
        //配置文件的明细
        List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
        if(configFileItemList != null && configFileItemList.size() >0){
            for (ConfigFileItem configFileItem : configFileItemList) {
                configFileItem.setConfigfileId(configFile.getId());
                configFileItemMapper.insert(configFileItem);
            }
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("filename", configDetail.getName());
        resultJson.put("tag", tags);
        return ActionReturnUtil.returnSuccessWithData(resultJson);
    }



    /**
     * update config serviceImpl on 17/03/24.
     *
     * @param configDetail required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil updateConfig(ConfigDetailDto configDetail, String userName) throws Exception {

        ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
        configFile.setUser(userName);
        String updateTime = DateUtil.timeFormat.format(new Date());
        configFile.setUpdateTime(updateTime);
        //设置同配置组updateTime为同一值
        configFileMapper.updateUpdateTime(updateTime,configDetail.getName());

        //根据configFile的id删除明细中对应的数据
        configFileItemMapper.deleteConfigFileItem(configFile.getId());
        // 入库
        configFileMapper.updateConfig(configFile);

        //配置文件的明细
        List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
        for (ConfigFileItem configFileItem : configFileItemList) {
            configFileItem.setConfigfileId(configFile.getId());
            configFileItemMapper.insert(configFileItem);
        }

        JSONObject resultJson = new JSONObject();
        resultJson.put("filename", configDetail.getName());
        return ActionReturnUtil.returnSuccessWithData(resultJson);
    }

    /**
     * delete config serviceImpl on 17/03/24.
     *
     * @param id        required
     * @param projectId required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public void deleteConfig(String id, String projectId) throws Exception {
        Assert.hasText(id);
        Assert.hasText(projectId);
        //删除配置信息
        configFileMapper.deleteConfig(id, projectId);

    }

    /**
     * find config lists for center serviceImpl on 17/03/24.
     *
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil searchConfig(String projectId, String clusterId, String repoName, String keyword) throws Exception {
        JSONArray array = new JSONArray();
        Set<String> clusterIds = null;
        Map<String, Cluster> userCluster = userService.getCurrentUserCluster();

        // 查询项目下所有的配置文件
        if (StringUtils.isBlank(clusterId)) {
            clusterIds = userCluster.keySet();
        } else {
            clusterIds = new HashSet<>();
            clusterIds.add(clusterId);
        }
        List<ConfigFile> list = configFileMapper.listConfigSearch(projectId, clusterIds, repoName, keyword);
        if (CollectionUtils.isEmpty(list)) {
            return ActionReturnUtil.returnSuccess();
        } else {

            Map<String, ConfigFile> configFileMap = getConfigFileMap(list);
            Collection<ConfigFile> values = configFileMap.values();
            for (ConfigFile value : values) {
                value.setClusterAliasName(userCluster.get(value.getClusterId()).getAliasName());
            }
            return ActionReturnUtil.returnSuccessWithData(values);
        }
    }

    /**
     * 获取不同配置中每个tag最新的配置存放在configFileMap
     *
     * @param list          根据条件获取的所有配置
     */
    public Map<String, ConfigFile> getConfigFileMap(List<ConfigFile> list) {
        Map<String, ConfigFile> configFileMap = new HashMap<>();
        for (ConfigFile configFile : list) {
            String key = configFile.getName() + configFile.getProjectId() + configFile.getClusterId();
            ConfigFile configFileValue = configFileMap.get(key);
            if (configFileValue == null) {
                configFileMap.put(key, configFile);
                continue;
            }
            if (Float.parseFloat(configFile.getTags()) > Float.parseFloat(configFileValue.getTags())) {
                configFileMap.put(key, configFile);
            }
        }
        return configFileMap;

    }

    /**
     * find config overview lists serviceImpl on 17/03/24.
     *
     * @param projectId required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil listConfig(String projectId, String repoName) throws Exception {
        JSONArray array = new JSONArray();
        Map<String, Cluster> userCluster = userService.getCurrentUserCluster();
        // 查询同一repo下的所有配置文件
        List<ConfigFile> lists = configFileMapper.listConfigOverview(projectId, repoName, userCluster.keySet());
        if (CollectionUtils.isEmpty(lists)) {
            return ActionReturnUtil.returnSuccess();
        } else {
            Map<String, ConfigFile> configFileMap = getConfigFileMap(lists);
            Collection<ConfigFile> configFiles = configFileMap.values();
            for (ConfigFile configFile : configFiles) {
                configFile.setClusterAliasName(userCluster.get(configFile.getClusterId()).getAliasName());
            }
            return ActionReturnUtil.returnSuccessWithData(configFiles);

        }
    }

    /**
     * find configMap serviceImpl on 17/03/24.
     *
     *
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil getConfigMap(String configMapId) throws Exception {

        ConfigFile configFile = configFileMapper.getConfig(configMapId);
        List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(configMapId);
        configFile.setConfigFileItemList(configFileItemList);
        ConfigDetailDto configDetailDto = ObjConverter.convert(configFile, ConfigDetailDto.class);

        List<Deployment> deploymentList = getServiceList(configDetailDto.getProjectId(), configDetailDto.getTenantId(), configMapId);
        configDetailDto.setDeploymentList(deploymentList);
        // 查找配置文件
        return ActionReturnUtil.returnSuccessWithData(configDetailDto);
    }

    /**
     * delete configs service on 17/03/24.
     *
     * @param name required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil deleteConfigMap(String name, String projectId, String clusterId) throws Exception {
        Assert.hasText(name);
        Assert.hasText(projectId);
        Assert.hasText(clusterId);
        configFileMapper.deleteConfigByName(name, projectId, clusterId);
        return ActionReturnUtil.returnSuccess();
    }

    /**
     * delete configs service on 17/03/24.
     *
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil deleteConfigByProject(String projectId) throws Exception {
        Assert.hasText(projectId);
        configFileMapper.deleteConfigByProject(projectId);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public int deleteByClusterId(String clusterId) {
        return configFileMapper.deleteByClusterId(clusterId);
    }

    /**
     * find a lastest config service on 17/03/24.
     * @param name required
     * @param projectId  required
     * @param repoName
     * @param clusterId  required
     * @param tags  required
     * @return ActionReturnUtil
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getLatestConfigMap(String name, String projectId, String repoName,String clusterId,String tags) throws Exception {

        ConfigFile latestConfig = configFileMapper.getLatestConfig(name, projectId, repoName,clusterId,tags);
        List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(latestConfig.getId());
        latestConfig.setConfigFileItemList(configFileItemList);
        ConfigDetailDto configDetailDto = ObjConverter.convert(latestConfig, ConfigDetailDto.class);

        List<Deployment> serviceList = getServiceList(configDetailDto.getProjectId(), configDetailDto.getTenantId(), configDetailDto.getId());
        configDetailDto.setDeploymentList(serviceList);

        Cluster cluster = clusterService.findClusterById(clusterId);
        String clusterName = cluster.getName();
        configDetailDto.setClusterName(clusterName);

        configDetailDto.setClusterAliasName(cluster.getAliasName());
        return ActionReturnUtil.returnSuccessWithData(configDetailDto);
    }

    @Override
    public ActionReturnUtil checkDuplicateName(String name, String projectId) throws Exception {
        Assert.hasText(name);
        Assert.hasText(projectId);

        // validate name
        List<ConfigFile> configFiles = configFileMapper.listConfigByName(name, projectId, null, null);
        if (CollectionUtils.isEmpty(configFiles)) {
            return ActionReturnUtil.returnSuccessWithData(false);
        } else {
            return ActionReturnUtil.returnSuccessWithData(true);
        }
    }

    public ActionReturnUtil getConfigMapByName(String namespace, String name) throws Exception {
        Assert.hasText(namespace);
        Assert.hasText(name);
        List<String> names = new ArrayList<>();
        if (name.contains(CommonConstant.COMMA)) {
            String[] n = name.split(CommonConstant.COMMA);
            names = Arrays.asList(n);
        } else {
            names.add(name);
        }
        List<ConfigMap> list = new ArrayList<>();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (names != null && names.size() > 0) {
            for (String n : names) {
                K8SClientResponse response = configmapService.doSepcifyConfigmap(namespace, n, cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                    return ActionReturnUtil.returnErrorWithMsg(response.getBody());
                }
                ConfigMap configMap = JsonUtil.jsonToPojo(response.getBody(), ConfigMap.class);
                list.add(configMap);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    @Override
    public ConfigFile getConfigByNameAndTag(String name,String tag, String projectId, String clusterId) {
        ConfigFile configFile = configFileMapper.getConfigByNameAndTag(name, tag, projectId, clusterId);
        if(configFile == null){
            return null;
        }
        List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(configFile.getId());
        configFile.setConfigFileItemList(configFileItemList);
        return configFile;
    }

    @Override
    public ActionReturnUtil getConfigMapByName(String name, String clusterId, String projectId) throws Exception  {
        List<ConfigFile> configFileList =  configFileMapper.getConfigMapByName(name,clusterId,projectId);
        return ActionReturnUtil.returnSuccessWithData(configFileList);
    }

    @Override
    public  List<Deployment> getServiceList(String projectId, String tenantId, String configMapId) throws Exception {
        AssertUtil.notBlank(projectId, DictEnum.PROJECT);
        AssertUtil.notBlank(tenantId,DictEnum.TENANT_ID);
        AssertUtil.notBlank(configMapId,DictEnum.CONFIG_MAP_ID);

        List<NamespaceLocal> namespaceListByTenantId = namespaceLocalService.getNamespaceListByTenantId(tenantId);
        List<Deployment> deploymentsList = new ArrayList<>();
        for (NamespaceLocal namespaceLocal : namespaceListByTenantId) {
            String namespace = namespaceLocal.getNamespaceName();
            DeploymentList deploymentList = deploymentsService.listDeployments(namespace, projectId);
            if(!CollectionUtils.isEmpty(deploymentList.getItems())){
                List<Deployment> deployments = deploymentList.getItems();
                for (Deployment deployment : deployments) {
                    List<Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();
                    if(!CollectionUtils.isEmpty(volumes)){
                        Volume volume = volumes.get(0);
                        String name = volume.getName();
                        //不挂存储的情况下，volume的名字形式是xxx-configMapId形式，可以据此来判断是否关联
                        if(!StringUtils.isEmpty(name)){
                            int lastIndexOf = name.lastIndexOf("-");
                            String subConfigMapId = name.substring(lastIndexOf + 1);
                            if(configMapId.equals(subConfigMapId)){
                                deploymentsList.add(deployment);
                            }
                        }
                    }
                }
            }
        }

        return deploymentsList;
    }


    @Override
    public ActionReturnUtil updateConfigTag(List<String> serviceNameList, String edition, String configName, String projectId,String tenantId,String clusterId) throws Exception {

        //根据服务名和projectId、tenantId返回对应服务
        List<ConfigFile> configFiles = configFileMapper.getConfigMapByName(configName,clusterId,projectId);
        List<String> configMapIds = new LinkedList<String>();
        for(ConfigFile configFile : configFiles){
            configMapIds.add(configFile.getId());
        }
        List<Deployment> deploymentList = new LinkedList<Deployment>();

        for(String configMapId : configMapIds){
            List<Deployment> deployments = getServiceList(projectId,tenantId,configMapId);
            for(Deployment deployment : deployments){
                if(serviceNameList.contains(deployment.getMetadata().getName())){
                    deploymentList.add(deployment);
                }
            }
        }
        //对选中的每个服务进行滚动升级
        for (Deployment deployment : deploymentList) {
            //设置每个服务的configMap为最新的
            List<Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();

            String userName = (String) session.getAttribute("username");
            //将滚动升级的容器参数给到CanaryDeployment，修改configMap
            CanaryDeployment canaryDeployment = new CanaryDeployment();
            canaryDeployment.setInstances(deployment.getStatus().getUpdatedReplicas());//更新的实例数与当前实例数相同时为滚动升级
            canaryDeployment.setSeconds(5);
            canaryDeployment.setName(deployment.getMetadata().getName());
            canaryDeployment.setMaxSurge(0);
            canaryDeployment.setMaxUnavailable(1);
            canaryDeployment.setNamespace(deployment.getMetadata().getNamespace());
            canaryDeployment.setProjectId(projectId);
            List<UpdateContainer> updateContainers = getUpdateContainerList(deployment,configName,edition,projectId,tenantId,clusterId);
            canaryDeployment.setContainers(updateContainers);
            //进行滚动升级
            versionControlService.canaryUpdate(canaryDeployment,deployment.getStatus().getUpdatedReplicas(),userName);
        }

        return ActionReturnUtil.returnSuccessWithData("success");
    }


    private  List<UpdateContainer> getUpdateContainerList(Deployment dep,String configName,String tags,String projectid,String tenantId,String clusterId) throws Exception{

        List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(dep);
        List<UpdateContainer> updateContainerList = new ArrayList<>();

        for (ContainerOfPodDetail containerOfPodDetail : containerList) {
            UpdateContainer updateContainer = new UpdateContainer();
            updateContainer.setName(containerOfPodDetail.getName());
            updateContainer.setArgs(containerOfPodDetail.getArgs());
            String tempImg = containerOfPodDetail.getImg();
            int indexOflastMH = tempImg.lastIndexOf(":");
            String tag = tempImg.substring(indexOflastMH+1);
            updateContainer.setTag(tag);
            updateContainer.setCommand(containerOfPodDetail.getCommand());
            updateContainer.setLivenessProbe(containerOfPodDetail.getLivenessProbe());
            updateContainer.setReadinessProbe(containerOfPodDetail.getReadinessProbe());
            CreateResourceDto createResourceDto = new CreateResourceDto();
            createResourceDto.setCpu((String) containerOfPodDetail.getResource().get("cpu"));
            createResourceDto.setMemory((String) containerOfPodDetail.getResource().get("memory"));
            updateContainer.setResource(createResourceDto);
            CreateResourceDto limit = new CreateResourceDto();
            limit.setCpu((String) containerOfPodDetail.getLimit().get("cpu"));
            limit.setMemory((String) containerOfPodDetail.getLimit().get("memory"));
            updateContainer.setLimit(limit);
            updateContainer.setSecurityContext(containerOfPodDetail.getSecurityContext());
            updateContainer.setImagePullPolicy(containerOfPodDetail.getImagePullPolicy());

            List<CreateEnvDto> envList = new ArrayList<>();
            if (containerOfPodDetail.getEnv() != null) {
                for (EnvVar envVar : containerOfPodDetail.getEnv()) {
                    CreateEnvDto createEnvDto = new CreateEnvDto();
                    createEnvDto.setKey(envVar.getName());
                    createEnvDto.setName(envVar.getName());
                    createEnvDto.setValue(envVar.getValue());
                    envList.add(createEnvDto);
                }
                updateContainer.setEnv(envList);
            }
            List<CreatePortDto> portList = new ArrayList<>();
            for (ContainerPort containerPort : containerOfPodDetail.getPorts()) {
                CreatePortDto createPortDto = new CreatePortDto();
                createPortDto.setProtocol(containerPort.getProtocol());
                createPortDto.setPort(String.valueOf(containerPort.getContainerPort()));
                createPortDto.setContainerPort(String.valueOf(containerPort.getContainerPort()));
                portList.add(createPortDto);
            }
            updateContainer.setPorts(portList);
            List<PersistentVolumeDto> updateVolumeList = new ArrayList<>();
            List<CreateConfigMapDto> configMapList = new ArrayList<>();
            if (containerOfPodDetail.getStorage() != null) {
                //获取要更新成configMap的configMapId
                ConfigFile configFileTemp = configFileMapper.getConfigByNameAndTag(configName,tags,projectid,clusterId);
                String configMapId = configFileTemp.getId();
                //获取服务的configMapId(更新之前的configMapId)
                int indexOfLastV = dep.getSpec().getTemplate().getSpec().getVolumes().get(0).getConfigMap().getItems().get(0).getKey().lastIndexOf("v");
                String oldtags = dep.getSpec().getTemplate().getSpec().getVolumes().get(0).getConfigMap().getItems().get(0).getKey().substring(indexOfLastV+1);
                ConfigFile configFileOld = configFileMapper.getConfigByNameAndTag(configName,oldtags,projectid,clusterId);
                String oldConfigMapId = configFileOld.getId();
                //获取新版本的所有配置文件
                List<ConfigFileItem> configFileItems = configFileItemMapper.getConfigFileItem(configMapId);
                //获取老版本的所有配置文件
                List<ConfigFileItem> oldConfigFileItems = configFileItemMapper.getConfigFileItem(oldConfigMapId);
                //用于删除元素的临时数组
                List<ConfigFileItem> tempList = new LinkedList<ConfigFileItem>();
                //获取服务目前选择的配置文件文件名
                List<VolumeMount> volumeMountList = dep.getSpec().getTemplate().getSpec().getContainers().get(0).getVolumeMounts();
                List<String> volumeConfigFileNames = new LinkedList<String>();
                for(VolumeMount volumeMount : volumeMountList){
                    int indexBeforeId = volumeMount.getName().lastIndexOf("-");
                    String configFileId = volumeMount.getName().substring(indexBeforeId+1);
                    String name = volumeMount.getSubPath();
                    volumeConfigFileNames.add(name);
                }
                /* 将之前版本中没有加入的配置文件剔除 */
                for(ConfigFileItem configFileItemIndex : configFileItems){
                    for(ConfigFileItem oldConfigFileItemIndex : oldConfigFileItems){
                        //判断新老配置组中名字、路径、内容都一致的配置文件（以此条件判断不同版本配置组的配置文件一致）
                        if(configFileItemIndex.getFileName().equals(oldConfigFileItemIndex.getFileName())
                                && configFileItemIndex.getPath().equals(oldConfigFileItemIndex.getPath())
                                && configFileItemIndex.getContent().equals(oldConfigFileItemIndex.getContent())){
                            //若服务的配置组中不存在老配置组的文件名，则删除
                            if(!volumeConfigFileNames.contains(configFileItemIndex.getFileName())){
                                tempList.add(configFileItemIndex);
                            }
                        }
                    }
                }
                configFileItems.removeAll(tempList);
                /* 配置configMap */

                for(ConfigFileItem configFileItemIndex : configFileItems) {

                    CreateConfigMapDto configMap = new CreateConfigMapDto();
                    configMap.setPath(configFileItemIndex.getPath());

                    configMap.setFile(configFileItemIndex.getFileName());
                    configMap.setConfigMapId(configFileItemIndex.getConfigfileId());

                    ActionReturnUtil configMapUtil = getConfigMap(configMap.getConfigMapId());
                    if (configMapUtil.getData() == null || !configMapUtil.isSuccess()) {
                        throw new MarsRuntimeException("未找到配置文件");
                    }

                    ConfigDetailDto configDetailDto = (ConfigDetailDto) configMapUtil.getData();
                    ConfigFile configFile = ObjConverter.convert(configDetailDto, ConfigFile.class);
                    configMap.setTag(configFile.getTags());//将版本号设置为所选版本

                    if (configFile != null) {
                        List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
                        for (ConfigFileItem configFileItem : configFileItemList) {
                            if (configMap.getFile().equals(configFileItem.getFileName())) {
                                configMap.setValue(configFileItem.getContent());
                            }
                        }
                    } else {
                        configMap.setValue(null);
                    }
                    configMapList.add(configMap);
                }
                /*configMap配置结束*/
            }
            updateContainer.setStorage(updateVolumeList);
            updateContainer.setConfigmap(configMapList);
            if (updateContainer.getLog() == null) {
                updateContainer.setLog(new LogVolume());
            }
            //更新镜像
            String imagesAllPath = containerOfPodDetail.getImg();
            int firstIndex = imagesAllPath.indexOf("/");
            int lastIndex = imagesAllPath.lastIndexOf(":");
            String image = imagesAllPath.substring(firstIndex+1,lastIndex);
            updateContainer.setImg(imagesAllPath.substring(firstIndex+1,lastIndex));
            updateContainer.setImagePullPolicy(CommonConstant.IMAGEPULLPOLICY_ALWAYS);

            updateContainerList.add(updateContainer);
        }
        return updateContainerList;
    }

    @Override
    public ActionReturnUtil getTagsByConfigName(String configName,String clusterId,String projectId) {
        List<ConfigFile> configFiles = configFileMapper.getConfigMapByName(configName,clusterId,projectId);
        List<String> tags = new LinkedList<String>();
        for(ConfigFile configFile : configFiles){
            tags.add(configFile.getTags());
        }
        return ActionReturnUtil.returnSuccessWithData(tags);
    }

    @Override
    public ActionReturnUtil  getAllServiceByConfigName(String configName,String clusterId,String projectId,String tenantId) throws Exception{
        //根据configName获取所有configMapId
        List<ConfigFile> configFiles = configFileMapper.getConfigMapByName(configName,clusterId,projectId);
        List<String> configMapIds = new LinkedList<String>();
        for(ConfigFile configFile : configFiles){
            configMapIds.add(configFile.getId());
        }
        List<Deployment> deploymentList = new LinkedList<Deployment>();
        List<ConfigService> configServices = new LinkedList<ConfigService>();
        for(String configMapId : configMapIds){
            List<Deployment> deployments = getServiceList(projectId,tenantId,configMapId);
            deploymentList.addAll(deployments);
            for(Deployment deployment : deployments) {
                ConfigService configService = new ConfigService();
                configService.setServiceName(deployment.getMetadata().getName());
                int tempIndex = deployment.getSpec().getTemplate().getSpec().getVolumes().get(0).getConfigMap().getItems().get(0).getKey().lastIndexOf("v");
                String tag = deployment.getSpec().getTemplate().getSpec().getVolumes().get(0).getConfigMap().getItems().get(0).getKey().substring(tempIndex+1);
                configService.setTag(tag);
                configService.setImage(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
                configService.setServiceDomainName(deployment.getMetadata().getName()+"."+deployment.getMetadata().getNamespace());
                configService.setCreateTime(deployment.getMetadata().getCreationTimestamp());
                configService.setUpdateTime(deployment.getStatus().getConditions().get(0).getLastUpdateTime());
                configService.setConfigName(configName);
                configService.setProjectId(projectId);
                configService.setTenantId(tenantId);
                configServices.add(configService);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(configServices);
    }

    @Override
    public void deleteConfigMap(String clusterId, String tenantId) throws Exception {
        configFileMapper.delConfByCidAndTid(clusterId, tenantId);
    }
}
