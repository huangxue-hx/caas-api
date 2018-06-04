package com.harmonycloud.dto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @Author jiangmi
 * @Description 操作审计类
 * @Date created in 2018-1-10
 * @Modified
 */
public class AuditRequestInfo implements Comparable<AuditRequestInfo>{
    private static Logger LOGGER = LoggerFactory.getLogger(AuditRequestInfo.class);

    private String url;

    private String method;

    private String subject;

    private String moduleChDesc;

    private String moduleEnDesc;

    private String actionChDesc;

    private String actionEnDesc;

    private String requestParams;   //http请求参数

    private String response;        //http请求结果

    private String remoteIp;        //访问IP

    private String status;        //http请求失败或成功

    private String actionTime;

    private String tenant;

    private String project;

    private String user;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActionTime() {
        return actionTime;
    }

    public void setActionTime(String actionTime) {
        this.actionTime = actionTime;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getModuleChDesc() {
        return moduleChDesc;
    }

    public void setModuleChDesc(String moduleChDesc) {
        this.moduleChDesc = moduleChDesc;
    }

    public String getModuleEnDesc() {
        return moduleEnDesc;
    }

    public void setModuleEnDesc(String moduleEnDesc) {
        this.moduleEnDesc = moduleEnDesc;
    }

    public String getActionChDesc() {
        return actionChDesc;
    }

    public void setActionChDesc(String actionChDesc) {
        this.actionChDesc = actionChDesc;
    }

    public String getActionEnDesc() {
        return actionEnDesc;
    }

    public void setActionEnDesc(String actionEnDesc) {
        this.actionEnDesc = actionEnDesc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public int compareTo(AuditRequestInfo o) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int res = 0 ;
        try {
            res =  df.parse(o.getActionTime()).compareTo(df.parse(this.getActionTime()));
        } catch (ParseException e) {
            LOGGER.error("转换失败，时间格式错误", e);
        }
        return res;
    }
}
