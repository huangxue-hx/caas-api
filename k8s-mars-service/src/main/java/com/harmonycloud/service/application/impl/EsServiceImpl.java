package com.harmonycloud.service.application.impl;

import com.harmonycloud.common.enumm.EnumLogSeverity;
import com.harmonycloud.common.enumm.EnumMonitorQuery;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.ClusterMapper;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.platform.bean.ContainerLog;
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
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class EsServiceImpl implements EsService {

	private static final int SEARCH_TIME = 60000;
	@Autowired
	private HttpSession session;

	private ClusterMapper clusterMapper;

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
			Assert.hasText(logQuery.getContainer(),"container cannot be null");
			Assert.hasText(logQuery.getNamespace(),"namespace cannot be null");
			Assert.hasText(logQuery.getLogDir(),"logDir cannot be null");
			SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
			scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
			scrollId = scrollResp.getScrollId();
		}else{
			scrollResp = client.prepareSearchScroll(scrollId).setScroll(new TimeValue(SEARCH_TIME)).execute().actionGet();
		}
		for (SearchHit it : scrollResp.getHits().getHits()) {
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
	
	public ActionReturnUtil listfileName(String container, String namespace, String clusterId) throws Exception {
		TreeSet<String> logFileNames = new TreeSet<String>();
		 //创建客户端
		if (StringUtils.isEmpty(container)) {
			return ActionReturnUtil.returnErrorWithMsg("container cannot be null");
		}

		if (StringUtils.isEmpty(namespace)) {
			return ActionReturnUtil.returnErrorWithMsg("namespace cannot be null");
		}

		Cluster cluster = null;
		if(clusterId != null && !clusterId.equals("")) {
			cluster = this.clusterMapper.findClusterById(clusterId);
		} else {
			cluster = (Cluster) session.getAttribute("currentCluster");
		}

		EsClient esClient = new EsClient(cluster);
		Client client = esClient.getEsClient();
		QueryBuilder queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.termQuery("container_name", container))
									.must(QueryBuilders.termQuery("namespace_name", namespace));
		SearchResponse scrollResp = null;

		scrollResp = client.prepareSearch("logstash-*")
			.addAggregation(AggregationBuilders.terms("logdir").field("logdir"))
				.setScroll(new TimeValue(SEARCH_TIME))
				.setQuery(queryBuilder).execute().actionGet();
		if(scrollResp.getAggregations() == null){
			return ActionReturnUtil.returnSuccessWithData(logFileNames);
		}
		Terms agg1 = scrollResp.getAggregations().get("logdir");
		List<Bucket> buckets = agg1.getBuckets();
		for (Bucket bucket : buckets) {
			String name = bucket.getKey().toString();
			logFileNames.add(name);
		}
		return ActionReturnUtil.returnSuccessWithData(logFileNames);
	}
	
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
				.must(QueryBuilders.termQuery("container_name", logQuery.getContainer()))
				.must(QueryBuilders.termQuery("namespace_name", logQuery.getNamespace()))
				.must(QueryBuilders.termQuery("logdir", logQuery.getLogDir()));

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
