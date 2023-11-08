package com.duan.blog.vo;

import com.duan.blog.dto.UserDTO;
import lombok.Data;

import java.util.List;

/**
 * @author 白日
 * @create 2023/11/8 21:02
 */
@Data
public class ReplyVo {
    private Long id;

    private UserDTO author;

    private String content;

    private String createDate;

    private UserDTO toUser;

    private Integer level;
}
