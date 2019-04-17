package com.harmonycloud.service.platform.serviceImpl.harbor;


import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborReplicationService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;


/**
 *
 */
@Service
public class HarborReplicationServiceImpl implements HarborReplicationService {

    @Autowired
    private ClusterService clusterService;
    @Autowired
    private HarborProjectService harborProjectService;
    @Autowired
    private HarborService harborService;
    @Autowired
    private HarborUserService harborUserService;
    @Autowired
    private HttpSession httpSession;

    /**
     * 新建跨harbor同步target
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createTarget(HarborReplicationTarget harborReplicationTarget) throws Exception{
        AssertUtil.notNull(harborReplicationTarget);
        HarborServer harborServer = clusterService.findHarborByHost(harborReplicationTarget.getSourceHarborHost());
        //先检查设置的镜像服务器连接是否成功
        ActionReturnUtil pingResult = pingTarget(harborReplicationTarget.getSourceHarborHost(), harborReplicationTarget);
        if(!pingResult.isSuccess()){
            return pingResult;
        }
        //check target检测是否已经有target存在
        ActionReturnUtil targetResponse = listTargets(harborServer.getHarborHost());
        if (!targetResponse.isSuccess()) {
            return targetResponse;
        }
        if(targetResponse.getData() != null) {
            List<HarborReplicationTarget> targets = (List<HarborReplicationTarget>) targetResponse.getData();
            for(HarborReplicationTarget target : targets){
                if(target.getEndpoint().trim().equalsIgnoreCase(harborReplicationTarget.getEndpoint())){
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.EXIST, DictEnum.HARBOR_TARGET.phrase(),true);
                }
            }
        }

        String url = HarborClient.getHarborUrl(harborServer) + "/api/targets";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");
        harborReplicationTarget.setInsecure(true);    // 不验证
        return HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, convertHarborReplicationTarget(harborReplicationTarget));
    }

    /**
     * 修改跨harbor同步target
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updateTarget(HarborReplicationTarget harborReplicationTarget) throws Exception{
        AssertUtil.notNull(harborReplicationTarget);
        AssertUtil.notNull(harborReplicationTarget.getSourceHarborHost(), DictEnum.HARBOR_HOST);
        AssertUtil.notNull(harborReplicationTarget.getId(), DictEnum.REPLICATION_TARGET_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborReplicationTarget.getSourceHarborHost());
        //先检查设置的镜像服务器连接是否成功
        ActionReturnUtil pingResult = pingTarget(harborReplicationTarget.getSourceHarborHost(), harborReplicationTarget);
        if(!pingResult.isSuccess()){
            return pingResult;
        }
        //check target检测是否已经有target存在
        ActionReturnUtil policyResponse = this.listPoliciesDetail(harborReplicationTarget.getSourceHarborHost(),harborReplicationTarget.getId());
        if(policyResponse.isSuccess() && policyResponse.getData()!=null){
            List<HarborPolicyDetail> policies = (List<HarborPolicyDetail>)policyResponse.getData();
            for(HarborPolicyDetail policy : policies){
                if(policy.getEnabled() == FLAG_TRUE){
                    return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPLICATION_ENABLE);
                }
            }
        }
        String url = HarborClient.getHarborUrl(harborServer) + "/api/targets/" + harborReplicationTarget.getId();
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        return HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, convertHarborReplicationTarget(harborReplicationTarget));

    }

    /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil pingEndpoint(String harborHost, String endpoint,String targetusername,String targetuserpassword)throws Exception{

        AssertUtil.notBlank(endpoint, DictEnum.SERVER_HOST);
        AssertUtil.notBlank(targetusername, DictEnum.USERNAME);
        AssertUtil.notBlank(targetusername, DictEnum.PASSWORD);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/targets/ping";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");

        Map<String, Object> paramsMap = new HashMap<String,Object>();
        paramsMap.put("endpoint", endpoint);
        paramsMap.put("username", targetusername);
        paramsMap.put("password", targetuserpassword);
        paramsMap.put("insecure", true);

        ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers,paramsMap);
        String loginUrl = endpoint + "/login";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("principal", targetusername);
        params.put("password", targetuserpassword);
        CloseableHttpResponse logResponse = HarborHttpsClientUtil.doPostWithLogin(loginUrl, params, null);
        Integer statusCode = logResponse.getStatusLine().getStatusCode();
        if(statusCode == HttpStatus.SC_MOVED_PERMANENTLY){
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HARBOR_PROTOCOL_INVALID);
        }
        return response;
    }
    /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil deleteTarget(String harborHost,Integer targetID) throws Exception{
        AssertUtil.notNull(targetID, DictEnum.REPLICATION_TARGET_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/targets/"+targetID;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        ActionReturnUtil actionReturnUtil = HarborHttpsClientUtil.httpDoDelete(url, null, headers);
        if(!actionReturnUtil.isSuccess() && actionReturnUtil.getData() != null){
            if(actionReturnUtil.getData().toString().contains("used by policies")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPLICATION_TARGET_USING);
            }
        }
        return actionReturnUtil;
    }

    /**
     * 查询跨harbor同步对象
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getTarget(String harborHost,Integer targetID) throws Exception{
        AssertUtil.notNull(targetID, DictEnum.REPLICATION_TARGET_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/targets/"+targetID;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpGetRequest(url, headers,null );
    }

    /**
     * 列举跨harbor同步对象
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listTargets(String harborHost) throws Exception{
        Set<HarborServer> harborServers = new HashSet<>();
        if(StringUtils.isBlank(harborHost)){
            harborServers = harborUserService.getCurrentUserAvailableHarbor();
        }else{
            harborServers.add(clusterService.findHarborByHost(harborHost));
        }
        List<HarborReplicationTarget> allTargets = new ArrayList<>();
        for(HarborServer harborServer : harborServers) {
            String url = HarborClient.getHarborUrl(harborServer) + "/api/targets";
            Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
            ActionReturnUtil listResponse = HarborHttpsClientUtil.httpGetRequest(url, headers, null);
            if (!listResponse.isSuccess()) {
                return listResponse;
            }
            if(listResponse.get("data") == null){
                continue;
            }
            List<HarborReplicationTarget> targets = JSONObject.parseArray(listResponse.get("data").toString(), HarborReplicationTarget.class);
            if(CollectionUtils.isEmpty(targets)){
                continue;
            }
            for(HarborReplicationTarget target : targets){
                target.setSourceHarborHost(harborServer.getHarborHost());
                target.setTargetHarborHost(target.getEndpoint().substring(target.getEndpoint().lastIndexOf("/")+1));
            }
            allTargets.addAll(targets);
        }
        return ActionReturnUtil.returnSuccessWithData(allTargets);

    }

    /**
     * 生成同步任务map
     * @return
     */
    private Map<String, Object> convertHarborReplicationPolicy(HarborReplicationPolicy harborReplicationPolicy) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String,Object>> projectsList = new LinkedList<Map<String,Object>>();
        Map<String, Object> metadata = new HashMap<>();
        List<Map<String,Object>> targetList = new LinkedList<Map<String,Object>>();
        Map<String, Object> trigger = new HashMap<>();
        List<Map<String,Object>> filters = new LinkedList<Map<String,Object>>();
        Integer policyId = 1;//新harbor需要传入id参数,创建后会变为自增值，但创建policy需要传入id值
        map.put("id",policyId);
        if (harborReplicationPolicy != null) {
            if (StringUtils.isNotEmpty(harborReplicationPolicy.getName())) {
                map.put("name", harborReplicationPolicy.getName());
            }
            if(StringUtils.isNotEmpty(harborReplicationPolicy.getTarget_project_name())){
                map.put("target_project_name",harborReplicationPolicy.getTarget_project_name());
            }
            if (StringUtils.isNotEmpty(harborReplicationPolicy.getDescription())) {
                map.put("description", harborReplicationPolicy.getDescription());
            }
            if (harborReplicationPolicy.getHarborProjectId() != null) {
                Map<String, Object> project = new HashMap<>();
                project.put("project_id",harborReplicationPolicy.getHarborProjectId());
                projectsList.add(project);
            }
            if (harborReplicationPolicy.getTargetId() != null) {
                Map<String, Object> target = new HashMap<>();
                target.put("id",harborReplicationPolicy.getTargetId());
                targetList.add(target);
            }
            if(harborReplicationPolicy.getRepositories() != null){
                Map<String,Object> filter = new HashMap<String,Object>();
                filter.put("kind","repository");
                filter.put("value",harborReplicationPolicy.getRepositories());
                filters.add(filter);
            }
            if(harborReplicationPolicy.getTags() != null){
                Map<String,Object> filter = new HashMap<String,Object>();
                filter.put("kind","tag");
                filter.put("value",harborReplicationPolicy.getTags());
                filters.add(filter);
            }
            if(harborReplicationPolicy.getLabels() != null){
                List<Integer> labels = harborReplicationPolicy.getLabels();
                for(Integer label : labels){
                    Map<String,Object> filter = new HashMap<String,Object>();
                    filter.put("kind","label");
                    filter.put("value",label);
                    filters.add(filter);
                }
            }
            if(harborReplicationPolicy.getTrigger() != null){
                trigger.put("kind",harborReplicationPolicy.getTrigger());
                if(harborReplicationPolicy.getTrigger().equals("Scheduled")){
                    Map<String,Object> scheduleParam = new HashMap<String,Object>();
                    scheduleParam.put("type",harborReplicationPolicy.getScheduled().getType());
                    if(harborReplicationPolicy.getScheduled().getType().equals("Weekly")){
                        scheduleParam.put("weekday",harborReplicationPolicy.getScheduled().getWeekday());
                    }else {
                        scheduleParam.put("weekday",0);
                    }
                    Integer offtime = 0;
                    Integer hour = harborReplicationPolicy.getScheduled().getHour();
                    Integer minute = harborReplicationPolicy.getScheduled().getMinute();
                    if(hour >= 8){
                        offtime = (hour - 8) * 3600 + minute * 60;
                    }else {
                        offtime = (hour+16) * 3600 + minute * 60;
                    }
                    scheduleParam.put("offtime",offtime);

                    trigger.put("schedule_param",scheduleParam);
                }
            }
            if(harborReplicationPolicy.getReplicateNow() != null){
                map.put("replicate_existing_image_now",harborReplicationPolicy.getReplicateNow());
            }
            map.put("filters",filters);
            map.put("trigger",trigger);
            map.put("projects",projectsList);
            map.put("targets",targetList);
            map.put("replicate_deletion",harborReplicationPolicy.getReplicateDeletion());
            map.put("error_job_count",0);
        }
        return map;
    }


    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createPolicy( HarborReplicationPolicy harborReplicationPolicy) throws Exception{
        AssertUtil.notNull(harborReplicationPolicy);
        HarborServer harborServer = clusterService.findHarborByHost(harborReplicationPolicy.getHarborHost());
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");
        //return HttpsClientUtil.httpPostRequestForHarborCreate(url, headers, convertHarborReplicationPolicy(harborReplicationPolicy));
        ActionReturnUtil result = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, convertHarborReplicationPolicy(harborReplicationPolicy));
        if ((boolean) result.get("success") != true){
            if(result.get("data").toString().contains("policy already exists with the same project and target")) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPLICATION_EXIST);
            }
        }
        return result;
    }
    /**
     * 修改跨harbor同步任务
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updatePolicy( HarborReplicationPolicy harborReplicationPolicy,String policyId) throws Exception{
        AssertUtil.notNull(harborReplicationPolicy);
        AssertUtil.notNull(policyId,DictEnum.REPLICATION_POLICY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborReplicationPolicy.getHarborHost());
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");
        ActionReturnUtil result = HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, convertHarborReplicationPolicy(harborReplicationPolicy));

        return result;
    }
    /**
     * 获取跨harbor同步任务详情
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil getPolicyDetail(String harborHost,String policyId)throws Exception{
        AssertUtil.notNull(harborHost);
        AssertUtil.notNull(policyId,DictEnum.REPLICATION_POLICY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyId;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");
        ActionReturnUtil result = HarborHttpsClientUtil.httpGetRequest(url, headers, null);

        return result;
    }
    /**
     * 删除跨harbor同步任务
     * @return
     * @throws Exception
     */
    public ActionReturnUtil deletePolicy(String harborHost, Integer policyID)throws Exception{
        AssertUtil.notNull(policyID, DictEnum.REPLICATION_POLICY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        ActionReturnUtil result =  HarborHttpsClientUtil.httpDoDelete(url, null, headers);
        if ((boolean) result.get("success") != true){
            if(result.get("data").toString().contains("plicy is enabled")) {
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPLICATION_ENABLE);
            }
            if(result.get("data").toString().contains("running/retrying/pending jobs")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.REPLICATION_PROCESSING);
            }
        }

        return result;
    }

    /**
     * 复制某一规则的跨harbor同步任务
     * @return
     * @throws Exception
     */
    public ActionReturnUtil startCopyPolicy(String harborHost,String policyId)throws Exception{
        AssertUtil.notNull(policyId, DictEnum.REPLICATION_POLICY_ID);
        AssertUtil.notNull(harborHost);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/replications";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");
        Map<String, Object> params = new HashMap<String,Object>();
        params.put("policy_id",Integer.parseInt(policyId));

        ActionReturnUtil result = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, params);

        return result;
    }
    /**
     * 列举所有跨harbor同步任务
     * @return
     * @throws Exception
     */
    @Override
    public List<HarborPolicyDetail> listPolicies(String harborHost) throws Exception {
        return this.listProjectPolicies(harborHost,null);
    }

    /**
     * 列举指定project跨harbor同步任务
     * @return
     * @throws Exception
     */
    @Override
    public List<HarborPolicyDetail> listProjectPolicies(String harborHost, Integer harborProjectID)throws Exception{
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = null;
        if(harborProjectID != null ){
            params = new HashMap<>();
            params.put("project_id", harborProjectID);
        }
        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        if(response.isSuccess() && response.getData() != null){
            List<HarborPolicyDetail> policyDetailList = this.getPolicyDetailList(response.getData().toString());
            return policyDetailList;
        }
        return Collections.emptyList();
    }
    /**
     * 列举指定target跨harbor同步任务
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listTargetPolicies(String harborHost, Integer targetID)throws Exception{
        AssertUtil.notNull(targetID, DictEnum.REPLICATION_TARGET_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        Map<String, Object> params = new HashMap<>();
        params.put("target_id", targetID);

        return HarborHttpsClientUtil.httpGetRequest(url, headers,params);
    }

    /**
     * 更改跨harbor同步任务是否有效
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil updatePolicyEnable(String harborHost, Integer policyID,Integer enabled)throws Exception{
        AssertUtil.notNull(enabled);
        AssertUtil.notNull(policyID, DictEnum.REPLICATION_POLICY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        ActionReturnUtil policyResult = getPolicyById(harborHost,policyID);
        Map<String,Object> policyMap = JsonUtil.convertJsonToMap(policyResult.getData().toString());
        String newTriggerKind = ((Map<String,Object>)policyMap.get("trigger")).get("kind").toString().equals("Immediate")?"Manual":"Immediate";
        Map<String,Object> trigger = new HashMap<String,Object>();
        trigger.put("kind",newTriggerKind);
        policyMap.put("trigger",trigger);

        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID;
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");

        return HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, policyMap);
    }


    public ActionReturnUtil getPolicyById(String harborHost, Integer policyID)throws Exception{
        AssertUtil.notNull(policyID, DictEnum.REPLICATION_POLICY_ID);
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID;

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, null);
    }


    /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listPolicyJobs(String harborHost, Map<String, Object> params) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/jobs/replication";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
    }
    /**
     * 查看harbor同步任务的具体子任务job的日志
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listJobLogs(String harborHost, Integer jobID) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/jobs/replication/"+jobID+"/log";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

        return HarborHttpsClientUtil.httpGetRequest(url, headers, null);
    }
    /**
     * 生成复制镜像map
     * @return
     */
    private Map<String, Object> convertHarborImageCopy(HarborImageCopy harborImageCopy) {
        Map<String, Object> map = new HashMap<>();
        if (harborImageCopy != null) {
            if (StringUtils.isNotEmpty(harborImageCopy.getDestRepoName())) {
                map.put("dest_repo_name", harborImageCopy.getDestRepoName());
            }
            if(StringUtils.isNotEmpty(harborImageCopy.getDestTag())) {
                map.put("dest_tag", harborImageCopy.getDestTag());
            }
            if(StringUtils.isNotEmpty(harborImageCopy.getSrcRepoName())) {
                map.put("src_repo_name", harborImageCopy.getSrcRepoName());
            }
            if(StringUtils.isNotEmpty(harborImageCopy.getSrcTag())) {
                map.put("src_tag", harborImageCopy.getSrcTag());
            }
        }
        return map;
    }
    /**
     * 复制镜像
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil copyImage(HarborImageCopy harborImageCopy) throws Exception{

        HarborServer harborServer = clusterService.findHarborByHost(harborImageCopy.getHarborHost());
        String url = HarborClient.getHarborUrl(harborServer) + "/api/repositories/copyImage";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        headers.put("Content-Type","application/json");

        return HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers,convertHarborImageCopy(harborImageCopy));
    }

    /**
     * get policy status ,return the number of the job with the status "running"or "pending"
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil policyStatus(String harborHost, Integer policyID) throws Exception{
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
//		String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID+"/status";
        String url = HarborClient.getHarborUrl(harborServer) + "/api/jobs/replication";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("policy_id",policyID);
//		params.put("start_time","946659660");//查询所有数据所以开始时间暂给2000年时间戳
//		SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		Date date = format.parse(new Date().toString());
//		params.put("end_time",date.getTime());
        return HarborHttpsClientUtil.httpGetRequest(url, headers, params);
    }

    /**
     * 列举target同步任务Detail
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil listPoliciesDetail(String harborHost, Integer targetID)throws Exception{
        AssertUtil.notNull(targetID, DictEnum.REPLICATION_TARGET_ID);
        ActionReturnUtil repoResponse = listTargetPolicies(harborHost, targetID);
        if ((boolean) repoResponse.get("success") == true) {
            repoResponse.put("data", getPolicyDetail(harborHost, repoResponse));
        } else {
            return repoResponse;
        }
        return repoResponse;
    }

    @Override
    public boolean checkUsingPoliciesName(String harborHost, String name) throws Exception {
        HarborServer harborServer = clusterService.findHarborByHost(harborHost);
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        ActionReturnUtil response = HarborHttpsClientUtil.httpGetRequest(url, headers, params);
        if (!response.isSuccess()) {
            throw new MarsRuntimeException(response.getData().toString());
        }
        List<Map<String, Object>> paramsTemp =JsonUtil.JsonToMapList(response.getData().toString());
        for (Map<String, Object> map:paramsTemp ) {
            String nameTemp = map.get("name").toString();
            if (nameTemp.equals(name)){
                return false;
            }
        }
        return true;
    }
    /**
     * 得到harbor policy detail
     *
     * @param repoResponse repo response
     * @return
     * @throws Exception
     */
    private List<HarborPolicyDetail> getPolicyDetail(String harborHost, ActionReturnUtil repoResponse) throws Exception {
        if (repoResponse.get("data") != null) {
            List<HarborPolicyDetail> policyDetailList = this.getPolicyDetailList(repoResponse.get("data").toString());
            List<HarborPolicyDetail> harborPolicyDetailList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(policyDetailList)) {
                for (HarborPolicyDetail policy : policyDetailList) {
                    harborPolicyDetailList.add(policy);
                    //HarborPolicyDetail harborPolicyDetail = new HarborPolicyDetail();
                    ActionReturnUtil policyResponse = policyStatus(harborHost, policy.getPolicy_id());
                    List<PolicyResponse> policyResponseList = JsonUtil.jsonToList(policyResponse.getData().toString(),PolicyResponse.class);
                    if ((boolean) policyResponse.get("success") == true) {
                        // HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
                        if (policyResponse.get("data") != null) {
                            //HarborPolicyStatus harborPolicyStatus= (HarborPolicyStatus) policyResponse.get("data");
//							HarborPolicyStatus harborPolicyStatus =getPolicyStatus(policyResponse.get("data").toString());
                            HarborPolicyStatus harborPolicyStatus = new HarborPolicyStatus();
                            harborPolicyStatus.setTotalNum(policyResponseList.size());
                            int unFinishedNum = 0, errNum = 0;
                            for(PolicyResponse response : policyResponseList){
                                if(response.getStatus().equals("pending")){
                                    errNum++;
                                }
                                if(response.getStatus().equals("starting")){
                                    unFinishedNum++;
                                }
                            }
                            harborPolicyStatus.setErrorNum(errNum);
                            harborPolicyStatus.setUnfinishedNum(unFinishedNum);
                            //	harborPolicyDetail.setHarborPolicyStatus(harborPolicyStatus);
                            //	harborPolicyDetail.setEnabled(policy.getEnabled());
                            //	harborPolicyDetail.setPolicy_name(policy.getPolicy_name());
                            //	harborPolicyDetail.setProject_name(policy.getProject_name());
                            policy.setHarborPolicyStatus(harborPolicyStatus);
                        }
                    }

                }
            }
            return harborPolicyDetailList;
        }
        return Collections.emptyList();
    }

    /**
     * 得到policy detail list
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private List<HarborPolicyDetail> getPolicyDetailList(String dataJson) throws Exception{
        if (StringUtils.isNotEmpty(dataJson)) {
//
            List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(dataJson);
            if (!CollectionUtils.isEmpty(mapList)) {
                List<HarborPolicyDetail> policyDetailList = new ArrayList<>();
                for (Map<String, Object> map : mapList) {
                    HarborPolicyDetail harborPolicyDetail =new HarborPolicyDetail();
                    if (map.get("id") != null) {
                        harborPolicyDetail.setPolicy_id((Integer)map.get("id"));
                    }
                    if (map.get("name") != null) {
                        harborPolicyDetail.setPolicy_name((String)map.get("name"));
                    }
                    if(map.get("target_project_name")!=null) {
                        harborPolicyDetail.setTarget_project_name((String) map.get("target_project_name"));
                    }
                    if (map.get("creation_time")!=null){
                        if(map.get("creation_time").toString().contains(".")){
                            harborPolicyDetail.setCreation_time(map.get("creation_time").toString().split("\\.")[0] + "Z");
                        }else {
                            harborPolicyDetail.setCreation_time(DateUtil.utcToGmt((String)map.get("creation_time")));
                        }
                    }
                    if (map.get("start_time")!=null){
                        if(map.get("start_time").toString().contains(".")){
                            harborPolicyDetail.setStart_time(map.get("start_time").toString().split("\\.")[0] + "Z");
                        }else {
                            harborPolicyDetail.setStart_time(DateUtil.utcToGmt((String)map.get("start_time")));
                        }
                    }
                    if (map.get("update_time")!=null){
                        if(map.get("update_time").toString().contains(".")){
                            harborPolicyDetail.setUpdate_time(map.get("update_time").toString().split("\\.")[0] + "Z");
                        }else {
                            harborPolicyDetail.setUpdate_time(DateUtil.utcToGmt((String)map.get("update_time")));
                        }
                    }
                    if (map.get("projects")!=null){
                        List<Map<String,Object>> projectsMapList = (List<Map<String,Object>>)map.get("projects");
                        harborPolicyDetail.setProject_name(projectsMapList.get(0).get("name").toString());
                        harborPolicyDetail.setProject_id(Integer.parseInt(projectsMapList.get(0).get("project_id").toString()));
                    }
                    if (map.get("error_job_count")!=null){
                        harborPolicyDetail.setError_job_count((Integer) map.get("error_job_count"));
                    }
                    if (map.get("targets")!=null){
                        List<Map<String,Object>> targetsMapList = (List<Map<String,Object>>)map.get("targets");
                        harborPolicyDetail.setTarget_name((targetsMapList.get(0).get("name").toString()));
                        harborPolicyDetail.setTarget_id(Integer.parseInt(targetsMapList.get(0).get("id").toString()));
                    }
                    if(map.get("trigger")!=null){
                        String triggerKind = ((Map<String,Object>)map.get("trigger")).get("kind").toString();
                        harborPolicyDetail.setHarborPolicyTrigger(triggerKind);
                        Integer enabled = triggerKind.equals("Immediate")?1:0;//新harbor取消了规则开关，以规则即刻/手动状态改为规则开关
                        harborPolicyDetail.setEnabled(enabled);
                    }
                    if(map.get("filters")!=null){
                        List<Map<String,Object>> filtersMapList = (List<Map<String,Object>>)map.get("filters");
                        List<Map<String,Object>> labelsMapList = new LinkedList<Map<String,Object>> ();
                        HarborPolicyFilter harborPolicyFilter = new HarborPolicyFilter();
                        for(Map<String,Object> filterMap : filtersMapList){
                            if(filterMap.get("kind").toString().equals("repository")){
                                harborPolicyFilter.setRepository(filterMap.get("value").toString());
                            }
                            if(filterMap.get("kind").toString().equals("tag")){
                                harborPolicyFilter.setTag(filterMap.get("value").toString());
                            }
                            if(filterMap.get("kind").toString().equals("label")){
                                Map<String,Object> labelMap = (Map<String,Object>)filterMap.get("value");
                                labelsMapList.add(labelMap);
                            }
                        }
                        harborPolicyFilter.setLabels(labelsMapList);
                        harborPolicyDetail.setHarborPolicyFilter(harborPolicyFilter);
                    }
                    policyDetailList.add(harborPolicyDetail);
                }
                return policyDetailList;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 得到policy status
     *
     * @param dataJson json格式返回的data
     * @return
     */
    private HarborPolicyStatus getPolicyStatus(String dataJson) throws Exception{
        HarborPolicyStatus harborPolicyStatus = new HarborPolicyStatus();
        if (StringUtils.isNotEmpty(dataJson)) {
            Map<String, Object> map = JsonUtil.jsonToMap(dataJson);

            if (map.get("unfinishedNum") != null) {
                harborPolicyStatus.setUnfinishedNum((Integer) map.get("unfinishedNum"));
            }
            if (map.get("totalNum") != null) {
                harborPolicyStatus.setTotalNum((Integer) map.get("totalNum"));
            }
            if (map.get("errorNum") != null) {
                harborPolicyStatus.setErrorNum((Integer) map.get("errorNum"));
            }
        }
        return harborPolicyStatus;
    }

    /**
     * 新建跨harbor同步任务细粒度
     * @return
     * @throws Exception
     */
