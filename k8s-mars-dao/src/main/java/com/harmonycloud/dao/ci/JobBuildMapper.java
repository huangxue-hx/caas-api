package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.JobBuild;
import com.harmonycloud.dao.ci.bean.JobWithBuild;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by anson on 17/7/17.
 */
public interface JobBuildMapper {
    List<JobBuild> queryByObject(JobBuild jobBuild);

    int countByObject(JobBuild jobBuild);

    List<JobBuild> queryByObjectWithPagination(@Param("jobBuild")JobBuild jobBuild, @Param("offset")Integer offset, @Param("rows")Integer rows);

    void insert(JobBuild jobBuild);

    void update(JobBuild jobBuild);

    void deleteByJobId(Integer id);

    String queryLogByObject(JobBuild jobBuild);

    Integer queryLastBuildNumById(Integer jobId);

    JobBuild queryLastBuildById(Integer jobId);

    void updateLogById(JobBuild jobBuild);

    JobBuild queryFirstBuildById(Integer jobId);

    void deleteByJobIdAndBuildNum(@Param("jobId")Integer jobId, @Param("buildNumList")List buildNumList);
}
