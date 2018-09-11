package com.harmonycloud.dao.harbor;

import com.harmonycloud.dao.harbor.bean.ImageRepository;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ImageRepositoryMapper {

    int isDefault(ImageRepository imageRepository);

    int insert(ImageRepository imageRepository);

    int update(ImageRepository imageRepository);

    List<ImageRepository> listRepositories(ImageRepository imageRepository);
    void deleteRepositories(ImageRepository imageRepository);
    List<ImageRepository> selectRepositories(@Param("projectId") String projectId, @Param("harborHosts") Set<String> harborHosts,
                                             @Param("clusterIds") Set<String> clusterIds, @Param("isPublic") Boolean isPublic,
                                             @Param("isNormal") Boolean isNormal);
    ImageRepository findRepositoryById(Integer id);
    int deleteRepositoryById(Integer id);
    int deleteByClusterId(@Param("clusterId")String clusterId);

    ImageRepository findRepositoryByNameAndTenantIdAndProjectId(@Param("repoName") String repoName,@Param("tenantId") String tenantId,@Param("projectId") String projectId);
}
