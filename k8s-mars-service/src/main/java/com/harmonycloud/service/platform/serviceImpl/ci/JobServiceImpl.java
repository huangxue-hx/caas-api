package com.harmonycloud.service.platform.serviceImpl.ci;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DockerfileTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.StageTemplateTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;

import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cicd.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.PrivilegeHelper;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ci.*;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.yaml.snakeyaml.Yaml;


import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by anson on 17/5/31.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class JobServiceImpl implements JobService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    JobMapper jobMapper;

    @Autowired
    StageMapper stageMapper;

    @Autowired
    JobBuildMapper jobBuildMapper;

    @Autowired
    StageBuildMapper stageBuildMapper;

    @Autowired
    StageService stageService;

    @Autowired
    HttpSession session;

    @Autowired
    DeploymentsService deploymentsService;

    @Autowired
    DeploymentService deploymentService;

    @Autowired
    ClusterService clusterService;

    @Autowired
    TenantService tenantService;

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    ApplicationDeployService applicationDeployService;

    @Autowired
    ConfigMapService configMapService;

    @Autowired
    DockerFileJobStageMapper dockerFileJobStageMapper;

    @Autowired
    BuildEnvironmentMapper buildEnvironmentMapper;

    @Autowired
    ProjectService projectService;

    @Autowired
    ParameterService parameterService;

    @Autowired
    ServiceService serviceService;

    @Autowired
    BlueGreenDeployService blueGreenDeployService;
    @Autowired
    PersistentVolumeService persistentVolumeService;

    @Autowired
    IntegrationTestService integrationTestService;

    @Autowired
    NamespaceLocalService namespaceLocalService;

    @Autowired
    StageTypeService stageTypeService;

    @Autowired
    StageBuildService stageBuildService;

    @Autowired
    RoleLocalService roleLocalService;

    @Autowired
    JobBuildService jobBuildService;

    @Autowired
    TriggerService triggerService;

    @Autowired
    DockerFileService dockerFileService;

    @Autowired
    PrivilegeHelper privilegeHelper;

    @Value("#{propertiesReader['web.url']}")
    private String webUrl;

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;

    @Autowired
    private DruidDataSource dataSource;

    @Autowired
    private DataSourceTransactionManager transactionManager;
    @Autowired
    SecretService secretService;


    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Integer createJob(JobDto jobDto) throws Exception {
        Job job;
        if(jobDto.getCopyId() == null) {
            job = jobDto.convertToBean();
        }else{
            job = this.getJobById(jobDto.getCopyId());
            if(job == null){
                throw new MarsRuntimeException(ErrorCodeMessage.COPIED_PIPELINE_NOT_EXIST);
            }
            job.setName(jobDto.getName());
            job.setNotification(jobDto.isNotification());
            job.setSuccessNotification(jobDto.isSuccessNotification());
            job.setFailNotification(jobDto.isFailNotification());
            job.setMail(JsonUtil.convertToJson(jobDto.getMail()));
        }

        String projectName = getProjectNameByProjectId(jobDto.getProjectId());
        String clusterName = getClusterNameByClusterId(jobDto.getClusterId());

        //确认项目、集群目录存在
        FolderJob folderJob = checkFolderJobExist(projectName, clusterName);

        // validate jobname
        validateJobName(jobDto.getName(), projectName, clusterName);

        String uuid = UUIDUtil.getUUID();
        job.setUuid(uuid);

        String username = (String)session.getAttribute("username");
        job.setCreateUser(username);
        job.setCreateTime(new Date());
        jobMapper.insertJob(job);
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        if(jobDto.getCopyId() == null) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("stageList", new ArrayList<>());
            dataModel.put("job", job);
            dataModel.put("apiUrl", apiUrl);
            String script = TemplateUtil.generate("pipeline.ftl", dataModel);
            dataModel.put("script", script);
            String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            try {
                jenkinsServer.createJob(folderJob, job.getName(), body);
            } catch (Exception e) {
                logger.error("新建流水线失败", e);
                throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_CREATE_ERROR);
            }
        }else{
            Trigger trigger = triggerService.getTrigger(jobDto.getCopyId());
            if(trigger != null){
                trigger.setJobId(job.getId());
                triggerService.insertTrigger(trigger);
            }
            ParameterDto parameterDto = parameterService.getParameter(jobDto.getCopyId());
            parameterDto.setJobId(job.getId());
            parameterService.insertParameter(parameterDto);

            Stage stageExample = new Stage();
            stageExample.setJobId(jobDto.getCopyId());
            List<Stage> stageList = stageService.selectByExample(stageExample);
            for(Stage stage : stageList){
                stage.setJobId(job.getId());
                stage.setCreateTime(DateUtil.getCurrentUtcTime());
                stage.setCreateUser(username);
                stageService.insert(stage);
                if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()){
                    stageService.createOrUpdateCredential(stage.getId(), stage.getCredentialsUsername(), DesUtil.decrypt(stage.getCredentialsPassword(), null));
                }
            }
            jenkinsServer.createJob(folderJob, job.getName(), generateJobBody(job));
        }
        return job.getId();
    }


    @Override
    public ActionReturnUtil updateJob(Job job) {
        String username = (String)session.getAttribute("username");
        job.setUpdateUser(username);
        job.setUpdateTime(new Date());
        jobMapper.updateJob(job);

        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Map<String, Object> dataModel = new HashMap<>();
        try {
            //dataModel.put("tag", job.getImageTag());
            //dataModel.put("script", generateScript(job));
            String body = null;
            dataModel.put("job", job);
            body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void deleteJob(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if(null == job){
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NOT_EXIST);
        }
        String projectName = getProjectNameByProjectId(job.getProjectId());
        String clusterName = getClusterNameByClusterId(job.getClusterId());

        //删除数据库中的流水线与步骤数据
        jobMapper.deleteJobById(id);
        stageMapper.deleteStageByJob(id);

        //删除jenkins中的流水线
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        try {
            jenkinsServer.deleteJob(folderJob, job.getName());
        } catch(Exception e){
            logger.error("删除流水线失败", e);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_DELETE_ERROR);
        }
    }

    @Override
    public void validateName(String jobName, String projectId, String clusterId) throws Exception{
        String projectName = getProjectNameByProjectId(projectId);
        String clusterName = getClusterNameByClusterId(clusterId);
        checkFolderJobExist(projectName, clusterName);
        validateJobName(jobName, projectName, clusterName);
    }

    @Override
    public List getJobList(String projectId, String clusterId, String type, String jobName) throws Exception{
        String projectName = getProjectNameByProjectId(projectId);

        List<Map> jobList = new ArrayList();

        Map<String, Object> params = new HashMap<>();
        params.put("tree", "jobs[name,color,lastBuild[number,building,result,timestamp],builds[result]]");
        ActionReturnUtil result;
        if(StringUtils.isNotEmpty(clusterId)) {
            Cluster cluster = clusterService.findClusterById(clusterId);
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + cluster.getName() + "/api/xml", null, params, false);
            jobList.addAll(getJobsByJenkinsResult(result, jobName, projectId, clusterId, cluster.getAliasName(), type));
        }else{
            Map<String, List<Map>> jobListMap = new HashMap();
            List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
            if(CollectionUtils.isNotEmpty(clusterList)) {
                CountDownLatch countDownLatch = new CountDownLatch(clusterList.size());
                for (Cluster cluster : clusterList) {
                    NewCachedThreadPool threadPool = NewCachedThreadPool.init();
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + cluster.getName() + "/api/xml", null, params, false);
                                jobListMap.put(cluster.getId(), getJobsByJenkinsResult(result, jobName, projectId, cluster.getId(), cluster.getAliasName(), type));
                            }catch(Exception e){
                                logger.error("获取流水线失败", e);
                            }finally {
                                countDownLatch.countDown();
                            }
                        }
                    });
                }
                countDownLatch.await();
                for (Cluster cluster : clusterList) {
                    jobList.addAll(jobListMap.get(cluster.getId()));
                }
            }
        }
        //数据权限过滤
        Iterator it = jobList.iterator();
        while(it.hasNext()){
            Map job = (Map)it.next();
            PipelinePrivilegeDto pipelinePrivilegeDto = new PipelinePrivilegeDto();
            pipelinePrivilegeDto.setName((String)job.get("name"));
            if (privilegeHelper.isFiltered(pipelinePrivilegeDto)) {
                it.remove();
            }
        }
        return jobList;
    }

    @Override
    public ActionReturnUtil getJobDetail(Integer id) throws Exception {
        Job dbJob = jobMapper.queryById(id);
        String projectName = getProjectNameByProjectId(dbJob.getProjectId());
        String clusterName = getClusterNameByClusterId(dbJob.getClusterId());
        Map job = new HashMap();
        job.put("id", id);
        job.put("jobName", dbJob.getName());
        job.put("type",dbJob.getType());
        job.put("tenant", dbJob.getTenant());
        job.put("clusterId", dbJob.getClusterId());
        //String jenkinsJobName = dbJob.getTenant() + "_" + dbJob.getName();
        Integer buildNum = null;
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + dbJob.getName() + "/lastBuild/api/json", null, null, false);
        List<Map> jenkinsStageMapList = new ArrayList<>();
        if(result.isSuccess()){
            Map map = JsonUtil.convertJsonToMap((String)result.get("data"));
            if(false == (boolean)map.get("building")){
                job.put("lastBuildStatus", map.get("result"));
            }else{
                job.put("lastBuildStatus", Constant.PIPELINE_STATUS_BUILDING);
            }
            job.put("lastBuildDuration", map.get("duration"));
            job.put("lastBuildTime", new Timestamp((Long)map.get("timestamp")));
            buildNum = (Integer)map.get("number");
            jenkinsStageMapList = stageService.getStageBuildFromJenkins(dbJob, 0);
        }else{
            job.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
            job.put("lastBuildDuration", null);
            job.put("lastBuildTime", null);
        }
        job.put("buildNum", buildNum);

        //返回工作空间
        job.put("workspace", "/home/workspace/" + projectName + "/" + clusterName + "/" + dbJob.getName());


/*
         String jenkinsJobName = dbJob.getTenant() + "_" + dbJob.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "name,builds[number,building,result,duration,timestamp],lastBuild[number,building,result,duration,timestamp]");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/api/xml", null, params, false);
       if (result.isSuccess()) {
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));

            Map buildMap;
            List<Map> jenkinsBuildList = new ArrayList<>();
            List buildList = new ArrayList<>();


            Map rootMap = (Map) jenkinsDataMap.get("workflowJob");
            if (rootMap.get("build") instanceof Map) {
                jenkinsBuildList.add((Map) rootMap.get("build"));
            } else if (rootMap.get("build") instanceof List) {
                jenkinsBuildList.addAll((List) rootMap.get("build"));
            }
            for (Map jenkinsBuild : jenkinsBuildList) {
                buildMap = new HashMap<>();
                buildMap.put("build_num", jenkinsBuild.get("number"));
                if ("false".equalsIgnoreCase((String) jenkinsBuild.get("building"))) {
                    buildMap.put("build_status", jenkinsBuild.get("result"));
                } else {
                    buildMap.put("build_status", "BUILDING");
                }
                if (jenkinsBuild.get("timestamp") != null) {
                    buildMap.put("start_time", df.format(new Timestamp(Long.valueOf((String) jenkinsBuild.get("timestamp")))));
                }
                buildMap.put("duration", jenkinsBuild.get("duration"));
                buildList.add(buildMap);
            }
            job.put("build_list", buildList);
*/

//            List<Job> dbJobList = jobMapper.select(tenantName, jobName, null);
//            if(dbJobList != null && dbJobList.size()==1){
//                Job dbJob = dbJobList.get(0);
//                job.put("projectType", dbJob.getProjectType());
//                job.put("buildType", dbJob.getBuildType());
//                job.put("repositoryType", dbJob.getRepositoryType());
//
//            }

//            JobBuild jobBuildCondition = new JobBuild();
//            jobBuildCondition.setJobId(dbJob.getId());
//            jobBuildCondition.setBuildNum(dbJob.getLastBuildNum());
//            List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);
//            if(null != jobBuildList && jobBuildList.size() == 1) {
//                JobBuild jobBuild = jobBuildList.get(0);
//                job.put("lastBuildStatus", jobBuild.getStatus());
//                job.put("lastBuildTime", jobBuild.getStartTime());
//                job.put("lastBuildDuration", jobBuild.getDuration());
//            }else {
//                job.put("lastBuildStatus", "NOTBUILT");
//                job.put("lastBuildTime", null);
//                job.put("lastBuildDuration", null);
//            }


        List<Map> stageMapList = new ArrayList<>();

        //get stage type info of stages for the job
        List<Stage> stageList = stageMapper.queryByJobId(id);
        List<StageType> stageTypeList = stageTypeService.queryByType(dbJob.getType());
        Map stageTypeMap = new HashMap<>();
        for(StageType stageType : stageTypeList){
            stageTypeMap.put(stageType.getId(),stageType.getName());
        }

