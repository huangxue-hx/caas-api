package com.harmonycloud.dao.cluster.bean;

import com.alibaba.fastjson.JSON;
import com.harmonycloud.common.util.date.DateUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by czm on 2017/6/8.
 */
public class RollbackBean implements Comparable<RollbackBean>{
    String revision;
    String revisionDetail;
    String podTemplete;
    String revisionTime;
    String name;
    String current;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getRevisionDetail() {
        return revisionDetail;
    }

    public void setRevisionDetail(String revisionDetail) {
        this.revisionDetail = revisionDetail;
    }

    public String getPodTemplete() {
        return podTemplete;
    }

    public void setPodTemplete(String podTemplete) {
        this.podTemplete = podTemplete;
    }

    public String getRevisionTime() {
        return revisionTime;
    }

    public void setRevisionTime(String revisionTime) {
        this.revisionTime = revisionTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    @Override
    public int compareTo(RollbackBean o) {
        return Integer.parseInt(this.getRevision())-Integer.parseInt(o.getRevision());
    }
}
