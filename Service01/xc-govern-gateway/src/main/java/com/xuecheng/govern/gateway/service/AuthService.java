package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    //从header中查询jwt令牌
    public String getJwtFromHeader(HttpServletRequest request){
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        String substring = authorization.substring(7);
        return substring;
    }

    //从cookie取出token
    //查询身份令牌
    public String getTokenFromCookie(HttpServletRequest request){
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        String access_token = map.get("uid");
        if(StringUtils.isEmpty(access_token)){
            return null;
        }
        return access_token;
    }

    //查询令牌的有效期
    public long getExpire(String access_token){
        String name = "user_token:"+access_token;
        Long expire = stringRedisTemplate.getExpire(name, TimeUnit.SECONDS);
        return expire;
    }
}