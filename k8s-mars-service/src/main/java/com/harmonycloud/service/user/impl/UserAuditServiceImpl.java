package com.harmonycloud.service.user.impl;


import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.dto.config.AuditRequestInfo;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.UserAuditSearch;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.service.user.UserAuditService;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;

import com.harmonycloud.common.Constant.CommonConstant;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by czm on 2017/3/29.
 */
@Service
public class UserAuditServiceImpl implements UserAuditService {
    private static Logger LOGGER = LoggerFactory.getLogger(UserAuditServiceImpl.class);
    @Autowired
    EsService esService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    private static TransportClient platformEsClient;

    private final static Integer ES_TTL = 1000;

    /**
     * 初始化操作审计日志es索引，如果没有则创建索引
     */
    @PostConstruct
    private void initAuditLogIndex(){
        try {
            platformEsClient = esService.getEsClient(clusterService.getPlatformCluster());
            if (esService.isExistIndex(Constant.ES_INDEX_AUDIT_LOG, clusterService.getPlatformCluster())) {
                return;
            }
            this.createIndexMapping(Constant.ES_INDEX_AUDIT_LOG, Constant.ES_INDEX_TYPE_AUDIT_LOG);
            LOGGER.info("操作审计ES index创建成功");
        }catch (Exception e){
            LOGGER.info("操作审计ES index创建失败",e);
        }
    }

    @Override
    public ActionReturnUtil serachByQuery(UserAuditSearch userAuditSearch) throws Exception {
        String scrollId = userAuditSearch.getScrollId();
        Integer pageSize = userAuditSearch.getSize();
        Integer pageNum = userAuditSearch.getPageNum();
        BoolQueryBuilder query = generateQuery(userAuditSearch);
        return this.searchFromIndex(query, scrollId, pageSize, pageNum);

    }


    /**
     * isAdmin 大于等于1 表示为管理员、否则为普通成员
     */

    public ActionReturnUtil serachByUserName(String username) throws Exception {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        //根据当前系统语言获取相应的字段
        String language = CommonConstant.DEFAULT_LANGUAGE_CHINESE;
        String sessionLanguage = String.valueOf(session.getAttribute("language"));
        if(org.apache.commons.lang3.StringUtils.isNotBlank(sessionLanguage) && !"null".equals(sessionLanguage)){
            language = sessionLanguage;
        }
        String module = CommonConstant.DEAULT_MODULE_CH;
        switch (language) {
            case CommonConstant.LANGUAGE_ENGLISH:
                module = CommonConstant.DEAULT_MODULE_EN;
        }
        return this.searchFromIndexByUser(query, module);

    }

    public ActionReturnUtil serachByModule(String username, String module, boolean isAdmin) throws Exception {

        BoolQueryBuilder query = QueryBuilders.boolQuery();

        /*if (!isAdmin) {
            query.must(QueryBuilders.termQuery("user", username));
        }*/

        if (module != null && !module.equals("")) {
            query.must(QueryBuilders.termQuery("moduleChDesc", module));
        }

//        ESFactory esFactory = new ESFactory();
        ActionReturnUtil lists = this.searchFromIndex(query, null,1000,0);

        return lists;
    }

    public ActionReturnUtil serachAuditsByUser(String username, boolean isAdmin) throws Exception {
        BoolQueryBuilder query = QueryBuilders.boolQuery();

        if (!isAdmin) {
            query.must(QueryBuilders.termQuery("user", username));
        }

//        ESFactory esFactory = new ESFactory();
        return this.searchFromIndex(query, null,10000,0);
    }


	@Override
	public ActionReturnUtil getAuditCount(UserAuditSearch userAuditSearch) throws Exception {
		BoolQueryBuilder query = generateQuery(userAuditSearch);
		return this.getTotalCounts(query);
	}

