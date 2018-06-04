package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.UrlDic;

import java.util.Map;

public interface UrlDicService {
    /**
     * 获取url对应的map(仅供测试使用)
     * @return
     * @throws Exception
     */
    public Map getUrlMap() throws Exception;
    /**
     * 获取拦截权限Url字典
     * @return
     * @throws Exception
     */
    public Map<String,UrlDic> getUrlDicMap() throws Exception;
}
