package com.harmonycloud.service.application.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.network.TopologyMapper;
import com.harmonycloud.dao.network.bean.Topology;
import com.harmonycloud.dto.business.TopologysDto;
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

	@Override
	public ActionReturnUtil saveToplogy(List<TopologysDto> list, int businessTemplateId) throws Exception {
		for(TopologysDto t : list){
			int sourceId = 0;
			int targetId = 0;
			if(t.getSource() != null && t.getSource().getId() != null){
				sourceId = Integer.parseInt(t.getSource().getId());
			}
			if(t.getTarget() != null && t.getTarget().getId() != null){
				targetId = Integer.parseInt(t.getTarget().getId());
			}
			if(sourceId != 0 && targetId != 0){
				List<Topology> topologys = topologyMapper.getTopology(businessTemplateId, sourceId, targetId);
				if(topologys == null || topologys.size() <= 0){
					Topology topology = new Topology();
					topology.setBusinessId(businessTemplateId);
					topology.setSource(sourceId+"");
					topology.setTarget(targetId+"");
					topologyMapper.insert(topology);
				}
			}else{
				return ActionReturnUtil.returnErrorWithMsg("不存在源或目标Id");
			}
		}
		return ActionReturnUtil.returnSuccess();
	}

}
