package com.harmonycloud.service.user.auth;

import org.springframework.stereotype.Service;

import com.harmonycloud.service.user.AuthDispatch;
import com.harmonycloud.service.user.AuthManager;


@Service
public class AuthDispatchImpl implements AuthDispatch {
    
    
    private AuthManager authCenter;
    
    @Override
    public String login(String userName, String password) throws Exception {
        return authCenter.auth(userName,password);
    }

    public void setAuthCenter(AuthManager authCenter) {
        this.authCenter = authCenter;
    }
    
    
}
