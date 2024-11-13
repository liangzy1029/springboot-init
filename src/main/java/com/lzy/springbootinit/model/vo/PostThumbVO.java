package com.lzy.springbootinit.model.vo;

import com.lzy.springbootinit.model.entity.PostThumb;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子点赞视图
 */
@Data
public class PostThumbVO implements Serializable {

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
     * @param postThumbVO
     * @return
     */
    public static PostThumb voToObj(PostThumbVO postThumbVO) {
        if (postThumbVO == null) {
            return null;
        }
        PostThumb postThumb = new PostThumb();
        BeanUtils.copyProperties(postThumbVO, postThumb);
        return postThumb;
    }

    /**
     * 对象转封装类
     *
     * @param postThumb
     * @return
     */
    public static PostThumbVO objToVo(PostThumb postThumb) {
        if (postThumb == null) {
            return null;
        }
        PostThumbVO postThumbVO = new PostThumbVO();
        BeanUtils.copyProperties(postThumb, postThumbVO);
        return postThumbVO;
    }

}