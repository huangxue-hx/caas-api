package com.harmonycloud.service.cluster.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ClusterLevelEnum;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.cluster.AddClusterDto;
import com.harmonycloud.dto.cluster.ClusterCRDDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.*;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.service.ClusterTemplateCRDService;
import com.harmonycloud.k8s.util.DefaultClient;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.k8sUtil;
import com.harmonycloud.service.application.SecretService;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.cluster.ClusterCRDService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.ClusterTemplateService;
import com.harmonycloud.service.application.DataCenterService;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class ClusterCRDServiceImpl implements ClusterCRDService {

    private static final String TEMPLATE   = "template";
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterCRDServiceImpl.class);

//    @Autowired
    private com.harmonycloud.k8s.service.ClusterCRDService clusterCRDService = new com.harmonycloud.k8s.service.ClusterCRDService();
    @Autowired
    ClusterTemplateService clusterTemplateService ;

    private ClusterTemplateCRDService clusterTemplaeCRDService = new ClusterTemplateCRDService();
    private static Logger logger = LoggerFactory.getLogger(ClusterCRDServiceImpl.class);

    @Autowired
    DataCenterService dataCenterService;
    @Autowired
    ClusterCacheManager clusterCacheManager;
    @Autowired
    TenantService tenantService;
    @Autowired
    HarborProjectService harborProjectService;
    @Autowired
    UserService userService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    SecretService secretService;

    @Override
    public ActionReturnUtil getCluster(String dataCenter, String name) throws Exception {
        Cluster cluster = DefaultClient.getDefaultCluster();
        K8SClientResponse response = clusterCRDService.getCluster(dataCenter, name ,cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
        ClusterCRD clusterCRD = JsonUtil.jsonToPojo(response.getBody(), ClusterCRD.class);
        ClusterCRDDto clusterCRDDto = this.convertDto(clusterCRD);
        return ActionReturnUtil.returnSuccessWithData(clusterCRDDto);
    }

    @Override
    public ActionReturnUtil listClusters(String template,String dataCenter) throws Exception {
        LOGGER.info("list clusters, template:{},dataCenter:{}",template,dataCenter);
        Cluster cluster = DefaultClient.getDefaultCluster();
        String newTemplate ;
        if (StringUtils.isNotBlank(template)) {
            newTemplate = TEMPLATE+"="+template;
        } else {
            newTemplate = template;
        }
        ClusterCRDList clusterCRDList = clusterCRDService.listCluster(newTemplate,dataCenter,cluster);
        if(clusterCRDList == null){
            LOGGER.warn("list clusters, clusterTPRList null.");
            return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
        }
        if (clusterCRDList.getItems() != null && clusterCRDList.getItems().isEmpty()) {
            logger.info("list clusters  null");
            return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
        }

        List<ClusterCRDDto> clusterCRDDtoList = new ArrayList<>();
        //初步排除异常对象
        for (ClusterCRD clusterCRD : clusterCRDList.getItems()){
            if (Objects.isNull(clusterCRD.getSpec()) || Objects.isNull(clusterCRD.getSpec().getInfo())){
                continue;
            }
            clusterCRDDtoList.add(this.convertDto(clusterCRD));
        }
        return ActionReturnUtil.returnSuccessWithData(clusterCRDDtoList);
    }


    @Override
    public ActionReturnUtil deleteCluster(String dataCenter, String clusterId, Boolean deleteData) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (null == cluster) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        if(deleteData == null || deleteData) {
            clusterService.deleteClusterData(clusterId);
        }
        Cluster platformCluster = DefaultClient.getDefaultCluster();
        //查询要删除的集群是否存在
        K8SClientResponse responseFind = clusterCRDService.getCluster(dataCenter, cluster.getName() ,platformCluster);
        if (!HttpStatusUtil.isSuccessStatus(responseFind.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(responseFind.getBody());
        }
        ClusterCRD clusterResult = JsonUtil.jsonToPojo(responseFind.getBody(), ClusterCRD.class);
        if (Objects.isNull(clusterResult)){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SClientResponse response = clusterCRDService.deleteCluster(dataCenter, cluster.getName(), platformCluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            //更新集群需要重新初始化集群信息，并同时更新redis缓存
            clusterCacheManager.initClusterCache();
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }

    public Cluster buildCluster(AddClusterDto clusterCRDDto) {
        Cluster cluster = new Cluster();
        cluster.setHost(clusterCRDDto.getHost());
        cluster.setPort(clusterCRDDto.getPort());
        cluster.setMachineToken(userService.getMachineToken());
        cluster.setProtocol("https");
        return cluster;
    }

    @Override
    public ActionReturnUtil addCluster(AddClusterDto clusterCRDDto) throws Exception {
        // 验证数据是否异常
        if (!checkAddDto(clusterCRDDto)){
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_BODY_DATA_ERROR);
        }
        List<Cluster> clusters = clusterCacheManager.listCluster();
        for(Cluster cluster : clusters){
            if(cluster.getHost().equalsIgnoreCase(clusterCRDDto.getHost())){
                throw new MarsRuntimeException(DictEnum.CLUSTER.phrase()
                        + " " + clusterCRDDto.getHost(), ErrorCodeMessage.EXIST);
            }
            if(cluster.getName().equalsIgnoreCase(clusterCRDDto.getName())){
                throw new MarsRuntimeException(DictEnum.CLUSTER.phrase()
                        + " " + clusterCRDDto.getName(), ErrorCodeMessage.EXIST);
            }
        }
        Cluster cluster = DefaultClient.getDefaultCluster();
        String name = ClusterLevelEnum.values()[clusterCRDDto.getTemplate()].name();
        K8SClientResponse tResponse  = clusterTemplaeCRDService.getClusterTemplate("cluster-top",name.toLowerCase(),cluster);
        if (!HttpStatusUtil.isSuccessStatus(tResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(tResponse.getBody());
        }
        Cluster newCluster = buildCluster(clusterCRDDto);
        K8SClientResponse baseResponse = clusterCRDService.getClusterBase(newCluster);
        if (!HttpStatusUtil.isSuccessStatus(baseResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_CONNECT_ERROR);
        }

        ClusterBaseCRD clusterbase = JsonUtil.jsonToPojo(baseResponse.getBody(), ClusterBaseCRD.class);

        ClusterCRD clusterCRD = new ClusterCRD();
        clusterCRD = this.resourceObject(clusterCRD);
        ObjectMeta meta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(TEMPLATE, ClusterLevelEnum.values()[clusterCRDDto.getTemplate()].name().toLowerCase());
        Map<String, Object> annos = new HashMap<>();
        annos.put("name",clusterCRDDto.getNickname());
        meta.setName(clusterCRDDto.getName());
        meta.setNamespace(clusterCRDDto.getDatacenter());
        meta.setLabels(labels);
        meta.setAnnotations(annos);
        clusterCRD.setMetadata(meta);
        ClusterSpec clusterSpec = new ClusterSpec();
        clusterSpec.setInfo(clusterbase.getSpec());
        Template template = JsonUtil.jsonToPojo(tResponse.getBody(), Template.class);
        clusterSpec.setTemplate(template.getTemplateSpec());
        clusterCRD.setSpec(clusterSpec);
        ClusterStatus status = new ClusterStatus();
        StatusConditions conditions = new StatusConditions();
        conditions.setStatus(false);
        conditions.setType("Ready");
        List<StatusConditions> statusConditionsList = new ArrayList<StatusConditions>();
        statusConditionsList.add(conditions);
        status.setConditions(statusConditionsList);
        clusterCRD.setStatus(status);
//        List<HarborProject> harborProjects= harborService.listProject(harborServer.getHarborHost(),null,null);
        K8SClientResponse response = clusterCRDService.addCluster(clusterCRD, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            //更新集群需要重新初始化集群信息，并同时更新redis缓存
            clusterCacheManager.initClusterCache();
            ClusterCRD clusterResult = K8SClient.converToBean(response, ClusterCRD.class);
            //处理新集群在租户下配额关系
            if (!Objects.isNull(clusterResult.getMetadata())){
                this.tenantService.dealQuotaWithNewCluster(k8sUtil.GetNamespaceName(clusterResult.getMetadata()));
            }
            //将集群使用的harbor的公共镜像仓库添加到数据库
            harborProjectService.getPublicHarborProject(clusterCRD.getSpec().getInfo().getHarbor().getAddress());
            //添加新集群为已有的项目创建 镜像仓库
            harborProjectService.addClusterHarborProject(k8sUtil.GetNamespaceName(clusterCRD.getMetadata()));
            //添加harbor secret供构建环境镜像拉取
            secretService.createHarborSecret(clusterCRDDto.getName(), CommonConstant.CICD_NAMESPACE,
                    clusterCRD.getSpec().getInfo().getHarbor().getAddress(),clusterCRD.getSpec().getInfo().getHarbor().getUser(),clusterCRD.getSpec().getInfo().getHarbor().getPassword());
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }

    @Override
    public ActionReturnUtil updateClusterStatus(Cluster cluster, boolean status, String type)  throws Exception {
        Cluster topCluster = DefaultClient.getDefaultCluster();
        K8SClientResponse getResponse = clusterCRDService.getCluster(cluster.getDataCenter(), cluster.getName() ,topCluster);
        ClusterCRD nowClusterCDR;
        if (HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
            nowClusterCDR = JsonUtil.jsonToPojo(getResponse.getBody(), ClusterCRD.class);
        }else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        ClusterStatus clusterStatus = nowClusterCDR.getStatus();
        List<StatusConditions>  conditionsList = new ArrayList<StatusConditions>();
        StatusConditions conditions = new StatusConditions();
        conditions.setStatus(status);
        conditions.setType("Ready");
        conditionsList.add(conditions);
        clusterStatus.setConditions(conditionsList);
        nowClusterCDR.setStatus(clusterStatus);

        K8SClientResponse response = clusterCRDService.updateCluster(nowClusterCDR, topCluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            //更新集群需要重新初始化集群信息，并同时更新redis缓存
            clusterCacheManager.initClusterCache();
            if (!Objects.isNull(nowClusterCDR.getMetadata())){
                if (status) {
                    this.tenantService.dealQuotaWithNormalCluster(k8sUtil.GetNamespaceName(nowClusterCDR.getMetadata()));
                } else {
                    this.tenantService.dealQuotaWithPauseCluster(k8sUtil.GetNamespaceName(nowClusterCDR.getMetadata()));
                }
            }
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }

    }

    @Override
    public ActionReturnUtil updateCluster(String name, ClusterCRDDto clusterCRDDto) throws Exception {
        // 不允许修改集群 name
        if (!name.equals(clusterCRDDto.getName())) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NAME_UNUPDATE);
        }
        // 验证数据是否异常
        if (!checkDto(clusterCRDDto)){
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_BODY_DATA_ERROR);
        }
        if (!checkHarbor(clusterCRDDto)) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.HARBOR_AUTH_FAIL);
        }
        Cluster cluster = DefaultClient.getDefaultCluster();
        ClusterCRD nowClusterCDR;
        K8SClientResponse getResponse = clusterCRDService.getCluster(clusterCRDDto.getDataCenter(), clusterCRDDto.getName() ,cluster);
        if (HttpStatusUtil.isSuccessStatus(getResponse.getStatus())){
            nowClusterCDR = JsonUtil.jsonToPojo(getResponse.getBody(), ClusterCRD.class);
        }else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        ClusterCRD clusterCRD = this.convertCRD(clusterCRDDto);
        if (null != clusterCRD.getStatus()) {

            List<StatusConditions> newConditions = clusterCRD.getStatus().getConditions();
            List<StatusConditions> oldConditions = nowClusterCDR.getStatus().getConditions();
            List<StatusConditions> updateList =  k8sUtil.GetUpdateStatus(newConditions,oldConditions);
            clusterCRD.getStatus().setConditions(updateList);
        } else {
            clusterCRD.setStatus(nowClusterCDR.getStatus());
        }
        clusterCRD.setMetadata(nowClusterCDR.getMetadata());
        K8SClientResponse response = clusterCRDService.updateCluster(clusterCRD, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            //更新集群需要重新初始化集群信息，并同时更新redis缓存
            clusterCacheManager.initClusterCache();
            //将集群使用的harbor的公共镜像仓库添加到数据库
            harborProjectService.getPublicHarborProject(clusterCRD.getSpec().getInfo().getHarbor().getAddress());
            //如果更新了harborHost，则将老的harborHost的公共镜像仓库删除
            harborProjectService.deletePublicHarborProject(null);
            //修改harbor secret供构建环境镜像拉取
            secretService.createHarborSecret(clusterCRDDto.getName(), CommonConstant.CICD_NAMESPACE, clusterCRDDto.getHarborAddress(),clusterCRDDto.getHarborAdminUser(), clusterCRDDto.getHarborAdminPwd());
            return ActionReturnUtil.returnSuccessWithMsg(response.getBody());
        } else {
            return ActionReturnUtil.returnErrorWithMsg(response.getBody());
        }
    }

    private boolean checkAddDto(AddClusterDto clusterDto) throws Exception {
        if (StringUtils.isBlank(clusterDto.getHost()) || clusterDto.getPort() == null ||
            StringUtils.isBlank(clusterDto.getProtocol()) ||
            clusterDto.getTemplate() == null ||
            StringUtils.isBlank(clusterDto.getNickname()) ||
            StringUtils.isBlank(clusterDto.getDatacenter()) ||
            StringUtils.isBlank(clusterDto.getName())){
            return false;
        }
        String name = ClusterLevelEnum.values()[clusterDto.getTemplate()].name();
        ActionReturnUtil response = clusterTemplateService.getClusterTemplate(name.toLowerCase());
        return  response.isSuccess();

    }

    /**
     * 验证body内的数据是否异常
     * @param clusterCRDDto body内数据
     * @return boolean
     * @throws Exception null
     */
    private  boolean checkDto(ClusterCRDDto clusterCRDDto) throws Exception {
        if (StringUtils.isBlank(clusterCRDDto.getK8sAddress())) {
            return false;
        }
        Integer envLabel = clusterCRDDto.getEnvLabel();
        String name = ClusterLevelEnum.values()[envLabel].name();
        //  是否存在对应集群模板
        ActionReturnUtil response = clusterTemplateService.getClusterTemplate(name.toLowerCase());
        return  response.isSuccess();


    }


    private boolean checkHarbor(ClusterCRDDto clusterCRDDto) {
        HarborServer harborServer = new HarborServer();
        harborServer.setHarborAdminAccount(clusterCRDDto.getHarborAdminUser());
        harborServer.setHarborAdminPassword(clusterCRDDto.getHarborAdminPwd());
        harborServer.setHarborHost(clusterCRDDto.getHarborAddress());
        harborServer.setHarborProtocol(clusterCRDDto.getHarborProtocol());
        harborServer.setHarborPort(clusterCRDDto.getHarborPort());
        try {
            String cookie =  HarborClient.loginWithAdmin(harborServer);
            if (cookie != null) {
                return true;
            }
            return false ;
        }catch(Exception e){
            LOGGER.error("验证harbor信息错误，harbor:{}", JSONObject.toJSONString(harborServer), e);
            return false;
        }
    }

    private ClusterCRD  resourceObject (ClusterCRD clusterCRD)  throws Exception {
        clusterCRD.setKind("Cluster");
        clusterCRD.setApiVersion("harmonycloud.cn/v1");
        return clusterCRD;
    }

    /**
     *  把 clusterCRDDto 对象 转换 成 clusterCRD
     * @param clusterCRDDto body data
     * @return ClusterCRD
     * @throws Exception null
     */
    private ClusterCRD convertCRD(ClusterCRDDto clusterCRDDto) throws Exception {
        ClusterCRD clusterCRD = new ClusterCRD();
        clusterCRD = resourceObject(clusterCRD);

        ObjectMeta meta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(TEMPLATE, ClusterLevelEnum.values()[clusterCRDDto.getEnvLabel()].name().toLowerCase());
        Map<String, Object> annos = new HashMap<>();
        annos.put("name", clusterCRDDto.getNickname());
        meta.setName(clusterCRDDto.getName());
        meta.setNamespace(clusterCRDDto.getDataCenter());
        meta.setLabels(labels);
        meta.setAnnotations(annos);
        clusterCRD.setMetadata(meta);
        ClusterSpec spec = new ClusterSpec();
        ClusterInfo info = new ClusterInfo();
        info.setAddress(clusterCRDDto.getK8sAddress());
        info.setPort(clusterCRDDto.getPort());
        info.setProtocol(clusterCRDDto.getProtocol());
        ClusterHarbor harbor = new ClusterHarbor();
        harbor.setAddress(clusterCRDDto.getHarborAddress());
        harbor.setPort(clusterCRDDto.getHarborPort());
        harbor.setProtocol(clusterCRDDto.getHarborProtocol());
        harbor.setUser(clusterCRDDto.getHarborAdminUser());
        harbor.setPassword(clusterCRDDto.getHarborAdminPwd());
        info.setNfs(clusterCRDDto.getNfs());
        info.setHarbor(harbor);
        info.setDomain(clusterCRDDto.getDomain());
        info.setExternal(clusterCRDDto.getExternal());
        info.setMysql(clusterCRDDto.getMysql());
        info.setJenkins(clusterCRDDto.getJenkins());
        info.setNetwork(clusterCRDDto.getNetwork());
        info.setRedis(clusterCRDDto.getRedis());
        spec.setInfo(info);
        spec.setTemplate(clusterCRDDto.getTemplate());
        clusterCRD.setSpec(spec);
        ClusterStatus status = new ClusterStatus();
        List<StatusConditions>  conditionsList = new ArrayList<StatusConditions>();
        StatusConditions conditions = new StatusConditions();
        conditions.setType("Ready");
        conditions.setStatus(clusterCRDDto.getIsEnable());
        conditionsList.add(conditions);
        status.setConditions(conditionsList);
        clusterCRD.setStatus(status);
        return clusterCRD;
    }

    /**
     * clusterCRD  转换成  ClusterCRDDto
     * @param clusterCRD   data from k8s
     * @return ClusterCRDDto
     * @throws Exception null
     */
    private ClusterCRDDto convertDto(ClusterCRD clusterCRD) throws Exception {
        ClusterCRDDto  clusterCRDDto = new ClusterCRDDto();

        ObjectMeta meta = clusterCRD.getMetadata();
        clusterCRDDto.setUid(k8sUtil.GetNamespaceName(meta));
        Map<String, Object> labelsMap = meta.getLabels();
        Map<String, Object> annosMap = meta.getAnnotations();
        clusterCRDDto.setName(meta.getName());
        clusterCRDDto.setDataCenter(meta.getNamespace());
        clusterCRDDto.setCreateTime(DateUtil.utcToGmtDate(meta.getCreationTimestamp()));
        ClusterSpec spec = clusterCRD.getSpec();
        ClusterStatus status = clusterCRD.getStatus();
        ClusterInfo info = spec.getInfo();
        List<ClusterTemplate> template = spec.getTemplate();

        clusterCRDDto.setEnvLabel(ClusterLevelEnum.getEnvLevel(labelsMap.get(TEMPLATE).toString()).getLevel());
        clusterCRDDto.setNickname(annosMap.get("name").toString());
        clusterCRDDto.setK8sAddress(info.getAddress());
        clusterCRDDto.setPort(info.getPort());
        clusterCRDDto.setProtocol(info.getProtocol());
        clusterCRDDto.setHarborAddress(info.getHarbor().getAddress());
        clusterCRDDto.setHarborAdminPwd(info.getHarbor().getPassword());
        clusterCRDDto.setHarborAdminUser(info.getHarbor().getUser());
        clusterCRDDto.setHarborPort(info.getHarbor().getPort());
        clusterCRDDto.setHarborProtocol(info.getHarbor().getProtocol());

        clusterCRDDto.setDomain(info.getDomain());
        clusterCRDDto.setNfs(info.getNfs());
        clusterCRDDto.setExternal(info.getExternal());
        clusterCRDDto.setMysql(info.getMysql());
        clusterCRDDto.setRedis(info.getRedis());
        clusterCRDDto.setNetwork(info.getNetwork());
        clusterCRDDto.setJenkins(info.getJenkins());

        clusterCRDDto.setTemplate(template);
        Map<String, Boolean> statusMap = status.getConditions().stream().collect(Collectors.toMap(StatusConditions::getType, condition -> condition.getStatus()));
        clusterCRDDto.setIsEnable(statusMap.get("Ready"));

        return clusterCRDDto;
    }
}
