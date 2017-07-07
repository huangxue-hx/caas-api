package com.harmonycloud.k8s.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.exception.K8sAuthException;
import com.harmonycloud.common.util.HttpSslClientUtil;
import com.harmonycloud.common.util.JsonUtil;
import com.harmonycloud.k8s.constant.Constant;




public class HttpK8SClientUtil {

	private static int TIMEOUT = 6000000;

	private static String UTF_8 = "UTF-8";
	

	/**
	 * 通过连接池获取HttpClient
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static CloseableHttpClient getHttpClient() throws Exception {
		return HttpSslClientUtil.createHttpsClient();
	}

	/**
	 * 发起GET请求,如果没有header和params则设置为null
	 * 
	 * @param url
	 * @param headers
	 *            请求头
	 * @param params
	 *            参数
	 * @return
	 */
	public static K8SClientResponse httpGetRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception{
		K8SClientResponse k8sResponse = new K8SClientResponse();
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);
		CloseableHttpClient httpClient = null;
		if (params != null) {
			ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
			ub.setParameters(pairs);
		}
		try {
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
			HttpGet httpGet = new HttpGet(ub.build());
			httpGet.setConfig(requestConfig);
			if (headers != null) {
				for (Map.Entry<String, Object> param : headers.entrySet()) {
					httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
				}
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpGet);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String charset = getContentCharSet(entity);
				// 使用EntityUtils的toString方法，传递编码，默认编码是UTF-8
				String result = EntityUtils.toString(entity, charset);
				if (result.contains("Unauthorized")) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
			   httpClient.close();
		}
		return k8sResponse;
	}
	
	/**
	 * 发起DELETE请求,如果没有header和params则设置为null
	 * 
	 * @param url
	 * @param headers
	 *            请求头
	 * @param params
	 *            参数
	 * @return
	 */
	public static K8SClientResponse httpDeleteRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception{
		K8SClientResponse k8sResponse = new K8SClientResponse();
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);
		CloseableHttpClient httpClient = null;
		if (params != null) {
			ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
			ub.setParameters(pairs);
		}
		try {
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
			HttpDelete httpDelete = new HttpDelete(ub.build());
			httpDelete.setConfig(requestConfig);
			if (headers != null) {
				for (Map.Entry<String, Object> param : headers.entrySet()) {
					httpDelete.addHeader(param.getKey(), String.valueOf(param.getValue()));
				}
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpDelete);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains("Unauthorized")) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
				httpClient.close();
		}
		return k8sResponse;
	}

	public static K8SClientResponse httpDeleteRequestForK8s(String url, Map<String, Object> headers,
													  Map<String, Object> params) throws Exception {
		K8SClientResponse k8sResponse = new K8SClientResponse();
		URIBuilder ub = new URIBuilder();
		ub.setPath(url);
		CloseableHttpClient httpClient = null;

		try {
			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
					.build();

			if(params != null){
				if (params != null) {
					ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
					ub.setParameters(pairs);
				}
			}
			HttpDeleteWithBody httpDeleteWithBody = new HttpDeleteWithBody(ub.build());
			httpDeleteWithBody.setConfig(requestConfig);
			if (headers != null) {
				for (Map.Entry<String, Object> param : headers.entrySet()) {
					httpDeleteWithBody.addHeader(param.getKey(), String.valueOf(param.getValue()));
				}
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpDeleteWithBody);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains(Constant.HTTP_UNAUTHORIZED)) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
				httpClient.close();
		}
		return k8sResponse;
	}

	/**
	 * 发起POST请求,如果没有header和params则设置为null
	 * 
	 * @param url
	 * @param headers
	 *            请求头
	 * @param params
	 *            设置参数
	 * @return
	 * @throws Exception 
	 */
	public static K8SClientResponse httpPostRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		K8SClientResponse k8sResponse = new K8SClientResponse();
		
		CloseableHttpClient httpClient = null;
		
		try {
			
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if (headers != null) {
    			for (Map.Entry<String, Object> param : headers.entrySet()) {
    				httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
    			}
    		}
			if (params != null) {
				ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, UTF_8));
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains(Constant.HTTP_UNAUTHORIZED)) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
			    httpClient.close();
		}
		return k8sResponse;
	}
	
	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static K8SClientResponse httpPostJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		K8SClientResponse k8sResponse = new K8SClientResponse();
		
		CloseableHttpClient httpClient = null;
		
		try {
			
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            if (headers != null) {
    			for (Map.Entry<String, Object> param : headers.entrySet()) {
    				httpPost.addHeader(param.getKey(), String.valueOf(param.getValue()));
    			}
    		}
            if(params!=null){
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPost.setEntity(entity);
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains(Constant.HTTP_UNAUTHORIZED)) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
				httpClient.close();
		}
		return k8sResponse;
	}

	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static K8SClientResponse httpPutJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		K8SClientResponse k8sResponse = new K8SClientResponse();
		
		CloseableHttpClient httpClient = null;
		
		try {
			
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpPut httpPut = new HttpPut(url);
            httpPut.setConfig(requestConfig);
            if (headers != null) {
    			for (Map.Entry<String, Object> param : headers.entrySet()) {
    				httpPut.addHeader(param.getKey(), String.valueOf(param.getValue()));
    			}
    		}
            if(params!=null){
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPut.setEntity(entity);
				System.out.println(entity.toString());
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPut);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains(Constant.HTTP_UNAUTHORIZED)) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch (Exception e) {
			throw e; 
		} finally {
			if (httpClient != null)
				httpClient.close();
		}
		return k8sResponse;
	}
	
	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static K8SClientResponse httpPatchJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		K8SClientResponse k8sResponse = new K8SClientResponse();
		
		CloseableHttpClient httpClient = null;
		
		try {
			
			// 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            HttpPatch httpPatch = new HttpPatch(url);
            httpPatch.setConfig(requestConfig);
            if (headers != null) {
    			for (Map.Entry<String, Object> param : headers.entrySet()) {
    				httpPatch.addHeader(param.getKey(), String.valueOf(param.getValue()));
    			}
    		}
            if(params!=null){
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPatch.setEntity(entity);
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPatch);
			Integer statusCode = response.getStatusLine().getStatusCode();
			k8sResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (result.contains(Constant.HTTP_UNAUTHORIZED)) {
					throw new K8sAuthException(Constant.HTTP_401);
				}
				k8sResponse.setBody(result);
				response.close();
				return k8sResponse;
			}
		} catch ( Exception e) {
			throw e;
		} finally {
			if (httpClient != null)
				httpClient.close();
		}
		return k8sResponse;
	}
	
	
	/**
	 * 将map转换为请求中的参数
	 * 
	 * @param params
	 * @return
	 */
	private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, Object> params) {
		ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
		}
		return pairs;
	}

	/**
	 * 默认编码utf -8
	 * Obtains character set of the entity, if known.
	 *
	 * @param entity must not be null
	 * @return the character set, or null if not found
	 * @throws IllegalArgumentException if entity is null
	 */
	public static String getContentCharSet(final HttpEntity entity) {
		String charset = "UTF-8";
		if (entity == null) {
			return charset;
		}
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset" );
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}

}
