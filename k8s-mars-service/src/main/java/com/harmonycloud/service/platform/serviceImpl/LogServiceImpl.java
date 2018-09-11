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
import com.pty4j.PtyProcess;
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
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import static com.harmonycloud.common.Constant.CommonConstant.*;

/**
 * 日志ervice实现类
 */
@Service
@Scope("prototype")
public class LogServiceImpl implements LogService {

    private static Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    //日志查询分页保留30分钟不失效
    private static final int SEARCH_TIME = 1800000;
    //每次只能查询100个POD的文件
    private static final int MAX_POD_FETCH_COUNT = 100;
    private static final int MAX_EXPORT_LENGTH = 100000;
    //日志最多查询的数量为200
    private static final int MAX_LOG_LINES = 200;

    @Value("${shell:#{null}}")
    private String shellStarter;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    PodService podService;
    @Autowired
    ClusterService clusterService;
    @Autowired
    EsService esService;

    private boolean isReady;
    private String[] termCommand;
    private PtyProcess process;
    private Integer columns = 20;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter outputWriter;
    private WebSocketSession webSocketSession;

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();


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
        if (StringUtils.isBlank(logQuery.getDeployment()) && StringUtils.isBlank(logQuery.getPod())
                && StringUtils.isBlank(logQuery.getContainer())) {
            throw new MarsRuntimeException(ErrorCodeMessage.NS_POD_CONTAINER_NOT_BLANK);
        }
        logQuery.setPageSize(MAX_EXPORT_LENGTH);
        SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
        SearchResponse scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
        Long totalHit = scrollResp.getHits().getTotalHits();
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + logQuery.getDeployment() + ".log");
        outputStream = response.getOutputStream();
        try {
            if (totalHit > MAX_EXPORT_LENGTH) {
                outputStream.write(("Find total " + totalHit + " line messages, only export " + MAX_EXPORT_LENGTH + " lines.\n").getBytes());
            }
            for (SearchHit it : scrollResp.getHits().getHits()) {
                outputStream.write((it.getSource().get("message").toString() + "\n").getBytes());
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
            if (StringUtils.isBlank(logQuery.getDeployment()) && StringUtils.isBlank(logQuery.getPod())
                    && StringUtils.isBlank(logQuery.getContainer())) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.NS_POD_CONTAINER_NOT_BLANK);
            }
            SearchRequestBuilder searchRequestBuilder = this.getSearchRequestBuilder(client, logQuery);
            scrollResp = searchRequestBuilder.setSize(logQuery.getPageSize()).execute().actionGet();
            scrollId = scrollResp.getScrollId();
        } else {
            scrollResp = client.prepareSearchScroll(scrollId).setScroll(new TimeValue(SEARCH_TIME)).execute().actionGet();
        }
        for (SearchHit it : scrollResp.getHits().getHits()) {
            log.append(it.getSource().get("message").toString() + "\n");
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
            SearchResponse searchResponse = client.prepareSearch("logstash-*")
                    .addSort("@timestamp", SortOrder.DESC)
                    .setScroll(new TimeValue(SEARCH_TIME))
                    .setQuery(matchBuilder)
                    .setQuery(queryBuilder).execute().actionGet();
            for (SearchHit it : searchResponse.getHits().getHits()) {
                result.add(it.getSource().get("@timestamp").toString() + "  " + it.getSource().get("message").toString() +
                        "(pid:" + it.getSource().get("pid").toString() + ") and (source:" + it.getSource().get("source").toString() + ")");
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
            queryBuilder.must(QueryBuilders.termQuery("pod_name", logQuery.getPod()));
            scrollResp = client.prepareSearch("logstash-*")
                    .setIndices(logQuery.getIndexes())
                    .setQuery(queryBuilder)
                    .addAggregation(AggregationBuilders.terms("logdir").field("logdir"))
                    .setScroll(new TimeValue(SEARCH_TIME))
                    .execute().actionGet();
            if (scrollResp.getAggregations() == null) {
                return logFileNames;
            }
            Terms agg1 = scrollResp.getAggregations().get("logdir");
            List<Terms.Bucket> buckets = agg1.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                String name = bucket.getKey().toString();
                logFileNames.add(name);
            }
            return logFileNames;
        }
        TermsBuilder logDirTermsBuilder = AggregationBuilders.terms("logdir").field("logdir").size(MAX_POD_FETCH_COUNT);
        TermsBuilder podTermsBuilder = AggregationBuilders.terms("pod_name").field("pod_name").size(MAX_POD_FETCH_COUNT);
        podTermsBuilder.subAggregation(logDirTermsBuilder);
        scrollResp = client.prepareSearch("logstash-*")
                .setIndices(logQuery.getIndexes())
                .addSort("@timestamp", SortOrder.DESC)
                .setQuery(queryBuilder)
                .addAggregation(podTermsBuilder)
                .setScroll(new TimeValue(SEARCH_TIME))
                .execute().actionGet();
        if (scrollResp.getAggregations() == null) {
            return logFileNames;
        }
        Terms podTerms = scrollResp.getAggregations().get("pod_name");
        List<Terms.Bucket> podBuckets = podTerms.getBuckets();
        for (Terms.Bucket bucket : podBuckets) {
            String bucketPodName = bucket.getKey().toString();
            Terms logDirTerms = bucket.getAggregations().get("logdir");
            List<Terms.Bucket> logDirBuckets = logDirTerms.getBuckets();
            for (Terms.Bucket dirBucket : logDirBuckets) {
                String logDir = dirBucket.getKey().toString();
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

        if (StringUtils.isNotBlank(logQuery.getLogDir())) {
            queryBuilder.must(QueryBuilders.termQuery("logdir", logQuery.getLogDir()));
        }
        if (StringUtils.isNotBlank(logQuery.getPod())) {
            queryBuilder.must(QueryBuilders.termQuery("pod_name", logQuery.getPod()));
        }
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("logstash-*")
                .setIndices(logQuery.getIndexes())
                .addSort("log_time", SortOrder.ASC)
                .setScroll(new TimeValue(SEARCH_TIME))
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
            logQuery.setDeployment(logQueryDto.getDeployment());
            logQuery.setClusterId(logQueryDto.getClusterId());
            return logQuery;
        }
        Assert.notNull(logQueryDto, "查询参数不能为空");
        Assert.hasText(logQueryDto.getDeployment(), "服务名不能为空");
        String fromDate = "";
        String toDate = "";
        String[] indexes;
        String style = DateUtil.getTimezoneFormatStyle(TimeZone.getDefault());
        if (StringUtils.isNotBlank(logQueryDto.getLogTimeStart())
                && StringUtils.isNotBlank(logQueryDto.getLogTimeEnd())) {
            //零时区格式
            if (logQueryDto.getLogTimeStart().indexOf("T") > 0
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
            }
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
    public List<String> queryLogFile(String pod, String namespace, String LogDir, String clusterId) {
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

            String command = MessageFormat.format("kubectl exec {0} -n {1} --server={2} --token={3} --insecure-skip-tls-verify=true -- ls {4} -F",
                    pod,namespace,cluster.getApiServerUrl(),cluster.getMachineToken(),LogDir);

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

    @Override
    public void logRealTimeRefresh(WebSocketSession session, LogQueryDto logQueryDto) {
        AssertUtil.notBlank(logQueryDto.getPod(),DictEnum.POD);
        AssertUtil.notBlank(logQueryDto.getNamespace(),DictEnum.NAMESPACE);
        Cluster cluster = null;
        boolean error = false;
        Process process = null;
        String command = null;
        try {
            if (StringUtils.isNotBlank(logQueryDto.getNamespace()) && !CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(logQueryDto.getNamespace())) {
                cluster = namespaceLocalService.getClusterByNamespaceName(logQueryDto.getNamespace());
            } else if (StringUtils.isNotBlank(logQueryDto.getClusterId())) {
                cluster = clusterService.findClusterById(logQueryDto.getClusterId());
            }
            if (cluster == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            //标准输出
            if(logQueryDto.getLogSource()== LogService.LOG_TYPE_STDOUT){
                AssertUtil.notBlank(logQueryDto.getContainer(),DictEnum.CONTAINER);
                 command = MessageFormat.format("kubectl logs {0} -c {1} -n {2} --tail={3} -f --server={4} --token={5} --insecure-skip-tls-verify=true",
                        logQueryDto.getPod(),logQueryDto.getContainer(),logQueryDto.getNamespace(),MAX_LOG_LINES,cluster.getApiServerUrl(), cluster.getMachineToken());


            }else if(logQueryDto.getLogSource()==LogService.LOG_TYPE_LOGFILE){
                AssertUtil.notBlank(logQueryDto.getLogDir(), DictEnum.LOG_DIR);
                AssertUtil.notBlank(logQueryDto.getLogFile(),DictEnum.LOG_FILE);
                //kubectl exec webapi-6cf47949c8-kwddh -n kube-system -- tail -200f /opt/logs/webapi-info.2018-06-29.log
                command = MessageFormat.format("kubectl exec {0} -n {1} --server={2} --token={3} --insecure-skip-tls-verify=true -- tail -f -n {4} {5}/{6}",
                        logQueryDto.getPod(),logQueryDto.getNamespace(),cluster.getApiServerUrl(),cluster.getMachineToken(),MAX_LOG_LINES,logQueryDto.getLogDir(),logQueryDto.getLogFile());

            }
            process = Runtime.getRuntime().exec(command);
            while (session.isOpen()) {
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                while ((line = stdInput.readLine()) != null) {
                    session.sendMessage(new TextMessage(line));
                }

                while ((line = stdError.readLine()) != null) {
                    logger.error("执行指令错误:{}",line);
                    error = true;
                }
                if (error) {
                    throw new MarsRuntimeException(ErrorCodeMessage.RUN_COMMAND_ERROR);
                }
            }
        } catch (Exception e) {
            logger.error("出现异常:",e);
            throw new MarsRuntimeException(ErrorCodeMessage.RUN_COMMAND_ERROR);
        } finally {
            if (null != process) {
                process.destroy();
            }
        }
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
            String index = ES_INDEX_LOGSTASH_PREFIX + DateUtil.DateToString(indexDate, DateStyle.YYYYMMDD_DOT);
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
        String lastIndex = ES_INDEX_LOGSTASH_PREFIX + DateUtil.DateToString(to, DateStyle.YYYYMMDD_DOT);
        String lastSnapshotIndex = lastIndex + ES_INDEX_SNAPSHOT_RESTORE;
        if (existIndexes.contains(lastIndex) && !indexes.contains(lastIndex)) {
            indexes.add(lastIndex);
        }
        if (existIndexes.contains(lastSnapshotIndex) && !indexes.contains(lastSnapshotIndex)) {
            indexes.add(lastSnapshotIndex);
        }
        return indexes.toArray(new String[0]);
    }

    private BoolQueryBuilder getQueryBuilder(LogQuery logQuery) {
        //日志时间范围查询设置
        QueryBuilder timeFilter = QueryBuilders.rangeQuery("@timestamp").from(logQuery.getLogDateStart())
                .to(logQuery.getLogDateEnd());
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().filter(timeFilter)
                .filter(QueryBuilders.termQuery("namespace_name", logQuery.getNamespace()));
        queryBuilder.filter(QueryBuilders.termQuery("deploy_name", logQuery.getDeployment()));
        if (StringUtils.isNotBlank(logQuery.getContainer())) {
            queryBuilder.filter(QueryBuilders.termQuery("container_name", logQuery.getContainer()));
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

    /**
     * 根据类型获取 获取日志语句
     * @param session
     * @param logQueryDto
     */
    public String getLogCommand(WebSocketSession session, LogQueryDto logQueryDto) {
        Cluster cluster = null;
        String command = null;
        try {
            if (StringUtils.isNotBlank(logQueryDto.getNamespace()) && !CommonConstant.KUBE_SYSTEM.equalsIgnoreCase(logQueryDto.getNamespace())) {
                cluster = namespaceLocalService.getClusterByNamespaceName(logQueryDto.getNamespace());
            } else if (StringUtils.isNotBlank(logQueryDto.getClusterId())) {
                cluster = clusterService.findClusterById(logQueryDto.getClusterId());
            }
            if (cluster == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
            }
            //标准输出
            if (logQueryDto.getLogSource() == LogService.LOG_TYPE_STDOUT) {
                AssertUtil.notBlank(logQueryDto.getContainer(), DictEnum.CONTAINER);
                command = MessageFormat.format("kubectl logs {0} -c {1} -n {2} --tail={3} -f --server={4} --token={5} --insecure-skip-tls-verify=true",
                        logQueryDto.getPod(), logQueryDto.getContainer(), logQueryDto.getNamespace(), MAX_LOG_LINES, cluster.getApiServerUrl(), cluster.getMachineToken());
            } else if (logQueryDto.getLogSource() == LogService.LOG_TYPE_LOGFILE) {
                AssertUtil.notBlank(logQueryDto.getLogDir(), DictEnum.LOG_DIR);
                AssertUtil.notBlank(logQueryDto.getLogFile(), DictEnum.LOG_FILE);
                //kubectl exec webapi-6cf47949c8-kwddh -n kube-system -- tail -200f /opt/logs/webapi-info.2018-06-29.log
                command = MessageFormat.format("kubectl exec {0} -n {1} --server={2} --token={3} --insecure-skip-tls-verify=true -- tail -f -n {4} {5}/{6}",
                        logQueryDto.getPod(), logQueryDto.getNamespace(), cluster.getApiServerUrl(), cluster.getMachineToken(), MAX_LOG_LINES, logQueryDto.getLogDir(), logQueryDto.getLogFile());
            }
        }catch (Exception e){
            logger.error("出现异常:",e);
            throw new MarsRuntimeException(ErrorCodeMessage.RUN_COMMAND_ERROR);
        }
        return command;
    }




}
