package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.dao.ci.bean.JobBuild;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-2-2
 * @Modified
 */
public interface JobBuildService {
    void insert(JobBuild jobBuild);

    void update(JobBuild jobBuild);

    String queryLogByObject(JobBuild jobBuild);

    List<JobBuild> queryByObject(JobBuild jobBuild);

    Integer queryLastBuildNumById(Integer jobId);

    void deleteByJobId(Integer jobId);

    JobBuild queryLastBuildById(Integer jobId);

    void updateLogById(JobBuild jobBuild);

    JobBuild queryFirstBuildById(Integer jobId);

    void deleteByJobIdAndBuildNum(Integer id, List buildNumList);
}
