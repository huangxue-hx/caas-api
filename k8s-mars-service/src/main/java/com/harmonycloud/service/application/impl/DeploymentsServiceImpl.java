package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cluster.IngressControllerDto;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.dto.scale.HPADto;
import com.harmonycloud.dto.scale.ResourceMetricScaleDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.service.EventService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.istio.IstioCommonService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.bean.monitor.InfluxdbQuery;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.convert.KubeAffinityConvert;
import com.harmonycloud.service.platform.convert.KubeServiceConvert;
import com.harmonycloud.service.platform.dto.PodDto;
import com.harmonycloud.service.platform.dto.ReplicaSetDto;
import com.harmonycloud.service.platform.service.InfluxdbService;
import com.harmonycloud.service.platform.service.WatchService;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.LABEL_KEY_APP;
import static com.harmonycloud.service.platform.constant.Constant.DEFAULT_POD_MAX_SURGE;
import static com.harmonycloud.service.platform.constant.Constant.DEFAULT_POD_MAX_UNAVAILABLE;
import static com.harmonycloud.service.platform.constant.Constant.TYPE_DEPLOYMENT;

@Service
public class DeploymentsServiceImpl implements DeploymentsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsServiceImpl.class);
    private static final int MAX_LOG_LINES = 2000;

    @Value("#{propertiesReader['msf.svc.name']}")
    private String msfSvcName;

    public String getMsfSvcName() {
        return msfSvcName;
    }

    public void setMsfSvcName(String msfSvcName) {
        this.msfSvcName = msfSvcName;
    }

    @Autowired
    private WatchService watchService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private HorizontalPodAutoscalerService hpaService;

    @Autowired
    private AutoScaleService autoScaleService;

    @Autowired
    private PodService podService;

    @Autowired
    private ServicesService sService;

    @Autowired
    private EventService eventService;

    @Autowired
    private ReplicasetsService rsService;

    @Autowired
    private RouterService routerService;

    @Autowired
    private HttpSession session;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.harmonycloud.service.platform.service.PodService podPlatFormService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private FileUploadToContainerService fileUploadToContainerService;

    /*@Autowired
    private PodDisruptionBudgetService pdbService;*/

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private DataPrivilegeService dataPrivilegeService;

    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    private StatefulSetService statefulSetService;

    @Autowired
    private PersistentVolumeService persistentVolumeService;

    @Autowired
    private IstioCommonService istioCommonService;

    public ActionReturnUtil listDeployments(String tenantId, String name, String namespace, String labels, String projectId, String clusterId) throws Exception {
        //参数判空
        if (StringUtils.isBlank(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //labels
        Map<String, Object> bodys = new HashMap<String, Object>();
        String labelSelector = Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId;
        if (!StringUtils.isBlank(labels)) {
            labelSelector = labelSelector + "," + labels;
        }
        bodys.put("labelSelector", labelSelector);

        //拆分namespace
        String[] ns = namespace.split(",");
        List<Map<String, Object>> result = new ArrayList<>();
        List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
        for (int i = 0; i < ns.length; i++) {
            if (ns[i] != null && !StringUtils.isEmpty(ns[i])) {
                try {
                    Cluster cluster = namespaceLocalService.getClusterByNamespaceName(ns[i]);
                    //判断该namespace是否有权限
                    if (clusterList.stream().noneMatch((c) -> c.getId().equals(cluster.getId()))) {
                        continue;
                    }
                    if (StringUtils.isNotBlank(clusterId) && !cluster.getId().equals(clusterId)) {
                        continue;
                    }
                    DeploymentList deployment = getDeployments(ns[i], bodys, cluster);
                    String aliasNamespace = namespaceLocalService.getNamespaceByName(ns[i]).getAliasName();
                    if (deployment != null && deployment.getItems().size() > 0) {
                        List<Map<String, Object>> res = K8sResultConvert.convertAppList(deployment, cluster, aliasNamespace);
                        result.addAll(res);
                    }
                }catch (Exception e){
                    LOGGER.error("查询deployment列表失败，namespace：{}", ns[i],e);
                }
            }
        }
        //数据过滤
        DataPrivilegeDto dataPrivilegeDto = new DataPrivilegeDto();
        dataPrivilegeDto.setProjectId(projectId);
        Iterator<Map<String, Object>> iterator = result.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> map = iterator.next();
            dataPrivilegeDto.setData((String) map.get(CommonConstant.NAME));
            dataPrivilegeDto.setNamespace((String) map.get(CommonConstant.DATA_NAMESPACE));
            dataPrivilegeDto.setDataResourceType(DataResourceTypeEnum.SERVICE.getCode());
            Map filteredMap = dataPrivilegeHelper.filterMap(map, dataPrivilegeDto);
            if (filteredMap == null) {
                iterator.remove();
            }else{
                map = filteredMap;
            }
        }
        return ActionReturnUtil.returnSuccessWithData(result);
    }

    @Override
    public DeploymentList listDeployments(String namespace, String projectId) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        Map<String, Object> body = null;
        if(StringUtils.isNotBlank(projectId)){
            body = new HashMap<>();
            body.put("labelSelector", Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID + "=" + projectId);
        }
        return this.getDeployments(namespace,body,cluster);
    }

    @Override
    public List<Map<String, Object>> listTenantDeploys(String tenantId, String namespace, String clusterId) throws Exception {
        AssertUtil.notBlank(tenantId, DictEnum.TENANT_ID);
        AssertUtil.notBlank(clusterId, DictEnum.CLUSTER_ID);
        List<Map<String, Object>> deploys = new ArrayList<>();
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<String> clusterIds = new ArrayList<>();
        clusterIds.add(clusterId);
        List<NamespaceLocal> namespaceLocals = new ArrayList<>();
        if(StringUtils.isBlank(namespace)) {
            List<NamespaceLocal> namespaces = namespaceLocalService.
                    getNamespaceListByTenantIdAndClusterId(tenantId, clusterIds);
            if(!CollectionUtils.isEmpty(namespaces)){
                namespaceLocals.addAll(namespaces);
            }
        }else{
            String[] namespaces = namespace.split(",");
            for(String ns : namespaces) {
                NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(ns);
                //过滤分区和集群不匹配的分区
                if (StringUtils.isNotBlank(clusterId) && !namespaceLocal.getClusterId().equals(clusterId)) {
                   continue;
                }
                namespaceLocals.add(namespaceLocal);
            }
        }
        for (NamespaceLocal namespaceLocal : namespaceLocals) {
            DeploymentList deploymentList = this.listDeployments(namespaceLocal.getNamespaceName(), null);
            if (deploymentList == null || CollectionUtils.isEmpty(deploymentList.getItems())) {
                continue;
            }
            deploys.addAll(K8sResultConvert.convertAppList(deploymentList, cluster, namespaceLocal.getAliasName()));
        }
        return deploys;
    }

    public DeploymentList getDeployments(String namespace, Map<String, Object> bodys, Cluster cluster) throws Exception {
        K8SClientResponse depRes = dpService.doDeploymentsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
            throw new MarsRuntimeException(depRes.getBody());
        }
        DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        return deployment;
    }

    public ActionReturnUtil startDeployments(String name, String namespace, String userName) throws Exception {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> anno = new HashMap<>();
        K8SClientResponse response = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (response.getStatus() == Constant.HTTP_404) {
            return ActionReturnUtil.returnSuccess();
        }
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
        // status code :0 stop 1:start 2:stopping 3:starting
        // 先判断状态
        if (Objects.nonNull(dep)) {
            anno = updateAnnotation((Map<String, Object>) dep.getMetadata().getAnnotations(), dep.getMetadata().getName(), Constant.STARTING);
            dep.getSpec().setReplicas(Integer.valueOf(anno.get("nephele/replicas").toString()));
            dep.getMetadata().setAnnotations(anno);
            Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put("Content-type", "application/json");
            K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(newRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnError();
    }

    public ActionReturnUtil stopDeployments(String name, String namespace, String userName) throws Exception {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        /*
        //先删除自动伸缩控制
        boolean scaleDeleted = autoScaleService.delete(namespace, name);
        if (!scaleDeleted) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_AUTOSCALE_DELETE_FAILURE);
        }
        */
        // String lrv = watchService.getLatestVersion(namespace);
        // watchAppEvent(name, namespace, null, lrv, userName);

        Map<String, Object> anno = new HashMap<>();
        // 将实例减为0
        // dep状态：status code :0 ：stop 1:start 2:stopping 3:starting
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (depRes.getStatus() == Constant.HTTP_404) {
            return ActionReturnUtil.returnSuccess();
        }
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        if (Objects.nonNull(dep)) {
            anno = updateAnnotation((Map<String, Object>) dep.getMetadata().getAnnotations(), dep.getMetadata().getName(), Constant.STOPPING);
            anno.put("nephele/replicas", String.valueOf(dep.getSpec().getReplicas()));
            dep.getSpec().setReplicas(0);
            dep.getMetadata().setAnnotations(anno);
            Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-type", "application/json");
            K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(newRes.getBody());
            }
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnError();
    }

    /**
     * 服务停止，启动时 修改metadata中的annotations
     * @param anno
     * @param name
     * @return
     * @throws Exception
     */
    private Map<String, Object> updateAnnotation(Map<String, Object> anno, String name, String action) throws Exception {
        Map<String, Object> annotation = anno;
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        annotation.put("updateTimestamp", updateTime);
        switch (action) {
            case Constant.STOPPING:
                if (anno.containsKey("nephele/status")) {
                    String status = anno.get("nephele/status").toString();
                    if (status.equals(Constant.STOPPING)) {
                        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.STOPPED, DictEnum.SERVICE.phrase() + name, true);
                    } else {
                        anno.put("nephele/status", Constant.STOPPING);
                    }
                } else {
                    anno.put("nephele/status", Constant.STOPPING);
                }
                break;
            case Constant.STARTING:
                if (anno.containsKey("nephele/status") && anno.get("nephele/status") != null) {
                    String status = anno.get("nephele/status").toString();
                    if (status.equals(Constant.STARTING)) {
                        return ActionReturnUtil
                                .returnErrorWithData(ErrorCodeMessage.STARTED,
                                        DictEnum.SERVICE.phrase() + name, true);
                    } else {
                        anno.put("nephele/status", Constant.STARTING);
                        if (anno.get("nephele/replicas") != null) {
                            anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
                        } else {
                            anno.put("nephele/replicas", "1");
                        }
                    }
                } else {
                    anno.put("nephele/status", Constant.STARTING);
                    if (anno.get("nephele/replicas") != null) {
                        anno.put("nephele/replicas", anno.get("nephele/replicas").toString());
                    } else {
                        anno.put("nephele/replicas", "1");
                    }

                }
                break;
            default:
                break;
        }
        return annotation;
    }

    public ActionReturnUtil getPodDetail(String name, String namespace) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        return ActionReturnUtil.returnSuccessWithData(podPlatFormService.getPodDetail(namespace, name, cluster));
    }

    @Override
    public ActionReturnUtil podList(String name, String namespace, boolean isFilterTerminated) throws Exception {
        if (StringUtils.isEmpty(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //获取rs
        Map<String, Object> bodys = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(name)) {
            bodys.put("labelSelector", "app=" + name);
        }
        K8SClientResponse rsresponse = rsService.doRsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsresponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(rsresponse.getBody(), UnversionedStatus.class);
            if (Objects.isNull(status)){
                return ActionReturnUtil.returnErrorWithData(rsresponse.getBody());
            }
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        ReplicaSetList rsList = K8SClient.converToBean(rsresponse, ReplicaSetList.class);

        List<ReplicaSet> rss = rsList.getItems();
        List<PodDetail> list = new LinkedList<PodDetail>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        if (rss != null && rss.size() > 0) {
            // 对时间进行顺序
            Collections.sort(rss, new Comparator<ReplicaSet>() {

                @Override
                public int compare(ReplicaSet o1, ReplicaSet o2) {
                    try {
                        return Long.valueOf(sdf.parse(o1.getMetadata().getCreationTimestamp()).getTime()).compareTo(Long.valueOf(sdf.parse(o2.getMetadata().getCreationTimestamp()).getTime()));
                    } catch (ParseException e) {
                        LOGGER.warn("获取PodList失败，deploy:{}，namespace:{}", name, namespace, e);
                        return 0;
                    }
                }

            });
            int tag = 0;
            for (ReplicaSet rs : rss) {
                String deploymentName = rs.getMetadata().getLabels().get(TYPE_DEPLOYMENT).toString();
                bodys = new HashMap<>();
                if (rs.getMetadata().getAnnotations() != null && !StringUtils.isEmpty(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString())) {
                    tag = Integer.parseInt(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString());
                } else {
                    tag++;
                }
                Map<String, Object> labels = rs.getMetadata().getLabels();
                if (!StringUtils.isEmpty(labels.get("pod-template-hash").toString())) {
                    bodys.put("labelSelector", "pod-template-hash=" + labels.get("pod-template-hash"));
                    K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
                    if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
                        return ActionReturnUtil.returnErrorWithData(podRes.getBody());
                    }
                    PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
                    List<PodDetail> podDetails = K8sResultConvert.podListConvert(podList, "v" + tag);
                    if(isFilterTerminated) {
                        podDetails.removeIf(podDetail -> podDetail.getTerminating() != null && podDetail.getTerminating());
                    }
                    podDetails.stream().forEach(pod -> pod.setDeployment(deploymentName));
                    list.addAll(podDetails);
                }
            }
        }
        return ActionReturnUtil.returnSuccessWithData(list);
    }

    @Override
    public ActionReturnUtil podList(String name, String namespace) throws Exception {
        return this.podList(name, namespace, false);
    }


    @Override
    public ActionReturnUtil getDeploymentDetail(String namespace, String name,boolean isFilter, String projectId) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        // 获取特定的deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        if (dep == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }

        // 根据项目过滤，不匹配直接返回
        Map<String, Object> depLabels = dep.getMetadata().getLabels();
        if (StringUtils.isNotBlank(projectId) && depLabels.get(Constant.TYPE_PROJECT_ID) != null
                && !depLabels.get(Constant.TYPE_PROJECT_ID).toString().equals(projectId)) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_NOT_MATCH_PROJECT);
        }

        // 获取cpaEvents
        Map<String, Object> bodys = new HashMap<String, Object>();
        AutoScaleDto scaleDto  = autoScaleService.get(namespace, name);
        EventList hapEve = new EventList();
        if (scaleDto != null ){
            bodys.put("fieldSelector", "involvedObject.uid=" + scaleDto.getUid());
            K8SClientResponse hpaeRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(hpaeRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(hpaeRes.getBody());
            }
            hapEve = JsonUtil.jsonToPojo(hpaeRes.getBody(), EventList.class);
        }

        // 获取service
        bodys.clear();
        bodys.put("labelSelector", Constant.TYPE_DEPLOYMENT + "=" + name
                + (StringUtils.isBlank(projectId) ? "" : ("," + Constant.LABEL_PROJECT_ID + "=" + projectId)));
        K8SClientResponse sRes = sService.doServiceByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(sRes.getBody());
        }
        ServiceList serviceList = JsonUtil.jsonToPojo(sRes.getBody(), ServiceList.class);

        // 获取deployment的events
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.uid=" + dep.getMetadata().getUid());
        K8SClientResponse deResponse = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(deResponse.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(deResponse.getBody());
        }
        EventList eventList = JsonUtil.jsonToPojo(deResponse.getBody(), EventList.class);

        // 获取pod
        bodys.clear();
        bodys.put("labelSelector", K8sResultConvert.convertDeploymentExpression(dep, name));
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        PodSpec podSpec = dep.getSpec().getTemplate().getSpec();
        AppDetail res = K8sResultConvert.convertAppDetail(dep, serviceList, eventList, hapEve, podList);
        res.setAutoScale(scaleDto);
        res.setClusterId(cluster.getId());
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(res.getNamespace());
        res.setAliasNamespace(namespaceLocal.getAliasName());
        res.setPrivateNamespace(namespaceLocal.getIsPrivate());
        res.setRealName(userService.getUser(res.getOwner()).getRealName());
        res.setHostAliases(podSpec.getHostAliases());
        res.setServiceType(Constant.DEPLOYMENT);

        //判断是否是微服务组件应用是否有权限操作
        boolean isOperationable = true;
        if (res.isMsf()) {
            isOperationable = userService.checkCurrentUserIsAdmin();
        }
        res.setOperationable(isOperationable);
        if(isFilter) {
            return ActionReturnUtil.returnSuccessWithData(dataPrivilegeHelper.filter(res));
        }
        return ActionReturnUtil.returnSuccessWithData(res);
    }

    @Override
    public ActionReturnUtil getDeploymentEvents(String namespace, String name) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        List<EventDetail> allEvents = new ArrayList<EventDetail>();
        Map<String, Object> bodys = new HashMap<String, Object>();
        String selExp = new String();
        String uid = new String();
        // 可利用异步操作，线程
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        Map<String, Object> deploymentSelector = dep.getSpec().getSelector().getMatchLabels();
        if (deploymentSelector == null || deploymentSelector.isEmpty()) {
            deploymentSelector.put(Constant.TYPE_DEPLOYMENT, name);
        }
        selExp = K8sResultConvert.convertExpression(deploymentSelector);
        uid = dep.getMetadata().getUid();

        bodys.put("labelSelector", selExp);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, bodys, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        // 循环podlist获取每个pod的事件
        bodys.clear();
        for (Pod pod : podList.getItems()) {
            bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
            K8SClientResponse podevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(podevRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(podevRes.getBody());
            }
            EventList podeventList = JsonUtil.jsonToPojo(podevRes.getBody(), EventList.class);
            if (podeventList.getItems() != null && podeventList.getItems().size() > 0) {
                allEvents.addAll(K8sResultConvert.convertPodEvent(podeventList.getItems()));
            }
        }

        // 获取dep事件
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.uid=" + uid);
        K8SClientResponse evRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(evRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(evRes.getBody());
        }
        EventList depeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
        if (depeventList.getItems() != null && depeventList.getItems().size() > 0) {
            allEvents.addAll(K8sResultConvert.convertPodEvent(depeventList.getItems()));
        }

        // rs事件
        /*if (rSetList.getItems() != null && rSetList.getItems().size() > 0) {
            bodys.clear();
            bodys.put("fieldSelector", "involvedObject.uid=" + rSetList.getItems().get(0));
            K8SClientResponse hpaevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(hpaevRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(hpaevRes.getBody());
            }
            EventList hpaeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
            if (hpaeventList.getItems() != null && hpaeventList.getItems().size() > 0) {
                allEvents.addAll(K8sResultConvert.convertPodEvent(hpaeventList.getItems()));
            }
        }*/

        // cpaEvents
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.name=" + name + "-cpa");
        K8SClientResponse hpaevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(hpaevRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(hpaevRes.getBody());
        }
        EventList hpaeventList = JsonUtil.jsonToPojo(hpaevRes.getBody(), EventList.class);
        if (hpaeventList.getItems() != null && hpaeventList.getItems().size() > 0) {
            allEvents.addAll(K8sResultConvert.convertPodEvent(hpaeventList.getItems()));
        }

        // 对event进行倒序排列
        return ActionReturnUtil.returnSuccessWithData(K8sResultConvert.sortByDesc(allEvents));
    }

    @Override
    public ActionReturnUtil scaleDeployment(String namespace, String name, Integer scale, String userName)
            throws Exception {
        if (scale == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE,"scale", true);
        }
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Integer replicas;
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (depRes.getStatus() == Constant.HTTP_404) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        replicas = dep.getSpec().getReplicas();
        dep.getSpec().setReplicas(scale);
        dep.getMetadata().setAnnotations(updateAnnotationInScale(dep.getMetadata().getAnnotations(), scale, replicas));
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus()) && newRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("实例伸缩失败：", newRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_SCALE_INSTANCE_FAILURE);
        }

        return ActionReturnUtil.returnSuccess();
    }

    private Map<String, Object> updateAnnotationInScale(Map<String, Object> annotation, Integer scale, Integer replicas) throws Exception {
        Map<String, Object> newAnnotation = annotation;
        if (scale == 0) {
            newAnnotation.put("nephele/status", Constant.STOPPING);
            newAnnotation.put("nephele/replicas", replicas.toString());
        } else if (replicas == 0) {
            newAnnotation.put("nephele/status", Constant.STARTING);
            newAnnotation.put("nephele/replicas", scale.toString());
        } else {
            newAnnotation.put("nephele/replicas", scale.toString());
        }
        return newAnnotation;
    }

    @Override
    public ActionReturnUtil deploymentContainer(String namespace, String name) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        List<ContainerOfPodDetail> containerOfPodDetails = null;
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        containerOfPodDetails = K8sResultConvert.convertDeploymentContainer(dep, dep.getSpec().getTemplate().getSpec().getContainers(), cluster);
        //查询存储容量，并返回新的containerList
        List<ContainerOfPodDetail> newContainerList = new ArrayList<>();
        for(ContainerOfPodDetail containers : containerOfPodDetails){
            List<VolumeMountExt> vms = new ArrayList<>();
            if(!Objects.isNull(containers.getStorage())) {
                for (VolumeMountExt vmExt : containers.getStorage()) {
                    if (persistentVolumeService.isFsPv(vmExt.getType()) && StringUtils.isNotBlank(vmExt.getPvcname())) {
                        PersistentVolumeClaim pvcByName = this.pvcService.getPvcByName(namespace, vmExt.getPvcname(), cluster);
                        if (pvcByName == null) {
                            LOGGER.info("pvc存储券不存在,pvcname:{},clusterName:{}", name, cluster.getName());
                            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_QUERY_FAIL);
                        }
                        Map<String, String> request = (Map)pvcByName.getSpec().getResources().getRequests();
                        vmExt.setCapacity(request.get(CommonConstant.STORAGE));
                    }
                    vms.add(vmExt);
                }
            }
            containers.setStorage(vms);
            newContainerList.add(containers);
        }
        return ActionReturnUtil.returnSuccessWithData(containerOfPodDetails);
    }

    @Override
    public ActionReturnUtil deploymentContainer(String namespace, String name, Cluster cluster) throws Exception {
        if (StringUtils.isEmpty(namespace) || cluster == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(dep);
        //查询存储容量，并返回新的containerList
        List<ContainerOfPodDetail> newContainerList = new ArrayList<>();
        for(ContainerOfPodDetail containers : containerList){
            List<VolumeMountExt> vms = new ArrayList<>();
            if(!Objects.isNull(containers.getStorage())) {
                for (VolumeMountExt vmExt : containers.getStorage()) {
                    if (persistentVolumeService.isFsPv(vmExt.getType()) && StringUtils.isNotBlank(vmExt.getPvcname())) {
                        PersistentVolumeClaim pvcByName = this.pvcService.getPvcByName(namespace, vmExt.getPvcname(), cluster);
                        if (pvcByName == null) {
                            LOGGER.info("pvc存储券不存在,pvcname:{},clusterName:{}", name, cluster.getName());
                            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.PV_QUERY_FAIL);
                        }
                        Map<String, String> request = (Map)pvcByName.getSpec().getResources().getRequests();
                        vmExt.setCapacity(request.get(CommonConstant.STORAGE));
                    }
                    vms.add(vmExt);
                }
            }
            containers.setStorage(vms);
            newContainerList.add(containers);
        }
        return ActionReturnUtil.returnSuccessWithData(newContainerList);
    }

    @Override
    public ActionReturnUtil namespaceContainer(String namespace) throws Exception {
        if (StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return namespaceContainer(namespace, cluster, null);
    }

    @Override
    public ActionReturnUtil namespaceContainer(String namespace, Cluster cluster, Map<String, Object> headers) throws Exception {
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, null, headers, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        DeploymentList depList = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        if (depList == null || CollectionUtils.isEmpty(depList.getItems())) {
            return ActionReturnUtil.returnSuccessWithData(Collections.emptyList());
        }
        List<Deployment> deployments = depList.getItems();
        List<ContainerOfPodDetail> containers = new ArrayList<>();
        for (int i = 0; i < deployments.size(); i++) {
            containers.addAll(K8sResultConvert.convertContainer(deployments.get(i)));
        }
        return ActionReturnUtil.returnSuccessWithData(containers);
    }

    public ActionReturnUtil getPodAppLog(String namespace, String container, String pod, Integer sinceSeconds, String clusterId) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();
        if (StringUtils.isNotBlank(container)) {
            bodys.put("container", container);
        }
        if (sinceSeconds != null && sinceSeconds > 0) {
            bodys.put("sinceSeconds", sinceSeconds);
        }

        Cluster cluster = null;
        if (clusterId != null && !clusterId.equals("")) {
            cluster = clusterService.findClusterById(clusterId);
        } else {
            cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        }

        //设置查询最大的日志量
        bodys.put("tailLines", MAX_LOG_LINES);
        K8SClientResponse response = podService.getPodLogByNamespace(namespace,
                pod, "log", null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("getPodAppLog failed. message:{}", response.getBody().toString());
            if (response.getBody() != null && response.getBody().indexOf("ContainerCreating") > 0) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_NOT_READY);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
        }
        if (StringUtils.isBlank(response.getBody())) {
            return ActionReturnUtil.returnSuccessWithData(ErrorCodeMessage.LOG_NULL);
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }


    private void watchAppEvent(String name, String namespace, String kind, String rv, String userName, Cluster cluster)
            throws Exception {
        String token = String.valueOf(K8SClient.getTokenMap().get(userName));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        Map<String, String> field = new HashMap<String, String>();
        field.put("involvedObject.name", name);
        field.put("involvedObject.namespace", namespace);
        watchService.watch(field, kind, rv, userName, cluster);

        // 获取pod
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "app=" + name);
        bodys.put("resourceVersion", rv);
        bodys.put("watch", "true");
        bodys.put("timeoutSeconds", 3);
        K8SClientResponse response = podService.getPodByNamespace(namespace, headers, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return;
        }
        PodDto podDto = StringUtils.isEmpty(response.getBody()) ? null
                : JsonUtil.jsonToPojo(response.getBody(), PodDto.class);
        if (null != podDto && null != podDto.getObject()) {
            Pod pod = podDto.getObject();
            field.put("involvedObject.name", pod.getMetadata().getName());
            field.put("involvedObject.namespace", pod.getMetadata().getNamespace());
        }
        watchService.watch(field, kind, rv, userName, cluster);

        // 获取rs
        K8SURL rsUrl = new K8SURL();
        rsUrl.setNamespace(namespace).setResource(Resource.REPLICASET);
        bodys.clear();
        bodys.put("labelSelector", "app=" + name);
        bodys.put("resourceVersion", rv);
        bodys.put("watch", "true");
        bodys.put("timeoutSeconds", 3);

        K8SClientResponse rs = new K8sMachineClient().exec(rsUrl, HTTPMethod.GET, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rs.getStatus())) {
            return;
        }
        ReplicaSetDto rsDto = JsonUtil.jsonToPojo(rs.getBody(), ReplicaSetDto.class);
        if (null != rsDto && null != rsDto.getObject()) {
            ReplicaSet replicaSet = rsDto.getObject();
            field.put("involvedObject.name", replicaSet.getMetadata().getName());
            field.put("involvedObject.namespace", replicaSet.getMetadata().getNamespace());
        }
        watchService.watch(field, kind, rv, userName, cluster);
    }

    @Override
    public ActionReturnUtil createDeployment(DeploymentDetailDto detail, String userName, String app, Cluster cluster, List<IngressDto> ingress) throws Exception {
        dataPrivilegeService.addResource(detail, app, DataResourceTypeEnum.APPLICATION);
        //参数校验
        boolean isIstioNamespace = istioCommonService.isIstioEnabled(detail.getNamespace());
        if(isIstioNamespace && Objects.isNull(detail.getDeployVersion())){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOY_VERSION_IS_NULL_WHEN_ISTIO_ENABLE);
        }

        List<CreateContainerDto> containers = detail.getContainers();
        if (containers != null && !containers.isEmpty()) {
            for (CreateContainerDto c : containers) {
                List<CreateConfigMapDto> configMaps = c.getConfigmap();
                if (configMaps != null && configMaps.size() > 0) {
                    K8SURL url1 = new K8SURL();
                    url1.setNamespace(detail.getNamespace()).setResource(Resource.CONFIGMAP).setName(detail.getName() + c.getName());
                    K8SClientResponse responses = new K8sMachineClient().exec(url1, HTTPMethod.GET, null, null, cluster);
                    Map<String, Object> convertJsonToMap = JsonUtil.convertJsonToMap(responses.getBody());
                    String metadata = convertJsonToMap.get(CommonConstant.METADATA).toString();
                    if (!CommonConstant.EMPTYMETADATA.equals(metadata)) {
                        return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAME_EXIST,
                                DictEnum.CONFIG_MAP.phrase() + detail.getName() + c.getName(), true);
                    }
                    this.createConfigMap(detail.getNamespace(), detail.getName() + c.getName(), detail.getName(), configMaps, cluster);
                }
            }
        }

        //根据namespace获取节点label，并将label作为节点强制亲和
        if (Objects.nonNull(detail)) {
            List<AffinityDto> nodeAffinityList = detail.getNodeAffinity();
            nodeAffinityList = this.setNamespaceLabelAffinity(detail.getNamespace(), nodeAffinityList);
            detail.setNodeAffinity(nodeAffinityList);
        }
