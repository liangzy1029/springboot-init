package com.lzy.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lzy.springbootinit.annotation.AuthCheck;
import com.lzy.springbootinit.common.BaseResponse;
import com.lzy.springbootinit.common.DeleteRequest;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.common.ResultUtils;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.BusinessException;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.model.dto.post.PostAddRequest;
import com.lzy.springbootinit.model.dto.post.PostEditRequest;
import com.lzy.springbootinit.model.dto.post.PostQueryRequest;
import com.lzy.springbootinit.model.dto.post.PostUpdateRequest;
import com.lzy.springbootinit.model.entity.Post;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.enums.UserRoleEnum;
import com.lzy.springbootinit.model.vo.PostVO;
import com.lzy.springbootinit.service.PostService;
import com.lzy.springbootinit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帖子接口
 */
@Slf4j
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    private final UserService userService;

    // region 增删改查

    /**
     * 创建帖子
     *
     * @param postAddRequest
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest) {
        // 在此处将实体类和 DTO 进行转换
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        List<String> tagList = postAddRequest.getTagList();
        post.setTags(JSONUtil.toJsonStr(tagList));
        // 数据校验
        postService.validPost(post, true);
        // 填充默认值
        User loginUser = userService.getLoginUser();
        post.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除帖子（仅本人或管理员可用）
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest) {
        Long id = deleteRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新贴子（仅管理员可用）
     *
     * @param postUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
        Long id = postUpdateRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 在此处将实体类和 DTO 进行转换
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tagList = postUpdateRequest.getTagList();
        post.setTags(JSONUtil.toJsonStr(tagList));
        // 数据校验
        postService.validPost(post, false);
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = postService.updateById(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新贴子（给用户使用）
     *
     * @param postEditRequest
     * @return
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest postEditRequest) {
        Long id = postEditRequest.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser();
        // 在此处将实体类和 DTO 进行转换
        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tagList = postEditRequest.getTagList();
        post.setTags(JSONUtil.toJsonStr(tagList));
        // 数据校验
        postService.validPost(post, false);
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin()) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = postService.updateById(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取帖子
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Post> getPostById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Post post = postService.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(post);
    }

    /**
     * 根据 id 获取帖子（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<PostVO> getPostVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Post post = postService.getById(id);
        ThrowUtils.throwIf(post == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(postService.getPostVO(post));
    }

    /**
     * 分页获取帖子列表（仅管理员可用）
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 查询数据库
        Page<Post> postPage = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postPage);
    }

    /**
     * 分页获取帖子列表（封装类）
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.ADMIN)
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Post> postPage = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        // 获取封装类
        return ResultUtils.success(postService.getPostVOPage(postPage));
    }

    /**
     * 分页获取帖子列表（用户）
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/my/list/page/vo")
    @AuthCheck(mustRole = UserRoleEnum.USER)
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser();
        postQueryRequest.setUserId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > CommonConstant.USER_PAGE_SIZE_LIMIT, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Post> postPage = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        // 获取封装类
        return ResultUtils.success(postService.getPostVOPage(postPage));
    }

    // endregion
}
