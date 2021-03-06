package com.harmonycloud.service.platform.convert;


import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.scale.HPADto;
import com.harmonycloud.dto.scale.ResourceMetricScaleDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.APIGroup;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.k8s.util.RandomNum;
import com.harmonycloud.service.application.StorageClassService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.util.BizUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.harmonycloud.service.platform.constant.Constant.*;

/**
 * @author jmi
 */
@Component
public class K8sResultConvert {
    public static final int TAG_LENGTH = 4;
    public static final String TAG_PATTERN = "v\\d-\\d";
    private static StorageClassService storageClassService;
    private static NamespaceLocalService namespaceLocalService;
    private static final Logger LOGGER = LoggerFactory.getLogger(K8sResultConvert.class);


    @Autowired
    public void setStorageClassService(StorageClassService storageClassService){
        K8sResultConvert.storageClassService = storageClassService;
    }
    @Autowired
    public void setNamespaceLocalService(NamespaceLocalService namespaceLocalService){
        K8sResultConvert.namespaceLocalService = namespaceLocalService;
    }

    public static AppDetail convertAppDetail(Deployment dep, ServiceList serviceList, EventList eventList,
                                             EventList hapEve, PodList podList) throws Exception {
        AppDetail appDetail = new AppDetail();

        // 封装返回值
        ObjectMeta meta = dep.getMetadata();
        appDetail.setName(meta.getName());
        appDetail.setNamespace(meta.getNamespace());
        appDetail.setVersion("v" + meta.getAnnotations().get("deployment.kubernetes.io/revision").toString());
        appDetail.setCreateTime(meta.getCreationTimestamp());
        appDetail.setInstance(dep.getSpec().getReplicas());
        appDetail.setOwner(meta.getLabels().get("nephele/user").toString());
        appDetail.setHostName(dep.getSpec().getTemplate().getSpec().getHostname());
        appDetail.setRestartPolicy(dep.getSpec().getTemplate().getSpec().getRestartPolicy());
        if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().isHostIPC())) {
            appDetail.setHostIPC(dep.getSpec().getTemplate().getSpec().isHostIPC());
        } else {
            appDetail.setHostIPC(false);
        }
        if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().isHostPID())) {
            appDetail.setHostPID(dep.getSpec().getTemplate().getSpec().isHostPID());
        } else {
            appDetail.setHostPID(false);
        }
        if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().isHostNetwork())) {
            appDetail.setHostNetwork(dep.getSpec().getTemplate().getSpec().isHostNetwork());
        } else {
            appDetail.setHostNetwork(false);
        }
        //亲和度
        if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity())) {
            //node 亲和度
            if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity())) {
                List<AffinityDto> list = KubeAffinityConvert.convertNodeAffinityDto(dep.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity());
                if (CollectionUtils.isNotEmpty(list)) {
                    appDetail.setNodeAffinity(list);
                }
            }

            //pod非亲和
            if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity().getPodAntiAffinity())) {
                List<AffinityDto> podAntiAffinityDtos = new ArrayList<>();
                podAntiAffinityDtos = KubeAffinityConvert.convertPodAntiAffinityDto(dep.getSpec().getTemplate().getSpec().getAffinity().getPodAntiAffinity());
                if (CollectionUtils.isNotEmpty(podAntiAffinityDtos)) {
                    for (AffinityDto affinityDto : podAntiAffinityDtos) {
                        if (StringUtils.isNotBlank(affinityDto.getNamespace())){
                            String namespaceAliasName = namespaceLocalService.getNamespaceByName(affinityDto.getNamespace()).getAliasName();
                            affinityDto.setNamespaceAliasName(namespaceAliasName);
                        }
                        if (affinityDto.getLabel().equals(Constant.TYPE_DEPLOYMENT + Constant.EQUAL + meta.getName()) &&
                                (StringUtils.isBlank(affinityDto.getNamespace()) || meta.getNamespace().equals(affinityDto.getNamespace()))) {
                            if (null != affinityDto.getType() && affinityDto.getType().equals(Constant.ANTIAFFINITY_TYPE_GROUP_SCHEDULE)) {
                                appDetail.setPodGroupSchedule(affinityDto);
                            } else {
                                appDetail.setPodDisperse(affinityDto);
                            }
                        } else {
                            appDetail.setPodAntiAffinity(affinityDto);
                        }
                    }
                }
            }

            // pod 亲和

            if (Objects.nonNull(dep.getSpec().getTemplate().getSpec().getAffinity().getPodAffinity())) {
                List<AffinityDto> podAffinityDtos = new ArrayList<>();
                podAffinityDtos = KubeAffinityConvert.convertPodAffinityDto(dep.getSpec().getTemplate().getSpec().getAffinity().getPodAffinity());
                if (StringUtils.isNotBlank(podAffinityDtos.get(0).getNamespace())){
                    String namespaceAliasName = namespaceLocalService.getNamespaceByName(podAffinityDtos.get(0).getNamespace()).getAliasName();
                    podAffinityDtos.get(0).setNamespaceAliasName(namespaceAliasName);
                }
                appDetail.setPodAffinity(podAffinityDtos.get(0));
            }
        }
        appDetail.setMsf(false);
        Map<String, Object> labels = new HashMap<String, Object>();
        for (Map.Entry<String, Object> m : meta.getLabels().entrySet()) {
            if (m.getKey().indexOf("nephele/") > 0) {
                labels.put(m.getKey(), m.getValue());
            }
            if ((Constant.NODESELECTOR_LABELS_PRE + "springcloud").equals(m.getKey())) {
                appDetail.setMsf(true);
            }
        }
        appDetail.setStatus(getDeploymentStatus(dep));

        if (CollectionUtils.isNotEmpty(serviceList.getItems())) {
            com.harmonycloud.k8s.bean.Service service = serviceList.getItems().get(0);
            appDetail.setClusterIP(service.getSpec().getClusterIP());
            appDetail.setServiceAddress(service.getMetadata().getName()+ "." +service.getMetadata().getNamespace());
            appDetail.setInternalPorts(service.getSpec().getPorts());
            if (StringUtils.isEmpty(service.getSpec().getSessionAffinity())) {
                appDetail.setSessionAffinity("false");
            } else {
                appDetail.setSessionAffinity(service.getSpec().getSessionAffinity());
            }
        }
        if(CollectionUtils.isNotEmpty(hapEve.getItems())) {
            List<EventDetail> eventDetailList = K8sResultConvert.convertPodEvent(hapEve.getItems());
            appDetail.setAutoScalingHistory(K8sResultConvert.sortByDesc(eventDetailList));
        }

        List<PodDetail> pods = new ArrayList<PodDetail>();
        for (int i = 0; i < podList.getItems().size(); i++) {
            Pod pod = podList.getItems().get(i);
            PodDetail podDetail = new PodDetail(pod.getMetadata().getName(), pod.getMetadata().getNamespace(),
                    pod.getStatus().getPhase(), pod.getStatus().getPodIP(), pod.getStatus().getHostIP(),
                    pod.getStatus().getStartTime());
            pods.add(podDetail);
        }
        appDetail.setPodList(pods);
        List<EventDetail> events = new ArrayList<EventDetail>();
        for (int i = 0; i < eventList.getItems().size(); i++) {
            Event event = eventList.getItems().get(i);
            EventDetail eventDetail = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(),
                    event.getLastTimestamp(), event.getCount(), event.getType());
            events.add(eventDetail);
        }
        appDetail.setEvents(events);

        if (dep.getSpec().getTemplate().getMetadata().getLabels().containsKey(Constant.TYPE_DEPLOY_VERSION)){
            appDetail.setDeployVersion((dep.getSpec().getTemplate().getMetadata().getLabels().get(Constant.TYPE_DEPLOY_VERSION).toString()));
        }

        convertAnnotation(meta.getAnnotations(), dep.getSpec().getTemplate().getSpec().getContainers(), appDetail);
        return appDetail;
    }

    public static AppDetail convertAppDetail(String namespace,String name,StatefulSet sta, Service service, EventList eventList, EventList hapEve, PodList podList, Cluster cluster) throws Exception {
        AppDetail appDetail = new AppDetail();
        appDetail.setServiceType(Constant.STATEFULSET);
        // 封装返回值
        Map<String, Object> bodys = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(name)) {
            bodys.put("labelSelector", Constant.TYPE_STATEFULSET + "=" + name);
        }
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(sta.getStatus().getUpdateRevision()).setResource(Resource.CONTROLLERREVISION);
        K8SClientResponse controllerRevisionRes = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(controllerRevisionRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(controllerRevisionRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        ControllerRevision controllerRevision = JsonUtil.jsonToPojo(controllerRevisionRes.getBody(),ControllerRevision.class);
        if(controllerRevision.getRevision() != null) {
            int version = controllerRevision.getRevision();
            appDetail.setVersion("v" + version);
        }
        ObjectMeta meta = sta.getMetadata();
        appDetail.setName(meta.getName());
        appDetail.setNamespace(meta.getNamespace());
        appDetail.setCreateTime(meta.getCreationTimestamp());
        appDetail.setInstance(sta.getSpec().getReplicas());
        appDetail.setOwner(meta.getLabels().get("nephele/user").toString());
        appDetail.setHostName(sta.getSpec().getTemplate().getSpec().getHostname());
        appDetail.setRestartPolicy(sta.getSpec().getTemplate().getSpec().getRestartPolicy());
        appDetail.setPodManagementPolicy(sta.getSpec().getPodManagementPolicy());
        if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().isHostIPC())) {
            appDetail.setHostIPC(sta.getSpec().getTemplate().getSpec().isHostIPC());
        } else {
            appDetail.setHostIPC(false);
        }
        if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().isHostPID())) {
            appDetail.setHostPID(sta.getSpec().getTemplate().getSpec().isHostPID());
        } else {
            appDetail.setHostPID(false);
        }
        if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().isHostNetwork())) {
            appDetail.setHostNetwork(sta.getSpec().getTemplate().getSpec().isHostNetwork());
        } else {
            appDetail.setHostNetwork(false);
        }
        //亲和度
        if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().getAffinity())) {
            //node 亲和度
            if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity())) {
                List<AffinityDto> list = KubeAffinityConvert.convertNodeAffinityDto(sta.getSpec().getTemplate().getSpec().getAffinity().getNodeAffinity());
                if (CollectionUtils.isNotEmpty(list)) {
                    appDetail.setNodeAffinity(list);
                }
            }

            //pod非亲和
            if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().getAffinity().getPodAntiAffinity())) {
                List<AffinityDto> podAntiAffinityDtos = new ArrayList<>();
                podAntiAffinityDtos = KubeAffinityConvert.convertPodAntiAffinityDto(sta.getSpec().getTemplate().getSpec().getAffinity().getPodAntiAffinity());
                if (CollectionUtils.isNotEmpty(podAntiAffinityDtos)) {
                    for (AffinityDto affinityDto : podAntiAffinityDtos) {
                        if (StringUtils.isNotBlank(affinityDto.getNamespace())){
                            String namespaceAliasName = namespaceLocalService.getNamespaceByName(affinityDto.getNamespace()).getAliasName();
                            affinityDto.setNamespaceAliasName(namespaceAliasName);
                        }
                        if (affinityDto.getLabel().equals(Constant.TYPE_STATEFULSET + Constant.EQUAL + meta.getName())) {
                            if(null != affinityDto.getType() && affinityDto.getType().equals(Constant.ANTIAFFINITY_TYPE_GROUP_SCHEDULE)){
                                appDetail.setPodGroupSchedule(affinityDto);
                            }else {
                                appDetail.setPodDisperse(affinityDto);
                            }
                        } else {
                            appDetail.setPodAntiAffinity(affinityDto);
                        }
                    }
                }
            }

            // pod 亲和
            if (Objects.nonNull(sta.getSpec().getTemplate().getSpec().getAffinity().getPodAffinity())) {
                List<AffinityDto> podAffinityDtos = new ArrayList<>();
                podAffinityDtos = KubeAffinityConvert.convertPodAffinityDto(sta.getSpec().getTemplate().getSpec().getAffinity().getPodAffinity());
                if (StringUtils.isNotBlank(podAffinityDtos.get(0).getNamespace())){
                    String namespaceAliasName = namespaceLocalService.getNamespaceByName(podAffinityDtos.get(0).getNamespace()).getAliasName();
                    podAffinityDtos.get(0).setNamespaceAliasName(namespaceAliasName);
                }
                appDetail.setPodAffinity(podAffinityDtos.get(0));
            }
        }
        appDetail.setMsf(false);
        Map<String, Object> labels = new HashMap<String, Object>();
        for (Map.Entry<String, Object> m : meta.getLabels().entrySet()) {
            if (m.getKey().indexOf("nephele/") > 0) {
                labels.put(m.getKey(), m.getValue());
            }
            if ((Constant.NODESELECTOR_LABELS_PRE + "springcloud").equals(m.getKey())) {
                appDetail.setMsf(true);
            }
        }
        appDetail.setStatus(getStatefulSetStatus(sta));
        appDetail.setClusterIP(service.getSpec().getClusterIP());
        appDetail.setServiceAddress(service.getMetadata().getName() + "." + service.getMetadata().getNamespace());
        appDetail.setInternalPorts(service.getSpec().getPorts());
        if (StringUtils.isEmpty(service.getSpec().getSessionAffinity())) {
            appDetail.setSessionAffinity("false");
        } else {
            appDetail.setSessionAffinity(service.getSpec().getSessionAffinity());
        }
        if(CollectionUtils.isNotEmpty(hapEve.getItems())) {
            List<EventDetail> eventDetailList = K8sResultConvert.convertPodEvent(hapEve.getItems());
            appDetail.setAutoScalingHistory(K8sResultConvert.sortByDesc(eventDetailList));
        }

        List<PodDetail> pods = new ArrayList<PodDetail>();
        for (int i = 0; i < podList.getItems().size(); i++) {
            Pod pod = podList.getItems().get(i);
            PodDetail podDetail = new PodDetail(pod.getMetadata().getName(), pod.getMetadata().getNamespace(),
                    pod.getStatus().getPhase(), pod.getStatus().getPodIP(), pod.getStatus().getHostIP(),
                    pod.getStatus().getStartTime());
            pods.add(podDetail);
        }
        appDetail.setPodList(pods);
        List<EventDetail> events = new ArrayList<EventDetail>();
        for (int i = 0; i < eventList.getItems().size(); i++) {
            Event event = eventList.getItems().get(i);
            EventDetail eventDetail = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(),
                    event.getLastTimestamp(), event.getCount(), event.getType());
            events.add(eventDetail);
        }
        appDetail.setEvents(events);

        convertAnnotation(meta.getAnnotations(), sta.getSpec().getTemplate().getSpec().getContainers(), appDetail);
        return appDetail;
    }

    private static void convertAnnotation(Map<String, Object> annotations, List<Container> containers, AppDetail appDetail){
        if (!annotations.containsKey("updateTimestamp")
                || StringUtils.isEmpty(annotations.get("updateTimestamp").toString())) {
            appDetail.setUpdateTime(appDetail.getCreateTime());
        } else {
            appDetail.setUpdateTime(annotations.get("updateTimestamp").toString());
        }
        if (annotations != null && annotations.containsKey("nephele/annotation")) {
            appDetail.setAnnotation(annotations.get("nephele/annotation").toString());
        }
        //labels
        Map<String, Object> labelMap = new HashMap<String, Object>();
        String labs = null;
        if (annotations != null && annotations.containsKey("nephele/labels")) {
            labs = annotations.get("nephele/labels").toString();
        }
        if (!StringUtils.isEmpty(labs)) {
            String[] arrLabel = labs.split(",");
            for (String l : arrLabel) {
                String[] tmp = l.split("=");
                labelMap.put(tmp[0], tmp[1]);
            }
            appDetail.setLabels(labelMap);
        }

        String repoUrl = (String)annotations.get("pulldep/repoUrl");
        String branch = (String)annotations.get("pulldep/branch");
        String tag = (String)annotations.get("pulldep/tag");
        String containerName = (String)annotations.get("pulldep/container");
        branch = (StringUtils.isBlank(branch) ? tag : branch);
        String containerMonutPath = null;

        List<Container> collect = containers.stream().filter(c -> {
            return c.getName().equals(containerName);
        }).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(collect)){
            List<VolumeMount> volumeMounts = collect.get(0).getVolumeMounts();
            List<VolumeMount> empty = volumeMounts.stream().filter(v -> {
                return v.getName().equals("empty-deploy");
            }).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(empty)){
                containerMonutPath = empty.get(0).getMountPath();
            }
        }

        Map<String, Object> pullDependence = new HashMap<>();
        pullDependence.put("repoUrl", repoUrl);
        pullDependence.put("branch", branch);
        pullDependence.put("containerName", containerName);
        pullDependence.put("containerMonutPath", containerMonutPath);
        appDetail.setPullDependence(pullDependence);


        String serviceDependence = (String)annotations.get("svcDepend/name");

        appDetail.setServiceDependence(serviceDependence);
    }

    public static List<PodDetail> podListConvert(PodList podList, String tag) throws Exception {
        List<Pod> pods = podList.getItems();
        List<PodDetail> res = new ArrayList<PodDetail>();
        for (int i = 0; i < pods.size(); i++) {
            PodDetail podDetail = new PodDetail(pods.get(i).getMetadata().getName(),
                    pods.get(i).getMetadata().getNamespace(), pods.get(i).getStatus().getPhase(),
                    pods.get(i).getStatus().getPodIP(), pods.get(i).getStatus().getHostIP(),
                    pods.get(i).getStatus().getStartTime());
            podDetail.setTag(tag);
            podDetail.setTerminating(pods.get(i).getMetadata().getDeletionTimestamp() != null);
            List<ContainerWithStatus> containers = new ArrayList<ContainerWithStatus>();
            List<ContainerStatus> containerStatues = pods.get(i).getStatus().getContainerStatuses();

            //istio版本标签
            if (pods.get(i).getMetadata().getLabels().containsKey(Constant.TYPE_DEPLOY_VERSION)){
                podDetail.setDeployVersion(pods.get(i).getMetadata().getLabels().get(Constant.TYPE_DEPLOY_VERSION).toString());
            }

            //flag的作用标记除了运行状态以外的pod状态：1为等待状态；2为terminated状态
            String podStatus = null;
            if (CollectionUtils.isNotEmpty(containerStatues)) {
                for (ContainerStatus cs : containerStatues) {
                    ContainerWithStatus containerWithStatus = new ContainerWithStatus();
                    containerWithStatus.setName(cs.getName());
                    containerWithStatus.setRestartCount(cs.getRestartCount());
                    if (Objects.nonNull(cs.getState().getWaiting())) {
                        podStatus = Constant.WAITING;
                        containerWithStatus.setState(Constant.WAITING);
                        containerWithStatus.setReason(cs.getState().getWaiting().getReason());
                        containerWithStatus.setMessage(cs.getState().getWaiting().getMessage());
                    } else if (Objects.nonNull(cs.getState().getRunning())) {
                        containerWithStatus.setState(Constant.RUNNING);
                        containerWithStatus.setStartedAt(cs.getState().getRunning().getStartedAt());
                    } else {
                        if (StringUtils.isEmpty(podStatus) || !Constant.WAITING.equals(podStatus)) {
                            podStatus = Constant.TERMINATED;
                        }
                        containerWithStatus.setState(Constant.TERMINATED);
                        containerWithStatus.setExitCode(cs.getState().getTerminated().getExitCode());
                        containerWithStatus.setSignal(cs.getState().getTerminated().getSignal());
                        containerWithStatus.setMessage(cs.getState().getTerminated().getMessage());
                        containerWithStatus.setReason(cs.getState().getTerminated().getReason());
                        containerWithStatus.setStartedAt(cs.getState().getTerminated().getStartedAt());
                        containerWithStatus.setFinishedAt(cs.getState().getTerminated().getFinishedAt());
                    }
                    containers.add(containerWithStatus);
                }
            }
            podDetail.setContainers(containers);
            if (StringUtils.isNotEmpty(podStatus)) {
                podDetail.setStatus(podStatus);
            }
            res.add(podDetail);
        }
        return res;
    }

    public static String convertDeploymentExpression(Deployment dep, String name) throws Exception{
        Map<String, Object> deploymentSelector = dep.getSpec().getSelector().getMatchLabels();
        if (deploymentSelector == null || deploymentSelector.isEmpty()) {
            deploymentSelector.put(Constant.TYPE_DEPLOYMENT, name);
        }
        return convertExpression(deploymentSelector);
    }

    public static String convertStatefulSetExpression(StatefulSet sta, String name) throws Exception{
        Map<String, Object> StatefulSetSelector = sta.getSpec().getSelector().getMatchLabels();
        if (StatefulSetSelector == null || StatefulSetSelector.isEmpty()) {
            StatefulSetSelector.put(Constant.TYPE_STATEFULSET, name);
        }
        return convertExpression(StatefulSetSelector);
    }

    public static String convertExpression(Map<String, Object> selector) throws Exception {
        // 获取所有的map的key和value，拼接成字符串
        String selExpression = "";
        for (Map.Entry<String, Object> m : selector.entrySet()) {
            selExpression += m.getKey() + '=' + m.getValue() + ',';
        }
        selExpression = selExpression.substring(0, selExpression.length() - 1);
        return selExpression;
    }

    public static List<EventDetail> convertPodEvent(List<Event> events) throws Exception {
        List<EventDetail> res = new ArrayList<EventDetail>();
        for (Event event : events) {
            EventDetail eventDetail = new EventDetail(event.getReason(), event.getMessage(), event.getFirstTimestamp(),
                    event.getLastTimestamp(), event.getCount(), event.getType());
            eventDetail.setInvolvedObject(event.getInvolvedObject());
            res.add(eventDetail);
        }
        return res;
    }

    public static List<EventDetail> sortByDesc(List<EventDetail> list) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (EventDetail detail : list) {
            Long startTime = sdf.parse(detail.getFirstTimestamp()).getTime();
            Long endTime = sdf.parse(detail.getLastTimestamp()).getTime();
            Long interval = endTime - startTime;
            interval = interval / 1000;
            if (interval < 60) {
                detail.setSpan(interval);
                detail.setSpanMetric(Constant.SECONDS);
            } else if (interval >= 60 && interval < 3600) {
                detail.setSpan(interval / 60);
                detail.setSpanMetric(Constant.MINUTES);
            } else if (interval >= 3600 && interval < 86400) {
                detail.setSpan(interval / 3600);
                detail.setSpanMetric(Constant.HOURS);
            } else {
                detail.setSpan(interval / 86400);
                detail.setSpanMetric(Constant.DAYS);
            }
        }

        // 对last时间进行倒序
        Collections.sort(list, new Comparator<EventDetail>() {

            @Override
            public int compare(EventDetail o1, EventDetail o2) {
                try {
                    return Long.valueOf(sdf.parse(o2.getLastTimestamp()).getTime()).compareTo(Long.valueOf(sdf.parse(o1.getLastTimestamp()).getTime()));
                } catch (ParseException e) {
                    LOGGER.warn("sort失败", e);
                    return 0;
                }
            }

        });
        return list;
    }


    @SuppressWarnings("unchecked")
    public static List<ContainerOfPodDetail> convertContainer(Deployment deployment) throws Exception {
        List<ContainerOfPodDetail> res = new ArrayList<ContainerOfPodDetail>();
        List<Container> containers = deployment.getSpec().getTemplate().getSpec().getContainers();
        if (containers != null && containers.size() > 0) {
            for (Container ct : containers) {
                ContainerOfPodDetail cOfPodDetail = new ContainerOfPodDetail(ct.getName(), ct.getImage(),
                        ct.getLivenessProbe(), ct.getReadinessProbe(), ct.getPorts(), ct.getArgs(), ct.getEnv(),
                        ct.getCommand());
                if (ct.getImagePullPolicy() != null) {
                    cOfPodDetail.setImagePullPolicy(ct.getImagePullPolicy());
                }
                cOfPodDetail.setDeploymentName(deployment.getMetadata().getName());
                cOfPodDetail.setSecurityContext(convertSecurityContext(ct.getSecurityContext()));
                //转换资源的request和limit
                convertResource(cOfPodDetail, ct.getResources());

                List<VolumeMount> volumeMounts = ct.getVolumeMounts();
                List<VolumeMountExt> vms = new ArrayList<VolumeMountExt>();
                if (volumeMounts != null && volumeMounts.size() > 0) {
                    for (VolumeMount vm : volumeMounts) {
                        VolumeMountExt vmExt = new VolumeMountExt(vm.getName(), vm.isReadOnly(), vm.getMountPath(),
                                vm.getSubPath());
                        for (Volume volume : deployment.getSpec().getTemplate().getSpec().getVolumes()) {
                            if (vm.getName().equals(volume.getName())) {
                                if (volume.getSecret() != null) {
                                    vmExt.setType("secret");
                                } else if (volume.getPersistentVolumeClaim() != null) {
                                    vmExt.setType(Constant.VOLUME_TYPE_PVC);
                                    vmExt.setPvcname(volume.getPersistentVolumeClaim().getClaimName());
                                } else if (volume.getEmptyDir() != null) {
                                    vmExt.setType("emptyDir");
                                    if (volume.getEmptyDir() != null) {
                                        vmExt.setEmptyDir(volume.getEmptyDir().getMedium());
                                    } else {
                                        vmExt.setEmptyDir(null);
                                    }

                                    if (vm.getName().indexOf("logdir") == 0) {
                                        vmExt.setType("logDir");
                                    }
                                } else if (volume.getConfigMap() != null) {
                                    Map<String, Object> configMap = new HashMap<String, Object>();
                                    configMap.put("name", volume.getConfigMap().getName());

                                    String mountPath = null;
                                    if(vm.getMountPath().contains(vm.getSubPath())){
                                        int lastIndexOf = vm.getMountPath().lastIndexOf("/");
                                        String subLastPath = vm.getMountPath().substring(lastIndexOf + 1);
                                        if(subLastPath.equals(vm.getSubPath())){
                                            mountPath = vm.getMountPath().substring(0, lastIndexOf);
                                        }

                                    }

                                    configMap.put("path", mountPath);
                                    vmExt.setType("configMap");
                                    vmExt.setConfigMapName(volume.getConfigMap().getName());
                                    vmExt.setMountPath(mountPath);
                                } else if (volume.getHostPath() != null) {
                                    vmExt.setType("hostPath");
                                    vmExt.setHostPath(volume.getHostPath().getPath());
                                    if (vm.getName().indexOf("logdir") == 0) {
                                        vmExt.setType("logDir");
                                    }
                                }
                                if (vmExt.getReadOnly() == null) {
                                    vmExt.setReadOnly(false);
                                }
                                vms.add(vmExt);
                                break;
                            }
                        }
                    }
//                    boolean isSyncTime = vms.stream().anyMatch(vm -> vm.getName().contains(Constant.VOLUME_SYNC_TIME_ZONE_NAME));
                    cOfPodDetail.setStorage(vms);
//                    cOfPodDetail.setTimeZone(isSyncTime);
                }
                res.add(cOfPodDetail);
            }
        }
        return res;
    }

    public static List<ContainerOfPodDetail> convertDeploymentContainer(Deployment deployment, List<Container> containers, Cluster cluster) throws Exception {
        return convertContainer(deployment, containers, cluster);
    }

    public static List<ContainerOfPodDetail> convertStatefulSetContainer(StatefulSet statefulSet, List<Container> containers, Cluster cluster) throws Exception {
        return convertContainer(statefulSet, containers, cluster);
    }

    public static List<ContainerOfPodDetail> convertReplicaSetContainer(ReplicaSet replicaSet, List<Container> containers, Cluster cluster) throws Exception {
        return convertContainer(replicaSet, containers, cluster);
    }

    private static List<ContainerOfPodDetail> convertContainer(Object obj, List<Container> containers, Cluster cluster) throws Exception {
        List<ContainerOfPodDetail> res = new ArrayList<ContainerOfPodDetail>();
        PodTemplateSpec podTemplateSpec = null;
        List<PersistentVolumeClaim> volumeClaimTemplate = null;
        String name = null;
        String namespace = null;
        Map<String, Object> annotations = null;
        if(obj instanceof Deployment){
            Deployment deployment = (Deployment)obj;
            annotations = deployment.getMetadata().getAnnotations();
            DeploymentSpec deploymentSpec = deployment.getSpec();
            podTemplateSpec = deploymentSpec.getTemplate();
            name = deployment.getMetadata().getName();
            namespace = deployment.getMetadata().getNamespace();
        }else if(obj instanceof StatefulSet){
            StatefulSet statefulSet = (StatefulSet)obj;
            StatefulSetSpec statefulSetSpec = statefulSet.getSpec();
            podTemplateSpec = statefulSetSpec.getTemplate();
            volumeClaimTemplate = statefulSetSpec.getVolumeClaimTemplates();
            name = statefulSet.getMetadata().getName();
            namespace = statefulSet.getMetadata().getNamespace();
        }else if(obj instanceof ReplicaSet){
            ReplicaSet replicaSet = (ReplicaSet)obj;
            ReplicaSetSpec replicaSetSpec = replicaSet.getSpec();
            podTemplateSpec = replicaSetSpec.getTemplate();
            name = replicaSet.getMetadata().getName();
            namespace = replicaSet.getMetadata().getNamespace();
        }
        if (containers != null && containers.size() > 0) {
            List<StorageClassDto> storageClassList = storageClassService.listStorageClass(cluster.getId());
            Map<String, StorageClassDto> storageClassMap = storageClassList.stream().collect(Collectors.toMap(StorageClassDto::getName, storageClass -> storageClass));
            for (Container ct : containers) {
                ContainerOfPodDetail cOfPodDetail = new ContainerOfPodDetail(ct.getName(), ct.getImage(),
                        ct.getLivenessProbe(), ct.getReadinessProbe(), ct.getPorts(), ct.getArgs(), ct.getEnv(),
                        ct.getCommand());
                if (ct.getImagePullPolicy() != null) {
                    cOfPodDetail.setImagePullPolicy(ct.getImagePullPolicy());
                }
                cOfPodDetail.setDeploymentName(name);
                cOfPodDetail.setSecurityContext(convertSecurityContext(ct.getSecurityContext()));
                //转换资源的request和limit
                convertResource(cOfPodDetail, ct.getResources());
                List<VolumeMount> volumeMounts = ct.getVolumeMounts();
                List<VolumeMountExt> vms = new ArrayList<VolumeMountExt>();
                if (volumeMounts != null && volumeMounts.size() > 0) {
                    for (VolumeMount vm : volumeMounts) {
                        VolumeMountExt vmExt = new VolumeMountExt(vm.getName(), vm.isReadOnly(), vm.getMountPath(),
                                vm.getSubPath());
                        boolean isAutoProvided = false;
                        if(CollectionUtils.isNotEmpty(volumeClaimTemplate)){
                            for(PersistentVolumeClaim pvc : volumeClaimTemplate){
                                if(vm.getName().equals(pvc.getMetadata().getName())){
                                    vmExt.setStorageClassName(pvc.getSpec().getStorageClassName());
                                    StorageClassDto storageClass = storageClassMap.get(pvc.getSpec().getStorageClassName());
                                    if(storageClass != null) {
                                        vmExt.setType(storageClass.getType());
                                        vmExt.setStorageClassType(storageClass.getType());
                                    }
                                    vmExt.setPvcname(pvc.getMetadata().getName());
                                    Map<String, Object> request = (Map<String, Object>)pvc.getSpec().getResources().getRequests();
                                    if(request != null && request.get(CommonConstant.STORAGE) != null) {
                                        vmExt.setCapacity((String)request.get(CommonConstant.STORAGE));
                                    }
                                    isAutoProvided = true;
                                    vms.add(vmExt);
                                    break;
                                }
                            }
                        }
                        if(!isAutoProvided) {
                            for (Volume volume : podTemplateSpec.getSpec().getVolumes()) {
                                if (vm.getName().equals(volume.getName())) {
                                    if (volume.getSecret() != null) {
                                        vmExt.setType("secret");
                                    } else if (volume.getPersistentVolumeClaim() != null) {
                                        PersistentVolumeClaim persistentVolumeClaim = getPvcByName(namespace, volume.getPersistentVolumeClaim().getClaimName(), cluster);
                                        if (persistentVolumeClaim != null) {
                                            String storageClassName = (String) persistentVolumeClaim.getMetadata().getAnnotations().get("volume.beta.kubernetes.io/storage-class");
                                            if(StringUtils.isBlank(storageClassName)){
                                                storageClassName = persistentVolumeClaim.getSpec().getStorageClassName();
                                            }
                                            if(StringUtils.isBlank(storageClassName)){
                                                storageClassName = (String)persistentVolumeClaim.getMetadata().getAnnotations().get(Constant.NODESELECTOR_LABELS_PRE + CommonConstant.STORAGECLASS);
                                            }
                                            vmExt.setStorageClassName(storageClassName);
                                            StorageClassDto storageClass = storageClassMap.get(storageClassName);
                                            if(storageClass != null) {
                                                vmExt.setType(storageClass.getType());
                                                vmExt.setStorageClassType(storageClass.getType());
                                            }
                                        }
                                        vmExt.setPvcname(volume.getPersistentVolumeClaim().getClaimName());
                                    } else if (volume.getEmptyDir() != null) {
                                        vmExt.setType("emptyDir");
                                        if (volume.getEmptyDir() != null) {
                                            vmExt.setEmptyDir(volume.getEmptyDir().getMedium());
                                        } else {
                                            vmExt.setEmptyDir(null);
                                        }
                                        vmExt.setCapacity(volume.getEmptyDir().getSizeLimit());

                                        if (vm.getName().indexOf("logdir") == 0) {
                                            vmExt.setType("logDir");
                                        }
                                    } else if (volume.getConfigMap() != null) {
                                        Map<String, Object> configMap = new HashMap<String, Object>();
                                        configMap.put("name", volume.getConfigMap().getName());
                                        vmExt.setOldName(vmExt.getName());
                                        if(volume.getName().length()>TAG_LENGTH){
                                            //volume名称为版本号结尾的是之前版本格式，改成新版configmapId结尾的格式
                                            String tag = volume.getName().substring(volume.getName().length() - TAG_LENGTH);
                                            if(tag.matches(TAG_PATTERN)){
                                                if(annotations != null && (annotations.get("configmapid-" + ct.getName()) != null)){
                                                    vmExt.setName(volume.getName().replace(tag, "") + "-" + annotations.get("configmapid-" + ct.getName()).toString());
                                                }
                                            }
                                        }
                                        String mountPath = null;
                                        if (vm.getMountPath().contains(vm.getSubPath())) {
                                            int lastIndexOf = vm.getMountPath().lastIndexOf("/");
                                            String subLastPath = vm.getMountPath().substring(lastIndexOf + 1);
                                            if (subLastPath.equals(vm.getSubPath())) {
                                                mountPath = vm.getMountPath().substring(0, lastIndexOf+1);
                                            }

                                        }

                                        configMap.put("path", mountPath);
                                        vmExt.setType("configMap");
                                        vmExt.setConfigMapName(volume.getConfigMap().getName());
                                        vmExt.setMountPath(mountPath);
                                    } else if (volume.getHostPath() != null) {
                                        vmExt.setType("hostPath");
                                        vmExt.setHostPath(volume.getHostPath().getPath());
                                        if (vm.getName().indexOf("logdir") == 0) {
                                            vmExt.setType("logDir");
                                        }
                                    }
                                    if (vmExt.getReadOnly() == null) {
                                        vmExt.setReadOnly(false);
                                    }
                                    vms.add(vmExt);
                                    break;
                                }
                            }
                        }
                    }
                    cOfPodDetail.setStorage(vms);
                }
                res.add(cOfPodDetail);
            }
        }
        return res;
    }

    private static SecurityContextDto convertSecurityContext(SecurityContext sc){
        if(sc == null){
            return null;
        }
        SecurityContextDto securityContextDto = new SecurityContextDto();
        boolean flag = false;
        securityContextDto.setPrivileged(sc.isPrivileged());
        if (sc.isPrivileged()) {
            flag = true;
        }
        if (sc.getCapabilities() != null) {
            if (sc.getCapabilities().getAdd() != null && sc.getCapabilities().getAdd().size() > 0) {
                flag = true;
                securityContextDto.setAdd(sc.getCapabilities().getAdd());
            }
            if (sc.getCapabilities().getDrop() != null && sc.getCapabilities().getDrop().size() > 0) {
                flag = true;
                securityContextDto.setDrop(sc.getCapabilities().getDrop());
            }
        }
        securityContextDto.setSecurity(flag);
        return securityContextDto;
    }

    private static void convertResource(ContainerOfPodDetail cOfPodDetail, ResourceRequirements resourceRequirements){
        if(resourceRequirements == null){
            return;
        }
        if (resourceRequirements.getLimits() != null) {
            String pattern = ".*m.*";
            Pattern r = Pattern.compile(pattern);
            String cpu = ((Map<Object, Object>) resourceRequirements.getLimits()).get("cpu").toString();
            Matcher m = r.matcher(cpu);
            if (!m.find()) {
                ((Map<Object, Object>) resourceRequirements.getLimits()).put("cpu",
                        Integer.valueOf(cpu) * 1000 + "m");
            }
            cOfPodDetail.setLimit(((Map<String, Object>) resourceRequirements.getLimits()));
            if(cOfPodDetail.getLimit().get(CommonConstant.NVIDIA_GPU) != null){
                cOfPodDetail.getLimit().put(CommonConstant.GPU, cOfPodDetail.getLimit().get(CommonConstant.NVIDIA_GPU));
                cOfPodDetail.getLimit().remove(CommonConstant.NVIDIA_GPU);
            }
        } else {
            cOfPodDetail.setLimit(null);
        }
        if (resourceRequirements.getRequests() != null) {
            String pattern = ".*m.*";
            Pattern r = Pattern.compile(pattern);
            String cpu = ((Map<Object, Object>) resourceRequirements.getRequests()).get("cpu").toString();
            Matcher m = r.matcher(cpu);
            if (!m.find()) {
                ((Map<Object, Object>) resourceRequirements.getRequests()).put("cpu",
                        Integer.valueOf(cpu) * 1000 + "m");
            }
            cOfPodDetail.setResource(((Map<String, Object>) resourceRequirements.getRequests()));
            if(cOfPodDetail.getResource().get(CommonConstant.NVIDIA_GPU) != null){
                cOfPodDetail.getResource().put(CommonConstant.GPU, cOfPodDetail.getResource().get(CommonConstant.NVIDIA_GPU));
                cOfPodDetail.getResource().remove(CommonConstant.NVIDIA_GPU);
            }
        } else {
            cOfPodDetail.setResource(cOfPodDetail.getLimit());
        }
    }

    private static PersistentVolumeClaim getPvcByName(String namespace, String pvcName, Cluster cluster) {
        K8SURL url = new K8SURL();
        url.setApiGroup(APIGroup.API_V1_VERSION);
        url.setNamespace(namespace);
        url.setResource(Resource.PERSISTENTVOLUMECLAIM);
        url.setSubpath(pvcName);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET,null,null,cluster);
        if(HttpStatusUtil.isSuccessStatus(response.getStatus())){
            return K8SClient.converToBean(response, PersistentVolumeClaim.class);
        }
        return null;
    }

    public static List<Map<String, Object>> convertAppList(DeploymentList depList, Cluster cluster, String aliasNamespace) throws Exception {
        List<Deployment> deps = depList.getItems();
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        if (deps != null && !deps.isEmpty()) {
            for (int i = 0; i < deps.size(); i++) {
                Deployment dep = deps.get(i);
                Map<String, Object> tMap = new HashMap<String, Object>();
                tMap.put("name", dep.getMetadata().getName());
                Map<String, Object> labelMap = new HashMap<String, Object>();
                String labels = null;
                if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/labels")) {
                    labels = dep.getMetadata().getAnnotations().get("nephele/labels").toString();
                }
                if (!StringUtils.isEmpty(labels)) {
                    String[] arrLabel = labels.split(",");
                    for (String l : arrLabel) {
                        String[] tmp = l.split("=");
                        labelMap.put(tmp[0], tmp[1]);
                    }
                    tMap.put("labels", labelMap);
                }
                //服务如果使用PVC，获取PVC名称
                List<Volume> volumeList = dep.getSpec().getTemplate().getSpec().getVolumes();
                if (volumeList != null) {
                    List<String> pvcNameList = new ArrayList<>();
                    for (Volume volume : volumeList) {
                        if (volume.getPersistentVolumeClaim() != null) {
                            pvcNameList.add(volume.getPersistentVolumeClaim().getClaimName());

                        }
                    }
                    if (pvcNameList.size() > 0) {
                        tMap.put("pvcNameList", pvcNameList);
                    }
                }

                convertLabels(dep.getMetadata().getLabels(), dep.getSpec().getTemplate().getMetadata().getLabels(), tMap);

                tMap.put("status", getDeploymentStatus(dep));
                if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("deployment.kubernetes.io/revision")) {
                    tMap.put("version", "v" + dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"));
                }
                List<String> img = new ArrayList<String>();
                List<String> cpu = new ArrayList<String>();
                List<String> memory = new ArrayList<String>();
                List<String> gpu = new ArrayList<>();
                List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
                boolean isPV = false;
                for (Container container : containers) {
                    img.add(container.getImage());
                    if (container.getResources() != null && container.getResources().getRequests() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> res1 = (Map<String, String>) container.getResources().getRequests();
                        cpu.add(res1.get("cpu"));
                        memory.add(res1.get("memory"));
                        if(res1.get(CommonConstant.NVIDIA_GPU) != null) {
                            gpu.add(res1.get(CommonConstant.NVIDIA_GPU));
                        }
                    } else if (container.getResources() != null && container.getResources().getLimits() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> res1 = (Map<String, String>) container.getResources().getLimits();
                        cpu.add(res1.get("cpu"));
                        memory.add(res1.get("memory"));
                    }
                }
                if (dep.getSpec().getTemplate().getSpec().getVolumes() != null && dep.getSpec().getTemplate().getSpec().getVolumes().size() > 0) {
                    for (Volume v : dep.getSpec().getTemplate().getSpec().getVolumes()) {
                        if (v.getPersistentVolumeClaim() != null) {
                            isPV = true;
                            break;
                        }
                    }
                }
                Map<String, Object> podLabel = dep.getSpec().getTemplate().getMetadata().getLabels();
                if(podLabel.containsKey(Constant.TYPE_DEPLOYMENT)){
                    tMap.put("systemLabel",Constant.TYPE_DEPLOYMENT+"="+podLabel.get(Constant.TYPE_DEPLOYMENT));
                }else{
                    tMap.put("systemLabel","");
                }
                boolean isMsf = false;
                if (dep.getMetadata().getLabels().containsKey(Constant.NODESELECTOR_LABELS_PRE + "springcloud")) {
                    isMsf = true;
                }
                Map<String, Object> depLabels = dep.getMetadata().getLabels();
                for (String key : depLabels.keySet()) {
                    //获取应用名
                    if (key.contains(Constant.TOPO_LABEL_KEY)) {
                        String[] array = key.split(CommonConstant.LINE);
                        String appName = array.length > 0 ? array[array.length -1] : null;
                        tMap.put("appName", appName);
                    }

                }

                tMap.put("serviceType","Deployment");
                tMap.put("isMsf", isMsf);
                tMap.put("isPV", isPV);
                tMap.put("cpu", cpu);
                tMap.put("memory", memory);
                if(CollectionUtils.isNotEmpty(gpu)){
                    tMap.put("gpu", gpu);
                }
                tMap.put("img", img);
                tMap.put("instance", dep.getSpec().getReplicas());
                tMap.put("createTime", dep.getMetadata().getCreationTimestamp());
                tMap.put("namespace", dep.getMetadata().getNamespace());
                tMap.put("selector", dep.getSpec().getSelector());
                tMap.put("clusterId", cluster.getId());
                tMap.put("aliasNamespace", aliasNamespace);
                res.add(tMap);
            }
        }
        return res;
    }

    public static List<Map<String, Object>> convertAppList(StatefulSetList staList, Cluster cluster, String aliasNamespace) throws Exception {
        List<StatefulSet> stas = staList.getItems();
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        if (stas != null && !stas.isEmpty()) {
            for (int i = 0; i < stas.size(); i++) {
                StatefulSet sta = stas.get(i);
                Map<String, Object> tMap = new HashMap<String, Object>();
                tMap.put("name", sta.getMetadata().getName());
                Map<String, Object> labelMap = new HashMap<String, Object>();
                String labels = null;
                if (sta.getMetadata().getAnnotations() != null && sta.getMetadata().getAnnotations().containsKey("nephele/labels")) {
                    labels = sta.getMetadata().getAnnotations().get("nephele/labels").toString();
                }
                if (!StringUtils.isEmpty(labels)) {
                    String[] arrLabel = labels.split(",");
                    for (String l : arrLabel) {
                        String[] tmp = l.split("=");
                        labelMap.put(tmp[0], tmp[1]);
                    }
                    tMap.put("labels", labelMap);
                }
                convertLabels(sta.getMetadata().getLabels(), sta.getSpec().getTemplate().getMetadata().getLabels(), tMap);
                tMap.put("status", getStatefulSetStatus(sta));
                List<String> img = new ArrayList<String>();
                List<String> cpu = new ArrayList<String>();
                List<String> memory = new ArrayList<String>();
                List<Container> containers = sta.getSpec().getTemplate().getSpec().getContainers();
                boolean isPV = false;
                for (Container container : containers) {
                    img.add(container.getImage());
                    if (container.getResources() != null && container.getResources().getRequests() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> res1 = (Map<String, String>) container.getResources().getRequests();
                        cpu.add(res1.get("cpu"));
                        memory.add(res1.get("memory"));
                    } else if (container.getResources() != null && container.getResources().getLimits() != null) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> res1 = (Map<String, String>) container.getResources().getLimits();
                        cpu.add(res1.get("cpu"));
                        memory.add(res1.get("memory"));
                    }
                }
                if (sta.getSpec().getTemplate().getSpec().getVolumes() != null && sta.getSpec().getTemplate().getSpec().getVolumes().size() > 0) {
                    for (Volume v : sta.getSpec().getTemplate().getSpec().getVolumes()) {
                        if (v.getPersistentVolumeClaim() != null) {
                            isPV = true;
                            break;
                        }
                    }
                }
                Map<String, Object> podLabel = sta.getSpec().getTemplate().getMetadata().getLabels();
                if(podLabel.containsKey(Constant.TYPE_STATEFULSET)){
                    tMap.put("systemLabel", Constant.TYPE_STATEFULSET+"="+podLabel.get(Constant.TYPE_STATEFULSET));
                }else{
                    tMap.put("systemLabel","");
                }
                boolean isMsf = false;
                if (sta.getMetadata().getLabels().containsKey(Constant.NODESELECTOR_LABELS_PRE + "springcloud")) {
                    isMsf = true;
                }
                Map<String, Object> depLabels = sta.getMetadata().getLabels();
                for (String key : depLabels.keySet()) {
                    //获取应用名
                    if (key.contains(Constant.TOPO_LABEL_KEY)) {
                        String[] array = key.split(CommonConstant.LINE);
                        String appName = array.length > 0 ? array[array.length -1] : null;
                        tMap.put("appName", appName);
                    }
                }
                tMap.put("serviceType","StatefulSet");
                tMap.put("isMsf", isMsf);
                tMap.put("isPV", isPV);
                tMap.put("cpu", cpu);
                tMap.put("memory", memory);
                tMap.put("img", img);
                tMap.put("instance", sta.getSpec().getReplicas());
                tMap.put("createTime", sta.getMetadata().getCreationTimestamp());
                tMap.put("namespace", sta.getMetadata().getNamespace());
                tMap.put("selector", sta.getSpec().getSelector());
                tMap.put("clusterId", cluster.getId());
                tMap.put("aliasNamespace", aliasNamespace);
                res.add(tMap);
            }
        }
        return res;
    }



    public static Deployment convertAppCreate(DeploymentDetailDto detail, String userName, String applicationName, List<IngressDto> ingress) throws Exception {
        Deployment dep = new Deployment();
        ObjectMeta meta = getMeta(detail, userName, applicationName, ingress);
        dep.setMetadata(meta);

        DeploymentSpec depSpec = new DeploymentSpec();
        depSpec.setReplicas(Integer.valueOf(detail.getInstance()));
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("Recreate");
        depSpec.setStrategy(strategy);
        // selector
        LabelSelector labelSelector = new LabelSelector();
        Map<String, Object> matchLabel = new HashMap<String, Object>();
        matchLabel.put(Constant.TYPE_DEPLOYMENT, detail.getName());
        labelSelector.setMatchLabels(matchLabel);
        depSpec.setSelector(labelSelector);
        String podDisperse = Constant.TYPE_DEPLOYMENT + Constant.EQUAL + detail.getName();
        PodTemplateSpec podTemplateSpec = getPodTemplateSpec(detail,meta,podDisperse,Constant.TYPE_DEPLOYMENT, null);
        depSpec.setTemplate(podTemplateSpec);
        dep.setSpec(depSpec);
        return dep;
    }

    public static StatefulSet convertAppCreateForStatefulSet(StatefulSetDetailDto detail, String userName, String applicationName, List<IngressDto> ingress) throws Exception {
        StatefulSet sta = new StatefulSet();
        ObjectMeta meta = getMeta(detail,userName,applicationName, ingress);
        sta.setMetadata(meta);

        if(CollectionUtils.isNotEmpty(detail.getInitContainers())) {
            for (CreateContainerDto createContainerDto : detail.getInitContainers()) {
                if (createContainerDto.getResource() == null) {
                    CreateResourceDto resource = new CreateResourceDto();
                    resource.setCpu("100m");
                    resource.setMemory("128Mi");
                    createContainerDto.setResource(resource);
                }
            }
        }

        StatefulSetSpec staSpec = new StatefulSetSpec();
        staSpec.setPodManagementPolicy(detail.getPodManagementPolicy());
        staSpec.setReplicas(Integer.valueOf(detail.getInstance()));
        StatefulSetUpdateStrategy updateStrategy = new StatefulSetUpdateStrategy();
        updateStrategy.setType("RollingUpdate");
        staSpec.setUpdateStrategy(updateStrategy);
        // selector
        LabelSelector labelSelector = new LabelSelector();
        Map<String, Object> matchLabel = new HashMap<String, Object>();
        matchLabel.put(Constant.TYPE_STATEFULSET, detail.getName());
        matchLabel.put(Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID, detail.getProjectId());
        labelSelector.setMatchLabels(matchLabel);
        staSpec.setSelector(labelSelector);
        staSpec.setServiceName(detail.getName());
        sta.setSpec(staSpec);
        String podDisperse = Constant.TYPE_STATEFULSET + Constant.EQUAL + detail.getName();
        PodTemplateSpec podTemplateSpec = getPodTemplateSpec(detail,meta,podDisperse,Constant.TYPE_STATEFULSET, staSpec);
        staSpec.setTemplate(podTemplateSpec);

        sta.setSpec(staSpec);

        return sta;
    }

    public static Map<String, Object> getLmMap(DeploymentDetailDto detail, String userName, String applicationName, List<IngressDto> ingress) throws Exception{
        Map<String, Object> lmMap = new HashMap<String, Object>();
        lmMap.put(Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID, detail.getProjectId());
        lmMap.put(Constant.NODESELECTOR_LABELS_PRE + "bluegreen", detail.getName()+ "-1");
        if (userName != null) {
            lmMap.put("nephele/user", userName);
        }
        if(ingress != null){
            lmMap.put(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE, INGRESS_SERVICE_TRUE);
        }
        if (!StringUtils.isEmpty(applicationName)) {
            lmMap.put(BizUtil.getTopoLabelKey(detail.getProjectId(), applicationName), detail.getNamespace());
        }
        return lmMap;

    }

    public static ObjectMeta getMeta(DeploymentDetailDto detail, String userName, String applicationName, List<IngressDto> ingress) throws Exception{
        ObjectMeta meta = new ObjectMeta();
        meta.setName(detail.getName());
        Map<String, Object> lmMap = getLmMap(detail, userName, applicationName, ingress);
        meta.setLabels(lmMap);
        Map<String, Object> anno = new HashMap<String, Object>();
        String annotation = detail.getAnnotation();
        if (!StringUtils.isEmpty(annotation) && annotation.lastIndexOf(",") == 0) {
            annotation = annotation.substring(0, annotation.length() - 1);
        }

        anno.put("nephele/annotation", annotation == null ? "" : annotation);
        anno.put("nephele/status", Constant.STARTING);
        anno.put("nephele/replicas", detail.getInstance());
        anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());

        PullDependenceDto pullDependence = detail.getPullDependence();
        if(pullDependence !=null){
            String repoUrl = pullDependence.getRepoUrl();
            String branch = pullDependence.getBranch();
            String tag = pullDependence.getTag();
            String container = pullDependence.getContainer();
            anno.put("pulldep/repoUrl",repoUrl == null ? "" : repoUrl);
            anno.put("pulldep/branch",branch == null ? "" : branch);
            anno.put("pulldep/tag",tag == null ? "" : tag);
            anno.put("pulldep/container",container == null ? "" : container);
        }


        ServiceDependenceDto serviceDependence = detail.getServiceDependence();
        if(serviceDependence != null){
            String serviceName = serviceDependence.getServiceName();
            String port = serviceDependence.getPort();
            String url = serviceDependence.getUrl();

            anno.put("svcDepend/name",serviceName == null ? "" : serviceName);
            anno.put("svcDepend/port",port == null ? "" : port);
            anno.put("svcDepend/url",url == null ? "" : url);
        }

        meta.setAnnotations(anno);
        return meta;
    }

    public static PodTemplateSpec getPodTemplateSpec(DeploymentDetailDto detail, ObjectMeta meta, String podDisperse, String serviceType, StatefulSetSpec statefulsetSpec) throws Exception{
        Map<String, Object> lmMap = meta.getLabels();
        setResourceType(detail.getContainers(), serviceType);
        setResourceType(detail.getInitContainers(), serviceType);
        Map<String, Object> map = K8sResultConvert.convertContainer(detail.getContainers(), detail.getInitContainers(), detail.getLogService(), detail.getLogPath(), detail.getName());
        List<Container> cs = (List<Container>) map.get("container");
        List<Volume> volumes = (List<Volume>) map.get("volume");
        List<Container> initCs = (List<Container>) map.get("initContainer");
        if(Constant.TYPE_STATEFULSET.equalsIgnoreCase(serviceType)){
            statefulsetSpec.setVolumeClaimTemplates((List < PersistentVolumeClaim >) map.get("volumeClaimTemplate"));
        }
        PodTemplateSpec podTemplateSpec = new PodTemplateSpec();
        PodSpec podSpec = new PodSpec();
        podSpec.setContainers(cs);
        podSpec.setRestartPolicy(detail.getRestartPolicy());
        podSpec.setHostname(detail.getHostName());
        podSpec.setInitContainers(initCs);

        //Affinity
        Affinity affinity = new Affinity();
        List<AffinityDto> list = new ArrayList<>();
        list.add(detail.getPodAntiAffinity());
        //pod 按主机分组调度
        if(Objects.nonNull(detail.getPodGroupSchedule())){
            AffinityDto aff = new AffinityDto();
            aff.setRequired(detail.getPodGroupSchedule().isRequired());
            aff.setLabel(serviceType + Constant.EQUAL + detail.getName());
            aff.setType(detail.getPodGroupSchedule().getType());
            list.add(aff);
        }
        //pod 分散
        if (Objects.nonNull(detail.getPodDisperse())) {
            AffinityDto aff = new AffinityDto();
            aff.setRequired(detail.getPodDisperse().isRequired());
            aff.setLabel(podDisperse);
            list.add(aff);
        }
        affinity = KubeAffinityConvert.convertAffinity(detail.getNodeAffinity(), detail.getPodAffinity(), list);
        podSpec.setAffinity(affinity);

        //hostIPC hostPID
        podSpec.setHostIPC(detail.isHostIPC());
        podSpec.setHostPID(detail.isHostPID());
        podSpec.setHostNetwork(detail.isHostNetwork());
        if (detail.isHostNetwork()) {
            podSpec.setDnsPolicy("ClusterFirstWithHostNet");
        }
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(CommonConstant.ADMIN + "-secret");
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);
        //存储
        if (CollectionUtils.isNotEmpty(volumes)) {
            podSpec.setVolumes(volumes);
        }

        ObjectMeta metadata = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(Constant.NODESELECTOR_LABELS_PRE + Constant.LABEL_PROJECT_ID, detail.getProjectId());
        labels.put(serviceType, detail.getName());
        if (!StringUtils.isEmpty(detail.getLabels())) {
            String[] ls = detail.getLabels().split(",");
            for (String label : ls) {
                String[] tmp = label.split("=");
                labels.put(tmp[0], tmp[1]);
                lmMap.put(tmp[0], tmp[1]);
                meta.setLabels(lmMap);
            }
        }
        // istio服务版本标签
        if (Objects.nonNull(detail.getDeployVersion())){
            labels.put(Constant.TYPE_DEPLOY_VERSION, detail.getDeployVersion());
        }

        //labels-QOS
        if (detail.getLabels() != null) {
            if (detail.getLabels().contains(",")) {
                String[] labs = detail.getLabels().split(",");
                if (labs != null && labs.length > 0) {
                    for (String s : labs) {
                        if (s.contains("qos") && s.contains("=")) {
                            labels.put("qos", s.split("=")[1]);
                        }
                    }
                }
            } else {
                if (detail.getLabels().contains("qos") && detail.getLabels().contains("=")) {
                    labels.put("qos", detail.getLabels().split("=")[1]);
                }
            }
        }

        //拉取依赖
        if(detail.getPullDependence() != null){
            makePullDependence(detail, meta, cs, podSpec);
        }


        if(detail.getServiceDependence() !=null){
            makeServiceDependence(meta, cs, podSpec, detail.getServiceDependence());
        }

        //service account
        if(detail.isAutomountServiceAccountToken()){
            podSpec.setAutomountServiceAccountToken(detail.isAutomountServiceAccountToken());
            if(null != detail.getServiceAccount()){
                podSpec.setServiceAccount(detail.getServiceAccount());
            }
            if(null != detail.getServiceAccountName()){
                podSpec.setServiceAccountName(detail.getServiceAccountName());
            }
        }

        labels.put(Constant.NODESELECTOR_LABELS_PRE + "bluegreen", detail.getName() + "-1");
        metadata.setLabels(labels);
        metadata.setAnnotations(convertQosAnnotation(detail.getAnnotation()));
        // 如果存在ip资源池，则设置
        if (StringUtils.isNotBlank(detail.getIpPoolName())) {
            if (metadata.getAnnotations() == null) {
                metadata.setAnnotations(new HashMap<>());
            }
            metadata.getAnnotations().put("hcipam_ippool", detail.getIpPoolName());
        }
        podTemplateSpec.setMetadata(metadata);
        podTemplateSpec.setSpec(podSpec);

        return podTemplateSpec;
    }

    /**
     * 设置服务类型StatefulSet/Deployment，用来日志收集的服务类型环境变量设置
     * @param containerDtos
     * @param serviceType
     */
    private static void setResourceType(List<CreateContainerDto> containerDtos, String serviceType) {
        if (CollectionUtils.isEmpty(containerDtos) || StringUtils.isBlank(serviceType)) {
            return;
        }
        for (CreateContainerDto containerDto : containerDtos) {
            if (StringUtils.isBlank(containerDto.getParentResourceType())) {
                if (Constant.TYPE_STATEFULSET.equalsIgnoreCase(serviceType)) {
                    containerDto.setParentResourceType(STATEFULSET);
                } else if (Constant.TYPE_DEPLOYMENT.equalsIgnoreCase(serviceType)) {
                    containerDto.setParentResourceType(DEPLOYMENT);
                }
            }
        }
    }

    private static void makeServiceDependence(ObjectMeta meta, List<Container> cs, PodSpec podSpec, ServiceDependenceDto serviceDependence) {
        String image = cs.get(0).getImage();
        String harborUrl = image.substring(0,image.indexOf("/"));
        //创建InitContainers
        List<Container> initContainers = new ArrayList<>();
        if (null != podSpec.getInitContainers()){
            initContainers = podSpec.getInitContainers();
        }
        Container c = new Container();
        c.setName(meta.getName()+"-svc");
        c.setImage(harborUrl + Constant.SERVICE_DEPENDENCE_IMAGE + ":" + Constant.SERVICE_DEPENDENCE_IMAGE_TAG);
        List<String> cmdList = new ArrayList<>();
        cmdList.add("/bin/bash");
        cmdList.add("-c");
        StringBuffer sb = new StringBuffer();

        sb.append("cd /root/shell_script ");

        //如果是http
        if(serviceDependence.getDetectWay().equals(Constant.SERVICE_DETECT_WAY_HTTP)){
            sb.append("&& ./curl.sh ");
            sb.append(serviceDependence.getServiceName());
            sb.append(":" + serviceDependence.getPort());
        }
        //如果是tcp
        if(serviceDependence.getDetectWay().equals(Constant.SERVICE_DETECT_WAY_TCP)){
            sb.append("&& ./ncat.sh ");
            sb.append(serviceDependence.getServiceName());
            sb.append(" " + serviceDependence.getPort());
        }


        if(StringUtils.isNotBlank(serviceDependence.getUrl())){
            sb.append("/"+ serviceDependence.getUrl());
        }
        sb.append(" " + serviceDependence.getIntervalTime() + " " + serviceDependence.getSuccessThreshold()
                + " " + serviceDependence.getFailThreshold());
        cmdList.add(sb.toString());
        c.setCommand(cmdList);

        ResourceRequirements resources = new ResourceRequirements();
        Map<String, Object> limits = new ConcurrentHashMap<>();
        limits.put("memory","100Mi");
        limits.put("cpu","100m");
        Map<String, Object> requests = limits;
        resources.setLimits(limits);
        resources.setRequests(requests);
        c.setResources(resources);

        initContainers.add(c);
        podSpec.setInitContainers(initContainers);
    }

    private static void makePullDependence(DeploymentDetailDto detail, ObjectMeta meta, List<Container> cs, PodSpec podSpec) {
        String image = cs.get(0).getImage();
        String harborUrl = image.substring(0,image.indexOf("/"));
        //创建InitContainers
        List<Container> initContainers = new ArrayList<>();
        if(null != podSpec.getInitContainers()){
            initContainers = podSpec.getInitContainers();
        }
        Container c = new Container();
        c.setName(meta.getName()+"-vcs");
        c.setImage(harborUrl + Constant.VCS_IMAGE + ":" + Constant.VCS_IMAGE_TAG);
        List<String> cmdList = new ArrayList<>();
        PullDependenceDto pullDependence = detail.getPullDependence();
        String projectName = null;
        cmdList.add("/bin/bash");
        cmdList.add("-c");
        StringBuffer sb = new StringBuffer();
        if(pullDependence.getPullWay().equals(Constant.PULL_WAY_GIT)){
            String gitUrl = pullDependence.getRepoUrl();
            String protocol = gitUrl.substring(0,gitUrl.indexOf("://")+3);
            String userName = pullDependence.getUsername();
            String password = pullDependence.getPassword();
            //git拉取命令中包含@时需替换为%40
            if(userName.contains("@")){
                userName = userName.replace("@", "%40");
            }
            if(password.contains("@")){
                password = password.replace("@", "%40");
            }
            gitUrl = gitUrl.substring(gitUrl.indexOf("://")+3,gitUrl.length());
            projectName = gitUrl.substring(gitUrl.lastIndexOf("/")+1,gitUrl.lastIndexOf(".git"));
            //避免git之https方式unable to get local issuer certificate问题
            sb.append("git config --global http.sslVerify false");
            sb.append(" && rm -rf " + projectName + "/* " + projectName + "/.git*");
            sb.append(" && git clone " + protocol + userName + ":" + password + "@" + gitUrl);

            //指定了分支
            if(StringUtils.isNotBlank(pullDependence.getBranch()) && !pullDependence.getBranch().equals("master")){
                sb.append(" && cd " + projectName);
                sb.append(" && git checkout -b " + pullDependence.getBranch() + " origin/" + pullDependence.getBranch());
            }
            //指定了tag
            if(StringUtils.isNotBlank(pullDependence.getTag())){
                sb.append(" && cd " + projectName);
                sb.append(" && git checkout " + pullDependence.getTag());
            }
            cmdList.add(sb.toString());

        }else if(pullDependence.getPullWay().equals(Constant.PULL_WAY_SVN)){
            String svnURL = pullDependence.getRepoUrl();
            while (svnURL.endsWith("/")){
                svnURL = svnURL.substring(0, svnURL.length()-1);
            }
            projectName = svnURL.substring(svnURL.lastIndexOf("/")+1, svnURL.length());
            sb.append(" svn co " + svnURL);
            //指定了分支
            if(StringUtils.isNotBlank(pullDependence.getBranch()) && !pullDependence.getBranch().equals("master")){
                sb.append("/" + pullDependence.getBranch());
            }
            //指定了tag
            if(StringUtils.isNotBlank(pullDependence.getTag())){
                sb.append("/" + pullDependence.getTag());
            }

            sb.append(" --username ");
            sb.append(pullDependence.getUsername());
            sb.append(" --password ");
            sb.append(pullDependence.getPassword());
            cmdList.add(sb.toString());
        }
        c.setCommand(cmdList);

        if(StringUtils.isNotBlank(pullDependence.getContainer())){
            List<Volume> volumeList = null;
            volumeList = podSpec.getVolumes();
            if(Objects.isNull(volumeList)){
                volumeList = new ArrayList<>();
            }
            Volume volume = new Volume();
            volume.setName("empty-deploy");
            volume.setEmptyDir(null);
            volumeList.add(volume);
            podSpec.setVolumes(volumeList);

            for (Container container : cs) {
                if(container.getName().equals(pullDependence.getContainer())){
                    List<VolumeMount> volumeMounts = container.getVolumeMounts();
                    VolumeMount vm = new VolumeMount();
                    if(Objects.isNull(volumeMounts)){
                        volumeMounts = new ArrayList<>();
                        container.setVolumeMounts(volumeMounts);
                    }
                    vm.setMountPath(pullDependence.getMountPath()+"/"+projectName);
                    vm.setName("empty-deploy");
                    volumeMounts.add(vm);
                }
            }


            List<VolumeMount> volumeMounts = new ArrayList<>();
            VolumeMount vm = new VolumeMount();
            vm.setMountPath(projectName);
            vm.setName("empty-deploy");
            volumeMounts.add(vm);
            c.setVolumeMounts(volumeMounts);

        }


        ResourceRequirements resources = new ResourceRequirements();
        Map<String, Object> limits = new ConcurrentHashMap<>();
        limits.put("memory","100Mi");
        limits.put("cpu","100m");
        Map<String, Object> requests = limits;
        resources.setLimits(limits);
        resources.setRequests(requests);
        c.setResources(resources);

        initContainers.add(c);
        podSpec.setInitContainers(initContainers);
    }

    public static Service convertAppCreateOfService(DeploymentDetailDto detail, String application,String serviceType) throws Exception {
        Service service = new Service();
        service.setApiVersion("v1");
        service.setKind("Service");
        ObjectMeta meta = new ObjectMeta();
        meta.setName(detail.getName());
        Map<String, Object> labels = new HashMap<String, Object>();
        String label = new String();
        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.valueOf(serviceType.toUpperCase());
        switch (serviceTypeEnum){
            case DEPLOYMENT:
                label = Constant.TYPE_DEPLOYMENT;
                break;
            case STATEFULSET:
                label = Constant.TYPE_STATEFULSET;
                detail.setClusterIP("None");
                break;
            default:
                break;
        }
        labels.put(label, detail.getName());

        if (!StringUtils.isEmpty(application)) {
            labels.put(BizUtil.getTopoLabelKey(detail.getProjectId(), application), detail.getNamespace());
        }
        meta.setLabels(labels);
        ServiceSpec ss = new ServiceSpec();
        Map<String, Object> selector = new HashMap<String, Object>();
        selector.put(label, detail.getName());
        selector.put(Constant.NODESELECTOR_LABELS_PRE + "bluegreen", detail.getName() + "-1");
        ss.setSelector(selector);
        if (!StringUtils.isEmpty(detail.getClusterIP())) {
            ss.setClusterIP(detail.getClusterIP());
        }
        List<CreateContainerDto> containers = detail.getContainers();
        if (containers != null && !containers.isEmpty()) {
            List<ServicePort> spList = new ArrayList<ServicePort>();
            for (CreateContainerDto c : containers) {
                if (c.getPorts() != null && c.getPorts().size() > 0) {
                    for (int i = 0; i < c.getPorts().size(); i++) {
                        CreatePortDto port = c.getPorts().get(i);
                        ServicePort sPort = new ServicePort();
                        if (StringUtils.isEmpty(port.getProtocol())) {
                            sPort.setProtocol(Constant.PROTOCOL_TCP);
                        } else {
                            sPort.setProtocol(port.getProtocol());
                        }
                        sPort.setPort(Integer.valueOf(port.getPort()));
                        sPort.setName(KubeServiceConvert.convertPortName(port.getName(),sPort.getPort()));
                        spList.add(sPort);
                    }
                }
            }
            ss.setPorts(spList);
        }
        service.setSpec(ss);
        service.setMetadata(meta);
        return service;
    }

    public static Map<String, Object> convertAppPut(Deployment dep, ServiceList svsList, List<UpdateContainer> newContainers, String name) throws Exception {
        Map<String, Container> ct = new HashMap<String, Container>();
        List<Container> containers = dep.getSpec().getTemplate().getSpec().getContainers();
        for (Container c : containers) {
            ct.put(c.getName(), c);
        }
        Map<String, Object> vtemp = new HashMap<String, Object>();
        List<Volume> volumes = new ArrayList<Volume>();
        List<ServicePort> ports = new ArrayList<ServicePort>();
        List<Container> newC = new ArrayList<Container>();
        for (UpdateContainer cc : newContainers) {//cc为新的
            Container container = ct.get(cc.getName());//container为旧的
            container.setImage(cc.getImg());
            if (cc.getResource() != null) {
                Map<String, String> res = new HashMap<String, String>();
                String regEx = "[^0-9]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(cc.getResource().getCpu());
                String result = m.replaceAll("").trim();
                res.put("cpu", result + "m");
                res.put("memory", (cc.getResource().getMemory().contains("Mi") || cc.getResource().getMemory().contains("Gi")) ? cc.getResource().getMemory() : cc.getResource().getMemory() + "Mi");
                container.getResources().setLimits(res);
                container.getResources().setRequests(res);
                if (cc.getLimit() != null) {
                    Map<String, String> resl = new HashMap<String, String>();
                    Matcher ml = p.matcher(cc.getLimit().getCpu());
                    String resultl = ml.replaceAll("").trim();
                    resl.put("cpu", resultl + "m");
                    resl.put("memory", (cc.getLimit().getMemory().contains("Mi") || cc.getLimit().getMemory().contains("Gi")) ? cc.getLimit().getMemory() : cc.getLimit().getMemory() + "Mi");
                    container.getResources().setLimits(resl);
                }
            }
            if (cc.getPorts() != null && !cc.getPorts().isEmpty()) {
                List<ContainerPort> ps = new ArrayList<ContainerPort>();
                for (CreatePortDto p : cc.getPorts()) {
                    ContainerPort port = new ContainerPort();
                    port.setContainerPort(Integer.valueOf(p.getContainerPort()));
                    port.setProtocol(p.getProtocol());
                    String portName = KubeServiceConvert.convertPortName(p.getName(),port.getContainerPort());
                    port.setName(portName);
                    ps.add(port);
                    container.setPorts(ps);
                    ServicePort servicePort = new ServicePort();
                    servicePort.setTargetPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setPort(Integer.valueOf(p.getContainerPort()));
                    servicePort.setProtocol(p.getProtocol());
                    servicePort.setName(portName);
                    ports.add(servicePort);
                }

            }

            List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
            container.setVolumeMounts(volumeMounts);
            if (cc.getStorage() != null && !cc.getStorage().isEmpty()) {
                List<PersistentVolumeDto> newVolume = cc.getStorage();
                for (int i = 0; i < newVolume.size(); i++) {
                    PersistentVolumeDto vol = newVolume.get(i);
                    if (vol.getType().equals("secret")) {
                        if (!vtemp.containsKey(vol.getPvcName())) {
                            vtemp.put(vol.getPvcName(), vol.getPvcName());
                            SecretVolumeSource secret = new SecretVolumeSource();
                            secret.setSecretName(vol.getPvcName());
                            Volume v = new Volume();
                            v.setSecret(secret);
                            v.setName(vol.getPvcName());
                            volumes.add(v);
                        }
                        VolumeMount volm = new VolumeMount();
                        volm.setName(vol.getPvcName());
                        volm.setMountPath(vol.getPath());
                        volm.setReadOnly(vol.getReadOnly().equals("true"));
                        volumeMounts.add(volm);
                    }

                    if (vol.getType().equals("pv")) {
                        if (!vtemp.containsKey(vol.getPvcName())) {
                            vtemp.put(vol.getPvcName(), vol.getPvcName());
                            PersistentVolumeClaimVolumeSource pvc = new PersistentVolumeClaimVolumeSource();
                            pvc.setClaimName(vol.getPvcName());
                            Volume v = new Volume();
                            v.setPersistentVolumeClaim(pvc);
                            v.setName(vol.getPvcName());
                            volumes.add(v);
                        }

                        VolumeMount volm = new VolumeMount();
                        volm.setName(vol.getPvcName());
                        volm.setMountPath(vol.getPath());
                        volm.setReadOnly(vol.getReadOnly().equals("true"));
                        volumeMounts.add(volm);
                    }

                    if (vol.getType().equals("gitRepo")) {
                        if (!vtemp.containsKey(vol.getGitUrl())) {
                            vtemp.put(vol.getGitUrl(), RandomNum.randomNumber(8));
                            GitRepoVolumeSource gitRepo = new GitRepoVolumeSource();
                            gitRepo.setRepository(vol.getGitUrl());
                            gitRepo.setRevision(vol.getRevision());
                            Volume v = new Volume();
                            v.setGitRepo(gitRepo);
                            v.setName(vol.getGitUrl());
                            volumes.add(v);
                        }

                        VolumeMount volm = new VolumeMount();
                        volm.setName(vol.getGitUrl());
                        volm.setMountPath(vol.getPath());
                        volm.setReadOnly(vol.getReadOnly().equals("true"));
                        volumeMounts.add(volm);
                    }

                    if (vol.getType().equals("logDir")) {
                        if (!vtemp.containsKey(vol.getPvcName())) {
                            vtemp.put(vol.getPvcName(), vol.getPvcName());
                            EmptyDirVolumeSource emp = new EmptyDirVolumeSource();
                            emp.setMedium("");
                            Volume v = new Volume();
                            v.setEmptyDir(emp);
                            v.setName(vol.getPvcName());
                            volumes.add(v);
                        }

                        VolumeMount volm = new VolumeMount();
                        volm.setName(vol.getPvcName());
                        volm.setMountPath(vol.getPath());
                        volm.setReadOnly(vol.getReadOnly());
                        volumeMounts.add(volm);
                    }

                    if (vol.getType().equals("configMap")) {
                        if (!vtemp.containsKey(vol.getPvcName())) {
                            vtemp.put(vol.getPvcName(), vol.getPvcName());
                            ConfigMapVolumeSource con = new ConfigMapVolumeSource();
                            con.setName("configmap" + name + cc.getName());//configmap name s
                            Volume v = new Volume();
                            v.setConfigMap(con);
                            v.setName(vol.getPvcName());
                            volumes.add(v);
                        }

                        VolumeMount volm = new VolumeMount();
                        volm.setName(vol.getPvcName());
                        volm.setMountPath(vol.getPath());
                        //volm.setSubPath(vol.getSubPath());
                        volm.setReadOnly(vol.getReadOnly());
                        volumeMounts.add(volm);
                    }
                }
            }
            container.setCommand(cc.getCommand());
            container.setArgs(cc.getArgs());
            if (cc.getEnv() != null && !cc.getEnv().isEmpty()) {
                List<EnvVar> envVars = new ArrayList<EnvVar>();
                for (CreateEnvDto env : cc.getEnv()) {
                    EnvVar eVar = new EnvVar();
                    eVar.setName(env.getKey());
                    eVar.setValue(env.getValue());
                    envVars.add(eVar);
                }
                container.setEnv(envVars);
            }
            newC.add(container);
        }

        dep.getSpec().getTemplate().getSpec().setContainers(newC);
        dep.getSpec().getTemplate().getSpec().setVolumes(volumes);
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("Recreate");
        dep.getSpec().setStrategy(strategy);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        Map<String, Object> anno = new HashMap<String, Object>();
        anno.put("updateTimestamp", updateTime);
        dep.getMetadata().setAnnotations(anno);
        Service svc = svsList.getItems().get(0);
        svc.getSpec().setPorts(ports);
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("dep", dep);
        res.put("service", svc);
        return res;
    }

    @SuppressWarnings("unchecked")
    public static List<VolumeListBean> convertVolumeList(DeploymentList depList, PersistentVolumeClaimList vol) throws Exception {
        List<VolumeListBean> vms = new ArrayList<VolumeListBean>();
        List<PersistentVolumeClaim> pvcs = vol.getItems();
        if (pvcs != null && pvcs.size() > 0) {
            for (PersistentVolumeClaim pvClaim : pvcs) {
                VolumeListBean temp = new VolumeListBean();
                temp.setName(pvClaim.getMetadata().getName());
                temp.setStatus(pvClaim.getStatus().getPhase());
                String ca = ((Map<String, Object>) pvClaim.getSpec().getResources().getRequests()).get("storage").toString();
                ca = ca.substring(0, ca.indexOf("Mi"));
                temp.setCapacity(Integer.valueOf(ca));
                temp.setCreateTime(pvClaim.getMetadata().getCreationTimestamp());
                temp.setNamespace(pvClaim.getMetadata().getNamespace());
                if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadWriteMany")) {
                    temp.setReadOnly(false);
                    temp.setMultiMount(true);
                }

                if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadWriteOnce")) {
                    temp.setReadOnly(false);
                    temp.setMultiMount(false);
                }

                if (pvClaim.getSpec().getAccessModes().get(0).equals("ReadOnlyMany")) {
                    temp.setReadOnly(true);
                    temp.setMultiMount(true);
                }
                vms.add(temp);
            }
        }
        List<Deployment> deployments = depList.getItems();
        if (deployments != null && deployments.size() > 0) {
            for (Deployment dep : deployments) {
                List<Volume> volumes = dep.getSpec().getTemplate().getSpec().getVolumes();
                if (volumes != null && volumes.size() > 0) {
                    for (VolumeListBean tmp : vms) {

                        for (Volume v : volumes) {
                            if (v.getPersistentVolumeClaim() != null && v.getPersistentVolumeClaim().getClaimName().equals(tmp.getName())) {
                                List<String> bounds = new ArrayList<String>();
                                bounds.add(dep.getMetadata().getName());
                                tmp.setBounds(bounds);
                            }
                        }
                    }
                }
            }
        }
        return vms;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertAppPod(Pod pod, List<Event> events) throws Exception {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", pod.getMetadata().getName());
        result.put("namespace", pod.getMetadata().getNamespace());
        PodStatus podStatus = pod.getStatus();
        result.put("status", podStatus.getPhase());
        if (StringUtils.isNotBlank(pod.getMetadata().getDeletionTimestamp())) {
            result.put("status", com.harmonycloud.service.platform.constant.Constant.TERMINATED);
        }
        result.put("startTime", podStatus.getStartTime());
        result.put("containerAmount", pod.getSpec().getContainers().size());
        result.put("ip", podStatus.getPodIP());
        result.put("hostIp", podStatus.getHostIP());
        result.put("createTime", pod.getMetadata().getCreationTimestamp());
        List<String> volumes = new ArrayList<String>();
        if(pod.getSpec().getVolumes() != null) {
            for (Volume volume : pod.getSpec().getVolumes()) {
                volumes.add(volume.getName());
            }
        }
        List<Container> containers = pod.getSpec().getContainers();
        List<ContainerStatus> containerStatusList = podStatus.getContainerStatuses();
        List<Map<String, Object>> resContainer = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < containers.size(); i++) {
            Container c = containers.get(i);
            Map<String, Object> tMap = new HashMap<String, Object>();
            tMap.put("name", c.getName());
            if (containerStatusList != null) {
                ContainerStatus containerStatus = containerStatusList.get(i);
                if (containerStatus.getState().getRunning() != null) {
                    tMap.put("status", Constant.RUNNING);
                    tMap.put("statusDetail", containerStatus.getState().getRunning());
                } else if (containerStatus.getState().getWaiting() != null) {
                    tMap.put("status", Constant.WAITING);
                    tMap.put("statusDetail", containerStatus.getState().getWaiting());
                } else if (containerStatus.getState().getTerminated() != null) {
                    tMap.put("status", Constant.TERMINATED);
                    tMap.put("statusDetail", containerStatus.getState().getTerminated());
                }
                tMap.put("restartTime", containerStatus.getRestartCount());
            } else {
                tMap.put("status", Constant.NOTCREATED);
                ContainerStateRunning runing = new ContainerStateRunning();
                runing.setStartedAt("");
                tMap.put("statusDetail", runing);
            }
            tMap.put("img", c.getImage());
            tMap.put("resource", c.getResources().getLimits());
            if (tMap.get("resource") != null) {
                Map<String, Object> resource = (Map<String, Object>) tMap.get("resource");
                if (resource.get("cpu") != null && resource.get("cpu").toString().indexOf("m") < 0) {
                    resource.put("cpu", Integer.valueOf(resource.get("cpu").toString()) * 1000 + "m");
                }
            }
            tMap.put("livenessProbe", c.getLivenessProbe());
            tMap.put("readinessProbe", c.getReadinessProbe());
            tMap.put("ports", c.getPorts());
            tMap.put("args", c.getArgs());
            tMap.put("env", c.getEnv());
            tMap.put("command", c.getCommand());
            List<VolumeMountExt> vExts = new ArrayList<VolumeMountExt>();
            if(c.getVolumeMounts()!=null) {
                for (VolumeMount vMount : c.getVolumeMounts()) {
                    for (Volume volume : pod.getSpec().getVolumes()) {
                        if (vMount.getName().equals(volume.getName())) {
                            VolumeMountExt vmExt = new VolumeMountExt(vMount.getName(), vMount.isReadOnly(), vMount.getMountPath(),
                                    vMount.getSubPath());
                            if (volume.getSecret() != null) {
                                vmExt.setType("secret");
                            } else if (volume.getPersistentVolumeClaim() != null) {
                                vmExt.setType("nfs");
                            } else if (volume.getEmptyDir() != null) {
                                vmExt.setType("emptyDir");
                                if (volume.getEmptyDir() != null) {
                                    vmExt.setEmptyDir(volume.getEmptyDir().getMedium());
                                } else {
                                    vmExt.setEmptyDir(null);
                                }

                                if (vMount.getName().indexOf("logdir") == 0) {
                                    vmExt.setType("logDir");
                                }
                            } else if (volume.getConfigMap() != null) {
                                Map<String, Object> configMap = new HashMap<String, Object>();
                                configMap.put("name", volume.getConfigMap().getName());
                                configMap.put("path", vMount.getMountPath());
                                vmExt.setType("configMap");
                                vmExt.setConfigMapName(volume.getConfigMap().getName());
                            } else if (volume.getHostPath() != null) {
                                vmExt.setType("hostPath");
                                vmExt.setHostPath(volume.getHostPath().getPath());
                                if (vMount.getName().indexOf("logdir") == 0) {
                                    vmExt.setType("logDir");
                                }
                            }
                            if (vmExt.getReadOnly() == null) {
                                vmExt.setReadOnly(false);
                            }
                            vExts.add(vmExt);
                            break;
                        }
                    }
                    tMap.put("storage", vExts);
                }
            }
            resContainer.add(tMap);
        }
        result.put("containers", resContainer);
        result.put("events", events);
        return result;
    }

    public static HorizontalPodAutoscaler convertHpa(HPADto hpaDto) throws Exception {
        HorizontalPodAutoscaler hpAutoscaler = new HorizontalPodAutoscaler();

        // 设置hpa对象的metadata
        ObjectMeta meta = new ObjectMeta();
        Map<String, Object> labels = new HashMap<String, Object>();
        labels.put("app", hpaDto.getDeploymentName());
        meta.setName(hpaDto.getDeploymentName() + "-hpa");
        meta.setLabels(labels);
        meta.setCreationTimestamp(null);
        meta.setDeletionGracePeriodSeconds(null);
        meta.setDeletionTimestamp(null);
        hpAutoscaler.setMetadata(meta);

        // 设置hpa对象的spec
        HorizontalPodAutoscalerSpec hpaSpec = new HorizontalPodAutoscalerSpec();
//        hpaSpec.setTargetCPUUtilizationPercentage(cpu);
        hpaSpec.setMinReplicas(hpaDto.getMinPods());
        hpaSpec.setMaxReplicas(hpaDto.getMaxPods());
        CrossVersionObjectReference targetRef = new CrossVersionObjectReference();
        targetRef.setKind(Constant.DEPLOYMENT);
        targetRef.setName(hpaDto.getDeploymentName());
        targetRef.setApiVersion(Constant.DEPLOYMENT_API_VERSION);
        hpaSpec.setScaleTargetRef(targetRef);
        if(!CollectionUtils.isEmpty(hpaDto.getResource())){
            List<MetricSpec> metricSpecList = new ArrayList<MetricSpec>();
            for(ResourceMetricScaleDto  dto : hpaDto.getResource()){
                MetricSpec metric =  new MetricSpec();
                metric.setType("Resource");
                ResourceMetricSource  resource = new ResourceMetricSource();
                resource.setName(dto.getName());
                resource.setTargetAverageUtilization(dto.getTargetUsage());
                metric.setResource(resource);
                metricSpecList.add(metric);
            }
            hpaSpec.setMetrics(metricSpecList);
        }
        hpAutoscaler.setSpec(hpaSpec);
        return hpAutoscaler;
    }

    /**
     * 组装 pod template
     */
    public static PodTemplateSpec convertPodTemplate(String name, List<CreateContainerDto> containers, String label, String annotation, String userName, String type, String nodeSelector, String restartPolicy, String namespace) throws Exception {
        //组装pod tempate
        PodTemplateSpec podTemplate = new PodTemplateSpec();

        //metadata
        ObjectMeta metadata = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(type, name);
        if (!StringUtils.isEmpty(label)) {
            String[] ls = label.split(",");
            for (String lab : ls) {
                String[] tmp = lab.split("=");
                labels.put(tmp[0], tmp[1]);
            }
        }
        metadata.setLabels(labels);
        metadata.setName(name);
        metadata.setNamespace(namespace);
        podTemplate.setMetadata(metadata);
        //podSpec
        PodSpec podSpec = new PodSpec();
        List<Container> cs = new ArrayList<Container>();
        List<Volume> volumes = new ArrayList<Volume>();
        if (containers != null && containers.size() > 0) {
            for (CreateContainerDto c : containers) {
                Container container = new Container();
                container.setName(c.getName());
                if (StringUtils.isEmpty(c.getTag())) {
                    container.setImage(c.getImg());
                } else {
                    container.setImage(c.getImg() + ":" + c.getTag());
                }
                container.setCommand(c.getCommand());
                container.setArgs(c.getArgs());
                if (c.getLivenessProbe() != null) {
                    Probe lProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getLivenessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getLivenessProbe().getHttpGet().getPath());
                        if (c.getLivenessProbe().getHttpGet().getPort() == 0) {
                            httpGet.setPort(Constant.LIVENESS_PORT);
                        } else {
                            //lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getLivenessProbe().getHttpGet().getPort());
                        }
                        lProbe.setHttpGet(httpGet);
                    }

                    if (c.getLivenessProbe().getExec() != null) {
                        if (c.getLivenessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getLivenessProbe().getExec().getCommand());
                            lProbe.setExec(exec);
                        }
                    }

                    if (c.getLivenessProbe().getTcpSocket() != null) {
                        if (c.getLivenessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            tcp.setPort(c.getLivenessProbe().getTcpSocket().getPort());
                        }
                        lProbe.setTcpSocket(tcp);
                    }
                    lProbe.setInitialDelaySeconds(c.getLivenessProbe().getInitialDelaySeconds());
                    lProbe.setTimeoutSeconds(c.getLivenessProbe().getTimeoutSeconds());
                    lProbe.setPeriodSeconds(c.getLivenessProbe().getPeriodSeconds());
                    lProbe.setSuccessThreshold(c.getLivenessProbe().getSuccessThreshold());
                    lProbe.setFailureThreshold(c.getLivenessProbe().getFailureThreshold());
                    container.setLivenessProbe(lProbe);
                }

                if (c.getReadinessProbe() != null) {
                    Probe rProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getReadinessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getReadinessProbe().getHttpGet().getPath());
                        if (c.getReadinessProbe().getHttpGet().getPort() == 0) {
                            rProbe.getHttpGet().setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getReadinessProbe().getHttpGet().getPort());
                        }
                        rProbe.setHttpGet(httpGet);
                    }

                    if (c.getReadinessProbe().getExec() != null) {
                        if (c.getReadinessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getReadinessProbe().getExec().getCommand());
                            rProbe.setExec(exec);
                        }
                    }

                    if (c.getReadinessProbe().getTcpSocket() != null) {
                        if (c.getReadinessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
                            tcp.setPort(c.getReadinessProbe().getTcpSocket().getPort());
                        }
                        rProbe.setTcpSocket(tcp);
                    }
                    rProbe.setInitialDelaySeconds(c.getReadinessProbe().getInitialDelaySeconds());
                    rProbe.setTimeoutSeconds(c.getReadinessProbe().getTimeoutSeconds());
                    rProbe.setPeriodSeconds(c.getReadinessProbe().getPeriodSeconds());
                    rProbe.setSuccessThreshold(c.getReadinessProbe().getSuccessThreshold());
                    rProbe.setFailureThreshold(c.getReadinessProbe().getFailureThreshold());
                    container.setReadinessProbe(rProbe);
                }

                if (c.getPorts() != null && !c.getPorts().isEmpty()) {
                    List<ContainerPort> ps = new ArrayList<ContainerPort>();
                    for (CreatePortDto p : c.getPorts()) {
                        ContainerPort port = new ContainerPort();
                        port.setContainerPort(Integer.valueOf(p.getPort()));
                        port.setProtocol(p.getProtocol());
                        ps.add(port);
                    }
                    container.setPorts(ps);
                }

                if (c.getEnv() != null && !c.getEnv().isEmpty()) {
                    List<EnvVar> envVars = new ArrayList<EnvVar>();
                    for (CreateEnvDto env : c.getEnv()) {
                        EnvVar eVar = new EnvVar();
                        eVar.setName(env.getKey());
                        eVar.setValue(env.getValue());
                        envVars.add(eVar);
                    }
                    container.setEnv(envVars);
                }

                if (c.getResource() != null) {
                    ResourceRequirements limit = new ResourceRequirements();
                    Map<String, String> res = new HashMap<String, String>();
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(c.getResource().getCpu());
                    String result = m.replaceAll("").trim();
                    res.put("cpu", result + "m");
//		              res.put("cpu", c.getResource().getCpu());
                    Matcher mm = p.matcher(c.getResource().getMemory());
                    String resultm = mm.replaceAll("").trim();
                    res.put("memory", resultm + "Mi");
                    limit.setLimits(res);
                    limit.setRequests(res);
                    if (c.getLimit() != null) {
                        Map<String, String> resli = new HashMap<String, String>();
                        Matcher l = p.matcher(c.getLimit().getCpu());
                        String resultl = l.replaceAll("").trim();
                        resli.put("cpu", resultl + "m");
                        Matcher ml = p.matcher(c.getLimit().getMemory());
                        String resultml = ml.replaceAll("").trim();
                        resli.put("memory", resultml + "Mi");
                        limit.setLimits(resli);
                    }
                    container.setResources(limit);
                }

                List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
                container.setVolumeMounts(volumeMounts);
                if (c.getStorage() != null && !c.getStorage().isEmpty()) {
                    Map<String, Object> volFlag = new HashMap<String, Object>();
                    for (PersistentVolumeDto vm : c.getStorage()) {
                        if (vm.getType() != null) {
                            switch (vm.getType()) {
                                case Constant.VOLUME_TYPE_NFS:
                                    if (!volFlag.containsKey(vm.getPvcName())) {
                                        PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
                                        volFlag.put(vm.getPvcName(), vm.getPvcName());
                                        if (vm.getReadOnly().equals("true")) {
                                            pvClaim.setReadOnly(true);
                                        }
                                        if (vm.getReadOnly().equals("false")) {
                                            pvClaim.setReadOnly(false);
                                        }
                                        pvClaim.setClaimName(vm.getPvcName());
                                        Volume vol = new Volume();
                                        vol.setPersistentVolumeClaim(pvClaim);
                                        vol.setName(vm.getPvcName());
                                        volumes.add(vol);
                                    }
                                    VolumeMount volm = new VolumeMount();
                                    volm.setName(vm.getPvcName());
                                    volm.setReadOnly(vm.getReadOnly());
                                    volm.setMountPath(vm.getPath());
                                    volumeMounts.add(volm);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_GITREPO:
                                    if (!volFlag.containsKey(vm.getGitUrl())) {
                                        volFlag.put(vm.getGitUrl(), RandomNum.randomNumber(8));
                                        Volume gitRep = new Volume();
                                        gitRep.setName(volFlag.get(vm.getGitUrl()).toString());
                                        GitRepoVolumeSource gp = new GitRepoVolumeSource();
                                        gp.setRepository(vm.getGitUrl());
                                        gp.setRevision(vm.getRevision());
                                        gitRep.setGitRepo(gp);
                                        volumes.add(gitRep);
                                    }
                                    VolumeMount volmg = new VolumeMount();
                                    volmg.setName(volFlag.get(vm.getGitUrl()).toString());
                                    volmg.setReadOnly(vm.getReadOnly());
                                    volmg.setMountPath(vm.getPath());
                                    volumeMounts.add(volmg);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_EMPTYDIR:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir())) {
                                        volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir(), RandomNum.getRandomString(8));
                                        Volume empty = new Volume();
                                        empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir()).toString());
                                        EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                                        if (vm.getEmptyDir() != null && "Memory".equals(vm.getEmptyDir())) {
                                            ed.setMedium(vm.getEmptyDir());//Memory
                                        }
                                        if (vm.getCapacity() != null){
                                            ed.setSizeLimit(vm.getCapacity());//sizeLimit
                                        }
                                        empty.setEmptyDir(ed);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volme = new VolumeMount();
                                    volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir()).toString());
                                    volme.setMountPath(vm.getPath());
                                    volumeMounts.add(volme);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_HOSTPASTH:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath())) {
                                        volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath(), RandomNum.getRandomString(8));
                                        Volume empty = new Volume();
                                        empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                        HostPath hp = new HostPath();
                                        hp.setPath(vm.getHostPath());
                                        empty.setHostPath(hp);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volmh = new VolumeMount();
                                    volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                    volmh.setMountPath(vm.getPath());
                                    volumeMounts.add(volmh);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }

                if (!StringUtils.isEmpty(c.getLog())) {
                    Volume emp = new Volume();
                    emp.setName("logdir" + c.getName());
                    EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                    ed.setMedium("");
                    emp.setEmptyDir(ed);
                    volumes.add(emp);
                    VolumeMount volm = new VolumeMount();
                    volm.setName("logdir" + c.getName());
                    volm.setMountPath(c.getLog());
                    volumeMounts.add(volm);
                    container.setVolumeMounts(volumeMounts);
                    List<EnvVar> envlist = new ArrayList<>();
                    EnvVar env = new EnvVar();
                    env.setName(Constant.PILOT_LOG_PREFIX);
                    env.setValue(c.getLog() + "/*");
                    envlist.add(env);
                    if (container.getEnv()!=null){
                        container.getEnv().add(env);
                    }else {
                        container.setEnv(envlist);
                    }
                }
                convertConfigMap(name+c.getName(),c.getName(),c.getConfigmap(),volumes,volumeMounts);
                if (c.getImagePullPolicy() != null) {
                    container.setImagePullPolicy(c.getImagePullPolicy());
                }
                cs.add(container);
            }
        }
        podSpec.setContainers(cs);
        Map<String, Object> nodeselector = new HashMap<>();
        if (!StringUtils.isEmpty(nodeSelector)) {
            String[] ns = {};
            if (nodeSelector.contains(",")) {
                ns = nodeSelector.split(",");
            } else {
                ns[0] = nodeSelector;
            }
            for (String n : ns) {
                if (n.contains("=")) {
                    String[] s = n.split("=");
                    nodeselector.put(s[0], s[1]);
                }
            }
        }
        podSpec.setNodeSelector(nodeselector);
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(userName + "-secret");
        if (Constant.TYPE_JOB.equals(type)) {
            if (StringUtils.isEmpty(restartPolicy)) {
                podSpec.setRestartPolicy(Constant.RESTARTPOLICY_NERVER);
            } else {
                podSpec.setRestartPolicy(restartPolicy);
            }
        }
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);
        if (volumes.size() > 0) {
            podSpec.setVolumes(volumes);
        }
        podTemplate.setSpec(podSpec);
        return podTemplate;
    }

    /**
     * 组装 pod template
     */
    public static PodTemplateSpec convertUpdatePodTemplate(Job job, List<CreateContainerDto> containers, String type, String label, String annotation, String userName, String nodeSelector) throws Exception {
        //组装pod tempate
        PodTemplateSpec podTemplate = job.getSpec().getTemplate();

        //metadata
        ObjectMeta metadata = podTemplate.getMetadata();
        Map<String, Object> labels = podTemplate.getMetadata().getLabels();
        /*if(labels.containsKey("controller-uid")) {
            labels.remove("controller-uid");
		}*/
        labels.put(type, job.getMetadata().getName());
        if (!StringUtils.isEmpty(label)) {
            String[] ls = label.split(",");
            for (String lab : ls) {
                String[] tmp = lab.split("=");
                labels.put(tmp[0], tmp[1]);
            }
        }
        metadata.setLabels(labels);
        metadata.setName(job.getMetadata().getName());
        metadata.setNamespace(job.getMetadata().getNamespace());
        podTemplate.setMetadata(metadata);
        //podSpec
        PodSpec podSpec = podTemplate.getSpec();
        List<Container> cs = new ArrayList<Container>();
        List<Volume> volumes = new ArrayList<Volume>();
        if (containers != null && containers.size() > 0) {
            for (CreateContainerDto c : containers) {
                Container container = new Container();
                container.setName(c.getName());
                if (StringUtils.isEmpty(c.getTag())) {
                    container.setImage(c.getImg());
                } else {
                    container.setImage(c.getImg() + ":" + c.getTag());
                }
                container.setCommand(c.getCommand());
                container.setArgs(c.getArgs());
                if (c.getLivenessProbe() != null) {
                    Probe lProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getLivenessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getLivenessProbe().getHttpGet().getPath());
                        if (c.getLivenessProbe().getHttpGet().getPort() == 0) {
                            httpGet.setPort(Constant.LIVENESS_PORT);
                        } else {
                            //lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getLivenessProbe().getHttpGet().getPort());
                        }
                        lProbe.setHttpGet(httpGet);
                    }

                    if (c.getLivenessProbe().getExec() != null) {
                        if (c.getLivenessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getLivenessProbe().getExec().getCommand());
                            lProbe.setExec(exec);
                        }
                    }

                    if (c.getLivenessProbe().getTcpSocket() != null) {
                        if (c.getLivenessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            tcp.setPort(c.getLivenessProbe().getTcpSocket().getPort());
                        }
                        lProbe.setTcpSocket(tcp);
                    }
                    lProbe.setInitialDelaySeconds(c.getLivenessProbe().getInitialDelaySeconds());
                    lProbe.setTimeoutSeconds(c.getLivenessProbe().getTimeoutSeconds());
                    lProbe.setPeriodSeconds(c.getLivenessProbe().getPeriodSeconds());
                    lProbe.setSuccessThreshold(c.getLivenessProbe().getSuccessThreshold());
                    lProbe.setFailureThreshold(c.getLivenessProbe().getFailureThreshold());
                    container.setLivenessProbe(lProbe);
                }

                if (c.getReadinessProbe() != null) {
                    Probe rProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getReadinessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getReadinessProbe().getHttpGet().getPath());
                        if (c.getReadinessProbe().getHttpGet().getPort() == 0) {
                            rProbe.getHttpGet().setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getReadinessProbe().getHttpGet().getPort());
                        }
                        rProbe.setHttpGet(httpGet);
                    }

                    if (c.getReadinessProbe().getExec() != null) {
                        if (c.getReadinessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getReadinessProbe().getExec().getCommand());
                            rProbe.setExec(exec);
                        }
                    }

                    if (c.getReadinessProbe().getTcpSocket() != null) {
                        if (c.getReadinessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
                            tcp.setPort(c.getReadinessProbe().getTcpSocket().getPort());
                        }
                        rProbe.setTcpSocket(tcp);
                    }
                    rProbe.setInitialDelaySeconds(c.getReadinessProbe().getInitialDelaySeconds());
                    rProbe.setTimeoutSeconds(c.getReadinessProbe().getTimeoutSeconds());
                    rProbe.setPeriodSeconds(c.getReadinessProbe().getPeriodSeconds());
                    rProbe.setSuccessThreshold(c.getReadinessProbe().getSuccessThreshold());
                    rProbe.setFailureThreshold(c.getReadinessProbe().getFailureThreshold());
                    container.setReadinessProbe(rProbe);
                }

                if (c.getPorts() != null && !c.getPorts().isEmpty()) {
                    List<ContainerPort> ps = new ArrayList<ContainerPort>();
                    for (CreatePortDto p : c.getPorts()) {
                        ContainerPort port = new ContainerPort();
                        port.setContainerPort(Integer.valueOf(p.getPort()));
                        port.setProtocol(p.getProtocol());
                        ps.add(port);
                    }
                    container.setPorts(ps);
                }

                if (c.getEnv() != null && !c.getEnv().isEmpty()) {
                    List<EnvVar> envVars = new ArrayList<EnvVar>();
                    for (CreateEnvDto env : c.getEnv()) {
                        EnvVar eVar = new EnvVar();
                        eVar.setName(env.getKey());
                        eVar.setValue(env.getValue());
                        envVars.add(eVar);
                    }
                    container.setEnv(envVars);
                }

                if (c.getResource() != null) {
                    ResourceRequirements limit = new ResourceRequirements();
                    Map<String, String> res = new HashMap<String, String>();
                    String regEx = "[^0-9]";
                    Pattern p = Pattern.compile(regEx);
                    Matcher m = p.matcher(c.getResource().getCpu());
                    String result = m.replaceAll("").trim();
                    res.put("cpu", result + "m");
//		              res.put("cpu", c.getResource().getCpu());
                    Matcher mm = p.matcher(c.getResource().getMemory());
                    String resultm = mm.replaceAll("").trim();
                    res.put("memory", resultm + "Mi");
                    limit.setLimits(res);
                    limit.setRequests(res);
                    if (c.getLimit() != null) {
                        Map<String, String> resli = new HashMap<String, String>();
                        Matcher l = p.matcher(c.getLimit().getCpu());
                        String resultl = l.replaceAll("").trim();
                        resli.put("cpu", resultl + "m");
                        Matcher ml = p.matcher(c.getLimit().getMemory());
                        String resultml = ml.replaceAll("").trim();
                        resli.put("memory", resultml + "Mi");
                        limit.setLimits(resli);
                    }
                    container.setResources(limit);
                }

                List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
                container.setVolumeMounts(volumeMounts);
                if (c.getStorage() != null && !c.getStorage().isEmpty()) {
                    Map<String, Object> volFlag = new HashMap<String, Object>();
                    for (PersistentVolumeDto vm : c.getStorage()) {
                        if (vm.getType() != null) {
                            switch (vm.getType()) {
                                case Constant.VOLUME_TYPE_NFS:
                                    if (!volFlag.containsKey(vm.getPvcName())) {
                                        PersistentVolumeClaimVolumeSource pvClaim = new PersistentVolumeClaimVolumeSource();
                                        volFlag.put(vm.getPvcName(), vm.getPvcName());
                                        if (vm.getReadOnly().equals("true")) {
                                            pvClaim.setReadOnly(true);
                                        }
                                        if (vm.getReadOnly().equals("false")) {
                                            pvClaim.setReadOnly(false);
                                        }
                                        pvClaim.setClaimName(vm.getPvcName());
                                        Volume vol = new Volume();
                                        vol.setPersistentVolumeClaim(pvClaim);
                                        vol.setName(vm.getPvcName());
                                        volumes.add(vol);
                                    }
                                    VolumeMount volm = new VolumeMount();
                                    volm.setName(vm.getPvcName());
                                    volm.setReadOnly(vm.getReadOnly());
                                    volm.setMountPath(vm.getPath());
                                    volumeMounts.add(volm);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_GITREPO:
                                    if (!volFlag.containsKey(vm.getGitUrl())) {
                                        volFlag.put(vm.getGitUrl(), RandomNum.randomNumber(8));
                                        Volume gitRep = new Volume();
                                        gitRep.setName(volFlag.get(vm.getGitUrl()).toString());
                                        GitRepoVolumeSource gp = new GitRepoVolumeSource();
                                        gp.setRepository(vm.getGitUrl());
                                        gp.setRevision(vm.getRevision());
                                        gitRep.setGitRepo(gp);
                                        volumes.add(gitRep);
                                    }
                                    VolumeMount volmg = new VolumeMount();
                                    volmg.setName(volFlag.get(vm.getGitUrl()).toString());
                                    volmg.setReadOnly(vm.getReadOnly());
                                    volmg.setMountPath(vm.getPath());
                                    volumeMounts.add(volmg);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_EMPTYDIR:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir())) {
                                        volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir(), RandomNum.getRandomString(8));
                                        Volume empty = new Volume();
                                        empty.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir()).toString());
                                        EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                                        if (vm.getEmptyDir() != null && "Memory".equals(vm.getEmptyDir())) {
                                            ed.setMedium(vm.getEmptyDir());//Memory
                                        }
                                        if (vm.getCapacity() != null){
                                            ed.setSizeLimit(vm.getCapacity());//sizeLimit
                                        }
                                        empty.setEmptyDir(ed);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volme = new VolumeMount();
                                    volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir()).toString());
                                    volme.setMountPath(vm.getPath());
                                    volumeMounts.add(volme);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_HOSTPASTH:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath())) {
                                        volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath(), RandomNum.getRandomString(8));
                                        Volume empty = new Volume();
                                        empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                        HostPath hp = new HostPath();
                                        hp.setPath(vm.getHostPath());
                                        empty.setHostPath(hp);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volmh = new VolumeMount();
                                    volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                    volmh.setMountPath(vm.getPath());
                                    volumeMounts.add(volmh);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }

                if (!StringUtils.isEmpty(c.getLog())) {
                    Volume emp = new Volume();
                    emp.setName("logdir" + c.getName());
                    EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                    ed.setMedium("");
                    emp.setEmptyDir(ed);
                    volumes.add(emp);
                    VolumeMount volm = new VolumeMount();
                    volm.setName("logdir" + c.getName());
                    volm.setMountPath(c.getLog());
                    volumeMounts.add(volm);
                    container.setVolumeMounts(volumeMounts);
                    List<EnvVar> envlist = new ArrayList<>();
                    EnvVar env = new EnvVar();
                    env.setName(Constant.PILOT_LOG_PREFIX);
                    env.setValue(c.getLog() + "/*");
                    envlist.add(env);
                    if (container.getEnv()!=null){
                        container.getEnv().add(env);
                    }else {
                        container.setEnv(envlist);
                    }
                }
                convertConfigMap(job.getMetadata().getName()+c.getName(),c.getName(),c.getConfigmap(),volumes,volumeMounts);
                cs.add(container);
            }
        }
        podSpec.setContainers(cs);
        Map<String, Object> nodeselector = job.getSpec().getTemplate().getSpec().getNodeSelector();
        if (!StringUtils.isEmpty(nodeSelector)) {
            String[] ns = {};
            if (nodeSelector.contains(",")) {
                ns = nodeSelector.split(",");
            } else {
                ns[0] = nodeSelector;
            }
            for (String n : ns) {
                if (n.contains("=")) {
                    String[] s = n.split("=");
                    nodeselector.put(s[0], s[1]);
                }
            }
        }
        podSpec.setNodeSelector(nodeselector);
        List<LocalObjectReference> imagePullSecrets = new ArrayList<>();
        LocalObjectReference e = new LocalObjectReference();
        e.setName(userName + "-secret");
        String restartPolicy = job.getSpec().getTemplate().getSpec().getRestartPolicy();
        if (Constant.TYPE_JOB.equals(type)) {
            if (StringUtils.isEmpty(restartPolicy)) {
                podSpec.setRestartPolicy(Constant.RESTARTPOLICY_NERVER);
            } else {
                podSpec.setRestartPolicy(restartPolicy);
            }
        }
        imagePullSecrets.add(e);
        podSpec.setImagePullSecrets(imagePullSecrets);
        if (volumes.size() > 0) {
            podSpec.setVolumes(volumes);
        }
        podTemplate.setSpec(podSpec);
        return podTemplate;
    }

    /**
     * 组装 PodTemplateMetadata
     *
     * @param name
     * @param type
     * @param annotation
     * @param label
     * @param userName
     * @return ObjectMeta
     */
    public static ObjectMeta convertPodTemplateMetadata(String name, String label, String annotation, String userName, String type) throws Exception {
        ObjectMeta metadata = new ObjectMeta();
        Map<String, Object> labels = new HashMap<>();
        labels.put(type, name);
        if (!StringUtils.isEmpty(label)) {
            String[] ls = label.split(",");
            for (String lab : ls) {
                String[] tmp = lab.split("=");
                labels.put(tmp[0], tmp[1]);
            }

        }
        if (com.alibaba.druid.util.StringUtils.isEmpty(userName)) {
            labels.put("nephele/user", userName);
        }
        metadata.setLabels(labels);
        metadata.setAnnotations(convertQosAnnotation(annotation));
        return metadata;
    }

    private static Map<String, Object> convertQosAnnotation(String annotation) throws Exception {
        //annotations-QOS
        Map<String, Object> metadataAnno = new HashMap<>();
        if (annotation != null) {
            if (annotation.contains(",")) {
                String[] qos = annotation.split(",");
                if (qos != null && qos.length > 0) {
                    for (String s : qos) {
                        if (s.contains("ingress") && s.contains("=")) {
                            metadataAnno.put("kubernetes.io/ingress-bandwidth", s.split("=")[1]);
                        }
                        if (s.contains("egress") && s.contains("=")) {
                            metadataAnno.put("kubernetes.io/egress-bandwidth", s.split("=")[1]);
                        }
                    }
                }
            } else {
                if (annotation.contains("ingress") && annotation.contains("=")) {
                    metadataAnno.put("kubernetes.io/ingress-bandwidth", annotation.split("=")[1]);
                }
                if (annotation.contains("egress") && annotation.contains("=")) {
                    metadataAnno.put("kubernetes.io/egress-bandwidth", annotation.split("=")[1]);
                }
            }
        }
        return metadataAnno;
    }

    /**
     * 组装 container
     *
     * @param name
     * @param containers
     * @param initContainers
     * @param logPath
     * @param logService
     * @return Map<String, Object>
     */
    public static Map<String, Object> convertContainer(List<CreateContainerDto> containers, List<CreateContainerDto> initContainers, String logService, String logPath, String name) throws Exception {
        Map<String, Object> res = new HashMap<String, Object>();
        res.put("container", convertContainer(containers, logService, logPath, name, res));
        res.put("initContainer", convertContainer(initContainers, null, null, name, res));
        return res;
    }

    private static List<Container> convertContainer(List<CreateContainerDto> containers, String logService, String logPath, String name, Map<String, Object> res) throws Exception {
        List<Container> cs = new ArrayList<>();
        if (containers != null && !containers.isEmpty()) {
            List<Volume> volumes;
            if(res.containsKey("volume")){
                volumes = (List<Volume>)res.get("volume");
            }else{
                volumes = new ArrayList<>();
            }
            List<PersistentVolumeClaim> volumeClaimTemplates = new ArrayList<>();
            if(res.containsKey("volumeClaimTemplate")){
                volumeClaimTemplates = (List<PersistentVolumeClaim>)res.get("volumeClaimTemplate");
            }else{
                volumeClaimTemplates = new ArrayList<>();
            }
            Map<String, Object> volFlag;
            if(res.containsKey("volFlag")){
                volFlag = (Map<String, Object>)res.get("volFlag");
            }else {
                volFlag = new HashMap<String, Object>();
            }
            for (CreateContainerDto c : containers) {
                Container container = new Container();
                if (c.getSecurityContext() != null && c.getSecurityContext().isSecurity()) {
                    SecurityContext securityContext = new SecurityContext();
                    if (c.getSecurityContext().isPrivileged() == true) {
                        securityContext.setPrivileged(c.getSecurityContext().isPrivileged());
                    }
                    Capabilities capabilities = new Capabilities();
                    if (c.getSecurityContext().getAdd() != null && c.getSecurityContext().getAdd().size() > 0) {
                        capabilities.setAdd(c.getSecurityContext().getAdd());
                    }
                    if (c.getSecurityContext().getDrop() != null && c.getSecurityContext().getDrop().size() > 0) {
                        capabilities.setDrop(c.getSecurityContext().getDrop());
                    }
                    securityContext.setCapabilities(capabilities);
                    container.setSecurityContext(securityContext);
                }
                container.setName(c.getName());
                if (StringUtils.isEmpty(c.getTag())) {
                    container.setImage(c.getImg());
                } else {
                    container.setImage(c.getImg() + ":" + c.getTag());
                }
                container.setCommand(c.getCommand());
                container.setArgs(c.getArgs());
                if (c.getLivenessProbe() != null) {
                    Probe lProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getLivenessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getLivenessProbe().getHttpGet().getPath());
                        if (c.getLivenessProbe().getHttpGet().getPort() == 0) {
                            httpGet.setPort(Constant.LIVENESS_PORT);
                        } else {
                            //lProbe.getHttpGet().setPort(c.getLivenessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getLivenessProbe().getHttpGet().getPort());
                        }
                        lProbe.setHttpGet(httpGet);
                    }

                    if (c.getLivenessProbe().getExec() != null) {
                        if (c.getLivenessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getLivenessProbe().getExec().getCommand());
                            lProbe.setExec(exec);
                        }
                    }

                    if (c.getLivenessProbe().getTcpSocket() != null) {
                        if (c.getLivenessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            tcp.setPort(c.getLivenessProbe().getTcpSocket().getPort());
                        }
                        lProbe.setTcpSocket(tcp);
                    }
                    lProbe.setInitialDelaySeconds(c.getLivenessProbe().getInitialDelaySeconds());
                    lProbe.setTimeoutSeconds(c.getLivenessProbe().getTimeoutSeconds());
                    lProbe.setPeriodSeconds(c.getLivenessProbe().getPeriodSeconds());
                    lProbe.setSuccessThreshold(c.getLivenessProbe().getSuccessThreshold());
                    lProbe.setFailureThreshold(c.getLivenessProbe().getFailureThreshold());
                    container.setLivenessProbe(lProbe);
                }

                if (c.getReadinessProbe() != null) {
                    Probe rProbe = new Probe();
                    HTTPGetAction httpGet = new HTTPGetAction();
                    TCPSocketAction tcp = new TCPSocketAction();
                    if (c.getReadinessProbe().getHttpGet() != null) {
                        httpGet.setPath(c.getReadinessProbe().getHttpGet().getPath());
                        if (c.getReadinessProbe().getHttpGet().getPort() == 0) {
                            rProbe.getHttpGet().setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getHttpGet().setPort(c.getReadinessProbe().getHttpGet().getPort());
                            httpGet.setPort(c.getReadinessProbe().getHttpGet().getPort());
                        }
                        rProbe.setHttpGet(httpGet);
                    }

                    if (c.getReadinessProbe().getExec() != null) {
                        if (c.getReadinessProbe().getExec().getCommand() != null) {
                            ExecAction exec = new ExecAction();
                            exec.setCommand(c.getReadinessProbe().getExec().getCommand());
                            rProbe.setExec(exec);
                        }
                    }

                    if (c.getReadinessProbe().getTcpSocket() != null) {
                        if (c.getReadinessProbe().getTcpSocket().getPort() == 0) {
                            tcp.setPort(Constant.LIVENESS_PORT);
                        } else {
                            // rProbe.getTcpSocket().setPort(c.getReadinessProbe().getTcpSocket().getPort());
                            tcp.setPort(c.getReadinessProbe().getTcpSocket().getPort());
                        }
                        rProbe.setTcpSocket(tcp);
                    }
                    rProbe.setInitialDelaySeconds(c.getReadinessProbe().getInitialDelaySeconds());
                    rProbe.setTimeoutSeconds(c.getReadinessProbe().getTimeoutSeconds());
                    rProbe.setPeriodSeconds(c.getReadinessProbe().getPeriodSeconds());
                    rProbe.setSuccessThreshold(c.getReadinessProbe().getSuccessThreshold());
                    rProbe.setFailureThreshold(c.getReadinessProbe().getFailureThreshold());
                    container.setReadinessProbe(rProbe);
                }

                if (c.getPorts() != null && !c.getPorts().isEmpty()) {
                    List<ContainerPort> ps = new ArrayList<ContainerPort>();
                    for (CreatePortDto p : c.getPorts()) {
                        ContainerPort port = new ContainerPort();
                        port.setContainerPort(Integer.valueOf(p.getPort()));
                        port.setProtocol(p.getProtocol());
                        port.setName(KubeServiceConvert.convertPortName(p.getName(), port.getContainerPort()));
                        ps.add(port);
                    }
                    container.setPorts(ps);
                }

                if (c.getEnv() != null && !c.getEnv().isEmpty()) {
                    List<EnvVar> envVars = new ArrayList<EnvVar>();
                    for (CreateEnvDto env : c.getEnv()) {
                        EnvVar eVar = new EnvVar();
                        eVar.setName(env.getKey());
                        if(StringUtils.isEmpty(env.getType()) || CommonConstant.ENV_TYPE_EQUAL.equals(env.getType())) {
                            eVar.setValue(env.getValue());
                        }else if(CommonConstant.ENV_TYPE_FROM.equals(env.getType())){
                            EnvVarSource envVarSource = new EnvVarSource();
                            ObjectFieldSelector objectFieldSelector = new ObjectFieldSelector();
                            objectFieldSelector.setApiVersion(com.harmonycloud.k8s.constant.Constant.VERSION_V1);
                            objectFieldSelector.setFieldPath(env.getValue());
                            envVarSource.setFieldRef(objectFieldSelector);
                            eVar.setValueFrom(envVarSource);
                        }
                        envVars.add(eVar);
                    }
                    container.setEnv(envVars);
                }

                if (c.getResource() != null) {
                    container.setResources(convertResource(c));
                }

                List<VolumeMount> volumeMounts = new ArrayList<VolumeMount>();
                container.setVolumeMounts(volumeMounts);
                if (c.getStorage() != null && !c.getStorage().isEmpty()) {

                    for (PersistentVolumeDto vm : c.getStorage()) {
                        if (vm.getType() != null) {
                            switch (vm.getType()) {
                                case Constant.VOLUME_TYPE_PVC:
                                    String volumeName = vm.getPvcName().replace(CommonConstant.DOT, CommonConstant.EMPTYSTRING);
                                    //构建spec.containers.volumeMounts
                                    VolumeMount volumeMount = new VolumeMount();
                                    volumeMount.setName(volumeName);
                                    volumeMount.setMountPath(vm.getPath());
                                    volumeMount.setReadOnly(vm.getReadOnly());
                                    volumeMounts.add(volumeMount);
                                    //构建spec.volumes
                                    if (!volFlag.containsKey(vm.getPvcName())) {
                                        volFlag.put(vm.getPvcName(), vm.getPvcName());
                                        Volume pvcVolume = new Volume();
                                        pvcVolume.setName(volumeName);
                                        PersistentVolumeClaimVolumeSource persistentVolumeClaimVolumeSource = new PersistentVolumeClaimVolumeSource();
                                        persistentVolumeClaimVolumeSource.setClaimName(vm.getPvcName());
                                        pvcVolume.setPersistentVolumeClaim(persistentVolumeClaimVolumeSource);
                                        volumes.add(pvcVolume);
                                    }
                                    break;
                                case Constant.VOLUME_TYPE_STORAGECLASS:
                                    //构建spec.containers.volumeMounts
                                    VolumeMount volumeMountSC = new VolumeMount();
                                    volumeMountSC.setName(vm.getVolumeName());
                                    volumeMountSC.setMountPath(vm.getPath());
                                    volumeMounts.add(volumeMountSC);
                                    //构建StatefulSetSpec.volumeClaimTemplates
                                    if (volumeClaimTemplates.stream().noneMatch(pvc -> vm.getVolumeName().equalsIgnoreCase(pvc.getMetadata().getName()))) {
                                        PersistentVolumeClaim pvc = new PersistentVolumeClaim();
                                        ObjectMeta meta = new ObjectMeta();
                                        meta.setName(vm.getVolumeName());
                                        pvc.setMetadata(meta);
                                        PersistentVolumeClaimSpec spec = new PersistentVolumeClaimSpec();
                                        spec.setStorageClassName(vm.getStorageClassName());
                                        spec.setAccessModes(getAccessModes(vm.getReadOnly(), vm.getBindOne()));
                                        ResourceRequirements resource = new ResourceRequirements();
                                        Map<String, Object> request = new HashMap<>();
                                        request.put("storage", vm.getCapacity() + CommonConstant.GI);
                                        resource.setRequests(request);
                                        spec.setResources(resource);
                                        pvc.setSpec(spec);
                                        volumeClaimTemplates.add(pvc);
                                    }
                                    break;
                                case Constant.VOLUME_TYPE_GITREPO:
                                    if (!volFlag.containsKey(vm.getGitUrl())) {
                                        volFlag.put(vm.getGitUrl(), RandomNum.randomNumber(8));
                                        Volume gitRep = new Volume();
                                        gitRep.setName(volFlag.get(vm.getGitUrl()).toString());
                                        GitRepoVolumeSource gp = new GitRepoVolumeSource();
                                        gp.setRepository(vm.getGitUrl());
                                        gp.setRevision(vm.getRevision());
                                        gitRep.setGitRepo(gp);
                                        volumes.add(gitRep);
                                    }
                                    VolumeMount volmg = new VolumeMount();
                                    volmg.setName(volFlag.get(vm.getGitUrl()).toString());
                                    volmg.setReadOnly(vm.getReadOnly());
                                    volmg.setMountPath(vm.getPath());
                                    volumeMounts.add(volmg);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_EMPTYDIR:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_EMPTYDIR + vm.getVolumeName() == null ? "" : vm.getVolumeName())) {
                                        volFlag.put(Constant.VOLUME_TYPE_EMPTYDIR + vm.getVolumeName() == null ? "" : vm.getVolumeName(), vm.getVolumeName());
                                        Volume empty = new Volume();
                                        empty.setName(vm.getVolumeName());
                                        EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                                        if (vm.getEmptyDir() != null && "Memory".equals(vm.getEmptyDir())) {
                                            ed.setMedium(vm.getEmptyDir());//Memory
                                        }
                                        if (vm.getCapacity() != null) {
                                            ed.setSizeLimit(vm.getCapacity());//sizeLimit
                                        }
                                        empty.setEmptyDir(ed);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volme = new VolumeMount();
                                    volme.setName(vm.getVolumeName());
                                    //volme.setName(volFlag.get(Constant.VOLUME_TYPE_EMPTYDIR + vm.getEmptyDir() == null ? "" : vm.getEmptyDir()).toString());
                                    volme.setMountPath(vm.getPath());
                                    volumeMounts.add(volme);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                case Constant.VOLUME_TYPE_HOSTPASTH:
                                    if (!volFlag.containsKey(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath())) {
                                        volFlag.put(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath(), RandomNum.getRandomString(8));
                                        Volume empty = new Volume();
                                        empty.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                        HostPath hp = new HostPath();
                                        hp.setPath(vm.getHostPath());
                                        empty.setHostPath(hp);
                                        volumes.add(empty);
                                    }
                                    VolumeMount volmh = new VolumeMount();
                                    volmh.setName(volFlag.get(Constant.VOLUME_TYPE_HOSTPASTH + vm.getHostPath()).toString());
                                    volmh.setMountPath(vm.getPath());
                                    volumeMounts.add(volmh);
                                    container.setVolumeMounts(volumeMounts);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }



                if (!StringUtils.isEmpty(c.getLog())) {
                    Volume emp = new Volume();
                    emp.setName(Constant.VOLUME_LOGDIR_NAME + c.getName());
                    EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                    ed.setMedium("");
                    emp.setEmptyDir(ed);
                    volumes.add(emp);
                    VolumeMount volm = new VolumeMount();
                    volm.setName(Constant.VOLUME_LOGDIR_NAME + c.getName());
                    volm.setMountPath(c.getLog());
                    volumeMounts.add(volm);
                    container.setVolumeMounts(volumeMounts);
                    List<EnvVar> envlist = new ArrayList<>();
                    EnvVar env = new EnvVar();
                    env.setName(Constant.PILOT_LOG_PREFIX);
                    env.setValue(c.getLog() + "/*");
                    envlist.add(env);
                    env = new EnvVar();
                    env.setName(Constant.PILOT_LOG_PREFIX_TAG);
                    env.setValue("k8s_resource_type=" + c.getParentResourceType() + ",k8s_resource_name=" + name);
                    envlist.add(env);
                    if (container.getEnv()!=null){
                        container.getEnv().addAll(envlist);
                    }else {
                        container.setEnv(envlist);
                    }
                }
                convertConfigMap(name+c.getName(),c.getName(), c.getConfigmap(),volumes,volumeMounts);

                if (!StringUtils.isEmpty(logService) && !logService.equals("false")) {
                    Volume emp = new Volume();
                    emp.setName(Constant.VOLUME_LOGDIR_NAME + c.getName());
                    EmptyDirVolumeSource ed = new EmptyDirVolumeSource();
                    ed.setMedium("");
                    emp.setEmptyDir(ed);
                    volumes.add(emp);
                    VolumeMount volm = new VolumeMount();
                    volm.setName(Constant.VOLUME_LOGDIR_NAME + c.getName());
                    volm.setMountPath(logPath);
                    volumeMounts.add(volm);
                    container.setVolumeMounts(volumeMounts);
                }

                if (c.getImagePullPolicy() != null) {
                    container.setImagePullPolicy(c.getImagePullPolicy());
                }
                cs.add(container);
            }
            res.put("volume", volumes);
            res.put("volumeClaimTemplate", volumeClaimTemplates);
            res.put("volFlag", volFlag);
        }

        return cs;
    }

    public static ResourceRequirements convertResource(CreateContainerDto c) throws Exception {
        ResourceRequirements limit = new ResourceRequirements();
        Map<String, String> res = new HashMap<String, String>();
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(c.getResource().getCpu());
        String result = m.replaceAll("").trim();
        res.put("cpu", result + "m");
        Matcher mm = p.matcher(c.getResource().getMemory());
        String resultm = mm.replaceAll("").trim();
        res.put("memory", resultm + "Mi");
        if(StringUtils.isNotEmpty(c.getResource().getGpu())) {
            res.put(CommonConstant.NVIDIA_GPU, c.getResource().getGpu());
        }
        limit.setLimits(res);
        limit.setRequests(res);
        if (c.getLimit() != null && c.getLimit().getCurrentRate() > 1) {
            Map<String, String> resli = new HashMap<String, String>();
            resli.put("cpu", (Integer.parseInt(result) * c.getLimit().getCurrentRate()) + "m");
            resli.put("memory", (Integer.parseInt(resultm) * c.getLimit().getCurrentRate()) + "Mi");
            if(StringUtils.isNotEmpty(c.getResource().getGpu())) {
                resli.put(CommonConstant.NVIDIA_GPU, c.getResource().getGpu());
            }
            limit.setLimits(resli);
        }
        return limit;
    }

    public static Map<String, Object> convertNodeSelector(String selector) throws Exception {
        //node Selector
        Map<String, Object> nodeSelector = new HashMap<>();
        if (selector != null && !selector.equals("")) {
            if (selector.contains(",")) {
                String[] ns = selector.split(",");
                for (String n : ns) {
                    if (n.contains("=")) {
                        String[] s = n.split("=");
                        nodeSelector.put(s[0], s[1]);
                    }
                }
            } else {
                if (selector.contains("=")) {
                    String[] s = selector.split("=");
                    nodeSelector.put(s[0], s[1]);
                }
            }
        }
        return nodeSelector;
    }

    public static DaemonSet convertDaemonSetCreate(DaemonSetDetailDto detail, String username) throws Exception {
        DaemonSet daemonSet = new DaemonSet();
        ObjectMeta meta = new ObjectMeta();
        meta.setName(detail.getName());

        //labels
        Map<String, Object> lmMap = new HashMap<String, Object>();
        if (!StringUtils.isEmpty(username)) {
            lmMap.put("nephele/user", username);
        }
        lmMap.put(Constant.TYPE_DAEMONSET, detail.getName());
        meta.setLabels(lmMap);

        //annotation
        Map<String, Object> anno = new HashMap<String, Object>();
        String annotation = detail.getAnnotation();
        if (!org.apache.commons.lang3.StringUtils.isEmpty(annotation) && annotation.lastIndexOf(",") == 0) {
            annotation = annotation.substring(0, annotation.length() - 1);
        }
        anno.put("nephele/annotation", annotation == null ? "" : annotation);
        anno.put("nephele/status", Constant.STARTING);
        anno.put("nephele/labels", detail.getLabels() == null ? "" : detail.getLabels());
        meta.setAnnotations(anno);
        meta.setNamespace(detail.getNamespace());
        daemonSet.setMetadata(meta);

        //DaemonSetSpec
        DaemonSetSpec spec = new DaemonSetSpec();

        //templateGeneration
        spec.setTemplateGeneration(detail.getTemplateGeneration());

        //selector
        LabelSelector selector = new LabelSelector();
        Map<String, Object> matchLabels = new HashMap<>();
        matchLabels.put(Constant.TYPE_DAEMONSET, detail.getName());
        selector.setMatchLabels(matchLabels);
        spec.setSelector(selector);

        //PodTemplateSpec
        PodTemplateSpec template = new PodTemplateSpec();

        //调用convertTemplate
        ObjectMeta metadata = new ObjectMeta();
        metadata = K8sResultConvert.convertPodTemplateMetadata(detail.getName(), detail.getLabels(), detail.getAnnotation(), username, Constant.TYPE_DAEMONSET);
        template.setMetadata(metadata);

        //spec
        PodSpec podSpec = new PodSpec();

        //Container
        List<Container> containers = new ArrayList<>();
        Map<String, Object> map = K8sResultConvert.convertContainer(detail.getContainers(), null, detail.getLogService(), detail.getLogPath(), detail.getName());
        containers = (List<Container>) map.get("container");
        podSpec.setContainers(containers);
        //volume
        List<Volume> volumes = new ArrayList<>();
        volumes = (List<Volume>) map.get("volume");
        podSpec.setVolumes(volumes);
        //hostIPC
        podSpec.setHostIPC(detail.isHostIPC());
        //hostPID
        podSpec.setHostPID(detail.isHostPID());
        //hostNetwork
        podSpec.setHostNetwork(detail.isHostNetwork());
        //imagePullSecrets
        List<LocalObjectReference> lors = new ArrayList<LocalObjectReference>();
        LocalObjectReference lor = new LocalObjectReference();
        lor.setName(CommonConstant.ADMIN + "-secret");
        lors.add(lor);
        podSpec.setImagePullSecrets(lors);

        //node Selector
        Map<String, Object> nodeselector = new HashMap<>();
        nodeselector = K8sResultConvert.convertNodeSelector(detail.getNodeSelector());
        podSpec.setNodeSelector(nodeselector);
        //RestartPolicy
        podSpec.setRestartPolicy(detail.getRestartPolicy());
        //hostName
        podSpec.setHostname(detail.getHostName());
        template.setSpec(podSpec);
        spec.setTemplate(template);
        daemonSet.setSpec(spec);
        return daemonSet;
    }

    /**
     * 组装数据——同步宿主主机时区--volume
     *
     * @param containerName required
     * @return Volume
     */
