package com.duan.blog.vo;

import com.duan.blog.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class ArticleVo {
    private Long id;

    private String title;

    private String summary;

    private int commentCounts;

    private int viewCounts;

    private int weight;
    /**
     * 创建时间
     */
    private String createDate;

    private String author;

    private List<Tag> tags;

    //private List<CategoryVo> categorys;

}
