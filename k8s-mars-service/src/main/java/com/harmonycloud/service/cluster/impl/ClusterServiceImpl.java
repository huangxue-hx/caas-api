package com.harmonycloud.service.cluster.impl;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;

import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.K8sModuleEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.StringUtil;
import com.harmonycloud.dao.tenant.bean.TenantClusterQuota;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.service.NodeService;
import com.harmonycloud.service.application.AppLogService;
import com.harmonycloud.service.application.ApplicationTemplateService;
import com.harmonycloud.service.application.ServiceService;
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

import static com.harmonycloud.common.Constant.CommonConstant.NUM_SIZE_MEMORY;
import static com.harmonycloud.common.Constant.CommonConstant.PERCENT_HUNDRED;
import static com.harmonycloud.service.platform.constant.Constant.NAMESPACE_SYSTEM;


/**
 * Created by hongjie
 */
@Service
public class ClusterServiceImpl implements ClusterService {

    private static final Logger LOG = LoggerFactory.getLogger(ClusterServiceImpl.class);
    private static final String MODULE_MONITOR = "monitor";

    @Autowired
    NodeService nodeService;
    @Autowired
    private TenantService tenantService;
    @Autowired
    DashboardService dashboardService;
    @Autowired
    InfluxdbService influxdbService;
    @Autowired
    com.harmonycloud.service.platform.service.NodeService platformNodeService;
    @Autowired
    NamespaceService namespaceService;
    @Autowired
    ClusterCRDService clusterCRDService;
    @Autowired
    UserService userService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    @Autowired
    NamespaceLocalService namespaceLocalService;
    @Autowired
    TenantClusterQuotaService tenantClusterQuotaService;
    @Autowired
    PodService podService;
    @Autowired
    ApplicationTemplateService applicationTemplateService;
    @Autowired
    ServiceService serviceService;
    @Autowired
    BuildEnvironmentService buildEnvironmentService;
    @Autowired
    AppLogService appLogService;
    @Autowired
    ConfigCenterService configCenterService;
    @Autowired
    DockerFileService dockerFileService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    JobService jobService;
    @Autowired
    MicroServiceInstanceService microServiceInstanceService;
    @Autowired
    NodePortClusterUsageService nodePortClusterUsageService;
    @Autowired
    RoleLocalService roleLocalService;


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
            throw new IllegalArgumentException(ErrorCodeMessage.CLUSTER_NOT_FOUND.phrase());
        }
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
    public List<Cluster> listAllCluster(Boolean isEnable) throws Exception {
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
    public ActionReturnUtil getClusterResourceUsage(String clusterId, String nodename) throws Exception{
        List<Cluster> listCluster = new ArrayList<>();
        if(clusterId != null) {
            Cluster cluster = this.findClusterById(clusterId);
            if(cluster == null){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            if(StringUtils.isNotBlank(nodename)){
                String hostname = platformNodeService.gethostname(nodename, cluster);
                if (hostname !=null ){
                    nodename = hostname;
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
                if(null != nodename && !"".equals(nodename)) {
                    allocatableMap = this.dashboardService.getNodeInfo(cluster, nodename);
                } else {
                    allocatableMap = this.dashboardService.getInfraInfoWorkNode(cluster);
                }

                clusterCpuCapacity = Double.parseDouble(allocatableMap.get("cpu").toString()) ;
                clusterMemoryCapacity = Double.parseDouble(allocatableMap.get("memoryGb").toString()) ;
                List<String> notWorkNodeList = nodeService.listNotWorkNode(cluster);
                clusterCpuUsage = this.influxdbService.getClusterResourceUsage("node", "cpu/usage_rate","nodename", cluster, notWorkNodeList, nodename);
                clusterCpuUsage = (double) clusterCpuUsage / 1000;
                clusterCpuUsage = new BigDecimal(clusterCpuUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterCpuUsageRate = 0;
                if(clusterCpuCapacity > 0) {
                    clusterCpuUsageRate = (double) clusterCpuUsage / clusterCpuCapacity;
                    clusterCpuUsageRate = new BigDecimal(clusterCpuUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                clusterMemoryUsage =  this.influxdbService.getClusterResourceUsage("node", "memory/working_set", "nodename",cluster, notWorkNodeList, nodename);
                clusterMemoryUsage = (double)clusterMemoryUsage / 1024 /1024/1024;
                clusterMemoryUsage = new BigDecimal(clusterMemoryUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterMemoryUsageRate = 0;
                if(clusterMemoryCapacity > 0) {
                    clusterMemoryUsageRate = (double) clusterMemoryUsage / clusterMemoryCapacity;
                    clusterMemoryUsageRate = new BigDecimal(clusterMemoryUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                clusterFilesystemUsage =  this.influxdbService.getClusterResourceUsage("node", "filesystem/usage", "nodename,resource_id",cluster, notWorkNodeList, nodename);
                clusterFilesystemUsage = (double)clusterFilesystemUsage/1024/1024/1024;
                clusterFilesystemUsage = new BigDecimal(clusterFilesystemUsage).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
                double clusterFilesystemUsageRate = 0;
                clusterFilesystemCapacity =  this.influxdbService.getClusterResourceUsage("node", "filesystem/limit", "nodename,resource_id",cluster, notWorkNodeList,nodename);
                if(clusterFilesystemCapacity >0) {
                    clusterFilesystemCapacity = (double) clusterFilesystemCapacity / 1024 / 1024 / 1024;
                    clusterFilesystemCapacity = new BigDecimal(clusterFilesystemCapacity).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
                    clusterFilesystemUsageRate = (double) clusterFilesystemUsage / clusterFilesystemCapacity;
                    clusterFilesystemUsageRate = new BigDecimal(clusterFilesystemUsageRate).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                }
                double clusterVolumeCapacity = 0;
                for (ClusterStorage storage : cluster.getStorages()) {
                    if (StringUtils.isNoneEmpty(storage.getCapacity())) {
                        clusterVolumeCapacity += Double.parseDouble(storage.getCapacity());
                    }
                }
//                默认单位为TB，转为GB
                clusterVolumeCapacity *= 1024;
                double clusterVolumeUsage = 0;
                double clusterVolumeUsageRateValue = 0;
                if(clusterVolumeCapacity > 0) {
                    clusterVolumeUsage = this.influxdbService.getClusterResourceUsage("pvc", "volume/usage", "", cluster, notWorkNodeList, nodename);
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
                mapClusterResourceUsage.put(cluster.getId(), map);
            }

        }

        return ActionReturnUtil.returnSuccessWithData(mapClusterResourceUsage);
    }


    /**
     * 获取集群domain
     *
     * @return
     */
    @Override
    public List<ClusterDomain> findDomain(String namespace) throws Exception {
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
    public Map<String, Object> getClusterComponentStatus(String clusterId) throws Exception{
        Cluster cluster = this.findClusterById(clusterId);
        List<PodDto> podDtoList = podService.getPodListByNamespace(cluster, NAMESPACE_SYSTEM);
        Map<String, Object> map = new HashMap<>();
        map.put(K8sModuleEnum.KUBE_APISERVER.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.KUBE_CONTROLLER_MANAGER.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.KUBE_SCHEDULER.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.ETCD.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.ELASTICSEARCH.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.CALICO.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.KUBE_DNS.getCode(), Constant.STATUS_NORMAL);
        map.put(K8sModuleEnum.SERVICE_LOADBALANCER.getCode(), Constant.STATUS_NORMAL);
        map.put(MODULE_MONITOR, Constant.STATUS_NORMAL);

        Map<String, K8sModuleEnum> moduleEnumMap = K8sModuleEnum.getModuleMap();
        int abnormalCount = 0;
        boolean heapsterCreated = false;
        boolean influxdbCreated = false;
        Set<String> createdComponent = new HashSet<>();
        for (PodDto pod : podDtoList) {

            for (Map.Entry<String, K8sModuleEnum> entry : moduleEnumMap.entrySet()) {
                if (pod.getName().contains(entry.getValue().getCode())) {
                    //设置已经创建的组件列表
                    if(entry.getValue() == K8sModuleEnum.HEAPSTER ){
                        heapsterCreated = true;
                        if(heapsterCreated && influxdbCreated) {
                            createdComponent.add(MODULE_MONITOR);
                        }
                    }else if(entry.getValue() == K8sModuleEnum.INFLUXDB){
                        influxdbCreated = true;
                        if(heapsterCreated && influxdbCreated) {
                            createdComponent.add(MODULE_MONITOR);
                        }
                    }else{
                        createdComponent.add(entry.getValue().getCode());
                    }
                    if (pod.getStatus().equalsIgnoreCase(Constant.RUNNING)) {
                        continue;
                    }
                    //设置状态为异常的组件，如果一个组件有多个pod，如calico，每个节点一个pod，如果其中一个pod状态不正常，则该组件状态为异常
                    if(entry.getValue() == K8sModuleEnum.HEAPSTER || entry.getValue() == K8sModuleEnum.INFLUXDB){
                        map.put(MODULE_MONITOR, Constant.STATUS_ABNORMAL);
                    }else{
                        map.put(entry.getValue().getCode(), Constant.STATUS_ABNORMAL);
                    }
                    abnormalCount ++;
                }
            }
        }
        //如果组件没有创建，状态修改为异常
        for (String componentCode : map.keySet()) {
            if(!createdComponent.contains(componentCode)){
                map.put(componentCode, Constant.STATUS_ABNORMAL);
                abnormalCount ++;
            }
        }
        double totalCount = map.size();
        String health = String.format("%.0f", (totalCount - abnormalCount) / totalCount * PERCENT_HUNDRED);
        Map<String, Object> resultMap = new HashMap<>();
        map.forEach((k, v) -> {
            resultMap.put(k.replaceAll(CommonConstant.LINE, ""), v);
        });
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
    public Map<String, String> getClustersStorageCapacity(){
        List<Cluster> clusters = clusterCacheManager.listCluster();
        Map<String, String> clustersMap = new HashMap<>();
        for (Cluster cluster: clusters) {
            Double capacity = 0.0;
            for(ClusterStorage storage: cluster.getStorages()){
                if(storage.getCapacity() != null) {
                    capacity = capacity + Double.parseDouble(storage.getCapacity()) * 1024;
                }
            }
            clustersMap.put(cluster.getId(),capacity.toString()+"GB");
        }
        return clustersMap;
    }

}

