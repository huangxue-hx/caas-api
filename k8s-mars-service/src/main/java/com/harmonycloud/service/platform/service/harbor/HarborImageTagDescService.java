package com.harmonycloud.service.platform.service.harbor;

import com.harmonycloud.dao.harbor.bean.ImageTagDesc;

import java.util.List;

public interface HarborImageTagDescService {


    /**
     * 查询某个镜像某个版本的描述
     *
     * @param repositoryId 镜像仓库id
     * @param imageName    镜像名称
     * @param tagName      版本号
     * @return
     */
    ImageTagDesc select(Integer repositoryId, String imageName, String tagName);


    /**
     * 查询镜像版本描述列表
     *
     * @param repositoryId 镜像仓库id
     * @param imageName    镜像名称
     * @param tagName      版本号
     * @return
     */
    List<ImageTagDesc> selectList(Integer repositoryId, String imageName, String tagName);


    /**
     * 创建镜像标签描述
     *
     * @param desc model
     * @return
     */
    boolean create(ImageTagDesc desc) throws Exception;


    /**
     * 更新镜像标签描述
     *
     * @param desc model
     * @return
     */
    boolean update(ImageTagDesc desc) throws Exception;

}
