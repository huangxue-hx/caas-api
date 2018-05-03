package com.harmonycloud.service.platform.service.ci;

import com.github.pagehelper.PageInfo;
import com.harmonycloud.dao.ci.bean.DockerFile;
import com.harmonycloud.dao.ci.bean.DockerFilePage;
import com.harmonycloud.dto.cicd.DockerFileDto;

import java.util.List;

public interface DockerFileService {

    /**
     * 根据租户查询所有的dockerfile
     * @return
     */
    List<DockerFile> findByAll(DockerFile dockerFile) throws Exception;

    /**
     * 根据项目id和集群id分页查询dockerfile
     * @param dockerFileDTO
     * @return
     */
    PageInfo<DockerFilePage> findByList(DockerFileDto dockerFileDTO) throws Exception;

    /**
     * 添加dockerfile
     * @param dockerFile
     */
    void insertDockerFile(DockerFile dockerFile) throws Exception;

    /**
     * 修改dockerfile
      * @param dockerFile
     */
    void updateDockerFile(DockerFile dockerFile) throws Exception;

    /**
     * 根据id删除dockerfile
     * @param id
     */
    void deleteDockerFile(Integer id) throws Exception;

    /**
     * 查询dockerfile
     * @param dockerFile
     * @return
     */
    List<DockerFile> selectDockerFile(DockerFile dockerFile);

    /**
     * 根据名称和租户查询dockerfile
     * @param dockerFile
     * @return
     */
    List<DockerFile> selectNameAndTenant(DockerFile dockerFile);

    /**
     * 根据id查询dockerfile
     * @param id
     * @return
     */
    DockerFile selectDockerFileById(Integer id);

    int deleteByClusterId(String clusterId);

    void deleteDockerfileByProject(String projectId);
}
