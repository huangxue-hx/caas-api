package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeMetricStatus {

	private String  currentWeekDayTime;

	public String getCurrentWeekDayTime() {
		return currentWeekDayTime;
	}

	public void setCurrentWeekDayTime(String currentWeekDayTime) {
		this.currentWeekDayTime = currentWeekDayTime;
	}
}
