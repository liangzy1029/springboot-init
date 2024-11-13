package com.lzy.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.springbootinit.annotation.AuthCheck;
import com.lzy.springbootinit.common.BaseResponse;
import com.lzy.springbootinit.common.DeleteRequest;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.common.ResultUtils;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourAddRequest;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourEditRequest;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourQueryRequest;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourUpdateRequest;
import com.lzy.springbootinit.model.entity.PostFavour;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.model.vo.PostFavourVO;
import com.lzy.springbootinit.service.PostFavourService;
import com.lzy.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子收藏接口
 */
@Slf4j
@RestController
@RequestMapping("/post_favour")
@RequiredArgsConstructor
public class PostFavourController {

    private final PostFavourService postFavourService;

    private final UserService userService;

    // region 增删改查

    /**
     * 创建帖子收藏
     *
     * @param postFavourAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Long> addPostFavour(@RequestBody PostFavourAddRequest postFavourAddRequest) {
        // 在此处将实体类和 DTO 进行转换
        PostFavour postFavour = new PostFavour();
        BeanUtils.copyProperties(postFavourAddRequest, postFavour);
        // 数据校验
        postFavourService.validPostFavour(postFavour, true);
        // 填充默认值
        User loginUser = userService.getLoginUser();
        postFavour.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = postFavourService.save(postFavour);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newPostFavourId = postFavour.getId();
        return ResultUtils.success(newPostFavourId);
    }

    /**
     * 删除帖子收藏（仅本人或管理员可用）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> deletePostFavour(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 判断是否存在
        PostFavour oldPostFavour = postFavourService.getById(id);
        ThrowUtils.throwIf(oldPostFavour == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPostFavour.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postFavourService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子收藏（仅管理员可用）
     *
     * @param postFavourUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updatePostFavour(@RequestBody PostFavourUpdateRequest postFavourUpdateRequest) {
        Long id = postFavourUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        PostFavour postFavour = new PostFavour();
        BeanUtils.copyProperties(postFavourUpdateRequest, postFavour);
        // 数据校验
        postFavourService.validPostFavour(postFavour, false);
        // 判断是否存在
        PostFavour oldPostFavour = postFavourService.getById(id);
        ThrowUtils.throwIf(oldPostFavour == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = postFavourService.updateById(postFavour);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子收藏（给用户使用）
     *
     * @param postFavourEditRequest
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> editPostFavour(@RequestBody PostFavourEditRequest postFavourEditRequest) {
        Long id = postFavourEditRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 在此处将实体类和 DTO 进行转换
        PostFavour postFavour = new PostFavour();
        BeanUtils.copyProperties(postFavourEditRequest, postFavour);
        // 数据校验
        postFavourService.validPostFavour(postFavour, false);
        // 判断是否存在
        PostFavour oldPostFavour = postFavourService.getById(id);
        ThrowUtils.throwIf(oldPostFavour == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPostFavour.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postFavourService.updateById(postFavour);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取帖子收藏
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<PostFavour> getPostFavourById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        PostFavour postFavour = postFavourService.getById(id);
        ThrowUtils.throwIf(postFavour == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(postFavour);
    }

    /**
     * 根据 id 获取帖子收藏（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<PostFavourVO> getPostFavourVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        PostFavour postFavour = postFavourService.getById(id);
        ThrowUtils.throwIf(postFavour == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(postFavourService.getPostFavourVO(postFavour));
    }

    /**
     * 分页获取帖子收藏列表（仅管理员可用）
     *
     * @param postFavourQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<PostFavour>> listPostFavourByPage(@RequestBody PostFavourQueryRequest postFavourQueryRequest) {
        long current = postFavourQueryRequest.getCurrent();
        long size = postFavourQueryRequest.getPageSize();
        // 查询数据库
        Page<PostFavour> postFavourPage = postFavourService.page(new Page<>(current, size), postFavourService.getQueryWrapper(postFavourQueryRequest));
        return ResultUtils.success(postFavourPage);
    }

    /**
     * 分页获取帖子收藏列表（封装类）
     *
     * @param postFavourQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<PostFavourVO>> listPostFavourVOByPage(@RequestBody PostFavourQueryRequest postFavourQueryRequest) {
        long current = postFavourQueryRequest.getCurrent();
        long size = postFavourQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<PostFavour> postFavourPage = postFavourService.page(new Page<>(current, size), postFavourService.getQueryWrapper(postFavourQueryRequest));
        // 获取封装类
        return ResultUtils.success(postFavourService.getPostFavourVOPage(postFavourPage));
    }

    /**
     * 分页获取帖子收藏列表（用户）
     *
     * @param postFavourQueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Page<PostFavourVO>> listMyPostFavourVOByPage(@RequestBody PostFavourQueryRequest postFavourQueryRequest) {
        long current = postFavourQueryRequest.getCurrent();
        long size = postFavourQueryRequest.getPageSize();
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser();
        postFavourQueryRequest.setUserId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<PostFavour> postFavourPage = postFavourService.page(new Page<>(current, size), postFavourService.getQueryWrapper(postFavourQueryRequest));
        // 获取封装类
        return ResultUtils.success(postFavourService.getPostFavourVOPage(postFavourPage));
    }

    // endregion
}
