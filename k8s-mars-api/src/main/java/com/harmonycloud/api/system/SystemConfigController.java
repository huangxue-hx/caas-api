package com.harmonycloud.api.system;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/systemConfig")
public class SystemConfigController {
	
	@Autowired
	SystemConfigService systemConfigService;


	private Logger logger = LoggerFactory.getLogger(this.getClass());


	@ResponseBody
	@RequestMapping(value="/saveLdap", method = RequestMethod.POST)
	public ActionReturnUtil saveLdapConfig(@ModelAttribute LdapConfigDto ldapConfigDto) throws Exception {

		try {
			logger.info("save ldapConfig");
			if (StringUtils.isEmpty(ldapConfigDto)) {
				return ActionReturnUtil.returnErrorWithMsg("LDAP配置信息不能为空");
			}

			systemConfigService.addLdapConfig(ldapConfigDto);
			return ActionReturnUtil.returnSuccessWithMsg("保存LDAP成功");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Failed to save systemConfig.", e.getMessage());
			return ActionReturnUtil.returnErrorWithMsg("保存LDAP失败");
		}
	}


	@ResponseBody
	@RequestMapping(value = "/findById", method = RequestMethod.GET)
	public ActionReturnUtil  getSystemConfigById(@RequestParam(value = "id") String id){
		try {
			SystemConfig systemConfig = systemConfigService.findById(id);
			logger.info("Get SystemConfig By id:{}", id);
			return ActionReturnUtil.returnSuccessWithData(systemConfig);
		} catch (Exception e) {
			logger.error("Failed to get SystemConfig By id", e);
			return ActionReturnUtil.returnSuccessWithData("获取Ldap配置失败");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/findLdap", method = RequestMethod.GET)
	public ActionReturnUtil  findLdap(){
		try {
			LdapConfigDto ldapConfigDto = this.systemConfigService.findByConfigType(CommonConstant.CONFIG_TYPE_LDAP);
			if (StringUtils.isEmpty(ldapConfigDto)) {
				return ActionReturnUtil.returnErrorWithMsg("获取Ldap配置失败");
			}

			return ActionReturnUtil.returnSuccessWithData(ldapConfigDto);
		} catch (Exception e) {
			logger.error("Failed to get SystemConfig", e);
			return ActionReturnUtil.returnSuccessWithData("获取Ldap配置失败");
		}
	}

	@ResponseBody
	@RequestMapping(value = "/trialTime", method = RequestMethod.GET)
	public ActionReturnUtil  trialTime(){
		try {
			SystemConfig systemConfig = this.systemConfigService.findByConfigName(CommonConstant.TRIAL_TIME);
			if (StringUtils.isEmpty(systemConfig)) {
				systemConfig = new SystemConfig();
				systemConfig.setConfigName("trial_time");
				systemConfig.setConfigValue("-1");
				systemConfig.setConfigType("system");
				return ActionReturnUtil.returnSuccessWithData(systemConfig);
			}
//			if(Integer.parseInt(systemConfig.getConfigValue()) != -1) {
//				systemConfig.setConfigValue(String.format("%.0f", Double.parseDouble(systemConfig.getConfigValue()) / 24));
//			}

			return ActionReturnUtil.returnSuccessWithData(systemConfig);
		} catch (Exception e) {
			logger.error("Failed to get Trial time", e);
			return ActionReturnUtil.returnSuccessWithData("获取试用时间失败");
		}
	}


}
