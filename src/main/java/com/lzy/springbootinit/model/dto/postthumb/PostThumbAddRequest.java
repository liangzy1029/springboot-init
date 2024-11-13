package com.lzy.springbootinit.model.dto.postthumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建帖子点赞请求
 */
@Data
public class PostThumbAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建用户 id
     */
    private Long userId;

}
