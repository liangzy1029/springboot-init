package com.lzy.springbootinit.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.util.DigestUtils;

/**
 * 用户信息工具类
 */
public class UserInfoUtils {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "lzy";

    /**
     * 默认密码
     */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 用户昵称前缀
     */
    public static final String DEFAULT_USER_NAME_PREFIX = "user_";

    /**
     * 账号最小长度
     */
    public static final int MIN_ACCOUNT_LENGTH = 4;

    /**
     * 账号最大长度
     */
    public static final int MAX_ACCOUNT_LENGTH = 32;

    /**
     * 密码最小长度
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 密码最大长度
     */
    public static final int MAX_PASSWORD_LENGTH = 32;

    /**
     * 获取默认密码
     *
     * @return
     */
    public static String defaultPassword() {
        return encryptPassword(DEFAULT_PASSWORD);
    }

    /**
     * 密码加密
     *
     * @param userPassword 用户密码
     * @return
     */
    public static String encryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获取默认用户昵称
     *
     * @return
     */
    public static String defaultUserName() {
        return DEFAULT_USER_NAME_PREFIX + RandomUtil.randomNumbers(6);
    }

    /**
     * 校验用户账号是否合法
     *
     * @param userAccount
     * @return
     */
    public static boolean isInvalidUserAccount(String userAccount) {
        return !validUserAccountLength(userAccount);
    }

    /**
     * 校验用户账号长度是否合法
     *
     * @param userAccount
     * @return
     */
    public static boolean validUserAccountLength(String userAccount) {
        if (StrUtil.isBlank(userAccount)) {
            return false;
        }
        // 校验账号长度
        return userAccount.length() >= MIN_ACCOUNT_LENGTH && userAccount.length() <= MAX_ACCOUNT_LENGTH;
    }

    /**
     * 校验用户账号是否合法
     *
     * @param userPassword
     * @return
     */
    public static boolean isInvalidUserPassword(String userPassword) {
        return !validUserPasswordLength(userPassword);
    }

    /**
     * 校验用户密码长度是否合法
     *
     * @param userPassword
     * @return
     */
    public static boolean validUserPasswordLength(String userPassword) {
        if (StrUtil.isBlank(userPassword)) {
            return false;
        }
        // 校验密码长度
        return userPassword.length() >= MIN_PASSWORD_LENGTH && userPassword.length() <= MAX_PASSWORD_LENGTH;
    }


}
