package com.harmonycloud.service.platform.bean.harbor;

import java.util.List;
import java.util.Map;

/**
 * 定时触发模式参数
 */
public class HarborPolicyScheduledTrigger {
    private String type;
    private Integer weekday;
    private Integer hour;
    private Integer minute;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getWeekday() {
        return weekday;
    }

    public void setWeekday(Integer weekday) {
        this.weekday = weekday;
    }

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Integer getMinute() {
        return minute;
    }

    public void setMinute(Integer minute) {
        this.minute = minute;
    }
}