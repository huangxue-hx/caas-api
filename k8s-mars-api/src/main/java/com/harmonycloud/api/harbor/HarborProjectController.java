package com.harmonycloud.api.harbor;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.bean.HarborUserBinding;
import com.harmonycloud.service.platform.service.harbor.HarborProjectService;
import com.harmonycloud.service.platform.service.harbor.HarborSecurityService;

/**
 * 
 * @author jmi
 *
 */
@Controller
@RequestMapping(value = "/harborProject")
public class HarborProjectController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private HarborProjectService harborProjectService;
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	private HarborSecurityService harborSecurityService;
	
	@ResponseBody
	@RequestMapping(value="/user/image")
	public ActionReturnUtil getUserImage(@RequestParam(value="namespace") String namespace) throws Exception{
		try {
			logger.info("获取用户的所有镜像");
			return harborProjectService.getAllImageOfUser(namespace, session.getAttribute("username").toString());
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/role",method=RequestMethod.GET)
	public ActionReturnUtil listHarborRole()throws Exception{
		try {
			return this.harborProjectService.listHarborRole();
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/role",method=RequestMethod.POST)
	public ActionReturnUtil bindingHarborUser(@ModelAttribute HarborUserBinding bindingUser)throws Exception{
		try {
			return this.harborProjectService.bindingHarborUser(bindingUser);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/role",method=RequestMethod.DELETE)
	public ActionReturnUtil deleteHarborUser(@ModelAttribute HarborUserBinding bindingUser)throws Exception{
		try {
			return this.harborProjectService.deleteHarborUser(bindingUser);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/clairStatistcsByNamespace", method=RequestMethod.GET)
	public ActionReturnUtil clairStatistcsByNamespace(@RequestParam(value="namespace") String namespace)throws Exception {
		try {
			return harborProjectService.getStatistcsByNamespace(namespace);
		} catch (Exception e) {
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/refresh", method=RequestMethod.GET)
	public ActionReturnUtil refreshImageRepo() throws Exception{
		try {
			return harborSecurityService.refreshImageRepo();
		} catch (Exception e) {
			throw e;
		}
	}
}



