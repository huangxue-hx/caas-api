package com.harmonycloud.api.application;


import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.service.application.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;


/**
 * 
 * @author jmi
 *
 */
@Controller
@RequestMapping("/clusters/{clusterId}/events")
public class EventController {
	
	@Autowired
	private EventService eventService;
	@Autowired
	private HttpSession session;

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public ActionReturnUtil listEvent(@PathVariable("clusterId") String clusterId,
									   @RequestParam(value = "name", required = false) String name,
									   @RequestParam(value = "namespace", required = false) String namespace,
									   @RequestParam(value = "type", required = false) String type) throws Exception{
		EventList eventList = eventService.getEvents(name, namespace, type, clusterId);
		if(eventList == null){
			logger.warn("查询event失败, clusterId:{},name:{},namespace:{},type:{}",new String[]{clusterId,name,namespace,type});
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}
		return ActionReturnUtil.returnSuccessWithData(eventList.getItems());

	}
	
	@ResponseBody
	@RequestMapping(value="/watch" , method=RequestMethod.GET)
	public ActionReturnUtil watchEvents(@PathVariable("clusterId") String clusterId,
			@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "namespace", required = false) String namespace,
			@RequestParam(value = "type", required = false) String type)throws Exception{
		logger.info("watch事件, clusterId:{},name:{},namespace:{},type:{}",new String[]{clusterId,name,namespace,type});
		String userName = (String) session.getAttribute("username");
		return eventService.watchEvents(name, namespace, type, userName, clusterId);

	}

	@ResponseBody
	@RequestMapping(value = "/overview", method = RequestMethod.GET)
	public ActionReturnUtil getEventsOverview(@PathVariable("clusterId") String clusterId,
			                           @RequestParam(value = "name", required = false) String name,
									   @RequestParam(value = "namespace", required = false) String namespace,
									   @RequestParam(value = "type", required = false) String type) throws Exception{

		return ActionReturnUtil.returnSuccessWithData(eventService.getEventOverview(name, namespace, type, clusterId));

	}



}
