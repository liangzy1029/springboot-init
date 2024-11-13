package com.lzy.springbootinit.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.springbootinit.annotation.AuthCheck;
import com.lzy.springbootinit.common.BaseResponse;
import com.lzy.springbootinit.common.DeleteRequest;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.common.ResultUtils;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.model.dto.user.*;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.model.vo.LoginUserVO;
import com.lzy.springbootinit.model.vo.UserVO;
import com.lzy.springbootinit.service.UserService;
import com.lzy.springbootinit.utils.UserInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    @AuthCheck(mustLogin = false)
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        return ResultUtils.success(userService.userRegister(userAccount, userPassword, checkPassword));
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    @AuthCheck(mustLogin = false)
    public BaseResponse<String> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtils.success(userService.userLogin(userAccount, userPassword));
    }

    /**
     * 用户注销
     *
     * @return
     */
    @PostMapping("/logout")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> userLogout() {
        userService.userLogout();
        return ResultUtils.success(true);
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @GetMapping("/get/login")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<LoginUserVO> getLoginUser() {
        User user = userService.getLoginUser();
        return ResultUtils.success(userService.getLoginUserVo(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户（仅管理员可用）
     *
     * @param userAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        // 在此处将实体类和 DTO 进行转换
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 数据格式化（去除前后空格等）
        userService.formatUser(user, true);
        // 数据校验
        userService.validUser(user, true);
        // 填充默认值
        user.setUserPassword(UserInfoUtils.defaultPassword());
        if (StrUtil.isBlank(user.getUserName())) {
            // 用户昵称默认为 user_ + 6位随机数字
            user.setUserName(UserInfoUtils.defaultUserName());
        }
        String userAccount = user.getUserAccount();
        synchronized (userAccount.intern()) {
            long count = userService.count((Wrappers.lambdaQuery(User.class).eq(User::getUserAccount, userAccount)));
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 写入数据库
            boolean result = userService.save(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户（仅管理员可用）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 判断是否存在
        User oldUser = userService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户（仅管理员可用）
     *
     * @param userUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        Long id = userUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // 数据格式化（去除前后空格等）
        userService.formatUser(user, false);
        // 数据校验
        userService.validUser(user, false);
        // 判断是否存在
        User oldUser = userService.getById(id);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.NOT_FOUND_ERROR);
        String newUserAccount = user.getUserAccount();
        boolean result = false;
        // 操作数据库
        if (StrUtil.isNotBlank(newUserAccount)) {
            result = userService.updateById(user);
        } else {
            synchronized (newUserAccount.intern()) {
                // 查询是否有重复账号
                long count = userService.count((Wrappers.lambdaQuery(User.class).eq(User::getUserAccount, newUserAccount)));
                ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
                result = userService.updateById(user);
            }
        }
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取包装类（仅管理员可用）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<UserVO> getUserVoById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户封装列表（仅管理员可用）
     *
     * @param userQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<UserVO>> listUserVoByPage(@RequestBody UserQueryRequest userQueryRequest) {
        int current = userQueryRequest.getCurrent();
        int size = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, size), userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = userService.getUserVOPage(userPage);
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 密码更新（仅管理员可用）
     *
     * @param passwordUpdateRequest
     * @return
     */
    @PostMapping("/update/password")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updatePassword(@RequestBody UserPasswordUpdateRequest passwordUpdateRequest) {
        // 获取值
        Long userId = passwordUpdateRequest.getUserId();
        String newPassword = StrUtil.trimToNull(passwordUpdateRequest.getNewPassword());
        String modifierPassword = StrUtil.trimToNull(passwordUpdateRequest.getModifierPassword());
        // 校验参数
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(newPassword, modifierPassword), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(newPassword), ErrorCode.PARAMS_ERROR, "密码不合法");
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(modifierPassword), ErrorCode.PARAMS_ERROR, "管理员登录密码错误");

        // 判断管理员密码是否正确
        User user = userService.getLoginUser();
        String encryptModifierPassword = UserInfoUtils.encryptPassword(modifierPassword);
        User adminUser = userService.getOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getId, user.getId())
                        .eq(User::getUserRole, user.getUserRole())
                        .eq(User::getUserPassword, encryptModifierPassword)
        );
        ThrowUtils.throwIf(adminUser == null, ErrorCode.PARAMS_ERROR, "管理员登录密码错误");
        // 更新密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setUserPassword(UserInfoUtils.encryptPassword(newPassword));
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @return
     */
    @PostMapping("/my/update")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserMyUpdateRequest userUpdateMyRequest) {
        User loginUser = userService.getLoginUser();
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        String userName = user.getUserName();
        if (StrUtil.isBlank(userName)) {
            user.setUserName(null);
        }
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 个人密码更新
     *
     * @param passwordUpdateMyRequest
     * @return
     */
    @PostMapping("/my/update/password")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> updateMyPassword(@RequestBody UserMyPasswordUpdateRequest passwordUpdateMyRequest) {
        // 获取值
        String oldPassword = StrUtil.trimToNull(passwordUpdateMyRequest.getOldPassword());
        String newPassword = StrUtil.trimToNull(passwordUpdateMyRequest.getNewPassword());
        // 校验参数
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(oldPassword, newPassword), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(oldPassword), ErrorCode.PARAMS_ERROR, "旧密码错误");
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(newPassword), ErrorCode.PARAMS_ERROR, "新密码不合法");

        // 判断旧密码是否正确
        User user = userService.getLoginUser();
        String encryptModifierPassword = UserInfoUtils.encryptPassword(oldPassword);
        User adminUser = userService.getOne(
                Wrappers.lambdaQuery(User.class)
                        .eq(User::getId, user.getId())
                        .eq(User::getUserPassword, encryptModifierPassword)
        );
        ThrowUtils.throwIf(adminUser == null, ErrorCode.PARAMS_ERROR, "旧密码错误");
        // 更新密码
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setUserPassword(UserInfoUtils.encryptPassword(newPassword));
        boolean result = userService.updateById(updateUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


}
