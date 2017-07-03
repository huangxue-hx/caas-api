package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.Cluster;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterMapper {


    /**
     * 新增集群
     * @param cluster
     */
	void addCluster(Cluster cluster);

	/**
	 * 根据id 查找cluster
	 * @param id
	 * @return
	 */
	Cluster findClusterById(String id);

	/**
	 * 根据host查找cluster
	 * @param host
	 * @return
	 */
	Cluster findClusterByHost(String host);

	/**
	 * 更新集群
	 * @param cluster
	 */
	void updateCluster(Cluster cluster);

	/**
	 * 集群列表
	 * @return
	 */
	List<Cluster> listClusters();
}