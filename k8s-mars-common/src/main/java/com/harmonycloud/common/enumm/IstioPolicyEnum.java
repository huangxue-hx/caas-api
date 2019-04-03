package com.harmonycloud.common.enumm;

public enum IstioPolicyEnum {

    //根据定义的ruleType字段类型定义
    CIRCUITBREAKER("服务熔断", "circuit breaker"),
    RATELIMIT("服务限流", "rate limit"),
    WHITELISTS("白名单", "white lists"),
    TIMEOUTRETRY("超时重试", "timeout retry"),
    FAULTINJECTION("故障注入", "fault injection"),
    TRAFFICSHIFTING("智能路由", "traffic shifting");

    private final String chPolicyName;
    private final String enPolicyName;

    IstioPolicyEnum(String chPolicyName, String enPolicyName) {
        this.chPolicyName = chPolicyName;
        this.enPolicyName = enPolicyName;
    }

    public String getChPolicyName() {
        return chPolicyName;
    }

    public String getEnPolicyName() {
        return enPolicyName;
    }

}
