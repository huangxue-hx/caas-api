package com.harmonycloud.common.util;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by czm on 2017/3/28.
 */
@Component
public class ESFactory {
	private static Logger logger = LoggerFactory.getLogger(ESFactory.class);

	public static TransportClient esClient;
	public static String host;
	public static Integer port;
	public static String version;
	public static String indexName;
	public static String type;
	public static String clusterName;
	public static ExecutorService executor = Executors.newFixedThreadPool(20);
	
	//private static final Integer ES_SCROLL_TIMEOUT = 60000;

	public static TransportClient createES() {
		if (esClient == null) {
			synchronized (ESFactory.class) {
				try {
					String[] hosts = host.split(",");
					Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
					TransportClient client = TransportClient.builder().settings(settings).build();
					for (String host : hosts) {
						client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
					}
					ESFactory.esClient = client;

					if (!isExistsType(indexName)) {
						createMapping(indexName, type);
						if (isExistsType(indexName)) {
							logger.debug("操作审计ES创建成功");
						} else {
							logger.debug("操作审计ES创建失败");
						}
					}

					return esClient;
				} catch (Exception e) {
					logger.error("创建ElasticSearch Client 失败", e);
					logger.debug("创建ElasticSearch Client 失败", e);
				}
			}

		}
		return esClient;
	}

	/**
	 * 插入数据到ES.
	 *
	 * @param sr
	 *            操作记录
	 * @return 插入是否成功
	 * @throws IOException
	 *             IO异常
	 */
	public static ActionReturnUtil insertToIndexBySR(SearchResult sr) throws Exception {
		logger.info("flagdezhi:");
		String method = sr.getMethod();
		String user = sr.getUser();
		String module = sr.getModule();
		String opTime = sr.getOpTime();
		String opFun = sr.getOpFun();
		String reqParams = sr.getRequestParams();
		String opStatus = sr.getOpStatus();
		String remoteIp = sr.getRemoteIp();
		String response = sr.getResponse();
		String path = sr.getPath();
		logger.debug("即将插入es：" + sr.getPath() + ";" + sr.getRemoteIp());
		IndexResponse indexResponse = ESFactory.createES()
				.prepareIndex(indexName, type, String.valueOf(new Date().getTime()))
				.setSource(                 // 这里可以直接用json字符串
						XContentFactory.jsonBuilder().startObject().field("user", user).field("tenant", sr.getTenant())
								.field("module", module).field("opFun", opFun).field("method", method)
								.field("opTime", opTime).field("requestParams", reqParams).field("remoteIp", remoteIp)
								.field("response", response).field("path", path).field("opStatus", opStatus).field("subject", sr.getSubject())
								.endObject())
				.setTTL(1000).get();
		return ActionReturnUtil.returnSuccess();
	}

	/**
	 * 根据查询条件从ES中查询结果.
	 *
	 * @param query
	 *            查询条件
	 * @return 查询结果列表
	 * @throws IOException
	 *             IO异常
	 */
	public static ActionReturnUtil searchFromIndex(BoolQueryBuilder query, String scrollId, int pageSize,
			int currentPage) throws IOException {
		
		SearchResponse response;
		response = ESFactory.createES().prepareSearch(indexName).setTypes(type)
				// .setScroll(new TimeValue(60000))
				.setQuery(query).addSort("opTime", SortOrder.DESC).setFrom((currentPage - 1) * pageSize).setSize(pageSize).setExplain(true) // 这里需要修改整整分页之后
				.get();
		// scrollid的分页,setSize中的5为分片数
		/*if (StringUtils.isBlank(scrollId)) {
			response = ESFactory.createES().prepareSearch(indexName).setTypes(type)
					.setSearchType(SearchType.QUERY_AND_FETCH).setScroll(new TimeValue(ES_SCROLL_TIMEOUT)).setQuery(query)
					.addSort("opTime", SortOrder.DESC).setFrom(0).setSize(pageSize/CommonConstant.ES_SHARDS).setExplain(true) // 这里需要修改整整分页之后
					.get();
			scrollId = response.getScrollId();
		} else {
			response = ESFactory.createES().prepareSearchScroll(scrollId).setScroll(new TimeValue(ES_SCROLL_TIMEOUT)).execute()
					.actionGet();
		}*/

		Iterator<SearchHit> it = response.getHits().iterator();
		List<SearchResult> searchResults = new ArrayList<>();

		while (it.hasNext()) {
			SearchHit sh = it.next();
			Map<String, Object> doc = sh.getSource();
			SearchResult sr = new SearchResult();

			if (doc.get("module") != null) {
				sr.setModule(doc.get("module").toString());
			}
			if (doc.get("requestParams") != null) {
				sr.setRequestParams(doc.get("requestParams").toString());
			}
			if (doc.get("opFun") != null) {
				sr.setOpFun(doc.get("opFun").toString());
			}
			if (doc.get("opStatus") != null) {
				sr.setOpStatus(doc.get("opStatus").toString());
			}
			if (doc.get("method") != null) {
				sr.setMethod(doc.get("method").toString());
			}
			if (doc.get("user") != null) {
				sr.setUser(doc.get("user").toString());
			}
			if (doc.get("opTime") != null) {
				sr.setOpTime(doc.get("opTime").toString());
			}
			if (doc.get("path") != null) {
				sr.setPath(doc.get("path").toString());
			}
			if (doc.get("response") != null) {
				sr.setResponse(doc.get("response").toString());
			}
			if (doc.get("remoteIp") != null) {
				sr.setRemoteIp(doc.get("remoteIp").toString());
			}
			if (doc.get("subject") != null) {
				sr.setSubject(doc.get("subject").toString());
			}
			if (doc.get("tenant") != null) {
				sr.setTenant(doc.get("tenant").toString());
			}
			searchResults.add(sr);
		}

		Map<String, Object> data = new HashMap<>();

		Collections.sort(searchResults);
		data.put("scrollId", response.getScrollId());
		data.put("log", searchResults);

		return ActionReturnUtil.returnSuccessWithData(data);
	}

