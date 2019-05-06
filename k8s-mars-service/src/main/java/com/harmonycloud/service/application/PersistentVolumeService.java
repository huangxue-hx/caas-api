package com.harmonycloud.service.application;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.PersistentVolumeDto;
import com.harmonycloud.dto.cluster.ErrDeployDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.PersistentVolume;
import com.harmonycloud.k8s.bean.PersistentVolumeClaimList;
import com.harmonycloud.k8s.bean.cluster.ClusterStorage;
import com.harmonycloud.service.platform.bean.PvDto;

import java.util.List;

public interface PersistentVolumeService {
	/**
	 * 查询某个集群的存储提供者列表
	 *
	 * @param clusterId
	 * @return
	 */
	public List<ClusterStorage> listProvider(String clusterId) throws Exception;
	/**
	 * 查询某个集群某个类型的存储提供者
	 *
	 * @param clusterId
	 * @return
	 */
	public ClusterStorage getProvider(String clusterId, String type) throws Exception;

	public ClusterStorage getProvider(Cluster cluster, String type) throws Exception;

	/**
	 * 为服务创建volume，先判断是否已经创建pv，没有则先创建pv，再创建pvc
	 * @param volume
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil createVolume(PersistentVolumeDto volume) throws Exception;

	/**
	 * 删除pvc,更新pvList
	 * @param pvcList
	 * @param cluster
	 * @return ActionReturnUtil
	 * */
	public ActionReturnUtil updatePVList(PersistentVolumeClaimList pvcList, Cluster cluster) throws Exception;

	/**
	 * 更新pv
	 * @param pv
	 * @param cluster
	 * @return ActionReturnUtil
	 * */
	public ActionReturnUtil updatePV(PersistentVolume pv, Cluster cluster)throws Exception;

	/**
	 * 发布服务时在集群上创建pv资源
	 * @param volume
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil createPv(PersistentVolumeDto volume, Cluster cluster) throws Exception;

	/**
	 * 根据projectId,namespace查询pv资源列表
	 * @param projectId
	 * @return
	 * @throws Exception
	 */
	public List<PvDto> listPv(String projectId, String clusterId, Boolean isBind) throws Exception;


	/**
	 * 根据name查询pv资源详情
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil getPv(String name, String clusterId) throws Exception;

	/**
	 * 根据name删除k8s的pv资源
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil deletePv(String name, String storageClass, Cluster cluster) throws Exception;


	/**
	 * 删除项目下的所有pv
	 * @param projectId
	 * @throws Exception
	 */
	public void deletePv(String projectId) throws Exception;

	ActionReturnUtil updatePv(PvDto pvDto) throws Exception;


	ActionReturnUtil updatePvByName(PersistentVolumeDto volumeDto) throws Exception;

	boolean isFsPv(String type);

	/**
	 * 根据name清空数据
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil recyclePv(String name, String clusterId) throws Exception;

	ActionReturnUtil releasePv(String name, String clusterId, String namespace, String serviceName) throws Exception;

	ErrDeployDto transferPV(PersistentVolume pv, Cluster cluster, String deployName) throws Exception;
}
