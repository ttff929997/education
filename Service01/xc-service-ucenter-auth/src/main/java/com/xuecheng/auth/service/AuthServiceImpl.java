package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public AuthToken login(String username,String password,String clientId,String clientSecret) {
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            ExceptionCast.throwException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        String access_token = authToken.getAccess_token();
        String s = JSON.toJSONString(authToken);
        boolean saveToken = this.saveToken(access_token, s, tokenValiditySeconds);
        if(!saveToken){
            ExceptionCast.throwException(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;
    }

    private boolean saveToken(String access_token,String content,long ttl){
        String name = "user_token:"+access_token;
        stringRedisTemplate.boundValueOps(name).set(content,ttl, TimeUnit.SECONDS);
        Long expire = stringRedisTemplate.getExpire(name);
        return expire>0;
    }


    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        ServiceInstance choose = loadBalancerClient.choose("XC-SERVICE-UCENTER-AUTH");
        URI uri = choose.getUri();
        String url = uri + "/auth/oauth/token";
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        String httpBasic = this.getHttpBasic(clientId, clientSecret);
        headers.add("Authorization",httpBasic);

        MultiValueMap<String,String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

        HttpEntity<MultiValueMap<String,String>> httpEntity = new HttpEntity<MultiValueMap<String, String>>(body, headers);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401) {
                    super.handleError(response);
                }
            }
        });
        Map map = null;
        try {//http请求spring security的申请令牌接口
            ResponseEntity<Map> mapResponseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<MultiValueMap<String, String>>(body, headers), Map.class);
            map = mapResponseEntity.getBody();
        } catch (RestClientException e) {
            e.printStackTrace();
            LOGGER.error("request oauth_token_password error: {}", e.getMessage());
            e.printStackTrace();
            ExceptionCast.throwException(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        if(map == null ||
                map.get("access_token") == null ||
                map.get("refresh_token") == null ||
                map.get("jti") == null){

            //解析spring security返回的错误信息
            if(map!=null && map.get("error_description")!=null){
                String error_description = (String) map.get("error_description");
                if(error_description.indexOf("UserDetailsService returned null")>=0){
                    ExceptionCast.throwException(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }else if(error_description.indexOf("坏的凭证")>=0){
                    ExceptionCast.throwException(AuthCode.AUTH_CREDENTIAL_ERROR);
                }
            }
            
            return null;
        }
        AuthToken authToken = new AuthToken();
        String jwt_token = (String) map.get("access_token");
        String refresh_token = (String) map.get("refresh_token");
        String access_token = (String) map.get("jti");
        authToken.setJwt_token(jwt_token);
        authToken.setAccess_token(access_token);
        authToken.setRefresh_token(refresh_token);
        return authToken;
    }

    //获取httpbasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        String str = clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(str.getBytes());
        return "Basic "+new String(encode);
    }

    @Override
    public ResponseResult logout() {
        return null;
    }

    //删除token
    public boolean delToken(String access_token){
        String name = "user_token:"+access_token;
        stringRedisTemplate.delete(name);
        return true;
    }


    @Override
    //从redis查询令牌
    public AuthToken getUserToken(String token){
        String key = "user_token:" + token;
        //从redis中取到令牌信息
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value,AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
