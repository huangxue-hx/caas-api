package com.harmonycloud.api.harbor;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.cluster.ClusterService;

import java.util.HashMap;
import java.util.Map;

import com.harmonycloud.service.platform.bean.harbor.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(description = "harbor镜像同步管理")
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
	@ApiOperation(value = "创建镜像同步的目标harbor服务器", notes = "创建镜像同步的目标harbor服务器")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "更新镜像同步的目标harbor服务器", notes = "更新镜像同步的目标harbor服务器")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "targetId", value = "目标harbor服务器id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "测试目标harbor服务器是否连通", notes = "测试目标harbor服务器是否连通")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "删除目标harbor服务器", notes = "删除目标harbor服务器")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "targetId", value = "目标harbor服务器id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "查询目标harbor服务器列表", notes = "查询目标harbor服务器列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "创建同步策略", notes = "创建同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createPolicy(@ModelAttribute HarborReplicationPolicy harborReplicationPolicy,
										 @PathVariable(value="harborHost") String harborHost) throws Exception{
		harborReplicationPolicy.setHarborHost(harborHost);
		logger.info("新建镜像仓库同步任务,harborReplicationPolicy:{}", JSONObject.toJSONString(harborReplicationPolicy));
		return this.harborReplicationService.createPolicy(harborReplicationPolicy);

	}

	/**
	 * 修改跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "修改同步策略", notes = "修改同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyID", value = "同步策略id", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}", method = RequestMethod.PUT)
	@ResponseBody
	public ActionReturnUtil updatePolicy(@ModelAttribute HarborReplicationPolicy harborReplicationPolicy,
										 @PathVariable(value="harborHost") String harborHost,
										 @PathVariable(value="policyId")String policyID) throws Exception{
		logger.info("更新镜像仓库同步任务,harborHost:{},policyID:{}",harborHost,policyID);
		return this.harborReplicationService.updatePolicy(harborReplicationPolicy,policyID);

	}

	/**
	 * 获取跨harbor同步任务详情
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "获取同步策略详情", notes = "获取同步策略详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name ="harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyID", value = "同步策略id", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}/detail", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getPolicyDetail(@PathVariable(value="harborHost") String harborHost,
											@PathVariable(value="policyId")String policyID) throws Exception{
		logger.info("更新镜像仓库同步任务,harborHost:{},policyID:{}",harborHost,policyID);
		return this.harborReplicationService.getPolicyDetail(harborHost,policyID);
	}

	/**
	 * 删除跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "删除同步策略", notes = "删除同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyId", value = "同步策略id", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}", method = RequestMethod.DELETE)
	@ResponseBody
	public ActionReturnUtil deletePolicy(@PathVariable(value="harborHost") String harborHost,
										 @PathVariable(value="policyId")String policyID)throws Exception{
		logger.info("删除镜像仓库同步任务,harborHost:{},policyID:{}",harborHost,policyID);
		return this.harborReplicationService.deletePolicy(harborHost, Integer.parseInt(policyID));
	}


	/**
	 * 手动开启复制某一跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "开启一条规则复制", notes = "开启一条规则复制")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyId", value = "同步策略id", paramType = "path",dataType = "String")})
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/{policyId}/copy", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil startCopyPolicy(@PathVariable(value="harborHost") String harborHost,
											@PathVariable(value="policyId")String policyId)throws Exception{
		logger.info("开启policyID:{}的复制规则",policyId);
		return this.harborReplicationService.startCopyPolicy(harborHost,policyId);
	}

	/**
	 * 列举指定project跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询某个仓库项目建立的同步策略", notes = "查询某个仓库项目建立的同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "harborProjectId", value = "仓库id", paramType = "query",dataType = "String")})
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
	@ApiOperation(value = "查询指定目标服务器的同步策略", notes = "查询指定目标服务器的同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "targetId", value = "目标harbor服务器id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "查询同步策略的同步任务", notes = "查询某个同步策略的同步任务列表")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "pageSize", value = "分页大小", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "page", value = "分页页码", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "policyID", value = "同步策略id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "startTime", value = "开始时间", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "endTime", value = "结束时间", paramType = "query",dataType = "String"),
			@ApiImplicitParam(name = "status", value = "同步状态", paramType = "query",dataType = "String")})
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
		if (StringUtils.isNotBlank(page)&&StringUtils.isNotBlank(pageSize)) {
			queryParams.put("page", page);
			queryParams.put("page_size", pageSize);
		}
		if (StringUtils.isNotBlank(startTime)) {queryParams.put("start_time", startTime);}
		if (StringUtils.isNotBlank(endTime)) {queryParams.put("end_time", endTime);}
		if (StringUtils.isNotBlank(policyID)) {queryParams.put("policy_id", policyID);}
		if (StringUtils.isNotBlank(status)) {queryParams.put("status", status);}
		return this.harborReplicationService.listPolicyJobs(harborHost, queryParams);
	}

	/**
	 * 查看harbor同步任务的具体子任务的日志
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "查询同步策略的具体某个同步任务", notes = "查询同步策略的具体某个同步任务")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "jobID", value = "同步任务id", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "启动同步策略", notes = "启动同步策略")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyID", value = "同步策略id", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "获取同步策略状态", notes = "获取同步策略状态")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "policyID", value = "同步策略id", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "查询同步策略详情", notes = "查询同步策略详情")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String"),
			@ApiImplicitParam(name = "targetId", value = "目标harbor服务器id", paramType = "path",dataType = "Integer")})
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
	@ApiOperation(value = "单个镜像复制", notes = "单个镜像复制")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "harborHost", value = "源harbor服务器地址", paramType = "path",dataType = "String")})
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
	@ApiOperation(value = "镜像细力度同步", notes = "镜像细力度同步")
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

	/**
	 * 检测规则同名
	 * @return
	 * @throws Exception
	 */
	@ApiOperation(value = "检测规则同名", notes = "检测规则同名")
	@RequestMapping(value = "/harbor/{harborHost}/replicationpolicies/checkname", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil checkUsingPoliciesName(@RequestParam String name,
												   @PathVariable(value = "harborHost") String harborHost)throws Exception{
		return ActionReturnUtil.returnSuccessWithData(harborReplicationService.checkUsingPoliciesName(harborHost,name));
	}

}
