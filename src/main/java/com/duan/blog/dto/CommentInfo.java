package com.duan.blog.dto;

import lombok.Data;

/**
 * @author 白日
 * @since 2023/10/2 17:45
 */
@Data
public class CommentInfo {
    private Long articleId;

    private String content;

    private Long parent = 0L;

    private Long toUserId = 0L;
}
