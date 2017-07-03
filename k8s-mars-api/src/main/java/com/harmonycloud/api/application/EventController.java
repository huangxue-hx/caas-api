package com.harmonycloud.api.application;


import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.k8s.bean.Event;
import com.harmonycloud.k8s.bean.EventList;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.application.EventService;
import com.harmonycloud.service.cluster.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @author jmi
 *
 */
@Controller
public class EventController {
	
	@Autowired
	private EventService eventService;
	@Autowired
	private ClusterService clusterService;
	@Autowired
	HttpSession session;

	final int EVENTCOUNT = 30;
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@ResponseBody
	@RequestMapping(value = "/events", method = RequestMethod.GET)
	public ActionReturnUtil eventsList(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "namespace", required = false) String namespace,
			@RequestParam(value = "type", required = false) String type) throws Exception{
		 try {
			logger.info("通过machineclient获取事件");
			return eventService.getEvents(name, namespace, type);
		} catch (Exception e) {
			logger.error("通过machineclient获取事件错误，name="+name+", namespace="+namespace+", type="+type+", e="+e.getMessage());
			throw e;
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/watch" , method=RequestMethod.GET)
	public ActionReturnUtil watchEvents(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "namespace", required = false) String namespace,
			@RequestParam(value = "type", required = false) String type)throws Exception{
		
		try {
			logger.info("watch事件");
			String userName = (String) session.getAttribute("username");
	        if(userName == null){
				throw new K8sAuthException(Constant.HTTP_401);
			}
			Cluster cluster = (Cluster) session.getAttribute("currentCluster");
			return eventService.watchEvents(name, namespace, type, userName, cluster);
		} catch (Exception e) {
			logger.error("watch事件事件错误，name="+name+", namespace="+namespace+", type="+type+", e="+e.getMessage());
			throw e;
		}
	}

	@ResponseBody
	@RequestMapping(value = "/events/clusterEventsOverview", method = RequestMethod.GET)
	public ActionReturnUtil eventsOverview(@RequestParam(value = "name", required = false) String name,
									   @RequestParam(value = "namespace", required = false) String namespace,
									   @RequestParam(value = "type", required = false) String type) throws Exception{
		try {
			logger.info("获取总览的事件");
			List<Cluster> listCluster = this.clusterService.listCluster();
			Map<String, Object> mapEventsOverview = new LinkedHashMap<String, Object>();
			Date currentTime = new Date();

			if(null != listCluster && listCluster.size() > 0) {
				for (Cluster cluster : listCluster) {
					List<String[]> oneHourWarning = new ArrayList<>();
					List<String[]> oneHourNormal = new ArrayList<>();
					List<String[]> threeHourWarning = new ArrayList<>();
					List<String[]> threeHourNormal = new ArrayList<>();
					List<String[]> sevenHourWarning = new ArrayList<>();
					List<String[]> sevenHourNormal = new ArrayList<>();
					List<String[]> twelveHourWarning = new ArrayList<>();
					List<String[]> twelveHourNormal = new ArrayList<>();
					List<String[]> twentyFourHourWarning = new ArrayList<>();
					List<String[]> twentyFourHourNormal = new ArrayList<>();

					int oneHourWarningCount = 0;
					int oneHourNormalCount = 0;
					int threeHourWarningCount = 0;
					int threeHourNormalCount = 0;
					int sevenHourWarningCount = 0;
					int sevenHourNormalCount = 0;
					int twelveHourWarningCount = 0;
					int twelveHourNormalCount = 0;
					int twentyFourHourWarningCount = 0;
					int twentyFourHourNormalCount = 0;
					EventList  eventList = eventService.getEvents(name, namespace, type, cluster);
//					if (null != eventList && null != eventList.getItems() && eventList.getItems().size() > 0)
					List<Event> eList = eventList.getItems();
					Map<String, Object> map = new LinkedHashMap<>();
					Map<String, List<String[]>> eventMap = new LinkedHashMap<>();
					Map<String, Integer> eventCount = new LinkedHashMap<>();

					for(Event e : eList) {
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String lastTimestamp = e.getLastTimestamp().replace("T", " ").replace("Z", "");


						Calendar calendar = Calendar.getInstance();
						calendar.setTime(sdf.parse(lastTimestamp));
						calendar.add(Calendar.HOUR, 8);//+8时区
						Date inputDate = calendar.getTime();

						String inputDateStr = sdf.format(inputDate);

						Calendar calendar1 = Calendar.getInstance();
						calendar1.setTime(currentTime);
						calendar1.add(Calendar.HOUR, -1);//当前时间减去1小时
						Date oneHour = calendar1.getTime();//获取1小时前的时间

						Calendar calendar3 = Calendar.getInstance();
						calendar3.setTime(currentTime);
						calendar3.add(Calendar.HOUR, -3);//当前时间减去3小时
						Date threeHour = calendar3.getTime();//获取3小时前的时间

						Calendar calendar7 = Calendar.getInstance();
						calendar7.setTime(currentTime);
						calendar7.add(Calendar.HOUR, -7);//当前时间减去7小时
						Date sevenHour = calendar7.getTime();//获取7小时前的时间

						Calendar calendar12 = Calendar.getInstance();
						calendar12.setTime(currentTime);
						calendar12.add(Calendar.HOUR, -12);//当前时间减去12小时
						Date twelveHour = calendar7.getTime();//获取12小时前的时间

						Calendar calendar24 = Calendar.getInstance();
						calendar24.setTime(currentTime);
						calendar24.add(Calendar.HOUR, -24);//当前时间减去24小时
						Date twentyFourHour = calendar24.getTime();//获取24小时前的时间


						if(inputDate.before(currentTime) && inputDate.after(oneHour)){
							if(e.getType().equalsIgnoreCase("Warning")) {
								String[] str = {inputDateStr, e.getMessage()};
								//显示前10条数据
								if(oneHourWarningCount < EVENTCOUNT) {
									oneHourWarning.add(str);
								}
								oneHourWarningCount ++;
								continue;
							} else if (e.getType().equalsIgnoreCase("Normal")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(oneHourNormalCount < EVENTCOUNT) {
									oneHourNormal.add(str);
								}
								oneHourNormalCount ++;
								continue;
							}
						}

						if(inputDate.before(currentTime) && inputDate.after(threeHour)){
							if(e.getType().equalsIgnoreCase("Warning")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(threeHourWarningCount < EVENTCOUNT) {
									threeHourWarning.add(str);
								}
								threeHourWarningCount ++;
								continue;
							} else if (e.getType().equalsIgnoreCase("Normal")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(threeHourNormalCount < EVENTCOUNT) {
									threeHourNormal.add(str);
								}
								threeHourNormalCount ++;
								continue;
							}
						}

						if(inputDate.before(currentTime) && inputDate.after(sevenHour)){
							if(e.getType().equalsIgnoreCase("Warning")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(sevenHourWarningCount < EVENTCOUNT) {
									sevenHourWarning.add(str);
								}
								sevenHourWarningCount ++;
								continue;
							} else if (e.getType().equalsIgnoreCase("Normal")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(sevenHourNormalCount < EVENTCOUNT) {
									sevenHourNormal.add(str);
								}
								sevenHourNormalCount ++;
								continue;
							}
						}

						if(inputDate.before(currentTime) && inputDate.after(twelveHour)){
							if(e.getType().equalsIgnoreCase("Warning")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(twelveHourWarningCount < EVENTCOUNT) {
									twelveHourWarning.add(str);
								}
								twelveHourWarningCount ++;
								continue;
							} else if (e.getType().equalsIgnoreCase("Normal")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(twelveHourNormalCount < EVENTCOUNT) {
									twelveHourNormal.add(str);
								}
								twelveHourNormalCount ++;
								continue;
							}
						}

						if(inputDate.before(currentTime) && inputDate.after(twentyFourHour)){
							if(e.getType().equalsIgnoreCase("Warning")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(twentyFourHourWarningCount < EVENTCOUNT) {
									twentyFourHourWarning.add(str);
								}
								twentyFourHourWarningCount ++;
								continue;
							} else if (e.getType().equalsIgnoreCase("Normal")) {
								String[] str = {inputDateStr, e.getMessage()};
								if(twentyFourHourNormalCount < EVENTCOUNT) {
									twentyFourHourNormal.add(str);
								}
								twentyFourHourNormalCount ++;
								continue;
							}
						}



					}

					eventMap.put("oneHourWarning", oneHourWarning);
					eventMap.put("oneHourNormal", oneHourNormal);
					eventMap.put("threeHourWarning", threeHourWarning);
					eventMap.put("threeHourNormal", threeHourNormal);
					eventMap.put("sevenHourWarning", sevenHourWarning);
					eventMap.put("sevenHourNormal", sevenHourNormal);
					eventMap.put("twelveHourWarning", twelveHourWarning);
					eventMap.put("twelveHourNormal", twelveHourNormal);
					eventMap.put("twentyFourHourWarning", twentyFourHourWarning);
					eventMap.put("twentyFourHourNormal", twentyFourHourNormal);

					eventCount.put("oneHourWarning", oneHourWarningCount);
					eventCount.put("oneHourNormal", oneHourNormalCount);
					eventCount.put("threeHourWarning", threeHourWarningCount);
					eventCount.put("threeHourNormal", threeHourNormalCount);
					eventCount.put("sevenHourWarning", sevenHourWarningCount);
					eventCount.put("sevenHourNormal", sevenHourNormalCount);
					eventCount.put("twelveHourWarning", twelveHourWarningCount);
					eventCount.put("twelveHourNormal", twelveHourNormalCount);
					eventCount.put("twentyFourHourWarning", twentyFourHourWarningCount);
					eventCount.put("twentyFourHourNormal", twentyFourHourNormalCount);

					map.put("eventMap", eventMap);
					map.put("eventCount",eventCount);



					mapEventsOverview.put(cluster.getId() + "", map);

				}

			}

			return ActionReturnUtil.returnSuccessWithData(mapEventsOverview);
		} catch (Exception e) {
			logger.error("获取总览的事件错误，name="+name+", namespace="+namespace+", type="+type+", e="+e.getMessage());
			throw e;
		}
	}



}
