package com.harmonycloud.service.application;

import com.harmonycloud.common.util.ActionReturnUtil;

public interface SecretService {
	
	/**
	 * 检测secret
	 * @param namespace
	 * @param userName
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public ActionReturnUtil checkedSecret(String userName, String password) throws Exception;

}