//            //get last build info of stages for the job
//            StageBuild stageBuildCondition = new StageBuild();
//            stageBuildCondition.setJobId(dbJob.getId());
//            stageBuildCondition.setBuildNum(dbJob.getLastBuildNum());
//            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);


        for(Stage stage:stageList){
            Map stageMap = new HashMap<>();
            stageMap.put("id",stage.getId());
            stageMap.put("stageName",stage.getStageName());
            stageMap.put("stageType",stageTypeMap.get(stage.getStageTypeId()));
            stageMap.put("stageOrder",stage.getStageOrder());

            for(Map jenkinsStageMap : jenkinsStageMapList){
                if(String.valueOf(stage.getId()).equals(jenkinsStageMap.get("name").toString().split("-")[1])){
                    stageMap.put("lastBuildStatus", convertStatus((String)jenkinsStageMap.get("status")));
                    stageMap.put("lastBuildTime", new Timestamp((Long)jenkinsStageMap.get("startTimeMillis")));
                    stageMap.put("lastBuildDuration", jenkinsStageMap.get("durationMillis"));
                    break;
                }
            }
            if(StringUtils.isEmpty((String)stageMap.get("lastBuildStatus"))){
                if(Constant.PIPELINE_STATUS_BUILDING.equals(job.get("lastBuildStatus")) && stage.getStageOrder() == 1) {
                    stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_WAITING);
                }else{
                    stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                }
                stageMap.put("lastBuildTime", null);
                stageMap.put("lastBuildDuration", null);
            }
            stageMap.put("buildNum", buildNum);
            //StageBuild stageBuild = (StageBuild)stageBuildMap.get(stage.getId());
//            if(stageBuild == null){
//                stageMap.put("lastBuildStatus", "NOTBUILT");
//                stageMap.put("lastBuildTime", null);
//                stageMap.put("lastBuildDuration", null);
//            }else {
//                stageMap.put("lastBuildStatus", stageBuild.getStatus());
//                stageMap.put("lastBuildTime", stageBuild.getStartTime());
//                stageMap.put("lastBuildDuration", stageBuild.getDuration());
//            }
            stageMapList.add(stageMap);
        }



//            Map stageBuildMap = new HashMap<>();
//            for(StageBuild stageBuild : stageBuildList){
//                stageBuildMap.put(stageBuild.getStageId(),stageBuild);
//            }



        job.put("stageList",stageMapList);

        return ActionReturnUtil.returnSuccessWithData(job);
//        }
//        else {
//            return ActionReturnUtil.returnError();
//        }
    }

    @Override
    public Integer build(Integer id, List<Map<String, Object>> parameters) throws Exception{
        Job job = jobMapper.queryById(id);
        String jobName = job.getName();
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        Integer lastBuildNumber = null;
        Map<String, Object> params;
        List<Stage> stageList = stageMapper.queryByJobId(id);
        validateJob(job, stageList);
        Map tagMap = new HashMap<>();

        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        //if (result.isSuccess()) {
        params = new HashMap<>();
        //   params.put("tree","number,timestamp");
        //  result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/xml", null, params, false);
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        JobWithDetails jobWithDetails = jenkinsServer.getJob(folderJob, job.getName());
        //ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/api/json", null, null, false);

        //if(result.isSuccess()){
        //Map dataMap = JsonUtil.convertJsonToMap((String) result.get("data"));
        lastBuildNumber =  jobWithDetails.getNextBuildNumber();
        jobMapper.updateLastBuildNum(id, lastBuildNumber);
        JobBuild jobBuild = new JobBuild();
        jobBuild.setJobId(id);
        jobBuild.setBuildNum(lastBuildNumber);
        List<JobBuild> jobBuildList = jobBuildService.queryByObject(jobBuild);
        if(CollectionUtils.isEmpty(jobBuildList)){
            jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
            jobBuild.setStartUser((String)session.getAttribute("username"));
            jobBuildService.insert(jobBuild);
        }else{
            jobBuild = jobBuildList.get(0);
            jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
            jobBuild.setStartUser((String)session.getAttribute("username"));
            jobBuildService.update(jobBuild);
        }

        Map<Integer, StageType> stageTypeMap = new HashMap<>();
        List<StageType> stageTypeList = stageTypeService.queryByType(job.getType());
        for(StageType stageType : stageTypeList){
            stageTypeMap.put(stageType.getId(),stageType);
        }

        for(Stage stage : stageList){
            StageBuild stageBuild = new StageBuild();
            stageBuild.setJobId(id);
            stageBuild.setStageId(stage.getId());
            stageBuild.setStageName(stage.getStageName());
            stageBuild.setStageOrder(stage.getStageOrder());
            stageBuild.setStageTypeId(stage.getStageTypeId());
            StageType stageType= stageTypeMap.get(stage.getStageTypeId());
            stageBuild.setStageType(stageType.getName());
            stageBuild.setStageTemplateTypeId(stageType.getTemplateType());
            stageBuild.setBuildNum(lastBuildNumber);
            stageBuild.setStatus(Constant.PIPELINE_STATUS_NOTBUILT);
            if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()){
                if(CommonConstant.IMAGE_TAG_RULE.equals(stage.getImageTagType())) {
                    stage.setImageBaseTag(generateTag(stage));
                    stageMapper.updateStage(stage);
                }
            }else if(StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()) {
                updateDeployStageBuild(stage, stageBuild);

            }
            stageBuildMapper.insert(stageBuild);
        }

        //}
//            if(result.isSuccess()){
//                Map body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
//                Map root = (Map)body.get("workflowRun");
//                Integer lastBuildNumber = Integer.valueOf((String) root.get("number"));
//                Date startTime = new Timestamp(Long.valueOf((String) root.get("timestamp")));
//                jobMapper.updateLastBuildNum(id, lastBuildNumber);
//                JobBuild jobBuild = new JobBuild();
//                jobBuild.setJobId(id);
//                jobBuild.setBuildNum(lastBuildNumber);
//                jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
//                jobBuild.setStartTime(startTime);
//                jobBuild.setStartUser((String)session.getAttribute("username"));
//                jobBuildMapper.insert(jobBuild);
//
//                Map<Integer, String> stageTypeMap = new HashMap<>();
//                List<StageType> stageTypeList = stageTypeMapper.queryByTenant(job.getTenant());
//                for(StageType stageType : stageTypeList){
//                    stageTypeMap.put(stageType.getId(),stageType.getName());
//                }
//
//                for(Stage stage : stageList){
//                    StageBuild stageBuild = new StageBuild();
//                    stageBuild.setJobId(id);
//                    stageBuild.setStageId(stage.getId());
//                    stageBuild.setStageName(stage.getStageName());
//                    stageBuild.setStageOrder(stage.getStageOrder());
//                    stageBuild.setStageType(stageTypeMap.get(stage.getStageTypeId()));
//                    stageBuild.setBuildNum(lastBuildNumber);
//                    stageBuild.setStatus(Constant.PIPELINE_STATUS_WAITING);
//                    if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()) {
//                        stageBuild.setImage(stage.getImageName() + ":" + tagMap.get(stage.getImageName()));
//                    }
//                    stageBuildMapper.insert(stageBuild);
//                }
//                Map data = new HashMap<>();
//                data.put("buildNum", lastBuildNumber);
//                return ActionReturnUtil.returnSuccessWithData(data);
//            }
        //}
        params.put("delay", "0sec");
        //构建时带上参数，若为空则使用自定义参数
        if(CollectionUtils.isNotEmpty(parameters)){
            for(Map<String, Object> parameterMap : parameters){
                params.put((String)parameterMap.get("name"), String.valueOf(parameterMap.get("value")));
            }
        }else{
            ParameterDto parameterDto = parameterService.getParameter(id);
            if(CollectionUtils.isNotEmpty(parameterDto.getParameters())){
                for(Map<String, Object> parameterMap : parameterDto.getParameters()){
                    if(CommonConstant.STRING_TYPE_PARAMETER == (Integer)parameterMap.get("type")) {
                        params.put((String) parameterMap.get("name"), String.valueOf(parameterMap.get("value")));
                    }else if(CommonConstant.CHOICE_TYPE_PARAMETER == (Integer)parameterMap.get("type")){
                        String value = (String)parameterMap.get("value");
                        String[] choices = value.split("\n");
                        params.put((String) parameterMap.get("name"), choices[0]);
                    }
                }
            }
        }
        try {
            //jobWithDetails.build(params);
            ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + jobName + "/buildWithParameters", null, params, null, null);
        } catch (Exception e){
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_BUILD_ERROR);
        }
        //result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/buildWithParameters", null, params, null, null);
        //if(result.isSuccess()){
        return lastBuildNumber;
    }


    @Override
    public ActionReturnUtil stopBuild(Integer jobId, String buildNum) throws Exception{
        Job job = jobMapper.queryById(jobId);
        String jobName = job.getName();
        String projectName = getProjectNameByProjectId(job.getProjectId());
        String clusterName = getClusterNameByClusterId(job.getClusterId());
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + jobName + "/" + buildNum + "/stop", null, null, null, 302);
        if (result.isSuccess()) {
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil deleteBuild(Integer id, String buildNum) throws Exception {
        Job job = jobMapper.queryById(id);
        String jenkinsJobName = job.getTenant() + job.getName();
        jobBuildMapper.deleteByJobId(id);
        stageBuildMapper.deleteByJobId(id);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/" + buildNum + "/doDelete", null, null, null, null);
        if (result.isSuccess()) {
            return ActionReturnUtil.returnSuccess();
        }else{
            throw new Exception("删除失败");
        }

    }

    @Override
    public ActionReturnUtil validateCredential(String repositoryType, String repositoryUrl, String username, String password) {
        return ScmUtil.checkCredentials(repositoryType, repositoryUrl, username, password);
    }

    @Override
    public ActionReturnUtil getBuildList(Integer id, Integer pageSize, Integer page) throws Exception {
        Job job = jobMapper.queryById(id);
        List<Map> buildList = new ArrayList<>();
        JobBuild jobBuildCondition = new JobBuild();
        jobBuildCondition.setJobId(id);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObjectWithPagination(jobBuildCondition, (page - 1) * pageSize, pageSize);
        int total = jobBuildMapper.countByObject(jobBuildCondition);
        List<Stage> stageList = stageMapper.queryByJobId(id);
        Map<Integer, Stage> stageMap = new HashMap<>();
        for(Stage stage: stageList){
            stageMap.put(stage.getId(),stage);
        }
        Map jobBuildMap = new HashMap<>();
        for(JobBuild jobBuild:jobBuildList){
            jobBuildMap.put(jobBuild.getBuildNum(),jobBuild);
        }
        for(JobBuild jobBuild:jobBuildList){
            int staticCount = 0;
            int staticSuccessCount = 0;
            int testCount = 0;
            int testSuccessCount =0;
            Integer jobDuration = 0;
            Map buildMap = new HashMap();
            if(Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus())){
                jobBuild = syncJobStatus(job, jobBuild.getBuildNum());
            }
            buildMap.put("buildNum", jobBuild.getBuildNum());
            buildMap.put("buildStatus", jobBuild.getStatus());
            buildMap.put("buildTime", jobBuild.getStartTime());
            buildMap.put("duration", jobBuild.getDuration());
            StageBuild stageBuildCondition = new StageBuild();
            stageBuildCondition.setJobId(id);
            stageBuildCondition.setBuildNum(jobBuild.getBuildNum());
            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
            int stageCount = stageBuildList.size();
//            List stageBuildMapList = new ArrayList<>();
            for(StageBuild stageBuild : stageBuildList){
//                Map stageBuildMap = new HashMap<>();
                if(!Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus()) && (Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus()) || Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()))){
                    allStageStatusSync(job, jobBuild.getBuildNum());
                    stageBuildMapper.updateWaitingStage(job.getId(), jobBuild.getBuildNum());
                    stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
//                    stageBuildMapList = new ArrayList<>();
                    jobDuration = 0;
                    staticCount = 0;
                    staticSuccessCount = 0;
                    testCount = 0;
                    testSuccessCount =0;
                    for(StageBuild newStageBuild : stageBuildList){
//                        stageBuildMap = new HashMap<>();
//                        stageBuildMap.put("stageId", newStageBuild.getStageId());
//                        stageBuildMap.put("stageName", newStageBuild.getStageName());
//                        stageBuildMap.put("stageOrder", newStageBuild.getStageOrder());
//                        stageBuildMap.put("stageType", newStageBuild.getStageType());
//                        stageBuildMap.put("stageTypeId", newStageBuild.getStageTypeId());
//                        stageBuildMap.put("stageTemplateTypeId", newStageBuild.getStageTemplateTypeId());
//                        if(Constant.PIPELINE_STATUS_WAITING.equals(newStageBuild.getStatus())){
//                            stageBuildMap.put("buildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
//                        }else {
//                            stageBuildMap.put("buildStatus", newStageBuild.getStatus());
//                        }
//                        stageBuildMap.put("buildNum", newStageBuild.getBuildNum());
//                        stageBuildMap.put("buildTime", newStageBuild.getStartTime());
//                        stageBuildMap.put("duration", newStageBuild.getDuration());
//                        stageBuildMap.put("log", newStageBuild.getLog());
//                        stageBuildMap.put("testResult", newStageBuild.getTestResult());
//                        stageBuildMap.put("testUrl", newStageBuild.getTestUrl());
                        if(StageTemplateTypeEnum.CODESCAN.getCode() == newStageBuild.getStageTemplateTypeId()){
                            staticCount++;
                            if(newStageBuild.getTestResult() != null && newStageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0){
                                staticSuccessCount++;
                            }
                        }else if(StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == newStageBuild.getStageTemplateTypeId()){
                            testCount++;
                            if(newStageBuild.getTestResult() != null && newStageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0){
                                testSuccessCount++;
                            }
                        }
                        //stageBuildMapList.add(stageBuildMap);
                        if(StringUtils.isNumeric((String)newStageBuild.getDuration())){
                            jobDuration += (int)Math.ceil(Long.parseLong((String)newStageBuild.getDuration())/1000.0)*1000;
                        }
                    }
                    break;
                }
