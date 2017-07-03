package com.harmonycloud.service.tenant;

import com.harmonycloud.common.util.ActionReturnUtil;

/**
 * Created by zgl on 17-4-5.
 */
public interface PrivatePartitionService {
	/**
	 * 设置私有分区
	 * @param tenantid
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil setPrivatePartition(String tenantid,String namespace) throws Exception;
	/**
	 * 设置共享分区
	 * @param tenantid
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil setSharePartition(String tenantid,String namespace,boolean config) throws Exception;
	/**
	 * 移除私有分区
	 * @param tenantid
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil removePrivatePartition(String tenantid,String namespace) throws Exception;
	/**
	 * 判断改namespace是否属于私有分区
	 * @param tenantid
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public boolean isPrivatePartition(String tenantid,String namespace) throws Exception;
	/**
	 * 获取分区的专属标签
	 * @param tenantid
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPrivatePartitionLabel(String tenantid,String namespace) throws Exception;
}
