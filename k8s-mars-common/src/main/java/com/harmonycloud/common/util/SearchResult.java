package com.harmonycloud.common.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by czm on 2017/3/29.
 * 根据条件查询查询结果
 *
 */
public class SearchResult implements Comparable<SearchResult>{
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResult.class);
    private String user;      //用户
    private String tenant;  
    private String module;    //模块
    private String opFun;     //接口调用模块方法
    private String method;    //http请求方法
    private String opTime;    //http请求开始时间
    private String requestParams;   //http请求参数
    private String opStatus;        //http请求失败或成功
    private String response;        //http请求结果
    private String remoteIp;        //访问IP
    private String path;            //http请求路径
    private String subject;         //主体对象

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

    public String getOpTime() {
        return opTime;
    }

    public void setOpTime(String opTime) {
        this.opTime = opTime;
    }


    public String getOpStatus() {
        return opStatus;
    }

    public void setOpStatus(String opStatus) {
        this.opStatus = opStatus;
    }
    
    public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestParams() {
		return requestParams;
	}

	public void setRequestParams(String requestParams) {
		this.requestParams = requestParams;
	}
	
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

	@Override
    public int compareTo(SearchResult o) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int res = 0 ;
        try {
            res =  df.parse(o.getOpTime()).compareTo(df.parse(this.getOpTime()));
        } catch (ParseException e) {
            LOGGER.warn("ParseException", e);
        }
        return res;
    }

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

}