    /**
     * 插入数据到ES.
     *
     * @param auditRequestInfo
     *            操作记录
     * @return 插入是否成功
     * @throws IOException
     *             IO异常
     */
    public ActionReturnUtil insertToEsIndex(AuditRequestInfo auditRequestInfo) throws Exception {
        LOGGER.debug("插入ElasticSearch:");
        LOGGER.debug("即将插入es，url：{},remoteIP:{}",auditRequestInfo.getUrl(), auditRequestInfo.getRemoteIp());
        IndexResponse indexResponse = platformEsClient
                .prepareIndex(Constant.ES_INDEX_AUDIT_LOG, Constant.ES_INDEX_TYPE_AUDIT_LOG, String.valueOf(new Date().getTime()))
                .setSource(                 // 这里可以直接用json字符串
                        XContentFactory.jsonBuilder().startObject().field("user", auditRequestInfo.getUser())
                                .field("tenant", auditRequestInfo.getTenant()).field("project", auditRequestInfo.getProject())
                                .field("moduleChDesc", auditRequestInfo.getModuleChDesc()).field("moduleEnDesc", auditRequestInfo.getModuleEnDesc())
                                .field("actionChDesc", auditRequestInfo.getActionChDesc()).field("actionEnDesc", auditRequestInfo.getActionEnDesc())
                                .field("method", auditRequestInfo.getMethod()).field("actionTime", auditRequestInfo.getActionTime())
                                .field("requestParams", auditRequestInfo.getRequestParams()).field("remoteIp", auditRequestInfo.getRemoteIp())
                                .field("response", auditRequestInfo.getResponse()).field("url", auditRequestInfo.getUrl())
                                .field("status", auditRequestInfo.getStatus()).field("subject", auditRequestInfo.getSubject())
                                .endObject()).setTTL(ES_TTL).get();
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
    public ActionReturnUtil searchFromIndex(BoolQueryBuilder query, String scrollId, int pageSize,
                                            int currentPage) throws Exception {
        //根据当前系统语言获取相应的字段
        String language = CommonConstant.DEFAULT_LANGUAGE_CHINESE;
        String sessionLanguage = String.valueOf(session.getAttribute("language"));
        if(org.apache.commons.lang3.StringUtils.isNotBlank(sessionLanguage) && !"null".equals(sessionLanguage)){
            language = sessionLanguage;
        }
        SearchResponse response;
        response = platformEsClient.prepareSearch(Constant.ES_INDEX_AUDIT_LOG).setTypes(Constant.ES_INDEX_TYPE_AUDIT_LOG)
                // .setScroll(new TimeValue(60000))
                .setQuery(query).addSort("actionTime", SortOrder.DESC).setFrom((currentPage - 1) * pageSize).setSize(pageSize).setExplain(true) // 这里需要修改整整分页之后
                .get();
        // scrollid的分页,setSize中的5为分片数
		/*if (StringUtils.isBlank(scrollId)) {
			response = ESFactory.createES().prepareSearch(Constant.ES_INDEX_AUDIT_LOG).setTypes(Constant.ES_INDEX_TYPE_AUDIT_LOG)
					.setSearchType(SearchType.QUERY_AND_FETCH).setScroll(new TimeValue(ES_SCROLL_TIMEOUT)).setQuery(query)
					.addSort("opTime", SortOrder.DESC).setFrom(0).setSize(pageSize/CommonConstant.ES_SHARDS).setExplain(true) // 这里需要修改整整分页之后
					.get();
			scrollId = response.getScrollId();
		} else {
			response = ESFactory.createES().prepareSearchScroll(scrollId).setScroll(new TimeValue(ES_SCROLL_TIMEOUT)).execute()
					.actionGet();
		}*/

        Iterator<SearchHit> it = response.getHits().iterator();
        List<AuditRequestInfo> searchResults = new ArrayList<>();

        while (it.hasNext()) {
            SearchHit sh = it.next();
            Map<String, Object> doc = sh.getSource();
            AuditRequestInfo sr = new AuditRequestInfo();
            if (doc.get("user") != null) {
                sr.setUser(String.valueOf(doc.get("user")));
            }
            if (doc.get("tenant") != null) {
                sr.setTenant(String.valueOf(doc.get("tenant")));
            }
            if (doc.get("project") != null) {
                sr.setSubject(String.valueOf(doc.get("project")));
            }
            if (doc.get("subject") != null) {
                sr.setSubject(String.valueOf(doc.get("subject")));
            }
            if (doc.get("url") != null) {
                sr.setUrl(String.valueOf(doc.get("url")));
            }
            if (doc.get("requestParams") != null) {
                sr.setRequestParams(String.valueOf(doc.get("requestParams")));
            }
            switch (language) {
                case CommonConstant.LANGUAGE_CHINESE:
                    if (doc.get("moduleChDesc") != null) {
                        sr.setModuleChDesc(String.valueOf(doc.get("moduleChDesc")));
                    }
                    if (doc.get("actionChDesc") != null) {
                        sr.setActionChDesc(String.valueOf(doc.get("actionChDesc")));
                    }
                    break;
                case CommonConstant.LANGUAGE_ENGLISH:
                    if (doc.get("moduleEnDesc") != null) {
                        sr.setModuleChDesc(String.valueOf(doc.get("moduleEnDesc")));
                    }
                    if (doc.get("actionEnDesc") != null) {
                        sr.setActionChDesc(String.valueOf(doc.get("actionEnDesc")));
                    }
                    break;
            }
            if (doc.get("status") != null) {
                sr.setStatus(String.valueOf(doc.get("status")));
            }
            if (doc.get("method") != null) {
                sr.setMethod(String.valueOf(doc.get("method")));
            }

            if (doc.get("actionTime") != null) {
                sr.setActionTime(String.valueOf(doc.get("actionTime")));
            }

            if (doc.get("response") != null) {
                sr.setResponse(String.valueOf(doc.get("response")));
            }
            if (doc.get("remoteIp") != null) {
                sr.setRemoteIp(String.valueOf(doc.get("remoteIp")));
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
    public ActionReturnUtil searchFromIndexByUser(BoolQueryBuilder query, String module) throws Exception {
        SearchResponse response = platformEsClient.prepareSearch(Constant.ES_INDEX_AUDIT_LOG).setTypes(Constant.ES_INDEX_TYPE_AUDIT_LOG)
                .setSearchType(SearchType.QUERY_AND_FETCH).addFields(module).setQuery(query).setFrom(0).setSize(10000)
                .setExplain(true).get();

        List<String> searchResults = new ArrayList<String>();

        if (response.getHits().getHits().length > 0) {
            searchResults.add("全部模块");
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
    
    public ActionReturnUtil getTotalCounts(BoolQueryBuilder query) throws Exception {
        // 计算页数对应的数据行数，先查询出来总的记录个数，计算
        SearchResponse pageResponse = platformEsClient.prepareSearch(Constant.ES_INDEX_AUDIT_LOG).setTypes(Constant.ES_INDEX_TYPE_AUDIT_LOG)
                .setSearchType(SearchType.QUERY_AND_FETCH).setQuery(query).setExplain(true)
                .get();

        //总数
        long totalRecords = pageResponse.getHits().getTotalHits();
        Map<String, Object> data = new HashMap<>();
        data.put("total", totalRecords);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    private void createIndexMapping(String indices, String mappingType) throws Exception {
        // 创建索引
        LOGGER.debug("正在创建索引:" + indices);
        platformEsClient.admin().indices().prepareCreate(indices).execute().actionGet();
        LOGGER.debug("创建索引结束:" + indices);

        XContentBuilder builder = jsonBuilder().startObject().startObject("properties")
                .startObject("user").field("type", "string").endObject()
                .startObject("tenant").field("type", "string").endObject()
                .startObject("project").field("type", "string").endObject()
                .startObject("moduleChDesc").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("moduleEnDesc").field("type", "string").endObject()
                .startObject("actionChDesc").field("type", "string").endObject()
                .startObject("actionEnDesc").field("type", "string").endObject()
                .startObject("method").field("type", "string").field("index", "not_analyzed").endObject()
                .startObject("actionTime").field("type", "date").field("format", "yyyy-MM-dd' 'HH:mm:ss").field("index", "not_analyzed").endObject()
                .startObject("requestParams").field("type", "string").endObject()
                .startObject("url").field("type", "string").endObject()
                .startObject("remoteIp").field("type", "string").endObject()
                .startObject("response").field("type", "string").endObject()
                .startObject("status").field("type", "boolean").field("index", "not_analyzed").endObject()
                .startObject("subject").field("type", "string").endObject().endObject().endObject();

        // 创建mapping
        LOGGER.debug("正在创建mapping:" + mappingType);
        PutMappingRequest mapping = Requests.putMappingRequest(indices).type(mappingType).source(builder);
        platformEsClient.admin().indices().putMapping(mapping).actionGet();
        LOGGER.debug("创建mapping结束:" + mappingType);
    }
	
	private BoolQueryBuilder getQueryBuildersByKeywords(String keyWords) throws Exception {
		//判断是不是中文
    	String regex = "[\u4e00-\u9fa5]";
    	Pattern pattern = Pattern.compile(regex);
    	Matcher matcher = pattern.matcher(keyWords);
    	BoolQueryBuilder queryCh = QueryBuilders.boolQuery();
        if (matcher.find()) {
            String decodeWords = URLDecoder.decode(keyWords, "UTF-8");
        	queryCh.should(QueryBuilders.matchPhraseQuery("actionChDesc", decodeWords));
        	queryCh.should(QueryBuilders.matchPhraseQuery("user", decodeWords));
        	queryCh.should(QueryBuilders.matchPhraseQuery("tenant", decodeWords));
        	queryCh.should(QueryBuilders.matchPhraseQuery("moduleChDesc", decodeWords));
        	queryCh.should(QueryBuilders.matchPhraseQuery("subject", decodeWords));
        	return queryCh;
        }else{
        	//判断是否有斜杠
        	Pattern slashPattern = Pattern.compile(".*/");
        	Matcher slashMatcher = slashPattern.matcher(keyWords);
        	if (slashMatcher.find()) {
        		return queryCh.must(QueryBuilders.matchPhraseQuery("url", keyWords));
        	} else {
        		return queryCh.must(QueryBuilders.queryStringQuery("*"+keyWords+"*").field("user")
            			.field("actionChDesc").field("tenant").field("moduleChDesc").field("subject").field("remoteIp"));
        	}
        }  
	}

	private BoolQueryBuilder generateQuery(UserAuditSearch userAuditSearch) throws Exception {
        String startTime = userAuditSearch.getStartTime();
        String endTime = userAuditSearch.getEndTime();
        String moduleName = userAuditSearch.getModuleName();
        String keyWords = userAuditSearch.getKeyWords();
        String user = userAuditSearch.getUser();
        String scrollId = userAuditSearch.getScrollId();
        List<String> userLists = userAuditSearch.getUserList();
        Integer pageSize = userAuditSearch.getSize();
        Integer pageNum = userAuditSearch.getPageNum();
        String tenantName = userAuditSearch.getTenantName();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime)) {
            LOGGER.info("操作审计开始时间：{}",startTime);
            LOGGER.info("操作审计结束时间：{}",endTime);
            if(DateUtil.timeFormat.parse(startTime).after(DateUtil.timeFormat.parse(endTime))){
                throw new MarsRuntimeException(ErrorCodeMessage.START_DATE_AFTER_END);
            }
            query.must(QueryBuilders.rangeQuery("actionTime").from(startTime).to(endTime));
        }

        if (StringUtils.isNotBlank(moduleName)&&!"all".equals(moduleName)) {
            String regex = "[\u4e00-\u9fa5]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(moduleName);
            if (matcher.find()) {
                moduleName = URLDecoder.decode(moduleName, "UTF-8");
                query.must(QueryBuilders.matchQuery("moduleChDesc", moduleName));
            } else {
                query.must(QueryBuilders.matchQuery("moduleEnDesc", moduleName));
            }
        }

        if (StringUtils.isNotBlank(keyWords)) {
            query.must(getQueryBuildersByKeywords(keyWords));
            //query.must(QueryBuilders.multiMatchQuery(keyWords,"user","opFun", "tenant", "module", "path", "subject", "remoteIp"));
        }

        if (StringUtils.isNotBlank(tenantName) && !"all".equals(tenantName)) {
            tenantName = URLDecoder.decode(tenantName, "UTF-8");
            query.must(QueryBuilders.matchPhraseQuery("tenant", tenantName));
        }

        if (userLists != null && userLists.size() > 0) {
            query.must(QueryBuilders.termsQuery("user", userLists));
        }
        return query;
    }

}
