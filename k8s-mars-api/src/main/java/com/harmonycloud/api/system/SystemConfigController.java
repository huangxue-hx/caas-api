package com.harmonycloud.api.system;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.common.util.AssertUtil;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.cicd.CicdConfigDto;
import com.harmonycloud.dto.user.CrowdConfigDto;
import com.harmonycloud.dto.user.LdapConfigDto;
import com.harmonycloud.service.system.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.harmonycloud.service.user.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.harmonycloud.service.util.NetworkUtil;
import sun.nio.ch.Net;

import static com.harmonycloud.common.Constant.CommonConstant.FLAG_TRUE;

@RestController @RequestMapping("/system/configs") public class SystemConfigController {

    @Autowired private SystemConfigService systemConfigService;

    @Autowired private AuthManagerCrowd authManagerCrowd;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ResponseBody @RequestMapping(value = "/ldap", method = RequestMethod.POST)
    public ActionReturnUtil saveLdapConfig(@ModelAttribute LdapConfigDto ldapConfigDto) throws Exception {
        AssertUtil.notNull(ldapConfigDto);
        try {
            //			logger.info("save ldapConfig");
            System.out.println("ldapConfig:" + ldapConfigDto.getIp());
            systemConfigService.addLdapConfig(ldapConfigDto);
            return ActionReturnUtil.returnSuccess();

        } catch (Exception e) {
            logger.warn("saveLdapConfig失败", e);
            logger.error("Failed to save systemConfig.", e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SAVE_FAIL);
        }
    }

    //crowd设置
    @ResponseBody @RequestMapping(value = "/crowd", method = RequestMethod.POST)
    public ActionReturnUtil saveCrowdConfig(@ModelAttribute CrowdConfigDto crowdConfigDto, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        AssertUtil.notNull(crowdConfigDto);
        try {
            //如果crowd要开启，判断这个crowd是否可以连通
            if (crowdConfigDto.getIsAccess() == FLAG_TRUE && !authManagerCrowd.testCrowd(crowdConfigDto)) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CONNECT_FAIL);
            }
            systemConfigService.addCrowdConfig(crowdConfigDto);
            HttpSession session = request.getSession();
            String username = (String)session.getAttribute("username");
            if(!CommonConstant.ADMIN.equals(username)) {
                String ip = NetworkUtil.getIpAddress(request);
                authManagerCrowd.setClientIp(ip);
                String token = authManagerCrowd.getToken(username);
                if(org.apache.commons.lang3.StringUtils.isBlank(username)){
                    return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.GET_CROWD_CONF_FAIL);
                }
                authManagerCrowd.addCookie(token, response);
            }

            return ActionReturnUtil.returnSuccess();

        } catch (Exception e) {
            logger.warn("saveCrowdConfig失败", e);
            logger.error("Failed to save systemConfig.", e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.SAVE_FAIL);
        }
    }

    @ResponseBody @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ActionReturnUtil getSystemConfigById(@PathVariable String id) throws Exception {
        try {
            SystemConfig systemConfig = systemConfigService.findById(id);
            //			logger.info("Get SystemConfig By id:{}", id);
            return ActionReturnUtil.returnSuccessWithData(systemConfig);
        } catch (Exception e) {
            logger.error("Failed to get SystemConfig By id", e.getMessage());
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL);
        }
    }

    @ResponseBody @RequestMapping(value = "/ldap", method = RequestMethod.GET) public ActionReturnUtil getLdap()
        throws Exception {
        try {
            LdapConfigDto ldapConfigDto = this.systemConfigService.findLdapConfig();
            if (StringUtils.isEmpty(ldapConfigDto)) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.GET_LDAP_CONF_FAIL);
            }

            return ActionReturnUtil.returnSuccessWithData(ldapConfigDto);
        } catch (Exception e) {
            logger.error("Failed to get SystemConfig", e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.GET_LDAP_CONF_FAIL);
        }
    }

    //crowd查询
    @ResponseBody @RequestMapping(value = "/crowd", method = RequestMethod.GET) public ActionReturnUtil getCrowd()
        throws Exception {
        try {
            CrowdConfigDto crowdConfigDto = this.systemConfigService.findCrowdConfig();
            if (StringUtils.isEmpty(crowdConfigDto)) {
                return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.GET_CROWD_CONF_FAIL);
            }

            return ActionReturnUtil.returnSuccessWithData(crowdConfigDto);
        } catch (Exception e) {
            logger.error("Failed to get SystemConfig", e);
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.GET_CROWD_CONF_FAIL);
        }
    }

    @ResponseBody @RequestMapping(value = "/localuserflag", method = RequestMethod.GET)
    public ActionReturnUtil getSsoConfig() throws Exception {
        return ActionReturnUtil.returnSuccessWithData(systemConfigService.getLocalUserFlag());
    }

    @ResponseBody @RequestMapping(value = "/trialtime", method = RequestMethod.GET)
    public ActionReturnUtil getTrialTime() throws Exception {
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
            return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.QUERY_FAIL);
        }
    }

    @ResponseBody @RequestMapping(value = "/maintenance", method = RequestMethod.GET)
    public ActionReturnUtil getMaintenanceStatus() {
        return ActionReturnUtil.returnSuccessWithData(systemConfigService.findMaintenanceStatus());
    }

    @ResponseBody @RequestMapping(value = "/maintenance", method = RequestMethod.POST)
    public ActionReturnUtil updateMaintenanceStatus(@RequestParam(value = "status") String status) {
        systemConfigService.updateMaintenanceStatus(status);
        return ActionReturnUtil.returnSuccess();
    }

    @ResponseBody @RequestMapping(value = "/cicd", method = RequestMethod.GET) public ActionReturnUtil getCicdConfig() {
        return ActionReturnUtil.returnSuccessWithData(systemConfigService.getCicdConfig());
    }

    @ResponseBody @RequestMapping(value = "/cicd", method = RequestMethod.POST)
    public ActionReturnUtil updateCicdConfig(@RequestBody CicdConfigDto cicdConfigDto) {
        systemConfigService.updateCicdConfig(cicdConfigDto);
        return ActionReturnUtil.returnSuccess();
    }

}
