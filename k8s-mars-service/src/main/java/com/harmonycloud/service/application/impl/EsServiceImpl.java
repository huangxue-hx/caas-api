package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.EnumLogSeverity;
import com.harmonycloud.common.enumm.EnumMonitorQuery;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.BizUtil;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.bean.ProviderPlugin;
import com.harmonycloud.service.platform.client.EsClient;
import com.harmonycloud.service.platform.constant.Constant;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class EsServiceImpl implements EsService {

	private static final int SEARCH_TIME = 60000;
	private static Logger LOGGER = LoggerFactory.getLogger(EsServiceImpl.class);
	@Autowired
	private HttpSession session;

	@Autowired
	private ClusterMapper clusterMapper;

	@Autowired
	private DeploymentsService deploymentsService;

	@Override
	public ActionReturnUtil fileLog(LogQuery logQuery)
			throws Exception {

		StringBuilder log = new StringBuilder();
		SearchResponse scrollResp = null;
		String scrollId = logQuery.getScrollId();

		Cluster cluster = null;
		if(logQuery.getClusterId() != null && !logQuery.getClusterId().equals("")) {
			cluster = this.clusterMapper.findClusterById(logQuery.getClusterId());
		} else {
			cluster = (Cluster) session.getAttribute("currentCluster");
		}

		EsClient esClient = new EsClient(cluster);
		TransportClient client = esClient.getEsClient();
		if(StringUtils.isBlank(scrollId)){
			if (StringUtils.isBlank(logQuery.getNamespace())) {
				return ActionReturnUtil.returnErrorWithMsg("分区名不能为空");
			}
			if(StringUtils.isBlank(logQuery.getDeployment()) && StringUtils.isBlank(logQuery.getPod())
					&& StringUtils.isBlank(logQuery.getContainer())){
				return ActionReturnUtil.returnErrorWithData("服务名、pod名称和容器名称不能都为空");
			}
			SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
			scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
			scrollId = scrollResp.getScrollId();
		}else{
			scrollResp = client.prepareSearchScroll(scrollId).setScroll(new TimeValue(SEARCH_TIME)).execute().actionGet();
		}
		for (SearchHit it : scrollResp.getHits().getHits()) {
			String podName = it.getSource().get("pod_name").toString();
			if(!BizUtil.isPodWithDeployment(podName, logQuery.getDeployment())){
				continue;
			}
			log.append(it.getSource().get("message").toString() +"\n");
		}
		if(log.toString().length() == 0){
			log.append("No log found.");
		}
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("log", log);
		data.put("scrollId", scrollId);
		data.put("totalHit", scrollResp.getHits().getTotalHits());
	    return ActionReturnUtil.returnSuccessWithData(data);
	}

	@Override
	public ActionReturnUtil listfileName(String namespace, String deploymentName, String podName,
										 String containerName, String clusterId) throws Exception {
		TreeSet<String> logFileNames = new TreeSet<String>();
		 //创建客户端
		if (StringUtils.isBlank(namespace)) {
			return ActionReturnUtil.returnErrorWithMsg("分区名不能为空");
		}
		if(StringUtils.isBlank(deploymentName) && StringUtils.isBlank(podName)
				&& StringUtils.isBlank(containerName)){
			return ActionReturnUtil.returnErrorWithData("服务名、pod名称和容器名称不能都为空");
		}
		Cluster cluster = null;
		if(clusterId != null && !clusterId.equals("")) {
			cluster = this.clusterMapper.findClusterById(clusterId);
		} else {
			cluster = (Cluster) session.getAttribute("currentCluster");
		}
		EsClient esClient = new EsClient(cluster);
		Client client = esClient.getEsClient();
		if(StringUtils.isBlank(containerName) && StringUtils.isBlank(podName)){
			ActionReturnUtil containerRes = deploymentsService.deploymentContainer(namespace,deploymentName,cluster);
			if(containerRes.isSuccess() && containerRes.get("data")!=null){
				List<ContainerOfPodDetail> containers = (List<ContainerOfPodDetail>)containerRes.get("data");
				for (ContainerOfPodDetail container : containers) {
					logFileNames.addAll(this.listLogFileNames(namespace, deploymentName, null,container.getName(), client, true));
				}
			}
		}else {
			logFileNames.addAll(this.listLogFileNames(namespace, deploymentName, podName, containerName, client,false));
		}
		return ActionReturnUtil.returnSuccessWithData(logFileNames);
	}

	@Override
	public ActionReturnUtil getProcessLog(String rangeType, String processName, String node) throws Exception {
		List<String> result = new ArrayList<String>();
		try {
			EsClient esClient = new EsClient();
			Client client = esClient.getEsClient();
			EnumMonitorQuery query = EnumMonitorQuery.getRangeData(rangeType);
			if (query == null) {
				return ActionReturnUtil.returnErrorWithMsg("params is null!");
			}
			Date now = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
			long startTime = now.getTime() - query.getMillisecond();
			String from = formatter.format(new Date(startTime));
			String to = formatter.format(now);
			QueryBuilder queryBuilder = QueryBuilders.rangeQuery("timestamp").from(from).to(to);
			QueryBuilder matchBuilder = QueryBuilders.disMaxQuery().add(QueryBuilders.termQuery("tag", processName))
					.add(QueryBuilders.matchQuery("host_ip", node));
			SearchResponse searchResponse = client.prepareSearch("logstash-*")
					            .addSort("@timestamp", SortOrder.DESC)
					            .setScroll(new TimeValue(SEARCH_TIME))
					            .setQuery(matchBuilder)
					            .setQuery(queryBuilder).execute().actionGet();
			for (SearchHit it : searchResponse.getHits().getHits()) {
				result.add(it.getSource().get("@timestamp").toString()+"  "+it.getSource().get("message").toString()+
						"(pid:"+it.getSource().get("pid").toString()+") and (source:"+it.getSource().get("source").toString()+")");
			} 	
		} catch (Exception e) {
			throw e;
		}
		return ActionReturnUtil.returnSuccessWithData(result);
	}

	@Override
	public ActionReturnUtil listProvider() throws Exception {
		List<ProviderPlugin> provider = new ArrayList<ProviderPlugin>();
		EsClient esClient = new EsClient();
		ProviderPlugin providerPlugin = new ProviderPlugin();
		providerPlugin.setIp(esClient.getHost());
		providerPlugin.setName(Constant.ES);
		providerPlugin.setVersion(esClient.getVersion());
		provider.add(providerPlugin);
		return ActionReturnUtil.returnSuccessWithData(provider);
	}

	/**
	 * 查询指定pod或容器的日志文件名称列表
	 * @param namespace
	 * @param deployment
	 * @param podName
	 * @param containerName
	 * @param client
	 * @return
	 * @throws MarsRuntimeException
	 */
	/*private TreeSet<String> listLogFileNames(String namespace, String deployment, String podName,
											 String containerName, Client client){
		if(StringUtils.isBlank(podName) && StringUtils.isBlank(containerName)){
			throw new IllegalArgumentException("查询应用日志文件名称列表出错，pod名称和容器名称不能同时为空");
		}
		TreeSet<String> logFileNames = new TreeSet<String>();

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("namespace_name", namespace));
		if(StringUtils.isNotBlank(containerName)){
			queryBuilder.must(QueryBuilders.termQuery("container_name", containerName));
		}
		if(StringUtils.isNotBlank(podName)){
			queryBuilder.must(QueryBuilders.termQuery("pod_name", podName));
		}else if(StringUtils.isNotBlank(deployment)){
			queryBuilder.must(QueryBuilders.regexpQuery("pod_name", deployment + "-.*"));
		}
		SearchResponse scrollResp = null;
		scrollResp = client.prepareSearch("logstash-*")
				.addAggregation(AggregationBuilders.terms("logdir").field("logdir"))
				.setScroll(new TimeValue(SEARCH_TIME))
				.setQuery(queryBuilder).execute().actionGet();
		if(scrollResp.getAggregations() == null){
			return logFileNames;
		}
		Terms agg1 = scrollResp.getAggregations().get("logdir");
		List<Bucket> buckets = agg1.getBuckets();
		for (Bucket bucket : buckets) {
			String name = bucket.getKey().toString();
			logFileNames.add(name);
		}
		return logFileNames;
	}*/


	/**
	 * 查询某个容器的日志文件名称列表，返回文件名称包含pod名称为前缀
	 * @param namespace
	 * @param deployment
	 * @param containerName
	 * @param client
	 * @return
	 */
	private TreeSet<String> listLogFileNames(String namespace, String deployment, String podName,
													String containerName, Client client, boolean withPodName){
		TreeSet<String> logFileNames = new TreeSet<String>();
		SearchResponse scrollResp = null;
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("namespace_name", namespace));
		if(StringUtils.isNotBlank(containerName)){
			queryBuilder.must(QueryBuilders.termQuery("container_name", containerName));
		}
		if(StringUtils.isNotBlank(podName)){
			queryBuilder.must(QueryBuilders.termQuery("pod_name", podName));
			scrollResp = client.prepareSearch("logstash-*")
					.addAggregation(AggregationBuilders.terms("logdir").field("logdir"))
					.setScroll(new TimeValue(SEARCH_TIME))
					.setQuery(queryBuilder).execute().actionGet();
			if(scrollResp.getAggregations() == null){
				return logFileNames;
			}
			Terms agg1 = scrollResp.getAggregations().get("logdir");
			List<Bucket> buckets = agg1.getBuckets();
			for (Bucket bucket : buckets) {
				String name = bucket.getKey().toString();
				logFileNames.add(name);
			}
			return logFileNames;
		}
		//pod名称为空，则容器名称不能为空
		Assert.hasText(containerName);
		TermsBuilder logDirTermsBuilder = AggregationBuilders.terms("logdir").field("logdir");
		TermsBuilder podTermsBuilder = AggregationBuilders.terms("pod_name").field("pod_name");
		podTermsBuilder.subAggregation(logDirTermsBuilder);
		scrollResp = client.prepareSearch("logstash-*")
				.addAggregation(podTermsBuilder)
				.setScroll(new TimeValue(SEARCH_TIME))
				.setQuery(queryBuilder).execute().actionGet();
		if(scrollResp.getAggregations() == null){
			return logFileNames;
		}
		Terms podTerms = scrollResp.getAggregations().get("pod_name");
		List<Bucket> podBuckets = podTerms.getBuckets();
		for (Bucket bucket : podBuckets) {
			String bucketPodName = bucket.getKey().toString();
			if(!BizUtil.isPodWithDeployment(bucketPodName, deployment)){
				LOGGER.warn("容器对应的pod名称" + bucketPodName + "前缀非服务名称， 不是同一个服务下的容器");
				continue;
			}
			Terms logDirTerms = bucket.getAggregations().get("logdir");
			List<Bucket> logDirBuckets = logDirTerms.getBuckets();
			for (Bucket dirBucket : logDirBuckets) {
				String logDir = dirBucket.getKey().toString();
				if(withPodName){
					logFileNames.add(bucketPodName + "/" + logDir);
				}else {
					logFileNames.add(logDir);
				}
			}
		}

		return logFileNames;
	}


	/**
	 * 根据查询条件设置SearchRequestBuilder
	 * @param client
	 * @param logQuery
     * @return
     */
	private SearchRequestBuilder getSearchRequestBuilder(TransportClient client, LogQuery logQuery){
		//日志时间范围查询设置
		QueryBuilder postFilter = QueryBuilders.rangeQuery("@timestamp").from(logQuery.getLogDateStart())
				.to(logQuery.getLogDateEnd());
		//日志时间范围查询设置
		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("namespace_name", logQuery.getNamespace()));
		if(StringUtils.isNotBlank(logQuery.getContainer())){
			queryBuilder.must(QueryBuilders.termQuery("container_name", logQuery.getContainer()));
		}
		if(StringUtils.isNotBlank(logQuery.getLogDir())){
			queryBuilder.must(QueryBuilders.termQuery("logdir", logQuery.getLogDir()));
		}
		if(StringUtils.isNotBlank(logQuery.getPod())){
			queryBuilder.must(QueryBuilders.termQuery("pod_name", logQuery.getPod()));
		}else if(StringUtils.isNotBlank(logQuery.getDeployment())){
			queryBuilder.must(QueryBuilders.regexpQuery("pod_name", logQuery.getDeployment() + "-.*"));
		}
		if(StringUtils.isNotBlank(logQuery.getSeverity()) && !logQuery.getSeverity().equalsIgnoreCase("All")){
			//日志内容过滤 warn, error等信息
			queryBuilder = queryBuilder.must(QueryBuilders.termQuery("message",
					EnumLogSeverity.getSeverityName(logQuery.getSeverity())));
		}
		if(StringUtils.isNotBlank(logQuery.getSearchWord())){
			//日志内容关键字查询
			queryBuilder = queryBuilder.must(QueryBuilders.matchQuery("message",
					logQuery.getSearchWord()));
		}
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch("logstash-*")
				.addSort("@timestamp", SortOrder.ASC)
				.setScroll(new TimeValue(SEARCH_TIME))
				.setPostFilter(postFilter)
				.setQuery(queryBuilder);
		return searchRequestBuilder;
	}

}
