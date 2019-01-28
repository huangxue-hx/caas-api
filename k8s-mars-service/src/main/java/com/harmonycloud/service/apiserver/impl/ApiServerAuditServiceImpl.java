package com.harmonycloud.service.apiserver.impl;

import com.google.common.collect.Lists;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.tenant.bean.NamespaceLocal;
import com.harmonycloud.dto.apiserver.ApiServerAuditSearchDto;
import com.harmonycloud.dto.apiserver.ClusterNamespaceDto;
import com.harmonycloud.dto.config.ApiServerAuditInfo;
import com.harmonycloud.k8s.bean.Namespace;
import com.harmonycloud.k8s.bean.ObjectMeta;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.service.NamespaceService;
import com.harmonycloud.service.apiserver.ApiServerAuditService;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cache.ClusterCacheManager;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.BaseAuditService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liangli
 */
@Service
public class ApiServerAuditServiceImpl extends BaseAuditService implements ApiServerAuditService {

    /**
     * apiserver audit log index
     */
    private static final String API_SERVER_AUDIT_INDEX = "logstash";

    /**
     * apiserver audit log type
     */
    private static final String API_SERVER_AUDIT_TYPE = "fluentd";
//    private static final String GROUP_BY_NAMESPACE = "group_by_namespace";
//    private static final String GROUP_BY_VERB = "group_by_verb";

    /**
     * 按照url分组统计
     */
    private static final String GROUP_BY_URL = "group_by_url";

    /**
     * apiserver记录该条日志的时间戳
     */
    private static final String ACTION_TIME = "timestamp";

    /**
     * 请求url
     */
    private static final String REQUEST_URI = "requestURI";

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ClusterCacheManager clusterCacheManager;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private EsService esService;

