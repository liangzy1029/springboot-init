package com.lzy.springbootinit.model.dto.postthumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新帖子点赞请求
 */
@Data
public class PostThumbUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建用户 id
     */
    private Long userId;

}
