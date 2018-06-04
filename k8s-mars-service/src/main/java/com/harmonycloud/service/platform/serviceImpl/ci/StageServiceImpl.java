package com.harmonycloud.service.platform.serviceImpl.ci;

import com.alibaba.druid.pool.DruidDataSource;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DockerfileTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.RepositoryTypeEnum;
import com.harmonycloud.common.enumm.StageTemplateTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ci.*;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.sonarqube.webapi.client.SonarProjectService;
import com.harmonycloud.sonarqube.webapi.client.SonarQualitygatesService;
import com.harmonycloud.sonarqube.webapi.client.SonarUserTokensService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by anson on 17/7/13.
 */
@Service
public class StageServiceImpl implements StageService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StageServiceImpl.class);

    @Autowired
    StageMapper stageMapper;

    @Autowired
    StageTypeMapper stageTypeMapper;

    @Autowired
    StageBuildMapper stageBuildMapper;

    @Autowired
    DockerFileMapper dockerFileMapper;

    @Autowired
    JobMapper jobMapper;

    @Autowired
    BuildEnvironmentMapper buildEnvironmentMapper;

    @Autowired
    JobService jobService;

    @Autowired
    DockerFileJobStageMapper dockerFileJobStageMapper;

    @Autowired
    private SonarProjectService sonarProjectService;

    @Autowired
    private SonarQualitygatesService sonarQualitygatesService;

    @Autowired
    private SonarUserTokensService sonarUserTokensService;

    @Autowired
    private TriggerService triggerService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private DockerFileService dockerFileService;

    @Autowired
    private StageTypeService stageTypeServce;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private StageBuildService stageBuildService;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;

    @Value("${sonar.url}")
    private String sonarUrl;

    @Autowired
    private DruidDataSource dataSource;

    @Autowired
    private BuildEnvironmentService buildEnvironmentService;

    @Autowired
    private DependenceServiceImpl dependenceService;

    @Autowired
    private JobBuildService jobBuildService;

    private long sleepTime = 2000L;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer addStage(StageDto stageDto) throws Exception{
        verifyResource(stageDto);
        verifyTag(stageDto);
        //increace order for all stages behind this stage
        stageMapper.increaseStageOrder(stageDto.getJobId(), stageDto.getStageOrder());
        Stage stage = stageDto.convertToBean();
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType()){
            stage.setCredentialsPassword(DesUtil.encrypt(stage.getCredentialsPassword(), null));
        }else if(StageTemplateTypeEnum.CUSTOM.getCode() == stageDto.getStageTemplateType()){
            if(stageDto.getBuildEnvironmentId() > 0){
                stage.setEnvironmentChange(true);
            }
        }

        stage.setCreateTime(new Date());

        stage.setUpdateTime(new Date());
        stageMapper.insertStage(stage);

        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType()){
            createOrUpdateCredential(stage.getId(), stage.getCredentialsUsername(), stageDto.getCredentialsPassword());
        }
        ActionReturnUtil result = jobService.updateJenkinsJob(stageDto.getJobId());
        if(!result.isSuccess()){
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_ADD_ERROR);
        }
        return stage.getId();
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStage(StageDto stageDto) throws Exception{
        verifyResource(stageDto);
        verifyTag(stageDto);
        Stage stage = stageDto.convertToBean();
        stage.setUpdateTime(new Date());
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType()) {
            stage.setCredentialsPassword(DesUtil.encrypt(stage.getCredentialsPassword(), null));
        }else if(StageTemplateTypeEnum.CUSTOM.getCode() == stageDto.getStageTemplateType()){
            if(stageDto.getBuildEnvironmentId() > 0){
                stage.setEnvironmentChange(true);
            }
        }

        stageMapper.updateStage(stage);

        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType()) {
            createOrUpdateCredential(stage.getId(), stage.getCredentialsUsername(), stageDto.getCredentialsPassword());
        }

        if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stageDto.getStageTemplateType() && DockerfileTypeEnum.PLATFORM.ordinal() == stageDto.getDockerfileType()){
            DockerFileJobStage dockerFileJobStage = new DockerFileJobStage();
            dockerFileJobStage.setStageId(stage.getId());
            dockerFileJobStage.setJobId(stage.getJobId());
            dockerFileJobStage.setDockerFileId(stageDto.getDockerfileId());
        }
        ActionReturnUtil result = jobService.updateJenkinsJob(stageDto.getJobId());
        if(!result.isSuccess()){
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_UPDATE_ERROR);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteStage(Integer id) throws Exception {
        Stage stage = stageMapper.queryById(id);
        stageMapper.deleteStage(id);
        stageMapper.decreaseStageOrder(stage.getJobId(), stage.getStageOrder());
        //dockerFileJobStageMapper.deleteDockerFileByStageId(id);
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()) {
            deleteCredentials(stage);
        }
        ActionReturnUtil result = jobService.updateJenkinsJob(stage.getJobId());
        if(!result.isSuccess()){
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_DELETE_ERROR);
        }
    }

    @Override
    public StageDto stageDetail(Integer id) throws Exception {
        Stage stage = stageMapper.queryById(id);
        StageDto stageDto = new StageDto();
        if(null == stage){
            stageDto = null;
        }else {
            stageDto.convertFromBean(stage);
        }
        StageType stageType = stageTypeMapper.queryById(stageDto.getStageTypeId());
        stageDto.setStageTypeName(stageType.getName());
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()){
            stageDto.setCredentialsPassword(DesUtil.decrypt(stageDto.getCredentialsPassword(), null));
        }else if(StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType() && stage.getOriginStageId() != null){
            Stage originStage = stageMapper.queryById(stage.getOriginStageId());
            if(originStage != null) {
                stageDto.setOriginJobId(originStage.getJobId());
            }
        }
        return stageDto;
    }

    @Override
    public List<StageType> listStageType(String type) throws Exception{
        return stageTypeServce.queryByType(type);
    }

    @Override
    public ActionReturnUtil addStageType(StageType stageType) {
        //stageType.setUserDefined(true);
        stageType.setTemplateType(CommonConstant.NUM_THREE);
        stageTypeMapper.insertStageType(stageType);
        Map data = new HashMap<>();
        data.put("id",stageType.getId());
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public ActionReturnUtil deleteStageType(Integer id) throws Exception{
        List<Stage> stageList = stageMapper.queryByStageTypeId(id);
        if(null != stageList && stageList.size()>0){
            throw new Exception("该类型被其他流程使用，无法删除。");
        }
        stageTypeMapper.deleteStageType(id);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getBuildList(Integer id, Integer pageSize, Integer page) {
        List stageBuildMapList = new ArrayList<>();
        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setStageId(id);
        int total = stageBuildMapper.countByObject(stageBuildCondition);
        List<StageBuild> stageBuildList = stageBuildMapper.queryByObjectWithPagination(stageBuildCondition, pageSize*(page-1), pageSize);
        for(StageBuild stageBuild:stageBuildList) {
            Map stageBuildMap = new HashMap<>();
            stageBuildMap.put("stageId", stageBuild.getStageId());
            stageBuildMap.put("name", stageBuild.getStageName());
            stageBuildMap.put("buildStatus", stageBuild.getStatus());
            stageBuildMap.put("buildNum", stageBuild.getBuildNum());
            stageBuildMap.put("buildTime", stageBuild.getStartTime());
            stageBuildMap.put("duration", stageBuild.getDuration());
            stageBuildMap.put("stageTemplateTypeId", stageBuild.getStageTemplateTypeId());
            stageBuildMap.put("testUrl", stageBuild.getTestUrl());
            stageBuildMap.put("testResult", stageBuild.getTestResult());
            stageBuildMapList.add(stageBuildMap);
        }
        Map data = new HashMap<>();
        data.put("total", total);
        data.put("pageSize", pageSize);
        data.put("page", page);
        data.put("totalPage", Math.ceil(1.0 * total/pageSize));
        data.put("buildList",stageBuildMapList);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public ActionReturnUtil listBuildEnvironemnt() {
        List<BuildEnvironment> buildEnvironmentList = buildEnvironmentMapper.queryAll();
        List data = new ArrayList<>();
        for(BuildEnvironment buildEnvironment : buildEnvironmentList){
            Map buildEnvMap = new HashMap<>();
            buildEnvMap.put("id", buildEnvironment.getId());
            buildEnvMap.put("name",buildEnvironment.getName());
            data.add(buildEnvMap);
        }
        return ActionReturnUtil.returnSuccessWithData(data);
    }


    @Override
    public List<Map> getStageBuildFromJenkins(Job job, Integer buildNum) throws Exception {
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        ActionReturnUtil result;
        if(buildNum == null || buildNum == 0){
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/lastBuild/wfapi/describe", null, null, false);
        }else{
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName()  + "/" + buildNum + "/wfapi/describe", null, null, false);
        }
        if(result.isSuccess()) {
            String data = (String) result.get("data");
            Map dataMap = JsonUtil.convertJsonToMap(data);
            return (List<Map>) dataMap.get("stages");
        }else{
            logger.error("获取构建信息失败", result.getData());
            throw new Exception();
        }
    }

    @Override
    public void stageBuildSync(Job job, Integer buildNum, Map stageMap, int stageOrder) throws Exception{
        JobBuild jobBuildCondition = new JobBuild();
        jobBuildCondition.setJobId(job.getId());
        jobBuildCondition.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildService.queryByObject(jobBuildCondition);
        JobBuild jobBuild = null;
        if(CollectionUtils.isNotEmpty(jobBuildList)){
            jobBuild = jobBuildList.get(0);
        }
        StageBuild condition = new StageBuild();
        condition.setJobId(job.getId());
        condition.setBuildNum(buildNum);
        condition.setStageOrder(stageOrder);
        List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(condition);
        if(CollectionUtils.isNotEmpty(stageBuildList)) {
            StageBuild stageBuild = stageBuildList.get(0);
            if(!Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()) && !Constant.PIPELINE_STATUS_NOTBUILT.equals(stageBuild.getStatus()) && !Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus())){
                return;
            }
//            stageBuild.setJobId(job.getId());
//            stageBuild.setBuildNum(buildNum);
//            stageBuild.setStageOrder(stageOrder);
            Stage stage = stageMapper.queryById(stageBuild.getStageId());
            if(StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()){
                if(StringUtils.isNotBlank(stageBuild.getStatus()) && !stageBuild.getStatus().equals(Constant.PIPELINE_STATUS_SUCCESS) && !stageBuild.getStatus().equals(Constant.PIPELINE_STATUS_FAILED)){
                    if(jobBuild !=null && Constant.PIPELINE_STATUS_ABORTED.equals(jobBuild.getStatus()) || Constant.PIPELINE_STATUS_FAILED.equals((String) stageMap.get("status"))){
                        stageBuild.setStatus(Constant.PIPELINE_STATUS_FAILED);
                    } else {
                        stageBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
                    }
                }
            }else{
                stageBuild.setStatus(convertStatus((String) stageMap.get("status")));
            }
            if (stageMap.get("startTimeMillis") instanceof Integer) {
                stageBuild.setStartTime(new Timestamp(Long.valueOf((Integer) stageMap.get("startTimeMillis"))));
            } else if (stageMap.get("startTimeMillis") instanceof Long) {
                stageBuild.setStartTime(new Timestamp((Long) stageMap.get("startTimeMillis")));
            }
            stageBuild.setDuration(String.valueOf(stageMap.get("durationMillis")));
            stageBuildMapper.updateByStageOrderAndBuildNum(stageBuild);
            stageBuild.setLog(getStageBuildLogFromJenkins(job, buildNum, (String) stageMap.get("id")));
            stageBuildMapper.updateStageLog(stageBuild);
        }
    }


    @Override
    public void getStageLogWS(WebSocketSession session, Integer id, Integer buildNum) {
        try {
            String existingLog = "";
            Stage stage = stageMapper.queryById(id);
            Job job = jobMapper.queryById(stage.getJobId());
            boolean building = true;
            long duration = 0L;
            while(building && session.isOpen()) {
                long startTime = System.currentTimeMillis();
                List<Map> stageMapList = getStageBuildFromJenkins(job, buildNum);
                if(stageMapList.size() >= stage.getStageOrder()) {
                    Map stageMap = stageMapList.get(stage.getStageOrder() - 1);
                    String log = getStageBuildLogFromJenkins(job, buildNum, (String)stageMap.get("id"));
                    int existLogLength = existingLog.length();
                    String newLog = log.substring(existLogLength);
                    if(!StringUtils.isBlank(newLog) || duration > CommonConstant.CICD_WEBSOCKET_MAX_DURATION) {
                        duration = 0L;
                        existingLog = log;
                        if(session.isOpen()) {
                            session.sendMessage(new TextMessage(newLog));
                        }
                    }
                    if(!Constant.PIPELINE_STATUS_INPROGRESS.equals(stageMap.get("status"))){
                        //building = false;
                    }
                }
                try{
                    Thread.sleep(sleepTime);
                }catch (Exception e){
                    logger.error("获取流水线步骤日志失败", e);
                }
                duration += System.currentTimeMillis() - startTime;
            }
        } catch (Exception e) {
            logger.error("get stage log error, stageId: {}", id, e);
        }finally{
            if(session.isOpen()){
                try {
                    session.close();
                } catch (IOException e) {
                    logger.error("close session error");
                }
            }
        }
    }

    public long countByExample(Stage stage) throws Exception{
        return stageMapper.countByExample(stage);
    }

    public List<Stage> selectByExample(Stage stage) throws Exception{
        return stageMapper.selectByExample(stage);
    }

    public Stage selectByPrimaryKey(Integer id) throws Exception{
        return stageMapper.queryById(id);
    }

    @Override
    public String getStageLog(Integer stageId, Integer buildNum) throws Exception {
        StageBuild stageBuild = new StageBuild();
        stageBuild.setStageId(stageId);
        stageBuild.setBuildNum(buildNum);
        String log = stageBuildService.getStageLogByObject(stageBuild);
        if(StringUtils.isBlank(log)){
            List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(stageBuild);
            if(stageBuildList.size() == 1) {
                stageBuild = stageBuildList.get(0);
                if (!Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()) && !Constant.PIPELINE_STATUS_NOTBUILT.equals(stageBuild.getStatus()) && !Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus())) {
                    Job job = jobService.getJobById(stageBuild.getJobId());
                    List<Map> stageMapList = getStageBuildFromJenkins(job, buildNum);
                    if (stageMapList.size() > stageBuild.getStageOrder()) {
                        Map stageMap = stageMapList.get(stageBuild.getStageOrder() - 1);
                        log = getStageBuildLogFromJenkins(job, buildNum, String.valueOf(stageMap.get("id")));
                        stageBuild.setLog(log);
                        try {
                            stageBuildMapper.updateStageLog(stageBuild);
                        } catch (Exception e) {
                            logger.error("保存步骤日志失败, stageId:{}, buildNum", stageId, buildNum);
                        }
                    }
                }
            }
        }
        return log;
    }

    @Override
    public void insert(Stage stage) throws Exception {
        stageMapper.insertStage(stage);
    }


    private String getStageBuildLogFromJenkins(Job job, Integer buildNum, String stageNodeId) throws Exception{
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() +"/"+ buildNum + "/execution/node/" + stageNodeId + "/wfapi/describe", null, null, false);
        if(result.isSuccess()){
            String data = (String)result.get("data");
            Map dataMap = JsonUtil.convertJsonToMap(data);
            List<Map> stageFlowNodeMapList = (List<Map>)dataMap.get("stageFlowNodes");
            StringBuilder log = new StringBuilder();
            for(Map stageFlowNodeMap : stageFlowNodeMapList){
                result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/" + buildNum + "/execution/node/" + stageFlowNodeMap.get("id") + "/wfapi/log", null, null, false);
                if(result.isSuccess()){
                    data = (String)result.get("data");
                    dataMap = JsonUtil.convertJsonToMap(data);
                    if(null != dataMap.get("text")){
                        log.append(dataMap.get("text"));
                    }
                }
            }
            if(StringUtils.isNotBlank(log.toString())) {
                return (log.toString().replaceAll("</?[^>]+>",""));
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrUpdateCredential(Integer stageId, String username, String password) throws Exception{
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/credentials/store/system/domain/_/credential/" + stageId + "/", null, null, false);
        if(result.isSuccess()){
            updateCredentials(stageId, username, password);
        }else{
            createCredentials(stageId, username, password);
        }
    }

    @Override
    public void updateUserCredentials(String username, String password) throws Exception {
        if(StringUtils.isBlank(username)){
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_CREDENTIALS_USERNAME_NOT_NULL);
        }
        Stage stageCondition = new Stage();
        stageCondition.setCredentialsUsername(username);
        List<Stage> stageList = stageMapper.selectByExample(stageCondition);
        if(CollectionUtils.isNotEmpty(stageList)) {
            stageMapper.updatePasswordByUsername(username, DesUtil.encrypt(password, null));
            for (Stage stage : stageList) {
                if ((stage.getCredentialsPassword() == null && StringUtils.isNotBlank(password)) || (stage.getCredentialsPassword() != null && !stage.getCredentialsPassword().equals(DesUtil.encrypt(password, null)))) {
                    updateCredentials(stage.getId(), username, DesUtil.encrypt(password, null));
                }
            }
        }
    }

    @Override
    public List<Stage> getStageByJobId(Integer jobId) {
        return stageMapper.queryByJobId(jobId);
    }

    private void createCredentials(Integer stageId, String username, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> credentialsMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        params.put("_.scope","scope:GLOBAL");
        params.put("_.username", username);
        params.put("_.password", password);
        params.put("_.id", stageId);
        credentialsMap.put("scope", "GLOBAL");
        credentialsMap.put("username", username);
        credentialsMap.put("password", password);
        credentialsMap.put("id", stageId);
        credentialsMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        credentialsMap.put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        tempMap.put("","0");
        tempMap.put("credentials", credentialsMap);
        params.put("json", JsonUtil.convertToJson(tempMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/createCredentials", null, params, null, 302);
        if (!result.isSuccess()) {
            throw new Exception();
        }
    }

    private void updateCredentials(Integer stageId, String username, String password) throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonMap = new HashMap<>();
        params.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        params.put("_.scope","GLOBAL");
        params.put("_.username", username);
        params.put("_.password", password);
        params.put("_.id", stageId);
        jsonMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        jsonMap.put("scope","GLOBAL");
        jsonMap.put("username", username);
        jsonMap.put("password", password);
        jsonMap.put("id", stageId);
        params.put("json", JsonUtil.convertToJson(jsonMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stageId + "/updateSubmit", null, params, null, 302);
        if (!result.isSuccess()) {
            throw new Exception();
        }
    }

    private void deleteCredentials(Stage stage) throws Exception {
        HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stage.getId() + "/doDelete", null, null, null, 302);
    }

    private String convertStatus(String status){
        if(Constant.PIPELINE_STATUS_INPROGRESS.equals(status)){
            return Constant.PIPELINE_STATUS_BUILDING;
        }else{
            return status;
        }
    }

    private String getProjectName(String repositoryUrl,String repositoryType){
        if(RepositoryTypeEnum.SVN.getType().equalsIgnoreCase(repositoryType)){
            return repositoryUrl.substring(repositoryUrl.lastIndexOf("/")+1);
        }else if(RepositoryTypeEnum.GIT.getType().equalsIgnoreCase(repositoryType)){
            return repositoryUrl.substring(repositoryUrl.lastIndexOf("/")+1,repositoryUrl.lastIndexOf("."));
        }else{
            return "";
        }
    }


    private void verifyTag(StageDto stageDto) throws Exception{
        if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stageDto.getStageTemplateType() && "1".equals(stageDto.getImageTagType())){
            if(stageDto.getImageBaseTag().split("\\.").length != stageDto.getImageIncreaseTag().split("\\.").length){
                throw new Exception("版本输入有误，请重新输入。");
            }
        }
    }

    private void verifyResource(StageDto stageDto) throws Exception{
        Job job = jobService.getJobById(stageDto.getJobId());
        if(job == null){
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_ALREADY_DELETED);
        }
        verifyStageResource(job, stageDto);
    }

    public void verifyStageResource(Job job, StageDto stageDto) throws Exception{
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType() || (StageTemplateTypeEnum.CUSTOM.getCode() == stageDto.getStageTemplateType() && stageDto.getBuildEnvironmentId() != 0 )){
            BuildEnvironment buildEnvironment = buildEnvironmentService.getBuildEnvironment(stageDto.getBuildEnvironmentId());
            if(buildEnvironment == null){
                throw new MarsRuntimeException(ErrorCodeMessage.ENVIRONMENT_ALREADY_DELETED);
            }
        }
        if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stageDto.getStageTemplateType()){
            List<StageDto.Dependence> dependenceList = stageDto.getDependences();
            if(CollectionUtils.isNotEmpty(dependenceList)){
                List<Map> list = dependenceService.listByProjectIdAndClusterId(job.getProjectId(), job.getClusterId(), null);
                for(StageDto.Dependence dependence : dependenceList){
                    boolean dependenceExist = false;
                    for(Map dependenceMap : list){
                        if(dependence.getPvName().equals(dependenceMap.get("pvName"))){
                            dependenceExist = true;
                            break;
                        }
                    }
                    if(!dependenceExist){
                        throw new MarsRuntimeException(ErrorCodeMessage.DEPENDENCE_ALREADY_DELETED);
                    }
                }
            }
        }
        if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stageDto.getStageTemplateType() && DockerfileTypeEnum.PLATFORM.ordinal() == stageDto.getDockerfileType()){
            if(dockerFileService.selectDockerFileById(stageDto.getDockerfileId()) == null){
                throw new MarsRuntimeException(ErrorCodeMessage.DOCKERFILE_ALREADY_DELETED);
            }
        }
        if(StageTemplateTypeEnum.DEPLOY.getCode() == stageDto.getStageTemplateType()){
            if(stageDto.getOriginStageId() != null){
                Stage originStage = stageMapper.queryById(stageDto.getOriginStageId());
                if(originStage == null){
                    throw new MarsRuntimeException(ErrorCodeMessage.ORIGIN_STAGE_NOT_EXIST);
                }
            }
        }
    }
}
