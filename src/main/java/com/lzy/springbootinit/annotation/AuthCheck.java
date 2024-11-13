package com.lzy.springbootinit.annotation;

import com.lzy.springbootinit.model.enums.UserRoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {

    /**
     * 必须有某个角色
     *
     * @return
     */
    UserRoleEnum mustRole() default UserRoleEnum.USER;

    /**
     * 是否必须登录
     *
     * @return
     */
    boolean mustLogin() default true;

}
