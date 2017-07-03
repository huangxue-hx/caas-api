package com.harmonycloud.api.harbor;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.service.platform.bean.*;
import com.harmonycloud.service.platform.integrationService.HarborIntegrationService;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.service.platform.serviceImpl.harbor.HarborProjectReplicationServiceImpl;
import com.harmonycloud.service.platform.client.HarborClient;
import com.harmonycloud.service.platform.service.harbor.HarborProjectReplicationService;
import com.harmonycloud.common.util.JsonUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by andy on 17-1-19.
 */
@Controller
public class HarborProjectReplicationController {

    @Autowired
    private HarborProjectReplicationServiceImpl harborProjectReplicationServiceImpl;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 新建跨harbor同步对象
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/createTarget", method = RequestMethod.POST)
    @ResponseBody
	public ActionReturnUtil createTarget(@ModelAttribute HarborReplicationTarget harborReplicationTarget )throws Exception{
		try {
			logger.info("新建镜像仓库同步对象");
			return this.harborProjectReplicationServiceImpl.createTarget(harborReplicationTarget);
		} catch (Exception e) {
			logger.error("新建镜像仓库同步对象失败:endpoint= "+ harborReplicationTarget.getEndpoint()+",targetuser ="+harborReplicationTarget.getName());
			throw e;

		}
	}

