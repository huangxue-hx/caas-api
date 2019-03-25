package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.RollbackBean;
import com.harmonycloud.k8s.bean.StatefulSet;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.platform.bean.CanaryDeployment;

import java.util.List;
import java.util.Map;

/**
 * Created by jmi on 18-7-3.
 */
public interface StatefulSetVersionControlService {

    /**
     * 灰度升级
     * @param detail
     * @param instances
     * @param cluster
     * @return
     * @throws Exception
     */
    StatefulSet canaryUpdateForStatefulSet(CanaryDeployment detail, int instances, Cluster cluster) throws Exception;

    /**
     * 获取灰度升级的状态
     * @param name
     * @param namespace
     * @param cluster
     * @return
     * @throws Exception
     */
    Map<String, Object> getUpdateStatus(String name, String namespace, Cluster cluster) throws Exception;


    /**
     * statefulset的版本回滚
     * @param name
     * @param revision
     * @param namespace
     * @param cluster
     * @return
     * @throws Exception
     */
    ActionReturnUtil rollbackStatefulSet(String name, String revision, String namespace, Cluster cluster) throws Exception;

    /**
     * 获取statefulset的版本列表
     * @param name
     * @param namespace
     * @param cluster
     * @return
     * @throws Exception
     */
    List<RollbackBean> listStatefulSetfulRevisionAndDetail(String name, String namespace, Cluster cluster) throws Exception;

    /**
     * 取消灰度升级（回滚到旧版本）
     * @param namespace
     * @param name
     * @param cluster
     * @return
     * @throws Exception
     */
    ActionReturnUtil cancelCanaryUpdateForStatefulSet(String namespace, String name, Cluster cluster) throws Exception;

    /**
     * 确认升级到新版本
     * @param namespace
     * @param name
     * @param cluster
     * @return
     * @throws Exception
     */
    StatefulSet resumeCanaryUpdateForStatefulSet(String namespace, String name, Cluster cluster) throws Exception;

}
