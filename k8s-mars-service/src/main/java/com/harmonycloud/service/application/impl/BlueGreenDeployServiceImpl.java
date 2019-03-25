package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.dto.application.SecurityContextDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.convert.KubeServiceConvert;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Describe 蓝绿发布实现类
 * @Author jmi
 * @Date created at 2017/12/28
 */
@Service
public class BlueGreenDeployServiceImpl extends VolumeAbstractService implements BlueGreenDeployService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private DeploymentService dpService;

    @Autowired
    private PVCService pvcService;

    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    private PvService pvService;

    @Autowired
    private ServicesService sService;

    @Autowired
    private ReplicasetsService rsService;

    @Autowired
    private PodService podService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private PersistentVolumeClaimService persistentVolumeClaimService;

    @Autowired
    private IstioService istioService;

    @Override
    public ActionReturnUtil deployByBlueGreen(UpdateDeployment updateDeployment, String userName, String projectId) throws Exception {
        // 参数判空
        if (Objects.isNull(updateDeployment) || StringUtils.isBlank(updateDeployment.getNamespace())
                || CollectionUtils.isEmpty(updateDeployment.getContainers())
                || StringUtils.isBlank(updateDeployment.getInstance())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        updateDeployment.setProjectId(projectId);
        String namespace = updateDeployment.getNamespace();
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(updateDeployment.getNamespace());
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }


        String name = updateDeployment.getName();
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector",CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + name+"="+ name);

        K8SClientResponse pvcsRes = pvcService.doSepcifyPVC(namespace, queryP, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcsRes.getStatus()) && pvcsRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcsRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        //获取所有带有标签的pvc
        PersistentVolumeClaimList persistentVolumeClaimList = JsonUtil.jsonToPojo(pvcsRes.getBody(), PersistentVolumeClaimList.class);
        List<PersistentVolumeClaim> persistentVolumeClaims = persistentVolumeClaimList.getItems();

        //pvc绑定服务
        List<UpdateContainer> containers = updateDeployment.getContainers();
        for (UpdateContainer container : containers) {
            List<PersistentVolumeDto> storage = container.getStorage();
            if(storage!=null){
                for (PersistentVolumeDto persistentVolumeDto : storage) {
                    String pvcName = persistentVolumeDto.getPvcName();
                    PersistentVolumeClaim pvcByName = pvcService.getPvcByName(namespace, pvcName, cluster);
                    if (null ==pvcByName){
                        break;
                    }
                    Map<String, Object> newLabels = pvcByName.getMetadata().getLabels();
                    newLabels.put(CommonConstant.LABEL_KEY_APP+CommonConstant.SLASH + name,name);
                    pvcService.updatePvcByName(pvcByName,cluster);
                    for (int j=0; j < persistentVolumeClaims.size(); j++) {
                        PersistentVolumeClaim persistentVolumeClaim = persistentVolumeClaims.get(j);
                        if(pvcByName.getMetadata().getName().equals(persistentVolumeClaim.getMetadata().getName())){
                            persistentVolumeClaims.remove(persistentVolumeClaim);
                        }
                    }
                }
            }
        }

        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        // 设置pod label
        boolean isExistLabel = false;
        String blueGreenLabel = Constant.NODESELECTOR_LABELS_PRE + "bluegreen";
        Map<String, Object> podLabels = dep.getSpec().getTemplate().getMetadata().getLabels();
        Map<String, Object> depLabels = dep.getMetadata().getLabels();
        String currentLabel = depLabels.get(blueGreenLabel).toString();
        if (Objects.nonNull(podLabels)) {
            for (Map.Entry<String, Object> entry : podLabels.entrySet()) {
                if (blueGreenLabel.equals(entry.getKey())) {
                    if (Objects.nonNull(entry.getValue())) {
                        String value = entry.getValue().toString();
                        if (value.lastIndexOf("-") > -1) {
                            Integer version = Integer.valueOf(value.substring(value.lastIndexOf("-") + 1));
                            version++;
                            podLabels.put(blueGreenLabel, name + "-" + version);
                            dep.getSpec().getTemplate().getMetadata().setLabels(podLabels);
                            depLabels.put(blueGreenLabel, name + "-" + version);
                            dep.getMetadata().setLabels(depLabels);
                            currentLabel = name + "-" + version;
                            isExistLabel = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!isExistLabel) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_FAILURE);
        }
        //引入StorageClass后无需创建pv
        //checkPvAndCreateVolume(updateDeployment.getContainers(), name, namespace, cluster, projectId);

        // 创建configmap
        Map<String, String> containerToConfigMap = deploymentsService.createConfigMapInUpdate(namespace, name, cluster, updateDeployment.getContainers());

        // 更新deployment对象内的数据
        PodTemplateSpec podTemplateSpec = KubeServiceConvert.convertDeploymentUpdate(dep.getSpec().getTemplate(), updateDeployment.getContainers(), name, containerToConfigMap, cluster);
        dep.getSpec().setTemplate(podTemplateSpec);
        // 设置蓝绿发布相关的参数
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("RollingUpdate");
        dep.getSpec().setMinReadySeconds(Constant.POD_MIN_READY_FIVE_SECONDS);
        RollingUpdateDeployment ru = new RollingUpdateDeployment();
        ru.setMaxSurge("100%");
        ru.setMaxUnavailable(Constant.POD_MAX_UNAVAILABLE);
        strategy.setRollingUpdate(ru);
        dep.getSpec().setStrategy(strategy);
        dep.getSpec().setPaused(false);

        //更新服务版本标签
        if(StringUtils.isNotEmpty(updateDeployment.getDeployVersion())){
            dep.getSpec().getTemplate().getMetadata().getLabels().put(Constant.TYPE_DEPLOY_VERSION, updateDeployment.getDeployVersion());
        }else {
            if (dep.getSpec().getTemplate().getMetadata().getLabels().containsKey(Constant.TYPE_DEPLOY_VERSION)){
                dep.getSpec().getTemplate().getMetadata().getLabels().remove(Constant.TYPE_DEPLOY_VERSION);
            }
        }

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        RequestAttributes request = RequestContextHolder.currentRequestAttributes();
        int replicas = Integer.valueOf(updateDeployment.getInstance());
        final String newBlueGreenLabel = currentLabel;
        ThreadPoolExecutorFactory.executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    RequestContextHolder.setRequestAttributes(request, true);
                    for (; ; ) {
                        // 暂停两秒钟等待蓝绿发布开始
                        Thread.sleep(Constant.THREAD_SLEEP_TIME);
                        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null,
                                HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
                            logger.error("蓝绿发布查询Deployment报错");
                            continue;
                        }
                        Deployment updatingDep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

                        //根据当前deployment的label获取rs,如果存在rs,就设置成paused
                        Map<String, Object> bodys = new HashMap<String, Object>();
                        bodys.put("labelSelector", "app=" + name + CommonConstant.COMMA +
                                blueGreenLabel + CommonConstant.EQUALITY_SIGN + newBlueGreenLabel);
                        K8SClientResponse rsResponse = rsService.doRsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(rsResponse.getStatus())) {
                            logger.error("蓝绿发布获取rs报错，{}", rsResponse.getBody());
                            continue;
                        }
                        ReplicaSetList rsList = K8SClient.converToBean(rsResponse, ReplicaSetList.class);
                        List<ReplicaSet> replicaSets = rsList.getItems();
                        if (CollectionUtils.isNotEmpty(replicaSets)) {
                            // 暂停升级参数设置
                            if (updatingDep.getSpec() == null) {
                                continue;
                            }
                            updatingDep.getSpec().setPaused(true);
                            Map<String, Object> depBodys = CollectionUtil.transBean2Map(updatingDep);

                            // 暂停升级
                            Map<String, Object> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            K8SClientResponse pauseRes = dpService.doSpecifyDeployment(namespace, name, headers, depBodys,
                                    HTTPMethod.PUT, cluster);
                            if (!HttpStatusUtil.isSuccessStatus(pauseRes.getStatus())) {
                                logger.error("蓝绿发布暂停Deployment报错，{}", pauseRes.getBody());
                            }
                            break;
                        } else if (updatingDep.getStatus() != null && updatingDep.getStatus().getReplicas() != null
                                && updatingDep.getStatus().getReplicas() == replicas*CommonConstant.NUM_TWO) {
                            break;
                        } else {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    logger.error("蓝绿发布错误", e);
                }
            }
        });

        // 进行蓝绿发布
        K8SClientResponse putRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT,
                cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            logger.error("蓝绿发布失败", putRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ActionReturnUtil switchFlow(String name, String namespace, boolean isSwitchNew) throws Exception {
        // 参数判空
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace) || Objects.isNull(isSwitchNew)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        // 获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        updateServiceSelector(namespace, name, cluster, dep, isSwitchNew);
        return ActionReturnUtil.returnSuccess();
    }

    private void updateServiceSelector(String namespace, String name, Cluster cluster, Deployment dep, boolean isSwitchNew) throws Exception {
        // 获取label
        Map<String, Object> depLabel = dep.getMetadata().getLabels();
        String blueGreenLabelValue = depLabel.get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen").toString();

        // 获取service
        K8SClientResponse rsRes = sService.doSepcifyService(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(rsRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }

        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(),
                com.harmonycloud.k8s.bean.Service.class);

        // 修改service的selector
        Map<String, Object> selector = (Map<String, Object>) service.getSpec().getSelector();
        List<ServicePort> ports = new ArrayList<>();
        if (isSwitchNew) {
            selector.put(Constant.NODESELECTOR_LABELS_PRE + "bluegreen", blueGreenLabelValue);
            ports = KubeServiceConvert.convertServicePort(dep.getSpec().getTemplate().getSpec().getContainers());
        } else {
            Integer version = Integer.valueOf(blueGreenLabelValue.substring(blueGreenLabelValue.lastIndexOf("-") + 1));
            version--;
            selector.put(Constant.NODESELECTOR_LABELS_PRE + "bluegreen", name + "-" + version);

            //获取rs
            Map<String, Object> bodys = new HashMap<>();
            bodys.put("labelSelector", "app=" + name + CommonConstant.COMMA + Constant.NODESELECTOR_LABELS_PRE + "bluegreen=" + name + "-" + version);
            K8SClientResponse rsResponse = rsService.doRsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(rsResponse.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(rsResponse.getBody(), UnversionedStatus.class);
                throw new MarsRuntimeException(status.getMessage());
            }
            ReplicaSetList rsList = K8SClient.converToBean(rsResponse, ReplicaSetList.class);
            List<ReplicaSet> replicaSets = rsList.getItems();
            for (ReplicaSet rs : replicaSets) {
                if (rs.getSpec().getReplicas() > 0) {
                    ports = KubeServiceConvert.convertServicePort(rs.getSpec().getTemplate().getSpec().getContainers());
                    break;
                }
            }
        }
        service.getSpec().setPorts(ports);
        service.getSpec().setSelector(selector);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(service);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse putRes = sService.doSepcifyService(namespace, name, headers, bodys, HTTPMethod.PUT,
                cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_SWITCH_FLOW_FAILURE);
        }
    }
    @Override
    public ActionReturnUtil confirmToNewVersion(String name, String namespace) throws Exception {
        // 参数判空
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        // 升级到新版本
        updateToNewVersion(name, namespace, cluster);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil rollbackToOldVersion(String name, String namespace) throws Exception {
        // 参数判空
        if (StringUtils.isBlank(name) || StringUtils.isBlank(namespace)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        DeploymentRollback deploymentRollback = new DeploymentRollback();
        RollbackConfig rollbackConfig = new RollbackConfig();
        deploymentRollback.setName(name);

        //将参数设置成0自动回滚到最新的版本
        rollbackConfig.setRevision(Constant.ROLLBACK_REVERSION);
        deploymentRollback.setRollbackTo(rollbackConfig);

        K8SClientResponse rollBackRes = dpService.rollbackSpecifiedDeployment(deploymentRollback, namespace, name, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rollBackRes.getStatus())) {
            logger.error("蓝绿保留旧版本失败", rollBackRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_ROLLBACK_FAILURE);
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            logger.error("获取deployment失败", dp.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_ROLLBACK_FAILURE);
        }
        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
        dep.getSpec().setPaused(false);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        K8SClientResponse dpUpdate = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpUpdate.getStatus())) {
            logger.error("取消deployment的pause", dpUpdate.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_ROLLBACK_FAILURE);
        }
        K8SClientResponse dpNew = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpNew.getStatus())) {
            logger.error("获取deployment失败", dpNew.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_ROLLBACK_FAILURE);
        }
        Deployment depNew = JsonUtil.jsonToPojo(dpNew.getBody(), Deployment.class);
        checkPvcInNewAndOldDeployment(depNew.getSpec().getTemplate(), namespace, name, cluster);
        updateServiceSelector(namespace, name, cluster, dep, false);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getInfoAboutTwoVersion(String name, String namespace) throws Exception {
        if (StringUtils.isEmpty(namespace) || StringUtils.isEmpty(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        //获取当前deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            logger.error("获取deployment失败,{}", depRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_NOT_FIND);
        }

        Deployment deployment = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        boolean paused = deployment.getSpec().isPaused();
        Map<String, Object> depAnno = deployment.getMetadata().getAnnotations();
        //当前deployment的版本
        int version = Integer.valueOf(depAnno.get("deployment.kubernetes.io/revision").toString());

        //获取所有的rs
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", "app=" + name);
        K8SClientResponse rsresponse = rsService.doRsByNamespace(namespace, null, bodys, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsresponse.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(rsresponse.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        ReplicaSetList rsList = K8SClient.converToBean(rsresponse, ReplicaSetList.class);
        List<ReplicaSet> replicaSets = rsList.getItems();

        Map<Integer, Object> res = new HashMap<>();

        // 对rs进行排序
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        // 对时间进行升序
        Collections.sort(replicaSets, new Comparator<ReplicaSet>() {

            @Override
            public int compare(ReplicaSet o1, ReplicaSet o2) {
                try {
                    return Long.valueOf(sdf.parse(o1.getMetadata().getCreationTimestamp()).getTime())
                            .compareTo(Long.valueOf(sdf.parse(o2.getMetadata().getCreationTimestamp()).getTime()));
                } catch (ParseException e) {
                    logger.error("replicaSets排序错误", e);
                    return 0;
                }
            }
        });
        List<ReplicaSet> replicaSetList = new ArrayList<>();
        for (ReplicaSet rs : replicaSets) {
            Map<String, Object> annotation = rs.getMetadata().getAnnotations();
            if (null != annotation.get("deployment.kubernetes.io/revision")) {
                int rsVersion = Integer.valueOf(annotation.get("deployment.kubernetes.io/revision").toString());
                //如果是暂停状态，则判断当前的rs版本是否与deployment版本相同或者少1
                if (paused) {
                    if (rsVersion == version || rsVersion == version - CommonConstant.NUM_ONE) {
                        replicaSetList.add(rs);
                    }
                } else {
                    //如果不处于暂停状态，只要判断当前的rs版本是否与deployment版本相同
                    if (rsVersion == version) {
                        res = loopReplicas(Arrays.asList(rs), cluster, name, null);
                        break;
                    }
                }
            }
        }
        //判断是否在蓝绿升级
        if (replicaSetList.size() == CommonConstant.NUM_TWO) {
            //获取service
            K8SClientResponse rsRes = sService.doSepcifyService(namespace, name, null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
                throw new MarsRuntimeException(rsRes.getBody());
            }
            com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(),
                    com.harmonycloud.k8s.bean.Service.class);
            Map<String, Object> selector = (Map<String, Object>) service.getSpec().getSelector();
            ReplicaSet rs1 = replicaSetList.get(0);
            String blueGreenLabel1 = rs1.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen").toString();
            ReplicaSet rs2 = replicaSetList.get(CommonConstant.NUM_ONE);
            String blueGreenLabel2 = rs2.getMetadata().getLabels().get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen").toString();
            //如果两个蓝绿的标签一致则是在灰度升级
            if (blueGreenLabel1.equals(blueGreenLabel2)) {
                res = loopReplicas(Arrays.asList(rs2), cluster, name, null);
            } else {
                //蓝绿升级
                String currentLabel = null;
                if (Objects.nonNull(selector.get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen"))) {
                    currentLabel = selector.get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen").toString();
                }
                res = loopReplicas(replicaSetList, cluster, name, currentLabel);
            }
        }
        return ActionReturnUtil.returnSuccessWithData(res);
    }

    private Map<Integer, Object> loopReplicas(List<ReplicaSet> replicaSets, Cluster cluster, String name, String currentLabel) throws Exception {
        Map<Integer, Object> res = new HashMap<>();
        int tag = 0;
        for (ReplicaSet rs : replicaSets) {
            Map<String, Object> bodys = new HashMap<>();
            //获取rs的版本作为返回Map的key
            if (rs.getMetadata().getAnnotations() != null && !StringUtils.isEmpty(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString())) {
                tag = Integer.parseInt(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString());
            } else {
                tag++;
            }
            Map<String, Object> rsLabels = rs.getMetadata().getLabels();
            if (!StringUtils.isEmpty(rsLabels.get("pod-template-hash").toString())) {
                //根据label获取rs管辖的pod列表
                bodys.put("labelSelector", "pod-template-hash=" + rsLabels.get("pod-template-hash"));
                K8SClientResponse podRes = podService.getPodByNamespace(rs.getMetadata().getNamespace(), null, bodys, HTTPMethod.GET, cluster);
                if (!HttpStatusUtil.isSuccessStatus(podRes.getStatus())) {
                    throw new MarsRuntimeException(podRes.getBody());
                }
                PodList podList = JsonUtil.jsonToPojo(podRes.getBody(), PodList.class);
                List<PodDetail> podDetails = new ArrayList<>();
                podDetails.addAll(K8sResultConvert.podListConvert(podList, "v" + tag));
                //组装container信息
                List<ContainerOfPodDetail> containerOfPodDetails = K8sResultConvert.convertReplicaSetContainer(rs,
                                                rs.getSpec().getTemplate().getSpec().getContainers(), cluster);
                Map<String, Object> tmMap = new HashMap<>();
                tmMap.put("pods", podDetails);
                tmMap.put("containers", containerOfPodDetails);
                if(rs.getSpec().getTemplate().getMetadata().getLabels().containsKey(Constant.TYPE_DEPLOY_VERSION)){
                    tmMap.put(Constant.TYPE_DEPLOY_VERSION, rs.getSpec().getTemplate().getMetadata().getLabels().get(Constant.TYPE_DEPLOY_VERSION));
                }
                if (StringUtils.isNotBlank(currentLabel)) {
                    boolean isCurrent = rsLabels.get(Constant.NODESELECTOR_LABELS_PRE + "bluegreen").toString().equals(currentLabel) ? true : false;
                    tmMap.put("current", isCurrent);
                }
                res.put(tag, tmMap);
            }
        }
        return res;
    }


    /**
     * 确认更新到新版本
     *
     * @param name
     * @param namespace
     * @param cluster
     * @throws Exception
     */
    private void updateToNewVersion(String name, String namespace, Cluster cluster) throws Exception {
        // 获取deployment,取消pause状态
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        dep.getSpec().setPaused(false);
        dep.getSpec().setMinReadySeconds(0);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        // 更新deployment
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse pauseRes = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT,
                cluster);
        if (!HttpStatusUtil.isSuccessStatus(pauseRes.getStatus())) {
            logger.error("升级到新版本Deployment报错");
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_UPDATE_FAILURE);
        }
        K8SClientResponse dpNew = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpNew.getStatus())) {
            logger.error("获取deployment失败", dpNew.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_BLUE_GREEN_ROLLBACK_FAILURE);
        }
        Deployment depNew = JsonUtil.jsonToPojo(dpNew.getBody(), Deployment.class);
        checkPvcInNewAndOldDeployment(depNew.getSpec().getTemplate(), namespace, name, cluster);
        // 更新service的selector
        updateServiceSelector(namespace, name, cluster, dep, true);
    }

    private void checkPvcInNewAndOldDeployment(PodTemplateSpec podTemplateSpec, String namespace, String name, Cluster cluster) throws Exception {
        List<Volume> rollbackVolumes = new ArrayList<>();
        List<String> rollbackPvc = new ArrayList<>();
        if (null != podTemplateSpec.getSpec() && null != podTemplateSpec.getSpec().getVolumes()) {
            rollbackVolumes = podTemplateSpec.getSpec().getVolumes();
            rollbackPvc = rollbackVolumes.stream().filter(rv->rv.getPersistentVolumeClaim() != null)
                    .map(rv->rv.getPersistentVolumeClaim().getClaimName()).collect(Collectors.toList());
        }


        // 获取pvc信息
        Map<String, Object> label = new HashMap<>();
        label.put("labelSelector", CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + name + CommonConstant.EQUALITY_SIGN + name);
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, label, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }

        PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcRes.getBody(), PersistentVolumeClaimList.class);

        List<String> currentPvc = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(pvcList.getItems())) {
            for(PersistentVolumeClaim pvc : pvcList.getItems()){
                currentPvc.add(pvc.getMetadata().getName());
                //去掉挂载的pvc删除标签
                if(!rollbackPvc.contains(pvc.getMetadata().getName())){
                    pvc.getMetadata().getLabels().remove(CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + name);
                    pvcService.updatePvcByName(pvc, cluster);
                }
            }
        }

        //新挂载的pvc增加标签
        label.clear();
        label.put(CommonConstant.LABEL_KEY_APP + CommonConstant.SLASH + name, name);
        if(CollectionUtils.isNotEmpty(rollbackPvc)) {
            List<String> newAddedPvcList = (List<String>) ListUtils.removeAll(rollbackPvc, currentPvc);
            for(String pvc : newAddedPvcList){
                persistentVolumeClaimService.updateLabel(pvc, namespace, cluster, label);
            }
        }

    }
}
