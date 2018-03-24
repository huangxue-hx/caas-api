package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.NodeDrainProgress;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * @author jmi
 *
 */
public interface NodeDrainProgressMapper {
    
	NodeDrainProgress findByNodeName(@Param("nodeName") String nodeName, @Param("clusterId") String clusterId);
	
	void insertDrainProgress(NodeDrainProgress nodeDrainProgress);
	
	void updateDrainProgress(NodeDrainProgress nodeDrainProgress);
}