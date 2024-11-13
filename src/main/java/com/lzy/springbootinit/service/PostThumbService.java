package com.lzy.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lzy.springbootinit.model.dto.postthumb.PostThumbQueryRequest;
import com.lzy.springbootinit.model.entity.PostThumb;
import com.lzy.springbootinit.model.vo.PostThumbVO;

/**
 * 帖子点赞服务
 */
public interface PostThumbService extends IService<PostThumb> {

    /**
     * 校验数据
     *
     * @param postThumb
     * @param add       对创建的数据进行校验
     */
    void validPostThumb(PostThumb postThumb, boolean add);

    /**
     * 获取查询条件
     *
     * @param postThumbQueryRequest
     * @return
     */
    QueryWrapper<PostThumb> getQueryWrapper(PostThumbQueryRequest postThumbQueryRequest);

    /**
     * 获取应用封装
     *
     * @param postThumb
     * @return
     */
    PostThumbVO getPostThumbVO(PostThumb postThumb);

    /**
     * 分页获取应用封装
     *
     * @param postThumbPage
     * @return
     */
    Page<PostThumbVO> getPostThumbVOPage(Page<PostThumb> postThumbPage);

}
