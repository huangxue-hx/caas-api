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
    List<DockerFile> findByAll(DockerFile dockerFile);

    /**
     * 根据租户和名称分页查询dockerfile
     * @param dockerFileDTO
     * @return
     */
    PageInfo<DockerFilePage> findByList(DockerFileDto dockerFileDTO);

    /**
     * 添加dockerfile
     * @param dockerFile
     */
    void insertDockerFile(DockerFile dockerFile);

    /**
     * 修改dockerfile
      * @param dockerFile
     */
    void updateDockerFile(DockerFile dockerFile);

    /**
     * 根据编号和租户删除dockerfile
     * @param dockerFile
     */
    void deleteDockerFile(DockerFile dockerFile);

    /**
     * 查询dockerfile
     * @param dockerFile
     * @return
     */
    DockerFile selectDockerFile(DockerFile dockerFile);

    /**
     * 根据名称和租户查询dockerfile
     * @param dockerFile
     * @return
     */
    List<DockerFile> selectNameAndTenant(DockerFile dockerFile);
}
