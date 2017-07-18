package com.harmonycloud.api.open;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.container.ContainerBriefDto;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 
 * @author jmi
 *
 */
@RequestMapping("/openapi")
@Controller
public class OpenApiController {

	@Autowired
	DeploymentsService dpService;
	
	@Autowired
	ClusterService clusterService;
	@Autowired
	UserService userService;

	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 查询namespace下容器名称列表
	 *
	 * @param namespace
	 * @return
	 */
	@RequestMapping(value = "/namespace/containers", method = RequestMethod.GET)
	public ResponseEntity<List<ContainerBriefDto>> getContainerList(@RequestParam(value = "tenantId") String tenantId,
																	@RequestParam(value = "namespace") String namespace)
			throws Exception {

		Cluster cluster = clusterService.findClusterByTenantId(tenantId);
		if(cluster == null){
			logger.info("未找到租户对应的集群信息,tenantId:{}", tenantId);
			return new ResponseEntity("未找到租户对应的集群信息", HttpStatus.BAD_REQUEST);
		}
		User user = userService.getUser("admin");
		Map<String, Object> headers  = new HashMap<>();
		headers.put("Authorization", "Bearer " + user.getToken());
		ActionReturnUtil result = dpService.namespaceContainer(namespace, cluster, headers);
		if((boolean)result.get("success")){
			List<ContainerBriefDto> containers = new ArrayList<>();
			List<ContainerOfPodDetail> containerOfPodDetails = (List<ContainerOfPodDetail>)result.get("data");
			for(ContainerOfPodDetail containerOfPodDetail : containerOfPodDetails){
				containers.add(new ContainerBriefDto(containerOfPodDetail.getName(),
						containerOfPodDetail.getDeploymentName()));
			}
			return new ResponseEntity(containers, HttpStatus.OK);
		}else{
			return new ResponseEntity("查找失败", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
