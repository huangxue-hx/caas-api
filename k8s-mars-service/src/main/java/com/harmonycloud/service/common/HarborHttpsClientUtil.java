package com.harmonycloud.service.common;

import com.harmonycloud.common.Constant.CommonConstant;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.*;
import com.harmonycloud.k8s.constant.Constant;
import com.harmonycloud.service.platform.client.HarborClient;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.harmonycloud.common.enumm.ErrorCodeMessage.HARBOR_HTTPS_ERROR;


public class HarborHttpsClientUtil {

	//harbor http访问连接超时时间
	private static final int CONNECT_TIMEOUT = 30000;
	//harbor http访问请求响应超时时间,前端超时时间设置1分钟，后台50秒超时时间能反馈给前端
	private static final int SOCKET_TIME_OUT = 50000;

	private static String UTF_8 = "UTF-8";

	private static final Logger LOGGER = LoggerFactory.getLogger(HarborHttpsClientUtil.class);

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
	public static ActionReturnUtil httpGetRequest(String url, Map<String, Object> headers,
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
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
                    .build();
			HttpGet httpGet = new HttpGet(ub.build());
			httpGet.setConfig(requestConfig);
			if (headers != null) {
				for (Entry<String, Object> param : headers.entrySet()) {
					httpGet.addHeader(param.getKey(), String.valueOf(param.getValue()));
				}
			}
			httpClient = getHttpClient();
			CloseableHttpResponse response = httpClient.execute(httpGet);
			return handleResult(httpClientResponse, response, url);
		}catch (SocketTimeoutException e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.RESPONSE_TIMEOUT);
		} catch (Exception e) {
			LOGGER.error("http get error,url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(HARBOR_HTTPS_ERROR);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.error("http client close error,url:{}", e);
			}
		}
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
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
					.build();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			if (headers != null) {
				for (Entry<String, Object> param : headers.entrySet()) {
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
			return handleResult(httpClientResponse, response, url);
		}catch (SocketTimeoutException e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.RESPONSE_TIMEOUT);
		} catch(Exception e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(HARBOR_HTTPS_ERROR);
		}finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.error("http client close error,url:{}", e);
			}
		}
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
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
					.build();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setConfig(requestConfig);
			if (headers != null) {
				for (Entry<String, Object> param : headers.entrySet()) {
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
		}catch (SocketTimeoutException e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.RESPONSE_TIMEOUT);
		} catch (Exception e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(HARBOR_HTTPS_ERROR);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.error("http client close error,url:{}", e);
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
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
					.build();
			HttpPut httpPut = new HttpPut(url);
			httpPut.setConfig(requestConfig);
			if (headers != null) {
				for (Entry<String, Object> param : headers.entrySet()) {
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
			return handleResult(httpClientResponse, response, url);
		}catch (SocketTimeoutException e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.RESPONSE_TIMEOUT);
		}catch (Exception e) {
			LOGGER.error("harbor http put error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(HARBOR_HTTPS_ERROR);
		} finally {
			try {
				if (httpClient != null)
					httpClient.close();
			} catch (IOException e) {
				LOGGER.error("http client close error,url:{}", e);
			}
		}
	}

	public static ActionReturnUtil httpDoDelete(String url,
            Map<String, Object> params, Map<String, Object> headers) throws Exception {
        return httpDoDelete(url, params, headers, SOCKET_TIME_OUT);
    }

	public static ActionReturnUtil httpDoDelete(String url,
				Map<String, Object> params, Map<String, Object> headers, Integer socketTimeout) throws Exception {

		Integer timeOut = SOCKET_TIME_OUT;
		if(socketTimeout != null){
			timeOut = socketTimeout;
		}
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		HttpDelete httpDelete = null;
		try {
			httpClient = getHttpClient();

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeOut).setConnectTimeout(CONNECT_TIMEOUT)
					.build();
			httpDelete = new HttpDelete(url);
			httpDelete.setConfig(requestConfig);
			setHeader(httpDelete, headers);
			response = httpClient.execute(httpDelete);
			HttpEntity resentity = response.getEntity();
			String content = null;
			if (resentity != null) {
				content = EntityUtils.toString(resentity, "UTF-8");
				if (HttpStatusUtil.isSuccessStatus(response.getStatusLine().getStatusCode())) {
					return ActionReturnUtil.returnSuccessWithData(content);
				} else {
					return ActionReturnUtil.returnErrorWithData(content);
				}
			}
		}catch (SocketTimeoutException e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.RESPONSE_TIMEOUT);
		} catch (Exception e) {
			LOGGER.error("harbor http delete error, url:{}",url, e);
			return ActionReturnUtil.returnErrorWithData(HARBOR_HTTPS_ERROR);
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
            httpClient = getHttpClient();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
                    .build();
            httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);
            setHeader(httpPost, headers);

            // 设置Http Post数据
            if (params != null) {
               String paramsJson = JsonUtil.objectToJson(params);
               StringEntity entity = new StringEntity(paramsJson);
               httpPost.setEntity(entity);
            }
            response = httpClient.execute(httpPost);
            return response;
        } catch (Exception e) {
			LOGGER.error("harbor http post error, url:{}",url, e);
            throw new MarsRuntimeException(HARBOR_HTTPS_ERROR);
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
	public static HttpClientResponse doGet(String url, Map<String, Object> params, Map<String, Object> headers) throws Exception {
        HttpGet httpGet = null;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = HttpSslClientUtil.createHttpsClient();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
                    .build();
            httpGet = new HttpGet(url);
            setHeader(httpGet, headers);
            httpGet.setConfig(requestConfig);

            response = httpClient.execute(httpGet);
            HttpEntity resentity = response.getEntity();
            String content = null;
            if (resentity != null) {
                content = EntityUtils.toString(resentity, UTF_8);
            }
            return new HttpClientResponse(response.getStatusLine().getStatusCode(), content);
        } catch (Exception e) {
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
            httpClient = getHttpClient();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
                    .build();
            httpPut = new HttpPut(url);
            httpPut.setConfig(requestConfig);
            if (params != null) {
                String paramsJson = JsonUtil.objectToJson(params);
                StringEntity entity = new StringEntity(paramsJson);
                httpPut.setEntity(entity);
             }
            setHeader(httpPut, headers);
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
            httpClient = getHttpClient();

            // 设置请求和传输超时时间
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
                    .build();
            httpDelete = new HttpDelete(url);
            httpDelete.setConfig(requestConfig);
            setHeader(httpDelete, headers);
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
		for (Entry<String, Object> param : params.entrySet()) {
			pairs.add(new BasicNameValuePair(param.getKey(), String.valueOf(param.getValue())));
		}
		return pairs;
	}


	public static CloseableHttpResponse doPostWithLogin(String url, Map<String, Object> params, Map<String, Object> headers) throws Exception {
		HttpPost httpPost = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = getHttpClient();

			// 设置请求和传输超时时间
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SOCKET_TIME_OUT).setConnectTimeout(CONNECT_TIMEOUT)
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


	private static void setHeader(HttpRequestBase method, Map<String, Object> headers) {
		if (headers == null) {
			return;
		}
		for (Entry<String, Object> entry : headers.entrySet()) {
			method.addHeader(entry.getKey(), String.valueOf(entry.getValue()));
		}
	}

	private static ActionReturnUtil handleResult(HttpClientResponse httpClientResponse,
												 CloseableHttpResponse response, String url) throws Exception{
		Integer statusCode = response.getStatusLine().getStatusCode();
		String statusMessage = response.getStatusLine().getReasonPhrase();
		httpClientResponse.setStatus(statusCode);
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			String result = EntityUtils.toString(entity);
			if (StringUtils.isEmpty(result) || "\n".equals(result)) {
				result = statusMessage;
			}
			//如果是未授权，可能是高可用harbor一台已经挂了，切换到另一台需要再重新登录
			if(HttpStatus.UNAUTHORIZED.value() == statusCode){
				LOGGER.warn("cookie 已经失效,url:{}",url);
				String harborHost = url.substring(url.indexOf("//")+2);
				harborHost = harborHost.substring(0,harborHost.indexOf(":"));
				HarborClient.clearCookie(harborHost);
				return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.HARBOR_COOKIE_INVALID);
			}
			httpClientResponse.setBody(result);
			response.close();
			if (HttpStatusUtil.isSuccessStatus(statusCode)) {
				return ActionReturnUtil.returnSuccessWithData(result);
			} else {
				return ActionReturnUtil.returnErrorWithData(result);
			}
		}
		return ActionReturnUtil.returnError();
	}

}
