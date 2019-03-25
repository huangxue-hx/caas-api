package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DaemonSetDetailDto;
import com.harmonycloud.k8s.bean.DaemonSet;
import com.harmonycloud.k8s.bean.cluster.Cluster;

import java.util.List;

/**
 * @Author jiangmi
 * @Description 定义daemonset业务接口
 * @Date created in 2017-12-18
 * @Modified
 */
public interface DaemonSetsService {
    /**
     * 创建DaemonSet
     *
     * @param detail
     * @param username
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil createDaemonSet(DaemonSetDetailDto detail, String username) throws Exception;

    /**
     * update DaemonSet
     *
     * @param detail
     * @param username
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil updateDaemonSet(DaemonSetDetailDto detail, String username) throws Exception;

    /**
     * get a DaemonSet
     *
     * @param name
     * @param username
     * @param namespace
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil getDaemonSetDetail(String name, String namespace, String username, String clusterId) throws Exception;

    /**
     * 根据
     * @param labels
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listDaemonSets(String labels) throws Exception;
    /**
     * 根据集群获取daemonset
     * @return
     * @throws Exception
     */
    public List<DaemonSet> listDaemonSets(Cluster cluster) throws Exception;

    /**
     * get DaemonSet list by namespaces
     *
     * @param name
     * @param username
     * @param namespaces
     * @return ActionReturnUtil
     * @throws Exception
     */
    public ActionReturnUtil deleteDaemonSet(String name, String namespaces, String username, String clusterId) throws Exception;

    /**
     * 获取podList
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * */
    public ActionReturnUtil listPods(String name, String namespace, String clusterId)throws Exception;

    /**
     * 获取EventList
     * @param name
     * @param namespace
     * @return ActionReturnUtil
     * */
    public ActionReturnUtil listEvents(String name, String namespace, String clusterId)throws Exception;

    /**
     * daemonset状态转换
     * @param daemonSet
     * @return
     */
    public String convertDaemonStatus(DaemonSet daemonSet);
}
