package com.harmonycloud.k8s.bean.scale;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author qg
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeMetricSource {


	private String weekday;

	private String  timeSection;

	private Integer targetPods;

	private Integer normalPods;

	public TimeMetricSource() {
		super();
	}

	public TimeMetricSource(String weekday, String timeSection, Integer targetPods, Integer normalPods) {
		this.weekday = weekday;
		this.targetPods = targetPods;
		this.timeSection = timeSection;
		this.normalPods = normalPods;
	}

	public Integer getNormalPods() {
		return normalPods;
	}

	public void setNormalPods(Integer normalPods) {
		this.normalPods = normalPods;
	}

	public String getWeekday() {
		return weekday;
	}

	public void setWeekday(String weekday) {
		this.weekday = weekday;
	}

	public String getTimeSection() {
		return timeSection;
	}

	public void setTimeSection(String timeSection) {
		this.timeSection = timeSection;
	}

	public Integer getTargetPods() {
		return targetPods;
	}

	public void setTargetPods(Integer targetPods) {
		this.targetPods = targetPods;
	}
}
