package com.harmonycloud.service.application;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dto.application.istio.CircuitBreakDto;
import com.harmonycloud.dto.application.istio.RateLimitDto;
import com.harmonycloud.dto.application.istio.WhiteListsDto;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Created by jmi on 18-9-11.
 */
public interface IstioService {

    ActionReturnUtil createCircuitBreakerPolicy(String deployName, CircuitBreakDto circuitBreakDto) throws Exception;

    ActionReturnUtil updateCircuitBreakerPolicy(String ruleId, CircuitBreakDto circuitBreakDto) throws Exception;

    ActionReturnUtil closeCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil openCircuitBreakerPolicy(String namespace, String policyName, String deployName) throws Exception;

    ActionReturnUtil deleteCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil listIstioPolicies(String deployName, String namespace, String ruleType) throws Exception;

    ActionReturnUtil getCircuitBreakerPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil createRateLimitPolicy(String deployName, RateLimitDto rateLimitDto) throws Exception;

    ActionReturnUtil updateRateLimitPolicy(String ruleId, RateLimitDto rateLimitDto) throws Exception;

    ActionReturnUtil closeRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil openRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil deleteRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil getRateLimitPolicy(String namespace, String policyId, String ruleName) throws Exception;

    ActionReturnUtil createWhiteListsPolicy(WhiteListsDto whiteListsDto) throws Exception;

    ActionReturnUtil updateWhiteListsPolicy(String ruleId, WhiteListsDto whiteListsDto) throws Exception;

    ActionReturnUtil closeWhiteListsPolicy(String namespace, String policyId, String ruleName) throws Exception;

    ActionReturnUtil openWhiteListsPolicy(String namespace,  String policyId, String ruleName) throws Exception;

    ActionReturnUtil deleteWhiteListPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil getWhiteListPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil getClusterIstioPolicySwitch(String clusterId) throws Exception;

    ActionReturnUtil updateClusterIstioPolicySwitch(boolean status, String clusterId) throws Exception;

    ActionReturnUtil  getNamespaceIstioPolicySwitch(String  namespace , String  cluster) throws MarsRuntimeException;

    ActionReturnUtil updateNamespaceIstioPolicySwitch(boolean status, String clusterId,String  namespaceName) throws Exception;

    boolean  getIstioGlobalStatus(String clusterId) throws Exception;
}
