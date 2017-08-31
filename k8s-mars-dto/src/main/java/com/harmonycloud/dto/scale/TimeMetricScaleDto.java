package com.harmonycloud.dto.scale;

/**
 * Created by root on 4/10/17.
 */
public class TimeMetricScaleDto {
    private String weekday;
	private String timeSection;
	private Integer targetPods;

	public TimeMetricScaleDto() {
	}

	public TimeMetricScaleDto(String weekday, String timeSection, Integer targetPods) {
		this.weekday = weekday;
		this.timeSection = timeSection;
		this.targetPods = targetPods;
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
