package com.harmonycloud.dto.cluster;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.Event;

import java.io.Serializable;

/**
 * Created by hongjie
 */
public class ClusterDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Cluster cluster;

	public ClusterDto() {

	}

	public ClusterDto(Cluster cluster) {
		this.cluster = cluster;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}
}
