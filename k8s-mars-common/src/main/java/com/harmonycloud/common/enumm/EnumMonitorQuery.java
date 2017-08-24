package com.harmonycloud.common.enumm;

import com.harmonycloud.common.Constant.CommonConstant;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 枚举
 * 
 * @author jmi
 *
 */
public enum EnumMonitorQuery {

	THIRTY_MINUTE("0","30m", "1m", 1800000L), SIX_HOUR("1","6h", "18m", 21600000L), ONE_DAY("2","24h", "1h", 86400000L),
	SEVEN_DAY("3","7d", "8h", 604800000L), THIRTY_DAY("4","30d", "8h", 2592000000L),
	FIVE_MINUTE("6","5m", "20s", 300000L);
    private String code;
	private String range;
	private String interval;
	private Long millisecond;

	/**
	 * 存放所有的code和Enmu的转换.
	 */
	private static final Map<String, EnumMonitorQuery> MONITOR_QUERY_MAP = new ConcurrentHashMap<>(
			EnumMonitorQuery.values().length);


	static {
		/**
		 * 将所有的实体类放入到map中,提供查询.
		 */
		for (EnumMonitorQuery type : EnumSet.allOf(EnumMonitorQuery.class)) {
			MONITOR_QUERY_MAP.put(type.getCode(), type);
		}
	}

	private EnumMonitorQuery(String code, String range, String interval, Long millisecond) {
		this.setCode(code);
		this.setRange(range);
		this.setInterval(interval);
		this.setMillisecond(millisecond);
	}

	public static EnumMonitorQuery getRangeData(String code) {
		if (code == null) {
			return null;
		}
		return MONITOR_QUERY_MAP.get(code);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public String getInterval() {
		return interval;
	}

	public void setInterval(String interval) {
		this.interval = interval;
	}

	public Long getMillisecond() {
		return millisecond;
	}

	public void setMillisecond(Long millisecond) {
		this.millisecond = millisecond;
	}

}
