package com.harmonycloud.service.platform.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.*;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.JSchClient;
import com.harmonycloud.common.util.date.DateStyle;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dto.config.AuditRequestInfo;
import com.harmonycloud.dto.log.LogQueryDto;
import com.harmonycloud.k8s.bean.Pod;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.service.PodService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.EsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.LogQuery;
import com.harmonycloud.service.platform.service.LogService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
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
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.harmonycloud.common.Constant.CommonConstant.*;

/**
 * 日志ervice实现类
 */
@Service
@Scope("prototype")
public class LogServiceImpl implements LogService {

    private static Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    //每次只能查询100个POD的文件
    private static final int MAX_POD_FETCH_COUNT = 100;
    private static final int MAX_EXPORT_LENGTH = 100000;

    @Value("${shell:#{null}}")
    private String shellStarter;
    @Value("${es.scroll.time:600000}")
    private String scrollTime;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private PodService podService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private EsService esService;


    @Override
    public void exportLog(String namespace, String podName, String clusterId, String logName, HttpServletResponse response) throws Exception {
        OutputStream outputStream = null;
        InputStream inputStream = null;
        Session session = null;
        try {
            response.setContentType("multipart/form-data");
            response.setHeader("Content-Disposition", "attachment;fileName=" + logName);
            outputStream = response.getOutputStream();
            Cluster cluster = clusterService.findClusterById(clusterId);
            if (null == cluster) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            K8SClientResponse podRes = podService.getPod(namespace, podName, cluster);
            Pod pod = K8SClient.converToBean(podRes, Pod.class);
            if (null == pod) {
                throw new MarsRuntimeException(DictEnum.POD.phrase(), ErrorCodeMessage.NOT_FOUND);
            }
            session = JSchClient.connect(pod.getStatus().getHostIP(), cluster.getUsername(), cluster.getPassword());
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;

            final AtomicReference<Boolean> isFileExist = new AtomicReference<>(false);
            channelSftp.ls(CommonConstant.DEFAULT_LOG_MOUNT_PATH, ((ChannelSftp.LsEntry lsEntry) -> {
                if (lsEntry.getFilename().equals(logName)) {
                    isFileExist.set(true);
                    return ChannelSftp.LsEntrySelector.BREAK;
                }
                return ChannelSftp.LsEntrySelector.CONTINUE;
            }));
            if (!isFileExist.get()) {
                throw new MarsRuntimeException(ErrorCodeMessage.FILE_NOT_FOUND);
            }
            inputStream = channelSftp.get(CommonConstant.DEFAULT_LOG_MOUNT_PATH + logName);
            //循环写入输出流
            byte[] b = new byte[2048];
            int length;
            while ((length = inputStream.read(b)) > 0) {
                outputStream.write(b, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
//            session.disconnect();
        } catch (MarsRuntimeException mre) {
            logger.error("日志导出失败", mre);
            throw mre;
        } catch (Exception e) {
            logger.error("日志导出失败", e);
            throw new MarsRuntimeException(ErrorCodeMessage.LOG_EXPORT_FAILED);
        } finally {
            try {
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception e) {
                logger.error("尝试Session关闭失败", e);
            }

        }
    }

    @Override
    public void exportLog(LogQuery logQuery, HttpServletResponse response) throws Exception {
        OutputStream outputStream = null;
        Cluster cluster = null;
        if (logQuery.getClusterId() != null) {
            cluster = clusterService.findClusterById(logQuery.getClusterId());
        } else {
            cluster = namespaceLocalService.getClusterByNamespaceName(logQuery.getNamespace());
        }
        TransportClient client = esService.getEsClient(cluster);
        if (StringUtils.isBlank(logQuery.getAppName())
                && StringUtils.isBlank(logQuery.getPod())
                && StringUtils.isBlank(logQuery.getContainer())) {
            throw new MarsRuntimeException(ErrorCodeMessage.NS_POD_CONTAINER_NOT_BLANK);
        }
        logQuery.setPageSize(MAX_EXPORT_LENGTH);
        SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
        SearchResponse scrollResp = null;
        try {
            scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
        } catch (SearchPhaseExecutionException e){
            if (isExceedMaxResult(e)) {
                //设置最大查询结果条数
                logger.info("设置日志条数最大搜索条数,index：{}, clusterId:{}",
                        JSONObject.toJSONString(logQuery.getIndexes()), cluster.getId());
                esService.updateIndexMaxResultWindow(client, logQuery.getIndexes(), MAX_EXPORT_LENGTH);
                scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
            } else {
                logger.error("导出日志失败，", e);
                throw new MarsRuntimeException(ErrorCodeMessage.QUERY_FAIL);
            }
        }
        Long totalHit = scrollResp.getHits().getTotalHits();
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + logQuery.getAppName() + ".log");
        outputStream = response.getOutputStream();
        try {
            if (totalHit > MAX_EXPORT_LENGTH) {
                outputStream.write(("Find total " + totalHit + " line messages, only export " + MAX_EXPORT_LENGTH + " lines.\n").getBytes());
            }
            for (SearchHit it : scrollResp.getHits().getHits()) {
                outputStream.write((it.getSourceAsMap().get("message").toString() + "\n").getBytes());
            }
        } catch (Exception e) {
            logger.error("导出日志失败", e);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

    }

    @Override
    public ActionReturnUtil fileLog(LogQuery logQuery)
            throws Exception {
        if (StringUtils.isBlank(logQuery.getNamespace())) {
            throw new MarsRuntimeException(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE);
        }
        logger.info("查询日志，query:{}",JSONObject.toJSONString(logQuery));
        Map<String, Object> data = new HashMap<String, Object>();
        if (logQuery.getIndexes() == null || logQuery.getIndexes().length == 0) {
            data.put("log", "");
            data.put("totalHit", 0);
        }
        StringBuilder log = new StringBuilder();
        SearchResponse scrollResp = null;
        String scrollId = logQuery.getScrollId();

        Cluster cluster = null;
        if (logQuery.getClusterId() != null) {
            cluster = clusterService.findClusterById(logQuery.getClusterId());
        } else {
            cluster = namespaceLocalService.getClusterByNamespaceName(logQuery.getNamespace());
        }
        TransportClient client = esService.getEsClient(cluster);
        if (StringUtils.isBlank(scrollId)) {
            if (StringUtils.isBlank(logQuery.getNamespace())) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NAMESPACE_NOT_BLANK);
            }
            if (StringUtils.isBlank(logQuery.getAppName()) && StringUtils.isBlank(logQuery.getPod())
                    && StringUtils.isBlank(logQuery.getContainer())) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NS_POD_CONTAINER_NOT_BLANK);
            }
            SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
            scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
            scrollId = scrollResp.getScrollId();
        } else {
            scrollResp = client.prepareSearchScroll(scrollId).setScroll(new TimeValue(Long.parseLong(scrollTime))).execute().actionGet();
        }
        for (SearchHit it : scrollResp.getHits().getHits()) {
            log.append(it.getSourceAsMap().get("message").toString() + "\n");
        }
        if (log.toString().length() == 0) {
            log.append("No log found.");
        }
        data.put("log", log);
        data.put("scrollId", scrollId);
        data.put("totalHit", scrollResp.getHits().getTotalHits());
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public ActionReturnUtil listfileName(LogQuery logQuery) throws Exception {
        TreeSet<String> logFileNames = new TreeSet<String>();
        if (logQuery.getIndexes() == null || logQuery.getIndexes().length == 0) {
            return ActionReturnUtil.returnSuccessWithData(logFileNames);
        }
        Cluster cluster = null;
        if (logQuery.getClusterId() != null) {
            cluster = clusterService.findClusterById(logQuery.getClusterId());
        } else {
            cluster = namespaceLocalService.getClusterByNamespaceName(logQuery.getNamespace());
        }
        Client client = esService.getEsClient(cluster);
        if (StringUtils.isBlank(logQuery.getContainer()) && StringUtils.isBlank(logQuery.getPod())) {
            logFileNames.addAll(this.listLogFileNames(logQuery, client, true));
        } else {
            logFileNames.addAll(this.listLogFileNames(logQuery, client, false));
        }
        return ActionReturnUtil.returnSuccessWithData(logFileNames);
    }

    @Override
    public ActionReturnUtil getProcessLog(String rangeType, String processName, String node, String clusterId) throws Exception {
        List<String> result = new ArrayList<String>();
        try {
            Client client = esService.getEsClient(clusterService.findClusterById(clusterId));
            EnumMonitorQuery query = EnumMonitorQuery.getRangeData(rangeType);
            if (query == null) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
            }
            Date now = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+08:00'");
            long startTime = now.getTime() - query.getMillisecond();
            String from = formatter.format(new Date(startTime));
            String to = formatter.format(now);
            QueryBuilder queryBuilder = QueryBuilders.rangeQuery("timestamp").from(from).to(to);
            QueryBuilder matchBuilder = QueryBuilders.disMaxQuery().add(QueryBuilders.termQuery("tag", processName))
                    .add(QueryBuilders.matchQuery("host_ip", node));
            SearchResponse searchResponse = client.prepareSearch(esService.getLogIndexPrefix() + "*")
                    .addSort("offset", SortOrder.DESC)
                    .setScroll(new TimeValue(Long.parseLong(scrollTime)))
                    .setQuery(matchBuilder)
                    .setQuery(queryBuilder).execute().actionGet();
            for (SearchHit it : searchResponse.getHits().getHits()) {
                result.add(it.getSourceAsMap().get("@timestamp").toString() + "  " + it.getSourceAsMap().get("message").toString() +
                        "(pid:" + it.getSourceAsMap().get("pid").toString() + ") and (source:" + it.getSourceAsMap().get("source").toString() + ")");
            }
        } catch (Exception e) {
            throw e;
        }
        return ActionReturnUtil.returnSuccessWithData(result);
    }

    /**
     * 查询某个容器的日志文件名称列表，返回文件名称包含pod名称为前缀
     *
     * @param client
     * @return
     */
    private TreeSet<String> listLogFileNames(LogQuery logQuery, Client client, boolean withPodName) {
        TreeSet<String> logFileNames = new TreeSet<String>();
        SearchResponse scrollResp = null;
        BoolQueryBuilder queryBuilder = this.getQueryBuilder(logQuery);
        if (StringUtils.isNotBlank(logQuery.getPod())) {
            queryBuilder.must(QueryBuilders.termQuery("k8s_pod.keyword", logQuery.getPod()));
            scrollResp = client.prepareSearch(esService.getLogIndexPrefix() + "*")
                    .setIndices(logQuery.getIndexes())
                    .setQuery(queryBuilder)
                    .addAggregation(AggregationBuilders.terms("source").field("source.keyword"))
                    .execute().actionGet();
            if (scrollResp.getAggregations() == null) {
                return logFileNames;
            }
            Terms agg1 = scrollResp.getAggregations().get("source");
            for (Terms.Bucket bucket : agg1.getBuckets()) {
                String name = bucket.getKey().toString();
                if (name.contains(CommonConstant.SLASH)){
                    name = name.substring(name.lastIndexOf(CommonConstant.SLASH)+1);
                }
                logFileNames.add(name);
            }
            return logFileNames;
        }
        scrollResp = client.prepareSearch(esService.getLogIndexPrefix() + "*")
                .setIndices(logQuery.getIndexes())
                .addSort("offset", SortOrder.DESC)
                .setQuery(queryBuilder)
                .addAggregation(
                        AggregationBuilders.terms("k8s_pod").field("k8s_pod.keyword").size(MAX_POD_FETCH_COUNT)
                        .subAggregation(AggregationBuilders.terms("source").field("source.keyword").size(MAX_POD_FETCH_COUNT)))
                .execute().actionGet();
        if (scrollResp.getAggregations() == null) {
            return logFileNames;
        }
        Terms podTerms = scrollResp.getAggregations().get("k8s_pod");
        Iterator<SearchHit> it = scrollResp.getHits().iterator();

        while (it.hasNext()) {
            SearchHit sh = it.next();
            Map<String, Object> doc = sh.getSourceAsMap();
            AuditRequestInfo sr = new AuditRequestInfo();
        }
        for (Terms.Bucket bucket : podTerms.getBuckets()) {
            String bucketPodName = bucket.getKey().toString();
            if (!bucketPodName.startsWith(logQuery.getAppName())){  //todo logpilot
                continue;
            }
            Terms logDirTerms = bucket.getAggregations().get("source");

            for (Terms.Bucket dirBucket : logDirTerms.getBuckets()) {
                String logDir = dirBucket.getKey().toString();
                if (logDir.contains(CommonConstant.SLASH)){
                    logDir = logDir.substring(logDir.lastIndexOf(CommonConstant.SLASH)+1);
                }
                if (withPodName) {
                    logFileNames.add(bucketPodName + "/" + logDir);
                } else {
                    logFileNames.add(logDir);
                }
            }
        }

        return logFileNames;
    }


    /**
     * 根据查询条件设置SearchRequestBuilder
     *
     * @param client
     * @param logQuery
     * @return
     */
    private SearchRequestBuilder getSearchRequestBuilder(TransportClient client, LogQuery logQuery) {
        //日志时间范围查询设置
        BoolQueryBuilder queryBuilder = getQueryBuilder(logQuery);

        if (StringUtils.isNoneBlank(logQuery.getLogDir())){
            queryBuilder.must(QueryBuilders.wildcardQuery("source.keyword",  "*" + logQuery.getLogDir()));
        }
        if (StringUtils.isNotBlank(logQuery.getPod())) {
            queryBuilder.must(QueryBuilders.termQuery("k8s_pod.keyword", logQuery.getPod()));
        }
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(esService.getLogIndexPrefix() + "*")
                .setIndices(logQuery.getIndexes())
                .addSort("@timestamp", SortOrder.ASC).addSort("offset", SortOrder.ASC)
                .setScroll(new TimeValue(Long.parseLong(scrollTime)))
                .setQuery(queryBuilder);
        return searchRequestBuilder;
    }

    /**
     * 参数校验，并将接口日志查询对象转换为内部服务查询对象
     *
     * @param logQueryDto 对外接口日志查询对象
     * @return 内部服务日志查询对象
     */
    @Override
    public LogQuery transLogQuery(LogQueryDto logQueryDto) throws Exception {
        Assert.hasText(logQueryDto.getNamespace(), "分区不能为空");
        if (StringUtils.isBlank(logQueryDto.getClusterId())) {
            String clusterId = namespaceLocalService.getClusterByNamespaceName(logQueryDto.getNamespace()).getId();
            logQueryDto.setClusterId(clusterId);
        }
        if (StringUtils.isNotBlank(logQueryDto.getScrollId())) {
            LogQuery logQuery = new LogQuery();
            logQuery.setScrollId(logQueryDto.getScrollId());
            logQuery.setNamespace(logQueryDto.getNamespace());
            logQuery.setAppName(logQueryDto.getAppName());
            logQuery.setAppType(logQueryDto.getAppType());
            logQuery.setClusterId(logQueryDto.getClusterId());
            return logQuery;
        }
        Assert.notNull(logQueryDto, "查询参数不能为空");
        Assert.hasText(logQueryDto.getAppName(), "服务名不能为空");
        Assert.hasText(logQueryDto.getAppType(), "服务类型不能为空");
        String fromDate = "";
        String toDate = "";
        String[] indexes;
        //String style = DateUtil.getTimezoneFormatStyle(TimeZone.getDefault());
        // ES中日志时间戳为0时区
        String style = DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS.getValue();
        if (StringUtils.isNotBlank(logQueryDto.getLogTimeStart())
                && StringUtils.isNotBlank(logQueryDto.getLogTimeEnd())) {
            fromDate = logQueryDto.getLogTimeStart();
            toDate = logQueryDto.getLogTimeEnd();
            //零时区格式
            /*if (logQueryDto.getLogTimeStart().indexOf("T") > 0
                    && logQueryDto.getLogTimeStart().indexOf("Z") > 0) {
                //将零时区转换成系统时区的时间
                Date from = DateUtil.stringToDate(logQueryDto.getLogTimeStart(),
                        DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS.getValue(), "UTC");
                Date to = DateUtil.stringToDate(logQueryDto.getLogTimeEnd(),
                        DateStyle.YYYY_MM_DD_T_HH_MM_SS_Z_SSS.getValue(), "UTC");
                fromDate = DateUtil.DateToString(from, style);
                toDate = DateUtil.DateToString(to, style);
                if (fromDate == null || toDate == null) {
                    throw new MarsRuntimeException(ErrorCodeMessage.DATE_FORMAT_ERROR);
                }
            } else {
                //不带时区，默认为云平台主机时区
                fromDate = logQueryDto.getLogTimeStart();
                toDate = logQueryDto.getLogTimeEnd();
            }*/
        } else {
            if (logQueryDto.getRecentTimeNum() == null || logQueryDto.getRecentTimeNum() == 0) {
                logQueryDto.setRecentTimeNum(DEFAULT_LOG_QUERY_TIME);
                logQueryDto.setRecentTimeUnit(TIME_UNIT_MINUTES);
            }
            SimpleDateFormat format = new SimpleDateFormat(style);
            Date current = new Date();
            Date from = DateUtil.addTime(current, logQueryDto.getRecentTimeUnit(),
                    -logQueryDto.getRecentTimeNum());
            fromDate = format.format(from);
            toDate = format.format(current);
        }
        logger.info("Query log time, fromDate:{},toDate:{}", fromDate, toDate);
        LogQuery logQuery = new LogQuery();
        BeanUtils.copyProperties(logQueryDto, logQuery);
        logQuery.setLogDateStart(fromDate);
        logQuery.setLogDateEnd(toDate);
        //获取查询时间段对应的索引列表
        Date startDate = DateUtil.StringToDate(fromDate, style);
        Date endDate = DateUtil.StringToDate(toDate, style);
        if (!endDate.after(startDate)) {
            throw new MarsRuntimeException(ErrorCodeMessage.DATE_FROM_AFTER_TO);
        }
        indexes = this.getIndexes(startDate, endDate, logQueryDto.getClusterId());
        logQuery.setIndexes(indexes);
        if (logQueryDto.getPageSize() == null) {
            logQuery.setPageSize(DEFAULT_PAGE_SIZE_200);
        } else if (logQueryDto.getPageSize() > MAX_PAGE_SIZE_1000) {
            logQuery.setPageSize(MAX_PAGE_SIZE_1000);
        }
        if (StringUtils.isBlank(logQuery.getSearchType())) {
            logQuery.setSearchType(EsSearchTypeEnum.MATCH_PHRASE.getCode());
        } else if (EsSearchTypeEnum.getByCode(logQuery.getSearchType()) == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.LOG_SEARCH_TYPE_NOT_SUPPORT);
        }
        return logQuery;
    }


    @Override
    public List<String> queryLogFile(String pod, String container, String namespace, String LogDir, String clusterId) {
        AssertUtil.notBlank(pod,DictEnum.POD);
        AssertUtil.notBlank(namespace,DictEnum.NAMESPACE);
        AssertUtil.notBlank(LogDir,DictEnum.LOG_DIR);

        Cluster cluster = null;
        boolean error = false;
        Process process = null;
        List<String> fileList = new ArrayList<>();
        try {
            if (StringUtils.isNotBlank(namespace) && !CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(namespace)) {
                cluster = namespaceLocalService.getClusterByNamespaceName(namespace);
            } else if (StringUtils.isNotBlank(clusterId)) {
                cluster = clusterService.findClusterById(clusterId);
            }
            if (cluster == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }

            //kubectl exec webapi-6cf47949c8-kwddh -n kube-system ls /opt/logs
            String command;
            if(StringUtils.isBlank(container)){
                command = MessageFormat.format("kubectl exec {0} -n {1} --server={2} --token={3} --insecure-skip-tls-verify=true -- ls {4} -F",
                        pod,namespace,cluster.getApiServerUrl(),cluster.getMachineToken(),LogDir);
            }else {
                command = MessageFormat.format("kubectl exec {0} -c {1} -n {2} --server={3} --token={4} --insecure-skip-tls-verify=true -- ls {5} -F",
                        pod, container, namespace, cluster.getApiServerUrl(), cluster.getMachineToken(), LogDir);
            }

            process = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = stdInput.readLine()) != null) {
                boolean bool = line.trim().endsWith("/");
                if (!bool) {
                    fileList.add(line.trim());
                }
            }

            while ((line = stdError.readLine()) != null) {
                logger.error("执行指令错误:{}",line);
                error = true;
            }
            if (error) {
                throw new Exception();
            }


        } catch (Exception e) {
            logger.error("出现异常:",e);
            throw new MarsRuntimeException(ErrorCodeMessage.RUN_COMMAND_ERROR);
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
        return fileList;
    }

    /**
     * 根据查询的时间区间 返回该时间段内es的索引列表
     *
     * @param from
     * @param to
     * @return
     */
    private String[] getIndexes(Date from, Date to, String clusterId) throws Exception {
        Set<String> indexes = new HashSet<>();
        Date indexDate = from;
        List<String> existIndexes = esService.getIndexes(clusterId);
        while (indexDate.before(to)) {
            String index = esService.getLogIndexPrefix() + DateUtil.DateToString(indexDate, DateStyle.YYYYMMDD_DOT);
            String snapshotIndex = index + ES_INDEX_SNAPSHOT_RESTORE;
            if (existIndexes.contains(index)) {
                indexes.add(index);
            }
            //同时查询快照恢复的索引
            if (existIndexes.contains(snapshotIndex)) {
                indexes.add(snapshotIndex);
            }
            indexDate = DateUtil.addDay(indexDate, 1);
        }
        //添加最后一天的索引
        String lastIndex = esService.getLogIndexPrefix() + DateUtil.DateToString(to, DateStyle.YYYYMMDD_DOT);
        String lastSnapshotIndex = lastIndex + ES_INDEX_SNAPSHOT_RESTORE;
        if (existIndexes.contains(lastIndex) && !indexes.contains(lastIndex)) {
            indexes.add(lastIndex);
        }
        if (existIndexes.contains(lastSnapshotIndex) && !indexes.contains(lastSnapshotIndex)) {
            indexes.add(lastSnapshotIndex);
        }
        //兼容日志收集filebeat升级前后创建索引时间问题,
        Date next = DateUtil.addDay(to, 1);
        String nextIndex= esService.getLogIndexPrefix() + DateUtil.DateToString(next, DateStyle.YYYYMMDD_DOT);
        String nextSnapshotIndex = nextIndex + ES_INDEX_SNAPSHOT_RESTORE;
        if (existIndexes.contains(nextIndex) && !indexes.contains(nextIndex)) {
            indexes.add(nextIndex);
        }
        if (existIndexes.contains(nextSnapshotIndex) && !indexes.contains(nextSnapshotIndex)) {
            indexes.add(nextSnapshotIndex);
        }
        return indexes.toArray(new String[0]);
    }

    private BoolQueryBuilder getQueryBuilder(LogQuery logQuery) {
        //日志时间范围查询设置
        QueryBuilder timeFilter = QueryBuilders.rangeQuery("@timestamp").from(logQuery.getLogDateStart())
                .to(logQuery.getLogDateEnd());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(timeFilter)
                .filter(QueryBuilders.termQuery("k8s_pod_namespace.keyword", logQuery.getNamespace()));
        queryBuilder.filter(QueryBuilders.termQuery("k8s_resource_name.keyword", logQuery.getAppName()));
        queryBuilder.filter(QueryBuilders.termQuery("k8s_resource_type.keyword", logQuery.getAppType()));
        if (StringUtils.isNotBlank(logQuery.getContainer())) {
            queryBuilder.filter(QueryBuilders.termQuery("k8s_container_name.keyword", logQuery.getContainer()));
        }
        if (StringUtils.isNotBlank(logQuery.getSeverity()) && !logQuery.getSeverity().equalsIgnoreCase("All")) {
            //日志内容过滤 warn, error等信息
            queryBuilder = queryBuilder.must(QueryBuilders.termQuery("message",
                    EnumLogSeverity.getSeverityName(logQuery.getSeverity())));
        }
        if (StringUtils.isNotBlank(logQuery.getSearchWord())) {
            String keyWord = logQuery.getSearchWord().trim().toLowerCase();
            //日志内容关键字查询
            if (EsSearchTypeEnum.MATCH.getCode().equalsIgnoreCase(logQuery.getSearchType())) {
                queryBuilder = queryBuilder.must(QueryBuilders.matchQuery("message", keyWord));
            } else if (EsSearchTypeEnum.MATCH_PHRASE.getCode().equalsIgnoreCase(logQuery.getSearchType())) {
                queryBuilder = queryBuilder.must(QueryBuilders.matchPhraseQuery("message", keyWord));
            } else if (EsSearchTypeEnum.WILDCARD.getCode().equalsIgnoreCase(logQuery.getSearchType())) {
                //模糊查询如果参数没有*，添加*进行模糊匹配
                if (!keyWord.contains("*")) {
                    keyWord = "*" + keyWord + "*";
                }
                queryBuilder = queryBuilder.must(QueryBuilders.wildcardQuery("message", keyWord));
            } else if (EsSearchTypeEnum.REGEXP.getCode().equalsIgnoreCase(logQuery.getSearchType())) {
                queryBuilder = queryBuilder.must(QueryBuilders.regexpQuery("message", keyWord));
            }
        }
        return queryBuilder;
    }

    private boolean isExceedMaxResult(SearchPhaseExecutionException e) {
        if (e.getCause() != null && e.getCause().getMessage() != null
                && e.getCause().getMessage().contains("max_result_window")) {
            return true;
        }
        return false;
    }

}
