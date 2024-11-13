package com.lzy.springbootinit.constant;

/**
 * redis 常量
 */
public interface RedisConstant {

    /**
     * 用户登录态 key
     */
    String LOGIN_TOKEN_KEY = "login:token:";

    /**
     * 登录 token 过期时间（秒）
     */
    Integer LOGIN_TOKEN_TTL = 60 * 60;
}
