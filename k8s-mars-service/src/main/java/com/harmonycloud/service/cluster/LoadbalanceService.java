package com.harmonycloud.service.cluster;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface LoadbalanceService {
	
	public ActionReturnUtil getStatsByService(String app, String namespace) throws Exception;

}
