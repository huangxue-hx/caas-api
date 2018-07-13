package com.harmonycloud.api.application;

import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.application.ParsedIngressListDto;
import com.harmonycloud.dto.application.ParsedIngressListUpdateDto;
import com.harmonycloud.dto.application.SvcRouterDto;
import com.harmonycloud.dto.application.TcpDeleteDto;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.RouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by czm on 2017/1/18.
 * 
 * jmi 补充
 */
@Controller
@RequestMapping("/tenants/{tenantId}/projects/{projectId}/deploys")
public class RouterController {

    @Autowired
	private RouterService routerService;
    
    @Autowired
	HttpSession session;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ResponseBody
	@RequestMapping(value = "/{deployName}/ingress", method = { RequestMethod.POST })
	public ActionReturnUtil ingCreate(@ModelAttribute ParsedIngressListDto parsedIngressList, @PathVariable(value = "deployName") String deployName) throws Exception{
		logger.info("创建ingress");
		parsedIngressList.setServiceName(deployName);
		return routerService.ingCreate(parsedIngressList);
		
	}

	/*@ResponseBody
	@RequestMapping(value = "/{deployName}/ingress", method = RequestMethod.PUT)
	public ActionReturnUtil ingUpdate(@ModelAttribute ParsedIngressListDto parsedIngressList) throws Exception{
		logger.info("修改ingress");
		return routerService.ingUpdate(parsedIngressList);
	}*/

