package com.harmonycloud.api.harbor;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.cluster.ClusterService;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.service.platform.bean.harbor.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.service.platform.service.harbor.HarborReplicationService;

/**
 * Created by andy on 17-1-19.
 */
@Controller
public class HarborReplicationController {

    @Autowired
    private HarborReplicationService harborReplicationService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Autowired
	ClusterService clusterService;

    /**
     * 新建跨harbor同步对象
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationtargets", method = RequestMethod.POST)
    @ResponseBody
	public ActionReturnUtil createTarget(@ModelAttribute HarborReplicationTarget harborReplicationTarget,
										 @PathVariable(value="harborHost") String harborHost)  throws Exception{
		logger.info("新建镜像仓库同步对象,harborReplicationTarget:{}", JSONObject.toJSONString(harborReplicationTarget));
		if (null == harborReplicationTarget.getSourceHarborHost()){
			harborReplicationTarget.setSourceHarborHost(harborHost);
		}
		return this.harborReplicationService.createTarget(harborReplicationTarget);
	}

	/**
	 * 修改跨harbor同步对象
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationtargets/{targetId}", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updateTarget(@ModelAttribute HarborReplicationTarget harborReplicationTarget,
										 @PathVariable(value="harborHost") String harborHost,
										 @PathVariable(value="targetId") Integer targetId)  throws Exception{
		logger.info("新建镜像仓库同步对象,harborReplicationTarget:{}", JSONObject.toJSONString(harborReplicationTarget));
		harborReplicationTarget.setSourceHarborHost(harborHost);
		harborReplicationTarget.setId(targetId);
		return this.harborReplicationService.updateTarget(harborReplicationTarget);
	}

    /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationtargets/ping", method = RequestMethod.POST)
    @ResponseBody
	public ActionReturnUtil pingEndpoint(@ModelAttribute HarborReplicationTarget harborReplicationTarget,
										 @PathVariable(value="harborHost") String harborHost) throws Exception{
		if (null == harborReplicationTarget.getSourceHarborHost()){
			harborReplicationTarget.setSourceHarborHost(harborHost);
		}
		logger.info("测试账户是否有权限,endpoint:{}, sourceHarborHost:{},userName:{}",
				new String[]{harborReplicationTarget.getEndpoint(),harborReplicationTarget.getSourceHarborHost(),harborReplicationTarget.getUsername()});
		return this.harborReplicationService.pingEndpoint(
				harborReplicationTarget.getSourceHarborHost(), harborReplicationTarget.getEndpoint(),
				harborReplicationTarget.getUsername(), harborReplicationTarget.getPassword());
	}
    
    /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationtargets/{targetId}", method = RequestMethod.DELETE)
    @ResponseBody
	public ActionReturnUtil deleteTarget(@PathVariable(value="harborHost") String harborHost,
										 @PathVariable(value="targetId") Integer targetID)throws Exception{

		logger.info("删除镜像仓库同步对象,harborHost:{},targetId:{}",harborHost,targetID);
		return this.harborReplicationService.deleteTarget(harborHost, targetID);

	}

    /**
     * 列举跨harbor同步对象 ok
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationtargets", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listTargets(@PathVariable(value="harborHost") String harborHost) throws Exception{
		logger.info("列举镜像仓库同步对象,harborHost:{}", harborHost);
		return this.harborReplicationService.listTargets(harborHost);
	}

    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies", method = RequestMethod.POST)
    @ResponseBody
	public ActionReturnUtil createPolicy(@ModelAttribute HarborReplicationPolicy harborReplicationPolicy,
										 @PathVariable(value="harborHost") String harborHost) throws Exception{
		harborReplicationPolicy.setHarborHost(harborHost);
		logger.info("新建镜像仓库同步任务,harborReplicationPolicy:{}", JSONObject.toJSONString(harborReplicationPolicy));
		return this.harborReplicationService.createPolicy(harborReplicationPolicy);

	}

    /**
     * 删除跨harbor同步任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}", method = RequestMethod.DELETE)
    @ResponseBody
	public ActionReturnUtil deletePolicy(@PathVariable(value="harborHost") String harborHost,
										 @PathVariable(value="policyId")String policyID)throws Exception{
		logger.info("删除镜像仓库同步任务,harborHost:{},policyID:{}",harborHost,policyID);
		return this.harborReplicationService.deletePolicy(harborHost, Integer.valueOf(policyID));
	}

    /**
	   * 列举指定project跨harbor同步任务
	   * @return
	   * @throws Exception
	   */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listPolicies(@PathVariable(value="harborHost") String harborHost,
												@RequestParam(value="harborProjectId",required = false)String harborProjectId) throws Exception{
		logger.info("列举指定project镜像仓库同步任务,harborHost:{},projectID:{}",harborHost,harborProjectId);
		if(StringUtils.isBlank(harborProjectId)){
			return ActionReturnUtil.returnSuccessWithData(harborReplicationService.listPolicies(harborHost));
		}else {
			return ActionReturnUtil.returnSuccessWithData(harborReplicationService
					.listProjectPolicies(harborHost, Integer.valueOf(harborProjectId)));
		}
	}

