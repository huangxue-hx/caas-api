package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface TopologyService {
	
	/**
     * delete topology by tenant.
     * 
     * @author gurongyun
     * 
     * @param tenant
     *            tenant name
     * @return ActionReturnUtil
     */
    ActionReturnUtil delToplogy(String [] tenant) throws Exception;
    
	/**
     * delete topology by BusinesstemplateId.
     * 
     * @author gurongyun
     * 
     * @param businesstemplateId
     * @return ActionReturnUtil
     */
    ActionReturnUtil deleteToplogy(int businesstemplateId) throws Exception;
}
