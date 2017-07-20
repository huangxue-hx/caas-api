package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.JobBuild;

import java.util.List;

/**
 * Created by anson on 17/7/17.
 */
public interface JobBuildMapper {
    List<JobBuild> queryByObject(JobBuild jobBuild);

    void insert(JobBuild jobBuild);

    void update(JobBuild jobBuild);
}
