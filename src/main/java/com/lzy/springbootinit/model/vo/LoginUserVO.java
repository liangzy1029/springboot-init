package com.lzy.springbootinit.model.vo;

import com.lzy.springbootinit.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 已登录用户视图（脱敏）
 */
@Data
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

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

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 封装类转对象
     *
     * @param loginUserVO
     * @return
     */
    public static User voToObj(LoginUserVO loginUserVO) {
        if (loginUserVO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(loginUserVO, user);
        return user;
    }

    /**
     * 对象转封装类
     *
     * @param user
     * @return
     */
    public static LoginUserVO objToVo(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

}
