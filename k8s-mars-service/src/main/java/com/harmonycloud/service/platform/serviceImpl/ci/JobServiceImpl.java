package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.enumm.ProjectTypeEnum;
import com.harmonycloud.common.enumm.RepositoryTypeEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.JobMapper;
import com.harmonycloud.dao.ci.bean.Job;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.socket.SystemWebSocketHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by anson on 17/5/31.
 */
@Service
public class JobServiceImpl implements JobService {
    @Resource
    SystemWebSocketHandler systemWebSocketHandler;

    @Autowired
    HarborClient harborClient;

    @Autowired
    JobMapper jobMapper;

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ActionReturnUtil createJob(Job job, String username) {
        List<String> jobList = new ArrayList<>();
        String jenkinsJobName = job.getTenant() + "_" + job.getJobName();

        //use the view to represent the user.
        //check if the view exists
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/view/" + username + "_view/api/json", null, null, false);
        if (!result.isSuccess()) {
            //if not, create a view with the username
            createView(username);
        } else {
            //if yes, get existed jobs in the view
            jobList = getViewJobList((String) result.get("data"));
        }
        if(StringUtils.isNotEmpty(job.getCredentialsUsername()) && StringUtils.isNotEmpty(job.getCredentialsPassword())) {
            createCredentials(job);
        }


        // validate jobname
        result = nameValidate(job.getJobName(), job.getTenant());
        if (!result.isSuccess()) {
            return result;
        }

        //create job
        Map<String, Object> params = new HashMap<>();
        params.put("name", jenkinsJobName);
        params.put("mode", "org.jenkinsci.plugins.workflow.job.WorkflowJob");
        params.put("json", JsonUtil.convertToJson(params));
        result = HttpJenkinsClientUtil.httpPostRequest("/createItem", null, params, null, 302);

        try {
            //config job
            if (result.isSuccess()) {
                Map<String, Object> dataModel = new HashMap<>();
                dataModel.put("tag", job.getImageTag());
                dataModel.put("script", generateScript(job));
                String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
                result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
            }

            //add the new job to the user view
            if (result.isSuccess()) {
                jobList.add(jenkinsJobName);
                Collections.sort(jobList);
                updateView(username, jobList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        jobMapper.insertJob(job);

        return ActionReturnUtil.returnSuccess();
    }



    @Override
    public ActionReturnUtil updateJob(Job job) {
        String jenkinsJobName = job.getTenant() + "_" + job.getJobName();
        Map<String, Object> dataModel = new HashMap<>();
        try {
            if(StringUtils.isNotEmpty(job.getCredentialsUsername()) && StringUtils.isNotEmpty(job.getCredentialsPassword())) {
                updateCredentials(job);
            }
            dataModel.put("tag", job.getImageTag());
            dataModel.put("script", generateScript(job));
            String body = null;
            body = TemplateUtil.generate("jobConfig.ftl", dataModel);
            ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        jobMapper.updateJob(job);

        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil deleteJob(String jobName, String tenantName) {
        String jenkinsJobName = tenantName + "_" + jobName;
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/doDelete", null, null, null, 302);
        if (!result.isSuccess()) {
            result.put("message", "not found");
        }else {
            jobMapper.deleteJobByTenantAndJobName(tenantName, jobName);
            deleteCredentials(jenkinsJobName);
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
    public ActionReturnUtil getJobList(String tenantName, String username) {
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "jobs[name,color,lastBuild[number,building,result,timestamp],builds[result]]");
        params.put("wrapper", "root");
        params.put("xpath", "//job/name[starts-with(text(),\"" + tenantName + "_\")]/..");
        ActionReturnUtil result;
        if (StringUtils.isEmpty(username)) {
            result = HttpJenkinsClientUtil.httpGetRequest("/view/all/api/xml", null, params, false);
        } else {
            result = HttpJenkinsClientUtil.httpGetRequest("/view/" + username + "_view/api/xml", null, params, false);
            if(!result.isSuccess()){
                return ActionReturnUtil.returnSuccessWithData(new ArrayList());
            }
        }
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

//            Map dbJobsMap = new HashMap<>();
//            List<Job> dbJobList = jobMapper.select(tenantName, null, username);
//            for(Job job:dbJobList){
//                dbJobsMap.put(job.getJobName(), job);
//            }

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

//                Job dbJob = (Job)dbJobsMap.get(jobMap.get("name"));
                List<Job> dbJobList = jobMapper.select(tenantName, (String)jobMap.get("name"), null);
                if(dbJobList != null && dbJobList.size()==1){
                    Job dbJob = dbJobList.get(0);
                    jobMap.put("harbor_project", dbJob.getHarborProject());
                    jobMap.put("image_name", dbJob.getImageName());
                    jobMap.put("image_tag", dbJob.getImageTag());
                }


                jobList.add(jobMap);
            }
            return ActionReturnUtil.returnSuccessWithData(jobList);
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil getJobDetail(String tenantName, String jobName) {
        String jenkinsJobName = tenantName + "_" + jobName;
        Map<String, Object> params = new HashMap<>();
        params.put("tree", "name,builds[number,building,result,duration,timestamp],lastBuild[number,building,result,duration,timestamp]");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/api/xml", null, params, false);
        if (result.isSuccess()) {
            Map jenkinsDataMap = XmlUtil.parseXmlStringToMap((String) result.get("data"));
            Map job = new HashMap();
            Map buildMap;
            List<Map> jenkinsBuildList = new ArrayList<>();
            List buildList = new ArrayList<>();
            job.put("jobName", jobName);
            job.put("tenant", tenantName);

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

            List<Job> dbJobList = jobMapper.select(tenantName, jobName, null);
            if(dbJobList != null && dbJobList.size()==1){
                Job dbJob = dbJobList.get(0);
                job.put("projectType", dbJob.getProjectType());
                job.put("buildType", dbJob.getBuildType());
                job.put("repositoryType", dbJob.getRepositoryType());
                job.put("repositoryUrl", dbJob.getRepositoryUrl());
                job.put("repositoryBranch", dbJob.getRepositoryBranch());
                job.put("baseImage", dbJob.getBaseImage());
                job.put("imageName", dbJob.getImageName());
                job.put("imageTag", dbJob.getImageTag());
                job.put("credentialsUsername", dbJob.getCredentialsUsername());
                job.put("credentialsPassword", dbJob.getCredentialsPassword());
                job.put("harborProject",dbJob.getHarborProject());
            }

            return ActionReturnUtil.returnSuccessWithData(job);
        }
        return ActionReturnUtil.returnError();
    }

    @Override
    public ActionReturnUtil build(String jobName, String tenantName, String tag) {
        String jenkinsJobName = tenantName + "_" + jobName;
        Map<String, Object> params = new HashMap<>();
        params.put("tag", tag);
        params.put("delay", "0sec");
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/buildWithParameters", null, params, null, null);
        if (result.isSuccess()) {
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

    private boolean updateView(String username, List jobList) throws IOException {
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

    private boolean createCredentials(Job job) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> credentialsMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        params.put("_.scope","scope:GLOBAL");
        params.put("_.username", job.getCredentialsUsername());
        params.put("_.password", job.getCredentialsPassword());
        params.put("_.id", job.getTenant() + "_" + job.getJobName());
        credentialsMap.put("scope", "GLOBAL");
        credentialsMap.put("username", job.getCredentialsUsername());
        credentialsMap.put("password", job.getCredentialsPassword());
        credentialsMap.put("id", job.getTenant() + "_" + job.getJobName());
        credentialsMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        credentialsMap.put("$class", "com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        tempMap.put("","0");
        tempMap.put("credentials", credentialsMap);
        params.put("json", JsonUtil.convertToJson(tempMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/createCredentials", null, params, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }

    private boolean updateCredentials(Job job){
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonMap = new HashMap<>();
        params.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        params.put("_.scope","GLOBAL");
        params.put("_.username", job.getCredentialsUsername());
        params.put("_.password", job.getCredentialsPassword());
        params.put("_.id", job.getTenant() + "_" + job.getJobName());
        jsonMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        jsonMap.put("scope","GLOBAL");
        jsonMap.put("username", job.getCredentialsUsername());
        jsonMap.put("password", job.getCredentialsPassword());
        jsonMap.put("id", job.getTenant() + "_" + job.getJobName());
        params.put("json", JsonUtil.convertToJson(jsonMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + job.getTenant() + "_" + job.getJobName() + "/updateSubmit", null, params, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }


    private boolean deleteCredentials(String jenkinsJobName) {
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + jenkinsJobName + "/doDelete", null, null, null, 302);
        if (result.isSuccess()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {

    }

}
