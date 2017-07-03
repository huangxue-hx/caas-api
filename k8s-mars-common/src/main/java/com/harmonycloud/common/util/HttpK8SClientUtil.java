package com.harmonycloud.common.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
	public static String httpGetRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception{
		HttpClientResponse httpClientResponse = new HttpClientResponse();
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
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
	public static String httpDeleteRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception{
		HttpClientResponse httpClientResponse = new HttpClientResponse();
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				return result;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
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
	public static String httpPostRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return result;
				} else {
					return "";
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPostJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return result;
				} else {
					return "";
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	/**
	 * post application/json for harbor
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static CloseableHttpResponse httpPostJsonRequestForHarbor(String url, Map<String, Object> headers,
											 Map<String, Object> params) throws Exception {

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
			if (!HttpStatusUtil.isSuccessStatus(statusCode)) {
				HttpEntity entity = response.getEntity();
				if(entity != null){
					String result = EntityUtils.toString(entity);
					System.out.println(result);
				}


			}
			return response;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPutJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return result;
				} else {
					return "";
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}
	
	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static String httpPatchJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws Exception {
		HttpClientResponse httpClientResponse = new HttpClientResponse();
		
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
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return result;
				} else {
					return "";
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
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

}
