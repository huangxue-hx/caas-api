package com.harmonycloud.service.application;

import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface BusinessServiceService {
	
	/**
     * delete business by tenant.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    ActionReturnUtil delbusiness(String [] tenant) throws Exception;
    
	/**
     * delete business by businesstemplateID.
     * 
     * @author gurongyun
     * 
     * @param businesstemplateId
     * @return ActionReturnUtil
     */
    ActionReturnUtil deletebusiness(int businessTemplateId) throws Exception;
    
    List<com.harmonycloud.dao.application.bean.BusinessService> listByBusiness(int businessTemplateId) throws Exception;
}
