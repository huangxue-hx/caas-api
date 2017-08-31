package com.harmonycloud.service.application;

import com.harmonycloud.dao.cluster.bean.Cluster;
import com.harmonycloud.dto.scale.AutoScaleDto;

/**
 * 
 * @author jmi
 *
 */
public interface AutoScaleService {

	boolean create(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception;

	boolean update(AutoScaleDto autoScaleDto, Cluster cluster) throws Exception;

	boolean delete(String namespace, String deploymentName, Cluster cluster) throws Exception;

	AutoScaleDto get(String namespace, String deploymentName, Cluster cluster) throws Exception;
}