	/**
	 * 在服务所属的集群分配一个未使用的端口
	 * @param namespace
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "/ports", method = { RequestMethod.GET })
	public ActionReturnUtil getPort(@RequestParam(value = "namespace") String namespace) throws Exception{
		return routerService.getPort(namespace);
	}
	
	@ResponseBody
	@RequestMapping(value = "/ports/check", method = { RequestMethod.POST })
	public ActionReturnUtil checkPort(@RequestParam(value = "port", required = true) String port,@RequestParam(value = "namespace", required = true) String namespace) throws Exception{
		logger.info("检查对外端口占用，port:{},namespace:{}", port, namespace);
		return routerService.checkPort(port,namespace);
	}

	@ResponseBody
	@RequestMapping(value = "/ports/{port}", method = { RequestMethod.POST })
	public ActionReturnUtil updatePort(@PathVariable(value = "port") String oldPort,
									   @RequestParam(value = "nowPort", required = true) String nowPort,
									   @RequestParam(value = "namespace", required = true) String namespace) throws Exception{
		logger.info("更新对外端口，oldPort:{},nowPort:{}", oldPort, nowPort);
		return routerService.updatePort(oldPort, nowPort, namespace);
	}
	
	@ResponseBody
	@RequestMapping(value = "/ports/{port}", method = { RequestMethod.DELETE })
	public ActionReturnUtil delPort(@PathVariable(value = "port") String port,@RequestParam(value = "namespace", required = true) String namespace) throws Exception{
		logger.info("删除对外端口占用，port:{},namespace:{}", port, namespace);
		return routerService.delPort(port, namespace);
	}
	
	@ResponseBody
	@RequestMapping(value = "/{deployName}/ingress", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteIng(@RequestParam(value = "namespace", required = true) String namespace,
									  @PathVariable(value = "deployName") String deployName,
                                      @RequestParam(value = "name", required = true) String name) throws Exception{

		try {
			logger.info("删除http路由");
			return routerService.ingDelete(namespace, name, deployName);
		} catch (Exception e) {
			logger.error("删除http路由错误，namespace="+namespace+",name="+name+",e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/{deployName}/rules", method = RequestMethod.GET)
	public ActionReturnUtil listSvc(@RequestParam(value = "namespace", required = true) String namespace) throws Exception{
		try {
			return routerService.svcList(namespace);
		} catch (Exception e) {
			logger.error("获取svc路由（tcp）列表错误，namespace="+namespace+",e="+e.getMessage());
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value="/{deployName}/rules", method = RequestMethod.PUT)
	public ActionReturnUtil updateTcpUdpRule(@PathVariable(value = "deployName") String name,
											 @ModelAttribute SvcRouterDto svcRouterDto)throws Exception {
		try {
			logger.info("更新svc路由");
			svcRouterDto.setName(name);
			return routerService.updateSystemRouteRule(svcRouterDto);
		} catch (Exception e) {
			logger.error("更新svc路由错误");
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/rules", method = RequestMethod.GET)
	public ActionReturnUtil listRouter(@RequestParam(value = "namespace") String namespace,
									  @RequestParam(value = "nameList") String nameList) throws Exception{
		String userName = (String) session.getAttribute("username");
		if (userName == null) {
			throw new K8sAuthException(Constant.HTTP_401);
		}
		return routerService.listExposedRouterWithIngressAndNginx(namespace, nameList);
	}

	@ResponseBody
	@RequestMapping(value="/{deployName}/rules", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteTcpUdpRule(@ModelAttribute TcpDeleteDto tcpDeleteDto,@PathVariable(value = "deployName") String deployName)throws Exception {
		logger.info("删除服务对外的规则");
		return routerService.deleteSystemRouteRule(tcpDeleteDto,deployName);
	}

	@ResponseBody
	@RequestMapping(value = "/ports/range", method = RequestMethod.GET)
	public ActionReturnUtil getPortRange(@RequestParam(value = "namespace") String namespace) throws Exception {
		return ActionReturnUtil.returnSuccessWithData(routerService.getPortRange(namespace, null));
	}
	
//	@ResponseBody
//	@RequestMapping(value = "/svcbyname", method = RequestMethod.POST)
//	public ActionReturnUtil listSvcByName(@ModelAttribute ParsedIngressListDto parsedIngressListDto) throws Exception{
//		try {
//			logger.info("获取svc路由（tcp）列表");
//			return routerService.listSvcByName(parsedIngressListDto);
//		} catch (Exception e) {
//			logger.error("获取svc路由（tcp）列表错误，"+",e="+e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}
	
//	@ResponseBody
//	@RequestMapping(value="/rules", method = RequestMethod.POST)
//	public ActionReturnUtil createRule(@ModelAttribute SvcRouterDto svcRouter)throws Exception {
//		try {
//			logger.info("创建config的rule");
//			return routerService.svcCreate(svcRouter);
//		} catch (Exception e) {
//			logger.error("创建config的rule 错误"+",e="+e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}




//	@ResponseBody
//	@RequestMapping(value="/rules", method = RequestMethod.POST)
//	public ActionReturnUtil createTcpSvc(@ModelAttribute SvcTcpDto svcTcpDto)throws Exception {
//		return routerService.createTcpSvc(svcTcpDto);
//	}
	
//	@ResponseBody
//	@RequestMapping(value="/rules/{rulename}", method = RequestMethod.DELETE)
//	public ActionReturnUtil deleteSvc(@RequestParam(value = "namespace", required = true) String namespace,
//                                      @PathVariable(value = "rulename") String name) throws Exception{
//		try {
//			logger.info("删除svc路由（tcp）");
//			return routerService.svcDelete(namespace,name);
//		} catch (Exception e) {
//			logger.error("删除svc路由（tcp）错误，namespace="+namespace+",name="+name+",e="+e.getMessage());
//			e.printStackTrace();
//			throw e;
//		}
//	}
//
//	@ResponseBody
//	@RequestMapping(value="/deltcp", method = RequestMethod.DELETE)
//	public ActionReturnUtil deleteTcpSvc(@ModelAttribute TcpDeleteDto tcpDeleteDto) throws Exception{
//		if(tcpDeleteDto == null){
//			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.INVALID_PARAMETER);
//		}
//		if(StringUtils.isEmpty(tcpDeleteDto.getName())){
//			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.PARAMETER_VALUE_NOT_PROVIDE, "tcp", true);
//		}
//		if(StringUtils.isEmpty(tcpDeleteDto.getNamespace())){
//			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.NAMESPACE_NOT_BLANK);
//		}
//		if(StringUtils.isEmpty(tcpDeleteDto.getTenantId())){
//			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.TENANT_NOT_BLANK);
//		}
//		return routerService.deleteTcpSvc(tcpDeleteDto.getNamespace(), tcpDeleteDto.getName(), tcpDeleteDto.getPorts(), tcpDeleteDto.getTenantId());
//	}

}
