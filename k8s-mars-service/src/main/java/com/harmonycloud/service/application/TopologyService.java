package com.harmonycloud.service.application;

import java.util.List;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.business.TopologyListDto;
import com.harmonycloud.dto.business.TopologysDto;

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
    
	/**
     * add topology .
     * 
     * @author gurongyun
     * 
     * @param businesstemplateId
     * @return ActionReturnUtil
     */
    ActionReturnUtil saveToplogy(List<TopologysDto> list, int businessTemplateId) throws Exception;
    
}
