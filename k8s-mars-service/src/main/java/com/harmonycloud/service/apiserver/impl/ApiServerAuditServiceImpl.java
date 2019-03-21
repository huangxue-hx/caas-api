package com.harmonycloud.service.apiserver.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.apiserver.EnumApiServerUrlAggRangeType;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liangli
 */
@Service
public class ApiServerAuditServiceImpl extends BaseAuditService implements ApiServerAuditService {

    /**
     * apiserver audit log index
     */
    private static final String API_SERVER_AUDIT_INDEX = "kubernetes-audit";

    /**
     * apiserver audit log type
     */
    private static final String API_SERVER_AUDIT_TYPE = "auditlog";
//    private static final String GROUP_BY_NAMESPACE = "group_by_namespace";
//    private static final String GROUP_BY_VERB = "group_by_verb";

    /**
     * 按照url分组统计
     */
    private static final String GROUP_BY_URL = "group_by_url";
    private static final String GROUP_BY_RESOURCE = "group_by_resource";
    private static final String GROUP_BY_RESOURCE_NAMESPACE = "group_by_resource_namespace";
    private static final String GROUP_BY_RESOURCE_NAME = "group_by_resource_name";
    private static final String[] FETCH_SOURCE_FILED = {"requestURI", "requestReceivedTimestamp", "responseStatus", "timestamp", "objectRef", "stageTimestamp", "verb"};

    /**
     * 按照verb分组统计
     */
    private static final String GROUP_BY_VERB = "group_by_verb";

    /**
     * apiserver记录该条日志的时间戳
     */
    private static final String ACTION_TIME = "timestamp";

    /**
     * 请求url
     */
    private static final String REQUEST_URI = "requestURI";

    /**
     * 所有
     */
    private static final String ALL = "all";

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private ClusterCacheManager clusterCacheManager;

    @Autowired
    private NamespaceService namespaceService;

    @Autowired
    private EsService esService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

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

        //如果url为空,在缓存里取然后分页(因分组后分页elasticsearch不支持,对性能有影响,需要业务自己实现)
        if (StringUtils.isBlank(search.getUrl())) {
            String key = search.getClusterId() + CommonConstant.UNDER_LINE + search.getNamespace()  + CommonConstant.UNDER_LINE + search.getVerbName()  + CommonConstant.UNDER_LINE + search.getKeyWords();
            //没有key再查询elasticsearch
            if (stringRedisTemplate.hasKey(key)) {
                int start = (search.getPageNum() - CommonConstant.NUM_ONE) * search.getSize();
                int end = start + search.getSize();
                Set<String> data = stringRedisTemplate.opsForZSet().range(key, start, end);
                //把string数据结构转为json返回
                return ActionReturnUtil.returnSuccessWithData(data.stream().map(d -> JSONObject.parseObject(d)).collect(Collectors.toSet()));
            }
        }

