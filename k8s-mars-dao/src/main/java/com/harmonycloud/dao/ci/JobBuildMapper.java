package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.JobBuild;
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
}
