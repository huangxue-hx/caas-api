package com.harmonycloud.service.user;

import com.harmonycloud.dao.user.bean.User;

import javax.servlet.http.HttpServletResponse;

public interface AuthManagerCrowd {

    String auth(String username, String password) throws Exception;

    boolean addUser(String username, String password, String realname, String email, String phone) throws Exception;

    void deleteUser(String username) throws Exception;

    User getUser(String username, String password) throws Exception;

    String getToken(String username, String password) throws Exception;

    void invalidateToken(String username) throws Exception;

    void AddCookie(String crowdToken, HttpServletResponse response) throws Exception;

    String testLogin(String crowdToken) throws Exception;

    public String getCookieName();
}
