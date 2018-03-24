package com.harmonycloud.service.platform.serviceImpl;

import com.alibaba.fastjson.JSONObject;
import com.harmonycloud.common.enumm.ErrorCodeMessage;
import com.harmonycloud.common.util.*;
import com.harmonycloud.dao.system.bean.SystemConfig;
import com.harmonycloud.dto.log.FullLinkPodDto;
import com.harmonycloud.dto.log.FullLinkQueryDto;
import com.harmonycloud.service.platform.service.FullLinkLogService;
import com.harmonycloud.service.system.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;

import static com.harmonycloud.common.Constant.CommonConstant.CONFIG_TYPE_FULLLINK;


@Service
public class FullLinkLogServiceImpl implements FullLinkLogService{

	private static final Logger LOGGER = LoggerFactory.getLogger(FullLinkLogServiceImpl.class);
    private static final String URL_LIST_APP = "/apm/applist";
	private static final String URL_ERROR_ANALYSIS = "/apm/app/errorAnalysis";
	private static final String URL_ERROR_TRANSACTIONS = "/apm/app/errorTransactions";
	private static final String URL_TRANSACTION_TRACE = "/apm/transactionTrace";
	private static final String URL_LOGIN = "/SysUser/Syslogin";
	private static final int DEFAULT_TRANSACTON_RECORD = 10;
	private static String master_ip = null;
	private static String http_url = null;


	@Autowired
	SystemConfigService systemConfigService;

	@PostConstruct
	public void init(){
		List<SystemConfig> fullLinkConfigs = systemConfigService.findByConfigType(CONFIG_TYPE_FULLLINK);
		if(CollectionUtils.isEmpty(fullLinkConfigs)){
			return;
		}
		for(SystemConfig config: fullLinkConfigs){
			String value = config.getConfigValue();
			switch (config.getConfigName()){
				case "apm_master_ip":
					master_ip = value;
					break;
				case "apm_http_url":
					http_url = config.getConfigValue();
					ApmHttpClient.setLoginUrl(value + URL_LOGIN);
					break;
				case "apm_username":
					ApmHttpClient.setUsername(value);
					break;
				case "apm_password":
					ApmHttpClient.setPassword(value);
					break;
				default:
					break;
			}
		}
		if(StringUtils.isBlank(master_ip)){
			LOGGER.info("未设置apm_master_ip");
		}
	}


	@Override
	public ActionReturnUtil listPod(FullLinkQueryDto queryDto) {
		if(StringUtils.isBlank(master_ip)){
			LOGGER.info("未设置apm全链路查询参数masterIp");
			return ActionReturnUtil.returnSuccess();
		}
		try {
			List<FullLinkPodDto> podListResult = new ArrayList<>();
			ActionReturnUtil response = ApmHttpClient.httpsGetRequest(http_url + URL_LIST_APP,
					formHeader(), convertParams(queryDto));
			LOGGER.info("listapp response:{}", JSONObject.toJSONString(response));
			if(response.isSuccess()) {
				Map map = JsonUtil.jsonToMap(response.get("data").toString());
				if((boolean)map.get("success") && !CollectionUtils.isEmpty((List)map.get("data"))){
					List<Map> podList = (List<Map>)map.get("data");
					for(Map podMap: podList){
						String name = podMap.get("name").toString();
						if(name.indexOf("@") == -1 || name.indexOf(":") == -1){
							LOGGER.error("agentId：{} 格式错误", name);
							continue;
						}
						String podName = name.substring(name.indexOf("@")+1, name.indexOf(":"));
						String[] podNamePart = podName.split("-");
						if(podNamePart.length <3){
							LOGGER.error("pod名称格式错误： " + podName);
							continue;
						}
						if(BizUtil.isPodWithDeployment(podName, queryDto.getDeployment())){
							FullLinkPodDto podDto = new FullLinkPodDto();
							podDto.setName(name);
							podDto.setPodName(podName);
							podListResult.add(podDto);
						}
					}
					return ActionReturnUtil.returnSuccessWithData(podListResult);
				}
			}
			return response;
		}catch (Exception e){
			LOGGER.info("listapp error for：{}", master_ip, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}

	}

	@Override
	public ActionReturnUtil errorAnalysis(FullLinkQueryDto queryDto) {
		try {
			ActionReturnUtil response = ApmHttpClient.httpsPostRequest(http_url + URL_ERROR_ANALYSIS,
					formHeader(), convertParams(queryDto));
			LOGGER.info("errorAnalysis response:{}", JSONObject.toJSONString(response));
			return response;
		}catch (Exception e){
			LOGGER.info("errorAnalysis fail for query：{}", JSONObject.toJSONString(queryDto), e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}
	}

	@Override
	public ActionReturnUtil errorTransactions(FullLinkQueryDto queryDto) {

		try {
			ActionReturnUtil response = ApmHttpClient.httpsPostRequest(http_url + URL_ERROR_TRANSACTIONS,
					formHeader(), convertParams(queryDto));
			LOGGER.info("errorTransactions response:{}", JSONObject.toJSONString(response));
			return response;
		}catch (Exception e){
			LOGGER.info("errorTransactions fail for query：{}", JSONObject.toJSONString(queryDto), e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}
	}

	@Override
	public ActionReturnUtil transactionTrace(String transactionId) {

		try {
			Map<String, Object> params = new HashMap<>();
			params.put("transactionId", transactionId);
			params.put("type","");
			params.put("target", master_ip);
			ActionReturnUtil response = ApmHttpClient.httpsPostRequest(http_url + URL_TRANSACTION_TRACE,
					formHeader(), params);
			LOGGER.info("transactionTrace response:{}", JSONObject.toJSONString(response));
			return response;
		}catch (Exception e){
			LOGGER.info("transactionTrace error for transactionId：{}", transactionId, e);
			return ActionReturnUtil.returnErrorWithData(ErrorCodeMessage.QUERY_FAIL);
		}
	}

	private Map<String, Object> convertParams(FullLinkQueryDto queryDto){
		Map<String, Object> params = new HashMap<>();
		if(StringUtils.isNotBlank(queryDto.getAgentId())) {
			params.put("agentId", queryDto.getAgentId());
		}
		if(StringUtils.isNotBlank(queryDto.getBusinessId())) {
			params.put("businessId", queryDto.getBusinessId());
		}
		if(StringUtils.isNotBlank(queryDto.getExceptionType())) {
			params.put("exceptionType", queryDto.getExceptionType());
		}
		//params.put("exceptionType", "Exception");
		if(queryDto.getStatusCode() != null) {
			params.put("statusCode", queryDto.getStatusCode());
		}
		params.put("top", queryDto.getTop()==null?DEFAULT_TRANSACTON_RECORD:queryDto.getTop());
		if(StringUtils.isNotBlank(queryDto.getUrl())) {
			params.put("url", queryDto.getUrl());
		}
		if(StringUtils.isNotBlank(queryDto.getServerUrl())) {
			params.put("serverUrl", queryDto.getServerUrl());
		}
		params.put("type","");
		params.put("from",queryDto.getFromTime());
		params.put("to",queryDto.getToTime());
		params.put("target", master_ip);
		return params;
	}

	private Map<String, Object> formHeader(){
		Map<String, Object> headers = new HashMap<>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		return headers;
	}


}
