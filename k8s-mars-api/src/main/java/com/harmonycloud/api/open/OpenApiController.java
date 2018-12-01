package com.harmonycloud.api.open;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.cluster.NodeBriefDto;
import com.harmonycloud.dto.config.ControllerUrlMapping;
import com.harmonycloud.dto.container.ContainerBriefDto;
import com.harmonycloud.dto.event.EventBriefDto;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.NodeCondition;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EventService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.LoadbalanceService;
import com.harmonycloud.service.migrate.DataMigrateService;
import com.harmonycloud.service.platform.bean.ContainerWithStatus;
import com.harmonycloud.service.platform.bean.KubeModuleStatus;
import com.harmonycloud.service.platform.bean.PodDetail;
import com.harmonycloud.service.platform.service.NodeService;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.system.ApiService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.UrlDicService;
import com.harmonycloud.service.user.UserService;
import jnr.x86asm.CONDITION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
	private DeploymentsService dpService;
	
	@Autowired
	private ClusterService clusterService;
	@Autowired
	private UserService userService;
	@Autowired
	private EventService eventService;
	@Autowired
	private PodService podService;
    @Autowired
    private JobService jobService;
    
    @Autowired
    private LoadbalanceService loadbalanceService;
    @Autowired
	private NamespaceLocalService namespaceLocalService;
	@Autowired
	private UrlDicService urlDicService;
	@Autowired
	private ApiService apiService;
	@Autowired
	private HttpSession session;
	@Autowired
	private DataMigrateService dataMigrateService;
	@Autowired
	private NodeService nodeService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 查询namespace下容器名称列表
	 *
	 * @param namespace
	 * @return
	 */
	@RequestMapping(value = "/namespace/containers", method = RequestMethod.GET)
	public ResponseEntity<List<ContainerBriefDto>> getContainerList(@RequestParam(value = "namespace") String namespace)
			throws Exception {

		ActionReturnUtil result = dpService.podList(null, namespace);
		if(!result.isSuccess()){
			return new ResponseEntity("查找失败", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		List<ContainerBriefDto> containers = new ArrayList<>();
		List<PodDetail> podDetails = (List<PodDetail>)result.getData();
		Map<String,List<PodDetail>> podMap = podDetails.stream().collect(Collectors.groupingBy(PodDetail::getDeployment));
		for(Map.Entry<String,List<PodDetail>> entry : podMap.entrySet()){
			ContainerBriefDto containerBriefDto = new ContainerBriefDto();
			containerBriefDto.setDeploymentName(entry.getKey());
			containerBriefDto.setPods(entry.getValue().stream().map(PodDetail::getName).collect(Collectors.toList()));
			containerBriefDto.setContainers(entry.getValue().get(0).getContainers().stream().map(ContainerWithStatus::getName).collect(Collectors.toList()));
			containers.add(containerBriefDto);
		}
		return new ResponseEntity(containers, HttpStatus.OK);
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
	 * 查询所有集群下不可用的节点
	 * @return
	 */
	@RequestMapping(value = "/node/unavailable")
	public ResponseEntity<List<NodeBriefDto>> getUnavailableNodes(){
		try {
			List<NodeBriefDto> nodes = nodeService.listUnavailableNodes();
			return new ResponseEntity(nodes, HttpStatus.OK);
		}catch (Exception e){
			logger.error("查找集群不可用节点失败", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@RequestMapping(value = "/getUrlDic", method = RequestMethod.GET)
	public ResponseEntity<List<EventBriefDto>> getUrlDic() throws Exception{
		Map urlMap = urlDicService.getUrlMap();
		return new ResponseEntity(urlMap, HttpStatus.OK);
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

    @RequestMapping(value = "/cicd/preBuild", method = RequestMethod.GET)
    public ResponseEntity preBuild(@RequestParam(value = "id")Integer id, @RequestParam(value = "buildNum")Integer buildNum, @RequestParam(value="dateTime")String dateTime) throws Exception{
        jobService.preBuild(id, buildNum, dateTime);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/cicd/postBuild", method = RequestMethod.GET)
    public ResponseEntity postBuild(@RequestParam(value = "id")Integer id, @RequestParam(value = "buildNum")Integer buildNum){
        jobService.postBuild(id, buildNum);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/cicd/stageSync", method = RequestMethod.GET)
    public ResponseEntity syncStage(@RequestParam(value = "id")Integer id, @RequestParam(value = "buildNum")Integer buildNum){
        jobService.stageSync(id, buildNum);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/cicdjobs/stages/{stageId}", method = RequestMethod.GET)
    public ResponseEntity runStage(@RequestParam(value = "buildNum")Integer buildNum, @PathVariable("stageId")Integer stageId) throws Exception{
		jobService.runStage(stageId, buildNum);
        return new ResponseEntity(HttpStatus.OK);
    }

    
    @RequestMapping(value="/app/stats", method = RequestMethod.GET)
    public ResponseEntity getStatsOfService(@RequestParam(value = "app") String app, @RequestParam(value = "namespace") String namespace) {
    	try {
			return new ResponseEntity(loadbalanceService.getStatsByService(app, namespace),HttpStatus.OK);
		}catch(Exception e){
			logger.error("获取应用的访问指标失败,", e);
			return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }

	/**
	 * 获取平台所有的controller接口信息
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/urlmapping", method = RequestMethod.GET)
	public Map<String, ControllerUrlMapping> getUrlMapping(@RequestParam(value = "order",required = false) String order,
														   HttpServletRequest request) throws Exception{
		return apiService.generateUrlMapping(order, request);
	}

	/**
	 * 数据迁移
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value="/migrate", method = RequestMethod.POST)
	public ResponseEntity migrate(@RequestParam(value = "version",required = false) String version,
								  @RequestParam(value = "execute",required = false) boolean execute) throws Exception{
		String userName = (String) session.getAttribute("username");
		if(!"admin".equals(userName) && !"xfliang".equals(userName)){
			return new ResponseEntity(HttpStatus.FORBIDDEN);
		}
		List<String> messages = dataMigrateService.migrateData(version,execute);
		return new ResponseEntity(messages, HttpStatus.OK);
	}

}
