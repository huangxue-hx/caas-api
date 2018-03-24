package com.harmonycloud.dto.cicd;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2017-12-20
 * @Modified
 */
public class TimeRuleDto {

    private String dayOfMonth;
    private String dayOfWeek;
    private String hour;
    private String minute;

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

}
