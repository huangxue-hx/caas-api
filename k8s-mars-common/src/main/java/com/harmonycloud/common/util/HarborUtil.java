package com.harmonycloud.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HarborUtil {
	@Value("#{propertiesReader['image.host']}")
	private String harborIP;
	@Value("#{propertiesReader['image.port']}")
	private String harborPort;
	@Value("#{propertiesReader['image.username']}")
	private String harborUser;
	@Value("#{propertiesReader['image.password']}")
	private String harborPassword;
	@Value("#{propertiesReader['image.timeout']}")
	private String harborTimeout;
	
	@Value("#{propertiesReader['image.version']}")
	private String harborVersion;

	
	/**
	 * 检测harbor登录是否超时
	 * 
	 * @return
	 * @throws Exception
	 */
	public String checkCookieTimeout() throws Exception {
		String createTime = CookieInfo.getValue("createtime");
		long nowTime = new Date().getTime();
		String cookies = CookieInfo.getValue("cookie");
		long interval = createTime == null ? 0: nowTime - Long.valueOf(createTime);
		if (interval > Integer.valueOf(harborTimeout) || cookies == null) {
			// 重新登陆
			String url = "http://" + harborIP + ":" + harborPort + "/login";
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("principal", harborUser);
			params.put("password", harborPassword);
			CookieInfo.add("createtime", String.valueOf(new Date(System.currentTimeMillis()).getTime()));
			CloseableHttpResponse response = HttpClientUtil.doPostWithLogin(url, params, null);
			String cookie = response.getHeaders("Set-Cookie")[0].getValue();
			CookieInfo.add("cookie", cookie.substring(0, cookie.indexOf(";")));
			cookies = cookie.substring(0, cookie.indexOf(";"));
		}
		return cookies;
	}

	public String getHarborIP() {
		return harborIP;
	}

	public void setHarborIP(String harborIP) {
		this.harborIP = harborIP;
	}

	public String getHarborPort() {
		return harborPort;
	}

	public void setHarborPort(String harborPort) {
		this.harborPort = harborPort;
	}

	public String getHarborUser() {
		return harborUser;
	}

	public void setHarborUser(String harborUser) {
		this.harborUser = harborUser;
	}

	public String getHarborPassword() {
		return harborPassword;
	}

	public void setHarborPassword(String harborPassword) {
		this.harborPassword = harborPassword;
	}

	public String getHarborTimeout() {
		return harborTimeout;
	}

	public void setHarborTimeout(String harborTimeout) {
		this.harborTimeout = harborTimeout;
	}

	public String getHarborVersion() {
		return harborVersion;
	}

	public void setHarborVersion(String harborVersion) {
		this.harborVersion = harborVersion;
	}

}