//                stageBuildMap.put("stageId", stageBuild.getStageId());
//                stageBuildMap.put("stageName", stageBuild.getStageName());
//                stageBuildMap.put("stageOrder", stageBuild.getStageOrder());
//                stageBuildMap.put("stageType", stageBuild.getStageType());
//                stageBuildMap.put("stageTypeId", stageBuild.getStageTypeId());
//                stageBuildMap.put("stageTemplateTypeId", stageBuild.getStageTemplateTypeId());
//                stageBuildMap.put("buildStatus", stageBuild.getStatus());
//                stageBuildMap.put("buildNum", stageBuild.getBuildNum());
//                stageBuildMap.put("buildTime", stageBuild.getStartTime());
//                stageBuildMap.put("duration", stageBuild.getDuration());
//                stageBuildMap.put("log", stageBuild.getLog());
//                stageBuildMap.put("testResult", stageBuild.getTestResult());
//                stageBuildMap.put("testUrl", stageBuild.getTestUrl());
                if(stageBuild.getStageTemplateTypeId() != null && StageTemplateTypeEnum.CODESCAN.getCode() == stageBuild.getStageTemplateTypeId()){
                    staticCount++;
                    if(stageBuild.getTestResult() != null && stageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0){
                        staticSuccessCount++;
                    }
                }else if(stageBuild.getStageTemplateTypeId() != null && StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stageBuild.getStageTemplateTypeId()){
                    testCount++;
                    if(stageBuild.getTestResult() != null && stageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0){
                        testSuccessCount++;
                    }
                }
//                stageBuildMapList.add(stageBuildMap);
                if(StringUtils.isNumeric((String)stageBuild.getDuration())){
                    jobDuration += (int)Math.ceil(Long.parseLong((String)stageBuild.getDuration())/1000.0)*1000;
                }
            }
            buildMap.put("stageCount", stageCount);
            buildMap.put("duration", jobDuration);
//            buildMap.put("stageList",stageBuildMapList);
            buildMap.put("staticResult", String.valueOf(staticSuccessCount) + "/" + staticCount);
            buildMap.put("testResult", String.valueOf(testSuccessCount) + "/" + testCount);
            buildList.add(buildMap);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("pageSize", pageSize);
        data.put("page", page);
        data.put("totalPage", Math.ceil(1.0 * total/pageSize));
        data.put("buildList",buildList);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public void getJobLogWS(WebSocketSession session, Integer id, String buildNum) {
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            Job job = jobMapper.queryById(id);
            String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
            transactionManager.commit(status);
            String start = "0";
            String moreData = "";

            while (moreData != null && session.isOpen()) {
                moreData = null;
                Map<String, Object> params = new HashMap<>();
                params.put("start", start);
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/" + buildNum + "/logText/progressiveHtml", null, params, true);
                if (result.isSuccess()) {
                    if (result.get("data") != null) {
                        Header[] headers = (Header[]) ((Map) result.get("data")).get("header");
                        for (Header header : headers) {
                            if ("X-More-Data".equalsIgnoreCase(header.getName())) {
                                moreData = header.getValue();
                            }
                            if ("X-Text-Size".equalsIgnoreCase(header.getName())) {
                                start = header.getValue();
                            }
                        }
                        if (StringUtils.isNotEmpty((String) ((Map) result.get("data")).get("body"))) {
                            String log = ((String) ((Map) result.get("data")).get("body")).replaceAll("</?[^>]+>","");
                            session.sendMessage(new TextMessage(log));
                        }
                    }
                }
                Connection conn = DataSourceUtils.getConnection(dataSource);
                conn.close();//手动关闭连接，防止长时间连接导致连接数达上限
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public ActionReturnUtil getNotification(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if(null == job){
            return ActionReturnUtil.returnErrorWithMap("message", "流程不存在'");
        }
        JobDto jobDto = new JobDto();
        jobDto.convertFromBean(job);
        Map notificationMap = new HashMap();
        notificationMap.put("notification", jobDto.isNotification());
        notificationMap.put("successNotification", jobDto.isSuccessNotification());
        notificationMap.put("failNotification", jobDto.isFailNotification());
        notificationMap.put("mail", jobDto.getMail());
        return ActionReturnUtil.returnSuccessWithData(notificationMap);
    }

    @Override
    public ActionReturnUtil updateNotification(JobDto jobDto) throws Exception {
        Job job = jobDto.convertToBean();
        String username = (String)session.getAttribute("username");
        job.setUpdateUser(username);
        job.setUpdateTime(new Date());
        jobMapper.updateNotification(job);
        updateJenkinsJob(job.getId());
        return ActionReturnUtil.returnSuccess();
    }

//    @Override
//    public ActionReturnUtil getTrigger(Integer id) throws Exception {
//        Job job = jobMapper.queryById(id);
//        if(null == job){
//            return ActionReturnUtil.returnErrorWithMap("message", "流程不存在");
//        }
//        Map triggerMap = new HashMap();
//        triggerMap.put("trigger", job.isTrigger());
//        triggerMap.put("pollScm", job.isPollScm());
//        triggerMap.put("pollScmCustomize", job.isPollScmCustomize());
//        if(job.isPollScmCustomize()){
//            triggerMap.put("cronExpForPollScm",job.getCronExpForPollScm());
//        }else{
//            List<JobDto.TimeRule> timeRuleList = new ArrayList<>();
//            if(StringUtils.isNotBlank(job.getCronExpForPollScm()));
//            String[] cronArray = job.getCronExpForPollScm().split("\n");
//            for(String cron: cronArray){
//                JobDto.TimeRule timeRule = new JobDto.TimeRule();
//                String[] part = cron.split(" ");
//                timeRule.setDayOfMonth(part[2]);
//                timeRule.setDayOfWeek(part[4]);
//                timeRule.setHour(part[1]);
//                timeRule.setMinute(part[0]);
//                timeRuleList.add(timeRule);
//            }
//            triggerMap.put("pollScmTimeRule", timeRuleList);
//        }
//        return ActionReturnUtil.returnSuccessWithData(triggerMap);
//    }

//    @Override
//    public ActionReturnUtil updateTrigger(JobDto jobDto) throws Exception {
//        String username = (String)session.getAttribute("username");
//        jobDto.setUpdateUser(username);
//        jobDto.setUpdateTime(new Date());
//        String cron = null;
//        List cronList = new ArrayList<>();
//        if(!jobDto.isPollScmCustomize()){
//            for(JobDto.TimeRule timeRule:jobDto.getPollScmTimeRule()){
//                cron = null;
//                if(StringUtils.isNotBlank(timeRule.getDayOfMonth()) && StringUtils.isNotBlank(timeRule.getDayOfWeek()) && StringUtils.isNotBlank(timeRule.getHour()) && StringUtils.isNotBlank(timeRule.getMinute())){
//                    cron = timeRule.getMinute() + " " + timeRule.getHour() + " " + timeRule.getDayOfMonth() + " * " + timeRule.getDayOfWeek();
//                }
//                cronList.add(cron);
//            }
//            jobDto.setCronExpForPollScm(String.join("\n", cronList));
//        }
//        Job job = jobDto.convertToBean();
//        jobMapper.updateTrigger(job);
//        stageService.updateJenkinsJob(jobDto.getId());
//        return ActionReturnUtil.returnSuccess();
//    }

    @Override
    public void preBuild(Integer id, Integer buildNum, String dateTime) throws Exception{
        Job job = jobMapper.queryById(id);
        String projectName = null;
        String clusterName = null;
        try {
            projectName = getProjectNameByProjectId(job.getProjectId());
            clusterName = getClusterNameByClusterId(job.getClusterId());
        } catch (Exception e) {
            logger.error("获取项目或集群失败", e);
        }
        try {
            Cluster topCluster = clusterService.getPlatformCluster();
            destroyCicdPod(topCluster);
        }catch (Exception e){
            logger.error("job运行失败,集群信息错误,job:{}", JSONObject.toJSONString(job),e);
            return;
        }

        Map tagMap = new HashMap<>();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName()  + "/config.xml", null, null, false);
        if(result.isSuccess()){
            Map body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map definition = (Map)body.get("flow-definition");
            Object param = ((Map)((Map)((Map)definition.get("properties")).get("hudson.model.ParametersDefinitionProperty")).get("parameterDefinitions")).get("hudson.model.StringParameterDefinition");
            if(param instanceof List){
                for(Map map : (List<Map>)param){
                    tagMap.put(map.get("name"), map.get("defaultValue"));
                }
            }
        }

        JobBuild jobbuild = new JobBuild();
        jobbuild.setJobId(id);
        jobbuild.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobbuild);
        if(jobBuildList == null || jobBuildList.size() == 0){
            String jenkinsJobName = job.getTenant()+"_"+job.getName();
            Map params = new HashMap<>();
            params.put("tree","number,timestamp");
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/lastBuild/api/xml", null, params, false);
            if(result.isSuccess()) {
                Map body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
                Map root = (Map) body.get("workflowRun");
                Integer lastBuildNumber = Integer.valueOf((String) root.get("number"));
                Date startTime = new Timestamp(Long.valueOf((String) root.get("timestamp")));
                jobMapper.updateLastBuildNum(id, lastBuildNumber);
                JobBuild jobBuild = new JobBuild();
                jobBuild.setJobId(id);
                jobBuild.setBuildNum(lastBuildNumber);
                jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
                jobBuild.setStartTime(startTime);
                jobBuild.setStartUser(null);
                jobBuildMapper.insert(jobBuild);
                Map<Integer, StageType> stageTypeMap = new HashMap<>();
                List<StageType> stageTypeList = null;
                try {
                    stageTypeList = stageTypeService.queryByType(job.getType());
                } catch (Exception e) {
                    logger.error("获取步骤类型失败", e);
                    return;
                }
                for(StageType stageType : stageTypeList){
                    stageTypeMap.put(stageType.getId(),stageType);
                }

                List<Stage> stageList = stageMapper.queryByJobId(id);
                for(Stage stage:stageList){
                    StageBuild stageBuild = new  StageBuild();
                    stageBuild.setJobId(id);
                    stageBuild.setStageId(stage.getId());
                    stageBuild.setStageName(stage.getStageName());
                    stageBuild.setStageOrder(stage.getStageOrder());
                    stageBuild.setStageTypeId(stage.getStageTypeId());
                    StageType stageType= stageTypeMap.get(stage.getStageTypeId());
                    stageBuild.setStageType(stageType.getName());
                    stageBuild.setStageTemplateTypeId(stageType.getTemplateType());
                    stageBuild.setBuildNum(lastBuildNumber);
                    stageBuild.setStatus(Constant.PIPELINE_STATUS_WAITING);
                    if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()){
                        if(CommonConstant.IMAGE_TAG_TIMESTAMP.equals(stage.getImageTagType())){
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + dateTime);
                        }else {
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + tagMap.get("tag" + stage.getStageOrder()));
                        }
                        if(CommonConstant.IMAGE_TAG_RULE.equals(stage.getImageTagType())) {
                            stage.setImageBaseTag(generateTag(stage));
                            stageMapper.updateStage(stage);
                        }
                    }
                    if(StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()){
                        updateDeployStageBuild(stage, stageBuild);
                    }
                    stageBuildMapper.insert(stageBuild);
                }
            }
        }else{
            List<Stage> stageList = stageMapper.queryByJobId(id);
            for(Stage stage:stageList){
                StageBuild condition = new StageBuild();
                condition.setStageId(stage.getId());
                condition.setBuildNum(buildNum);
                List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(condition);
                if(CollectionUtils.isNotEmpty(stageBuildList)){
                    StageBuild stageBuild = stageBuildList.get(0);

                    if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType()){
                        if(CommonConstant.IMAGE_TAG_TIMESTAMP.equals(stage.getImageTagType())){
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + dateTime);
                        }else{
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + tagMap.get("tag" + stage.getStageOrder()));
                        }

                    }else if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()) {
                        updateDeployStageBuild(stage, stageBuild);
                    }
                    stageBuildMapper.updateByStageOrderAndBuildNum(stageBuild);
                }
            }
        }
    }

    private void updateDeployStageBuild(Stage stage, StageBuild stageBuild) throws Exception{
        String tag = stage.getImageTag();
        Integer stageId = stage.getOriginStageId();
        if(stageId == null) {
            stageBuild.setImage(stage.getImageName() + ":" + tag);
        }else{
            Stage ciStage = stageService.selectByPrimaryKey(stageId);
            StageBuild condition = new StageBuild();
            condition.setStageId(ciStage.getId());
            condition.setStatus(Constant.PIPELINE_STATUS_SUCCESS);
            List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(condition);
            for(StageBuild ciStageBuild : stageBuildList){
                if(ciStageBuild.getImage() !=null && ciStageBuild.getImage().contains(stage.getImageName())){
                    stageBuild.setImage(ciStageBuild.getImage());
                    break;
                }
            }
        }
    }


    @Override
    public void postBuild(Integer id, Integer buildNum) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                try {
                    updateJenkinsJob(id);
                    Thread.sleep(3000);
                    Job job = jobMapper.queryById(id);
                    syncJobStatus(job, buildNum);
                    allStageStatusSync(job, buildNum);
                    stageBuildMapper.updateWaitingStage(job.getId(), buildNum);
                    sendNotification(job, buildNum);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        NewCachedThreadPool threadPool = NewCachedThreadPool.init();
        threadPool.execute(worker);
    }

    @Override
    public void stageSync(Integer id, Integer buildNum) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    stageStatusSync(id, buildNum);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        NewCachedThreadPool threadPool = NewCachedThreadPool.init();
        threadPool.execute(worker);
    }

    @Override
    public void deploy(Integer stageId, Integer buildNum) throws Exception{
        Stage stage = stageMapper.queryById(stageId);
        if(StringUtils.isBlank(stage.getServiceName())){
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_NAME_NOT_BLANK);
        }
        Job job = jobMapper.queryById(stage.getJobId());

        StageDto stageDto = new StageDto();
        stageDto.convertFromBean(stage);


        Cluster cluster = clusterService.findClusterById(job.getClusterId());

        if(CommonConstant.FRESH_RELEASE.equals(stage.getDeployType())){
            doFreshRelease(job, stageDto, cluster, buildNum);
        }else if(CommonConstant.CANARY_RELEASE.equals(stage.getDeployType())){
            doCanaryRelease(job, stageDto, cluster, buildNum);
        }else if(CommonConstant.BLUE_GREEN_RELEASE.equals(stage.getDeployType())){
            doBlueGreenRelease(job, stageDto, cluster, buildNum);
        }


