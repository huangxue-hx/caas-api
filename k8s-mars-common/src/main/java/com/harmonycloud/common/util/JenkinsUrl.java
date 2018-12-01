package com.harmonycloud.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author w_kyzhang
 * @Description
 * @Date 2018-1-23
 * @Modified
 */
public class JenkinsUrl {

    private String name;
    private String[] folders;
    private String buildNumber;
    private String api;
    private Map<String, String> params = new HashMap<>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getFolders() {
        return folders;
    }

    public void setFolders(String... folders) {
        this.folders = folders;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getFolderUrl(){
        StringBuilder sb = new StringBuilder();
        if(folders != null) {
            for (String folder : folders) {
                sb.append("/job/").append(folder);
            }
        }
        return sb.toString();
    }

    public String getUrl(){
        StringBuilder sb = new StringBuilder();
        sb.append(getFolderUrl());
        if(StringUtils.isNotEmpty(name)) {
            sb.append("/job/").append(name);
        }
        if(StringUtils.isNotEmpty(buildNumber)){
            sb.append("/").append(buildNumber);
        }
        if(StringUtils.isNotEmpty(api)){
            sb.append("/").append(api);
        }
        if(params != null && CollectionUtils.isNotEmpty(params.keySet())) {
            sb.append("?");
            Iterator<String> iterator = params.keySet().iterator();
            while(iterator.hasNext()){
                String key = iterator.next();
                sb.append(key).append("=").append(params.get(key));
                if(iterator.hasNext()){
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }
}
