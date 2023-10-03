package com.duan.blog.dto;

import lombok.Data;

/**
 * @author 白日
 * @date Created in 2023/10/2 17:45
 */
@Data
public class CommentInfo {
    private Long articleId;

    private String content;

    private Long parent = 0l;

    private Long toUserId = 0l;
}
