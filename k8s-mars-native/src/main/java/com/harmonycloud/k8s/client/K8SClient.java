package com.harmonycloud.k8s.client;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.util.HttpStatusUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.util.K8SClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author qg
 *
 */
@Component
public class K8SClient {

	private HttpSession session;

	private static final Logger LOGGER = LoggerFactory.getLogger(K8SClient.class);
	/**
	 * 保存全局token信息
	 */
	private static Map<String, Object> tokenMap = new ConcurrentHashMap<String, Object>();


	public static Map<String, Object> getTokenMap() {
		return tokenMap;
	}

	public static void setTokenMap(Map<String, Object> tokenMap) {
		K8SClient.tokenMap = tokenMap;
	}

	/**
	 * 将response转换为bean
	 * 
	 * @param response
	 * @param clazz
	 * @return
	 */
	public static <T> T converToBean(K8SClientResponse response, Class<T> clazz) {

		if (response != null && HttpStatusUtil.isSuccessStatus(response.getStatus())) {
			String body = response.getBody();
			if (body != null) {
				return JsonUtil.jsonToPojo(body, clazz);
			}
		}else{
			LOGGER.error("converToBean response:{}", JSONObject.toJSONString(response));
		}
		return null;
	}

	
	/**
	 * 获取当前用户的token
	 * @return
	 * @throws Exception 
	 */
	public String getK8sToken(){
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		session = request.getSession();
		String username = String.valueOf(session.getAttribute("username"));
		String token = String.valueOf(tokenMap.get(username));
		return token;
	}

}
