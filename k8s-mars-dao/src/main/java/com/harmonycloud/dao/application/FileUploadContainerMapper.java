package com.harmonycloud.dao.application;

import com.harmonycloud.dao.application.bean.FileUploadContainer;
import com.harmonycloud.dao.application.bean.FileUploadContainerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface FileUploadContainerMapper {
    int countByExample(FileUploadContainerExample example);

    int deleteByExample(FileUploadContainerExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(FileUploadContainer record);

    int insertSelective(FileUploadContainer record);

    List<FileUploadContainer> selectByExample(FileUploadContainerExample example);

    FileUploadContainer selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") FileUploadContainer record, @Param("example") FileUploadContainerExample example);

    int updateByExample(@Param("record") FileUploadContainer record, @Param("example") FileUploadContainerExample example);

    int updateByPrimaryKeySelective(FileUploadContainer record);

    int updateByPrimaryKey(FileUploadContainer record);
}