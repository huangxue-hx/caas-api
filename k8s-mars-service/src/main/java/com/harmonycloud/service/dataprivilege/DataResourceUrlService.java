package com.harmonycloud.service.dataprivilege;

import com.harmonycloud.dao.dataprivilege.bean.DataResourceUrl;

import java.util.Map;

/**
 * Created by 15988 on 2018/7/2.
 */
public interface DataResourceUrlService {
    /**
     * 获取拦截权限Url字典
     * @return
     * @throws Exception
     */
    public Map<String,DataResourceUrl> getDataResourceUrlMap() throws Exception;
}
