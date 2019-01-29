package com.harmonycloud.api.cluster;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.harmonycloud.dto.cluster.ClusterTransferDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.DeploymentTransferDto;
import com.harmonycloud.service.application.ClusterTransferService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

@Api(value = "ClusterTransferController", description = "集群迁移接口")
@RequestMapping("/clusters")
@Controller
public class ClusterTransferController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private ClusterTransferService clusterTransferService;
			

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "迁移服务", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/transferDeploy", method = RequestMethod.POST)
	public ActionReturnUtil transferDeployService(@RequestBody DeploymentTransferDto deploymentTransferDto) throws Exception {
	    logger.info("transfer deploy service");
	    return clusterTransferService.transferDeployService(deploymentTransferDto);
	}

	@ApiResponse(code = 200, message = "success", response = ActionReturnUtil.class)
	@ApiOperation(value = "迁移服务", response = ActionReturnUtil.class, httpMethod = "POST", consumes = "", produces = "", notes = "")
	@ResponseBody
	@RequestMapping(value = "/transferCluster", method = RequestMethod.POST)
	public ActionReturnUtil transferCluster(@RequestBody List<ClusterTransferDto> clusterTransferDto) throws Exception {
		logger.info("transfer cluster service");
		return clusterTransferService.transferCluster(clusterTransferDto);
	}
}
