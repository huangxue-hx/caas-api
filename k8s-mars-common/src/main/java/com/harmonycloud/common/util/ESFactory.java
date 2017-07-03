package com.harmonycloud.common.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.harmonycloud.common.util.date.DateUtil;
import net.sf.json.util.JSONBuilder;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by czm on 2017/3/28.
 */
@Component
public class ESFactory {
    private static Logger logger = LoggerFactory.getLogger(ESFactory.class);

    public static TransportClient esClient;
    private static String host;
    private static Integer port;
    private static String version;
    private static String indexName;
    private static String type;
    private static String clusterName;
    public static ExecutorService executor = Executors.newFixedThreadPool(20);

    public static TransportClient createES() {
        if (esClient == null) {
            try {
                String[] hosts = host.split(",");
                Settings settings = Settings.settingsBuilder().put("cluster.name", clusterName).build();
                TransportClient client = TransportClient.builder().settings(settings).build();
                for (String host : hosts) {
                    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
                }
                ESFactory.esClient = client;

                if(!isExistsType(indexName)){
                    createMapping(indexName,type);
                    if(isExistsType(indexName)){
                         logger.debug("操作审计ES创建成功");
                    }else {
                        logger.debug("操作审计ES创建失败");
                    }
                }

                return esClient;
            } catch (Exception e) {
                logger.error("创建ElasticSearch Client 失败", e);
                logger.debug("创建ElasticSearch Client 失败", e);
                e.printStackTrace();
            }

        }
        return esClient;
    }


    /**
     * 插入数据到ES.
     *
     * @param sr 操作记录
     * @return 插入是否成功
     * @throws IOException IO异常
     */
    public static ActionReturnUtil insertToIndexBySR(SearchResult sr) throws Exception {
        boolean flag = false;
        //索引不存在创建索引
        if(!isExistsType(indexName)){
            createMapping(indexName,type);
            if(isExistsType(indexName)){
                logger.debug("操作审计ES创建成功");

            }
        }else {
            flag =true;
        }

        if (sr != null && flag) {

//            String tenant = sr.getTenant();
            String opType = sr.getOpType();
            String user = sr.getUser();
            String module = sr.getModule();
            String opTime = sr.getOpTime();
            String opFun = sr.getOpFun();
            String opDetails = sr.getOpDetails();
            String opStatus = sr.getOpStatus();

            IndexResponse indexResponse = ESFactory.createES().prepareIndex(indexName, type, String.valueOf(new Date().getTime()))
                    .setSource(// 这里可以直接用json字符串
                            XContentFactory.jsonBuilder().startObject().field("user", user)
                                    .field("tenant", "tmp")
                                    .field("module", module).field("opFun", opFun)
                                    .field("opType", opType).field("opTime", opTime).field("opDetails", opDetails).field("opStatus", opStatus).endObject())
                    .setTTL(1000).get();
            return ActionReturnUtil.returnSuccess();
        }


        return ActionReturnUtil.returnError();
    }


