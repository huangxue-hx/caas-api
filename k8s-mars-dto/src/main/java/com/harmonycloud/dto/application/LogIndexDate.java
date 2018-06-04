package com.harmonycloud.dto.application;


import java.io.Serializable;
import java.util.Date;

public class LogIndexDate extends IndexInfo implements Serializable {
    private static final long serialVersionUID = 38223223254836476L;
    private String logDate;
    //是否恢复完成
    private boolean restoredDone;

    public LogIndexDate() {
    }

    public LogIndexDate(String logDate) {
        this.logDate = logDate;
    }

    public String getLogDate() {
        return logDate;
    }

    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public boolean getRestoredDone() {
        return restoredDone;
    }

    public void setRestoredDone(boolean restoredDone) {
        this.restoredDone = restoredDone;
    }
}
