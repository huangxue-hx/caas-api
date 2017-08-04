package com.harmonycloud.api.open;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.container.ContainerBriefDto;
import com.harmonycloud.dto.event.EventBriefDto;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EventService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.KubeModuleStatus;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.platform.service.ci.JobService;
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

import java.util.*;


/**
 * 
 * @author jmi
 * 此controller下的接口不需要进行用户登录验证
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
	@Autowired
	EventService eventService;
	@Autowired
	PodService podService;
    @Autowired
    JobService jobService;

	
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

	/**
	 * 查询所有集群下的节点的重启事件
	 * @return
	 */
	@RequestMapping(value = "/node/restartevents", method = RequestMethod.GET)
	public ResponseEntity<List<EventBriefDto>> getNodeRestartEvents() {
        try {
			Map<String, List<Event>> eventMap = eventService.getNodeRestartEvents();
			List<EventBriefDto> eventBriefDtos = new ArrayList<>();
			for(Map.Entry<String, List<Event>> entry : eventMap.entrySet()){
				String cluster = entry.getKey();
				List<Event> events = entry.getValue();
				for(Event event : events){
					EventBriefDto eventBriefDto = new EventBriefDto(cluster, event.getReason(), event.getMessage(),
							event.getFirstTimestamp(), event.getInvolvedObject().getName());
					eventBriefDtos.add(eventBriefDto);
				}
			}
			return new ResponseEntity(eventBriefDtos, HttpStatus.OK);
		}catch(Exception e){
			logger.error("查找集群节点重启事件失败,", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 查询所有集群下kube-system核心组件的状态和重启等信息
	 * @return
	 */
	@RequestMapping(value = "/kubemodule/status", method = RequestMethod.GET)
	public ResponseEntity<List<KubeModuleStatus>> getKubeModuleStatus() {
		try {
			return new ResponseEntity(podService.getKubeModuleStatus(),HttpStatus.OK);
		}catch(Exception e){
			logger.error("获取核心组件状态信息失败,", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

    @RequestMapping(value = "/cicd/postBuild", method = RequestMethod.GET)
    public ResponseEntity postBuild(@RequestParam(value = "id")Integer id, @RequestParam(value = "buildNum")Integer buildNum){
        jobService.postBuild(id, buildNum);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/cicd/stageSync", method = RequestMethod.GET)
    public ResponseEntity stageSync(@RequestParam(value = "id")Integer id, @RequestParam(value = "buildNum")Integer buildNum){
        jobService.stageSync(id, buildNum);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/cicd/deploy", method = RequestMethod.GET)
    public ResponseEntity deploy(@RequestParam(value = "buildNum")Integer buildNum, @RequestParam(value = "stageId")Integer stageId){
        try {
            jobService.deploy(stageId, buildNum);
        } catch (Exception e) {
            return new ResponseEntity("fail",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

}
