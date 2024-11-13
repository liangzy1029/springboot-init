package com.lzy.springbootinit.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    // region 权限

    /**
     * 普通用户
     */
    String DEFAULT_ROLE = "user";

    /**
     * 超级管理员
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    // endregion

    /**
     * token请求头
     */
    String TOKEN_REQUEST_HEADER = "token";


}
