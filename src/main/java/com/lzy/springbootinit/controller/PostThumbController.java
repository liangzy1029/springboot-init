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
import com.lzy.springbootinit.model.dto.postthumb.PostThumbAddRequest;
import com.lzy.springbootinit.model.dto.postthumb.PostThumbEditRequest;
import com.lzy.springbootinit.model.dto.postthumb.PostThumbQueryRequest;
import com.lzy.springbootinit.model.dto.postthumb.PostThumbUpdateRequest;
import com.lzy.springbootinit.model.entity.PostThumb;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.model.vo.PostThumbVO;
import com.lzy.springbootinit.service.PostThumbService;
import com.lzy.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子点赞接口
 */
@Slf4j
@RestController
@RequestMapping("/post_thumb")
@RequiredArgsConstructor
public class PostThumbController {

    private final PostThumbService postThumbService;

    private final UserService userService;

    // region 增删改查

    /**
     * 创建帖子点赞
     *
     * @param postThumbAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Long> addPostThumb(@RequestBody PostThumbAddRequest postThumbAddRequest) {
        // 在此处将实体类和 DTO 进行转换
        PostThumb postThumb = new PostThumb();
        BeanUtils.copyProperties(postThumbAddRequest, postThumb);
        // 数据校验
        postThumbService.validPostThumb(postThumb, true);
        // 填充默认值
        User loginUser = userService.getLoginUser();
        postThumb.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = postThumbService.save(postThumb);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newPostThumbId = postThumb.getId();
        return ResultUtils.success(newPostThumbId);
    }

    /**
     * 删除帖子点赞（仅本人或管理员可用）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> deletePostThumb(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 判断是否存在
        PostThumb oldPostThumb = postThumbService.getById(id);
        ThrowUtils.throwIf(oldPostThumb == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPostThumb.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postThumbService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子点赞（仅管理员可用）
     *
     * @param postThumbUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updatePostThumb(@RequestBody PostThumbUpdateRequest postThumbUpdateRequest) {
        Long id = postThumbUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        PostThumb postThumb = new PostThumb();
        BeanUtils.copyProperties(postThumbUpdateRequest, postThumb);
        // 数据校验
        postThumbService.validPostThumb(postThumb, false);
        // 判断是否存在
        PostThumb oldPostThumb = postThumbService.getById(id);
        ThrowUtils.throwIf(oldPostThumb == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = postThumbService.updateById(postThumb);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新帖子点赞（给用户使用）
     *
     * @param postThumbEditRequest
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> editPostThumb(@RequestBody PostThumbEditRequest postThumbEditRequest) {
        Long id = postThumbEditRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 在此处将实体类和 DTO 进行转换
        PostThumb postThumb = new PostThumb();
        BeanUtils.copyProperties(postThumbEditRequest, postThumb);
        // 数据校验
        postThumbService.validPostThumb(postThumb, false);
        // 判断是否存在
        PostThumb oldPostThumb = postThumbService.getById(id);
        ThrowUtils.throwIf(oldPostThumb == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPostThumb.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postThumbService.updateById(postThumb);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取帖子点赞
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<PostThumb> getPostThumbById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        PostThumb postThumb = postThumbService.getById(id);
        ThrowUtils.throwIf(postThumb == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(postThumb);
    }

    /**
     * 根据 id 获取帖子点赞（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<PostThumbVO> getPostThumbVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        PostThumb postThumb = postThumbService.getById(id);
        ThrowUtils.throwIf(postThumb == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(postThumbService.getPostThumbVO(postThumb));
    }

    /**
     * 分页获取帖子点赞列表（仅管理员可用）
     *
     * @param postThumbQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<PostThumb>> listPostThumbByPage(@RequestBody PostThumbQueryRequest postThumbQueryRequest) {
        long current = postThumbQueryRequest.getCurrent();
        long size = postThumbQueryRequest.getPageSize();
        // 查询数据库
        Page<PostThumb> postThumbPage = postThumbService.page(new Page<>(current, size), postThumbService.getQueryWrapper(postThumbQueryRequest));
        return ResultUtils.success(postThumbPage);
    }

    /**
     * 分页获取帖子点赞列表（封装类）
     *
     * @param postThumbQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<PostThumbVO>> listPostThumbVOByPage(@RequestBody PostThumbQueryRequest postThumbQueryRequest) {
        long current = postThumbQueryRequest.getCurrent();
        long size = postThumbQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<PostThumb> postThumbPage = postThumbService.page(new Page<>(current, size), postThumbService.getQueryWrapper(postThumbQueryRequest));
        // 获取封装类
        return ResultUtils.success(postThumbService.getPostThumbVOPage(postThumbPage));
    }

    /**
     * 分页获取帖子点赞列表（用户）
     *
     * @param postThumbQueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Page<PostThumbVO>> listMyPostThumbVOByPage(@RequestBody PostThumbQueryRequest postThumbQueryRequest) {
        long current = postThumbQueryRequest.getCurrent();
        long size = postThumbQueryRequest.getPageSize();
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser();
        postThumbQueryRequest.setUserId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<PostThumb> postThumbPage = postThumbService.page(new Page<>(current, size), postThumbService.getQueryWrapper(postThumbQueryRequest));
        // 获取封装类
        return ResultUtils.success(postThumbService.getPostThumbVOPage(postThumbPage));
    }

    // endregion
}
