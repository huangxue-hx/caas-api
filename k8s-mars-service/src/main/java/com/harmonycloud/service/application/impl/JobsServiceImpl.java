package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.JobsDetailDto;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.UnversionedStatus;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.service.JobService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.JobsService;
import com.harmonycloud.service.platform.constant.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 7/9/17.
 */
@Service
public class JobsServiceImpl implements JobsService{

    @Autowired
    JobService jobService;


    @Override
    public ActionReturnUtil createJob(JobsDetailDto detail, String userName, String business, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil listJob(String tenantId, String name, String namespace, String labels, String status) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil getJobDetail(String namespace, String name, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil startJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil stopJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil replaceJob(JobsDetailDto detail, String userName, Cluster cluster) throws Exception {
        return null;
    }

    @Override
    public ActionReturnUtil deleteJob(String name, String namespace, String userName, Cluster cluster) throws Exception {

        //delete job
        ActionReturnUtil deleteJob = jobService.delJobByName(name,namespace,cluster);
        if (!deleteJob.isSuccess()){
            return deleteJob;
        }

        //delete configmap
        K8SURL cUrl = new K8SURL();
        cUrl.setNamespace(namespace).setResource(Resource.CONFIGMAP);
        Map<String, Object> queryP = new HashMap<>();
        queryP.put("labelSelector", "jobs=" + name);
        cUrl.setQueryParams(queryP);
        K8SClientResponse conRes = new K8SClient().doit(cUrl, HTTPMethod.DELETE, null, null,null);
        if (!HttpStatusUtil.isSuccessStatus(conRes.getStatus()) && conRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(conRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        //delete pvc
        K8SURL pvcUrl = new K8SURL();
        pvcUrl.setNamespace(namespace).setResource(Resource.PERSISTENTVOLUMECLAIM);
        Map<String, Object> queryPvc = new HashMap<>();
        queryPvc.put("labelSelector", "jobs=" + name);
        pvcUrl.setQueryParams(queryPvc);
        K8SClientResponse pvcRes = new K8SClient().doit(pvcUrl, HTTPMethod.DELETE, null, null,null);
        if (!HttpStatusUtil.isSuccessStatus(pvcRes.getStatus()) && pvcRes.getStatus() != Constant.HTTP_404) {
            UnversionedStatus status = JsonUtil.jsonToPojo(pvcRes.getBody(), UnversionedStatus.class);
            return ActionReturnUtil.returnErrorWithMsg(status.getMessage());
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil reRunJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }
}
