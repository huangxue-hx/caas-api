package com.harmonycloud.api.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.TopologyListDto;
import com.harmonycloud.service.application.TopologyService;

@RequestMapping("/topology")
@Controller
public class TopologyController {
	
	@Autowired
	TopologyService topologyService;
    /**
     * add topology on 17/05/05.
     * 
     * @param topologyList
     * 
     * @return ActionReturnUtil
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
	private ActionReturnUtil insertTopology(@ModelAttribute TopologyListDto topologyList) throws Exception {
    	if(topologyList == null || topologyList.getTopologyList() == null || topologyList.getTopologyList().size() <= 0 || topologyList.getBusinessTemplateId() == 0 ){
    		return ActionReturnUtil.returnErrorWithMsg("拓扑图为空");
    	}
		return topologyService.saveToplogy( topologyList.getTopologyList(), topologyList.getBusinessTemplateId());
	}
}
