package com.harmonycloud.service.cluster.impl;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.K8sModuleEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.dto.application.StorageClassDto;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DaemonSetService;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.NodeService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.ClusterCRDService;
import com.harmonycloud.service.cluster.NodePortClusterUsageService;
import com.harmonycloud.service.integration.MicroServiceInstanceService;
import com.harmonycloud.service.platform.bean.PodDto;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.platform.service.ci.BuildEnvironmentService;
import com.harmonycloud.service.platform.service.ci.DockerFileService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.TenantClusterQuotaService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;
import static com.harmonycloud.service.platform.constant.Constant.NAMESPACE_SYSTEM;


/**
 * Created by hongjie
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterServiceImpl.class);
    private static final String MODULE_MONITOR = "monitor";

    @Autowired
    private NodeService nodeService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private InfluxdbService influxdbService;
    @Autowired
    private com.harmonycloud.service.platform.service.NodeService platformNodeService;
    @Autowired
    private NamespaceService namespaceService;
    @Autowired
    private ClusterCRDService clusterCRDService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClusterCacheManager clusterCacheManager;
    @Autowired
    private NamespaceLocalService namespaceLocalService;
    @Autowired
    private TenantClusterQuotaService tenantClusterQuotaService;
    @Autowired
    private PodService podService;
    @Autowired
    private ApplicationTemplateService applicationTemplateService;
    @Autowired
    private ServiceService serviceService;
    @Autowired
    private BuildEnvironmentService buildEnvironmentService;
    @Autowired
    private AppLogService appLogService;
    @Autowired
    private ConfigCenterService configCenterService;
    @Autowired
    private DockerFileService dockerFileService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private JobService jobService;
    @Autowired
    private MicroServiceInstanceService microServiceInstanceService;
    @Autowired
    private NodePortClusterUsageService nodePortClusterUsageService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private DeploymentService deploymentService;
    @Autowired
    private DaemonSetService daemonSetService;
    @Autowired
    private StorageClassService storageClassService;
    @Autowired
    private StatefulSetsService statefulSetsService;



    @Override
    public Cluster getPlatformCluster() throws Exception {
        return clusterCacheManager.getPlatformCluster();
    }

    @Override
    public Cluster findClusterById(String clusterId){
        Cluster platformCluster = clusterCacheManager.getPlatformCluster();
        if(platformCluster != null && platformCluster.getId().equals(clusterId)){
            return platformCluster;
        }
        Cluster cluster = clusterCacheManager.getCluster(clusterId);
        if(cluster == null){
            LOG.error("未找到集群，clusterId：{}", clusterId);
            throw new IllegalArgumentException(ErrorCodeMessage.CLUSTER_NOT_FOUND.phrase());
        }
        return cluster;
    }

    @Override
    public Cluster getClusterDetail(String clusterId) throws Exception {
        Cluster cluster = this.findClusterById(clusterId);
        ActionReturnUtil response = clusterCRDService.getCluster(cluster.getDataCenter(), cluster.getName());
        if(!response.isSuccess() || response.getData() == null){
            LOG.error("查询集群详情失败,clusterId:{},res:{}",clusterId,JSONObject.toJSONString(response));
            return cluster;
        }
        ClusterCRDDto clusterCRDDto = (ClusterCRDDto)response.getData();
        cluster.setClusterComponent(clusterCRDDto.getTemplate());
        return cluster;
    }

    @Override
    public String getHarborHost(String clusterId){
        Cluster cluster = this.findClusterById(clusterId);
        if(cluster == null){
            return null;
        }
        return cluster.getHarborServer().getHarborHost();
    }

    /**
     * 获取所有状态正常的业务集群列表，不包含容器云平台部署的集群
     *
     * @return
     */
    @Override
    public List<Cluster> listCluster() throws Exception {
        List<Cluster> clusters = clusterCacheManager.listCluster();
        if(CollectionUtils.isEmpty(clusters)){
            return Collections.emptyList();
        }
        return clusters.stream().filter(Cluster::getIsEnable).collect(Collectors.toList());
    }

    @Override
    public List<Cluster> listAllCluster(Boolean isEnable) {
        List<Cluster> clusters = clusterCacheManager.listCluster();
        if(CollectionUtils.isEmpty(clusters)){
            clusters = new ArrayList<>();
            clusters.add(clusterCacheManager.getPlatformCluster());
        }else{
            clusters.add(clusterCacheManager.getPlatformCluster());
        }
        if(isEnable != null){
            return clusters.stream().filter(Cluster::getIsEnable).collect(Collectors.toList());
        }
        return clusters;
    }

    @Override
    public List<Cluster> listCluster(String dataCenter,  Boolean isEnable, String  template ) throws Exception {
        List<Cluster> clusters = clusterCacheManager.listCluster();
        if (StringUtils.isNotBlank(template)) {
            Integer level = ClusterLevelEnum.getEnvLevel(template).getLevel();
            if (level == ClusterLevelEnum.PLATFORM.getLevel() ) {
                List<Cluster> platfromClusters = new ArrayList<Cluster>();
                Cluster platfromCluster = clusterCacheManager.getPlatformCluster();
                platfromClusters.add(platfromCluster);
                return platfromClusters;
            }
            clusters = clusters.stream().filter(cluster -> cluster.getLevel().equals(level)).collect(Collectors.toList());
        }
        if(StringUtils.isBlank(dataCenter) && isEnable == null){
            return clusters;
        }else if(StringUtils.isNotBlank(dataCenter) && isEnable != null){
            return clusters.stream().filter(cluster -> cluster.getDataCenter().equals(dataCenter)
                    && cluster.getIsEnable() == isEnable).collect(Collectors.toList());
        }else if(StringUtils.isNotBlank(dataCenter) && isEnable == null){
            return clusters.stream().filter(cluster -> cluster.getDataCenter().equals(dataCenter)).collect(Collectors.toList());
        }else {
            return clusters.stream().filter(cluster -> cluster.getIsEnable() == isEnable).collect(Collectors.toList());
        }
    }

    @Override
    public Map<String, List<Cluster>> groupCluster(Boolean isEnable) throws Exception {
        List<Cluster> clusters = clusterCacheManager.listCluster();
        clusters.add(clusterCacheManager.getPlatformCluster());
        if(isEnable != null){
            clusters = clusters.stream().filter(cluster -> cluster.getIsEnable() == isEnable).collect(Collectors.toList());
        }
        return clusters.stream().collect(Collectors.groupingBy(Cluster::getDataCenter));
    }

    @Override
    public List<String> listDisableClusterIds() throws Exception {
        List<Cluster> disableClusters = this.listCluster(null, false,null);
        if(CollectionUtils.isEmpty(disableClusters)){
            return Collections.emptyList();
        }
        return disableClusters.stream().map(Cluster::getId).collect(Collectors.toList());
    }

    /**
     * 获取集群资源使用量列表
     *
     * @return
     * @throws Exception
     */
    @Override
    public List<Map> ListClusterQuota() throws Exception {
        List<Map> list = new ArrayList<>();
        List<Cluster> clusters = this.listCluster();
        //如果集群为空返回
        if (clusters.isEmpty()){
            return  list;
        }
        for (Cluster cluster:clusters) {
            Map quotaByClusterId = this.getClusterQuotaByClusterId(cluster.getId());
            list.add(quotaByClusterId);
        }
        return list;
    }

    /**
     * 根据集群id获取集群资源使用量
     *
     * @param clusterId
     * @return
     * @throws Exception
     */
    @Override
    public Map getClusterQuotaByClusterId(String clusterId) throws Exception {
        // 初始化判断1
        if (Objects.isNull(clusterId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTERID_NOT_BLANK);
        }
        // 集群有效性判断
        Cluster cluster = this.findClusterById(clusterId);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        // 获取集群分区列表
        List<Map<String, Object>> namespaceListByClusterId = this.namespaceService.getNamespaceListByClusterId(clusterId);
        //获取集群资源使用量
        Map<String, Object> clusterQuota = tenantService.getTotalQuotaByNamespaceDataList(namespaceListByClusterId);
        return clusterQuota;
    }

    @Override
    public Map<String, Map<String, Object>> getClusterAllocatedResources(String clusterId) throws Exception {
        List<Cluster> listCluster = new ArrayList<>();
        if(StringUtils.isNotBlank(clusterId)){
            listCluster.add(this.findClusterById(clusterId));
        }else{
            listCluster = this.listCluster();
        }
        Map<String, Map<String, Object>> mapClusterResourceUsage = new HashMap<>();
        if(null != listCluster && listCluster.size() > 0) {
            for (Cluster cluster : listCluster) {
                double clusterCpuCapacity = 0;
                double clusterMemoryCapacity = 0;
                Map<String, Object> res = new HashMap<String, Object>();
                Map<String, Object> allocatableMap = this.dashboardService.getInfraInfoWorkNode(cluster);

                clusterCpuCapacity = Double.parseDouble(allocatableMap.get("cpu").toString());
                clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memoryGb").toString()) ;
                List<TenantClusterQuota> clusterQuotas = tenantClusterQuotaService.getClusterQuotaByClusterId(cluster.getId(),false);
                double clusterCpuAllocatedResources =0;
                double clusterMemoryAllocatedResources =0;
                for(TenantClusterQuota quota : clusterQuotas){
                    clusterCpuAllocatedResources += quota.getCpuQuota();
                    clusterMemoryAllocatedResources += quota.getMemoryQuota();
                }
                clusterMemoryAllocatedResources = (double) clusterMemoryAllocatedResources / NUM_SIZE_MEMORY;

                double cpufenzi = (double) clusterCpuCapacity - clusterCpuAllocatedResources;
                cpufenzi = new BigDecimal(cpufenzi).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                if(cpufenzi < 0){
                    cpufenzi = 0;
                }
                double clusterCpuAllocatedResourcesRate = (double) cpufenzi / clusterCpuCapacity ;

                double memfenz = (double) clusterMemoryCapacity - clusterMemoryAllocatedResources;
                memfenz = new BigDecimal(memfenz).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                if(memfenz < 0){
                    memfenz = 0;
                }
                double clusterMemoryAllocatedResourcesRate = memfenz / clusterMemoryCapacity ;

                Map<String, Object> map = new HashMap<String, Object>();

                map.put("clusterCpuAllocatedResourcesRate", String.format("%.0f",clusterCpuAllocatedResourcesRate*100));
                map.put("clusterMemoryAllocatedResourcesRate", String.format("%.0f",clusterMemoryAllocatedResourcesRate*100));
                map.put("clusterCpuCapacity", clusterCpuCapacity);
                map.put("clusterCpuAllocatedResources", cpufenzi);
                map.put("clusterMemoryCapacity", String.format("%.1f", clusterMemoryCapacity));
                map.put("clusterMemoryAllocatedResources", memfenz);
                mapClusterResourceUsage.put(cluster.getId(), map);
            }
        }
        return mapClusterResourceUsage;
    }

    @Override
    public ActionReturnUtil getClusterResourceUsage(String clusterId, String nodeName) throws Exception{
        String hostName = nodeName;
        List<Cluster> listCluster = new ArrayList<>();
        if(clusterId != null) {
            Cluster cluster = this.findClusterById(clusterId);
            if(cluster == null){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            if(StringUtils.isNotBlank(nodeName)){
                String host = platformNodeService.gethostname(nodeName, cluster);
                if (host != null ){
                    hostName = host;
                }
            }
            listCluster.add(cluster);
        } else {
            listCluster = this.listCluster();
        }
        Map<String, Map<String, Object>> mapClusterResourceUsage = new HashMap<>();
        if(null != listCluster && listCluster.size() > 0) {
            for (Cluster cluster : listCluster) {
                double clusterCpuCapacity = 0;
                double clusterMemoryCapacity = 0;
                double clusterFilesystemCapacity = 0;
                double clusterFilesystemUsage = 0;
                double clusterMemoryUsage = 0;
                double clusterCpuUsage = 0;
                Map<String, Object> res = new HashMap<String, Object>();

                Map<String, Object> allocatableMap = null;
                if(null != hostName && !"".equals(hostName)) {
                    allocatableMap = this.dashboardService.getNodeInfo(cluster, hostName);
                } else {
                    allocatableMap = this.dashboardService.getInfraInfoWorkNode(cluster);
                }

                clusterCpuCapacity = Double.parseDouble(allocatableMap.get("cpu").toString()) ;
                clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memoryGb").toString()) ;
                List<String> notWorkNodeList = nodeService.listNotPublicNode(cluster);
                clusterCpuUsage = this.influxdbService.getClusterResourceUsage("node", "cpu/usage_rate","nodename", cluster, notWorkNodeList, hostName);
                clusterCpuUsage = (double) clusterCpuUsage / 1000;
                clusterCpuUsage = new BigDecimal(clusterCpuUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterCpuUsageRate = 0;
                if(clusterCpuCapacity > 0) {
                    clusterCpuUsageRate = (double) clusterCpuUsage / clusterCpuCapacity;
                    clusterCpuUsageRate = new BigDecimal(clusterCpuUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                clusterMemoryUsage =  this.influxdbService.getClusterResourceUsage("node", "memory/working_set", "nodename",cluster, notWorkNodeList, hostName);
                clusterMemoryUsage = (double)clusterMemoryUsage / 1024 /1024/1024;
                clusterMemoryUsage = new BigDecimal(clusterMemoryUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterMemoryUsageRate = 0;
                if(clusterMemoryCapacity > 0) {
                    clusterMemoryUsageRate = (double) clusterMemoryUsage / clusterMemoryCapacity;
                    clusterMemoryUsageRate = new BigDecimal(clusterMemoryUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                clusterFilesystemUsage =  this.influxdbService.getClusterResourceUsage("node", "filesystem/usage", "nodename,resource_id",cluster, notWorkNodeList, hostName);
                clusterFilesystemUsage = (double)clusterFilesystemUsage/1024/1024/1024;
                clusterFilesystemUsage = new BigDecimal(clusterFilesystemUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterFilesystemUsageRate = 0;
                clusterFilesystemCapacity =  this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id",cluster, notWorkNodeList,hostName);
                if(clusterFilesystemCapacity >0) {
                    clusterFilesystemCapacity = (double) clusterFilesystemCapacity / 1024 / 1024 / 1024;
                    clusterFilesystemCapacity = new BigDecimal(clusterFilesystemCapacity).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
                    clusterFilesystemUsageRate = (double) clusterFilesystemUsage / clusterFilesystemCapacity;
                    clusterFilesystemUsageRate = new BigDecimal(clusterFilesystemUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                double clusterVolumeCapacity = 0;
                List<StorageClassDto> storageClassDtoList = storageClassService.listStorageClass(cluster.getId());
                for (StorageClassDto sc : storageClassDtoList){
                    if (StringUtils.isNotEmpty(sc.getStorageLimit()) && sc.getStatus()==1){
                        clusterVolumeCapacity += Double.parseDouble(sc.getStorageLimit());
                    }
                }
                double clusterVolumeUsage = 0;
                double clusterVolumeUsageRateValue = 0;
                if(clusterVolumeCapacity > 0) {
                    clusterVolumeUsage = this.influxdbService.getClusterResourceUsage("pvc", "volume/usage", "", cluster, notWorkNodeList, hostName);
                    if (clusterVolumeUsage > 0) {
                        clusterVolumeUsage = clusterVolumeUsage / 1024 / 1024 / 1024;
                        clusterVolumeUsage = BigDecimal.valueOf(clusterVolumeUsage).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        clusterVolumeUsageRateValue = clusterVolumeUsage / clusterVolumeCapacity;
                        clusterVolumeUsageRateValue = BigDecimal.valueOf(clusterVolumeUsageRateValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    }
                }
                Map<String, Object> map = new HashMap<String, Object>();

                map.put("clusterCpuUsageRateName", String.format("%.0f", clusterCpuUsageRate*100) + "%");
                String [] cpuUsageRateArray = {clusterCpuUsageRate+"", (double)(1-clusterCpuUsageRate) + ""};
                map.put("clusterCpuUsageRateValue", cpuUsageRateArray);

                map.put("clusterMemoryUsageRateName", String.format("%.0f", clusterMemoryUsageRate*100) + "%");
                String [] memoryUsageRateArray = {clusterMemoryUsageRate +"", (double)(1-clusterMemoryUsageRate) + ""};
                map.put("clusterMemoryUsageRateValue", memoryUsageRateArray);

                map.put("clusterFilesystemUsageRateName", String.format("%.0f", clusterFilesystemUsageRate*100) + "%");
                String [] filesystemUsageRateArray = {clusterFilesystemUsageRate +"", (double)(1-clusterFilesystemUsageRate) +""};
                map.put("clusterFilesystemUsageRateValue", filesystemUsageRateArray);

                map.put("clusterCpuCapacity", clusterCpuCapacity);
                map.put("clusterCpuUsage", clusterCpuUsage);
                map.put("clusterMemoryCapacity", String.format("%.1f", clusterMemoryCapacity));
                map.put("clusterMemoryUsage", clusterMemoryUsage);
                map.put("clusterFilesystemCapacity", clusterFilesystemCapacity);
                map.put("clusterFilesystemUsage", clusterFilesystemUsage);
                map.put("clusterVolumeCapacity", String.format("%.1f", clusterVolumeCapacity));
                map.put("clusterVolumeUsage", clusterVolumeUsage);
                map.put("clusterVolumeUsageRateValue", new String[]{Double.toString(clusterVolumeUsageRateValue), Double.toString((double) (1 - clusterVolumeUsageRateValue))});
                map.put("clusterVolumeUsageRateName", String.format("%.0f", clusterVolumeUsageRateValue * 100) + "%");
                //节点gpu资源总量、使用量
                if(StringUtils.isNotEmpty(hostName) && allocatableMap.get(CommonConstant.GPU) != null){
                    int gpuCapacity = Integer.valueOf((String)allocatableMap.get(CommonConstant.GPU));
                    int gpuUsage = this.getGpuUsage(hostName, cluster);
                    map.put("gpuCapacity", gpuCapacity);
                    map.put("gpuUsage", gpuUsage);
                    String gpuRateName = "0%";
                    double gpuUsageRate = 0;
                    if(gpuCapacity != 0) {
                        gpuUsageRate = new BigDecimal((double) gpuUsage / gpuCapacity).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        gpuRateName = String.format("%.0f", gpuUsageRate * 100) + "%";
                    }
                    map.put("gpuUsageRateName", gpuRateName);
                    map.put("gpuUsageRateValue", new String[]{Double.toString(gpuUsageRate), Double.toString((double) (1 - gpuUsageRate))});
                }
                mapClusterResourceUsage.put(cluster.getId(), map);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(mapClusterResourceUsage);
    }

    /**
     * 获取节点上的gpu使用量
     * @param hostName
     * @param cluster
     * @return
     */
    private Integer getGpuUsage(String hostName, Cluster cluster) {
        int gpuUsage = 0;
        Node node = nodeService.getNode(hostName, cluster);
        if(node != null && node.getMetadata().getLabels() != null){
            Object privateNamespace = node.getMetadata().getLabels().get(CommonConstant.HARMONYCLOUD_TENANTNAME_NS);
            if(privateNamespace != null){
                privateNamespace.toString();
            }
        }
        if(StringUtils.isNotEmpty(hostName)){
            String selector = CommonConstant.SPEC_NODENAME + "=" + hostName;
            Map<String, Object> body = new HashMap<>();
            body.put(CommonConstant.FIELD_SELECTOR, selector);
            K8SURL url = new K8SURL();
            url.setResource(Resource.POD);
            K8SClientResponse podRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, body, cluster);
            if(HttpStatusUtil.isSuccessStatus(podRes.getStatus())){
                PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
                if(podList.getItems() != null && !CollectionUtils.isEmpty(podList.getItems())){
                    for(Pod pod : podList.getItems()){
                        boolean allocated = false;
                        if(CommonConstant.POD_STATUS_PENDING.equalsIgnoreCase(pod.getStatus().getPhase())){
                            List<PodCondition> podConditions = pod.getStatus().getConditions();
                            if(!CollectionUtils.isEmpty(podConditions)){
                                for(PodCondition podCondition : podConditions) {
                                    if (podCondition.getType().equalsIgnoreCase(CommonConstant.POD_CONDITION_PODSCHEDULED)){
                                        allocated = true;
                                        break;
                                    }
                                }
                            }
                        }else if(CommonConstant.POD_STATUS_RUNNING.equalsIgnoreCase(pod.getStatus().getPhase())){
                            allocated = true;
                        }

                        if(allocated){
                            for(Container container : pod.getSpec().getContainers()){
                                if(container.getResources() != null && container.getResources().getLimits() != null){
                                    Map<String, Object> limits = (Map<String, Object>)container.getResources().getLimits();
                                    if(limits.get(CommonConstant.NVIDIA_GPU) != null){
                                        gpuUsage += Integer.valueOf(limits.get(CommonConstant.NVIDIA_GPU).toString());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return gpuUsage;
    }


    /**
     * 获取集群domain
     *
     * @return
     */
    @Override
    public ClusterDomain findDomain(String namespace) throws Exception {
        if (StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return this.findClusterById(cluster.getId()).getDomains();
    }

    @Override
    public HarborServer findHarborByHost(String host) throws MarsRuntimeException{
        try {
            AssertUtil.notBlank(host, DictEnum.HARBOR_HOST);
            List<Cluster> clusters = clusterCacheManager.listCluster();
            clusters.add(this.getPlatformCluster());
            for(Cluster cluster : clusters){
                if(host.equals(cluster.getHarborServer().getHarborHost())){
                    return cluster.getHarborServer();
                }
            }
        }catch (Exception e){
            LOG.error("根据harborhost查找habor失败,harborHost:{}",host,e);
            throw new MarsRuntimeException(ErrorCodeMessage.HARBOR_FIND_ERROR, host,false);
        }
        throw new MarsRuntimeException(ErrorCodeMessage.HARBOR_FIND_ERROR, host,false);
    }

    @Override
    public Set<HarborServer> listAllHarbors() throws Exception{
        Set<HarborServer> harborServers = new HashSet<>();
        List<Cluster> clusters = clusterCacheManager.listCluster();
        if(CollectionUtils.isEmpty(clusters)){
            return harborServers;
        }
        for(Cluster cluster : clusters){
            harborServers.add(cluster.getHarborServer());
        }
        return harborServers;
    }

    @Override
    public String getEntry(String namespace) throws Exception {
        if (StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        List<ClusterExternal> clusterExternalList = cluster.getExternal();
        String topIp = null;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(clusterExternalList)) {
            for (ClusterExternal external : clusterExternalList) {
                topIp = external.getTopLb();
                break;
            }
        }
        return topIp;
    }

    @Override
    public String getClusterNameByClusterId(String clusterId) throws Exception{
        Cluster cluster = findClusterById(clusterId);
        if(null == cluster){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return cluster.getName();
    }

    @Override
    public Map<String, Object> getClusterComponentStatus(String clusterId) throws Exception {
        Cluster cluster = this.findClusterById(clusterId);
        //pod方式部署的组件
        Map<String, String> podComStatus = new HashMap<>();
        podComStatus.put(K8sModuleEnum.KUBE_APISERVER.getCode(), Constant.STATUS_NORMAL);
        podComStatus.put(K8sModuleEnum.KUBE_CONTROLLER_MANAGER.getCode(), Constant.STATUS_NORMAL);
        podComStatus.put(K8sModuleEnum.KUBE_SCHEDULER.getCode(), Constant.STATUS_NORMAL);
        podComStatus.put(K8sModuleEnum.ETCD.getCode(), Constant.STATUS_NORMAL);
        //deployment方式部署的组件
        Map<String, String> deployComStatus = new HashMap<>();
        deployComStatus.put(K8sModuleEnum.KUBE_DNS.getCode(), Constant.STATUS_NORMAL);
        deployComStatus.put(K8sModuleEnum.NFS.getCode(), Constant.STATUS_NORMAL);
        deployComStatus.put(K8sModuleEnum.HEAPSTER.getCode(), Constant.STATUS_NORMAL);
        deployComStatus.put(K8sModuleEnum.INFLUXDB.getCode(), Constant.STATUS_NORMAL);
        deployComStatus.put(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getCode(), Constant.STATUS_NORMAL);
        //statefulset方式部署的组件
        Map<String, String> statefulsetComStatus = new HashMap<>();
        statefulsetComStatus.put(K8sModuleEnum.ELASTICSEARCH.getCode(), Constant.STATUS_NORMAL);
        if (cluster.getNetworkType().equalsIgnoreCase(K8S_NETWORK_CALICO)) {
            deployComStatus.put(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getCode(), Constant.STATUS_NORMAL);
        }
        //daemonSet方式部署的组件
        Map<String, String> daemonsetComStatus = new HashMap<>();
        if (cluster.getNetworkType().equalsIgnoreCase(K8S_NETWORK_HCIPAM)) {
            daemonsetComStatus.put(K8sModuleEnum.HCIPAM.getCode(), Constant.STATUS_NORMAL);
        } else {
            daemonsetComStatus.put(K8sModuleEnum.CALICO.getCode(), Constant.STATUS_NORMAL);
        }
        daemonsetComStatus.put(K8sModuleEnum.SERVICE_LOADBALANCER.getCode(), Constant.STATUS_NORMAL);
        daemonsetComStatus.put(K8sModuleEnum.LOG_PILOT.getCode(), Constant.STATUS_NORMAL);

        Set<String> createdComponent = new HashSet<>();
        int podAbnormalCount = this.calPodComHealthy(createdComponent, podComStatus, cluster);
        int deployAbnormalCount = this.calDeployComHealthy(createdComponent, deployComStatus, cluster);
        int daemonsetAbnormalCount = this.calDaemonsetComHealthy(createdComponent, daemonsetComStatus, cluster);
        int statefulsetAbnormalCount = this.calStatefulsetComHealthy(createdComponent, statefulsetComStatus, cluster);
        int abnormalCount = podAbnormalCount + deployAbnormalCount + daemonsetAbnormalCount + statefulsetAbnormalCount;

        //汇总所有组件的状态
        Map<String, Object> allComStatus = new HashMap<>();
        allComStatus.putAll(podComStatus);
        allComStatus.putAll(deployComStatus);
        allComStatus.putAll(daemonsetComStatus);
        allComStatus.putAll(statefulsetComStatus);
        //设置组合组件的状态
        abnormalCount += this.calComposeComHealth(createdComponent, allComStatus, cluster);

        //如果组件没有创建，状态修改为异常
        for (String componentCode : allComStatus.keySet()) {
            //存储没有创建也是正常的，只需判断已经创建的deployment的状态
            if (!componentCode.equals(K8sModuleEnum.NFS.getCode())) {
                if (!createdComponent.contains(componentCode)) {
                    allComStatus.put(componentCode, Constant.STATUS_ABNORMAL);
                    abnormalCount++;
                }
            }
        }

        double totalCount = allComStatus.size();
        String health = String.format("%.0f", (totalCount - abnormalCount) / totalCount * PERCENT_HUNDRED);
        Map<String, Object> resultMap = new HashMap<>();
        List<String> abnormalComponentNames = new ArrayList<>();
        allComStatus.forEach((k, v) -> {
            if(v.equals(Constant.STATUS_ABNORMAL)){
                abnormalComponentNames.add(k);
            }
            resultMap.put(k.replaceAll(CommonConstant.LINE, ""), v);
        });
        resultMap.put("abnormalComponent", abnormalComponentNames);
        resultMap.put("clusterComponentHealth", health);
        resultMap.put("totalComponentCount", totalCount);
        resultMap.put("normalComponentCount", totalCount - abnormalCount);
        return resultMap;

    }

    @Override
    public void deleteClusterData(String clusterId) throws Exception{
        try {
            int deleteCount = harborProjectService.deleteByClusterId(clusterId);
            LOG.info("delete repository count:{}",deleteCount);
            deleteCount = applicationTemplateService.deleteByClusterId(clusterId);
            LOG.info("delete app template count:{}",deleteCount);
            deleteCount = serviceService.deleteTemplateByClusterId(clusterId);
            LOG.info("delete service template count:{}",deleteCount);
            deleteCount = buildEnvironmentService.deleteByClusterId(clusterId);
            LOG.info("delete build env count:{}",deleteCount);
            deleteCount = appLogService.deleteBackupRuleByClusterId(clusterId);
            LOG.info("delete log backup rule count:{}",deleteCount);
            deleteCount = configCenterService.deleteByClusterId(clusterId);
            LOG.info("delete config file count:{}",deleteCount);
            deleteCount = dockerFileService.deleteByClusterId(clusterId);
            LOG.info("delete docker file count:{}",deleteCount);
            deleteCount = jobService.deleteByClusterId(clusterId);
            LOG.info("delete cicd job count:{}",deleteCount);
            deleteCount = microServiceInstanceService.deleteByClusterId(clusterId);
            LOG.info("delete msf instance count:{}",deleteCount);
            deleteCount = namespaceLocalService.deleteByClusterId(clusterId);
            LOG.info("delete namespace count:{}",deleteCount);
            deleteCount = nodePortClusterUsageService.deleteByClusterId(clusterId);
            LOG.info("delete node port cluster usage count:{}",deleteCount);
            deleteCount = tenantClusterQuotaService.deleteByClusterId(clusterId);
            LOG.info("delete tenant cluster quota count:{}",deleteCount);
            deleteCount = roleLocalService.deleteRoleCluster(clusterId);
            LOG.info("update role cluster count:{}",deleteCount);
        }catch (MarsRuntimeException e){
            throw e;
        }catch (Exception e){
            LOG.error("删除集群数据失败,clusterId:{}",clusterId, e);
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_DATA_DELETE_FAIL);
        }
    }

    @Override
    public Map<String, String> getClustersStorageCapacity() throws Exception{
        List<Cluster> clusters = this.listAllCluster(null);
        Map<String, String> clustersMap = new HashMap<>();
        for (Cluster cluster: clusters) {
            Double capacity = 0.0;
            List<StorageClassDto> storageClassDtoList = storageClassService.listStorageClass(cluster.getId());
            for (StorageClassDto sc : storageClassDtoList){
                if (StringUtils.isNotEmpty(sc.getStorageLimit()) && sc.getStatus()==1){
                    capacity += Double.parseDouble(sc.getStorageLimit());
                }
            }
            clustersMap.put(cluster.getId(),capacity.toString()+"GB");
        }
        return clustersMap;
    }

    /**
     * 查询k8s核心组件状态，根据pod状态判断
     */
    private int calPodComHealthy(Set<String> createdComponent, Map<String, String> map,
                                 Cluster cluster) throws Exception {
        int abnormalCount = 0;
        List<PodDto> podDtoList = podService.getPodListByNamespace(cluster, NAMESPACE_SYSTEM);
        for (PodDto pod : podDtoList) {
            //k8s组件节点
            for (String code : map.keySet()) {
                if (pod.getName().startsWith(code)) {
                    //设置已经创建的组件列表
                    createdComponent.add(code);
                    if (pod.getStatus().equalsIgnoreCase(Constant.RUNNING)) {
                        continue;
                    }
                    //设置状态为异常的组件，如果一个组件有多个pod，每个节点一个pod，如果其中一个pod状态不正常，则该组件状态为异常
                    map.put(code, Constant.STATUS_ABNORMAL);
                    abnormalCount++;
                }
            }
        }
        return abnormalCount;
    }

    /**
     * 查询平台核心组件状态，根据deployment状态判断
     */
    private int calDeployComHealthy(Set<String> createdComponent, Map<String, String> map,
                                    Cluster cluster) throws Exception {
        int abnormalCount = 0;
        boolean nfsAbnormal = false;
        K8SClientResponse depRes = deploymentService.doDeploymentsByNamespace(KUBE_SYSTEM, null,
                null, HTTPMethod.GET, cluster);
        DeploymentList deploymentList = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        for (Deployment dep : deploymentList.getItems()) {
            String deploymentName = dep.getMetadata().getName();
            //nfs存储的deployment名称不固定，由用户创建，deployment名称以nfs-client-provisioner开头
            // 只要其中有一个状态异常，则该组件为异常
            if (deploymentName.startsWith(K8sModuleEnum.NFS.getK8sComponentName())) {
                createdComponent.add(K8sModuleEnum.NFS.getCode());
                if (dep.getStatus().getUnavailableReplicas() != null) {
                    map.put(K8sModuleEnum.NFS.getCode(), Constant.STATUS_ABNORMAL);
                    nfsAbnormal = true;
                }
            }
            //平台节点
            for (String code : map.keySet()) {
                if (deploymentName.contains(code)) {
                    createdComponent.add(code);
                    //deployment组件的状态是否异常根据服务的不可用副本数是否不为空判断
                    if (dep.getStatus().getUnavailableReplicas() != null) {
                        map.put(code, Constant.STATUS_ABNORMAL);
                        //组合组件先不计算异常数量，等全部组件状态计算之后，再另行计算
                        if(!isComposeComponent(deploymentName)){
                            abnormalCount++;
                        }
                    }
                }
            }
        }
        if (nfsAbnormal) {
            abnormalCount++;
        }
        return abnormalCount;
    }

    /**
     * 查询负载均衡组件状态，根据daemonset状态判断
     */
    private int calDaemonsetComHealthy(Set<String> createdComponent, Map<String, String> map,
                                       Cluster cluster) throws Exception {
        int abnormalCount = 0;
        //负载均衡 nginx-ingress-controller使用daemonset启动
        DaemonSetList dsRes = daemonSetService.listDaemonSet(KUBE_SYSTEM, null, cluster);
        boolean nginxIngressAbnormal = false;
        for (DaemonSet daemonSet : dsRes.getItems()) {
            String daemonSetName = daemonSet.getMetadata().getName();
            for (String code : map.keySet()) {
                if (daemonSetName.startsWith(code)) {
                    //设置已经创建的组件列表
                    createdComponent.add(code);
                    //设置状态为异常的组件，如果一个组件有多个pod，每个节点一个pod，如果其中一个pod状态不正常，则该组件状态为异常
                    if (daemonSet.getStatus().getNumberReady() == 0 || daemonSet.getStatus().getNumberUnavailable() != null) {
                        map.put(code, Constant.STATUS_ABNORMAL);
                        if (!isComposeComponent(daemonSetName)) {
                            //负载均衡nginx-ingress-controller 可以自定义创建多个，计算异常数量时只计算一个
                            if(daemonSetName.startsWith(K8sModuleEnum.SERVICE_LOADBALANCER.getK8sComponentName())){
                                nginxIngressAbnormal = true;
                            }else {
                                abnormalCount++;
                            }
                        }
                        LOG.error("集群组件异常，名称：{}, 状态:{}", daemonSetName, JSONObject.toJSONString(daemonSet.getStatus()));
                        continue;
                    }
                }
            }
        }

        if(nginxIngressAbnormal){
            abnormalCount++;
        }
        return abnormalCount;
    }

    /**
     * 查询使用Statefulset方式部署的组件状态
     */
    private int calStatefulsetComHealthy(Set<String> createdComponent, Map<String, String> map,
                                       Cluster cluster) throws Exception {
        int abnormalCount = 0;
        StatefulSetList dsRes = statefulSetsService.listStatefulSets(KUBE_SYSTEM, null, cluster);
        for (StatefulSet statefulSet : dsRes.getItems()) {
            String statefulSetName = statefulSet.getMetadata().getName();
            for (String code : map.keySet()) {
                if (statefulSetName.startsWith(code)) {
                    //设置已经创建的组件列表
                    createdComponent.add(code);
                    //设置状态为异常的组件，如果一个组件有多个pod，每个节点一个pod，如果其中一个pod状态不正常，则该组件状态为异常
                    if (statefulSet.getStatus().getReplicas() != statefulSet.getStatus().getReadyReplicas()) {
                        map.put(code, Constant.STATUS_ABNORMAL);
                        if (!isComposeComponent(statefulSetName)) {
                            abnormalCount++;
                        }
                        LOG.error("集群组件异常，名称：{}, 状态:{}", statefulSetName, JSONObject.toJSONString(statefulSet.getStatus()));
                        continue;
                    }
                }
            }
        }
        return abnormalCount;
    }

    /**
     * 设置组合组件的状态， 监控由heapster和influxdb组成， 日志由fluentd和es组成
     *
     * @param allComStatus
     */
    private int calComposeComHealth(Set<String> createdComponent, Map<String, Object> allComStatus, Cluster cluster) {
        int composeComAbnormalCount = 0;
        //监控由heapster和influxdb组成
        if (createdComponent.contains(K8sModuleEnum.HEAPSTER.getCode())
             && createdComponent.contains(K8sModuleEnum.INFLUXDB.getCode())
                && allComStatus.get(K8sModuleEnum.HEAPSTER.getCode()).equals(Constant.STATUS_NORMAL)
                && allComStatus.get(K8sModuleEnum.INFLUXDB.getCode()).equals(Constant.STATUS_NORMAL)) {
            createdComponent.add(K8sModuleEnum.MONITOR.getCode());
            allComStatus.put(K8sModuleEnum.MONITOR.getCode(), Constant.STATUS_NORMAL);
        } else {
            composeComAbnormalCount++;
            allComStatus.put(K8sModuleEnum.MONITOR.getCode(), Constant.STATUS_ABNORMAL);
        }
        //日志由fluentd和es组成, 前端判断日志根据es的code显示
        if (createdComponent.contains(K8sModuleEnum.LOG_PILOT.getCode())
                && createdComponent.contains(K8sModuleEnum.ELASTICSEARCH.getCode())
                && allComStatus.get(K8sModuleEnum.LOG_PILOT.getCode()).equals(Constant.STATUS_NORMAL)
                && allComStatus.get(K8sModuleEnum.ELASTICSEARCH.getCode()).equals(Constant.STATUS_NORMAL)) {
            createdComponent.add(K8sModuleEnum.LOGGING.getCode());
            allComStatus.put(K8sModuleEnum.LOGGING.getCode(), Constant.STATUS_NORMAL);
        } else {
            composeComAbnormalCount++;
            allComStatus.put(K8sModuleEnum.LOGGING.getCode(), Constant.STATUS_ABNORMAL);
        }
        if (cluster.getNetworkType().equalsIgnoreCase(K8S_NETWORK_CALICO)) {
            //网络组件由calico-node的daemonset和calico-kube-controllers 的deployment组成
            if (createdComponent.contains(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getCode())
                    && createdComponent.contains(K8sModuleEnum.CALICO.getCode())
                    && allComStatus.get(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getCode()).equals(Constant.STATUS_NORMAL)
                    && allComStatus.get(K8sModuleEnum.CALICO.getCode()).equals(Constant.STATUS_NORMAL)) {
                allComStatus.put(K8sModuleEnum.CALICO.getCode(), Constant.STATUS_NORMAL);
            } else {
                composeComAbnormalCount++;
                allComStatus.put(K8sModuleEnum.CALICO.getCode(), Constant.STATUS_ABNORMAL);
            }
        }
        allComStatus.remove(K8sModuleEnum.HEAPSTER.getCode());
        allComStatus.remove(K8sModuleEnum.INFLUXDB.getCode());
        allComStatus.remove(K8sModuleEnum.LOG_PILOT.getCode());
        allComStatus.remove(K8sModuleEnum.ELASTICSEARCH.getCode());
        allComStatus.remove(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getCode());
        return composeComAbnormalCount;
    }

    private boolean isComposeComponent(String name){
        if(name.equals(K8sModuleEnum.HEAPSTER.getK8sComponentName())){
            return true;
        }
        if(name.equals(K8sModuleEnum.INFLUXDB.getK8sComponentName())){
            return true;
        }
        if(name.equals(K8sModuleEnum.CALICO_KUBE_CONTROLLER.getK8sComponentName())){
            return true;
        }
        if(name.equals(K8sModuleEnum.ELASTICSEARCH.getK8sComponentName())){
            return true;
        }
        if(name.equals(K8sModuleEnum.LOG_PILOT.getK8sComponentName())){
            return true;
        }
        if(name.equals(K8sModuleEnum.CALICO.getK8sComponentName())){
            return true;
        }
        return false;
    }

}

