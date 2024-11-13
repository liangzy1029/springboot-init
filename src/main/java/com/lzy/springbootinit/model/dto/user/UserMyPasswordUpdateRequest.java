package com.lzy.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户个人密码更新请求
 */
@Data
public class UserMyPasswordUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;


}