    /**
     * 测试账户是否有权限
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/createTarget/ping", method = RequestMethod.POST)
    @ResponseBody
	//public ActionReturnUtil pingEndpoint(@RequestParam String endpoint,String targetusername,String targetuserpassword)throws Exception{
	public ActionReturnUtil pingEndpoint(@RequestParam(value = "endpoint") String endpoint,
										 @RequestParam(value = "username") String targetusername,
										 @RequestParam(value = "password") String targetuserpassword)throws Exception{
		try {
			logger.info("测试镜像仓库同步对象账户是否有权限");
			return this.harborProjectReplicationServiceImpl.pingEndpoint(endpoint, targetusername, targetuserpassword);
		} catch (Exception e) {
			logger.error("测试镜像仓库同步对象账户是否有权限失败:endpoint= "+ endpoint+",targetuser="+targetusername);
			throw e;
		}
	}
    
    /**
     * 删除跨harbor同步对象
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/deleteTarget", method = RequestMethod.DELETE)
    @ResponseBody
	public ActionReturnUtil deleteTarget(@RequestParam(value="targetId") String targetID)throws Exception{
		try {
			logger.info("删除镜像仓库同步对象");
			return this.harborProjectReplicationServiceImpl.deleteTarget(Integer.valueOf(targetID));
		} catch (Exception e) {
			logger.error("删除镜像仓库同步对象失败:targetID= "+ targetID);
			throw e;
		}
	}
    /**
     * 列举跨harbor同步对象 ok
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/listTargets", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listTargets()throws Exception{
		try {
			logger.info("列举镜像仓库同步对象");
			//return this.harborProjectReplicationServiceImpl.listTargets();
			ActionReturnUtil listResponse =this.harborProjectReplicationServiceImpl.listTargets();
			if ((boolean)listResponse.get("success") == true) {
				Object convertJson = JsonUtil.jsonToPojo(listResponse.get("data").toString(),Object.class);
				listResponse.put("data", convertJson);
				return listResponse;
			}else{
				return listResponse;
			}
		} catch (Exception e) {
			logger.error("列举镜像仓库同步对象失败");
			throw e;
		}
	}
    /**
     * 新建跨harbor同步任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/createPolicy", method = RequestMethod.POST)
    @ResponseBody
	public ActionReturnUtil createPolicy(@ModelAttribute HarborReplicationPolicy harborReplicationPolicy)throws Exception{
		try {
			logger.info("新建镜像仓库同步任务");
			return this.harborProjectReplicationServiceImpl.createPolicy(harborReplicationPolicy);
		} catch (Exception e) {
			logger.error("新建镜像仓库同步任务失败:policy="+harborReplicationPolicy.getName());
			throw e;
		}
	}
    /**
     * 删除跨harbor同步任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/deletePolicy", method = RequestMethod.DELETE)
    @ResponseBody
	public ActionReturnUtil deletePolicy(@RequestParam(value="policyId")String policyID)throws Exception{
		try {
			logger.info("删除镜像仓库同步任务");
			return this.harborProjectReplicationServiceImpl.deletePolicy(Integer.valueOf(policyID));
		} catch (Exception e) {
			logger.error("删除镜像仓库同步任务失败:policyID="+policyID);
			throw e;
		}
	}
    /**
	   * 列举指定project跨harbor同步任务
	   * @return
	   * @throws Exception
	   */
    @RequestMapping(value = "/harborProject/replication/listProjectPolicies", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listProejctPolicies(@RequestParam(value="project_id")String projectID)throws Exception{
		try {
			logger.info("列举指定project镜像仓库同步任务");
			return this.harborProjectReplicationServiceImpl.listProjectPolicies(Integer.valueOf(projectID));
		} catch (Exception e) {
			logger.error("列举指定project镜像仓库同步失败:projectID="+projectID);
			throw e;
		}
	}
	/**
	 * 列举指定target跨harbor同步任务
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/replication/listTargetPolicies", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listTargetPolicies(@RequestParam(value="target_id")String targetID)throws Exception{
		try {
			logger.info("列举指定target镜像仓库同步任务");
			return this.harborProjectReplicationServiceImpl.listTargetPolicies(Integer.valueOf(targetID));
		} catch (Exception e) {
			logger.error("列举指定target镜像仓库同步失败:targetID="+targetID);
			throw e;
		}
	}
    /**
     * 列举所有跨harbor同步任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/listPolicies", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listPolicies()throws Exception{
		try {
			logger.info("列举所有镜像仓库同步任务");
			return this.harborProjectReplicationServiceImpl.listPolicies();
		} catch (Exception e) {
			logger.error("列举所有镜像仓库同步任务失败");
			throw e;
		}
	}
    /**
     * 查看指定harbor同步任务的所有子任务
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/listPolicyJobs", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listPolicyJobs(@RequestParam(value = "page",required = false) String page,
										   @RequestParam(value = "page_size",required = false) String pageSize,
										   @RequestParam(value = "policy_id") String policyID,
										   @RequestParam(value = "start_time",required = false) String start_time,
										   @RequestParam(value = "end_time",required = false) String end_time,
										   @RequestParam(value = "status",required = false) String status)throws Exception{
		try {
			logger.info("查看指定镜像仓库同步任务的所有子任务");
			return this.harborProjectReplicationServiceImpl.listPolicyJobs(Integer.valueOf(policyID), page, pageSize, end_time, start_time, status);
		} catch (Exception e) {
			logger.error("查看指定镜像仓库同步任务的所有子任务失败: policyId="+policyID);
			throw e;
		}
	}
    /**
     * 查看harbor同步任务的具体子任务的日志
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/listJobLogs", method = RequestMethod.GET)
    @ResponseBody
	public ActionReturnUtil listJobLogs(@RequestParam(value="jobId")String jobID)throws Exception{
		try {
			logger.info("查看镜像仓库同步任务的具体子任务的日志");
			return this.harborProjectReplicationServiceImpl.listJobLogs(Integer.valueOf(jobID));
		} catch (Exception e) {
			logger.error("查看指定镜像仓库同步任务的所有子任务的日志失败: jobId="+jobID);
			throw e;
		}
	}
    /**
     * 更改跨harbor同步任务是否有效
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/harborProject/replication/enablement", method = RequestMethod.PUT)
    @ResponseBody
	public ActionReturnUtil enablePlicy(@RequestParam(value="policyId")String policyID,@ModelAttribute HarborReplicationPolicyEnable harborReplicationPolicyEnable)throws Exception{
		try {
			logger.info("更改跨镜像仓库同步任务是否有效");
			return this.harborProjectReplicationServiceImpl.updatePolicyEnable(Integer.valueOf(policyID), harborReplicationPolicyEnable);
		} catch (Exception e) {
			logger.error("更改跨镜像仓库同步任务是否有效失败:policyId="+policyID);
			throw e;
		}
	}

	/**copy images
	 *
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/copyImage", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil copyImage(@ModelAttribute HarborImageCopy harborImageCopy)throws Exception{
		try {
			logger.info("复制镜像");
			return this.harborProjectReplicationServiceImpl.copyImage(harborImageCopy);
		} catch (Exception e) {
			logger.info("复制镜像失败:image ="+harborImageCopy.getSrc_repo_name()+":"+harborImageCopy.getSrc_tag());
			throw e;
		}
	}

	/**
	 * 查看harbor同步任务的status
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/replication/policyStatus", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil getPolicyStatus(@RequestParam(value="policyId")String policyID)throws Exception{
		try {
			logger.info("查看镜像仓库同步任务的status");
			return this.harborProjectReplicationServiceImpl.policyStatus(Integer.valueOf(policyID));
		} catch (Exception e) {
			logger.info("查看镜像仓库同步任务的status失败:policyID"+policyID);
			throw e;
		}
	}

	/**
	 * 列举指定target跨harbor同步任务detail
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/replication/listTargetPoliciesDetail", method = RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil listTargetPoliciesDetail(@RequestParam(value="target_id")String targetID)throws Exception{
		try {
			logger.info("列举指定target镜像仓库同步任务detail");
			return this.harborProjectReplicationServiceImpl.listPoliciesDetail(Integer.valueOf(targetID));
		} catch (Exception e) {
			logger.error("列举指定target镜像仓库同步detail失败:targetID="+targetID);
			throw e;
		}
	}

	/**
	 * 新建跨harbor同步target
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/replication/newTarget", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil newTarget(@ModelAttribute HarborReplicationTarget harborReplicationTarget )throws Exception{
		try {
			logger.info("新建镜像仓库同步target");
			return this.harborProjectReplicationServiceImpl.newTarget(harborReplicationTarget);
		} catch (Exception e) {
			logger.error("新建镜像仓库同步target失败:endpoint= "+ harborReplicationTarget.getEndpoint()+",targetuser ="+harborReplicationTarget.getName());
			throw e;

		}
	}
	/**
	 * 新建跨harbor同步policy细粒度
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/harborProject/partialreplication/createPolicy", method = RequestMethod.POST)
	@ResponseBody
	public ActionReturnUtil createPartialPolicy(@RequestBody Map<String,Object> request)throws Exception{
		Map<String, Object> policyMap =request;
		try {
			logger.info("新建镜像仓库同步任务");
			System.out.print(request);
		//	String dataJson = JsonUtil.convertToJson("");
		//	policyMap = JsonUtil.jsonToMap(dataJson);
			return this.harborProjectReplicationServiceImpl.createPartialPolicy(policyMap);
		} catch (Exception e) {
			logger.error("新建镜像仓库同步任务失败:partialPolicy=" +policyMap.get("name"));
			throw e;
		}
	}
}
