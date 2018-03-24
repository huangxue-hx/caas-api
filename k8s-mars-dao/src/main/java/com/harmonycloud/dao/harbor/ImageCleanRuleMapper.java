package com.harmonycloud.dao.harbor;

import com.harmonycloud.dao.harbor.bean.ImageCleanRule;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageCleanRuleMapper {

    List<ImageCleanRule> list();
    int insert(ImageCleanRule rule);
    int delete(Long id);
    int update(ImageCleanRule rule);
    List<ImageCleanRule> getBySelective(ImageCleanRule rule);
    List<ImageCleanRule> getByName(String name);
    List<ImageCleanRule> listByIds(@Param("repositoryIds")List<Integer> repositoryIds);
    void deleteByClusterId(@Param("clusterId")String clusterId);


}
