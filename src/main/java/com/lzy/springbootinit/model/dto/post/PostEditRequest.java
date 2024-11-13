package com.lzy.springbootinit.model.dto.post;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 编辑帖子请求
 */
@Data
public class PostEditRequest implements Serializable {

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
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;

}
