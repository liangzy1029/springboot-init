package com.lzy.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.springbootinit.model.dto.post.PostQueryRequest;
import com.lzy.springbootinit.model.entity.Post;
import com.lzy.springbootinit.model.vo.PostVO;

/**
 * 帖子服务
 */
public interface PostService extends IService<Post> {

    /**
     * 校验数据
     *
     * @param post
     * @param add  对创建的数据进行校验
     */
    void validPost(Post post, boolean add);

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 获取应用封装
     *
     * @param post
     * @return
     */
    PostVO getPostVO(Post post);

    /**
     * 分页获取应用封装
     *
     * @param postPage
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage);

}
