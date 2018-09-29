package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.ServiceTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.RollbackBean;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.*;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.PersistentVolumeService;
import com.harmonycloud.service.application.StatefulSetVersionControlService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.platform.bean.CanaryDeployment;
import com.harmonycloud.service.platform.bean.PvDto;
import com.harmonycloud.service.platform.bean.UpdateContainer;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.KubeServiceConvert;
import com.harmonycloud.service.platform.dto.PodDto;
import com.harmonycloud.service.platform.dto.ReplicaSetDto;
import com.harmonycloud.service.platform.service.WatchService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Created by czm on 2017/4/26.
 */

@Service
public class VersionControlServiceImpl extends VolumeAbstractService implements VersionControlService {


    @Autowired
    DeploymentService dpService;

    @Autowired
    ServicesService sService;

    @Autowired
    com.harmonycloud.service.application.ServiceService serviceService;

    @Autowired
    WatchService watchService;

    @Autowired
    PodService podService;

    @Autowired
    private PersistentVolumeService volumeSerivce;

    @Autowired
    ReplicasetsService rsService;

    @Autowired
    PVCService pvcService;

    @Autowired
    private PvService pvService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private StatefulSetVersionControlService statefulSetVersionControlService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //更新Deployment的时候不断查询状态
    @Override
    public ActionReturnUtil canaryUpdate(CanaryDeployment detail, int instances, String userName) throws Exception {
        //参数判空
        if (Objects.isNull(detail) || CollectionUtils.isEmpty(detail.getContainers()) || StringUtils.isBlank(detail.getName()) || StringUtils.isBlank(detail.getNamespace())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }

        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(detail.getNamespace());
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        ServiceTypeEnum serviceTypeEnum;
        if(StringUtils.isBlank(detail.getServiceType())){
            serviceTypeEnum = ServiceTypeEnum.DEPLOYMENT;
        }else {
            serviceTypeEnum = ServiceTypeEnum.valueOf(detail.getServiceType().toUpperCase());
        }
        switch (serviceTypeEnum){
            case DEPLOYMENT:
                return this.canaryUpdateForDeployment(detail, instances, cluster);
            case STATEFULSET:
                StatefulSet statefulSet = statefulSetVersionControlService.canaryUpdateForStatefulSet(detail, instances, cluster);
                //更新service端口
                increaseServiceByDeployment(statefulSet.getMetadata(), statefulSet.getSpec().getTemplate(), cluster);
                return ActionReturnUtil.returnSuccessWithData("success");
            default:
                break;
        }
        return ActionReturnUtil.returnSuccess();
    }

