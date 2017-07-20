package com.harmonycloud.api.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.ParsedIngressListDto;
import com.harmonycloud.dto.business.ParsedIngressListUpdateDto;
import com.harmonycloud.dto.business.SvcRouterDto;
import com.harmonycloud.dto.business.SvcRouterUpdateDto;
import com.harmonycloud.dto.svc.CheckPort;
import com.harmonycloud.dto.svc.SvcTcpDto;
import com.harmonycloud.service.application.RouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by czm on 2017/1/18.
 * 
 * jmi 补充
 */
@Controller
@RequestMapping("/router")
public class RouterController {

    @Autowired
	private RouterService routerService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ResponseBody
	@RequestMapping(value = "/ing", method = { RequestMethod.GET })
	public ActionReturnUtil ingList(@RequestParam(value = "namespace") String namespace) throws Exception{// 方法命名
        try {
        	if (StringUtils.isEmpty(namespace)) {
    			return ActionReturnUtil.returnErrorWithMsg("namespace can not be null");
    		}
        	List<ParsedIngressListDto> data = routerService.ingList(namespace);
        	return ActionReturnUtil.returnSuccessWithData(data);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
	}

	@ResponseBody
	@RequestMapping(value = "/ing", method = { RequestMethod.POST })
	public ActionReturnUtil ingCreate(@ModelAttribute ParsedIngressListDto parsedIngressList) throws Exception{
		
		try {
			if (parsedIngressList == null) {
				return ActionReturnUtil.returnErrorWithMsg("params can not be null");
			}
			return routerService.ingCreate(parsedIngressList);
		} catch (Exception e) {
			throw e;
		}
		
	}

	@ResponseBody
	@RequestMapping(value = "/ing", method = RequestMethod.PUT)
	public ActionReturnUtil ingUpdate(@ModelAttribute ParsedIngressListUpdateDto parsedIngressList) throws Exception{
		try {
			return routerService.ingUpdate(parsedIngressList);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getPort", method = { RequestMethod.GET })
	public ActionReturnUtil getPort(@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			return routerService.getPort(tenantId);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/getListPort", method = { RequestMethod.GET })
	public ActionReturnUtil getListPort(@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			return  routerService.getListPort(tenantId);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/checkPort", method = { RequestMethod.POST })
	public ActionReturnUtil checkPort(@RequestParam(value = "port", required = true) String port,@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			return routerService.checkPort(port,tenantId);
		} catch (Exception e) {
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/updatePort", method = { RequestMethod.POST })
	public ActionReturnUtil updatePort(@RequestParam(value = "oldport", required = true) String oldport,@RequestParam(value = "nowport", required = true) String nowport,@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			return routerService.updatePort(oldport,nowport,tenantId);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/delPort", method = { RequestMethod.DELETE })
	public ActionReturnUtil delPort(@RequestParam(value = "port", required = true) String port,@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			return routerService.delPort(port,tenantId);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/ing", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteIng(@RequestParam(value = "namespace", required = true) String namespace,
                                      @RequestParam(value = "name", required = true) String name) throws Exception{

		try {
			logger.info("删除http路由");
			return routerService.ingDelete(namespace, name);
		} catch (Exception e) {
			logger.error("删除http路由错误，namespace="+namespace+",name="+name+",e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/svc", method = RequestMethod.GET)
	public ActionReturnUtil listSvc(@RequestParam(value = "namespace", required = true) String namespace) throws Exception{
		try {
			logger.info("获取svc路由（tcp）列表");
			return routerService.svcList(namespace);
		} catch (Exception e) {
			logger.error("获取svc路由（tcp）列表错误，namespace="+namespace+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/svcbyname", method = RequestMethod.POST)
	public ActionReturnUtil listSvcByName(@ModelAttribute ParsedIngressListDto parsedIngressListDto) throws Exception{
		try {
			logger.info("获取svc路由（tcp）列表");
			return routerService.listSvcByName(parsedIngressListDto);
		} catch (Exception e) {
			logger.error("获取svc路由（tcp）列表错误，"+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/svc", method = RequestMethod.POST)
	public ActionReturnUtil createSvc(@ModelAttribute SvcRouterDto svcRouter)throws Exception {
		try {
			logger.info("创建svc路由（tcp）");
			return routerService.svcCreate(svcRouter);
		} catch (Exception e) {
			logger.error("创建svc路由（tcp）错误"+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/createhttpsvc", method = RequestMethod.POST)
	public ActionReturnUtil createhttpsvc(@ModelAttribute SvcTcpDto svcTcpDto)throws Exception {
		try {
			logger.info("创建svc路由（tcp）");
			return routerService.createhttpsvc(svcTcpDto);
		} catch (Exception e) {
			logger.error("创建svc路由（tcp）错误"+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/createtcp", method = RequestMethod.POST)
	public ActionReturnUtil createTcpSvc(@ModelAttribute SvcTcpDto svcTcpDto)throws Exception {
		try {
			logger.info("创建svc路由（tcp）");
			return routerService.createTcpSvc(svcTcpDto);
		} catch (Exception e) {
			logger.error("创建svc路由（tcp）错误"+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/createhttp", method = RequestMethod.POST)
	public ActionReturnUtil createHttpSvc(@ModelAttribute ParsedIngressListDto parsedIngressList)throws Exception {
		try {
			logger.info("创建svc路由（http）");
			return routerService.createHttpSvc(parsedIngressList);
		} catch (Exception e) {
			logger.error("创建svc路由（http）错误"+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/svc", method = RequestMethod.PUT)
	public ActionReturnUtil updateSvc(@ModelAttribute SvcRouterUpdateDto svcRouterUpdateDto)throws Exception {
		try {
			logger.info("更新svc路由（tcp）");
			return routerService.svcUpdate(svcRouterUpdateDto);
		} catch (Exception e) {
			logger.error("更新svc路由（tcp）错误");
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/svc", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteSvc(@RequestParam(value = "namespace", required = true) String namespace,
                                      @RequestParam(value = "name", required = true) String name) throws Exception{
		try {
			logger.info("删除svc路由（tcp）");
			return routerService.svcDelete(namespace,name);
		} catch (Exception e) {
			logger.error("删除svc路由（tcp）错误，namespace="+namespace+",name="+name+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value="/deltcp", method = RequestMethod.DELETE)
	public ActionReturnUtil deleteTcpSvc(@RequestParam(value = "namespace", required = true) String namespace,
                                      @RequestParam(value = "name", required = true) String name,@RequestParam(value = "port", required = true) String port,@RequestParam(value = "tenantId", required = true) String tenantId) throws Exception{
		try {
			logger.info("删除svc路由（tcp）");
			return routerService.deleteTcpSvc(namespace,name,port,tenantId);
		} catch (Exception e) {
			logger.error("删除svc路由（tcp）错误，namespace="+namespace+",name="+name+",e="+e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/entry", method = RequestMethod.GET)
	public ActionReturnUtil getEntry() throws Exception{
		try {
			logger.info("获取入口");
			return routerService.getEntry();
		} catch (Exception e) {
			logger.error("获取入口错误,e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/host", method = RequestMethod.GET)
	public ActionReturnUtil getHost() throws Exception{
		try {
			logger.info("获取host");
			return routerService.getHost();
		} catch (Exception e) {
			logger.error("获取host错误,e="+e.getMessage());
			throw e;
		}
	}

}
