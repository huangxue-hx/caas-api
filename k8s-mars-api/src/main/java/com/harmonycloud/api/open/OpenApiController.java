package com.harmonycloud.api.open;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dto.config.ControllerUrlMapping;
import com.harmonycloud.dto.config.MethodUrlMapping;
import com.harmonycloud.k8s.bean.DeploymentList;
import com.harmonycloud.k8s.bean.cluster.Cluster;
import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.container.ContainerBriefDto;
import com.harmonycloud.dto.event.EventBriefDto;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.service.application.DeploymentsService;
import com.harmonycloud.service.application.EventService;
import com.harmonycloud.service.cluster.ClusterService;
import com.harmonycloud.service.cluster.LoadbalanceService;
import com.harmonycloud.service.platform.bean.ContainerOfPodDetail;
import com.harmonycloud.service.platform.bean.ContainerWithStatus;
import com.harmonycloud.service.platform.bean.KubeModuleStatus;
import com.harmonycloud.service.platform.bean.PodDetail;
import com.harmonycloud.service.platform.service.PodService;
import com.harmonycloud.service.platform.service.ci.JobService;
import com.harmonycloud.service.tenant.NamespaceLocalService;
import com.harmonycloud.service.user.UrlDicService;
import com.harmonycloud.service.user.UserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.*;
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
    
    @Autowired
    LoadbalanceService loadbalanceService;
    @Autowired
	NamespaceLocalService namespaceLocalService;
	@Autowired
	UrlDicService urlDicService;

	@Value("#{propertiesReader['sourcecode.dir']}")
	private String sourceCodeDir;
	
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
		//获取平台controller类以及方法的注释信息
		Map<String,String> methodDescs = new HashMap<>();
		Map<String,String> controllerDescs = new HashMap<>();
		String apiControllerDir = sourceCodeDir;
		if(StringUtils.isNotBlank(apiControllerDir)) {
			if (sourceCodeDir.endsWith("/") || sourceCodeDir.endsWith("\\")) {
				apiControllerDir += "k8s-mars-api/src/main/java";
			} else {
				apiControllerDir += "/k8s-mars-api/src/main/java";
			}
			File file = new File(apiControllerDir);
			getMethodDesc(file, methodDescs, controllerDescs);
		}
		//获取spring收集的所有controller的url mapping
		WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
		RequestMappingHandlerMapping rmhp = wc.getBean(RequestMappingHandlerMapping.class);
		Map<RequestMappingInfo, HandlerMethod> map = rmhp.getHandlerMethods();
		Map<String, ControllerUrlMapping> urlMappings = new HashMap<>();
		for (Iterator<RequestMappingInfo> iterator = map.keySet().iterator(); iterator.hasNext();) {
			RequestMappingInfo info = iterator.next();
			HandlerMethod method = map.get(info);
			String controllerName = method.getBean().toString();
			String firstLetter = controllerName.substring(0,1).toUpperCase();
			controllerName = firstLetter + controllerName.substring(1);
			ControllerUrlMapping controllerUrlMapping = urlMappings.get(controllerName);
			if(controllerUrlMapping == null){
				controllerUrlMapping = new ControllerUrlMapping();
				controllerUrlMapping.setControllerName(controllerName);
				controllerUrlMapping.setControllerDesc(controllerDescs.get(controllerName));
				List<MethodUrlMapping> methodUrlMappings = new ArrayList();
				controllerUrlMapping.setMethodUrlMappings(methodUrlMappings);
				urlMappings.put(controllerName, controllerUrlMapping);
			}
			MethodUrlMapping urlMapping = new MethodUrlMapping();
			urlMapping.setControllerName(controllerName);
			urlMapping.setMethodName(method.getMethod().getName());
			urlMapping.setMethodDesc(methodDescs.get(controllerName+CommonConstant.DOT + urlMapping.getMethodName()));
			urlMapping.setRestUrl(info.getPatternsCondition().toString().replace("[","").replace("]",""));
			urlMapping.setHttpMethod(info.getMethodsCondition().toString().replace("[","").replace("]",""));

			MethodParameter[] params = method.getMethodParameters();
			StringBuffer buffer = new StringBuffer();
			for(int i=0;i<params.length;i++) {
				if(i == params.length -1){
					buffer.append(params[i].getParameterType().getSimpleName());
				}else {
					buffer.append(params[i].getParameterType().getSimpleName() + CommonConstant.COMMA);
				}
			}
			urlMapping.setParams(buffer.toString());
			controllerUrlMapping.getMethodUrlMappings().add(urlMapping);
		}
		System.out.println("Controller名称|Controller描述|方法名|Rest Url|Http访问类型|方法描述|方法参数类型");
		for (Iterator<String> iterator = urlMappings.keySet().iterator(); iterator.hasNext();) {
			System.out.print(urlMappings.get(iterator.next()));
		}
		return urlMappings;
	}

	/**
	 * 获取方法或controller类的注释信息
	 * @param file controller类文件
	 * @param methodDescs 方法注释信息
	 * @param controllerDescs controller注释信息
	 * @throws Exception
	 */
	private void getMethodDesc(File file, Map<String,String> methodDescs, Map<String,String> controllerDescs) throws Exception{
		if(!file.exists()){
			return;
		}
    	if(file.isDirectory()){
    		File[] files = file.listFiles();
    		for(File f : files){
				getMethodDesc(f,methodDescs,controllerDescs);
			}
		}else{
            if(file.getName().endsWith(".java")){
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
				String controllerName = file.getName().replace(".java","");
				String line = "";
				boolean descStart = false;
				boolean classStart = false;
				String methodDesc = "";
				String controllerDesc = "";
				while((line = reader.readLine())!=null){
					line = line.trim();
					//注释开始
					if(line.startsWith("/**")){
						descStart = true;
					}
					//类定义开始
					if(line.startsWith("public class")){
						controllerDescs.put(controllerName,controllerDesc.replaceAll("\\*"," ")
								.replaceAll("/"," ").trim());
						controllerDesc = "";
						classStart = true;
						continue;
					}
					if (descStart) {
						//注释开始，将注释拼接到一个字符串，只收集方法或类的描述信息，不包含@param,@return注释信息，等描述信息规范，完整之后再收集
						if(!line.replaceAll(" ","").contains("*@")
								&& (line.startsWith("*") || line.startsWith("/*"))) {
							if (classStart) {
								methodDesc += line.replaceFirst("/*", "");
							} else {
								controllerDesc += line.replaceFirst("/*", "");
							}
						}else{
							descStart = false;
						}
					}
					//注释结束
					if(line.endsWith("*/")){
						descStart = false;
					}
					//方法定义，获取方法名，将方法名对应的描述放入map
					if(classStart && line.startsWith("public")){
						if(line.indexOf("(") ==-1){
							throw new MarsRuntimeException(ErrorCodeMessage.METHOD_FORMAT_ERROR, line ,false);
						}
						String method = line.substring(0, line.indexOf("("));
						method = method.substring(method.lastIndexOf(" ")+1);
						methodDescs.put(controllerName + CommonConstant.DOT + method, methodDesc.replaceAll("\\*"," ")
								.replaceAll("/"," ").trim());
						methodDesc = "";
						descStart = false;
					}else if(classStart && line.startsWith("//") && line.contains("public") && line.contains("ActionReturnUtil")){
						//方法体已经被注释，忽略该方法
						methodDesc = "";
						descStart = false;
					}
				}
			}
		}
	}

	/*public static void main(String[] args){
		Map<String,String> methodDescs = new HashMap<>();
		Map<String,String> controllerDescs = new HashMap<>();
		File file = new File("C:\\code\\k8s-mars\\k8s-mars-api\\src\\main\\java");
		try {
			getMethodDesc(file, methodDescs, controllerDescs);
		}catch (Exception e){
			e.printStackTrace();
		}
		for (Iterator<String> iterator = controllerDescs.keySet().iterator(); iterator.hasNext();) {
			String controllerName = iterator.next();
			System.out.println(controllerName + " " + controllerDescs.get(controllerName));
		}
		for (Iterator<String> iterator = methodDescs.keySet().iterator(); iterator.hasNext();) {
			String methodName = iterator.next();
			System.out.println(methodName + " " + methodDescs.get(methodName));
		}
	}*/

}
