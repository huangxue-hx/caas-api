package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.IngressDto;
import com.harmonycloud.dto.application.StatefulSetDetailDto;
import com.harmonycloud.k8s.bean.StatefulSetList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.AppDetail;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.EventDetail;
import com.harmonycloud.service.platform.bean.PodDetail;

import java.util.List;
import java.util.Map;

/**
 * Created by anson on 18/8/7.
 */
public interface StatefulSetsService {

    AppDetail getStatefulSetDetail(String namespace, String name, String projectId) throws Exception;

    List<Map<String, Object>> listStatefulSets(String tenantId, String name, String namespace, String labels, String projectId, String clusterId) throws Exception;

    ActionReturnUtil createStatefulSet(StatefulSetDetailDto detail, String userName, String app, Cluster cluster, List<IngressDto> ingress) throws Exception;

    void startStatefulSet(String name, String namespace, String userName) throws Exception;

    void stopStatefulSet(String name, String namespace, String userName) throws Exception;

    ActionReturnUtil deleteStatefulSet(String name, String namespace, String userName, Cluster cluster) throws Exception;

    void scaleStatefulSet(String namespace, String name, Integer scale, String userName) throws Exception;

    List<ContainerOfPodDetail> statefulSetContainer(String namespace, String name) throws Exception;

    List<EventDetail> getStatefulSetEvents(String namespace, String name) throws Exception;

    List<PodDetail> podList(String name, String namespace, boolean isFilterTerminated) throws Exception;

    ActionReturnUtil deleteStatfulServiceByprojectId(String projectId, String tenantId) throws Exception;

    /**
     * 更新labels。
     * 可同时操作多个label，通过Entry的Value值是否为null来判断具体动作为添加/更新还是删除。
     * @param namespace
     * @param name
     * @param cluster
     * @param label 若Entry的Key与Value均不为null,则添加或更新label；若Entry的Key不为null、Value为null则删除此Key对应的label
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil updateLabels(String namespace, String name, Cluster cluster, Map<String, Object> label) throws Exception;

    /**
     * 查询某分区项目下的有状态服务
     * @param namespace
     * @param projectId
     * @return
     */
    StatefulSetList listStatefulSets(String namespace, String projectId) throws Exception;

    /**
     * 查询某分区项目下的有状态服务
     * @param namespace
     * @param projectId
     * @return
     */
    StatefulSetList listStatefulSets(String namespace, String projectId, Cluster cluster) throws Exception;
}
