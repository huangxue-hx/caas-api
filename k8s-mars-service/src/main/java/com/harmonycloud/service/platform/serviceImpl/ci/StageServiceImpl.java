package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.enumm.DockerfileTypeEnum;
import com.harmonycloud.common.enumm.StageTemplateTypeEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.ci.*;
import com.harmonycloud.dao.ci.bean.*;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.constant.Constant;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.platform.service.ci.StageService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by anson on 17/7/13.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StageServiceImpl implements StageService {

    @Autowired
    HarborClient harborClient;

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

    @Value("#{propertiesReader['api.url']}")
    private String apiUrl;


    @Override
    public ActionReturnUtil addStage(StageDto stageDto) throws Exception{
        //increace order for all stages behind this stage
        stageMapper.increaseStageOrder(stageDto.getJobId(), stageDto.getStageOrder());
        Stage stage = stageDto.convertToBean();
        stage.setCreateTime(new Date());
        stage.setUpdateTime(new Date());
        stageMapper.insertStage(stage);

        if(StageTemplateTypeEnum.CODECHECKOUT.ordinal() == stageDto.getStageTemplateType()){
            createOrUpdateCredential(stage);
        }
        Job job = jobMapper.queryById(stageDto.getJobId());
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        String body = generateJobBody(job);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        if(!result.isSuccess()){
            throw new Exception("创建步骤失败。");
        }
        Map data = new HashMap<>();
        data.put("id",stage.getId());
        return ActionReturnUtil.returnSuccessWithData(data);
    }



    @Override
    public ActionReturnUtil updateStage(StageDto stageDto) throws Exception{

        Stage stage = stageDto.convertToBean();
        stage.setUpdateTime(new Date());

        stageMapper.updateStage(stage);

        if(StageTemplateTypeEnum.CODECHECKOUT.ordinal() == stageDto.getStageTemplateType()) {
            createOrUpdateCredential(stage);
        }
        Job job = jobMapper.queryById(stageDto.getJobId());
        String jenkinsJobName = job.getTenant() + "_" + job.getName();

        String body = generateJobBody(job);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        return result;
    }

    @Override
    public ActionReturnUtil deleteStage(Integer id) throws Exception {
        Stage stage = stageMapper.queryById(id);
        stageMapper.deleteStage(id);
        stageMapper.decreaseStageOrder(stage.getJobId(), stage.getStageOrder());
        if(StageTemplateTypeEnum.CODECHECKOUT.ordinal() == stage.getStageTemplateType()) {
            deleteCredentials(stage);
        }
        return ActionReturnUtil.returnSuccess();
    }

    @Override
    public ActionReturnUtil stageDetail(Integer id) throws Exception {
        Stage stage = stageMapper.queryById(id);
        StageDto stageDto = new StageDto();
        if(null == stage){
            stageDto = null;
        }else {
            stageDto.convertFromBean(stage);
        }
        StageType stageType = stageTypeMapper.queryById(stageDto.getStageTypeId());
        stageDto.setStageTypeName(stageType.getName());
        return ActionReturnUtil.returnSuccessWithData(stageDto);
    }

    @Override
    public ActionReturnUtil listStageType(String tenantId) throws Exception{
        List<StageType> stageTypeList = stageTypeMapper.queryByTenantId(tenantId);
        return ActionReturnUtil.returnSuccessWithData(stageTypeList);
    }

    @Override
    public ActionReturnUtil addStageType(StageType stageType) {
        stageType.setUserDefined(true);
        stageType.setTemplateType(3);
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
    public ActionReturnUtil getBuildList(Integer id) {
        List stageBuildMapList = new ArrayList<>();
        StageBuild stageBuildCondition = new StageBuild();
        stageBuildCondition.setStageId(id);
        List<StageBuild> stageBuildList = stageBuildMapper.queryByObject(stageBuildCondition);
        for(StageBuild stageBuild:stageBuildList) {
            Map stageBuildMap = new HashMap<>();
            stageBuildMap.put("buildStatus", stageBuild.getStatus());
            stageBuildMap.put("buildNum", stageBuild.getBuildNum());
            stageBuildMap.put("buildTime", stageBuild.getStartTime());
            stageBuildMap.put("duration", stageBuild.getDuration());
            stageBuildMap.put("log", stageBuild.getLog());
            stageBuildMapList.add(stageBuildMap);
        }
        return ActionReturnUtil.returnSuccessWithData(stageBuildMapList);
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
    public ActionReturnUtil listDeployImage(Integer jobId, Integer stageOrder) {
        List<Stage> stageList = stageMapper.queryByJobId(jobId);
        List deployImageList = new ArrayList<>();
        for(Stage stage : stageList){
            if(stage.getStageOrder() < stageOrder && StageTemplateTypeEnum.IMAGEBUILD.ordinal() == stage.getStageTemplateType()){
                deployImageList.add(stage.getHarborProject() +"/"+ stage.getImageName());
            }
        }
        return ActionReturnUtil.returnSuccessWithData(deployImageList);
    }

    @Override
    public List<Map> getStageBuildFromJenkins(Job job, Integer buildNum) throws Exception {
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/wfapi/describe", null, null, false);
        if(result.isSuccess()) {
            String data = (String) result.get("data");
            Map dataMap = JsonUtil.convertJsonToMap(data);
            return (List<Map>) dataMap.get("stages");
        }else{
            throw new Exception("获取构建信息失败。");
        }
    }

    @Override
    public void stageBuildSync(Job job, Integer buildNum, Map stageMap){
        StageBuild stageBuild = new StageBuild();
        stageBuild.setJobId(job.getId());
        stageBuild.setBuildNum(buildNum);
        stageBuild.setStatus((String)stageMap.get("status"));
        stageBuild.setStartTime(new Timestamp((Long)stageMap.get("startTimeMillis")));
        stageBuild.setDuration(String.valueOf(stageMap.get("durationMillis")));
        stageBuild.setLog(getStageBuildLogFromJenkins(job, buildNum, (String)stageMap.get("id")));
        stageBuildMapper.updateByStageNameAndBuildNum(stageBuild, (String)stageMap.get("name"));
    }


    @Override
    public void getStageLogWS(WebSocketSession session, Integer id, Integer buildNum) {
        String existingLog = "";
        Stage stage = stageMapper.queryById(id);
        Job job = jobMapper.queryById(stage.getJobId());
        try {
            while(session.isOpen()) {
                List<Map> stageMapList = getStageBuildFromJenkins(job, buildNum);
                if(stageMapList.size() >= stage.getStageOrder()) {
                    Map stageMap = stageMapList.get(stage.getStageOrder() - 1);
                    String log = getStageBuildLogFromJenkins(job, buildNum, (String)stageMap.get("id"));
                    String newLog;
                    if(!StringUtils.isBlank(newLog = log.replaceFirst(existingLog, ""))) {
                        existingLog = log;
                        session.sendMessage(new TextMessage(newLog));
                    }
                    if(!Constant.PIPELINE_STATUS_INPROGRESS.equals(stageMap.get("status"))){
                        break;
                    }
                    Thread.sleep(2000);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(session.isOpen()){
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getStageBuildLogFromJenkins(Job job, Integer buildNum, String stageNodeId){
        String jenkinsJobName = job.getTenant() + "_" + job.getName();
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/execution/node/" + stageNodeId + "/wfapi/describe", null, null, false);
        if(result.isSuccess()){
            String data = (String)result.get("data");
            Map dataMap = JsonUtil.convertJsonToMap(data);
            List<Map> stageFlowNodeMapList = (List<Map>)dataMap.get("stageFlowNodes");
            StringBuilder log = new StringBuilder();
            for(Map stageFlowNodeMap : stageFlowNodeMapList){
                result = HttpJenkinsClientUtil.httpGetRequest("/job/" + jenkinsJobName + "/" + buildNum + "/execution/node/" + stageFlowNodeMap.get("id") + "/wfapi/log", null, null, false);
                if(result.isSuccess()){
                    data = (String)result.get("data");
                    dataMap = JsonUtil.convertJsonToMap(data);
                    if(null != dataMap.get("text")){
                        log.append(dataMap.get("text"));
                    }
                }
            }
            return(log.toString());
        }
        return null;
    }

    private String generateJobBody(Job job) throws Exception{
        Map dataModel = new HashMap<>();
        List<Stage> stageList = stageMapper.queryByJobId(job.getId());
        dataModel.put("job", job);
        dataModel.put("stageList", stageList);
        dataModel.put("script", generateScript(job, stageList));
        return TemplateUtil.generate("jobConfig.ftl", dataModel);
    }

    private String generateScript(Job job, List<Stage> stageList) throws Exception {
        Map dataModel = new HashMap();
        dataModel.put("harborHost", harborClient.getHost());
        List<StageDto> stageDtoList = new ArrayList<>();
        Map<Integer, DockerFile> dockerFileMap = new HashedMap();
        for(Stage stage:stageList){
            StageDto newStageDto = new StageDto();
            newStageDto.convertFromBean(stage);
            if(StageTemplateTypeEnum.IMAGEBUILD.ordinal() == newStageDto.getStageTemplateType()){
                if(DockerfileTypeEnum.PLATFORM.ordinal() == newStageDto.getDockerfileType()){
                    DockerFile dockerFileCondition = new DockerFile();
                    dockerFileCondition.setId(newStageDto.getDockerfileId());
                    DockerFile dockerFile = dockerFileMapper.selectDockerFile(dockerFileCondition);
                    dockerFile.setContent(StringEscapeUtils.escapeJava(dockerFile.getContent()));
                    dockerFileMap.put(stage.getStageOrder(), dockerFile);
                }
            }
            if(StageTemplateTypeEnum.CODECHECKOUT.ordinal() == newStageDto.getStageTemplateType()){
                BuildEnvironment buildEnvironment = buildEnvironmentMapper.queryById(newStageDto.getBuildEnvironment());
                newStageDto.setBuildEnvironment(buildEnvironment.getImage());
            }
            stageDtoList.add(newStageDto);
        }
        dataModel.put("apiUrl", apiUrl);
        dataModel.put("job", job);
        dataModel.put("dockerFileMap", dockerFileMap);
        dataModel.put("stageList", stageDtoList);
        String script = null;

        try {
            script = TemplateUtil.generate("pipeline.ftl", dataModel);
        }catch(Exception e){
            e.printStackTrace();
        }
        return script;
    }

    private void createOrUpdateCredential(Stage stage) throws Exception{
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/credentials/store/system/domain/_/credential/" + stage.getId() + "/", null, null, false);
        if(result.isSuccess()){
            updateCredentials(stage);
        }else{
            createCredentials(stage);
        }
    }

    private void createCredentials(Stage stage) throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> credentialsMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        params.put("_.scope","scope:GLOBAL");
        params.put("_.username", stage.getCredentialsUsername());
        params.put("_.password", stage.getCredentialsPassword());
        params.put("_.id", stage.getId());
        credentialsMap.put("scope", "GLOBAL");
        credentialsMap.put("username", stage.getCredentialsUsername());
        credentialsMap.put("password", stage.getCredentialsPassword());
        credentialsMap.put("id", stage.getId());
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

    private void updateCredentials(Stage stage) throws Exception {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonMap = new HashMap<>();
        params.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        params.put("_.scope","GLOBAL");
        params.put("_.username", stage.getCredentialsUsername());
        params.put("_.password", stage.getCredentialsPassword());
        params.put("_.id", stage.getId());
        jsonMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        jsonMap.put("scope","GLOBAL");
        jsonMap.put("username", stage.getCredentialsUsername());
        jsonMap.put("password", stage.getCredentialsPassword());
        jsonMap.put("id", stage.getId());
        params.put("json", JsonUtil.convertToJson(jsonMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stage.getId() + "/updateSubmit", null, params, null, 302);
        if (!result.isSuccess()) {
            throw new Exception();
        }
    }

    private void deleteCredentials(Stage stage) throws Exception {
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stage.getId() + "/doDelete", null, null, null, 302);
    }



}
