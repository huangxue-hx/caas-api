package com.harmonycloud.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.user.RoleService;

@Controller
public class InitController {
	
	@Autowired
	private RoleService roleService;
	
	/**
	 * 初始化clusterRole
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value="/clusterrole/initialization", method=RequestMethod.GET)
	@ResponseBody
	public ActionReturnUtil initClusterRole() throws Exception {
		return roleService.initClusterRole();
	}
}
