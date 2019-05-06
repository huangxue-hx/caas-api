package com.harmonycloud.service.test.istio;

import com.harmonycloud.common.util.ActionReturnUtil;
import com.harmonycloud.dao.istio.RuleOverviewMapper;
import com.harmonycloud.dao.istio.bean.RuleOverview;
import com.harmonycloud.dto.application.istio.*;
import com.harmonycloud.service.istio.*;
import com.harmonycloud.service.test.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

public class IstioServiceTest extends BaseTest {

    @Autowired
    private IstioCommonService istioCommonService;

    @Autowired
    private IstioCircuitBreakerService circuitBreakerService;

    @Autowired
    private IstioRateLimitService rateLimitService;

    @Autowired
    private IstioWhiteListsService whiteListsService;

    @Autowired
    private IstioTrafficShiftingService trafficShiftingService;

    @Autowired
    private IstioFaultInjectionService faultInjectionService;

    @Autowired
    private IstioTimeoutRetryService timeoutRetryService;

    @Autowired
    private HttpSession session;

    @Autowired
    private RuleOverviewMapper ruleOverviewMapper;

    private String name = "test";

    private String namespace = "logtesttenant-log-app";

    @Test
    public void testCreateDestinationRule() throws Exception {
        ActionReturnUtil actionReturnUtil = istioCommonService.createDestinationRule(name, namespace, "v1");
        assertTrue(actionReturnUtil.isSuccess());
    }

    @Test
    public void testUpdateDestinationRule() throws Exception {
        ActionReturnUtil actionReturnUtil = istioCommonService.updateDestinationRule(name, namespace, "v1", false);
        assertTrue(actionReturnUtil.isSuccess());
    }

