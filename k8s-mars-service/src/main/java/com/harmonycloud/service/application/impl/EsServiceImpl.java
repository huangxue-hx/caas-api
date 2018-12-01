package com.harmonycloud.service.application.impl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.application.RestoreInfoDto;
import com.harmonycloud.dto.application.LogIndexDate;
import com.harmonycloud.dto.application.SnapshotInfoDto;
import com.harmonycloud.dto.log.EsSnapshotDto;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesAction;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesRequestBuilder;
import org.elasticsearch.action.admin.cluster.repositories.get.GetRepositoriesResponse;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryAction;
import org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsAction;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.get.GetSnapshotsResponse;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotAction;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotRequestBuilder;
import org.elasticsearch.action.admin.cluster.snapshots.restore.RestoreSnapshotResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.recovery.RecoveryAction;
import org.elasticsearch.action.admin.indices.recovery.RecoveryRequestBuilder;
import org.elasticsearch.action.admin.indices.recovery.RecoveryResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.RepositoryMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.indices.recovery.RecoveryState;
import org.elasticsearch.repositories.RepositoryMissingException;
import org.elasticsearch.snapshots.SnapshotInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.harmonycloud.common.Constant.CommonConstant.*;


@Service
public class EsServiceImpl implements EsService {
	
	private static Logger LOGGER = LoggerFactory.getLogger(EsServiceImpl.class);
	private Map<String, TransportClient> esClients = new ConcurrentHashMap<>();
	private Map<String, List<String>> indexMap = new ConcurrentHashMap<>();

	@Value("#{propertiesReader['es.backup.path']}")
	private String esBackupRootDir;

	@Value("${elasticsearch.index.prefix:logstash-}")
	private String esIndexPrefix;
	
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private UserService userService;

	@Override
	public TransportClient getEsClient(Cluster cluster) throws Exception{
		TransportClient transportClient = esClients.get(cluster.getId());
		if(transportClient == null){
			transportClient = this.createEsClient(cluster);
			esClients.put(cluster.getId(), transportClient);
		}
		return transportClient;
	}

	public List<String> getIndexes(String clusterId) throws Exception{
		if(StringUtils.isBlank(clusterId)){
			return null;
		}
		TransportClient esClient = esClients.get(clusterId);
		if(esClient == null){
			esClient = this.getEsClient(clusterService.findClusterById(clusterId));
		}
		ClusterStateResponse response = esClient.admin().cluster().prepareState().execute().actionGet();
		String[] indexes = response.getState().getMetaData().getConcreteAllIndices();
		indexMap.put(clusterId, Arrays.asList(indexes));
		return indexMap.get(clusterId);
	}

	@Override
	public boolean isExistIndex(String index, Cluster cluster) throws Exception {
		IndicesExistsResponse response = this.getEsClient(cluster).admin().indices()
				.exists(new IndicesExistsRequest().indices(new String[] {index })).actionGet();
		return response.isExists();
	}

	@Override
	public boolean deleteIndex(String indexName, Cluster cluster) throws Exception {
		if(!isExistIndex(indexName, cluster)) {
			throw new MarsRuntimeException(DictEnum.LOG_INDEX.phrase(), ErrorCodeMessage.NOT_FOUND);
		}
		DeleteIndexResponse dResponse = this.getEsClient(cluster).admin().indices().prepareDelete(indexName)
				.execute().actionGet();
		return dResponse.isAcknowledged();

	}

