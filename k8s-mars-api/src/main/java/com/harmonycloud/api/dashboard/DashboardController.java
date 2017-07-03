package com.harmonycloud.api.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.platform.service.DashboardService;
import com.harmonycloud.service.platform.service.InfluxdbService;


/**
 * dashboard
 * @author jmi
 *
 */

@Controller
@RequestMapping(value = "/dashboard")
public class DashboardController {
	
	@Autowired
	DashboardService dashboardService;
	
	@Autowired
	InfluxdbService influxdbService;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody
	@RequestMapping(value = "/podInfo", method = RequestMethod.GET)
	public ActionReturnUtil getPodInfo() throws Exception{
		
		try {
			logger.info("dashboard获取pod信息");
			return dashboardService.getPodInfo();
		} catch (Exception e) {
			logger.error("dashboard获取pod信息失败,e:"+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/infraInfo", method = RequestMethod.GET)
	public ActionReturnUtil getInfraInfo() throws Exception{
		
		try {
			logger.info("dashboard获取机器信息");
			return dashboardService.getInfraInfo();
		} catch (Exception e) {
			logger.error("dashboard获取机器信息失败,e:"+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/warningInfo", method = RequestMethod.GET)
	public ActionReturnUtil getWarningInfo(@RequestParam(value = "namespace", required=false) String namespace) throws Exception{
		
		try {
			logger.info("dashboard获取告警信息");
			return dashboardService.getWarningInfo(namespace);
		} catch (Exception e) {
			logger.error("dashboard获取告警信息失败, namspace="+namespace+", e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/eventInfo" , method = RequestMethod.GET)
	public ActionReturnUtil getEventInfo(@RequestParam(value = "namespace", required=false) String namespace) throws Exception{
		
		try {
			logger.info("dashboard获取事件信息");
			return dashboardService.getEventInfo(namespace);
		} catch (Exception e) {
			logger.error("dashboard获取事件信息失败, namspace="+namespace+", e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value = "/nodemonitor" , method = RequestMethod.GET)
	public ActionReturnUtil nodeMonit(@RequestParam(value = "rangeType") String rangeType, 
			@RequestParam(value = "target") String target,
			@RequestParam(value = "type") String type) throws Exception{
		
		try {
			logger.info("dashboard获取事件信息");
			return influxdbService.nodeQuery(type, rangeType, target, null, null, null);
		} catch (Exception e) {
			logger.error("dashboard获取事件信息失败, rangeType="+rangeType+", target="+target+", type="+type+", e="+e.getMessage());
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/nodeLicense" , method = RequestMethod.GET)
	public ActionReturnUtil getNodeLicense () throws Exception{
		try {
			logger.info("获取node license");
			return dashboardService.getＮodeLicense();
		} catch (Exception e) {
			logger.error("获取node license失败");
			throw e;
		}
	}

}
