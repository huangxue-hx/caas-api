package com.harmonycloud.common.enumm.apiserver;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum EnumApiServerUrlAggRangeType {
    /**
     * ID:0 30分内 1分一个点共30个点 30m=1800000ms
     */
    THIRTY_MINUTE("0","30m", "1m", 1800000L),
    /**
     * ID:1 2小时内 5分一个点共24个点 2h=7200000ms
     */
    TWO_HOUR("1","2h", "5m", 7200000L),
    /**
     * ID:2 6小时内 15分一个点共24个点 6h=21600000ms
     */
    SIX_HOUR("2","6h", "15m", 21600000L),
    /**
     * ID:3 12小时内 30分一个点共24个点 12h=43200000ms
     */
    TWELVE_HOUR("3","12h", "30m", 43200000L),
    /**
     * ID:4 24小时内 48min一个点共30个点 24h=86400000ms
     */
    TWENTY_FOUR_HOUR("4","24h", "48m", 86400000L),
    /**
     * ID:5 7天内 4小时一个点共42个点 7d=604800000ms
     */
    SEVEN_DAY("5","7d", "4h", 604800000L),
    /**
     * ID:6 30天内 10小时一个点共72个点 30d=2592000000ms
     */
    THIRTY_DAY("6","30d", "10h", 2592000000L);
    private String code;
    private String range;
    private String interval;
    private Long millisecond;

    /**
     * 存放所有的code和Enmu的转换.
     */
    private static final Map<String, EnumApiServerUrlAggRangeType> UrlAggRangeType_MAP = new ConcurrentHashMap<>(EnumApiServerUrlAggRangeType.values().length);

    static {
        /**
         * 将所有的实体类放入到map中,提供查询.
         */
        for (EnumApiServerUrlAggRangeType type : EnumSet.allOf(EnumApiServerUrlAggRangeType.class)) {
            UrlAggRangeType_MAP.put(type.getCode(), type);
        }
    }

    EnumApiServerUrlAggRangeType(String code, String range, String interval, Long millisecond) {
        this.setCode(code);
        this.setRange(range);
        this.setInterval(interval);
        this.setMillisecond(millisecond);
    }

    public static EnumApiServerUrlAggRangeType getEnumRangeType(String code) {
        if (code == null) {
            return null;
        }
        return UrlAggRangeType_MAP.get(code);
    }

    public String getCode() {
        return code;
    }

    private void setCode(String code) {
        this.code = code;
    }

    public String getRange() {
        return range;
    }

    private void setRange(String range) {
        this.range = range;
    }

    public String getInterval() {
        return interval;
    }

    private void setInterval(String interval) {
        this.interval = interval;
    }

    public Long getMillisecond() {
        return millisecond;
    }

    private void setMillisecond(Long millisecond) {
        this.millisecond = millisecond;
    }
}