//    public static Volume convertSyncTimeZoneVolume(String containerName) {
//        Volume empty = new Volume();
//        empty.setName(Constant.VOLUME_SYNC_TIME_ZONE_NAME + containerName);
//        HostPath hp = new HostPath();
//        hp.setPath(Constant.VOLUME_SYNC_TIME_ZONE_PATH);
//        empty.setHostPath(hp);
//        return empty;
//    }

    /**
     * 组装数据——同步宿主主机时区--volumeMount
     *
     * @param containerName required
     * @return VolumeMount
     */
//    public static VolumeMount convertSyncTimeZoneVolumeMount(String containerName) {
//        VolumeMount volmh = new VolumeMount();
//        volmh.setName(Constant.VOLUME_SYNC_TIME_ZONE_NAME + containerName);
//        volmh.setMountPath(Constant.VOLUME_SYNC_TIME_ZONE_PATH);
//        volmh.setReadOnly(true);
//        return volmh;
//    }

    /**
     * 组装Prode(前端)
     *
     * @param probe
     * @return Probe
     */
    public static Probe convertProdeDto(Probe probe) throws Exception {
        Probe p = new Probe();
        if (probe.getExec() != null) {
            p.setExec(probe.getExec());
        }
        if (probe.getFailureThreshold() != null) {
            p.setFailureThreshold(probe.getFailureThreshold());
        }
        if (probe.getHttpGet() != null) {
            p.setHttpGet(probe.getHttpGet());
        }
        if (probe.getInitialDelaySeconds() != null) {
            p.setInitialDelaySeconds(probe.getInitialDelaySeconds());
        }
        if (probe.getPeriodSeconds() != null) {
            p.setPeriodSeconds(probe.getPeriodSeconds());
        }
        if (probe.getSuccessThreshold() != null) {
            p.setSuccessThreshold(probe.getSuccessThreshold());
        }
        if (probe.getTcpSocket() != null) {
            p.setTcpSocket(probe.getTcpSocket());
        }
        if (probe.getTimeoutSeconds() != null) {
            p.setTimeoutSeconds(probe.getTimeoutSeconds());
        }
        return p;
    }

    /**
     * DaemonSet详情返回参数封装
     *
     * @return
     * @throws Exception
     */
    public static DaemonSetDetailDto convertDaemonSetDetail(DaemonSet ds) throws Exception {
        DaemonSetDetailDto detail = new DaemonSetDetailDto();
        //封装返回实体
        detail.setName(ds.getMetadata().getName());
        detail.setNamespace(ds.getMetadata().getNamespace());
        Map<String, Object> annotation = ds.getMetadata().getAnnotations();
        if (annotation != null && !annotation.isEmpty()) {
            //labels
            if (annotation.containsKey("nephele/labels")) {
                detail.setLabels(annotation.get("nephele/labels").toString());
            }
            //annotation
            if (annotation.containsKey("nephele/annotation")) {
                detail.setAnnotation(annotation.get("nephele/annotation").toString());
            }
            //updatetime
            if (annotation.containsKey("updateTimestamp")) {
                detail.setUpdateTime(annotation.get("updateTimestamp").toString());
            }
        }
        //状态
        if (ds.getStatus() != null && ds.getStatus().getDesiredNumberScheduled() != null && ds.getStatus().getNumberAvailable() != null && ds.getStatus().getDesiredNumberScheduled().equals(ds.getStatus().getNumberAvailable())) {
            detail.setStatus(Constant.SERVICE_START);
        } else {
            detail.setStatus(Constant.SERVICE_STARTING);
        }
        detail.setPods(ds.getStatus().getDesiredNumberScheduled());
        detail.setRunningPods(ds.getStatus().getNumberAvailable());
        boolean isSystem = true;
        Map<String, Object> l = ds.getMetadata().getLabels();
        if (l != null && !l.isEmpty()) {
            //创建者
            if (l.containsKey("nephele/user")) {
                detail.setCreator(l.get("nephele/user").toString());
            }
            if (l.containsKey(Constant.TYPE_DAEMONSET)) {
                isSystem = false;
            }
        }
        //templateGeneration
        detail.setTemplateGeneration(ds.getSpec().getTemplateGeneration());
        detail.setSystem(isSystem);

        //创建时间
        detail.setCreateTime(ds.getMetadata().getCreationTimestamp());
        PodSpec spec = ds.getSpec().getTemplate().getSpec();

        // nodeselector
        if (spec.getNodeSelector() != null) {
            Map<String, Object> ns = spec.getNodeSelector();
            String nodeSelector = null;
            for (Map.Entry<String, Object> entry : ns.entrySet()) {
                nodeSelector = entry.getKey() + "=" + entry.getValue();
            }
            detail.setNodeSelector(nodeSelector);
        }
        //restartPolicy
        if (spec.getRestartPolicy() != null) {
            detail.setRestartPolicy(spec.getRestartPolicy());
        }
        return detail;
    }

    public static String getDeploymentStatus(Deployment dep) throws Exception {
        String depStatus = null;
        if (dep.getMetadata().getAnnotations() != null && dep.getMetadata().getAnnotations().containsKey("nephele/status")) {
            Integer state = Integer.valueOf(dep.getMetadata().getAnnotations().get("nephele/status").toString());
            switch (state) {
                case 3:
                    if (dep.getSpec().getReplicas() != null && dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getReadyReplicas() != null && dep.getStatus().getReadyReplicas().equals(dep.getStatus().getAvailableReplicas()) ) {
                        if (dep.getSpec().isPaused() && dep.getSpec().getStrategy() != null && dep.getSpec().getStrategy().getRollingUpdate() != null
                                && StringUtils.isNotBlank(String.valueOf(dep.getSpec().getStrategy().getRollingUpdate().getMaxSurge()))) {
                            String maxSurge = String.valueOf(dep.getSpec().getStrategy().getRollingUpdate().getMaxSurge());
                            int precent = maxSurge.equals(Constant.ROLLINGUPDATE_MAX_UNAVAILABLE)? Integer.valueOf(maxSurge.substring(0, maxSurge.indexOf("%"))) : 0;
                            int maxSurgeIns = precent/CommonConstant.PERCENT_HUNDRED * dep.getSpec().getReplicas();
                            depStatus = dep.getSpec().getReplicas() + maxSurgeIns == dep.getStatus().getReadyReplicas() ? Constant.SERVICE_START : Constant.SERVICE_STARTING;
                        } else if (dep.getSpec().getReplicas().equals(dep.getStatus().getReadyReplicas())) {
                            depStatus = Constant.SERVICE_START;
                        } else {
                            depStatus = Constant.SERVICE_STARTING;
                        }
                    } else {
                        depStatus = Constant.SERVICE_STARTING;
                    }
                    break;
                case 2:
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                        depStatus = Constant.SERVICE_STOPPING;
                    } else {
                        depStatus = Constant.SERVICE_STOP;
                    }
                    break;
                default:
                    if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                        depStatus = Constant.SERVICE_START;
                    } else {
                        depStatus = Constant.SERVICE_STOP;
                    }
                    break;
            }
        } else {
            if (dep.getStatus().getAvailableReplicas() != null && dep.getStatus().getAvailableReplicas() > 0) {
                depStatus = Constant.SERVICE_START;
            } else {
                depStatus = Constant.SERVICE_STOP;
            }
        }
        return depStatus;
    }

    public static String getStatefulSetStatus(StatefulSet sta) throws Exception {
        String staStatus = null;
        if (sta.getMetadata().getAnnotations() != null && sta.getMetadata().getAnnotations().containsKey("nephele/status")) {
            Integer state = Integer.valueOf(sta.getMetadata().getAnnotations().get("nephele/status").toString());
            switch (state) {
                case 3:
                    if (sta.getSpec().getReplicas() != null && sta.getStatus().getCurrentReplicas() != null && sta.getStatus().getReadyReplicas() != null && sta.getStatus().getReadyReplicas().equals(sta.getStatus().getCurrentReplicas())&& sta.getStatus().getReadyReplicas().equals(sta.getSpec().getReplicas())) {
                        if (sta.getSpec().getUpdateStrategy() != null && sta.getSpec().getUpdateStrategy().getRollingUpdate() != null
                                && StringUtils.isNotBlank(String.valueOf(sta.getSpec().getUpdateStrategy().getRollingUpdate().getPartition()))) {
                            String maxSurge = String.valueOf(sta.getSpec().getUpdateStrategy().getRollingUpdate().getPartition());
                            int precent = maxSurge.equals(Constant.ROLLINGUPDATE_MAX_UNAVAILABLE)? Integer.valueOf(maxSurge.substring(0, maxSurge.indexOf("%"))) : 0;
                            int maxSurgeIns = precent/ CommonConstant.PERCENT_HUNDRED * sta.getSpec().getReplicas();
                            staStatus = sta.getSpec().getReplicas() + maxSurgeIns == sta.getStatus().getReadyReplicas() ? Constant.SERVICE_START : Constant.SERVICE_STARTING;
                        } else if (sta.getSpec().getReplicas().equals(sta.getStatus().getReadyReplicas())) {
                            staStatus = Constant.SERVICE_START;
                        } else {
                            staStatus = Constant.SERVICE_STARTING;
                        }
                    } else {
                        staStatus = Constant.SERVICE_STARTING;
                    }
                    break;
                case 2:
                    if (sta.getStatus().getCurrentReplicas() != null && sta.getStatus().getCurrentReplicas() > 0) {
                        staStatus = Constant.SERVICE_STOPPING;
                    } else {
                        staStatus = Constant.SERVICE_STOP;
                    }
                    break;
                default:
                    if (sta.getStatus().getCurrentReplicas() != null && sta.getStatus().getCurrentReplicas() > 0) {
                        staStatus = Constant.SERVICE_START;
                    } else {
                        staStatus = Constant.SERVICE_STOP;
                    }
                    break;
            }
        } else {
            if (sta.getStatus().getCurrentReplicas() != null && sta.getStatus().getCurrentReplicas() > 0) {
                staStatus = Constant.SERVICE_START;
            } else {
                staStatus = Constant.SERVICE_STOP;
            }
        }
        return staStatus;
    }

    private static List<String> getAccessModes(Boolean isReadonly, Boolean isBindOne) throws MarsRuntimeException {
        if (isReadonly == null || isBindOne == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        List<String> accessModes = new ArrayList<>();
        if (isReadonly) {
            accessModes.add(CommonConstant.READONLYMANY);
        } else if (isBindOne) {
            accessModes.add(CommonConstant.READWRITEONCE);
        } else {
            accessModes.add(CommonConstant.READWRITEMANY);

        }
        return accessModes;

    }

    private static void convertLabels(Map<String, Object> labels, Map<String, Object> podLabels ,Map<String, Object> tMap){
        Map<String, Object> labelMap = (Map<String, Object>)tMap.get("labels");
        if(labelMap == null){
            labelMap = new HashMap<>();
        }
        //获取对外服务标签
        String serviceType = null;
        if (labels != null && labels.containsKey(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE)) {
            serviceType = labels.get(NODESELECTOR_LABELS_PRE + LABEL_INGRESS_SERVICE).toString();
        }
        if (!StringUtils.isEmpty(serviceType)) {
            labelMap.put(LABEL_INGRESS_SERVICE, serviceType);
            tMap.put("labels", labelMap);
        }

        //获取自动伸缩标签
        String autoscaleStatus = null;
        if(labels != null && labels.containsKey(NODESELECTOR_LABELS_PRE + LABEL_AUTOSCALE)) {
            autoscaleStatus = labels.get(NODESELECTOR_LABELS_PRE + LABEL_AUTOSCALE).toString();
        }
        if(!StringUtils.isEmpty(autoscaleStatus)){
            labelMap.put(LABEL_AUTOSCALE, autoscaleStatus);
            tMap.put("labels", labelMap);
        }

        //获取服务版本标签
        /*String deployVersion = null;
        if(podLabels != null && podLabels.containsKey(Constant.TYPE_DEPLOY_VERSION)) {
            deployVersion = podLabels.get(Constant.TYPE_DEPLOY_VERSION).toString();
        }
        if(!StringUtils.isEmpty(deployVersion)){
            labelMap.put(Constant.TYPE_DEPLOY_VERSION, deployVersion);
            tMap.put("labels", labelMap);
        }*/
    }

    public static void convertConfigMap(String name, String containerName, List<CreateConfigMapDto> configMaps,
                                        List<Volume> volumes, List<VolumeMount> volumeMounts){
        if(CollectionUtils.isEmpty(configMaps)){
            return;
        }
        int configMapNo = 1;
        for (CreateConfigMapDto cm : configMaps) {
            if (cm == null || StringUtils.isBlank(cm.getPath())) {
                continue;
            }
            String filename = cm.getFile();
            Volume cMap = new Volume();
            cMap.setName(containerName + "-" + configMapNo + "-" + cm.getConfigMapId());
            ConfigMapVolumeSource coMap = new ConfigMapVolumeSource();
            coMap.setName(name);
            List<KeyToPath> items = new LinkedList<KeyToPath>();
            KeyToPath key = new KeyToPath();
            key.setKey(cm.getFile() + "v" + cm.getTag());
            key.setPath(filename);
            items.add(key);
            coMap.setItems(items);
            cMap.setConfigMap(coMap);
            volumes.add(cMap);
            VolumeMount volm = new VolumeMount();
            volm.setName(containerName + "-" + configMapNo + "-" + cm.getConfigMapId());
            volm.setMountPath((cm.getPath().endsWith("/")?cm.getPath():(cm.getPath() + "/")) + filename);
            volm.setSubPath(filename);
            volumeMounts.add(volm);
            configMapNo++;
        }
    }
}
