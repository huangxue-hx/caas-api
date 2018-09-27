package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.CollectionUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.dataprivilege.DataPrivilegeDto;
import com.harmonycloud.dto.scale.AutoScaleDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.EventService;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.platform.bean.AppDetail;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.PodDetail;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.user.RoleLocalService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Created by anson on 18/8/7.
 */
@Service
public class StatefulSetsServiceImpl implements StatefulSetsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatefulSetsServiceImpl.class);

    @Autowired
    AutoScaleService autoScaleService;

    @Autowired
    StatefulSetService statefulSetService;

    @Autowired
    DeploymentsService deploymentsService;

    @Autowired
    FileUploadToContainerService fileUploadToContainerService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    RoleLocalService roleLocalService;

    @Autowired
    DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    EventService eventService;

    @Autowired
    UserService userService;

    @Autowired
    PodService podService;

    @Autowired
    ServicesService servicesService;

    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    DataPrivilegeService dataPrivilegeService;

    @Autowired
    PodDisruptionBudgetService pdbService;

    @Autowired
    PVCService pvcService;

    @Autowired
    ConfigMapService configMapService;

    @Autowired
    NamespaceService namespaceService;

    @Autowired
    ServiceService serviceService;

    @Autowired
    RouterService routerService;

    @Autowired
    PersistentVolumeClaimService PersistentVolumeClaimService;

    @Override
    public AppDetail getStatefulSetDetail(String namespace, String name) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        AppDetail res = null;
        Map<String, Object> bodys = new HashMap<String, Object>();

        // 获取cpaEvents
        bodys.clear();
        AutoScaleDto scaleDto  = autoScaleService.get(namespace, name);
        EventList hapEve = new EventList();
        if (scaleDto != null ){
            bodys.put("fieldSelector", "involvedObject.uid=" + scaleDto.getUid());
            K8SClientResponse hpaeRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(hpaeRes.getStatus())) {
                throw new MarsRuntimeException(hpaeRes.getBody());
            }
            hapEve = JsonUtil.jsonToPojo(hpaeRes.getBody(), EventList.class);
        }
        // 获取特定的statefulSet
        StatefulSet sta = statefulSetService.getStatefulSet(namespace, name, cluster);
        // 获取statefulSet的events
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.uid=" + sta.getMetadata().getUid());
        K8SClientResponse staRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(staRes.getStatus())) {
            throw new MarsRuntimeException(staRes.getBody());
        }
        EventList events = JsonUtil.jsonToPojo(staRes.getBody(), EventList.class);

        // 获取pod
        bodys.clear();
        bodys.put("labelSelector", K8sResultConvert.convertStatefulSetExpression(sta, name));
        K8SClientResponse podResponse = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podResponse.getStatus())) {
            throw new MarsRuntimeException(podResponse.getBody());
        }
        PodList pods = JsonUtil.jsonToPojo(podResponse.getBody(), PodList.class);
        //获取Service
        String serviceName = sta.getSpec().getServiceName();
        K8SClientResponse svcRespond = servicesService.doSepcifyService(namespace, serviceName, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(svcRespond.getStatus())) {
            throw new MarsRuntimeException(svcRespond.getBody());
        }
        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(svcRespond.getBody(), com.harmonycloud.k8s.bean.Service.class);
        res = K8sResultConvert.convertAppDetail(namespace,name,sta, service, events, hapEve, pods,cluster);
        res.setHostAliases(sta.getSpec().getTemplate().getSpec().getHostAliases());
        List<ContainerOfPodDetail> initContainers = K8sResultConvert.convertStatefulSetContainer(sta, sta.getSpec().getTemplate().getSpec().getInitContainers(), cluster);
        //去掉拉取依赖，依赖服务
        Iterator<ContainerOfPodDetail> it = initContainers.iterator();
        while(it.hasNext()){
            ContainerOfPodDetail container = it.next();
            if((name + "-svc").equals(container.getName()) || (name + "-vcs").equals(container.getName())){
                it.remove();
            }
        }
        res.setInitContainers(initContainers);
        res.setAutoScale(scaleDto);
        res.setClusterId(cluster.getId());
        res.setAliasNamespace(namespaceLocalService.getNamespaceByName(res.getNamespace()).getAliasName());
        res.setRealName(userService.getUser(res.getOwner()).getRealName());


        return dataPrivilegeHelper.filter(res);
    }

    public List<Map<String, Object>> listStatefulSets(String tenantId, String name, String namespace, String labels, String projectId, String clusterId) throws Exception{
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
                Cluster cluster = namespaceLocalService.getClusterByNamespaceName(ns[i]);
                //判断该namespace是否有权限
                if (clusterList.stream().noneMatch((c) -> c.getId().equals(cluster.getId()))) {
                    continue;
                }
                if (StringUtils.isNotBlank(clusterId) && !cluster.getId().equals(clusterId)) {
                    continue;
                }
                String aliasNamespace = namespaceLocalService.getNamespaceByName(ns[i]).getAliasName();
                try {
                    StatefulSetList statefulSet = statefulSetService.listStatefulSets(ns[i],null, bodys, cluster);
                    if (statefulSet != null && statefulSet.getItems().size() > 0) {
                        result.addAll(K8sResultConvert.convertAppList(statefulSet, cluster, aliasNamespace));
                    }
                }catch (Exception e) {
                    LOGGER.error("查询statefulSet列表失败，namespace：{}", ns[i], e);
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
        return result;
    }

    @Override
    public ActionReturnUtil createStatefulSet(StatefulSetDetailDto detail, String userName, String app, Cluster cluster, List<IngressDto> ingress) throws Exception {
        dataPrivilegeService.addResource(detail, app, DataResourceTypeEnum.APPLICATION);
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
                    configMapService.createConfigMap(detail.getNamespace(), detail.getName() + c.getName(), detail.getName(), configMaps, cluster);
                }
            }
        }

        //根据namespace获取节点label，并将label作为节点强制亲和
        if (Objects.nonNull(detail)) {
            List<AffinityDto> nodeAffinityList = detail.getNodeAffinity();
            nodeAffinityList = this.setNamespaceLabelAffinity(detail.getNamespace(), nodeAffinityList);
            detail.setNodeAffinity(nodeAffinityList);
        }

        K8SURL k8surl = new K8SURL();
        k8surl.setNamespace(detail.getNamespace()).setResource(Resource.DEPLOYMENT);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type", "application/json");
        Map<String, Object> bodys = new HashMap<String, Object>();

        com.harmonycloud.k8s.bean.Service service = K8sResultConvert.convertAppCreateOfService(detail, app, Constant.STATEFULSET);
        k8surl.setNamespace(detail.getNamespace()).setResource(Resource.SERVICE);

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
        String minAvailableValue = systemConfigService.findConfigValueByName(Constant.SYSTEM_CONFIG_PDB_MIN_AVAILABLE);
        K8SClientResponse pdbRes = null;

        StatefulSet statefulSet = K8sResultConvert.convertAppCreateForStatefulSet(detail, userName, app, ingress);
        //HostAlias-自定义 hosts file
        statefulSet.getSpec().getTemplate().getSpec().setHostAliases(detail.getHostAliases());
        ActionReturnUtil resultState = statefulSetService.createStatefulSet(detail.getNamespace(), statefulSet, cluster);
        if (!resultState.isSuccess()) {
            return resultState;
        }
        result = resultState.getData();

        pdbRes = pdbService.createPdbByType(detail.getNamespace(), statefulSet.getMetadata().getName() + Constant.PDB_SUFFIX, statefulSet.getSpec().getSelector(), Constant.PDB_TYPE_MIN_AVAILABLE, minAvailableValue, cluster);



        Map<String, Object> resMap = new HashMap<String, Object>();
        resMap.put("statefulSet", result);
        resMap.put("service", resS);

        if(!HttpStatusUtil.isSuccessStatus((pdbRes.getStatus()))){
            UnversionedStatus status = JsonUtil.jsonToPojo(pdbRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        com.harmonycloud.k8s.bean.PodDisruptionBudget resPdb =
                JsonUtil.jsonToPojo(pdbRes.getBody(), com.harmonycloud.k8s.bean.PodDisruptionBudget.class);
        resMap.put("podDisruptionBudget", resPdb);

        //pvc打标签
        Map<String, Object> pvcLabel = new HashedMap();
        pvcLabel.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET + CommonConstant.LINE + detail.getName(), detail.getName());
        if(CollectionUtils.isNotEmpty(detail.getContainers())){
            for(CreateContainerDto container : detail.getContainers()){
                if(container.getStorage() != null){
                    for(PersistentVolumeDto persistentVolumeDto : container.getStorage()){
                        if(StringUtils.isNotBlank(persistentVolumeDto.getPvcName())) {
                            ActionReturnUtil res = PersistentVolumeClaimService.updateLabel(persistentVolumeDto.getPvcName(), detail.getNamespace(), cluster, pvcLabel);
                            if(!res.isSuccess()){
                                return res;
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(detail.getInitContainers())){
            for(CreateContainerDto container : detail.getInitContainers()){
                if(container.getStorage() != null){
                    for(PersistentVolumeDto persistentVolumeDto : container.getStorage()){
                        if(StringUtils.isNotBlank(persistentVolumeDto.getPvcName())) {
                            ActionReturnUtil res = PersistentVolumeClaimService.updateLabel(persistentVolumeDto.getPvcName(), detail.getNamespace(), cluster, pvcLabel);
                            if(!res.isSuccess()){
                                return res;
                            }
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(statefulSet.getSpec().getVolumeClaimTemplates())){
            for(PersistentVolumeClaim template : statefulSet.getSpec().getVolumeClaimTemplates()){
                for(int i=0; i<statefulSet.getSpec().getReplicas(); i++) {
                    String pvcName = template.getMetadata().getName() + CommonConstant.LINE + statefulSet.getMetadata().getName() + CommonConstant.LINE + String.valueOf(i);
                    PersistentVolumeClaim pvc = pvcService.getPvcByName(detail.getNamespace(), pvcName, cluster);
                    if (pvc != null) {
                        Map<String, Object> labels = pvc.getMetadata().getLabels();
                        labels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.LABEL_KEY_STATEFULSET + CommonConstant.LINE + detail.getName(), detail.getName());
                        labels.put(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.AUTO_PROVISION_LABEL, CommonConstant.TRUE_STRING);
                        K8SClientResponse pvcResponse = pvcService.updatePvcByName(pvc, cluster);
                        if (!HttpStatusUtil.isSuccessStatus((pvcResponse.getStatus()))) {
                            UnversionedStatus status = JsonUtil.jsonToPojo(pvcResponse.getBody(), UnversionedStatus.class);
                            return ActionReturnUtil.returnErrorWithData(status.getMessage());
                        }
                    }
                }
            }
        }

        return ActionReturnUtil.returnSuccessWithData(resMap);
    }

    @Override
    public void startStatefulSet(String name, String namespace, String userName) throws Exception{
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> anno = new HashMap<>();
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        if (Objects.nonNull(statefulSet)) {
            anno = serviceService.updateAnnotation((Map<String, Object>) statefulSet.getMetadata().getAnnotations(), statefulSet.getMetadata().getName(), Constant.STARTING);
            statefulSet.getSpec().setReplicas(Integer.valueOf(anno.get("nephele/replicas").toString()));
            statefulSet.getMetadata().setAnnotations(anno);
            statefulSetService.updateStatefulSet(namespace, name, statefulSet, cluster);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }
    }

    @Override
    public void stopStatefulSet(String name, String namespace, String userName) throws Exception{
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> anno = new HashMap<>();
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        if (Objects.nonNull(statefulSet)) {
            anno = serviceService.updateAnnotation((Map<String, Object>) statefulSet.getMetadata().getAnnotations(), statefulSet.getMetadata().getName(), Constant.STOPPING);
            statefulSet.getSpec().setReplicas(0);
            statefulSet.getMetadata().setAnnotations(anno);
            statefulSetService.updateStatefulSet(namespace, name, statefulSet, cluster);
        }else{
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }
    }

    @Override
    public ActionReturnUtil deleteStatefulSet(String name, String namespace, String userName, Cluster cluster) throws Exception {
        StatefulSetDetailDto delObj = new StatefulSetDetailDto();
        delObj.setName(name);
        delObj.setNamespace(namespace);
        dataPrivilegeService.deleteResource(delObj);

        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", Constant.TYPE_STATEFULSET + CommonConstant.EQUALITY_SIGN + name);

        ActionReturnUtil res = serviceService.deleteServiceResource(name, namespace, cluster, queryP, Constant.STATEFULSET);
        if(!res.isSuccess()){
            return res;
        }


        // 获取statefulSet
        K8SClientResponse stsRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(stsRes.getStatus()) && stsRes.getStatus() != Constant.HTTP_404) {
            LOGGER.error("获取StatefulSet失败,StatefulSetName:{}, error:{}", name, stsRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
        }

        StatefulSet sts = new StatefulSet();
        if (stsRes.getStatus() != Constant.HTTP_404) {
            sts = JsonUtil.jsonToPojo(stsRes.getBody(), StatefulSet.class);
        }

        if (sts != null && sts.getSpec() != null) {
            // 删除statefulSet
            K8SClientResponse delRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.DELETE, cluster);
            if (!HttpStatusUtil.isSuccessStatus(delRes.getStatus()) && delRes.getStatus() != Constant.HTTP_404) {
                LOGGER.error("删除StatefulSet失败,StatefulSetName:{}, error:{}", name, delRes.getBody());
                throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_DELETE_FAILURE);
            }
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void scaleStatefulSet(String namespace, String name, Integer scale, String userName) throws Exception{
        if (scale == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE,"scale", true);
        }
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        Integer replicas = statefulSet.getSpec().getReplicas();
        statefulSet.getSpec().setReplicas(scale);
        statefulSet.getMetadata().setAnnotations(serviceService.updateAnnotationInScale(statefulSet.getMetadata().getAnnotations(), scale, replicas));
        statefulSetService.updateStatefulSet(namespace, name, statefulSet, cluster);
    }

    @Override
    public List<ContainerOfPodDetail> statefulSetContainer(String namespace, String name) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        List<ContainerOfPodDetail> containerOfPodDetails = K8sResultConvert.convertStatefulSetContainer(statefulSet, statefulSet.getSpec().getTemplate().getSpec().getContainers(), cluster);
        return containerOfPodDetails;
    }

    @Override
    public List<EventDetail> getStatefulSetEvents(String namespace, String name) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        K8SClientResponse statefulSetRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(statefulSetRes.getStatus())) {
            throw new MarsRuntimeException(statefulSetRes.getBody());
        }
        StatefulSet statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);
        Map<String, Object> statefulSetSelector = statefulSet.getSpec().getSelector().getMatchLabels();
        if (statefulSetSelector == null || statefulSetSelector.isEmpty()) {
            statefulSetSelector.put(Constant.TYPE_DEPLOYMENT, name);
        }
        List<EventDetail> allEvents = new ArrayList<EventDetail>();
        Map<String, Object> bodys = new HashMap<String, Object>();
        String selExp = K8sResultConvert.convertExpression(statefulSetSelector);
        String uid = statefulSet.getMetadata().getUid();
        bodys.put("labelSelector", selExp);
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            throw new MarsRuntimeException(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);

        // 循环podlist获取每个pod的事件
        bodys.clear();
        for (Pod pod : podList.getItems()) {
            bodys.put("fieldSelector", "involvedObject.uid=" + pod.getMetadata().getUid());
            K8SClientResponse podevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(podevRes.getStatus())) {
                throw new MarsRuntimeException(podevRes.getBody());
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
            throw new MarsRuntimeException(evRes.getBody());
        }
        EventList depeventList = JsonUtil.jsonToPojo(evRes.getBody(), EventList.class);
        if (depeventList.getItems() != null && depeventList.getItems().size() > 0) {
            allEvents.addAll(K8sResultConvert.convertPodEvent(depeventList.getItems()));
        }

        // cpaEvents
        bodys.clear();
        bodys.put("fieldSelector", "involvedObject.name=" + name + "-cpa");
        K8SClientResponse hpaevRes = eventService.doEventByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(hpaevRes.getStatus())) {
            throw new MarsRuntimeException(hpaevRes.getBody());
        }
        EventList hpaeventList = JsonUtil.jsonToPojo(hpaevRes.getBody(), EventList.class);
        if (hpaeventList.getItems() != null && hpaeventList.getItems().size() > 0) {
            allEvents.addAll(K8sResultConvert.convertPodEvent(hpaeventList.getItems()));
        }
        return K8sResultConvert.sortByDesc(allEvents);
    }

    @Override
    public List<PodDetail> podList(String name, String namespace) throws Exception{
        if (StringUtils.isEmpty(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        List<PodDetail> list = new ArrayList<>();
        try {
            list = podListForStatefulSet(name, namespace, cluster);
        }catch(MarsRuntimeException e){
            throw new MarsRuntimeException(e.getMessage());
        }
        return list;
    }

    @Override
    public ActionReturnUtil updateLabels(String namespace, String name, Cluster cluster, Map<String, Object> label) throws Exception {
        if(label.isEmpty()){
            return ActionReturnUtil.returnError();
        }

        K8SClientResponse stsRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(stsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(stsRes.getBody());
        }

        StatefulSet sts = JsonUtil.jsonToPojo(stsRes.getBody(), StatefulSet.class);
        Map<String, Object> stsLabels = sts.getMetadata().getLabels();
        for(Map.Entry<String, Object> entry : label.entrySet()){
            if (entry.getValue() != null){
                stsLabels.put(entry.getKey(), entry.getValue());
            }else{
                stsLabels.remove(entry.getKey());
            }
        }
        sts.getMetadata().setLabels(stsLabels);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(sts);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Content-type","application/json");

        K8SClientResponse putRes = statefulSetService.doSpecifyStatefulSet(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(putRes.getBody());
        }

        return ActionReturnUtil.returnSuccess();

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

    private List<PodDetail> podListForStatefulSet(String name, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();
        List<PodDetail> list = new LinkedList<PodDetail>();
        if(StringUtils.isNotBlank(name)) {
            bodys.put("labelSelector", Constant.TYPE_STATEFULSET + "=" + name);
        }
        K8SClientResponse podRes = podService.getPodByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
            throw new MarsRuntimeException(podRes.getBody());
        }
        PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
        for (Pod pod : podList.getItems()) {
            PodList PodDetailList = new PodList();
            List<Pod> pods = new ArrayList<>();
            String controllerRevisonName = new String();
            if (!StringUtils.isEmpty(pod.getMetadata().getLabels().get(Constant.TYPE_CONTROLLERREVISIONNAME).toString())){
                controllerRevisonName = pod.getMetadata().getLabels().get(Constant.TYPE_CONTROLLERREVISIONNAME).toString();
            }
            K8SURL url = new K8SURL();
            url.setNamespace(namespace).setName(controllerRevisonName).setResource(Resource.CONTROLLERREVISION);
            K8SClientResponse controllerRevisionRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
            if (!HttpStatusUtil.isSuccessStatus(controllerRevisionRes.getStatus())) {
                throw new MarsRuntimeException(controllerRevisionRes.getBody());
            }
            ControllerRevision controllerRevision = JsonUtil.jsonToPojo(controllerRevisionRes.getBody(),ControllerRevision.class);
            int tag = controllerRevision.getRevision();
            pods.add(pod);
            PodDetailList.setItems(pods);
            List<PodDetail> podDetails = K8sResultConvert.podListConvert(PodDetailList, "v" + tag);
            list.addAll(podDetails);
        }
        return list;
    }


}
