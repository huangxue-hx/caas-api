package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dto.cicd.JobDto;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import com.harmonycloud.service.platform.socket.SystemWebSocketHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

/**
 * Created by anson on 17/5/31.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class JobServiceImpl implements JobService {
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

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dfForTag = new SimpleDateFormat("yyyyMMddHHmmss");
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
            String script = TemplateUtil.generate("pipeline.ftl", dataModel);
            dataModel.put("script", script);
            String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        }else{
            throw new Exception();
        }

        return ActionReturnUtil.returnSuccess();
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
    public ActionReturnUtil getJobList(String tenantName) {
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
    public ActionReturnUtil getJobDetail(Integer id) {
        Job dbJob = jobMapper.queryById(id);

        String jenkinsJobName = dbJob.getTenant() + "_" + dbJob.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "name,builds[number,building,result,duration,timestamp],lastBuild[number,building,result,duration,timestamp]");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/api/xml", null, params, false);
        if (result.isSuccess()) {
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map job = new HashMap();
            Map buildMap;
            List<Map> jenkinsBuildList = new ArrayList<>();
            List buildList = new ArrayList<>();
            job.put("jobName", dbJob.getName());
            job.put("tenant", dbJob.getTenant());

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


//            List<Job> dbJobList = jobMapper.select(tenantName, jobName, null);
//            if(dbJobList != null && dbJobList.size()==1){
//                Job dbJob = dbJobList.get(0);
//                job.put("projectType", dbJob.getProjectType());
//                job.put("buildType", dbJob.getBuildType());
//                job.put("repositoryType", dbJob.getRepositoryType());
//
//            }

            JobBuild jobBuildCondition = new JobBuild();
            jobBuildCondition.setJobId(dbJob.getId());
            jobBuildCondition.setBuildNum(dbJob.getLastBuildNum());
            List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);
            if(null != jobBuildList && jobBuildList.size() == 1) {
                JobBuild jobBuild = jobBuildList.get(0);
                job.put("lastBuildStatus", jobBuild.getStatus());
                job.put("lastBuildTime", jobBuild.getStartTime());
                job.put("lastBuildDuration", jobBuild.getDuration());
            }else {
                job.put("lastBuildStatus", "NOTBUILT");
                job.put("lastBuildTime", null);
                job.put("lastBuildDuration", null);
            }


            List<Map> stageMapList = new ArrayList<>();

            //get stage type info of stages for the job
            List<Stage> stageList = stageMapper.queryByJobId(id);
            List<StageType> stageTypeList = stageTypeMapper.queryByTenantId(dbJob.getTenant());
            Map stageTypeMap = new HashMap<>();
            for(StageType stageType : stageTypeList){
                stageTypeMap.put(stageType.getId(),stageType.getName());
            }

            //get last build info of stages for the job
            StageBuild stageBuildCondition = new StageBuild();
            stageBuildCondition.setJobId(dbJob.getId());
            stageBuildCondition.setBuildNum(dbJob.getLastBuildNum());
            List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
            Map stageBuildMap = new HashMap<>();
            for(StageBuild stageBuild : stageBuildList){
                stageBuildMap.put(stageBuild.getStageId(),stageBuild);
            }

            for(Stage stage:stageList){
                Map stageMap = new HashMap<>();
                stageMap.put("id",stage.getId());
                stageMap.put("stageName",stage.getStageName());
                stageMap.put("stageType",stageTypeMap.get(stage.getStageTypeId()));
                stageMap.put("stageOrder",stage.getStageOrder());

                StageBuild stageBuild = (StageBuild)stageBuildMap.get(stage.getId());
                if(stageBuild == null){
                    stageMap.put("lastBuildStatus", "NOTBUILT");
                    stageMap.put("lastBuildTime", null);
                    stageMap.put("lastBuildDuration", null);
                }else {
                    stageMap.put("lastBuildStatus", stageBuild.getStatus());
                    stageMap.put("lastBuildTime", stageBuild.getStartTime());
                    stageMap.put("lastBuildDuration", stageBuild.getDuration());
                }
                stageMapList.add(stageMap);
            }

            job.put("stageList",stageMapList);

            return ActionReturnUtil.returnSuccessWithData(job);
        }
        else {
            return ActionReturnUtil.returnError();
        }
    }

    @Override
    public ActionReturnUtil build(Integer id) {
        Job job = jobMapper.queryById(id);
        String jenkinsJobName = job.getTenant() + "_" + job.getName();

        Map<String, Object> params = new HashMap<>();
        List<Stage> stageList = stageMapper.queryByJobId(id);
        for(Stage stage:stageList){
            String tag;
            if("0".equals(stage.getImageTagType())){
                tag = dfForTag.format(new Date());
            }else if("1".equals(stage.getImageTagType())){
                tag = stage.getImageBaseTag();
            }else if("2".equals(stage.getImageTagType())){
                tag = stage.getImageTag();
            }else{
                continue;
            }
            params.put("tag" + stage.getStageOrder(), tag);
        }
        params.put("delay", "0sec");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/buildWithParameters", null, params, null, null);
        if (result.isSuccess()) {
            params = new HashMap<>();
            params.put("tree","number,timestamp");
            result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/lastBuild/api/xml", null, params, false);
            if(result.isSuccess()){
                Map body = XmlUtil.parseXmlStringToMap((String) result.get("data"));
                Map root = (Map)body.get("workflowRun");
                Integer lastBuildNumber = Integer.valueOf((String) root.get("number"));
                Date startTime = new Timestamp(Long.valueOf((String) root.get("timestamp")));
                jobMapper.updateLastBuildNum(id, lastBuildNumber);
                JobBuild jobBuild = new JobBuild();
                jobBuild.setJobId(id);
                jobBuild.setBuildNum(lastBuildNumber);
                jobBuild.setStatus("BUILDING");
                jobBuild.setStartTime(startTime);
                jobBuild.setStartUser((String)session.getAttribute("username"));
                jobBuildMapper.insert(jobBuild);

                for(Stage stage : stageList){
                    StageBuild stageBuild = new StageBuild();
                    stageBuild.setJobId(id);
                    stageBuild.setStageId(stage.getId());
                    stageBuild.setBuildNum(lastBuildNumber);
                    stageBuild.setStatus("WAITING");
                    stageBuildMapper.insert(stageBuild);
                }
            }
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnError();
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
    public ActionReturnUtil deleteBuild(String jobName, String tenantName, String buildNum) {
        String jenkinsJobName = tenantName + "_" + jobName;
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/" + buildNum + "/doDelete", null, null, null, null);
        if (result.isSuccess()) {
            return ActionReturnUtil.returnSuccess();
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil credentialsValidate(String repositoryType, String repositoryUrl, String username, String password) {
        return ScmUtil.checkCredentials(repositoryType, repositoryUrl, username, password);
    }

    @Override
    public ActionReturnUtil getBuildDetail(String tenantName, String jobName, String buildNum) {
        String jenkinsJobName = tenantName + "_" + jobName;
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
    }

    @Override
    public void sendMessage(WebSocketSession session, String jobName, String buildNum) {
        String start = "0";
        String moreData = "true";
        try {
            while (moreData != null) {
                moreData = null;
                Map params = new HashMap<>();
                params.put("start", start);
                ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jobName + "/" + buildNum + "/logText/progressiveHtml", null, params, true);
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
                        Thread.sleep(1000);

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                session.close();
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
        notificationMap.put("sucessNotification", jobDto.isSuccessNotification());
        notificationMap.put("failNotification", jobDto.isFailNotification());
        notificationMap.put("mail", jobDto.getMail());
        return ActionReturnUtil.returnSuccessWithData(notificationMap);
    }

    @Override
    public ActionReturnUtil updateNotification(JobDto jobDto) {
        Job job = jobDto.convertToBean();
        String username = (String)session.getAttribute("username");
        job.setUpdateUser(username);
        job.setUpdateTime(new Date());
        jobMapper.updateNotification(job);
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
        triggerMap.put("cronExpForPollScm", job.getCronExpForPollScm());
        return ActionReturnUtil.returnSuccessWithData(triggerMap);
    }

    @Override
    public ActionReturnUtil updateTrigger(Job job) {
        String username = (String)session.getAttribute("username");
        job.setUpdateUser(username);
        job.setUpdateTime(new Date());
        jobMapper.updateTrigger(job);
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public void postBuild(Integer id, Integer buildNum) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                String status = statusSync(id, buildNum);
                notification(id, buildNum, status);
            }
        };
        executor.execute(worker);


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

    private String statusSync(Integer id, Integer buildNum){
        Job job = jobMapper.queryById(id);

        JobBuild jobBuildCondition = new JobBuild();
        jobBuildCondition.setJobId(id);
        jobBuildCondition.setBuildNum(buildNum);
        List<JobBuild> jobBuildList = jobBuildMapper.queryByObject(jobBuildCondition);
        JobBuild jobBuild = new JobBuild();
        if(null != jobBuildList && jobBuildList.size()==1){
            jobBuild = jobBuildList.get(0);
        }else{
            jobBuild.setJobId(id);
            jobBuild.setBuildNum(buildNum);
            jobBuildMapper.insert(jobBuild);
        }

        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "building,timestamp,result,duration,number");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/api/xml", null, params, false);
        ActionReturnUtil logResult = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/logText/progressiveHtml", null, null, false);
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
        return jobBuild.getStatus();

    }

    private void notification(Integer id, Integer buildNum, String status){
        Job job = jobMapper.queryById(id);
        if(job.isNotification()) {
            if (job.isFailNotification() && job.isSuccessNotification() || job.isFailNotification() && "FAILURE".equals(status) || job.isSuccessNotification() && "SUCCESS".equals(status)) {
                JobDto jobDto = new JobDto();
                jobDto.convertFromBean(job);
                jobDto.getMail();
            }
        }
    }

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
    public static void main(String[] args) throws Exception {
        /*Config config = new ConfigBuilder().withMasterUrl("https://10.10.101.143:6443").withTrustCerts(true).withOauthToken("330957b867a3462ea457bec41410624b1").build();
        KubernetesClient client = new DefaultKubernetesClient(config);
//        List<Namespace> nameSpaceList =client.namespaces().list().getItems();
//
//        Namespace ns = nameSpaceList.get(0);
//
//        System.out.println(ns.getMetadata().getName());
        Container c = new Container();
        Map m=new HashMap();
       m.put("cpu","0");
        m.put("memory","500Mi");

        c.setImage("nginx");
        c.setName("slave");
        c.setResources(new ResourceRequirementsBuilder().withLimits(m).build());
        Pod p = new PodBuilder().withNewMetadata().withName("slave").withNamespace("gywtesttenant-ping").endMetadata()
                .withNewSpec().withContainers(c).endSpec().build();
        client.pods().create(p);*/
