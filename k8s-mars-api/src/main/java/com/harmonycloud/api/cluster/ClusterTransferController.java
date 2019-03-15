package com.harmonycloud.api.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.ClusterTransferDetailDto;
import com.harmonycloud.dto.cluster.ClusterTransferDto;
import com.harmonycloud.service.application.ClusterTransferService;
import com.harmonycloud.service.tenant.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Api(value = "ClusterTransferController", description = "集群迁移接口")
@RequestMapping("/clusters")
@Controller
public class ClusterTransferController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ClusterTransferService clusterTransferService;

	@Autowired
	private TenantService tenantService;

/*	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "迁移服务", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/transferDeploy", method = RequestMethod.POST)
	public ActionReturnUtil transferDeployService(@RequestBody DeploymentTransferDto deploymentTransferDto) throws Exception {
	    logger.info("transfer deploy service");
	    return clusterTransferService.transferDeployService(deploymentTransferDto);
	}*/

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "迁移集群", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/transferCluster", method = RequestMethod.POST)
	public ActionReturnUtil transferCluster(@RequestBody List<ClusterTransferDto> clusterTransferDto) throws Exception {
		logger.info("transfer {}",clusterTransferDto.toString());
		return clusterTransferService.transferCluster(clusterTransferDto);
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "查询迁移的集群详情", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/getTransferCluster", method = RequestMethod.POST)
	public ActionReturnUtil getTransferCluster(@RequestBody ClusterTransferDetailDto clusterTransferDto) throws Exception {
		logger.info("getTransferCluster,{}",clusterTransferDto.toString());
		return clusterTransferService.getTransferCluster(clusterTransferDto);
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "查询迁移的服务详情", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/getDeployDetail", method = RequestMethod.POST)
	public ActionReturnUtil getDeployDetail(@RequestBody ClusterTransferDetailDto clusterTransferDto) throws Exception{
		logger.info("getDeployDetail,{}",clusterTransferDto.toString());
		return clusterTransferService.getDeployDetail(clusterTransferDto);
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "查询迁移的历史纪录", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/getDeployDetailBackUp", method = RequestMethod.POST)
	public ActionReturnUtil getDeployDetailBackUp(@RequestBody ClusterTransferDetailDto clusterTransferDto) throws Exception{
		logger.info("getDeployDetailBackUp,{}",clusterTransferDto.toString());
		return clusterTransferService.getDeployDetailBackUp(clusterTransferDto);
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "根据集群Id查询租户", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/getTenantIdByCluster", method = RequestMethod.POST)
	public ActionReturnUtil getTenantIdByCluster(@RequestBody ClusterTransferDetailDto clusterTransferDto) throws Exception {
		logger.info("transfer cluster service");
		return ActionReturnUtil.returnSuccessWithData(tenantService.queryTenantByClusterId(clusterTransferDto.getClusterId()));
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "获取租户下分区内的服务列表", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/tenants/namespaces/deploys", method = RequestMethod.POST)
	public ActionReturnUtil getDeploymentByNamespaceAndTenant(@RequestBody List<ClusterTransferDto> clusterTransferDto) throws Exception {
		logger.info("get service");
		return clusterTransferService.getDeployAndStatefulSet(clusterTransferDto);
	}
}
