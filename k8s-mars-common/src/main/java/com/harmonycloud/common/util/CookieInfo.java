package com.harmonycloud.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * harbor中的cookie信息
 * 
 * @author yj
 * @date 2017年1月10日
 */
@Component
public class CookieInfo {

	private static String harborIP;

	private static String harborPort;

	private static String harborUser;

	private static String harborPassword;

	public static Map<String, String> cookieMap = new HashMap<String, String>();

/*	static {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	public static void init() throws Exception {
		cookieMap.clear();
		// 做一次登录
		String url = "http://" + harborIP + ":" + harborPort + "/login";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("principal", harborUser);
		params.put("password", harborPassword);
		cookieMap.put("createtime", String.valueOf(new Date(System.currentTimeMillis()).getTime()));
		CloseableHttpResponse response = HttpClientUtil.doPostWithLogin(url, params, null);
		String cookie = response.getHeaders("Set-Cookie")[0].getValue();
		cookieMap.put("cookie", cookie.substring(0, cookie.indexOf(";")));
	}

	public static void add(Map<String, String> map) {
		cookieMap.putAll(map);
	}

	public static void add(String key, String value) {
		cookieMap.put(key, value);
	}

	public static String getValue(String key) {
		return cookieMap.get(key);
	}

	public String getHarborIP() {
		return harborIP;
	}

	@Value("#{propertiesReader['image.port']}")
	public void setHarborIP(String harborIP) {
		CookieInfo.harborIP = harborIP;
	}

	public String getHarborPort() {
		return harborPort;
	}

	@Value("#{propertiesReader['image.host']}")
	public void setHarborPort(String harborPort) {
		CookieInfo.harborPort = harborPort;
	}

	public String getHarborUser() {
		return harborUser;
	}

	@Value("#{propertiesReader['image.username']}")
	public void setHarborUser(String harborUser) {
		CookieInfo.harborUser = harborUser;
	}

	public String getHarborPassword() {
		return harborPassword;
	}

	@Value("#{propertiesReader['image.password']}")
	public void setHarborPassword(String harborPassword) {
		CookieInfo.harborPassword = harborPassword;
	}

}
