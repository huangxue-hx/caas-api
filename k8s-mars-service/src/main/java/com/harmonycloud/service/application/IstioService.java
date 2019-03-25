package com.harmonycloud.service.application;

import com.harmonycloud.common.exception.MarsRuntimeException;
import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.istio.bean.IstioGlobalConfigure;
import com.harmonycloud.dto.application.istio.*;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * update by weg on 18-11-19.
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

    ActionReturnUtil getRateLimitPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil createWhiteListsPolicy(WhiteListsDto whiteListsDto) throws Exception;

    ActionReturnUtil updateWhiteListsPolicy(String ruleId, WhiteListsDto whiteListsDto) throws Exception;

    ActionReturnUtil closeWhiteListsPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil openWhiteListsPolicy(String namespace,  String ruleId, String ruleName) throws Exception;

    ActionReturnUtil deleteWhiteListsPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil getWhiteListsPolicy(String namespace, String ruleId, String ruleName) throws Exception;

    ActionReturnUtil createTrafficShiftingPolicy(String deployName, TrafficShiftingDto trafficShiftingDto) throws Exception;

    ActionReturnUtil updateTrafficShiftingPolicy(String ruleId, TrafficShiftingDto trafficShiftingDto) throws Exception;

    ActionReturnUtil closeTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil openTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil deleteTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil getTrafficShiftingPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil createFaultInjectionPolicy(String deployName, FaultInjectionDto faultInjectionDto) throws Exception;

    ActionReturnUtil updateFaultInjectionPolicy(String ruleId, FaultInjectionDto faultInjectionDto) throws Exception;

    ActionReturnUtil closeFaultInjectionPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil openFaultInjectionPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil deleteFaultInjectionPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil getFaultInjectionPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil createTimeoutRetryDtoPolicy(String deployName, TimeoutRetryDto timeoutRetryDto) throws Exception;

    ActionReturnUtil updateTimeoutRetryPolicy(String ruleId, TimeoutRetryDto timeoutRetryDto) throws Exception;

    ActionReturnUtil closeTimeoutRetryPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil openTimeoutRetryPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil deleteTimeoutRetryPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil getTimeoutRetryPolicy(String namespace, String ruleId, String deployName) throws Exception;

    ActionReturnUtil getClusterIstioPolicySwitch(String clusterId) throws Exception;

    ActionReturnUtil updateClusterIstioPolicySwitch(boolean status, String clusterId) throws Exception;

    ActionReturnUtil  getNamespaceIstioPolicySwitch(String  namespace , String  cluster) throws MarsRuntimeException;

    ActionReturnUtil updateNamespaceIstioPolicySwitch(boolean status, String clusterId,String  namespaceName) throws Exception;

    boolean  getIstioGlobalStatus(String clusterId) throws Exception;

    ActionReturnUtil getDesServiceVersion(String deployName, String namespace) throws Exception;

    ActionReturnUtil getSourceServiceVersion(String deployName, String namespace) throws Exception;
}
