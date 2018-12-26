package com.harmonycloud.service.cache;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.enumm.ComponentServiceTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.user.bean.Role;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.dto.cluster.DataCenterDto;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.k8s.util.DefaultClient;
import com.harmonycloud.service.application.DataCenterService;
import com.harmonycloud.service.cluster.ClusterCRDService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.NUM_ONE;
import static com.harmonycloud.common.Constant.CommonConstant.PROTOCOL_HTTP;
import static com.harmonycloud.k8s.constant.Constant.ES_CLUSTER_NAME;

/**
 * cluster集群信息redis管理
 */
@Component
public class ClusterCacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterCacheManager.class);
    private static final int ES_DEFAULT_PORT = 30093;
    private static final int INFLUXDB_DEFAULT_PORT = 30018;
    private static final int PRIVILEGE_TIMEOUT = 1800;
    private static final String REDIS_KEY_CLUSTER = "cluster";
    private static final String REDIS_KEY_PRIVILEGE = "privilege";
    private static final String REDIS_KEY_USER = "userStatus";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ClusterCRDService clusterCRDService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleLocalService roleLocalService;
    @Autowired
    private DataCenterService dataCenterService;
    //容器云平台部署的集群，上层集群
    private static Cluster platformCluster;
    @Value("${public.key:}")
    private String publicKey;

    /**
     * 初始化获取所有集群列表
     */
    @PostConstruct
    public Map<String, Cluster> initClusterCache() throws MarsRuntimeException {
        LOGGER.info("初始化cluster集群信息");
        Map<String, Cluster> clusters = new HashMap<>();
        try {
            ActionReturnUtil clusterResponse = clusterCRDService.listClusters(null,null);
            if (!clusterResponse.isSuccess() || clusterResponse.get("data") == null) {
                LOGGER.error("获取集群tpr列表错误,response:{}", JSONObject.toJSONString(clusterResponse));
                return Collections.emptyMap();
            }
            List<ClusterCRDDto> clusterTPRDtos = (List<ClusterCRDDto>) clusterResponse.get("data");
            ActionReturnUtil dataCenterResponse = dataCenterService.listDataCenter(false,null);
            if (!dataCenterResponse.isSuccess() || dataCenterResponse.get("data") == null) {
                LOGGER.error("获取数据中心列表错误,response:{}", JSONObject.toJSONString(dataCenterResponse));
                return Collections.emptyMap();
            }
            List<DataCenterDto> dataCenters = (List)dataCenterResponse.getData();
            LOGGER.info("初始化cluster集群信息,集群数量：{}", clusterTPRDtos.size());
            List<Cluster> listClusters = this.convertCluster(clusterTPRDtos, dataCenters);
            if (CollectionUtils.isEmpty(listClusters)) {
                LOGGER.warn("获取集群列表为空");
                return Collections.emptyMap();
            }
            //创建map的存储，方便getById
            for (Cluster cluster : listClusters) {
                if (!Objects.isNull(cluster.getLevel()) && cluster.getLevel().equals(ClusterLevelEnum.PLATFORM.getLevel())) {
                    cluster.setIsEnable(Boolean.TRUE);
                    platformCluster = cluster;
                } else {
                    clusters.put(cluster.getId(), cluster);
                }
            }
            if(platformCluster == null){
                platformCluster = DefaultClient.getDefaultCluster();
            }
        }catch(Exception e){
            LOGGER.error("初始化集群列表失败,",e);
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_DATA_INIT_ERROR);
        }
        try{
            //将初始化的集群信息存入redis缓存
            this.resetCluster(clusters);
        }catch (Exception e){
            LOGGER.error("集群信息列表存入redis缓存失败,",e);
        }
        return clusters;
    }

    /**
     * 获取单个集群对象
     * @param clusterId 集群id
     * @return
     */
    public Cluster getCluster(String clusterId){
        Assert.hasText(clusterId);
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        String clusterJson = clusterHashOps.get(clusterId);
        if(StringUtils.isNotBlank(clusterJson)){
            Cluster cluster = JSONObject.parseObject(clusterJson, Cluster.class);
            return cluster;
        }
        LOGGER.warn("redis缓存未找到集群信息，clusterId：{}",clusterId);
        Map<String, Cluster> clusters = initClusterCache();
        return clusters.get(clusterId);


    }

    /**
     * 根据角色id获取权限更新状态
     * @param roleId
     * @return
     */
    public Boolean getRolePrivilegeStatus(Integer roleId,String userName) throws Exception{
        if(Objects.isNull(roleId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Role role = this.roleLocalService.getRoleById(roleId);
        if (!role.getAvailable()){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
        }
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_PRIVILEGE);
        String privilegeStatus = null;
        if (StringUtils.isNotBlank(userName)){
            privilegeStatus = clusterHashOps.get(role.getName() + userName);
        } else {
            privilegeStatus = clusterHashOps.get(role.getName());
        }
        Boolean status = Boolean.valueOf(privilegeStatus);
        return status;
    }

    /**
     * 获取用户的状态
     * @param roleId
     * @param userName
     * @param tenantId
     * @param projectId
     * @return
     * @throws Exception
     */
    public Boolean getRolePrivilegeStatusForTenantOrProject(Integer roleId,String userName,String tenantId,String projectId) throws Exception{
        if(Objects.isNull(roleId) && StringUtils.isBlank(userName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Role role = this.roleLocalService.getRoleById(roleId);
        if (!role.getAvailable()){
            throw new MarsRuntimeException(ErrorCodeMessage.ROLE_DISABLE);
        }
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_PRIVILEGE);
        String privilegeStatus = null;
        if (StringUtils.isNotBlank(tenantId)){
            privilegeStatus = clusterHashOps.get(role.getName() + userName + tenantId);
        } else {
            privilegeStatus = clusterHashOps.get(role.getName() + userName + projectId);
        }
        Boolean status = Boolean.valueOf(privilegeStatus);
        return status;
    }
    public void updateRolePrivilegeStatusForTenantOrProject(Integer roleId,String userName,String tenantId,String projectId,Boolean status) throws Exception{
        if(Objects.isNull(roleId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Role role = this.roleLocalService.getRoleById(roleId);
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_PRIVILEGE);
        if (StringUtils.isNotBlank(tenantId)){
            clusterHashOps.put(role.getName() + userName + tenantId, status.toString());
        } else {
            clusterHashOps.put(role.getName() + userName + projectId, status.toString());
        }
        clusterHashOps.expire(PRIVILEGE_TIMEOUT, TimeUnit.SECONDS);

    }
    /**
     * 更新角色权限的状态
     * @param roleId
     * @param userName
     * @param status
     * @throws Exception
     */
    public void updateRolePrivilegeStatus(Integer roleId,String userName,Boolean status) throws Exception{
        if(Objects.isNull(roleId)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Role role = this.roleLocalService.getRoleById(roleId);
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_PRIVILEGE);
        if (StringUtils.isNotBlank(userName)){
            clusterHashOps.put(role.getName() + userName, status.toString());
        } else {
            clusterHashOps.put(role.getName(), status.toString());
        }
        clusterHashOps.expire(PRIVILEGE_TIMEOUT, TimeUnit.SECONDS);

    }
    /**
     * 根据用户名获取用户的状态
     * @param userName
     * @return
     */
    public Boolean getUserStatus(String userName) throws Exception{
        if(StringUtils.isBlank(userName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_USER);
        String privilegeStatus = clusterHashOps.get(userName);
        Boolean status = Boolean.valueOf(privilegeStatus);
        return status;
    }

    /**
     * 根据用户名更新用户的状态
     * @param userName
     * @param status
     * @throws Exception
     */
    public void updateUserStatus(String userName,Boolean status) throws Exception{
        if(StringUtils.isBlank(userName)){
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_USER);
        clusterHashOps.put(userName, status.toString());
        clusterHashOps.expire(PRIVILEGE_TIMEOUT, TimeUnit.SECONDS);

    }
    /**
     * 获取所有集群列表
     * @return 所有集群的列表
     */
    public List<Cluster> listCluster(){
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        Map<String, String> maps = clusterHashOps.entries();
        if(CollectionUtils.isEmpty(maps)){
            LOGGER.warn("redis查询集群列表为空,调k8s apiserver获取集群信息");
            Map<String, Cluster> clusters = initClusterCache();
            return clusters.values().stream().collect(Collectors.toList());
        }
        Set<Cluster> clusters = new TreeSet<>();
        for (String clusterId : maps.keySet()) {
            clusters.add(JSONObject.parseObject(maps.get(clusterId), Cluster.class));
        }
        return new ArrayList<>(clusters);
    }

    /**
     * 增加或更新单个集群信息
     * @param cluster 集群信息
     */
    public void putCluster(Cluster cluster){
        LOGGER.info("更新cluster缓存，cluster：{}",JSONObject.toJSONString(cluster));
        Assert.notNull(cluster);
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        String clusterJson = JSONObject.toJSONString(cluster);
        clusterHashOps.put(cluster.getId(), clusterJson);
    }

    /**
     * 增加或更新map包含的集群信息
     * @param clusterMap 需要更新的集群信息
     */
    public void putCluster(Map<String,Cluster> clusterMap){
        if(CollectionUtils.isEmpty(clusterMap)){
            return;
        }
        LOGGER.info("更新cluster缓存，size：{}",clusterMap.size());
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        Map<String,String> clusters = new HashMap<>(clusterMap.size());
        for(String clusterId : clusterMap.keySet()){
            clusters.put(clusterId, JSONObject.toJSONString(clusterMap.get(clusterId)));
        }
        clusterHashOps.putAll(clusters);
        //缓存有效期1天，每24小时会重置更新缓存
        clusterHashOps.expireAt(DateUtil.addDay(new Date(),NUM_ONE));
    }

    /**
     * 删除一个集群
     * @param clusterId 集群id
     */
    public void removeCluster(String clusterId){
        LOGGER.info("删除cluster缓存，clusterId：{}",clusterId);
        Assert.hasText(clusterId);
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        clusterHashOps.delete(clusterId);
    }

    public Cluster getPlatformCluster(){
        if(platformCluster == null){
            initClusterCache();
        }
        return platformCluster;
    }

    /**
     * 重置redis缓存的集群信息，先删除再重新创建
     * @param clusterMap 需要更新的集群信息
     */
    private void resetCluster(Map<String,Cluster> clusterMap){
        LOGGER.info("重置cluster缓存，size：{}",clusterMap.size());
        BoundHashOperations<String, String, String> clusterHashOps = stringRedisTemplate
                .boundHashOps(REDIS_KEY_CLUSTER);
        Set<String> existClusterIds = clusterHashOps.keys();
        Set<String> newClusterIds = clusterMap.keySet();
        //老的集群clusterId不在新的集群列表中，既该集群已经被删除，则将redis中该集群信息删除
        for(String clusterId : existClusterIds){
            if(!newClusterIds.contains(clusterId)){
                clusterHashOps.delete(REDIS_KEY_CLUSTER, clusterId);
            }
        }
        //更新最新集群信息到redis
        putCluster(clusterMap);
    }

    /**
     * 将tpr结构的集群对象转换成业务结构对象
     * @param clusterCRDDtos
     * @return
     */
    private List<Cluster> convertCluster(List<ClusterCRDDto> clusterCRDDtos, List<DataCenterDto> dataCenters) throws Exception{
        Map<String,String> dataCenterMap = dataCenters.stream().collect(Collectors.toMap(DataCenterDto::getName, dataCenter -> dataCenter.getAnnotations()));
        List<Cluster> clusters = new ArrayList<>();
        // 获取每个harbor被哪些集群共用
        List<Map<String,String>> referredClusters = this.getHarborReferredClusters(clusterCRDDtos);
        Map<String,String> harborClusterIds = referredClusters.get(0);
        Map<String,String> harborClusterNames = referredClusters.get(1);
        Map<String,String> harborClusterAliasNames = referredClusters.get(CommonConstant.NUM_TWO);
        for(ClusterCRDDto clusterTPRDto : clusterCRDDtos){
            Cluster cluster = new Cluster();
            cluster.setId(clusterTPRDto.getUid());
            cluster.setDataCenter(clusterTPRDto.getDataCenter());
            cluster.setDataCenterName(dataCenterMap.get(cluster.getDataCenter()));
            cluster.setName(clusterTPRDto.getName());
            cluster.setAliasName(clusterTPRDto.getNickname());
            cluster.setHost(clusterTPRDto.getK8sAddress());
            cluster.setPort(clusterTPRDto.getPort() == null? CommonConstant.DEFAULT_KUBE_APISERVER_PORT:clusterTPRDto.getPort());
            cluster.setProtocol(StringUtils.isBlank(clusterTPRDto.getProtocol())?CommonConstant.PROTOCOL_HTTPS:clusterTPRDto.getProtocol());
            cluster.setMachineToken(userService.getMachineToken());

            cluster.setLevel(clusterTPRDto.getEnvLabel());

            cluster.setDomains(clusterTPRDto.getDomain());
            cluster.setStorages(clusterTPRDto.getNfs());
            cluster.setMysql(clusterTPRDto.getMysql());
            cluster.setJenkins(clusterTPRDto.getJenkins());
            cluster.setNetwork(clusterTPRDto.getNetwork());
            cluster.setRedis(clusterTPRDto.getRedis());
            cluster.setClusterComponent(clusterTPRDto.getTemplate());
            cluster.setCreateTime(clusterTPRDto.getCreateTime());
            cluster.setIsEnable(clusterTPRDto.getIsEnable());
            cluster.setGitInfo(clusterTPRDto.getGitInfo());

            List<ClusterTemplate> clusterTemplates = clusterTPRDto.getTemplate();
            for(ClusterTemplate clusterTemplate : clusterTemplates){
                if(ComponentServiceTypeEnum.INFLUXDB.getName().equalsIgnoreCase(clusterTemplate.getType())){
                    cluster.setInfluxdbUrl(HttpClientUtil.getHttpUrl(PROTOCOL_HTTP ,cluster.getHost(), INFLUXDB_DEFAULT_PORT) + "/query");
                    cluster.setInfluxdbDb(Constant.INFLUXDB_DB_NAME);
                }
            }
            HarborServer harborServer = new HarborServer();
            harborServer.setHarborProtocol(StringUtils.isBlank(clusterTPRDto.getHarborProtocol())? PROTOCOL_HTTP:clusterTPRDto.getHarborProtocol());
            harborServer.setHarborPort(clusterTPRDto.getHarborPort() == null?CommonConstant.DEFAULT_HARBOR_PORT:clusterTPRDto.getHarborPort());
            harborServer.setHarborHost(clusterTPRDto.getHarborAddress());
            harborServer.setHarborAdminAccount(clusterTPRDto.getHarborAdminUser());
            harborServer.setHarborAdminPassword(StringUtils.isBlank(publicKey) ? clusterTPRDto.getHarborAdminPwd() : ConfigTools.decrypt(publicKey,
                    clusterTPRDto.getHarborAdminPwd()));
            //harbor登录cookie 15分钟内有效
            harborServer.setHarborLoginTimeOut(Constant.HARBOR_LOGIN_TIMEOUT);
            harborServer.setReferredClusterNames(harborClusterNames.get(harborServer.getHarborHost()));
            harborServer.setReferredClusterIds(harborClusterIds.get(harborServer.getHarborHost()));
            harborServer.setCreateTime(clusterTPRDto.getCreateTime());
            //es 可配置集群地址
            if(clusterTPRDto.getElasticsearch() != null){
                ElasticsearchConnect elasticsearch = clusterTPRDto.getElasticsearch();
                cluster.setEsHost(StringUtils.isBlank(elasticsearch.getHost()) ? cluster.getHost():elasticsearch.getHost());
                cluster.setEsPort(elasticsearch.getPort() == null ? ES_DEFAULT_PORT:elasticsearch.getPort());
                cluster.setEsClusterName(StringUtils.isBlank(elasticsearch.getName()) ? ES_CLUSTER_NAME:elasticsearch.getName());
            }else {
                cluster.setEsHost(cluster.getHost());
                cluster.setEsPort(ES_DEFAULT_PORT);
                cluster.setEsClusterName(ES_CLUSTER_NAME);
            }
            harborServer.setReferredClusterAliasNames(harborClusterAliasNames.get(harborServer.getHarborHost()));
            cluster.setHarborServer(harborServer);
            cluster.setExternal(clusterTPRDto.getExternal());
            clusters.add(cluster);

        }
        return clusters;
    }

    private Integer getServiceApiPort(List<ServicePort> servicePorts) throws MarsRuntimeException{
        for(ServicePort servicePort : servicePorts){
            if(Constant.SERVICE_TYPE_API.equalsIgnoreCase(servicePort.getType())){
                return servicePort.getPort();
            }
        }
        throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_SERVICE_PORT_ERROR);
    }

    /**
     * 获取每个harbor被哪些集群共用
     * @param clusterTPRDtos
     * @return
     */
    private List<Map<String,String>> getHarborReferredClusters(List<ClusterCRDDto> clusterTPRDtos){
        //每个harbor对应的集群列表，key为harborAddress，
        Map<String, List<ClusterCRDDto>> harborAddressMap = clusterTPRDtos.stream()
                .collect(Collectors.groupingBy(ClusterCRDDto::getHarborAddress));
        Map<String,String> harborClusterNames = new HashMap<>();
        Map<String,String> harborClusterIds = new HashMap<>();
        Map<String,String> harborClusterAliasNames = new HashMap<>();
        for(Map.Entry<String, List<ClusterCRDDto>> entry: harborAddressMap.entrySet()){
            String clusterName = "";
            String clusterId = "";
            String clusterAliasName = "";
            for(ClusterCRDDto clusterTPRDto : entry.getValue()){
                clusterName += clusterTPRDto.getName() + CommonConstant.COMMA;
                clusterId += clusterTPRDto.getUid() + CommonConstant.COMMA;
                clusterAliasName += clusterTPRDto.getNickname() + CommonConstant.COMMA;
            }
            harborClusterNames.put(entry.getKey(), clusterName.substring(0,clusterName.length()-1));
            harborClusterIds.put(entry.getKey(), clusterId.substring(0,clusterId.length()-1));
            harborClusterAliasNames.put(entry.getKey(), clusterAliasName.substring(0,clusterAliasName.length()-1));
        }
        List<Map<String,String>> referredClusters = new ArrayList<>();
        referredClusters.add(harborClusterIds);
        referredClusters.add(harborClusterNames);
        referredClusters.add(harborClusterAliasNames);
        return referredClusters;
    }

}
