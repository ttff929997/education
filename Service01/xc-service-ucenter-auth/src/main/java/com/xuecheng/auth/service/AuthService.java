package com.xuecheng.auth.service;

import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;

public interface AuthService {

    public AuthToken login(String username, String password, String clientId, String clientSecret);


    public ResponseResult logout();

    public AuthToken getUserToken(String token);

    public boolean delToken(String access_token);
}