	/**
	 * 查询用户操作对应的模块列表.
	 *
	 * @param query
	 *            查询条件
	 * @return 模块列表
	 * @throws IOException
	 *             IO异常
	 */
	public static ActionReturnUtil searchFromIndexByUser(BoolQueryBuilder query) throws IOException {

		SearchResponse response = ESFactory.createES().prepareSearch(indexName).setTypes(type)
				.setSearchType(SearchType.QUERY_AND_FETCH).addFields("module").setQuery(query).setFrom(0).setSize(10000)
				.setExplain(true).get();

		List<String> searchResults = new ArrayList<String>();

		if (response.getHits().getHits().length > 0) {
			searchResults.add("全部");
		}
		Set<String> searchResults1 = new HashSet<String>();
		for (SearchHit hit : response.getHits().getHits()) {
			Set<Map.Entry<String, SearchHitField>> fieldEntry = hit.getFields().entrySet();
			for (Map.Entry<String, SearchHitField> entry : fieldEntry) {
				searchResults1.add(entry.getValue().getValue().toString());
			}
		}
		searchResults.addAll(searchResults1);
		return ActionReturnUtil.returnSuccessWithData(searchResults);
	}

	public static void createMapping(String indices, String mappingType) throws Exception {
		// 创建索引
		logger.debug("正在创建索引:" + indices);
		ESFactory.esClient.admin().indices().prepareCreate(indices).execute().actionGet();
		logger.debug("创建索引结束:" + indices);

		XContentBuilder builder = jsonBuilder().startObject().startObject("properties")
				.startObject("user").field("type", "string").endObject()
				.startObject("tenant").field("type", "string").endObject()
				.startObject("module").field("type", "string").field("index", "not_analyzed").endObject()
				.startObject("opFun").field("type", "string").endObject()
				.startObject("method").field("type", "string").field("index", "not_analyzed").endObject()
				.startObject("opTime").field("type", "date").field("format", "yyyy-MM-dd' 'HH:mm:ss").field("index", "not_analyzed").endObject()
				.startObject("requestParams").field("type", "string").endObject()
				.startObject("path").field("type", "string").endObject()
				.startObject("remoteIp").field("type", "string").endObject()
				.startObject("response").field("type", "string").endObject()
				.startObject("opStatus").field("type", "boolean").field("index", "not_analyzed").endObject()
				.startObject("subject").field("type", "string").endObject().endObject().endObject();

		// 创建mapping
		logger.debug("正在创建mapping:" + mappingType);
		PutMappingRequest mapping = Requests.putMappingRequest(indices).type(mappingType).source(builder);
		ESFactory.esClient.admin().indices().putMapping(mapping).actionGet();
		logger.debug("创建mapping结束:" + mappingType);
	}

	public static boolean isExistsType(String indexName) throws Exception {
		IndicesExistsResponse response = ESFactory.createES().admin().indices()
				.exists(new IndicesExistsRequest().indices(new String[] { indexName })).actionGet();
		return response.isExists();
	}
	
	public static ActionReturnUtil getTotalCounts(BoolQueryBuilder query) throws Exception {
		// 计算页数对应的数据行数，先查询出来总的记录个数，计算
		SearchResponse pageResponse = ESFactory.createES().prepareSearch(indexName).setTypes(type)
						.setSearchType(SearchType.QUERY_AND_FETCH).setQuery(query).setExplain(true)
						.get();
		        
		//总数
		long totalRecords = pageResponse.getHits().getTotalHits();
		Map<String, Object> data = new HashMap<>();
		data.put("total", totalRecords);
		return ActionReturnUtil.returnSuccessWithData(data);
	}

	@Value("#{propertiesReader['es.host']}")
	public void setHost(String host) {
		ESFactory.host = host;
	}

	@Value("#{propertiesReader['es.port']}")
	public void setPort(Integer port) {
		ESFactory.port = port;
	}

	@Value("#{propertiesReader['es.useropcluster']}")
	public void setClusterName(String clusterName) {
		ESFactory.clusterName = clusterName;
	}

	@Value("#{propertiesReader['es.version']}")
	public void setVersion(String version) {
		ESFactory.version = version;
	}

	@Value("#{propertiesReader['es.esType']}")
	public void setType(String type) {
		ESFactory.type = type;
	}

	@Value("#{propertiesReader['es.useropindex']}")
	public void setIndexName(String useropindex) {
		ESFactory.indexName = useropindex;
	}

	public static String getHost() {
		return host;
	}

	public static Integer getPort() {
		return port;
	}

	public static String getClusterName() {
		return clusterName;
	}

	public static String getVersion() {
		return version;
	}

	public static String getIndexName() {
		return indexName;
	}

	public static String getType() {
		return type;
	}

}