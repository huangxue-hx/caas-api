package com.harmonycloud.service.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.service.application.ApplicationService;
import com.harmonycloud.service.application.BusinessService;
import com.harmonycloud.service.application.BusinessServiceService;
import com.harmonycloud.service.application.ServiceService;
import com.harmonycloud.service.application.TopologyService;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApplicationServiceImpl implements ApplicationService {

	@Autowired
    private TopologyService topologyService;
	
	@Autowired
    private BusinessServiceService businessServiceService;
	
	@Autowired
    private BusinessService businessService;
	
	@Autowired
    private ServiceService serviceService;
	
	@Override
	public ActionReturnUtil deleteTemplateByTenant(String [] tenant) throws Exception {
		if(tenant != null && tenant.length > 0){
			//删除拓扑图 
			ActionReturnUtil topologyres=topologyService.delToplogy(tenant);
			if(topologyres != null && !topologyres.isSuccess()){
				return topologyres;
			}
			//删除业务应用Map关系
			ActionReturnUtil budsermapres=businessServiceService.delbusiness(tenant);
			if(budsermapres != null && !budsermapres.isSuccess()){
				return budsermapres;
			}
			//删除业务模板
			ActionReturnUtil businessres=businessService.deleteBusinessTemplateByTenant(tenant);
			if(businessres != null && !businessres.isSuccess()){
				return businessres;
			}
			//删除应用模板
			ActionReturnUtil serviceres=serviceService.deleteServiceByTenant(tenant);
			if(serviceres != null && !serviceres.isSuccess()){
				return serviceres;
			}
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("tenant 为空");
		}
	}

}
