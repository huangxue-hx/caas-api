package com.harmonycloud.common.util;


import com.harmonycloud.common.util.date.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by czm on 2017/3/29.
 * 根据条件查询查询结果
 *
 */
public class SearchResult implements Comparable<SearchResult>{
    private String user;
//    private String tenant;
    private String module;
    private String opFun;
    private String opType;
    private String opTime;
    private String opDetails;
    private String opStatus;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

//    public String getTenant() {
//        return tenant;
//    }
//
//    public void setTenant(String tenant) {
//        this.tenant = tenant;
//    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getOpFun() {
        return opFun;
    }

    public void setOpFun(String opFun) {
        this.opFun = opFun;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getOpTime() {
        return opTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }

    public String getOpDetails() {
        return opDetails;
    }

    public void setOpDetails(String opDetails) {
        this.opDetails = opDetails;
    }

    public String getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(String opStatus) {
        this.opStatus = opStatus;
    }


    @Override
    public int compareTo(SearchResult o) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int res = 0 ;
        try {
            res =  df.parse(o.getOpTime()).compareTo(df.parse(this.getOpTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return res;
    }
}
