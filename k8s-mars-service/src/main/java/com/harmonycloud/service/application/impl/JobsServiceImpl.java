package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.JobsDetailDto;
import com.harmonycloud.service.application.JobsService;

/**
 * Created by root on 7/9/17.
 */
public class JobsServiceImpl implements JobsService{
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
        return null;
    }

    @Override
    public ActionReturnUtil reRunJob(String name, String namespace, String userName, Cluster cluster) throws Exception {
        return null;
    }
}
