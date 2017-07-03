package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface ApplicationService {
	/**
	 * delete (topology,businessTemplateServiceTemplateMap,businessTemplate,ServiceTemplate)
	 * by tenant*/
	ActionReturnUtil deleteTemplateByTenant(String [] tenant) throws Exception; 
}
