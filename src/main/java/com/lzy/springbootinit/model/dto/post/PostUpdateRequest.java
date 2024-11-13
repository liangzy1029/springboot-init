package com.lzy.springbootinit.model.dto.post;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新帖子请求
 */
@Data
public class PostUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tagList;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;

}