    /**
     * 根据查询条件从ES中查询结果.
     *
     * @param query 查询条件
     * @return 查询结果列表
     * @throws IOException IO异常
     */
    public static ActionReturnUtil searchFromIndex(BoolQueryBuilder query, String scrollId, int pageSize, int currentPage) throws IOException {
        //计算页数对应的数据行数，先查询出来总的记录个数，计算
        SearchResponse pageResponse = ESFactory.createES().prepareSearch(indexName)
                .setTypes(type)
                .setSearchType(SearchType.QUERY_AND_FETCH)
                .setQuery(query)
                .setFrom(0).setSize(10000).setExplain(true)
                .get();

//        long totalRecords = pageResponse.getHits().getTotalHits();
//
//        long fromLocation = pageSize * (currentPage-1);
//
//        if (pageSize * currentPage > totalRecords) {
//            fromLocation = totalRecords;
//        }


        //暂时去掉scrollid的分页
        SearchResponse response;
//        if(StringUtils.isBlank(scrollId)){
        response = ESFactory.createES().prepareSearch(indexName)
                .setTypes(type)
                .setSearchType(SearchType.QUERY_AND_FETCH)
//                    .setScroll(new TimeValue(60000))
                .setQuery(query)
                .addSort("opTime", SortOrder.DESC)
                .setFrom(0).setSize(100).setExplain(true) //这里需要修改整整分页之后
                .get();
//            scrollId = response.getScrollId();
//        }else {
//            response = ESFactory.createES().prepareSearchScroll(scrollId).setScroll(new TimeValue(60000)).execute().actionGet();
//        }

        Iterator<SearchHit> it = response.getHits().iterator();
        List<SearchResult> searchResults = new ArrayList<>();

        while (it.hasNext()) {
            SearchHit sh = it.next();
            Map<String, Object> doc = sh.getSource();
            SearchResult sr = new SearchResult();

            if (doc.get("module") != null) {
                sr.setModule(doc.get("module").toString());
            }
            if (doc.get("opDetails") != null) {
                sr.setOpDetails(doc.get("opDetails").toString());
            }
            if (doc.get("opFun") != null) {
                sr.setOpFun(doc.get("opFun").toString());
            }
            if (doc.get("opStatus") != null) {
                sr.setOpStatus(doc.get("opStatus").toString());
            }
            if (doc.get("opType") != null) {
                sr.setOpType(doc.get("opType").toString());
            }
            if (doc.get("user") != null) {
                sr.setUser(doc.get("user").toString());
            }
            if (doc.get("user") != null) {
                sr.setOpTime(doc.get("opTime").toString());
            }
            searchResults.add(sr);
        }

        Map<String, Object> data = new HashMap<>();

        Collections.sort(searchResults);

//        data.put("scrollId",scrollId);
        data.put("log", searchResults);
//        data.put("totalPages", (totalRecords + pageSize-1) / pageSize);

        return ActionReturnUtil.returnSuccessWithData(data);
    }

    /**
     * 查询用户操作对应的模块列表.
     *
     * @param query 查询条件
     * @return 模块列表
     * @throws IOException IO异常
     */
    public static ActionReturnUtil searchFromIndexByUser(BoolQueryBuilder query) throws IOException {

        SearchResponse response = ESFactory.createES().prepareSearch(indexName)
                .setTypes(type)
                .setSearchType(SearchType.QUERY_AND_FETCH)
                .addFields("module")
                .setQuery(query)
                .setFrom(0).setSize(10000).setExplain(true)
                .get();


        Set<String> searchResults = new HashSet<String>();

        for (SearchHit hit : response.getHits().getHits()) {
            Set<Map.Entry<String, SearchHitField>> fieldEntry = hit.getFields().entrySet();
            for (Map.Entry<String, SearchHitField> entry : fieldEntry) {
                searchResults.add(entry.getValue().getValue().toString());
            }
        }
        if(response.getHits().getHits().length>0){
            searchResults.add("all");
        }


        return ActionReturnUtil.returnSuccessWithData(searchResults);
    }

    public static void createMapping(String indices, String mappingType) throws Exception {
        //创建索引
        logger.debug("正在创建索引:"+indices);
        ESFactory.esClient.admin().indices().prepareCreate(indices).execute().actionGet();
        logger.debug("创建索引结束:"+indices);

        XContentBuilder builder = jsonBuilder()
                .startObject()
                .startObject("properties")
                .startObject("user").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("tenant").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("module").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("opFun").field("type", "string").endObject()
                .startObject("opType").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("opTime").field("type", "date").field("format","yyyy-MM-dd' 'HH:mm:ss").field("index", "not_analyzed").endObject()
                .startObject("opDetails").field("type", "string").endObject()
                .startObject("opStatus").field("type", "boolean").field("index", "not_analyzed").endObject()
                .endObject()
                .endObject();

        //创建mapping
        logger.debug("正在创建mapping:"+mappingType);
        PutMappingRequest mapping = Requests.putMappingRequest(indices).type(mappingType).source(builder);
        ESFactory.esClient.admin().indices().putMapping(mapping).actionGet();
        logger.debug("创建mapping结束:"+mappingType);
    }

    public static boolean isExistsType(String indexName){
        IndicesExistsResponse  response =
                ESFactory.createES().admin().indices().exists(
                        new IndicesExistsRequest().indices(new String[]{indexName})).actionGet();
        return response.isExists();
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
    public  void setType(String type) {
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
