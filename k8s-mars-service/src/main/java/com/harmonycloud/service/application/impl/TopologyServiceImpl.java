package com.harmonycloud.service.application.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.network.TopologyMapper;
import com.harmonycloud.service.application.TopologyService;

@Service
@Transactional(rollbackFor = Exception.class)
public class TopologyServiceImpl implements TopologyService {

	@Autowired
    private TopologyMapper topologyMapper;
	
	@Override
	public ActionReturnUtil delToplogy(String[] tenant) throws Exception {
		if(tenant != null && tenant.length > 0 ){
			topologyMapper.deleteTopologyByTenant(tenant);
			return ActionReturnUtil.returnSuccess();
		}else{
			return ActionReturnUtil.returnErrorWithMsg("tenant 不能为空");
		}
	}

	@Override
	public ActionReturnUtil deleteToplogy(int businessTemplateId) throws Exception {
		topologyMapper.deleteTopology(businessTemplateId);
		return ActionReturnUtil.returnSuccess();
	}

}