//        Deployment dep = K8sResultConvert.convertAppCreate(detail, userName, app, ingress);

        K8SURL k8surl = new K8SURL();
        k8surl.setNamespace(detail.getNamespace()).setResource(Resource.DEPLOYMENT);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();
//        bodys = CollectionUtil.transBean2Map(dep);
//        K8SClientResponse response = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, bodys, cluster);
//        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
//            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
//            return ActionReturnUtil.returnErrorWithData(status.getMessage());
//        }
//        Deployment resD = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);
        com.harmonycloud.k8s.bean.Service service = K8sResultConvert.convertAppCreateOfService(detail, app, Constant.DEPLOYMENT);
        k8surl.setNamespace(detail.getNamespace()).setResource(Resource.SERVICE);
//        bodys.clear();
        bodys = CollectionUtil.transBean2Map(service);
        K8SClientResponse sResponse = new K8sMachineClient().exec(k8surl, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(sResponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(sResponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        com.harmonycloud.k8s.bean.Service resS = JsonUtil.jsonToPojo(sResponse.getBody(),
                com.harmonycloud.k8s.bean.Service.class);

        bodys.clear();
        Object result = new Object();

        //创建pdb，minAvailable与maxUnavailable值从系统配置表system_config中获取
        /*String minAvailableValue = systemConfigService.findConfigValueByName(Constant.SYSTEM_CONFIG_PDB_MIN_AVAILABLE);
        K8SClientResponse pdbRes = null;*/
        Deployment dep = K8sResultConvert.convertAppCreate(detail, userName, app, ingress);

        //HostAlias-自定义 hosts file
        if(CollectionUtils.isNotEmpty(detail.getHostAliases())){
            dep.getSpec().getTemplate().getSpec().setHostAliases(detail.getHostAliases());
        }


        bodys = CollectionUtil.transBean2Map(dep);
        K8SClientResponse response = dpService.doSpecifyDeployment(detail.getNamespace(),null, headers, bodys, HTTPMethod.POST,cluster);

        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            LOGGER.error("创建服务失败，response：{}", JSONObject.toJSONString(response));
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        result = JsonUtil.jsonToPojo(response.getBody(), Deployment.class);

        //pdbRes = pdbService.createPdbByType(detail.getNamespace(), dep.getMetadata().getName() + Constant.PDB_SUFFIX, dep.getSpec().getSelector(), Constant.PDB_TYPE_MIN_AVAILABLE, minAvailableValue, cluster);


        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("deployment", result);
        resMap.put("service", resS);

        /*if(!HttpStatusUtil.isSuccessStatus((pdbRes.getStatus()))){
            UnversionedStatus status = JsonUtil.jsonToPojo(pdbRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        com.harmonycloud.k8s.bean.PodDisruptionBudget resPdb =
                JsonUtil.jsonToPojo(pdbRes.getBody(), com.harmonycloud.k8s.bean.PodDisruptionBudget.class);
        resMap.put("podDisruptionBudget", resPdb);*/

        //pvc打标签
        if(CollectionUtils.isNotEmpty(detail.getContainers())){
            for(CreateContainerDto container : detail.getContainers()){
                if(container.getStorage() != null){
                    for(PersistentVolumeDto persistentVolumeDto : container.getStorage()){
                        PersistentVolumeClaim pvc = pvcService.getPvcByName(detail.getNamespace(), persistentVolumeDto.getPvcName(), cluster);
                        if(pvc != null){
                            Map<String, Object> labels = pvc.getMetadata().getLabels();
                            labels.put(LABEL_KEY_APP + CommonConstant.SLASH + detail.getName(), detail.getName());
                            K8SClientResponse pvcResponse = pvcService.updatePvcByName(pvc, cluster);
                            if(!HttpStatusUtil.isSuccessStatus((pvcResponse.getStatus()))){
                                UnversionedStatus status = JsonUtil.jsonToPojo(pvcResponse.getBody(), UnversionedStatus.class);
                                return ActionReturnUtil.returnErrorWithData(status.getMessage());
                            }
                        }
                    }
                }
            }
        }
        if (isIstioNamespace) {
            //create DestinationRule
            ActionReturnUtil destinationRuleReturn = istioCommonService.createDestinationRule(detail.getName(), detail.getNamespace(), detail.getDeployVersion());
            if (!destinationRuleReturn.isSuccess()) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CREATE_DESTINATIONRULE_IS_ERROR);
            }
        }

        return ActionReturnUtil.returnSuccessWithData(resMap);
    }




    @Override
    public ActionReturnUtil deleteDeployment(String name, String namespace, String userName, Cluster cluster) throws Exception {
        DeploymentDetailDto delObj = new DeploymentDetailDto();
        delObj.setName(name);
        delObj.setNamespace(namespace);
        dataPrivilegeService.deleteResource(delObj);
        //先删除自动伸缩控制
        boolean scaleDeleted = autoScaleService.delete(namespace, name);
        if (!scaleDeleted) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_AUTOSCALE_DELETE_FAILURE);
        }

        //删除pdb
        /*if(pdbService.existPdb(namespace ,name + Constant.PDB_SUFFIX, cluster)){
            K8SClientResponse pdbRes = pdbService.deletePdb(namespace, name + Constant.PDB_SUFFIX, cluster);
            if(!HttpStatusUtil.isSuccessStatus((pdbRes.getStatus()))){
                UnversionedStatus status = JsonUtil.jsonToPojo(pdbRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
        }*/

        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("获取Deployment失败,DeploymentName:{}, error:{}", name, depRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }

        Deployment dep = new Deployment();
        if (depRes.getStatus() != Constant.HTTP_404) {
            dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        }

        // 删除configmap
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", "app=" + name);
        K8SClientResponse conRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除configmap失败,DeploymentName:{}, error:{}", name, conRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }

        if (dep != null && dep.getSpec() != null) {
            // 删除deployment
            K8SClientResponse delRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.DELETE, cluster);
            if (!HttpStatusUtil.isSuccessStatus(delRes.getStatus()) && delRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除Deployment失败,DeploymentName:{}, error:{}", name, delRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }

            // 删除ingress
            cUrl.setResource(Resource.INGRESS);
            K8SClientResponse ingRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
            if (!HttpStatusUtil.isSuccessStatus(ingRes.getStatus()) && ingRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除Ingress失败,DeploymentName:{}, error:{}", name, ingRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }

            // update pvc
            Map<String, Object> pvclabel = new HashMap<String, Object>();
            pvclabel.put("labelSelector", LABEL_KEY_APP + CommonConstant.SLASH + name + "=" + name);

            K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }

            PersistentVolumeClaimList persistentVolumeList = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);


            if (persistentVolumeList != null && persistentVolumeList.getItems() != null) {

                for (PersistentVolumeClaim onePvc : persistentVolumeList.getItems()) {
                    onePvc.getMetadata().getLabels().remove(LABEL_KEY_APP + CommonConstant.SLASH + name);
                    K8SClientResponse response = pvcService.updatePvcByName(onePvc,cluster);
                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                        LOGGER.error("更新PVC失败,DeploymentName:{}, error:{}", name, response.getBody());
                        throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
                    }

                }
            }

            //获取projectId
            String projectId = (String) dep.getMetadata().getLabels().get("harmonycloud.cn/projectId");
            //通过projectId查找tenantId
            Project project = projectService.getProjectByProjectId(projectId);
            String tenantId = project.getTenantId();
            //通过tenantId找icName
            List<IngressControllerDto> icList = tenantService.getTenantIngressController(tenantId, cluster.getId());
            //删除对外暴露端口（nginx和数据库）
            routerService.deleteRulesByName(namespace, name, icList, cluster);
        }

        // 获取service
        K8SClientResponse svcRes = sService.doServiceByNamespace(namespace, null, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
            LOGGER.error("获取Service失败,DeploymentName:{}, error:{}", name, svcRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }
        ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);

        // 循环删除service,在k8s中service只能根据名称删除
        List<com.harmonycloud.k8s.bean.Service> svc = svcs.getItems();
        K8SURL svcUrl = new K8SURL();
        svcUrl.setNamespace(namespace).setResource(Resource.SERVICE);
        for (int i = 0; i < svc.size(); i++) {
            svcUrl.setName(svc.get(i).getMetadata().getName());
            K8SClientResponse serviceRes = new K8sMachineClient().exec(svcUrl, HTTPMethod.DELETE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(serviceRes.getStatus()) && serviceRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除Service失败,ServiceName:{}, error:{}", svc.get(i).getMetadata().getName(), serviceRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }
        }

        //删除文件上传到容器记录
        fileUploadToContainerService.deleteUploadRecord(namespace, name);

        //删除已经创建Istio策略
        boolean isIstioNamespace = istioCommonService.isIstioEnabled(namespace);
        if (isIstioNamespace) {
            //delete istio policy resource
            istioCommonService.deleteIstioPolicy(namespace, name, cluster.getId());
        }
        return ActionReturnUtil.returnSuccess();
    }

    //删除statefulset
    @Override
    public ActionReturnUtil deleteStatefulSet(String name, String namespace, String userName, Cluster cluster) throws Exception {
        //先删除自动伸缩控制
        boolean scaleDeleted = autoScaleService.delete(namespace, name);
        if (!scaleDeleted) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.SERVICE_AUTOSCALE_DELETE_FAILURE);
        }

        //删除pdb
        /*if(pdbService.existPdb(namespace ,name + Constant.PDB_SUFFIX, cluster)){
            K8SClientResponse pdbRes = pdbService.deletePdb(namespace, name + Constant.PDB_SUFFIX, cluster);
            if(!HttpStatusUtil.isSuccessStatus((pdbRes.getStatus()))){
                UnversionedStatus status = JsonUtil.jsonToPojo(pdbRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
        }*/

        try {
            String label = Constant.TYPE_STATEFULSET + "=" + name;
            String errorName = "StatefulSetName";
            Map<String, Object> queryStatefulSet = new HashMap<>();
            queryStatefulSet.put("labelSelector", label);
            //获取StatefulSet
            K8SClientResponse statefulSetRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(statefulSetRes.getStatus()) && statefulSetRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("获取StatefulSet失败,StatefulSetName:{}, error:{}", name, statefulSetRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }
            deleteConfigmap(name, namespace, queryStatefulSet, cluster, errorName);
            StatefulSet statefulSet = new StatefulSet();
            if (statefulSetRes.getStatus() != Constant.HTTP_404) {
                statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);
            }
            if (statefulSet != null && statefulSet.getSpec() != null) {
                // 删除StatefulSet
                K8SClientResponse deleteResult = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.DELETE, cluster);
                if (!HttpStatusUtil.isSuccessStatus(deleteResult.getStatus()) && deleteResult.getStatus() != Constant.HTTP_404) {
                    LOGGER.error("删除StatefulSet失败,StatefulSetName:{}, error:{}", name, deleteResult.getBody());
                    throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
                }

                deleteResource(name, namespace, queryStatefulSet, cluster, label, errorName);
            }
            deleteService(name, namespace, queryStatefulSet, cluster, errorName);
            //删除文件上传到容器记录
            fileUploadToContainerService.deleteUploadRecord(namespace, name);

            //删除已经创建Istio策略
            boolean isIstioNamespace = istioCommonService.isIstioEnabled(namespace);
            if (isIstioNamespace) {
                //delete istio policy resource
                istioCommonService.deleteIstioPolicy(namespace, name, cluster.getId());
            }
        } catch (Exception e) {
            LOGGER.error("删除StatefulSet失败");
            return ActionReturnUtil.returnErrorWithData(e.getMessage());
        }
        return ActionReturnUtil.returnSuccess();

    }

    /**
     * 删除configmap
     * @param name
     * @param namespace
     * @param queryP
     * @param cluster
     * @param errorName
     */
    private void deleteConfigmap(String name, String namespace, Map<String, Object> queryP, Cluster cluster, String errorName) {
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        K8SClientResponse conRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除configmap失败," + errorName + ":{}, error:{}", name, conRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }
    }

    /**
     * 获取并删除service
     * @param name
     * @param namespace
     * @param queryP
     * @param cluster
     * @param errorName
     * @throws Exception
     */
    private void deleteService(String name, String namespace, Map<String, Object> queryP, Cluster cluster, String errorName) throws Exception {
        // 获取service
        K8SClientResponse svcRes = sService.doServiceByNamespace(namespace, null, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
            LOGGER.error("获取Service失败," + errorName +":{}, error:{}", name, svcRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }
        ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);

        // 循环删除service,在k8s中service只能根据名称删除
        List<com.harmonycloud.k8s.bean.Service> svc = svcs.getItems();
        K8SURL svcUrl = new K8SURL();
        svcUrl.setNamespace(namespace).setResource(Resource.SERVICE);
        for (int i = 0; i < svc.size(); i++) {
            svcUrl.setName(svc.get(i).getMetadata().getName());
            K8SClientResponse serviceRes = new K8sMachineClient().exec(svcUrl, HTTPMethod.DELETE, null, null, cluster);
            if (!HttpStatusUtil.isSuccessStatus(serviceRes.getStatus()) && serviceRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除Service失败,ServiceName:{}, error:{}", svc.get(i).getMetadata().getName(), serviceRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }
        }
    }

    /**
     * 删除ingress,pvc,对外暴露端口,升级pv
     * @param name
     * @param namespace
     * @param queryP
     * @param cluster
     * @param label
     * @param errorName
     * @return
     * @throws Exception
     */
    private ActionReturnUtil deleteResource(String name, String namespace, Map<String, Object> queryP, Cluster cluster, String label, String errorName) throws Exception {
        K8SURL cUrl = new K8SURL();
        // 删除ingress
        cUrl.setNamespace(namespace).setResource(Resource.INGRESS);
        K8SClientResponse ingRes = new K8sMachineClient().exec(cUrl, HTTPMethod.DELETE, null, queryP, cluster);
        if (!HttpStatusUtil.isSuccessStatus(ingRes.getStatus()) && ingRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("删除Ingress失败," + errorName + ":{}, error:{}", name, ingRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }

        // update pvc
        Map<String, Object> pvclabel = new HashMap<String, Object>();
        pvclabel.put("labelSelector", LABEL_KEY_APP + CommonConstant.SLASH + name + "=" + name);

        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, pvclabel, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }

        PersistentVolumeClaimList persistentVolumeClaimList = K8SClient.converToBean(pvcRes, PersistentVolumeClaimList.class);


        if (persistentVolumeClaimList != null && persistentVolumeClaimList.getItems() != null) {

            for (PersistentVolumeClaim onePvc : persistentVolumeClaimList.getItems()) {
                onePvc.getMetadata().getLabels().remove(LABEL_KEY_APP + CommonConstant.SLASH + name);
                K8SClientResponse response = pvcService.updatePvcByName(onePvc,cluster);
                if (!HttpStatusUtil.isSuccessStatus(response.getStatus()) && response.getStatus() != Constant.HTTP_404) {
                    LOGGER.error("更新PVC失败,DeploymentName:{}, error:{}", name, response.getBody());
                    throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
                }

            }
        }
        NamespaceLocal namespaceLocal = namespaceLocalService.getNamespaceByName(namespace);
        List<IngressControllerDto> icList = new ArrayList<>();
        if(namespaceLocal != null) {
            //通过tenantId找icName
            icList.addAll(tenantService.getTenantIngressController(namespaceLocal.getTenantId(), cluster.getId()));
        }
        //删除对外暴露端口（nginx和数据库）
        routerService.deleteRulesByName(namespace, name, icList, cluster);
        return null;
    }

    @Override
    public ActionReturnUtil replaceDeployment(UpdateDeployment detail, String userName, Cluster cluster) throws Exception {

        String lrv = watchService.getLatestVersion(detail.getNamespace(), null, cluster);

        new Thread() {
            @Override
            public void run() {
                try {
                    watchAppEvent(detail.getName(), detail.getNamespace(), null, lrv, userName, cluster);
                } catch (Exception e) {
                    LOGGER.warn("修改deployment:{}失败，cluster:{}，namespace:{}",
                            detail.getName() ,detail.getNamespace(), cluster.getName());
                }
            }
        }.start();

        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        // 获取service
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", "app=" + detail.getName());
        K8SClientResponse svcRes = sService.doServiceByNamespace(detail.getNamespace(), null, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(svcRes.getBody());
        }
        ServiceList svcs = JsonUtil.jsonToPojo(svcRes.getBody(), ServiceList.class);

        Map<String, Object> res = K8sResultConvert.convertAppPut(dep, svcs, detail.getContainers(), detail.getName());
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map((Deployment) res.get("dep"));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse putRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers,
                bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(putRes.getBody());
        }
        com.harmonycloud.k8s.bean.Service service = (com.harmonycloud.k8s.bean.Service) res.get("service");
        bodys.clear();
        bodys = CollectionUtil.transBean2Map(service);
        if (service != null) {
            K8SClientResponse putSvcRes = sService.doSepcifyService(detail.getNamespace(),
                    service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(putSvcRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(putSvcRes.getBody());
            }
        }
        return ActionReturnUtil.returnSuccessWithData("success");
    }

    @Override
    public ActionReturnUtil getAutoScaleDeployment(String name, String namespace) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SClientResponse response = hpaService.doSpecifyHpautoscaler(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            HorizontalPodAutoscaler hpa = JsonUtil.jsonToPojo(response.getBody(), HorizontalPodAutoscaler.class);
            HPADto dto = this.convertDto(hpa);
            dto.setDeploymentName(name);
            return ActionReturnUtil.returnSuccessWithData(dto);
        } else {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }


    }

    public HPADto convertDto(HorizontalPodAutoscaler hpa) throws Exception {
        HPADto dto = new HPADto();
        dto.setNamespace(hpa.getMetadata().getNamespace());
        HorizontalPodAutoscalerSpec hpaSpec = hpa.getSpec();
        dto.setMaxPods(hpaSpec.getMaxReplicas());
        dto.setMinPods(hpaSpec.getMinReplicas());
        List<MetricSpec> metricSpecList = hpaSpec.getMetrics();
        List<ResourceMetricScaleDto> resourceList = new ArrayList<ResourceMetricScaleDto>();
        if (!(metricSpecList == null || metricSpecList.size() == 0)) {
            for (MetricSpec metric : metricSpecList) {
                ResourceMetricSource source = metric.getResource();
                if (source == null) {
                    continue;
                } else {
                    ResourceMetricScaleDto resource = new ResourceMetricScaleDto();
                    resource.setName(source.getName());
                    resource.setTargetUsage(source.getTargetAverageUtilization());
                    resourceList.add(resource);
                }
            }
        }

        dto.setResource(resourceList);
        return dto;
    }

    @Override
    public ActionReturnUtil autoScaleDeployment(HPADto hpaDto) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(hpaDto.getNamespace());
        if (cluster == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(K8sResultConvert.convertHpa(hpaDto));
        K8SClientResponse response = hpaService.postHpautoscalerByNamespace(hpaDto.getNamespace(), headers, bodys, HTTPMethod.POST, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }

    @Override
    public ActionReturnUtil updateAutoScaleDeployment(HPADto hpaDto) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(hpaDto.getNamespace());
        if (cluster == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys = CollectionUtil.transBean2Map(K8sResultConvert.convertHpa(hpaDto));
        K8SClientResponse response = hpaService.doHpautoscalerByNamespace(hpaDto.getNamespace(), headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }

    @Override
    public ActionReturnUtil deleteAutoScaleDeployment(String name, String namespace) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (cluster == null) {
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        K8SClientResponse response = hpaService.doSpecifyHpautoscaler(namespace, name, null, null, HTTPMethod.DELETE, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(response.getBody());
        }
        return ActionReturnUtil.returnSuccessWithData(response.getBody());
    }

    @Override
    public ActionReturnUtil updateAppDeployment(UpdateDeployment deploymentDetail, String userName) throws Exception {
        //参数判空
        if (Objects.isNull(deploymentDetail) || StringUtils.isEmpty(deploymentDetail.getNamespace()) || StringUtils.isEmpty(deploymentDetail.getName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        String namespace = deploymentDetail.getNamespace();
        String name = deploymentDetail.getName();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);

        //根据名称获取Deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (Constant.HTTP_404 == depRes.getStatus()) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        //更新服务内的容器信息
        if (CollectionUtils.isNotEmpty(deploymentDetail.getContainers())) {
            dep = KubeServiceConvert.convertUpdateDeploymentData(dep, deploymentDetail.getContainers());
        }

        //更新亲和度
        Affinity affinity = new Affinity();
        List<AffinityDto> list = new ArrayList<>();
        list.add(deploymentDetail.getPodAntiAffinity());
        if (Objects.nonNull(deploymentDetail.getPodDisperse())) {
            AffinityDto aff = new AffinityDto();
            aff.setRequired(deploymentDetail.getPodDisperse().isRequired());
            aff.setLabel(TYPE_DEPLOYMENT + Constant.EQUAL + deploymentDetail.getName());
            list.add(aff);
        }
        if (Objects.nonNull(deploymentDetail.getPodGroupSchedule())) {
            AffinityDto aff = new AffinityDto();
            aff.setRequired(deploymentDetail.getPodGroupSchedule().isRequired());
            aff.setLabel(TYPE_DEPLOYMENT + Constant.EQUAL + deploymentDetail.getName());
            aff.setType(Constant.ANTIAFFINITY_TYPE_GROUP_SCHEDULE);
            list.add(aff);
        }
        List<AffinityDto> nodeAffinityList = deploymentDetail.getNodeAffinity();
        nodeAffinityList = setNamespaceLabelAffinity(namespace, nodeAffinityList);
        affinity = KubeAffinityConvert.convertAffinity(nodeAffinityList, deploymentDetail.getPodAffinity(), list);
        dep.getSpec().getTemplate().getSpec().setAffinity(affinity);
        //更新hostNetwork设置
        dep.getSpec().getTemplate().getSpec().setHostNetwork(deploymentDetail.isHostNetwork());

        //更新备注
        if(null != deploymentDetail.getAnnotation()){
            dep.getMetadata().getAnnotations().put("nephele/annotation", deploymentDetail.getAnnotation());
        }
        dep.getMetadata().getAnnotations().put("updateTimestamp", DateUtil.getCurrentUtcTimestamp());
        //更新HostAlias（自定义hosts file）
        if(CollectionUtils.isNotEmpty(deploymentDetail.getHostAliases())){
            dep.getSpec().getTemplate().getSpec().setHostAliases(deploymentDetail.getHostAliases());
        }else{
            dep.getSpec().getTemplate().getSpec().setHostAliases(null);
        }
        //设置修改服务的时候使用滚动升级，先杀pod，逐个pod升级
        RollingUpdateDeployment rollingUpdateDeployment = new RollingUpdateDeployment();
        rollingUpdateDeployment.setMaxSurge(DEFAULT_POD_MAX_SURGE);
        rollingUpdateDeployment.setMaxUnavailable(DEFAULT_POD_MAX_UNAVAILABLE);
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("RollingUpdate");
        strategy.setRollingUpdate(rollingUpdateDeployment);
        dep.getSpec().setStrategy(strategy);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse newRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(newRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_UPDATE_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil checkDeploymentName(String name, String namespace, boolean isTpl) throws Exception {
        //参数判空
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //判断名称是否是微服务组件名称
        String[] msfSvcNameArray = msfSvcName.split(CommonConstant.COMMA);
        List<String> nameList = Arrays.asList(msfSvcNameArray);
        if (nameList.contains(name)) {
            return ActionReturnUtil.returnSuccessWithData(true);
        }
        if (!isTpl) {
            Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            K8SClientResponse depRes = dpService.doDeploymentsByNamespace(namespace, null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus()) && depRes.getStatus() != Constant.HTTP_404) {
                UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            DeploymentList deployment = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
            if (Objects.nonNull(deployment)) {
                List<Deployment> deploymentList = deployment.getItems();
                if (CollectionUtils.isNotEmpty(deploymentList)) {
                    for (Deployment dep : deploymentList) {
                        if (name.equals(dep.getMetadata().getName())) {
                            return ActionReturnUtil.returnSuccessWithData(true);
                        }
                    }
                }
            }
            K8SClientResponse stsRes = statefulSetService.doSpecifyStatefulSet(namespace, null, null, null, HTTPMethod.GET, cluster);
            if(!HttpStatusUtil.isSuccessStatus(stsRes.getStatus())){
                UnversionedStatus sta = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(sta.getMessage());
            }
            StatefulSetList statefulSetList = JsonUtil.jsonToPojo(stsRes.getBody(), StatefulSetList.class);
            if (Objects.nonNull(statefulSetList)) {
                List<StatefulSet> list = statefulSetList.getItems();
                if (CollectionUtils.isNotEmpty(list)) {
                    for (StatefulSet dep : list) {
                        if (name.equals(dep.getMetadata().getName())) {
                            return ActionReturnUtil.returnSuccessWithData(true);
                        }
                    }
                }
            }

        }
        return ActionReturnUtil.returnSuccessWithData(false);
    }

    private List<AffinityDto> setNamespaceLabelAffinity(String namespace, List<AffinityDto> nodeAffinityList) throws Exception {
        NamespaceLocal namespaceLocal = this.namespaceLocalService.getNamespaceByName(namespace);
        String nodeLabel = namespaceService.getPrivatePartitionLabel(namespaceLocal.getTenantId(), namespace);
        AffinityDto nodeAffinity = new AffinityDto();
        nodeAffinity.setLabel(nodeLabel);
        nodeAffinity.setRequired(true);
        List<AffinityDto> nodeAffinitys = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(nodeAffinityList)) {
            nodeAffinitys = nodeAffinityList;
        }
        nodeAffinitys.add(nodeAffinity);
        return nodeAffinitys;
    }

    @Override
    public Map<String, String> createConfigMapInUpdate(String namespace, String depName, Cluster cluster, List<UpdateContainer> containers) throws Exception {
        Map<String, String> containerToConfigmapMap = new HashMap<String, String>();
        if (containers != null && !containers.isEmpty()) {
            for (UpdateContainer c : containers) {
                if (c.getConfigmap() != null) {
                    List<CreateConfigMapDto> configMaps = c.getConfigmap();
                    if (configMaps != null && configMaps.size() > 0) {
                        //配置文件清理根据该格式判断是否需要清理
                        String configmapName = depName + c.getName() + UUIDUtil.get16UUID();
                        this.createConfigMap(namespace, configmapName, depName, configMaps, cluster);
                        containerToConfigmapMap.put(c.getName(), configmapName);
                    }
                }
            }
        }
        return containerToConfigmapMap;
    }

    private void createConfigMap(String namespace, String configMapName, String depName, List<CreateConfigMapDto> configMaps, Cluster cluster) throws Exception {
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> bodys = new HashMap<String, Object>();
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("namespace", namespace);
        meta.put("name", configMapName);
        Map<String, Object> label = new HashMap<String, Object>();
        label.put("app", depName);
        meta.put("labels", label);
        bodys.put("metadata", meta);
        Map<String, Object> data = new HashMap<String, Object>();
        for (CreateConfigMapDto configMap : configMaps) {
            if (configMap != null && !StringUtils.isEmpty(configMap.getPath())) {
                if (Objects.isNull(configMap.getValue())){
                    throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_IS_EMPTY);
                }
                if (StringUtils.isEmpty(configMap.getFile())) {
                    data.put("config.json", configMap.getValue().toString());
                } else {
                    data.put(configMap.getFile() + "v" + configMap.getTag(), configMap.getValue().toString());
                }
            }
        }
        bodys.put("data", data);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-type", "application/json");
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.POST, headers, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(response.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
    }

    @Override
    public ActionReturnUtil updateLabels(String namespace, String deploymentName, Cluster cluster, Map<String, Object> label) throws Exception {
        if(label.isEmpty()){
            return ActionReturnUtil.returnError();
        }

        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, deploymentName, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }

        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        Map<String, Object> depLabels = dep.getMetadata().getLabels();
        for(Map.Entry<String, Object> entry : label.entrySet()){
            if (entry.getValue() != null){
                depLabels.put(entry.getKey(), entry.getValue());
            }else{
                depLabels.remove(entry.getKey());
            }
        }
        dep.getMetadata().setLabels(depLabels);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type","application/json");

        K8SClientResponse putRes = dpService.doSpecifyDeployment(namespace, deploymentName, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(putRes.getBody());
        }

        return ActionReturnUtil.returnSuccess();

    }

    @Override
    public ActionReturnUtil updateAnnotations(String namespace, String deploymentName, Cluster cluster, Map<String, Object> annotations) throws Exception {
        if(annotations.isEmpty()){
            return ActionReturnUtil.returnError();
        }

        if (Objects.isNull(cluster)){
            cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        }

        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, deploymentName, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(depRes.getBody());
        }

        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        Map<String, Object> depAnnotations = dep.getMetadata().getAnnotations();
        for(Map.Entry<String, Object> entry : annotations.entrySet()){
            if (entry.getValue() != null){
                depAnnotations.put(entry.getKey(), entry.getValue());
            }else{
                depAnnotations.remove(entry.getKey());
            }
        }
        dep.getMetadata().setAnnotations(depAnnotations);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type","application/json");

        K8SClientResponse putRes = dpService.doSpecifyDeployment(namespace, deploymentName, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(putRes.getBody());
        }

        return ActionReturnUtil.returnSuccess();

    }

    @Override
    public String getDeploymentDetailYaml(String namespace, String name,String path) throws Exception {
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        // 获取特定的deployment
        Map<String, Object> headers=new HashMap<String, Object>();
        headers.put("Accept","application/yaml");

        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, headers, null, HTTPMethod.GET, cluster);
        System.out.println(depRes.getBody());
        //  Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        // JSONObject json = JSONObject.fromObject(dep);
        //   return dep;
        Yaml yaml = new Yaml();
        FileWriter writer;
        try {
            File file2= new File(path);
            if (!file2.exists()) {
                try {
                    file2.createNewFile(); // 文件的创建，注意与文件夹创建的区别
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    LOGGER.warn("创建文件失败", name, namespace, e);
                }
            }

            writer = new FileWriter(file2,false);
            writer.write(yaml.dump(depRes.getBody()));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            LOGGER.warn("获取deployment:{} yaml失败, namespace:{}", name, namespace, e);
        }
        return yaml.dump(depRes.getBody());
    }

}