	/**
	 * 列举指定target跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationtargets/{targetId}/replicationpolicies", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listTargetPolicies(@PathVariable(value="harborHost") String harborHost,
											   @PathVariable(value="targetId")String targetID)throws Exception{
		return this.harborReplicationService.listTargetPolicies(harborHost, Integer.valueOf(targetID));
	}

    /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}/policyjobs", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listPolicyJobs(@RequestParam(value = "page",required = false) String page,
										   @RequestParam(value = "pageSize",required = false) String pageSize,
										   @PathVariable(value = "policyId") String policyID,
										   @RequestParam(value = "startTime",required = false) String startTime,
										   @RequestParam(value = "endTime",required = false) String endTime,
										   @RequestParam(value = "status",required = false) String status,
										   @PathVariable(value = "harborHost") String harborHost)throws Exception{
		Map<String, Object> queryParams = new HashMap<>();
		queryParams.put("page", page);
		queryParams.put("page_size", pageSize);
		queryParams.put("start_time", startTime);
		queryParams.put("end_time", endTime);
		queryParams.put("policy_id", policyID);
		queryParams.put("status", status);
		return this.harborReplicationService.listPolicyJobs(harborHost, queryParams);
	}

    /**
     * 查看harbor同步任务的具体子任务的日志
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/policyjobs/{jobId}", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listJobLogs(@PathVariable(value = "harborHost") String harborHost,
										@PathVariable(value="jobId")String jobID)throws Exception{
		return this.harborReplicationService.listJobLogs(harborHost,Integer.valueOf(jobID));
	}

    /**
     * 更改跨harbor同步任务是否有效
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}/enable", method = RequestMethod.PUT)
    @ResponseBody
	public ActionReturnUtil enablePolicy(@PathVariable(value = "harborHost") String harborHost,
										@PathVariable(value="policyId")String policyID,
										@ModelAttribute HarborReplicationPolicyEnable harborReplicationPolicyEnable)throws Exception{
		logger.info("更改跨镜像仓库同步任务是否有效,harborHost:{},policyId:{}",harborHost,policyID);
		return this.harborReplicationService.updatePolicyEnable(harborHost, Integer.valueOf(policyID), harborReplicationPolicyEnable.getEnabled());
	}

	/**
	 * 查看harbor同步任务的status
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}/status", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getPolicyStatus(@PathVariable(value = "harborHost") String harborHost,
											@PathVariable(value="policyId")String policyID)throws Exception{
		return this.harborReplicationService.policyStatus(harborHost, Integer.valueOf(policyID));
	}

	/**
	 * 列举指定target跨harbor同步任务detail
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationtargets/{targetId}/replicationpolicies/detail", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listTargetPoliciesDetail(@PathVariable(value = "harborHost") String harborHost,
													 @PathVariable(value="targetId")String targetID)throws Exception{
		return this.harborReplicationService.listPoliciesDetail(harborHost, Integer.valueOf(targetID));
	}

	/**
	 * 同一个harbor镜像复制
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/syncimage", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil copyImage(@ModelAttribute HarborImageCopy harborImageCopy,
									  @PathVariable(value = "harborHost") String harborHost)throws Exception{
		harborImageCopy.setHarborHost(harborHost);
		logger.info("复制镜像,harborImageCopy:{}",harborImageCopy);
		return this.harborReplicationService.copyImage(harborImageCopy);
	}

	/**
	 * 新建跨harbor同步policy细粒度，针对镜像的同步
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/partialpolicies", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createPartialPolicy(@ModelAttribute ImagePartialSyncInfo imagePartialSyncInfo)throws Exception{
		if (imagePartialSyncInfo == null) {
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}
		if(null == imagePartialSyncInfo.getSrcClusterId()
				|| null == imagePartialSyncInfo.getDestClusterId()
				|| null == imagePartialSyncInfo.getProjectId()){
			ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
		}

		return this.harborReplicationService.createPartialPolicy(imagePartialSyncInfo);
	}


}
