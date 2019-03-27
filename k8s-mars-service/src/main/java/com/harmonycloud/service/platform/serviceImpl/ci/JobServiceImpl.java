package com.harmonycloud.service.platform.serviceImpl.ci;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.DataResourceTypeEnum;
import com.harmonycloud.common.enumm.DockerfileTypeEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.StageTemplateTypeEnum;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.application.bean.ConfigFile;
import com.harmonycloud.dao.application.bean.ConfigFileItem;
import com.harmonycloud.dao.application.bean.ServiceTemplates;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dao.harbor.bean.ImageRepository;
import com.harmonycloud.dao.tenant.bean.Project;
import com.harmonycloud.dao.tenant.bean.TenantBinding;
import com.harmonycloud.dto.application.*;
import com.harmonycloud.dto.cicd.CicdConfigDto;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.dto.cicd.ParameterDto;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.dto.config.ConfigDetailDto;
import com.harmonycloud.k8s.bean.*;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.client.K8sMachineClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.constant.Resource;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.k8s.util.K8SURL;
import com.harmonycloud.service.application.*;
import com.harmonycloud.service.cache.ImageCacheManager;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.DataPrivilegeHelper;
import com.harmonycloud.service.dataprivilege.DataPrivilegeService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.bean.harbor.HarborProjectInfo;
import com.harmonycloud.service.platform.bean.harbor.HarborRepositoryMessage;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ConfigCenterService;
import com.harmonycloud.service.platform.service.ci.*;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.system.SystemConfigService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.tenant.NamespaceService;
import com.harmonycloud.service.tenant.ProjectService;
import com.harmonycloud.service.tenant.TenantService;
import com.harmonycloud.service.user.RoleLocalService;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.FolderJob;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.yaml.snakeyaml.Yaml;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by anson on 17/5/31.
 */
