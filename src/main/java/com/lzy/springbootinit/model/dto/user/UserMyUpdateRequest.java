package com.lzy.springbootinit.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新个人信息请求
 */
@Data
public class UserMyUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

}
