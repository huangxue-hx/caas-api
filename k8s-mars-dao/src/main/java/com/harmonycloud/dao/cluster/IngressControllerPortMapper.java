package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.IngressControllerPort;
import com.harmonycloud.dao.cluster.bean.IngressControllerPortExample;
import java.util.List;

public interface IngressControllerPortMapper {
    int deleteByExample(IngressControllerPortExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(IngressControllerPort record);

    int insertSelective(IngressControllerPort record);

    List<IngressControllerPort> selectByExample(IngressControllerPortExample example);

    IngressControllerPort selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(IngressControllerPort record);

    int updateByPrimaryKey(IngressControllerPort record);
}