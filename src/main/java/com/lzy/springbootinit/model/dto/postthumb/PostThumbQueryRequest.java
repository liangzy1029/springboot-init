package com.lzy.springbootinit.model.dto.postthumb;

import com.lzy.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询帖子点赞请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostThumbQueryRequest extends PageRequest implements Serializable {

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
