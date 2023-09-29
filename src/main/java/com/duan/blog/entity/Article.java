package com.duan.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import static com.duan.blog.utils.SystemConstants.ARTICLE_COMMON;

@Data
@TableName("tb_article")
public class Article {

    private Long id;

    private String title;

    private String summary;

    private int commentCounts;

    private int viewCounts;

    /**
     * 作者id
     */
    private Long authorId;
    /**
     * 内容id
     */
    private Long bodyId;
    /**
     *类别id
     */
    private Long categoryId;

    /**
     * 置顶
     */
    private int weight = ARTICLE_COMMON;


    /**
     * 创建时间
     */
    private Long createDate;
}