//	@Override
//	public ActionReturnUtil createPartialPolicy(String harborHost, Map<String, Object> harborReplicationPolicy) throws Exception{
//		HarborServer harborServer = clusterService.findHarborByHost(harborHost);
//		String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/partialreplication";
//		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
//		return HttpsClientUtil.httpPostRequestForHarbor(url, headers, harborReplicationPolicy);
//
//	}

    /**
     * 新建跨harbor同步任务细粒度
     * @param imagePartialSyncInfo
     * @return
     * @throws Exception
     */
    @Override
    public ActionReturnUtil createPartialPolicy(ImagePartialSyncInfo imagePartialSyncInfo) throws Exception{

        Cluster cluster = clusterService.findClusterById(imagePartialSyncInfo.getSrcClusterId());
        String url = HarborClient.getHarborUrl(cluster.getHarborServer()) + "/api/policies/partialreplication";
        Map<String, Object> headers = HarborClient.getAdminCookieHeader(cluster.getHarborServer());
        Map<String, Object> harborReplicationPolicy = new HashMap<>();
        harborReplicationPolicy.put("target_id", imagePartialSyncInfo.getTargetId());
        harborReplicationPolicy.put("project_id", imagePartialSyncInfo.getHarborProjectId());
        harborReplicationPolicy.put("name",imagePartialSyncInfo.getHarborProjectName());
        harborReplicationPolicy.put("enabled", 0);
        harborReplicationPolicy.put("partial", 1);
        harborReplicationPolicy.put("image", imagePartialSyncInfo.getImages());
        return HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers, harborReplicationPolicy);

    }


    private ActionReturnUtil pingTarget(String harborHost, HarborReplicationTarget harborReplicationTarget) throws Exception{
        ActionReturnUtil pingResponse = pingEndpoint(harborHost, harborReplicationTarget.getEndpoint(),harborReplicationTarget.getUsername(),harborReplicationTarget.getPassword());
        if ((boolean) pingResponse.get("success") == true) {
            return pingResponse;
        }else{
            if(pingResponse.get("data").toString().contains("Timeout")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONNECT_TIMEOUT);
            }
            if(pingResponse.get("data").toString().contains("no route to host")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONNECT_FAIL);
            }
            if(pingResponse.get("data").equals("Unauthorized")){
                return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.AUTH_FAIL);
            }
            return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.CONNECT_FAIL);
        }
    }

    /**
     * 生成同步对象map
     * @return
     */
    private Map<String, Object> convertHarborReplicationTarget(HarborReplicationTarget harborReplicationTarget) {
        Map<String, Object> map = new HashMap<>();
        if (harborReplicationTarget != null) {
            if (StringUtils.isNotEmpty(harborReplicationTarget.getEndpoint())) {
                map.put("endpoint", harborReplicationTarget.getEndpoint());
            }
            if (StringUtils.isNotEmpty(harborReplicationTarget.getName())) {
                map.put("name", harborReplicationTarget.getName());
            }
            if (StringUtils.isNotEmpty(harborReplicationTarget.getUsername())){
                map.put("username", harborReplicationTarget.getUsername());
            }
            if (StringUtils.isNotEmpty(harborReplicationTarget.getPassword())){
                map.put("password", harborReplicationTarget.getPassword());
            }
            if (harborReplicationTarget.getInsecure() != null) {
                map.put("insecure", harborReplicationTarget.getInsecure());
            }
        }
        return map;
    }



}