@Service
public class JobServiceImpl implements JobService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private StageMapper stageMapper;

    @Autowired
    private JobBuildMapper jobBuildMapper;

    @Autowired
    private StageBuildMapper stageBuildMapper;

    @Autowired
    private StageService stageService;

    @Autowired
    private HttpSession session;

    @Autowired
    private DeploymentsService deploymentsService;

    @Autowired
    private DeploymentService deploymentService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private BuildEnvironmentMapper buildEnvironmentMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ParameterService parameterService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private BlueGreenDeployService blueGreenDeployService;

    @Autowired
    private IntegrationTestService integrationTestService;

    @Autowired
    private NamespaceLocalService namespaceLocalService;

    @Autowired
    private StageTypeService stageTypeService;

    @Autowired
    private StageBuildService stageBuildService;

    @Autowired
    private RoleLocalService roleLocalService;

    @Autowired
    private JobBuildService jobBuildService;

    @Autowired
    private TriggerService triggerService;

    @Autowired
    private DockerFileService dockerFileService;

    @Autowired
    private HarborService harborService;

    @Autowired
    private ConfigCenterService configCenterService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NamespaceService namespaceService;

    @Value("#{propertiesReader['web.url']}")
    private String webUrl;

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;

    @Value("#{propertiesReader['jenkins.timeout']}")
    private String jenkinsTimeout;


    @Value("${build.nodeselector:HarmonyCloud_Status=E}")
    private String buildNodeSelector;

    @Autowired
    private SecretService secretService;
    private long sleepTime = 2000L;

    @Autowired
    private HarborProjectService harborProjectService;

    @Autowired
    private DataPrivilegeService dataPrivilegeService;

    @Autowired
    private DataPrivilegeHelper dataPrivilegeHelper;

    @Autowired
    private ImageCacheManager imageCacheManager;

    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createJob(JobDto jobDto) throws Exception {
        Job job;
        //新建或复制
        if (jobDto.getCopyId() == null) {
            job = jobDto.convertToBean();
        } else {
            job = this.getJobById(jobDto.getCopyId());
            if (job == null) {
                throw new MarsRuntimeException(ErrorCodeMessage.COPIED_PIPELINE_NOT_EXIST);
            }
            job.setName(jobDto.getName());
            job.setDescription(jobDto.getDescription());
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

        String username = (String) session.getAttribute("username");
        job.setCreateUser(username);
        job.setCreateTime(new Date());
        jobMapper.insertJob(job);
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        //新建或复制
        if (jobDto.getCopyId() == null) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("stageList", new ArrayList<>());
            dataModel.put("job", job);
            dataModel.put("apiUrl", apiUrl);
            dataModel.put("timeout", jenkinsTimeout);
            dataModel.put("nodeSelector", buildNodeSelector);
            dataModel.put("harborAddress", clusterService.findClusterById(jobDto.getClusterId()).getHarborServer().getHarborAddress());
            String script = TemplateUtil.generate("pipeline.ftl", dataModel);
            dataModel.put("script", script);
            String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            logger.info("构建流水线pod nodeSelector:{}", buildNodeSelector);
            try {
                jenkinsServer.createJob(folderJob, job.getName(), body);
            } catch (Exception e) {
                logger.error("新建流水线失败", e);
                throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_CREATE_ERROR);
            }
        } else {
            Trigger trigger = triggerService.getTrigger(jobDto.getCopyId());
            if (trigger != null) {
                trigger.setJobId(job.getId());
                triggerService.insertTrigger(trigger);
            }
            ParameterDto parameterDto = parameterService.getParameter(jobDto.getCopyId());
            parameterDto.setJobId(job.getId());
            parameterService.insertParameter(parameterDto);

            Stage stageExample = new Stage();
            stageExample.setJobId(jobDto.getCopyId());
            List<Stage> stageList = stageService.selectByExample(stageExample);
            for (Stage stage : stageList) {
                stage.setJobId(job.getId());
                stage.setCreateTime(DateUtil.getCurrentUtcTime());
                stage.setCreateUser(username);
                stageService.insert(stage);
                if (StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()) {
                    stageService.createOrUpdateCredential(stage.getId(), stage.getCredentialsUsername(), DesUtil.decrypt(stage.getCredentialsPassword(), null));
                }
            }
            jenkinsServer.createJob(folderJob, job.getName(), generateJobBody(job));
        }
        //初始化数据权限
        dataPrivilegeService.addResource(job, null, null);
        return job.getId();
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteJob(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if (null == job) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NOT_EXIST);
        }
        String projectName = getProjectNameByProjectId(job.getProjectId());
        String clusterName = getClusterNameByClusterId(job.getClusterId());

        //删除数据库中的流水线与步骤数据
        jobMapper.deleteJobById(id);
        stageMapper.deleteStageByJob(id);
        //删除数据库中的构建记录
        jobBuildService.deleteByJobId(id);
        stageBuildService.deleteByJobId(id);

        //删除jenkins中的流水线
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        JobWithDetails jobWithDetails = jenkinsServer.getJob(folderJob, job.getName());
        if (jobWithDetails == null) {
            return;
        }
        try {
            jenkinsServer.deleteJob(folderJob, job.getName());
        } catch (Exception e) {
            logger.error("删除流水线失败", e);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_DELETE_ERROR);
        }
        //删除数据权限
        dataPrivilegeService.deleteResource(job);
    }

    @Override
    public void validateName(String jobName, String projectId, String clusterId) throws Exception {
        String projectName = getProjectNameByProjectId(projectId);
        String clusterName = getClusterNameByClusterId(clusterId);
        checkFolderJobExist(projectName, clusterName);
        validateJobName(jobName, projectName, clusterName);
    }

    @Override
    public List getJobList(String projectId, String clusterId, String type, String jobName) throws Exception {
        List<Map> jobMapList = new ArrayList();
        List<JobWithBuild> jobList = new ArrayList<>();
        List<Cluster> clusterList = roleLocalService.listCurrentUserRoleCluster();
        Map<String, Cluster> clusterMap = clusterList.stream().collect(Collectors.toMap(Cluster::getId, cluster -> cluster));
        if (StringUtils.isNotEmpty(clusterId)) {
            jobList = jobMapper.selectJobWithLastBuild(projectId, clusterId, jobName, type);
        } else {
            for (Cluster cluster : clusterList) {
                jobList.addAll(jobMapper.selectJobWithLastBuild(projectId, cluster.getId(), jobName, type));
            }
        }
        for (JobWithBuild jobWithBuild : jobList) {
            Map<String, Object> jobMap = new HashMap<>();
            jobMap.put("id", jobWithBuild.getId());
            jobMap.put("clusterId", jobWithBuild.getClusterId());
            Cluster cluster = clusterMap.get(jobWithBuild.getClusterId());
            jobMap.put("clusterName", cluster != null ? cluster.getAliasName() : null);
            jobMap.put("name", jobWithBuild.getName());
            jobMap.put("description", jobWithBuild.getDescription());
            jobMap.put("type", jobWithBuild.getType());
            jobMap.put("lastBuildStatus", StringUtils.isBlank(jobWithBuild.getStatus()) ? Constant.PIPELINE_STATUS_NOTBUILT : jobWithBuild.getStatus());
            jobMap.put("lastBuildTime", jobWithBuild.getStartTime());
            jobMap.put("lastBuildNumber", jobWithBuild.getBuildNum());
            jobMapList.add(jobMap);
        }

        return dataPrivilegeHelper.filterMap(jobMapList, DataResourceTypeEnum.PIPELINE);
    }

    @Override
    public ActionReturnUtil getJobDetail(Integer id) throws Exception {
        Job dbJob = jobMapper.queryById(id);
        String projectName = getProjectNameByProjectId(dbJob.getProjectId());
        String clusterName = getClusterNameByClusterId(dbJob.getClusterId());
        Map job = new HashMap();
        job.put("id", id);
        job.put("jobName", dbJob.getName());
        job.put("description", dbJob.getDescription());
        job.put("type", dbJob.getType());
        job.put("tenant", dbJob.getTenant());
        job.put("clusterId", dbJob.getClusterId());

        JobWithBuild jobWithBuild = jobMapper.selectJobWithLastBuildById(id);
        job.put("lastBuildStatus", StringUtils.isBlank(jobWithBuild.getStatus()) ? Constant.PIPELINE_STATUS_NOTBUILT : jobWithBuild.getStatus());
        job.put("lastBuildTime", jobWithBuild.getStartTime());
        job.put("buildNum", jobWithBuild.getBuildNum());

        //返回工作空间
        job.put("workspace", "/home/workspace/" + projectName + "/" + clusterName + "/" + dbJob.getName());


        List<Map> stageMapList = new ArrayList<>();

        //get stage type info of stages for the job
        List<Stage> stageList = stageMapper.queryByJobId(id);
        List<StageType> stageTypeList = stageTypeService.queryByType(dbJob.getType());
        Map stageTypeMap = new HashMap<>();
        for (StageType stageType : stageTypeList) {
            stageTypeMap.put(stageType.getId(), stageType.getName());
        }

        for (Stage stage : stageList) {
            Map stageMap = new HashMap<>();
            stageMap.put("id", stage.getId());
            stageMap.put("stageName", stage.getStageName());
            stageMap.put("stageType", stageTypeMap.get(stage.getStageTypeId()));
            stageMap.put("stageOrder", stage.getStageOrder());

            StageBuild stageBuild = stageBuildService.selectLastBuildById(stage.getId());
            stageMap.put("lastBuildStatus", stageBuild == null ? null : stageBuild.getStatus());
            stageMap.put("lastBuildTime", stageBuild == null ? null : stageBuild.getStartTime());


            if (StringUtils.isEmpty((String) stageMap.get("lastBuildStatus"))) {
                if (Constant.PIPELINE_STATUS_BUILDING.equals(job.get("lastBuildStatus")) && stage.getStageOrder() == 1) {
                    stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_WAITING);
                } else {
                    stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                }
                stageMap.put("lastBuildTime", null);
            }
            stageMap.put("buildNum", jobWithBuild.getBuildNum());

            stageMapList.add(stageMap);
        }


        job.put("stageList", stageMapList);

        return ActionReturnUtil.returnSuccessWithData(dataPrivilegeHelper.filterMap(job, DataResourceTypeEnum.PIPELINE));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer build(Integer id, List<Map<String, Object>> parameters, String image, String tag) throws Exception {
        Job job = jobMapper.queryById(id);
        String jobName = job.getName();
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        Integer lastBuildNumber = null;
        Map<String, Object> params;
        List<Stage> stageList = stageMapper.queryByJobId(id);
        //校验步骤信息
        validateJob(job, stageList, StringUtils.isBlank(image));
        Map tagMap = new HashMap<>();

        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        params = new HashMap<>();
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        JobWithDetails jobWithDetails = jenkinsServer.getJob(folderJob, job.getName());
        lastBuildNumber = jobWithDetails.getNextBuildNumber();
        jobMapper.updateLastBuildNum(id, lastBuildNumber);
        JobBuild jobBuild = new JobBuild();
        jobBuild.setJobId(id);
        jobBuild.setBuildNum(lastBuildNumber);
        List<JobBuild> jobBuildList = jobBuildService.queryByObject(jobBuild);
        if (CollectionUtils.isEmpty(jobBuildList)) {
            jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
            jobBuild.setStartUser(image != null ? null : (String) session.getAttribute(CommonConstant.USERNAME));
            jobBuildService.insert(jobBuild);
        } else {
            jobBuild = jobBuildList.get(0);
            jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
            jobBuild.setStartUser(image != null ? null : (String) session.getAttribute(CommonConstant.USERNAME));
            jobBuildService.update(jobBuild);
        }

        Map<Integer, StageType> stageTypeMap = new HashMap<>();
        List<StageType> stageTypeList = stageTypeService.queryByType(job.getType());
        for (StageType stageType : stageTypeList) {
            stageTypeMap.put(stageType.getId(), stageType);
        }

        for (Stage stage : stageList) {
            StageDto stageDto = new StageDto();
            stageDto.convertFromBean(stage);
            stageService.verifyStageResource(job, stageDto);
            StageBuild stageBuild = new StageBuild();
            stageBuild.setJobId(id);
            stageBuild.setStageId(stage.getId());
            stageBuild.setStageName(stage.getStageName());
            stageBuild.setStageOrder(stage.getStageOrder());
            stageBuild.setStageTypeId(stage.getStageTypeId());
            StageType stageType = stageTypeMap.get(stage.getStageTypeId());
            stageBuild.setStageType(stageType.getName());
            stageBuild.setStageTemplateTypeId(stageType.getTemplateType());
            stageBuild.setBuildNum(lastBuildNumber);
            if (stage.getStageOrder() == 1) {
                stageBuild.setStatus(Constant.PIPELINE_STATUS_WAITING);
            } else {
                stageBuild.setStatus(Constant.PIPELINE_STATUS_NOTBUILT);
            }
            if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()) {
                if (CommonConstant.IMAGE_TAG_RULE.equals(stage.getImageTagType())) {
                    stage.setImageBaseTag(generateTag(stage));
                    stageMapper.updateStage(stage);
                }
            }
            stageBuildMapper.insert(stageBuild);
        }

        params.put("delay", "0sec");
        //构建时带上参数，若为空则使用默认参数
        if (CollectionUtils.isNotEmpty(parameters)) {
            for (Map<String, Object> parameterMap : parameters) {
                params.put((String) parameterMap.get("name"), String.valueOf(parameterMap.get("value")));
            }
        } else {
            ParameterDto parameterDto = parameterService.getParameter(id);
            if (CollectionUtils.isNotEmpty(parameterDto.getParameters())) {
                for (Map<String, Object> parameterMap : parameterDto.getParameters()) {
                    if (CommonConstant.STRING_TYPE_PARAMETER == (Integer) parameterMap.get("type")) {
                        params.put((String) parameterMap.get("name"), String.valueOf(parameterMap.get("value")));
                    } else if (CommonConstant.CHOICE_TYPE_PARAMETER == (Integer) parameterMap.get("type")) {
                        String value = (String) parameterMap.get("value");
                        String[] choices = value.split("\n");
                        params.put((String) parameterMap.get("name"), choices[0]);
                    }
                }
            }
        }
        try {
            //jobWithDetails.build(params);
            ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + jobName + "/buildWithParameters", null, params, null, null);
        } catch (Exception e) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_BUILD_ERROR);
        }
        //result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/buildWithParameters", null, params, null, null);
        //if(result.isSuccess()){
        return lastBuildNumber;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stopBuild(Integer jobId, String buildNum) throws Exception {
        Job job = jobMapper.queryById(jobId);
        if (job == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NOT_EXIST);
        }
        String jobName = job.getName();
        String projectName = getProjectNameByProjectId(job.getProjectId());
        String clusterName = getClusterNameByClusterId(job.getClusterId());
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        JobWithDetails jobWithDetails = jenkinsServer.getJob(folderJob, jobName);
        if (jobWithDetails == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_NOT_EXIST_IN_JENKINS);
        }
        Build build = jobWithDetails.getBuildByNumber(Integer.valueOf(buildNum));
        try {
            build.Stop();
        } catch (Exception e) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_BUILD_STOP_ERROR);
        }
        List<Stage> stageList = stageService.getStageByJobId(jobId);
        StageBuild condition = new StageBuild();
        condition.setJobId(jobId);
        condition.setBuildNum(Integer.valueOf(buildNum));
        List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(condition);
        if (CollectionUtils.isNotEmpty(stageBuildList)) {
            boolean abort = false;
            for (StageBuild stageBuild : stageBuildList) {
                if (StageTemplateTypeEnum.CODESCAN.getCode() == stageBuild.getStageTemplateTypeId() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stageBuild.getStageTemplateTypeId()) {
                    if (Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()) || Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus())) {
                        stageBuild.setStatus(Constant.PIPELINE_STATUS_FAILED);
                        stageBuildService.updateStageBuildByStageIdAndBuildNum(stageBuild);
                        abort = true;
                    }
                }
            }
            if (abort) {
                JobBuild jobBuildCondition = new JobBuild();
                jobBuildCondition.setBuildNum(Integer.valueOf(buildNum));
                jobBuildCondition.setJobId(jobId);
                List<JobBuild> jobBuildList = jobBuildService.queryByObject(jobBuildCondition);
                if (CollectionUtils.isNotEmpty(jobBuildList)) {
                    JobBuild jobBuild = jobBuildList.get(0);
                    if (Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus())) {
                        jobBuild.setStatus(Constant.PIPELINE_STATUS_ABORTED);
                        jobBuildService.update(jobBuild);
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActionReturnUtil deleteBuild(Integer id, String buildNum) throws Exception {
        Job job = jobMapper.queryById(id);
        String jenkinsJobName = job.getTenant() + job.getName();
        jobBuildMapper.deleteByJobId(id);
        stageBuildMapper.deleteByJobId(id);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/" + buildNum + "/doDelete", null, null, null, null);
        if (result.isSuccess()) {
            return ActionReturnUtil.returnSuccess();
        } else {
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
        for (Stage stage : stageList) {
            stageMap.put(stage.getId(), stage);
        }
        Map jobBuildMap = new HashMap<>();
        for (JobBuild jobBuild : jobBuildList) {
            jobBuildMap.put(jobBuild.getBuildNum(), jobBuild);
        }
        for (JobBuild jobBuild : jobBuildList) {
            int staticCount = 0;
            int staticSuccessCount = 0;
            int testCount = 0;
            int testSuccessCount = 0;
            Integer jobDuration = 0;
            Map buildMap = new HashMap();
            if (Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus())) {
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
            for (StageBuild stageBuild : stageBuildList) {
                if (!Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus()) && (Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus()) || Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()))) {
                    allStageStatusSync(job, jobBuild.getBuildNum());
                    stageBuildMapper.updateWaitingStage(job.getId(), jobBuild.getBuildNum());
                    stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
                    jobDuration = 0;
                    staticCount = 0;
                    staticSuccessCount = 0;
                    testCount = 0;
                    testSuccessCount = 0;
                    for (StageBuild newStageBuild : stageBuildList) {
                        if (StageTemplateTypeEnum.CODESCAN.getCode() == newStageBuild.getStageTemplateTypeId()) {
                            staticCount++;
                            if (newStageBuild.getTestResult() != null && newStageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0) {
                                staticSuccessCount++;
                            }
                        } else if (StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == newStageBuild.getStageTemplateTypeId()) {
                            testCount++;
                            if (newStageBuild.getTestResult() != null && newStageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0) {
                                testSuccessCount++;
                            }
                        }
                        //stageBuildMapList.add(stageBuildMap);
                        if (StringUtils.isNumeric((String) newStageBuild.getDuration())) {
                            jobDuration += (int) Math.ceil(Long.parseLong((String) newStageBuild.getDuration()) / 1000.0) * 1000;
                        }
                    }
                    break;
                }
                if (stageBuild.getStageTemplateTypeId() != null && StageTemplateTypeEnum.CODESCAN.getCode() == stageBuild.getStageTemplateTypeId()) {
                    staticCount++;
                    if (stageBuild.getTestResult() != null && stageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0) {
                        staticSuccessCount++;
                    }
                } else if (stageBuild.getStageTemplateTypeId() != null && StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stageBuild.getStageTemplateTypeId()) {
                    testCount++;
                    if (stageBuild.getTestResult() != null && stageBuild.getTestResult().indexOf(CommonConstant.SUCCESS) == 0) {
                        testSuccessCount++;
                    }
                }
                if (StringUtils.isNumeric((String) stageBuild.getDuration())) {
                    jobDuration += (int) Math.ceil(Long.parseLong((String) stageBuild.getDuration()) / 1000.0) * 1000;
                }
            }
            buildMap.put("stageCount", stageCount);
            buildMap.put("duration", jobDuration);
            buildMap.put("staticResult", String.valueOf(staticSuccessCount) + "/" + staticCount);
            buildMap.put("testResult", String.valueOf(testSuccessCount) + "/" + testCount);
            buildList.add(buildMap);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("pageSize", pageSize);
        data.put("page", page);
        data.put("totalPage", Math.ceil(1.0 * total / pageSize));
        data.put("buildList", buildList);
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public void getJobLogWS(WebSocketSession session, Integer id, String buildNum) {
        try {
            Job job = jobMapper.queryById(id);
            String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
            String start = "0";
            String moreData = "";
            long duration = 0L;
            while (moreData != null && session.isOpen()) {
                long startTime = System.currentTimeMillis();
                moreData = null;
                Map<String, Object> params = new HashMap<>();
                params.put("start", start);
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/" + buildNum + "/logText/progressiveHtml", null, params, true);
                if (result.isSuccess()) {
                    if (result.get("data") != null) {
                        Header[] headers = (Header[]) ((Map) result.get("data")).get("header");
                        for (Header header : headers) {
                            //当还有日志输出时，会有X-More-Data
                            if ("X-More-Data".equalsIgnoreCase(header.getName())) {
                                moreData = header.getValue();
                            }
                            //当前已经获取的日志标记，下一次从该处取新的日志
                            if ("X-Text-Size".equalsIgnoreCase(header.getName())) {
                                start = header.getValue();
                            }
                        }
                        //当获取到的日志非空或30秒内无返回时，返回日志内容
                        if (StringUtils.isNotEmpty((String) ((Map) result.get("data")).get("body")) || duration > CommonConstant.CICD_WEBSOCKET_MAX_DURATION) {
                            duration = 0L;
                            String log = ((String) ((Map) result.get("data")).get("body")).replaceAll("</?[^>]+>", "");
                            session.sendMessage(new TextMessage(log));
                        }
                    }
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    logger.error("获取流水线日志失败", e);
                }
                duration += System.currentTimeMillis() - startTime;
            }

        } catch (Exception e) {
            logger.warn("获取流水线日志失败", e);
        } finally {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                logger.warn("session关闭失败", e);
            }
        }
    }


    @Override
    public ActionReturnUtil getNotification(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if (null == job) {
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
    @Transactional(rollbackFor = Exception.class)
    public ActionReturnUtil updateNotification(JobDto jobDto) throws Exception {
        Job job = jobDto.convertToBean();
        String username = (String) session.getAttribute("username");
        job.setUpdateUser(username);
        job.setUpdateTime(new Date());
        jobMapper.updateNotification(job);
        updateJenkinsJob(job.getId());
        return ActionReturnUtil.returnSuccess();
    }


    @Override
    public void preBuild(Integer id, Integer buildNum, String dateTime) throws Exception {
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
        } catch (Exception e) {
            logger.error("job运行失败,集群信息错误,job:{}", JSONObject.toJSONString(job), e);
            return;
        }

        Map tagMap = new HashMap<>();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/config.xml", null, null, false);
        if (result.isSuccess()) {
            Map body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map definition = (Map) body.get("flow-definition");
            Object param = ((Map) ((Map) ((Map) definition.get("properties")).get("hudson.model.ParametersDefinitionProperty")).get("parameterDefinitions")).get("hudson.model.StringParameterDefinition");
            if (param instanceof List) {
                for (Map map : (List<Map>) param) {
                    tagMap.put(map.get("name"), map.get("defaultValue"));
                }
            }
        } else {
            logger.error("获取流水线信息失败", result.getData());
            throw new MarsRuntimeException(ErrorCodeMessage.JENKINS_PIPELINE_INFO_GET_ERROR);
        }

        JobBuild jobbuild = new JobBuild();
        jobbuild.setJobId(id);
        jobbuild.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobbuild);
        List<Stage> stageList = stageService.getStageByJobId(id);
        Map<String, String> buildImageMap = new HashMap<>();
        if (jobBuildList == null || jobBuildList.size() == 0) {
            Map params = new HashMap<>();
            params.put("tree", "number,timestamp");
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/job/" + job.getName() + "/lastBuild/api/xml", null, params, false);
            if (result.isSuccess()) {
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
                for (StageType stageType : stageTypeList) {
                    stageTypeMap.put(stageType.getId(), stageType);
                }

                for (Stage stage : stageList) {
                    StageBuild stageBuild = new StageBuild();
                    stageBuild.setJobId(id);
                    stageBuild.setStageId(stage.getId());
                    stageBuild.setStageName(stage.getStageName());
                    stageBuild.setStageOrder(stage.getStageOrder());
                    stageBuild.setStageTypeId(stage.getStageTypeId());
                    StageType stageType = stageTypeMap.get(stage.getStageTypeId());
                    stageBuild.setStageType(stageType.getName());
                    stageBuild.setStageTemplateTypeId(stageType.getTemplateType());
                    stageBuild.setBuildNum(lastBuildNumber);
                    stageBuild.setStatus(Constant.PIPELINE_STATUS_NOTBUILT);
                    if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()) {
                        if (CommonConstant.IMAGE_TAG_TIMESTAMP.equals(stage.getImageTagType())) {
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + dateTime);
                        } else {
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + tagMap.get("tag" + stage.getStageOrder()));
                        }
                        buildImageMap.put(stageBuild.getImage().split(":")[0], stageBuild.getImage().split(":")[1]);
                        if (CommonConstant.IMAGE_TAG_RULE.equals(stage.getImageTagType())) {
                            stage.setImageBaseTag(generateTag(stage));
                            stageMapper.updateStage(stage);
                        }
                    } else if (StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()) {
                        if(buildImageMap.get(stage.getImageName()) != null && stage.getOriginStageId() != null && StringUtils.isBlank(stage.getImageTag())){
                            stage.setImageTag(buildImageMap.get(stage.getImageName()));
                        }
                        updateDeployStageBuild(stage, stageBuild);
                    }
                    stageBuildMapper.insert(stageBuild);
                }
            } else {
                logger.error("获取流水线信息失败", result.getData());
                throw new MarsRuntimeException(ErrorCodeMessage.JENKINS_PIPELINE_INFO_GET_ERROR);
            }
        } else {
            for (Stage stage : stageList) {
                StageBuild condition = new StageBuild();
                condition.setStageId(stage.getId());
                condition.setBuildNum(buildNum);
                List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(condition);
                if (CollectionUtils.isNotEmpty(stageBuildList)) {
                    StageBuild stageBuild = stageBuildList.get(0);

                    if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()) {
                        if (CommonConstant.IMAGE_TAG_TIMESTAMP.equals(stage.getImageTagType())) {
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + dateTime);
                        } else {
                            stageBuild.setImage(stage.getHarborProject() + "/" + stage.getImageName() + ":" + tagMap.get("tag" + stage.getStageOrder()));
                        }
                        buildImageMap.put(stageBuild.getImage().split(":")[0], stageBuild.getImage().split(":")[1]);
                    } else if (StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType() && StringUtils.isBlank(stageBuild.getImage())) {
                        if(buildImageMap.get(stage.getImageName()) != null && stage.getOriginStageId() == null && StringUtils.isBlank(stage.getImageTag())){
                            stage.setImageTag(buildImageMap.get(stage.getImageName()));
                        }
                        updateDeployStageBuild(stage, stageBuild);
                    }
                    stageBuildMapper.updateByStageOrderAndBuildNum(stageBuild);
                }
            }
        }
        //校验步骤配置
        try {
            for (Stage stage : stageList) {
                StageDto stageDto = new StageDto();
                stageDto.convertFromBean(stage);
                stageService.verifyStageResource(job, stageDto);
            }
        } catch (MarsRuntimeException e) {
            ErrorCodeMessage errorCodeMessage = ErrorCodeMessage.valueOf(e.getErrorCode());
            throw new MarsRuntimeException(errorCodeMessage.getReasonEnPhrase() + "(" + errorCodeMessage.getReasonChPhrase() + ")");
        }
        //更新Jenkins中镜像tag
        updateJenkinsImageTag(job);
    }

    private void updateDeployStageBuild(Stage stage, StageBuild stageBuild) throws Exception {
        String tag = stage.getImageTag();
        Integer stageId = stage.getOriginStageId();
        //根据镜像来源流水线步骤是否为空区分镜像来源于流水线或镜像仓库
        if (stageId == null) {
            if (StringUtils.isNotBlank(tag)) {
                stageBuild.setImage(stage.getImageName() + ":" + tag);
            } else {
                Job job = getJobById(stage.getJobId());
                String image = stage.getImageName();
                if (StringUtils.isBlank(image)) {
                    throw new MarsRuntimeException(ErrorCodeMessage.DEPLOY_IMAGE_NOT_EXIST);
                }
                String[] imageArray = image.split("/");
                if (imageArray.length != 2) {
                    throw new MarsRuntimeException(ErrorCodeMessage.DEPLOY_IMAGE_NOT_EXIST);
                }
                String repository = imageArray[0];
                ActionReturnUtil result = harborService.getFirstImage(job.getProjectId(), job.getClusterId(), repository, image);
                if (result.isSuccess()) {
                    List<HarborProjectInfo> list = (List<HarborProjectInfo>) result.getData();
                    for (HarborProjectInfo harborProjectInfo : list) {
                        if (harborProjectInfo.getProject_name().equals(repository)) {
                            List<HarborRepositoryMessage> harborRepositoryMessageList = harborProjectInfo.getHarborRepositoryMessagesList();
                            for (HarborRepositoryMessage harborRepositoryMessage : harborRepositoryMessageList) {
                                if (image.equals(harborRepositoryMessage.getRepository())) {
                                    List<String> tags = harborRepositoryMessage.getTags();
                                    if (CollectionUtils.isNotEmpty(tags)) {
                                        tag = tags.get(0);
                                    } else {
                                        throw new MarsRuntimeException(ErrorCodeMessage.DEPLOY_IMAGE_NOT_EXIST);
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }

                }

                stageBuild.setImage(stage.getImageName() + ":" + tag);
            }
        } else {
            Stage ciStage = stageService.selectByPrimaryKey(stageId);
            if (ciStage == null) {
                return;
            }
            StageBuild condition = new StageBuild();
            condition.setStageId(ciStage.getId());
            condition.setStatus(Constant.PIPELINE_STATUS_SUCCESS);
            List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(condition);
            for (StageBuild ciStageBuild : stageBuildList) {
                if (ciStageBuild.getImage() != null && ciStageBuild.getImage().contains(stage.getImageName())) {
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
                int retry = 0;
                while (retry <= 3) {
                    try {

                        Thread.sleep(3000);
                        Job job = jobMapper.queryById(id);
                        allStageStatusSync(job, buildNum);
                        syncJobStatus(job, buildNum);
                        stageBuildMapper.updateWaitingStage(job.getId(), buildNum);
                        break;
                    } catch (Exception e) {
                        logger.error("流水线状态更新失败, retry:{}", retry, e);
                        try {
                            if (retry > 0) {
                                Thread.sleep(CommonConstant.CICD_SLEEP_TIME_300000);
                            }
                        } catch (InterruptedException e1) {
                        }
                        retry++;
                    }
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
                int retry = 0;
                while (retry <= 3) {
                    try {
                        Thread.sleep(500);
                        stageStatusSync(id, buildNum);
                        break;
                    } catch (Exception e) {
                        logger.error("同步步骤状态失败,retry: {}", retry, e);
                        try {
                            if (retry > 0) {
                                Thread.sleep(CommonConstant.CICD_SLEEP_TIME_30000);
                            }
                        } catch (InterruptedException e1) {
                        }
                        retry++;
                    }
                }
            }
        };
        NewCachedThreadPool threadPool = NewCachedThreadPool.init();
        threadPool.execute(worker);
    }

    @Override
    public void deploy(Integer stageId, Integer buildNum) throws Exception {
        Stage stage = stageMapper.queryById(stageId);
        if (StringUtils.isBlank(stage.getServiceName())) {
            throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_NAME_NOT_BLANK);
        }
        Job job = jobMapper.queryById(stage.getJobId());

        StageDto stageDto = new StageDto();
        stageDto.convertFromBean(stage);


        Cluster cluster = clusterService.findClusterById(job.getClusterId());
        //根据升级类型进行升级，全新发布已废弃
        if (CommonConstant.FRESH_RELEASE.equals(stage.getDeployType())) {
            doFreshRelease(job, stageDto, cluster, buildNum);
        } else if (CommonConstant.CANARY_RELEASE.equals(stage.getDeployType())) {
            doCanaryRelease(job, stageDto, cluster, buildNum);
        } else if (CommonConstant.BLUE_GREEN_RELEASE.equals(stage.getDeployType())) {
            doBlueGreenRelease(job, stageDto, cluster, buildNum);
        }

    }

    @Override
    public void jobStatusWS(WebSocketSession session, Integer id) {
        try {
            //根据job id查询job
            Job job = jobMapper.queryById(id);
            String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
            String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
            //获取stageList
            List<Stage> dbStageList = stageMapper.queryByJobId(id);
            StringBuilder lastStatus = new StringBuilder();
            StringBuilder currentStatus = null;
            long duration = 0L;
            //拼接Jenkins流水线状态和步骤状态的URL
            JenkinsUrl lastBuildStatusUrl = new JenkinsUrl();
            lastBuildStatusUrl.setFolders(projectName, clusterName);
            lastBuildStatusUrl.setName(job.getName());
            lastBuildStatusUrl.setApi("lastBuild/api/json");
            JenkinsUrl lastBuildDesUrl = new JenkinsUrl();
            lastBuildDesUrl.setFolders(projectName, clusterName);
            lastBuildDesUrl.setName(job.getName());
            lastBuildDesUrl.setApi("lastBuild/wfapi/describe");
            //循环查询流水线和步骤状态
            while (session.isOpen()) {
                long startTime = System.currentTimeMillis();
                List<Stage> dbStageListCp = new ArrayList<>();
                dbStageListCp.addAll(dbStageList);
                currentStatus = new StringBuilder();
                StringBuilder stageStatus = new StringBuilder();
                Map jenkinsJobMap = new HashMap<>();
                String jobStatus = null;
                //获取Jenkins流水线状态
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest(lastBuildStatusUrl.getUrl(), null, null, false);
                if (result.isSuccess()) {
                    jenkinsJobMap = JsonUtil.convertJsonToMap((String) result.get("data"));
                    if ((boolean) jenkinsJobMap.get("building") == true) {
                        jobStatus = Constant.PIPELINE_STATUS_BUILDING;
                    } else {
                        jobStatus = (String) jenkinsJobMap.get("result");
                    }
                }
                //获取步骤的构建信息
                result = HttpJenkinsClientUtil.httpGetRequest(lastBuildDesUrl.getUrl(), null, null, false);
                if (result.isSuccess()) {
                    String data = (String) result.get("data");
                    Map dataMap = JsonUtil.convertJsonToMap(data);
                    List<Map> stages = (List<Map>) dataMap.get("stages");
                    int i = 0;
                    List stageList = new ArrayList<>();
                    String status = "";
                    //遍历Jenkins中获取的步骤
                    for (Map stage : stages) {
                        Map stageMap = new HashMap<>();
                        stageMap.put("lastBuildStatus", convertStatus((String) stage.get("status")));
                        stageMap.put("lastBuildDuration", String.valueOf(stage.get("durationMillis")));
                        if (stage.get("startTimeMillis") instanceof Long) {
                            stageMap.put("lastBuildTime", new Timestamp((Long) stage.get("startTimeMillis")));
                        } else {
                            stageMap.put("lastBuildTime", new Timestamp(Long.valueOf((Integer) stage.get("startTimeMillis"))));
                        }
                        //遍历数据库的步骤列表，匹配到对应步骤
                        for (Stage dbStage : dbStageListCp) {
                            String jenkinsStageName = stage.get("name").toString();
                            String[] stageArray = jenkinsStageName.split("-");
                            if (dbStage.getId().toString().equals(stageArray[stageArray.length - CommonConstant.NUM_ONE])) {
                                stageMap.put("stageId", dbStage.getId());
                                stageMap.put("stageOrder", dbStage.getStageOrder());
                                //静态扫描和集成测试步骤的状态取自数据库构建记录表，并且决定流水线最终状态
                                if (StageTemplateTypeEnum.CODESCAN.getCode() == dbStage.getStageTemplateType() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == dbStage.getStageTemplateType()) {
                                    StageBuild stageBuild = stageBuildService.selectLastBuildById(dbStage.getId());
                                    stageMap.put("lastBuildStatus", stageBuild.getStatus());
                                    if (Constant.PIPELINE_STATUS_FAILED.equals(stageBuild.getStatus()) && StringUtils.isBlank(status)) {
                                        status = Constant.PIPELINE_STATUS_FAILURE;
                                    } else if (Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus())) {
                                        status = Constant.PIPELINE_STATUS_BUILDING;
                                    }
                                }
                                dbStageListCp.remove(dbStageListCp.indexOf(dbStage));
                                break;
                            }
                        }
                        i++;
                        stageList.add(stageMap);
                        stageStatus.append(convertStatus((String) stage.get("status")));
                    }
                    //数据库中没匹配到的步骤，则还没有开始构建
                    for (Stage stage : dbStageListCp) {
                        Map stageMap = new HashMap<>();
                        stageMap.put("stageId", stage.getId());
                        stageMap.put("stageOrder", stage.getStageOrder());
                        //若步骤为第一步，且流水线为运行状态，则该步骤状态为等待中，其他都为未构建
                        if (stage.getStageOrder() == CommonConstant.NUM_ONE) {
                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_BUILDING.equals(jobStatus) ? Constant.PIPELINE_STATUS_WAITING : Constant.PIPELINE_STATUS_NOTBUILT);
                        } else {
                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                        }
                        stageList.add(stageMap);
                        stageStatus.append((String) stageMap.get("lastBuildStatus"));
                        i++;
                    }
                    //流水线状态以终止状态最为优先，其次为构建中，再为上述根据测试套件步骤决定的状态，最后为Jenkins中状态
                    JobBuild jobBuild = jobBuildService.queryLastBuildById(id);
                    if (Constant.PIPELINE_STATUS_ABORTED.equals(jobBuild.getStatus())) {
                        jobStatus = Constant.PIPELINE_STATUS_ABORTED;
                    } else if (StringUtils.isNotBlank(status) && !Constant.PIPELINE_STATUS_BUILDING.equals(jobStatus)) {
                        jobStatus = status;
                    }
                    //当前状态为流水线状态拼接每个步骤的状态
                    currentStatus.append(jobStatus);
                    currentStatus.append(stageStatus);

                    //当前状态与之前的状态不一致或30秒内没有返回时，则返回当前状态
                    if (!currentStatus.toString().equals(lastStatus.toString()) || duration > CommonConstant.CICD_WEBSOCKET_MAX_DURATION) {
                        duration = 0L;
                        Map jobMap = new HashMap<>();
                        jobMap.put("stageList", stageList);
                        jobMap.put("lastBuildStatus", jobStatus);
                        if (dataMap.get("startTimeMillis") instanceof Long) {
                            jobMap.put("lastBuildTime", new Timestamp((Long) dataMap.get("startTimeMillis")));
                        } else {
                            jobMap.put("lastBuildTime", new Timestamp(Long.valueOf((Integer) dataMap.get("startTimeMillis"))));
                        }

                        jobMap.put("lastBuildNum", jobBuild == null ? null : jobBuild.getBuildNum());
                        jobMap.put("lastBuildDuration", jenkinsJobMap.get("duration"));
                        session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobMap))));
                    }
                }
                lastStatus = currentStatus;

                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    logger.error("获取流水线状态失败", e);
                }
                duration += System.currentTimeMillis() - startTime;
            }
        } catch (Exception e) {
            logger.error("get job status error", e);
        } finally {
            try {
                if (session.isOpen()) {
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
        jobMap.put("kind", "Pipeline");
        jobMap.put("name", job.getName());
        Map notification = new LinkedHashMap<>();
        if (job.isNotification()) {
            notification.put("email", jobDto.getMail());
            notification.put("successNotification", job.isSuccessNotification());
            notification.put("failNotification", job.isFailNotification());
            jobMap.put("notification", notification);
        }
        Map buildEnvMap = new HashMap<>();
        List<BuildEnvironment> buildEnvList = buildEnvironmentMapper.queryAll();
        for (BuildEnvironment buildEnv : buildEnvList) {
            buildEnvMap.put(buildEnv.getId(), buildEnv.getName());
        }
        List stages = new ArrayList<>();
        List<Stage> stageList = stageMapper.queryByJobId(id);
        for (Stage stage : stageList) {
            StageDto stageDto = new StageDto();
            stageDto.convertFromBean(stage);
            Map stageMap = new LinkedHashMap<>();
            stageMap.put("name", stage.getStageName());
            stageMap.put("type", stage.getStageTypeId());

            if (StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()) {
                Map repository = new LinkedHashMap<>();
                repository.put("type", stage.getRepositoryType());
                repository.put("url", stage.getRepositoryUrl());
                if (StringUtils.isNotEmpty(stage.getRepositoryBranch())) {
                    repository.put("branch", stage.getRepositoryBranch());
                }
                if (StringUtils.isNotEmpty(stage.getCredentialsUsername())) {
                    repository.put("username", stage.getCredentialsUsername());
                }
                if (StringUtils.isNotEmpty(stage.getCredentialsUsername())) {
                    repository.put("password", "*");
                }
                stageMap.put("repository", repository);
                stageMap.put("buildEnvironment", buildEnvMap.get(stage.getBuildEnvironmentId()));
                if (CollectionUtils.isNotEmpty(stageDto.getEnvironmentVariables())) {
                    stageMap.put("environmentVariables", stageDto.getEnvironmentVariables());
                }
                if (CollectionUtils.isNotEmpty(stageDto.getDependences())) {
                    List<Map> depList = new ArrayList<>();
                    for (StageDto.Dependence dep : stageDto.getDependences()) {
                        Map map = BeanUtils.describe(dep);
                        map.remove("class");
                        map.remove("common");
                        depList.add(map);
                    }
                    stageMap.put("dependencies", depList);
                }
            } else if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()) {
                stageMap.put("DockerfileFrom", stage.getDockerfileType());
                if ("1".equals(stage.getDockerfileType())) {
                    stageMap.put("DockerfilePath", stage.getDockerfilePath());
                } else if ("2".equals(stage.getDockerfileType())) {
                    stageMap.put("DockerfileId", stage.getDockerfileId());
                }
                stageMap.put("image", stage.getImageName());
                stageMap.put("imageTagType", Integer.valueOf(stage.getImageTagType()));
                if ("2".equals(stage.getImageTagType())) {
                    stageMap.put("customTag", stage.getImageTag());
                } else if ("1".equals(stage.getImageTagType())) {
                    stageMap.put("baseTag", stage.getImageBaseTag());
                    stageMap.put("increaseTag", stage.getImageIncreaseTag());
                }
            } else if (StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()) {
                stageMap.put("image", stage.getImageName());
                stageMap.put("namespace", stage.getNamespace());
                stageMap.put("service", stage.getServiceName());
                stageMap.put("container", stage.getContainerName());
            }
            if (StageTemplateTypeEnum.CUSTOM.getCode() == stage.getStageTemplateType()) {
                if (stage.getBuildEnvironmentId() != null && stage.getBuildEnvironmentId() != 0) {
                    stageMap.put("buildEnvironment", buildEnvMap.get(stage.getBuildEnvironmentId()));
                }
                if (CollectionUtils.isNotEmpty(stageDto.getEnvironmentVariables())) {
                    stageMap.put("environmentVariables", stageDto.getEnvironmentVariables());
                }
            }

            if (CollectionUtils.isNotEmpty(stageDto.getCommand())) {
                stageMap.put("command", stageDto.getCommand());
            }
            stages.add(stageMap);
        }
        jobMap.put("stages", stages);
        String body = yaml.dumpAsMap(jobMap);
        return body.replaceAll("password: .*\\n", "password: ******\\\n");
    }


    @Override
    public String getJobLog(Integer id, Integer buildNum) throws Exception {
        JobBuild jobBuild = new JobBuild();
        jobBuild.setJobId(id);
        jobBuild.setBuildNum(buildNum);
        String log = jobBuildService.queryLogByObject(jobBuild);
        if (StringUtils.isBlank(log)) {
            List<JobBuild> buildList = jobBuildService.queryByObject(jobBuild);
            if (buildList.size() == 1) {
                jobBuild = buildList.get(0);
                if (!Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus()) && !Constant.PIPELINE_STATUS_NOTBUILT.equals(jobBuild.getStatus())) {
                    JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
                    Job job = getJobById(id);
                    String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
                    String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
                    FolderJob folderJob = getFolderJob(projectName, clusterName);
                    JobWithDetails jobWithDetails = jenkinsServer.getJob(folderJob, job.getName());
                    Build build = jobWithDetails.getBuildByNumber(buildNum);
                    BuildWithDetails buildWithDetails = build.details();
                    log = buildWithDetails.getConsoleOutputText();
                    jobBuild.setLog(log);
                    try {
                        jobBuildService.updateLogById(jobBuild);
                    } catch (Exception e) {
                        logger.error("流水线日志保存失败，id:{}, buildNum:{}", job.getId(), buildNum);
                    }
                }
            }
        }
        return log;
    }

    @Override
    public void getJobListWS(WebSocketSession session, String projectId, String clusterId) {
        try {
            String projectName = projectService.getProjectNameByProjectId(projectId);
            String clusterName = null;
            List<Cluster> clusterList = null;
            if (StringUtils.isNotBlank(clusterId)) {
                clusterName = clusterService.getClusterNameByClusterId(clusterId);
            } else {
                clusterList = roleLocalService.listCurrentUserRoleCluster();
            }
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
                List<Job> dbJobList = jobMapper.select(projectId, clusterId, null, null, null);

                if (StringUtils.isNotBlank(clusterName)) {
                    result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + clusterName + "/api/xml", null, params, false);
                    currentStatus = getCurrentJobsStatusInJenkins(result, clusterId, jobList, dbJobList, currentStatus);
                } else if (CollectionUtils.isNotEmpty(clusterList)) {
                    for (Cluster cluster : clusterList) {
                        result = HttpJenkinsClientUtil.httpGetRequest("/job/" + projectName + "/job/" + cluster.getName() + "/api/xml", null, params, false);
                        currentStatus = getCurrentJobsStatusInJenkins(result, cluster.getId(), jobList, dbJobList, currentStatus);
                    }
                }

                if (!currentStatus.toString().equals(lastStatus.toString())) {
                    session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobList))));
                }
                lastStatus = currentStatus;

                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    logger.error("获取流水线列表失败", e);
                }
            }
        } catch (Exception e) {
            logger.error("get job list error", e);
        } finally {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (IOException e) {
                logger.error("websocket close error", e);
            }
        }
    }

    private StringBuilder getCurrentJobsStatusInJenkins(ActionReturnUtil result, String clusterId, List jobList, List<Job> dbJobList, StringBuilder currentStatus) {
        if (result.isSuccess()) {
            String jenkinsJobName;
            //List jobList = new ArrayList<>();
            Map jobMap;
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            if (jenkinsDataMap.get("root") instanceof String) {
                return currentStatus;
            }
            Map rootMap = (Map) jenkinsDataMap.get("root");
            List<Map> jenkinsJobList = new ArrayList<>();
            if (rootMap.get("folder") instanceof String) {
                return currentStatus;
            }
            Map folderMap = (Map) rootMap.get("folder");
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
                    for (Job job : dbJobList) {
                        if (job.getClusterId().equals(clusterId) && job.getName().equals(jenkinsJobName)) {
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
    @Transactional(rollbackFor = Exception.class)
    public void destroyCicdPod(Cluster cluster) throws Exception {
        List jenkinsPodList = new ArrayList<>();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/computer/api/json", null, null, false);
        if (result.isSuccess()) {
            Map dataMap = JsonUtil.convertJsonToMap((String) result.get("data"));
            List<Map> computerList = new ArrayList<>();
            if (dataMap.get("computer") instanceof List) {
                computerList.addAll((List<Map>) dataMap.get("computer"));
            } else {
                computerList.add((Map) dataMap.get("computer"));
            }
            for (Map computer : computerList) {
                if (StringUtils.contains((String) computer.get("_class"), "kubernetes")) {
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
        if (cluster == null) {
            clusterList = clusterService.listCluster();
        } else {
            clusterList.add(cluster);
        }
        for (Cluster c : clusterList) {
            K8SClientResponse response = new K8sMachineClient().exec(k8SURL, HTTPMethod.GET, null, labels, c);
            if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
                PodList podList = JsonUtil.jsonToPojo(response.getBody(), PodList.class);
                for (Pod pod : podList.getItems()) {
                    if (!jenkinsPodList.contains(pod.getMetadata().getName()) && !"Running".equals(pod.getStatus())) {
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
        for (Stage stage : stageList) {
            if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType()) {
                Map map = new HashMap();
                map.put("stageId", stage.getId());
                map.put("imageName", stage.getHarborProject() + "/" + stage.getImageName());
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
        if (stage == null) {
            logger.error("流水线步骤不存在{}", stageId);
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_NOT_EXIST);
        }
        if (StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()) {
            deploy(stageId, buildNum);
        } else if (StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType()) {
            scanCodeBySuite(stage, buildNum);
        } else if (StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()) {
            testBySuite(stage, buildNum);
        } else if (StageTemplateTypeEnum.IMAGEPUSH.getCode() == stage.getStageTemplateType()) {
            imagePush(stage);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByClusterId(String clusterId) {
        List<Job> jobs = jobMapper.select(null, clusterId, null, null, null);
        if (CollectionUtils.isEmpty(jobs)) {
            return 0;
        }
        int deleteCount = jobMapper.deleteByClusterId(clusterId);
        for (Job job : jobs) {
            stageMapper.deleteStageByJob(job.getId());
        }
        return deleteCount;
    }


    private void scanCodeBySuite(Stage stage, Integer buildNum) throws Exception {
        integrationTestService.executeTestSuite(stage.getSuiteId(), stage.getId(), buildNum);
    }

    private void testBySuite(Stage stage, Integer buildNum) throws Exception {
        boolean serviceStarted;
        Stage stageExample = new Stage();
        stageExample.setJobId(stage.getJobId());
        List<Stage> stageList = stageService.selectByExample(stageExample);
        for (Stage jobStage : stageList) {
            int newInstance = 0;
            if (StageTemplateTypeEnum.DEPLOY.getCode() == jobStage.getStageTemplateType() && jobStage.getStageOrder() < stage.getStageOrder()) {
                int repeat = CommonConstant.NUM_TEN;
                String namespace = jobStage.getNamespace();
                String serviceName = jobStage.getServiceName();
                while (repeat > 0) {
                    Thread.sleep(Constant.THREAD_SLEEP_TIME_10000);
                    serviceStarted = true;
                    repeat--;
                    ActionReturnUtil result = deploymentsService.getDeploymentDetail(namespace, serviceName,false);
                    if (result.isSuccess()) {
                        //获取服务状态，并判断
                        AppDetail appDetail = (AppDetail) result.get("data");
                        if (!Constant.SERVICE_START.equals(appDetail.getStatus())) {
                            continue;
                        }
                        //获取新版本的更新实例数
                        if (CommonConstant.FRESH_RELEASE.equals(jobStage.getDeployType())) {
                            newInstance = appDetail.getInstance();
                        } else if (CommonConstant.CANARY_RELEASE.equals(jobStage.getDeployType())) {
                            newInstance = jobStage.getInstances();
                            if (jobStage.getInstances() > repeat) {
                                repeat = jobStage.getInstances();
                            }
                        }
                    } else {
                        continue;
                    }
                    result = deploymentsService.podList(serviceName, namespace);
                    if (result.isSuccess()) {
                        List<PodDetail> podList = (List<PodDetail>) result.getData();
                        String tag1 = null;
                        String tag2 = null;
                        int count1 = 0;
                        int count2 = 0;
                        //获取新旧版本实例数
                        for (PodDetail pod : podList) {
                            if (tag1 == null || tag1.equals(pod.getTag())) {
                                tag1 = pod.getTag();
                                count1++;
                            } else if (tag2 == null || tag2.equals(pod.getTag())) {
                                tag2 = pod.getTag();
                                count2++;
                            } else {
                                serviceStarted = false;
                            }
                        }
                        if (tag1 != null) {
                            int tag = Integer.valueOf(tag1.replace("v", ""));
                            if (tag2 != null) {
                                //当有两个版本时，比较两个版本中新版本的实例数是否达到更新实例数
                                if (tag > Integer.valueOf(tag2.replace("v", ""))) {
                                    if (count1 != newInstance) {
                                        continue;
                                    }
                                } else {
                                    if (count2 != newInstance) {
                                        continue;
                                    }
                                }
                            } else {
                                //只有新版本时，确认实例数是否达到
                                if (count1 != newInstance) {
                                    continue;
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                    if (serviceStarted) {
                        break;
                    }
                }
            }
        }
        integrationTestService.executeTestSuite(stage.getSuiteId(), stage.getId(), buildNum);
    }

    /**
     * 获取jenkins目录
     *
     * @param folderName 目录名称
     * @return
     * @throws Exception
     */
    private FolderJob getFolderJob(String... folderName) throws Exception {
        FolderJob folderJob = null;
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        if (folderName.length > 0) {
            for (int i = 0; i < folderName.length; i++) {
                com.offbytwo.jenkins.model.Job job = jenkinsServer.getJob(folderJob, folderName[i]);
                if (job == null) {
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
     *
     * @param jobName     流水线名
     * @param projectName 项目名
     * @param clusterName 集群名
     */
    private void validateJobName(String jobName, String projectName, String clusterName) {
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

    private List<Map> getJobsByJenkinsResult(ActionReturnUtil result, String jobName, String projectId, String clusterId, String clusterName, String type) throws Exception {
        List<Map> jobList = new ArrayList<>();
        if (result.isSuccess()) {
            String jenkinsJobName;
            Map jobMap;
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            List<Map> jenkinsJobList = new ArrayList<>();
            List<Map> jenkinsBuildList;

            if (jenkinsDataMap.get("folder") instanceof String) {
                return Collections.emptyList();
            }
            Map folderMap = (Map) jenkinsDataMap.get("folder");
            if (folderMap.get("job") instanceof Map) {
                jenkinsJobList.add((Map) folderMap.get("job"));
            } else if (folderMap.get("job") instanceof List) {
                jenkinsJobList.addAll((List) folderMap.get("job"));
            }
            for (Map jenkinsJob : jenkinsJobList) {
                jobMap = new HashMap();
                jenkinsJobName = (String) jenkinsJob.get("name");
                if (jenkinsJobName != null) {

                    if (StringUtils.isNotBlank(jobName) && !jenkinsJobName.contains(jobName)) {
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
                    if (lastBuildMap.get("number") != null) {
                        jobMap.put("lastBuildNumber", lastBuildMap.get("number"));
                    }
                } else {
                    jobMap.put("lastBuildStatus", "NOTBUILT");
                    jobMap.put("lastBuildTime", "");
                }

                jenkinsBuildList = new ArrayList<>();
                int successNum = 0;
                int failNum = 0;
                if (jenkinsJob.get("build") instanceof Map) {
                    jenkinsBuildList.add((Map) jenkinsJob.get("build"));
                } else if (jenkinsJob.get("build") instanceof List) {
                    jenkinsBuildList.addAll((List) jenkinsJob.get("build"));
                }
                for (Object jenkinsBuild : jenkinsBuildList) {
                    if (jenkinsBuild instanceof String) {
                        continue;
                    } else {
                        Map jenkinsBuildMap = (Map) jenkinsBuild;
                        if (jenkinsBuildMap.get("result") != null) {
                            if ("SUCCESS".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
                                successNum++;
                            } else if ("Failure".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
                                failNum++;
                            }
                        }
                    }
                }
                jobMap.put("successNum", successNum);
                jobMap.put("failNum", failNum);
                jobMap.put("clusterName", clusterName);
                List<Job> dbJobList = jobMapper.select(projectId, clusterId, (String) jobMap.get("name"), null, null);
                if (CollectionUtils.isNotEmpty(dbJobList)) {
                    for (Job dbJob : dbJobList) {
                        if (StringUtils.isNotBlank(dbJob.getName()) && dbJob.getName().equals(jobMap.get("name"))) {
                            if (StringUtils.isNotBlank(type) && !type.equals(dbJob.getType())) {
                                continue;
                            }
                            jobMap.put("id", dbJob.getId());
                            jobMap.put("clusterId", dbJob.getClusterId());
                            jobMap.put("type", dbJob.getType());
                            break;
                        }
                    }
                    if (jobMap.get("id") == null) {
                        continue;
                    }
                } else {
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


    private boolean deleteCredentials(String jenkinsJobName) {
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + jenkinsJobName + "/doDelete", null, null, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }


    public JobBuild syncJobStatus(Job job, Integer buildNum) throws Exception {
        Project project = projectService.getProjectByProjectId(job.getProjectId());
        if (null == project) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        String projectName = project.getProjectName();
        Cluster cluster = clusterService.findClusterById(job.getClusterId());
        if (null == cluster) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        String clusterName = cluster.getName();

        JobBuild jobBuildCondition = new JobBuild();
        jobBuildCondition.setJobId(job.getId());
        jobBuildCondition.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);
        JobBuild jobBuild = new JobBuild();
        if (null != jobBuildList && jobBuildList.size() == 1) {
            jobBuild = jobBuildList.get(0);
        } else if (null == jobBuildList || jobBuildList.size() == 0) {
            jobBuild.setJobId(job.getId());
            jobBuild.setBuildNum(buildNum);
            jobBuildMapper.insert(jobBuild);
        }
        //调用Jenkins接口，获取流水线构建信息
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "building,timestamp,result,duration,number");
        JenkinsUrl xmlUrl = new JenkinsUrl();
        xmlUrl.setFolders(projectName, clusterName);
        xmlUrl.setName(job.getName());
        xmlUrl.setBuildNumber(String.valueOf(buildNum));
        xmlUrl.setApi("api/xml");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest(xmlUrl.getUrl(), null, params, false);
        //获取Jenkins中流水线构建日志
        JenkinsUrl logUrl = new JenkinsUrl();
        logUrl.setFolders(projectName, clusterName);
        logUrl.setName(job.getName());
        logUrl.setBuildNumber(String.valueOf(buildNum));
        logUrl.setApi("consoleText");
        ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest(logUrl.getUrl(), null, null, false);
        if (result.isSuccess()) {
            //解析构建信息
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map build = new HashMap();
            Map rootMap = (Map) jenkinsDataMap.get("workflowRun");
            if (rootMap != null) {
                if ("false".equalsIgnoreCase((String) rootMap.get("building"))) {
                    jobBuild.setStatus((String) rootMap.get("result"));
                } else {
                    jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
                }
                if (rootMap.get("timestamp") != null) {
                    jobBuild.setStartTime(new Timestamp(Long.valueOf((String) rootMap.get("timestamp"))));
                }
                jobBuild.setDuration((String) rootMap.get("duration"));
            }
            String status = "";
            //若步骤中含有静态扫描和集成测试步骤，则根据数据库中该类步骤状态来确定流水线状态
            List<Stage> stageList = stageService.getStageByJobId(job.getId());
            for (Stage stage : stageList) {
                if (StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()) {
                    StageBuild condition = new StageBuild();
                    condition.setJobId(job.getId());
                    condition.setStageId(stage.getId());
                    condition.setBuildNum(buildNum);
                    List<StageBuild> stageBuildList = stageBuildService.selectStageBuildByObject(condition);
                    if (CollectionUtils.isNotEmpty(stageBuildList)) {
                        StageBuild stageBuild = stageBuildList.get(0);
                        if (Constant.PIPELINE_STATUS_FAILED.equals(stageBuild.getStatus()) && StringUtils.isBlank(status)) {
                            status = Constant.PIPELINE_STATUS_FAILURE;
                        } else if (Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus())) {
                            status = Constant.PIPELINE_STATUS_BUILDING;
                        }
                    }
                }
            }

            if (StringUtils.isNotBlank(status) && !Constant.PIPELINE_STATUS_ABORTED.equals(jobBuild.getStatus()) && !Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus())) {
                jobBuild.setStatus(status);
            }
            String preStatus = null;
            jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);

            if (null != jobBuildList && jobBuildList.size() == 1) {
                preStatus = jobBuildList.get(0).getStatus();
            }
            if (Constant.PIPELINE_STATUS_ABORTED.equals(jobBuild.getStatus()) && Constant.PIPELINE_STATUS_BUILDING.equals(status) ||
                    ((Constant.PIPELINE_STATUS_BUILDING.equals(preStatus) || StringUtils.isBlank(preStatus)) && !Constant.PIPELINE_STATUS_BUILDING.equals(status))) {
                allStageStatusSync(job, buildNum);
            }
            jobBuildService.update(jobBuild);
            if((Constant.PIPELINE_STATUS_BUILDING.equals(preStatus) || StringUtils.isBlank(preStatus))&& (Constant.PIPELINE_STATUS_FAILURE.equals(jobBuild.getStatus()) || Constant.PIPELINE_STATUS_SUCCESS.equals(jobBuild.getStatus()))){
                try {
                    sendNotification(job, buildNum);
                }catch(Exception e){
                    logger.error("流水线邮件通知发送失败,jobId: {}, {}", job.getId(), e.getMessage());
                }
            }
            if (logResult.isSuccess()) {
                jobBuild.setLog((String) logResult.get("data"));
            } else {
                logger.error("获取流水线日志失败", logResult.getData());
                throw new MarsRuntimeException(ErrorCodeMessage.JENKINS_PIPELINE_INFO_GET_ERROR);
            }
            jobBuildService.updateLogById(jobBuild);

        } else {
            logger.error("获取流水线信息失败", result.getData());
            throw new MarsRuntimeException(ErrorCodeMessage.JENKINS_PIPELINE_INFO_GET_ERROR);
        }
        return jobBuild;

    }

    private void stageStatusSync(Integer id, Integer buildNum) throws Exception {
        Stage stage = stageMapper.queryById(id);
        Job job = jobMapper.queryById(stage.getJobId());
        List<Map> stageMapList = stageService.getStageBuildFromJenkins(job, buildNum);
        int i = stage.getStageOrder() - 2;
        //同步上一个步骤状态与当前步骤状态
        for (i = i < 0 ? 0 : i; i < stageMapList.size(); i++) {
            try {
                stageService.stageBuildSync(job, buildNum, stageMapList.get(i), i + 1);
            } catch (Exception e) {
                logger.error("步骤同步失败, stageOrder:{}, buildNum:{}", i + 1, buildNum);
                throw new MarsRuntimeException(ErrorCodeMessage.SYNC_STAGE_ERROR);
            }
        }
    }


    private void allStageStatusSync(Job job, Integer buildNum) throws Exception {
        List<Map> stageMapList = stageService.getStageBuildFromJenkins(job, buildNum);
        int i = 0;
        for (Map stageMap : stageMapList) {
            try {
                stageService.stageBuildSync(job, buildNum, stageMap, ++i);
            } catch (Exception e) {
                logger.error("步骤同步失败, stageOrder:{}, buildNum:{}", i + 1, buildNum);
                throw new MarsRuntimeException(ErrorCodeMessage.SYNC_STAGE_ERROR);
            }
        }
    }


    private void sendNotification(Job job, Integer buildNum) throws Exception {
        if (job.isNotification()) {
            JobBuild jobBuildContidion = new JobBuild();
            jobBuildContidion.setJobId(job.getId());
            jobBuildContidion.setBuildNum(buildNum);
            List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildContidion);
            JobBuild jobBuild;
            if (jobBuildList != null && jobBuildList.size() == 1) {
                jobBuild = jobBuildList.get(0);
            } else {
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
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setTo((String[]) mailList.toArray(new String[mailList.size()]));
                    helper.setSubject(MimeUtility.encodeText("【容器云平台】CICD Notification_" + job.getName() + "_" + jobBuild.getBuildNum(), MimeUtility.mimeCharset("gb2312"), null));
                    Map dataModel = new HashMap<>();
                    List<String> statusList = new ArrayList<>();
                    dataModel.put("url", webUrl + "/#/cicd/process/" + job.getId());
                    dataModel.put("jobName", job.getName());
                    TenantBinding tenant = tenantService.getTenantByTenantid(job.getTenantId());
                    dataModel.put("tenantName", tenant.getAliasName());
                    Project project = projectService.getProjectByProjectId(job.getProjectId());
                    dataModel.put("projectName", project.getAliasName());
                    dataModel.put("status", jobBuild.getStatus());
                    statusList.add(jobBuild.getStatus());
                    dataModel.put("time", new Date());
                    dataModel.put("startTime", jobBuild.getStartTime());
                    StageBuild stageBuildCondition = new StageBuild();
                    stageBuildCondition.setJobId(job.getId());
                    stageBuildCondition.setBuildNum(buildNum);
                    List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
                    List stageBuildMapList = new ArrayList<>();
                    int jobDuration = 0;
                    for (StageBuild stageBuild : stageBuildList) {
                        Map stageBuildMap = new HashMap<>();
                        Stage stage = stageMapper.queryById(stageBuild.getStageId());
                        stageBuildMap.put("name", stage.getStageName());
                        stageBuildMap.put("status", stageBuild.getStatus());
                        statusList.add(stageBuild.getStatus());
                        stageBuildMap.put("startTime", stageBuild.getStartTime());
                        int duration = 0;
                        if (StringUtils.isNotBlank(stageBuild.getDuration())) {
                            duration = (int) Math.ceil(Long.parseLong(stageBuild.getDuration()) / 1000.0) * CommonConstant.NUM_THOUSAND;
                        }
                        jobDuration += duration;
                        stageBuildMap.put("duration", DateUtil.getDuration(Long.valueOf(duration)));
                        stageBuildMapList.add(stageBuildMap);
                    }
                    dataModel.put("duration", DateUtil.getDuration(Long.valueOf(jobDuration)));
                    dataModel.put("stageBuildList", stageBuildMapList);
                    helper.setText(TemplateUtil.generate("notification.ftl", dataModel), true);
                    ClassLoader classLoader = MailUtil.class.getClassLoader();
                    InputStream inputStream = classLoader.getResourceAsStream("alarm-icon.png");
                    byte[] bytes = MailUtil.stream2byte(inputStream);
                    helper.addInline("icon-info", new ByteArrayResource(bytes), "image/png");
                    if(statusList.contains(Constant.PIPELINE_STATUS_SUCCESS)) {
                        inputStream = classLoader.getResourceAsStream("icon-status-success.png");
                        bytes = MailUtil.stream2byte(inputStream);
                        helper.addInline("icon-status-success", new ByteArrayResource(bytes), "image/png");
                    }
                    if(statusList.contains(Constant.PIPELINE_STATUS_FAILED) || statusList.contains(Constant.PIPELINE_STATUS_FAILURE)) {
                        inputStream = classLoader.getResourceAsStream("icon-status-fail.png");
                        bytes = MailUtil.stream2byte(inputStream);
                        helper.addInline("icon-status-fail", new ByteArrayResource(bytes), "image/png");
                    }
                    if(statusList.contains(Constant.PIPELINE_STATUS_NOTBUILT)) {
                        inputStream = classLoader.getResourceAsStream("icon-status-unfinished.png");
                        bytes = MailUtil.stream2byte(inputStream);
                        helper.addInline("icon-status-unfinished", new ByteArrayResource(bytes), "image/png");
                    }
                    MailUtil.sendMimeMessage(mimeMessage);
                } catch (Exception e) {
                    logger.error("发送邮件失败", e);
                    throw new Exception("发送邮件失败");
                }
            }
        }
    }

    private String generateTag(Stage stage) {
        String tag = "";
        if (!StringUtils.isBlank(stage.getImageBaseTag()) && !StringUtils.isBlank(stage.getImageIncreaseTag())) {
            String[] baseArray = stage.getImageBaseTag().split("\\.");
            String[] increaceArray = stage.getImageIncreaseTag().split("\\.");

            if (baseArray.length > 0 && increaceArray.length > 0) {
                String suffix = "";
                int start = 0;
                for (int i = baseArray[0].length() - 1; i >= 0; i--) {
                    if (Character.isDigit(baseArray[0].charAt(i))) {
                        continue;
                    }
                    start = i + 1;
                    suffix = baseArray[0].substring(0, start);
                    break;
                }
                for (int i = 0; i < baseArray.length; i++) {
                    if (i == 0) {
                        baseArray[i] = baseArray[i].substring(start);
                    }
                    tag = tag + String.valueOf(Integer.valueOf(baseArray[i]) + Integer.valueOf(increaceArray[i])) + ".";
                }
                tag = (suffix + tag).substring(0, (suffix + tag).length() - 1);

            }
        }
        return tag;
    }

    private String convertStatus(String status) {
        if (Constant.PIPELINE_STATUS_INPROGRESS.equals(status) || Constant.PIPELINE_STATUS_NOTEXECUTED.equals(status)) {
            return Constant.PIPELINE_STATUS_BUILDING;
        } else {
            return status;
        }
    }

    private FolderJob checkFolderJobExist(String projectName, String clusterName) throws Exception {
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        com.offbytwo.jenkins.model.Job projectFolder = jenkinsServer.getJob(projectName);
        if (projectFolder == null) {
            jenkinsServer.createFolder(projectName);
            projectFolder = jenkinsServer.getJob(projectName);
        }
        FolderJob folderJob = new FolderJob(projectFolder.getName(), projectFolder.getUrl());
        com.offbytwo.jenkins.model.Job clusterFolder = jenkinsServer.getJob(folderJob, clusterName);
        if (clusterFolder == null) {
            jenkinsServer.createFolder(folderJob, clusterName);
            clusterFolder = jenkinsServer.getJob(folderJob, clusterName);
        }
        folderJob = new FolderJob(clusterFolder.getName(), clusterFolder.getUrl());
        return folderJob;
    }

    private void doFreshRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception {
//        session.setAttribute("tenantId", job.getTenantId());
        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setStageId(stageDto.getId());
        stageBuildCondition.setBuildNum(buildNum);
        List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
        if (CollectionUtils.isEmpty(stageBuildList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_BUILD_NOT_EXIST);
        }
        StageBuild stageBuild = stageBuildList.get(0);
        if (StringUtils.isBlank(stageBuild.getImage()) || stageBuild.getImage().split(":").length != 2) {
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
            for (Deployment deployment : deployments) {
                if (stageDto.getServiceName().equals(deployment.getMetadata().getName())) {
                    Integer instances = deployment.getSpec().getReplicas();
                    stageDto.setInstances(instances);
                    List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(deployment);
                    if (CollectionUtils.isNotEmpty(containerList)) {
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
        if (CollectionUtils.isNotEmpty(stageDto.getConfigMaps())) {
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
        if (CollectionUtils.isNotEmpty(jobBuildList)) {
            user = jobBuildList.get(0).getStartUser();
        }
        if (StringUtils.isEmpty(user)) {
            user = job.getCreateUser();
        }
        res = serviceService.deployService(serviceDeploy, user);
        if (!res.isSuccess()) {
            logger.error("发布失败", res);
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_DEPLOY_ERROR);
        }
    }

    private void doCanaryRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception {
        verifyUpgrade(stageDto.getServiceName(), stageDto.getNamespace(), stageDto.getInstances(), false);
        verifyUpgradeResource(stageDto, cluster);
        K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stageDto.getNamespace(), stageDto.getServiceName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            throw new MarsRuntimeException(ErrorCodeMessage.DEPLOYMENT_GET_FAILURE);
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<UpdateContainer> updateContainerList = getUpdateContainerList(dep, job, stageDto, cluster, buildNum);

        //设置灰度升级的参数
        CanaryDeployment canaryDeployment = new CanaryDeployment();
        canaryDeployment.setProjectId(job.getProjectId());
        canaryDeployment.setName(stageDto.getServiceName());
        canaryDeployment.setContainers(updateContainerList);
        if (stageDto.getMaxSurge() != null) {
            canaryDeployment.setMaxSurge(stageDto.getMaxSurge());
        } else {
            canaryDeployment.setMaxSurge(0);
        }
        if (stageDto.getMaxUnavailable() != null) {
            canaryDeployment.setMaxUnavailable(stageDto.getMaxUnavailable());
        } else {
            canaryDeployment.setMaxUnavailable(1);
        }
        canaryDeployment.setInstances(stageDto.getInstances());
        canaryDeployment.setNamespace(stageDto.getNamespace());
        canaryDeployment.setSeconds(5);
        //服务标签
        Map<String, Object> labels = dep.getSpec().getTemplate().getMetadata().getLabels();
        if(labels != null && labels.get(Constant.TYPE_DEPLOY_VERSION) != null) {
            canaryDeployment.setDeployVersion((String)labels.get(Constant.TYPE_DEPLOY_VERSION));
        }

        versionControlService.canaryUpdate(canaryDeployment, canaryDeployment.getInstances(), null);
    }

    private void doBlueGreenRelease(Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception {
        verifyUpgrade(stageDto.getServiceName(), stageDto.getNamespace(), stageDto.getInstances(), false);
        verifyUpgradeResource(stageDto, cluster);
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
        //服务标签
        Map<String, Object> labels = dep.getSpec().getTemplate().getMetadata().getLabels();
        if(labels != null && labels.get(Constant.TYPE_DEPLOY_VERSION) != null) {
            updateDeployment.setDeployVersion((String)labels.get(Constant.TYPE_DEPLOY_VERSION));
        }

        blueGreenDeployService.deployByBlueGreen(updateDeployment, null, job.getProjectId());
    }

    private List<UpdateContainer> getUpdateContainerList(Deployment dep, Job job, StageDto stageDto, Cluster cluster, Integer buildNum) throws Exception {

        List<ContainerOfPodDetail> containerList = K8sResultConvert.convertDeploymentContainer(dep, dep.getSpec().getTemplate().getSpec().getContainers(), cluster);
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
            if(containerOfPodDetail.getResource().get(CommonConstant.GPU) != null) {
                createResourceDto.setGpu((String)containerOfPodDetail.getResource().get(CommonConstant.GPU));
            }
            updateContainer.setResource(createResourceDto);
            CreateResourceDto limit = new CreateResourceDto();
            limit.setCpu((String) containerOfPodDetail.getLimit().get("cpu"));
            limit.setMemory((String) containerOfPodDetail.getLimit().get("memory"));
            if(containerOfPodDetail.getLimit().get(CommonConstant.GPU) != null) {
                limit.setGpu((String)containerOfPodDetail.getResource().get(CommonConstant.GPU));
            }
            updateContainer.setLimit(limit);
            List<CreateEnvDto> envList = new ArrayList<>();
            if (containerOfPodDetail.getEnv() != null) {
                updateContainer.setEnv(containerOfPodDetail.getEnv());
            }
            List<CreatePortDto> portList = new ArrayList<>();
            for (ContainerPort containerPort : containerOfPodDetail.getPorts()) {
                CreatePortDto createPortDto = new CreatePortDto();
                createPortDto.setName(containerPort.getName());
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
                    } else if (StringUtils.isNotBlank(volumeMountExt.getPvcname()) || "emptyDir".equals(volumeMountExt.getType()) || "hostPath".equals(volumeMountExt.getType())) {
                        PersistentVolumeDto updateVolume = new PersistentVolumeDto();
                        updateVolume.setType(volumeMountExt.getType());
                        updateVolume.setReadOnly(volumeMountExt.getReadOnly());
                        updateVolume.setPath(volumeMountExt.getMountPath());
                        updateVolume.setEmptyDir(volumeMountExt.getEmptyDir());
                        updateVolume.setCapacity(volumeMountExt.getCapacity());
                        updateVolume.setHostPath(volumeMountExt.getHostPath());
                        updateVolume.setRevision(volumeMountExt.getRevision());
                        updateVolume.setName(volumeMountExt.getName());
                        updateVolume.setVolumeName(volumeMountExt.getName());
                        if (StringUtils.isNotBlank(volumeMountExt.getPvcname())) {
                            updateVolume.setPvcName(volumeMountExt.getPvcname());
                        }
                        updateVolumeList.add(updateVolume);
                    } else if ("configMap".equals(volumeMountExt.getType()) && CollectionUtils.isEmpty(configMapList)) {
                        //非升级容器或cd中不替换配置文件时需要取原有的配置文件信息
                        if (!stageDto.getContainerName().equals(containerOfPodDetail.getName()) || CollectionUtils.isEmpty(stageDto.getConfigMaps())) {
                            String configMapId = null;
                            if (volumeMountExt.getName() != null && volumeMountExt.getName().lastIndexOf("-") > 0) {
                                int indexByFileName = volumeMountExt.getName().lastIndexOf("-");
                                configMapId = volumeMountExt.getName().substring(indexByFileName + 1);
                            }
                            //升级时从数据库读取配置文件的内容
                            ConfigDetailDto configDetailDto = configCenterService.getConfigMap(configMapId);
                            if (configDetailDto == null) {
                                throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_NOT_EXIST);
                            }

                            ConfigFile configFile = ObjConverter.convert(configDetailDto, ConfigFile.class);
                            if (configFile != null) {
                                List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
                                for (ConfigFileItem configFileItem : configFileItemList) {
                                    CreateConfigMapDto configMap = new CreateConfigMapDto();
                                    configMap.setTag(configFile.getTags());
                                    configMap.setFile(configFileItem.getFileName());
                                    configMap.setPath(configFileItem.getPath());
                                    configMap.setValue(configFileItem.getContent());
                                    configMap.setConfigMapId(configMapId);
                                    configMapList.add(configMap);
                                }
                            }
                        }
                    }
                }
            }
            updateContainer.setStorage(updateVolumeList);
            updateContainer.setConfigmap(configMapList);

            if (updateContainer.getLog() == null) {
                updateContainer.setLog(new LogVolume());
            }
            //当遍历到需要升级的容器时，更新镜像
            if (stageDto.getContainerName().equals(containerOfPodDetail.getName())) {
                updateContainer.setImg(stageBuild.getImage());
                //若配置不为空，则查询数据库中的最新配置信息，更新到需要升级的容器配置中
                if (CollectionUtils.isNotEmpty(stageDto.getConfigMaps())) {
                    configMapList = new ArrayList<>();
                    String configMapId = stageDto.getConfigMaps().get(0).getConfigMapId();
                    ConfigDetailDto configDetailDto = configCenterService.getConfigMap(configMapId);
                    if(configDetailDto==null){
                        throw new MarsRuntimeException(ErrorCodeMessage.CONFIGMAP_NOT_EXIST);
                    }
                    ConfigFile configFile = ObjConverter.convert(configDetailDto, ConfigFile.class);
                    if (configFile != null) {
                        List<ConfigFileItem> configFileItemList = configFile.getConfigFileItemList();
                        for (ConfigFileItem configFileItem : configFileItemList) {
                            CreateConfigMapDto createConfigMapDto = new CreateConfigMapDto();
                            createConfigMapDto.setTag(configFile.getTags());
                            createConfigMapDto.setPath(configFileItem.getPath());
                            createConfigMapDto.setFile(configFileItem.getFileName());
                            createConfigMapDto.setValue(configFileItem.getContent());
                            createConfigMapDto.setConfigMapId(configMapId);
                            configMapList.add(createConfigMapDto);
                        }
                    }
                    updateContainer.setConfigmap(configMapList);
                }
            } else {
                String[] imageArray = containerOfPodDetail.getImg().split(CommonConstant.SLASH);
                if(imageArray.length > CommonConstant.NUM_TWO){
                    imageArray = ArrayUtils.subarray(imageArray, imageArray.length - CommonConstant.NUM_TWO, imageArray.length);
                }
                updateContainer.setImg(StringUtils.join(imageArray, CommonConstant.SLASH));
            }

            updateContainer.setImagePullPolicy(containerOfPodDetail.getImagePullPolicy());

            updateContainerList.add(updateContainer);
        }
        return updateContainerList;
    }

    private String getProjectNameByProjectId(String projectId) throws Exception {
        Project project = projectService.getProjectByProjectId(projectId);
        if (null == project) {
            throw new MarsRuntimeException(ErrorCodeMessage.PROJECT_NOT_EXIST);
        }
        return project.getProjectName();
    }

    private String getClusterNameByClusterId(String clusterId) throws Exception {
        Cluster cluster = clusterService.findClusterById(clusterId);
        if (null == cluster) {
            throw new MarsRuntimeException(ErrorCodeMessage.CLUSTER_NOT_FOUND);
        }
        return cluster.getName();
    }

    private void validateJob(Job job, List<Stage> stageList, boolean validateDeploy) throws Exception {
        if (CollectionUtils.isEmpty(stageList)) {
            throw new MarsRuntimeException(ErrorCodeMessage.STAGE_EMPTY);
        }
        for (Stage stage : stageList) {
            boolean valid = false;
            if (StageTemplateTypeEnum.CODESCAN.getCode() == stage.getStageTemplateType() || StageTemplateTypeEnum.INTEGRATIONTEST.getCode() == stage.getStageTemplateType()) {
                List<Map> suiteList = integrationTestService.getTestSuites(job.getProjectId(), job.getType());
                for (Map map : suiteList) {
                    if (stage.getSuiteId().equals(map.get("suiteId"))) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    throw new MarsRuntimeException(ErrorCodeMessage.TEST_SUITE_NOT_EXIST);
                }
            } else if (validateDeploy && StageTemplateTypeEnum.DEPLOY.getCode() == stage.getStageTemplateType()) {
                verifyUpgrade(stage.getServiceName(), stage.getNamespace(), stage.getInstances(), true);
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
        for (StageBuild stageBuild : stageBuildList) {
            Map<String, Object> stageBuildMap = new HashMap<String, Object>();
            if (!Constant.PIPELINE_STATUS_BUILDING.equals(status) && (Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus()) || Constant.PIPELINE_STATUS_BUILDING.equals(stageBuild.getStatus()))) {
                allStageStatusSync(job, buildNum);
                stageBuildMapper.updateWaitingStage(job.getId(), buildNum);
                stageBuildList = stageBuildService.selectStageBuildByObject(stageBuildCondition);
                stageBuildMapList = new ArrayList<>();
                for (StageBuild newStageBuild : stageBuildList) {
                    stageBuildMap = new HashMap<>();
                    stageBuildMap.put("stageId", newStageBuild.getStageId());
                    stageBuildMap.put("stageName", newStageBuild.getStageName());
                    stageBuildMap.put("stageOrder", newStageBuild.getStageOrder());
                    stageBuildMap.put("stageType", MessageUtil.getMessage(newStageBuild.getStageType()));
                    stageBuildMap.put("stageTemplateTypeId", newStageBuild.getStageTemplateTypeId());
                    if (Constant.PIPELINE_STATUS_WAITING.equals(newStageBuild.getStatus())) {
                        stageBuildMap.put("buildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
                    } else {
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
            stageBuildMap.put("stageType", MessageUtil.getMessage(stageBuild.getStageType()));
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
        if (!result.isSuccess()) {
            logger.error("更新jenkins配置失败, id:{}", id, result.getData());
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_CONFIG_UPDATE_ERROR_IN_JENKINS);
        }
        return result;
    }

    @Override
    public void deletePipelineByProject(String projectId) throws Exception {
        List<Job> jobList = jobMapper.select(projectId, null, null, null, null);
        for (Job job : jobList) {
            jobMapper.deleteJobById(job.getId());
            stageMapper.deleteStageByJob(job.getId());
            triggerService.deleteByJobId(job.getId());
            parameterService.deleteByJobId(job.getId());
            jobBuildService.deleteByJobId(job.getId());
            stageBuildService.deleteByJobId(job.getId());
        }
        String projectName = projectService.getProjectNameByProjectId(projectId);
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        if (jenkinsServer.getJob(projectName) != null) {
            jenkinsServer.deleteJob(projectName);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rename(Integer jobId, String newName) throws Exception {
        Job job = getJobById(jobId);
        if (job.getName().equals(newName)) {
            return;
        }
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        validateJobName(newName, projectName, clusterName);

        jobMapper.updateJobName(job.getId(), newName);
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        jenkinsServer.renameJob(folderJob, job.getName(), newName);
        if (jenkinsServer.getJob(folderJob, newName) == null) {
            throw new MarsRuntimeException(ErrorCodeMessage.PIPELINE_RENAME_ERROR);
        }
    }

    @Override
    public void updateJob(JobDto jobDto) {
        Job job = jobDto.convertToBean();
        job.setUpdateTime(DateUtil.getCurrentUtcTime());
        job.setUpdateUser((String) session.getAttribute(CommonConstant.USERNAME));
        jobMapper.updateJob(job);
    }

    @Override
    public void deleteBuildResult() throws Exception {
        CicdConfigDto cicdConfigDto = systemConfigService.getCicdConfig();
        if (cicdConfigDto.getRemainNumber() != null) {
            List<Job> jobList = jobMapper.select(null, null, null, null, null);
            for (Job job : jobList) {
                try {
                    Integer lastBuildNum = job.getLastBuildNum();
                    if (lastBuildNum != null) {
                        int deleteLastBuildNum = lastBuildNum - cicdConfigDto.getRemainNumber();
                        JobBuild firstJobBuild = jobBuildService.queryFirstBuildById(job.getId());
                        int deleteFirstBuildNum = 1;
                        if (firstJobBuild != null) {
                            deleteFirstBuildNum = firstJobBuild.getBuildNum();
                        }
                        if (deleteLastBuildNum > 0 && firstJobBuild != null && deleteFirstBuildNum <= deleteLastBuildNum) {
                            String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
                            String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
                            String name = job.getName();
                            List buildNumList = new ArrayList();
                            //遍历每个构建，发送jenkins请求进行删除
                            for (int buildNum = deleteFirstBuildNum; buildNum <= deleteLastBuildNum; buildNum++) {
                                JenkinsUrl url = new JenkinsUrl();
                                url.setFolders(projectName, clusterName);
                                url.setName(name);
                                url.setBuildNumber(String.valueOf(buildNum));
                                url.setApi("doDelete");
                                ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest(url.getUrl(), null, null, null, HttpStatus.SC_MOVED_TEMPORARILY);
                                if (result.isSuccess()) {
                                    //构建中，无法删除
                                    if (StringUtils.isNotBlank((String) result.get("data")) && result.get("data").toString().contains("Resource busy")) {
                                        logger.error("删除构建记录失败,jobId:{},buildNum:{}", job.getId(), buildNum);
                                    } else {
                                        buildNumList.add(buildNum);
                                    }
                                } else {
                                    //已删除，接口不可以
                                    if (StringUtils.isNotBlank((String) result.get("message")) && result.get("message").toString().contains(String.valueOf(HttpStatus.SC_NOT_FOUND))) {
                                        buildNumList.add(buildNum);
                                    } else {
                                        logger.error("删除构建记录失败,jobId:{},buildNum:{}", job.getId(), buildNum);
                                    }
                                }
                            }
                            //删除数据库中的构建记录
                            if (CollectionUtils.isNotEmpty(buildNumList)) {
                                jobBuildService.deleteByJobIdAndBuildNum(job.getId(), buildNumList);
                                stageBuildService.deleteByJobIdAndBuildNum(job.getId(), buildNumList);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("删除流水线构建记录失败, jobId: {}", job.getId());
                }
            }
        }
    }

    private String generateJobBody(Job job) throws Exception {
        Map dataModel = new HashMap<>();
        List<Stage> stageList = stageMapper.queryByJobId(job.getId());
        Trigger trigger = triggerService.getTrigger(job.getId());
        ParameterDto parameterDto = parameterService.getParameter(job.getId());
        dataModel.put("job", job);
        dataModel.put("trigger", trigger);
        if (trigger != null && CommonConstant.JOBTRIGGER == trigger.getType() && trigger.getTriggerJobId() != null) {
            Job triggerJob = getJobById(trigger.getTriggerJobId());
            if (triggerJob != null) {
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
        dataModel.put("harborAddress", clusterService.findClusterById(job.getClusterId()).getHarborServer().getHarborAddress());
        List<StageDto> stageDtoList = new ArrayList<>();
        List<StageDto> imageBuildStages = new ArrayList<>();
        Map<Integer, DockerFile> dockerFileMap = new HashedMap();
        boolean dockerEnvironmentExit = false;
        for (Stage stage : stageList) {
            StageDto newStageDto = new StageDto();
            newStageDto.convertFromBean(stage);
            if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == newStageDto.getStageTemplateType()) {
                if (!dockerEnvironmentExit) {
                    BuildEnvironment buildEnvironment = buildEnvironmentMapper.selectByPrimaryKey(0);
                    if (buildEnvironment == null) {
                        throw new MarsRuntimeException(ErrorCodeMessage.DEFAULT_BUILD_ENVIRONMENT_NOT_EXIST);
                    }
                    String image = buildEnvironment.getImage();
                    if (image.split("/").length < CommonConstant.NUM_THREE) {
                        Cluster topCluster = clusterService.getPlatformCluster();
                        image = topCluster.getHarborServer().getHarborAddress() + "/" + image;
                    }
                    newStageDto.setEnvironmentChange(true);
                    newStageDto.setBuildEnvironment(image);
                    dockerEnvironmentExit = true;
                }
                if (DockerfileTypeEnum.PLATFORM.ordinal() == newStageDto.getDockerfileType()) {
                    DockerFile dockerFile = dockerFileService.selectDockerFileById(newStageDto.getDockerfileId());
                    dockerFile.setContent(StringEscapeUtils.escapeJava(dockerFile.getContent()));
                    dockerFileMap.put(stage.getStageOrder(), dockerFile);
                    //把步骤类型为镜像构建的放到一个list里面
                    imageBuildStages.add(newStageDto);
                }
            }
            if (StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType()) {
                if (StringUtils.isNotBlank(stage.getRepositoryBranch())) {
                    String[] str = stage.getRepositoryBranch().split(":", CommonConstant.NUM_TWO);
                    if (CommonConstant.REFERENCES_BRANCH.equals(str[0])) {
                        if (str.length == 2) {
                            newStageDto.setRepositoryBranch(str[CommonConstant.NUM_ONE]);
                        }
                    } else if (CommonConstant.REFERENCES_TAG.equals(str[0])) {
                        if (str.length == 2 && StringUtils.isNotBlank(str[CommonConstant.NUM_ONE])) {
                            newStageDto.setRepositoryBranch("refs/tags/" + str[CommonConstant.NUM_ONE]);
                        }
                    }
                }
            }
            if (StageTemplateTypeEnum.CODECHECKOUT.getCode() == stage.getStageTemplateType() ||
                    (StageTemplateTypeEnum.CUSTOM.getCode() == stage.getStageTemplateType() && stage.isEnvironmentChange())) {
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
        dataModel.put("timeout", jenkinsTimeout);
        dataModel.put("nodeSelector", buildNodeSelector);
        String script = null;
        logger.info("构建流水线pod nodeSelector:{}", buildNodeSelector);
        try {
            script = TemplateUtil.generate("pipeline.ftl", dataModel);
        } catch (Exception e) {
            logger.error("流水线脚本生成失败.{}", e);
        }
        return script;
    }

    private void verifyUpgrade(String service, String namespace, Integer instance, boolean manually) throws Exception {
        //校验分区
        if(StringUtils.isEmpty(namespace) || namespaceLocalService.getNamespaceByName(namespace) == null){
            throw new MarsRuntimeException(ErrorCodeMessage.NAMESPACE_NOT_FOUND);
        }
        //判断服务是否启动
        ActionReturnUtil deploymentDetailResult = deploymentsService.getDeploymentDetail(namespace, service,false);
        if (deploymentDetailResult.isSuccess()) {
            AppDetail appDetail = (AppDetail) deploymentDetailResult.getData();
            if (appDetail != null) {
                if (Constant.SERVICE_STOP.equals(appDetail.getStatus())) {
                    if (manually) {
                        throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_NOT_STARTED_INFORM);
                    } else {
                        throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_NOT_STARTED);
                    }
                }else{
                    if(instance != null && instance > appDetail.getInstance()){
                        if (manually) {
                            throw new MarsRuntimeException(ErrorCodeMessage.UPGRADE_INSTANCE_EXCEED_SERVICE_INFORM);
                        } else {
                            throw new MarsRuntimeException(ErrorCodeMessage.UPGRADE_INSTANCE_EXCEED_SERVICE);
                        }
                    }
                }
            }
        }

        //判断是否在蓝绿升级中
        ActionReturnUtil result = blueGreenDeployService.getInfoAboutTwoVersion(service, namespace);
        if (result.isSuccess()) {
            Map data = (Map) result.getData();
            if (data != null && data.keySet().size() == 2) {
                if (manually) {
                    throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_ALREADY_IN_BLUE_GREEN_UPGRADE_INFORM);
                } else {
                    throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_ALREADY_IN_BLUE_GREEN_UPGRADE);
                }
            }
        }
        //判断是否在灰度升级中
        ActionReturnUtil updateStatusResult = versionControlService.getUpdateStatus(namespace, service, Constant.DEPLOYMENT);
        if (updateStatusResult.isSuccess()) {
            Map data = (Map) updateStatusResult.getData();
            if (data != null) {
                List<Integer> counts = (List<Integer>) data.get("counts");
                if (CollectionUtils.isNotEmpty(counts) && counts.size() == 2 && counts.get(1) != 0) {
                    if (manually) {
                        throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_ALREADY_IN_CANARY_UPGRADE_INFORM);
                    } else {
                        throw new MarsRuntimeException(ErrorCodeMessage.SERVICE_ALREADY_IN_CANARY_UPGRADE);
                    }
                }
            }
        }
    }

    /**
     * 更新jenkins中的镜像版本
     *
     * @param job
     * @throws Exception
     */
    private void updateJenkinsImageTag(Job job) throws Exception {
        JenkinsServer jenkinsServer = JenkinsClient.getJenkinsServer();
        String projectName = projectService.getProjectNameByProjectId(job.getProjectId());
        String clusterName = clusterService.getClusterNameByClusterId(job.getClusterId());
        FolderJob folderJob = getFolderJob(projectName, clusterName);
        String jobXml = jenkinsServer.getJobXml(folderJob, job.getName());
        Document doc = DocumentHelper.parseText(jobXml);
        Element rootElement = doc.getRootElement();
        List<Stage> stageList = stageService.getStageByJobId(job.getId());
        Map<String, String> tagMap = new HashMap<>();
        for (Stage stage : stageList) {
            if (StageTemplateTypeEnum.IMAGEBUILD.getCode() == stage.getStageTemplateType() && CommonConstant.IMAGE_TAG_RULE.equals(stage.getImageTagType())) {
                tagMap.put("tag" + stage.getStageOrder(), stage.getImageBaseTag());
            }
        }
        if (tagMap.size() > 0) {
            List<Element> paramElements = rootElement.element("properties").element("hudson.model.ParametersDefinitionProperty").element("parameterDefinitions").elements("hudson.model.StringParameterDefinition");
            for (Element element : paramElements) {
                String param = element.elementText("name");
                if (tagMap.get(param) != null) {
                    element.element("defaultValue").setText(tagMap.get(param));
                }
            }
        }
        jenkinsServer.updateJob(folderJob, job.getName(), doc.asXML(), false);
    }


    /**
     * 镜像推送
     */
    private void imagePush(Stage stage) throws Exception{
        ImageRepository imageRepository = harborProjectService.findRepositoryById(stage.getRepositoryId());
        //校验镜像仓库
        if(imageRepository == null){
            throw new MarsRuntimeException(ErrorCodeMessage.REPOSITORY_NOT_EXIST);
        }
        if(!stage.getImageName().contains(CommonConstant.SLASH)){
            stage.setImageName(stage.getHarborProject() + CommonConstant.SLASH +stage.getImageName());
        }
        //校验镜像
        ActionReturnUtil actionReturnUtil = harborProjectService.getImage(imageRepository.getId(), stage.getImageName());
        if(!actionReturnUtil.isSuccess()){
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_NOT_EXIST);
        }
        HarborRepositoryMessage harborRepositoryMessage = (HarborRepositoryMessage)actionReturnUtil.getData();
        if(harborRepositoryMessage == null){
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_NOT_EXIST);
        }
        //校验镜像版本
        if(StringUtils.isEmpty(stage.getImageTag()) && CollectionUtils.isEmpty(harborRepositoryMessage.getTags())){
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_TAG_NOT_EXIST);
        }else if(StringUtils.isNotEmpty(stage.getImageTag()) && !harborRepositoryMessage.getTags().contains(stage.getImageTag())){
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_TAG_NOT_EXIST);
        }

        if(StringUtils.isEmpty(stage.getImageTag())){
            stage.setImageTag(harborRepositoryMessage.getTags().get(0));
        }
        boolean result = harborProjectService.syncImage(stage.getRepositoryId(),stage.getImageName(),stage.getImageTag(),stage.getDestClusterId(),true);
        if(!result){
            throw new MarsRuntimeException(ErrorCodeMessage.IMAGE_PUSH_ERROR);
        }
    }

    /**
     * 校验升级时的资源配额
     * @param stageDto
     * @param cluster
     * @throws Exception
     */
    private void verifyUpgradeResource(StageDto stageDto, Cluster cluster) throws Exception{
        K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stageDto.getNamespace(), stageDto.getServiceName(), null, null, HTTPMethod.GET, cluster);
        if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {
            throw new MarsRuntimeException(depRes.getBody());
        }
        Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
        List<Container> containerList = dep.getSpec().getTemplate().getSpec().getContainers();
        BigDecimal instanceCpu = BigDecimal.ZERO;
        BigDecimal instanceMemory = BigDecimal.ZERO;
        Integer instanceGpu = 0;
        //计算每个容器的资源总和
        for(Container container:containerList){
            if(container.getResources() != null && container.getResources().getRequests() !=null){
                Map<String, Object> request = (Map<String, Object>)container.getResources().getRequests();
                String cpu = (String)request.get(CommonConstant.CPU);
                String memory = (String)request.get(CommonConstant.MEMORY);
                if(cpu.contains(CommonConstant.SMALLM)){
                    instanceCpu = instanceCpu.add(new BigDecimal(cpu.split(CommonConstant.SMALLM)[0]).divide(new BigDecimal(CommonConstant.NUM_THOUSAND)));
                }else{
                    instanceCpu = instanceCpu.add(new BigDecimal(cpu));
                }
                if(memory.contains(CommonConstant.SMALLM)){
                    instanceMemory = instanceMemory.add(new BigDecimal(memory.split(CommonConstant.SMALLM)[0]));
                }else if(memory.contains(CommonConstant.MI)){
                    instanceMemory = instanceMemory.add(new BigDecimal(memory.split(CommonConstant.MI)[0]));
                }else if(memory.contains(CommonConstant.SMALLG)){
                    instanceMemory = instanceMemory.add(new BigDecimal(memory.split(CommonConstant.SMALLG)[0]).multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY)));
                }else if(memory.contains(CommonConstant.GI)){
                    instanceMemory = instanceMemory.add(new BigDecimal(memory.split(CommonConstant.GI)[0]).multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY)));
                }
                if(request.get(CommonConstant.NVIDIA_GPU) != null){
                    instanceGpu += Integer.valueOf((String)request.get(CommonConstant.NVIDIA_GPU));
                }
            }
        }
        //获取分区下的资源配额
        Map<String, Object> quotaMap = namespaceService.getNamespaceQuota(stageDto.getNamespace());
        List<Object> cpus = (List<Object>)quotaMap.get(CommonConstant.CPU);
        List<Object> memorys = (List<Object>)quotaMap.get(CommonConstant.MEMORY);
        List<Object> gpus = (List<Object>)quotaMap.get(CommonConstant.GPU);
        String hardType = (String)quotaMap.get(CommonConstant.HARDTYPE);
        String usedType = (String)quotaMap.get(CommonConstant.USEDTYPE);
        //计算cpu剩余量与服务cpu比较
        BigDecimal remainCpu = BigDecimal.ZERO;
        if(CollectionUtils.isNotEmpty(cpus) && cpus.size() == 2) {
            BigDecimal totalCpu = new BigDecimal((String)cpus.get(0));
            BigDecimal usedCpu = new BigDecimal((String)cpus.get(1));
            remainCpu = totalCpu.subtract(usedCpu);

        }
        //计算内存剩余量与服务内存比较
        BigDecimal remainMemory = BigDecimal.ZERO;
        if(CollectionUtils.isNotEmpty(memorys) && memorys.size() == 2) {
            BigDecimal totalMemory = new BigDecimal((String)memorys.get(0));
            if(CommonConstant.GB.equals(hardType)){
                totalMemory = totalMemory.multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY));
            }else if(CommonConstant.TB.equals(hardType)){
                totalMemory = totalMemory.multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY));
            }
            BigDecimal usedMemory = new BigDecimal((String)memorys.get(1));
            if(CommonConstant.GB.equals(usedType)){
                usedMemory = usedMemory.multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY));
            }else if(CommonConstant.TB.equals(usedType)){
                usedMemory = usedMemory.multiply(BigDecimal.valueOf(CommonConstant.NUM_SIZE_MEMORY * CommonConstant.NUM_SIZE_MEMORY));
            }
            remainMemory = totalMemory.subtract(usedMemory);

        }
        //GPU校验
        Integer remainGpu = 0;
        if(CollectionUtils.isNotEmpty(gpus) && gpus.size() == 2) {
            Integer totalGpu = Integer.valueOf((String)gpus.get(0));
            Integer usedGpu = Integer.valueOf((String)gpus.get(1));
            remainGpu = totalGpu - usedGpu;

        }
        if(CommonConstant.CANARY_RELEASE.equals(stageDto.getDeployType()) && stageDto.getMaxSurge() != null && stageDto.getMaxSurge() == 1){
            //不中断服务升级，需校验剩余资源
            if(remainCpu.compareTo(instanceCpu)<0 || remainMemory.compareTo(instanceMemory) < 0){
                throw new MarsRuntimeException(ErrorCodeMessage.NO_ENOUGH_RESOURCE);
            }
            if(instanceGpu != null && remainGpu < instanceGpu) {
                throw new MarsRuntimeException(ErrorCodeMessage.NO_ENOUGH_RESOURCE);
            }
        }else if(CommonConstant.BLUE_GREEN_RELEASE.equals(stageDto.getDeployType())){
            int instant = dep.getSpec().getReplicas();
            if(remainCpu.compareTo(instanceCpu.multiply(BigDecimal.valueOf(instant))) < 0  || remainMemory.compareTo(instanceMemory.multiply(BigDecimal.valueOf(instant))) < 0){
                throw new MarsRuntimeException(ErrorCodeMessage.NO_ENOUGH_RESOURCE_BLUEGREEN);
            }
            if(instanceGpu != null && remainGpu < (instanceGpu*instant)) {
                throw new MarsRuntimeException(ErrorCodeMessage.NO_ENOUGH_RESOURCE_BLUEGREEN);
            }
        }
    }
}
