package com.harmonycloud.service.application;

import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dto.application.CreateConfigMapDto;
import com.harmonycloud.dto.application.JobsDetailDto;

/**
 * Created by root on 7/9/17.
 */
public interface JobsService {

    /**
     * 创建job
     * @param detail
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createJob(JobsDetailDto detail, String userName) throws Exception;

    /**
     * 获取当前namespace的job（name参数目前没有用）
     * 可对label进行搜索
     * @param name
     * @param namespace
     * @param labels
     * @return
     * @throws Exception
     */
    public ActionReturnUtil listJob(String projectId, String namespace, String labels, String status, String clusterId) throws Exception;

    /**
     * 获取job详情
     * @param namespace
     * @param name
     * @return
     * @throws Exception
     */
    public ActionReturnUtil getJobDetail(String namespace, String name) throws Exception;


    /**
     * 启动job（需要进行消息推送 watch）
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil startJob(String name, String namespace, String userName) throws Exception;

    /**
     * 停止job（需要进行消息推送 watch）
     * @param name
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil stopJob(String name, String namespace, String userName) throws Exception;

    /**
     * 更新job
     * @param detail
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName) throws Exception;

    /**
     * 删除job
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil deleteJob(String name, String namespace, String userName) throws Exception;


    /**
     * re run job
     * @param name
     * @param namespace
     * @param userName
     * @return
     * @throws Exception
     */
    public ActionReturnUtil reRunJob(String name, String namespace, String userName) throws Exception;
    
    /**
     * create Configmap
     * @param name
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil createConfigMap(List<CreateConfigMapDto> configMaps, String namespace, String containerName, String name, Cluster cluster, String type, String appName) throws Exception;
    
    /**
     * 更新job 
     * @param name
     * @param namespace
     * @return
     * @throws Exception
     */
    public ActionReturnUtil updateJobParallelism(String name, String namespace, int parallelism) throws Exception;

}
