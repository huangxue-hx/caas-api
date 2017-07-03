package com.harmonycloud.dao.network;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.harmonycloud.dao.network.bean.Topology;

public interface TopologyMapper {

    int insert(Topology record);
    
    List<Topology> listToplogyByBusinessTemplatesId(@Param("businessId") int businessId);
    
    List<Topology> selectByBusinessIdAndServiceTemplateId(@Param("businessTemplateId") int businessTemplateId,@Param("businessId") int businessId);
    
    void deleteTopologyByTenant(@Param("tenant") String [] tenant);
    
    void deleteTopology(@Param("businessTemplateId") int businessTemplateId);
}