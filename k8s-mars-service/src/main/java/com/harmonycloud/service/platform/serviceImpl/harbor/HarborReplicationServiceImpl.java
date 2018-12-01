package com.harmonycloud.service.platform.serviceImpl.harbor;


import java.net.URLEncoder;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.enumm.DictEnum;
import com.harmonycloud.common.util.*;
import com.harmonycloud.common.util.date.DateUtil;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.k8s.bean.cluster.HarborServer;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.common.HarborHttpsClientUtil;
import com.harmonycloud.service.platform.bean.harbor.*;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborService;
import com.harmonycloud.service.platform.service.harbor.HarborUserService;
import org.apache.commons.lang3.StringUtils;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborReplicationService;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpSession;

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
		String url = HarborClient.getHarborUrl(harborServer) + "/api/targets/ping?endpoint="+endpoint+"&username="
				+targetusername+"&password="+URLEncoder.encode(targetuserpassword,"UTF-8");

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

		ActionReturnUtil response = HarborHttpsClientUtil.httpPostRequestForHarbor(url, headers,null);
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

		return HarborHttpsClientUtil.httpGetRequest(url, null, headers);
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
	private Map<String, Object> convertHarborReplicationPolicy(HarborReplicationPolicy harborReplicationPolicy) {
		 Map<String, Object> map = new HashMap<>();
	        if (harborReplicationPolicy != null) {
	            if (StringUtils.isNotEmpty(harborReplicationPolicy.getName())) {
	                map.put("name", harborReplicationPolicy.getName());
	            }
	            if (StringUtils.isNotEmpty(harborReplicationPolicy.getDescription())) {
	            	map.put("description", harborReplicationPolicy.getDescription());
	            }
	            if (harborReplicationPolicy.getHarborProjectId() != null) {
	                map.put("project_id", harborReplicationPolicy.getHarborProjectId());
	            }
	            if (harborReplicationPolicy.getEnabled()!= null) {
	                map.put("enabled", harborReplicationPolicy.getEnabled());
	            }
	            if (harborReplicationPolicy.getTargetId()!= null) {
	                map.put("target_id", harborReplicationPolicy.getTargetId());
	            }
	            if (harborReplicationPolicy.getPartial()!=null){
	            	map.put("partial",harborReplicationPolicy.getPartial());
				}

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
        String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID+"/enablement";

        Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);
		Map<String, Object> params = new HashMap<>();
		params.put("enabled",enabled);
        return HarborHttpsClientUtil.httpPutRequestForHarbor(url, headers, params);
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
		String url = HarborClient.getHarborUrl(harborServer) + "/api/policies/replication/"+policyID+"/status";

		Map<String, Object> headers = HarborClient.getAdminCookieHeader(harborServer);

		return HarborHttpsClientUtil.httpGetRequest(url, headers, null);
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
					if ((boolean) policyResponse.get("success") == true) {
						// HarborManifest tagDetail = (HarborManifest) maniResponse.get("data");
						if (policyResponse.get("data") != null) {
							//HarborPolicyStatus harborPolicyStatus= (HarborPolicyStatus) policyResponse.get("data");
							HarborPolicyStatus harborPolicyStatus =getPolicyStatus(policyResponse.get("data").toString());
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
					if (map.get("enabled") != null) {
						harborPolicyDetail.setEnabled((Integer)map.get("enabled"));
					}
					if (map.get("creation_time")!=null){
						harborPolicyDetail.setCreation_time(DateUtil.utcToGmt((String)map.get("creation_time")));
					}
					if (map.get("start_time")!=null){
						harborPolicyDetail.setStart_time(DateUtil.utcToGmt((String)map.get("start_time")));
					}
					if (map.get("update_time")!=null){
						harborPolicyDetail.setUpdate_time(DateUtil.utcToGmt((String)map.get("update_time")));
					}
					if (map.get("project_name")!=null){
						harborPolicyDetail.setProject_name((String)map.get("project_name"));
					}
					if (map.get("project_id")!=null){
						harborPolicyDetail.setProject_id((Integer) map.get("project_id"));
					}
					if (map.get("error_job_count")!=null){
						harborPolicyDetail.setError_job_count((Integer) map.get("error_job_count"));
					}
					if (map.get("target_name")!=null){
						harborPolicyDetail.setTarget_name((String) map.get("target_name"));
					}
					if (map.get("target_id")!=null){
						harborPolicyDetail.setTarget_id((Integer) map.get("target_id"));
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
		}
		return map;
	}

}
