package com.lzy.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lzy.springbootinit.common.ErrorCode;
import com.lzy.springbootinit.constant.CommonConstant;
import com.lzy.springbootinit.exception.ThrowUtils;
import com.lzy.springbootinit.mapper.PostMapper;
import com.lzy.springbootinit.model.dto.post.PostQueryRequest;
import com.lzy.springbootinit.model.entity.Post;
import com.lzy.springbootinit.model.entity.User;
import com.lzy.springbootinit.model.vo.PostVO;
import com.lzy.springbootinit.model.vo.UserVO;
import com.lzy.springbootinit.service.PostService;
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
 * 帖子服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final UserService userService;

    /**
     * 校验数据
     *
     * @param post
     * @param add  对创建的数据进行校验
     */
    @Override
    public void validPost(Post post, boolean add) {
        ThrowUtils.throwIf(post == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        Integer thumbNum = post.getThumbNum();
        Integer favourNum = post.getFavourNum();

        // 创建数据时，参数不能为空
        if (add) {
            // 补充校验规则
            ThrowUtils.throwIf(StrUtil.isBlank(title), ErrorCode.PARAMS_ERROR, "帖子标题不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(content), ErrorCode.PARAMS_ERROR, "帖子内容不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(tags), ErrorCode.PARAMS_ERROR, "帖子标签不能为空");
        }
        // 修改数据时，有参数则校验
        // 补充校验规则
        if (StrUtil.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "帖子名称要小于 80");
        }
        if (thumbNum != null) {
            ThrowUtils.throwIf(thumbNum < 0, ErrorCode.PARAMS_ERROR, "帖子点赞数不能小于 0");
        }
        if (favourNum != null) {
            ThrowUtils.throwIf(favourNum < 0, ErrorCode.PARAMS_ERROR, "帖子收藏数不能小于 0");
        }

    }

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTagList();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();

        // 补充需要的查询条件
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(
                    qw -> qw.like("title", searchText)
                            .or().like("content", searchText)
            );
        }
        // 模糊查询
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(content), "content", content);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.ne(ObjUtil.isNotEmpty(notId), "id", notId);
        queryWrapper.ne(ObjUtil.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(
                SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField
        );
        return queryWrapper;
    }

    /**
     * 获取帖子封装
     *
     * @param post
     * @return
     */
    @Override
    public PostVO getPostVO(Post post) {
        // 对象转封装类
        PostVO postVO = PostVO.objToVo(post);
        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // endregion
        return postVO;
    }

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @return
     */
    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollUtil.isEmpty(postList)) {
            return postVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PostVO> postVOList = postList.stream().map(PostVO::objToVo).collect(Collectors.toList());

        // 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        postVOList.forEach(postVO -> {
            Long userId = postVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
        });
        // endregion

        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

}
