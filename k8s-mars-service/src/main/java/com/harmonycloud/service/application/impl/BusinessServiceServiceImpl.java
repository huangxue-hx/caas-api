package com.harmonycloud.service.application.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.application.BusinessServiceMapper;
import com.harmonycloud.dao.application.bean.BusinessService;
import com.harmonycloud.service.application.BusinessServiceService;

@Service
@Transactional(rollbackFor = Exception.class)
public class BusinessServiceServiceImpl implements BusinessServiceService {

	@Autowired
    private BusinessServiceMapper businessServiceMapper;
	
	@Override
	public ActionReturnUtil delbusiness(String[] tenant) throws Exception {
		if(tenant != null && tenant.length > 0){
			businessServiceMapper.deleteByTenant(tenant);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("tenant 为空");
		}
	}

	@Override
	public ActionReturnUtil deletebusiness(int businesstemplateId) throws Exception {
		businessServiceMapper.deleteBusinessServiceByBusinessTemplateId(businesstemplateId);
		return ActionReturnUtil.returnSuccess();
	}

	@Override
	public List<BusinessService> listByBusiness(int businessTemplateId) throws Exception {
		return businessServiceMapper.listBusinessServiceByBusinessTemplatesId(businessTemplateId);
	}

}
