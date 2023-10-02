package com.duan.blog.pojo;

import lombok.Data;

/**
 * @author 白日
 * @date Created in 2023/10/2 9:46
 */
@Data
public class Comment {
    private Long id;

    private String content;

    private Long createDate;

    private Long articleId;

    private Long authorId;

    private Long parentId;

    private Long toUid;

    private Integer level;
}
