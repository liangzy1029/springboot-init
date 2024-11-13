package com.lzy.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 密码更新请求
 */
@Data
public class UserPasswordUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 被修改用户 id
     */
    private Long userId;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 修改者的密码
     */
    private String modifierPassword;

}