    /**
     * 前台查询数据
     *
     * @param search 前台查询数据对象
     * @return 查询到的数据
     * @throws MarsRuntimeException
     * @throws UnsupportedEncodingException
     * @throws ParseException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public ActionReturnUtil searchByQuery(ApiServerAuditSearchDto search) throws Exception {

        //如果clusterId为空,则默认查平台所在的集群，前台只显示pageSize条，多查无意义
        BoolQueryBuilder query = generateQuery(search);
        String startTime = search.getStartTime();
        String endTime = search.getEndTime();
        if (StringUtils.isBlank(search.getClusterId())) {
            //根据时间范围判断落在哪几个索引
            List<String> indexList = getExistIndexNames(platformEsClient, startTime, endTime);
            return this.searchFromIndex(platformEsClient, query, search, indexList, clusterCacheManager.getPlatformCluster().getAliasName());
        } else {
            Cluster cluster = clusterCacheManager.getCluster(search.getClusterId());

            if (cluster == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            TransportClient esClient = esService.getEsClient(cluster);
            //根据时间范围判断落在哪几个索引
            List<String> indexList = getExistIndexNames(esClient, startTime, endTime);
            return this.searchFromIndex(esClient, query, search, indexList, cluster.getAliasName());
        }
    }

    /**
     * 根据查询条件从ES中查询结果.
     *
     * @param query     查询条件
     * @param search    查询参数
     * @param indexList 索引集合
     * @return 查询到的日志数据
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public ActionReturnUtil searchFromIndex(TransportClient esClient, BoolQueryBuilder query, ApiServerAuditSearchDto search, List<String> indexList, String clusterAliasName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

        if (CollectionUtils.isEmpty(indexList)) {
            return ActionReturnUtil.returnSuccessWithData(Collections.EMPTY_LIST);
        }

        int pageSize = search.getSize();
        //.size(0)表示聚合返回所有，默认为10条，0表示返回所有
        TermsBuilder requestURITerms = AggregationBuilders.terms(GROUP_BY_URL).field(REQUEST_URI).size(0);
        SearchRequestBuilder searchRequestBuilder = multiIndexSearch(esClient, indexList);

        SearchResponse response;
        response = searchRequestBuilder
                .setTypes(API_SERVER_AUDIT_TYPE)
                // .setScroll(new TimeValue(60000))
                .setQuery(query).addSort(ACTION_TIME, SortOrder.DESC)
                .addAggregation(requestURITerms)
                .setFrom((search.getPageNum() - CommonConstant.NUM_ONE) * pageSize).setSize(pageSize) // 这里需要修改整整分页之后
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

        Aggregations aggregations = response.getAggregations();
        Terms urlTerm = aggregations.get(GROUP_BY_URL);

        List<Terms.Bucket> urlBuckets = urlTerm.getBuckets();

        //最多只会返回size条
        List<ApiServerAuditInfo> auditInfos = Lists.newArrayListWithCapacity(pageSize);
        Iterator<SearchHit> it = response.getHits().iterator();
        while (it.hasNext()) {
            ApiServerAuditInfo infos = new ApiServerAuditInfo();
            SearchHit hit = it.next();

            Map<String, Object> source = hit.getSource();

            infos.setActionTime(DateUtil.utc2Local((String) source.get(ACTION_TIME), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue()));
            infos.setRequestUrl((String) source.get(REQUEST_URI));
            infos.setVerb((String) source.get("verb"));

            Map<String, String> objectRef = (Map<String, String>) source.get("objectRef");

            infos.setActionObject(objectRef.get("resource"));
            infos.setActionObjectName(objectRef.get("name"));
            infos.setNamespace(objectRef.get("namespace"));

            //"responseStatus": {"metadata": { }, "code": 200}
            Map<String, Object> responseStatus = (Map<String, Object>) source.get("responseStatus");
            infos.setActionResult((Integer) responseStatus.get("code"));


            //"requestReceivedTimestamp": "2019-01-14T02:49:31.523234Z",
            //"stageTimestamp": "2019-01-14T02:49:32.228184Z",
            long spendTime = convertSpendTime((String) source.get("stageTimestamp"), (String) source.get("requestReceivedTimestamp"));
            //返回毫秒
            infos.setRequestSpendTime(spendTime / 1000000.0 + "");

            for (Terms.Bucket urlBacket : urlBuckets) {
                if (StringUtils.equals(urlBacket.getKeyAsString(), infos.getRequestUrl())) {
                    infos.setCurrentUrlCount(urlBacket.getDocCount());
                    break;
                }
            }
            infos.setClusterAliasName(clusterAliasName);
            auditInfos.add(infos);
        }

        return ActionReturnUtil.returnSuccessWithData(auditInfos);
    }


    /**
     * 查询集群审计日志
     *
     * @param search 前台查询数据
     * @return 查到的数据
     * @throws MarsRuntimeException
     * @throws ParseException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    public ActionReturnUtil getAuditCount(ApiServerAuditSearchDto search) throws Exception {
        long totalRecords = 0;
        BoolQueryBuilder query = generateQuery(search);
        //ClusterId为空则查询所有集群
        if (StringUtils.isBlank(search.getClusterId())) {
            List<Cluster> clusters = clusterCacheManager.listCluster();

            if (CollectionUtils.isEmpty(clusters)) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            for (Cluster cluster : clusters) {
                TransportClient esClient = esService.getEsClient(cluster);
                List<String> indexList = getExistIndexNames(esClient, search.getStartTime(), search.getEndTime());
                totalRecords += getTotalCounts(esClient, query, indexList);
            }
        } else {
            Cluster cluster = clusterCacheManager.getCluster(search.getClusterId());

            //找不到集群
            if (null == cluster) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            TransportClient esClient = esService.getEsClient(cluster);
            List<String> indexList = getExistIndexNames(esClient, search.getStartTime(), search.getEndTime());
            totalRecords = getTotalCounts(esClient, query, indexList);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("total", totalRecords);

        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public ActionReturnUtil getAuditLogsNamespace() throws MarsRuntimeException {

        List<Cluster> clusters = clusterCacheManager.listCluster();

        if (CollectionUtils.isEmpty(clusters)) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        //收集clusterIds
        List<String> clusterIds = clusters.stream().map(Cluster::getId).collect(Collectors.toList());
        //获取所有cluster下的namespace信息,避免循环多次查询
        List<NamespaceLocal> namespaceLocal = namespaceLocalService.getNamespaceByClsterIds(clusterIds);

        List<ClusterNamespaceDto> clusterNamespaceDtos = Lists.newArrayListWithCapacity(clusters.size());
        for (Cluster cluster : clusters) {
            ClusterNamespaceDto clusterNamespaceDto = new ClusterNamespaceDto();
            //获取集群下的所有namespace资源对象
            List<Namespace> k8sNamespace = namespaceService.list(cluster);
            //只收集namespace的名称
            List<String> k8sNamespaceNames = k8sNamespace.stream().map(Namespace::getMetadata).map(ObjectMeta::getName).collect(Collectors.toList());

            //最终返回给前台的是数据库中查到的分区别名+k8s自带的namespace(kube-system、kube等)
            List<String> aliasNames = Lists.newArrayListWithCapacity(k8sNamespaceNames.size());
            for (NamespaceLocal nsLocal : namespaceLocal) {

                //只处理当前集群的
                if (!StringUtils.equals(nsLocal.getClusterId(), cluster.getId())) {
                    continue;
                }

                //属于当前集群且集群中查询到的name和数据库查到的相等,则需要获取到用户能看懂的别名--Alias_name
                if (k8sNamespaceNames.contains(nsLocal.getNamespaceName())) {
                    aliasNames.add(nsLocal.getAliasName());
                    //获取到别名后，移除k8s中查询的集合中该namespace name
                    k8sNamespaceNames.remove(nsLocal.getNamespaceName());
                }
            }

            //最后将别名集合和其他无别名集合合并
            aliasNames.addAll(k8sNamespaceNames);

            clusterNamespaceDto.setId(cluster.getId());
            clusterNamespaceDto.setAliasName(cluster.getAliasName());
            clusterNamespaceDto.setDataCenter(cluster.getDataCenter());
            clusterNamespaceDto.setName(cluster.getName());
            clusterNamespaceDto.setNamespaces(aliasNames);
            clusterNamespaceDtos.add(clusterNamespaceDto);
        }

        return ActionReturnUtil.returnSuccessWithData(clusterNamespaceDtos);
    }


    /**
     * 生成查询条件
     *
     * @param search 前台查询数据
     * @return 生成的查询条件
     * @throws ParseException
     */
    private BoolQueryBuilder generateQuery(ApiServerAuditSearchDto search) throws ParseException {
        String startTime = search.getStartTime();
        String endTime = search.getEndTime();
        String verbName = search.getVerbName();
        String keyWords = search.getKeyWords();
        String namespace = search.getNamespace();
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            if (DateUtil.UTC_FORMAT.parse(startTime).after(DateUtil.UTC_FORMAT.parse(endTime))) {
                throw new MarsRuntimeException(ErrorCodeMessage.START_DATE_AFTER_END);
            }
            query.must(QueryBuilders.rangeQuery(ACTION_TIME).from(startTime).to(endTime));
        }