//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        session = request.getSession();
//        session.setAttribute("username", "admin");
//        K8SClient.tokenMap.put("admin", cluster.getMachineToken());


    }

    @Override
    public void jobStatusWS(WebSocketSession session, Integer id) {
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            Job job = jobMapper.queryById(id);
            String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
            List<Stage> dbStageList = stageMapper.queryByJobId(id);
            transactionManager.commit(status);
            StringBuilder lastStatus = new StringBuilder();
            StringBuilder currentStatus;
            while (session.isOpen()) {
                List<Stage> dbStageListCp = new ArrayList<>();
                dbStageListCp.addAll(dbStageList);
                currentStatus =  new StringBuilder();
                Map jenkinsJobMap = new HashMap<>();
                String jobStatus = null;
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName()  + "/lastBuild/api/json", null, null, false);
                if (result.isSuccess()) {
                    jenkinsJobMap = JsonUtil.convertJsonToMap((String) result.get("data"));
                    if((boolean)jenkinsJobMap.get("building") == true) {
                        jobStatus = Constant.PIPELINE_STATUS_BUILDING;
                    }else{
                        jobStatus = (String)jenkinsJobMap.get("result");
                    }
                }
                result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName()  + "/lastBuild/wfapi/describe", null, null, false);
                if (result.isSuccess()) {
                    String data = (String) result.get("data");
                    Map dataMap = JsonUtil.convertJsonToMap(data);
                    //String jobStatus = (String)dataMap.get("status");
                    currentStatus.append(jobStatus);
                    List<Map> stages = (List<Map>) dataMap.get("stages");
                    int i = 0;
                    List stageList = new ArrayList<>();
                    for (Map stage : stages) {
                        Map stageMap = new HashMap<>();
                        stageMap.put("lastBuildStatus", convertStatus((String)stage.get("status")));
                        stageMap.put("lastBuildDuration", String.valueOf(stage.get("durationMillis")));
                        if(stage.get("startTimeMillis") instanceof Long) {
                            stageMap.put("lastBuildTime", new Timestamp((Long) stage.get("startTimeMillis")));
                        }else{
                            stageMap.put("lastBuildTime", new Timestamp(Long.valueOf((Integer) stage.get("startTimeMillis"))));
                        }
                        for(Stage dbStage : dbStageListCp){
                            String jenkinsStageName = stage.get("name").toString();
                            String[] stageArray = jenkinsStageName.split("-");
                            if(dbStage.getId().toString().equals(stageArray[stageArray.length-1])){
                                stageMap.put("stageId", dbStage.getId());
                                stageMap.put("stageOrder", dbStage.getStageOrder());
                                dbStageListCp.remove(dbStageListCp.indexOf(dbStage));
                                break;
                            }
                        }
                        i++;
                        stageList.add(stageMap);
                        currentStatus.append(convertStatus((String)stage.get("status")));
                    }
                    for(Stage stage : dbStageListCp){
                        Map stageMap = new HashMap<>();
                        stageMap.put("stageId", stage.getId());
                        stageMap.put("stageOrder", stage.getStageOrder());
                        if(stage.getStageOrder() == 1){
                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_BUILDING.equals(jobStatus)?Constant.PIPELINE_STATUS_WAITING:Constant.PIPELINE_STATUS_NOTBUILT);
                        }else{
                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                        }
                        stageList.add(stageMap);
                        currentStatus.append((String)stageMap.get("lastBuildStatus"));
                        i++;
                    }
//                    while(stageList.size()<stageCount){
//                        Map stageMap = new HashMap<>();
//                        if(dbStageList.get(i) != null){
//                            stageMap.put("stageId", dbStageList.get(i).getId());
//                        }
//                        if(stageList.size() == 0){
//                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_INPROGRESS.equals(jobStatus)?Constant.PIPELINE_STATUS_WAITING:Constant.PIPELINE_STATUS_NOTBUILT);
//                        }else {
//                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
//                        }
//                        stageMap.put("stageOrder", stageList.size()+1);
//                        stageList.add(stageMap);
//                        currentStatus.append((String)stageMap.get("lastBuildStatus"));
//                        i++;
//                    }


                    if (!currentStatus.toString().equals(lastStatus.toString())) {
                        Map jobMap = new HashMap<>();
                        jobMap.put("stageList", stageList);
                        jobMap.put("lastBuildStatus", convertStatus(jobStatus));
                        if(dataMap.get("startTimeMillis") instanceof Long) {
                            jobMap.put("lastBuildTime", new Timestamp((Long) dataMap.get("startTimeMillis")));
                        }else{
                            jobMap.put("lastBuildTime", new Timestamp(Long.valueOf((Integer) dataMap.get("startTimeMillis"))));
                        }
                        status = transactionManager.getTransaction(def);
                        Integer lastBuildNum = jobBuildService.queryLastBuildNumById(id);
                        transactionManager.commit(status);
                        jobMap.put("lastBuildNum", lastBuildNum);
                        jobMap.put("lastBuildDuration", jenkinsJobMap.get("duration"));
                        session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobMap))));
                    }
                }
                lastStatus = currentStatus;
                Connection conn = DataSourceUtils.getConnection(dataSource);
                conn.close();//手动关闭连接，防止长时间连接导致连接数达上限

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("get job status error", e);
        } finally {
            try {
                if(session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                logger.error("websocket close error", e);
            }
        }

    }

    @Override
    public String getYaml(Integer id) throws Exception {
        Yaml yaml = new Yaml();
        Job job = jobMapper.queryById(id);
        JobDto jobDto = new JobDto();
        jobDto.convertFromBean(job);
        Map jobMap = new LinkedHashMap<>();
        jobMap.put("kind","Pipeline");
        jobMap.put("name",job.getName());
        Map notification = new LinkedHashMap<>();
        if(job.isNotification()){
            notification.put("email", jobDto.getMail());
            notification.put("successNotification", job.isSuccessNotification());
            notification.put("failNotification", job.isFailNotification());
            jobMap.put("notification", notification);
        }
        //TODO:trigger yaml
//        if(job.isTrigger()){
//            Map trigger = new LinkedHashMap<>();
//            job.getCronExpForPollScm();
//            trigger.put("pollScm", job.getCronExpForPollScm());
//        }
        Map buildEnvMap = new HashMap<>();
        List<BuildEnvironment> buildEnvList = buildEnvironmentMapper.queryAll();
        for(BuildEnvironment buildEnv:buildEnvList){
            buildEnvMap.put(buildEnv.getId(), buildEnv.getName());
        }
        List stages = new ArrayList<>();
        List<Stage> stageList = stageMapper.queryByJobId(id);
        for(Stage stage : stageList){
            StageDto stageDto = new StageDto();
            stageDto.convertFromBean(stage);
            Map stageMap = new LinkedHashMap<>();
            stageMap.put("name", stage.getStageName());
            stageMap.put("type", stage.getStageTypeId());

            if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()){
                Map repository = new LinkedHashMap<>();
                repository.put("type", stage.getRepositoryType());
                repository.put("url", stage.getRepositoryUrl());
                if(StringUtils.isNotEmpty(stage.getRepositoryBranch())){
                    repository.put("branch", stage.getRepositoryBranch());
                }
                if(StringUtils.isNotEmpty(stage.getCredentialsUsername())) {
                    repository.put("username", stage.getCredentialsUsername());
                }
                if(StringUtils.isNotEmpty(stage.getCredentialsUsername())) {
                    repository.put("password", "*");
                }
                stageMap.put("repository", repository);
                stageMap.put("buildEnvironment",buildEnvMap.get(stage.getBuildEnvironmentId()));
                if(CollectionUtils.isNotEmpty(stageDto.getEnvironmentVariables())) {
                    stageMap.put("environmentVariables", stageDto.getEnvironmentVariables());
                }
                if(CollectionUtils.isNotEmpty(stageDto.getDependences())) {
                    List<Map> depList = new ArrayList<>();
                    for (StageDto.Dependence dep : stageDto.getDependences()) {
                        Map map = BeanUtils.describe(dep);
                        map.remove("class");
                        map.remove("common");
                        depList.add(map);
                    }
                    stageMap.put("dependencies", depList);
                }
            }
            else if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()){
                stageMap.put("DockerfileFrom", stage.getDockerfileType());
                if("1".equals(stage.getDockerfileType())) {
                    stageMap.put("DockerfilePath", stage.getDockerfilePath());
                }else if("2".equals(stage.getDockerfileType())) {
                    stageMap.put("DockerfileId", stage.getDockerfileId());
                }
                stageMap.put("image", stage.getImageName());
                stageMap.put("imageTagType", Integer.valueOf(stage.getImageTagType()));
                if("2".equals(stage.getImageTagType())) {
                    stageMap.put("customTag", stage.getImageTag());
                }
                else if("1".equals(stage.getImageTagType())) {
                    stageMap.put("baseTag", stage.getImageBaseTag());
                    stageMap.put("increaseTag", stage.getImageIncreaseTag());
                }
            }
            else if(StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()){
                stageMap.put("image", stage.getImageName());
                stageMap.put("namespace", stage.getNamespace());
                stageMap.put("service",stage.getServiceName());
                stageMap.put("container",stage.getContainerName());
            }
            if(StageTemplateTypeEnum.CUSTOM.getCode() == stage.getStageTemplateType()){
                if(stage.getBuildEnvironmentId() != null && stage.getBuildEnvironmentId() != 0){
                    stageMap.put("buildEnvironment",buildEnvMap.get(stage.getBuildEnvironmentId()));
                }
                if(CollectionUtils.isNotEmpty(stageDto.getEnvironmentVariables())) {
                    stageMap.put("environmentVariables", stageDto.getEnvironmentVariables());
                }
            }

            if(CollectionUtils.isNotEmpty(stageDto.getCommand())) {
                stageMap.put("command", stageDto.getCommand());
            }
            stages.add(stageMap);
        }
        jobMap.put("stages", stages);
        String body = yaml.dumpAsMap(jobMap);
        return body.replaceAll("password: .*\\n","password: ******\\\n");
    }

