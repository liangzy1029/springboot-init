package com.lzy.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.mapper.PostFavourMapper;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourQueryRequest;
import com.lzy.springbootinit.model.entity.PostFavour;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.vo.PostFavourVO;
import com.lzy.springbootinit.model.vo.UserVO;
import com.lzy.springbootinit.service.PostFavourService;
import com.lzy.springbootinit.service.UserService;
import com.lzy.springbootinit.utils.SqlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子收藏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour> implements PostFavourService {

    private final UserService userService;

    /**
     * 校验数据
     *
     * @param postFavour
     * @param add        对创建的数据进行校验
     */
    @Override
    public void validPostFavour(PostFavour postFavour, boolean add) {
        ThrowUtils.throwIf(postFavour == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long postId = postFavour.getPostId();
        Long userId = postFavour.getUserId();

        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(postId == null, ErrorCode.PARAMS_ERROR, "帖子 id不能为空");
            ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR, "创建用户 id不能为空");
        }
        // 修改数据时，有参数则校验
        // 补充校验规则

    }

    /**
     * 获取查询条件
     *
     * @param postFavourQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<PostFavour> getQueryWrapper(PostFavourQueryRequest postFavourQueryRequest) {
        QueryWrapper<PostFavour> queryWrapper = new QueryWrapper<>();
        if (postFavourQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = postFavourQueryRequest.getId();
        Long postId = postFavourQueryRequest.getPostId();
        Long userId = postFavourQueryRequest.getUserId();
        String sortField = postFavourQueryRequest.getSortField();
        String sortOrder = postFavourQueryRequest.getSortOrder();

        // 补充需要的查询条件

        // 模糊查询

        // 精确查询
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(postId), "postId", postId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);

        // 排序规则
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField
        );

        return queryWrapper;
    }

    /**
     * 获取帖子收藏封装
     *
     * @param postFavour
     * @return
     */
    @Override
    public PostFavourVO getPostFavourVO(PostFavour postFavour) {
        // 对象转封装类
        PostFavourVO postFavourVO = PostFavourVO.objToVo(postFavour);
        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = postFavour.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postFavourVO.setUser(userVO);
        // endregion

        return postFavourVO;
    }

    /**
     * 分页获取帖子收藏封装
     *
     * @param postFavourPage
     * @return
     */
    @Override
    public Page<PostFavourVO> getPostFavourVOPage(Page<PostFavour> postFavourPage) {
        List<PostFavour> postFavourList = postFavourPage.getRecords();
        Page<PostFavourVO> postFavourVOPage = new Page<>(postFavourPage.getCurrent(), postFavourPage.getSize(), postFavourPage.getTotal());
        if (CollUtil.isEmpty(postFavourList)) {
            return postFavourVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PostFavourVO> postFavourVOList = postFavourList.stream().map(PostFavourVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postFavourList.stream().map(PostFavour::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        postFavourVOList.forEach(postFavourVO -> {
            Long userId = postFavourVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postFavourVO.setUser(userService.getUserVO(user));
        });
        // endregion

        postFavourVOPage.setRecords(postFavourVOList);

        return postFavourVOPage;
    }

}