    @Test
    public void testDeleteDestinationRule() throws Exception {
        ActionReturnUtil actionReturnUtil = istioCommonService.deleteDestinationRule(name, namespace);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建策略时判断是否有该策略信息
     */
    @Test
    @Rollback(false)
    public void createCircuit() throws Exception {
        session.setAttribute("userId", "1");
        CircuitBreakDto circuitBreakDto = new CircuitBreakDto();
        circuitBreakDto.setMaxConnections(10);
        circuitBreakDto.setHttpVersion("http1");
        circuitBreakDto.setHttp1MaxPendingRequests(5);
        circuitBreakDto.setMaxRequestsPerConnection(1024);
        circuitBreakDto.setConsecutiveErrors(5);
        circuitBreakDto.setInterval(5);
        circuitBreakDto.setBaseEjectionTime(10);
        circuitBreakDto.setNamespace("istio-demo-qs");
        circuitBreakDto.setRuleType("circuitBreaker");
        circuitBreakDto.setRuleName("cruitbreak2");
        circuitBreakDto.setServiceName("svc-demo-sq");
        circuitBreakDto.setScope("0");
        circuitBreakDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.createCircuitBreakerPolicy("svc-demo-sq", circuitBreakDto);
        ActionReturnUtil actionReturnUtils = circuitBreakerService.createCircuitBreakerPolicy("svc-demo-sq", circuitBreakDto);
        assertFalse(actionReturnUtil.isSuccess());

    }

    //删除指定的创建策略
    @Test
    @Rollback(false)
    public void deleteCircuit() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "circuitBreaker");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.deleteRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-sq");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建熔断策略
     */
    @Test
    @Rollback(false)
    public void createCircuitBreak() throws Exception {
        session.setAttribute("userId", "1");
        CircuitBreakDto circuitBreakDto = new CircuitBreakDto();
        circuitBreakDto.setMaxConnections(10);
        circuitBreakDto.setHttpVersion("http1");
        circuitBreakDto.setHttp1MaxPendingRequests(5);
        circuitBreakDto.setMaxRequestsPerConnection(1024);
        circuitBreakDto.setConsecutiveErrors(5);
        circuitBreakDto.setInterval(5);
        circuitBreakDto.setBaseEjectionTime(10);
        circuitBreakDto.setNamespace("istio-demo-qs");
        circuitBreakDto.setRuleType("circuitBreaker");
        circuitBreakDto.setRuleName("cruitbreak1");
        circuitBreakDto.setServiceName("svc-demo-istio");
        circuitBreakDto.setScope("0");
        circuitBreakDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.createCircuitBreakerPolicy("svc-demo-istio", circuitBreakDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 更新熔断策略中 查询该条策略信息为空的情况
     */
    @Test
    @Rollback(false)
    public void updateCircuit() throws Exception {
        session.setAttribute("userId", "1");
        CircuitBreakDto circuitBreakDto = new CircuitBreakDto();
        circuitBreakDto.setMaxConnections(10);
        circuitBreakDto.setHttpVersion("http1");
        circuitBreakDto.setHttp1MaxPendingRequests(6);
        circuitBreakDto.setMaxRequestsPerConnection(1024);
        circuitBreakDto.setConsecutiveErrors(6);
        circuitBreakDto.setInterval(5);
        circuitBreakDto.setBaseEjectionTime(10);
        circuitBreakDto.setNamespace("istio-demo-qs");
        circuitBreakDto.setRuleType("circuitBreaker");
        circuitBreakDto.setRuleName("cruitbreak1");
        circuitBreakDto.setServiceName("svc-demo-istio");
        circuitBreakDto.setScope("0");
        circuitBreakDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.updateCircuitBreakerPolicy("121", circuitBreakDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 修改熔断 策略
     */
    @Test
    @Rollback(false)
    public void updateCircuitBreak() throws Exception {
        //查询除策略信息
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "circuitBreaker");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        CircuitBreakDto circuitBreakDto = new CircuitBreakDto();
        circuitBreakDto.setMaxConnections(10);
        circuitBreakDto.setHttpVersion("http1");
        circuitBreakDto.setHttp1MaxPendingRequests(6);
        circuitBreakDto.setMaxRequestsPerConnection(1024);
        circuitBreakDto.setConsecutiveErrors(6);
        circuitBreakDto.setInterval(5);
        circuitBreakDto.setBaseEjectionTime(10);
        circuitBreakDto.setNamespace("istio-demo-qs");
        circuitBreakDto.setRuleType("circuitBreaker");
        circuitBreakDto.setRuleName("cruitbreak1");
        circuitBreakDto.setServiceName("svc-demo-istio");
        circuitBreakDto.setScope("0");
        circuitBreakDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.updateCircuitBreakerPolicy(ruleOverviewList.get(0).getRuleId(), circuitBreakDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 关闭熔断策略
     */
    @Test
    @Rollback(false)
    public void closeCircuitBreak() throws Exception {
        //查询除策略信息
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "circuitBreaker");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = circuitBreakerService.closeCircuitBreakerPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 开启操作 模拟查询策略是否为空
     */
    @Test
    @Rollback(false)
    public void openCircuit() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.openCircuitBreakerPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev");
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 开启熔断策略
     */
    @Test
    @Rollback(false)
    public void openCircuitBreak() throws Exception {
        //查询除策略信息
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "circuitBreaker");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = circuitBreakerService.openCircuitBreakerPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取熔断策略详情 模拟数据库的值为空
    @Test
    @Rollback(false)
    public void getCircuitData() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = circuitBreakerService.getCircuitBreakerPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev");
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 获取熔断策略详情
     */
    @Test
    @Rollback(false)
    public void getCircuitBreak() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "circuitBreaker");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = circuitBreakerService.getCircuitBreakerPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    /**
     * 删除熔断策略
     */
    @Test
    @Rollback(false)
    public void deleteCircuitBreak() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "circuitBreaker");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.deleteRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建操作  模拟前端传入的Algorithm异常
     */
    @Test
    @Rollback(false)
    public void createRateAlgorithm() throws Exception {
        session.setAttribute("userId", "1");
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setAlgorithm("FIXED_WINDOWs");
        rateLimitDto.setMaxAmount("10");
        rateLimitDto.setNamespace("istio-demo-qs");
        rateLimitDto.setRuleType("rateLimit");
        rateLimitDto.setCreateTime(new Date());
        rateLimitDto.setRuleName("ratelimit1");
        rateLimitDto.setServiceName("svc-demo-istio");
        rateLimitDto.setScope("0");
        rateLimitDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = rateLimitService.createRateLimitPolicy("svc-demo-istio", rateLimitDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 创建操作 模拟数据库已经存在该策略
     */
    @Test
    @Rollback(false)
    public void createRate() throws Exception {
        session.setAttribute("userId", "1");
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setAlgorithm("FIXED_WINDOW");
        rateLimitDto.setMaxAmount("10");
        rateLimitDto.setNamespace("istio-demo-qs");
        rateLimitDto.setRuleType("rateLimit");
        rateLimitDto.setCreateTime(new Date());
        rateLimitDto.setRuleName("ratelimit1");
        rateLimitDto.setServiceName("svc-demo-sq");
        rateLimitDto.setScope("0");
        rateLimitDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = rateLimitService.createRateLimitPolicy("svc-demo-sq", rateLimitDto);
        ActionReturnUtil actionReturnUtils = rateLimitService.createRateLimitPolicy("svc-demo-sq", rateLimitDto);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void deleterate() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "rateLimit");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.deleteRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-sq");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建限流策略
     */
    @Test
    @Rollback(false)
    public void createRateLimit() throws Exception {
        session.setAttribute("userId", "1");
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setAlgorithm("FIXED_WINDOW");
        rateLimitDto.setMaxAmount("10");
        rateLimitDto.setNamespace("istio-demo-qs");
        rateLimitDto.setRuleType("rateLimit");
        rateLimitDto.setCreateTime(new Date());
        rateLimitDto.setRuleName("ratelimit1");
        rateLimitDto.setServiceName("svc-demo-istio");
        rateLimitDto.setScope("0");
        rateLimitDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = rateLimitService.createRateLimitPolicy("svc-demo-istio", rateLimitDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    // 更新操作 模拟该策略为空
    @Test
    @Rollback(false)
    public void updateRate() throws Exception {
        session.setAttribute("userId", "1");
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setAlgorithm("FIXED_WINDOW");
        rateLimitDto.setMaxAmount("10");
        rateLimitDto.setNamespace("istio-demo-qs");
        rateLimitDto.setRuleType("rateLimit");
        rateLimitDto.setCreateTime(new Date());
        rateLimitDto.setRuleName("ratelimit1");
        rateLimitDto.setServiceName("svc-demo-sq");
        rateLimitDto.setScope("0");
        rateLimitDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = rateLimitService.updateRateLimitPolicy("123", rateLimitDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 修改限流策略
     *
     * @throws Exception 异常信息
     */
    @Test
    @Rollback(false)
    public void updateRateLimit() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "rateLimit");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        RateLimitDto rateLimitDto = new RateLimitDto();
        rateLimitDto.setAlgorithm("FIXED_WINDOW");
        rateLimitDto.setMaxAmount("10");
        rateLimitDto.setNamespace("istio-demo-qs");
        rateLimitDto.setRuleType("rateLimit");
        rateLimitDto.setCreateTime(new Date());
        rateLimitDto.setRuleName("ratelimit1");
        rateLimitDto.setServiceName("svc-demo-istio");
        rateLimitDto.setScope("0");
        rateLimitDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = rateLimitService.updateRateLimitPolicy(ruleOverviewList.get(0).getRuleId(), rateLimitDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void closeRateLimit() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "rateLimit");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.closeRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //打开操作 模拟为获取到对应的策略
    @Test
    @Rollback(false)
    public void openRate() throws Exception {
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.openRateLimitPolicy("istio-demo-qs", "123", "svc-demo-sq");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void openRateLimit() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "rateLimit");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.openRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取详情   模拟该策略为空
    @Test
    @Rollback(false)
    public void getRate() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.getRateLimitPolicy("istio-demo-qs", "123", "svc-demo-istio");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void getRateLimitPolicy() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "rateLimit");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = rateLimitService.getRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    @Test
    @Rollback(false)
    public void deleteRateLimit() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "rateLimit");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = rateLimitService.deleteRateLimitPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //创建白名单操作  模拟已经存在该策略
    @Test
    @Rollback(false)
    public void createWhite() throws Exception {
        session.setAttribute("userId", "1");
        WhiteListsDto whiteListsDto = new WhiteListsDto();
        WhiteServiceDto whiteServiceDto = new WhiteServiceDto();
        whiteServiceDto.setName("svc-demo-sq");
        whiteServiceDto.setNamespace("istio-demo-qs");
        List<WhiteServiceDto> whiteServiceDtos = new ArrayList();
        whiteServiceDtos.add(whiteServiceDto);

        whiteListsDto.setWhiteNameList(whiteServiceDtos);
        whiteListsDto.setRuleName("whitelist1");
        whiteListsDto.setRuleType("whiteLists");
        whiteListsDto.setNamespace("istio-demo-qs");
        whiteListsDto.setServiceName("svc-demo-sq");
        whiteListsDto.setScope("0");
        ActionReturnUtil actionReturnUtil = whiteListsService.createWhiteListsPolicy(whiteListsDto);
        ActionReturnUtil actionReturnUtils = whiteListsService.createWhiteListsPolicy(whiteListsDto);
        assertFalse(actionReturnUtils.isSuccess());
    }

    /**
     * 创建白名单策略
     *
     * @return
     */
    @Test
    @Rollback(false)
    public void createWhiteLists() throws Exception {
        session.setAttribute("userId", "1");
        WhiteListsDto whiteListsDto = new WhiteListsDto();
        WhiteServiceDto whiteServiceDto = new WhiteServiceDto();
        whiteServiceDto.setName("svc-demo-sq");
        whiteServiceDto.setNamespace("istio-demo-qs");
        List<WhiteServiceDto> whiteServiceDtos = new ArrayList();
        whiteServiceDtos.add(whiteServiceDto);

        whiteListsDto.setWhiteNameList(whiteServiceDtos);
        whiteListsDto.setRuleName("whitelist1");
        whiteListsDto.setRuleType("whiteLists");
        whiteListsDto.setNamespace("istio-demo-qs");
        whiteListsDto.setServiceName("svc-demo-istio");
        whiteListsDto.setScope("0");
        ActionReturnUtil actionReturnUtil = whiteListsService.createWhiteListsPolicy(whiteListsDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    //更新白名单操作
    @Test
    @Rollback(false)
    public void updateWhite() throws Exception {
        session.setAttribute("userId", "1");
        WhiteListsDto whiteListsDto = new WhiteListsDto();
        WhiteServiceDto whiteServiceDto = new WhiteServiceDto();
        whiteServiceDto.setName("svc-demo-sq");
        whiteServiceDto.setNamespace("istio-demo-qs");
        List<WhiteServiceDto> whiteServiceDtos = new ArrayList();
        whiteServiceDtos.add(whiteServiceDto);

        whiteListsDto.setWhiteNameList(whiteServiceDtos);
        whiteListsDto.setRuleName("whitelist1");
        whiteListsDto.setRuleType("whiteLists");
        whiteListsDto.setNamespace("istio-demo-qs");
        whiteListsDto.setServiceName("svc-demo-istio");
        whiteListsDto.setScope("0");
        ActionReturnUtil actionReturnUtil = whiteListsService.updateWhiteListsPolicy("123", whiteListsDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    /**
     * 修改白名单策略
     */
    @Test
    @Rollback(false)
    public void updateWhiteLists() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "whiteLists");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        WhiteListsDto whiteListsDto = new WhiteListsDto();
        WhiteServiceDto whiteServiceDto = new WhiteServiceDto();
        whiteServiceDto.setName("svc-demo-sq");
        whiteServiceDto.setNamespace("istio-demo-qs");
        List<WhiteServiceDto> whiteServiceDtos = new ArrayList();
        whiteServiceDtos.add(whiteServiceDto);

        whiteListsDto.setWhiteNameList(whiteServiceDtos);
        whiteListsDto.setRuleName("whitelist1");
        whiteListsDto.setRuleType("whiteLists");
        whiteListsDto.setNamespace("istio-demo-qs");
        whiteListsDto.setServiceName("svc-demo-istio");
        whiteListsDto.setScope("0");
        ActionReturnUtil actionReturnUtil = whiteListsService.updateWhiteListsPolicy(ruleOverviewList.get(0).getRuleId(), whiteListsDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 关闭白名单
     */
    @Test
    @Rollback(false)
    public void closeWhiteLists() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "whiteLists");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = whiteListsService.closeWhiteListsPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //白名单打开操作  模拟对应的策略为空
    @Test
    @Rollback(false)
    public void openWhite() throws Exception {
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = whiteListsService.openWhiteListsPolicy("istio-demo-qs", "123", "svc-demo-sq");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void openWhiteLists() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "whiteLists");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = whiteListsService.openWhiteListsPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取详情  模拟策略为空
    @Test
    @Rollback(false)
    public void getWhite() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = whiteListsService.getWhiteListsPolicy("istio-demo-qs", "123", "svc-demo-istio");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void getWhiteListsPolicy() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "whiteLists");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = whiteListsService.getWhiteListsPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    @Test
    @Rollback(false)
    public void deleteWhiteLists() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "whiteLists");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = whiteListsService.deleteWhiteListsPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    // 创建智能路由操作  模拟DesServices以及matches为空
    @Test
    @Rollback(false)
    public void createDesAndMatch() throws Exception {
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemoistio");
//        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-istio");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-istio");
        trafficShiftingDto.setScope("0");
        trafficShiftingDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.createTrafficShiftingPolicy("svc-demo-istio", trafficShiftingDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //创建智能路由操作  模拟数据库里已经存在策略
    @Test
    @Rollback(false)
    public void createTrafficData() throws Exception {
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemosq");
        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-sq");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-sq");
        trafficShiftingDto.setScope("0");
        trafficShiftingDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.createTrafficShiftingPolicy("svc-demo-sq", trafficShiftingDto);
        ActionReturnUtil actionReturnUtils = trafficShiftingService.createTrafficShiftingPolicy("svc-demo-sq", trafficShiftingDto);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void deleteTraffic() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.deleteTrafficShiftingPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-sq");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建智能路由
     */
    @Test
    @Rollback(false)
    public void createTrafficShifting() throws Exception {
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemoistio");
        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-istio");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-istio");
        trafficShiftingDto.setScope("0");
        trafficShiftingDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.createTrafficShiftingPolicy("svc-demo-istio", trafficShiftingDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    //更新智能路由操作  模拟数据库里不存在该策略
    @Test
    @Rollback(false)
    public void updateTrafficData() throws Exception {
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemoistio");
        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-istio");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-istio");
        trafficShiftingDto.setScope("0");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.updateTrafficShiftingPolicy("123", trafficShiftingDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //更新智能路由操作  模拟DesServices以及matches为空
    @Test
    @Rollback(false)
    public void updateTrafficDesAndmatches() throws Exception {
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemosq");
        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-sq");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-sq");
        trafficShiftingDto.setScope("0");
        trafficShiftingDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.createTrafficShiftingPolicy("svc-demo-sq", trafficShiftingDto);
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDtos = new TrafficShiftingDto();
        TrafficShiftingMatchDto trafficShiftingMatchDtos = new TrafficShiftingMatchDto();
        List<String> header = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDtos.setHeaders(headers);
        trafficShiftingMatchDtos.setSubset("svcdemosq");

        trafficShiftingDtos.setProtocol("http");
        trafficShiftingDtos.setRuleName("svc-demo-sq");
        trafficShiftingDtos.setRuleType("trafficShifting");
        trafficShiftingDtos.setNamespace("istio-demo-qs");
        trafficShiftingDtos.setServiceName("svc-demo-sq");
        trafficShiftingDtos.setScope("0");
        ActionReturnUtil actionReturnUtils = trafficShiftingService.updateTrafficShiftingPolicy(ruleOverviewList.get(0).getRuleId(), trafficShiftingDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void updateTrafficShifting() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        TrafficShiftingDto trafficShiftingDto = new TrafficShiftingDto();
        List<TrafficShiftingMatchDto> matchs = new ArrayList();
        TrafficShiftingMatchDto trafficShiftingMatchDto = new TrafficShiftingMatchDto();
        List<String> headers = new ArrayList();
        headers.add("key=nihao");
        trafficShiftingMatchDto.setHeaders(headers);
        trafficShiftingMatchDto.setSubset("svcdemoistio");
        matchs.add(trafficShiftingMatchDto);
        trafficShiftingDto.setMatches(matchs);

        trafficShiftingDto.setProtocol("http");
        trafficShiftingDto.setRuleName("svc-demo-istio");
        trafficShiftingDto.setRuleType("trafficShifting");
        trafficShiftingDto.setNamespace("istio-demo-qs");
        trafficShiftingDto.setServiceName("svc-demo-istio");
        trafficShiftingDto.setScope("0");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.updateTrafficShiftingPolicy(ruleOverviewList.get(0).getRuleId(), trafficShiftingDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 关闭智能路由
     */
    @Test
    @Rollback(false)
    public void closeTrafficShifting() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.closeTrafficShiftingPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //开启智能路由  模拟数据库没有改策略信息
    @Test
    @Rollback(false)
    public void openTraffic() throws Exception {
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.openTrafficShiftingPolicy("istio-demo-qs", "123", "svc-demo-istio");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void openTrafficShifting() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.openTrafficShiftingPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取详情 模拟数据库的数据位空
    @Test
    @Rollback(false)
    public void getTraffic() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.getTrafficShiftingPolicy("istio-demo-qs", "123", "svc-demo-istio");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void getTrafficShiftingPolicy() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "trafficShifting");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = trafficShiftingService.getTrafficShiftingPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    @Test
    @Rollback(false)
    public void deleteTrafficShifting() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "trafficShifting");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = trafficShiftingService.deleteTrafficShiftingPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //创建超时重试操作   模拟perTryTimeout以及attempts的前端传入参数异常
    @Test
    @Rollback(false)
    public void createTimeoutPerAndAttem() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry2");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-sq");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //创建超时重试   模拟 Timeout 以及 Attempts的值为空
    @Test
    @Rollback(false)
    public void createTimeoutTimeAndAttem() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
//        timeoutRetryDto.setTimeout("10");
//        timeoutRetryDto.setAttempts("5");
//        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry2");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-sq");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //创建超时重试操作  模拟数据已经存在该策略
    @Test
    @Rollback(false)
    public void createTimeoutData() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setAttempts("5");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry2");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-sq");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        ActionReturnUtil actionReturnUtils = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void deleteTimeout() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.deleteTimeoutRetryPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-sq", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建超时重试
     */
    @Test
    @Rollback(false)
    public void createTimeoutRetry() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setAttempts("5");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry1");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-istio");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-istio", timeoutRetryDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    //更新超时重试操作  模拟PerTryTimeout 和 Attempts传入的参数异常
    @Test
    @Rollback(false)
    public void updateTimeoutPerAndAttempts() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setAttempts("5");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry2");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-sq");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDtos = new TimeoutRetryDto();
        timeoutRetryDtos.setTimeout("10");
//        timeoutRetryDtos.setAttempts("5");
        timeoutRetryDtos.setPerTryTimeout("8");
        timeoutRetryDtos.setRuleName("timeoutretry1");
        timeoutRetryDtos.setRuleType("timeoutRetry");
        timeoutRetryDtos.setNamespace("istio-demo-qs");
        timeoutRetryDtos.setServiceName("svc-demo-sq");
        timeoutRetryDtos.setScope("0");
        timeoutRetryDtos.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtils = timeoutRetryService.updateTimeoutRetryPolicy(ruleOverviewList.get(0).getRuleId(), timeoutRetryDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    //更新超时重试操作 模拟timeout以及attempts都为空
    @Test
    @Rollback(false)
    public void updateTimeAndAttem() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setAttempts("5");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry2");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-sq");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.createTimeoutRetryPolicy("svc-demo-sq", timeoutRetryDto);
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDtos = new TimeoutRetryDto();
//        timeoutRetryDtos.setTimeout("10");
//        timeoutRetryDtos.setAttempts("5");
//        timeoutRetryDtos.setPerTryTimeout("8");
        timeoutRetryDtos.setRuleName("timeoutretry1");
        timeoutRetryDtos.setRuleType("timeoutRetry");
        timeoutRetryDtos.setNamespace("istio-demo-qs");
        timeoutRetryDtos.setServiceName("svc-demo-sq");
        timeoutRetryDtos.setScope("0");
        timeoutRetryDtos.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtils = timeoutRetryService.updateTimeoutRetryPolicy(ruleOverviewList.get(0).getRuleId(), timeoutRetryDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    //更新超时重试操作  模拟数据库里不存在该策略
    @Test
    @Rollback(false)
    public void updateTimeoutData() throws Exception {
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDtos = new TimeoutRetryDto();
        timeoutRetryDtos.setTimeout("10");
        timeoutRetryDtos.setAttempts("5");
        timeoutRetryDtos.setPerTryTimeout("8");
        timeoutRetryDtos.setRuleName("timeoutretry1");
        timeoutRetryDtos.setRuleType("timeoutRetry");
        timeoutRetryDtos.setNamespace("istio-demo-qs");
        timeoutRetryDtos.setServiceName("svc-demo-sq");
        timeoutRetryDtos.setScope("0");
        timeoutRetryDtos.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtils = timeoutRetryService.updateTimeoutRetryPolicy("123", timeoutRetryDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void updateTimeoutRetry() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        TimeoutRetryDto timeoutRetryDto = new TimeoutRetryDto();
        timeoutRetryDto.setTimeout("10");
        timeoutRetryDto.setAttempts("5");
        timeoutRetryDto.setPerTryTimeout("8");
        timeoutRetryDto.setRuleName("timeoutretry1");
        timeoutRetryDto.setRuleType("timeoutRetry");
        timeoutRetryDto.setNamespace("istio-demo-qs");
        timeoutRetryDto.setServiceName("svc-demo-istio");
        timeoutRetryDto.setScope("0");
        timeoutRetryDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.updateTimeoutRetryPolicy(ruleOverviewList.get(0).getRuleId(), timeoutRetryDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 关闭超时重试
     */
    @Test
    @Rollback(false)
    public void closeTimeoutRetry() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.closeTimeoutRetryPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //开启超时重试操作  模拟数据库无该策略信息
    @Test
    @Rollback(false)
    public void openTimeoutData() throws Exception {
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.openTimeoutRetryPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev", "");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void openTimeoutRetry() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.openTimeoutRetryPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev", "");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取详情  模拟数据库无该策略信息
    @Test
    @Rollback(false)
    public void getTimeoutData() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.getTimeoutRetryPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void getTimeoutRetryPolicy() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "timeoutRetry");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = timeoutRetryService.getTimeoutRetryPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    @Test
    @Rollback(false)
    public void deleteTimeoutRetry() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "timeoutRetry");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = timeoutRetryService.deleteTimeoutRetryPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //创建故障注入操作  模拟前端数据异常
    @Test
    @Rollback(false)
    public void createFaultException() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
//        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection2");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //创建故障注入操作 模拟fixedDelay以及httpStatus不传值
    @Test
    @Rollback(false)
    public void createFaultFixedAndttpStatus() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
//        faultInjectionDto.setFixedDelay("10");
//        faultInjectionDto.setDelayPercent("100");
//        faultInjectionDto.setHttpStatus("500");
//        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection2");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-sq");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        assertFalse(actionReturnUtil.isSuccess());
    }

    //创建故障注入操作 模拟数据库无该策略信息
    @Test
    @Rollback(false)
    public void createFaultData() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection2");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-sq");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        ActionReturnUtil actionReturnUtils = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void deleteFault() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.deleteFaultInjectionPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-sq", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 创建故障注入
     */
    @Test
    @Rollback(false)
    public void createFaultInjection() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection1");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-istio");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-istio", faultInjectionDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    //更新故障注入操作 模拟前端数据异常
    @Test
    @Rollback(false)
    public void updateFaultException() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection2");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-sq");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDtos = new FaultInjectionDto();
        faultInjectionDtos.setFixedDelay("10");
//        faultInjectionDtos.setDelayPercent("100");
        faultInjectionDtos.setHttpStatus("500");
//        faultInjectionDtos.setCodePercent("100");
        faultInjectionDtos.setRuleName("faultinjection1");
        faultInjectionDtos.setRuleType("faultInjection");
        faultInjectionDtos.setNamespace("istio-demo-qs");
        faultInjectionDtos.setServiceName("svc-demo-sq");
        faultInjectionDtos.setScope("0");
        faultInjectionDtos.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtils = faultInjectionService.updateFaultInjectionPolicy(ruleOverviewList.get(0).getRuleId(), faultInjectionDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    //更新故障注入操作  模拟FixedDelay以及HttpStatus传值为空
    @Test
    @Rollback(false)
    public void updateFault() throws Exception {
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection2");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-sq");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.createFaultInjectionPolicy("svc-demo-sq", faultInjectionDto);
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-sq");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDtos = new FaultInjectionDto();
//        faultInjectionDtos.setFixedDelay("10");
//        faultInjectionDtos.setDelayPercent("100");
//        faultInjectionDtos.setHttpStatus("500");
//        faultInjectionDtos.setCodePercent("100");
        faultInjectionDtos.setRuleName("faultinjection1");
        faultInjectionDtos.setRuleType("faultInjection");
        faultInjectionDtos.setNamespace("istio-demo-qs");
        faultInjectionDtos.setServiceName("svc-demo-sq");
        faultInjectionDtos.setScope("0");
        faultInjectionDtos.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtils = faultInjectionService.updateFaultInjectionPolicy(ruleOverviewList.get(0).getRuleId(), faultInjectionDtos);
        assertFalse(actionReturnUtils.isSuccess());
    }

    @Test
    @Rollback(false)
    public void updateFaultInjection() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        FaultInjectionDto faultInjectionDto = new FaultInjectionDto();
        faultInjectionDto.setFixedDelay("10");
        faultInjectionDto.setDelayPercent("100");
        faultInjectionDto.setHttpStatus("500");
        faultInjectionDto.setCodePercent("100");
        faultInjectionDto.setRuleName("faultinjection1");
        faultInjectionDto.setRuleType("faultInjection");
        faultInjectionDto.setNamespace("istio-demo-qs");
        faultInjectionDto.setServiceName("svc-demo-istio");
        faultInjectionDto.setScope("0");
        faultInjectionDto.setClusterId("dc-yantai--dev");
        ActionReturnUtil actionReturnUtil = faultInjectionService.updateFaultInjectionPolicy(ruleOverviewList.get(0).getRuleId(), faultInjectionDto);
        assertTrue(actionReturnUtil.isSuccess());
    }

    /**
     * 关闭白名单
     */
    @Test
    @Rollback(false)
    public void closeFaultInjection() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.closeFaultInjectionPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //开启故障注入操作 模拟数据库没有改策略信息
    @Test
    @Rollback(false)
    public void openFaultData() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.openFaultInjectionPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev", "");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void openFaultInjection() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        //打开操作
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.openFaultInjectionPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev", "");
        assertTrue(actionReturnUtil.isSuccess());
    }

    //获取详情  模拟数据库的策略为空
    @Test
    @Rollback(false)
    public void getFaultData() throws Exception {
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.getFaultInjectionPolicy("istio-demo-qs", "123", "svc-demo-istio", "dc-yantai--dev");
        assertFalse(actionReturnUtil.isSuccess());
    }

    @Test
    @Rollback(false)
    public void getFaultInjectionPolicy() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "faultInjection");
        session.setAttribute("userId", "1");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        ActionReturnUtil actionReturnUtil = faultInjectionService.getFaultInjectionPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertNotNull(actionReturnUtil.getData());
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(actionReturnUtil.getData());
        String dataStatus = jsonObject.get("dataStatus").toString();
        assertEquals(dataStatus, "0");
    }

    @Test
    @Rollback(false)
    public void deleteFaultInjection() throws Exception {
        Map ruleInfo = new HashMap();
        ruleInfo.put("ruleClusterId", "dc-yantai--dev");
        ruleInfo.put("ruleNs", "istio-demo-qs");
        ruleInfo.put("ruleSvc", "svc-demo-istio");
        ruleInfo.put("ruleType", "faultInjection");
        List<RuleOverview> ruleOverviewList = this.getRuleOverview(ruleInfo);
        session.setAttribute("userId", "1");
        ActionReturnUtil actionReturnUtil = faultInjectionService.deleteFaultInjectionPolicy("istio-demo-qs", ruleOverviewList.get(0).getRuleId(), "svc-demo-istio", "dc-yantai--dev");
        assertTrue(actionReturnUtil.isSuccess());
    }


    public List<RuleOverview> getRuleOverview(Map ruleInfo) {
        //查询destinationrule
        List<RuleOverview> ruleOverviewList = ruleOverviewMapper.selectByRuleInfo(ruleInfo);
        return ruleOverviewList;
    }
}