        String startTime = search.getStartTime();
        String endTime = search.getEndTime();
        search.setStartTime(DateUtil.local2Utc(startTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
        search.setEndTime(DateUtil.local2Utc(endTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
        BoolQueryBuilder query = generateQuery(search);
        //如果clusterId为空,则默认查平台所在的集群，前台只显示pageSize条，多查无意义
        if (StringUtils.isBlank(search.getClusterId())) {

            //根据时间范围判断落在哪几个索引
            List<String> indexList = getExistIndexNames(platformEsClient, startTime, endTime);

            SearchResponse response = search(platformEsClient, query, search, indexList);

            if (Objects.isNull(response)) {
                return ActionReturnUtil.returnSuccess();
            }

            if (StringUtils.isBlank(search.getUrl())) {
                return buildGroupUrl(search, response);
            }

            return buildReturnData(response, clusterCacheManager.getPlatformCluster().getAliasName(), search.getSize());
        } else {
            Cluster cluster = clusterCacheManager.getCluster(search.getClusterId());

            if (cluster == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            TransportClient esClient = esService.getEsClient(cluster);
            //根据时间范围判断落在哪几个索引
            List<String> indexList = getExistIndexNames(esClient, startTime, endTime);
            SearchResponse response = search(esClient, query, search, indexList);

            if (Objects.isNull(response)) {
                return ActionReturnUtil.returnSuccess();
            }
            if (StringUtils.isBlank(search.getUrl())) {
                return buildGroupUrl(search, response);
            }

            return buildReturnData(response, cluster.getAliasName(), search.getSize());
        }
    }

    /**
     * 构造分组后的url 先缓存到redis 便于分页
     * @param search 查询参数对象体
     * @param response elasticsearch返回体
     * @return 查询结果
     */
    private ActionReturnUtil buildGroupUrl(ApiServerAuditSearchDto search, SearchResponse response) {

        Aggregations aggregations = response.getAggregations();
        Terms urlTerm = aggregations.get(GROUP_BY_URL);

        List<Terms.Bucket> buckets = urlTerm.getBuckets();
        List<JSONObject> data = Lists.newArrayListWithCapacity(search.getSize());
        String key = search.getClusterId() + CommonConstant.UNDER_LINE + search.getNamespace()  + CommonConstant.UNDER_LINE + search.getVerbName()  + CommonConstant.UNDER_LINE + search.getKeyWords();

        Set<ZSetOperations.TypedTuple<String>> allDatas = new HashSet<>(buckets.size());

        int start = (search.getPageNum() - CommonConstant.NUM_ONE) * search.getSize();
        int end = start + search.getSize();
        //取出分组后的url
        for (int i = 0; i < buckets.size(); i++) {
            String url = buckets.get(i).getKeyAsString();
            Aggregations aggs = buckets.get(i).getAggregations();
            Terms verbGroup = aggs.get(GROUP_BY_VERB);
            String verbName = verbGroup.getBuckets().isEmpty() ? "" : verbGroup.getBuckets().get(CommonConstant.NUM_ZERO).getKeyAsString();

            Terms resourceGroup = aggs.get(GROUP_BY_RESOURCE);
            String resource = resourceGroup.getBuckets().isEmpty() ? "" : resourceGroup.getBuckets().get(CommonConstant.NUM_ZERO).getKeyAsString();

            Terms resourceNamespaceGroup = aggs.get(GROUP_BY_RESOURCE_NAMESPACE);
            String resourceNamespace = resourceNamespaceGroup.getBuckets().isEmpty() ? "" : resourceNamespaceGroup.getBuckets().get(CommonConstant.NUM_ZERO).getKeyAsString();

            Terms resourceNameGroup = aggs.get(GROUP_BY_RESOURCE_NAME);
            String resourceName = resourceNameGroup.getBuckets().isEmpty() ? "" : resourceNameGroup.getBuckets().get(CommonConstant.NUM_ZERO).getKeyAsString();

            JSONObject recordObj = new JSONObject();
            recordObj.put("requestUrl", url);
            recordObj.put("actionObject", resource);
            recordObj.put("namespace", resourceNamespace);
            recordObj.put("actionObjectName", resourceName);
            recordObj.put("verb", verbName);
            //返回pageNum页的size条记录
            if (start <= i && i < end) {
                data.add(recordObj);
            }
            ZSetOperations.TypedTuple<String> oneData = new DefaultTypedTuple<>(recordObj.toJSONString(), Double.valueOf(i));
            allDatas.add(oneData);
        }
        //ZSet数据结构存入redis
        stringRedisTemplate.opsForZSet().add(key, allDatas);
        //过期时间3分钟
        stringRedisTemplate.expire(key, CommonConstant.NUM_THREE, TimeUnit.MINUTES);

        return ActionReturnUtil.returnSuccessWithData(data);
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
    private SearchResponse search(TransportClient esClient, BoolQueryBuilder query, ApiServerAuditSearchDto search, List<String> indexList) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (CollectionUtils.isEmpty(indexList)) {
            return null;
        }

        int pageSize = search.getSize();

        SearchRequestBuilder searchRequestBuilder = multiIndexSearch(esClient, indexList);

        searchRequestBuilder.setTypes(API_SERVER_AUDIT_TYPE).setQuery(query)
                .setFetchSource(FETCH_SOURCE_FILED, ArrayUtils.EMPTY_STRING_ARRAY);
        if (StringUtils.isBlank(search.getUrl())) {
            //.size(0)表示聚合返回所有，默认为10条，0表示返回所有
            TermsBuilder requestURITerms = AggregationBuilders.terms(GROUP_BY_URL).field(REQUEST_URI).size(0);
            TermsBuilder verbName = AggregationBuilders.terms(GROUP_BY_VERB).field("verb").size(0);
            TermsBuilder resource = AggregationBuilders.terms(GROUP_BY_RESOURCE).field("objectRef.resource").size(0);
            TermsBuilder resourceNamespace = AggregationBuilders.terms(GROUP_BY_RESOURCE_NAMESPACE).field("objectRef.namespace").size(0);
            TermsBuilder resourceName = AggregationBuilders.terms(GROUP_BY_RESOURCE_NAME).field("objectRef.name").size(0);
            searchRequestBuilder.addAggregation(requestURITerms.subAggregation(verbName).subAggregation(resource).subAggregation(resourceNamespace).subAggregation(resourceName)).setSize(0);
        } else {
            searchRequestBuilder.setFrom((search.getPageNum() - CommonConstant.NUM_ONE) * pageSize).setSize(pageSize).addSort(ACTION_TIME, SortOrder.DESC);
        }
        SearchResponse response = searchRequestBuilder.get();
        return response;
    }

    /**
     * 构造指定url查询结果
     * @param response elasticsearch返回体
     * @param clusterAliasName 集群别名
     * @param pageSize 每页显示记录条数
     * @return 查询结果
     */
    private ActionReturnUtil buildReturnData(SearchResponse response, String clusterAliasName, Integer pageSize) {

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

            Integer actionCode = (Integer) responseStatus.get("code");
            //exec接口会有101请求码
            if (actionCode < CommonConstant.HTTP_STATUS_400 && actionCode >= CommonConstant.HTTP_STATUS_100) {
                infos.setActionResult("成功");
            } else {
                infos.setActionResult("失败");
            }

            //"requestReceivedTimestamp": "2019-01-14T02:49:31.523234Z",
            //"stageTimestamp": "2019-01-14T02:49:32.228184Z",
            long spendTime = convertSpendTime((String) source.get("stageTimestamp"), (String) source.get("requestReceivedTimestamp"));
            //返回毫秒
            infos.setRequestSpendTime(spendTime + "");

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
        String startTime = search.getStartTime();
        String endTime = search.getEndTime();
        search.setStartTime(DateUtil.local2Utc(startTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
        search.setEndTime(DateUtil.local2Utc(endTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue()));
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
                List<String> indexList = getExistIndexNames(esClient, startTime, endTime);
                if (StringUtils.isNotBlank(search.getUrl())) {
                    totalRecords += getTotalCounts(esClient, query, indexList);
                } else {
                    totalRecords += getCountsGroupByUrl(esClient, query, indexList);
                }
            }
        } else {
            Cluster cluster = clusterCacheManager.getCluster(search.getClusterId());

            //找不到集群
            if (null == cluster) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            TransportClient esClient = esService.getEsClient(cluster);
            List<String> indexList = getExistIndexNames(esClient, startTime, endTime);
            if (StringUtils.isNotBlank(search.getUrl())) {
                totalRecords += getTotalCounts(esClient, query, indexList);
            } else {
                totalRecords += getCountsGroupByUrl(esClient, query, indexList);
            }
        }

        Map<String, Object> data = new HashMap<>(CommonConstant.NUM_ONE);
        data.put("total", totalRecords);

        return ActionReturnUtil.returnSuccessWithData(data);
    }

    /**
     * 根据不同的url数量
     *
     * @param esClient  集群es client
     * @param query     查询参数
     * @param indexList 索引
     * @return 数量
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private long getCountsGroupByUrl(TransportClient esClient, BoolQueryBuilder query, List<String> indexList) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        SearchRequestBuilder searchRequestBuilder = multiIndexSearch(esClient, indexList);
        TermsBuilder requestURITerms = AggregationBuilders.terms(GROUP_BY_URL).field(REQUEST_URI).size(0);

        // 计算页数对应的数据行数，先查询出来总的记录个数，计算
        SearchResponse response = searchRequestBuilder
                .setTypes(API_SERVER_AUDIT_TYPE)
                .setSearchType(SearchType.QUERY_AND_FETCH)
                .setQuery(query)
                .addAggregation(requestURITerms)
                .get();

        Aggregations aggregations = response.getAggregations();
        if (aggregations == null) {
            return CommonConstant.NUM_ZERO;
        }

        Terms urlTerm = aggregations.get(GROUP_BY_URL);
        if (urlTerm == null) {
            return CommonConstant.NUM_ZERO;
        }

        List<Terms.Bucket> urlBuckets = urlTerm.getBuckets();

        //总数
        return urlBuckets.size();
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
            List<JSONObject> namespaceInfos = Lists.newArrayListWithCapacity(k8sNamespaceNames.size());
            for (NamespaceLocal nsLocal : namespaceLocal) {

                //只处理当前集群的
                if (!StringUtils.equals(nsLocal.getClusterId(), cluster.getId())) {
                    continue;
                }

                //属于当前集群且集群中查询到的name和数据库查到的相等,则需要获取到用户能看懂的别名--Alias_name
                String namespaceName = nsLocal.getNamespaceName();
                if (k8sNamespaceNames.contains(namespaceName)) {

                    JSONObject ns = new JSONObject(CommonConstant.NUM_TWO);
                    ns.put("aliasName", nsLocal.getAliasName());
                    ns.put("namespace", namespaceName);

                    namespaceInfos.add(ns);
                    //获取到别名后，移除k8s中查询到的集合中该namespace name
                    k8sNamespaceNames.remove(nsLocal.getNamespaceName());
                }
            }

            //最后将别名集合和其他无别名集合合并
            for (String ns : k8sNamespaceNames) {
                JSONObject nsInfo = new JSONObject(CommonConstant.NUM_TWO);
                nsInfo.put("aliasName", ns);
                nsInfo.put("namespace", ns);
                namespaceInfos.add(nsInfo);
            }

            clusterNamespaceDto.setId(cluster.getId());
            clusterNamespaceDto.setAliasName(cluster.getAliasName());
            clusterNamespaceDto.setDataCenter(cluster.getDataCenter());
            clusterNamespaceDto.setName(cluster.getName());
            clusterNamespaceDto.setNamespaces(namespaceInfos);
            clusterNamespaceDtos.add(clusterNamespaceDto);
        }

        return ActionReturnUtil.returnSuccessWithData(clusterNamespaceDtos);
    }

    @Override
    public ActionReturnUtil getUrlHistogram(@NotBlank String clusterId, @NotBlank String verbName, @NotBlank String url, @NotBlank String rangeType) throws Exception {

        Cluster cluster = clusterCacheManager.getCluster(clusterId);

        //找不到集群
        if (null == cluster) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }

        TransportClient esClient = esService.getEsClient(cluster);

        EnumApiServerUrlAggRangeType enumRangeType = EnumApiServerUrlAggRangeType.getEnumRangeType(rangeType);
        if (Objects.isNull(enumRangeType)) {
            throw new MarsRuntimeException(ErrorCodeMessage.INVALID_PARAMETER);
        }

        long currentTimeMillis = System.currentTimeMillis();
        String endTime = DateFormatUtils.format(currentTimeMillis, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue());
        String startTime = DateFormatUtils.format(currentTimeMillis - enumRangeType.getMillisecond(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue());
        //落到哪些索引上
        List<String> indexList = getExistIndexNames(esClient, startTime, endTime);

        String startUTCTime = DateUtil.local2Utc(startTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());
        String endUTCTime = DateUtil.local2Utc(endTime, DateStyle.YYYY_MM_DD_HH_MM_SS.getValue(), DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z.getValue());

        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(QueryBuilders.termQuery("tag", "kubernetes"));
        query.must(QueryBuilders.termQuery(REQUEST_URI, url));
        query.must(QueryBuilders.termQuery("verb", verbName));
        query.must(QueryBuilders.rangeQuery(ACTION_TIME).from(startUTCTime).to(endUTCTime));

        SearchRequestBuilder searchRequestBuilder = multiIndexSearch(esClient, indexList);

        DateHistogramBuilder dateHistogram = AggregationBuilders.dateHistogram("by_time")
                .interval(new DateHistogramInterval(enumRangeType.getInterval()))
                .format(DateStyle.YYYY_MM_DD_HH_MM_SS.getValue())
                .minDocCount(0).field(ACTION_TIME).extendedBounds(currentTimeMillis - enumRangeType.getMillisecond(), currentTimeMillis);
        // 计算页数对应的数据行数，先查询出来总的记录个数，计算
        SearchResponse response = searchRequestBuilder
                .setTypes(API_SERVER_AUDIT_TYPE)
                .setQuery(query)
                .addAggregation(dateHistogram)
                .setSize(0)
                .get();

        Aggregations aggregations = response.getAggregations();

        List<? extends Histogram.Bucket> buckets = ((Histogram) aggregations.get("by_time")).getBuckets();

        JSONArray array = new JSONArray();
        for (Histogram.Bucket bucket : buckets) {
            JSONObject agg = new JSONObject();
            long docCount = bucket.getDocCount();
            String time = DateFormatUtils.format(((DateTime) bucket.getKey()).getMillis(), DateStyle.YYYY_MM_DD_HH_MM_SS.getValue());
            agg.put("count", docCount);
            agg.put("time", time);
            array.add(agg);
        }

        return ActionReturnUtil.returnSuccessWithData(array);
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
        if (StringUtils.isNotBlank(namespace) && !ALL.equals(namespace)) {
            query.must(QueryBuilders.termQuery("objectRef.namespace", namespace));
        }

        if (StringUtils.isNotBlank(keyWords)) {
            query.must(QueryBuilders.queryStringQuery("*" + keyWords + "*").field(REQUEST_URI)
                    .field("objectRef.resource").field("objectRef.namespace").field("objectRef.name").field("verb"));
        }

        if (StringUtils.isNotBlank(verbName) && !ALL.equals(verbName)) {
            query.must(QueryBuilders.termQuery("verb", verbName));
        }

        String url = search.getUrl();
        if (StringUtils.isNotBlank(search.getUrl())) {
            query.must(QueryBuilders.termQuery(REQUEST_URI, url));
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
        //kubernetes-audit-2019.01.09
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
            Date startDate = DateUtil.StringToDate(startTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
            Date endDate = DateUtil.StringToDate(endTime, DateStyle.YYYY_MM_DD_HH_MM_SS);
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
                        indexNameList.add(API_SERVER_AUDIT_INDEX + CommonConstant.LINE + endYear + CommonConstant.DOT + String.format("%02d", endMonth) + CommonConstant.DOT + String.format("%02d", i));
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
     * 计算花费的时间，SimpleDateFormat转换出来的有问题
     *
     * @param stage   stageTime 2019-01-14T02:49:32.228184Z
     * @param request requestTime 2019-01-14T02:49:31.523234Z
     * @return 花费的时间
     */
    private long convertSpendTime(String stage, String request) {
        Instant inst1 = Instant.parse(stage);
        Instant inst2 = Instant.parse(request);

        Duration between = Duration.between(inst2, inst1);

        return between.toMillis();
    }
}
