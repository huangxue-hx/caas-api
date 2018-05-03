package com.harmonycloud.dao.ci;

import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DockerFileMapper {

    List<DockerFile> findByAll(DockerFile dockerFile);

    List<DockerFilePage> findPageByAll(@Param("dockerFile")DockerFile dockerFile, @Param("clusterIdList")List clusterIdList);

    List<DockerFile> selectDockerFile(DockerFile dockerFile);

    List<DockerFile> selectNameAndTenant(DockerFile dockerFile);

    void insertDockerFile(DockerFile dockerFile);

    void updateDockerFile(DockerFile dockerFile);

    void deleteDockerFile(Integer id);

    DockerFile selectDockerFileById(Integer id);

    int deleteByClusterId(@Param("clusterId")String clusterId);

    void deleteByProjectId(String projectId);
}
