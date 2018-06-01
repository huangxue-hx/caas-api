package com.harmonycloud.service.system;

import com.harmonycloud.dto.config.ControllerUrlMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 平台接口api服务
 */
public interface ApiService {

    /**
     * 获取平台所有Controller接口定义信息
     * @param order
     * @param request
     * @return
     */
    Map<String, ControllerUrlMapping> generateUrlMapping(String order, HttpServletRequest request);
}
