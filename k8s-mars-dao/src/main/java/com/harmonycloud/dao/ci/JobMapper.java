package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.JobWithBuild;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by anson on 17/6/9.
 */
public interface JobMapper {
    List<Job> select(@Param("projectId") String projectId, @Param("clusterId") String clusterId, @Param("jobName") String jobName, @Param("createUser") String createUser, @Param("type") String type);

    Job queryById(Integer id);

    void insertJob(Job job);

    void deleteJobByTenantAndJobName(@Param("tenant") String tenant, @Param("jobName") String jobName);

    void updateJob(Job job);

    void deleteJobById(Integer id);

    void updateNotification(Job job);

    void updateTrigger(Job job);

    void updateLastBuildNum(@Param("id") Integer id,  @Param("buildNum") Integer buildNum);

    Job queryByUuid(String uuid);

    int deleteByClusterId(@Param("clusterId")String clusterId);

    void updateJobName(@Param("id")Integer id, @Param("name")String name);

    List<JobWithBuild> selectJobWithLastBuild(@Param("projectId") String projectId, @Param("clusterId") String clusterId, @Param("jobName") String jobName, @Param("type") String type);

    JobWithBuild selectJobWithLastBuildById(Integer jobId);
}