        //查询指定了namespace,用term查询
        if (StringUtils.isNotBlank(namespace) && !"all".equals(namespace)) {
            query.must(QueryBuilders.termQuery("objectRef.namespace", namespace));
        }

        if (StringUtils.isNotBlank(keyWords)) {
            query.must(QueryBuilders.queryStringQuery("*" + keyWords + "*").field(REQUEST_URI)
                    .field("objectRef.resource").field("objectRef.namespace").field("objectRef.name").field("verb"));
        }

        if (StringUtils.isNotBlank(verbName) && !"all".equals(verbName)) {
            query.must(QueryBuilders.termQuery("verb", verbName));
        }

        query.must(QueryBuilders.termQuery("tag", "kubernetes"));
        return query;
    }

    /**
     * 符合条件的数量
     *
     * @param query     查询条件
     * @param indexList 索引集合
     * @return 符合条件的数量
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public long getTotalCounts(TransportClient esClient, BoolQueryBuilder query, List<String> indexList) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        SearchRequestBuilder searchRequestBuilder = multiIndexSearch(esClient, indexList);
        // 计算页数对应的数据行数，先查询出来总的记录个数，计算
        SearchResponse pageResponse = searchRequestBuilder
                .setTypes(API_SERVER_AUDIT_TYPE)
                .setSearchType(SearchType.QUERY_AND_FETCH).setQuery(query).setExplain(true)
                .get();

        //总数
        return pageResponse.getHits().getTotalHits();
    }

    private String generateIndexName() {
        Date now = DateUtil.getCurrentUtcTime();
        String date = DateUtil.DateToString(now, DateStyle.YYYYMMDD_DOT);
        //logstash-2019.01.09
        String indexName = API_SERVER_AUDIT_INDEX + CommonConstant.LINE + date;
        return indexName;
    }

    /**
     * 符合时间段的索引集合
     *
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @return 符合时间段的索引集合
     * @throws MarsRuntimeException
     */
    private List<String> getExistIndexNames(TransportClient esClient, String startTime, String endTime) throws MarsRuntimeException {
        List<String> indexNameList = new ArrayList<>();
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
            Date startDate = DateUtil.StringToDate(startTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
            Date endDate = DateUtil.StringToDate(endTime, DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z);
            int startYear = DateUtil.getYear(startDate);
            int startMonth = DateUtil.getMonth(startDate) + CommonConstant.NUM_ONE;
            int startDay = DateUtil.getDay(startDate);
            int endYear = DateUtil.getYear(endDate);
            int endMonth = DateUtil.getMonth(endDate) + CommonConstant.NUM_ONE;
            int endDay = DateUtil.getDay(endDate);
            int year = endYear - startYear;
            //开始与结束时同一年
            if (year == 0) {
                //同一个月
                if (endMonth - startMonth == 0) {
                    for (int i = startDay; i <= endDay; i++) {
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + endYear + CommonConstant.DOT + String.format("%02d", startMonth) + CommonConstant.DOT + String.format("%02d", i));
                    }
                } else {
                    //开始时间月
                    for (int i = startDay; i <= CommonConstant.NUM_THRITY_ONE; i++) {
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + endYear + CommonConstant.DOT + String.format("%02d", startMonth) + CommonConstant.DOT + String.format("%02d", i));
                    }
                    //结束月
                    for (int i = CommonConstant.NUM_ONE; i <= endDay; i++) {
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + endYear + CommonConstant.DOT + String.format("%02d", endDay) + CommonConstant.DOT + String.format("%02d", i));
                    }
                }
            } else {//不同一年
                //开始年
                for (int i = startMonth; i <= CommonConstant.NUM_TWELVE; i++) {
                    for (int j = startDay; j <= CommonConstant.NUM_THRITY_ONE; j++) {
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + startYear + CommonConstant.DOT + String.format("%02d", i) + CommonConstant.DOT + String.format("%02d", j));
                    }
                }
                //结束年
                for (int i = CommonConstant.NUM_ONE; i < endMonth; i++) {
                    for (int m = CommonConstant.NUM_ONE; m <= endDay; m++) {
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + endYear + CommonConstant.DOT + String.format("%02d", i) + CommonConstant.DOT + String.format("%02d", m));
                    }
                }
            }
        }
        indexNameList = CollectionUtils.isNotEmpty(indexNameList) ? indexNameList : Arrays.asList(generateIndexName());
        //取得已存在的索引
        GetIndexResponse indexResponse = esClient.admin().indices().prepareGetIndex().execute().actionGet();
        String[] indices = indexResponse.getIndices();
        indexNameList.retainAll(Arrays.asList(indices));
        return indexNameList;
    }

    /**
     * 计算花费的时间，SimpleDateFormat转换出来的有问题，故转换为纳秒计算,可以不关注时和分级别
     * @param stage stageTime 2019-01-14T02:49:32.228184Z
     * @param request requestTime 2019-01-14T02:49:31.523234Z
     * @return 花费的时间
     */
    private static long convertSpendTime(String stage, String request) {
        String formatIn = DateStyle.YYYY_MM_DD_T_HH_MM_SS_SSSSSS_Z.getValue();
        LocalDateTime stageTime = LocalDateTime.parse(stage, DateTimeFormatter.ofPattern(formatIn));
        LocalDateTime requestTime = LocalDateTime.parse(request, DateTimeFormatter.ofPattern(formatIn));
        return (stageTime.getHour() - requestTime.getHour()) * 60 * 60 * 1000 * 1000 * 1000 +
                (stageTime.getMinute() - requestTime.getMinute()) * 60 * 1000 * 1000 * 1000 +
                (stageTime.getSecond() - requestTime.getSecond()) * 1000 * 1000 * 1000 +
                (stageTime.getNano() - requestTime.getNano());
    }
}
