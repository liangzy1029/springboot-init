package com.lzy.springbootinit.model.vo;

import com.lzy.springbootinit.model.entity.PostFavour;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子收藏视图
 */
@Data
public class PostFavourVO implements Serializable {

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

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param postFavourVO
     * @return
     */
    public static PostFavour voToObj(PostFavourVO postFavourVO) {
        if (postFavourVO == null) {
            return null;
        }
        PostFavour postFavour = new PostFavour();
        BeanUtils.copyProperties(postFavourVO, postFavour);
        return postFavour;
    }

    /**
     * 对象转封装类
     *
     * @param postFavour
     * @return
     */
    public static PostFavourVO objToVo(PostFavour postFavour) {
        if (postFavour == null) {
            return null;
        }
        PostFavourVO postFavourVO = new PostFavourVO();
        BeanUtils.copyProperties(postFavour, postFavourVO);
        return postFavourVO;
    }

}