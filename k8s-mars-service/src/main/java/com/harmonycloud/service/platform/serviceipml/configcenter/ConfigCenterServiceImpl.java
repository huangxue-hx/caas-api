package com.harmonycloud.service.platform.serviceipml.configcenter;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.ConfigFileItemMapper;
import com.harmonycloud.dao.application.ConfigFileMapper;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.ConfigmapService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
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
    private RoleLocalService roleLocalService;
    @Autowired
    private UserService userService;

    @Autowired
    private ConfigmapService configmapService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private DeploymentsService deploymentsService;

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
        configFile.setCreateTime(DateUtil.timeFormat.format(new Date()));
        configFile.setUser(userName);
        if (!CollectionUtils.isEmpty(list)) {
            // 存在版本号+0.1
            tags = Double.valueOf(list.get(0).getTags()) + Constant.TEMPLATE_TAG_INCREMENT;
        }
        configFile.setTags(decimalFormat.format(tags) + "");
        configFile.setClusterId(configFile.getClusterId());
        if (StringUtils.isNotBlank(configFile.getClusterId())) {
            configFile.setClusterName(clusterService.findClusterById(configFile.getClusterId()).getName());
        }
        // 入库
        configFileMapper.saveConfigFile(configFile);
        //配置文件的明细
        List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
        for (ConfigFileItem configFileItem : configFileItemList) {
            configFileItem.setConfigfileId(configFile.getId());
            configFileItemMapper.insert(configFileItem);
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
                        Volume volume = volumes.get(0);//获取到Volume
                        String name = volume.getName();//获取volume名字
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

}
