package com.harmonycloud.common.util;

import com.harmonycloud.common.util.HttpClientResponse;
import com.harmonycloud.common.util.HttpClientUtil;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TenantUtils {

	private static String openstackIP;
	private static String port;
	private static String pwd;
	private static String adminport;
	private static String netport;

	@SuppressWarnings("unchecked")
	public static String gettoken() throws Exception{
		String addUrl = openstackIP + ":" + port + "/v2.0/tokens";
		Map<String, Map<String, Object>> params = new HashMap<String, Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		Map<String, Object> map2 = new HashMap<String, Object>();
		map2.put("password", pwd);
		map2.put("username", "admin");
		map1.put("passwordCredentials", map2);
		map1.put("tenantName", "admin");
		params.put("auth", map1);
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("Content-type", "application/json");
		String token = null;
		try {
			HttpClientResponse response = HttpClientUtil.doRawPost(addUrl, params, header);
			Map<String, Object> map = JsonUtil.convertJsonToMap(response.getBody());
			Map<String, Object> object = (Map<String, Object>) map.get("access");
			Map<String, Object> object2 = (Map<String, Object>) object.get("token");
			token = object2.get("id").toString();
			// token =
			// response.getBody().split("serviceCatalog")[0].split("id\":
			// \"")[1].split("\",")[0];
			if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				return token;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
			//return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static String createtenant(String tenantname, String description, String token) throws Exception{
		String addUrl = openstackIP + ":" + adminport + "/v2.0/tenants";
		Map<String, Map<String, Object>> params = new HashMap<String, Map<String, Object>>();
		Map<String, Object> map1 = new HashMap<String, Object>();
		map1.put("description", description);
		map1.put("name", tenantname);
		map1.put("enabled", true);
		params.put("tenant", map1);
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("X-Auth-Token", token);
		header.put("Content-type", "application/json");
		try {
			HttpClientResponse response = HttpClientUtil.doRawPost(addUrl, params, header);
			Map<String, Object> map = JsonUtil.convertJsonToMap(response.getBody());
			Map<String, Object> object = (Map<String, Object>) map.get("tenant");
			String tenantid = object.get("id").toString();
			// String[] split = response.getBody().split("id\":
			// \"")[1].split("\",");
			if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				return tenantid;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
			//return null;
		}
	}

	public static String networklist(String tenantname, String networkid, String token) throws Exception{
		String addUrl = null;
		if ("".equals(networkid) || networkid == null) {
			addUrl = openstackIP + ":" + netport + "/v2.0/networks";
		} else {
			addUrl = openstackIP + ":" + netport + "/v2.0/networks/" + networkid;
		}

		Map<String, Object> header = new HashMap<String, Object>();
		header.put("X-Auth-Token", token);
		// header.put("Content-type", "application/json");
		try {
			HttpClientResponse response = HttpClientUtil.doGet(addUrl, null, header);
			String body = response.getBody();
			if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				return body;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
			//return null;
		}
	}

	public static String subnetwork(String tenantname, String networkid, String token) throws Exception{
		String addUrl = null;
		if ("".equals(networkid) || networkid == null) {
			addUrl = openstackIP + ":" + netport + "/v2.0/subnets";
		} else {
			addUrl = openstackIP + ":" + netport + "/v2.0/subnets/" + networkid;
		}
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("X-Auth-Token", token);
		// header.put("Content-type", "application/json");
		try {
			HttpClientResponse response = HttpClientUtil.doGet(addUrl, null, header);
			String body = response.getBody();
			if (HttpStatusUtil.isSuccessStatus(response.getStatus())) {
				return body;
			} else {
				return null;
			}
		} catch (Exception e) {
			throw e;
			//return null;
		}
	}

	public static String deleteTenantforOpenstack(String tenantid, String token) {
		String addUrl = null;
		addUrl = openstackIP + ":" + adminport + "/v3/projects/" + tenantid;
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("X-Auth-Token", token);
		try {
			HttpClientResponse response = HttpClientUtil.doDelete(addUrl, null, header);
			String body = response.getBody();
			return body;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date getUtctime() {
		SimpleDateFormat adf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

		StringBuffer UTCTimeBuffer = new StringBuffer();
		// 1、取得本地时间：
		Calendar cal = Calendar.getInstance();
		// 2、取得时间偏移量：
		int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
		// 3、取得夏令时差：
		int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
		// 4、从本地时间里扣除这些差量，即可以取得UTC时间：
		cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		UTCTimeBuffer.append(year).append("-").append(month).append("-").append(day);
		UTCTimeBuffer.append("T").append(hour).append(":").append(minute).append(":").append(second).append("Z");
		Date date = null;
		try {
			date = adf.parse(UTCTimeBuffer.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

	public static String getOpenstackIP() {
		return openstackIP;
	}

	@Value("#{propertiesReader['openstack.ip']}")
	public void setOpenstackIP(String openstackIP) {
		this.openstackIP = openstackIP;
	}

	public static String getPort() {
		return port;
	}

	@Value("#{propertiesReader['openstack.port']}")
	public void setPort(String port) {
		this.port = port;
	}

	public static String getPwd() {
		return pwd;
	}

	@Value("#{propertiesReader['openstack.pwd']}")
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getAdminport() {
		return adminport;
	}

	@Value("#{propertiesReader['openstack.adminport']}")
	public void setAdminport(String adminport) {
		this.adminport = adminport;
	}

	public static String getNetport() {
		return netport;
	}

	@Value("#{propertyResolver['openstack.netport']}")
	public static void setNetport(String netport) {
		TenantUtils.netport = netport;
	}
	public static String getuuid(){
        // 通过uuid生成token
        UUID uuid = UUID.randomUUID();
        String str = uuid.toString();

        // 去掉"-"符号
        String id = str.substring(0, 8) + str.substring(9, 13) + str.substring(14, 18) + str.substring(19, 23)
                + str.substring(24);
        return id;
    }
  
}
