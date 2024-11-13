package com.lzy.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.springbootinit.model.dto.user.UserQueryRequest;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.vo.LoginUserVO;
import com.lzy.springbootinit.model.vo.UserVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    Long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount
     * @param userPassword
     * @return
     */
    String userLogin(String userAccount, String userPassword);

    /**
     * 用户注销
     *
     * @return
     */
    void userLogout();

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVo(User user);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser();

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @return
     */
    User getLoginUserPermitNull();

    /**
     * 是否为管理员
     *
     * @return
     */
    boolean isAdmin();

    /**
     * 是否为管理员
     *
     * @param role
     * @return
     */
    boolean isAdmin(String role);


    /**
     * 格式化数据
     *
     * @param user
     * @param add  对创建的数据格式化
     */
    void formatUser(User user, boolean add);

    /**
     * 校验数据
     *
     * @param user
     * @param add  对创建的数据校验
     */
    void validUser(User user, boolean add);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取应用封装
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 分页获取应用封装
     *
     * @param userPage
     * @return
     */
    Page<UserVO> getUserVOPage(Page<User> userPage);

}
