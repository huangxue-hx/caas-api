package com.harmonycloud.service.platform.service;

import com.harmonycloud.dao.cluster.bean.NodeDrainProgress;

/**
 * 节点应用迁移进度服务
 * @author jmi
 *
 */
public interface NodeDrainProgressService {
    
	NodeDrainProgress findByNodeName(String nodeName, String clusterId);
	
	void insertDrainProgress(NodeDrainProgress nodeDrainProgress);
	
	void updateDrainProgress(NodeDrainProgress nodeDrainProgress);
}