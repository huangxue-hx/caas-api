package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.NodePortClusterRange;
import org.apache.ibatis.annotations.Param;

import javax.lang.model.type.IntersectionType;

/**
 * @Author jiangmi
 * @Description
 * @Date created in 2017-12-12
 * @Modified
 */
public interface NodePortClusterRangeMapper {

     NodePortClusterRange findByClusterId(@Param("clusterId") String clusterId);

}
