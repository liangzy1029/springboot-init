package com.lzy.springbootinit.model.dto.postfavour;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新帖子收藏请求
 */
@Data
public class PostFavourUpdateRequest implements Serializable {

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
