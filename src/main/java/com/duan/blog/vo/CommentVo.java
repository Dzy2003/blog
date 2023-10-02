package com.duan.blog.vo;

import com.duan.blog.dto.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @author 白日
 * @date Created in 2023/10/2 10:06
 */
@Data
public class CommentVo {
    private Long id;

    private UserDTO author;

    private String content;

    private List<CommentVo> childrens;

    private String createDate;

    private Integer level;

    private UserDTO toUser;
}
