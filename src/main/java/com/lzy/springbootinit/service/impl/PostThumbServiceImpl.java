package com.lzy.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.mapper.PostThumbMapper;
import com.lzy.springbootinit.model.dto.postthumb.PostThumbQueryRequest;
import com.lzy.springbootinit.model.entity.PostThumb;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.vo.PostThumbVO;
import com.lzy.springbootinit.model.vo.UserVO;
import com.lzy.springbootinit.service.PostThumbService;
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
 * 帖子点赞服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb> implements PostThumbService {

    private final UserService userService;

    /**
     * 校验数据
     *
     * @param postThumb
     * @param add       对创建的数据进行校验
     */
    @Override
    public void validPostThumb(PostThumb postThumb, boolean add) {
        ThrowUtils.throwIf(postThumb == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long postId = postThumb.getPostId();
        Long userId = postThumb.getUserId();

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
     * @param postThumbQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<PostThumb> getQueryWrapper(PostThumbQueryRequest postThumbQueryRequest) {
        QueryWrapper<PostThumb> queryWrapper = new QueryWrapper<>();
        if (postThumbQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = postThumbQueryRequest.getId();
        Long postId = postThumbQueryRequest.getPostId();
        Long userId = postThumbQueryRequest.getUserId();
        String sortField = postThumbQueryRequest.getSortField();
        String sortOrder = postThumbQueryRequest.getSortOrder();

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
     * 获取帖子点赞封装
     *
     * @param postThumb
     * @return
     */
    @Override
    public PostThumbVO getPostThumbVO(PostThumb postThumb) {
        // 对象转封装类
        PostThumbVO postThumbVO = PostThumbVO.objToVo(postThumb);
        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = postThumb.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postThumbVO.setUser(userVO);
        // endregion

        return postThumbVO;
    }

    /**
     * 分页获取帖子点赞封装
     *
     * @param postThumbPage
     * @return
     */
    @Override
    public Page<PostThumbVO> getPostThumbVOPage(Page<PostThumb> postThumbPage) {
        List<PostThumb> postThumbList = postThumbPage.getRecords();
        Page<PostThumbVO> postThumbVOPage = new Page<>(postThumbPage.getCurrent(), postThumbPage.getSize(), postThumbPage.getTotal());
        if (CollUtil.isEmpty(postThumbList)) {
            return postThumbVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PostThumbVO> postThumbVOList = postThumbList.stream().map(PostThumbVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postThumbList.stream().map(PostThumb::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        postThumbVOList.forEach(postThumbVO -> {
            Long userId = postThumbVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postThumbVO.setUser(userService.getUserVO(user));
        });
        // endregion

        postThumbVOPage.setRecords(postThumbVOList);

        return postThumbVOPage;
    }

}
