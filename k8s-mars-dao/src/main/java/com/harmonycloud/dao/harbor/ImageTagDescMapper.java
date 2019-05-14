package com.harmonycloud.dao.harbor;

import com.harmonycloud.dao.harbor.bean.ImageTagDesc;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageTagDescMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(ImageTagDesc record);

    int insertSelective(ImageTagDesc record);

    ImageTagDesc selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ImageTagDesc record);

    int updateByPrimaryKey(ImageTagDesc record);

    // 根据条件查询镜像版本描述
    List<ImageTagDesc> selectList(@Param("repositoryId") Integer repositoryId, @Param("imageName") String imageName, @Param("tagName") String tagName);
}