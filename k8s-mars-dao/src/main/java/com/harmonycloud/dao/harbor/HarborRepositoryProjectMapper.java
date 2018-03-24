package com.harmonycloud.dao.harbor;

import com.harmonycloud.dao.harbor.bean.HarborRepositoryProject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HarborRepositoryProjectMapper {
    int insert(HarborRepositoryProject harborRepositoryProject);
    int update(HarborRepositoryProject harborRepositoryProject);
    int delete(Long id);
    List<HarborRepositoryProject> listByRepositoryName(String repositoryName);

}
