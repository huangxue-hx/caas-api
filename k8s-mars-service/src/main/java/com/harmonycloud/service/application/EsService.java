package com.harmonycloud.service.application;


import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.RestoreInfoDto;
import com.harmonycloud.dto.application.SnapshotInfoDto;
import com.harmonycloud.dto.log.EsSnapshotDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import java.util.List;

/**
 * es相关接口
 * 
 * 
 * @author jmi
 *
 */
public interface EsService {

	/**
	 * 根据集群id查询集群的es组件连接Client
	 * @param cluster 集群信息
	 * @return
	 */
	TransportClient getEsClient(Cluster cluster) throws Exception;

	/**
	 * 查询某个集群的所有日志索引列表
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	List<String> getIndexes(String clusterId) throws Exception;

	/**
	 * 判断某个集群的es组件是否已经存在索引
	 * @param indexName
	 * @param cluster
	 * @return
	 * @throws Exception
	 */
	boolean isExistIndex(String indexName, Cluster cluster) throws Exception;

	/**
	 * 删除索引
	 * @param indexName
	 * @param cluster
	 * @return
	 * @throws Exception
	 */
	boolean deleteIndex(String indexName, Cluster cluster) throws Exception;

	/**
	 * 创建快照仓库
	 *
	 * @param esSnapshotDtoIn 必填：clusterId, backupDir    选填：maxSnapshotSpeed, maxRestoreSpeed
	 * @throws Exception
	 */
	void createSnapshotRepository(EsSnapshotDto esSnapshotDtoIn);

	/**
	 * 查询仓库信息
	 *
	 * @param clusterId
	 * @return
	 * @throws Exception
	 */
	List<RepositoryMetaData> listSnapshotRepositories(String clusterId) throws Exception;

	/**
	 * 创建快照，如果仓库不存在则创建仓库
	 *
	 * @param esSnapshotDtoIn 必填：clusterId, backupDir, dates     选填：maxSnapshotSpeed, maxRestoreSpeed
	 * @throws Exception
	 */
	void createSnapshotWithRepo(EsSnapshotDto esSnapshotDtoIn);

	/**
	 * 创建快照
	 *
	 * @param clusterId
	 * @param snapshotName
	 * @param indices
	 * @throws Exception
	 */
	void createSnapshot(String clusterId, String snapshotName, String[] indices);

	/**
	 * 查询快照
	 *
	 * @param clusterId
	 * @param snapshotNames
	 * @return
	 * @throws Exception
	 */
	List<SnapshotInfoDto> listSnapshots(String clusterId, String[] snapshotNames) throws Exception;

	/**
	 * 删除快照
	 *
	 * @param clusterId
	 * @param snapshotName
	 * @throws Exception
	 */
	void deleteSnapshot(String clusterId, String snapshotName);

	/**
	 * 恢复快照
	 *
	 * @param esSnapshotDtoIn 必填：clusterId, snapshotName    选填：renamePrefix, renameSuffix, indexNames
	 * @return
	 * @throws Exception
	 */
	RestoreInfoDto restoreSnapshots(EsSnapshotDto esSnapshotDtoIn);

	/**
	 * 删除已经恢复的快照
	 *
	 * @return
	 * @throws Exception
	 */
	boolean deleteRestoredIndex(String date, String clusterId) throws Exception;

	/**获取最新快照备份时间
	 *
	 * @param clusterId
	 * @return
	 */
	public SnapshotInfoDto getLastSnapshot(String clusterId) throws Exception;

	String getLogIndexPrefix();

}
