package com.lzy.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.springbootinit.model.dto.postfavour.PostFavourQueryRequest;
import com.lzy.springbootinit.model.entity.PostFavour;
import com.lzy.springbootinit.model.vo.PostFavourVO;

/**
 * 帖子收藏服务
 */
public interface PostFavourService extends IService<PostFavour> {

    /**
     * 校验数据
     *
     * @param postFavour
     * @param add        对创建的数据进行校验
     */
    void validPostFavour(PostFavour postFavour, boolean add);

    /**
     * 获取查询条件
     *
     * @param postFavourQueryRequest
     * @return
     */
    QueryWrapper<PostFavour> getQueryWrapper(PostFavourQueryRequest postFavourQueryRequest);

    /**
     * 获取应用封装
     *
     * @param postFavour
     * @return
     */
    PostFavourVO getPostFavourVO(PostFavour postFavour);

    /**
     * 分页获取应用封装
     *
     * @param postFavourPage
     * @return
     */
    Page<PostFavourVO> getPostFavourVOPage(Page<PostFavour> postFavourPage);

}
