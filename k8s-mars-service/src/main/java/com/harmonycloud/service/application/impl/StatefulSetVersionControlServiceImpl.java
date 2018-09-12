package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.cluster.bean.RollbackBean;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.StatefulSetService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.StatefulSetVersionControlService;
import com.harmonycloud.service.platform.bean.CanaryDeployment;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.KubeServiceConvert;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Created by jmi on 18-7-3.
 */
@Service
public class StatefulSetVersionControlServiceImpl implements StatefulSetVersionControlService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StatefulSetService statefulSetService;

    @Autowired
    private DeploymentsService deploymentsService;

    @Override
    public StatefulSet canaryUpdateForStatefulSet(CanaryDeployment detail, int instances, Cluster cluster) throws Exception {
        // 获取statefulSet
        K8SClientResponse statefulSetRes = statefulSetService.doSpecifyStatefulSet(detail.getNamespace(), detail.getName(), null, null,
                HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(statefulSetRes.getStatus())) {
            UnversionedStatus status = JsonUtil.jsonToPojo(statefulSetRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        StatefulSet statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);

        //在这里创建configmap,在convertAppPut当中增加Deployment的注解,返回的是容器和configmap之间的映射关系列表
        Map<String, String> containerToConfigMap = deploymentsService.createConfigMapInUpdate(detail.getNamespace(), detail.getName(), cluster, detail.getContainers());

        //更新旧的statefulSet对象
        PodTemplateSpec podTemplateSpec = KubeServiceConvert.convertDeploymentUpdate(statefulSet.getSpec().getTemplate(), detail.getContainers(), detail.getName(), containerToConfigMap, cluster);
        statefulSet.getSpec().setTemplate(podTemplateSpec);
        //修改时间
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String updateTime = sdf.format(now);
        Map<String, Object> anno = statefulSet.getMetadata().getAnnotations();
        anno.put("updateTimestamp", updateTime);
        //将页面上填写的数据保存到annotation中
        int partition = statefulSet.getSpec().getReplicas()-detail.getInstances();
        anno.put("statefulset.canaryupdate/partition", String.valueOf(partition));
        statefulSet.getMetadata().setAnnotations(anno);
        Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        //设置灰度升级相关的参数
        StatefulSetUpdateStrategy statefulSetUpdateStrategy = new StatefulSetUpdateStrategy();
        statefulSetUpdateStrategy.setType("RollingUpdate");
        statefulSet.getSpec().setUpdateStrategy(statefulSetUpdateStrategy);
        //当实例数目不为0时才需要灰度更新
        if (detail.getInstances() == 0) {
            logger.error("实例数为0不需要灰度更新");
            throw new MarsRuntimeException("实例数为0不需要灰度更新");
        }
        RollingUpdateStatefulSetStrategy rollingUpdateStatefulSetStrategy = new RollingUpdateStatefulSetStrategy();
        rollingUpdateStatefulSetStrategy.setPartition(partition);
        statefulSet.getSpec().getUpdateStrategy().setRollingUpdate(rollingUpdateStatefulSetStrategy);
        //开始更新
        K8SClientResponse statefulSetUpdateRes = statefulSetService.doSpecifyStatefulSet(detail.getNamespace(), detail.getName(), headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(statefulSetUpdateRes.getStatus())) {
            logger.error("灰度升级statefulSet报错");
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_CANARY_SCALE_FAILURE);
        }
        CountDownLatch countDownLatch = new CountDownLatch(CommonConstant.NUM_ONE);
        ThreadPoolExecutorFactory.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (; ; ) {
                        //暂停两秒钟等待灰度升级开始
                        Thread.sleep(CommonConstant.NUM_THOUSAND);
                        //判断是否升级完成
                        StatefulSet updateStatefulSet = statefulSetService.getStatefulSet(detail.getNamespace(), detail.getName(), cluster);
                        if (updateStatefulSet.getStatus().getUpdatedReplicas() != null && updateStatefulSet.getStatus().getUpdatedReplicas() != 0) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error("灰度升级失败", e);
                } finally {
                    countDownLatch.countDown();
                }

            }
        });
        countDownLatch.await();
        return statefulSet;
    }

    @Override
    public Map<String, Object> getUpdateStatus(String name, String namespace, Cluster cluster) throws Exception {
        //获取statefulset
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        Integer replicas = statefulSet.getStatus().getReplicas();
        Integer currentReplicas = statefulSet.getStatus().getCurrentReplicas();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("instances", 0);
        Map<String, Object> annotation = statefulSet.getMetadata().getAnnotations();
        if (Objects.nonNull(annotation.get("statefulset.canaryupdate/partition"))) {
            resultMap.put("instances", Integer.valueOf(annotation.get("statefulset.canaryupdate/partition").toString()));
        }
        List<Integer> counts = new ArrayList<>();
        Integer updateCounts = 0;
        resultMap.put("pause", CommonConstant.FALSE);
        if (statefulSet.getStatus().getUpdatedReplicas() != null && statefulSet.getStatus().getUpdatedReplicas() != 0) {
            updateCounts = statefulSet.getStatus().getUpdatedReplicas();
            counts.add(currentReplicas);
            counts.add(updateCounts);
            resultMap.put("pause", CommonConstant.TRUE);
        } else {
            counts.add(replicas);
            counts.add(0);
            resultMap.put("pause", CommonConstant.FALSE);
        }
        resultMap.put("counts", counts);
        resultMap.put("message", statefulSet.getStatus().getConditions());
        return resultMap;
    }

    @Override
    public ActionReturnUtil rollbackStatefulSet(String name, String revision, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", Constant.TYPE_STATEFULSET + CommonConstant.EQUALITY_SIGN + name);
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONTROLLERREVISION);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            logger.debug("StatefulSet版本获取失败，{}", response.getBody());
            throw new MarsRuntimeException();
        }
        ControllerRevisionList controllerRevisionList = JsonUtil.jsonToPojo(response.getBody(), ControllerRevisionList.class);
        List<ControllerRevision> crList = controllerRevisionList.getItems();
        List<ControllerRevision> revisionList = crList.stream().filter(cr -> revision.equals(String.valueOf(cr.getRevision())))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(revisionList)) {
            ControllerRevision cr = revisionList.get(0);
            Object data = cr.getData();
            Map<String, Object> templateMap = (Map) ((Map) data).get("spec");
            String podTemplate = JsonUtil.convertToJson(templateMap);
            StatefulSetSpec statefulSetSpec = JsonUtil.jsonToPojo(podTemplate, StatefulSetSpec.class);
            PodTemplateSpec podTemplateSpec = statefulSetSpec.getTemplate();
            //更新正在运行的StatefulSet
            StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
            statefulSet.getSpec().setTemplate(podTemplateSpec);
            return statefulSetService.updateStatefulSet(namespace, name, statefulSet, cluster);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public List<RollbackBean> listStatefulSetfulRevisionAndDetail(String name, String namespace, Cluster cluster) throws Exception {
        Map<String, Object> bodys = new HashMap<String, Object>();
        bodys.put("labelSelector", Constant.TYPE_STATEFULSET + CommonConstant.EQUALITY_SIGN + name);
        //使用ControllerRevision资源对象查询版本
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setResource(Resource.CONTROLLERREVISION);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            logger.debug("StatefulSet版本获取失败，{}", response.getBody());
            throw new MarsRuntimeException();
        }
        ControllerRevisionList controllerRevisionList = JsonUtil.jsonToPojo(response.getBody(), ControllerRevisionList.class);
        List<ControllerRevision> crList = controllerRevisionList.getItems();
        //获取当前运行在statefulset版本
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        List<RollbackBean> reversions = new ArrayList<>();
        crList.stream().sorted((ControllerRevision cr1, ControllerRevision cr2) ->
                cr2.getRevision().compareTo(cr1.getRevision())
        ).forEach(cr -> {
            RollbackBean rollbackBean = new RollbackBean();
            String crName = cr.getMetadata().getName();
            rollbackBean.setName(crName);
            rollbackBean.setRevision(String.valueOf(cr.getRevision()));
            rollbackBean.setRevisionTime(cr.getMetadata().getCreationTimestamp());
            Object data = cr.getData();
            Map<String, Object> templateMap = (Map) ((Map) data).get("spec");
            String podTemplate = JsonUtil.convertToJson(templateMap);
            rollbackBean.setPodTemplete(podTemplate);
            StatefulSetSpec statefulSetSpec = JsonUtil.jsonToPojo(podTemplate, StatefulSetSpec.class);
            PodSpec podSpec = statefulSetSpec.getTemplate().getSpec();
            if (CollectionUtils.isNotEmpty(podSpec.getVolumes())) {
                List<Volume> volume = podSpec.getVolumes();
                if (volume != null && volume.size() > 0) {
                    List<String> cfgmap = new ArrayList<>();
                    for (Volume v : volume) {
                        if (v.getConfigMap() != null) {
                            cfgmap.add(v.getConfigMap().getName());
                        }
                    }
                    rollbackBean.setConfigmap(cfgmap.stream().distinct().collect(Collectors.toList()));
                }
            }
            rollbackBean.setCurrent(crName.equals(statefulSet.getStatus().getCurrentRevision()) ?
                    CommonConstant.TRUE_STRING : CommonConstant.FALSE_STRING);
            reversions.add(rollbackBean);
        });
        return reversions;
    }

    @Override
    public ActionReturnUtil cancelCanaryUpdateForStatefulSet(String namespace, String name, Cluster cluster) throws Exception {
        K8SClientResponse statefulSetRes = statefulSetService.doSpecifyStatefulSet(namespace, name, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(statefulSetRes.getStatus())) {
            UnversionedStatus sta = JsonUtil.jsonToPojo(statefulSetRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithData(sta.getMessage());
        }
        StatefulSet statefulSet = JsonUtil.jsonToPojo(statefulSetRes.getBody(), StatefulSet.class);
        String currentRevision = statefulSet.getStatus().getCurrentRevision();

        Map<String, Object> bodys = new HashMap<>();
        bodys.put("labelSelector", Constant.TYPE_STATEFULSET + CommonConstant.EQUALITY_SIGN + name);
        K8SURL url = new K8SURL();
        url.setNamespace(namespace).setName(currentRevision).setResource(Resource.CONTROLLERREVISION);
        K8SClientResponse response = new K8sMachineClient().exec(url, HTTPMethod.GET, null, bodys, cluster);
        if (!HttpStatusUtil.isSuccessStatus(response.getStatus())) {
            logger.debug("StatefulSet版本获取失败，{}", response.getBody());
            throw new MarsRuntimeException();
        }
        ControllerRevision controllerRevision = JsonUtil.jsonToPojo(response.getBody(), ControllerRevision.class);

        Object data = controllerRevision.getData();
        Map<String, Object> templateMap = (Map) ((Map) data).get("spec");
        String podTemplate = JsonUtil.convertToJson(templateMap);
        StatefulSetSpec statefulSetSpec = JsonUtil.jsonToPojo(podTemplate, StatefulSetSpec.class);
        PodTemplateSpec podTemplateSpec = statefulSetSpec.getTemplate();
        statefulSet.getSpec().setTemplate(podTemplateSpec);
        statefulSet.getSpec().getUpdateStrategy().getRollingUpdate().setPartition(0);
        return statefulSetService.updateStatefulSet(namespace, name, statefulSet, cluster);
    }

    @Override
    public StatefulSet resumeCanaryUpdateForStatefulSet(String namespace, String name, Cluster cluster) throws Exception {
        StatefulSet statefulSet = statefulSetService.getStatefulSet(namespace, name, cluster);
        statefulSet.getSpec().getUpdateStrategy().getRollingUpdate().setPartition(0);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, Object> bodys = CollectionUtil.transBean2Map(statefulSet);
        K8SClientResponse statefulSetUpdatedRes = statefulSetService.doSpecifyStatefulSet(namespace, name, headers, bodys, HTTPMethod.PUT, cluster);
        if (!HttpStatusUtil.isSuccessStatus(statefulSetUpdatedRes.getStatus())) {
            logger.error("确认灰度升级失败, {}", statefulSetUpdatedRes.getBody());
            UnversionedStatus status = JsonUtil.jsonToPojo(statefulSetUpdatedRes.getBody(), UnversionedStatus.class);
            throw new MarsRuntimeException(status.getMessage());
        }
        StatefulSet statefulSetUpdated = JsonUtil.jsonToPojo(statefulSetUpdatedRes.getBody(), StatefulSet.class);
        return statefulSetUpdated;
    }
}
