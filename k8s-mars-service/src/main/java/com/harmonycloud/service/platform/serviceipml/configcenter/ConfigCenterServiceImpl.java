package com.harmonycloud.service.platform.serviceipml.configcenter;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
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
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.service.DaemonSetService;
import com.harmonycloud.k8s.service.ReplicasetsService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.StatefulSetsService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import com.harmonycloud.service.util.BizUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.KUBE_SYSTEM;
import static com.harmonycloud.common.util.UUIDUtil.UUID_LENGTH_16;
import static com.harmonycloud.common.util.UUIDUtil.UUID_LENGTH_36;
import static com.harmonycloud.service.platform.constant.Constant.DEPLOYMENT;
import static com.harmonycloud.service.platform.convert.K8sResultConvert.TAG_LENGTH;
import static com.harmonycloud.service.platform.convert.K8sResultConvert.TAG_PATTERN;

/**
 * Created by gurongyun on 17/03/24. configcenter serviceImpl
 */
@Service
public class ConfigCenterServiceImpl implements ConfigCenterService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigCenterServiceImpl.class);

    private DecimalFormat decimalFormat = new DecimalFormat("######0.0");

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

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private DaemonSetService daemonSetService;

    @Autowired
    private DataPrivilegeService dataPrivilegeService;

    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    private StatefulSetsService statefulSetsService;

    @Autowired
    private ReplicasetsService replicasetsService;

    private static String CONFIGLIST_DATA = "configList";


    /**
     * add or update config serviceImpl on 17/03/24.
     *
     * @param configDetail required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Transactional
    @Override
    public ActionReturnUtil saveConfig(ConfigDetailDto configDetail, String userName) throws Exception {
        Assert.notNull(configDetail);
        double tags = Constant.TEMPLATE_TAG;
        String projectId;
        if(configDetail.getAppStore()){
            configDetail.setClusterId(null);
            projectId = null;
        }else{
            projectId = configDetail.getProjectId();
        }
        // 检查数据库有没有存在
        List<ConfigFile> list = configFileMapper.listConfigByName(configDetail.getName(), projectId, configDetail.getClusterId(), null);
        if (Objects.nonNull(configDetail.getIsCreate()) && Boolean.valueOf(configDetail.getIsCreate())) {
            if (!CollectionUtils.isEmpty(list)) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONFIGMAP_NAME_DUPLICATE);
            }
        }
        ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
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
        }else {
            configFile.setCreateTime(list.get(0).getCreateTime());//创建配置时，添加创建时间字段
        }
        if(StringUtils.isEmpty(configFile.getClusterId())){
            configFile.setClusterId(null);
            configFile.setClusterName(null);
        }else {
            configFile.setClusterName(clusterService.findClusterById(configFile.getClusterId()).getName());
        }
        if(!configDetail.getAppStore() && (configDetail.getIsCreate() == null || Boolean.valueOf(configDetail.getIsCreate()))) {
            dataPrivilegeService.addResource(configDetail, null, null);
        }
        return saveConfig(configFile);
    }

    @Transactional
    @Override
    public ActionReturnUtil saveConfig(ConfigFile configFile) throws Exception {
        Assert.notNull(configFile);
        ConfigFile existFile;
        if(configFile.getAppStore()){
            existFile = this.getConfigByNameAndTag(configFile.getName(),
                    configFile.getTags(), null, null);
        }else {
            existFile = this.getConfigByNameAndTag(configFile.getName(),
                    configFile.getTags(), configFile.getProjectId(), configFile.getClusterId());
        }
        if(existFile != null){
            return ActionReturnUtil.returnErrorWithData(DictEnum.CONFIG_MAP.phrase(),ErrorCodeMessage.EXIST);
        }
        if(StringUtils.isBlank(configFile.getId())){
            configFile.setId(UUIDUtil.get16UUID());
        }
        configFile.setCreateTime(DateUtil.timeFormat.format(new Date()));
        configFile.setUpdateTime(configFile.getCreateTime());
        if(StringUtils.isBlank(configFile.getTags())){
            configFile.setTags(decimalFormat.format(Constant.TEMPLATE_TAG) + "");
        }
        // 入库
        configFileMapper.saveConfigFile(configFile);
        //配置文件的明细
        this.insertConfigFileItem(configFile.getId(), configFile.getConfigFileItemList());

        JSONObject resultJson = new JSONObject();
        resultJson.put("filename", configFile.getName());
        resultJson.put("tag", configFile.getTags());
        resultJson.put("id", configFile.getId());
        return ActionReturnUtil.returnSuccessWithData(resultJson);
    }



    /**
     * update config serviceImpl on 17/03/24.
     *
     * @param configDetail required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Transactional
    @Override
    public ActionReturnUtil updateConfig(ConfigDetailDto configDetail, String userName) throws Exception {

        ConfigFile configFile = ObjConverter.convert(configDetail, ConfigFile.class);
        configFile.setUser(userName);
        String updateTime = DateUtil.timeFormat.format(new Date());
        configFile.setUpdateTime(updateTime);

        //根据configFile的id删除明细中对应的数据
        configFileItemMapper.deleteConfigFileItem(configFile.getId());
        // 入库
        configFileMapper.updateConfig(configFile);

        //配置文件的明细
        this.insertConfigFileItem(configFile.getId(), configFile.getConfigFileItemList());

        JSONObject resultJson = new JSONObject();
        resultJson.put("filename", configDetail.getName());
        return ActionReturnUtil.returnSuccessWithData(resultJson);
    }

    /**
     * delete config serviceImpl on 17/03/24.
     *
     * @param id        required
     * @param projectId required
     * @param isAppStore
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public void deleteConfig(String id, String projectId, boolean isAppStore) throws Exception {
        Assert.hasText(id);
        Assert.hasText(projectId);

        //删除配置信息
        if(isAppStore){
            configFileMapper.deleteConfig(id, null);
        }else {
            ConfigFile config = configFileMapper.getConfig(id);
            configFileMapper.deleteConfig(id, projectId);
            List<ConfigFile> configList = configFileMapper.listConfigByName(config.getName(), config.getProjectId(), config.getClusterId(), null);
            if(CollectionUtils.isEmpty(configList)){
                ConfigDetailDto configDetail = new ConfigDetailDto();
                configDetail.setName(config.getName());
                configDetail.setProjectId(config.getProjectId());
                configDetail.setClusterId(config.getClusterId());
                dataPrivilegeService.deleteResource(configDetail);
            }
        }
    }

    /**
     * find config lists for center serviceImpl on 17/03/24.
     *
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Override
    public ActionReturnUtil searchConfig(String projectId, String clusterId, String repoName, String keyword, boolean isAppStore) throws Exception {
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
        List<ConfigFile> list = new ArrayList<>();
        if(StringUtils.isNotBlank(clusterId) || (StringUtils.isBlank(clusterId) && !isAppStore)) {
            list = configFileMapper.listConfigSearch(projectId, clusterIds, repoName, keyword, Boolean.TRUE);
        }
        if(isAppStore || (StringUtils.isBlank(clusterId) && userService.checkCurrentUserIsAdmin())){
            List<ConfigFile> appStoreConfigList = configFileMapper.listConfigSearch(null, null, repoName, keyword, Boolean.TRUE);
            if(!CollectionUtils.isEmpty(appStoreConfigList)){
                list.addAll(appStoreConfigList);
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return ActionReturnUtil.returnSuccessWithData(new HashMap<>().values());
        } else {

            Map<String, ConfigFile> configFileMap = getConfigFileMap(list);
            Collection<ConfigFile> values = configFileMap.values();
            this.filterConfigFile(values, userCluster);
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
            String key;
            if(StringUtils.isNotEmpty(configFile.getClusterId())) {
                key = configFile.getName() + configFile.getProjectId() + configFile.getClusterId();
            }else{
                key = configFile.getName();
            }
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
        if(userService.checkCurrentUserIsAdmin()){
            List<ConfigFile> appStoreConfigList = configFileMapper.listConfigOverview(null, repoName, null);
            if(!CollectionUtils.isEmpty(appStoreConfigList)){
                lists.addAll(appStoreConfigList);
            }
        }
        if (CollectionUtils.isEmpty(lists)) {
            return ActionReturnUtil.returnSuccessWithData(new HashMap<>().values());
        } else {
            Map<String, ConfigFile> configFileMap = getConfigFileMap(lists);
            Collection<ConfigFile> configFiles = configFileMap.values();
            this.filterConfigFile(configFiles, userCluster);
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
    public ConfigDetailDto getConfigMap(String configMapId) throws Exception {
        // 查找配置文件
        ConfigFile configFile = configFileMapper.getConfig(configMapId);
        if(configFile == null){
            throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_NOT_EXIST);
        }
        List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(configMapId);
        configFile.setConfigFileItemList(configFileItemList);
        ConfigDetailDto configDetailDto = ObjConverter.convert(configFile, ConfigDetailDto.class);

        return configDetailDto;
    }

    /**
     * get configMap with service
     * @param configMapId
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getConfigMapWithService(String configMapId) throws Exception {
        ConfigDetailDto configDetailDto = this.getConfigMap(configMapId);
        if(StringUtils.isEmpty(configDetailDto.getClusterId())){
            configDetailDto.setAppStore(true);
        }else{
            configDetailDto.setAppStore(false);
        }
        return ActionReturnUtil.returnSuccessWithData(configDetailDto);
    }

    /**
     * delete configs service on 17/03/24.
     *
     * @param name required
     * @return ActionReturnUtil
     * @author gurongyun
     */
    @Transactional
    @Override
    public ActionReturnUtil deleteConfigMap(String name, String projectId, String clusterId, boolean isAppStore) throws Exception {
        Assert.hasText(name);
        Assert.hasText(projectId);
        if(isAppStore){
            configFileMapper.deleteConfigByName(name, null, null);
        }else{
            configFileMapper.deleteConfigByName(name, projectId, clusterId);
            //删除数据权限
            ConfigDetailDto configDetail = new ConfigDetailDto();
            configDetail.setName(name);
            configDetail.setProjectId(projectId);
            configDetail.setClusterId(clusterId);
            dataPrivilegeService.deleteResource(configDetail);
        }

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
    public ActionReturnUtil getLatestConfigMap(String name, String projectId, String repoName,String clusterId,String tags, boolean isAppStore) throws Exception {
        ConfigFile latestConfig;
        if(isAppStore){
            latestConfig = configFileMapper.getLatestConfig(name, null, repoName,null,tags);
        }else{
            latestConfig = configFileMapper.getLatestConfig(name, projectId, repoName,clusterId,tags);
        }

        if(latestConfig == null){
            return ActionReturnUtil.returnErrorWithData(DictEnum.CONFIG_MAP.phrase(), ErrorCodeMessage.NOT_FOUND);
        }
        List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(latestConfig.getId());
        latestConfig.setConfigFileItemList(configFileItemList);
        ConfigDetailDto configDetailDto = ObjConverter.convert(latestConfig, ConfigDetailDto.class);

        if(StringUtils.isNotEmpty(configDetailDto.getClusterId())) {
            Cluster cluster = clusterService.findClusterById(clusterId);
            String clusterName = cluster.getName();
            configDetailDto.setClusterName(clusterName);

            configDetailDto.setClusterAliasName(cluster.getAliasName());
            configDetailDto.setAppStore(false);
        }else{
            configDetailDto.setAppStore(true);
        }
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
    public ActionReturnUtil getConfigMapByName(String name, String clusterId, String projectId, boolean isAppStore, boolean isFilter) throws Exception  {
        List<ConfigFile> configFileList;
        if(isAppStore){
            configFileList =  configFileMapper.getConfigMapByName(name,null,null);
        }else{
            configFileList =  configFileMapper.getConfigMapByName(name,clusterId,projectId);
        }
        for(ConfigFile configFile : configFileList){
            if(StringUtils.isNotEmpty(configFile.getClusterId())) {
                Cluster cluster = clusterService.findClusterById(configFile.getClusterId());
                configFile.setClusterAliasName(cluster.getAliasName());
                configFile.setAppStore(false);
            }else {
                configFile.setAppStore(true);
            }
            List<ConfigFileItem> configFileItemList = configFileItemMapper.getConfigFileItem(configFile.getId());
            configFile.setConfigFileItemList(configFileItemList);
            User user = userService.getUser(configFile.getUser());
            if (user != null) {
                configFile.setUserRealName(user.getRealName());
            } else {
                configFile.setUserRealName(configFile.getUser());
            }
        }
        Map map = new HashMap();
        map.put(CONFIGLIST_DATA, configFileList);
        if(isFilter && !isAppStore) {
            DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
            dataPrivilegeDto.setData(name);
            dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.CONFIGFILE.getCode());
            dataPrivilegeDto.setClusterId(clusterId);
            dataPrivilegeDto.setProjectId(projectId);
            dataPrivilegeHelper.filterMap(map, dataPrivilegeDto);
        }else {
            map.put(DataPrivilegeHelper.DATA_PRIVILEGE, DataPrivilegeHelper.READWRITE);
        }
        return ActionReturnUtil.returnSuccessWithData(map);
    }

    @Override
    public  List<Deployment> getServiceList(String projectId, String tenantId, String clusterId, String configMapId) throws Exception {
        AssertUtil.notBlank(projectId, DictEnum.PROJECT);
        AssertUtil.notBlank(tenantId,DictEnum.TENANT_ID);
        AssertUtil.notBlank(configMapId,DictEnum.CONFIG_MAP_ID);

        List<NamespaceLocal> namespaceListByTenantId = namespaceLocalService.getSimpleNamespaceListByTenantId(tenantId, clusterId);
        List<Deployment> deploymentsList = new ArrayList<>();
        for (NamespaceLocal namespaceLocal : namespaceListByTenantId) {
            String namespace = namespaceLocal.getNamespaceName();
            DeploymentList deploymentList = deploymentsService.listDeployments(namespace, projectId);
            if (!CollectionUtils.isEmpty(deploymentList.getItems())) {
                List<Deployment> deployments = deploymentList.getItems();
                for (Deployment deployment : deployments) {
                    Map<String, Object> annotations = deployment.getMetadata().getAnnotations();
                    List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
                    List<Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();
                    if (CollectionUtils.isEmpty(volumes)) {
                        continue;
                    }
                    boolean find = false;
                    for (Volume volume : volumes) {
                        if (volume.getConfigMap() == null) {
                            continue;
                        }
                        String name = volume.getName();
                        String tag = volume.getName().substring(volume.getName().length() - TAG_LENGTH);
                        //兼容以前1.0.3的版本，volume名称是版本号结尾的, 升级到1.1.0版本做过数据迁移，将configmapid放到annotations
                        if (tag.matches(TAG_PATTERN)) {
                            for (Container container : containers) {
                                if (annotations != null && (annotations.get("configmapid-" + container.getName()) != null)
                                        && configMapId.equals(annotations.get("configmapid-" + container.getName()))) {
                                    deploymentsList.add(deployment);
                                    find = true;
                                    break;
                                }
                            }
                        } else {
                            //不挂存储的情况下，volume的名字形式是xxx-configMapId形式，可以据此来判断是否关联
                            if (!StringUtils.isEmpty(name)) {
                                int lastIndexOf = name.lastIndexOf("-");
                                String subConfigMapId = name.substring(lastIndexOf + 1);
                                if (configMapId.equals(subConfigMapId)) {
                                    deploymentsList.add(deployment);
                                    find = true;
                                }
                            }
                        }
                        //其中一个volume找到配置文件引用，则转到下一个服务处理
                        if (find) {
                            break;
                        }
                    }
                }
            }
        }

        return deploymentsList;
    }

    @Override
    public void cleanHistoryConfigMap() throws Exception {
        List<Cluster> clusters = clusterService.listCluster();
        for (Cluster cluster : clusters) {
            List<NamespaceLocal> namespaceLocals = namespaceLocalService.getNamespaceListByClusterId(cluster.getId());
            if (CollectionUtils.isEmpty(namespaceLocals)) {
                continue;
            }
            for (NamespaceLocal namespaceLocal : namespaceLocals) {
                try {
                    //获取分区下的所有配置文件名称
                    List<String> configMapNames = this.getConfigMapNames(namespaceLocal.getNamespaceName(), cluster);
                    if (CollectionUtils.isEmpty(configMapNames)) {
                        continue;
                    }
                    Set<String> usingConfigMapNames = new HashSet<>();
                    Set<String> appContainerNames = new HashSet<>();
                    //获取分区下的所有rs，以及rs使用的配置文件
                    K8SClientResponse rsResponse = replicasetsService.doRsByNamespace(namespaceLocal.getNamespaceName(), null, null, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(rsResponse.getStatus())) {
                        logger.error("获取rs报错，{}", rsResponse.getBody());
                        continue;
                    }
                    ReplicaSetList rsList = K8SClient.converToBean(rsResponse, ReplicaSetList.class);
                    List<ReplicaSet> replicaSets = rsList.getItems();
                    for (ReplicaSet replicaSet : replicaSets) {
                        List<Volume> volumes = replicaSet.getSpec().getTemplate().getSpec().getVolumes();
                        //获取rs使用的配置文件
                        usingConfigMapNames.addAll(this.getUsingConfigMapNames(volumes));
                        //获取rs对应的deployment名称
                        if (CollectionUtils.isEmpty(replicaSet.getMetadata().getOwnerReferences())
                                || !DEPLOYMENT.equalsIgnoreCase(replicaSet.getMetadata().getOwnerReferences().get(0).getKind())) {
                            logger.error("rs对应的OwnerReferences不存在或不是Deployment，replicaSet:{}",
                                    com.alibaba.fastjson.JSONObject.toJSONString(replicaSet));
                            continue;
                        }
                        String deployName = replicaSet.getMetadata().getOwnerReferences().get(0).getName();
                        List<Container> containers = replicaSet.getSpec().getTemplate().getSpec().getContainers();
                        for (Container container : containers) {
                            appContainerNames.add(deployName + container.getName());
                        }
                    }
                    for (String configMapName : configMapNames) {
                        //配置文件被服务使用，不清理
                        if (usingConfigMapNames.contains(configMapName)) {
                            continue;
                        }
                        //配置文件除去后16位uuid后为服务名+容器名的情况才会清理
                        if (configMapName.length() > UUID_LENGTH_16) {
                            String name = configMapName.substring(0, configMapName.length() - UUID_LENGTH_16);
                            //只清理配置文件名称为服务名+容器名+uuid组成的文件,如果不是这种格式的不清理
                            if (!appContainerNames.contains(name)) {
                                //兼容以前配置文件名称使用36位的uuid
                                if (configMapName.length() > UUID_LENGTH_36) {
                                    name = configMapName.substring(0, configMapName.length() - UUID_LENGTH_36);
                                    if (!appContainerNames.contains(name)) {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                            logger.info("清理配置文件，clusterId:{},namespace:{}, name:{}", cluster.getId(),
                                    namespaceLocal.getNamespaceName(), configMapName);
                            configmapService.delete( namespaceLocal.getNamespaceName(), configMapName, cluster);
                        }
                    }
                } catch (Exception e) {
                    logger.error("清理配置文件异常，namespace:{}", namespaceLocal.getNamespaceName(), e);
                }


            }
        }
    }

    /**
     * 查询某个分区下的所有configmap名称
     */
    private List<String> getConfigMapNames(String namespace, Cluster cluster) throws Exception {
        K8SClientResponse configmapRes = configmapService.doSepcifyConfigmap(namespace, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(configmapRes.getStatus()) || configmapRes.getBody() == null) {
            logger.error("查询配置文件失败, namespace:{},res:{}",namespace,
                    com.alibaba.fastjson.JSONObject.toJSONString(configmapRes));
        }
        ConfigMapList cmlist= JsonUtil.jsonToPojo(configmapRes.getBody(), ConfigMapList.class);
        if (cmlist == null || CollectionUtils.isEmpty(cmlist.getItems())) {
            return Collections.emptyList();
        }
        List<ConfigMap> configMaps = cmlist.getItems();
        List<String> configMapList = new ArrayList<>();
        for (ConfigMap config : configMaps) {
            configMapList.add(config.getMetadata().getName());
        }
        return configMapList;
    }

    private Set<String> getUsingConfigMapNames(List<Volume> volumes) {
        if (CollectionUtils.isEmpty(volumes)) {
            return Collections.emptySet();
        }
        Set<String> configMapNames = new HashSet<>();
        for (Volume volume : volumes) {
            if (volume.getConfigMap() != null) {
                configMapNames.add(volume.getConfigMap().getName());
            }
        }
        return configMapNames;
    }

    public  List<DaemonSet> getDaemonSetList(String configMapId,String clusterId) throws Exception {
        AssertUtil.notBlank(configMapId,DictEnum.CONFIG_MAP_ID);
        List<DaemonSet> daemonSetList = new ArrayList<>();
        Cluster cluster = clusterService.findClusterById(clusterId);
        //获取守护进程列表
        DaemonSetList daemonSets = daemonSetService.listDaemonSet(KUBE_SYSTEM, cluster);
        if (CollectionUtils.isEmpty(daemonSets.getItems())) {
            return daemonSetList;
        }
        for(DaemonSet daemonSet : daemonSets.getItems()){
            //获取守护进程详情
            List<Volume> volumes = daemonSet.getSpec().getTemplate().getSpec().getVolumes();
            boolean isUsingConfigMap = this.isUsingConfigMap(configMapId, volumes);
            if(isUsingConfigMap){
                daemonSetList.add(daemonSet);
            }
        }
        return daemonSetList;
    }

    private List<StatefulSet> getStatefulSet(String projectId, String tenantId, String clusterId, String configMapId, boolean isAppStore) throws Exception{
        if(!isAppStore) {
            AssertUtil.notBlank(projectId, DictEnum.PROJECT);
            AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        }
        AssertUtil.notBlank(configMapId,DictEnum.CONFIG_MAP_ID);

        List<StatefulSet> statefulSets = new ArrayList<>();
        if(isAppStore){
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            for(Cluster cluster : clusterList){
                StatefulSetList statefulSetList = statefulSetsService.listStatefulSets(null, null, cluster);
                if (!CollectionUtils.isEmpty(statefulSetList.getItems())) {
                    for (StatefulSet statefulSet : statefulSetList.getItems()) {
                        List<Volume> volumes = statefulSet.getSpec().getTemplate().getSpec().getVolumes();
                        boolean isUsingConfigMap = this.isUsingConfigMap(configMapId, volumes);
                        if(isUsingConfigMap){
                            statefulSets.add(statefulSet);
                        }
                    }
                }
            }
        }else {
            List<NamespaceLocal> namespaceListByTenantId = namespaceLocalService.getSimpleNamespaceListByTenantId(tenantId, clusterId);
            for (NamespaceLocal namespaceLocal : namespaceListByTenantId) {
                String namespace = namespaceLocal.getNamespaceName();
                StatefulSetList statefulSetList = statefulSetsService.listStatefulSets(namespace, projectId);
                if (!CollectionUtils.isEmpty(statefulSetList.getItems())) {
                    for (StatefulSet statefulSet : statefulSetList.getItems()) {
                        List<Volume> volumes = statefulSet.getSpec().getTemplate().getSpec().getVolumes();
                        boolean isUsingConfigMap = this.isUsingConfigMap(configMapId, volumes);
                        if (isUsingConfigMap) {
                            statefulSets.add(statefulSet);
                        }
                    }
                }
            }
        }
        return statefulSets;
    }

    private boolean isUsingConfigMap(String configMapId, List<Volume> volumes){
        if (CollectionUtils.isEmpty(volumes)) {
            return false;
        }
        for (Volume volume : volumes) {
            if (volume.getConfigMap() == null) {
                continue;
            }
            String name = volume.getName();
            if (!StringUtils.isEmpty(name)) {
                int lastIndexOf = name.lastIndexOf("-");
                String subConfigMapId = name.substring(lastIndexOf + 1);
                if (configMapId.equals(subConfigMapId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private  List<Deployment> getDeploymentByRs(String projectId, String tenantId, String clusterId, String configMapId, boolean isAppStore) throws Exception {
        if(!isAppStore) {
            AssertUtil.notBlank(projectId, DictEnum.PROJECT);
            AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        }
        AssertUtil.notBlank(configMapId,DictEnum.CONFIG_MAP_ID);
        List<Deployment> deploymentsList = new ArrayList<>();
        if(isAppStore){
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            for(Cluster cluster : clusterList) {
                K8SClientResponse rsRes = replicasetsService.doRsByNamespace(null, null, null, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                    logger.error("获取rs失败", rsRes.getBody());
                    continue;
                }
                List<String> serviceList = new ArrayList<>();
                ReplicaSetList replicaSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);
                deploymentsList.addAll(this.getDeployment(replicaSetList, configMapId, cluster, null));
            }
        }else {
            List<NamespaceLocal> namespaceListByTenantId = namespaceLocalService.getSimpleNamespaceListByTenantId(tenantId, clusterId);
            for (NamespaceLocal namespaceLocal : namespaceListByTenantId) {
                String namespace = namespaceLocal.getNamespaceName();
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
                List<String> serviceList = new ArrayList<>();
                Map bodys = new HashMap();
                bodys.put(CommonConstant.LABELSELECTOR, Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId);
                K8SClientResponse rsRes = replicasetsService.doRsByNamespace(namespace, null, bodys, null, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                    logger.error("获取rs失败", rsRes.getBody());
                    continue;
                }
                ReplicaSetList replicaSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);
                deploymentsList.addAll(this.getDeployment(replicaSetList, configMapId, cluster, namespace));
            }
        }
        return deploymentsList;
    }

    private List<Deployment> getDeployment(ReplicaSetList replicaSetList, String configMapId, Cluster cluster, String namespace) throws Exception {
        List<String> serviceList = new ArrayList<>();
        List<Deployment> deploymentsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(replicaSetList.getItems())) {
            List<ReplicaSet> replicaSets = replicaSetList.getItems();
            //判断实例数大于0的rs中是否含有配置文件
            for (ReplicaSet replicaSet : replicaSets) {
                String serviceName = null;
                String rsNamespace = replicaSet.getMetadata().getNamespace();
                if (replicaSet.getMetadata().getLabels() != null && replicaSet.getMetadata().getLabels().get(CommonConstant.LABEL_KEY_APP) != null) {
                    serviceName = replicaSet.getMetadata().getLabels().get(CommonConstant.LABEL_KEY_APP).toString();
                }
                if (replicaSet.getSpec().getReplicas() == 0 || serviceList.contains(serviceName + CommonConstant.DOT + rsNamespace)) {
                    continue;
                }
                Map<String, Object> annotations = replicaSet.getMetadata().getAnnotations();
                List<Container> containers = replicaSet.getSpec().getTemplate().getSpec().getContainers();
                List<Volume> volumes = replicaSet.getSpec().getTemplate().getSpec().getVolumes();
                if (CollectionUtils.isEmpty(volumes)) {
                    continue;
                }
                if (existConfig(volumes, containers, annotations, configMapId)) {
                    serviceList.add(serviceName + CommonConstant.DOT + rsNamespace);
                }
            }
            //根据通过rs获取到的服务，其余的deployment再检验是否包含相应配置文件
            DeploymentList deploymentList = deploymentsService.getDeployments(namespace, null,  cluster);
            if (!CollectionUtils.isEmpty(deploymentList.getItems())) {
                List<Deployment> deployments = deploymentList.getItems();
                for (Deployment deployment : deployments) {
                    String depNamespace = deployment.getMetadata().getNamespace();
                    if (serviceList.contains(deployment.getMetadata().getName() + CommonConstant.DOT + depNamespace)) {
                        deploymentsList.add(deployment);
                    } else {
                        Map<String, Object> annotations = deployment.getMetadata().getAnnotations();
                        List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
                        List<Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();
                        if (CollectionUtils.isEmpty(volumes)) {
                            continue;
                        }
                        if (existConfig(volumes, containers, annotations, configMapId)) {
                            deploymentsList.add(deployment);
                        }
                    }
                }
            }
        }
        return deploymentsList;
    }

    private boolean existConfig(List<Volume> volumes, List<Container> containers, Map<String, Object> annotations, String configMapId){
        for (Volume volume : volumes) {
            if (volume.getConfigMap() == null) {
                continue;
            }
            String name = volume.getName();
            String tag = volume.getName().substring(volume.getName().length() - TAG_LENGTH);
            //兼容以前1.0.3的版本，volume名称是版本号结尾的, 升级到1.1.0版本做过数据迁移，将configmapid放到annotations
            if (tag.matches(TAG_PATTERN)) {
                for (Container container : containers) {
                    if (annotations != null && (annotations.get("configmapid-" + container.getName()) != null)
                            && configMapId.equals(annotations.get("configmapid-" + container.getName()))) {
                        return true;
                    }
                }
            } else {
                //不挂存储的情况下，volume的名字形式是xxx-configMapId形式，可以据此来判断是否关联
                if (!StringUtils.isEmpty(name)) {
                    int lastIndexOf = name.lastIndexOf("-");
                    String subConfigMapId = name.substring(lastIndexOf + 1);
                    if (configMapId.equals(subConfigMapId)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
            List<Deployment> deployments = getServiceList(projectId,tenantId, clusterId, configMapId);
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
            //对比服务版本和更新到的目标版本
            boolean isSameEdition = false;
            String depTag = null;
            for(Volume volume : volumes){
                String key = volume.getConfigMap().getItems().get(0).getKey();
                int index = key.lastIndexOf("v");
                depTag = key.substring(index+1);
                if(depTag.equals(edition)){
                    isSameEdition = true;
                    break;
                }
            }

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
            //之前版本中不用的配置文件，在现版本中也做剔除处理
            List<UpdateContainer> updateContainers = getUpdateContainerList(deployment,configName,edition,projectId,tenantId,clusterId,isSameEdition);
            canaryDeployment.setContainers(updateContainers);
            //进行滚动升级
            versionControlService.canaryUpdate(canaryDeployment,deployment.getStatus().getUpdatedReplicas(),userName);
        }

        return ActionReturnUtil.returnSuccessWithData("success");
    }


    private  List<UpdateContainer> getUpdateContainerList(Deployment dep,String configName,String tags,String projectid,String tenantId,String clusterId,boolean isSameEdition) throws Exception{

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
                updateContainer.setEnv(containerOfPodDetail.getEnv());
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
                if(!isSameEdition) {
                    // 若为不同版本，将之前版本中没有加入的配置文件剔除
                    for (ConfigFileItem configFileItemIndex : configFileItems) {
                        for (ConfigFileItem oldConfigFileItemIndex : oldConfigFileItems) {
                            //判断新老配置组中名字、路径、内容都一致的配置文件（以此条件判断不同版本配置组的配置文件一致）
                            if (configFileItemIndex.getFileName().equals(oldConfigFileItemIndex.getFileName())
                                    && configFileItemIndex.getPath().equals(oldConfigFileItemIndex.getPath())
                                    && configFileItemIndex.getContent().equals(oldConfigFileItemIndex.getContent())) {
                                //若服务的配置组中不存在老配置组的文件名，则删除
                                if (!volumeConfigFileNames.contains(configFileItemIndex.getFileName())) {
                                    tempList.add(configFileItemIndex);
                                }
                            }
                        }
                    }
                    if (!tempList.isEmpty()) {
                        configFileItems.removeAll(tempList);
                    }
                }

                /* 配置configMap */

                for(ConfigFileItem configFileItemIndex : configFileItems) {

                    CreateConfigMapDto configMap = new CreateConfigMapDto();
                    configMap.setPath(configFileItemIndex.getPath());

                    configMap.setFile(configFileItemIndex.getFileName());
                    configMap.setConfigMapId(configFileItemIndex.getConfigfileId());

                    ConfigDetailDto configDetailDto = getConfigMap(configMap.getConfigMapId());
                    if (configDetailDto == null) {
                        throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_NOT_EXIST);
                    }

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

                for (VolumeMountExt volumeMountExt : containerOfPodDetail.getStorage()) {
                    if ("logDir".equals(volumeMountExt.getType())) {
                        LogVolume logVolumn = new LogVolume();
                        logVolumn.setName(volumeMountExt.getName());
                        logVolumn.setMountPath(volumeMountExt.getMountPath());
                        logVolumn.setReadOnly(volumeMountExt.getReadOnly().toString());
                        logVolumn.setType(volumeMountExt.getType());

                        updateContainer.setLog(logVolumn);
                    } else if (Constant.VOLUME_TYPE_PVC.equals(volumeMountExt.getType()) || Constant.VOLUME_TYPE_EMPTYDIR.equals(volumeMountExt.getType()) || Constant.VOLUME_TYPE_HOSTPASTH.equals(volumeMountExt.getType())) {
                        PersistentVolumeDto updateVolume = new PersistentVolumeDto();
                        updateVolume.setType(volumeMountExt.getType());
                        updateVolume.setReadOnly(volumeMountExt.getReadOnly());
                        updateVolume.setPath(volumeMountExt.getMountPath());
                        updateVolume.setEmptyDir(volumeMountExt.getEmptyDir());
                        updateVolume.setHostPath(volumeMountExt.getHostPath());
                        updateVolume.setRevision(volumeMountExt.getRevision());
                        if (Constant.VOLUME_TYPE_PVC.equals(volumeMountExt.getType())) {
                            updateVolume.setPvcName(volumeMountExt.getPvcname());
                        }
                        updateVolumeList.add(updateVolume);
                    }
                }
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
    public ActionReturnUtil getTagsByConfigName(String configName, String clusterId, String projectId, boolean isAppStore) {
        List<ConfigFile> configFiles;
        if(isAppStore){
            configFiles = configFileMapper.getConfigMapByName(configName,null,null);
        }else{
            configFiles = configFileMapper.getConfigMapByName(configName,clusterId,projectId);
        }
        List<String> tags = new LinkedList<String>();
        for(ConfigFile configFile : configFiles){
            tags.add(configFile.getTags());
        }
        return ActionReturnUtil.returnSuccessWithData(tags);
    }

    @Override
    public ActionReturnUtil  getAllServiceByConfigName(String configName, String clusterId, String projectId, String tenantId, boolean isAppStore) throws Exception{
        //根据configName获取所有configMapId
        List<ConfigFile> configFiles;
        if(isAppStore){
            configFiles = configFileMapper.getConfigMapByName(configName,null,null);
        }else{
            configFiles = configFileMapper.getConfigMapByName(configName,clusterId,projectId);
        }
        if(CollectionUtils.isEmpty(configFiles)){
            return ActionReturnUtil.returnSuccess();
        }
        Map<String, ConfigFile> configFileMap = configFiles.stream().collect(Collectors.toMap(ConfigFile::getId, config->config));
        List<ConfigService> configServices = new LinkedList<ConfigService>();

        for(String configMapId : configFileMap.keySet()){
            List<DaemonSet> daemonSets;
            if(isAppStore){
                daemonSets = Collections.emptyList();
            }else{
                daemonSets = getDaemonSetList(configMapId,clusterId);
            }
            List<Deployment> deployments = getDeploymentByRs(projectId,tenantId, clusterId, configMapId, isAppStore);
            List<StatefulSet> statefulSets = getStatefulSet(projectId, tenantId, clusterId, configMapId, isAppStore);

            for(DaemonSet daemonSet : daemonSets){
                ConfigService configService = new ConfigService();
                configService.setServiceName(daemonSet.getMetadata().getName());
                configService.setServiceNamespace(daemonSet.getMetadata().getNamespace());
                configService.setTag(configFileMap.get(configMapId).getTags());
                String imageName = daemonSet.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
                int firstg = imageName.indexOf("/");
                configService.setImage(imageName.substring(firstg+1));
                configService.setServiceDomainName(daemonSet.getMetadata().getName()+"."+daemonSet.getMetadata().getNamespace());
                configService.setCreateTime(daemonSet.getMetadata().getCreationTimestamp());
                configService.setUpdateTime(daemonSet.getMetadata().getCreationTimestamp());//守护进程无法更新，更新时间和创建时间保持一致
                configService.setConfigName(configName);
                configService.setProjectId(projectId);
                configService.setTenantId(tenantId);
                configService.setType("daemonSet");

                configServices.add(configService);
            }

            for(Deployment deployment : deployments) {
                ConfigService configService = new ConfigService();
                configService.setServiceName(deployment.getMetadata().getName());
                configService.setServiceNamespace(deployment.getMetadata().getNamespace());
                configService.setTag(configFileMap.get(configMapId).getTags());
                String imageName = deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
                int firstg = imageName.indexOf("/");
                configService.setImage(imageName.substring(firstg+1));
                configService.setServiceDomainName(deployment.getMetadata().getName()+"."+deployment.getMetadata().getNamespace());
                configService.setCreateTime(deployment.getMetadata().getCreationTimestamp());
                configService.setUpdateTime(BizUtil.getUpdateTime(deployment.getMetadata()));
                configService.setConfigName(configName);
                configService.setProjectId(projectId);
                configService.setTenantId(tenantId);
                configService.setType("deployment");
                if (deployment.getStatus().getAvailableReplicas() != null && deployment.getStatus().getUnavailableReplicas() == null ) {
                    configService.setStatus("available");
                }else{
                    configService.setStatus("unavailable");
                }
                dataPrivilegeHelper.filter(configService, true);
                configServices.add(configService);
            }

            for(StatefulSet statefulSet : statefulSets){
                ConfigService configService = new ConfigService();
                configService.setServiceName(statefulSet.getMetadata().getName());
                configService.setServiceNamespace(statefulSet.getMetadata().getNamespace());
                configService.setTag(configFileMap.get(configMapId).getTags());
                String imageName = statefulSet.getSpec().getTemplate().getSpec().getContainers().get(0).getImage();
                int firstg = imageName.indexOf("/");
                configService.setImage(imageName.substring(firstg+1));
                configService.setServiceDomainName(statefulSet.getMetadata().getName()+"."+statefulSet.getMetadata().getNamespace());
                configService.setCreateTime(statefulSet.getMetadata().getCreationTimestamp());
                configService.setUpdateTime(BizUtil.getUpdateTime(statefulSet.getMetadata()));
                configService.setConfigName(configName);
                configService.setProjectId(projectId);
                configService.setTenantId(tenantId);
                configService.setType(Constant.STATEFULSET);
                dataPrivilegeHelper.filter(configService, true);
                configServices.add(configService);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(configServices);
    }

    @Override
    public void deleteConfigMap(String clusterId, String tenantId) throws Exception {
        configFileMapper.delConfByCidAndTid(clusterId, tenantId);
    }

    private void insertConfigFileItem(String configId, List<ConfigFileItem> configFileItemList) {
        AssertUtil.notBlank(configId, DictEnum.CONFIG_MAP_ID);
        if (CollectionUtils.isEmpty(configFileItemList)) {
            return;
        }
        for (ConfigFileItem configFileItem : configFileItemList) {
            configFileItem.setConfigfileId(configId);
            if (!configFileItem.getPath().endsWith("/")) {
                configFileItem.setPath(configFileItem.getPath() + "/");
            }
            configFileItemMapper.insert(configFileItem);
        }
    }

    private void filterConfigFile(Collection<ConfigFile> configFileList, Map<String, Cluster> userCluster) throws Exception {
        Iterator<ConfigFile> iterator = configFileList.iterator();
        while(iterator.hasNext()){
            ConfigFile configFile = iterator.next();
            if(StringUtils.isNotEmpty(configFile.getClusterId())) {
                configFile = dataPrivilegeHelper.filter(configFile);
                if (configFile == null) {
                    iterator.remove();
                    continue;
                }
            }else{
                configFile.setDataPrivilege(DataPrivilegeHelper.READWRITE);
            }
            if(StringUtils.isNotEmpty(configFile.getClusterId())) {
                configFile.setClusterAliasName(userCluster.get(configFile.getClusterId()).getAliasName());
                configFile.setAppStore(false);
            }else{
                configFile.setAppStore(true);
            }
        }
    }
}
