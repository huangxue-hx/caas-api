package com.harmonycloud.service.platform.service.ci;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cicd.DependenceDto;
import com.harmonycloud.dto.cicd.DependenceFileDto;

import java.util.List;
import java.util.Map;

/**
 * @Author w_kyzhang
 * @Description 依赖管理方法接口
 * @Date 2017-7-29
 * @Modified
 */
public interface DependenceService {
    /**
     *根据项目id和集群id及名称查询依赖列表
     * @param projectId
     * @param clusterId
     * @param name
     * @return
     * @throws Exception
     */
    List<Map> listByProjectIdAndClusterId(String projectId, String clusterId, String name) throws Exception;

    /**
     * 新增依赖
     * @param dependenceDto
     * @throws Exception
     */
    void add(DependenceDto dependenceDto) throws Exception;

    /**
     * 删除依赖
     * @param name 依赖名
     * @param projectId 项目id
     * @param clusterId 集群id
     * @throws Exception
     */
    void delete(String name, String projectId, String clusterId) throws Exception;

    /**
     * 上传文件至依赖目录
     * @param dependenceFileDto
     * @throws Exception
     */
    void uploadFile(DependenceFileDto dependenceFileDto) throws Exception;

    /**
     * 返回依赖目录下的文件
     *
     * @param dependenceName 依赖名
     * @param projectId      项目id
     * @param clusterId      集群id
     * @param path           路径
     * @return
     * @throws Exception
     */
    List listFile(String dependenceName, String projectId, String clusterId, String path) throws Exception;

    /**
     * 删除依赖目录中的文件
     *
     * @param dependenceName 依赖名
     * @param projectId      项目id
     * @param clusterId      集群id
     * @param path           路径
     * @throws Exception
     */
    void deleteFile(String dependenceName, String projectId, String clusterId, String path) throws Exception;

    /**
     * 删除项目下的依赖
     * @param projectId 项目id
     */
    void deleteDependenceByProject(String projectId);
}