	private TransportClient createEsClient(Cluster cluster){
		try {
			Settings settings = Settings.settingsBuilder().put("cluster.name", cluster.getEsClusterName()).build();
			TransportClient transportClient = TransportClient.builder().settings(settings).build();
			transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(cluster.getEsHost()), cluster.getEsPort()));
			return transportClient;
		}catch (Exception e){
			LOGGER.error("创建ElasticSearch Client 失败,cluster:{}", JSONObject.toJSONString(cluster), e);
			throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_ES_SERVICE_ERROR);
		}

	}

	/**
	 * 创建快照仓库
	 *
	 * @param esSnapshotDtoIn
	 * @throws Exception
	 */
	public void createSnapshotRepository(EsSnapshotDto esSnapshotDtoIn) {
		try {
			Cluster cluster = clusterService.findClusterById(esSnapshotDtoIn.getClusterId());
			TransportClient client = getEsClient(cluster);
			Settings.Builder builder = Settings.builder();
			//使用集群id作为备份子目录
			builder.put(CommonConstant.ES_REPOSITORY_LOCATION, esBackupRootDir + SLASH + esSnapshotDtoIn.getClusterId());
			if (!StringUtils.isAnyBlank(esSnapshotDtoIn.getMaxSnapshotSpeed())) {
				builder.put(CommonConstant.ES_REPOSITORY_MAX_SNAPSHOT_SPEED, esSnapshotDtoIn.getMaxSnapshotSpeed() + MB);
			}
			if (!StringUtils.isAnyBlank(esSnapshotDtoIn.getMaxRestoreSpeed())){
				builder.put(CommonConstant.ES_REPOSITORY_MAX_RESTORE_SPEED, esSnapshotDtoIn.getMaxRestoreSpeed()  + MB);
			}
			Settings settings = builder.build();
			PutRepositoryRequestBuilder putRepo
					= new PutRepositoryRequestBuilder(client.admin().cluster(), PutRepositoryAction.INSTANCE);
			putRepo.setName(cluster.getName());
			putRepo.setType(CommonConstant.ES_REPOSITORY_TYPE);
			putRepo.setVerify(false);
			putRepo.setSettings(settings);
			putRepo.execute().actionGet();
		} catch (Exception e) {
			LOGGER.error("创建快照仓库失败", e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_CREATE_REPO_FAILED);
		}

	}

	/**
	 * 查询仓库信息
	 *
	 * @param cluster
	 * @return
	 * @throws Exception
	 */
	public List<RepositoryMetaData> listSnapshotRepositories(Cluster cluster) throws Exception {
		TransportClient client = getEsClient(cluster);
		GetRepositoriesRequestBuilder retRepo
				= new GetRepositoriesRequestBuilder(client.admin().cluster(), GetRepositoriesAction.INSTANCE);
		GetRepositoriesResponse response = retRepo.execute().actionGet();
		return response.repositories();
	}

	/**
	 * 创建快照，如果仓库不存在则创建仓库
	 *
	 * @param esSnapshotDtoIn
	 * @throws Exception
	 */
	public void createSnapshotWithRepo(EsSnapshotDto esSnapshotDtoIn){
		try {
			checkIndexNames(esSnapshotDtoIn);
			Cluster cluster = clusterService.findClusterById(esSnapshotDtoIn.getClusterId());
			// 检查仓库，不存在则创建
			List<RepositoryMetaData> repositoryMetaDatas = listSnapshotRepositories(cluster);

			if (CollectionUtils.isEmpty(repositoryMetaDatas)){
				createSnapshotRepository(esSnapshotDtoIn);
			}else{
				boolean exist = false;
				for(RepositoryMetaData repositoryMetaData : repositoryMetaDatas){
					if(repositoryMetaData.name().equalsIgnoreCase(cluster.getName())){
						exist = true;
					}
				}
				if(!exist){
					createSnapshotRepository(esSnapshotDtoIn);
				}
			}
			String snapshotName = esSnapshotDtoIn.getSnapshotName();
			if (StringUtils.isAnyBlank(snapshotName)){
				snapshotName = CommonConstant.ES_SNAPSHOT_CREATE_MANUAL_PREFIX
						+ DateUtil.DateToString(new Date(), DateStyle.YYMMDDHHMMSS);
			}
			createSnapshot(esSnapshotDtoIn.getClusterId(), snapshotName, esSnapshotDtoIn.getIndexNames());
		}catch (MarsRuntimeException e){
			throw e;
		}catch (Exception e){
			LOGGER.error("创建快照失败", e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_CREATE_FAILED);
		}

	}

	/**
	 * 创建快照
	 *
	 * @param clusterId
	 * @param snapshotName
	 * @param indices
	 * @throws Exception
	 */
	public void createSnapshot(String clusterId, String snapshotName, String[] indices) {
		try {
			Cluster cluster = clusterService.findClusterById(clusterId);
			TransportClient client = getEsClient(cluster);
			CreateSnapshotRequestBuilder snapshotRequestBuilder
					= new CreateSnapshotRequestBuilder(client.admin().cluster(), CreateSnapshotAction.INSTANCE);
			snapshotRequestBuilder.setRepository(cluster.getName());
			snapshotRequestBuilder.setSnapshot(snapshotName);
			snapshotRequestBuilder.setIndices(indices);
			snapshotRequestBuilder.setIncludeGlobalState(false);
			snapshotRequestBuilder.execute().actionGet();
		}catch (IndexNotFoundException e){
			LOGGER.error("创建快照失败,索引不存在,index:{}",indices, e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_INDEX_NOT_EXIST);
		}catch (Exception e){
			LOGGER.error("创建快照失败", e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_CREATE_FAILED);

		}

	}

	/**
	 * 查询快照
	 *
	 * @param clusterId
	 * @param snapshotNames
	 * @return
	 * @throws Exception
	 */
    public List<SnapshotInfoDto> listSnapshots(String clusterId, String[] snapshotNames) throws Exception{
    	List<Cluster> clusters = new ArrayList<>();
    	if(StringUtils.isBlank(clusterId)){
			clusters.addAll(userService.getCurrentUserCluster().values());
		}else{
			clusters.add(clusterService.findClusterById(clusterId));
		}
		List<SnapshotInfoDto> snapshotInfoDtos = new ArrayList<>();
    	for(Cluster cluster : clusters) {
			try {
				// 检查仓库是否已经创建，如果没有，也就没有快照
				List<RepositoryMetaData> repositoryMetaDatas = listSnapshotRepositories(cluster);
				if (CollectionUtils.isEmpty(repositoryMetaDatas)){
					continue;
				}
				TransportClient client = getEsClient(cluster);
				GetSnapshotsRequestBuilder snapshotsRequestBuilder
						= new GetSnapshotsRequestBuilder(client.admin().cluster(), GetSnapshotsAction.INSTANCE);
				snapshotsRequestBuilder.setRepository(cluster.getName());
				snapshotsRequestBuilder.setIgnoreUnavailable(true);
				if (!Objects.isNull(snapshotNames) && snapshotNames.length > 0) {
					snapshotsRequestBuilder.setSnapshots(snapshotNames);
				}
				GetSnapshotsResponse response = snapshotsRequestBuilder.execute().actionGet();
				if (response == null || CollectionUtils.isEmpty(response.getSnapshots())) {
                   continue;
				}
				Map<String,LogIndexDate> restoredDate = new HashMap<>();
				Set<String> inRestoredSnapshot = this.getRestoreSnapshot(client,cluster.getName(),restoredDate);
				List<String> indexes = this.getIndexes(cluster.getId());
				snapshotInfoDtos.addAll(response.getSnapshots().stream().map(snapshotInfo -> {
					SnapshotInfoDto snapshotInfoDto = this.convertFromESBean(snapshotInfo,indexes,restoredDate);
					snapshotInfoDto.setInRestore(inRestoredSnapshot.contains(snapshotInfo.name()));
					snapshotInfoDto.setClusterId(cluster.getId());
					snapshotInfoDto.setClusterAliasName(cluster.getAliasName());
					return snapshotInfoDto;
				}).collect(Collectors.toList()));

			} catch (RepositoryMissingException rme) {
				LOGGER.error("该集群尚未创建ES仓库", rme);
				continue;
			} catch (Exception e) {
				LOGGER.error("查询快照失败", e);
				continue;
			}
		}
        return snapshotInfoDtos;
	}

	/**
	 * 删除快照
	 *
	 * @param clusterId
	 * @param snapshotName
	 * @throws Exception
	 */
	public void deleteSnapshot(String clusterId, String snapshotName) {
		try {
			Cluster cluster = clusterService.findClusterById(clusterId);
			TransportClient client = getEsClient(cluster);
			DeleteSnapshotRequestBuilder snapshotRequestBuilder
					= new DeleteSnapshotRequestBuilder(client.admin().cluster(), DeleteSnapshotAction.INSTANCE);
			snapshotRequestBuilder.setRepository(cluster.getName());
			snapshotRequestBuilder.setSnapshot(snapshotName);
			snapshotRequestBuilder.execute().actionGet();
		} catch (Exception e){
			LOGGER.error("删除快照失败", e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_DELETE_FAILED);
		}

	}

	/**
	 * 恢复快照
	 *
	 * @param esSnapshotDtoIn
	 * @return
	 * @throws Exception
	 */
	public RestoreInfoDto restoreSnapshots(EsSnapshotDto esSnapshotDtoIn) {
		//恢复后的索引名称加后缀
		try {
			String[] snapshots = new String[]{esSnapshotDtoIn.getSnapshotName()};
			List<SnapshotInfoDto> snapshotInfos = listSnapshots(esSnapshotDtoIn.getClusterId(), snapshots);
			if (CollectionUtils.isEmpty(snapshotInfos)){
				return null;
			}
			Cluster cluster = clusterService.findClusterById(esSnapshotDtoIn.getClusterId());
			List<String> existIndexes = this.getIndexes(esSnapshotDtoIn.getClusterId());
			TransportClient client = getEsClient(cluster);
			if (Objects.isNull(esSnapshotDtoIn.getIndexNames()) || esSnapshotDtoIn.getIndexNames().length == 0) {
				//如果设置了时间段，则恢复指定时间段内的日志
				if (StringUtils.isNotBlank(esSnapshotDtoIn.getLogDateStart()) && StringUtils.isNotBlank(esSnapshotDtoIn.getLogDateEnd())) {
					List<String> indexDates = getIndexDates(esSnapshotDtoIn);
					List<String> indices = snapshotInfos.get(0).getIndices();
					List<String> reBuildIndex = new ArrayList<>();
					for (String indexDate : indexDates) {
						if (indices.contains(esIndexPrefix + indexDate)) {
							reBuildIndex.add(esIndexPrefix + indexDate);
						}
					}
					if (CollectionUtils.isEmpty(reBuildIndex)) {
						throw new MarsRuntimeException(ErrorCodeMessage.LOG_SNAPSHOT_NO_INDEX);
					}
					esSnapshotDtoIn.setIndexNames(reBuildIndex.toArray(new String[reBuildIndex.size()]));
				} else {
					List<String> indices = snapshotInfos.get(0).getIndices();
					esSnapshotDtoIn.setIndexNames(indices.toArray(new String[indices.size()]));
				}
			}
			//判断是否已经存在已恢复的索引
			String restoredIndex = "";
            for(String indexName : esSnapshotDtoIn.getIndexNames()){
				if(existIndexes.contains(indexName + ES_INDEX_SNAPSHOT_RESTORE)){
					String indexDate = indexName.replaceFirst(esIndexPrefix,"");
					restoredIndex += indexDate.replaceAll("\\.","-") + COMMA;
				}
			}
			if(StringUtils.isNotBlank(restoredIndex)){
				throw new MarsRuntimeException(restoredIndex.substring(0,restoredIndex.length()-1),
						ErrorCodeMessage.LOG_SNAPSHOT_RESTORE_EXISTS);
			}
			RestoreSnapshotRequestBuilder restoreReqBuilder = new RestoreSnapshotRequestBuilder(client.admin().cluster(), RestoreSnapshotAction.INSTANCE);
			restoreReqBuilder.setRepository(cluster.getName());
			restoreReqBuilder.setSnapshot(esSnapshotDtoIn.getSnapshotName());
			restoreReqBuilder.setIndices(esSnapshotDtoIn.getIndexNames());
			restoreReqBuilder.setPartial(true);
			StringBuilder replacement = new StringBuilder();
			replacement.append(CommonConstant.ES_INDEX_RENAME_REPALCEMENT);
			replacement.append(ES_INDEX_SNAPSHOT_RESTORE);
			restoreReqBuilder.setRenamePattern(CommonConstant.ES_RESTORE_RENAME_PATTERN);
			restoreReqBuilder.setRenameReplacement(replacement.toString());
			RestoreSnapshotResponse response = restoreReqBuilder.execute().actionGet();
			RestoreInfoDto restoreInfoDto = new RestoreInfoDto();
			return restoreInfoDto.convertFromESBean(response.getRestoreInfo());

		}catch (MarsRuntimeException e){
			throw e;
		}catch (Exception e){
			LOGGER.error("恢复快照失败", e);
			throw new MarsRuntimeException(ErrorCodeMessage.APP_LOG_SNAPSHOT_RESTORE_FAILED);
		}

	}

	@Override
	public boolean deleteRestoredIndex(String date, String clusterId) throws Exception{
		AssertUtil.notBlank(clusterId, DictEnum.CLUSTER);
		AssertUtil.notNull(date);
		Cluster cluster = clusterService.findClusterById(clusterId);
		String indexDate = DateUtil.StringToString(date, DateStyle.YYYY_MM_DD,DateStyle.YYYYMMDD_DOT);
		if(indexDate == null){
			throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PARAMETER);
		}
		String indexName = esIndexPrefix + indexDate + ES_INDEX_SNAPSHOT_RESTORE;
		return this.deleteIndex(indexName, cluster);
	}

	/**获取最新自动快照备份时间
	 *
	 * @param clusterId
	 * @return
	 */
	public SnapshotInfoDto getLastSnapshot(String clusterId) throws Exception{
		List<SnapshotInfoDto> snapshotInfoDtos = listSnapshots(clusterId,null);
		if (CollectionUtils.isEmpty(snapshotInfoDtos)){
			return null;
		}
		// 一般最新创建的快照放在末尾，从后往前查属于自动备份的最新快照速度快些。
		// 由于手动备份的快照索引时间有各种可能，不具备参考价值，所以只考虑自动备份的。
		boolean isAutoBackupFound = false;
		int i = snapshotInfoDtos.size() -1;
		for (; i >= 0 ; i--) {
			if (snapshotInfoDtos.get(i).getName().startsWith(CommonConstant.ES_SNAPSHOT_CREATE_AUTO_PREFIX)){
				isAutoBackupFound = true;
				break;
			}
		}
		return isAutoBackupFound ? snapshotInfoDtos.get(i) : null;
	}

	@Override
	public String getLogIndexPrefix() {
		return esIndexPrefix;
	}

	/**
	 * 根据时间日期获取对应的索引名称列表
	 * @param esSnapshotDtoIn
	 */
	private void checkIndexNames(EsSnapshotDto esSnapshotDtoIn) throws Exception{
		List<String> indexNames = new ArrayList<>();
		List<String> existIndexes = this.getIndexes(esSnapshotDtoIn.getClusterId());
		if(esSnapshotDtoIn.getIndexNames() != null && esSnapshotDtoIn.getIndexNames().length > 0){
			for(String indexName : esSnapshotDtoIn.getIndexNames()) {
				if (existIndexes.contains(indexName)) {
					indexNames.add(indexName);
				}
			}
			if(CollectionUtils.isEmpty(indexNames)){
				throw new MarsRuntimeException(ErrorCodeMessage.LOG_SNAPSHOT_NO_INDEX);
			}
			esSnapshotDtoIn.setIndexNames(indexNames.toArray(new String[0]));
			return;
		}
		List<String> indexDates;
		String[] indexDateArray= esSnapshotDtoIn.getDates();
		if(indexDateArray == null || indexDateArray.length == 0){
			indexDates = this.getIndexDates(esSnapshotDtoIn);
		}else{
			indexDates = Arrays.asList(indexDateArray);
		}
		for(String indexDate : indexDates) {
			String indexName = esIndexPrefix + indexDate;
			if (existIndexes.contains(indexName)) {
				indexNames.add(indexName);
			}
		}
		if(CollectionUtils.isEmpty(indexNames)){
			throw new MarsRuntimeException(ErrorCodeMessage.LOG_SNAPSHOT_NO_INDEX);
		}
		esSnapshotDtoIn.setIndexNames(indexNames.toArray(new String[0]));
	}

	private List<String> getIndexDates(EsSnapshotDto esSnapshotDtoIn){
		List<String> indexDates = new ArrayList<>();
		if(StringUtils.isBlank(esSnapshotDtoIn.getLogDateStart()) || StringUtils.isBlank(esSnapshotDtoIn.getLogDateEnd())){
			throw new MarsRuntimeException(ErrorCodeMessage.LOG_SNAPSHOT_DATE_ERROR);
		}
		Date start = DateUtil.StringToDate(esSnapshotDtoIn.getLogDateStart(),DateStyle.YYYY_MM_DD);
		Date end = DateUtil.StringToDate(esSnapshotDtoIn.getLogDateEnd(),DateStyle.YYYY_MM_DD);
		if(start.after(end)){
			throw new MarsRuntimeException(ErrorCodeMessage.LOG_SNAPSHOT_DATE_ERROR);
		}
		while(start.before(end) || start.equals(end)){
			indexDates.add(DateUtil.DateToString(start,DateStyle.YYYYMMDD_DOT));
			start = DateUtil.addDay(start,1);
		}
		return indexDates;
	}

	/**
	 * 获取正在恢复中的快照列表
	 * @param client
	 * @param clusterName
	 * @return
	 */
	private Set<String> getRestoreSnapshot(TransportClient client,String clusterName, Map<String, LogIndexDate> restoredDates){
		Set<String> inRestoreSnapshot = new HashSet<>();
		RecoveryRequestBuilder recoveryRequestBuilder = new RecoveryRequestBuilder(client.admin().cluster(), RecoveryAction.INSTANCE);
		RecoveryResponse recoveryResponse = recoveryRequestBuilder.execute().actionGet();
		if(recoveryResponse.hasRecoveries()){
			Map<String, List<RecoveryState>> stateMap = recoveryResponse.shardRecoveryStates();
			for(Map.Entry<String, List<RecoveryState>> entry : stateMap.entrySet()){
				List<RecoveryState> states = entry.getValue();
				if(CollectionUtils.isEmpty(states)){
					continue;
				}
				String indexName = entry.getKey();
				if(indexName.endsWith(ES_INDEX_SNAPSHOT_RESTORE)) {
					LogIndexDate logIndexDate = new LogIndexDate();
					logIndexDate.setLogDate(this.getLogDateFromIndexName(indexName));
					logIndexDate.setIndexName(indexName);
					logIndexDate.setCreated(new Date(states.get(0).getIndex().startTime()));
					logIndexDate.setRestoredDone(true);
					restoredDates.put(indexName,logIndexDate);
				}
				for(RecoveryState state : states){
					//如果有一个分片状态不是完成状态，则这个恢复的索引状态为恢复中
					if(restoredDates.get(indexName) != null  && restoredDates.get(indexName).getRestoredDone()
							&& !state.getStage().name().equalsIgnoreCase(CommonConstant.DONE)){
						restoredDates.get(indexName).setRestoredDone(false);
					}
					if(state.getType().name().equalsIgnoreCase("SNAPSHOT")
							&& state.getRestoreSource() != null
							&& !state.getStage().name().equalsIgnoreCase(CommonConstant.DONE)){
						String repository = state.getRestoreSource().snapshotId().getRepository();
						String snapshot = state.getRestoreSource().snapshotId().getSnapshot();
						if(repository.equalsIgnoreCase(clusterName)) {
							inRestoreSnapshot.add(snapshot);
						}
					}
				}
			}
		}
		return inRestoreSnapshot;
	}

	private SnapshotInfoDto convertFromESBean(SnapshotInfo snapshotInfo, List<String> indexes, Map<String,LogIndexDate> restoredDate){
		if (null == snapshotInfo){
			return null;
		}
		SnapshotInfoDto snapshotInfoDto = new SnapshotInfoDto();
		List<LogIndexDate> logIndexDates = new ArrayList<>();
		List<String> indices = snapshotInfo.indices();
		snapshotInfoDto.setIndices(indices);
		snapshotInfoDto.setName(snapshotInfo.name());
		snapshotInfoDto.setState(snapshotInfo.state());
		snapshotInfoDto.setReason(snapshotInfo.reason());
		snapshotInfoDto.setStartTime(snapshotInfo.startTime());
		snapshotInfoDto.setEndTime(snapshotInfo.endTime());
		snapshotInfoDto.setTotalShards(snapshotInfo.totalShards());
		snapshotInfoDto.setSuccessfulShards(snapshotInfo.successfulShards());
		snapshotInfoDto.setVersion(snapshotInfo.version());
		Date start = DateUtil.StringToDate(indices.get(0).replace(esIndexPrefix,""),
				DateStyle.YYYYMMDD_DOT);
		Date end = start;
		for(String indexName : indices){
			String strIndexDate = indexName.replace(esIndexPrefix,"");
			if(indexes.contains(indexName+CommonConstant.ES_INDEX_SNAPSHOT_RESTORE)
					&& restoredDate.get(indexName+CommonConstant.ES_INDEX_SNAPSHOT_RESTORE) != null){
				logIndexDates.add(restoredDate.get(indexName+CommonConstant.ES_INDEX_SNAPSHOT_RESTORE));
			}
			Date indexDate = DateUtil.StringToDate(strIndexDate, DateStyle.YYYYMMDD_DOT);
			if(indexDate.after(end)){
				end = indexDate;
			}
			if(indexDate.before(start)){
				start = indexDate;
			}
		}
		snapshotInfoDto.setRestoredDate(logIndexDates);
		snapshotInfoDto.setLogStartDate(DateUtil.DateToString(start,DateStyle.YYYY_MM_DD));
		snapshotInfoDto.setLogEndDate(DateUtil.DateToString(end, DateStyle.YYYY_MM_DD));
		return snapshotInfoDto;
	}

	private String getLogDateFromIndexName(String indexName){
		String strIndexDate = indexName.replace(esIndexPrefix,"");
		strIndexDate = strIndexDate.replace(CommonConstant.ES_INDEX_SNAPSHOT_RESTORE,"");
		return strIndexDate.replaceAll("\\.","-");
	}

}
