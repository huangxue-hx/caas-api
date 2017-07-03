package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.Job;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by anson on 17/6/9.
 */
public interface JobMapper {
    List<Job> select(@Param("tenant")String tenant, @Param("jobName")String jobName, @Param("createUser")String createUser);

    void insertJob(Job job);

    void deleteJobByTenantAndJobName(@Param("tenant")String tenant, @Param("jobName")String jobName);

    void updateJob(Job job);
}
