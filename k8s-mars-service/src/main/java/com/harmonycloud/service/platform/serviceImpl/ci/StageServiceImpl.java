package com.harmonycloud.service.platform.serviceImpl.ci;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpJenkinsClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.TemplateUtil;
import com.harmonycloud.dao.ci.StageMapper;
import com.harmonycloud.dao.ci.StageTypeMapper;
import com.harmonycloud.dao.ci.bean.Stage;
import com.harmonycloud.dao.ci.bean.StageType;
import com.harmonycloud.dto.cicd.StageDto;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.ci.StageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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



    @Override
    public ActionReturnUtil addStage(StageDto stageDto) throws Exception{
        //increace order for all stages behind this stage
        stageMapper.increaseStageOrder(stageDto.getJobId(), stageDto.getStageOrder());
        Stage stage = stageDto.convertToBean();
        stage.setCreateTime(new Date());
        stage.setUpdateTime(new Date());
        stageMapper.insertStage(stage);

        if(CommonConstant.STAGE_TEMPLATE_COMPILE == stageDto.getStageTemplateType()){
            createOrUpdateCredential(stage);
        }

        String jenkinsJobName = stageDto.getTenant() + "_" + stageDto.getJobName();
        Map dataModel = new HashMap<>();
        dataModel.put("tag", "test");
        dataModel.put("script", generateScript(stageDto));
        String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);

        return result;
    }



    @Override
    public ActionReturnUtil updateStage(StageDto stageDto) throws Exception{

        Stage stage = stageDto.convertToBean();
        stage.setUpdateTime(new Date());

        stageMapper.updateStage(stage);

        if(CommonConstant.STAGE_TEMPLATE_COMPILE == stageDto.getStageTemplateType()) {
            createOrUpdateCredential(stage);
        }

        String jenkinsJobName = stageDto.getTenant() + "_" + stageDto.getJobName();
        Map dataModel = new HashMap();
        dataModel.put("tag", "test");
        dataModel.put("script", generateScript(stageDto));
        String body = TemplateUtil.generate("jobConfig.ftl", dataModel);
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/job/" + jenkinsJobName + "/config.xml", null, null, body, null);
        return result;
    }

    @Override
    public ActionReturnUtil deleteStage(Integer id) throws Exception {
        Stage stage = stageMapper.queryById(id);
        stageMapper.deleteStage(id);
        stageMapper.decreaseStageOrder(stage.getJobId(), stage.getStageOrder());
        deleteCredentials(stage);
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
        return ActionReturnUtil.returnErrorWithData(stageDto);
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
        return ActionReturnUtil.returnSuccess();
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

    private String generateScript(StageDto stageDto) throws Exception {
        Map dataModel = new HashMap();
        dataModel.put("harborHost", harborClient.getHost());
        List<Stage> stageList = stageMapper.queryByJobId(stageDto.getJobId());
        List<StageDto> stageDtoList = new ArrayList<>();
        for(Stage stage:stageList){
            StageDto newStageDto = new StageDto();
            newStageDto.convertFromBean(stage);
            stageDtoList.add(newStageDto);
        }
        dataModel.put("stageList", stageDtoList);
        String script = TemplateUtil.generate("pipeline.ftl", dataModel);
        return script;
    }

    private void createOrUpdateCredential(Stage stage) throws Exception{
        ActionReturnUtil result = HttpJenkinsClientUtil.httpGetRequest("/credentials/store/system/domain/_/credential/" + stage.getTenant() + "_" + stage.getJobName() + "_" + stage.getId() + "/", null, null, false);
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
        params.put("_.id", stage.getTenant() + "_" + stage.getJobName());
        credentialsMap.put("scope", "GLOBAL");
        credentialsMap.put("username", stage.getCredentialsUsername());
        credentialsMap.put("password", stage.getCredentialsPassword());
        credentialsMap.put("id", stage.getTenant() + "_" + stage.getJobName() + "_" + stage.getId());
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
        params.put("_.id", stage.getTenant() + "_" + stage.getJobName());
        jsonMap.put("stapler-class","com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl");
        jsonMap.put("scope","GLOBAL");
        jsonMap.put("username", stage.getCredentialsUsername());
        jsonMap.put("password", stage.getCredentialsPassword());
        jsonMap.put("id", stage.getTenant() + "_" + stage.getJobName());
        params.put("json", JsonUtil.convertToJson(jsonMap));
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stage.getTenant() + "_" + stage.getJobName() + "_" + stage.getId() + "/updateSubmit", null, params, null, 302);
        if (!result.isSuccess()) {
            throw new Exception();
        }
    }

    private void deleteCredentials(Stage stage) throws Exception {
        ActionReturnUtil result = HttpJenkinsClientUtil.httpPostRequest("/credentials/store/system/domain/_/credential/" + stage.getTenant() + "_" + stage.getJobName() + "_" + stage.getId() + "/doDelete", null, null, null, 302);
        if(!result.isSuccess()){
            throw new Exception();
        }
    }



}
