package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;

public interface SecretService {
	
	/**
	 * 检测secret
	 * @param userName
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil checkedSecret(String userName, String password) throws Exception;

	/**
	 * 创建secret
	 * @param namespace
	 * @param userName
	 * @param password
	 * @param cluster
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil doSecret(String namespace, String userName, String password,Cluster cluster) throws Exception;

	void createHarborSecret(String name, String namespace, String harborHost, String username, String password) throws Exception;
}
