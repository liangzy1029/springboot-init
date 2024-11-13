package com.lzy.springbootinit.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.constant.RedisConstant;
import com.lzy.springbootinit.constant.UserConstant;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.mapper.UserMapper;
import com.lzy.springbootinit.model.dto.user.UserQueryRequest;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.model.vo.LoginUserVO;
import com.lzy.springbootinit.model.vo.UserVO;
import com.lzy.springbootinit.service.UserService;
import com.lzy.springbootinit.utils.SqlUtils;
import com.lzy.springbootinit.utils.UserInfoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate stringRedisTemplate;

    private final HttpServletRequest request;


    /**
     * 用户注册
     *
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public Long userRegister(String userAccount, String userPassword, String checkPassword) {
        userAccount = StrUtil.trimToNull(userAccount);
        userPassword = StrUtil.trimToNull(userPassword);
        checkPassword = StrUtil.trimToNull(checkPassword);
        // 校验
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR);
        // 校验账号
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserAccount(userAccount), ErrorCode.PARAMS_ERROR, "用户账号不合法");
        // 密码和校验密码相同
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码输入不一致");
        // 校验密码
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不合法");

        synchronized (userAccount.intern()) {
            // 账户不能重复
            long count = this.baseMapper.selectCount(Wrappers.lambdaQuery(User.class).eq(User::getUserAccount, userAccount));
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            // 加密
            user.setUserPassword(UserInfoUtils.encryptPassword(userPassword));
            // 用户昵称默认为 user_ + 6位随机数字
            user.setUserName(UserInfoUtils.defaultUserName());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }

    }

    @Override
    public String userLogin(String userAccount, String userPassword) {
        userAccount = StrUtil.trimToNull(userAccount);
        userPassword = StrUtil.trimToNull(userPassword);
        // 1.校验
        ThrowUtils.throwIf(!StrUtil.isAllNotBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR);
        // 校验账号
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserAccount(userAccount), ErrorCode.PARAMS_ERROR, "用户账号不合法");
        // 校验密码
        ThrowUtils.throwIf(UserInfoUtils.isInvalidUserPassword(userPassword), ErrorCode.PARAMS_ERROR, "用户密码不合法");

        // 2.加密
        String encryptPassword = UserInfoUtils.encryptPassword(userPassword);
        // 查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        if (UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已被封禁");
        }
        // 3.记录用户登录状态
        LoginUserVO loginUserVo = getLoginUserVo(user);
        // 生成随机token
        String token = UUID.randomUUID().toString(true);
        Map<String, Object> loginUserVoMap = BeanUtil.beanToMap(loginUserVo, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((key, value) -> Optional.ofNullable(value).map(Object::toString).orElse(null)));
        // 保存到 redis
        String key = RedisConstant.LOGIN_TOKEN_KEY + token;
        stringRedisTemplate.opsForHash().putAll(key, loginUserVoMap);
        stringRedisTemplate.expire(key, RedisConstant.LOGIN_TOKEN_TTL, TimeUnit.SECONDS);
        // 4.返回token
        return token;
    }

    /**
     * 用户注销
     *
     * @return
     */
    @Override
    public void userLogout() {
        String token = request.getHeader(UserConstant.TOKEN_REQUEST_HEADER);
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.NOT_LOGIN_ERROR);
        stringRedisTemplate.delete(RedisConstant.LOGIN_TOKEN_KEY + token);
    }

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user
     * @return
     */
    @Override
    public LoginUserVO getLoginUserVo(User user) {
        return LoginUserVO.objToVo(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return
     */
    @Override
    public User getLoginUser() {
        String token = request.getHeader(UserConstant.TOKEN_REQUEST_HEADER);
        ThrowUtils.throwIf(StrUtil.isBlank(token), ErrorCode.NOT_LOGIN_ERROR);
        Map<Object, Object> loginUserVOMap = stringRedisTemplate.opsForHash().entries(RedisConstant.LOGIN_TOKEN_KEY + token);
        if (loginUserVOMap.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "登录已过期");
        }
        LoginUserVO loginUserVO = BeanUtil.fillBeanWithMap(loginUserVOMap, new LoginUserVO(), false);

        return LoginUserVO.voToObj(loginUserVO);
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @return
     */
    @Override
    public User getLoginUserPermitNull() {
        String token = request.getHeader(UserConstant.TOKEN_REQUEST_HEADER);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        Map<Object, Object> loginUserVOMap = stringRedisTemplate.opsForHash().entries(RedisConstant.LOGIN_TOKEN_KEY + token);
        if (loginUserVOMap.isEmpty()) {
            return null;
        }
        LoginUserVO loginUserVO = BeanUtil.fillBeanWithMap(loginUserVOMap, new LoginUserVO(), false);

        return LoginUserVO.voToObj(loginUserVO);
    }

    /**
     * 是否为管理员
     *
     * @return
     */
    @Override
    public boolean isAdmin() {
        User user = getLoginUser();
        return isAdmin(user.getUserRole());
    }

    /**
     * 是否为管理员
     *
     * @param role
     * @return
     */
    @Override
    public boolean isAdmin(String role) {
        return UserRoleEnum.ADMIN.getValue().equals(role);
    }

    @Override
    public void formatUser(User user, boolean add) {
        String userAccount = user.getUserAccount();
        String userName = user.getUserName();
        String userRole = user.getUserRole();

        // 去掉前后空白字符，且如果是空字符串，则设置为 null
        user.setUserAccount(StrUtil.trimToNull(userAccount));
        user.setUserName(StrUtil.trimToNull(userName));
        user.setUserRole(StrUtil.trimToNull(userRole));
    }

    /**
     * 校验数据
     *
     * @param user
     * @param add  对创建的数据校验
     */
    @Override
    public void validUser(User user, boolean add) {
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        // 获取校验值
        String userAccount = user.getUserAccount();
        String userName = user.getUserName();
        String userRole = user.getUserRole();
        String userProfile = user.getUserProfile();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(userAccount), ErrorCode.PARAMS_ERROR, "用户账号不能为空");
            ThrowUtils.throwIf(userAccount.length() > 50, ErrorCode.PARAMS_ERROR, "用户账号要小于 50");
            UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色非法");
        }

        // 修改数据时，有参数时校验
        if (StrUtil.isNotBlank(userName)) {
            ThrowUtils.throwIf(userName.length() > 50, ErrorCode.PARAMS_ERROR, "用户昵称要小于 50");
        }

        if (StrUtil.isNotBlank(userRole)) {
            UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(userRole);
            ThrowUtils.throwIf(userRoleEnum == null, ErrorCode.PARAMS_ERROR, "用户角色非法");
        }

        if (StrUtil.isNotBlank(userProfile)) {
            ThrowUtils.throwIf(userProfile.length() > 300, ErrorCode.PARAMS_ERROR, "用户简介要小于 300");
        }


    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (userQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String searchText = userQueryRequest.getSearchText();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();

        // 添加条件
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(
                    qw -> qw.like("userName", searchText)
                            .or().like("userProfile", searchText)
            );
        }

        // 模糊查询
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);

        // 精确查询
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);

        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                CommonConstant.SORT_ORDER_ASC.equals(sortOrder),
                sortField);

        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param user
     * @return
     */
    @Override
    public UserVO getUserVO(User user) {
        // 对象转封装类
        return UserVO.objToVo(user);
    }

    /**
     * 分页获取用户封装
     *
     * @param userPage
     * @return
     */
    @Override
    public Page<UserVO> getUserVOPage(Page<User> userPage) {
        List<User> userList = userPage.getRecords();
        Page<UserVO> userVOPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        if (CollUtil.isEmpty(userList)) {
            return userVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserVO> userVOList = userList.stream().map(UserVO::objToVo).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

}




