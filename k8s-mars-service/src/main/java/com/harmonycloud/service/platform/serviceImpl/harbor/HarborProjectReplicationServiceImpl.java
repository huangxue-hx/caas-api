package com.harmonycloud.service.platform.serviceImpl.harbor;


import java.util.*;

import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.service.platform.bean.*;
import org.apache.commons.lang3.StringUtils;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HarborUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborProjectReplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 *
 */
@Service
public class HarborProjectReplicationServiceImpl implements HarborProjectReplicationService {
	 @Autowired
	 private HarborUtil harborUtil;
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
	/**
	 * 新建跨harbor同步对象
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil createTarget(HarborReplicationTarget harborReplicationTarget) throws Exception{
		if (harborReplicationTarget == null) {
            return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/targets";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpPostRequestForHarbor(url, headers, convertHarborReplicationTarget(harborReplicationTarget));
		
	}
	 /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
	@Override
	public ActionReturnUtil pingEndpoint(String endpoint,String targetusername,String targetuserpassword)throws Exception{
		if (StringUtils.isEmpty(endpoint)) {
            return ActionReturnUtil.returnErrorWithMsg("endpoint cannot be null");
        }
		if (StringUtils.isEmpty(targetusername)) {
            return ActionReturnUtil.returnErrorWithMsg("targetusername cannot be null");
        }
		if (StringUtils.isEmpty(targetuserpassword)) {
            return ActionReturnUtil.returnErrorWithMsg("targetuserpassword cannot be null");
        }

        String url = HarborClient.getPrefix() + "/api/targets/ping?endpoint="+endpoint+"&username="+targetusername+"&password="+targetuserpassword;

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

     //   Map<String, Object> params = new HashMap<>();
     //  params.put("endpoint", endpoint);
    //   params.put("username", targetusername);
    //    params.put("password", targetuserpassword);
        return HttpClientUtil.httpPostRequestForHarbor(url, headers,null);
	}
	 /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
	@Override
	public ActionReturnUtil deleteTarget(Integer targetID) throws Exception{
		if (targetID == null || targetID < 0) {
            return ActionReturnUtil.returnErrorWithMsg("targetId is invalid");
        }
		String url = HarborClient.getPrefix() + "/api/targets/"+targetID;
		
		Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpDoDelete(url, null, headers);
	} 
	/**
     * 列举跨harbor同步对象
     * @return
     * @throws Exception
     */
	@Override
    public ActionReturnUtil listTargets() throws Exception{
    	 
         String url = HarborClient.getPrefix() + "/api/targets";

         Map<String, Object> headers = new HashMap<>();
         headers.put("cookie", harborUtil.checkCookieTimeout());

		ActionReturnUtil listResponse =HttpClientUtil.httpGetRequest(url, headers, null);
		/*
		if ((boolean)listResponse.get("success") == true) {
			Object convertJson = JsonUtil.jsonToPojo(listResponse.get("data").toString(),Object.class);
			listResponse.put("data", convertJson);
			//return listResponse;
		}
		*/
		return listResponse;
		//return listResponse;
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
	            if (harborReplicationPolicy.getProject_id() != null) {
	                map.put("project_id", harborReplicationPolicy.getProject_id());
	            }
	            if (harborReplicationPolicy.getEnabled()!= null) {
	                map.put("enabled", harborReplicationPolicy.getEnabled());
	            }
	            if (harborReplicationPolicy.getTarget_id()!= null) {
	                map.put("target_id", harborReplicationPolicy.getTarget_id());
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
	public ActionReturnUtil createPolicy(HarborReplicationPolicy harborReplicationPolicy) throws Exception{
		 if (harborReplicationPolicy == null) {
			 return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
	      }

	      String url = HarborClient.getPrefix() + "/api/policies/replication";

	      Map<String, Object> headers = new HashMap<>();
	      headers.put("cookie", harborUtil.checkCookieTimeout());

	      //return HttpClientUtil.httpPostRequestForHarborCreate(url, headers, convertHarborReplicationPolicy(harborReplicationPolicy));
		 return HttpClientUtil.httpPostRequestForHarbor(url, headers, convertHarborReplicationPolicy(harborReplicationPolicy));
			
		}
	 /**
	     * 删除跨harbor同步任务
	     * @return
	     * @throws Exception
	     */
	 public ActionReturnUtil deletePolicy(Integer policyID)throws Exception{
		 if (policyID == null || policyID < 0) {
	          return ActionReturnUtil.returnErrorWithMsg("policyID is invalid");
	      }
	      String url = HarborClient.getPrefix() + "/api/policies/replication/"+policyID;
			
	      Map<String, Object> headers = new HashMap<>();
	      headers.put("cookie", harborUtil.checkCookieTimeout());

		 ActionReturnUtil result =  HttpClientUtil.httpDoDelete(url, null, headers);
		 if ((boolean) result.get("success") != true
				 && result.get("data").toString().contains("plicy is enabled")) {
			 return ActionReturnUtil.returnErrorWithData("请先停止同步规则");
		 }
		 return result;
	 }
	 /**
	   * 列举指定project跨harbor同步任务
	   * @return
	   * @throws Exception
	   */
	 @Override
	public ActionReturnUtil listProjectPolicies(Integer projectID)throws Exception{
		 if (projectID == null || projectID < 0) {
			 return ActionReturnUtil.returnErrorWithMsg("projectID is invalid");
		 }
		   //String url = HarborClient.getPrefix() + "/api/policies/replication?project_id="+projectID;
		   String url = HarborClient.getPrefix() + "/api/policies/replication";
		   Map<String, Object> headers = new HashMap<>();
	       headers.put("cookie", harborUtil.checkCookieTimeout());

	       Map<String, Object> params = new HashMap<>();
	       params.put("project_id", projectID);

	       return HttpClientUtil.httpGetRequest(url, headers,params);
	   }
	/**
	 * 列举指定target跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@Override
	   public ActionReturnUtil listTargetPolicies(Integer targetID)throws Exception{
		if (targetID == null || targetID < 0) {
			return ActionReturnUtil.returnErrorWithMsg("targetID is invalid");
		}
		//String url = HarborClient.getPrefix() + "/api/policies/replication?target_id="+targetID;
		String url = HarborClient.getPrefix() + "/api/policies/replication";
		Map<String, Object> headers = new HashMap<>();
		headers.put("cookie", harborUtil.checkCookieTimeout());

		Map<String, Object> params = new HashMap<>();
		params.put("target_id", targetID);

		return HttpClientUtil.httpGetRequest(url, headers,params);
	   }
	 /**
	     * 生成同步任务是否有效的更改map
	     * @return
	     */
	private Map<String, Object> convertHarborReplicationPolicyEnable(HarborReplicationPolicyEnable harborReplicationPolicyEnable) {
		 Map<String, Object> map = new HashMap<>();
	        if (harborReplicationPolicyEnable != null) {
	            
	            if (harborReplicationPolicyEnable.getEnabled() != null) {
	                map.put("enabled", harborReplicationPolicyEnable.getEnabled());
	            }
	           
	        }
	        return map;
	    }
	/**
	 * 更改跨harbor同步任务是否有效
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil updatePolicyEnable(Integer policyID,HarborReplicationPolicyEnable harborReplicationPolicyEnable)throws Exception{
		if (harborReplicationPolicyEnable == null) {
            return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
        }
		if (policyID == null || policyID < 0) {
            return ActionReturnUtil.returnErrorWithMsg("replicationID is invalid");
        }
        String url = HarborClient.getPrefix() + "/api/policies/replication/"+policyID+"/enablement";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpPutRequestForHarbor(url, headers, convertHarborReplicationPolicyEnable(harborReplicationPolicyEnable));
	}


	/**
     * 列举所有跨harbor同步任务
     * @return
     * @throws Exception
     */
	@Override
	public ActionReturnUtil listPolicies() throws Exception {
        String url = HarborClient.getPrefix() + "/api/policies/replication";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpGetRequest(url, headers, null);
    }
	
	 /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
	@Override
	public ActionReturnUtil listPolicyJobs(Integer policyID, String page, String pageSize, String end_time,
			String start_time, String status) throws Exception {
		/*
		 page = (page == null || page < 1) ? 1 : page;
	        pageSize = (pageSize == null || pageSize < 1) ? 100 : pageSize;
*/
	        String url = HarborClient.getPrefix() + "/api/jobs/replication";

	        Map<String, Object> headers = new HashMap<>();
	        headers.put("cookie", harborUtil.checkCookieTimeout());

	        Map<String, Object> params = new HashMap<>();
	        params.put("page", page);
	        params.put("page_size", pageSize);
	        params.put("start_time", start_time);
	        params.put("end_time", end_time);
	        params.put("policy_id", policyID);
	        params.put("status", status);

	        return HttpClientUtil.httpGetRequest(url, headers, params);
	}
	 /**
     * 查看harbor同步任务的具体子任务job的日志
     * @return
     * @throws Exception
     */
	@Override
	public ActionReturnUtil listJobLogs(Integer jobID) throws Exception {
		String url = HarborClient.getPrefix() + "/api/jobs/replication/"+jobID+"/log";

        Map<String, Object> headers = new HashMap<>();
        headers.put("cookie", harborUtil.checkCookieTimeout());

        return HttpClientUtil.httpGetRequest(url, headers, null);
	}
	/**
	 * 生成复制镜像map
	 * @return
	 */
	private Map<String, Object> convertHarborImageCopy(HarborImageCopy harborImageCopy) {
		Map<String, Object> map = new HashMap<>();
		if (harborImageCopy != null) {
			if (StringUtils.isNotEmpty(harborImageCopy.getDest_repo_name())) {
				map.put("dest_repo_name", harborImageCopy.getDest_repo_name());
			}
			if(StringUtils.isNotEmpty(harborImageCopy.getDest_tag())) {
				map.put("dest_tag", harborImageCopy.getDest_tag());
			}
			if(StringUtils.isNotEmpty(harborImageCopy.getSrc_repo_name())) {
				map.put("src_repo_name", harborImageCopy.getSrc_repo_name());
			}
			if(StringUtils.isNotEmpty(harborImageCopy.getSrc_tag())) {
				map.put("src_tag", harborImageCopy.getSrc_tag());
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


		String url = HarborClient.getPrefix() + "/api/repositories/copy";

		Map<String, Object> headers = new HashMap<>();
		headers.put("cookie", harborUtil.checkCookieTimeout());

		return HttpClientUtil.httpPostRequestForHarbor(url, headers,convertHarborImageCopy(harborImageCopy));
	}

	/**
	 * get policy status ,return the number of the job with the status "running"or "pending"
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil policyStatus(Integer policyID) throws Exception{
		String url = HarborClient.getPrefix() + "/api/policies/replication/"+policyID+"/status";

		Map<String, Object> headers = new HashMap<>();
		headers.put("cookie", harborUtil.checkCookieTimeout());

		return HttpClientUtil.httpGetRequest(url, headers, null);
	}

	/**
	 * 列举target同步任务Detail
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil listPoliciesDetail(Integer targetID)throws Exception{
		if (targetID == null || targetID < 0) {
			return ActionReturnUtil.returnErrorWithMsg("targetID is invalid");
		}
		ActionReturnUtil repoResponse = listTargetPolicies(targetID);
		if ((boolean) repoResponse.get("success") == true) {
			repoResponse.put("data", getPolicyDetail(repoResponse));
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
	private List<HarborPolicyDetail> getPolicyDetail(ActionReturnUtil repoResponse) throws Exception {
		if (repoResponse.get("data") != null) {
			List<HarborPolicyDetail> policyDetailList = this.getPolicyDetailList(repoResponse.get("data").toString());
			List<HarborPolicyDetail> harborPolicyDetailList = new ArrayList<>();
			if (!CollectionUtils.isEmpty(policyDetailList)) {
				for (HarborPolicyDetail policy : policyDetailList) {
					harborPolicyDetailList.add(policy);
					//HarborPolicyDetail harborPolicyDetail = new HarborPolicyDetail();
					ActionReturnUtil policyResponse = policyStatus(policy.getPolicy_id());
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
					if (map.get("update_time")!=null){
						harborPolicyDetail.setUpdate_time((String)map.get("update_time"));
					}
					if (map.get("project_name")!=null){
						harborPolicyDetail.setProject_name((String)map.get("project_name"));
					}
					if (map.get("project_id")!=null){
						harborPolicyDetail.setProject_id((Integer) map.get("project_id"));
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
	 * 新建跨harbor同步target
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil newTarget(HarborReplicationTarget harborReplicationTarget) throws Exception{
		if (harborReplicationTarget == null) {
			return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
		}
		//先检查设置的镜像服务器连接是否成功
		ActionReturnUtil pingResult = pingTarget(harborReplicationTarget);
		if((boolean) pingResult.get("success") != true){
			return pingResult;
		}
		//check target检测是否已经有target存在
		ActionReturnUtil targetResponse = listTargets();
		if ((boolean) targetResponse.get("success") != true) {
			return targetResponse;
		}
		List<Map<String, Object>> mapList = JsonUtil.JsonToMapList(targetResponse.get("data").toString());
		//如果已经存在target，先删除
		if (!CollectionUtils.isEmpty(mapList)) {
			Map<String, Object> targetMap = mapList.get(0);
			Integer targetCurrent = (Integer)targetMap.get("id");
			//delete the current target;删除已经存在的target
			ActionReturnUtil targetDeleResponse = deleteTarget(targetCurrent);
			if ((boolean) targetDeleResponse.get("success") != true) {
				if(targetDeleResponse.get("data").toString().contains("used by policies")){
					return ActionReturnUtil.returnErrorWithData("请先停止并删除同步规则");
				}
				if(targetDeleResponse.get("data").toString().contains("running/retrying/pending jobs")){
					return ActionReturnUtil.returnErrorWithData("同步任务未结束，请稍后重试");
				}
				return targetDeleResponse;
			}
		}
		//create target;创建target
		return createTarget(harborReplicationTarget);
	}

	/**
	 * 新建跨harbor同步任务细粒度
	 * @return
	 * @throws Exception
	 */
	@Override
	public ActionReturnUtil createPartialPolicy(Map<String, Object> harborReplicationPolicy) throws Exception{
		if (harborReplicationPolicy == null) {
			return ActionReturnUtil.returnErrorWithMsg("parameter cannot be null");
		}
		String url = HarborClient.getPrefix() + "/api/policies/partialreplication";
		Map<String, Object> headers = new HashMap<>();
		headers.put("cookie", harborUtil.checkCookieTimeout());
		return HttpClientUtil.httpPostRequestForHarbor(url, headers, harborReplicationPolicy);

	}

	private ActionReturnUtil pingTarget(HarborReplicationTarget harborReplicationTarget) throws Exception{
		ActionReturnUtil pingResponse = pingEndpoint(harborReplicationTarget.getEndpoint(),harborReplicationTarget.getUsername(),harborReplicationTarget.getPassword());
		if ((boolean) pingResponse.get("success") == true) {
			return pingResponse;
		}else{
			if(pingResponse.get("data").toString().contains("Timeout")){
				return ActionReturnUtil.returnErrorWithData("服务器连接超时");
			}
			if(pingResponse.get("data").toString().contains("no route to host")){
				return ActionReturnUtil.returnErrorWithData("服务器连接失败");
			}
			if(pingResponse.get("data").equals("Unauthorized")){
				return ActionReturnUtil.returnErrorWithData("认证未通过, 请检查用户名或密码是否正确");
			}
			return pingResponse;
		}
	}

}