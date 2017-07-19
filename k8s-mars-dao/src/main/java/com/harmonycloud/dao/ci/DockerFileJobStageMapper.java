package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.DockerFileJobStage;

public interface DockerFileJobStageMapper {

    void insertDockerFileJobStage(DockerFileJobStage dockerFileJobStage);

    void deleteDockerFileByJobId(Integer jobId);

    void deleteDockerFileByStageId(Integer stageId);
}