    //更新Deployment的时候不断查询状态
    private ActionReturnUtil canaryUpdateForDeployment(CanaryDeployment detail, int instances, Cluster cluster) throws Exception {

        CountDownLatch mCountDownLatch = new CountDownLatch(CommonConstant.NUM_ONE);

        final RequestAttributes request = RequestContextHolder.getRequestAttributes();
        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(depRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        //在这里创建configmap,在convertAppPut当中增加Deployment的注解,返回的是容器和configmap之间的映射关系列表
        Map<String, String> containerToConfigMap = deploymentsService.createConfigMapInUpdate(detail.getNamespace(), detail.getName(), cluster, detail.getContainers());

        //更新旧的Deployment对象
        PodTemplateSpec podTemplateSpec = KubeServiceConvert.convertDeploymentUpdate(dep.getSpec().getTemplate(), detail.getContainers(), detail.getName(), containerToConfigMap, cluster);
        dep.getSpec().setTemplate(podTemplateSpec);
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        Map<String, Object> anno = dep.getMetadata().getAnnotations();
        anno.put("updateTimestamp", updateTime);
        dep.getMetadata().setAnnotations(anno);
        //设置灰度升级相关的参数
        DeploymentStrategy strategy = new DeploymentStrategy();
        strategy.setType("RollingUpdate");
        //当实例数目不为0时才需要灰度更新
        if (detail.getInstances() != 0) {
            if (detail.getSeconds() < Constant.POD_MIN_READY_FIVE_SECONDS) {
                dep.getSpec().setMinReadySeconds(Constant.POD_MIN_READY_FIVE_SECONDS);
            } else {
                dep.getSpec().setMinReadySeconds(detail.getSeconds());
            }
            RollingUpdateDeployment ru = new RollingUpdateDeployment();
            if (detail.getMaxSurge() == 0 && detail.getMaxUnavailable() == 0) {
                ru.setMaxSurge(Constant.POD_MAX_SURGE);
                ru.setMaxUnavailable(Constant.POD_MAX_UNAVAILABLE);
            } else {
                ru.setMaxSurge(detail.getMaxSurge());
                ru.setMaxUnavailable(detail.getMaxUnavailable());
            }
            strategy.setRollingUpdate(ru);
            dep.getSpec().setStrategy(strategy);
        }
        dep.getSpec().setPaused(false);

        //获取pvc
        String namespace = detail.getNamespace();
        String name = detail.getName();
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

        //获取传过来的pvc
        List<UpdateContainer> containers = detail.getContainers();
        List<Volume> volumes = new ArrayList<>();
        for (int i=0; i<containers.size();i++) {
            UpdateContainer container = containers.get(i);
            List<VolumeMount> volumeMounts = new ArrayList<>();
            List<PersistentVolumeDto> storage = container.getStorage();
            if(storage.size()==0){
                for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaims) {
                    Map<String, Object> labels = persistentVolumeClaim.getMetadata().getLabels();
                    labels.remove(CommonConstant.LABEL_KEY_APP+CommonConstant.SLASH + name);
                    pvcService.updatePvcByName(persistentVolumeClaim,cluster);
                }
            }else{
                for (PersistentVolumeDto persistentVolumeDto : storage) {
                    String pvcName = persistentVolumeDto.getPvcName();
                    PersistentVolumeClaim pvcByName = pvcService.getPvcByName(namespace, pvcName, cluster);
                    Map<String, Object> newLabels = pvcByName.getMetadata().getLabels();
                    newLabels.put(CommonConstant.LABEL_KEY_APP+CommonConstant.SLASH + name,name);
                    K8SClientResponse  response = pvcService.updatePvcByName(pvcByName,cluster);
                    if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                        throw new MarsRuntimeException(response.getBody());
                    }
                    //拼装存储卷
                    Volume volume = new Volume();
                    PersistentVolumeClaimVolumeSource claim = new PersistentVolumeClaimVolumeSource();
                    volume.setName(pvcName);
                    claim.setClaimName(pvcName);
                    volume.setPersistentVolumeClaim(claim);
                    volumes.add(volume);
                    VolumeMount volumeMount = new VolumeMount();
                    volumeMount.setName(pvcName);
                    volumeMount.setMountPath(persistentVolumeDto.getPath());
                    volumeMount.setReadOnly(persistentVolumeDto.getReadOnly());
                    volumeMounts.add(volumeMount);
                    for (int j=0; j<persistentVolumeClaims.size();j++) {
                        PersistentVolumeClaim persistentVolumeClaim = persistentVolumeClaims.get(j);
                        if(pvcByName.getMetadata().getName().equals(persistentVolumeClaim.getMetadata().getName())){
                            persistentVolumeClaims.remove(persistentVolumeClaim);
                        }
                    }
                }

            }
            dep.getSpec().getTemplate().getSpec().getContainers().get(i).setVolumeMounts(volumeMounts);
        }
        dep.getSpec().getTemplate().getSpec().setVolumes(volumes);
        //移除原pv标签
        for (PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaims){
            Map<String, Object> labels = persistentVolumeClaim.getMetadata().getLabels();
            labels.remove(CommonConstant.LABEL_KEY_APP+CommonConstant.SLASH + name);
            persistentVolumeClaim.getMetadata().setLabels(labels);
            pvcService.updatePvcByName(persistentVolumeClaim,cluster);
        }
        //将页面上填写的数据保存到annotation中
        anno.put("deployment.canaryupdate/maxsurge", String.valueOf(detail.getMaxSurge()));
        anno.put("deployment.canaryupdate/maxunavailable", String.valueOf(detail.getMaxUnavailable()));
        anno.put("deployment.canaryupdate/instances", String.valueOf(detail.getInstances()));
        dep.getMetadata().setAnnotations(anno);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        ESFactory.executor.execute((new Runnable() {
            @Override
            public void run() {
                try {
                    RequestContextHolder.setRequestAttributes(request);

                    for (; ; ) {
                        //如果实例数为0则不需要灰度更新
                        if (detail.getInstances() == 0) {
                            break;
                        }
                        //暂停两秒钟等待灰度升级开始
                        Thread.sleep(2000);

                        K8SClientResponse dp = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), null, null, HTTPMethod.GET, cluster);
                        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
                            logger.error("灰度升级查询Deployment报错");
                        }
                        Deployment dep1 = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

                        //如果实例数量是副本数的话就说明没有更新必要
                        if (dep1.getStatus() != null && dep1.getStatus().getUpdatedReplicas() != null && dep1.getStatus().getUpdatedReplicas().equals(dep1.getSpec().getReplicas())) {
                            break;
                        }
                        //升级达到指定个数
                        if (dep1.getStatus() != null && dep1.getStatus().getUpdatedReplicas() != null && dep1.getStatus().getUpdatedReplicas() == instances) {
                            //暂停升级参数设置
                            if (dep1.getSpec() == null) {
                                continue;
                            }
                            dep1.getSpec().setPaused(true);

                            Map<String, Object> bodys = CollectionUtil.transBean2Map(dep1);

                            //暂停升级
                            Map<String, Object> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            K8SClientResponse pauseRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers, bodys, HTTPMethod.PUT, cluster);

                            if (!HttpStatusUtil.isSuccessStatus(pauseRes.getStatus())) {
                                logger.error("灰度升级暂停Deployment报错");
                            }
                            break;
                        } else if (dep1.getStatus() != null && dep1.getStatus().getReplicas() != null && dep1.getStatus().getReplicas() == instances) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("灰度升级失败", e);
                } finally {
                    mCountDownLatch.countDown();
                }
            }
        }));

        //触发灰度升级
        K8SClientResponse putRes = dpService.doSpecifyDeployment(detail.getNamespace(), detail.getName(), headers,
                bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(putRes.getStatus())) {
            logger.error("触发灰度升级失败, {}", putRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_CANARY_SCALE_FAILURE);
        }

        //在这里阻塞线程，等待子线程唤醒
        mCountDownLatch.await();

        //更新service端口
        increaseServiceByDeployment(dep.getMetadata(), dep.getSpec().getTemplate(), cluster);

        return ActionReturnUtil.returnSuccessWithData("success");
    }


    @Override
    public ActionReturnUtil getUpdateStatus(String namespace, String name, String serviceType) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> jsonObject = new HashMap<>();
        if(StringUtils.isBlank(serviceType)){
            serviceType = Constant.DEPLOYMENT;
        }
        ServiceTypeEnum typeEnum = ServiceTypeEnum.valueOf(serviceType.toUpperCase());
        switch (typeEnum) {
            case DEPLOYMENT:
                jsonObject = this.getDeploymentUpdateStatus(name, namespace, cluster);
                break;
            case STATEFULSET:
                jsonObject = statefulSetVersionControlService.getUpdateStatus(name, namespace, cluster);
                break;
            default:
                break;
        }
        return ActionReturnUtil.returnSuccessWithData(jsonObject);
    }

    @Override
    public ActionReturnUtil canaryRollback(String namespace, String name, String revision, String podTemplate, String projectId) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        //根据namespace获取集群
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (null == cluster) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        //获取回滚版本的pvc
        PodTemplateSpec podTemplateSpec = JsonUtil.jsonToPojo(podTemplate, PodTemplateSpec.class);
        List<Volume> rollbackVolumes = new ArrayList<>();
        if (null != podTemplateSpec.getSpec() && null != podTemplateSpec.getSpec().getVolumes()) {
            rollbackVolumes = podTemplateSpec.getSpec().getVolumes();
        }

        //将PersistentVolumeClaim转成PersistentVolumeDto对象
        List<PersistentVolumeDto> pvDtoList = new ArrayList<>();
        rollbackVolumes.stream().forEach(rv -> {
            if (null != rv.getPersistentVolumeClaim()) {
                PersistentVolumeClaimVolumeSource pvc = rv.getPersistentVolumeClaim();
                PersistentVolumeDto pvDto = new PersistentVolumeDto();
                pvDto.setNamespace(namespace);
                pvDto.setReadOnly(pvc.isReadOnly());
                pvDto.setPvcName(pvc.getClaimName());
                pvDtoList.add(pvDto);
            }
        });

        //获取当前服务的pvc
        PersistentVolumeClaimList pvcList = getServicePvcList(name, namespace, cluster);
        if (Objects.nonNull(pvcList) && CollectionUtils.isNotEmpty(pvcList.getItems())) {
            if (CollectionUtils.isNotEmpty(pvDtoList)) {
                for (PersistentVolumeDto onePv : pvDtoList) {
                    for (PersistentVolumeClaim onePvc : pvcList.getItems()) {
                        boolean flag = true;
                        String pvc = onePvc.getMetadata().getName();
                        if (pvc.equals(onePv.getPvcName())) {
                            flag = false;
                        }
                        if (flag) {
                            ActionReturnUtil volumeRes = volumeSerivce.getPv(onePv.getPvcName(), cluster.getId());
                            if(!volumeRes.isSuccess()){
                                return volumeRes;
                            }
                            PvDto pvDto = (PvDto) volumeRes.getData();
                            onePv.setBindOne(pvDto.getIsBindOne());
                            onePv.setCapacity(pvDto.getCapacity());
                            handlePersistentVolumeClaimVolume(onePv, projectId, name, namespace);
                        }
                    }
                }
            }
            //比较两个pvc删除多的pvc
            for (PersistentVolumeClaim onePvc : pvcList.getItems()) {
                String pvc = onePvc.getMetadata().getName();
                boolean boo = true;
                if (CollectionUtils.isNotEmpty(pvDtoList)) {
                    for (PersistentVolumeDto onePv : pvDtoList) {
                        if (pvc.equals(onePv.getPvcName())) {
                            boo = false;
                        }
                    }
                }
                if (boo) {
                    pvcService.deletePVC(namespace, onePvc.getMetadata().getName(), cluster);
                    if (onePvc.getSpec() != null && onePvc.getSpec().getVolumeName() != null) {
                        // update pv
                        String pvName = onePvc.getSpec().getVolumeName();
                        PersistentVolume pv = pvService.getPvByName(pvName, cluster);
                        ActionReturnUtil pvRes = volumeSerivce.updatePV(pv, cluster);
                        if(!pvRes.isSuccess()){
                            return pvRes;
                        }
                    }
                }
            }
        }else {
            if (CollectionUtils.isNotEmpty(pvDtoList)) {
                for (PersistentVolumeDto onePv : pvDtoList) {
                    ActionReturnUtil volumeRes = volumeSerivce.getPv(onePv.getPvcName(), cluster.getId());
                    if(!volumeRes.isSuccess()){
                        return volumeRes;
                    }
                    PvDto pvDto = (PvDto) volumeRes.getData();
                    onePv.setBindOne(pvDto.getIsBindOne());
                    onePv.setCapacity(pvDto.getCapacity());
                    handlePersistentVolumeClaimVolume(onePv, projectId, name, namespace);
                }
            }
        }
        DeploymentRollback deploymentRollback = new DeploymentRollback();
        RollbackConfig rollbackConfig = new RollbackConfig();
        deploymentRollback.setName(name);
        //将参数设置成需要回滚的版本号
        rollbackConfig.setRevision(Integer.parseInt(revision));
        deploymentRollback.setRollbackTo(rollbackConfig);

        K8SClientResponse rollbackres = dpService.rollbackSpecifiedDeployment(deploymentRollback, namespace, name, cluster);

        if (!HttpStatusUtil.isSuccessStatus(rollbackres.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(rollbackres.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(dp.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
        if (dep != null && dep.getMetadata() != null && dep.getMetadata().getName() != null) {
            return updateServiceByDeployment(dep, cluster);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil resumeCanaryUpdate(String namespace, String name) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            logger.error("恢复灰度升级,获取Deployment出错");
            return ActionReturnUtil.returnErrorWithData(dp.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        dep.getSpec().setPaused(false);
        dep.getSpec().setMinReadySeconds(0);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        K8SClientResponse dpPut = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpPut.getStatus())) {
            logger.error("恢复灰度升级,更新Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dpPut.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }

        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
            logger.error("恢复灰度升级,获得Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dpUpdated.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(status.getMessage());
        }
        Deployment depUpdated = JsonUtil.jsonToPojo(dpUpdated.getBody(), Deployment.class);

        updateServiceByDeployment(depUpdated, cluster);

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil pauseCanaryUpdate(String namespace, String name) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            logger.error("暂停灰度升级,获取Deployment出错");
            UnversionedStatus status = JsonUtil.jsonToPojo(dp.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
        dep.getSpec().setPaused(true);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);

        K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
            logger.error("暂停灰度升级,更新Deployment出错", dpUpdated.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_PAUSE_CANARY_SCALE_FAILURE);
        }
        return ActionReturnUtil.returnSuccess();

    }

    private Map<String, Object> getRevisionDetail(String namespace, String name, String revision, Cluster cluster) throws Exception {

        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Map<String, Object> res = new HashedMap();
        //通过获得Deployment的RS来显示版本信息
        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> query = dep.getSpec().getSelector().getMatchLabels();

        Map<String, Object> body = new HashedMap();
        body.put("labelSelector", "app=" + query.get("app"));

        //根据label查询出来所有的RS
//        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, query, null, HTTPMethod.GET);
        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        for (ReplicaSet rs : rSetList.getItems()) {
            if (revision.equals(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                res.put("podTemplete", JSON.toJSONString(rs.getSpec().getTemplate()));
                res.put("revisionTime", rs.getMetadata().getCreationTimestamp());
                res.put("name", rs.getMetadata().getName());
                res.put("current", "false");

                if (revision.equals(dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                    res.put("current", "true");
                }
            }


        }
        return res;
    }


    private Set<String> listReversions(String namespace, String name, Cluster cluster) throws Exception {

        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> queryMap = dep.getSpec().getSelector().getMatchLabels();
        StringBuilder queryValue = new StringBuilder();
        int i = 0;
        //遍历拼装query
        for (String key : queryMap.keySet()) {
            queryValue.append(key + "=" + queryMap.get(key));
            if (i < queryMap.keySet().size() - 1) {
                queryValue.append(",");
            }
            i++;
        }

        Map<String, Object> query = new HashedMap();
        query.put("labelSelector", queryValue);


        //根据label查询出来所有的RS
        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, null, query, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return null;
        }
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        Set<String> res = new HashSet<>();

        for (ReplicaSet rs : rSetList.getItems()) {
            res.add(rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString());
        }

        return res;
    }

    @Override
    public ActionReturnUtil listRevisionAndDetails(String namespace, String name) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        Cluster cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
        if (Objects.isNull(cluster)) {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        //通过获得Deployment的RS来显示版本信息
        //查询出来Deployment
        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);

        Map<String, Object> query = dep.getSpec().getSelector().getMatchLabels();

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("labelSelector", "app=" + query.get("app"));

        K8SClientResponse rsRes = rsService.doRsByNamespace(namespace, headers, body, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }
        List<RollbackBean> reversions = new ArrayList<RollbackBean>();
        ReplicaSetList rSetList = JsonUtil.jsonToPojo(rsRes.getBody(), ReplicaSetList.class);

        for (ReplicaSet rs : rSetList.getItems()) {
            String reversion = rs.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision").toString();
            RollbackBean rollbackBean = new RollbackBean();
            rollbackBean.setName(rs.getMetadata().getName());
            rollbackBean.setRevisionTime(rs.getMetadata().getCreationTimestamp());
            rollbackBean.setPodTemplete(JSON.toJSONString(rs.getSpec().getTemplate()));
            if (rs.getSpec().getTemplate().getSpec().getVolumes() != null) {
                List<Volume> volume = rs.getSpec().getTemplate().getSpec().getVolumes();
                if (volume != null && volume.size() > 0) {
                    List<String> cfgmap = new ArrayList<String>();
                    for (Volume v : volume) {
                        if (v.getConfigMap() != null) {
                            cfgmap.add(v.getConfigMap().getName());
                        }
                    }
                    rollbackBean.setConfigmap(cfgmap.stream().distinct().collect(Collectors.toList()));
                }
            }
            rollbackBean.setCurrent("false");
            rollbackBean.setRevision(reversion);
            if (reversion.equals(dep.getMetadata().getAnnotations().get("deployment.kubernetes.io/revision"))) {
                rollbackBean.setCurrent("true");
            }

            reversions.add(rollbackBean);
        }

        reversions.sort((RollbackBean r1, RollbackBean r2) -> r2.getRevision().compareTo(r1.getRevision()));

        return ActionReturnUtil.returnSuccessWithData(reversions);
    }

    @Override
    public ActionReturnUtil cancelCanaryUpdate(String namespace, String name) throws Exception {
        if (StringUtils.isBlank(namespace) || StringUtils.isBlank(name)) {
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
            UnversionedStatus sta = JsonUtil.jsonToPojo(rollBackRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
        }
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        K8SClientResponse dp = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);

        if (!HttpStatusUtil.isSuccessStatus(dp.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(dp.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
        }
        Deployment dep = JsonUtil.jsonToPojo(dp.getBody(), Deployment.class);
        if (dep != null && dep.getMetadata() != null && dep.getMetadata().getName() != null) {
            dep.getSpec().setPaused(false);
            Map<String, Object> bodys = CollectionUtil.transBean2Map(dep);
            K8SClientResponse dpUpdate = dpService.doSpecifyDeployment(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(dpUpdate.getStatus())) {
                UnversionedStatus status = JsonUtil.jsonToPojo(dpUpdate.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }

            K8SClientResponse dpUpdated = dpService.doSpecifyDeployment(namespace, name, null, null, HTTPMethod.GET, cluster);
            if (!HttpStatusUtil.isSuccessStatus(dpUpdated.getStatus())) {
                logger.error("回滚升级,获得Deployment出错");
                UnversionedStatus status = JsonUtil.jsonToPojo(dpUpdated.getBody(), UnversionedStatus.class);
                return ActionReturnUtil.returnErrorWithData(status.getMessage());
            }
            Deployment depUpdated = JsonUtil.jsonToPojo(dpUpdated.getBody(), Deployment.class);
            return updateServiceByDeployment(depUpdated, cluster);
        }
        return ActionReturnUtil.returnSuccess();

    }

    @SuppressWarnings("unused")
    private void watchAppEvent(String name, String namespace, String kind, String rv, String userName, Cluster cluster)
            throws Exception {
        String token = String.valueOf(K8SClient.tokenMap.get(userName));
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

    private ActionReturnUtil updateServiceByDeployment(Deployment dep, Cluster cluster) throws Exception {
        K8SClientResponse rsRes = sService.doSepcifyService(dep.getMetadata().getNamespace(),
                dep.getMetadata().getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }

        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        List<ServicePort> ports = KubeServiceConvert.convertServicePort(dep.getSpec().getTemplate().getSpec().getContainers());
        if (service != null) {
            service.getSpec().setPorts(ports);
            Map<String, Object> bodys = CollectionUtil.transBean2Map(service);
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");

            K8SClientResponse sRes = sService.doSepcifyService(service.getMetadata().getNamespace(), service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
            if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(sRes.getBody());
            }
        } else {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            Map<String, Object> meta = new HashedMap();
            meta.put("name", dep.getMetadata().getName());
            Map<String, Object> labells = new HashedMap();
            labells.put("app", dep.getMetadata().getName());
            meta.put("labels", labells);
            Map<String, Object> spec = new HashedMap();
            spec.put("ports", ports);
            meta.put("spec", spec);

            K8SClientResponse sRes = sService.doSepcifyService(dep.getMetadata().getNamespace(), null, headers, meta, HTTPMethod.POST, cluster);
            if (!HttpStatusUtil.isSuccessStatus(sRes.getStatus())) {
                return ActionReturnUtil.returnErrorWithData(sRes.getBody());
            }
        }

        return ActionReturnUtil.returnSuccess();

    }


    private ActionReturnUtil increaseServiceByDeployment(ObjectMeta meta, PodTemplateSpec podTemplateSpec, Cluster cluster) throws Exception {

        Map<String, Object> query = new HashMap<String, Object>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");


        K8SClientResponse rsRes = sService.doSepcifyService(meta.getNamespace(),
                meta.getName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(rsRes.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(rsRes.getBody());
        }
        com.harmonycloud.k8s.bean.Service service = JsonUtil.jsonToPojo(rsRes.getBody(), com.harmonycloud.k8s.bean.Service.class);
        List<ServicePort> ports = new ArrayList();
        Map<Integer, Object> portsMapping = new HashedMap();


        if (service != null) {
            ports = service.getSpec().getPorts();
        } else {
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NOT_EXIST, DictEnum.SERVICE.phrase(), true);
        }

        for (ServicePort port : ports) {
            portsMapping.put(port.getTargetPort(), port);
        }
        ports = KubeServiceConvert.convertServicePort(podTemplateSpec.getSpec().getContainers());
        service.getSpec().setPorts(ports);

        Map<String, Object> bodys = CollectionUtil.transBean2Map(service);

        K8SClientResponse res = sService.doSepcifyService(service.getMetadata().getNamespace(), service.getMetadata().getName(), headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(res.getStatus())) {
            return ActionReturnUtil.returnErrorWithData(res.getBody());
        }

        return ActionReturnUtil.returnSuccessWithData(res);

    }

    private PersistentVolumeClaimList getServicePvcList(String name, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> label = new HashMap<String, Object>();
        label.put("labelSelector", "app=" + name);
        K8SClientResponse pvcRes = pvcService.doSepcifyPVC(namespace, label, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        PersistentVolumeClaimList pvcList = JsonUtil.jsonToPojo(pvcRes.getBody(), PersistentVolumeClaimList.class);
        return pvcList;
    }

    private void handlePersistentVolumeClaimVolume(PersistentVolumeDto pv, String projectId, String name, String namespace) throws Exception {
        pv.setVolumeName(pv.getPvcName());
        pv.setProjectId(projectId);
        pv.setServiceName(name);
        pv.setNamespace(namespace);
        volumeSerivce.createVolume(pv);
    }

    private Map<String, Object> getDeploymentUpdateStatus(String name, String namespace, Cluster cluster) throws Exception {
        // 获取deployment
        K8SClientResponse depRes = dpService.doSpecifyDeployment(namespace, name, null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            logger.error("获取灰度升级进程:获得进度出错", depRes.getBody());
            throw new MarsRuntimeException(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);

        //返回当前列表中新老实例,格式,顺序为:新实例,老实例,总共实例,当新的实例等于总的实例的时候终止前端定时器
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("maxSurge", CommonConstant.NUM_ONE);
        resultMap.put("maxUnavailable", 0);
        resultMap.put("instances", 0);
        List<Integer> counts = new ArrayList<>();
        //排除蓝绿
        if ("RollingUpdate".equals(dep.getSpec().getStrategy().getType())) {
            String maxSurge = String.valueOf(dep.getSpec().getStrategy().getRollingUpdate().getMaxSurge());
            if (!maxSurge.equals(Constant.ROLLINGUPDATE_MAX_UNAVAILABLE)) {
                Integer updateCounts = 0;
                if (dep.getStatus().getUpdatedReplicas() != null && dep.getStatus().getUpdatedReplicas() != 0) {
                    updateCounts = dep.getStatus().getUpdatedReplicas();
                    counts.add(updateCounts);
                    counts.add(dep.getSpec().getReplicas() - updateCounts);
                } else {
                    if (dep.getStatus().getUnavailableReplicas() != null) {
                        int unavailableReplicas = dep.getStatus().getUnavailableReplicas();
                        updateCounts = dep.getSpec().getReplicas() - unavailableReplicas;
                        if (updateCounts < 0) {
                            updateCounts = 0;
                        }
                        counts.add(updateCounts);
                        counts.add(unavailableReplicas);
                    }
                }
            }
            if (counts != null && counts.size() > 0 && counts.get(CommonConstant.NUM_ONE) > 0) {
                Map<String, Object> anno = dep.getMetadata().getAnnotations();
                if (Objects.nonNull(anno.get("deployment.canaryupdate/maxsurge"))) {
                    resultMap.put("maxSurge", Integer.valueOf(anno.get("deployment.canaryupdate/maxsurge").toString()));
                }
                if (Objects.nonNull(anno.get("deployment.canaryupdate/maxunavailable"))) {
                    resultMap.put("maxUnavailable", Integer.valueOf(anno.get("deployment.canaryupdate/maxunavailable").toString()));
                }
                if (Objects.nonNull(anno.get("deployment.canaryupdate/instances"))) {
                    resultMap.put("instances", Integer.valueOf(anno.get("deployment.canaryupdate/instances").toString()));
                }
            }
        } else {
            Integer updateCounts = 0;
            if (dep.getStatus().getUpdatedReplicas() != null && dep.getStatus().getUpdatedReplicas() != 0) {
                updateCounts = dep.getStatus().getUpdatedReplicas();
                counts.add(updateCounts);
                counts.add(0);
            } else {
                if (dep.getStatus().getUnavailableReplicas() != null) {
                    int unavailableReplicas = dep.getStatus().getUnavailableReplicas();
                    updateCounts = dep.getSpec().getReplicas() - unavailableReplicas;
                    if (updateCounts < 0) {
                        updateCounts = 0;
                    }
                    counts.add(updateCounts);
                    counts.add(0);
                }
            }
        }
        resultMap.put("pause", dep.getSpec().isPaused());
        resultMap.put("counts", counts);
        resultMap.put("message", dep.getStatus().getConditions());
        return resultMap;
    }
}