package com.harmonycloud.common.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by czm on 2017/3/27.
 */
public class DicUtil {
	public static final Properties properties = new Properties();



	static {
		try {
			InputStream inputStream = DicUtil.class.getClassLoader().getResourceAsStream("properties/dic.properties");
			BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			properties.load(bf);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private DicUtil() {
	}

	/**
	 * 获取properties中的值.
	 * 
	 * @param key
	 *            key
	 * @return value
	 */
	public static String get(String key) {
		String value = properties.getProperty(key);
		if (value == null) {
			return "";
		}
		return value;
	}

	/**
	 * 根据models数组装模块名称.
	 * 
	 * @param uri
	 *            api路径数组
	 * @param index
	 *            作为模块名称的数组元素的末尾位置
	 * @return 模块名称
	 */
	public static String parseModelName(String uri, int index) {
		String[] subPath = new String[100];
		if (StringUtils.isNotBlank(uri)) {
			subPath = uri.split("/");
		}

		if (index < 0) {
			index = 0;
		}
		if (index > subPath.length) {
			index = subPath.length;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < index; i++) {
			sb.append(subPath[i]);
			if (i < index - 1) {
				sb.append("_");
			}
		}

		if (StringUtils.isNotBlank(sb.toString())) {
			sb.append("_model");
		}

		return sb.toString();
	}

	/**
	 * 解析properties文件中的key.
	 * 
	 * @param uri
	 *            request
	 * @param method
	 *            请求类型
	 * @return properties文件中功能描述的key
	 */
	public static String parseDicKey(String uri, String method) {

		String[] subPath = new String[100];
		if (StringUtils.isNotBlank(uri)) {
			subPath = uri.split("/");
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < subPath.length; i++) {
			if (StringUtils.isNotBlank(subPath[i])) {
				sb.append(subPath[i] + "_");

			}
		}
		sb.append(method);

		return sb.toString();
	}

	/**
	 * 获取请求的参数.
	 * 
	 * @param request
	 *            request
	 * @return 参数组成的字符串
	 * @throws IOException
	 *             IO异常
	 */
	public static String parseParams(HttpServletRequest request){
		StringBuilder sb = new StringBuilder();
		// String json;
		// HttpPutFormContentRequestWrapper requestWrapper = new
		// HttpPutFormContentRequestWrapper( request,null);
		// json = requestWrapper.getRequestParams();
		//
		// if (json != null && !json.equals("")) {
		// sb.append("请求参数:" + json);
		// } else {
		Enumeration enu = request.getParameterNames();

		while (enu.hasMoreElements()) {
			if (sb.length() <= 0) {
				sb.append("请求参数:");
			}
			String paraName = (String) enu.nextElement();
			sb.append(paraName + "->" + request.getParameter(paraName) + ";");
		}
		// }
		return sb.toString();

	}

	public static String parseDicKeyOfParams(String uri, String method) {

		String[] subPath = new String[100];
		if (StringUtils.isNotBlank(uri)) {
			subPath = uri.split("/");
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < subPath.length; i++) {
			if (StringUtils.isNotBlank(subPath[i])) {
				sb.append(subPath[i] + "_");

			}
		}
		sb.append(method + "_params");

		return sb.toString();
	}

	public static Map<String, Object> parseRequestParams(HttpServletRequest request, String key) throws IOException {
		StringBuilder sb = new StringBuilder();
		Enumeration<String> enu = request.getParameterNames();
		Map<String, Object> res = new HashMap<String, Object>();
        List<String> values = new ArrayList<String>();
        String[] ps = key.split(";");
		while (enu.hasMoreElements()) {
			if (sb.length() <= 0) {
				sb.append("请求参数:");
			}
			String paraName = (String) enu.nextElement();
			sb.append(paraName + "->" + request.getParameter(paraName) + ";");
			for (String v : ps) {
				if (paraName.equals(v) || paraName.indexOf(v) > -1) {
					String value = request.getParameter(paraName);
					values.add(value);
				}
			}
		}
		res.put("allParams", sb.toString());
		res.put("values", values);
		return res;
	}
}
