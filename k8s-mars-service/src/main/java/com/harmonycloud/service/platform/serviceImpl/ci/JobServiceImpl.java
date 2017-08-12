package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.enumm.StageTemplateTypeEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.business.CreateConfigMapDto;
import com.harmonycloud.dto.business.CreateEnvDto;
import com.harmonycloud.dto.business.CreatePortDto;
import com.harmonycloud.dto.business.CreateResourceDto;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.k8s.bean.ConfigMap;
import com.harmonycloud.k8s.bean.ContainerPort;
import com.harmonycloud.k8s.bean.Deployment;
import com.harmonycloud.k8s.bean.EnvVar;
import com.harmonycloud.k8s.client.K8SClient;
import com.harmonycloud.k8s.constant.HTTPMethod;
import com.harmonycloud.k8s.service.DeploymentService;
import com.harmonycloud.k8s.util.K8SClientResponse;
import com.harmonycloud.service.application.BusinessDeployService;
import com.harmonycloud.service.application.ConfigMapService;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.VersionControlService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.convert.K8sResultConvert;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.platform.socket.SystemWebSocketHandler;
import com.harmonycloud.service.tenant.TenantService;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by anson on 17/5/31.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class JobServiceImpl implements JobService {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);

    @Resource
    SystemWebSocketHandler systemWebSocketHandler;

    @Autowired
    HarborClient harborClient;

    @Autowired
    JobMapper jobMapper;

    @Autowired
    StageMapper stageMapper;

    @Autowired
    StageTypeMapper stageTypeMapper;

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
    BusinessDeployService businessDeployService;

    @Autowired
    ConfigMapService configMapService;

    @Autowired
    DockerFileJobStageMapper dockerFileJobStageMapper;

    @Autowired
    BuildEnvironmentMapper buildEnvironmentMapper;

    @Value("#{propertiesReader['web.url']}")
    private String webUrl;

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    public ActionReturnUtil createJob(JobDto jobDto) throws Exception {
        Job job = jobDto.convertToBean();
        String jenkinsJobName = job.getTenant() + "_" + job.getName();

        // validate jobname
        ActionReturnUtil result = nameValidate(job.getName(), job.getTenant());
        if (!result.isSuccess()) {
            return result;
        }
        String username = (String)session.getAttribute("username");
        job.setCreateUser(username);
        job.setCreateTime(new Date());
        jobMapper.insertJob(job);

        //create job
        Map<String, Object> params = new HashMap<>();
        params.put("name", jenkinsJobName);
        params.put("mode", "org.jenkinsci.plugins.workflow.job.WorkflowJob");
        params.put("json", JsonUtil.convertToJson(params));
        result = HttpJenkinsClientUtil.httpPostRequest("/createItem", null, params, null, 302);

        //config job
        if (result.isSuccess()) {
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("stageList", new ArrayList<>());
            dataModel.put("job", job);
            dataModel.put("apiUrl",apiUrl);
            String script = TemplateUtil.generate("pipeline.ftl", dataModel);
            dataModel.put("script", script);
            String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        }else{
            throw new Exception();
        }
        Map data = new HashMap<>();
        data.put("id",job.getId());
        return ActionReturnUtil.returnSuccessWithData(data);
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
    public ActionReturnUtil deleteJob(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if(null == job){
            throw new Exception("流程不存在。");
        }
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        jobMapper.deleteJobById(id);
        stageMapper.deleteStageByJob(id);
        dockerFileJobStageMapper.deleteDockerFileByJobId(id);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/doDelete", null, null, null, 302);
        if (!result.isSuccess()) {
            throw new Exception("删除失败。");
        }
        return result;
    }

    @Override
    public ActionReturnUtil nameValidate(String jobName, String tenantName) {
        String jenkinsJobName = tenantName + "_" + jobName;
        Map<String, Object> params = new HashMap<>();
        params.put("value", jenkinsJobName);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/view/all/checkJobName", null, params, false);
        String data = String.valueOf(result.get("data"));
        if (result.isSuccess()) {
            if (data.contains("exists")) {
                return ActionReturnUtil.returnErrorWithMap("message", "A job already exists with the name '" + jobName + "'");
            } else if (data.contains("error")) {
                return ActionReturnUtil.returnErrorWithMap("message", data.replaceAll("<.*?>", ""));
            } else {
                return ActionReturnUtil.returnSuccess();
            }
        } else {
            return result;
        }
    }

    @Override
    public ActionReturnUtil getJobList(String tenantName, String jobName) {
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "jobs[name,color,lastBuild[number,building,result,timestamp],builds[result]]");
        params.put("wrapper", "root");
        params.put("xpath", "//job/name[starts-with(text(),\"" + tenantName + "_\")]/..");
        ActionReturnUtil result;
        result = HttpJenkinsClientUtil.httpGetRequest("/view/all/api/xml", null, params, false);

        if (result.isSuccess()) {
            String jenkinsJobName;
            List jobList = new ArrayList<>();
            Map jobMap;
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            if(jenkinsDataMap.get("root") instanceof String){
                return ActionReturnUtil.returnSuccessWithData(jobList);
            }
            Map rootMap = (Map) jenkinsDataMap.get("root");
            List<Map> jenkinsJobList = new ArrayList<>();
            List<Map> jenkinsBuildList;


            if (rootMap.get("job") instanceof Map) {
                jenkinsJobList.add((Map) rootMap.get("job"));
            } else if (rootMap.get("job") instanceof List) {
                jenkinsJobList.addAll((List) rootMap.get("job"));
            }
            for (Map jenkinsJob : jenkinsJobList) {
                jobMap = new HashMap();
                jenkinsJobName = (String) jenkinsJob.get("name");
                if (jenkinsJobName != null) {
                    String[] name = jenkinsJobName.split("_", 2);
                    if (name.length == 2) {
                        if(jobName !=null && !name[1].contains(jobName)){
                            continue;
                        }
                        jobMap.put("tenant", name[0]);
                        jobMap.put("name", name[1]);
                    }
                }
                Map lastBuildMap = (Map) jenkinsJob.get("lastBuild");
                if (lastBuildMap != null) {
                    if ("false".equalsIgnoreCase((String) lastBuildMap.get("building"))) {
                        jobMap.put("last_build_status", lastBuildMap.get("result"));
                    } else {
                        jobMap.put("last_build_status", "BUILDING");
                    }
                    if (lastBuildMap.get("timestamp") != null) {
                        jobMap.put("last_build_time", df.format(new Timestamp(Long.valueOf((String) lastBuildMap.get("timestamp")))));
                    }
                    if(lastBuildMap.get("number") != null){
                        jobMap.put("last_build_number", lastBuildMap.get("number"));
                    }
                } else {
                    jobMap.put("last_build_status", "NOTBUILT");
                    jobMap.put("last_build_time", "");
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

                jobMap.put("success_num",successNum);
                jobMap.put("fail_num",failNum);


                List<Job> dbJobList = jobMapper.select(tenantName, (String)jobMap.get("name"), null);
                if(dbJobList != null && dbJobList.size() == 1){
                    Job dbJob = dbJobList.get(0);
                    jobMap.put("id", dbJob.getId());
                }
                else{
                    continue;
                }

                jobList.add(jobMap);
            }
            return ActionReturnUtil.returnSuccessWithData(jobList);
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil getJobDetail(Integer id) throws Exception {
        Job dbJob = jobMapper.queryById(id);
        Map job = new HashMap();
        job.put("id", id);
        job.put("jobName", dbJob.getName());
        job.put("tenant", dbJob.getTenant());
        String jenkinsJobName = dbJob.getTenant() + "_" + dbJob.getName();
        Integer buildNum = null;
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/json", null, null, false);
        List<Map> jenkinsStageMap = new ArrayList<>();
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
            jenkinsStageMap = stageService.getStageBuildFromJenkins(dbJob, 0);
        }else{
            job.put("lastBuildStatus", Constant.PIPELINE_STATUS_NOTBUILT);
            job.put("lastBuildDuration", null);
            job.put("lastBuildTime", null);
        }
        job.put("buildNum", buildNum);


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
            List<StageType> stageTypeList = stageTypeMapper.queryByTenant(dbJob.getTenant());
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

            if(stage.getStageOrder() <= jenkinsStageMap.size()){
                Map map = jenkinsStageMap.get(stage.getStageOrder() - 1);
                stageMap.put("lastBuildStatus", convertStatus((String)map.get("status")));
                stageMap.put("lastBuildTime", new Timestamp((Long)map.get("startTimeMillis")));
                stageMap.put("lastBuildDuration", map.get("durationMillis"));
            }else{
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
    public ActionReturnUtil build(Integer id) throws Exception{
        Job job = jobMapper.queryById(id);
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Integer lastBuildNumber = null;
        Map<String, Object> params = new HashMap<>();
        List<Stage> stageList = stageMapper.queryByJobId(id);
        Map tagMap = new HashMap<>();
        for(Stage stage:stageList){
            if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType()) {
//                if ("0".equals(stage.getImageTagType())) {
//                    tag = DateUtil.DateToString(new Date(), DateStyle.YYMMDDHHMMSS);
                if ("1".equals(stage.getImageTagType())) {
                    stage.setImageBaseTag(generateTag(stage));
                    stageMapper.updateStage(stage);
                    tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), stage.getImageBaseTag());
                }else if ("2".equals(stage.getImageTagType())) {
                    tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), stage.getImageTag());
                }
//                tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), tag);
//                params.put("tag" + stage.getStageOrder(), tag);
            }
        }

        //if (result.isSuccess()) {
            params = new HashMap<>();
         //   params.put("tree","number,timestamp");
          //  result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/xml", null, params, false);
            ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/api/json", null, null, false);
            if(result.isSuccess()){
                Map dataMap = JsonUtil.convertJsonToMap((String) result.get("data"));
                lastBuildNumber = (Integer)dataMap.get("nextBuildNumber");
                jobMapper.updateLastBuildNum(id, lastBuildNumber);
                JobBuild jobBuild = new JobBuild();
                jobBuild.setJobId(id);
                jobBuild.setBuildNum(lastBuildNumber);
                jobBuild.setStatus(Constant.PIPELINE_STATUS_BUILDING);
                jobBuild.setStartUser((String)session.getAttribute("username"));
                jobBuildMapper.insert(jobBuild);

                Map<Integer, String> stageTypeMap = new HashMap<>();
                List<StageType> stageTypeList = stageTypeMapper.queryByTenant(job.getTenant());
                for(StageType stageType : stageTypeList){
                    stageTypeMap.put(stageType.getId(),stageType.getName());
                }

                for(Stage stage : stageList){
                    StageBuild stageBuild = new StageBuild();
                    stageBuild.setJobId(id);
                    stageBuild.setStageId(stage.getId());
                    stageBuild.setStageName(stage.getStageName());
                    stageBuild.setStageOrder(stage.getStageOrder());
                    stageBuild.setStageType(stageTypeMap.get(stage.getStageTypeId()));
                    stageBuild.setBuildNum(lastBuildNumber);
                    stageBuild.setStatus(Constant.PIPELINE_STATUS_WAITING);
                    if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()) {
                        stageBuild.setImage(stage.getImageName() + ":" + tagMap.get(stage.getImageName()));
                    }
                    stageBuildMapper.insert(stageBuild);
                }

            }
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
        result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/buildWithParameters", null, params, null, null);
        if(result.isSuccess()){
            Map data = new HashMap<>();
            data.put("buildNum", lastBuildNumber);
            return ActionReturnUtil.returnSuccessWithData(data);
        }else{
            throw new Exception("启动失败。");
        }
    }


    @Override
    public ActionReturnUtil stopBuild(String jobName, String tenantName, String buildNum) {
        String jenkinsJobName = tenantName + "_" + jobName;
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/" + buildNum + "/stop", null, null, null, 302);
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
    public ActionReturnUtil credentialsValidate(String repositoryType, String repositoryUrl, String username, String password) {
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
            Map buildMap = new HashMap();
            if(Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus())){
                jobBuild = jobStatusSync(job, jobBuild.getBuildNum());
            }
            buildMap.put("buildNum", jobBuild.getBuildNum());
            buildMap.put("buildStatus", jobBuild.getStatus());
            buildMap.put("buildTime", jobBuild.getStartTime());
            buildMap.put("duration", jobBuild.getDuration());
            buildMap.put("log", jobBuild.getLog());
            StageBuild stageBuildCondition = new StageBuild();
            stageBuildCondition.setJobId(id);
            stageBuildCondition.setBuildNum(jobBuild.getBuildNum());
            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
            List stageBuildMapList = new ArrayList<>();
            for(StageBuild stageBuild : stageBuildList){
                Map stageBuildMap = new HashMap<>();
                if(!Constant.PIPELINE_STATUS_BUILDING.equals(jobBuild.getStatus()) && Constant.PIPELINE_STATUS_WAITING.equals(stageBuild.getStatus())){
                    allStageStatusSync(job, jobBuild.getBuildNum());
                    stageBuildMapper.updateWaitingStage(job.getId(), jobBuild.getBuildNum());
                    stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
                    stageBuildMapList = new ArrayList<>();
                    for(StageBuild newStageBuild : stageBuildList){
                        stageBuildMap.put("stageId", newStageBuild.getStageId());
                        stageBuildMap.put("stageName", newStageBuild.getStageName());
                        stageBuildMap.put("stageOrder", newStageBuild.getStageOrder());
                        stageBuildMap.put("stageType", newStageBuild.getStageType());
                        stageBuildMap.put("buildStatus", newStageBuild.getStatus());
                        stageBuildMap.put("buildNum", newStageBuild.getBuildNum());
                        stageBuildMap.put("buildTime", newStageBuild.getStartTime());
                        stageBuildMap.put("duration", newStageBuild.getDuration());
                        stageBuildMap.put("log", newStageBuild.getLog());
                        stageBuildMapList.add(stageBuildMap);
                    }
                    break;
                }
                stageBuildMap.put("stageId", stageBuild.getStageId());
                stageBuildMap.put("stageName", stageBuild.getStageName());
                stageBuildMap.put("stageOrder", stageBuild.getStageOrder());
                stageBuildMap.put("stageType", stageBuild.getStageType());
                stageBuildMap.put("buildStatus", stageBuild.getStatus());
                stageBuildMap.put("buildNum", stageBuild.getBuildNum());
                stageBuildMap.put("buildTime", stageBuild.getStartTime());
                stageBuildMap.put("duration", stageBuild.getDuration());
                stageBuildMap.put("log", stageBuild.getLog());
                stageBuildMapList.add(stageBuildMap);
            }

            buildMap.put("stageList",stageBuildMapList);
            buildList.add(buildMap);
        }
        Map data = new HashMap<>();
        data.put("total", total);
        data.put("pageSize", pageSize);
        data.put("page", page);
        data.put("totalPage", Math.ceil(1.0 * total/pageSize));
        data.put("buildList",buildList);
        return ActionReturnUtil.returnSuccessWithData(data);
        /*
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "building,timestamp,result,duration,number");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/api/xml", null, params, false);
        ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/logText/progressiveHtml", null, null, false);
        if (result.isSuccess()) {
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map build = new HashMap();
            //Map buildMap;
            //List<Map> jenkinsBuildList = new ArrayList<>();
            //List buildList = new ArrayList<>();
            build.put("name", jobName);
            build.put("tenant", tenantName);
            Map rootMap = (Map) jenkinsDataMap.get("workflowRun");
            if (rootMap != null) {
                build.put("build_num", rootMap.get("number"));
                if ("false".equalsIgnoreCase((String) rootMap.get("building"))) {
                    build.put("build_status", rootMap.get("result"));
                } else {
                    build.put("build_status", "BUILDING");
                }
                if (rootMap.get("timestamp") != null) {
                    build.put("start_time", new Timestamp(Long.valueOf((String) rootMap.get("timestamp"))).toString());
                }
                build.put("duration", rootMap.get("duration"));
            }
            if (logResult.isSuccess()) {
                build.put("build_log", logResult.get("data"));
            }
            return ActionReturnUtil.returnSuccessWithData(build);
        }
        return ActionReturnUtil.returnError();
        */
    }

    @Override
    public void getJobLogWS(WebSocketSession session, Integer id, String buildNum) {
        Job job = jobMapper.queryById(id);
        String jenkinsjobName = job.getTenant() + "_" +job.getName();
        String start = "0";
        String moreData = "";
        try {
            while (moreData != null && session.isOpen()) {
                moreData = null;
                Map params = new HashMap<>();
                params.put("start", start);
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsjobName + "/" + buildNum + "/logText/progressiveHtml", null, params, true);
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
                            session.sendMessage(new TextMessage((String) ((Map) result.get("data")).get("body")));
                        }
                        Thread.sleep(2000);

                    }
                }
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
        stageService.updateJenkinsJob(job.getId());
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil getTrigger(Integer id) throws Exception {
        Job job = jobMapper.queryById(id);
        if(null == job){
            return ActionReturnUtil.returnErrorWithMap("message", "流程不存在");
        }
        Map triggerMap = new HashMap();
        triggerMap.put("trigger", job.isTrigger());
        triggerMap.put("pollScm", job.isPollScm());
        if(StringUtils.isNotBlank(job.getCronExpForPollScm())){
            String cron = job.getCronExpForPollScm();
            String[] part = cron.split(" ");
            triggerMap.put("dayOfMonth", part[2]);
            triggerMap.put("dayOfWeek", part[4]);
            triggerMap.put("hour", part[1]);
            triggerMap.put("minute", part[0]);
        }
        return ActionReturnUtil.returnSuccessWithData(triggerMap);
    }

    @Override
    public ActionReturnUtil updateTrigger(JobDto jobDto) {
        String username = (String)session.getAttribute("username");
        jobDto.setUpdateUser(username);
        jobDto.setUpdateTime(new Date());
        String cron = null;
        if(StringUtils.isNotBlank(jobDto.getDayOfMonth()) && StringUtils.isNotBlank(jobDto.getDayOfWeek()) && StringUtils.isNotBlank(jobDto.getHour()) && StringUtils.isNotBlank(jobDto.getMinute())){
            cron = jobDto.getMinute() + " " + jobDto.getHour() + " " + jobDto.getDayOfMonth() + " * " + jobDto.getDayOfWeek();
        }
        jobDto.setCronExpForPollScm(cron);
        Job job = jobDto.convertToBean();
        jobMapper.updateTrigger(job);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void preBuild(Integer id, Integer buildNum, String dateTime){
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                JobBuild jobbuild = new JobBuild();
                jobbuild.setJobId(id);
                jobbuild.setBuildNum(buildNum);
                List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobbuild);
                if(jobBuildList == null || jobBuildList.size() == 0){
                    Job job = jobMapper.queryById(id);
                    String jenkinsJobName = job.getTenant()+"_"+job.getName();
                    Map params = new HashMap<>();
                    params.put("tree","number,timestamp");
                    ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/xml", null, params, false);
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

                        Map<Integer, String> stageTypeMap = new HashMap<>();
                        List<StageType> stageTypeList = stageTypeMapper.queryByTenant(job.getTenant());
                        for(StageType stageType : stageTypeList){
                            stageTypeMap.put(stageType.getId(),stageType.getName());
                        }

                        Map tagMap = new HashMap<>();
                        result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/config.xml", null, null, false);
                        if(result.isSuccess()){
                            body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
                            Map definition = (Map)body.get("flow-definition");
                            Object param = ((Map)((Map)((Map)definition.get("properties")).get("hudson.model.ParametersDefinitionProperty")).get("parameterDefinitions")).get("hudson.model.StringParameterDefinition");
                            if(param instanceof List){
                                for(Map map : (List<Map>)param){
                                    tagMap.put(map.get("name"), map.get("defaultValue"));
                                }
                            }
                        }

                        List<Stage> stageList = stageMapper.queryByJobId(id);
                        for(Stage stage:stageList){
                            StageBuild stageBuild = new  StageBuild();
                            stageBuild.setJobId(id);
                            stageBuild.setStageId(stage.getId());
                            stageBuild.setStageName(stage.getStageName());
                            stageBuild.setStageOrder(stage.getStageOrder());
                            stageBuild.setStageType(stageTypeMap.get(stage.getStageTypeId()));
                            stageBuild.setBuildNum(lastBuildNumber);
                            stageBuild.setStatus(Constant.PIPELINE_STATUS_WAITING);
                            if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType()){
                                if("0".equals(stage.getImageTagType())){
                                    tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), dateTime);
                                }else {
                                    tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), tagMap.get("tag" + stage.getStageOrder()));
                                }
                                if("1".equals(stage.getImageTagType())){
                                    stage.setImageBaseTag(generateTag(stage));
                                    stageMapper.updateStage(stage);
                                }
                            }
                            if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()){
                                stageBuild.setImage(stage.getImageName() + ":" + tagMap.get(stage.getImageName()));
                            }
                            stageBuildMapper.insert(stageBuild);
                        }
                    }
                }else{
                    Map tagMap = new HashMap<>();
                    List<Stage> stageList = stageMapper.queryByJobId(id);
                    for(Stage stage:stageList){
                        if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType() && "0".equals(stage.getImageTagType())){

                            tagMap.put(stage.getHarborProject() + "/" + stage.getImageName(), dateTime);
                        }else if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()){
                            StageBuild stageBuild = new StageBuild();
                            stageBuild.setStageId(stage.getId());
                            stageBuild.setBuildNum(buildNum);
                            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuild);
                            if(stageBuildList != null && stageBuildList.size() == 1){
                                StageBuild sBuild = stageBuildList.get(0);
                                if(tagMap.get(stage.getImageName())!=null){
                                    sBuild.setImage(stage.getImageName()+":"+tagMap.get(stage.getImageName()));
                                    stageBuildMapper.updateByStageOrderAndBuildNum(sBuild);
                                }
                            }
                        }
                    }



                }
            }
        };
        NewCachedThreadPool threadPool = NewCachedThreadPool.init();
        threadPool.execute(worker);
    }


    @Override
    public void postBuild(Integer id, Integer buildNum) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                try {
                    stageService.updateJenkinsJob(id);
                    Thread.sleep(3000);
                    Job job = jobMapper.queryById(id);
                    jobStatusSync(job, buildNum);
                    allStageStatusSync(job, buildNum);
                    stageBuildMapper.updateWaitingStage(job.getId(), buildNum);
                    notification(job, buildNum);
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
        Job job = jobMapper.queryById(stage.getJobId());
        Cluster cluster = null;
            cluster = clusterService.findClusterByTenantId(job.getTenantId());

            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            session = request.getSession();
            session.setAttribute("username","admin");
            K8SClient.tokenMap.put("admin", cluster.getMachineToken());

            K8SClientResponse depRes = deploymentService.doSpecifyDeployment(stage.getNamespace(), stage.getServiceName(), null, null, HTTPMethod.GET ,cluster);
            if (!HttpStatusUtil.isSuccessStatus(depRes.getStatus())) {

            }
            Deployment dep = JsonUtil.jsonToPojo(depRes.getBody(), Deployment.class);
            List<ContainerOfPodDetail> containerList = K8sResultConvert.convertContainer(dep);
            List<UpdateContainer> updateContainerList = new ArrayList<>();

            StageBuild stageBuildCondition = new StageBuild();
            stageBuildCondition.setStageId(stageId);
            stageBuildCondition.setBuildNum(buildNum);
            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
            StageBuild stageBuild = new StageBuild();
            if(stageBuildList.get(0) != null){
                stageBuild = stageBuildList.get(0);
            }

            for(ContainerOfPodDetail containerOfPodDetail: containerList){
                UpdateContainer updateContainer = new UpdateContainer();
                updateContainer.setName(containerOfPodDetail.getName());
                updateContainer.setArgs(containerOfPodDetail.getArgs());
                updateContainer.setCommand(containerOfPodDetail.getCommand());
                updateContainer.setLivenessProbe(containerOfPodDetail.getLivenessProbe());
                updateContainer.setReadinessProbe(containerOfPodDetail.getReadinessProbe());
                CreateResourceDto createResourceDto = new CreateResourceDto();
                createResourceDto.setCpu((String)containerOfPodDetail.getResource().get("cpu"));
                createResourceDto.setMemory((String)containerOfPodDetail.getResource().get("memory"));
                updateContainer.setResource(createResourceDto);
                List<CreateEnvDto> envList = new ArrayList<>();
                if(containerOfPodDetail.getEnv() != null) {
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
                for(ContainerPort containerPort :containerOfPodDetail.getPorts()){
                    CreatePortDto createPortDto = new CreatePortDto();
                    createPortDto.setProtocol(containerPort.getProtocol());
                    createPortDto.setPort(String.valueOf(containerPort.getContainerPort()));
                    createPortDto.setContainerPort(String.valueOf(containerPort.getContainerPort()));
                    portList.add(createPortDto);
                }
                updateContainer.setPorts(portList);
                List<UpdateVolume> updateVolumnList = new ArrayList<>();
                List<CreateConfigMapDto> configMaplist = new ArrayList<>();
                if(containerOfPodDetail.getStorage() != null) {
                    for (VolumeMountExt volumeMountExt : containerOfPodDetail.getStorage()) {
                        if ("logDir".equals(volumeMountExt.getType())) {
                            LogVolume logVolumn = new LogVolume();
                            logVolumn.setName(volumeMountExt.getName());
                            logVolumn.setMountPath(volumeMountExt.getMountPath());
                            logVolumn.setReadOnly(volumeMountExt.getReadOnly().toString());
                            logVolumn.setType(volumeMountExt.getType());

                            updateContainer.setLog(logVolumn);
                        } else if ("nfs".equals(volumeMountExt.getType()) || "emptyDir".equals(volumeMountExt.getType()) || "hostPath".equals(volumeMountExt.getType())) {
                            UpdateVolume updateVolume = new UpdateVolume();
                            updateVolume.setType(volumeMountExt.getType());
                            updateVolume.setReadOnly(volumeMountExt.getReadOnly().toString());
                            updateVolume.setMountPath(volumeMountExt.getMountPath());
                            updateVolume.setName(volumeMountExt.getName());
                            updateVolume.setEmptyDir(volumeMountExt.getEmptyDir());
                            updateVolume.setHostPath(volumeMountExt.getHostPath());
                            updateVolume.setRevision(volumeMountExt.getRevision());
                            updateVolume.setSubPath(volumeMountExt.getSubPath());
                            if ("nfs".equals(volumeMountExt.getType())) {
                                updateVolume.setPvcBindOne("true");
                                updateVolume.setPvcTenantid(job.getTenantId());
                                updateVolume.setPvcName(updateVolume.getName());
                                ActionReturnUtil result = businessDeployService.selectPv(job.getTenantId(), null, 1);
                                if (result.isSuccess()) {
                                    JSONArray array = (JSONArray) result.get("data");
                                    List<PvDto> pvDtoList = JsonUtil.jsonToList(array.toString(), PvDto.class);
                                    for (PvDto pvDto : pvDtoList) {
                                        if (volumeMountExt.getName().split("-")[0].equals(pvDto.getName())) {
                                            updateVolume.setPvcCapacity(pvDto.getCapacity());
                                            break;
                                        }
                                    }
                                }
                            }
                            updateVolumnList.add(updateVolume);
                        } else if ("configMap".equals(volumeMountExt.getType())) {
                            CreateConfigMapDto configMap = new CreateConfigMapDto();
                            configMap.setPath(volumeMountExt.getMountPath());
                            if (volumeMountExt.getName() != null && volumeMountExt.getName().lastIndexOf("v") > 0) {
                                configMap.setTag(volumeMountExt.getName().substring(volumeMountExt.getName().lastIndexOf("v") + 1).replace("-", "."));
                                configMap.setFile(volumeMountExt.getName().substring(0, volumeMountExt.getName().lastIndexOf("v")));
                            }
                            ActionReturnUtil configMapResult = configMapService.getConfigMapByName(stage.getNamespace(), volumeMountExt.getConfigMapName(), null, cluster);
                            if (configMapResult.isSuccess()) {
                                ConfigMap config = (ConfigMap) configMapResult.get("data");
                                Map data = (Map) config.getData();
                                configMap.setValue((String) data.get(volumeMountExt.getName().replace("-", ".")));
                            }
                            configMaplist.add(configMap);
                        }
                    }
                }
                updateContainer.setStorage(updateVolumnList);
                updateContainer.setConfigmap(configMaplist);

                if(updateContainer.getLog() == null){
                    updateContainer.setLog(new LogVolume());
                }

                if(stage.getContainerName().equals(containerOfPodDetail.getName())){
                    updateContainer.setImg(stageBuild.getImage());
                }else{
                    updateContainer.setImg(containerOfPodDetail.getImg());
                }
                updateContainerList.add(updateContainer);
            }

            CanaryDeployment canaryDeployment = new CanaryDeployment();
            canaryDeployment.setName(stage.getServiceName());
            canaryDeployment.setContainers(updateContainerList);
            canaryDeployment.setInstances(dep.getSpec().getReplicas());
            canaryDeployment.setNamespace(stage.getNamespace());
            canaryDeployment.setSeconds(5);

            versionControlService.canaryUpdate(canaryDeployment, dep.getSpec().getReplicas(), null, cluster);


    }

    @Override
    public void jobStatusWS(WebSocketSession session, Integer id) {
        try {
            Job job = jobMapper.queryById(id);
            String jenkinsJobName = job.getTenant() + "_" + job.getName();
            List<Stage> dbStageList = stageMapper.queryByJobId(id);
            StringBuilder lastStatus = new StringBuilder();
            StringBuilder currentStatus;
            while (session.isOpen()) {
                List<Stage> dbStageListCp = new ArrayList<>();
                dbStageListCp.addAll(dbStageList);
                currentStatus =  new StringBuilder();
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/wfapi/describe", null, null, false);
                if (result.isSuccess()) {
                    String data = (String) result.get("data");
                    Map dataMap = JsonUtil.convertJsonToMap(data);
                    String jobStatus = (String)dataMap.get("status");
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
                            if(dbStage.getStageName().equals(stage.get("name"))){
                                stageMap.put("stageId", dbStage.getId());
                                stageMap.put("stageOrder", dbStage.getStageOrder());
                                dbStageListCp.remove(dbStageListCp.indexOf(dbStage));
                                break;
                            }
                        }
//                        if(dbStageList.get(i) != null){
//                            stageMap.put("stageId", dbStageList.get(i).getId());
//                        }
                        i++;
//                        stageMap.put("stageOrder", i);
                        stageList.add(stageMap);
                        currentStatus.append(convertStatus((String)stage.get("status")));
                    }
                    for(Stage stage : dbStageListCp){
                        Map stageMap = new HashMap<>();
                        stageMap.put("stageId", stage.getId());
                        stageMap.put("stageOrder", stage.getStageOrder());
                        if(stage.getStageOrder() == 1){
                            stageMap.put("lastBuildStatus", Constant.PIPELINE_STATUS_INPROGRESS.equals(jobStatus)?Constant.PIPELINE_STATUS_WAITING:Constant.PIPELINE_STATUS_NOTBUILT);
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
                        if("FAILED".equals(dataMap.get("status"))){
                            jobMap.put("lastBuildStatus", "FAILURE");
                        }else {
                            jobMap.put("lastBuildStatus", convertStatus(jobStatus));
                        }
                        if(dataMap.get("startTimeMillis") instanceof Long) {
                            jobMap.put("lastBuildTime", new Timestamp((Long) dataMap.get("startTimeMillis")));
                        }else{
                            jobMap.put("lastBuildTime", new Timestamp(Long.valueOf((Integer) dataMap.get("startTimeMillis"))));
                        }
                        result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/json", null, null, false);
                        if (result.isSuccess()) {
                            dataMap = JsonUtil.convertJsonToMap((String) result.get("data"));
                        }
                        jobMap.put("lastBuildDuration", dataMap.get("duration"));
                        session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobMap))));
                    }
                }
                lastStatus = currentStatus;

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
    public ActionReturnUtil getYaml(Integer id) {
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
        if(job.isTrigger()){
            Map trigger = new LinkedHashMap<>();
            job.getCronExpForPollScm();
            trigger.put("pollScm", job.getCronExpForPollScm());
        }
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

            if(StageTemplateTypeEnum.CODECHECKOUT.ordinal() == stage.getStageTemplateType()){
                Map repository = new LinkedHashMap<>();
                repository.put("type", stage.getRepositoryType());
                repository.put("url", stage.getRepositoryUrl());
                repository.put("branch", stage.getRepositoryBranch());
                repository.put("username", stage.getCredentialsUsername());
                repository.put("password", stage.getCredentialsPassword());
                stageMap.put("repository", repository);
                stageMap.put("buildEnvironment",buildEnvMap.get(stage.getBuildEnvironment()));
                stageMap.put("environmentVariables", stageDto.getEnvironmentVariables());
                stageMap.put("dependencies", stageDto.getDependences());

            }
            else if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType()){
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
            else if(StageTemplateTypeEnum.DEPLOY.ordinal() == stage.getStageTemplateType()){
                stageMap.put("image", stage.getImageName());
                stageMap.put("namespace", stage.getNamespace());
                stageMap.put("service",stage.getServiceName());
                stageMap.put("container",stage.getContainerName());
            }
            if(stageDto.getCommand().size()>0) {
                stageMap.put("command", stageDto.getCommand());
            }
            stages.add(stageMap);
        }
        jobMap.put("stages", stages);
        //System.out.println(yaml.dumpAsMap(jobMap));
        return ActionReturnUtil.returnSuccessWithData(yaml.dumpAsMap(jobMap));
    }

    @Override
    public ActionReturnUtil getLastBuildLog(Integer id) {
        Job job = jobMapper.queryById(id);
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Map data = new HashMap();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/consoleText", null, null, false);
        if(result.isSuccess()){
            data.put("log", result.get("data"));
        }else{
            data.put("log", "");
        }
        return ActionReturnUtil.returnSuccessWithData(data);
    }

    @Override
    public void getJobListWS(WebSocketSession session, String tenant) {
        try {
            StringBuilder lastStatus = new StringBuilder();
            StringBuilder currentStatus;
            Map<String, Object> params = new HashMap<>();
            params.put("tree", "jobs[name,color,lastBuild[number,building,result,timestamp],builds[result]]");
            params.put("wrapper", "root");
            params.put("xpath", "//job/name[starts-with(text(),\"" + tenant + "_\")]/..");
            ActionReturnUtil result;
            while (session.isOpen()) {
                currentStatus = new StringBuilder();
                result = HttpJenkinsClientUtil.httpGetRequest("/view/all/api/xml", null, params, false);
                if(result.isSuccess()) {
                    String jenkinsJobName;
                    List jobList = new ArrayList<>();
                    Map jobMap;
                    Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
                    if (jenkinsDataMap.get("root") instanceof String) {
                        continue;
                    }
                    Map rootMap = (Map) jenkinsDataMap.get("root");
                    List<Map> jenkinsJobList = new ArrayList<>();
                    //List<Map> jenkinsBuildList;


                    if (rootMap.get("job") instanceof Map) {
                        jenkinsJobList.add((Map) rootMap.get("job"));
                    } else if (rootMap.get("job") instanceof List) {
                        jenkinsJobList.addAll((List) rootMap.get("job"));
                    }
                    for (Map jenkinsJob : jenkinsJobList) {
                        jobMap = new HashMap();
                        jenkinsJobName = (String) jenkinsJob.get("name");
                        if (jenkinsJobName != null) {
                            String[] name = jenkinsJobName.split("_", 2);
                            if (name.length == 2) {
                                jobMap.put("tenant", name[0]);
                                jobMap.put("name", name[1]);
                            }
                        }
                        Map lastBuildMap = (Map) jenkinsJob.get("lastBuild");
                        if (lastBuildMap != null) {
                            if ("false".equalsIgnoreCase((String) lastBuildMap.get("building"))) {
                                jobMap.put("last_build_status", lastBuildMap.get("result"));
                            } else {
                                jobMap.put("last_build_status", "BUILDING");
                            }
                            if (lastBuildMap.get("timestamp") != null) {
                                jobMap.put("last_build_time", df.format(new Timestamp(Long.valueOf((String) lastBuildMap.get("timestamp")))));
                            }
                            if (lastBuildMap.get("number") != null) {
                                jobMap.put("last_build_number", lastBuildMap.get("number"));
                            }
                        } else {
                            jobMap.put("last_build_status", "NOTBUILT");
                            jobMap.put("last_build_time", "");
                        }
                        currentStatus.append(jobMap.get("last_build_status"));

//                        jenkinsBuildList = new ArrayList<>();
//                        int successNum = 0;
//                        int failNum = 0;
//                        if (jenkinsJob.get("build") instanceof Map) {
//                            jenkinsBuildList.add((Map) jenkinsJob.get("build"));
//                        } else if (jenkinsJob.get("build") instanceof List) {
//                            jenkinsBuildList.addAll((List) jenkinsJob.get("build"));
//                        }
//                        for (Object jenkinsBuild : jenkinsBuildList) {
//                            if (jenkinsBuild instanceof String) {
//                                continue;
//                            } else {
//                                Map jenkinsBuildMap = (Map) jenkinsBuild;
//                                if (jenkinsBuildMap.get("result") != null) {
//                                    if ("SUCCESS".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
//                                        successNum++;
//                                    } else if ("Failure".equalsIgnoreCase((String) jenkinsBuildMap.get("result"))) {
//                                        failNum++;
//                                    }
//                                }
//                            }
//                        }
//                        jobMap.put("success_num", successNum);
//                        jobMap.put("fail_num", failNum);


                        List<Job> dbJobList = jobMapper.select(tenant, (String) jobMap.get("name"), null);
                        if (dbJobList != null && dbJobList.size() == 1) {
                            Job dbJob = dbJobList.get(0);
                            jobMap.put("id", dbJob.getId());
                        } else {
                            continue;
                        }

                        jobList.add(jobMap);
                    }
                    if(!currentStatus.toString().equals(lastStatus.toString())){
                        session.sendMessage(new TextMessage(JsonUtil.convertToJson(ActionReturnUtil.returnSuccessWithData(jobList))));
                    }
                    lastStatus = currentStatus;
                }
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

    public boolean deleteView() {
        return true;
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

    private JobBuild jobStatusSync(Job job, Integer buildNum){
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
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/api/xml", null, params, false);
        //ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/logText/progressiveHtml", null, null, false);
        ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/consoleText", null, null, false);
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




    private void notification(Job job, Integer buildNum){
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
                    helper.setSubject(MimeUtility.encodeText("CICD构建通知_" + job.getName() + "_" + jobBuild.getBuildNum(), MimeUtility.mimeCharset("gb2312"), null));
                    Map dataModel = new HashMap<>();
                    Cluster cluster = clusterService.findClusterByTenantId(job.getTenantId());
                    dataModel.put("url", webUrl + "/#/cicd/process/"+job.getId());
                    dataModel.put("jobName",job.getName());
                    dataModel.put("status",jobBuild.getStatus());
                    dataModel.put("time",new Date());
                    dataModel.put("startTime",jobBuild.getStartTime());
                    dataModel.put("duration", DateUtil.getDuration(jobBuild.getDuration()));
                    StageBuild stageBuildCondition = new StageBuild();
                    stageBuildCondition.setJobId(job.getId());
                    stageBuildCondition.setBuildNum(buildNum);
                    List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
                    List stageBuildMapList = new ArrayList<>();
                    for(StageBuild stageBuild : stageBuildList){
                        Map stageBuildMap = new HashMap<>();
                        Stage stage = stageMapper.queryById(stageBuild.getStageId());
                        stageBuildMap.put("name",stage.getStageName());
                        stageBuildMap.put("status",stageBuild.getStatus());
                        stageBuildMap.put("startTime", stageBuild.getStartTime());
                        stageBuildMap.put("duration", DateUtil.getDuration(stageBuild.getDuration()));
                        stageBuildMapList.add(stageBuildMap);
                    }
                    dataModel.put("stageBuildList",stageBuildMapList);
                    helper.setText(TemplateUtil.generate("notification.ftl",dataModel), true);
                    ClassLoader classLoader = MailUtil.class.getClassLoader();
                    InputStream inputStream = classLoader.getResourceAsStream("icon-info.png");
                    byte[] bytes = MailUtil.stream2byte(inputStream);
                    helper.addInline("icon-info", new ByteArrayResource(bytes), "image/png");
                    inputStream = classLoader.getResourceAsStream("icon-status.png");
                    bytes = MailUtil.stream2byte(inputStream);
                    helper.addInline("icon-status", new ByteArrayResource(bytes), "image/png");
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


    public static void main(String[] args) throws Exception {



    }

}
