package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dto.scale.AutoScaleDto;

/**
 * 
 * @author jmi
 *
 */
public interface AutoScaleService {

	ActionReturnUtil create(AutoScaleDto autoScaleDto) throws Exception;

	ActionReturnUtil update(AutoScaleDto autoScaleDto) throws Exception;

	boolean delete(String namespace, String deploymentName) throws Exception;

	AutoScaleDto get(String namespace, String deploymentName) throws Exception;
}