//    @Override
//    public ActionReturnUtil getLastBuildLog(Integer id) throws Exception{
//        Job job = jobMapper.queryById(id);
//        Project project = projectService.getProjectByProjectId(job.getProjectId());
//        if(null == project){
//            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
//        }
//        String projectName = project.getProjectName();
//        Cluster cluster = clusterService.findClusterById(job.getClusterId());
//        if(null == cluster){
//            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
//        }
//        String clusterName = cluster.getName();
//        String jenkinsJobName = job.getTenant() + "_" + job.getName();
//        Map data = new HashMap();
//        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName()  + "/lastBuild/consoleText", null, null, false);
//        if(result.isSuccess()){
//            data.put("log", result.get("data"));
//        }else{
//            data.put("log", "");
//        }
//        return ActionReturnUtil.returnSuccessWithData(data);
//    }

    @Override
    public String getJobLog(Integer id, Integer buildNum){
        JobBuild jobBuild = new JobBuild();
        jobBuild.setJobId(id);
        jobBuild.setBuildNum(buildNum);
        return jobBuildService.queryLogByObject(jobBuild);
    }

    @Override
    public void getJobListWS(WebSocketSession session, String projectId, String clusterId) {
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionStatus status = transactionManager.getTransaction(def);
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = null;
            List<Cluster> clusterList = null;
            if(StringUtils.isNotBlank(clusterId)) {
                clusterName = clusterService.getClusterNameByClusterId(clusterId);
            }else{
                clusterList = roleLocalService.getClusterListByRoleId((Integer)session.getAttributes().get(CommonConstant.ROLEID));
            }
            transactionManager.commit(status);
            StringBuilder lastStatus = new StringBuilder();
            StringBuilder currentStatus;
            Map<String, Object> params = new HashMap<>();
            params.put("tree", "jobs[name,color,lastBuild[number,building,result,timestamp],builds[result]]");
            params.put("wrapper", "root");
            params.put("xpath", "/*");
            ActionReturnUtil result;
            while (session.isOpen()) {
                currentStatus = new StringBuilder();
                List jobList = new ArrayList();
                status = transactionManager.getTransaction(def);
                List<Job> dbJobList = jobMapper.select(projectId, clusterId, null, null);
                transactionManager.commit(status);

                if(StringUtils.isNotBlank(clusterName)){
                    result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/api/xml", null, params, false);
                    currentStatus = getCurrentJobsStatusInJenkins(result, clusterId, jobList, dbJobList, currentStatus);
                }else if(CollectionUtils.isNotEmpty(clusterList)){
                    for(Cluster cluster : clusterList){
                        result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + cluster.getName() + "/api/xml", null, params, false);
                        currentStatus = getCurrentJobsStatusInJenkins(result, cluster.getId(), jobList, dbJobList, currentStatus);
                    }
                }

                if(!currentStatus.toString().equals(lastStatus.toString())){
                    session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobList))));
                }
                lastStatus = currentStatus;
                Connection conn = DataSourceUtils.getConnection(transactionManager.getDataSource());
                conn.close();//手动关闭连接，防止长时间连接导致连接数达上限

                Thread.sleep(2000);
            }
        } catch (Exception e) {
            logger.error("get job list error", e);
        } finally {
            try {
                if(session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                logger.error("websocket close error", e);
            }
        }
    }

    private StringBuilder getCurrentJobsStatusInJenkins(ActionReturnUtil result, String clusterId, List jobList, List<Job> dbJobList, StringBuilder currentStatus){
        if(result.isSuccess()) {
            String jenkinsJobName;
            //List jobList = new ArrayList<>();
            Map jobMap;
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            if (jenkinsDataMap.get("root") instanceof String) {
                return currentStatus;
            }
            Map rootMap = (Map) jenkinsDataMap.get("root");
            List<Map> jenkinsJobList = new ArrayList<>();
            if(rootMap.get("folder") instanceof  String){
                return currentStatus;
            }
            Map folderMap = (Map)rootMap.get("folder");
            if (folderMap.get("job") instanceof Map) {
                jenkinsJobList.add((Map) folderMap.get("job"));
            } else if (folderMap.get("job") instanceof List) {
                jenkinsJobList.addAll((List) folderMap.get("job"));
            }
            for (Map jenkinsJob : jenkinsJobList) {
                jobMap = new HashMap();
                jenkinsJobName = (String) jenkinsJob.get("name");
                jobMap.put("name", jenkinsJobName);

                Map lastBuildMap = (Map) jenkinsJob.get("lastBuild");
                if (lastBuildMap != null) {
                    if ("false".equalsIgnoreCase((String) lastBuildMap.get("building"))) {
                        jobMap.put("lastBuildStatus", lastBuildMap.get("result"));
                    } else {
                        jobMap.put("lastBuildStatus", "BUILDING");
                    }
                    if (lastBuildMap.get("timestamp") != null) {
                        jobMap.put("lastBuildTime", df.format(new Timestamp(Long.valueOf((String) lastBuildMap.get("timestamp")))));
                    }
                    if (lastBuildMap.get("number") != null) {
                        jobMap.put("lastBuildNumber", lastBuildMap.get("number"));
                    }
                } else {
                    jobMap.put("lastBuildStatus", "NOTBUILT");
                    jobMap.put("lastBuildTime", "");
                }

                if (CollectionUtils.isNotEmpty(dbJobList)) {
                    for(Job job : dbJobList){
                        if(job.getClusterId().equals(clusterId) && job.getName().equals(jenkinsJobName)){
                            jobMap.put("id", job.getId());
                        }
                    }
                    currentStatus.append(jobMap.get("lastBuildStatus"));
                } else {
                    continue;
                }
                jobList.add(jobMap);
            }
        }
        return currentStatus;
    }

    @Override
    public void destroyCicdPod(Cluster cluster) throws Exception{
        List jenkinsPodList = new ArrayList<>();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/computer/api/json", null, null, false);
        if(result.isSuccess()){
            Map dataMap = JsonUtil.convertJsonToMap((String)result.get("data"));
            List<Map> computerList = new ArrayList<>();
            if(dataMap.get("computer") instanceof List){
                computerList.addAll((List<Map>)dataMap.get("computer"));
            }else{
                computerList.add((Map)dataMap.get("computer"));
            }
            for(Map computer : computerList){
                if(StringUtils.contains((String)computer.get("_class"), "kubernetes")){
                    jenkinsPodList.add(computer.get("displayName"));
                }
            }
        }

        K8SURL k8SURL = new K8SURL();
        k8SURL.setResource(Resource.POD);
        k8SURL.setNamespace(CommonConstant.CICD_NAMESPACE);
        Map<String, Object> labels = new HashMap<>();
        labels.put("labelSelector", "jenkins=slave");
        List<Cluster> clusterList = new ArrayList<>();
        if(cluster == null){
            clusterList = clusterService.listCluster();
        }else{
            clusterList.add(cluster);
        }
        for(Cluster c : clusterList){
            K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, labels, c);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                PodList podList = JsonUtil.jsonToPojo(response.getBody(), PodList.class);
                for (Pod pod : podList.getItems()) {
                    if(!jenkinsPodList.contains(pod.getMetadata().getName()) && !"Running".equals(pod.getStatus())){
                        k8SURL.setName(pod.getMetadata().getName());
                        new K8sMachineClient().exec(k8SURL, HTTPMethod.DELETE, null, null, c);
                    }
                }
            }
        }
    }

    @Override
    public List listDeployImage(Integer jobId) {
        List<Stage> stageList = stageMapper.queryByJobId(jobId);
        List deployImageList = new ArrayList<>();
        for(Stage stage : stageList){
            if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()){
                Map map = new HashMap();
                map.put("stageId", stage.getId());
                map.put("imageName", stage.getHarborProject() +"/"+ stage.getImageName());
                deployImageList.add(map);
            }
        }
        return deployImageList;
    }

    @Override
    public Job getJobById(Integer id) throws Exception {
        return jobMapper.queryById(id);
    }

    @Override
    public Job getJobByUuid(String uuid) throws Exception {
        return jobMapper.queryByUuid(uuid);
    }

    @Override
    public void runStage(Integer stageId, Integer buildNum) throws Exception {
        Stage stage = stageMapper.queryById(stageId);
        if(stage == null){
            logger.error("流水线步骤不存在{}", stageId);
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_NOT_EXIST);
        }
        if(StageTemplateTypeEnum.DEPLOY.getCode() ==  stage.getStageTemplateType()){
            deploy(stageId, buildNum);
        }else if(StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType()){
            scanCodeBySuite(stage, buildNum);
        }else if(StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()){
            testBySuite(stage, buildNum);
        }
    }

    @Override
    public int deleteByClusterId(String clusterId){
        List<Job> jobs = jobMapper.select(null,clusterId,null,null);
        if(CollectionUtils.isEmpty(jobs)){
            return 0;
        }
        int deleteCount = jobMapper.deleteByClusterId(clusterId);
        for(Job job : jobs) {
            stageMapper.deleteStageByJob(job.getId());
        }
        return deleteCount;
    }



    private void scanCodeBySuite(Stage stage, Integer buildNum) throws Exception{
        integrationTestService.executeTestSuite(stage.getSuiteId(), stage.getId(), buildNum);
    }

    private void testBySuite(Stage stage, Integer buildNum) throws Exception{
        boolean serviceStarted;
        Stage stageExample = new Stage();
        stageExample.setJobId(stage.getJobId());
        List<Stage> stageList = stageService.selectByExample(stageExample);
        for(Stage jobStage : stageList){
            int newInstance = 0;
            if(StageTemplateTypeEnum.DEPLOY.getCode() == jobStage.getStageTemplateType() && jobStage.getStageOrder()<stage.getStageOrder()){
                int repeat = CommonConstant.NUM_TEN;
                String namespace = jobStage.getNamespace();
                String serviceName = jobStage.getServiceName();
                while(repeat>0) {
                    Thread.sleep(Constant.THREAD_SLEEP_TIME_10000);
                    serviceStarted= true;
                    repeat--;
                    ActionReturnUtil result = deploymentsService.getDeploymentDetail(namespace, serviceName);
                    if (result.isSuccess()) {
                        //获取服务状态，并判断
                        AppDetail appDetail = (AppDetail) result.get("data");
                        if (!Constant.SERVICE_START.equals(appDetail.getStatus())) {
                            continue;
                        }
                        //获取新版本的更新实例数
                        if(CommonConstant.FRESH_RELEASE.equals(jobStage.getDeployType())){
                            newInstance = appDetail.getInstance();
                        }else if(CommonConstant.CANARY_RELEASE.equals(jobStage.getDeployType())){
                            newInstance = jobStage.getInstances();
                            if(jobStage.getInstances() > repeat){
                                repeat = jobStage.getInstances();
                            }
                        }
                    }else{
                        continue;
                    }
                    result = deploymentsService.podList(serviceName, namespace);
                    if(result.isSuccess()){
                        List<PodDetail> podList = (List<PodDetail>)result.getData();
                        String tag1 = null;
                        String tag2 = null;
                        int count1 = 0;
                        int count2 = 0;
                        //获取新旧版本实例数
                        for(PodDetail pod : podList){
                            if(tag1 == null || tag1.equals(pod.getTag())){
                                tag1 = pod.getTag();
                                count1++;
                            }else if(tag2 == null || tag2.equals(pod.getTag())){
                                tag2 = pod.getTag();
                                count2++;
                            }else{
                                serviceStarted = false;
                            }
                        }
                        if(tag1 != null){
                            int tag = Integer.valueOf(tag1.replace("v",""));
                            if(tag2 != null){
                                //当有两个版本时，比较两个版本中新版本的实例数是否达到更新实例数
                                if(tag>Integer.valueOf(tag2.replace("v",""))){
                                    if(count1 != newInstance){
                                        continue;
                                    }
                                }else{
                                    if(count2 != newInstance){
                                        continue;
                                    }
                                }
                            }else{
                                //只有新版本时，确认实例数是否达到
                                if(count1 != newInstance){
                                    continue;
                                }
                            }
                        }
                    }else{
                        continue;
                    }
                    if(serviceStarted){
                        break;
                    }
                }
            }
        }
        integrationTestService.executeTestSuite(stage.getSuiteId(), stage.getId(), buildNum);
    }

    /**
     * 获取jenkins目录
     * @param folderName 目录名称
     * @return
     * @throws Exception
     */
    private FolderJob getFolderJob(String ... folderName) throws Exception {
        FolderJob folderJob = null;
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        if(folderName.length>0) {
            for(int i = 0; i<folderName.length; i++){
                com.offbytwo.jenkins.model.Job job = jenkinsServer.getJob(folderJob, folderName[i]);
                if(job == null){
                    return null;
                }
                folderJob = new FolderJob(job.getName(), job.getUrl());
            }
            return folderJob;
        }
        return null;
    }

    /**
     * 验证流水线名称
     * @param jobName 流水线名
     * @param projectName 项目名
     * @param clusterName 集群名
     */
    private void validateJobName(String jobName, String projectName, String clusterName){
        Map<String, Object> params = new HashMap<>();
        params.put("value", jobName);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/checkJobName", null, params, false);
        String data = String.valueOf(result.get("data"));
        if (result.isSuccess()) {
            if (data.contains("exists")) {
                throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NAME_DUPLICATE);
            } else if (data.contains("error")) {
                logger.error("流水线名称验证失败", data);
                throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NAME_VALIDATE_ERROR);
            }
        } else {
            logger.error("流水线名称验证失败", data);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NAME_VALIDATE_ERROR);
        }
    }

    private List<Map> getJobsByJenkinsResult(ActionReturnUtil result, String jobName, String projectId, String clusterId, String clusterName, String type) throws Exception{
        List<Map> jobList = new ArrayList<>();
        if (result.isSuccess()) {
            String jenkinsJobName;
            Map jobMap;
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            List<Map> jenkinsJobList = new ArrayList<>();
            List<Map> jenkinsBuildList;

            if(jenkinsDataMap.get("folder") instanceof  String){
                return Collections.emptyList();
            }
            Map folderMap = (Map)jenkinsDataMap.get("folder");
            if (folderMap.get("job") instanceof Map) {
                jenkinsJobList.add((Map) folderMap.get("job"));
            } else if (folderMap.get("job") instanceof List) {
                jenkinsJobList.addAll((List) folderMap.get("job"));
            }
            for (Map jenkinsJob : jenkinsJobList) {
                jobMap = new HashMap();
                jenkinsJobName = (String) jenkinsJob.get("name");
                if (jenkinsJobName != null) {

                    if(StringUtils.isNotBlank(jobName) && !jenkinsJobName.contains(jobName)){
                        continue;
                    }
                    jobMap.put("name", jenkinsJobName);
                }
                Map lastBuildMap = (Map) jenkinsJob.get("lastBuild");
                if (lastBuildMap != null) {
                    if ("false".equalsIgnoreCase((String) lastBuildMap.get("building"))) {
                        jobMap.put("lastBuildStatus", lastBuildMap.get("result"));
                    } else {
                        jobMap.put("lastBuildStatus", "BUILDING");
                    }
                    if (lastBuildMap.get("timestamp") != null) {
                        jobMap.put("lastBuildTime", df.format(new Timestamp(Long.valueOf((String) lastBuildMap.get("timestamp")))));
                    }
                    if(lastBuildMap.get("number") != null){
                        jobMap.put("lastBuildNumber", lastBuildMap.get("number"));
                    }
                } else {
                    jobMap.put("lastBuildStatus", "NOTBUILT");
                    jobMap.put("lastBuildTime", "");
                }

                jenkinsBuildList = new ArrayList<>();
                int successNum = 0;
                int failNum = 0;
                if(jenkinsJob.get("build") instanceof Map){
                    jenkinsBuildList.add((Map)jenkinsJob.get("build"));
                }else if(jenkinsJob.get("build") instanceof List){
                    jenkinsBuildList.addAll((List)jenkinsJob.get("build"));
                }
                for (Object jenkinsBuild : jenkinsBuildList) {
                    if(jenkinsBuild instanceof String){
                        continue;
                    }else {
                        Map jenkinsBuildMap = (Map)jenkinsBuild;
                        if (jenkinsBuildMap.get("result") != null) {
                            if ("SUCCESS".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
                                successNum++;
                            } else if ("Failure".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
                                failNum++;
                            }
                        }
                    }
                }
                jobMap.put("successNum",successNum);
                jobMap.put("failNum",failNum);
                jobMap.put("clusterName", clusterName);
                List<Job> dbJobList = jobMapper.select(projectId, clusterId, (String)jobMap.get("name"), null);
                if(CollectionUtils.isNotEmpty(dbJobList)){
                    for(Job dbJob : dbJobList){
                        if(StringUtils.isNotBlank(dbJob.getName()) && dbJob.getName().equals(jobMap.get("name"))){
                            if(StringUtils.isNotBlank(type) && !type.equals(dbJob.getType())){
                                continue;
                            }
                            jobMap.put("id", dbJob.getId());
                            jobMap.put("clusterId", dbJob.getClusterId());
                            jobMap.put("type",dbJob.getType());
                            break;
                        }
                    }
                    if(jobMap.get("id") == null){
                        continue;
                    }
                }
                else{
                    continue;
                }
                jobList.add(jobMap);
            }
        }
        return jobList;
    }


    private boolean createView(String username) {
        String viewName = username + "_view";
        Map<String, Object> params = new HashMap<>();
        params.put("name", viewName);
        params.put("mode", "hudson.model.ListView");
        params.put("json", JsonUtil.convertToJson(params));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/createView", null, params, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }

    private boolean updateView(String username, List jobList) throws Exception {
        String viewName = username + "_view";
        Map dataModel = new HashMap();
        dataModel.put("viewName", viewName);
        dataModel.put("jobList", jobList);
        String body = TemplateUtil.generate("viewConfig.ftl", dataModel);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/view/" + viewName + "/config.xml", null, null, body, null);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }

    private List<String> getViewJobList(String data) {
        List<String> jobList = new ArrayList<>();
        Map viewMap = JsonUtil.convertJsonToMap(data);
        List<Map> JobList = (List) viewMap.get("jobs");
        for (Map jobMap : JobList) {
            jobList.add((String) jobMap.get("name"));
        }
        return jobList;
    }


/*
    private String generateScript(Job job) {
        StringBuilder script = new StringBuilder("node(");
        String projectType = job.getProjectType();
        //switch node
        if (ProjectTypeEnum.JAVA.toString().equalsIgnoreCase(projectType)) {
            script.append("'java'){ ");
        } else if (ProjectTypeEnum.JAVASCRIPT.toString().equalsIgnoreCase(projectType)) {
            script.append("'java'){ ");
        }
        //check out
        script.append("\n\tstage('Check out'){ ");
        if (RepositoryTypeEnum.SVN.getType().equalsIgnoreCase(job.getRepositoryType())) {
            script.append("\n\t\tcheckout([$class: 'SubversionSCM',  locations: [[credentialsId: '").append(job.getTenant() + "_" + job.getJobName()).append("', depthOption: 'infinity', ignoreExternalsOption: true, local: '.', remote: '").append(job.getRepositoryUrl()).append("']]]) }");
        } else if (RepositoryTypeEnum.GIT.getType().equalsIgnoreCase(job.getRepositoryType())) {
            script.append("\n\t\tgit url:'").append(job.getRepositoryUrl()).append("',").append("credentialsId:'").append(job.getTenant() + "_" + job.getJobName()).append("'");
            if(StringUtils.isNotEmpty(job.getRepositoryBranch())){
                script.append(", branch:'").append(job.getRepositoryBranch()).append("'");
            }
            script.append(" }");
        }
        //build project
        if (ProjectTypeEnum.JAVA.toString().equalsIgnoreCase(projectType)) {
            script.append("\n\tstage('Build project') {\n\t\tsh 'mvn clean install -Dmaven.test.failure.ignore=true' } ");
        } else if (ProjectTypeEnum.JAVASCRIPT.toString().equalsIgnoreCase(projectType)) {
            script.append(" ");
        }
        //prepare docker env and Dockerfile
        script.append("\n\tstage('Prepare docker envriment') { \n\t\tsh 'nohup wrapdocker 2>/dev/null' \n\t\tsh 'cp /root/script/* ./' \n\t\tsh './pre_Dockerfile_").append(job.getProjectType().toLowerCase()).append(".sh' } ");
        //build and push image
        script.append("\n\tstage('Build and push image') { \n\t\twithCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'harbor', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) \n\t\t{ sh 'docker login ")
                .append(harborClient.getHost())
                .append(" --username=$USERNAME --password=$PASSWORD' } \n\t\tdocker.build('")
                .append(harborClient.getHost())
                .append("/")
                .append(job.getHarborProject())
                .append("/")
                .append(job.getImageName())
                .append(":$tag').push() } \n} ");

        return script.toString();
    }

*/


    private boolean deleteCredentials(String jenkinsJobName) {
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + jenkinsJobName + "/doDelete", null, null, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }

    private JobBuild syncJobStatus(Job job, Integer buildNum) throws Exception{
        Project project = projectService.getProjectByProjectId(job.getProjectId());
        if(null == project){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        String projectName = project.getProjectName();
        Cluster cluster = clusterService.findClusterById(job.getClusterId());
        if(null == cluster){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String clusterName = cluster.getName();

        JobBuild jobBuildCondition = new JobBuild();
        jobBuildCondition.setJobId(job.getId());
        jobBuildCondition.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);
        JobBuild jobBuild = new JobBuild();
        if(null != jobBuildList && jobBuildList.size()==1){
            jobBuild = jobBuildList.get(0);
        }else if(null == jobBuildList || jobBuildList.size() == 0){
            jobBuild.setJobId(job.getId());
            jobBuild.setBuildNum(buildNum);
            jobBuildMapper.insert(jobBuild);
        }

        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "building,timestamp,result,duration,number");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" +job.getName() + "/" + buildNum + "/api/xml", null, params, false);
        //ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/logText/progressiveHtml", null, null, false);
        ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" +job.getName()  + "/" + buildNum + "/consoleText", null, null, false);
        if (result.isSuccess()) {
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map build = new HashMap();
            Map rootMap = (Map) jenkinsDataMap.get("workflowRun");
            if (rootMap != null) {
                //build.put("build_num", rootMap.get("number"));
                if ("false".equalsIgnoreCase((String) rootMap.get("building"))) {
                    //build.put("build_status", rootMap.get("result"));
                    jobBuild.setStatus((String)rootMap.get("result"));
                } else {
                    //build.put("build_status", "BUILDING");
                    jobBuild.setStatus("BUILDING");
                }
                if (rootMap.get("timestamp") != null) {
                    //build.put("start_time", new Timestamp(Long.valueOf((String) rootMap.get("timestamp"))).toString());
                    jobBuild.setStartTime(new Timestamp(Long.valueOf((String) rootMap.get("timestamp"))));
                }
                jobBuild.setDuration((String)rootMap.get("duration"));
                //build.put("duration", rootMap.get("duration"));
            }
            if (logResult.isSuccess()) {
                //build.put("build_log", logResult.get("data"));
                jobBuild.setLog((String) logResult.get("data"));
            }
            jobBuildMapper.update(jobBuild);

        }
        return jobBuild;

    }

    private void stageStatusSync(Integer id, Integer buildNum) throws Exception {
        Stage stage = stageMapper.queryById(id);
        Job job = jobMapper.queryById(stage.getJobId());
        List<Map> stageMapList = stageService.getStageBuildFromJenkins(job, buildNum);
        int i = stage.getStageOrder() - 2;
        for(i = i<0?0:i ; i< stageMapList.size();i++){
            stageService.stageBuildSync(job, buildNum, stageMapList.get(i), i+1);
        }

    }


    private void allStageStatusSync(Job job, Integer buildNum) throws Exception {
        List<Map> stageMapList = stageService.getStageBuildFromJenkins(job, buildNum);
        int i=0;
        for(Map stageMap : stageMapList){
            stageService.stageBuildSync(job, buildNum, stageMap, ++i);
        }
    }




    private void sendNotification(Job job, Integer buildNum){
        if(job.isNotification()) {
            JobBuild jobBuildContidion = new JobBuild();
            jobBuildContidion.setJobId(job.getId());
            jobBuildContidion.setBuildNum(buildNum);
            List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildContidion);
            JobBuild jobBuild;
            if(jobBuildList != null && jobBuildList.size() == 1) {
                jobBuild = jobBuildList.get(0);
            }else{
                logger.error("流程构建记录不存在。");
                return;
            }

            if (job.isFailNotification() && job.isSuccessNotification() && ("FAILURE".equals(jobBuild.getStatus()) || "SUCCESS".equals(jobBuild.getStatus()))
                    || job.isFailNotification() && "FAILURE".equals(jobBuild.getStatus())
                    || job.isSuccessNotification() && "SUCCESS".equals(jobBuild.getStatus())) {
                JobDto jobDto = new JobDto();
                jobDto.convertFromBean(job);
                List<String> mailList = jobDto.getMail();
                MimeMessage mimeMessage = MailUtil.getJavaMailSender().createMimeMessage();
                try {
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
                    helper.setTo((String[])mailList.toArray(new String[mailList.size()]));
                    helper.setSubject(MimeUtility.encodeText("CICD Notification_" + job.getName() + "_" + jobBuild.getBuildNum(), MimeUtility.mimeCharset("gb2312"), null));
                    Map dataModel = new HashMap<>();
                    Cluster cluster = clusterService.findClusterById(job.getClusterId());
                    dataModel.put("url", webUrl + "/#/cicd/process/"+job.getId());
                    dataModel.put("jobName",job.getName());
                    dataModel.put("status",jobBuild.getStatus());
                    dataModel.put("time",new Date());
                    dataModel.put("startTime",jobBuild.getStartTime());
                    StageBuild stageBuildCondition = new StageBuild();
                    stageBuildCondition.setJobId(job.getId());
                    stageBuildCondition.setBuildNum(buildNum);
                    List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
                    List stageBuildMapList = new ArrayList<>();
                    int jobDuration = 0;
                    for(StageBuild stageBuild : stageBuildList){
                        Map stageBuildMap = new HashMap<>();
                        Stage stage = stageMapper.queryById(stageBuild.getStageId());
                        stageBuildMap.put("name",stage.getStageName());
                        stageBuildMap.put("status",stageBuild.getStatus());
                        stageBuildMap.put("startTime", stageBuild.getStartTime());
                        int duration = 0;
                        if(StringUtils.isNotBlank(stageBuild.getDuration())){
                            duration = (int)Math.ceil(Long.parseLong(stageBuild.getDuration()) / 1000.0) * CommonConstant.NUM_THOUSAND;
                        }
                        jobDuration += duration;
                        stageBuildMap.put("duration", DateUtil.getDuration(Long.valueOf(duration)));
                        stageBuildMapList.add(stageBuildMap);
                    }
                    dataModel.put("duration", DateUtil.getDuration(Long.valueOf(jobDuration)));
                    dataModel.put("stageBuildList",stageBuildMapList);
                    helper.setText(TemplateUtil.generate("notification.ftl",dataModel), true);
//                    ClassLoader classLoader = MailUtil.class.getClassLoader();
//                    InputStream inputStream = classLoader.getResourceAsStream("icon-info.png");
//                    byte[] bytes = MailUtil.stream2byte(inputStream);
//                    helper.addInline("icon-info", new ByteArrayResource(bytes), "image/png");
//                    inputStream = classLoader.getResourceAsStream("icon-status.png");
//                    bytes = MailUtil.stream2byte(inputStream);
//                    helper.addInline("icon-status", new ByteArrayResource(bytes), "image/png");
                    MailUtil.sendMimeMessage(mimeMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String generateTag(Stage stage) {
        String tag = "";
        if(!StringUtils.isBlank(stage.getImageBaseTag()) && !StringUtils.isBlank(stage.getImageIncreaseTag())) {
            String[] baseArray = stage.getImageBaseTag().split("\\.");
            String[] increaceArray = stage.getImageIncreaseTag().split("\\.");

            if (baseArray.length > 0 && increaceArray.length > 0) {
                String suffix = "";
                int start = 0;
                for(int i = baseArray[0].length() - 1; i>=0; i--){
                    if(Character.isDigit(baseArray[0].charAt(i))){
                        continue;
                    }
                    start = i + 1;
                    suffix = baseArray[0].substring(0, start);
                    break;
                }
                for(int i = 0; i<baseArray.length; i++){
                    if(i == 0){
                        baseArray[i] = baseArray[i].substring(start);
                    }
                    tag = tag + String.valueOf(Integer.valueOf(baseArray[i]) + Integer.valueOf(increaceArray[i])) + ".";
                }
                tag = (suffix + tag).substring(0, (suffix + tag).length() - 1);

            }
        }
        return tag;
    }

    private String convertStatus(String status){
        if(Constant.PIPELINE_STATUS_INPROGRESS.equals(status) || Constant.PIPELINE_STATUS_NOTEXECUTED.equals(status)) {
            return Constant.PIPELINE_STATUS_BUILDING;
        }else{
            return status;
        }
    }

    private FolderJob checkFolderJobExist(String projectName, String clusterName) throws Exception {
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        com.offbytwo.jenkins.model.Job projectFolder = jenkinsServer.getJob(projectName);
        if(projectFolder == null) {
            jenkinsServer.createFolder(projectName);
            projectFolder =  jenkinsServer.getJob(projectName);
        }
        FolderJob folderJob = new FolderJob(projectFolder.getName(),projectFolder.getUrl());
        com.offbytwo.jenkins.model.Job clusterFolder = jenkinsServer.getJob(folderJob, clusterName);
        if(clusterFolder == null ){
            jenkinsServer.createFolder(folderJob, clusterName);
            clusterFolder = jenkinsServer.getJob(folderJob, clusterName);
        }
        folderJob = new FolderJob(clusterFolder.getName(),clusterFolder.getUrl());
        return folderJob;
    }

    private void doFreshRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception{
//        session.setAttribute("tenantId", job.getTenantId());
        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setStageId(stageDto.getId());
        stageBuildCondition.setBuildNum(buildNum);
        List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
        if(CollectionUtils.isEmpty(stageBuildList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_BUILD_NOT_EXIST);
        }
        StageBuild stageBuild = stageBuildList.get(0);
        if(StringUtils.isBlank(stageBuild.getImage()) || stageBuild.getImage().split(":").length != 2){
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOY_IMAGE_NAME_ERROR);
        }
        //查询service是否已存在，若存在做更新
        K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stageDto.getNamespace(), null, null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            logger.error("获取deployment失败,namespace{}", stageDto.getNamespace(), depRes.getBody());
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        DeploymentList depList = JsonUtil.jsonToPojo(depRes.getBody(), DeploymentList.class);
        if (depList != null && CollectionUtils.isNotEmpty(depList.getItems())) {
            List<Deployment> deployments = depList.getItems();
            for(Deployment deployment : deployments){
                if(stageDto.getServiceName().equals(deployment.getMetadata().getName())){
                    Integer instances = deployment.getSpec().getReplicas();
                    stageDto.setInstances(instances);
                    List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(deployment);
                    if(CollectionUtils.isNotEmpty(containerList)) {
                        ContainerOfPodDetail containerOfPodDetail = containerList.get(0);
                        stageDto.setContainerName(containerOfPodDetail.getName());
                    }
                    doCanaryRelease(job, stageDto, cluster, buildNum);
                    return;
                }
            }
        }

        //模板全新发布
        Project project = projectService.getProjectByProjectId(job.getProjectId());
        ServiceTemplates serviceTemplate = serviceService.getSpecificTemplate(stageDto.getServiceTemplateName(), stageDto.getServiceTemplateTag(), job.getTenant(), project.getProjectId());
        ServiceTemplateDto serviceTemplateDto = serviceService.getServiceTemplateDtoByServiceTemplate(serviceTemplate, stageDto.getServiceName(), serviceTemplate.getName(), stageDto.getServiceTemplateTag(), stageDto.getNamespace(), project.getProjectId());
        //更换镜像
        DeploymentDetailDto deploymentDetailDto = serviceTemplateDto.getDeploymentDetail();
        List<CreateContainerDto> containers = deploymentDetailDto.getContainers();
        CreateContainerDto containerDto = containers.get(0);
        containerDto.setImg(stageBuild.getImage().split(":")[0]);
        containerDto.setTag(stageBuild.getImage().split(":")[1]);
        //更换配置
        if(CollectionUtils.isNotEmpty(stageDto.getConfigMaps())) {
            containerDto.setConfigmap(stageDto.getConfigMaps());
        }

        ServiceDeployDto serviceDeploy = new ServiceDeployDto();
        serviceDeploy.setNamespace(stageDto.getNamespace());
        serviceDeploy.setServiceTemplate(serviceTemplateDto);
        ActionReturnUtil res = serviceService.checkService(serviceTemplateDto, cluster, stageDto.getNamespace());
        if (!res.isSuccess()) {
            logger.error("发布失败", res);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_DEPLOY_ERROR);
        }
        String user = null;
        JobBuild jobBuild = new JobBuild();
        jobBuild.setJobId(job.getId());
        jobBuild.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildService.queryByObject(jobBuild);
        if(CollectionUtils.isNotEmpty(jobBuildList)){
            user = jobBuildList.get(0).getStartUser();
        }
        if(StringUtils.isEmpty(user)){
            user = job.getCreateUser();
        }
        res = serviceService.deployService(serviceDeploy, user);
        if (!res.isSuccess()) {
            logger.error("发布失败", res);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_DEPLOY_ERROR);
        }
    }

    private void doCanaryRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception{
        K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stageDto.getNamespace(), stageDto.getServiceName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<UpdateContainer> updateContainerList = getUpdateContainerList(dep, job, stageDto, cluster, buildNum);

        CanaryDeployment canaryDeployment = new CanaryDeployment();
        canaryDeployment.setName(stageDto.getServiceName());
        canaryDeployment.setContainers(updateContainerList);
        if(stageDto.getMaxSurge() != null) {
            canaryDeployment.setMaxSurge(stageDto.getMaxSurge());
        }else{
            canaryDeployment.setMaxSurge(0);
        }
        if(stageDto.getMaxUnavailable() != null) {
            canaryDeployment.setMaxUnavailable(stageDto.getMaxUnavailable());
        }else{
            canaryDeployment.setMaxUnavailable(1);
        }
        canaryDeployment.setInstances(stageDto.getInstances());
        canaryDeployment.setNamespace(stageDto.getNamespace());
        canaryDeployment.setSeconds(5);

        versionControlService.canaryUpdate(canaryDeployment, canaryDeployment.getInstances(), null);
    }

    private void doBlueGreenRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception{
        ActionReturnUtil result = deploymentsService.getDeploymentDetail(stageDto.getServiceName(), stageDto.getNamespace());
        AppDetail appDetail = (AppDetail)result.get("data");

        K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stageDto.getNamespace(), stageDto.getServiceName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<UpdateContainer> updateContainerList = getUpdateContainerList(dep, job, stageDto, cluster, buildNum);
        UpdateDeployment updateDeployment = new UpdateDeployment();
        updateDeployment.setName(dep.getMetadata().getName());
        updateDeployment.setNamespace(stageDto.getNamespace());
        updateDeployment.setInstance(String.valueOf(dep.getSpec().getReplicas()));
        updateDeployment.setContainers(updateContainerList);


        blueGreenDeployService.deployByBlueGreen(updateDeployment, null);
    }

    private List<UpdateContainer> getUpdateContainerList(Deployment dep, Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception {

        List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(dep);
        List<UpdateContainer> updateContainerList = new ArrayList<>();

        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setStageId(stageDto.getId());
        stageBuildCondition.setBuildNum(buildNum);
        List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
        StageBuild stageBuild = new StageBuild();
        if (stageBuildList.get(0) != null) {
            stageBuild = stageBuildList.get(0);
        }

        for (ContainerOfPodDetail containerOfPodDetail : containerList) {
            UpdateContainer updateContainer = new UpdateContainer();
            updateContainer.setName(containerOfPodDetail.getName());
            updateContainer.setArgs(containerOfPodDetail.getArgs());
            updateContainer.setCommand(containerOfPodDetail.getCommand());
            updateContainer.setLivenessProbe(containerOfPodDetail.getLivenessProbe());
            updateContainer.setReadinessProbe(containerOfPodDetail.getReadinessProbe());
            CreateResourceDto createResourceDto = new CreateResourceDto();
            createResourceDto.setCpu((String) containerOfPodDetail.getResource().get("cpu"));
            createResourceDto.setMemory((String) containerOfPodDetail.getResource().get("memory"));
            updateContainer.setResource(createResourceDto);
            CreateResourceDto limit = new CreateResourceDto();
            limit.setCpu((String) containerOfPodDetail.getLimit().get("cpu"));
            limit.setMemory((String) containerOfPodDetail.getLimit().get("memory"));
            updateContainer.setLimit(limit);
            List<CreateEnvDto> envList = new ArrayList<>();
            if (containerOfPodDetail.getEnv() != null) {
                for (EnvVar envVar : containerOfPodDetail.getEnv()) {
                    CreateEnvDto createEnvDto = new CreateEnvDto();
                    createEnvDto.setKey(envVar.getName());
                    createEnvDto.setName(envVar.getName());
                    createEnvDto.setValue(envVar.getValue());
                    envList.add(createEnvDto);
                }
                updateContainer.setEnv(envList);
            }
            List<CreatePortDto> portList = new ArrayList<>();
            for (ContainerPort containerPort : containerOfPodDetail.getPorts()) {
                CreatePortDto createPortDto = new CreatePortDto();
                createPortDto.setProtocol(containerPort.getProtocol());
                createPortDto.setPort(String.valueOf(containerPort.getContainerPort()));
                createPortDto.setContainerPort(String.valueOf(containerPort.getContainerPort()));
                portList.add(createPortDto);
            }
            updateContainer.setPorts(portList);
            List<PersistentVolumeDto> updateVolumeList = new ArrayList<>();
            List<CreateConfigMapDto> configMapList = new ArrayList<>();
            if (containerOfPodDetail.getStorage() != null) {
                for (VolumeMountExt volumeMountExt : containerOfPodDetail.getStorage()) {
                    if ("logDir".equals(volumeMountExt.getType())) {
                        LogVolume logVolumn = new LogVolume();
                        logVolumn.setName(volumeMountExt.getName());
                        logVolumn.setMountPath(volumeMountExt.getMountPath());
                        logVolumn.setReadOnly(volumeMountExt.getReadOnly().toString());
                        logVolumn.setType(volumeMountExt.getType());

                        updateContainer.setLog(logVolumn);
                    } else if ("nfs".equals(volumeMountExt.getType()) || "emptyDir".equals(volumeMountExt.getType()) || "hostPath".equals(volumeMountExt.getType())) {
                        PersistentVolumeDto updateVolume = new PersistentVolumeDto();
                        updateVolume.setType(volumeMountExt.getType());
                        updateVolume.setReadOnly(volumeMountExt.getReadOnly());
                        updateVolume.setPath(volumeMountExt.getMountPath());
                        updateVolume.setVolumeName(volumeMountExt.getName());
                        updateVolume.setEmptyDir(volumeMountExt.getEmptyDir());
                        updateVolume.setHostPath(volumeMountExt.getHostPath());
                        updateVolume.setRevision(volumeMountExt.getRevision());
                        if ("nfs".equals(volumeMountExt.getType())) {
                            updateVolume.setBindOne(true);
                            //updateVolume.setPvcTenantid(job.getTenantId());
                            updateVolume.setPvcName(updateVolume.getVolumeName());
                            List<PvDto> pvDtoList = persistentVolumeService.listPv(job.getProjectId(), cluster.getId(), Boolean.TRUE);
                            if (pvDtoList != null) {
                                for (PvDto pvDto : pvDtoList) {
                                    //todo -------------
                                    if (volumeMountExt.getName().split("-")[0].equals(pvDto.getName())) {
                                        updateVolume.setCapacity(pvDto.getCapacity());
                                        break;
                                    }
                                }
                            }
                        }
                        updateVolumeList.add(updateVolume);
                    } else if ("configMap".equals(volumeMountExt.getType())) {
                        CreateConfigMapDto configMap = new CreateConfigMapDto();
                        configMap.setPath(volumeMountExt.getMountPath());
                        if (volumeMountExt.getName() != null && volumeMountExt.getName().lastIndexOf("v") > 0) {
                            configMap.setTag(volumeMountExt.getName().substring(volumeMountExt.getName().lastIndexOf("v") + 1).replace("-", "."));
                            configMap.setFile(volumeMountExt.getName().substring(0, volumeMountExt.getName().lastIndexOf("v")));
                        }
                        ActionReturnUtil configMapResult = configMapService.getConfigMapByName(stageDto.getNamespace(), volumeMountExt.getConfigMapName(), null, cluster);
                        if (configMapResult.isSuccess()) {
                            ConfigMap config = (ConfigMap) configMapResult.get("data");
                            Map data = (Map) config.getData();
                            configMap.setValue((String) data.get(volumeMountExt.getName().replace("-", ".")));
                        }
                        configMapList.add(configMap);
                    }
                }
            }
            updateContainer.setStorage(updateVolumeList);
            updateContainer.setConfigmap(configMapList);

            if (updateContainer.getLog() == null) {
                updateContainer.setLog(new LogVolume());
            }

            if (stageDto.getContainerName().equals(containerOfPodDetail.getName())) {
                updateContainer.setImg(stageBuild.getImage());
                //更新配置
                if(CollectionUtils.isNotEmpty(stageDto.getConfigMaps())){
                    configMapList = new ArrayList<>();
                    for(CreateConfigMapDto createConfigMapDto : stageDto.getConfigMaps()) {
                        configMapList.add(createConfigMapDto);
                    }
                    updateContainer.setConfigmap(configMapList);
                }
            } else {
                updateContainer.setImg(containerOfPodDetail.getImg());
            }

            updateContainer.setImagePullPolicy(CommonConstant.IMAGEPULLPOLICY_ALWAYS);

            updateContainerList.add(updateContainer);
        }
        return updateContainerList;
    }

    private String getProjectNameByProjectId(String projectId) throws Exception{
        Project project = projectService.getProjectByProjectId(projectId);
        if(null == project){
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        return project.getProjectName();
    }

    private String getClusterNameByClusterId(String clusterId) throws Exception{
        Cluster cluster = clusterService.findClusterById(clusterId);
        if(null == cluster){
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return cluster.getName();
    }

    private void validateJob(Job job, List<Stage> stageList) throws Exception{
        if(CollectionUtils.isEmpty(stageList)){
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_EMPTY);
        }
        for(Stage stage : stageList){
            boolean valid = false;
            if(StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()){
                List<Map> suiteList = integrationTestService.getTestSuites(job.getProjectId(), job.getType());
                for(Map map : suiteList){
                    if(stage.getSuiteId().equals(map.get("suiteId"))){
                        valid = true;
                        break;
                    }
                }
                if(!valid){
                    throw new MarsRuntimeException(ErrorCodeMessage.TEST_SUITE_NOT_EXIST);
                }
            }
        }
    }

    @Override
    public List getStageBuildResult(Integer id, Integer buildNum, String status) throws Exception {
        Job job = jobMapper.queryById(id);
        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setJobId(id);
        stageBuildCondition.setBuildNum(buildNum);
        List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(stageBuildCondition);
        List<Map<String, Object>> stageBuildMapList = new ArrayList<Map<String, Object>>();
        for(StageBuild stageBuild : stageBuildList){
            Map<String, Object> stageBuildMap = new HashMap<String, Object>();
            if(!Constant.PIPELINE_STATUS_BUILDING.equals(status) && (Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus()) || Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()))){
                allStageStatusSync(job, buildNum);
                stageBuildMapper.updateWaitingStage(job.getId(), buildNum);
                stageBuildList = stageBuildService.selectStageBuildByObject(stageBuildCondition);
                stageBuildMapList = new ArrayList<>();
                for(StageBuild newStageBuild : stageBuildList){
                    stageBuildMap = new HashMap<>();
                    stageBuildMap.put("stageId", newStageBuild.getStageId());
                    stageBuildMap.put("stageName", newStageBuild.getStageName());
                    stageBuildMap.put("stageOrder", newStageBuild.getStageOrder());
                    stageBuildMap.put("stageType", newStageBuild.getStageType());
                    stageBuildMap.put("stageTemplateTypeId", newStageBuild.getStageTemplateTypeId());
                    if(Constant.PIPELINE_STATUS_WAITING.equals(newStageBuild.getStatus())){
                        stageBuildMap.put("buildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                    }else {
                        stageBuildMap.put("buildStatus", newStageBuild.getStatus());
                    }
                    stageBuildMap.put("buildNum", newStageBuild.getBuildNum());
                    stageBuildMap.put("buildTime", newStageBuild.getStartTime());
                    stageBuildMap.put("duration", newStageBuild.getDuration());
                    stageBuildMap.put("testResult", newStageBuild.getTestResult());
                    stageBuildMap.put("testUrl", newStageBuild.getTestUrl());
                    //stageBuildMap.put("log", newStageBuild.getLog());
                    stageBuildMapList.add(stageBuildMap);
                }
                break;
            }
            stageBuildMap.put("stageId", stageBuild.getStageId());
            stageBuildMap.put("stageName", stageBuild.getStageName());
            stageBuildMap.put("stageOrder", stageBuild.getStageOrder());
            stageBuildMap.put("stageType", stageBuild.getStageType());
            stageBuildMap.put("stageTemplateTypeId", stageBuild.getStageTemplateTypeId());
            stageBuildMap.put("buildStatus", stageBuild.getStatus());
            stageBuildMap.put("buildNum", stageBuild.getBuildNum());
            stageBuildMap.put("buildTime", stageBuild.getStartTime());
            stageBuildMap.put("duration", stageBuild.getDuration());
            stageBuildMap.put("testResult", stageBuild.getTestResult());
            stageBuildMap.put("testUrl", stageBuild.getTestUrl());
            //stageBuildMap.put("log", stageBuild.getLog());
            stageBuildMapList.add(stageBuildMap);
        }
        return stageBuildMapList;
    }

    @Override
    public ActionReturnUtil updateJenkinsJob(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        String body = generateJobBody(job);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/config.xml", null, null, body, null);
        return result;
    }

    private String generateJobBody(Job job) throws Exception{
        Map dataModel = new HashMap<>();
        List<Stage> stageList = stageMapper.queryByJobId(job.getId());
        Trigger trigger = triggerService.getTrigger(job.getId());
        ParameterDto parameterDto = parameterService.getParameter(job.getId());
        dataModel.put("job", job);
        dataModel.put("trigger", trigger);
        if(trigger != null && CommonConstant.JOBTRIGGER == trigger.getType() && trigger.getTriggerJobId() != null){
            Job triggerJob = getJobById(trigger.getTriggerJobId());
            if(triggerJob != null) {
                dataModel.put("triggerJobName", triggerJob.getName());
            }
        }
        dataModel.put("parameterList", parameterDto.getParameters());
        dataModel.put("stageList", stageList);
        dataModel.put("script", generateScript(job, stageList));
        return TemplateUtil.generate("jobConfig.ftl", dataModel);
    }

    private String generateScript(Job job, List<Stage> stageList) throws Exception {
        Map dataModel = new HashMap();
        dataModel.put("harborHost", clusterService.findClusterById(job.getClusterId()).getHarborServer().getHarborHost());
        List<StageDto> stageDtoList = new ArrayList<>();
        List<StageDto> imageBuildStages = new ArrayList<>();
        Map<Integer, DockerFile> dockerFileMap = new HashedMap();
        boolean dockerEnvironmentExit = false;
        for(Stage stage:stageList){
            StageDto newStageDto = new StageDto();
            newStageDto.convertFromBean(stage);
            if(StageTemplateTypeEnum.IMAGEBUILD.getCode() == newStageDto.getStageTemplateType()){
                if(!dockerEnvironmentExit){
                    BuildEnvironment buildEnvironment = buildEnvironmentMapper.selectByPrimaryKey(0);
                    if(buildEnvironment == null){
                        throw new MarsRuntimeException(ErrorCodeMessage.DEFAULT_BUILD_ENVIRONMENT_NOT_EXIST);
                    }
                    String image = buildEnvironment.getImage();
                    if(image.split("/").length<CommonConstant.NUM_THREE){
                        Cluster topCluster = clusterService.getPlatformCluster();
                        image = topCluster.getHarborServer().getHarborHost() + "/" + image;
                    }
                    newStageDto.setEnvironmentChange(true);
                    newStageDto.setBuildEnvironment(image);
                    dockerEnvironmentExit = true;
                }
                if(DockerfileTypeEnum.PLATFORM.ordinal() == newStageDto.getDockerfileType()){
                    DockerFile dockerFile = dockerFileService.selectDockerFileById(newStageDto.getDockerfileId());
                    dockerFile.setContent(StringEscapeUtils.escapeJava(dockerFile.getContent()));
                    dockerFileMap.put(stage.getStageOrder(), dockerFile);
                    //把步骤类型为镜像构建的放到一个list里面
                    imageBuildStages.add(newStageDto);
                }
            }
            if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()){
                if(StringUtils.isNotBlank(stage.getRepositoryBranch())) {
                    String[] str = stage.getRepositoryBranch().split(":",CommonConstant.NUM_TWO);
                    if(CommonConstant.REFERENCES_BRANCH.equals(str[0])){
                        if(str.length == 2){
                            newStageDto.setRepositoryBranch(str[CommonConstant.NUM_ONE]);
                        }
                    }else if(CommonConstant.REFERENCES_TAG.equals(str[0])){
                        if(str.length == 2 && StringUtils.isNotBlank(str[CommonConstant.NUM_ONE])){
                            newStageDto.setRepositoryBranch("refs/tags/"+str[CommonConstant.NUM_ONE]);
                        }
                    }
                }
            }
            if(StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType() ||
                    (StageTemplateTypeEnum.CUSTOM.getCode() == stage.getStageTemplateType() && stage.isEnvironmentChange())){
                dockerEnvironmentExit = true;
                BuildEnvironment buildEnvironment = buildEnvironmentMapper.selectByPrimaryKey(stage.getBuildEnvironmentId());
                newStageDto.setBuildEnvironment(buildEnvironment.getImage());
            }
            stageDtoList.add(newStageDto);
        }
        dataModel.put("secret", clusterService.getClusterNameByClusterId(job.getClusterId()) + "-secret");
        dataModel.put("apiUrl", apiUrl);
        dataModel.put("job", job);
        dataModel.put("dockerFileMap", dockerFileMap);
        dataModel.put("stageList", stageDtoList);
        dataModel.put("imageBuildStages", imageBuildStages);
        String script = null;

        try {
            script = TemplateUtil.generate("pipeline.ftl", dataModel);
        }catch(Exception e){
            e.printStackTrace();
        }
        return script;
    }


}