package com.duan.blog.dto;

import lombok.Data;

/**
 * @author 白日
 * @since 2023/10/2 17:45
 */
@Data
public class CommentInfo {
    // 文章ID
    private Long articleId;
    // 评论内容
    private String content;
    // 父评论ID
    private Long parent = 0L;
    // 回复用户ID
    private Long toUserId = 0L;
}
