package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.User;
import com.harmonycloud.dto.user.CrowdConfigDto;
import javax.servlet.http.HttpServletResponse;

public interface AuthManagerCrowd {

    boolean testCrowd(CrowdConfigDto crowdConfigDto) throws Exception;

    String auth(String username, String password) throws Exception;

    User getUser(String username, String password) throws Exception;

    String getToken(String username, String password) throws Exception;

    void invalidateToken(String username) throws Exception;

    void addCookie(String crowdToken, HttpServletResponse response);

    void clearCookie(HttpServletResponse response);

    String testLogin(String crowdToken) throws Exception;

    String getCookieName();

    String getToken(String username) throws Exception;
}
