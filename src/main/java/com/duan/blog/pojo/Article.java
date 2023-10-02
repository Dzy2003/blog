package com.duan.blog.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import static com.duan.blog.utils.SystemConstants.ARTICLE_COMMON;

@Data
@TableName("tb_article")
public class Article {

    private Long id;

    private String title;

    private String summary;

    private Integer commentCounts;

    private Integer viewCounts;

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
    private Integer weight = ARTICLE_COMMON;


    /**
     * 创建时间
     */
    private Long createDate;
}
