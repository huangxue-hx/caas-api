package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;

import java.util.List;

public interface DockerFileMapper {

    List<DockerFile> findByAll(DockerFile dockerFile);

    List<DockerFilePage> findPageByAll(DockerFile dockerFile);

    DockerFile selectDockerFile(DockerFile dockerFile);

    List<DockerFile> selectNameAndTenant(DockerFile dockerFile);

    void insertDockerFile(DockerFile dockerFile);

    void updateDockerFile(DockerFile dockerFile);

    void deleteDockerFile(DockerFile dockerFile);

}
