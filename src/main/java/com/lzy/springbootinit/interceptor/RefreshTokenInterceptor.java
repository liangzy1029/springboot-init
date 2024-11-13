package com.lzy.springbootinit.interceptor;

import cn.hutool.core.util.StrUtil;
import com.lzy.springbootinit.constant.RedisConstant;
import com.lzy.springbootinit.constant.UserConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * 刷新 token 拦截器
 */
@RequiredArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 在此处实现刷新Token的逻辑
        String token = request.getHeader(UserConstant.TOKEN_REQUEST_HEADER);
        if (StrUtil.isNotBlank(token)) {
            String key = RedisConstant.LOGIN_TOKEN_KEY + token;
            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (Boolean.TRUE.equals(hasKey)) {
                stringRedisTemplate.expire(key, RedisConstant.LOGIN_TOKEN_TTL, TimeUnit.SECONDS);
            }
        }

        return true;
    }
}
