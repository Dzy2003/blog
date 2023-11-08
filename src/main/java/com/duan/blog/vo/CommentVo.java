package com.duan.blog.vo;

import com.duan.blog.dto.UserDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author 白日
 * @date Created in 2023/10/2 10:06
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentVo extends ReplyVo{
    private List<CommentVo> children;

    private Long childrenCount;
}
