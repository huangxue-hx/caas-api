package com.harmonycloud.service.application;

import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateConfigMapDto;
import com.harmonycloud.dto.business.JobsDetailDto;

/**
 * Created by root on 7/9/17.
 */
public interface JobsService {

    /**
     * 创建deployment
     * @param detail
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception;

    /**
     * 获取当前namespace的job（name参数目前没有用）
     * 可对label进行搜索
     * @param name
     * @param namespace
     * @param labels
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listJob(String tenantId, String name, String namespace, String labels, String status) throws Exception;

    /**
     * 获取job详情
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getJobDetail(String namespace, String name, Cluster cluster) throws Exception;


    /**
     * 启动job（需要进行消息推送 watch）
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil startJob(String name, String namespace, String userName, Cluster cluster) throws Exception;

    /**
     * 停止job（需要进行消息推送 watch）
     * @param name
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil stopJob(String name, String namespace, String userName, Cluster cluster) throws Exception;

    /**
     * 更新job
     * @param detail
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception;

    /**
     * 删除job
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil deleteJob(String name, String namespace, String userName, Cluster cluster) throws Exception;


    /**
     * re run job
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil reRunJob(String name, String namespace, String userName, Cluster cluster) throws Exception;
    
    /**
     * create Configmap
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createConfigMap(List<CreateConfigMapDto> configMaps, String namespace, String containerName, String name, Cluster cluster, String type, String businessName) throws Exception;
}
