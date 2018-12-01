package com.harmonycloud.common.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.harmonycloud.common.enumm.ErrorCodeMessage;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.harmonycloud.common.Constant.CommonConstant;

import javax.servlet.http.HttpServletRequest;


public class HttpClientUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtil.class);


	private static int TIMEOUT = 5000;

	private static String UTF_8 = "UTF-8";

	/**
	 * 通过连接池获取HttpClient
	 * 
	 * @return
	 * @throws Exception 
	 */
	private static CloseableHttpClient getHttpClient()  {
		/*return HttpSslClientUtil.createHttpsClient();*/
		return HttpClients.createDefault();
	}

	/**
	 * 发起GET请求,如果没有header和params则设置为null,如果不需要设置长时间timeout则传null
	 * @param url
	 * @param headers
	 * @param params
	 * @param timeOut
	 * @return
	 * @throws Exception
	 */
	public static ActionReturnUtil httpGetRequest(String url, Map<String, Object> headers,
												  Map<String, Object> params,int timeOut) throws IOException{
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
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut).setConnectTimeout(timeOut)
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
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return ActionReturnUtil.returnSuccessWithData(result);
				} else {
					return ActionReturnUtil.returnErrorWithData(result);
				}
			}
		} catch (Exception e) {
            LOGGER.warn("http请求失败，url:{}", url, e);
			return ActionReturnUtil.returnErrorWithMsg(ErrorCodeMessage.CONNECT_FAIL,e.getMessage(),false);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.warn("关闭httpClient失败", e);
			}
		}
		return ActionReturnUtil.returnError();
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
	public static ActionReturnUtil httpGetRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws IOException{
		return httpGetRequest(url,headers,params,TIMEOUT);
	}
	
	public static HttpClientResponse httpGetRequestNew(String url, Map<String, Object> headers,
			Map<String, Object> params) throws IOException, URISyntaxException {
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
			}
			return httpClientResponse;
		} catch (IOException e) {
			throw e;
		}finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				throw e;
			}
		}
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
	public static ActionReturnUtil httpPostRequest(String url, Map<String, Object> headers,
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
					return ActionReturnUtil.returnSuccessWithData(result);
				} else {
					return ActionReturnUtil.returnErrorWithMsg(result);
				}
			}
		} catch (ClientProtocolException e) {
			LOGGER.warn("httpPostRequest失败，url:{}", url, e);
		} catch (IOException e) {
			LOGGER.warn("httpPostRequest失败，url:{}", url, e);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.warn("关闭httpClient失败", e);
			}
		}
		return ActionReturnUtil.returnError();
	}

	/**
	 * 发起POST请求,如果没有header和params则设置为null          harbor
	 *
	 * @param url
	 * @param headers 请求头
	 * @param params  设置参数
	 * @return
	 * @throws Exception
	 */
	public static ActionReturnUtil httpPostRequestForHarbor(String url, Map<String, Object> headers,
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
			if(params != null){
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPost.setEntity(entity);
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			Integer statusCode = response.getStatusLine().getStatusCode();
			String statusMessage = response.getStatusLine().getReasonPhrase();
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if(StringUtils.isEmpty(result) || "\n".equals(result)){
					result = statusMessage;
				}
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return ActionReturnUtil.returnSuccessWithData(result);
				} else {
					//return ActionReturnUtil.returnErrorWithMsg(result);
					return ActionReturnUtil.returnErrorWithData(result);
				}
			}
		} catch (ClientProtocolException e) {
			LOGGER.warn("httpPostRequestForHarbor失败，url:{}", url, e);
		} catch (IOException e) {
			LOGGER.warn("httpPostRequestForHarbor失败，url:{}", url, e);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.warn("关闭httpClient失败", e);
			}
		}
		return ActionReturnUtil.returnError();
	}

	/**
	 * 发起POST请求,如果没有header和params则设置为null          harbor create
	 *
	 * @param url
	 * @param headers 请求头
	 * @param params  设置参数
	 * @throws Exception
	 * @return 返回harbor projectId
	 */
	public static ActionReturnUtil httpPostRequestForHarborCreate(String url, Map<String, Object> headers,
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
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPost.setEntity(entity);
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPost);
			Integer statusCode = response.getStatusLine().getStatusCode();
			String statusMessage = response.getStatusLine().getReasonPhrase();
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (StringUtils.isEmpty(result) || "\n".equals(result)) {
					result = statusMessage;
				}
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					Map<String,String> res = new HashMap<>();
					String location = response.getHeaders("Location")[0].getValue();
					String newHarborProjectId = location.substring(location.lastIndexOf(CommonConstant.SLASH) + 1,
							location.length());
					res.put("result",result);
					res.put("harborProjectId",newHarborProjectId);
					return ActionReturnUtil.returnSuccessWithData(res);
				} else {
					return ActionReturnUtil.returnErrorWithData(result);
				}
			}
		} catch (ClientProtocolException e) {
			LOGGER.warn("httpPostRequestForHarborCreate失败，url:{}", url, e);
		} catch (Exception e) {
			LOGGER.warn("httpPostRequestForHarborCreate失败，url:{}", url, e);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.warn("关闭httpClient失败", e);
			}
		}
		return ActionReturnUtil.returnError();
	}

	/**
	 * 发起PUT请求,如果没有header和params则设置为null          harbor
	 *
	 * @param url
	 * @param headers 请求头
	 * @param params  设置参数
	 * @return
	 * @throws Exception
	 */
	public static ActionReturnUtil httpPutRequestForHarbor(String url, Map<String, Object> headers,
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
			if (params != null) {
				HttpEntity entity = new StringEntity(
						JsonUtil.objectToJson(params), "utf-8");
				httpPut.setEntity(entity);
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpPut);
			Integer statusCode = response.getStatusLine().getStatusCode();
			String statusMessage = response.getStatusLine().getReasonPhrase();
			httpClientResponse.setStatus(statusCode);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String result = EntityUtils.toString(entity);
				if (StringUtils.isEmpty(result) || "\n".equals(result)) {
					result = statusMessage;
				}
				httpClientResponse.setBody(result);
				response.close();
				if (HttpStatusUtil.isSuccessStatus(statusCode)) {
					return ActionReturnUtil.returnSuccessWithData(result);
				} else {
					return ActionReturnUtil.returnErrorWithData(result);
				}
			}
		} catch (ClientProtocolException e) {
			LOGGER.warn("httpPutRequestForHarbor失败，url:{}", url, e);
		} catch (IOException e) {
			LOGGER.warn("httpPutRequestForHarbor失败，url:{}", url, e);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.warn("关闭httpClient失败", e);
			}
		}
		return ActionReturnUtil.returnError();
	}

	/**
	 * post application/json
	 * @param url
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static HttpClientResponse httpPostJsonRequest(String url, Map<String, Object> headers,
			Map<String, Object> params) throws IOException {
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
				return httpClientResponse;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			
				if (httpClient != null) {
					httpClient.close();
			} 
		}
		return httpClientResponse;
	}
	
	public static ActionReturnUtil httpDoDelete(String url,
            Map<String, Object> params, Map<String, Object> headers) throws IOException {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpDelete httpDelete = null;
        try {
            httpClient = HttpClients.createDefault();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            httpDelete = new HttpDelete(url);
            httpDelete.setConfig(requestConfig);
            setHeaders(httpDelete, headers);
            response = httpClient.execute(httpDelete);
            HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
				content = EntityUtils.toString(resentity, "UTF-8");
				//}
				if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
					return ActionReturnUtil.returnSuccessWithData(content);
				} else {
					return ActionReturnUtil.returnErrorWithData(content);
				}
			}
        } catch (IOException e) {
            throw e;
        } finally {
            httpDelete.abort();
        }
        return ActionReturnUtil.returnError();
    }

	public static CloseableHttpResponse doBodyPost(String url, Map<String, Object> params, Map<String, Object> headers) throws Exception {
        HttpPost httpPost = null;
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.createDefault();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            setHeaders(httpPost, headers);
            
            // 设置Http Post数据
            if (params != null) {
               String paramsJson = JsonUtil.objectToJson(params);
               StringEntity entity = new StringEntity(paramsJson);
               httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            /*HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
                content = EntityUtils.toString(resentity, "UTF-8");
            }
            return new HttpClientResponse(response.getStatusLine().getStatusCode(), content);*/
            return response;
        } catch (Exception e) {
            throw e;
        } finally {
            httpPost.abort();
        }
    }
	
	
	/**
	 * get请求,返回status和body
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpClientResponse doGet(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpSslClientUtil.createHttpsClient();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            httpGet = new HttpGet(url);
            setHeaders(httpGet, headers);
            httpGet.setConfig(requestConfig);

            response = httpClient.execute(httpGet);
            HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
                content = EntityUtils.toString(resentity, UTF_8);
            }
            return new HttpClientResponse(response.getStatusLine().getStatusCode(), content);
        } catch (IOException e) {
            throw e;
        } finally {
            if (httpGet != null) {
                httpGet.abort();
            }
            httpClient.close();
        }
    }
	
	
	/**
	 * put 请求
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpClientResponse doPut(String url,
            Map<String, Object> params, Map<String, Object> headers) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpPut httpPut = null;
        try {
//            httpClient = HttpClients.createDefault();
			httpClient = HttpSslClientUtil.createHttpsClient();
            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            httpPut = new HttpPut(url);
            httpPut.setConfig(requestConfig);
            if (params != null) {
                String paramsJson = JsonUtil.objectToJson(params);
                StringEntity entity = new StringEntity(paramsJson);
                httpPut.setEntity(entity);
             }
            setHeaders(httpPut, headers);
            response = httpClient.execute(httpPut);
            HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
                content = EntityUtils.toString(resentity, "UTF-8");
            }
            return new HttpClientResponse(response.getStatusLine()
                    .getStatusCode(), content);
        } catch (Exception e) {
            throw e;
        } finally {
            httpPut.abort();
        }
    }
	/**
	 * post 请求
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpClientResponse doPost(String url,
										   Map<String, Object> params, Map<String, Object> headers) throws Exception {
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
//            httpClient = HttpClients.createDefault();
			httpClient = HttpSslClientUtil.createHttpsClient();
			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
					.build();
			httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			if (params != null) {
				String paramsJson = JsonUtil.objectToJson(params);
				StringEntity entity = new StringEntity(paramsJson);
				httpPost.setEntity(entity);
			}
			setHeaders(httpPost, headers);
			response = httpClient.execute(httpPost);
			HttpEntity resentity = response.getEntity();
			String content = null;
			if (resentity != null) {
				content = EntityUtils.toString(resentity, "UTF-8");
			}
			return new HttpClientResponse(response.getStatusLine()
					.getStatusCode(), content);
		} catch (Exception e) {
			throw e;
		} finally {
			httpPost.abort();
		}
	}
	/**
	 * delete 请求
	 * @param url
	 * @param params
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static HttpClientResponse doDelete(String url,
            Map<String, Object> params, Map<String, Object> headers) throws Exception {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        HttpDelete httpDelete = null;
        try {
//            httpClient = HttpClients.createDefault();
			httpClient = HttpSslClientUtil.createHttpsClient();
            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                    .build();
            httpDelete = new HttpDelete(url);
            httpDelete.setConfig(requestConfig);
            setHeaders(httpDelete, headers);
            response = httpClient.execute(httpDelete);
            HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
                content = EntityUtils.toString(resentity, "UTF-8");
            }
            return new HttpClientResponse(response.getStatusLine()
                    .getStatusCode(), content);
        } catch (Exception e) {
            throw e;
        } finally {
            httpDelete.abort();
        }
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
	 * 设置请求头
	 * @param method
	 * @param headers
	 */
	private static void setHeaders(HttpRequestBase method, Map<String, Object> headers) {
        if (headers == null) {
            return;
        }
        for (Entry<String, Object> entry : headers.entrySet()) {
            method.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }

	public static HttpClientResponse doRawPost(String url,Map<String, Map<String,Object>> params, Map<String, Object> headers) throws Exception {
		HttpPost httpPost = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createDefault();

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
					.build();
			httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			setHeaders(httpPost, headers);

			// 设置Http Post数据
			if (params != null) {
				String paramsJson = JsonUtil.convertToJson(params);
				StringEntity entity = new StringEntity(paramsJson);
				httpPost.setEntity(entity);
			}
			response = httpClient.execute(httpPost);
			HttpEntity resentity = response.getEntity();
			String content = null;
			if (resentity != null) {
				content = EntityUtils.toString(resentity, "UTF-8");
			}
			return new HttpClientResponse(response.getStatusLine().getStatusCode(), content);
		} catch (Exception e) {
			throw e;
		} finally {
			httpPost.abort();
		}
	}
	
	public static CloseableHttpResponse doPostWithLogin(String url, Map<String, Object> params, Map<String, Object> headers) throws Exception {
		HttpPost httpPost = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createDefault();

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
					.build();
			httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			setHeader(httpPost, headers);
			List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
			// 设置Http Post数据
			if (params != null) {
				for (Entry<String, Object> entry : params.entrySet()) {
					requestParams.add(new BasicNameValuePair(entry.getKey(), String.valueOf(entry.getValue())));
				}
			}
			HttpEntity entity = new UrlEncodedFormEntity(requestParams, "utf-8");
			httpPost.setEntity(entity);
			response = httpClient.execute(httpPost);
			return response;
		} catch (Exception e) {
			throw e;
		} finally {
			httpPost.abort();
		}
	}

	public static String getHttpUrl(String protocol, String host, Integer port){
		Assert.hasText(protocol);
		Assert.hasText(host);
		Assert.notNull(port);
		return protocol + "://" + host + ":" + port;
	}

	private static void setHeader(HttpRequestBase method, Map<String, Object> headers) {
		if (headers == null) {
			return;
		}
		for (Map.Entry<String, Object> entry : headers.entrySet()) {
			method.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
		}
	}

	/**
	 * 判断是否是application/json方式
	 * @param request 请求
	 * @return boolean
	 */
	public static boolean isApplicationJsonType(HttpServletRequest request) {
		String contentType = request.getContentType();
		if (contentType != null) {
			try {
				MediaType mediaType = MediaType.parseMediaType(contentType);
				if (MediaType.APPLICATION_JSON.includes(mediaType)) {
					return true;
				}
				return false;
			} catch (IllegalArgumentException ex) {
				return false;
			}
		} else {
			return false;
		}
	}
}