//        //client.pods().delete(p);
//
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//
//        ExecWatch w = client.pods().inNamespace("tenanta-aaaaaa").withName("slave").readingInput(System.in)
//                .writingOutput(System.out)
//                .writingError(System.err).withTTY().usingListener(new SimpleListener())
//                .exec();
//        InputStreamPumper pump = new InputStreamPumper(w.getOutput(), new SystemOutCallback());
//        try{ executorService.submit(pump);
//
//        w.getInput().write("ls -al\n".getBytes());
//        } catch (Exception e) {
//            throw KubernetesClientException.launderThrowable(e);
//        } finally {
//            executorService.shutdownNow();
//            w.close();
//            pump.close();
//        }

        StageDto stage = new StageDto();
        List cl = new ArrayList<>();
        //cl.add("test1");
        //cl.add("test2");
        stage.setTenant("test");
        stage.setJobName("test");
        stage.setJobId(1);
        stage.setStageOrder(4);
        stage.setRepositoryType("git");
        //stage.setStageType(StageTemplateTypeEnum.IMAGEBUILD);
        stage.setStageOrder(1);
        stage.setStageName("test");
        stage.setRepositoryUrl("http://1111");
        stage.setRepositoryBranch("master");
        stage.setHarborProject("pj");
        stage.setImageName("test");
        stage.setCommand(cl);

        //new JobServiceImpl().addStage(stage);


        //System.out.println(StageTemplateTypeEnum.DEPLOY.ordinal());




    }
/*
    private static class SystemOutCallback implements Callback<byte[]> {
        @Override
        public void call(byte[] data) {
            System.out.print(new String(data));
        }
    }
*/
}
