package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.dao.ci.JobBuildMapper;
import com.harmonycloud.dao.ci.bean.JobBuild;
import com.harmonycloud.service.platform.service.ci.JobBuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-2-2
 * @Modified
 */
@Service
public class JobBuildServiceImpl implements JobBuildService{

    @Autowired
    private JobBuildMapper jobBuildMapper;

    @Override
    public void insert(JobBuild jobBuild) {
        jobBuildMapper.insert(jobBuild);
    }

    @Override
    public void update(JobBuild jobBuild) {
        jobBuildMapper.update(jobBuild);
    }

    @Override
    public String queryLogByObject(JobBuild jobBuild) {
        return jobBuildMapper.queryLogByObject(jobBuild);
    }

    @Override
    public List<JobBuild> queryByObject(JobBuild jobBuild){
        return jobBuildMapper.queryByObject(jobBuild);
    }

    @Override
    public Integer queryLastBuildNumById(Integer jobId){
        return jobBuildMapper.queryLastBuildNumById(jobId);
    }

    @Override
    public void deleteByJobId(Integer jobId) {
        jobBuildMapper.deleteByJobId(jobId);
    }

    @Override
    public JobBuild queryLastBuildById(Integer jobId){
        return jobBuildMapper.queryLastBuildById(jobId);
    }

    @Override
    public void updateLogById(JobBuild jobBuild) {
        jobBuildMapper.updateLogById(jobBuild);
    }

    @Override
    public JobBuild queryFirstBuildById(Integer jobId) {
        return jobBuildMapper.queryFirstBuildById(jobId);
    }

    @Override
    public void deleteByJobIdAndBuildNum(Integer id, List buildNumList) {
        jobBuildMapper.deleteByJobIdAndBuildNum(id, buildNumList);
    }


}
