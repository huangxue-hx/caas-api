package com.harmonycloud.dao.cluster;

import com.harmonycloud.dao.cluster.bean.ClusterDomain;


public interface ClusterDomainMapper {
	/**
	 * 查询域名
	 * @return
	 */
	ClusterDomain  find();
	//修改域名
	void updateDomain(String domain);

    void insert(String domain);
}